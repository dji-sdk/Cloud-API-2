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

package com.dji.fh.feature.dashboard.spi.sdk;

import com.dji.fh.feature.dashboard.spi.FlightTaskCommandService;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MediaCountService;
import com.dji.fh.feature.dashboard.spi.ModelCountService;
import com.dji.fh.feature.dashboard.spi.PersonnelCountService;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.feature.user.repository.UserRepository;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SDK 模式下的看板 SPI 实现自动装配。
 *
 * <p>仅当 {@code fh.skeleton.sdk.mode=sdk} 且 SDK 类存在于 classpath 时才装配。
 * 此配置中的 bean 会覆盖 {@code DashboardAutoConfiguration} 中的
 * {@code @ConditionalOnMissingBean} mock 默认实现。
 *
 * @author DJI DevKit Team
 */
@Configuration
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "sdk")
@ConditionalOnClass(name = "com.dji.flighthubsdk.FlightHubSDK")
public class SdkDashboardAutoConfiguration {

    @Bean
    public FlightTaskQueryService flightTaskQueryService(DeviceQueryService deviceQueryService,
                                                         WaylineQueryService waylineQueryService) {
        return new SdkFlightTaskQueryService(deviceQueryService, waylineQueryService);
    }

    @Bean
    public FlightTaskConflictService flightTaskConflictService(FlightTaskQueryService queryService) {
        return new SdkFlightTaskConflictService(queryService);
    }

    @Bean
    public WaylineQueryService waylineQueryService() {
        return new SdkWaylineQueryService();
    }

    @Bean
    public MediaCountService mediaCountService() {
        return new SdkMediaCountService();
    }

    @Bean
    public ModelCountService modelCountService() {
        return new SdkModelCountService();
    }

    @Bean
    public FlightTaskCommandService flightTaskCommandService() {
        return new SdkFlightTaskCommandService();
    }

    @Bean
    public PersonnelCountService personnelCountService(UserRepository userRepository) {
        return new SdkPersonnelCountService(userRepository);
    }
}
