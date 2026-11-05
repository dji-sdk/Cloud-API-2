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

package com.dji.flighthub.mcp.tools.livesync;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.bean.LiveSyncCameraCapacity;
import com.dji.flighthubsdk.bean.LiveSyncCreateResult;
import com.dji.flighthubsdk.bean.LiveSyncDeviceItemV2;
import com.dji.flighthubsdk.bean.LiveSyncDeviceV2;
import com.dji.flighthubsdk.bean.LiveSyncDevicesV2Result;
import com.dji.flighthubsdk.bean.LiveSyncItem;
import com.dji.flighthubsdk.bean.LiveSyncListResult;
import com.dji.flighthubsdk.bean.LiveSyncSchemaOption;
import com.dji.flighthubsdk.bean.LiveSyncSummaryResult;
import com.dji.flighthubsdk.bean.LiveSyncVideoCapacity;
import com.dji.flighthubsdk.core.LiveSyncModule;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FhLiveSyncTool 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhLiveSyncToolTest {

    private SessionManager sessionManager;
    private FhLiveSyncTool mockTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService mockSdk = new SdkService() {
            @Override public boolean isMockMode() { return true; }
        };
        mockTool = new FhLiveSyncTool(sessionManager, mockSdk);
    }

    // ==================== 基础校验 ====================

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        assertEquals("fh_live_sync", mockTool.getToolDefinition().get("name"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = mockTool.execute(Map.of("action", "devices"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenUnknownAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "bogus"));
        assertEquals(false, result.get("success"));
    }

    // ==================== mock 模式 ====================

    @Test
    @SuppressWarnings("unchecked")
    void devices_shouldReturnMockData_inMockMode() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "devices"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        assertEquals(1L, data.get("total"));
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
        assertFalse(devices.isEmpty());
        Map<String, Object> host = ((List<Map<String, Object>>) devices.get(0).get("hosts")).get(0);
        assertEquals("DRONE-MOCK-001", host.get("sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldReturnMockItem_inMockMode() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "action", "create", "sn", "DRONE-001", "camera_index", "81-0-0",
                "schema", "rtmp", "schema_option", Map.of("url", "rtmp://test")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        Map<String, Object> item = (Map<String, Object>) data.get("item");
        assertEquals("DRONE-001", item.get("sn"));
        assertEquals("81-0-0", item.get("camera_index"));
        assertEquals("rtmp", item.get("schema"));
    }

    @Test
    void create_shouldFail_whenMissingRequiredParams() {
        sessionManager.createSession("uuid", "ws");
        assertEquals(false, mockTool.execute(Map.of("action", "create")).get("success"));
        assertEquals(false, mockTool.execute(Map.of("action", "create", "sn", "X")).get("success"));
        assertEquals(false, mockTool.execute(Map.of("action", "create", "sn", "X", "camera_index", "Y")).get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void update_shouldReturnMockItem_inMockMode() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of(
                "action", "update", "uuid", "test-uuid", "enable", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> item = (Map<String, Object>) ((Map<String, Object>) result.get("data")).get("item");
        assertEquals("test-uuid", item.get("uuid"));
        assertEquals(2, item.get("status"));
        assertEquals(true, item.get("enable"));
    }

    @Test
    void update_shouldFail_whenMissingUuid() {
        sessionManager.createSession("uuid", "ws");
        assertEquals(false, mockTool.execute(Map.of("action", "update")).get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void list_shouldReturnEmptyMockList() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "list"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(0L, data.get("total"));
        assertEquals(List.of(), data.get("list"));
    }

    @Test
    void delete_shouldFail_whenMissingUuid() {
        sessionManager.createSession("uuid", "ws");
        assertEquals(false, mockTool.execute(Map.of("action", "delete")).get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void delete_shouldReturnMockSuccess() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "delete", "uuid", "del-uuid"));
        assertEquals(true, result.get("success"));
        assertEquals("del-uuid", ((Map<String, Object>) result.get("data")).get("uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void summary_shouldReturnMockCounts() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "summary"));
        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(0L, data.get("total"));
        assertEquals(0L, data.get("enable_count"));
    }

    // ==================== 真实模式 ====================

    private FhLiveSyncTool realTool(LiveSyncModule module) {
        Workspace workspace = mock(Workspace.class);
        when(workspace.getLiveSyncModule()).thenReturn(module);
        SdkService realSdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Workspace getWorkspace() { return workspace; }
        };
        return new FhLiveSyncTool(sessionManager, realSdk);
    }

    private static FlightHubError successError() {
        return new FlightHubError(FlightHubSdkErrorCode.SUCCESS);
    }

    private static FlightHubError failError() {
        return new FlightHubError(FlightHubSdkErrorCode.ERROR_OPERATION_FAILED, 400, 1001, "test failure");
    }

    @Test
    @SuppressWarnings("unchecked")
    void devices_shouldReturnRealDeviceList() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);

        LiveSyncVideoCapacity video = new LiveSyncVideoCapacity();
        video.setVideoType("normal-0");
        video.setVideoIndex("0");
        LiveSyncCameraCapacity camera = new LiveSyncCameraCapacity();
        camera.setCameraIndex("81-0-0");
        camera.setVideosList(List.of(video));
        LiveSyncDeviceV2 drone = new LiveSyncDeviceV2();
        drone.setSn("DRONE-REAL");
        drone.setName("Test Drone");
        drone.setDeviceType("drone");
        drone.setCameraCapacity(List.of(camera));
        LiveSyncDeviceItemV2 devItem = new LiveSyncDeviceItemV2();
        LiveSyncDeviceV2 gw = new LiveSyncDeviceV2();
        gw.setSn("DOCK-REAL");
        gw.setDeviceType("dock");
        devItem.setGateway(gw);
        devItem.setHosts(List.of(drone));
        LiveSyncDevicesV2Result devResult = new LiveSyncDevicesV2Result();
        devResult.setTotal(1);
        devResult.setList(List.of(devItem));

        doAnswer(inv -> {
            FlightHubCallback.GetLiveSyncDevicesV2Callback cb = inv.getArgument(0);
            cb.onResult(successError(), devResult);
            return null;
        }).when(module).getLiveSyncDevicesV2(any());

        Map<String, Object> result = realTool(module).execute(Map.of("action", "devices"));
        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1L, data.get("total"));
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
        Map<String, Object> host = ((List<Map<String, Object>>) devices.get(0).get("hosts")).get(0);
        assertEquals("DRONE-REAL", host.get("sn"));
        List<Map<String, Object>> cameras = (List<Map<String, Object>>) host.get("camera_capacity");
        assertEquals("81-0-0", cameras.get(0).get("camera_index"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_shouldCallSdkAndReturnItem() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);

        LiveSyncItem item = new LiveSyncItem();
        item.setUuid("real-uuid");
        item.setStatus(1);
        item.setSn("DRONE-R");
        item.setCameraIndex("81-0-0");
        item.setSchema("rtmp");
        LiveSyncSchemaOption opt = new LiveSyncSchemaOption();
        opt.setUrl("rtmp://server/stream");
        item.setSchemaOption(opt);
        LiveSyncCreateResult createResult = new LiveSyncCreateResult();
        createResult.setItem(item);

        doAnswer(inv -> {
            FlightHubCallback.CreateLiveSyncCallback cb = inv.getArgument(1);
            cb.onResult(successError(), createResult);
            return null;
        }).when(module).createLiveSync(any(), any());

        Map<String, Object> result = realTool(module).execute(Map.of(
                "action", "create", "sn", "DRONE-R", "camera_index", "81-0-0",
                "schema", "rtmp", "video_type", "normal-0", "video_quality", 80,
                "schema_option", Map.of("url", "rtmp://server/stream")));

        assertEquals(true, result.get("success"));
        Map<String, Object> retItem = (Map<String, Object>) ((Map<String, Object>) result.get("data")).get("item");
        assertEquals("real-uuid", retItem.get("uuid"));
        assertEquals("rtmp", retItem.get("schema"));
        Map<String, Object> retOpt = (Map<String, Object>) retItem.get("schema_option");
        assertEquals("rtmp://server/stream", retOpt.get("url"));
    }

    @Test
    void create_shouldFailOnSdkError() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);
        doAnswer(inv -> {
            FlightHubCallback.CreateLiveSyncCallback cb = inv.getArgument(1);
            cb.onResult(failError(), null);
            return null;
        }).when(module).createLiveSync(any(), any());

        assertThrows(SdkCallbackBridge.SdkException.class, () ->
                realTool(module).execute(Map.of("action", "create", "sn", "X",
                        "camera_index", "Y", "schema", "rtmp")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void update_shouldPassEnable() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);

        LiveSyncItem updatedItem = new LiveSyncItem();
        updatedItem.setUuid("upd-uuid");
        updatedItem.setEnable(true);
        updatedItem.setStatus(2);
        doAnswer(inv -> {
            FlightHubCallback.UpdateLiveSyncCallback cb = inv.getArgument(1);
            cb.onResult(successError(), updatedItem);
            return null;
        }).when(module).updateLiveSync(any(), any());

        Map<String, Object> result = realTool(module).execute(Map.of(
                "action", "update", "uuid", "upd-uuid", "enable", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> retItem = (Map<String, Object>) ((Map<String, Object>) result.get("data")).get("item");
        assertEquals("upd-uuid", retItem.get("uuid"));
        assertEquals(true, retItem.get("enable"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void list_shouldPassPaginationAndFilters() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);

        LiveSyncItem item = new LiveSyncItem();
        item.setUuid("list-uuid");
        item.setSn("SN-1");
        LiveSyncListResult listResult = new LiveSyncListResult();
        listResult.setPage(2);
        listResult.setSize(5);
        listResult.setTotal(10);
        listResult.setList(List.of(item));
        doAnswer(inv -> {
            FlightHubCallback.GetLiveSyncListCallback cb = inv.getArgument(1);
            cb.onResult(successError(), listResult);
            return null;
        }).when(module).getLiveSyncList(any(), any());

        Map<String, Object> result = realTool(module).execute(Map.of(
                "action", "list", "page", 2, "size", 5, "sn", "SN-1", "schema", "rtmp", "enable", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("page"));
        assertEquals(5, data.get("size"));
        assertEquals(10L, data.get("total"));
        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
        assertEquals("list-uuid", list.get(0).get("uuid"));
    }

    @Test
    void delete_shouldCallSdkWithUuid() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);
        doAnswer(inv -> {
            FlightHubCallback.DeleteLiveSyncCallback cb = inv.getArgument(1);
            cb.onResult(successError());
            return null;
        }).when(module).deleteLiveSync(eq("del-uuid"), any());

        Map<String, Object> result = realTool(module).execute(Map.of("action", "delete", "uuid", "del-uuid"));

        assertEquals(true, result.get("success"));
        verify(module).deleteLiveSync(eq("del-uuid"), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void summary_shouldReturnRealCounts() {
        sessionManager.createSession("uuid", "ws");
        LiveSyncModule module = mock(LiveSyncModule.class);

        LiveSyncSummaryResult summaryResult = new LiveSyncSummaryResult();
        summaryResult.setTotal(5);
        summaryResult.setEnableCnt(3);
        doAnswer(inv -> {
            FlightHubCallback.GetLiveSyncSummaryCallback cb = inv.getArgument(0);
            cb.onResult(successError(), summaryResult);
            return null;
        }).when(module).getLiveSyncSummary(any());

        Map<String, Object> result = realTool(module).execute(Map.of("action", "summary"));
        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(5L, data.get("total"));
        assertEquals(3L, data.get("enable_count"));
    }

    @Test
    void execute_shouldFailWhenWorkspaceNull() {
        sessionManager.createSession("uuid", "ws");
        SdkService nullWsSdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Workspace getWorkspace() { return null; }
        };
        FhLiveSyncTool tool = new FhLiveSyncTool(sessionManager, nullWsSdk);

        Map<String, Object> result = tool.execute(Map.of("action", "devices"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldFailWhenModuleNull() {
        sessionManager.createSession("uuid", "ws");
        Workspace workspace = mock(Workspace.class);
        when(workspace.getLiveSyncModule()).thenReturn(null);
        SdkService nullModSdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Workspace getWorkspace() { return workspace; }
        };
        FhLiveSyncTool tool = new FhLiveSyncTool(sessionManager, nullModSdk);

        Map<String, Object> result = tool.execute(Map.of("action", "devices"));
        assertEquals(false, result.get("success"));
    }
}
