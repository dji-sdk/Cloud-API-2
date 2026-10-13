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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtAuthenticator 单元测试。
 * 覆盖：配置校验、签名验证、过期检查、alg 校验、userId 提取。
 *
 * @author DJI FlightHub SDK Team
 */
class JwtAuthenticatorTest {

    private static final String TEST_SECRET = "test-jwt-secret-key-32bytes-long";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FlightHubProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        properties.getTransport().setAuthMode("jwt");
        properties.getTransport().setJwtSecret(TEST_SECRET);
    }

    private JwtAuthenticator createAuthenticator() {
        return new JwtAuthenticator(properties, OBJECT_MAPPER);
    }

    // ==================== validateConfiguration ====================

    @Nested
    @DisplayName("validateConfiguration - 启动校验")
    class ValidateConfigurationTests {

        @Test
        @DisplayName("jwtSecret 已配置时正常启动")
        void validateConfiguration_shouldPass_whenSecretConfigured() {
            JwtAuthenticator auth = createAuthenticator();
            assertDoesNotThrow(auth::validateConfiguration);
        }

        @Test
        @DisplayName("jwtSecret 为空时抛 IllegalStateException")
        void validateConfiguration_shouldThrow_whenSecretMissing() {
            properties.getTransport().setJwtSecret(null);
            JwtAuthenticator auth = createAuthenticator();
            assertThrows(IllegalStateException.class, auth::validateConfiguration);
        }

        @Test
        @DisplayName("仅设置 jwtPublicKey 时抛 IllegalStateException（RSA 未实现）")
        void validateConfiguration_shouldThrow_whenOnlyPublicKeySet() {
            properties.getTransport().setJwtSecret(null);
            properties.getTransport().setJwtPublicKey("/path/to/key.pem");
            JwtAuthenticator auth = createAuthenticator();
            IllegalStateException ex = assertThrows(IllegalStateException.class, auth::validateConfiguration);
            assertTrue(ex.getMessage().contains("RSA"));
        }
    }

    // ==================== authenticate - 成功路径 ====================

    @Nested
    @DisplayName("authenticate - 成功路径")
    class AuthenticateSuccessTests {

        @Test
        @DisplayName("有效 JWT 应返回成功并携带 userId")
        void authenticate_shouldReturnSuccess_whenValidJwt() {
            JwtAuthenticator auth = createAuthenticator();
            String token = buildJwt("user-001", futureExp());

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer " + token)
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertEquals("user-001", result.userId());
        }

        @Test
        @DisplayName("未启用时应返回 null")
        void authenticate_shouldReturnNull_whenDisabled() {
            properties.getTransport().setAuthMode("token");
            JwtAuthenticator auth = createAuthenticator();

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer some-token")
                    .build();

            assertNull(auth.authenticate(request));
        }
    }

    // ==================== authenticate - 失败路径 ====================

    @Nested
    @DisplayName("authenticate - 失败路径")
    class AuthenticateFailureTests {

        @Test
        @DisplayName("缺少 Authorization header 应返回 401")
        void authenticate_shouldReturn401_whenNoAuthHeader() {
            JwtAuthenticator auth = createAuthenticator();
            MockServerHttpRequest request = MockServerHttpRequest.get("/sse").build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertEquals(401, result.httpStatus());
        }

        @Test
        @DisplayName("无效 Bearer 前缀应返回 401")
        void authenticate_shouldReturn401_whenInvalidScheme() {
            JwtAuthenticator auth = createAuthenticator();
            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Basic abc123")
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
        }

        @Test
        @DisplayName("格式错误的 JWT（非三段）应返回 401")
        void authenticate_shouldReturn401_whenMalformedJwt() {
            JwtAuthenticator auth = createAuthenticator();
            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer not.a.valid.jwt.token")
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
        }

        @Test
        @DisplayName("签名不匹配应返回 401")
        void authenticate_shouldReturn401_whenSignatureInvalid() {
            JwtAuthenticator auth = createAuthenticator();
            String token = buildJwt("user-001", futureExp());
            // 篡改 payload
            String[] parts = token.split("\\.");
            String tamperedToken = parts[0] + "." + base64Url("{\"sub\":\"hacker\",\"exp\":" + futureExp() + "}") + "." + parts[2];

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer " + tamperedToken)
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("signature"));
        }

        @Test
        @DisplayName("过期 JWT 应返回 401")
        void authenticate_shouldReturn401_whenTokenExpired() {
            JwtAuthenticator auth = createAuthenticator();
            long pastExp = System.currentTimeMillis() / 1000 - 3600;
            String token = buildJwt("user-001", pastExp);

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer " + token)
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("expired"));
        }

        @Test
        @DisplayName("缺少 sub claim 应返回 401")
        void authenticate_shouldReturn401_whenSubMissing() {
            JwtAuthenticator auth = createAuthenticator();
            String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
            String payload = base64Url("{\"exp\":" + futureExp() + "}");
            String signature = hmacSign(header + "." + payload);
            String token = header + "." + payload + "." + signature;

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer " + token)
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("sub"));
        }
    }

    // ==================== alg 校验 ====================

    @Nested
    @DisplayName("authenticate - alg header 校验")
    class AlgValidationTests {

        @Test
        @DisplayName("alg:none 应被拒绝")
        void authenticate_shouldReject_whenAlgNone() {
            JwtAuthenticator auth = createAuthenticator();
            String header = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
            String payload = base64Url("{\"sub\":\"user-001\",\"exp\":" + futureExp() + "}");
            String token = header + "." + payload + ".dummysig";

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer " + token)
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("algorithm"));
        }

        @Test
        @DisplayName("alg:RS256 应被拒绝")
        void authenticate_shouldReject_whenAlgRS256() {
            JwtAuthenticator auth = createAuthenticator();
            String header = base64Url("{\"alg\":\"RS256\",\"typ\":\"JWT\"}");
            String payload = base64Url("{\"sub\":\"user-001\",\"exp\":" + futureExp() + "}");
            String signature = hmacSign(header + "." + payload);
            String token = header + "." + payload + "." + signature;

            MockServerHttpRequest request = MockServerHttpRequest.get("/sse")
                    .header("Authorization", "Bearer " + token)
                    .build();

            McpAuthenticator.AuthResult result = auth.authenticate(request);
            assertNotNull(result);
            assertEquals(401, result.httpStatus());
            assertTrue(result.detail().contains("algorithm"));
        }
    }

    // ==================== 辅助方法 ====================

    private static long futureExp() {
        return System.currentTimeMillis() / 1000 + 3600;
    }

    private static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TEST_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildJwt(String userId, long expSeconds) {
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"sub\":\"" + userId + "\",\"exp\":" + expSeconds + "}");
        String signature = hmacSign(header + "." + payload);
        return header + "." + payload + "." + signature;
    }
}
