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

package com.dji.fh.feature.user.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dji.fh.feature.user.UserProperties;
import com.dji.fh.feature.user.entity.UserEntity;
import com.dji.fh.feature.user.repository.UserRepository;
import com.dji.fh.kernel.spi.auth.AuthenticationException;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * JwtAuthenticationProvider 单元测试。
 *
 * @author DJI DevKit Team
 */
class JwtAuthenticationProviderTest {

    private static final String SECRET = "test-secret-key-at-least-32-characters-long!!";

    private JwtTokenProvider tokenProvider;
    private JwtAuthenticationProvider authProvider;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        UserProperties props = new UserProperties(
                new UserProperties.Jwt(SECRET, Duration.ofHours(1)),
                new UserProperties.Registration(true, "user")
        );
        tokenProvider = new JwtTokenProvider(props);
        userRepository = mock(UserRepository.class);
        authProvider = new JwtAuthenticationProvider(tokenProvider, userRepository);
    }

    private UserEntity stubUser(Long id, int passwordVersion) {
        UserEntity user = new UserEntity("testuser", "hash", "nick", null, "admin");
        try {
            var idField = UserEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception ignored) {
            // 测试环境反射设 ID
        }
        for (int i = 0; i < passwordVersion; i++) {
            user.incrementPasswordVersion();
        }
        return user;
    }

    @Test
    void authenticate_shouldReturnContext_whenValidBearerToken() throws AuthenticationException {
        String token = tokenProvider.generateToken(1L, "alice", "admin", 0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(stubUser(1L, 0)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        Optional<SecurityContext> result = authProvider.authenticate(request);

        assertThat(result).isPresent();
        assertThat(result.get().principalId()).isEqualTo("1");
        assertThat(result.get().roles()).containsExactly("admin");
    }

    @Test
    void authenticate_shouldReturnEmpty_whenNoAuthHeader() throws AuthenticationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Optional<SecurityContext> result = authProvider.authenticate(request);
        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_shouldReturnEmpty_whenNonBearerScheme() throws AuthenticationException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        Optional<SecurityContext> result = authProvider.authenticate(request);
        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_shouldReturnContext_whenTokenInQueryParam() throws AuthenticationException {
        String token = tokenProvider.generateToken(2L, "bob", "user", 0);
        when(userRepository.findById(2L)).thenReturn(Optional.of(stubUser(2L, 0)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("token", token);

        Optional<SecurityContext> result = authProvider.authenticate(request);

        assertThat(result).isPresent();
        assertThat(result.get().principalId()).isEqualTo("2");
        assertThat(result.get().roles()).containsExactly("user");
    }

    @Test
    void authenticate_shouldPreferHeaderOverQueryParam() throws AuthenticationException {
        String headerToken = tokenProvider.generateToken(1L, "alice", "admin", 0);
        String queryToken = tokenProvider.generateToken(2L, "bob", "user", 0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(stubUser(1L, 0)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + headerToken);
        request.addParameter("token", queryToken);

        Optional<SecurityContext> result = authProvider.authenticate(request);

        assertThat(result).isPresent();
        assertThat(result.get().principalId()).isEqualTo("1");
    }

    @Test
    void authenticate_shouldThrow_whenInvalidToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");

        assertThatThrownBy(() -> authProvider.authenticate(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void authenticate_shouldThrow_whenPasswordVersionMismatch() {
        // token 签发时 passwordVersion=0，但 DB 中已递增到 1（用户改密）
        String token = tokenProvider.generateToken(1L, "alice", "admin", 0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(stubUser(1L, 1)));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        assertThatThrownBy(() -> authProvider.authenticate(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("密码已变更");
    }

    @Test
    void order_shouldBe100() {
        assertThat(authProvider.order()).isEqualTo(100);
    }
}
