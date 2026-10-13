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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dji.fh.feature.user.UserProperties;
import com.dji.fh.feature.user.dto.AuthResponse;
import com.dji.fh.feature.user.dto.LoginRequest;
import com.dji.fh.feature.user.dto.RegisterRequest;
import com.dji.fh.feature.user.dto.UserInfo;
import com.dji.fh.feature.user.entity.UserEntity;
import com.dji.fh.feature.user.repository.UserRepository;
import com.dji.fh.feature.user.security.JwtTokenProvider;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

/**
 * UserService 集成测试。
 *
 * @author DJI DevKit Team
 */
@DataJpaTest
@ContextConfiguration(classes = UserServiceTest.Config.class)
class UserServiceTest {

    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.dji.fh.feature.user.entity")
    @EnableJpaRepositories(basePackages = "com.dji.fh.feature.user.repository")
    static class Config {
    }

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        UserProperties props = new UserProperties(
                new UserProperties.Jwt("test-secret-key-at-least-32-characters-long!!", Duration.ofHours(1)),
                new UserProperties.Registration(true, "user")
        );
        JwtTokenProvider tokenProvider = new JwtTokenProvider(props);
        userService = new UserService(userRepository, tokenProvider, props);
    }

    // ==================== 注册 ====================

    @Test
    void register_firstUser_shouldBeAdminAndApproved() {
        AuthResponse response = userService.register(
                new RegisterRequest("firstuser", "Password123", "First", null));

        assertThat(response.user().role()).isEqualTo("admin");
        assertThat(response.user().status()).isEqualTo("approved");
        assertThat(response.token()).isNotBlank();
        assertThat(response.message()).isNull();
    }

    @Test
    void register_secondUser_shouldBePendingWithoutToken() {
        userService.register(new RegisterRequest("first", "Password123", null, null));
        AuthResponse response = userService.register(
                new RegisterRequest("second", "Password123", null, null));

        assertThat(response.user().role()).isEqualTo("user");
        assertThat(response.user().status()).isEqualTo("pending");
        assertThat(response.token()).isNull();
        assertThat(response.message()).contains("审核");
    }

    @Test
    void register_duplicateUsername_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));

        assertThatThrownBy(() ->
                userService.register(new RegisterRequest("alice", "Password123", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void register_duplicateEmail_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, "a@test.com"));

        assertThatThrownBy(() ->
                userService.register(new RegisterRequest("bob", "Password123", null, "a@test.com")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("邮箱");
    }

    // ==================== 登录 ====================

    @Test
    void login_approvedUser_shouldReturnToken() {
        // 首个用户自动 approved
        userService.register(new RegisterRequest("alice", "Password123", "Alice", null));

        AuthResponse response = userService.login(new LoginRequest("alice", "Password123"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().username()).isEqualTo("alice");
    }

    @Test
    void login_pendingUser_shouldThrowWithReviewMessage() {
        userService.register(new RegisterRequest("admin", "Password123", null, null));
        userService.register(new RegisterRequest("bob", "Password123", null, null));

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("bob", "Password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("审核");
    }

    @Test
    void login_wrongPassword_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("alice", "wrongpassword")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_nonExistentUser_shouldThrow() {
        assertThatThrownBy(() ->
                userService.login(new LoginRequest("nobody", "Password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    @Test
    void login_disabledUser_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();
        entity.setEnabled(false);
        userRepository.save(entity);

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("alice", "Password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户名或密码错误");
    }

    // ==================== 审核管理 ====================

    @Test
    void approveUser_shouldAllowLogin() {
        userService.register(new RegisterRequest("admin", "Password123", null, null));
        AuthResponse pendingResponse = userService.register(
                new RegisterRequest("bob", "Password123", null, null));
        Long bobId = pendingResponse.user().id();

        userService.approveUser(bobId);

        AuthResponse loginResponse = userService.login(new LoginRequest("bob", "Password123"));
        assertThat(loginResponse.token()).isNotBlank();
    }

    @Test
    void rejectUser_shouldPreventLogin() {
        userService.register(new RegisterRequest("admin", "Password123", null, null));
        AuthResponse pendingResponse = userService.register(
                new RegisterRequest("bob", "Password123", null, null));
        Long bobId = pendingResponse.user().id();

        userService.rejectUser(bobId);

        assertThatThrownBy(() ->
                userService.login(new LoginRequest("bob", "Password123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("审核");
    }

    @Test
    void setRole_shouldChangeRole() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        // 注册第二个 admin，确保 alice 不是唯一 admin
        userService.register(new RegisterRequest("bob", "Password123", null, null));
        UserEntity bob = userRepository.findByUsername("bob").orElseThrow();
        userService.approveUser(bob.getId());
        userService.setRole(bob.getId(), "admin");

        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();
        userService.setRole(alice.getId(), "user");

        UserInfo info = userService.getCurrentUser(alice.getId());
        assertThat(info.role()).isEqualTo("user");
    }

    @Test
    void setRole_invalidRole_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() -> userService.setRole(entity.getId(), "superadmin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("无效角色");
    }

    @Test
    void resetPassword_shouldAllowLoginWithNewPassword() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        userService.resetPassword(entity.getId(), "newPassword123");

        AuthResponse response = userService.login(new LoginRequest("alice", "newPassword123"));
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void resetPassword_tooShort_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() -> userService.resetPassword(entity.getId(), "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 位");
    }

    @Test
    void changePassword_correctOld_shouldSucceedAndAllowLoginWithNewPassword() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        userService.changePassword(entity.getId(), "Password123", "NewPass456");

        // 旧密码应已失效
        assertThatThrownBy(() -> userService.login(new LoginRequest("alice", "Password123")))
                .isInstanceOf(IllegalArgumentException.class);
        // 新密码可登录
        AuthResponse response = userService.login(new LoginRequest("alice", "NewPass456"));
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void changePassword_wrongOld_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() ->
                userService.changePassword(entity.getId(), "wrongpass", "NewPass456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("旧密码错误");
        // 原密码仍可登录（未变更）
        AuthResponse response = userService.login(new LoginRequest("alice", "Password123"));
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void changePassword_sameAsOld_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() ->
                userService.changePassword(entity.getId(), "Password123", "Password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不能与旧密码相同");
    }

    @Test
    void changePassword_tooShortNew_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() ->
                userService.changePassword(entity.getId(), "Password123", "short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("密码至少")
                .hasMessageContaining("8 位");
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        // 注册第二个用户，确保 alice 不是唯一 admin
        userService.register(new RegisterRequest("bob", "Password123", null, null));
        userService.approveUser(userRepository.findByUsername("bob").orElseThrow().getId());
        userService.setRole(userRepository.findByUsername("bob").orElseThrow().getId(), "admin");

        UserEntity alice = userRepository.findByUsername("alice").orElseThrow();
        userService.deleteUser(alice.getId());

        assertThat(userRepository.findByUsername("alice")).isEmpty();
    }

    @Test
    void deleteUser_lastAdmin_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() -> userService.deleteUser(entity.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("管理员");
    }

    @Test
    void setRole_demoteLastAdmin_shouldThrow() {
        userService.register(new RegisterRequest("alice", "Password123", null, null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        assertThatThrownBy(() -> userService.setRole(entity.getId(), "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("管理员");
    }

    // ==================== 查询 ====================

    @Test
    void getCurrentUser_shouldReturnUserInfo() {
        userService.register(new RegisterRequest("alice", "Password123", "Alice", null));
        UserEntity entity = userRepository.findByUsername("alice").orElseThrow();

        UserInfo info = userService.getCurrentUser(entity.getId());

        assertThat(info.username()).isEqualTo("alice");
        assertThat(info.nickname()).isEqualTo("Alice");
        assertThat(info.status()).isEqualTo("approved");
    }

    @Test
    void listUsers_shouldReturnAll() {
        userService.register(new RegisterRequest("admin", "Password123", null, null));
        userService.register(new RegisterRequest("user1", "Password123", null, null));

        assertThat(userService.listUsers()).hasSize(2);
    }
}
