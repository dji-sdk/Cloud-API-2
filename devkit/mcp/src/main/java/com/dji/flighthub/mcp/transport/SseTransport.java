/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.flighthub.mcp.transport;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpPromptRegistry;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.elicit.ClientBridge;
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.elicit.ElicitResult;
import com.dji.flighthub.mcp.elicit.ElicitationRegistry;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SSE 传输层 — 通过 HTTP Server-Sent Events 暴露 MCP 协议。
 * 用于远程 AI Agent 平台集成。
 *
 * <p>端点：
 * <ul>
 *   <li>GET /sse — SSE 响应流</li>
 *   <li>POST /message — 发送 JSON-RPC 请求</li>
 * </ul>
 *
 * <p>支持 server→client 反向请求（elicitation）：L1+ 危险操作在独立工作线程上执行，
 * 执行期间可通过 SSE 向 client 发起 {@code elicitation/create} 确认请求，
 * 阻塞等待 client POST 回的真人选择后继续执行。
 */
@RestController
@Profile("sse")
public class SseTransport {

    private static final Logger log = LoggerFactory.getLogger(SseTransport.class);
    private static final String MCP_PROTOCOL_VERSION = "2025-06-18";
    private static final String SERVER_NAME = "dji-cloud-api2";
    private static final String SERVER_VERSION = "1.0.0";

    /** JSON-RPC 错误码：Grace period 已过期，需重新 fh_connect */
    private static final int ERROR_GRACE_PERIOD_EXPIRED = -32001;
    /** JSON-RPC 错误码：未知的 previousClientId（从未连接或已清理） */
    private static final int ERROR_UNKNOWN_PREVIOUS_CLIENT = -32002;
    /** JSON-RPC 错误码：方法不存在 */
    private static final int ERROR_METHOD_NOT_FOUND = -32601;
    /** JSON-RPC 错误码：缺少必填参数 */
    private static final int ERROR_INVALID_PARAMS = -32602;

    /** elicitation 反向请求方法名（MCP 协议约定） */
    private static final String METHOD_ELICITATION_CREATE = "elicitation/create";
    /** 反向请求 id 前缀，避免与 client 正向请求 id 冲突 */
    private static final String REVERSE_ID_PREFIX = "fh-elicit-";

    /** tool 执行线程池核心线程数 */
    private static final int TOOL_CORE_THREADS = 2;
    /** tool 执行线程池最大线程数 */
    private static final int MAX_TOOL_THREADS = 8;
    /** tool 执行线程池空闲线程存活时间（秒） */
    private static final long TOOL_THREAD_KEEPALIVE_SECONDS = 60L;
    /** tool 执行线程池等待队列容量 */
    private static final int TOOL_QUEUE_CAPACITY = 64;

    private final McpToolRegistry toolRegistry;
    private final McpPromptRegistry promptRegistry;
    private final ObjectMapper objectMapper;
    private final ClientRegistry clientRegistry;
    private final SessionManager sessionManager;
    private final FlightHubProperties properties;
    /** 对外 base-path 前缀（如 /pf-devkit/ai-plugin/mcp），用于 endpoint 事件下发绝对路径 */
    private final String basePath;

    /** elicitation 反向请求关联登记表 */
    private final ElicitationRegistry elicitationRegistry = new ElicitationRegistry();
    /** 反向请求 id 自增计数 */
    private final AtomicLong reverseIdSeq = new AtomicLong();
    /** 每个 client 是否声明了 elicitation 能力 */
    private final Map<String, Boolean> clientElicitationSupport = new ConcurrentHashMap<>();

    /** L1+ 危险工具执行线程池（有界，防止失控增长） */
    private final ExecutorService toolExecutor =
            new ThreadPoolExecutor(TOOL_CORE_THREADS, MAX_TOOL_THREADS,
                    TOOL_THREAD_KEEPALIVE_SECONDS, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(TOOL_QUEUE_CAPACITY),
                    runnable -> {
                        Thread thread = new Thread(runnable, "sse-tool-worker");
                        thread.setDaemon(true);
                        return thread;
                    });

    /** 超时调度器 — 单线程，仅负责触发 cancel，不占用工作线程 */
    private final ScheduledExecutorService timeoutScheduler = new ScheduledThreadPoolExecutor(1, runnable -> {
        Thread thread = new Thread(runnable, "sse-tool-timeout");
        thread.setDaemon(true);
        return thread;
    });

