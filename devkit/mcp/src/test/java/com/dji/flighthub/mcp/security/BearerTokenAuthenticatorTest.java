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

import com.dji.flighthub.mcp.config.FlightHubProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BearerTokenAuthenticator 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class BearerTokenAuthenticatorTest {

    private static final String CONFIGURED_TOKEN = "test-secret-token-12345";

    // ==================== isEnabled() ====================

    @Nested
    @DisplayName("isEnabled() — 认证启用判断")
    class EnabledTest {

        @Test
        @DisplayName("token 已配置时返回 true")
        void isEnabled_shouldReturnTrue_whenTokenConfigured() {
            BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);
            assertTrue(auth.isEnabled());
        }

        @Test
        @DisplayName("token 为空时返回 false（开发模式）")
        void isEnabled_shouldReturnFalse_whenTokenEmpty() {
            BearerTokenAuthenticator auth = createAuthenticator("");
            assertFalse(auth.isEnabled());
        }

        @Test
        @DisplayName("token 为 null 时返回 false")
        void isEnabled_shouldReturnFalse_whenTokenNull() {
            BearerTokenAuthenticator auth = createAuthenticator(null);
            assertFalse(auth.isEnabled());
        }

        @Test
        @DisplayName("token 为空白字符时返回 false")
        void isEnabled_shouldReturnFalse_whenTokenBlank() {
            BearerTokenAuthenticator auth = createAuthenticator("   ");
            assertFalse(auth.isEnabled());
        }
    }

    // ==================== authenticate() ====================

    @Nested
    @DisplayName("authenticate() — 认证逻辑")
    class AuthenticateTest {

        @Test
        @DisplayName("有效 token 应认证通过（返回 null）")
        void authenticate_shouldReturnNull_whenTokenValid() {
            BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + CONFIGURED_TOKEN)
                    .build();

            assertNull(auth.authenticate(request));
        }

        @Test
        @DisplayName("缺少 Authorization header 应返回 401")
        void authenticate_shouldReturn401_whenNoHeader() {
            BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse").build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("Missing Authorization"));
        }

        @Test
        @DisplayName("非 Bearer 前缀应返回 401")
        void authenticate_shouldReturn401_whenNotBearerScheme() {
            BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header(HttpHeaders.AUTHORIZATION, "Basic abc123")
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("Invalid Authorization scheme"));
        }

        @Test
        @DisplayName("空 token 值应返回 401")
        void authenticate_shouldReturn401_whenEmptyToken() {
            BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
        }

        @Test
        @DisplayName("错误 token 应返回 401")
        void authenticate_shouldReturn401_whenTokenMismatch() {
            BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer wrong-token")
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("Invalid bearer token"));
        }

        @Test
        @DisplayName("认证器未启用时应直接通过（返回 null）")
        void authenticate_shouldReturnNull_whenDisabled() {
            BearerTokenAuthenticator auth = createAuthenticator("");

            // 即使没有 header 也应该通过（因为认证器未启用）
            MockServerHttpRequest request = MockServerHttpRequest.get("/sse").build();

            // isEnabled() = false，但 authenticate() 仍会检查
            // 注意：调用方应先检查 isEnabled()，这里测试 authenticate 本身行为
            // 当 token 未配置时 authenticate 应返回 null（跳过）
            assertNull(auth.authenticate(request));
        }
    }

    // ==================== scheme() ====================

    @Test
    @DisplayName("scheme() 应返回 bearer-token")
    void scheme_shouldReturnBearerToken() {
        BearerTokenAuthenticator auth = createAuthenticator(CONFIGURED_TOKEN);
        assertEquals("bearer-token", auth.scheme());
    }

    // ==================== 工厂方法 ====================

    private BearerTokenAuthenticator createAuthenticator(String token) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getTransport().setSseAuthToken(token);
        return new BearerTokenAuthenticator(properties);
    }
}
