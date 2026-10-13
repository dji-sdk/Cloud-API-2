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

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.user.dto.AuthResponse;
import com.dji.fh.feature.user.dto.LoginRequest;
import com.dji.fh.feature.user.dto.RegisterRequest;
import com.dji.fh.feature.user.dto.UserInfo;
import com.dji.fh.feature.user.service.UserService;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * AuthController 单元测试。
 *
 * @author DJI DevKit Team
 */
class AuthControllerTest {

    private StubUserService stubService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        stubService = new StubUserService();
        controller = new AuthController(stubService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void register_success_shouldReturnOk() {
        AuthResponse expected = new AuthResponse("jwt-token", new UserInfo(1L, "alice", "Alice", "admin", null, false, "approved", null));
        stubService.registerHandler = req -> expected;

        ResponseEntity<Object> response = controller.register(
                new RegisterRequest("alice", "Password123", "Alice", null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void register_duplicateUser_shouldReturnBadRequest() {
        stubService.registerHandler = req -> {
            throw new IllegalArgumentException("用户名已存在");
        };

        ResponseEntity<Object> response = controller.register(
                new RegisterRequest("alice", "Password123", null, null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }

    @Test
    void login_success_shouldReturnOk() {
        AuthResponse expected = new AuthResponse("jwt-token", new UserInfo(1L, "alice", "Alice", "user", null, false, "approved", null));
        stubService.loginHandler = req -> expected;

        ResponseEntity<Object> response = controller.login(new LoginRequest("alice", "Password123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void login_invalidCredentials_shouldReturnBadRequest() {
        stubService.loginHandler = req -> {
            throw new IllegalArgumentException("用户名或密码错误");
        };

        ResponseEntity<Object> response = controller.login(new LoginRequest("alice", "wrong"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void me_unauthenticated_shouldReturn401() {
        ResponseEntity<Object> response = controller.me();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void me_authenticated_shouldReturnUserInfo() {
        SecurityContextHolder.set(authenticatedContext(1L, "user"));
        UserInfo expected = new UserInfo(1L, "alice", "Alice", "user", null, false, "approved", null);
        stubService.getCurrentUserHandler = id -> expected;

        ResponseEntity<Object> response = controller.me();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void me_authenticatedButNoUserId_shouldReturn401() {
        SecurityContextHolder.set(new SecurityContext() {
            @Override
            public String principalId() {
                return "some-user";
            }

            @Override
            public Set<String> roles() {
                return Set.of("user");
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of();
            }
        });

        ResponseEntity<Object> response = controller.me();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void me_authenticatedWithNumberUserId_shouldReturnUserInfo() {
        SecurityContextHolder.set(new SecurityContext() {
            @Override
            public String principalId() {
                return "user-42";
            }

            @Override
            public Set<String> roles() {
                return Set.of("user");
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of("userId", Integer.valueOf(42));
            }
        });
        UserInfo expected = new UserInfo(42L, "bob", "Bob", "user", null, false, "approved", null);
        stubService.getCurrentUserHandler = id -> expected;

        ResponseEntity<Object> response = controller.me();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void listUsers_unauthenticated_shouldReturn401() {
        ResponseEntity<Object> response = controller.listUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void listUsers_nonAdmin_shouldReturn403() {
        SecurityContextHolder.set(authenticatedContext(1L, "user"));

        ResponseEntity<Object> response = controller.listUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void listUsers_admin_shouldReturnList() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        List<UserInfo> expected = List.of(new UserInfo(1L, "admin", "Admin", "admin", null, false, "approved", null));
        stubService.listUsersResult = expected;

        ResponseEntity<Object> response = controller.listUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    void setEnabled_unauthenticated_shouldReturn401() {
        ResponseEntity<Object> response = controller.setEnabled(1L, Map.of("enabled", false));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void setEnabled_nonAdmin_shouldReturn403() {
        SecurityContextHolder.set(authenticatedContext(1L, "user"));

        ResponseEntity<Object> response = controller.setEnabled(2L, Map.of("enabled", false));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void setEnabled_missingField_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));

        ResponseEntity<Object> response = controller.setEnabled(2L, Map.of());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void setEnabled_success_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));

        ResponseEntity<Object> response = controller.setEnabled(2L, Map.of("enabled", false));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubService.setEnabledCalled).isTrue();
    }

    @Test
    void setEnabled_userNotFound_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        stubService.setEnabledHandler = (id, enabled) -> {
            throw new IllegalArgumentException("用户不存在");
        };

        ResponseEntity<Object> response = controller.setEnabled(99L, Map.of("enabled", true));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== 审核端点 ====================

    @Test
    void approveUser_admin_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.approveUser(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void approveUser_nonAdmin_shouldReturn403() {
        SecurityContextHolder.set(authenticatedContext(1L, "user"));
        ResponseEntity<Object> response = controller.approveUser(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rejectUser_admin_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.rejectUser(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void setRole_admin_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.setRole(2L, Map.of("role", "admin"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void setRole_missingField_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.setRole(2L, Map.of());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resetPassword_admin_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.resetPassword(2L, Map.of("password", "newpass123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void resetPassword_missingField_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.resetPassword(2L, Map.of());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ==================== 修改自己的密码 /users/me/password ====================

    @Test
    void changeMyPassword_admin_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.changeMyPassword(
                Map.of("oldPassword", "oldpass123", "newPassword", "newpass456"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stubService.changePasswordCalled).isTrue();
    }

    @Test
    void changeMyPassword_unauthenticated_shouldReturn401() {
        ResponseEntity<Object> response = controller.changeMyPassword(
                Map.of("oldPassword", "oldpass123", "newPassword", "newpass456"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void changeMyPassword_missingOldPassword_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.changeMyPassword(
                Map.of("newPassword", "newpass456"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void changeMyPassword_missingNewPassword_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.changeMyPassword(
                Map.of("oldPassword", "oldpass123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void changeMyPassword_wrongOldPassword_shouldReturnBadRequest() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        stubService.changePasswordHandler = (uid, oldP, newP) -> {
            throw new IllegalArgumentException("旧密码错误");
        };
        ResponseEntity<Object> response = controller.changeMyPassword(
                Map.of("oldPassword", "wrongold", "newPassword", "newpass456"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteUser_admin_shouldReturnOk() {
        SecurityContextHolder.set(authenticatedContext(1L, "admin"));
        ResponseEntity<Object> response = controller.deleteUser(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteUser_unauthenticated_shouldReturn401() {
        ResponseEntity<Object> response = controller.deleteUser(2L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private SecurityContext authenticatedContext(Long userId, String role) {
        return new SecurityContext() {
            @Override
            public String principalId() {
                return "user-" + userId;
            }

            @Override
            public Set<String> roles() {
                return Set.of(role);
            }

            @Override
            public Map<String, Object> attributes() {
                return Map.of("userId", userId);
            }
        };
    }

    /**
     * 手动 stub — 绕过 JDK 24 对 Mockito ByteBuddy 的限制。
     */
    static class StubUserService extends UserService {

        Function<RegisterRequest, AuthResponse> registerHandler = req -> null;
        Function<LoginRequest, AuthResponse> loginHandler = req -> null;
        Function<Long, UserInfo> getCurrentUserHandler = id -> null;
        List<UserInfo> listUsersResult = List.of();
        SetEnabledHandler setEnabledHandler = (id, enabled) -> {};
        boolean setEnabledCalled = false;

        StubUserService() {
            super(null, null, null);
        }

        @Override
        public AuthResponse register(RegisterRequest request) {
            return registerHandler.apply(request);
        }

        @Override
        public AuthResponse login(LoginRequest request) {
            return loginHandler.apply(request);
        }

        @Override
        public UserInfo getCurrentUser(Long userId) {
            return getCurrentUserHandler.apply(userId);
        }

        @Override
        public List<UserInfo> listUsers() {
            return listUsersResult;
        }

        @Override
        public void setEnabled(Long userId, boolean enabled) {
            setEnabledCalled = true;
            setEnabledHandler.accept(userId, enabled);
        }

        @Override
        public void approveUser(Long userId) {
            adminActionHandler.accept(userId);
        }

        @Override
        public void rejectUser(Long userId) {
            adminActionHandler.accept(userId);
        }

        @Override
        public void setRole(Long userId, String role) {
            setRoleHandler.accept(userId, role);
        }

        @Override
        public void resetPassword(Long userId, String newPassword) {
            resetPasswordHandler.accept(userId, newPassword);
        }

        @Override
        public void changePassword(Long userId, String oldPassword, String newPassword) {
            changePasswordCalled = true;
            changePasswordHandler.accept(userId, oldPassword, newPassword);
        }

        @Override
        public void deleteUser(Long userId) {
            adminActionHandler.accept(userId);
        }

        java.util.function.Consumer<Long> adminActionHandler = id -> {};
        java.util.function.BiConsumer<Long, String> setRoleHandler = (id, role) -> {};
        java.util.function.BiConsumer<Long, String> resetPasswordHandler = (id, pwd) -> {};

        /** 修改自己密码调用记录 */
        boolean changePasswordCalled = false;
        TriConsumer<Long, String, String> changePasswordHandler = (id, oldP, newP) -> {};

        @FunctionalInterface
        interface SetEnabledHandler {
            void accept(Long id, boolean enabled);
        }

        @FunctionalInterface
        interface TriConsumer<A, B, C> {
            void accept(A a, B b, C c);
        }
    }
}
