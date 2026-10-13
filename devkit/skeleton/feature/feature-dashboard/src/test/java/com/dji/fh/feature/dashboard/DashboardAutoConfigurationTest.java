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

import static org.assertj.core.api.Assertions.assertThat;

import com.dji.fh.feature.dashboard.spi.FlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MediaCountService;
import com.dji.fh.feature.dashboard.spi.MockFlightTaskConflictService;
import com.dji.fh.feature.dashboard.spi.MockFlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MockMediaCountService;
import com.dji.fh.feature.dashboard.spi.MockModelCountService;
import com.dji.fh.feature.dashboard.spi.MockPersonnelCountService;
import com.dji.fh.feature.dashboard.spi.MockWaylineQueryService;
import com.dji.fh.feature.dashboard.spi.ModelCountService;
import com.dji.fh.feature.dashboard.spi.PersonnelCountService;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.feature.device.DeviceAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class DashboardAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DeviceAutoConfiguration.class,
                    DashboardAutoConfiguration.class
            ))
            .withBean(com.dji.fh.kernel.spi.query.DeviceQueryService.class,
                    () -> java.util.Collections::emptyList);

    @Test
    void shouldRegisterDefaultBeans() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(DashboardService.class);
            assertThat(ctx).hasSingleBean(DashboardRestController.class);
            assertThat(ctx).hasSingleBean(FlightTaskRestController.class);
            assertThat(ctx).hasSingleBean(FlightTaskQueryService.class);
            assertThat(ctx).hasSingleBean(FlightTaskConflictService.class);
            assertThat(ctx).hasSingleBean(WaylineQueryService.class);
            assertThat(ctx).hasSingleBean(MediaCountService.class);
            assertThat(ctx).hasSingleBean(ModelCountService.class);
            assertThat(ctx).hasSingleBean(PersonnelCountService.class);
        });
    }

    @Test
    void defaultFlightTaskQueryService_shouldBeMockImpl() {
        runner.run(ctx -> {
            FlightTaskQueryService service = ctx.getBean(FlightTaskQueryService.class);
            assertThat(service).isInstanceOf(MockFlightTaskQueryService.class);
        });
    }

    @Test
    void defaultFlightTaskConflictService_shouldBeMockImpl() {
        runner.run(ctx -> {
            FlightTaskConflictService service = ctx.getBean(FlightTaskConflictService.class);
            assertThat(service).isInstanceOf(MockFlightTaskConflictService.class);
        });
    }

    @Test
    void customFlightTaskConflictService_shouldReplaceDefault() {
        runner.withBean(FlightTaskConflictService.class, () -> candidate -> java.util.List.of())
                .run(ctx -> {
                    FlightTaskConflictService service = ctx.getBean(FlightTaskConflictService.class);
                    assertThat(service).isNotInstanceOf(MockFlightTaskConflictService.class);
                });
    }

    @Test
    void defaultWaylineQueryService_shouldBeMockImpl() {
        runner.run(ctx -> {
            WaylineQueryService service = ctx.getBean(WaylineQueryService.class);
            assertThat(service).isInstanceOf(MockWaylineQueryService.class);
        });
    }

    @Test
    void defaultMediaCountService_shouldBeMockImpl() {
        runner.run(ctx -> {
            MediaCountService service = ctx.getBean(MediaCountService.class);
            assertThat(service).isInstanceOf(MockMediaCountService.class);
        });
    }

    @Test
    void defaultModelCountService_shouldBeMockImpl() {
        runner.run(ctx -> {
            ModelCountService service = ctx.getBean(ModelCountService.class);
            assertThat(service).isInstanceOf(MockModelCountService.class);
        });
    }

    @Test
    void defaultPersonnelCountService_shouldBeMockImpl() {
        runner.run(ctx -> {
            PersonnelCountService service = ctx.getBean(PersonnelCountService.class);
            assertThat(service).isInstanceOf(MockPersonnelCountService.class);
        });
    }

    @Test
    void customFlightTaskQueryService_shouldReplaceDefault() {
        runner.withBean(FlightTaskQueryService.class, () -> range -> java.util.List.of())
                .run(ctx -> {
                    FlightTaskQueryService service = ctx.getBean(FlightTaskQueryService.class);
                    assertThat(service).isNotInstanceOf(MockFlightTaskQueryService.class);
                });
    }

    @Test
    void customWaylineQueryService_shouldReplaceDefault() {
        runner.withBean(WaylineQueryService.class, () -> () -> 999)
                .run(ctx -> {
                    WaylineQueryService service = ctx.getBean(WaylineQueryService.class);
                    assertThat(service).isNotInstanceOf(MockWaylineQueryService.class);
                    assertThat(service.countWaylines()).isEqualTo(999);
                });
    }

    @Test
    void customMediaCountService_shouldReplaceDefault() {
        runner.withBean(MediaCountService.class, () -> () -> 500)
                .run(ctx -> {
                    MediaCountService service = ctx.getBean(MediaCountService.class);
                    assertThat(service).isNotInstanceOf(MockMediaCountService.class);
                    assertThat(service.countMediaFiles()).isEqualTo(500);
                });
    }
}
