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

import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.DockOsdData;
import com.dji.fh.kernel.spi.event.DockOsdUpdateEvent;
import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import com.dji.fh.kernel.spi.event.HmsAlertLevel;
import com.dji.fh.kernel.spi.event.OsdData;
import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import com.dji.fh.kernel.spi.query.DeviceQueryService.DeviceSnapshot;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DeviceStatusService 单元测试。
 *
 * <p>纯 Java 单测，不依赖 Spring context。直接 new service + 手动调 @EventListener 方法。
 *
 * @author DJI DevKit Team
 */
class DeviceStatusServiceTest {

    private static final Instant T0 = Instant.parse("2026-06-29T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(T0, ZoneOffset.UTC);

    /** 测试用存活超时：远大于用例内的时间推进量之外，另由 LivenessTests 单独覆盖超时分支 */
    private static final Duration TEST_LIVENESS_TIMEOUT = Duration.ofMinutes(5);

    /** 可配置的 stub DeviceQueryService */
    private final List<DeviceSnapshot> stubDevices = new ArrayList<>();
    private final DeviceQueryService stubQueryService = () -> List.copyOf(stubDevices);

    private DeviceStatusService service;

    @BeforeEach
    void setup() {
        stubDevices.clear();
        DeviceProperties props = new DeviceProperties(3, true, TEST_LIVENESS_TIMEOUT, null);
        service = new DeviceStatusService(props, stubQueryService, FIXED_CLOCK);
    }

    /**
     * 可手动推进的时钟，用于验证存活超时分支。
     */
    private static final class MutableClock extends Clock {

        private final AtomicReference<Instant> now = new AtomicReference<>(T0);

        void advance(Duration delta) {
            now.updateAndGet(current -> current.plus(delta));
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now.get();
        }
    }

    // ---------- 事件处理测试（OSD/HMS 缓存层） ----------

    @Nested
    class EventHandlingTests {

        @Test
        void onDeviceOnline_shouldCreateEntry_whenNewDevice() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "DroneA"));

