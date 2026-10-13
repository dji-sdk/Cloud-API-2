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

import com.dji.fh.kernel.spi.auth.AuthenticationProvider;
import com.dji.fh.kernel.spi.auth.NoOpSecurityContext;
import com.dji.fh.kernel.spi.auth.PermissionEvaluator;
import com.dji.fh.kernel.spi.auth.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuthAutoConfiguration 装配测试。
 *
 * @author DJI DevKit Team
 */
class AuthAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuthAutoConfiguration.class));

    @Test
    void noOpProvider_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(AuthenticationProvider.class);
            assertThat(ctx.getBean(AuthenticationProvider.class))
                    .isInstanceOf(NoOpAuthenticationProvider.class);
        });
    }

    @Test
    void defaultPermissionEvaluator_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(PermissionEvaluator.class);
            assertThat(ctx.getBean(PermissionEvaluator.class))
                    .isInstanceOf(DefaultPermissionEvaluator.class);
        });
    }

    @Test
    void authorizationAspect_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(AuthorizationAspect.class));
    }

    @Test
    void provider_shouldLetUserOverride_whenUserBeanPresent() {
        runner.withBean(AuthenticationProvider.class, () -> new AuthenticationProvider() {
            @Override
            public Optional<SecurityContext> authenticate(HttpServletRequest request) {
                return Optional.of(NoOpSecurityContext.INSTANCE);
            }
        }).run(ctx -> {
            assertThat(ctx.getBean(AuthenticationProvider.class))
                    .isNotInstanceOf(NoOpAuthenticationProvider.class);
        });
    }
}
