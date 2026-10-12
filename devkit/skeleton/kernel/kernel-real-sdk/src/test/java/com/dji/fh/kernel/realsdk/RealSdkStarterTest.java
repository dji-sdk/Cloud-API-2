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

package com.dji.fh.kernel.realsdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.KernelReadyEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.Device;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RealSdkStarter 单元测试。
 *
 * <p>通过 mock {@link SdkInvoker} interface 完全隔离 SDK 真实调用 + native lib，CI 友好。
 * {@code getDeviceList} 的 callback 通过 {@code doAnswer} 在调用瞬间触发，模拟 SDK 异步推送。
 *
 * @author DJI DevKit Team
 */
class RealSdkStarterTest {

    private SdkInvoker sdkInvoker;
    private SdkEventBus bus;
    private RealSdkAdapter adapter;
    private OsdAggregator osdAggregator;
    private RealSdkProperties props;
    private RealSdkStarter starter;

    @BeforeEach
    void setup() {
        sdkInvoker = mock(SdkInvoker.class);
        bus = mock(SdkEventBus.class);
        adapter = new RealSdkAdapter(bus);
        props = new RealSdkProperties(
                "t", "test.example.com", "443", "wss://test", "info",
                Duration.ofMillis(500),  // 短超时让测试快
                1000L, Duration.ofSeconds(5),
                Duration.ofSeconds(1), 2,  // 重试延迟 1s，最多 2 次（测试用短值）
                "user_id", "your-auth-value",
                Duration.ZERO);  // 测试中关闭周期调和，避免干扰
        osdAggregator = new OsdAggregator(bus, Clock.fixed(Instant.now(), ZoneOffset.UTC), props);
        starter = new RealSdkStarter(sdkInvoker, adapter, osdAggregator, props, bus);
    }

    @Test
    void onReady_shouldPublishKernelReadyEvent_whenInitializeAndListenSucceed() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        when(sdkInvoker.listenWorkspaceDeviceChanged(any())).thenReturn(7);
        when(sdkInvoker.getVersion()).thenReturn("2.0.0");
        stubGetDeviceListEmpty();

        starter.onReady();

