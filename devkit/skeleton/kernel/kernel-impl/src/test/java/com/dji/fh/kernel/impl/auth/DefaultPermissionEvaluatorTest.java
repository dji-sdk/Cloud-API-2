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

package com.dji.fh.kernel.impl.auth;

import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPermissionEvaluatorTest {

    private final DefaultPermissionEvaluator evaluator = new DefaultPermissionEvaluator();

    @Test
    void hasPermission_shouldReturnTrue_whenAttributeContainsPermission() {
        SecurityContext ctx = new TestContext("alice", Set.of(),
                Map.of("permissions", List.of("device:read", "device:write")));

        assertThat(evaluator.hasPermission(ctx, "device:read")).isTrue();
    }

    @Test
    void hasPermission_shouldReturnFalse_whenAttributeMissingPermission() {
        SecurityContext ctx = new TestContext("alice", Set.of(),
                Map.of("permissions", List.of("device:read")));

        assertThat(evaluator.hasPermission(ctx, "device:write")).isFalse();
    }

    @Test
    void hasPermission_shouldReturnFalse_whenNoPermissionsAttribute() {
        SecurityContext ctx = new TestContext("alice", Set.of(), Map.of());

        assertThat(evaluator.hasPermission(ctx, "device:read")).isFalse();
    }

    @Test
    void hasPermission_shouldReturnFalse_whenNoOpContext() {
        assertThat(evaluator.hasPermission(
                NoOpSecurityContext.INSTANCE, "anything")).isFalse();
    }

    @Test
    void hasPermission_shouldTolerateNonListAttribute_whenAttributeIsString() {
        // 防御：attributes["permissions"] 类型不是 List 时不抛，返回 false
        SecurityContext ctx = new TestContext("alice", Set.of(),
                Map.of("permissions", "not-a-list"));

        assertThat(evaluator.hasPermission(ctx, "device:read")).isFalse();
    }

    private record TestContext(String principalId, Set<String> roles,
                               Map<String, Object> attributes) implements SecurityContext {
    }
}
