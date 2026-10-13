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

package com.dji.fh.feature.user.service;

import com.dji.fh.feature.user.UserProperties;
import com.dji.fh.feature.user.dto.AuthResponse;
import com.dji.fh.feature.user.dto.LoginRequest;
import com.dji.fh.feature.user.dto.RegisterRequest;
import com.dji.fh.feature.user.dto.UserInfo;
import com.dji.fh.feature.user.entity.UserEntity;
import com.dji.fh.feature.user.repository.UserRepository;
import com.dji.fh.feature.user.security.JwtTokenProvider;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户业务逻辑 — 注册、登录、审核、管理。
 *
 * @author DJI DevKit Team
 */
public class UserService {

    /** 审核状态常量。 */
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    /** 登录失败统一错误消息（防止用户名枚举攻击）。 */
    private static final String LOGIN_FAILED_MESSAGE = "用户名或密码错误";

    /** 密码最小长度（注册/重置/修改统一策略）。 */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** 密码长度不足错误消息。 */
    private static final String PASSWORD_TOO_SHORT_MESSAGE = "密码至少 " + MIN_PASSWORD_LENGTH + " 位";

    /** 密码复杂度不足错误消息。 */
    private static final String PASSWORD_COMPLEXITY_MESSAGE = "密码需包含大写字母、小写字母和数字";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserProperties properties;
    private final PasswordEncoder passwordEncoder;

