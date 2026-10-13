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

package com.dji.fh.kernel.spi.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class SecurityContextHolderTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clear();
    }

    @Test
    void current_shouldReturnNoOp_whenNothingSet() {
        SecurityContext ctx = SecurityContextHolder.current();

        assertThat(ctx).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void current_shouldReturnSet_whenContextSet() {
        SecurityContext custom = new TestContext("alice", Set.of("admin"));

        SecurityContextHolder.set(custom);
        SecurityContext ctx = SecurityContextHolder.current();

        assertThat(ctx).isSameAs(custom);
        assertThat(ctx.principalId()).isEqualTo("alice");
    }

    @Test
    void current_shouldRevertToNoOp_whenCleared() {
        SecurityContextHolder.set(new TestContext("bob", Set.of()));
        SecurityContextHolder.clear();

        assertThat(SecurityContextHolder.current()).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    @Test
    void set_shouldBehaveLikeClear_whenNullPassed() {
        SecurityContextHolder.set(new TestContext("carol", Set.of("ops")));
        SecurityContextHolder.set(null);

        assertThat(SecurityContextHolder.current()).isSameAs(NoOpSecurityContext.INSTANCE);
    }

    private record TestContext(String principalId, Set<String> roles) implements SecurityContext {
        @Override
        public Map<String, Object> attributes() {
            return Map.of();
        }
    }
}
