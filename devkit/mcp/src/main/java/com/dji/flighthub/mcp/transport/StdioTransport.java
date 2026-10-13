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

import com.dji.flighthub.mcp.config.McpPromptRegistry;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.elicit.ClientBridge;
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.elicit.ElicitResult;
import com.dji.flighthub.mcp.elicit.ElicitationRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.dji.flighthub.mcp.config.FlightHubProperties;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
 * Stdio 传输层 — 通过 stdin/stdout 收发 JSON-RPC 请求。
 * 实现 MCP 协议的本地 IDE 集成模式。
 *
 * <p>支持 server→client 反向请求（elicitation）：tool 在独立工作线程上执行，
 * reader 主循环区分「client 发来的新请求」与「client 对反向请求的响应」，
 * 后者按 JSON-RPC id 唤醒等待中的 tool 线程。所有 stdout 写入经统一锁串行化，
 * 避免正向响应与反向请求交织破坏 JSON 行。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Profile("stdio")
public class StdioTransport implements CommandLineRunner, ClientBridge {

    private static final Logger log = LoggerFactory.getLogger(StdioTransport.class);

    private static final String MCP_PROTOCOL_VERSION = "2025-06-18";
    private static final String SERVER_NAME = "dji-cloud-api2";
    private static final String SERVER_VERSION = "1.0.0";

    private static final int JSON_RPC_PARSE_ERROR = -32700;
    private static final int JSON_RPC_METHOD_NOT_FOUND = -32601;
    private static final int JSON_RPC_INVALID_PARAMS = -32602;

    /** elicitation 反向请求方法名（MCP 协议约定） */
    private static final String METHOD_ELICITATION_CREATE = "elicitation/create";

