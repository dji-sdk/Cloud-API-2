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

package com.dji.fh.feature.device;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * DeviceAutoConfiguration 装配切片测试。
 *
 * @author DJI DevKit Team
 */
class DeviceAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DeviceAutoConfiguration.class));

    @Test
    void shouldRegisterAllBeans_whenDefaultProperties() {
        runner.withBean(com.dji.fh.kernel.spi.query.DeviceQueryService.class,
                        () -> java.util.Collections::emptyList)
                .run(ctx -> {
            assertThat(ctx).hasSingleBean(DeviceStatusService.class);
            assertThat(ctx).hasSingleBean(DeviceRestController.class);
            assertThat(ctx).hasSingleBean(DeviceProperties.class);
        });
    }

    @Test
    void shouldNotRegisterBeans_whenEnabledFalse() {
        runner.withPropertyValues("fh.skeleton.feature.device.enabled=false")
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(DeviceStatusService.class);
                    assertThat(ctx).doesNotHaveBean(DeviceRestController.class);
                });
    }

    @Test
    void shouldAllowIsvOverrideService() {
        DeviceProperties props = new DeviceProperties(10, true, Duration.ofMinutes(5), null);
        com.dji.fh.kernel.spi.query.DeviceQueryService qs = java.util.Collections::emptyList;
        DeviceStatusService custom = new DeviceStatusService(props, qs, Clock.systemUTC());
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DeviceAutoConfiguration.class))
                .withBean(DeviceStatusService.class, () -> custom)
                .run(ctx -> assertThat(ctx.getBean(DeviceStatusService.class)).isSameAs(custom));
    }

    @Test
    void shouldBindHmsMaxFromProperties() {
        runner.withBean(com.dji.fh.kernel.spi.query.DeviceQueryService.class,
                        () -> java.util.Collections::emptyList)
                .withPropertyValues("fh.skeleton.feature.device.hms-max-per-device=100")
                .run(ctx -> {
                    DeviceProperties props = ctx.getBean(DeviceProperties.class);
                    assertThat(props.hmsMaxPerDevice()).isEqualTo(100);
                });
    }

    @Test
    void shouldBindLivenessTimeoutFromProperties() {
        runner.withBean(com.dji.fh.kernel.spi.query.DeviceQueryService.class,
                        () -> java.util.Collections::emptyList)
                .withPropertyValues("fh.skeleton.feature.device.liveness-timeout=30s")
                .run(ctx -> {
                    DeviceProperties props = ctx.getBean(DeviceProperties.class);
                    assertThat(props.livenessTimeout()).isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    void shouldUseDefaultLivenessTimeout_whenPropertyAbsent() {
        runner.withBean(com.dji.fh.kernel.spi.query.DeviceQueryService.class,
                        () -> java.util.Collections::emptyList)
                .run(ctx -> {
                    DeviceProperties props = ctx.getBean(DeviceProperties.class);
                    assertThat(props.livenessTimeout()).isEqualTo(Duration.ofMinutes(5));
                });
    }
}
