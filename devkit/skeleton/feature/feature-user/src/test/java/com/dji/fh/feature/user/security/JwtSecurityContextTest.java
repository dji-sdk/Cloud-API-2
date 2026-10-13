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
import org.junit.jupiter.api.Test;

/**
 * JwtSecurityContext 单元测试。
 *
 * @author DJI DevKit Team
 */
class JwtSecurityContextTest {

    @Test
    void shouldBeAuthenticated() {
        JwtSecurityContext ctx = new JwtSecurityContext(1L, "alice", "admin");
        assertThat(ctx.isAuthenticated()).isTrue();
    }

    @Test
    void principalId_shouldReturnStringUserId() {
        JwtSecurityContext ctx = new JwtSecurityContext(42L, "bob", "user");
        assertThat(ctx.principalId()).isEqualTo("42");
    }

    @Test
    void roles_shouldContainExactRole() {
        JwtSecurityContext ctx = new JwtSecurityContext(1L, "alice", "admin");
        assertThat(ctx.roles()).containsExactly("admin");
    }

    @Test
    void hasRole_shouldWorkCorrectly() {
        JwtSecurityContext ctx = new JwtSecurityContext(1L, "alice", "admin");
        assertThat(ctx.hasRole("admin")).isTrue();
        assertThat(ctx.hasRole("user")).isFalse();
    }

    @Test
    void attributes_shouldContainUsernameAndRole() {
        JwtSecurityContext ctx = new JwtSecurityContext(1L, "alice", "admin");
        assertThat(ctx.attributes()).containsEntry("username", "alice");
        assertThat(ctx.attributes()).containsEntry("role", "admin");
        assertThat(ctx.attributes()).containsEntry("userId", 1L);
    }
}