    /**
     * 构造函数。
     *
     * @param userRepository   用户数据访问
     * @param jwtTokenProvider JWT token 签发器
     * @param properties       用户模块配置
     */
    public UserService(UserRepository userRepository,
                       JwtTokenProvider jwtTokenProvider,
                       UserProperties properties) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.properties = properties;
        this.passwordEncoder = new BCryptPasswordEncoder(10);
    }

    /**
     * 用户注册。首个注册用户自动成为 admin 并直接 approved。
     *
     * <p>使用 SERIALIZABLE 隔离级别防止并发首注册产生多个 admin（TOCTOU 竞态）。
     * 数据库唯一约束兜底防止重复用户名。
     *
     * @param request 注册请求
     * @return 认证响应（admin 含 token，普通用户 token 为 null + 提示消息）
     * @throws IllegalArgumentException 用户名或邮箱已存在
     */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public AuthResponse register(RegisterRequest request) {
        // 安全发布专项修复：开放注册默认关闭，防止公网被匿名占领首用户 admin
        if (!properties.registration().enabled()) {
            throw new IllegalArgumentException("注册功能未开放，请联系管理员创建账号");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("用户名 '" + request.username() + "' 已存在");
        }

        if (request.email() != null && !request.email().isBlank()
                && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("邮箱 '" + request.email() + "' 已被注册");
        }

        boolean isFirstUser = userRepository.count() == 0;
        String role = isFirstUser ? "admin" : properties.registration().defaultRole();
        String status = isFirstUser ? STATUS_APPROVED : STATUS_PENDING;
        String hash = passwordEncoder.encode(request.password());

        UserEntity entity = new UserEntity(
                request.username(), hash, request.nickname(), request.email(), role);
        entity.setStatus(status);
        try {
            entity = userRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("用户名 '" + request.username() + "' 已存在");
        }

        if (isFirstUser) {
            String token = jwtTokenProvider.generateToken(
                    entity.getId(), entity.getUsername(), role, entity.getPasswordVersion());
            return new AuthResponse(token, toUserInfo(entity));
        }
        return new AuthResponse(null, toUserInfo(entity), "注册成功，请等待管理员审核");
    }

    /**
     * 用户登录。
     *
     * <p>密码错误、用户不存在、账号禁用统一返回相同错误消息，防止枚举攻击。
     * 账号待审核返回明确提示（非安全敏感信息）。
     *
     * @param request 登录请求
     * @return 认证响应（含 token）
     * @throws IllegalArgumentException 认证失败
     */
    public AuthResponse login(LoginRequest request) {
        UserEntity entity = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException(LOGIN_FAILED_MESSAGE));

        if (!entity.isEnabled()) {
            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }

        if (!passwordEncoder.matches(request.password(), entity.getPasswordHash())) {
            throw new IllegalArgumentException(LOGIN_FAILED_MESSAGE);
        }

        if (!STATUS_APPROVED.equals(entity.getStatus())) {
            throw new IllegalArgumentException("账号尚未通过审核，请联系管理员");
        }

        String token = jwtTokenProvider.generateToken(
                entity.getId(), entity.getUsername(), entity.getRole(), entity.getPasswordVersion());
        return new AuthResponse(token, toUserInfo(entity));
    }

    /**
     * 获取当前用户信息。
     *
     * @param userId 用户 ID
     * @return 用户信息
     * @throws IllegalArgumentException 用户不存在
     */
    public UserInfo getCurrentUser(Long userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return toUserInfo(entity);
    }

    /**
     * 获取所有用户列表（admin 使用）。
     *
     * @return 用户信息列表
     */
    public List<UserInfo> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserInfo)
                .toList();
    }

    /**
     * 启用/禁用用户。
     *
     * @param userId  目标用户 ID
     * @param enabled 是否启用
     * @throws IllegalArgumentException 用户不存在
     */
    public void setEnabled(Long userId, boolean enabled) {
        UserEntity entity = findUserOrThrow(userId);
        entity.setEnabled(enabled);
        userRepository.save(entity);
    }

    /**
     * 审核通过用户。
     *
     * @param userId 目标用户 ID
     * @throws IllegalArgumentException 用户不存在
     */
    public void approveUser(Long userId) {
        UserEntity entity = findUserOrThrow(userId);
        entity.setStatus(STATUS_APPROVED);
        userRepository.save(entity);
    }

    /**
     * 审核拒绝用户。
     *
     * @param userId 目标用户 ID
     * @throws IllegalArgumentException 用户不存在
     */
    public void rejectUser(Long userId) {
        UserEntity entity = findUserOrThrow(userId);
        entity.setStatus(STATUS_REJECTED);
        userRepository.save(entity);
    }

    /**
     * 变更用户角色。
     *
     * @param userId 目标用户 ID
     * @param role   新角色（admin / user）
     * @throws IllegalArgumentException 用户不存在或角色无效
     */
    public void setRole(Long userId, String role) {
        if (!"admin".equals(role) && !"user".equals(role)) {
            throw new IllegalArgumentException("无效角色: " + role);
        }
        UserEntity entity = findUserOrThrow(userId);
        // 防止降级唯一管理员导致系统锁死
        if ("admin".equals(entity.getRole()) && !"admin".equals(role)) {
            requireNotLastAdmin();
        }
        entity.setRole(role);
        userRepository.save(entity);
    }

    /**
     * 管理员重置用户密码。
     *
     * <p>允许 admin 重置任意用户密码，包括自己（管理员此前无法重置自己的密码，
     * 必须依赖其他管理员或后台介入，存在实际运维痛点）。admin 重置自己时
     * 建议优先使用 {@link #changePassword}（校验旧密码），本方法作为管理员通道保留。
     *
     * @param userId      目标用户 ID
     * @param newPassword 新密码（明文，内部 BCrypt 编码）
     * @throws IllegalArgumentException 用户不存在或密码太短
     */
    public void resetPassword(Long userId, String newPassword) {
        validatePasswordStrength(newPassword);
        UserEntity entity = findUserOrThrow(userId);
        entity.setPasswordHash(passwordEncoder.encode(newPassword));
        entity.incrementPasswordVersion();
        userRepository.save(entity);
    }

    /**
     * 当前用户修改自己的密码（需校验旧密码）。
     *
     * <p>改密后自动递增 passwordVersion，使所有已签发的旧 JWT 立即失效。
     * 调用者需提示用户使用新密码重新登录。
     *
     * @param userId      当前用户 ID（由 controller 从 SecurityContext 提取）
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文，内部 BCrypt 编码）
     * @throws IllegalArgumentException 用户不存在、旧密码错误或新密码不合规
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        validatePasswordStrength(newPassword);
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("旧密码不能为空");
        }
        UserEntity entity = findUserOrThrow(userId);
        if (!passwordEncoder.matches(oldPassword, entity.getPasswordHash())) {
            throw new IllegalArgumentException("旧密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }
        entity.setPasswordHash(passwordEncoder.encode(newPassword));
        entity.incrementPasswordVersion();
        userRepository.save(entity);
    }

    /**
     * 删除用户。
     *
     * @param userId 目标用户 ID
     * @throws IllegalArgumentException 用户不存在
     */
    public void deleteUser(Long userId) {
        UserEntity entity = findUserOrThrow(userId);
        // 防止删除唯一管理员导致系统锁死
        if ("admin".equals(entity.getRole())) {
            requireNotLastAdmin();
        }
        userRepository.delete(entity);
    }

    /**
     * 按 ID 查找用户，不存在则抛异常。
     */
    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    /**
     * 校验密码强度（注册/重置/修改统一策略）。
     *
     * <p>规则：至少 8 字符，且包含大写、小写和数字。
     *
     * @param password 待校验密码（明文）
     * @throws IllegalArgumentException 密码为 null 或不满足策略
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(PASSWORD_TOO_SHORT_MESSAGE);
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new IllegalArgumentException(PASSWORD_COMPLEXITY_MESSAGE);
        }
    }

    /**
     * 校验系统中是否存在多于一个 admin，防止唯一管理员被删除或降级。
     */
    private void requireNotLastAdmin() {
        if (userRepository.countByRole("admin") <= 1) {
            throw new IllegalArgumentException("无法操作：系统至少需要保留一个管理员");
        }
    }

    private UserInfo toUserInfo(UserEntity entity) {
        return new UserInfo(entity.getId(), entity.getUsername(),
                entity.getNickname(), entity.getRole(),
                entity.getEmail(), entity.isEmailVerified(),
                entity.getStatus(), entity.getCreatedAt());
    }
}
