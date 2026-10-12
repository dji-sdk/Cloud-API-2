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

import com.dji.fh.kernel.spi.auth.SecurityContext;
import java.util.Map;
import java.util.Set;

/**
 * JWT 认证后的 SecurityContext 实现。
 *
 * <p>principalId 为用户 ID 的字符串形式，roles 包含单个角色（admin 或 user）。
 *
 * @author DJI DevKit Team
 */
public class JwtSecurityContext implements SecurityContext {

    private final Long userId;
    private final String username;
    private final String role;

    /**
     * 构造函数。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色
     */
    public JwtSecurityContext(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    @Override
    public String principalId() {
        return String.valueOf(userId);
    }

    @Override
    public Set<String> roles() {
        return Set.of(role);
    }

    @Override
    public Map<String, Object> attributes() {
        return Map.of(
                "userId", userId,
                "username", username,
                "role", role
        );
    }
}
