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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * JWT 认证器 — 多 session 模式下的用户身份认证。
 * 从 Authorization: Bearer <jwt> 头中解析 JWT，验证 HMAC-SHA256 签名，提取 userId。
 *
 * <p>激活条件：flighthub.transport.auth-mode=jwt
 * <p>使用 @Primary 覆盖默认的 BearerTokenAuthenticator。
 *
 * <p>支持的 JWT 结构（Header.Payload.Signature）：
 * <ul>
 *   <li>Header: {"alg":"HS256","typ":"JWT"}</li>
 *   <li>Payload: {"sub":"user_id", "exp":1720000000, ...}</li>
 *   <li>Signature: HMAC-SHA256(base64(header).base64(payload), secret)</li>
 * </ul>
 *
 * <p>验证通过后将 userId 存入请求属性 {@link #ATTR_USER_ID}，
 * 供 transport 层读取并设置到 ClientEntry。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
@Primary
@ConditionalOnProperty(name = "flighthub.transport.auth-mode", havingValue = "jwt")
public class JwtAuthenticator implements McpAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticator.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SCHEME_NAME = "jwt";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String EXPECTED_ALG = "HS256";

    /** exchange 属性 key：认证通过后存入 userId，供 transport 层读取 */
    public static final String ATTR_USER_ID = "fh.userId";

    private final FlightHubProperties properties;
    private final ObjectMapper objectMapper;

    public JwtAuthenticator(FlightHubProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 启动校验 — 拒绝不安全的 JWT 配置，防止 fail-open。
     *
     * <p>拦截场景：
     * <ul>
     *   <li>auth-mode=jwt 但 jwtSecret 为空 → 全开放，无日志信号</li>
     *   <li>仅设置 jwtPublicKey（RSA）→ 未实现，同样 fail-open</li>
     * </ul>
     */
    @PostConstruct
    void validateConfiguration() {
        String jwtSecret = properties.getTransport().getJwtSecret();
        String jwtPublicKey = properties.getTransport().getJwtPublicKey();
        boolean secretMissing = (jwtSecret == null || jwtSecret.isBlank());

        if (secretMissing && jwtPublicKey != null && !jwtPublicKey.isBlank()) {
            throw new IllegalStateException(
                    "JWT 认证配置错误：flighthub.transport.jwt-public-key 已设置但 RSA 验证尚未实现。"
                            + "请改用 FH_JWT_SECRET（HMAC-SHA256），或移除 jwt-public-key 配置。"
                            + "当前配置会导致认证被静默跳过（fail-open）。");
        }

        if (secretMissing) {
            throw new IllegalStateException(
                    "JWT 认证配置错误：flighthub.transport.auth-mode=jwt 但 FH_JWT_SECRET 未设置。"
                            + "请设置 FH_JWT_SECRET 环境变量，或将 auth-mode 改为 token。"
                            + "当前配置会导致认证被静默跳过（fail-open）。");
        }

        log.info("JWT 认证器已启动：HMAC-SHA256 模式，secret 已配置");
    }

    @Override
    public AuthResult authenticate(ServerHttpRequest request) {
        if (!isEnabled()) {
            return null;
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isBlank()) {
            return AuthResult.unauthorized("Missing Authorization header. Expected: Authorization: Bearer <jwt>");
        }

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            return AuthResult.unauthorized("Invalid Authorization scheme. Expected: Bearer <jwt>");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return AuthResult.unauthorized("Empty bearer token.");
        }

        // 解析 JWT 三段结构
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return AuthResult.unauthorized("Malformed JWT: expected 3 parts (header.payload.signature).");
        }

        try {
            // 验证 header 中的 alg 声明（防御 alg:none 和算法混淆攻击）
            String headerJson = new String(Base64.getUrlDecoder().decode(padBase64(parts[0])),
                    StandardCharsets.UTF_8);
            JsonNode header = objectMapper.readTree(headerJson);
            String alg = header.has("alg") ? header.get("alg").asText("") : "";
            if (!EXPECTED_ALG.equals(alg)) {
                log.warn("JWT alg 不匹配: expected={}, actual={}, remote={}",
                        EXPECTED_ALG, alg, request.getRemoteAddress());
                return AuthResult.unauthorized("Unsupported JWT algorithm: " + alg
                        + ". Only " + EXPECTED_ALG + " is accepted.");
            }

            // 验证签名
            String secret = properties.getTransport().getJwtSecret();
            if (secret == null || secret.isBlank()) {
                log.error("JWT 认证配置错误：FH_JWT_SECRET 未设置");
                return AuthResult.unauthorized("Server configuration error: JWT secret not configured.");
            }

            String signatureInput = parts[0] + "." + parts[1];
            byte[] expectedSig = hmacSha256(signatureInput.getBytes(StandardCharsets.UTF_8),
                    secret.getBytes(StandardCharsets.UTF_8));
            byte[] actualSig = Base64.getUrlDecoder().decode(padBase64(parts[2]));

            if (!MessageDigest.isEqual(expectedSig, actualSig)) {
                log.warn("JWT 签名验证失败: remote={}", request.getRemoteAddress());
                return AuthResult.unauthorized("Invalid JWT signature.");
            }

            // 解析 payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(padBase64(parts[1])),
                    StandardCharsets.UTF_8);
            JsonNode payload = objectMapper.readTree(payloadJson);

            // 检查过期时间
            if (payload.has("exp")) {
                long expSeconds = payload.get("exp").asLong();
                if (System.currentTimeMillis() / 1000 > expSeconds) {
                    return AuthResult.unauthorized("JWT token expired.");
                }
            }

            // 提取 userId（sub claim）
            String userId = payload.has("sub") ? payload.get("sub").asText(null) : null;
            if (userId == null || userId.isBlank()) {
                return AuthResult.unauthorized("JWT missing 'sub' claim (user identifier).");
            }

            // 认证通过 — 返回携带服务端验证过的 userId 的成功结果
            log.debug("JWT 认证通过: userId={}, remote={}", userId, request.getRemoteAddress());
            return AuthResult.success(userId);

        } catch (IllegalArgumentException e) {
            return AuthResult.unauthorized("Malformed JWT: invalid base64 encoding.");
        } catch (Exception e) {
            log.error("JWT 解析异常: {}", e.getMessage(), e);
            return AuthResult.unauthorized("JWT processing error: " + e.getMessage());
        }
    }

    @Override
    public String scheme() {
        return SCHEME_NAME;
    }

    @Override
    public boolean isEnabled() {
        String jwtSecret = properties.getTransport().getJwtSecret();
        return "jwt".equals(properties.getTransport().getAuthMode())
                && jwtSecret != null && !jwtSecret.isBlank();
    }

    /**
     * HMAC-SHA256 计算。
     */
    private byte[] hmacSha256(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        return mac.doFinal(data);
    }

    /**
     * 补齐 Base64 URL 编码的 padding（JWT 通常省略 '='）。
     */
    private String padBase64(String base64Url) {
        int remainder = base64Url.length() % 4;
        if (remainder == 2) {
            return base64Url + "==";
        } else if (remainder == 3) {
            return base64Url + "=";
        }
        return base64Url;
    }
}