            // 通过 stub queryService 返回空 → fallback 到内存缓存
            DeviceState state = service.findBySn("SN-1").orElseThrow();
            assertThat(state.online()).isTrue();
            assertThat(state.deviceType()).isEqualTo(DeviceType.DRONE);
            assertThat(state.model()).isEqualTo("M30T");
            assertThat(state.name()).isEqualTo("DroneA");
            assertThat(state.onlineSince()).isEqualTo(T0);
        }

        @Test
        void onDeviceOnline_shouldUpdateEntry_whenDeviceAlreadyKnown() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "Old"));
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "New"));

            DeviceState state = service.findBySn("SN-1").orElseThrow();
            assertThat(state.name()).isEqualTo("New");
        }

        @Test
        void onDeviceOnline_shouldNotResetOnlineSince_whenAlreadyOnline() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "Drone"));
            Instant firstOnlineSince = service.findBySn("SN-1").orElseThrow().onlineSince();

            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "Drone"));

            assertThat(service.findBySn("SN-1").orElseThrow().onlineSince()).isEqualTo(firstOnlineSince);
        }

        @Test
        void onDeviceOffline_shouldMarkOffline_whenDeviceKnown() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, null, null));
            service.onDeviceOffline(new DeviceOfflineEvent("mock", "SN-1", DeviceType.DRONE, "timeout"));

            DeviceState state = service.findBySn("SN-1").orElseThrow();
            assertThat(state.online()).isFalse();
            assertThat(state.onlineSince()).isNull();
        }

        @Test
        void onDeviceOffline_shouldCreateOfflineEntry_whenDeviceUnknown() {
            service.onDeviceOffline(new DeviceOfflineEvent("mock", "UNKNOWN", DeviceType.DOCK, "x"));

            // 必须补建条目：否则「SDK 快照 online=true + 无 entry」的机场永远无法被纠正为离线
            DeviceState state = service.findBySn("UNKNOWN").orElseThrow();
            assertThat(state.online()).isFalse();
            // 补建的 entry 从未收到过名称，name 仍需非空（兜底 sn），避免前端渲染出空串
            assertThat(state.name()).isEqualTo("UNKNOWN");
        }

        @Test
        void onOsdUpdate_shouldUpdateLastOsd_whenDeviceKnown() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, null, null));
            OsdData osd = new OsdData(Map.of("lat", 40.0, "lng", 120.0));
            service.onOsdUpdate(new OsdUpdateEvent("mock", "SN-1", osd));

            DeviceState state = service.findBySn("SN-1").orElseThrow();
            assertThat(state.lastOsd()).isEqualTo(osd);
            assertThat(state.lastOsdAt()).isEqualTo(T0);
        }

        @Test
        void onOsdUpdate_shouldIgnore_whenDeviceUnknown() {
            OsdData osd = new OsdData(Map.of("lat", 40.0));
            service.onOsdUpdate(new OsdUpdateEvent("mock", "GHOST", osd));

            assertThat(service.findBySn("GHOST")).isEmpty();
        }

        @Test
        void onDockOsdUpdate_shouldUpdateLastOsd_whenDeviceKnown() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "DOCK-1", DeviceType.DOCK, "D2", "机场"));
            DockOsdData dockOsd = new DockOsdData(Map.of("lat", 39.9, "lng", 116.4, "temperature", 25.0));
            service.onDockOsdUpdate(new DockOsdUpdateEvent("poller", "DOCK-1", dockOsd));

            DeviceState state = service.findBySn("DOCK-1").orElseThrow();
            assertThat(state.lastOsd()).isNotNull();
            assertThat(state.lastOsd().get("lat")).contains(39.9);
            assertThat(state.lastOsd().get("lng")).contains(116.4);
            assertThat(state.lastOsdAt()).isEqualTo(T0);
        }

        @Test
        void onDockOsdUpdate_shouldIgnore_whenDeviceUnknown() {
            DockOsdData dockOsd = new DockOsdData(Map.of("lat", 39.9));
            service.onDockOsdUpdate(new DockOsdUpdateEvent("poller", "GHOST-DOCK", dockOsd));

            assertThat(service.findBySn("GHOST-DOCK")).isEmpty();
        }

        @Test
        void onHmsAlert_shouldAppendToDeque_whenDeviceKnown() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, null, null));
            service.onHmsAlert(new HmsAlertEvent("mock", "SN-1", "FH-001", HmsAlertLevel.WARNING, null));

            DeviceState state = service.findBySn("SN-1").orElseThrow();
            assertThat(state.recentHms()).hasSize(1);
            assertThat(state.recentHms().get(0).alertCode()).isEqualTo("FH-001");
        }

        @Test
        void onHmsAlert_shouldEvictOldest_whenDequeExceedsMax() {
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, null, null));
            service.onHmsAlert(new HmsAlertEvent("mock", "SN-1", "A1", HmsAlertLevel.INFO, null));
            service.onHmsAlert(new HmsAlertEvent("mock", "SN-1", "A2", HmsAlertLevel.INFO, null));
            service.onHmsAlert(new HmsAlertEvent("mock", "SN-1", "A3", HmsAlertLevel.INFO, null));
            // hmsMax=3，第 4 条应淘汰 A1
            service.onHmsAlert(new HmsAlertEvent("mock", "SN-1", "A4", HmsAlertLevel.INFO, null));

            DeviceState state = service.findBySn("SN-1").orElseThrow();
            assertThat(state.recentHms()).hasSize(3);
            assertThat(state.recentHms().get(0).alertCode()).isEqualTo("A2");
            assertThat(state.recentHms().get(2).alertCode()).isEqualTo("A4");
        }

        @Test
        void onHmsAlert_shouldIgnore_whenDeviceUnknown() {
            service.onHmsAlert(new HmsAlertEvent("mock", "GHOST", "FH-X", HmsAlertLevel.ERROR, null));

            assertThat(service.findBySn("GHOST")).isEmpty();
        }
    }

    // ---------- SDK 直查测试（DeviceQueryService） ----------

    @Nested
    class QueryTests {

        @Test
        void listAll_shouldReturnDevicesFromQueryService() {
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, true));
            stubDevices.add(new DeviceSnapshot("SN-2", "DOCK", "D2", "Dock1", null, true));

            List<DeviceState> result = service.listAll();
            assertThat(result).hasSize(2);
            assertThat(result).extracting(DeviceState::sn).containsExactlyInAnyOrder("SN-1", "SN-2");
        }

        @Test
        void listAll_shouldReturnEmpty_whenQueryServiceReturnsEmpty() {
            List<DeviceState> result = service.listAll();
            assertThat(result).isEmpty();
        }

        @Test
        void listAll_shouldOverlayOsdData_whenCacheHasOsd() {
            // 先通过事件填充 OSD 缓存
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, null, null));
            OsdData osd = new OsdData(Map.of("battery", 75));
            service.onOsdUpdate(new OsdUpdateEvent("mock", "SN-1", osd));

            // QueryService 返回设备
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, true));

            List<DeviceState> result = service.listAll();
            assertThat(result).hasSize(1);
            DeviceState state = result.get(0);
            assertThat(state.sn()).isEqualTo("SN-1");
            assertThat(state.lastOsd()).isEqualTo(osd);
            assertThat(state.model()).isEqualTo("M30T");
        }

        @Test
        void listAll_shouldResolveDockType_fromDeviceClass() {
            stubDevices.add(new DeviceSnapshot("SN-DOCK", "DOCK", null, "MyDock", null, true));

            List<DeviceState> result = service.listAll();
            assertThat(result.get(0).deviceType()).isEqualTo(DeviceType.DOCK);
        }

        @Test
        void listAll_shouldUseNickname_whenPresent() {
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "DefaultName", "MyNickname", true));

            List<DeviceState> result = service.listAll();
            assertThat(result.get(0).name()).isEqualTo("MyNickname");
        }

        @Test
        void findBySn_shouldReturnFromQueryService() {
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, true));

            assertThat(service.findBySn("SN-1")).isPresent();
            assertThat(service.findBySn("SN-1").orElseThrow().model()).isEqualTo("M30T");
        }

        @Test
        void findBySn_shouldFallbackToCache_whenNotInQueryResult() {
            // 内存缓存有数据，但 queryService 返回空
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "Cached"));

            assertThat(service.findBySn("SN-1")).isPresent();
            assertThat(service.findBySn("SN-1").orElseThrow().name()).isEqualTo("Cached");
        }

        @Test
        void findBySn_shouldReturnEmpty_whenNotFoundAnywhere() {
            assertThat(service.findBySn("NONEXIST")).isEmpty();
        }

        @Test
        void listAll_shouldMapChildDevices_whenSnapshotHasSubDevices() {
            DeviceSnapshot drone = new DeviceSnapshot(
                    "DRONE-1", "DRONE", "M30T", "Drone1", null, true);
            DeviceSnapshot dock = new DeviceSnapshot(
                    "DOCK-1", "DOCK", "D2", "Dock1", null, true, List.of(drone));
            stubDevices.add(dock);
            stubDevices.add(drone);

            List<DeviceState> result = service.listAll();
            DeviceState dockState = result.stream()
                    .filter(d -> "DOCK-1".equals(d.sn()))
                    .findFirst().orElseThrow();

            assertThat(dockState.childDevices()).hasSize(1);
            assertThat(dockState.childDevices().get(0).sn()).isEqualTo("DRONE-1");
            assertThat(dockState.childDevices().get(0).deviceType()).isEqualTo(DeviceType.DRONE);

            // 挂载在机场下的飞机不再作为顶层元素重复返回（设备去重缺陷②）
            assertThat(result).extracting(DeviceState::sn).containsExactly("DOCK-1");
        }

        @Test
        void listAll_shouldReturnTopLevelOnce_whenSnapshotDuplicated() {
            DeviceSnapshot drone = new DeviceSnapshot(
                    "DRONE-1", "DRONE", "M30T", "Drone1", null, true);
            stubDevices.add(drone);
            stubDevices.add(new DeviceSnapshot("DRONE-1", "DRONE", "M30T", "Drone1", null, true));

            List<DeviceState> result = service.listAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).sn()).isEqualTo("DRONE-1");
        }

        @Test
        void listAll_shouldKeepTopLevelDevice_whenItsSnMatchesOwnChildDevice() {
            // SDK 异常数据：机场把自身列为子设备时，不应把自己整个去重掉
            DeviceSnapshot self = new DeviceSnapshot(
                    "DOCK-1", "DOCK", "D2", "Dock1", null, true);
            DeviceSnapshot dock = new DeviceSnapshot(
                    "DOCK-1", "DOCK", "D2", "Dock1", null, true, List.of(self));
            stubDevices.add(dock);

            List<DeviceState> result = service.listAll();

            assertThat(result).extracting(DeviceState::sn).containsExactly("DOCK-1");
        }

        @Test
        void listAll_shouldReturnEmptyChildDevices_whenNoSubDevices() {
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, true));

            List<DeviceState> result = service.listAll();
            assertThat(result.get(0).childDevices()).isEmpty();
        }

        @Test
        void listAll_shouldFallbackToSn_whenNameFieldsBlank() {
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "  ", null, true));

            List<DeviceState> result = service.listAll();

            assertThat(result.get(0).name()).isEqualTo("SN-1");
        }

        @Test
        void listAll_shouldFallbackToDeviceName_whenNicknameBlank() {
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", "  ", true));

            List<DeviceState> result = service.listAll();

            assertThat(result.get(0).name()).isEqualTo("Drone1");
        }
    }

    // ---------- 在线状态优先级测试 ----------

    @Nested
    class OnlineStatusPriorityTests {

        @Test
        void listAll_shouldUseEventDrivenOffline_whenSdkQuerySaysOnline() {
            // 设备先上线再下线（事件驱动）
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "Drone1"));
            service.onDeviceOffline(new DeviceOfflineEvent("mock", "SN-1", DeviceType.DRONE, "power_off"));

            // SDK 查询仍返回 online=true（平台 device 表滞后）
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, true));

            List<DeviceState> result = service.listAll();
            assertThat(result).hasSize(1);
            DeviceState state = result.get(0);
            assertThat(state.online()).isFalse();
            assertThat(state.onlineSince()).isNull();
        }

        @Test
        void listAll_shouldUseEventDrivenOnline_whenSdkQuerySaysOffline() {
            // 设备上线（事件驱动）
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "SN-1", DeviceType.DRONE, "M30T", "Drone1"));

            // SDK 查询返回 online=false（极端情况：SDK 查询滞后于上线事件）
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, false));

            List<DeviceState> result = service.listAll();
            assertThat(result).hasSize(1);
            DeviceState state = result.get(0);
            assertThat(state.online()).isTrue();
            assertThat(state.onlineSince()).isEqualTo(T0);
        }

        @Test
        void listAll_shouldUseSdkOnline_whenNoEventCache() {
            // 无事件缓存，SDK 查询返回 online=true
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone1", null, true));

            List<DeviceState> result = service.listAll();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).online()).isTrue();
        }

        @Test
        void listAll_shouldApplyPriorityToChildDevices() {
            // 子设备（飞行器）已下线（事件驱动）
            service.onDeviceOnline(new DeviceOnlineEvent("mock", "DRONE-1", DeviceType.DRONE, "M4TD", "Drone"));
            service.onDeviceOffline(new DeviceOfflineEvent("mock", "DRONE-1", DeviceType.DRONE, "power_off"));

            // SDK 查询：dock 在线，子设备仍标记为 online（平台滞后）
            DeviceSnapshot droneSnap = new DeviceSnapshot(
                    "DRONE-1", "DRONE", "M4TD", "Drone", null, true);
            DeviceSnapshot dockSnap = new DeviceSnapshot(
                    "DOCK-1", "DOCK", "D3", "Dock", null, true, List.of(droneSnap));
            stubDevices.add(dockSnap);
            stubDevices.add(droneSnap);

            List<DeviceState> result = service.listAll();

            // dock 的子设备应使用事件驱动状态（offline）
            DeviceState dockState = result.stream()
                    .filter(d -> "DOCK-1".equals(d.sn()))
                    .findFirst().orElseThrow();
            assertThat(dockState.childDevices()).hasSize(1);
            assertThat(dockState.childDevices().get(0).online()).isFalse();

            // 挂载飞机不重复出现在顶层（设备去重缺陷②），其状态改由父设备承载
            assertThat(result).extracting(DeviceState::sn).containsExactly("DOCK-1");
        }
    }

    // ---------- 存活超时兜底测试 ----------

    /**
     * 覆盖"平台从未推送下线事件、SDK 快照又长期滞后"的场景 —— 现场故障机场的真实状态：
     * {@code lastOsd} 为空、无任何事件，但 SDK 查询返回 {@code online: true}。
     */
    @Nested
    class LivenessTimeoutTests {

        private MutableClock mutableClock;
        private DeviceStatusService livenessService;

        @BeforeEach
        void setupLiveness() {
            mutableClock = new MutableClock();
            livenessService = new DeviceStatusService(
                    new DeviceProperties(3, true, TEST_LIVENESS_TIMEOUT, null), stubQueryService, mutableClock);
        }

        @Test
        void listAll_shouldReportOffline_whenHeartbeatExceedsTimeout() {
            livenessService.onDeviceOnline(
                    new DeviceOnlineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "D3", "机场"));
            // SDK 快照长期滞后：设备已离线但平台 device 表仍写 online=true
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));

            // 超时之前：仍为在线
            mutableClock.advance(TEST_LIVENESS_TIMEOUT.minusSeconds(1));
            assertThat(livenessService.listAll().get(0).online()).isTrue();

            // 超过存活超时且期间无任何 OSD/HMS 心跳 → 收敛为离线
            mutableClock.advance(Duration.ofSeconds(2));
            DeviceState state = livenessService.listAll().get(0);
            assertThat(state.online()).isFalse();
            assertThat(state.onlineSince()).isNull();
        }

        @Test
        void listAll_shouldReportOnline_whenOsdArrivesBeforeTimeout() {
            livenessService.onDeviceOnline(
                    new DeviceOnlineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "D3", "机场"));
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));

            mutableClock.advance(TEST_LIVENESS_TIMEOUT.minusSeconds(1));
            // 心跳刷新了 lastSeenAt，超时窗口重新计时
            livenessService.onDockOsdUpdate(new DockOsdUpdateEvent(
                    "real-sdk", "DOCK-1", new DockOsdData(Map.of("lat", 39.9))));

            mutableClock.advance(Duration.ofSeconds(30));
            assertThat(livenessService.listAll().get(0).online()).isTrue();
        }

        @Test
        void listAll_shouldReportOnline_whenHmsAlertArrivesBeforeTimeout() {
            livenessService.onDeviceOnline(
                    new DeviceOnlineEvent("real-sdk", "SN-1", DeviceType.DRONE, "M30T", "Drone"));
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone", null, true));

            mutableClock.advance(TEST_LIVENESS_TIMEOUT.minusSeconds(1));
            // HMS 告警同样属于存活信号，应刷新 lastSeenAt、重新计时
            livenessService.onHmsAlert(
                    new HmsAlertEvent("real-sdk", "SN-1", "FH-001", HmsAlertLevel.WARNING, null));

            mutableClock.advance(Duration.ofMinutes(1));
            assertThat(livenessService.listAll().get(0).online()).isTrue();
        }

        @Test
        void listAll_shouldReportOffline_whenOnlyStaleHmsAlertExists() {
            livenessService.onDeviceOnline(
                    new DeviceOnlineEvent("real-sdk", "SN-1", DeviceType.DRONE, "M30T", "Drone"));
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone", null, true));

            // HMS 是最后一次存活信号，此后长时间静默 → 判离线（告警本身不代表设备在线）
            livenessService.onHmsAlert(
                    new HmsAlertEvent("real-sdk", "SN-1", "FH-001", HmsAlertLevel.WARNING, null));
            mutableClock.advance(TEST_LIVENESS_TIMEOUT.plusSeconds(1));

            assertThat(livenessService.listAll().get(0).online()).isFalse();
        }

        @Test
        void findBySn_shouldReportOffline_whenHeartbeatExceedsTimeout() {
            livenessService.onDeviceOnline(
                    new DeviceOnlineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "D3", "机场"));

            mutableClock.advance(TEST_LIVENESS_TIMEOUT.plusSeconds(1));

            // queryService 为空 → 走 fallback 分支
            DeviceState state = livenessService.findBySn("DOCK-1").orElseThrow();
            assertThat(state.online()).isFalse();
        }

        @Test
        void listAll_shouldDisableTimeout_whenLivenessTimeoutIsZero() {
            DeviceStatusService noTimeoutService = new DeviceStatusService(
                    new DeviceProperties(3, true, Duration.ZERO, null), stubQueryService, mutableClock);
            noTimeoutService.onDeviceOnline(
                    new DeviceOnlineEvent("real-sdk", "SN-1", DeviceType.DRONE, "M30T", "Drone"));
            stubDevices.add(new DeviceSnapshot("SN-1", "DRONE", "M30T", "Drone", null, true));

            mutableClock.advance(Duration.ofDays(30));

            // 关闭兜底后退回纯事件驱动：无下线事件则维持在线
            assertThat(noTimeoutService.listAll().get(0).online()).isTrue();
        }

        @Test
        void listAll_shouldReportOffline_whenOfflineEventArrivesForUnknownDevice() {
            // 前置：SDK 快照称在线，但设备从未收到过上线事件
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));

            livenessService.onDeviceOffline(
                    new DeviceOfflineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "ws-change"));

            DeviceState state = livenessService.listAll().get(0);
            assertThat(state.online()).isFalse();
        }
    }

    // ---------- 缓存自愈（reconcileOnline）测试 ----------

    /**
     * 覆盖"事件缓存卡死在 offline，但 SDK 快照持续判定为 online"的现场故障场景 ——
     * workspace 设备变更监听在通知中心 WS 未就绪时注册失败/收到迟到的错误下线事件，
     * 之后再无上线事件能纠正缓存。
     */
    @Nested
    class ReconcileCacheHealingTests {

        private static final Duration RECONCILE_WINDOW = Duration.ofSeconds(10);

        private MutableClock mutableClock;
        private DeviceStatusService reconcileService;

        @BeforeEach
        void setupReconcile() {
            mutableClock = new MutableClock();
            reconcileService = new DeviceStatusService(
                    new DeviceProperties(3, true, TEST_LIVENESS_TIMEOUT, RECONCILE_WINDOW),
                    stubQueryService, mutableClock);
        }

        @Test
        void listAll_shouldReconcileToOnline_whenCachedOfflinePersistsPastWindow() {
            // 缓存卡死在 offline（如迟到的错误下线事件），但 SDK 快照持续判定为 online
            reconcileService.onDeviceOffline(
                    new DeviceOfflineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "ws-not-open"));
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));

            // 首次读取只是记下 mismatchSince，还未到确认窗口，仍判离线
            DeviceState first = reconcileService.listAll().get(0);
            assertThat(first.online()).isFalse();

            // 窗口内再次读取（抖动窗口未过）仍不纠正
            mutableClock.advance(RECONCILE_WINDOW.minusSeconds(1));
            assertThat(reconcileService.listAll().get(0).online()).isFalse();

            // 超过确认窗口后纠正为 online
            mutableClock.advance(Duration.ofSeconds(2));
            DeviceState reconciled = reconcileService.listAll().get(0);
            assertThat(reconciled.online()).isTrue();
            assertThat(reconciled.onlineSince()).isEqualTo(mutableClock.instant());
        }

        @Test
        void listAll_shouldStayOffline_whenSnapshotAlsoSaysOffline() {
            // 缓存 offline 且 SDK 快照也是 offline：设备真的离线，不应被纠正
            reconcileService.onDeviceOffline(
                    new DeviceOfflineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "power_off"));
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, false));

            mutableClock.advance(RECONCILE_WINDOW.plusSeconds(1));

            assertThat(reconcileService.listAll().get(0).online()).isFalse();
        }

        @Test
        void listAll_shouldStayOffline_whenSnapshotFlipsBackToOfflineBeforeWindowElapses() {
            // 单次抖动：快照短暂 online 后又变回 offline，不应误纠正
            reconcileService.onDeviceOffline(
                    new DeviceOfflineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "ws-not-open"));
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));

            reconcileService.listAll();
            mutableClock.advance(RECONCILE_WINDOW.minusSeconds(1));

            // 快照抖动回 offline：mismatchSince 应被清零
            stubDevices.clear();
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, false));
            assertThat(reconcileService.listAll().get(0).online()).isFalse();

            // 快照再次变回 online：计时需重新开始，未过窗口不纠正
            stubDevices.clear();
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));
            mutableClock.advance(RECONCILE_WINDOW.minusSeconds(1));
            assertThat(reconcileService.listAll().get(0).online()).isFalse();
        }

        @Test
        void listAll_shouldReMarkOffline_whenGenuineOfflineEventArrivesAfterReconciliation() {
            // 先触发一次缓存自愈纠正为 online
            reconcileService.onDeviceOffline(
                    new DeviceOfflineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "ws-not-open"));
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));
            reconcileService.listAll();
            mutableClock.advance(RECONCILE_WINDOW.plusSeconds(1));
            assertThat(reconcileService.listAll().get(0).online()).isTrue();

            // 之后平台推送真实下线事件，应立即再次生效（不受此前自愈状态影响）
            reconcileService.onDeviceOffline(
                    new DeviceOfflineEvent("real-sdk", "DOCK-1", DeviceType.DOCK, "power_off"));
            stubDevices.clear();
            stubDevices.add(new DeviceSnapshot("DOCK-1", "DOCK", "D3", "机场", null, true));

            assertThat(reconcileService.listAll().get(0).online()).isFalse();
        }

        @Test
        void listAll_shouldNotReconcile_whenNoEventCacheExists() {
            // 无事件缓存条目时直接采用 SDK 快照，不涉及 reconcileOnline 分支
            stubDevices.add(new DeviceSnapshot("SN-NEW", "DRONE", "M30T", "Drone", null, true));

            assertThat(reconcileService.listAll().get(0).online()).isTrue();
        }
    }
}
