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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.dji.fh.kernel.spi.event.DeviceOfflineEvent;
import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import com.dji.fh.kernel.spi.event.HmsAlertLevel;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.flighthubsdk.bean.DeviceChange;
import com.dji.flighthubsdk.bean.DockOnlineStatus;
import com.dji.flighthubsdk.bean.DroneOnlineStatus;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsDataItem;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * RealSdkAdapter 单元测试。
 *
 * <p>SDK bean（{@code WorkspaceDeviceChange} / {@code DeviceChange} / {@code HmsData} 等）
 * 都是 final class，标准 Mockito 不能 mock；测试改用 SDK bean 的公共无参构造器 + setter
 * 构造真实对象。
 *
 * @author DJI DevKit Team
 */
class RealSdkAdapterTest {

    private SdkEventBus bus;
    private RealSdkAdapter adapter;

    @BeforeEach
    void setup() {
        bus = mock(SdkEventBus.class);
        adapter = new RealSdkAdapter(bus);
    }

    @Test
    void onWorkspaceDeviceChange_shouldPublishDeviceOnlineEvent_whenChangeTypeOnline() {
        WorkspaceDeviceChange data = stubChange("ONLINE", List.of("SN-1"));

        adapter.onWorkspaceDeviceChange(data);

        verify(bus).publish(argThat((DeviceOnlineEvent e) ->
                "SN-1".equals(e.deviceSn()) && "real-sdk".equals(e.source())));
    }

    @Test
    void onWorkspaceDeviceChange_shouldPublishDeviceOfflineEvent_whenChangeTypeOffline() {
        WorkspaceDeviceChange data = stubChange("OFFLINE", List.of("SN-2"));

        adapter.onWorkspaceDeviceChange(data);

        verify(bus).publish(argThat((DeviceOfflineEvent e) ->
                "SN-2".equals(e.deviceSn()) && "ws-change".equals(e.reason())));
    }

    @Test
    void onWorkspaceDeviceChange_shouldPublishMultipleEvents_whenMultipleDevicesInOneChange() {
        WorkspaceDeviceChange data = stubChange("ONLINE", List.of("SN-A", "SN-B", "SN-C"));

        adapter.onWorkspaceDeviceChange(data);

        verify(bus).publish(argThat((DeviceOnlineEvent e) -> "SN-A".equals(e.deviceSn())));
        verify(bus).publish(argThat((DeviceOnlineEvent e) -> "SN-B".equals(e.deviceSn())));
        verify(bus).publish(argThat((DeviceOnlineEvent e) -> "SN-C".equals(e.deviceSn())));
    }

    @Test
    void onWorkspaceDeviceChange_shouldSkipUnknownChangeType() {
        WorkspaceDeviceChange data = stubChange("UPDATE", List.of("SN-X"));

        adapter.onWorkspaceDeviceChange(data);

        verifyNoInteractions(bus);
    }

    @Test
    void onWorkspaceDeviceChange_shouldHandleNullDeviceChanges() {
        WorkspaceDeviceChange data = new WorkspaceDeviceChange();
        data.setDeviceChanges(null);

        adapter.onWorkspaceDeviceChange(data);

        verifyNoInteractions(bus);
    }

    @Test
    void onWorkspaceDeviceChange_shouldHandleNullData() {
        adapter.onWorkspaceDeviceChange(null);

        verifyNoInteractions(bus);
    }

    @Test
    void onWorkspaceDeviceChange_shouldHandleNullSnsInChange() {
        // DeviceChange.changeDevices == null（防御性 null 检查）
        WorkspaceDeviceChange data = new WorkspaceDeviceChange();
        DeviceChange change = new DeviceChange();
        change.setChangeType("ONLINE");
        change.setChangeDevices(null);
        data.setDeviceChanges(List.of(change));

        adapter.onWorkspaceDeviceChange(data);

        verifyNoInteractions(bus);
    }