    public SseTransport(McpToolRegistry toolRegistry, McpPromptRegistry promptRegistry,
                        ObjectMapper objectMapper, ClientRegistry clientRegistry,
                        SessionManager sessionManager, FlightHubProperties properties,
                        @Value("${spring.webflux.base-path:}") String basePath) {
        this.toolRegistry = toolRegistry;
        this.promptRegistry = promptRegistry;
        this.objectMapper = objectMapper;
        this.clientRegistry = clientRegistry;
        this.sessionManager = sessionManager;
        this.properties = properties;
        this.basePath = normalizeBasePath(basePath);
    }

    /**
     * 规范化 base-path：去除尾部斜杠，保证拼接时路径分隔符唯一。
     *
     * @param raw 原始配置值
     * @return 规范化后的 base-path，空串表示无前缀
     */
    private static String normalizeBasePath(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String trimmed = raw.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

    /**
     * 优雅关闭 tool 执行线程池，等待 in-flight 任务完成（防止 SDK 资源泄漏）。
     */
    @jakarta.annotation.PreDestroy
    void shutdown() {
        toolExecutor.shutdown();
        try {
            if (!toolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("toolExecutor 等待超时，强制终止 in-flight 任务");
                toolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            toolExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * SSE 端点 — 客户端连接此端点接收 MCP 响应。
     *
     * @param authorization Bearer Token（用于身份标识，重连时校验一致性）
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> connect(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            org.springframework.web.server.ServerWebExchange exchange) {
        String clientId = UUID.randomUUID().toString().substring(0, 8);
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        clientRegistry.register(clientId, sink);

        // 记录连接时的身份标识（用于重连时校验防劫持）
        String tokenHash = hashToken(authorization);
        if (tokenHash != null) {
            ClientRegistry.ClientEntry entry = clientRegistry.getClient(clientId);
            if (entry != null) {
                entry.setTokenHash(tokenHash);
            }
        }

        // JWT 模式下从认证结果提取 userId；非 JWT 模式不设置 userId（保持 null）
        Object userId = exchange.getAttribute("fh.userId");
        if (userId != null) {
            ClientRegistry.ClientEntry entry = clientRegistry.getClient(clientId);
            if (entry != null) {
                entry.setUserId(userId.toString());
            }
        }

        log.info("SSE client connected: {}", clientId);

        // 发送 endpoint 事件，告知客户端 POST 消息的地址
        // MCP 协议要求 data 为纯路径字符串（非 JSON），客户端使用 new URL(data, baseUrl) 解析
        // base-path 存在时必须下发绝对路径：相对路径会让 new URL(data, baseUrl) 丢弃 baseUrl 的 path，
        // 导致 POST 落到无前缀的 /message/{clientId} 而 404
        String endpointEvent = String.format("%s/message/%s", basePath, clientId);
        sink.tryEmitNext(ServerSentEvent.<String>builder()
                .event("endpoint")
                .data(endpointEvent)
                .build());

        return sink.asFlux()
                .doFinally(signalType -> {
                    clientElicitationSupport.remove(clientId);
                    String sessionId = clientRegistry.disconnect(clientId);
                    if (sessionId != null) {
                        sessionManager.enterGracePeriod(sessionId);
                        log.info("SSE client disconnected, session 进入 grace period: clientId={}, sessionId={}, signal={}",
                                clientId, sessionId, signalType);
                    } else {
                        log.info("SSE client disconnected (无 session 绑定): {} ({})", clientId, signalType);
                    }
                });
    }

    /** HTTP 请求头：客户端 ID 回退标识 */
    private static final String HEADER_X_CLIENT_ID = "X-Client-Id";
    /** HTTP 请求头：MCP Session ID（MCP 协议标准头） */
    private static final String HEADER_MCP_SESSION_ID = "Mcp-Session-Id";

    /**
     * 消息端点（路径参数版本）— 推荐方式，避免部分客户端丢失 query string。
     *
     * <p>适配 Claude Code ≤2.1.196 等客户端的 SSE transport bug：
     * 客户端收到 endpoint 事件后 POST 时丢失了 URL query string，
     * 使用路径参数 `/message/{clientId}` 可彻底规避此问题。
     *
     * @param clientId 路径参数中的客户端唯一标识
     * @param request  JSON-RPC 请求体
     * @return HTTP 同步响应（实际 JSON-RPC 响应通过 SSE 异步推送）
     */
    @PostMapping(value = "/message/{clientId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> messageByPath(
            @PathVariable String clientId,
            @RequestBody Map<String, Object> request
    ) {
        return doMessage(clientId, request);
    }

    /**
     * 消息端点（query 参数版本）— 保留向后兼容。
     *
     * <p>当 clientId query 参数缺失时，按以下优先级回退解析：
     * <ol>
     *   <li>X-Client-Id 请求头</li>
     *   <li>Mcp-Session-Id 请求头（通过 session 反查 clientId）</li>
     *   <li>单客户端自动推断（当前仅 1 个活跃客户端时直接使用）</li>
     * </ol>
     *
     * @param clientId      query 参数中的客户端标识（可选）
     * @param xClientId     X-Client-Id 请求头（回退方式 1）
     * @param mcpSessionId  Mcp-Session-Id 请求头（回退方式 2）
     * @param request       JSON-RPC 请求体
     * @return HTTP 同步响应
     */
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> message(
            @RequestParam(required = false) String clientId,
            @RequestHeader(value = HEADER_X_CLIENT_ID, required = false) String xClientId,
            @RequestHeader(value = HEADER_MCP_SESSION_ID, required = false) String mcpSessionId,
            @RequestBody Map<String, Object> request
    ) {
        String resolvedClientId = resolveClientId(clientId, xClientId, mcpSessionId);
        if (resolvedClientId == null) {
            log.warn("POST /message 无法解析 clientId: 缺少 query param、header 且活跃客户端数 ≠ 1");
            return Map.of("error", "Missing clientId. Provide via path (/message/{clientId}), "
                    + "query param (?clientId=xxx), or X-Client-Id header.");
        }
        return doMessage(resolvedClientId, request);
    }

    /**
     * 消息处理核心逻辑 — 路径参数和 query 参数两种端点共享。
     *
     * @param clientId 已解析的客户端唯一标识
     * @param request  JSON-RPC 请求体
     * @return HTTP 同步响应
     */
    private Map<String, Object> doMessage(String clientId, Map<String, Object> request) {
        Sinks.Many<ServerSentEvent<String>> sink = clientRegistry.getSink(clientId);
        if (sink == null) {
            return Map.of("error", "Unknown client. Please connect to /sse first.");
        }

        // 刷新客户端活跃时间（用于心跳超时检测）
        clientRegistry.touchClient(clientId);

        Map<String, Object> response = handleRequest(clientId, request);

        // 通过 SSE 发送响应
        if (response != null) {
            try {
                String json = objectMapper.writeValueAsString(response);
                sink.tryEmitNext(ServerSentEvent.<String>builder()
                        .event("message")
                        .data(json)
                        .build());
            } catch (Exception e) {
                log.error("Failed to serialize SSE response", e);
            }
        }

        return Map.of("status", "accepted");
    }

    /**
     * clientId 多源解析 — 按优先级回退。
     *
     * <p>解析优先级：
     * <ol>
     *   <li>query param（标准 MCP SSE 方式）</li>
     *   <li>X-Client-Id 请求头（适配丢失 query string 的客户端）</li>
     *   <li>Mcp-Session-Id 请求头反查（通过已绑定的 session 找到对应 clientId）</li>
     *   <li>单客户端自动推断（仅 1 个活跃连接时直接使用，适用于单 session 场景）</li>
     * </ol>
     *
     * @param queryParam   来自 URL query 的 clientId
     * @param xClientId    来自 X-Client-Id 请求头的 clientId
     * @param mcpSessionId 来自 Mcp-Session-Id 请求头的 session ID
     * @return 解析到的 clientId，全部失败返回 null
     */
    private String resolveClientId(String queryParam, String xClientId, String mcpSessionId) {
        // 优先级 1: query param（标准路径）
        if (queryParam != null && !queryParam.isBlank()) {
            return queryParam;
        }

        // 优先级 2: X-Client-Id 请求头
        if (xClientId != null && !xClientId.isBlank()) {
            log.debug("clientId 通过 X-Client-Id header 回退解析: {}", xClientId);
            return xClientId;
        }

        // 优先级 3: Mcp-Session-Id 请求头反查
        if (mcpSessionId != null && !mcpSessionId.isBlank()) {
            String resolved = clientRegistry.findClientBySessionId(mcpSessionId);
            if (resolved != null) {
                log.debug("clientId 通过 Mcp-Session-Id header 反查解析: sessionId={} → clientId={}",
                        mcpSessionId, resolved);
                return resolved;
            }
        }

        // 优先级 4: 单客户端自动推断
        List<String> activeIds = clientRegistry.getActiveClientIds();
        if (activeIds.size() == 1) {
            String singleClient = activeIds.get(0);
            log.debug("clientId 通过单客户端自动推断: {}", singleClient);
            return singleClient;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleRequest(String clientId, Map<String, Object> request) {
        String method = (String) request.get("method");
        Object id = request.get("id");
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());

        // 优先检测是否为 elicitation 反向请求的响应
        if (tryHandleElicitationResponse(id, request)) {
            return null; // 已唤醒等待线程，无需 SSE 推送
        }

        log.debug("SSE request: method={}, id={}", method, id);

        return switch (method) {
            case "initialize" -> handleInitialize(id, params, clientId);
            case "initialized" -> null;
            case "tools/list" -> handleToolsList(id);
            case "tools/call" -> handleToolsCall(id, params, clientId);
            case "prompts/list" -> handlePromptsList(id);
            case "prompts/get" -> handlePromptsGet(id, params);
            case "reconnect" -> handleReconnect(id, params, clientId);
            case "ping" -> createSuccessResponse(id, Map.of());
            default -> createErrorResponse(id, ERROR_METHOD_NOT_FOUND, "Method not found: " + method);
        };
    }

    /**
     * 处理 initialize：探测 client 的 elicitation 能力并声明 server 能力。
     *
     * @param id       请求 id
     * @param params   请求参数（含 client capabilities）
     * @param clientId 客户端标识
     * @return initialize 响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleInitialize(Object id, Map<String, Object> params, String clientId) {
        boolean supportsElicitation = detectElicitationCapability(params);
        clientElicitationSupport.put(clientId, supportsElicitation);
        log.info("SSE client initialize: clientId={}, elicitation={}", clientId, supportsElicitation);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", MCP_PROTOCOL_VERSION);
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", Map.of("listChanged", false));
        capabilities.put("prompts", Map.of("listChanged", false));
        // 声明 server 支持 elicitation（告知 client 可接收反向确认请求）
        capabilities.put("elicitation", Map.of());
        result.put("capabilities", capabilities);
        result.put("serverInfo", Map.of("name", SERVER_NAME, "version", SERVER_VERSION));
        return createSuccessResponse(id, result);
    }

    private Map<String, Object> handleToolsList(Object id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tools", toolRegistry.getToolDefinitions());
        return createSuccessResponse(id, result);
    }

    // ==================== Prompts 协议处理 ====================

    private Map<String, Object> handlePromptsList(Object id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("prompts", promptRegistry.getPromptList());
        return createSuccessResponse(id, result);
    }

    private Map<String, Object> handlePromptsGet(Object id, Map<String, Object> params) {
        try {
            Map<String, Object> result = promptRegistry.handleGetRequest(params);
            return createSuccessResponse(id, result);
        } catch (McpPromptRegistry.PromptRequestException e) {
            return createErrorResponse(id, ERROR_INVALID_PARAMS, e.getMessage());
        }
    }

    /**
     * 处理 tools/call 请求。
     *
     * <p>L0 安全工具：同步执行，立即返回结果（快，无需线程切换）。
     * <p>L1+ 危险工具：提交到 toolExecutor 异步执行（支持 elicitation 阻塞等待），
     * 返回 null（结果通过 SSE 异步推送）。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolsCall(Object id, Map<String, Object> params, String clientId) {
        String toolName = (String) params.get("name");
        Object rawArgs = params.getOrDefault("arguments", Map.of());
        Map<String, Object> arguments = (rawArgs instanceof Map) ? (Map<String, Object>) rawArgs : Map.of();

        // L1+ 危险工具走异步路径（支持 elicitation 阻塞等待）
        if (toolRegistry.isHumanConfirmRequired(toolName)) {
            executeToolAsync(id, toolName, arguments, clientId);
            return null; // 结果通过 SSE 异步推送
        }

        // L0 安全工具：同步执行
        log.info("SSE tool call: name={}, id={}, client={}", toolName, id, clientId);
        return executeToolSync(id, toolName, arguments, clientId);
    }

    /**
     * 同步执行 tool（L0 安全工具，无需 elicitation 通道）。
     * 提交到 toolExecutor 并使用 Future.get(timeout) 做超时保护。
     */
    private Map<String, Object> executeToolSync(Object id, String toolName,
                                                 Map<String, Object> arguments, String clientId) {
        int timeoutSeconds = properties.getTransport().getToolTimeoutSeconds();
        Future<Map<String, Object>> future;
        try {
            future = toolExecutor.submit(() -> {
                RequestContext.set(id, toolName, clientId);
                enrichRequestContextFromClient(clientId);
                try {
                    return toolRegistry.executeTool(toolName, arguments);
                } finally {
                    RequestContext.clear();
                }
            });
        } catch (java.util.concurrent.RejectedExecutionException e) {
            log.warn("Tool executor saturated (sync), rejecting: tool={}, id={}", toolName, id);
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("success", false);
            errorResult.put("error_message", "Server busy — too many concurrent tool calls. Please retry later.");
            return buildToolResponse(id, errorResult);
        }
        try {
            Map<String, Object> toolResult = future.get(timeoutSeconds, TimeUnit.SECONDS);
            return buildToolResponse(id, toolResult);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            log.error("Tool execution timeout ({}s): name={}, client={}", timeoutSeconds, toolName, clientId);
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("success", false);
            errorResult.put("error_message", "Tool execution timeout (" + timeoutSeconds + "s). "
                    + "The operation took too long. Please retry or check device connectivity.");
            return buildToolResponse(id, errorResult);
        } catch (Exception e) {
            Throwable cause = (e instanceof java.util.concurrent.ExecutionException) ? e.getCause() : e;
            log.error("Tool execution error: name={}, error={}", toolName, cause.getMessage(), cause);
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("success", false);
            errorResult.put("error_message", "Internal error: " + cause.getMessage());
            return buildToolResponse(id, errorResult);
        }
    }

    /**
     * 异步执行 tool（L1+ 危险工具）— 提交到工作线程，绑定 ClientBridge 支持 elicitation。
     *
     * <p>执行流程：
     * <ol>
     *   <li>提交到 toolExecutor 工作线程</li>
     *   <li>绑定 per-client 的 {@link SseClientBridge}（用于 elicitation 反向请求）</li>
     *   <li>tool 内部按需调用 {@code requireHumanConfirm()} → 通过 bridge 发起确认</li>
     *   <li>执行完成后通过 SSE 推送结果</li>
     * </ol>
     */
    private void executeToolAsync(Object id, String toolName, Map<String, Object> arguments, String clientId) {
        log.info("SSE async tool call (L1+): name={}, id={}, client={}", toolName, id, clientId);
        int timeoutSeconds = properties.getTransport().getToolTimeoutSeconds();
        AtomicBoolean responded = new AtomicBoolean(false);
        try {
            Future<?> future = toolExecutor.submit(() -> {
                SseClientBridge bridge = new SseClientBridge(clientId);
                ClientBridgeHolder.set(bridge);
                RequestContext.set(id, toolName, clientId);
                enrichRequestContextFromClient(clientId);
                try {
                    Map<String, Object> toolResult = toolRegistry.executeTool(toolName, arguments);
                    if (responded.compareAndSet(false, true)) {
                        emitResponse(clientId, buildToolResponse(id, toolResult));
                    }
                } catch (Exception e) {
                    log.error("Async tool execution error: name={}, error={}", toolName, e.getMessage(), e);
                    if (responded.compareAndSet(false, true)) {
                        Map<String, Object> errorResult = new LinkedHashMap<>();
                        errorResult.put("success", false);
                        errorResult.put("error_message", "Internal error: " + e.getMessage());
                        emitResponse(clientId, buildToolResponse(id, errorResult));
                    }
                } finally {
                    ClientBridgeHolder.clear();
                    RequestContext.clear();
                }
            });
            // 超时守护：使用 ScheduledExecutorService，不占工作线程
            timeoutScheduler.schedule(() -> {
                if (responded.compareAndSet(false, true)) {
                    future.cancel(true);
                    log.error("Async tool execution timeout ({}s): name={}, client={}", timeoutSeconds, toolName, clientId);
                    Map<String, Object> errorResult = new LinkedHashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("error_message", "Tool execution timeout (" + timeoutSeconds + "s). "
                            + "The operation took too long. Please retry or check device connectivity.");
                    emitResponse(clientId, buildToolResponse(id, errorResult));
                }
            }, timeoutSeconds, TimeUnit.SECONDS);
        } catch (java.util.concurrent.RejectedExecutionException e) {
            log.warn("Tool executor saturated, rejecting: tool={}, id={}", toolName, id);
            if (responded.compareAndSet(false, true)) {
                Map<String, Object> errorResult = new LinkedHashMap<>();
                errorResult.put("success", false);
                errorResult.put("error_message", "Server busy — too many concurrent operations. Please retry later.");
                emitResponse(clientId, buildToolResponse(id, errorResult));
            }
        }
    }

    /**
     * 从 ClientRegistry 中提取当前客户端的 sessionId 和 userId，
     * 主动设置到 RequestContext 中（多 session 路由核心 — eager set 模式）。
     */
    private void enrichRequestContextFromClient(String clientId) {
        if (clientRegistry == null) {
            return;
        }
        ClientRegistry.ClientEntry entry = clientRegistry.getClient(clientId);
        if (entry == null) {
            return;
        }
        RequestContext ctx = RequestContext.current();
        if (ctx == null) {
            return;
        }
        if (entry.getSessionId() != null) {
            ctx.setSessionId(entry.getSessionId());
        }
        if (entry.getUserId() != null) {
            ctx.setUserId(entry.getUserId());
        }
    }

    /**
     * 构建 MCP tool 响应结构。
     */
    private Map<String, Object> buildToolResponse(Object id, Map<String, Object> toolResult) {
        String resultText;
        try {
            resultText = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(toolResult);
        } catch (Exception e) {
            resultText = toolResult.toString();
        }

        boolean isError = !Boolean.TRUE.equals(toolResult.get("success"));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", List.of(Map.of("type", "text", "text", resultText)));
        result.put("isError", isError);
        return createSuccessResponse(id, result);
    }

    /**
     * 通过 SSE 向指定客户端推送响应。
     */
    private void emitResponse(String clientId, Map<String, Object> response) {
        Sinks.Many<ServerSentEvent<String>> sink = clientRegistry.getSink(clientId);
        if (sink == null) {
            log.warn("Cannot emit response: client {} already disconnected", clientId);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(response);
            sink.tryEmitNext(ServerSentEvent.<String>builder()
                    .event("message")
                    .data(json)
                    .build());
        } catch (Exception e) {
            log.error("Failed to emit SSE response to client {}: {}", clientId, e.getMessage());
        }
    }

    /**
     * 处理重连请求 — 恢复之前断连的 session。
     *
     * <p>协议：
     * <pre>
     * 请求: {"method":"reconnect","params":{"previousClientId":"abc12345"}}
     * 成功: {"result":{"reconnected":true,"sessionId":"sess_xxx","workspaceName":"...",...}}
     * 错误: {"error":{"code":-32001,"message":"Grace period expired"}}
     *       {"error":{"code":-32002,"message":"Unknown previousClientId"}}
     * </pre>
     */
    private Map<String, Object> handleReconnect(Object id, Map<String, Object> params, String newClientId) {
        String previousClientId = (String) params.get("previousClientId");
        if (previousClientId == null || previousClientId.isBlank()) {
            return createErrorResponse(id, ERROR_INVALID_PARAMS, "Missing required parameter: previousClientId");
        }

        // 获取新连接的身份标识用于校验
        ClientRegistry.ClientEntry newEntry = clientRegistry.getClient(newClientId);
        String newTokenHash = (newEntry != null) ? newEntry.getTokenHash() : null;

        // 原子操作：find + grace 校验 + 身份校验 + 迁移
        int gracePeriodSeconds = properties.getTransport().getGracePeriodSeconds();
        ClientRegistry.ReconnectResult reconnectResult = clientRegistry.tryCompleteReconnect(
                previousClientId, newClientId, gracePeriodSeconds, newTokenHash);

        // 根据结果返回对应响应
        return switch (reconnectResult.getStatus()) {
            case SUCCESS -> buildReconnectSuccessResponse(id, reconnectResult);
            case NOT_FOUND -> {
                log.warn("重连失败，未知 previousClientId: {}", previousClientId);
                yield createErrorResponse(id, ERROR_UNKNOWN_PREVIOUS_CLIENT,
                        "Unknown previousClientId: " + previousClientId + ". Never connected or grace period already expired.");
            }
            case EXPIRED -> {
                log.warn("重连失败，grace period 已过期: previousClientId={}", previousClientId);
                yield createErrorResponse(id, ERROR_GRACE_PERIOD_EXPIRED,
                        "Grace period expired for previousClientId: " + previousClientId + ". Please reconnect with fh_connect.");
            }
            case IDENTITY_MISMATCH -> {
                log.warn("重连失败，身份不匹配: previousClientId={}, newClientId={}", previousClientId, newClientId);
                yield createErrorResponse(id, ERROR_UNKNOWN_PREVIOUS_CLIENT,
                        "Identity mismatch: reconnecting client does not match the original connection.");
            }
            case NEW_CLIENT_NOT_FOUND -> createErrorResponse(id, ERROR_UNKNOWN_PREVIOUS_CLIENT,
                    "Reconnect failed: new client not registered.");
        };
    }

    /**
     * 构建重连成功响应。
     */
    private Map<String, Object> buildReconnectSuccessResponse(Object id, ClientRegistry.ReconnectResult reconnectResult) {
        String sessionId = reconnectResult.getSessionId();

        // 联动 SessionManager：退出 grace period（原子性，防止被 expire 竞态销毁）
        boolean exitSuccess = sessionManager.exitGracePeriod(sessionId);
        if (!exitSuccess) {
            // session 已被 expireGracePeriod 抢先销毁（极端竞态）
            return createErrorResponse(id, ERROR_GRACE_PERIOD_EXPIRED,
                    "Grace period expired during reconnect (race condition). Please reconnect with fh_connect.");
        }

        SessionContext session = sessionManager.getSession(sessionId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reconnected", true);
        result.put("sessionId", sessionId);

        if (session != null) {
            result.put("workspaceName", session.getWorkspaceName());
            ClientRegistry.DisconnectedBinding binding = reconnectResult.getBinding();
            if (binding != null) {
                long graceElapsedSeconds = java.time.Duration.between(
                        binding.getDisconnectedAt(), java.time.Instant.now()).getSeconds();
                result.put("graceElapsedSeconds", graceElapsedSeconds);
            }

            // 控制权状态
            Map<String, Object> controlAuth = new LinkedHashMap<>();
            controlAuth.put("active", session.hasControlAuthority());
            if (session.hasControlAuthority()) {
                controlAuth.put("deviceSn", session.getActiveControlDeviceSn());
            }
            result.put("controlAuthority", controlAuth);

            // DRC 连接状态
            Map<String, Object> drcLink = new LinkedHashMap<>();
            drcLink.put("active", session.hasDrcLink());
            if (session.hasDrcLink()) {
                drcLink.put("deviceSn", session.getActiveDrcDeviceSn());
            }
            result.put("drcLink", drcLink);
        }

        log.info("客户端重连成功: sessionId={}", sessionId);
        return createSuccessResponse(id, result);
    }

    private Map<String, Object> createSuccessResponse(Object id, Map<String, Object> result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        return response;
    }

    private Map<String, Object> createErrorResponse(Object id, int code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }

    // ==================== 身份标识工具方法 ====================

    /** Token hash 截取长度（SHA-256 前 16 个 hex 字符，64-bit 碰撞空间足够） */
    private static final int TOKEN_HASH_LENGTH = 16;

    /**
     * 从 Authorization 头提取 Bearer Token 并计算 SHA-256 hash（截取前 16 字符）。
     * 用于重连时身份校验，防止跨客户端 session 劫持。
     *
     * @param authorization HTTP Authorization 头值（形如 "Bearer xxx"）
     * @return hash 字符串，无 token 时返回 null
     */
    private static String hashToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        String token = authorization.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(TOKEN_HASH_LENGTH);
            for (int i = 0; i < TOKEN_HASH_LENGTH / 2; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 由 JDK 保证可用，不应走到这里
            log.warn("SHA-256 算法不可用，跳过 token hash: {}", e.getMessage());
            return null;
        }
    }

    // ==================== Elicitation 反向请求支持 ====================

    /**
     * 尝试处理 elicitation 反向请求的响应。
     *
     * <p>当 client 收到 {@code elicitation/create} SSE 事件后，将真人选择以 JSON-RPC
     * 响应格式 POST 回来。响应的 id 以 {@value #REVERSE_ID_PREFIX} 开头，借此识别。
     *
     * @param id      请求中的 id 字段
     * @param request 完整请求 Map
     * @return true 表示已识别为 elicitation 响应并完成处理
     */
    @SuppressWarnings("unchecked")
    private boolean tryHandleElicitationResponse(Object id, Map<String, Object> request) {
        if (!(id instanceof String strId) || !strId.startsWith(REVERSE_ID_PREFIX)) {
            return false;
        }

        // 检查是否有对应的待响应 future
        if (!elicitationRegistry.isPending(strId)) {
            log.debug("收到 elicitation 响应但无匹配的待响应请求: id={}", strId);
            return false;
        }

        // 安全解析 result — 防止恶意 client 传非 Map 值导致 ClassCastException
        Object rawResult = request.get("result");
        Map<String, Object> result = rawResult instanceof Map<?, ?> m
                ? (Map<String, Object>) m : null;
        ElicitResult elicitResult = ElicitResult.fromResponse(result);
        elicitationRegistry.complete(strId, elicitResult);
        log.debug("Elicitation 响应已处理: id={}, action={}", strId, elicitResult.action());
        return true;
    }

    /**
     * 从 initialize 参数中探测 client 是否声明 elicitation 能力。
     *
     * @param params initialize 请求参数
     * @return true 表示 client 支持 elicitation
     */
    @SuppressWarnings("unchecked")
    private static boolean detectElicitationCapability(Map<String, Object> params) {
        if (params == null) {
            return false;
        }
        Object capabilities = params.get("capabilities");
        if (!(capabilities instanceof Map<?, ?> capMap)) {
            return false;
        }
        return capMap.containsKey("elicitation");
    }

    // ==================== SseClientBridge（per-client 反向通道） ====================

    /**
     * 每次 L1+ tool 执行时创建的 per-client {@link ClientBridge} 实现。
     *
     * <p>通过指定客户端的 SSE sink 推送 {@code elicitation/create} 事件，
     * 并阻塞等待 client POST 回响应（由 {@link #tryHandleElicitationResponse} 唤醒）。
     */
    private class SseClientBridge implements ClientBridge {

        private final String clientId;

        SseClientBridge(String clientId) {
            this.clientId = clientId;
        }

        @Override
        public boolean isRemote() {
            return true;
        }

        @Override
        public boolean supportsElicitation() {
            return Boolean.TRUE.equals(clientElicitationSupport.get(clientId));
        }

        @Override
        public ElicitResult elicit(String message, Map<String, Object> requestedSchema, long timeoutMillis) {
            Sinks.Many<ServerSentEvent<String>> sink = clientRegistry.getSink(clientId);
            if (sink == null) {
                log.warn("Elicitation 失败: client {} 已断连", clientId);
                return ElicitResult.cancel();
            }

            // 生成唯一请求 id 并登记 future
            String requestId = REVERSE_ID_PREFIX + reverseIdSeq.incrementAndGet();
            CompletableFuture<ElicitResult> future = elicitationRegistry.register(requestId);

            // 构建并推送 elicitation/create 事件
            Map<String, Object> elicitRequest = buildElicitationRequest(requestId, message, requestedSchema);
            try {
                String json = objectMapper.writeValueAsString(elicitRequest);
                Sinks.EmitResult emitResult = sink.tryEmitNext(ServerSentEvent.<String>builder()
                        .event("message")
                        .data(json)
                        .build());

                if (!emitResult.isSuccess()) {
                    log.warn("Elicitation 推送失败: clientId={}, result={}", clientId, emitResult);
                    elicitationRegistry.complete(requestId, ElicitResult.cancel());
                    return ElicitResult.cancel();
                }
            } catch (Exception e) {
                log.error("Elicitation 序列化失败: {}", e.getMessage());
                elicitationRegistry.complete(requestId, ElicitResult.cancel());
                return ElicitResult.cancel();
            }

            log.info("Elicitation 请求已推送，等待真人确认: clientId={}, requestId={}", clientId, requestId);

            // 阻塞等待 client POST 回响应（或超时）
            return elicitationRegistry.await(requestId, future, timeoutMillis);
        }
    }

    /**
     * 构建 elicitation/create 反向请求 JSON-RPC 消息。
     *
     * @param requestId       反向请求 id
     * @param message         提示信息
     * @param requestedSchema 表单 schema
     * @return JSON-RPC 请求 Map
     */
    private static Map<String, Object> buildElicitationRequest(String requestId, String message,
                                                               Map<String, Object> requestedSchema) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("message", message);
        params.put("requestedSchema", requestedSchema == null ? Map.of() : requestedSchema);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", requestId);
        request.put("method", METHOD_ELICITATION_CREATE);
        request.put("params", params);
        return request;
    }
}
