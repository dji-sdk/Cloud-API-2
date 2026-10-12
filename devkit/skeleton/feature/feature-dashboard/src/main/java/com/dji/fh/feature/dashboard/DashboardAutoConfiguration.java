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

package com.dji.fh.feature.dashboard;

import com.dji.fh.feature.dashboard.spi.FlightTaskCommandService;
import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MediaCountService;
import com.dji.fh.feature.dashboard.spi.MockFlightTaskCommandService;
import com.dji.fh.feature.dashboard.spi.MockFlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.MockFlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MockMediaCountService;
import com.dji.fh.feature.dashboard.spi.MockModelCountService;
import com.dji.fh.feature.dashboard.spi.MockPersonnelCountService;
import com.dji.fh.feature.dashboard.spi.MockWaylineQueryService;
import com.dji.fh.feature.dashboard.spi.ModelCountService;
import com.dji.fh.feature.dashboard.spi.PersonnelCountService;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.feature.device.DeviceStatusService;
import com.dji.fh.feature.dashboard.spi.sdk.SdkDashboardAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 看板模块自动配置。
 *
 * <p>所有 SPI 均使用 {@code @ConditionalOnMissingBean}，SDK 模式下由
 * {@code SdkDashboardAutoConfiguration} 提供真实 SDK 实现，覆盖这些默认 mock。
 * ISV 也可声明自有 bean 覆盖。
 *
 * @author DJI DevKit Team
 */
@Configuration
@AutoConfigureAfter(SdkDashboardAutoConfiguration.class)
public class DashboardAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlightTaskQueryService flightTaskQueryService() {
        return new MockFlightTaskQueryService();
    }

    @Bean
    @ConditionalOnMissingBean
    public WaylineQueryService waylineQueryService() {
        return new MockWaylineQueryService();
    }

    @Bean
    @ConditionalOnMissingBean
    public MediaCountService mediaCountService() {
        return new MockMediaCountService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelCountService modelCountService() {
        return new MockModelCountService();
    }

    @Bean
    @ConditionalOnMissingBean
    public PersonnelCountService personnelCountService() {
        return new MockPersonnelCountService();
    }

    @Bean
    @ConditionalOnMissingBean
    public DashboardService dashboardService(DeviceStatusService deviceStatusService,
                                             FlightTaskQueryService taskQueryService,
                                             WaylineQueryService waylineQueryService,
                                             MediaCountService mediaCountService,
                                             ModelCountService modelCountService,
                                             PersonnelCountService personnelCountService) {
        return new DashboardService(deviceStatusService, taskQueryService,
                waylineQueryService, mediaCountService, modelCountService,
                personnelCountService);
    }

    @Bean
    @ConditionalOnMissingBean
    public DashboardRestController dashboardRestController(DashboardService service) {
        return new DashboardRestController(service);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlightTaskCommandService flightTaskCommandService() {
        return new MockFlightTaskCommandService();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlightTaskConflictService flightTaskConflictService() {
        return new MockFlightTaskConflictService();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlightTaskRestController flightTaskRestController(FlightTaskQueryService queryService,
                                                             FlightTaskCommandService commandService,
                                                             FlightTaskConflictService conflictService) {
        return new FlightTaskRestController(queryService, commandService, conflictService);
    }
}
