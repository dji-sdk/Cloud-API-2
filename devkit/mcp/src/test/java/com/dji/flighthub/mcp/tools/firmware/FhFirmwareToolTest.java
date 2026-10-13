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

package com.dji.flighthub.mcp.tools.firmware;

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
 * FhFirmwareTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、Mock 路径、confirm 门控。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFirmwareToolTest {

    private SessionManager sessionManager;
    private FhFirmwareTool tool;

    @BeforeEach
    void setUp() throws Exception {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {  public boolean isMockMode() { return true; } };
        // 用测试专用构造函数注入允许目录，避免依赖 /app/firmware 真实目录与 FH_FIRMWARE_UPLOAD_DIR 环境变量。
        tool = new FhFirmwareTool(sessionManager, sdkService, "/app/firmware");
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_firmware", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "destroy"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== check_status ====================

    @Test
    void executeCheckStatus_shouldReturnError_whenDeviceSnsMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "check_status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sns"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCheckStatus_shouldReturnMockResult() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "check_status",
                "device_sns", List.of("DOCK-001")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("dock_firmware"));
        assertNotNull(data.get("drone_firmware"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== list ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeList_shouldReturnMockFirmwares() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("firmwares"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== query_progress ====================

    @Test
    void executeQueryProgress_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "query_progress"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeQueryProgress_shouldReturnMockProgress() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "query_progress",
                "device_sn", "DOCK-001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(72, data.get("progress"));
    }

    // ==================== release_note ====================

    @Test
    void executeReleaseNote_shouldReturnError_whenModelKeysMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "release_note"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_model_keys"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeReleaseNote_shouldReturnMockNotes() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "release_note",
                "device_model_keys", List.of("dock2")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("release_notes"));
    }

    // ==================== compatibility ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeCompatibility_shouldReturnMockConfig() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "compatibility"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("configs"));
    }

    // ==================== upgrade ====================

    @Test
    void executeUpgrade_shouldReturnError_whenDeviceSnsMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "upgrade"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sns"));
    }

    @Test
    void executeUpgrade_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "upgrade",
                "device_sns", List.of("DOCK-001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUpgrade_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upgrade");
        args.put("device_sns", List.of("DOCK-001", "DOCK-002"));
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("upgrade_triggered", data.get("status"));
        assertEquals(2, data.get("device_count"));
    }

    // ==================== upload ====================

    @Test
    void executeUpload_shouldReturnError_whenFilePathMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "upload"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_path"));
    }

    @Test
    void executeUpload_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload");
        args.put("file_path", "/app/firmware/firmware.bin");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUpload_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload");
        args.put("file_path", "/app/firmware/firmware_v01.07.bin");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("uploaded", data.get("status"));
        assertEquals("/app/firmware/firmware_v01.07.bin", data.get("file_path"));
        assertNotNull(data.get("object_key"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void executeUpload_shouldReject_whenPathTraversal() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload");
        // 路径穿越：词法归一后逃逸出 /app/firmware，即使 confirm=true 也必须拒绝
        args.put("file_path", "/app/firmware/../../etc/passwd");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("must be within"));
    }

    @Test
    void executeUpload_shouldReject_whenAbsolutePathOutsideDir() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload");
        // 绝对路径位于允许目录之外（非穿越，直接越界）
        args.put("file_path", "/etc/passwd");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("must be within"));
    }

    // ==================== submit ====================

    @Test
    void executeSubmit_shouldReturnError_whenModelKeyMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "submit");
        args.put("version", "v01.07.0100");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_model_key"));
    }

    @Test
    void executeSubmit_shouldReturnError_whenVersionMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "submit");
        args.put("device_model_key", "dock2");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("version"));
    }

    @Test
    void executeSubmit_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "submit");
        args.put("device_model_key", "dock2");
        args.put("version", "v01.07.0100");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSubmit_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "submit");
        args.put("device_model_key", "dock2");
        args.put("version", "v01.07.0100");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("submitted", data.get("status"));
        assertEquals("dock2", data.get("device_model_key"));
        assertEquals("v01.07.0100", data.get("version"));
        assertNotNull(data.get("file_id"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== release / unrelease / delete ====================

    @Test
    void executeRelease_shouldReturnError_whenFileIdMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "release");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeRelease_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "release");
        args.put("file_id", "fw_001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("released", data.get("status"));
    }

    @Test
    void executeUnrelease_shouldReturnError_whenFileIdMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "unrelease");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_id"));
    }

    @Test
    void executeUnrelease_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "unrelease");
        args.put("file_id", "fw_001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUnrelease_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "unrelease");
        args.put("file_id", "fw_001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("unreleased", data.get("status"));
        assertEquals("fw_001", data.get("file_id"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void executeDelete_shouldReturnError_whenFileIdMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_id"));
    }

    @Test
    void executeDelete_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete");
        args.put("file_id", "fw_001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeDelete_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete");
        args.put("file_id", "fw_002");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("deleted", data.get("status"));
        assertEquals("fw_002", data.get("file_id"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }
}