        verify(bus).publish(argThat((KernelReadyEvent e) ->
                "real-sdk".equals(e.source())
                && KernelReadyEvent.Mode.SDK.equals(e.mode())
                && "2.0.0".equals(e.version())));
        assertThat(starter.workspaceListenId()).isEqualTo(7);
    }

    @Test
    void onReady_shouldNotPublishReady_whenInitializeFails() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.ERROR_INIT_FAILED);

        starter.onReady();

        verifyNoInteractions(bus);
        verify(sdkInvoker, never()).listenWorkspaceDeviceChanged(any());
    }

    @Test
    void onReady_shouldPublishDeviceOnlineEventsForInitialDevices() {
        prepareInitializeAndListenIdsSuccess();
        stubGetDeviceListWith(
                deviceTopo("SN-A", "DRONE", "M30T", "DroneA", null),
                deviceTopo("SN-B", "DRONE", "M30T", "DroneB", "Nickname-B"));

        starter.onReady();

        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe
                        && "SN-A".equals(doe.deviceSn())
                        && "DroneA".equals(doe.deviceName())));
        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe
                        && "SN-B".equals(doe.deviceSn())
                        && "Nickname-B".equals(doe.deviceName())));
        verify(bus).publish(any(KernelReadyEvent.class));
    }

    @Test
    void onReady_shouldPublishDeviceOfflineEvent_whenInitialSnapshotIsOffline() {
        prepareInitializeAndListenIdsSuccess();
        stubGetDeviceListWith(deviceTopo("SN-OFFLINE", "DRONE", "M30T", "DroneOffline", null, false));

        starter.onReady();

        verify(bus).publish(argThat(e ->
                e instanceof DeviceOfflineEvent doe && "SN-OFFLINE".equals(doe.deviceSn())));
        verify(bus, never()).publish(argThat(e -> e instanceof DeviceOnlineEvent));
        verify(sdkInvoker, never()).listenDroneOnlineStatus(eq("SN-OFFLINE"), any());
        assertThat(starter.perDeviceListenIds()).doesNotContainKey("SN-OFFLINE");
    }

    @Test
    void onReady_shouldBranchPerDeviceByOwnOnlineFlag_whenDevicesMixed() {
        prepareInitializeAndListenIdsSuccess();
        stubGetDeviceListWith(
                deviceTopo("SN-ON", "DRONE", "M30T", "DroneOn", null, true),
                deviceTopo("SN-OFF", "DRONE", "M30T", "DroneOff", null, false));

        starter.onReady();

        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe && "SN-ON".equals(doe.deviceSn())));
        verify(bus).publish(argThat(e ->
                e instanceof DeviceOfflineEvent doe && "SN-OFF".equals(doe.deviceSn())));
        assertThat(starter.perDeviceListenIds()).containsKey("SN-ON");
        assertThat(starter.perDeviceListenIds()).doesNotContainKey("SN-OFF");
    }

    @Test
    void onReady_shouldContinueWithoutPerDeviceListen_whenGetDeviceListTimesOut() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        when(sdkInvoker.listenWorkspaceDeviceChanged(any())).thenReturn(7);
        when(sdkInvoker.getVersion()).thenReturn("2.0.0");
        // 不调 getDeviceList 的 callback → 让 latch.await 超时
        doNothing().when(sdkInvoker).getDeviceList(any());

        starter.onReady();

        // workspace listen 已注册，KernelReadyEvent 已发，但没 per-device listen
        verify(sdkInvoker).listenWorkspaceDeviceChanged(any());
        verify(bus).publish(any(KernelReadyEvent.class));
        verify(sdkInvoker, never()).listenAircraftLocation(any(), any());
    }

    @Test
    void onReady_shouldContinue_whenGetDeviceListReturnsError() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        when(sdkInvoker.listenWorkspaceDeviceChanged(any())).thenReturn(7);
        when(sdkInvoker.getVersion()).thenReturn("2.0.0");
        doAnswer(invocation -> {
            FlightHubCallback.DeviceListCallback cb = invocation.getArgument(0);
            cb.onResult(null, new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED));
            return null;
        }).when(sdkInvoker).getDeviceList(any());

        starter.onReady();

        verify(bus).publish(any(KernelReadyEvent.class));
        verify(sdkInvoker, never()).listenAircraftLocation(any(), any());
    }

    @Test
    void registerListens_shouldSkipDevice_whenListenThrowsIllegalState() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.listenDroneOnlineStatus(eq("SN-BAD"), any()))
                .thenThrow(new IllegalStateException("Drone instance null"));
        stubGetDeviceListWith(
                deviceTopo("SN-BAD", "DRONE", "", "", null),
                deviceTopo("SN-OK", "DRONE", "", "", null));

        starter.onReady();
        starter.shutdown();

        // SN-BAD 不入 perDeviceListenIds → shutdown 时不调 SN-BAD 的 cancel
        verify(sdkInvoker, never()).cancelListenDroneOnlineStatus(eq("SN-BAD"), anyInt());
        verify(sdkInvoker).cancelListenDroneOnlineStatus(eq("SN-OK"), eq(11));
    }

    @Test
    void registerListens_shouldSkipNegativeListenId_andNotCancelLater() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        when(sdkInvoker.listenWorkspaceDeviceChanged(any())).thenReturn(7);
        when(sdkInvoker.getVersion()).thenReturn("v");
        when(sdkInvoker.listenDroneOnlineStatus(any(), any())).thenReturn(11);
        when(sdkInvoker.listenAircraftHMS(eq("SN-1"), any())).thenReturn(-1);  // 失败
        when(sdkInvoker.listenAircraftLocation(any(), any())).thenReturn(13);
        when(sdkInvoker.listenAircraftBatteryInfo(any(), any())).thenReturn(14);
        when(sdkInvoker.listenAircraftAttitude(any(), any())).thenReturn(15);
        stubGetDeviceListWith(deviceTopo("SN-1", "DRONE", "", "", null));

        starter.onReady();
        starter.shutdown();

        // 不应调 cancelListenAircraftHMS(SN-1, -1)
        verify(sdkInvoker, never()).cancelListenAircraftHMS(eq("SN-1"), eq(-1));
        // 其他 listen 的 cancel 正常调
        verify(sdkInvoker).cancelListenDroneOnlineStatus(eq("SN-1"), eq(11));
        verify(sdkInvoker).cancelListenAircraftLocation(eq("SN-1"), eq(13));
    }

    @Test
    void shutdown_shouldCancelAllListensAndShutdownSdk() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListWith(deviceTopo("SN-1", "DRONE", "", "", null));
        starter.onReady();

        starter.shutdown();

        verify(sdkInvoker).cancelListenDroneOnlineStatus(eq("SN-1"), eq(11));
        verify(sdkInvoker).cancelListenAircraftHMS(eq("SN-1"), eq(12));
        verify(sdkInvoker).cancelListenAircraftLocation(eq("SN-1"), eq(13));
        verify(sdkInvoker).cancelListenAircraftBatteryInfo(eq("SN-1"), eq(14));
        verify(sdkInvoker).cancelListenAircraftAttitude(eq("SN-1"), eq(15));
        verify(sdkInvoker).cancelListenWorkspaceDeviceChanged(eq(7));
        verify(sdkInvoker).shutdown();
    }

    @Test
    void shutdown_shouldContinueRemainingCancels_whenOneCancelThrows() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListWith(deviceTopo("SN-1", "DRONE", "", "", null));
        starter.onReady();
        doThrow(new RuntimeException("cancel boom"))
                .when(sdkInvoker).cancelListenAircraftHMS(any(), anyInt());

        starter.shutdown();  // 不应抛出

        // 其他 cancel + shutdown 仍调
        verify(sdkInvoker).cancelListenAircraftLocation(any(), anyInt());
        verify(sdkInvoker).cancelListenWorkspaceDeviceChanged(anyInt());
        verify(sdkInvoker).shutdown();
    }

    @Test
    void shutdown_shouldHandleWorkspaceListenCancelThrow() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        when(sdkInvoker.listenWorkspaceDeviceChanged(any())).thenReturn(7);
        when(sdkInvoker.getVersion()).thenReturn("v");
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListEmpty();
        starter.onReady();
        doThrow(new RuntimeException("ws cancel boom"))
                .when(sdkInvoker).cancelListenWorkspaceDeviceChanged(anyInt());

        starter.shutdown();  // 不应抛出

        verify(sdkInvoker).shutdown();
    }

    // ---------- 测试辅助 ----------

    /**
     * 模拟 initialize + 5 类 per-device listen 都返回正 listenId。
     * sn 任意，listenId 固定 11~15 便于 verify 时校验。
     */
    private void prepareInitializeAndListenIdsSuccess() {
        when(sdkInvoker.initialize(any())).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        when(sdkInvoker.listenWorkspaceDeviceChanged(any())).thenReturn(7);
        when(sdkInvoker.getVersion()).thenReturn("2.0.0");
        when(sdkInvoker.listenDroneOnlineStatus(any(), any())).thenReturn(11);
        when(sdkInvoker.listenAircraftHMS(any(), any())).thenReturn(12);
        when(sdkInvoker.listenAircraftLocation(any(), any())).thenReturn(13);
        when(sdkInvoker.listenAircraftBatteryInfo(any(), any())).thenReturn(14);
        when(sdkInvoker.listenAircraftAttitude(any(), any())).thenReturn(15);
    }

    private void stubGetDeviceListEmpty() {
        doAnswer(invocation -> {
            FlightHubCallback.DeviceListCallback cb = invocation.getArgument(0);
            cb.onResult(List.of(), new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(sdkInvoker).getDeviceList(any());
    }

    private void stubGetDeviceListWith(DeviceTopo... topos) {
        doAnswer(invocation -> {
            FlightHubCallback.DeviceListCallback cb = invocation.getArgument(0);
            cb.onResult(Arrays.asList(topos), new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(sdkInvoker).getDeviceList(any());
    }

    /**
     * 构造一个 DeviceTopo（其内 Device 含指定字段），默认在线（{@code online=true}）。
     */
    private static DeviceTopo deviceTopo(String sn, String deviceClass,
                                          String modelKey, String deviceName, String nickname) {
        return deviceTopo(sn, deviceClass, modelKey, deviceName, nickname, true);
    }

    /**
     * 构造一个 DeviceTopo（其内 Device 含指定字段 + 显式在线态）。
     */
    private static DeviceTopo deviceTopo(String sn, String deviceClass, String modelKey,
                                          String deviceName, String nickname, boolean online) {
        Device device = new Device();
        device.setDeviceSn(sn);
        device.setDeviceClass(deviceClass);
        device.setModelKey(modelKey);
        device.setDeviceName(deviceName);
        device.setNickname(nickname);
        device.setOnline(online);
        DeviceTopo topo = new DeviceTopo();
        topo.setDevice(device);
        topo.setSubDevices(List.of());
        return topo;
    }

    /**
     * 构造含子设备的 DeviceTopo，主设备默认在线。
     */
    private static DeviceTopo deviceTopoWithSub(String sn, String deviceClass,
                                                 String modelKey, String deviceName,
                                                 String nickname, List<Device> subDevices) {
        Device mainDevice = new Device();
        mainDevice.setDeviceSn(sn);
        mainDevice.setDeviceClass(deviceClass);
        mainDevice.setModelKey(modelKey);
        mainDevice.setDeviceName(deviceName);
        mainDevice.setNickname(nickname);
        mainDevice.setOnline(true);
        DeviceTopo topo = new DeviceTopo();
        topo.setDevice(mainDevice);
        topo.setSubDevices(subDevices);
        return topo;
    }

    /**
     * 构造 Device 实例，默认在线（{@code online=true}）。
     */
    private static Device device(String sn, String deviceClass,
                                  String modelKey, String deviceName, String nickname) {
        return device(sn, deviceClass, modelKey, deviceName, nickname, true);
    }

    /**
     * 构造 Device 实例 + 显式在线态。
     */
    private static Device device(String sn, String deviceClass, String modelKey,
                                  String deviceName, String nickname, boolean online) {
        Device d = new Device();
        d.setDeviceSn(sn);
        d.setDeviceClass(deviceClass);
        d.setModelKey(modelKey);
        d.setDeviceName(deviceName);
        d.setNickname(nickname);
        d.setOnline(online);
        return d;
    }

    @Test
    void onReady_shouldProcessSubDevicesInTopo() {
        prepareInitializeAndListenIdsSuccess();
        // 构造一个 topo：主设备是 DOCK（domain 1），子设备是 DRONE
        DeviceTopo topo = deviceTopoWithSub(
                "DOCK-SN", "DOCK", "1-67-0", "DJI Dock", null,
                List.of(device("DRONE-SN", "DRONE", "0-67-0", "M30T", "我的飞机")));
        stubGetDeviceListWith(topo);

        starter.onReady();

        // 主设备和子设备都应产生 DeviceOnlineEvent
        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe && "DOCK-SN".equals(doe.deviceSn())));
        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe && "DRONE-SN".equals(doe.deviceSn())));
    }

    @Test
    void resolveDeviceType_shouldUseDockFromDeviceClass() {
        prepareInitializeAndListenIdsSuccess();
        stubGetDeviceListWith(deviceTopo("SN-DOCK", "DOCK", "1-67-0", "Dock1", null));

        starter.onReady();

        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe
                        && "SN-DOCK".equals(doe.deviceSn())
                        && doe.deviceType() == com.dji.fh.kernel.spi.event.DeviceType.DOCK));
    }

    @Test
    void resolveDeviceType_shouldFallbackToModelKeyDomain1AsDock() {
        prepareInitializeAndListenIdsSuccess();
        // deviceClass 为空但 modelKey domain=1（gateway/dock）
        stubGetDeviceListWith(deviceTopo("SN-GW", "", "1-67-0", "Gateway", null));

        starter.onReady();

        verify(bus).publish(argThat(e ->
                e instanceof DeviceOnlineEvent doe
                        && "SN-GW".equals(doe.deviceSn())
                        && doe.deviceType() == com.dji.fh.kernel.spi.event.DeviceType.DOCK));
    }

    @Test
    void onReady_shouldRegisterDockLocationListen_whenDeviceIsDock() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.listenDockLocation(any(), any())).thenReturn(21);
        stubGetDeviceListWith(deviceTopo("DOCK-1", "DOCK", "1-67-0", "TestDock", null));

        starter.onReady();

        verify(sdkInvoker).listenDockLocation(eq("DOCK-1"), any());
        verify(sdkInvoker, never()).listenDroneOnlineStatus(eq("DOCK-1"), any());
        assertThat(starter.perDeviceListenIds()).containsKey("DOCK-1");
        assertThat(starter.perDeviceListenIds().get("DOCK-1"))
                .containsEntry(RealSdkStarter.ListenKind.DOCK_LOCATION, 21);
    }

    @Test
    void shutdown_shouldCancelDockLocationListen() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.listenDockLocation(any(), any())).thenReturn(21);
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListWith(deviceTopo("DOCK-1", "DOCK", "1-67-0", "TestDock", null));
        starter.onReady();

        starter.shutdown();

        verify(sdkInvoker).cancelListenDockLocation(eq("DOCK-1"), eq(21));
    }

    @Test
    void onReady_shouldRetryDockLocationListen_whenInitialAttemptFails() throws Exception {
        prepareInitializeAndListenIdsSuccess();
        // 第一次返回 -4（通知中心未就绪），第二次成功
        when(sdkInvoker.listenDockLocation(any(), any()))
                .thenReturn(-4)
                .thenReturn(21);
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListWith(deviceTopo("DOCK-1", "DOCK", "1-67-0", "TestDock", null));

        starter.onReady();

        // 初始注册失败，perDeviceListenIds 中没有 DOCK_LOCATION
        assertThat(starter.perDeviceListenIds().get("DOCK-1"))
                .doesNotContainKey(RealSdkStarter.ListenKind.DOCK_LOCATION);

        // 等待重试（props.dockListenRetryDelay = 1s）
        Thread.sleep(1500);

        // 重试成功后应出现在 map 中
        assertThat(starter.perDeviceListenIds().get("DOCK-1"))
                .containsEntry(RealSdkStarter.ListenKind.DOCK_LOCATION, 21);

        starter.shutdown();
    }

    @Test
    void onReady_shouldRegisterDockOnlineListen_whenDeviceIsDock() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.listenDockLocation(any(), any())).thenReturn(21);
        when(sdkInvoker.listenDockOnlineStatus(any(), any())).thenReturn(22);
        stubGetDeviceListWith(deviceTopo("DOCK-1", "DOCK", "1-67-0", "TestDock", null));

        starter.onReady();

        verify(sdkInvoker).listenDockOnlineStatus(eq("DOCK-1"), any());
        assertThat(starter.perDeviceListenIds().get("DOCK-1"))
                .containsEntry(RealSdkStarter.ListenKind.DOCK_ONLINE, 22);
    }

    @Test
    void shutdown_shouldCancelDockOnlineListen() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.listenDockLocation(any(), any())).thenReturn(21);
        when(sdkInvoker.listenDockOnlineStatus(any(), any())).thenReturn(22);
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListWith(deviceTopo("DOCK-1", "DOCK", "1-67-0", "TestDock", null));
        starter.onReady();

        starter.shutdown();

        verify(sdkInvoker).cancelListenDockOnlineStatus(eq("DOCK-1"), eq(22));
    }

    @Test
    void onReady_shouldClassifyBaseStationAsDrone_notDock() {
        prepareInitializeAndListenIdsSuccess();
        // base_station（O4 地面站）不应触发 listenDockLocation
        stubGetDeviceListWith(deviceTopo("BS-1", "base_station", "8-135-0", "O4 Station", null));

        starter.onReady();

        verify(sdkInvoker, never()).listenDockLocation(eq("BS-1"), any());
        verify(sdkInvoker).listenDroneOnlineStatus(eq("BS-1"), any());
    }

    // ---------- A3c 动态 listen 测试 ----------

    @Test
    void onNewDeviceOnline_shouldRegisterListens_whenSnNotYetRegistered() {
        prepareInitializeAndListenIdsSuccess();
        stubGetDeviceListEmpty();
        starter.onReady();

        com.dji.fh.kernel.spi.event.DeviceOnlineEvent event =
                new com.dji.fh.kernel.spi.event.DeviceOnlineEvent("real-sdk", "SN-NEW",
                        com.dji.fh.kernel.spi.event.DeviceType.DRONE, "M30T", "NewDrone");
        starter.onNewDeviceOnline(event);

        verify(sdkInvoker).listenDroneOnlineStatus(eq("SN-NEW"), any());
        verify(sdkInvoker).listenAircraftHMS(eq("SN-NEW"), any());
        verify(sdkInvoker).listenAircraftLocation(eq("SN-NEW"), any());
    }

    @Test
    void onNewDeviceOnline_shouldSkip_whenSnAlreadyRegistered() {
        prepareInitializeAndListenIdsSuccess();
        stubGetDeviceListWith(deviceTopo("SN-EXISTS", "DRONE", "", "", null));
        starter.onReady();

        com.dji.fh.kernel.spi.event.DeviceOnlineEvent event =
                new com.dji.fh.kernel.spi.event.DeviceOnlineEvent("real-sdk", "SN-EXISTS",
                        com.dji.fh.kernel.spi.event.DeviceType.DRONE, null, null);
        starter.onNewDeviceOnline(event);

        verify(sdkInvoker, times(1)).listenDroneOnlineStatus(eq("SN-EXISTS"), any());
    }

    @Test
    void onDeviceOffline_shouldCancelListens_whenSnRegistered() {
        prepareInitializeAndListenIdsSuccess();
        when(sdkInvoker.shutdown()).thenReturn(FlightHubSdkErrorCode.SUCCESS);
        stubGetDeviceListWith(deviceTopo("SN-1", "DRONE", "", "", null));
        starter.onReady();

        com.dji.fh.kernel.spi.event.DeviceOfflineEvent offEvent =
                new com.dji.fh.kernel.spi.event.DeviceOfflineEvent("real-sdk", "SN-1",
                        com.dji.fh.kernel.spi.event.DeviceType.DRONE, "ws-change");
        starter.onDeviceOffline(offEvent);

        verify(sdkInvoker).cancelListenDroneOnlineStatus(eq("SN-1"), eq(11));
        verify(sdkInvoker).cancelListenAircraftHMS(eq("SN-1"), eq(12));
        assertThat(starter.perDeviceListenIds()).doesNotContainKey("SN-1");
    }
}
