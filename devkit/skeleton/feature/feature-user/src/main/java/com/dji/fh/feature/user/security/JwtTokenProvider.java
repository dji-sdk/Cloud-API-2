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

import com.dji.fh.feature.user.UserProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JWT token 签发与验证。
 *
 * <p>使用 HS256 签名，payload 包含 userId（sub）、username、role。
 *
 * @author DJI DevKit Team
 */
public class JwtTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_PASSWORD_VERSION = "pwd_ver";

    private final SecretKey key;
    private final long expiryMillis;

    /**
     * 构造函数。
     *
     * @param props 用户模块配置
     */
    public JwtTokenProvider(UserProperties props) {
        this.key = Keys.hmacShaKeyFor(
                props.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.expiryMillis = props.jwt().expiry().toMillis();
    }

    /**
     * 签发 JWT token。
     *
     * @param userId          用户 ID
     * @param username        用户名
     * @param role            角色
     * @param passwordVersion 密码版本号（改密时递增，使旧 token 失效）
     * @return JWT token 字符串
     */
    public String generateToken(Long userId, String username, String role, int passwordVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiryMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_PASSWORD_VERSION, passwordVersion)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 签发 JWT token（向后兼容，passwordVersion 默认 0）。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色
     * @return JWT token 字符串
     */
    public String generateToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, 0);
    }

    /**
     * 验证 token 是否有效。
     *
     * @param token JWT token
     * @return 有效返回 true
     */
    public boolean validate(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            LOG.debug("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 token 中提取用户 ID。
     *
     * @param token JWT token
     * @return 用户 ID
     * @throws JwtException token 无效时
     */
    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * 从 token 中提取用户名。
     *
     * @param token JWT token
     * @return 用户名
     * @throws JwtException token 无效时
     */
    public String getUsername(String token) {
        return parseClaims(token).get(CLAIM_USERNAME, String.class);
    }

    /**
     * 从 token 中提取角色。
     *
     * @param token JWT token
     * @return 角色
     * @throws JwtException token 无效时
     */
    public String getRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    /**
     * 从 token 中提取密码版本号。
     *
     * @param token JWT token
     * @return 密码版本号（旧 token 无此 claim 时返回 0）
     */
    public int getPasswordVersion(String token) {
        Integer ver = parseClaims(token).get(CLAIM_PASSWORD_VERSION, Integer.class);
        return ver != null ? ver : 0;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
