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

package com.dji.flighthub.mcp.security;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * MCP 传输层鉴权接口。
 * 定义请求级别的认证策略，支持扩展不同的认证方式（Bearer Token、OAuth、mTLS 等）。
 *
 * <p>实现约定：
 * <ul>
 *   <li>authenticate() 返回 null 表示认证通过</li>
 *   <li>authenticate() 返回非 null {@link AuthResult} 表示认证失败，包含错误信息</li>
 *   <li>实现类必须是线程安全的</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
public interface McpAuthenticator {

    /**
     * 对传入请求进行认证。
     *
     * @param request HTTP 请求（包含 header、query params 等）
     * @return null 表示认证通过；非 null 表示认证失败
     */
    AuthResult authenticate(ServerHttpRequest request);

    /**
     * 返回认证方式名称，用于日志和错误消息。
     *
     * @return 认证方式标识，如 "bearer-token"、"oauth2"
     */
    String scheme();

    /**
     * 是否启用此认证器。未启用时跳过认证（开发/调试场景）。
     *
     * @return true 表示启用认证拦截
     */
    boolean isEnabled();

    /**
     * 认证结果 — 表示认证成功（携带已验证的 claims）或认证失败（携带错误信息）。
     *
     * <p>httpStatus == 0 表示成功，> 0 表示失败。
     * 成功结果通过 {@link #success(String)} 创建，可携带服务端验证过的 userId，
     * 防止客户端通过伪造 header 进行会话劫持。
     */
    record AuthResult(int httpStatus, String error, String detail, String userId) {

        /** 认证成功（无 claims，兼容非 JWT 认证器） */
        public static AuthResult success() {
            return new AuthResult(0, null, null, null);
        }

        /** 认证成功并携带服务端验证过的 userId（JWT 认证器专用） */
        public static AuthResult success(String userId) {
            return new AuthResult(0, null, null, userId);
        }

        /** 401 未认证 */
        public static AuthResult unauthorized(String detail) {
            return new AuthResult(401, "Unauthorized", detail, null);
        }

        /** 403 已认证但权限不足 */
        public static AuthResult forbidden(String detail) {
            return new AuthResult(403, "Forbidden", detail, null);
        }

        /** 是否认证成功 */
        public boolean isSuccess() {
            return httpStatus == 0;
        }
    }
}
