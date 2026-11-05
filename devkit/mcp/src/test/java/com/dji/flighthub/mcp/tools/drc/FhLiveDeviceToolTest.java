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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.DockLiveCapacity;
import com.dji.flighthubsdk.bean.DockLiveStatus;
import com.dji.flighthubsdk.bean.LiveCapacityCamera;
import com.dji.flighthubsdk.bean.LiveCapacityDevice;
import com.dji.flighthubsdk.bean.LiveCapacityVideo;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveStatusItem;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * FhLiveDeviceTool 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhLiveDeviceToolTest {

    private static final String DEVICE_SN = "DRONE-LIVE-DEV-001";

    private SessionManager sessionManager;
    private FhLiveDeviceTool mockTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService mockSdk = new SdkService() {
            @Override
            public boolean isMockMode() { return true; }
        };
        mockTool = new FhLiveDeviceTool(sessionManager, mockSdk);
    }

    // ==================== 基础校验 ====================

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        assertEquals("fh_live_device", mockTool.getToolDefinition().get("name"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = mockTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "available_range"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "available_range"));
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "bogus"));
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    // ==================== mock mode ====================

    @Test
    @SuppressWarnings("unchecked")
    void availableRange_shouldReturnMockStreams() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "available_range"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        assertEquals(2, data.get("count"));
        List<Map<String, Object>> streams = (List<Map<String, Object>>) data.get("streams");
        assertEquals(2, streams.size());
        assertEquals(DEVICE_SN, streams.get(0).get("device_sn"));
        assertNotNull(streams.get(0).get("video_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void dockLiveStatus_shouldReturnMockStatus() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "dock_live_status"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        List<Map<String, Object>> statuses = (List<Map<String, Object>>) data.get("live_status");
        assertFalse(statuses.isEmpty());
        assertNotNull(statuses.get(0).get("video_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void dockLiveCapacity_shouldReturnMockCapacity() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "dock_live_capacity"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        assertEquals(3L, data.get("available_video_number"));
        assertEquals(3L, data.get("coexist_video_number_max"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void audienceCount_shouldReturnMockCount() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "audience_count"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertFalse(items.isEmpty());
        assertEquals(DEVICE_SN, items.get(0).get("sn"));
        assertNotNull(items.get(0).get("count"));
    }

    // ==================== real mode ====================

    private FhLiveDeviceTool realToolWithDrone(LiveDeviceModule module) {
        Drone drone = mock(Drone.class);
        when(drone.getLiveDeviceModule()).thenReturn(module);
        SdkService realSdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return drone; }
        };
        return new FhLiveDeviceTool(sessionManager, realSdk);
    }

    @Test
    @SuppressWarnings("unchecked")
    void availableRange_shouldReturnRealStreams() {
        sessionManager.createSession("uuid", "ws");
        LiveDeviceModule module = mock(LiveDeviceModule.class);

        LiveInfo info1 = new LiveInfo();
        info1.setDeviceSn(DEVICE_SN);
        info1.setPayloadIndex("81-0-0");
        info1.setVideoId(DEVICE_SN + "/81-0-0/normal-0");

        LiveInfo info2 = new LiveInfo();
        info2.setDeviceSn(DEVICE_SN);
        info2.setPayloadIndex("81-0-0");
        info2.setVideoId(DEVICE_SN + "/81-0-0/wide-0");

        when(module.listenLiveAvailableRange(any())).thenAnswer(inv -> {
            com.dji.flighthubsdk.FlightHubCallback.LiveAvailableRangeCallback cb = inv.getArgument(0);
            cb.onResult(List.of(info1, info2));
            return 1;
        });

        Map<String, Object> result = realToolWithDrone(module).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "available_range"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("count"));
        List<Map<String, Object>> streams = (List<Map<String, Object>>) data.get("streams");
        assertEquals(DEVICE_SN + "/81-0-0/normal-0", streams.get(0).get("video_id"));
        assertEquals(DEVICE_SN + "/81-0-0/wide-0", streams.get(1).get("video_id"));
    }

    @Test
    void availableRange_shouldFailWhenListenIdNegative() {
        sessionManager.createSession("uuid", "ws");
        LiveDeviceModule module = mock(LiveDeviceModule.class);
        when(module.listenLiveAvailableRange(any())).thenReturn(-1);

        Map<String, Object> result = realToolWithDrone(module).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "available_range"));

        assertEquals(false, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void dockLiveStatus_shouldReturnRealStatus() {
        sessionManager.createSession("uuid", "ws");
        LiveDeviceModule module = mock(LiveDeviceModule.class);

        LiveStatusItem statusItem = new LiveStatusItem();
        statusItem.setVideoId(DEVICE_SN + "/81-0-0/normal-0");
        statusItem.setVideoType("normal-0");
        statusItem.setVideoQuality(80);
        statusItem.setStatus(1);
        statusItem.setErrorStatus(0);

        DockLiveStatus status = new DockLiveStatus();
        status.setLiveStatus(List.of(statusItem));

        when(module.listenDockLiveStatus(any())).thenAnswer(inv -> {
            com.dji.flighthubsdk.FlightHubCallback.DockLiveStatusCallback cb = inv.getArgument(0);
            cb.onResult(status);
            return 1;
        });

        Map<String, Object> result = realToolWithDrone(module).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "dock_live_status"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> statuses = (List<Map<String, Object>>) data.get("live_status");
        assertEquals(1, statuses.size());
        assertEquals(DEVICE_SN + "/81-0-0/normal-0", statuses.get(0).get("video_id"));
        assertEquals(1L, statuses.get(0).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void dockLiveCapacity_shouldReturnRealCapacity() {
        sessionManager.createSession("uuid", "ws");
        LiveDeviceModule module = mock(LiveDeviceModule.class);

        LiveCapacityVideo video = new LiveCapacityVideo();
        video.setVideoIndex("0");
        video.setVideoType("normal-0");
        video.setSwitchableVideoTypes(List.of("wide-0", "zoom-0"));

        LiveCapacityCamera camera = new LiveCapacityCamera();
        camera.setCameraIndex("81-0-0");
        camera.setAvailableVideoNumber(2);
        camera.setCoexistVideoNumberMax(2);
        camera.setVideoList(List.of(video));

        LiveCapacityDevice device = new LiveCapacityDevice();
        device.setSn(DEVICE_SN);
        device.setAvailableVideoNumber(3);
        device.setCoexistVideoNumberMax(3);
        device.setCameraList(List.of(camera));

        DockLiveCapacity capacity = new DockLiveCapacity();
        capacity.setAvailableVideoNumber(3);
        capacity.setCoexistVideoNumberMax(3);
        capacity.setDeviceList(List.of(device));

        when(module.listenDockLiveCapacity(any())).thenAnswer(inv -> {
            com.dji.flighthubsdk.FlightHubCallback.DockLiveCapacityCallback cb = inv.getArgument(0);
            cb.onResult(capacity);
            return 1;
        });

        Map<String, Object> result = realToolWithDrone(module).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "dock_live_capacity"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(3L, data.get("available_video_number"));
        assertEquals(3L, data.get("coexist_video_number_max"));
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
        assertEquals(1, devices.size());
        assertEquals(DEVICE_SN, devices.get(0).get("sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void audienceCount_shouldReturnRealCount() {
        sessionManager.createSession("uuid", "ws");
        LiveDeviceModule module = mock(LiveDeviceModule.class);

        com.dji.flighthubsdk.bean.DeviceAudienceCountItem item =
                new com.dji.flighthubsdk.bean.DeviceAudienceCountItem(DEVICE_SN, 7);

        doAnswer(inv -> {
            com.dji.flighthubsdk.FlightHubCallback.GetAudienceCountCallback cb = inv.getArgument(1);
            cb.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                    com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.SUCCESS), List.of(item));
            return null;
        }).when(module).getAudienceCount(any(), any());

        Map<String, Object> result = realToolWithDrone(module).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "audience_count",
                "level_1_group", "region-east"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> items = (List<Map<String, Object>>) data.get("items");
        assertEquals(1, items.size());
        assertEquals(DEVICE_SN, items.get(0).get("sn"));
        assertEquals(7, items.get(0).get("count"));
    }

    @Test
    void audienceCount_shouldReturnError_whenSdkFails() {
        sessionManager.createSession("uuid", "ws");
        LiveDeviceModule module = mock(LiveDeviceModule.class);

        doAnswer(inv -> {
            com.dji.flighthubsdk.FlightHubCallback.GetAudienceCountCallback cb = inv.getArgument(1);
            cb.onResult(new com.dji.flighthubsdk.bean.FlightHubError(
                    com.dji.flighthubsdk.generated.FlightHubSdkErrorCode.UNKNOWN), List.of());
            return null;
        }).when(module).getAudienceCount(any(), any());

        Map<String, Object> result = realToolWithDrone(module).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "audience_count"));

        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldFailWhenDeviceNotFound() {
        sessionManager.createSession("uuid", "ws");
        SdkService nullSdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return null; }
            @Override public Dock getDock(String sn) { return null; }
        };
        FhLiveDeviceTool tool = new FhLiveDeviceTool(sessionManager, nullSdk);

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "available_range"));
        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("unavailable"));
    }

    @Test
    void execute_shouldFailWhenModuleNull() {
        sessionManager.createSession("uuid", "ws");
        Drone drone = mock(Drone.class);
        when(drone.getLiveDeviceModule()).thenReturn(null);
        SdkService sdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return drone; }
        };
        FhLiveDeviceTool tool = new FhLiveDeviceTool(sessionManager, sdk);

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "available_range"));
        assertEquals(false, result.get("success"));
    }
}
