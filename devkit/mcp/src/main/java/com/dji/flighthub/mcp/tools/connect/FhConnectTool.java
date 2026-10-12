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

package com.dji.flighthub.mcp.tools.connect;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.transport.ClientRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * fh_connect — 聚合初始化工具。
 * 执行 4 个步骤：SDK 初始化 → 设置认证头 → 注册 session → 验证 session。
 * 成功后返回 session_id 和 workspace 信息，并自动将 session 绑定到当前 SSE 客户端。
 *
 * <p>连接策略：默认使用 http://localhost:30812（同机部署场景），
 * 如果默认地址连接失败，通过 elicitation 引导用户输入正确的 server address 和 port。
 */
@Component
public class FhConnectTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhConnectTool.class);

    /** WebSocket 通知地址的固定路径后缀。 */
    private static final String WS_NOTIFICATION_PATH = "/openapi/v0.9/notification/ws/v1/notifications";

    /** stdio 传输模式的客户端标识。 */
    private static final String STDIO_CLIENT_ID = SessionManager.STDIO_CLIENT_ID;

    /** 同机部署默认地址。 */
    static final String DEFAULT_SERVER_ADDRESS = "http://localhost";

    /** 同机部署默认端口。 */
    static final int DEFAULT_PORT = 30812;

    private final FlightHubProperties properties;
    private final SdkService sdkService;
    private final ClientRegistry clientRegistry;

    public FhConnectTool(FlightHubProperties properties, SessionManager sessionManager,
                         SdkService sdkService, ClientRegistry clientRegistry) {
        super(sessionManager);
        this.properties = properties;
        this.sdkService = sdkService;
        this.clientRegistry = clientRegistry;
    }

    /**
     * MCP 注册所需的 tool 定义元数据。
     * 注意：access_token 和 workspace_uuid 由服务端环境变量配置，不通过参数传入。
     */
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_connect");
        def.put("description", "Connect to FlightHub platform and initialize SDK session. "
                + "This is the prerequisite for all other fh_* operations. "
                + "All connection parameters are pre-configured on the server side — "
                + "just call this tool with an empty arguments object {}. "
                + "Do NOT pass any parameters. Do NOT ask the user for server address or credentials.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of());
        schema.put("required", List.of());

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行连接操作。
     * server_address 和 port 可通过参数覆盖，access_token 和 workspace_uuid 固定从环境变量读取。
     * 默认使用同机部署地址（http://localhost:30812），连接失败时通过 elicitation 引导用户输入。
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        String serverAddress = getStringArg(args, "server_address", properties.getServer().getAddress());
        int port = getIntArg(args, "port", properties.getServer().getPort());
        String accessToken = properties.getAuth().getAccessToken();
        String workspaceUuid = properties.getAuth().getWorkspaceUuid();
        String notificationServerAddress = getStringArg(args, "notification_server_address", null);

        // 检查已有 session（多 session 模式下仅检查当前用户/客户端）
        RequestContext reqCtx = RequestContext.current();
        Map<String, Object> existingCheck = checkExistingSession(reqCtx);
        if (existingCheck != null) {
            return existingCheck;
        }

        if (serverAddress == null || serverAddress.isBlank()) {
            serverAddress = DEFAULT_SERVER_ADDRESS;
            port = DEFAULT_PORT;
        }

        if (notificationServerAddress == null || notificationServerAddress.isBlank()) {
            notificationServerAddress = deriveWebSocketUrl(serverAddress, port);
        }

        log.info("fh_connect: connecting to {}:{} workspace={} wsAddr={}",
                serverAddress, port,
                workspaceUuid != null ? workspaceUuid.substring(0, Math.min(8, workspaceUuid.length())) + "…" : "none",
                notificationServerAddress);

        Map<String, Object> result = doConnect(serverAddress, port, accessToken, workspaceUuid,
                notificationServerAddress, reqCtx);

        // 连接失败且使用的是默认地址 → 通过 elicitation 引导用户输入
        if (!isSuccess(result) && isDefaultAddress(serverAddress, port)) {
            Map<String, Object> elicitedResult = elicitAndRetry(accessToken, workspaceUuid, reqCtx);
            if (elicitedResult != null) {
                if (!isSuccess(elicitedResult)) {
                    // 用户提供的地址也失败，补充上下文
                    elicitedResult.put("error_detail",
                            "Both default address (" + DEFAULT_SERVER_ADDRESS + ":" + DEFAULT_PORT
                            + ") and user-provided address failed to connect.");
                }
                return elicitedResult;
            }
        }

        return result;
    }

    /**
     * 判断当前使用的地址是否为默认同机部署地址。
     */
    private static boolean isDefaultAddress(String address, int port) {
        return DEFAULT_SERVER_ADDRESS.equals(address) && port == DEFAULT_PORT;
    }

    /**
     * 通过 elicitation 向用户请求 server address 和 port，然后重试连接。
     *
     * @return 重试成功时返回连接结果；用户未提供输入或 elicitation 不可用时返回 null
     */
    private Map<String, Object> elicitAndRetry(String accessToken, String workspaceUuid, RequestContext reqCtx) {
        Map<String, Object> schema = buildAddressInputSchema();
        Map<String, Object> userInput = requireInput(
                "Default address " + DEFAULT_SERVER_ADDRESS + ":" + DEFAULT_PORT + " connection failed. "
                        + "Please enter the FlightHub server address and port:\n"
                        + "（默认地址连接失败，请输入 FlightHub 服务器地址和端口）",
                schema);

        if (userInput == null) {
            return null;
        }

        String newAddress = (String) userInput.get("server_address");
        Object portObj = userInput.get("port");
        int newPort = DEFAULT_PORT;
        if (portObj instanceof Number) {
            newPort = ((Number) portObj).intValue();
        } else if (portObj instanceof String && !((String) portObj).isBlank()) {
            try {
                newPort = Integer.parseInt((String) portObj);
            } catch (NumberFormatException ignored) {
                // 保持默认端口
            }
        }

        if (newAddress == null || newAddress.isBlank()) {
            return null;
        }

        String wsUrl = deriveWebSocketUrl(newAddress, newPort);
        log.info("fh_connect: retrying with user-provided address {}:{}", newAddress, newPort);
        return doConnect(newAddress, newPort, accessToken, workspaceUuid, wsUrl, reqCtx);
    }

    /**
     * 构建 elicitation 表单的 JSON Schema。
     */
    private Map<String, Object> buildAddressInputSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("server_address", Map.of(
                "type", "string",
                "description", "FlightHub server address (e.g. http://192.168.1.100)"
        ));
        props.put("port", Map.of(
                "type", "integer",
                "description", "FlightHub server port",
                "default", DEFAULT_PORT
        ));

        schema.put("properties", props);
        schema.put("required", List.of("server_address"));
        return schema;
    }

    /**
     * 判断 doConnect 返回结果是否成功。
     */
    private static boolean isSuccess(Map<String, Object> result) {
        Object success = result.get("success");
        return Boolean.TRUE.equals(success);
    }

    /**
     * 检查当前客户端/用户是否已有活跃 session。
     *
     * @param reqCtx 当前请求上下文
     * @return 已有 session 时返回响应（错误或 reconnect），无已有 session 时返回 null
     */
    private Map<String, Object> checkExistingSession(RequestContext reqCtx) {
        if (reqCtx != null && reqCtx.getClientId() != null
                && !STDIO_CLIENT_ID.equals(reqCtx.getClientId())) {
            return checkSseSession(reqCtx);
        }
        // Stdio 模式：全局单 session
        if (sessionManager.hasActiveSession()) {
            SessionContext existing = sessionManager.getActiveSession();
            return errorResult("Already connected",
                    "A session is already active (id=" + existing.getSessionId() + "). "
                    + "Call fh_ops with action='shutdown' first to disconnect.");
        }
        return null;
    }

    /**
     * SSE 模式下检查当前客户端/用户的已有 session。
     */
    private Map<String, Object> checkSseSession(RequestContext reqCtx) {
        // 检查当前 client 是否已绑定 session
        if (clientRegistry != null) {
            ClientRegistry.ClientEntry entry = clientRegistry.getClient(reqCtx.getClientId());
            if (entry != null && entry.getSessionId() != null) {
                SessionContext existing = sessionManager.getSession(entry.getSessionId());
                if (existing != null) {
                    return errorResult("Already connected",
                            "This client already has an active session (id=" + existing.getSessionId() + "). "
                            + "Call fh_ops with action='shutdown' first to disconnect.");
                }
            }
        }
        // 同一用户（JWT userId）是否已有 session
        String userId = reqCtx.getUserId();
        if (userId != null) {
            SessionContext userSession = sessionManager.getSessionByUserId(userId);
            if (userSession != null) {
                return reconnectUserSession(userSession, userId);
            }
        }
        return null;
    }

    /**
     * 将已有 session 重新绑定到当前客户端（用户重连场景）。
     * 绑定前验证 session 存活状态，防止返回过期的 SDK 连接。
     */
    private Map<String, Object> reconnectUserSession(SessionContext userSession, String userId) {
        // 验证 session 是否仍然存活（防止返回过期的 SDK 连接）
        if (userSession.isExpired(properties.getSecurity().getSessionTimeout())) {
            log.info("用户 {} 的 session {} 已过期，需重新初始化 SDK", userId, userSession.getSessionId());
            sessionManager.closeSession(userSession.getSessionId());
            return null;
        }

        bindSessionToClient(userSession.getSessionId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("session_id", userSession.getSessionId());
        data.put("workspace_name", userSession.getWorkspaceName());
        data.put("workspace_uuid", userSession.getWorkspaceUuid());
        data.put("status", "reconnected");
        data.put("max_security_level", "L" + userSession.getMaxSecurityLevel());
        return successResult(data, "Reconnected to existing session for user " + userId + ".");
    }

    /**
     * 执行 SDK 初始化并创建新 session。
     */
    private Map<String, Object> doConnect(String serverAddress, int port, String accessToken,
                                           String workspaceUuid, String notificationServerAddress,
                                           RequestContext reqCtx) {
        try {
            log.info("Initializing SDK: {} : {} ...", serverAddress, port);
            String httpAuthKey = properties.getAuth().getHttpAuthKey();
            String httpAuthValue = properties.getAuth().getHttpAuthValue();
            long sdkSessionId = sdkService.initialize(serverAddress, port, accessToken, workspaceUuid,
                    notificationServerAddress, httpAuthKey, httpAuthValue);

            if (sdkSessionId < 0) {
                return errorResult("Connection failed",
                        "Failed to connect to FlightHub. Check server address, port, and access token.");
            }

            String wsLabel = (workspaceUuid != null && workspaceUuid.length() >= 8)
                    ? workspaceUuid.substring(0, 8) : (workspaceUuid != null ? workspaceUuid : "default");
            String workspaceName = "Workspace-" + wsLabel;
            String effectiveWsUuid = workspaceUuid != null ? workspaceUuid : "";

            String currentUserId = (reqCtx != null) ? reqCtx.getUserId() : null;
            SessionContext session = (currentUserId != null)
                    ? sessionManager.createSessionForUser(currentUserId, effectiveWsUuid, workspaceName)
                    : sessionManager.createSession(effectiveWsUuid, workspaceName);

            bindSessionToClient(session.getSessionId());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("session_id", session.getSessionId());
            data.put("workspace_name", workspaceName);
            data.put("workspace_uuid", effectiveWsUuid);
            data.put("server_address", serverAddress);
            data.put("connected_devices", 0);
            data.put("drones_online", 0);
            data.put("docks_online", 0);
            data.put("status", "ready");
            data.put("max_security_level", "L" + session.getMaxSecurityLevel());

            log.info("fh_connect: success, session_id={}", session.getSessionId());
            return successResultWithNextSteps(data, "Successfully connected to FlightHub. Session is ready.",
                    List.of(
                            Map.of("action", "fh_device_list", "reason", "See available devices"),
                            Map.of("action", "fh_session_info", "reason", "View session details"),
                            Map.of("action", "fh_drone_snapshot", "reason", "Get real-time drone status")
                    ));
        } catch (Exception e) {
            log.error("fh_connect failed: {}", e.getMessage(), e);
            return errorResult("Connection failed",
                    "Failed to connect to FlightHub: " + e.getMessage()
                    + ". Please verify server address, port, and that FH_ACCESS_TOKEN / FH_WORKSPACE_UUID"
                    + " environment variables are correctly set.");
        }
    }

    /**
     * 从 HTTP server_address + port 推导 WebSocket 通知地址。
     * 规则：http → ws，https → wss，拼接固定路径。
     * 如果 server_address 已包含端口号则跳过 port 拼接，避免产生畸形 URL。
     *
     * @param serverAddress HTTP 地址（如 http://your-server.com 或 http://your-server.com:30812）
     * @param port          端口号（仅在 serverAddress 不含端口时使用）
     * @return 完整的 WebSocket 通知地址
     */
    private static String deriveWebSocketUrl(String serverAddress, int port) {
        String base = serverAddress.trim();
        // 协议转换：http → ws，https → wss
        if (base.startsWith("https://")) {
            base = "wss://" + base.substring("https://".length());
        } else if (base.startsWith("http://")) {
            base = "ws://" + base.substring("http://".length());
        } else {
            base = "ws://" + base;
        }
        // 去除尾部斜杠
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        // 检测 host 部分是否已包含端口（格式 ://host:digits），避免重复拼接
        String hostPart = base.substring(base.indexOf("://") + 3);
        if (hostPart.matches(".*:\\d+$")) {
            return base + WS_NOTIFICATION_PATH;
        }
        return base + ":" + port + WS_NOTIFICATION_PATH;
    }

    /**
     * 将 session 绑定到当前请求的 SSE 客户端。
     *
     * <p>通过 {@link RequestContext} 获取当前 clientId，调用 {@link ClientRegistry#bindSession}。
     * 在 session 创建后立即调用，消除 disconnect 线程与绑定之间的 TOCTOU 竞态。
     *
     * <p>Stdio 模式下 clientId 为 "stdio"，ClientRegistry 中无此 entry，bindSession 返回 false — 这是预期行为。
     *
     * @param sessionId 新创建的 session ID
     */
    private void bindSessionToClient(String sessionId) {
        RequestContext ctx = RequestContext.current();
        if (ctx == null) {
            log.debug("bindSessionToClient: 无 RequestContext（可能在测试环境）, sessionId={}", sessionId);
            return;
        }
        String clientId = ctx.getClientId();
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        boolean bound = clientRegistry.bindSession(clientId, sessionId);
        if (bound) {
            log.info("Session 绑定到客户端: clientId={}, sessionId={}", clientId, sessionId);
        } else {
            // SSE 客户端已断开，或 Stdio 模式 — 非致命，session 仍可用
            log.debug("Session 绑定跳过（客户端不在 registry 中）: clientId={}, sessionId={}", clientId, sessionId);
        }
    }
}
