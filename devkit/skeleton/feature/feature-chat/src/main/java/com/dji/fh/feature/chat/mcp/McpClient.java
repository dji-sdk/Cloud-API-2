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

package com.dji.fh.feature.chat.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP Server JSON-RPC 客户端。
 *
 * <p>通过 SSE 连接 MCP Server，发送 JSON-RPC 请求并等待响应。
 * 采用 JDK HttpClient 实现，无额外依赖。
 *
 * <p>生命周期：
 * <ol>
 *   <li>{@link #connect()} — 建立 SSE 连接，获取 clientId 和 message endpoint</li>
 *   <li>{@link #initialize()} — 发送 JSON-RPC initialize 请求完成握手</li>
 *   <li>{@link #listTools()} — 获取 MCP Server 全量 Tool 列表</li>
 *   <li>{@link #callTool(String, Map)} — 调用指定 Tool</li>
 * </ol>
 *
 * @author DJI DevKit Team
 */
public class McpClient {

    private static final Logger LOG = LoggerFactory.getLogger(McpClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * MCP Server 对外接口前缀，与 mcp 端 spring.webflux.base-path 保持一致。
     *
     * <p>SSE 连接 URL = baseUrl + MCP_BASE_PATH + "/sse"。
     * MCP Server 在 sse profile 下始终配置该 base-path，故此处按常量处理；
     * 若 ISV 自定义 mcp base-path，需同步调整。
     */
    private static final String MCP_BASE_PATH = "/pf-devkit/ai-plugin/mcp";

    /**
     * 历史遗留 base-path 前缀清单（升级兼容）。
     *
     * <p>{@link #MCP_BASE_PATH} 由 {@code /ai-plugin/mcp} 变更为 {@code /pf-devkit/ai-plugin/mcp} 后，
     * 上一版本若 ISV 把完整旧前缀写进 {@code fh.chat.mcp.url}
     * （如 {@code http://host:8080/ai-plugin/mcp}），旧代码会剥离为 {@code http://host:8080}；
     * 新常量只认新前缀，旧前缀不再被剥离，会导致 connect() 拼出双重路径而 404。
     * 此处保留对历史前缀的幂等剥离，保证平滑升级。设计约定仍要求 url 仅填 host:port。
     */
    private static final List<String> LEGACY_MCP_BASE_PATHS = List.of("/ai-plugin/mcp");

    /** JSON-RPC 请求 ID 生成器 */
    private final AtomicInteger requestIdCounter = new AtomicInteger(1);

    /** 等待响应的 pending futures，key 为 request id */
    private final ConcurrentHashMap<String, CompletableFuture<JsonNode>> pendingRequests =
            new ConcurrentHashMap<>();

    /** MCP Server 基础 URL */
    private final String baseUrl;

    /** MCP Server SSE 鉴权 Bearer Token（留空表示不鉴权） */
    private final String authToken;

    /** 调用超时 */
    private final Duration timeout;

    /** 连接超时 */
    private final Duration connectTimeout;

    /** SSE 中获取的 message endpoint URL */
    private final AtomicReference<String> messageEndpoint = new AtomicReference<>();

    /** JDK HttpClient */
    private final HttpClient httpClient;

    /** 是否启用断线自动重连 */
    private final boolean reconnectEnabled;

    /** 重连最大尝试次数 */
    private final int reconnectMaxAttempts;

    /** 重连初始延迟（毫秒） */
    private final long reconnectInitialDelayMs;

    /** 重连最大延迟（毫秒） */
    private final long reconnectMaxDelayMs;

    /** 重连调度器 */
    private final ScheduledExecutorService reconnectScheduler;

    /** 是否启用心跳保活 */
    private final boolean keepaliveEnabled;

    /** 客户端主动 ping 间隔（毫秒） */
    private final long keepaliveIntervalMs;

    /** 心跳超时容忍时长（毫秒） */
    private final long heartbeatTimeoutMs;

    /** ping 请求超时（毫秒），固定 5 秒 */
    private static final long PING_TIMEOUT_MS = 5000L;

    /** 最后一次收到服务端 heartbeat 事件的时间（毫秒） */
    private volatile long lastHeartbeatReceivedAt = 0;

    /** 定时保活任务引用 */
    private volatile ScheduledFuture<?> keepaliveTask = null;

    /** 是否已完成连接和初始化（曾经成功过） */
    private volatile boolean initialized = false;

    /** SSE 连接是否活跃 */
    private volatile boolean connected = false;

    /** 是否正在关闭 */
    private volatile boolean shuttingDown = false;

    /** 当前活跃的 SSE reader 线程（用于区分新旧 reader） */
    private volatile Thread activeSseThread = null;

    /** 重连成功后的回调（用于重新建立 FlightHub session 等） */
    private volatile Runnable onReconnectedCallback = null;

    /**
     * 构造 McpClient。
     *
     * @param properties MCP 连接配置
     */
    public McpClient(McpClientProperties properties) {
        this.baseUrl = normalizeBaseUrl(properties.getUrl());
        this.authToken = properties.getAuthToken();
        this.timeout = properties.getTimeout();
        this.connectTimeout = properties.getConnectTimeout();
        this.reconnectEnabled = properties.isReconnectEnabled();
        this.reconnectMaxAttempts = properties.getReconnectMaxAttempts();
        this.reconnectInitialDelayMs = properties.getReconnectInitialDelay().toMillis();
        this.reconnectMaxDelayMs = properties.getReconnectMaxDelay().toMillis();
        this.keepaliveEnabled = properties.isKeepaliveEnabled();
        this.keepaliveIntervalMs = properties.getKeepaliveInterval().toMillis();
        this.heartbeatTimeoutMs = properties.getHeartbeatTimeout().toMillis();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        this.reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mcp-reconnect");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 规范化 baseUrl：去除尾部斜杠；若 ISV 已在 url 中带上 MCP_BASE_PATH 前缀则剥离并告警，
     * 避免 connect() 再拼一次 MCP_BASE_PATH 造成双重前缀（如
     * {@code http://host:8080/pf-devkit/ai-plugin/mcp} + {@code /pf-devkit/ai-plugin/mcp/sse}）。
     *
     * <p>设计约定（见设计文档 D5）：{@code fh.chat.mcp.url} 仅填 host:port，不带 base-path。
     * 此处做幂等兜底，捕获 ISV 把前缀写进 url 的常见误配。
     *
     * <p>同时兼容历史前缀（{@link #LEGACY_MCP_BASE_PATHS}）：从上一版本升级且 url 仍带旧 base-path
     * （如 {@code /ai-plugin/mcp}）时同样剥离，避免升级后静默 404。
     *
     * @param raw 原始配置 url
     * @return 规范化后的 baseUrl（无尾部斜杠、不含当前/历史 MCP base-path）
     */
    private static String normalizeBaseUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String trimmed = raw.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        if (trimmed.endsWith(MCP_BASE_PATH)) {
            LOG.warn("fh.chat.mcp.url={} 已包含 MCP base-path 前缀 {}，已自动剥离避免双重前缀；"
                    + "建议仅配置 host:port（如 http://host:8080）。", raw, MCP_BASE_PATH);
            trimmed = trimmed.substring(0, trimmed.length() - MCP_BASE_PATH.length());
            while (trimmed.endsWith("/")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
        }
        // 兼容历史前缀：上一版本 base-path 为 /ai-plugin/mcp，升级后常量已变，
        // 但旧 url 仍可能带该前缀，剥离以保证平滑升级（设计约定仍只填 host:port）
        for (String legacy : LEGACY_MCP_BASE_PATHS) {
            if (trimmed.endsWith(legacy)) {
                LOG.warn("fh.chat.mcp.url={} 仍带历史 MCP base-path 前缀 {}，已自动剥离；"
                        + "建议更新为新前缀 {} 或仅配置 host:port。",
                        raw, legacy, MCP_BASE_PATH);
                trimmed = trimmed.substring(0, trimmed.length() - legacy.length());
                while (trimmed.endsWith("/")) {
                    trimmed = trimmed.substring(0, trimmed.length() - 1);
                }
            }
        }
        return trimmed;
    }

    /**
     * 连接 MCP Server SSE 端点并完成初始化握手。
     *
     * <p>阻塞直到 SSE 连接建立并收到 endpoint 事件，然后发送 initialize 请求。
     *
     * @throws McpClientException 连接或初始化失败时抛出
     */
    public void connectAndInitialize() {
        try {
            stopKeepalive();
            connect();
            initialize();
            this.initialized = true;
            this.connected = true;
            this.lastHeartbeatReceivedAt = System.currentTimeMillis();
            startKeepalive();
            LOG.info("McpClient 连接并初始化成功，messageEndpoint={}", messageEndpoint.get());
        } catch (Exception e) {
            throw new McpClientException("MCP Server 连接初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 建立 SSE 连接，解析 endpoint 事件获取 message URL。
     *
     * <p>启动后台线程持续读取 SSE 流，将 JSON-RPC 响应分发到 pending futures。
     */
    private void connect() {
        String sseUrl = baseUrl + MCP_BASE_PATH + "/sse";
        LOG.info("连接 MCP Server SSE: {}", sseUrl);

        CompletableFuture<String> endpointFuture = new CompletableFuture<>();

        // 后台线程持续读取 SSE 流
        Thread sseThread = new Thread(() -> readSseStream(sseUrl, endpointFuture), "mcp-sse-reader");
        sseThread.setDaemon(true);
        activeSseThread = sseThread;
        sseThread.start();

        // 等待 endpoint 事件
        try {
            String endpoint = endpointFuture.get(connectTimeout.toMillis(), TimeUnit.MILLISECONDS);
            // endpoint 可能是相对路径或绝对路径
            if (endpoint.startsWith("/")) {
                messageEndpoint.set(baseUrl + endpoint);
            } else {
                messageEndpoint.set(endpoint);
            }
        } catch (Exception e) {
            throw new McpClientException("等待 SSE endpoint 超时: " + e.getMessage(), e);
        }
    }

    /**
     * 持续读取 SSE 流，解析事件并分发响应。
     *
     * @param sseUrl SSE 连接 URL
     * @param endpointFuture 用于通知 endpoint 已获取的 future
     */
    private void readSseStream(String sseUrl, CompletableFuture<String> endpointFuture) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(sseUrl))
                    .GET();
            applyAuthHeader(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<java.io.InputStream> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                endpointFuture.completeExceptionally(
                        new McpClientException("SSE 连接失败，HTTP " + response.statusCode()));
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String currentEvent = "";
                StringBuilder dataBuffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("event:")) {
                        currentEvent = line.substring(6).trim();
                    } else if (line.startsWith("data:")) {
                        if (!dataBuffer.isEmpty()) {
                            dataBuffer.append('\n');
                        }
                        dataBuffer.append(line.substring(5).trim());
                    } else if (line.isEmpty()) {
                        // 空行表示事件结束（SSE 规范）
                        if (!dataBuffer.isEmpty()) {
                            // 空 event 按 SSE 规范视为 "message"
                            String eventType = currentEvent.isEmpty() ? "message" : currentEvent;
                            handleSseEvent(eventType, dataBuffer.toString(), endpointFuture);
                            dataBuffer.setLength(0);
                            currentEvent = "";
                        }
                    }
                }
                // 流结束时若还有未处理的数据，也要分发
                if (!dataBuffer.isEmpty()) {
                    String eventType = currentEvent.isEmpty() ? "message" : currentEvent;
                    handleSseEvent(eventType, dataBuffer.toString(), endpointFuture);
                }
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.warn("SSE 流断开: {}", e.getMessage());
            if (!endpointFuture.isDone()) {
                endpointFuture.completeExceptionally(e);
            }
        } finally {
            // 仅当前线程是活跃 reader 时才触发状态变更和重连（防止旧 reader 覆盖新连接）
            boolean isActiveReader = Thread.currentThread() == activeSseThread;
            if (isActiveReader) {
                connected = false;
                messageEndpoint.set(null);
                McpClientException disconnectEx =
                        new McpClientException("SSE 连接已断开，请求无法完成");
                pendingRequests.forEach((id, future) -> future.completeExceptionally(disconnectEx));
                pendingRequests.clear();
            }

            // 触发自动重连（仅活跃 reader 断开、曾经初始化成功且未关闭时）
            if (isActiveReader && initialized && reconnectEnabled && !shuttingDown) {
                LOG.info("SSE 连接断开，将启动自动重连...");
                scheduleReconnect(1);
            }
        }
    }

    /**
     * 处理单个 SSE 事件。
     *
     * @param event 事件类型
     * @param data 事件数据
     * @param endpointFuture endpoint future
     */
    private void handleSseEvent(String event, String data,
                                CompletableFuture<String> endpointFuture) {
        switch (event) {
            case "endpoint" -> {
                LOG.debug("收到 endpoint 事件: {}", data);
                endpointFuture.complete(data);
            }
            case "message" -> {
                try {
                    JsonNode jsonNode = MAPPER.readTree(data);
                    String id = jsonNode.has("id") ? jsonNode.get("id").asText() : null;
                    if (id != null) {
                        CompletableFuture<JsonNode> future = pendingRequests.remove(id);
                        if (future != null) {
                            future.complete(jsonNode);
                        }
                    }
                } catch (JsonProcessingException e) {
                    LOG.warn("解析 SSE message 失败: {}", data, e);
                }
            }
            case "heartbeat" -> {
                lastHeartbeatReceivedAt = System.currentTimeMillis();
                LOG.debug("收到 heartbeat: {}", data);
            }
            default -> LOG.debug("忽略未知 SSE 事件: event={}, data={}", event, data);
        }
    }

    /**
     * 调度一次重连尝试，使用指数退避策略。
     *
     * @param attempt 当前尝试次数（从 1 开始）
     */
    private void scheduleReconnect(int attempt) {
        if (!reconnectEnabled || shuttingDown) {
            return;
        }
        if (attempt > reconnectMaxAttempts) {
            LOG.error("SSE 重连失败，已达到最大尝试次数 {}，放弃重连", reconnectMaxAttempts);
            return;
        }

        long delay = Math.min(
                reconnectInitialDelayMs * (1L << Math.min(attempt - 1, 30)),
                reconnectMaxDelayMs
        );
        LOG.info("SSE 重连调度：第 {} 次尝试，延迟 {}ms", attempt, delay);

        reconnectScheduler.schedule(() -> {
            if (shuttingDown) {
                return;
            }
            try {
                connectAndInitialize();
                LOG.info("SSE 重连成功（第 {} 次尝试），messageEndpoint={}", attempt, messageEndpoint.get());
                invokeReconnectedCallback();
            } catch (Exception e) {
                LOG.warn("SSE 重连失败（第 {} 次尝试）: {}", attempt, e.getMessage());
                scheduleReconnect(attempt + 1);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行重连成功回调，失败不影响重连流程。
     */
    private void invokeReconnectedCallback() {
        Runnable callback = onReconnectedCallback;
        if (callback != null) {
            try {
                callback.run();
                LOG.info("重连回调执行成功");
            } catch (Exception e) {
                LOG.warn("重连回调执行失败（不影响 SSE 连接）: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭 McpClient，停止重连调度器。
     *
     * <p>由 Spring 容器在 bean 销毁时调用。
     */
    public void shutdown() {
        shuttingDown = true;
        connected = false;
        stopKeepalive();
        reconnectScheduler.shutdownNow();
        McpClientException shutdownEx = new McpClientException("McpClient 正在关闭");
        pendingRequests.forEach((id, future) -> future.completeExceptionally(shutdownEx));
        pendingRequests.clear();
        LOG.info("McpClient 已关闭");
    }

    /**
     * 发送 JSON-RPC initialize 请求完成 MCP 协议握手。
     */
    private void initialize() {
        ObjectNode params = MAPPER.createObjectNode();
        params.put("protocolVersion", "2024-11-05");
        ObjectNode clientInfo = MAPPER.createObjectNode();
        clientInfo.put("name", "fh-skeleton-chat");
        clientInfo.put("version", "1.0.0");
        params.set("clientInfo", clientInfo);
        ObjectNode capabilities = MAPPER.createObjectNode();
        params.set("capabilities", capabilities);

        JsonNode response = sendRequest("initialize", params);
        if (response == null || response.has("error")) {
            throw new McpClientException("MCP initialize 失败: " + response);
        }
        LOG.debug("MCP initialize 成功: {}", response);

        // 发送 initialized 通知（无需响应）
        sendNotification("notifications/initialized", MAPPER.createObjectNode());
    }

    /**
     * 获取 MCP Server 全量 Tool 列表。
     *
     * @return Tool 列表的 JSON 响应
     * @throws McpClientException 调用失败时抛出
     */
    public JsonNode listTools() {
        ensureInitialized();
        JsonNode response = sendRequest("tools/list", MAPPER.createObjectNode());
        if (response == null) {
            throw new McpClientException("tools/list 响应为 null");
        }
        if (response.has("error")) {
            throw new McpClientException("tools/list 错误: " + response.get("error"));
        }
        return response.get("result");
    }

    /**
     * 调用 MCP Server 的指定 Tool。
     *
     * @param toolName  Tool 名称
     * @param arguments Tool 参数
     * @return Tool 返回的文本内容
     * @throws McpClientException 调用失败或超时时抛出
     */
    public String callTool(String toolName, Map<String, Object> arguments) {
        // 若已知断连，先同步重连再调用
        if (!connected && initialized && reconnectEnabled && !shuttingDown) {
            LOG.info("callTool 检测到断连状态，尝试同步重连...");
            try {
                connectAndInitialize();
                LOG.info("同步重连成功，继续执行 tool={}", toolName);
                invokeReconnectedCallback();
            } catch (Exception e) {
                LOG.warn("同步重连失败: {}", e.getMessage());
                throw new McpClientException("MCP Server 连接已断开且重连失败: " + e.getMessage(), e);
            }
        }

        ensureInitialized();

        ObjectNode params = MAPPER.createObjectNode();
        params.put("name", toolName);
        params.set("arguments", MAPPER.valueToTree(arguments));

        JsonNode response = sendRequest("tools/call", params);
        if (response == null) {
            throw new McpClientException("tools/call 响应为 null，toolName=" + toolName);
        }
        if (response.has("error")) {
            JsonNode error = response.get("error");
            return buildErrorJson("mcp_error", error.has("message")
                    ? error.get("message").asText() : error.toString());
        }

        // 提取 result.content[0].text
        JsonNode result = response.get("result");
        if (result != null && result.has("content")) {
            JsonNode content = result.get("content");
            if (content.isArray() && !content.isEmpty()) {
                JsonNode first = content.get(0);
                if (first.has("text")) {
                    return first.get("text").asText();
                }
            }
        }
        return result != null ? result.toString() : "{}";
    }

    /**
     * 获取 MCP Server 的 tool 列表，提取指定 tool 的 inputSchema。
     *
     * @param toolName tool 名称
     * @param toolsResult tools/list 的 result 节点
     * @return inputSchema JSON 字符串；未找到时返回空 schema
     */
    public static String extractInputSchema(String toolName, JsonNode toolsResult) {
        if (toolsResult != null && toolsResult.has("tools")) {
            for (JsonNode tool : toolsResult.get("tools")) {
                if (toolName.equals(tool.path("name").asText())) {
                    JsonNode schema = tool.get("inputSchema");
                    return schema != null ? schema.toString() : "{}";
                }
            }
        }
        return "{\"type\":\"object\",\"properties\":{}}";
    }

    /**
     * 提取指定 tool 的 description。
     *
     * @param toolName tool 名称
     * @param toolsResult tools/list 的 result 节点
     * @return description 字符串；未找到时返回默认描述
     */
    public static String extractDescription(String toolName, JsonNode toolsResult) {
        if (toolsResult != null && toolsResult.has("tools")) {
            for (JsonNode tool : toolsResult.get("tools")) {
                if (toolName.equals(tool.path("name").asText())) {
                    return tool.path("description").asText(toolName);
                }
            }
        }
        return toolName;
    }

    /**
     * 发送 JSON-RPC 请求并等待响应。
     *
     * @param method JSON-RPC 方法名
     * @param params 参数节点
     * @return 完整的 JSON-RPC 响应
     */
    private JsonNode sendRequest(String method, JsonNode params) {
        String id = String.valueOf(requestIdCounter.getAndIncrement());

        ObjectNode request = MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", id);
        request.put("method", method);
        request.set("params", params);

        CompletableFuture<JsonNode> responseFuture = new CompletableFuture<>();
        pendingRequests.put(id, responseFuture);

        try {
            postMessage(request);
            return responseFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pendingRequests.remove(id);
            // SSE 连接表面存活但 session 已失效（MCP Server 重启），标记断连触发重连
            LOG.warn("JSON-RPC 响应超时 method={}，疑似 SSE session 失效，触发重连", method);
            connected = false;
            if (initialized && reconnectEnabled && !shuttingDown) {
                scheduleReconnect(1);
            }
            throw new McpClientException("JSON-RPC 响应超时 method=" + method
                    + "（已触发重连，请稍后重试）", e);
        } catch (Exception e) {
            pendingRequests.remove(id);
            throw new McpClientException("JSON-RPC 请求失败 method=" + method + ": " + e.getMessage(), e);
        }
    }

    /**
     * 发送 JSON-RPC 通知（不等待响应）。
     *
     * @param method 方法名
     * @param params 参数节点
     */
    private void sendNotification(String method, JsonNode params) {
        ObjectNode notification = MAPPER.createObjectNode();
        notification.put("jsonrpc", "2.0");
        notification.put("method", method);
        notification.set("params", params);

        try {
            postMessage(notification);
        } catch (Exception e) {
            LOG.warn("发送通知失败 method={}: {}", method, e.getMessage());
        }
    }

    /**
     * 通过 HTTP POST 发送 JSON-RPC 消息到 MCP Server 的 message endpoint。
     *
     * @param body JSON 消息体
     */
    private void postMessage(JsonNode body) {
        String endpoint = messageEndpoint.get();
        if (endpoint == null) {
            throw new McpClientException("message endpoint 未就绪");
        }

        try {
            String jsonBody = MAPPER.writeValueAsString(body);
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            applyAuthHeader(requestBuilder);
            HttpRequest request = requestBuilder.build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                LOG.warn("POST message 返回 HTTP {}: {}", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new McpClientException("POST message 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 若配置了 authToken，为请求添加 {@code Authorization: Bearer <token>} 头。
     *
     * <p>MCP Server SSE 模式启用鉴权（{@code FH_SSE_AUTH_TOKEN}）时必填，
     * 留空则不添加，适用于未启用鉴权的内网调试场景。
     *
     * @param requestBuilder HTTP 请求构建器
     */
    private void applyAuthHeader(HttpRequest.Builder requestBuilder) {
        if (authToken != null && !authToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }
    }

    /**
     * 确保 client 已初始化且连接活跃。
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new McpClientException("McpClient 未初始化，请先调用 connectAndInitialize()");
        }
        if (!connected) {
            throw new McpClientException("SSE 连接已断开，正在重连中...");
        }
    }

    /**
     * 返回 SSE 连接是否活跃。
     *
     * @return 连接状态
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 返回是否已初始化。
     *
     * @return 初始化状态
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 设置重连成功后的回调。
     *
     * <p>典型用途：重连后重新调用 fh_connect 建立 FlightHub session，
     * 否则新 SSE 客户端因"无 session 绑定"会被 HeartbeatScheduler 清理。
     *
     * @param callback 重连成功后执行的回调
     */
    public void setOnReconnectedCallback(Runnable callback) {
        this.onReconnectedCallback = callback;
    }

    /**
     * 启动心跳保活定时任务。
     *
     * <p>定期检查服务端 heartbeat 是否超时，并发送 ping 请求刷新服务端 lastActivityAt。
     * 任一检测失败则标记断连并触发重连。
     */
    private void startKeepalive() {
        if (!keepaliveEnabled || shuttingDown) {
            return;
        }
        stopKeepalive();
        keepaliveTask = reconnectScheduler.scheduleAtFixedRate(
                this::performKeepaliveCheck,
                keepaliveIntervalMs,
                keepaliveIntervalMs,
                TimeUnit.MILLISECONDS
        );
        LOG.debug("心跳保活已启动，间隔 {}ms，超时 {}ms", keepaliveIntervalMs, heartbeatTimeoutMs);
    }

    /**
     * 停止心跳保活定时任务。
     */
    private void stopKeepalive() {
        ScheduledFuture<?> task = keepaliveTask;
        if (task != null) {
            task.cancel(false);
            keepaliveTask = null;
        }
    }

    /**
     * 执行一次保活检查：
     * <ol>
     *   <li>检查服务端 heartbeat 是否超时</li>
     *   <li>发送 ping 请求验证连接可用性</li>
     * </ol>
     */
    private void performKeepaliveCheck() {
        if (!connected || shuttingDown) {
            return;
        }

        // 检查服务端 heartbeat 超时
        long elapsed = System.currentTimeMillis() - lastHeartbeatReceivedAt;
        if (elapsed > heartbeatTimeoutMs) {
            LOG.warn("心跳超时：{}ms 未收到服务端 heartbeat（阈值 {}ms），触发重连",
                    elapsed, heartbeatTimeoutMs);
            triggerKeepaliveReconnect();
            return;
        }

        // 发送 ping 刷新服务端 lastActivityAt
        try {
            sendPing();
            LOG.debug("发送 ping 成功");
        } catch (Exception e) {
            LOG.warn("ping 失败: {}，触发重连", e.getMessage());
            triggerKeepaliveReconnect();
        }
    }

    /**
     * 发送 JSON-RPC ping 请求（服务端 SseTransport 返回空 success）。
     *
     * @throws McpClientException ping 超时或失败时抛出
     */
    private void sendPing() {
        String id = String.valueOf(requestIdCounter.getAndIncrement());
        ObjectNode request = MAPPER.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", id);
        request.put("method", "ping");
        request.set("params", MAPPER.createObjectNode());

        CompletableFuture<JsonNode> responseFuture = new CompletableFuture<>();
        pendingRequests.put(id, responseFuture);

        try {
            postMessage(request);
            responseFuture.get(PING_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            pendingRequests.remove(id);
            throw new McpClientException("ping 超时", e);
        } catch (Exception e) {
            pendingRequests.remove(id);
            throw new McpClientException("ping 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 由保活检查触发的重连：停止保活任务、标记断连、调度重连。
     */
    private void triggerKeepaliveReconnect() {
        stopKeepalive();
        connected = false;
        if (initialized && reconnectEnabled && !shuttingDown) {
            scheduleReconnect(1);
        }
    }

    /**
     * 构建错误 JSON 字符串。
     *
     * @param code 错误码
     * @param message 错误消息
     * @return JSON 字符串
     */
    private String buildErrorJson(String code, String message) {
        try {
            ObjectNode error = MAPPER.createObjectNode();
            error.put("status", "error");
            error.put("code", code);
            error.put("message", message);
            return MAPPER.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            return "{\"status\":\"error\",\"code\":\"" + code + "\"}";
        }
    }
}
