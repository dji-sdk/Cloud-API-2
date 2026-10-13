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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * {@link GlobalExceptionHandler} 单元测试。
 *
 * @author DJI DevKit Team
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clear();
    }

    @Test
    void handleAccessDenied_shouldReturn401_whenAnonymous() {
        // 不设置 SecurityContext → 默认匿名（NoOpSecurityContext）
        AccessDeniedException ex = new AccessDeniedException("需要已认证用户，当前匿名");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("code")).isEqualTo("unauthenticated");
        assertThat(response.getBody().get("message")).isEqualTo("需要已认证用户，当前匿名");
    }

    @Test
    void handleAccessDenied_shouldReturn403_whenAuthenticatedButLacksRole() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of("guest")));
        AccessDeniedException ex = new AccessDeniedException("缺少所需角色: [admin]（requireAll=false）");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("code")).isEqualTo("forbidden");
        assertThat(response.getBody().get("message")).asString().contains("admin");
    }

    @Test
    void handleAccessDenied_shouldReturn403_whenAuthenticatedButLacksPermission() {
        SecurityContextHolder.set(new TestCtx("bob", Set.of("operator")));
        AccessDeniedException ex = new AccessDeniedException("缺少所需权限: device:write");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("code")).isEqualTo("forbidden");
        assertThat(response.getBody().get("message")).asString().contains("device:write");
    }

    private record TestCtx(String principalId, Set<String> roles) implements SecurityContext {
        @Override
        public Map<String, Object> attributes() {
            return Map.of();
        }
    }
}
