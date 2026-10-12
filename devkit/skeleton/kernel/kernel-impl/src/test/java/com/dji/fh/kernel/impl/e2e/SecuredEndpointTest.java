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

package com.dji.fh.kernel.impl.e2e;

import com.dji.fh.kernel.impl.auth.AccessDeniedException;
import com.dji.fh.kernel.spi.auth.RequireRole;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * 验证 @RequireRole 切面在完整 Spring context 中生效。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(classes = {SecuredEndpointTest.MinimalApp.class, SecuredEndpointTest.AdminService.class})
class SecuredEndpointTest {

    @Autowired AdminService service;

    @AfterEach
    void clearCtx() {
        SecurityContextHolder.clear();
    }

    @Test
    void adminEndpoint_shouldDeny_whenAnonymous() {
        // 不 set context → NoOp（anonymous）
        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> service.adminAction());
    }

    @Test
    void adminEndpoint_shouldProceed_whenUserHasAdminRole() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of("admin")));

        assertThat(service.adminAction()).isEqualTo("admin-ok");
    }

    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class MinimalApp {
        @org.springframework.context.annotation.Bean
        com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }

    @Component
    public static class AdminService {
        @RequireRole("admin")
        public String adminAction() {
            return "admin-ok";
        }
    }

    private record TestCtx(String principalId, Set<String> roles) implements SecurityContext {
        @Override
        public Map<String, Object> attributes() {
            return Map.of();
        }
    }
}
