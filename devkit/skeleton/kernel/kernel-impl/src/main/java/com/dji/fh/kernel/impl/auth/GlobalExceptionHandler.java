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

package com.dji.fh.kernel.impl.auth;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dji.fh.kernel.spi.auth.InvalidTokenException;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;

/**
 * 全局异常处理器 — 将框架层鉴权异常统一映射为规范 HTTP 响应。
 *
 * <p>状态码区分：
 * <ul>
 *   <li>未认证（匿名用户访问 @RequireAuthenticated 端点）→ HTTP 401 Unauthorized</li>
 *   <li>已认证但权限不足（@RequireRole / @RequirePermission 校验失败）→ HTTP 403 Forbidden</li>
 * </ul>
 *
 * <p>ISV 覆盖方式（任选其一）：
 * <ul>
 *   <li>声明 {@code @RestControllerAdvice @Order(Ordered.HIGHEST_PRECEDENCE)} 更高优先级的自定义 Advice</li>
 *   <li>排除本 AutoConfiguration：{@code @SpringBootApplication(exclude = AuthAutoConfiguration.class)}</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理鉴权失败异常。
     *
     * <p>根据当前 SecurityContext 是否已认证区分 401/403：
     * <ul>
     *   <li>未认证 → 401 Unauthorized（{@code code: "unauthenticated"}）</li>
     *   <li>已认证但权限不足 → 403 Forbidden（{@code code: "forbidden"}）</li>
     * </ul>
     *
     * @param ex 鉴权失败异常
     * @return 包含错误码和消息的响应体
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        boolean authenticated = SecurityContextHolder.current().isAuthenticated();
        String requestUri = resolveRequestUri();

        if (!authenticated) {
            LOG.warn("认证失败: uri={}, reason={}", requestUri, ex.getMessage());
            Map<String, Object> body = Map.of(
                    "code", "unauthenticated",
                    "message", ex.getMessage()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }

        LOG.warn("权限不足: uri={}, principal={}, reason={}",
                requestUri, SecurityContextHolder.current().principalId(), ex.getMessage());
        Map<String, Object> body = Map.of(
                "code", "forbidden",
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * 处理无效凭证异常（安全发布专项）。
     *
     * <p>携带无效/过期 token 的请求在认证过滤阶段抛 {@link InvalidTokenException}，
     * 此处统一映射为 HTTP 401 Unauthorized，与匿名未认证的语义一致但错误码区分。
     *
     * <p>注意：过滤阶段抛出的异常（DispatcherServlet 之前）不会到达本处理器 ——
     * {@code AuthenticatingSecurityFilter} 已直接写 401 JSON。本方法仅作为
     * 业务层仍抛出该异常时的兜底，响应体同样使用固定文案，不回显 JWT 细节。
     *
     * @param ex 无效凭证异常
     * @return 401 响应
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        String requestUri = resolveRequestUri();
        LOG.warn("无效凭证被拒绝: uri={}, reason={}", requestUri, ex.getMessage());
        Map<String, Object> body = Map.of(
                "code", "invalid_token",
                "message", "无效或已过期的访问凭证，请重新登录"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * 尝试从 RequestContext 获取当前请求 URI，用于日志上下文。
     */
    private static String resolveRequestUri() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return request.getMethod() + " " + request.getRequestURI();
            }
        } catch (Exception ignored) {
            // RequestContextHolder 不可用时静默降级
        }
        return "unknown";
    }
}
