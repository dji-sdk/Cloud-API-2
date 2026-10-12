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

import com.dji.fh.kernel.spi.auth.PublicEndpoint;
import com.dji.fh.kernel.spi.auth.RequireAuthenticated;
import com.dji.fh.kernel.spi.auth.RequirePermission;
import com.dji.fh.kernel.spi.auth.RequireRole;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import com.dji.fh.kernel.spi.auth.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * AuthorizationAspect 6 分支测试（3 注解 × 通过/拒绝）。
 *
 * @author DJI DevKit Team
 */
class AuthorizationAspectTest {

    private final SecuredApi target = new SecuredApi();
    private final SecuredApi proxy = wrap(target);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clear();
    }

    private SecuredApi wrap(SecuredApi target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new AuthorizationAspect(new DefaultPermissionEvaluator()));
        return factory.getProxy();
    }

    // ---- @RequireAuthenticated ----

    @Test
    void authenticated_shouldProceed_whenUserPresent() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of(), Map.of()));

        assertThat(proxy.authenticated()).isEqualTo("ok-auth");
    }

    @Test
    void authenticated_shouldDeny_whenAnonymous() {
        // 不 set context → 默认 NoOp（anonymous）

        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> proxy.authenticated())
                .withMessageContaining("已认证");
    }

    // ---- @RequireRole ----

    @Test
    void role_shouldProceed_whenUserHasRequiredRole() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of("admin"), Map.of()));

        assertThat(proxy.adminOnly()).isEqualTo("ok-admin");
    }

    @Test
    void role_shouldDeny_whenUserLacksRole() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of("guest"), Map.of()));

        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> proxy.adminOnly())
                .withMessageContaining("admin");
    }

    // ---- @RequirePermission ----

    @Test
    void permission_shouldProceed_whenUserHasPermission() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of(),
                Map.of("permissions", List.of("device:read"))));

        assertThat(proxy.readDevice()).isEqualTo("ok-read");
    }

    @Test
    void permission_shouldDeny_whenUserLacksPermission() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of(),
                Map.of("permissions", List.of("device:read"))));

        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> proxy.writeDevice())
                .withMessageContaining("device:write");
    }

    // ---- @PublicEndpoint 覆盖类级 @RequireAuthenticated ----

    @Test
    void publicEndpoint_shouldProceed_whenAnonymous() {
        // 不 set context → NoOp（anonymous）；@PublicEndpoint 应覆盖方法级 @RequireAuthenticated
        assertThat(proxy.publicLogin()).isEqualTo("ok-public");
    }

    @Test
    void publicEndpoint_shouldStillProceed_whenAuthenticated() {
        SecurityContextHolder.set(new TestCtx("alice", Set.of(), Map.of()));

        assertThat(proxy.publicLogin()).isEqualTo("ok-public");
    }

    // ---- 类级 @RequireAuthenticated + 方法级 @PublicEndpoint（AuthController 实际场景）----

    @Test
    void classLevelRequireAuthenticated_shouldDenyAnonymous_whenNoPublicEndpoint() {
        ClassLevelSecuredApi proxy = wrapClassLevel(new ClassLevelSecuredApi());

        // 类级 @RequireAuthenticated，方法无 @PublicEndpoint → 匿名拒绝
        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> proxy.protectedAction())
                .withMessageContaining("已认证");
    }

    @Test
    void classLevelRequireAuthenticated_shouldAllowAnonymous_whenMethodHasPublicEndpoint() {
        ClassLevelSecuredApi proxy = wrapClassLevel(new ClassLevelSecuredApi());

        // 类级 @RequireAuthenticated + 方法级 @PublicEndpoint → 匿名放行（登录入口语义）
        assertThat(proxy.login()).isEqualTo("ok-login");
    }

    @Test
    void classLevelRequireAuthenticated_shouldAllowAuthenticated_whenMethodHasPublicEndpoint() {
        ClassLevelSecuredApi proxy = wrapClassLevel(new ClassLevelSecuredApi());
        SecurityContextHolder.set(new TestCtx("alice", Set.of(), Map.of()));

        assertThat(proxy.login()).isEqualTo("ok-login");
    }

    private ClassLevelSecuredApi wrapClassLevel(ClassLevelSecuredApi target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new AuthorizationAspect(new DefaultPermissionEvaluator()));
        return factory.getProxy();
    }

    /**
     * 受切面拦截的被测 API（方法级注解）。
     */
    public static class SecuredApi {

        @RequireAuthenticated
        public String authenticated() {
            return "ok-auth";
        }

        @RequireRole("admin")
        public String adminOnly() {
            return "ok-admin";
        }

        @RequirePermission("device:read")
        public String readDevice() {
            return "ok-read";
        }

        @RequirePermission("device:write")
        public String writeDevice() {
            return "ok-write";
        }

        /**
         * 模拟登录入口：方法级 @PublicEndpoint 放行匿名访问。
         */
        @PublicEndpoint
        public String publicLogin() {
            return "ok-public";
        }
    }

    /**
     * 类级 @RequireAuthenticated 的受测 API（模拟 AuthController 实际结构）。
     */
    @RequireAuthenticated
    public static class ClassLevelSecuredApi {

        /** 受保护端点：无 @PublicEndpoint，匿名应被拒绝。 */
        public String protectedAction() {
            return "ok-protected";
        }

        /** 登录入口：@PublicEndpoint 覆盖类级 @RequireAuthenticated，匿名放行。 */
        @PublicEndpoint
        public String login() {
            return "ok-login";
        }
    }

    private record TestCtx(String principalId, Set<String> roles,
                           Map<String, Object> attributes) implements SecurityContext {
    }
}