    @Test
    void onAircraftHMS_shouldPublishHmsAlertEvent_forEachHmsDataItem() {
        HmsData data = stubHms(List.of(
                stubBasics("FH-001", 2),
                stubBasics("FH-002", 3)));

        adapter.onAircraftHMS("SN-A", data);

        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "SN-A".equals(e.deviceSn())
                && "FH-001".equals(e.alertCode())
                && HmsAlertLevel.WARNING.equals(e.level())));
        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "FH-002".equals(e.alertCode())
                && HmsAlertLevel.ERROR.equals(e.level())));
    }

    @Test
    void onAircraftHMS_shouldMapAllLevels() {
        HmsData data = stubHms(List.of(
                stubBasics("L0", 0),
                stubBasics("L1", 1),
                stubBasics("L2", 2),
                stubBasics("L3", 3),
                stubBasics("L4", 4),
                stubBasics("L99", 99)));

        adapter.onAircraftHMS("SN-A", data);

        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "L0".equals(e.alertCode()) && HmsAlertLevel.INFO.equals(e.level())));
        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "L1".equals(e.alertCode()) && HmsAlertLevel.INFO.equals(e.level())));
        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "L2".equals(e.alertCode()) && HmsAlertLevel.WARNING.equals(e.level())));
        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "L3".equals(e.alertCode()) && HmsAlertLevel.ERROR.equals(e.level())));
        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "L4".equals(e.alertCode()) && HmsAlertLevel.CRITICAL.equals(e.level())));
        verify(bus).publish(argThat((HmsAlertEvent e) ->
                "L99".equals(e.alertCode()) && HmsAlertLevel.WARNING.equals(e.level())));
    }

    @Test
    void onAircraftHMS_shouldHandleEmptyHmsDataItems() {
        HmsData data = stubHms(Collections.emptyList());

        adapter.onAircraftHMS("SN-A", data);

        verifyNoInteractions(bus);
    }

    @Test
    void onAircraftHMS_shouldHandleNullData() {
        adapter.onAircraftHMS("SN-A", null);

        verifyNoInteractions(bus);
    }

    @Test
    void onAircraftHMS_shouldEmitNullMessage_alwaysInV01() {
        // v0.1：message 字段固定为 null（SDK HMS 翻译表查询留给 ISV / 后续 plan）
        HmsData data = stubHms(List.of(stubBasics("FH-X", 2)));

        adapter.onAircraftHMS("SN-A", data);

        verify(bus).publish(argThat((HmsAlertEvent e) -> e.message() == null));
    }

    @Test
    void onDroneOnlineStatus_shouldPublishOnlineEvent_whenOnline() {
        DroneOnlineStatus status = new DroneOnlineStatus();
        status.setOnline(true);

        adapter.onDroneOnlineStatus("SN-A", DeviceType.DRONE, status);

        verify(bus).publish(argThat((DeviceOnlineEvent e) ->
                "SN-A".equals(e.deviceSn()) && e.deviceType() == DeviceType.DRONE));
    }

    @Test
    void onDroneOnlineStatus_shouldPublishOfflineEvent_whenOffline() {
        DroneOnlineStatus status = new DroneOnlineStatus();
        status.setOnline(false);

        adapter.onDroneOnlineStatus("SN-A", DeviceType.DRONE, status);

        verify(bus).publish(argThat((DeviceOfflineEvent e) ->
                "SN-A".equals(e.deviceSn()) && "heartbeat-offline".equals(e.reason())));
    }

    @Test
    void onDroneOnlineStatus_shouldPublishOfflineEvent_whenNullData() {
        adapter.onDroneOnlineStatus("SN-A", DeviceType.DRONE, null);

        verify(bus).publish(argThat((DeviceOfflineEvent e) ->
                "SN-A".equals(e.deviceSn()) && "heartbeat-offline".equals(e.reason())));
    }

    @Test
    void onDockOnlineStatus_shouldPublishOnlineEvent_whenOnline() {
        DockOnlineStatus status = new DockOnlineStatus();
        status.setOnline(true);

        adapter.onDockOnlineStatus("DOCK-1", status);

        verify(bus).publish(argThat((DeviceOnlineEvent e) ->
                "DOCK-1".equals(e.deviceSn()) && e.deviceType() == DeviceType.DOCK));
    }

    @Test
    void onDockOnlineStatus_shouldPublishOfflineEvent_whenOffline() {
        DockOnlineStatus status = new DockOnlineStatus();
        status.setOnline(false);

        adapter.onDockOnlineStatus("DOCK-1", status);

        verify(bus).publish(argThat((DeviceOfflineEvent e) ->
                "DOCK-1".equals(e.deviceSn()) && "heartbeat-offline".equals(e.reason())));
    }

    @Test
    void onDockOnlineStatus_shouldPublishOfflineEvent_whenNullData() {
        adapter.onDockOnlineStatus("DOCK-1", null);

        verify(bus).publish(argThat((DeviceOfflineEvent e) ->
                "DOCK-1".equals(e.deviceSn()) && "heartbeat-offline".equals(e.reason())));
    }

    // ---------- 测试辅助 ----------

    private static WorkspaceDeviceChange stubChange(String changeType, List<String> sns) {
        WorkspaceDeviceChange data = new WorkspaceDeviceChange();
        DeviceChange change = new DeviceChange();
        change.setChangeType(changeType);
        change.setChangeDevices(new ArrayList<>(sns));
        data.setDeviceChanges(List.of(change));
        return data;
    }

    private static HmsData stubHms(List<HmsDataBasics> items) {
        HmsData data = new HmsData();
        List<HmsDataItem> wrapped = items.stream().map(basics -> {
            HmsDataItem item = new HmsDataItem();
            item.setBasics(basics);
            return item;
        }).toList();
        data.setHmsDataItems(wrapped);
        return data;
    }

    private static HmsDataBasics stubBasics(String code, int level) {
        HmsDataBasics basics = new HmsDataBasics();
        basics.setCode(code);
        basics.setLevel(level);
        return basics;
    }
}
