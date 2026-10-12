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

import com.dji.fh.kernel.spi.query.DeviceQueryService;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 设备状态管理 feature 的 AutoConfiguration。
 *
 * <p>默认启用（{@code matchIfMissing = true}）；ISV 设
 * {@code fh.skeleton.feature.device.enabled=false} 可完全禁用。
 *
 * <p>Service 和 Controller 都通过 {@code @ConditionalOnMissingBean} 允许 ISV 覆盖。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@ConditionalOnProperty(name = "fh.skeleton.feature.device.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DeviceProperties.class)
public class DeviceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DeviceStatusService deviceStatusService(DeviceProperties props,
                                                       DeviceQueryService queryService,
                                                       Clock clock) {
        return new DeviceStatusService(props, queryService, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeviceRestController deviceRestController(DeviceStatusService service) {
        return new DeviceRestController(service);
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock featureDeviceClock() {
        return Clock.systemUTC();
    }
}
