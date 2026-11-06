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

package com.dji.flighthub.mcp.tools.safety;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhOfflineMapTool 单元测试。
 * 覆盖 session 校验、action 校验、各操作的 mock 模式执行、破坏性操作确认拦截。
 *
 * @author DJI FlightHub SDK Team
 */
class FhOfflineMapToolTest {

    private SessionManager sessionManager;
    private FhOfflineMapTool offlineMapTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = SafetyTestHelper.createSdkServiceWithWorkspace();
        offlineMapTool = new FhOfflineMapTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = offlineMapTool.getToolDefinition();

        assertEquals("fh_offline_map", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "get_model_info"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "invalid_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnModelInfo_whenGetModelInfo() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "get_model_info"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("3.2.1", data.get("version"));
        assertEquals(256, data.get("size_mb"));
        assertEquals("china_mainland", data.get("coverage"));
        assertEquals("ready", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnUpdateStarted_whenUpdateModelInfoWithModel3dItems() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info",
                "request_params", Map.of(
                        "model_3d_items", List.of(Map.of(
                                "uuid", "model-001",
                                "object_key", "offline-maps/model-001.b3dm",
                                "file_type", "b3dm"
                        ))
                )
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("update_started", data.get("status"));
        assertEquals(1, data.get("model_3d_count"));
        assertEquals(0, data.get("model_2d_count"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldAcceptCamelCaseItemKeys_whenUpdateModelInfo() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info",
                "request_params", Map.of(
                        "model_2d_items", List.of(Map.of(
                                "objectKey", "offline-maps/model-002.tif",
                                "fileType", "tif"
                        ))
                )
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(0, data.get("model_3d_count"));
        assertEquals(1, data.get("model_2d_count"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateModelInfoMissingRequestParams() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("request_params"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateModelInfoRequestParamsEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info",
                "request_params", Map.of()
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Empty request_params"));
        // 必须提示服务端的 225101，避免调用方再次拿到不可解释的 SDK 错误
        assertTrue(result.get("suggestion").toString().contains("225101"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateModelInfoItemsEmptyArrays() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info",
                "request_params", Map.of(
                        "model_3d_items", List.of(),
                        "model_2d_items", List.of()
                )
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Empty request_params"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateModelInfoItemMissingObjectKey() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info",
                "request_params", Map.of(
                        "model_3d_items", List.of(Map.of(
                                "uuid", "model-001",
                                "file_type", "b3dm"
                        ))
                )
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("model_3d_items"));
    }

    @Test
    void execute_shouldReturnError_whenUpdateModelInfoItemNotObject() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "update_model_info",
                "request_params", Map.of(
                        "model_3d_items", List.of("offline-maps/model-001.b3dm")
                )
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("model_3d_items"));
    }

    @Test
    void execute_shouldRequireConfirm_whenCancel() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "cancel"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenCancelWithConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "cancel",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("cancelled", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnConfiguredStatus_whenSetEnabled() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "set_enabled",
                "enabled", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(true, data.get("offline_map_enabled"));
    }

    @Test
    void execute_shouldReturnError_whenSyncMissingDeviceSns() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "sync"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or empty device_sns"));
    }

    @Test
    void execute_shouldDenySync_whenSessionLevelIsL0() {
        // sync 已补 enforceSecurityLevel(REVERSIBLE)：session 上限 L0 拒绝
        FlightHubProperties l0Props = new FlightHubProperties();
        l0Props.getSecurity().setMaxLevel(0);
        SessionManager l0Sessions = new SessionManager(l0Props);
        l0Sessions.createSession("uuid", "ws");
        SdkService l0Sdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhOfflineMapTool l0Tool = new FhOfflineMapTool(l0Sessions, l0Sdk);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "sync",
                "device_sns", List.of("DOCK-001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSyncTriggered_whenSyncWithDeviceSns() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "sync",
                "device_sns", List.of("DOCK-001", "DOCK-002")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("sync_triggered", data.get("status"));
        assertEquals(2, data.get("device_count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnDeviceStatus_whenGetSyncStatus() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "get_sync_status",
                "device_sns", List.of("DOCK-001", "DOCK-002")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");
        assertEquals(2, devices.size());
        assertEquals("DOCK-001", devices.get(0).get("device_sn"));
        assertNotNull(devices.get(0).get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSignalMaps_whenSearchSignalMaps() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "search_signal_maps",
                "device_sns", List.of("DOCK-001")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        List<Map<String, Object>> signalMaps = (List<Map<String, Object>>) data.get("signal_maps");
        assertNotNull(signalMaps);
        assertFalse(signalMaps.isEmpty());
        // 默认应包含 4g_lte 和 5g_nr
        assertEquals("4g_lte", signalMaps.get(0).get("type"));
        assertEquals("5g_nr", signalMaps.get(1).get("type"));
    }

    @Test
    void execute_shouldReturnError_whenInvokeFlysafeUpgradeMissingDeviceSns() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "invoke_flysafe_upgrade");
        args.put("confirm", true);

        Map<String, Object> result = offlineMapTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or empty device_sns"));
    }

    @Test
    void execute_shouldRequireConfirm_whenInvokeFlysafeUpgrade() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "invoke_flysafe_upgrade",
                "device_sns", List.of("DOCK-001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenInvokeFlysafeUpgradeWithConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = offlineMapTool.execute(Map.of(
                "action", "invoke_flysafe_upgrade",
                "device_sns", List.of("DOCK-001", "DOCK-002"),
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("upgrade_triggered", data.get("status"));
        assertEquals(2, data.get("device_count"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(offlineMapTool.requiresHumanConfirmation());
    }

    // ==================== API 移除错误（非 Mock 模式 + workspace 可用）====================

    @Test
    void execute_shouldReturnApiRemovedError_whenInvokeFlysafeUpgradeWithRealSdk() {
        sessionManager.createSession("uuid", "ws");

        SdkService nonMockSdkService = SafetyTestHelper.createNonMockSdkServiceWithWorkspace();
        FhOfflineMapTool nonMockTool = new FhOfflineMapTool(sessionManager, nonMockSdkService);

        Map<String, Object> result = nonMockTool.execute(Map.of(
                "action", "invoke_flysafe_upgrade",
                "device_sns", List.of("DOCK-001"),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK 2.0"));
    }
}
