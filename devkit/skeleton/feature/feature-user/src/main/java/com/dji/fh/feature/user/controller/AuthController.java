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

package com.dji.fh.feature.user.controller;

import com.dji.fh.feature.user.dto.AuthResponse;
import com.dji.fh.feature.user.dto.LoginRequest;
import com.dji.fh.feature.user.dto.RegisterRequest;
import com.dji.fh.feature.user.dto.UserInfo;
import com.dji.fh.feature.user.service.UserService;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证与管理 REST API。
 *
 * @author DJI DevKit Team
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    /**
     * 构造函数。
     *
     * @param userService 用户业务逻辑
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 公开端点 ====================

    /**
     * 用户注册。
     *
     * @param request 注册请求
     * @return 认证响应（首个用户含 token，后续用户 token 为 null + 审核提示）
     */
    @PostMapping("/auth/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 用户登录。
     *
     * @param request 登录请求
     * @return 认证响应（含 JWT token）
     */
    @PostMapping("/auth/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 认证端点 ====================

    /**
     * 获取当前用户信息（需已认证）。
     *
     * @return 用户信息
     */
    @GetMapping("/users/me")
    public ResponseEntity<Object> me() {
        SecurityContext ctx = SecurityContextHolder.current();
        if (!ctx.isAuthenticated()) {
            return unauthorized();
        }
        Long userId = extractUserId(ctx);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "无法获取用户身份"));
        }
        UserInfo info = userService.getCurrentUser(userId);
        return ResponseEntity.ok(info);
    }

    /**
     * 修改自己的密码（任何已认证用户，含管理员）。
     *
     * <p>解决"管理员无法重置自己密码"的运维痛点：管理员改自己密码走此端点，
     * 需校验旧密码，防止会话被劫持后直接改密锁号。请求体需包含
     * {@code oldPassword} 与 {@code newPassword}。
     *
     * <p><b>安全限制</b>：本端点仅校验旧密码，不失效已签发的 JWT token。
     * 若攻击者已窃取 token，受害者改密后旧 token 在过期前仍有效。
     * 完整的会话失效需配合 token 版本化，不在本改动范围。
     *
     * @param body 请求体
     * @return 200 或错误
     */
    @PutMapping("/users/me/password")
    public ResponseEntity<Object> changeMyPassword(@RequestBody Map<String, String> body) {
        SecurityContext ctx = SecurityContextHolder.current();
        if (!ctx.isAuthenticated()) {
            return unauthorized();
        }
        Long userId = extractUserId(ctx);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "无法获取用户身份"));
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "缺少 oldPassword 字段"));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "缺少 newPassword 字段"));
        }
        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== 管理员端点 ====================

    /**
     * 用户列表（仅 admin）。
     *
     * @return 用户信息列表
     */
    @GetMapping("/users")
    public ResponseEntity<Object> listUsers() {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        return ResponseEntity.ok(userService.listUsers());
    }

    /**
     * 启用/禁用用户（仅 admin）。
     *
     * @param id   用户 ID
     * @param body 请求体，需包含 enabled 字段
     * @return 200 或错误
     */
    @PutMapping("/users/{id}/enabled")
    public ResponseEntity<Object> setEnabled(@PathVariable("id") Long id,
                                             @RequestBody Map<String, Boolean> body) {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "缺少 enabled 字段"));
        }
        return executeAdminAction(() -> userService.setEnabled(id, enabled));
    }

    /**
     * 审核通过用户（仅 admin）。
     *
     * @param id 用户 ID
     * @return 200 或错误
     */
    @PutMapping("/users/{id}/approve")
    public ResponseEntity<Object> approveUser(@PathVariable("id") Long id) {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        return executeAdminAction(() -> userService.approveUser(id));
    }

    /**
     * 审核拒绝用户（仅 admin）。
     *
     * @param id 用户 ID
     * @return 200 或错误
     */
    @PutMapping("/users/{id}/reject")
    public ResponseEntity<Object> rejectUser(@PathVariable("id") Long id) {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        return executeAdminAction(() -> userService.rejectUser(id));
    }

    /**
     * 变更用户角色（仅 admin）。
     *
     * @param id   用户 ID
     * @param body 请求体，需包含 role 字段
     * @return 200 或错误
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<Object> setRole(@PathVariable("id") Long id,
                                          @RequestBody Map<String, String> body) {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "缺少 role 字段"));
        }
        return executeAdminAction(() -> userService.setRole(id, role));
    }

    /**
     * 管理员重置用户密码（仅 admin）。
     *
     * @param id   用户 ID
     * @param body 请求体，需包含 password 字段
     * @return 200 或错误
     */
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<Object> resetPassword(@PathVariable("id") Long id,
                                                @RequestBody Map<String, String> body) {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        String password = body.get("password");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "缺少 password 字段"));
        }
        return executeAdminAction(() -> userService.resetPassword(id, password));
    }

    /**
     * 删除用户（仅 admin）。
     *
     * @param id 用户 ID
     * @return 200 或错误
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable("id") Long id) {
        ResponseEntity<Object> guard = requireAdmin();
        if (guard != null) {
            return guard;
        }
        return executeAdminAction(() -> userService.deleteUser(id));
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 管理员权限校验。通过返回 null，失败返回 401/403 响应。
     */
    private ResponseEntity<Object> requireAdmin() {
        SecurityContext ctx = SecurityContextHolder.current();
        if (!ctx.isAuthenticated()) {
            return unauthorized();
        }
        if (!ctx.hasRole("admin")) {
            return forbidden();
        }
        return null;
    }

    /**
     * 执行管理操作，统一捕获 IllegalArgumentException。
     */
    private ResponseEntity<Object> executeAdminAction(Runnable action) {
        try {
            action.run();
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private ResponseEntity<Object> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("error", "未认证"));
    }

    private ResponseEntity<Object> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "权限不足"));
    }

    /**
     * 从 SecurityContext 安全提取 userId，防止 NPE 和 ClassCastException。
     *
     * @param ctx 安全上下文
     * @return userId，提取失败返回 null
     */
    private Long extractUserId(SecurityContext ctx) {
        Object raw = ctx.attributes().get("userId");
        if (raw instanceof Long longVal) {
            return longVal;
        }
        if (raw instanceof Number numVal) {
            return numVal.longValue();
        }
        return null;
    }
}
