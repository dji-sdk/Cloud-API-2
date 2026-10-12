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
import static org.assertj.core.api.Assertions.within;

import com.dji.fh.feature.dashboard.dto.DashboardStatsDto;
import com.dji.fh.feature.dashboard.dto.DockStatsDto;
import com.dji.fh.feature.dashboard.dto.ExecutionStatsDto;
import com.dji.fh.feature.dashboard.dto.FlightStatsDto;
import com.dji.fh.feature.dashboard.dto.TaskStatsDto;
import com.dji.fh.feature.dashboard.spi.FlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MediaCountService;
import com.dji.fh.feature.dashboard.spi.MockFlightTaskQueryService;
import com.dji.fh.feature.dashboard.spi.MockMediaCountService;
import com.dji.fh.feature.dashboard.spi.MockModelCountService;
import com.dji.fh.feature.dashboard.spi.MockPersonnelCountService;
import com.dji.fh.feature.dashboard.spi.MockWaylineQueryService;
import com.dji.fh.feature.dashboard.spi.ModelCountService;
import com.dji.fh.feature.dashboard.spi.PersonnelCountService;
import com.dji.fh.feature.dashboard.spi.WaylineQueryService;
import com.dji.fh.feature.device.DeviceProperties;
import com.dji.fh.feature.device.DeviceStatusService;
import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    /** 固定时钟，保证存活超时兜底不会因真实时间流逝而误判离线。 */
    private static final Instant T0 = Instant.parse("2026-09-14T01:47:19Z");

    /** 与 setUp() 中注入的 DeviceStatusService 保持一致的存活超时。 */
    private static final Duration TEST_LIVENESS_TIMEOUT = Duration.ofMinutes(5);

    /** 测试用机场 SN（合成值，非真实设备）。 */
    private static final String DOCK_SN = "DOCK-SN-1";

    /** 测试用飞机 SN（合成值，非真实设备）。 */
    private static final String DRONE_SN = "DRONE-SN-1";

    private DashboardService dashboardService;
    private DeviceStatusService deviceStatusService;
    private FlightTaskQueryService taskQueryService;
    private WaylineQueryService waylineQueryService;
    private MediaCountService mediaCountService;
    private ModelCountService modelCountService;
    private PersonnelCountService personnelCountService;

    /** SDK 查询结果替身：listAll() 以快照列表为骨架，事件只负责覆盖在线状态。 */
    private List<DeviceSnapshot> stubDevices;

    @BeforeEach
    void setUp() {
        stubDevices = new ArrayList<>();
        deviceStatusService = new DeviceStatusService(
                new DeviceProperties(50, true, TEST_LIVENESS_TIMEOUT, null),
                () -> List.copyOf(stubDevices),
                Clock.fixed(T0, ZoneOffset.UTC));
        taskQueryService = new MockFlightTaskQueryService();
        waylineQueryService = new MockWaylineQueryService();
        mediaCountService = new MockMediaCountService();
        modelCountService = new MockModelCountService();
        personnelCountService = new MockPersonnelCountService();

        dashboardService = new DashboardService(
                deviceStatusService, taskQueryService, waylineQueryService,
                mediaCountService, modelCountService, personnelCountService);
    }

    @Test
    void getStats_shouldReturnNonNullDto() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats).isNotNull();
        assertThat(stats.docks()).isNotNull();
        assertThat(stats.drones()).isNotNull();
        assertThat(stats.relays()).isNotNull();
        assertThat(stats.counts()).isNotNull();
        assertThat(stats.dataResults()).isNotNull();
        assertThat(stats.tasks()).isNotNull();
        assertThat(stats.execution()).isNotNull();
        assertThat(stats.flight()).isNotNull();
        assertThat(stats.devices()).isNotNull();
    }

    @Test
    void getStats_shouldReturnDeterministicResults() {
        DashboardStatsDto first = dashboardService.getStats("week");
        DashboardStatsDto second = dashboardService.getStats("week");
        assertThat(first.flight().count()).isEqualTo(second.flight().count());
        assertThat(first.tasks().daily()).isEqualTo(second.tasks().daily());
    }

    @Test
    void getStats_shouldReturnSevenDaysForWeekRange() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.tasks().daily()).hasSize(7);
        assertThat(stats.execution().daily()).hasSize(7);
        assertThat(stats.flight().daily()).hasSize(7);
    }

    @Test
    void getStats_shouldReturnThirtyDaysForMonthRange() {
        DashboardStatsDto stats = dashboardService.getStats("month");
        assertThat(stats.tasks().daily()).hasSize(30);
        assertThat(stats.execution().daily()).hasSize(30);
        assertThat(stats.flight().daily()).hasSize(30);
    }

    @Test
    void getStats_shouldReturnMockRelaysWithFlag() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.relays().mock()).isTrue();
        assertThat(stats.relays().total()).isEqualTo(10);
    }

    @Test
    void getStats_shouldReturnPersonnelAsMockWhenZero() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.counts().personnel().mock()).isTrue();
        assertThat(stats.counts().personnel().value()).isZero();
    }

    @Test
    void getStats_shouldReturnWaylineCountFromService() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.counts().waylines()).isEqualTo(128);
    }

    @Test
    void getStats_shouldReturnMediaCountFromService() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.dataResults().media().total()).isEqualTo(128);
    }

    @Test
    void getStats_shouldReturnModelCountFromService() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.dataResults().models().total()).isEqualTo(24);
    }

    @Test
    void getStats_shouldReturnMockReportsWithFlag() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.dataResults().reports().mock()).isTrue();
    }

    @Test
    void getStats_shouldMarkDistanceAsRealWithinFlight() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        // 里程通过逐任务轨迹查询获取，不再标记为 mock
        assertThat(stats.flight().distanceKm().mock()).isFalse();
    }

    @Test
    void getStats_shouldReturnEmptyDeviceListWithoutEvents() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        assertThat(stats.devices()).isEmpty();
        assertThat(stats.docks().total()).isZero();
        assertThat(stats.drones().total()).isZero();
    }

    /**
     * 机场状态四字段语义固化测试。
     *
     * <p>现场现象是「机场已离线，看板仍显示 空闲 1（100.0%）、离线 0」。
     * 本组用例从看板出口锁定 {@code (total, idle, working, debugging, offline)} 的映射：
     * {@code working} / {@code debugging} 恒为 0（本服务不消费任务进度事件），
     * {@code idle} 等于在线机场数、{@code offline} 等于离线机场数，四项之和恒等于 {@code total}。
     */
    @Nested
    class DockStatsTests {

        /**
         * 注册一台 SDK 快照里 {@code online=true} 的机场 —— 模拟平台 device 表未回写离线的真实情形。
         */
        private void givenDockSnapshot(String sn, boolean snapshotOnline) {
            stubDevices.add(new DeviceSnapshot(sn, "DOCK", "D3", "机场", null, snapshotOnline));
        }

        /** 上线一台机场（快照同时登记，listAll 以快照列表为骨架）。 */
        private void givenOnlineDock(String sn) {
            givenDockSnapshot(sn, true);
            deviceStatusService.onDeviceOnline(
                    new DeviceOnlineEvent("test", sn, DeviceType.DOCK, "Dock3", "机场3"));
        }

        @Test
        void dockStats_shouldBeAllZero_whenNoDockAtAll() {
            DockStatsDto docks = dashboardService.getStats("week").docks();
            assertThat(docks.total()).isZero();
            assertThat(docks.idle()).isZero();
            assertThat(docks.working()).isZero();
            assertThat(docks.debugging()).isZero();
            assertThat(docks.offline()).isZero();
        }

        @Test
        void dockStats_shouldCountIdleAndNeverWorkingOrDebugging_whenDockOnline() {
            givenOnlineDock(DOCK_SN);

            DockStatsDto docks = dashboardService.getStats("week").docks();
            assertThat(docks.total()).isEqualTo(1);
            assertThat(docks.idle()).isEqualTo(1);
            // 未接入任务进度事件前，在线机场一律计入「空闲」，不得凭空产生作业/调试计数
            assertThat(docks.working()).isZero();
            assertThat(docks.debugging()).isZero();
            assertThat(docks.offline()).isZero();
        }

        @Test
        void dockStats_shouldCountOffline_whenOfflineEventArrives() {
            givenOnlineDock(DOCK_SN);
            deviceStatusService.onDeviceOffline(
                    new DeviceOfflineEvent("test", DOCK_SN, DeviceType.DOCK, "关闭"));

            DockStatsDto docks = dashboardService.getStats("week").docks();
            assertThat(docks.total()).isEqualTo(1);
            assertThat(docks.idle()).isZero();
            assertThat(docks.working()).isZero();
            assertThat(docks.debugging()).isZero();
            assertThat(docks.offline()).isEqualTo(1);
        }

        @Test
        void dockStats_shouldCountOffline_whenOnlyOfflineEventArrivesForUnknownDock() {
            // 设备此前从未收到上线事件，只收到下线事件，
            // 而 SDK 快照仍回报 online=true —— 必须被判定为离线
            givenDockSnapshot(DOCK_SN, true);
            deviceStatusService.onDeviceOffline(
                    new DeviceOfflineEvent("test", DOCK_SN, DeviceType.DOCK, "心跳超时"));

            DockStatsDto docks = dashboardService.getStats("week").docks();
            assertThat(docks.total()).isEqualTo(1);
            assertThat(docks.idle()).isZero();
            assertThat(docks.offline()).isEqualTo(1);
        }

        @Test
        void dockStats_shouldNotCountDrones() {
            stubDevices.add(new DeviceSnapshot(
                    DRONE_SN, "DRONE", "M4TD", "飞机", null, true));
            deviceStatusService.onDeviceOnline(new DeviceOnlineEvent(
                    "test", DRONE_SN, DeviceType.DRONE, "M4TD", "飞机"));

            DockStatsDto docks = dashboardService.getStats("week").docks();
            assertThat(docks.total()).isZero();
            assertThat(docks.idle()).isZero();
            assertThat(docks.offline()).isZero();
        }

        @Test
        void dockStats_shouldKeepSumEqualToTotal() {
            givenOnlineDock(DOCK_SN);
            givenOnlineDock("DOCK-SN-2");
            deviceStatusService.onDeviceOffline(
                    new DeviceOfflineEvent("test", "DOCK-SN-2", DeviceType.DOCK, "关闭"));

            DockStatsDto docks = dashboardService.getStats("week").docks();
            assertThat(docks.total()).isEqualTo(2);
            assertThat(docks.idle()).isEqualTo(1);
            assertThat(docks.offline()).isEqualTo(1);
            assertThat(docks.idle() + docks.working() + docks.debugging() + docks.offline())
                    .isEqualTo(docks.total());
        }
    }

    @Test
    void tasks_shouldAggregateByType() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        TaskStatsDto tasks = stats.tasks();
        for (TaskStatsDto.DailyEntry entry : tasks.daily()) {
            assertThat(entry.planned()).isGreaterThanOrEqualTo(0);
            assertThat(entry.manual()).isGreaterThanOrEqualTo(0);
            assertThat(entry.planned() + entry.manual()).isGreaterThan(0);
        }
    }

    @Test
    void execution_shouldAggregateByStatus() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        ExecutionStatsDto execution = stats.execution();
        for (ExecutionStatsDto.DailyEntry entry : execution.daily()) {
            assertThat(entry.success()).isGreaterThanOrEqualTo(0);
            assertThat(entry.suspended()).isGreaterThanOrEqualTo(0);
            assertThat(entry.failed()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    void flight_shouldHaveRealCountAndDurationWithMockedDistance() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        FlightStatsDto flight = stats.flight();
        assertThat(flight.count()).isGreaterThan(0);
        assertThat(flight.durationMinutes()).isGreaterThan(0);
        // 里程不再标记为 mock，值来自模拟任务的距离累加，应大于 0
        assertThat(flight.distanceKm().mock()).isFalse();
        assertThat(flight.distanceKm().value()).isGreaterThan(0);
    }

    @Test
    void flight_dailyMinutes_shouldSumToTotalDuration() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        FlightStatsDto flight = stats.flight();
        double dailySum = flight.daily().stream()
                .mapToDouble(FlightStatsDto.DailyEntry::minutes)
                .sum();
        // 时长按秒累加后统一换算，日明细与总计的偏差应仅来自各自的 2 位小数舍入
        assertThat(flight.durationMinutes()).isCloseTo(dailySum, within(0.5));
    }

    @Test
    void dailyDates_shouldBeInChronologicalOrder() {
        DashboardStatsDto stats = dashboardService.getStats("week");
        List<String> dates = stats.tasks().daily().stream()
                .map(TaskStatsDto.DailyEntry::date)
                .toList();
        for (int i = 1; i < dates.size(); i++) {
            assertThat(LocalDate.parse(dates.get(i)))
                    .isAfter(LocalDate.parse(dates.get(i - 1)));
        }
    }

    @Test
    void getStats_shouldGracefullyHandleTaskQueryFailure() {
        FlightTaskQueryService failingService = range -> {
            throw new RuntimeException("SDK unavailable");
        };
        DashboardService serviceWithFailure = new DashboardService(
                deviceStatusService, failingService, waylineQueryService,
                mediaCountService, modelCountService, personnelCountService);

        DashboardStatsDto stats = serviceWithFailure.getStats("week");

        assertThat(stats.tasks().daily()).hasSize(7);
        assertThat(stats.tasks().daily()).allMatch(
                e -> e.planned() == 0 && e.manual() == 0);
        assertThat(stats.flight().count()).isZero();
        assertThat(stats.flight().durationMinutes()).isZero();
        assertThat(stats.flight().distanceKm().mock()).isFalse();
    }

    @Test
    void getStats_shouldGracefullyHandleWaylineQueryFailure() {
        WaylineQueryService failingService = () -> {
            throw new RuntimeException("SDK unavailable");
        };
        DashboardService serviceWithFailure = new DashboardService(
                deviceStatusService, taskQueryService, failingService,
                mediaCountService, modelCountService, personnelCountService);

        DashboardStatsDto stats = serviceWithFailure.getStats("week");
        assertThat(stats.counts().waylines()).isZero();
    }

    @Test
    void getStats_shouldGracefullyHandleMediaCountFailure() {
        MediaCountService failingService = () -> {
            throw new RuntimeException("SDK unavailable");
        };
        DashboardService serviceWithFailure = new DashboardService(
                deviceStatusService, taskQueryService, waylineQueryService,
                failingService, modelCountService, personnelCountService);

        DashboardStatsDto stats = serviceWithFailure.getStats("week");
        assertThat(stats.dataResults().media().total()).isZero();
    }

    @Test
    void getStats_shouldGracefullyHandleModelCountFailure() {
        ModelCountService failingService = () -> {
            throw new RuntimeException("SDK unavailable");
        };
        DashboardService serviceWithFailure = new DashboardService(
                deviceStatusService, taskQueryService, waylineQueryService,
                mediaCountService, failingService, personnelCountService);

        DashboardStatsDto stats = serviceWithFailure.getStats("week");
        assertThat(stats.dataResults().models().total()).isZero();
    }
}