    /** 反向请求 id 前缀，避免与 client 正向请求 id 冲突 */
    private static final String REVERSE_ID_PREFIX = "fh-elicit-";

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};

    private final McpToolRegistry toolRegistry;
    private final McpPromptRegistry promptRegistry;
    private final ObjectMapper objectMapper;
    private final FlightHubProperties properties;

    /** stdout 写锁：串行化所有出站 JSON 行 */
    private final Object stdoutLock = new Object();

    /** tool 执行线程池上限：避免无限线程增长，同时允许并发 elicitation */
    private static final int MAX_TOOL_THREADS = 8;

    /** tool 执行线程池（有界，防止失控增长；排队保证命令不丢） */
    private final ExecutorService toolExecutor =
            new ThreadPoolExecutor(2, MAX_TOOL_THREADS,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(64),
                    runnable -> {
                        Thread thread = new Thread(runnable, "mcp-tool-worker");
                        thread.setDaemon(true);
                        return thread;
                    });

    /** 超时调度器 — 单线程，仅负责触发 cancel，不占工作线程 */
    private final ScheduledExecutorService timeoutScheduler = new ScheduledThreadPoolExecutor(1, runnable -> {
        Thread thread = new Thread(runnable, "stdio-tool-timeout");
        thread.setDaemon(true);
        return thread;
    });

    /** elicitation 反向请求关联登记表 */
    private final ElicitationRegistry elicitationRegistry = new ElicitationRegistry();

    /** 反向请求 id 自增计数 */
    private final AtomicLong reverseIdSeq = new AtomicLong();

    /** client 是否声明了 elicitation 能力（initialize 阶段探测） */
    private volatile boolean clientSupportsElicitation = false;

    /** 出站流引用，便于测试替换 */
    private final PrintStream out = System.out;

    public StdioTransport(McpToolRegistry toolRegistry, McpPromptRegistry promptRegistry,
                          ObjectMapper objectMapper, FlightHubProperties properties) {
        this.toolRegistry = toolRegistry;
        this.promptRegistry = promptRegistry;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Stdio transport started. Waiting for JSON-RPC requests on stdin...");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                processLine(line);
            }
        } finally {
            toolExecutor.shutdownNow();
        }

        log.info("Stdin closed. Shutting down...");
    }

    /**
     * 处理一行入站 JSON。区分三类：反向响应、通知（无 id）、正向请求。
     *
     * @param line 入站 JSON 文本行
     */
    private void processLine(String line) {
        Map<String, Object> message;
        try {
            message = objectMapper.readValue(line, MAP_TYPE_REF);
        } catch (Exception e) {
            log.error("Error parsing request: {}", e.getMessage(), e);
            writeErrorResponse(null, JSON_RPC_PARSE_ERROR, "Parse error: " + e.getMessage());
            return;
        }

        // 优先判断是否为反向请求（elicitation）的响应：命中则唤醒对应 tool 线程后结束
        if (tryHandleReverseResponse(message)) {
            return;
        }

        try {
            dispatchClientRequest(message);
        } catch (Exception e) {
            // 兜底：畸形请求（如 method 非字符串）不应崩溃 reader 线程
            Object id = message.get("id");
            log.error("Error dispatching request id={}: {}", id, e.getMessage(), e);
            writeErrorResponse(id, JSON_RPC_PARSE_ERROR, "Invalid request: " + e.getMessage());
        }
    }

    /**
     * 判断并处理 client 对反向请求的响应。
     *
     * @param message 入站消息
     * @return true 表示已作为反向响应处理；false 表示不是反向响应
     */
    private boolean tryHandleReverseResponse(Map<String, Object> message) {
        // 反向响应特征：无 method 字段，且 id 命中登记表
        if (message.get("method") != null) {
            return false;
        }
        Object rawId = message.get("id");
        if (!(rawId instanceof String id) || !elicitationRegistry.isPending(id)) {
            return false;
        }
        ElicitResult result = parseElicitResponse(message);
        elicitationRegistry.complete(id, result);
        return true;
    }

    /**
     * 从 JSON-RPC 响应中解析 elicitation 结果。
     *
     * @param message 反向响应消息
     * @return 解析后的结果；含 error 时按 CANCEL 处理
     */
    @SuppressWarnings("unchecked")
    private ElicitResult parseElicitResponse(Map<String, Object> message) {
        if (message.get("error") != null) {
            // client 返回错误（如不支持/内部错误）→ 视为取消，绝不放行
            return ElicitResult.cancel();
        }
        Object rawResult = message.get("result");
        Map<String, Object> result = rawResult instanceof Map<?, ?> m
                ? (Map<String, Object>) m : null;
        return ElicitResult.fromResponse(result);
    }

    /**
     * 分发 client 发来的正向请求 / 通知。
     *
     * @param request 入站请求
     */
    private void dispatchClientRequest(Map<String, Object> request) {
        String method = (String) request.get("method");
        if ("tools/call".equals(method)) {
            // tool 可能发起反向请求，必须移出 reader 主循环到工作线程执行
            executeToolAsync(request);
            return;
        }
        Map<String, Object> response = handleRequest(request);
        if (response != null) {
            writeMessage(response);
        }
    }

    /**
     * 在工作线程上执行 tools/call，执行期间绑定本 transport 为 client bridge。
     *
     * @param request tools/call 请求
     */
    @SuppressWarnings("unchecked")
    private void executeToolAsync(Map<String, Object> request) {
        Object id = request.get("id");
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());
        int timeoutSeconds = properties.getTransport().getToolTimeoutSeconds();
        AtomicBoolean responded = new AtomicBoolean(false);
        try {
            Future<?> future = toolExecutor.submit(() -> {
                ClientBridgeHolder.set(this);
                try {
                    Map<String, Object> response = handleToolsCall(id, params);
                    if (responded.compareAndSet(false, true)) {
                        writeMessage(response);
                    }
                } catch (Exception e) {
                    log.error("Tool execution error: {}", e.getMessage(), e);
                    if (responded.compareAndSet(false, true)) {
                        writeMessage(createErrorResponse(id, JSON_RPC_PARSE_ERROR,
                                "Tool execution error: " + e.getMessage()));
                    }
                } finally {
                    ClientBridgeHolder.clear();
                }
            });
            // 超时守护：使用 ScheduledExecutorService，不占工作线程
            timeoutScheduler.schedule(() -> {
                if (responded.compareAndSet(false, true)) {
                    future.cancel(true);
                    log.error("Tool execution timeout ({}s): id={}", timeoutSeconds, id);
                    Map<String, Object> errorResult = new LinkedHashMap<>();
                    errorResult.put("success", false);
                    errorResult.put("error_message", "Tool execution timeout (" + timeoutSeconds + "s). "
                            + "The operation took too long. Please retry or check device connectivity.");
                    writeMessage(buildToolResponse(id, errorResult));
                }
            }, timeoutSeconds, TimeUnit.SECONDS);
        } catch (java.util.concurrent.RejectedExecutionException e) {
            log.warn("Tool executor saturated, rejecting request id={}: {}", id, e.getMessage());
            if (responded.compareAndSet(false, true)) {
                writeMessage(createErrorResponse(id, -32603,
                        "Server busy — too many concurrent tool calls. Please retry later."));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleRequest(Map<String, Object> request) {
        String method = (String) request.get("method");
        Object id = request.get("id");
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());

        log.debug("Received request: method={}, id={}", method, id);

        if (method == null) {
            return createErrorResponse(id, JSON_RPC_METHOD_NOT_FOUND, "Method is null");
        }

        return switch (method) {
            case "initialize" -> handleInitialize(id, params);
            case "initialized" -> null;
            case "tools/list" -> handleToolsList(id);
            case "prompts/list" -> handlePromptsList(id);
            case "prompts/get" -> handlePromptsGet(id, params);
            case "ping" -> createSuccessResponse(id, Map.of());
            default -> createErrorResponse(id, JSON_RPC_METHOD_NOT_FOUND, "Method not found: " + method);
        };
    }

    /**
     * 处理 initialize：探测 client 的 elicitation 能力并声明 server 能力。
     * 协议版本协商：取 client 请求的版本与 server 支持版本中的较低者回显。
     *
     * @param id     请求 id
     * @param params 请求参数（含 client capabilities）
     * @return initialize 响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> handleInitialize(Object id, Map<String, Object> params) {
        clientSupportsElicitation = detectElicitationCapability(params);
        log.info("Client elicitation capability: {}", clientSupportsElicitation);

        String negotiatedVersion = negotiateProtocolVersion(params);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", negotiatedVersion);
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", Map.of("listChanged", false));
        capabilities.put("prompts", Map.of("listChanged", false));
        result.put("capabilities", capabilities);
        result.put("serverInfo", Map.of("name", SERVER_NAME, "version", SERVER_VERSION));
        return createSuccessResponse(id, result);
    }

    /**
     * 协商协议版本：若 client 声明的版本低于 server 支持版本，回显 client 版本；
     * 否则回显 server 版本（向前兼容）。
     *
     * @param params initialize 请求参数
     * @return 协商后的协议版本
     */
    private String negotiateProtocolVersion(Map<String, Object> params) {
        if (params == null) {
            return MCP_PROTOCOL_VERSION;
        }
        Object clientVersion = params.get("protocolVersion");
        if (clientVersion instanceof String cv && !cv.isBlank()) {
            if (cv.compareTo(MCP_PROTOCOL_VERSION) < 0) {
                return cv;
            }
        }
        return MCP_PROTOCOL_VERSION;
    }

    /**
     * 从 initialize 参数中探测 client 是否声明 elicitation 能力。
     *
     * @param params initialize 请求参数
     * @return true 表示 client 支持 elicitation
     */
    @SuppressWarnings("unchecked")
    private boolean detectElicitationCapability(Map<String, Object> params) {
        if (params == null) {
            return false;
        }
        Object capabilities = params.get("capabilities");
        if (!(capabilities instanceof Map<?, ?> capMap)) {
            return false;
        }
        return ((Map<String, Object>) capMap).containsKey("elicitation");
    }

    private Map<String, Object> handleToolsList(Object id) {
        List<Map<String, Object>> tools = toolRegistry.getToolDefinitions();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tools", tools);
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
            return createErrorResponse(id, JSON_RPC_INVALID_PARAMS, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> handleToolsCall(Object id, Map<String, Object> params) {
        String toolName = (String) params.get("name");
        Object rawArgs = params.getOrDefault("arguments", Map.of());
        Map<String, Object> arguments = (rawArgs instanceof Map) ? (Map<String, Object>) rawArgs : Map.of();

        log.info("Tool call: name={}, id={}", toolName, id);

        RequestContext.set(id, toolName, RequestContext.STDIO_CLIENT_ID);
        try {
            Map<String, Object> toolResult = toolRegistry.executeTool(toolName, arguments);

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
        } finally {
            RequestContext.clear();
        }
    }

    // ==================== ClientBridge 实现（反向通道） ====================

    @Override
    public boolean supportsElicitation() {
        return clientSupportsElicitation;
    }

    @Override
    public ElicitResult elicit(String message, Map<String, Object> requestedSchema, long timeoutMillis) {
        String requestId = REVERSE_ID_PREFIX + reverseIdSeq.incrementAndGet();
        CompletableFuture<ElicitResult> future = elicitationRegistry.register(requestId);

        Map<String, Object> reverseRequest = buildElicitationRequest(requestId, message, requestedSchema);
        writeMessage(reverseRequest);

        return elicitationRegistry.await(requestId, future, timeoutMillis);
    }

    /**
     * 构建 elicitation/create 反向请求 JSON-RPC 消息。
     *
     * @param requestId       反向请求 id
     * @param message         提示信息
     * @param requestedSchema 表单 schema
     * @return JSON-RPC 请求 Map
     */
    private Map<String, Object> buildElicitationRequest(String requestId, String message,
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

    // ==================== Response Builders ====================

    /**
     * 串行化写出一条出站消息（正向响应或反向请求）。
     *
     * @param message 出站消息
     */
    private void writeMessage(Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            synchronized (stdoutLock) {
                out.println(json);
                out.flush();
            }
        } catch (Exception e) {
            log.error("Failed to write message: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> createSuccessResponse(Object id, Map<String, Object> result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        return response;
    }

    /**
     * 将 tool 执行结果包装为 MCP content 格式的 JSON-RPC 响应。
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

    private Map<String, Object> createErrorResponse(Object id, int code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }

    private void writeErrorResponse(Object id, int code, String message) {
        writeMessage(createErrorResponse(id, code, message));
    }
}
