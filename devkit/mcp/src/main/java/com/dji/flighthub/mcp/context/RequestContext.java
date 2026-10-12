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

package com.dji.flighthub.mcp.context;

import com.dji.flighthub.mcp.session.SessionContext;

import java.time.Instant;

/**
 * 请求级别上下文 — 通过 ThreadLocal 在 transport 层设置，工具执行链路内可读取。
 *
 * <p>生命周期：
 * <ul>
 *   <li>Transport.handleToolsCall() 开头 → {@link #set(Object, String, String)}</li>
 *   <li>工具执行期间 → {@link #current()} 获取当前上下文</li>
 *   <li>Transport.handleToolsCall() finally → {@link #clear()}</li>
 * </ul>
 *
 * <p>用于：审计日志、幂等缓存 key 构建。
 *
 * @author DJI FlightHub SDK Team
 */
public class RequestContext {

    private static final ThreadLocal<RequestContext> HOLDER = new ThreadLocal<>();

    /**
     * Stdio 模式下的客户端标识常量。
     *
     * <p>{@link com.dji.flighthub.mcp.transport.StdioTransport} 在设置请求上下文时固定使用此值；
     * {@link com.dji.flighthub.mcp.session.SessionManager} 据此识别单 session 回退路径，
     * 避免 stdio 模式下因 {@code ClientRegistry} 无 "stdio" entry 而误返回 null。
     */
    public static final String STDIO_CLIENT_ID = "stdio";

    /** JSON-RPC request id（String 或 Integer，由 client 生成） */
    private final Object requestId;

    /** 被调用的工具名（如 "fh_takeoff"） */
    private final String toolName;

    /** 客户端标识（SSE 模式为 clientId，Stdio 模式为 {@link #STDIO_CLIENT_ID}） */
    private final String clientId;

    /** 请求到达时间 */
    private final Instant timestamp;

    /** Session ID（transport 层 eager set 或通过 enrichWithSession 补充） */
    private String sessionId;

    /** Workspace UUID（通过 enrichWithSession 补充） */
    private String workspaceUuid;

    /** 用户标识（JWT 模式下由 transport 层从认证结果提取并设置） */
    private String userId;

    private RequestContext(Object requestId, String toolName, String clientId) {
        this.requestId = requestId;
        this.toolName = toolName;
        this.clientId = clientId;
        this.timestamp = Instant.now();
    }

    /**
     * 在 transport 层设置当前请求上下文。
     *
     * @param requestId JSON-RPC request id
     * @param toolName  被调用的工具名
     * @param clientId  客户端标识
     */
    public static void set(Object requestId, String toolName, String clientId) {
        HOLDER.set(new RequestContext(requestId, toolName, clientId));
    }

    /**
     * 获取当前线程的请求上下文。
     *
     * @return 当前上下文，未设置时返回 null
     */
    public static RequestContext current() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程的请求上下文（防止 ThreadLocal 泄漏）。
     * 必须在 finally 块中调用。
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 补充 session 信息（在 tool 执行时通过 sessionManager 获取后调用）。
     *
     * @param session 活跃 session 上下文
     */
    public void enrichWithSession(SessionContext session) {
        if (session != null) {
            this.sessionId = session.getSessionId();
            this.workspaceUuid = session.getWorkspaceUuid();
        }
    }

    /**
     * 构建幂等缓存 key。
     * 格式: clientId:toolName:requestId（三段式，避免跨工具碰撞）
     *
     * <p>JSON-RPC 2.0 规范仅要求 requestId 在当前未完成请求中唯一，
     * 客户端完全可以复用 ID（如递增整数从 1 开始，每次重连重置）。
     * 因此 key 必须包含 toolName 才能区分不同工具的调用。
     *
     * @return 幂等 key，requestId 为 null 时返回 null（不做幂等）
     */
    public String buildIdempotencyKey() {
        if (requestId == null) {
            return null;
        }
        return clientId + ":" + toolName + ":" + requestId;
    }

    /**
     * 获取 JSON-RPC request id。
     *
     * @return request id（String 或 Integer，由 client 生成）
     */
    public Object getRequestId() {
        return requestId;
    }

    /**
     * 获取被调用的工具名。
     *
     * @return 工具名（如 "fh_takeoff"）
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * 获取客户端标识。
     *
     * @return 客户端 ID（SSE 模式为 clientId，Stdio 模式为 {@link #STDIO_CLIENT_ID}）
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 获取请求到达时间。
     *
     * @return 请求时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * 获取 Session ID（通过 enrichWithSession 补充或 transport 层 eager set）。
     *
     * @return session ID，未设置时为 null
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 由 transport 层主动设置 session ID（多 session 模式下，通过 ClientRegistry 预解析）。
     *
     * @param sessionId 已解析的 session ID
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * 获取 Workspace UUID（通过 enrichWithSession 补充）。
     *
     * @return workspace UUID，未补充时为 null
     */
    public String getWorkspaceUuid() {
        return workspaceUuid;
    }

    /**
     * 获取用户标识（JWT 认证模式下可用）。
     *
     * @return 用户 ID，非 JWT 模式或未设置时为 null
     */
    public String getUserId() {
        return userId;
    }

    /**
     * 由 transport 层设置用户标识（从 JWT token 的 sub claim 提取）。
     *
     * @param userId 用户 ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
