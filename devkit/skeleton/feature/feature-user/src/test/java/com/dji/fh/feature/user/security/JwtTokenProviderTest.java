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

import com.dji.fh.feature.user.UserProperties;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JwtTokenProvider 单元测试。
 *
 * @author DJI DevKit Team
 */
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        UserProperties props = new UserProperties(
                new UserProperties.Jwt("test-secret-key-at-least-32-characters-long!!", Duration.ofHours(1)),
                new UserProperties.Registration(true, "user")
        );
        provider = new JwtTokenProvider(props);
    }

    @Test
    void generateAndParse_shouldRoundTrip() {
        String token = provider.generateToken(1L, "alice", "admin");

        assertThat(provider.getUserId(token)).isEqualTo(1L);
        assertThat(provider.getUsername(token)).isEqualTo("alice");
        assertThat(provider.getRole(token)).isEqualTo("admin");
    }

    @Test
    void validate_shouldReturnTrue_forValidToken() {
        String token = provider.generateToken(1L, "alice", "user");
        assertThat(provider.validate(token)).isTrue();
    }

    @Test
    void validate_shouldReturnFalse_forTamperedToken() {
        String token = provider.generateToken(1L, "alice", "user");
        assertThat(provider.validate(token + "tampered")).isFalse();
    }

    @Test
    void validate_shouldReturnFalse_forNull() {
        assertThat(provider.validate(null)).isFalse();
    }

    @Test
    void validate_shouldReturnFalse_forBlank() {
        assertThat(provider.validate("  ")).isFalse();
    }

    @Test
    void parse_shouldThrow_forInvalidToken() {
        assertThatThrownBy(() -> provider.getUserId("garbage"))
                .isInstanceOf(JwtException.class);
    }
}
