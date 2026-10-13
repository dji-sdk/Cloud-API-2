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

package com.dji.flighthub.mcp.tools.camera;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhCameraTool Mock 模式单元测试。
 * 验证参数校验、mock 数据结构。
 */
class FhCameraToolTest {

    private FhCameraTool tool;
    private SessionManager sessionManager;

    private static final String DRONE_SN = "DRONE-CAMERA-001";

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService();
        tool = new FhCameraTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldHaveCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_camera", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("drone_sn"));
        assertTrue(required.contains("action"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "panorama_start"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDroneSn() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "panorama_start"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "nonexistent_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_panoramaStart() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "panorama_start"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("panorama_start", data.get("action"));
        assertEquals("started", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_panoramaStop() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "panorama_stop"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("stopped", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_switchMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "switch_mode",
                "camera_mode", "VIDEO"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("VIDEO", data.get("camera_mode"));
    }

    @Test
    void execute_shouldReturnError_switchMode_invalidMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "switch_mode",
                "camera_mode", "INVALID_MODE"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("camera_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_switchLens() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "switch_lens",
                "lens_type", "ZOOM"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("ZOOM", data.get("lens_type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_nightScene() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "night_scene",
                "night_mode", "auto"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("auto", data.get("night_mode"));
    }

    @Test
    void execute_shouldReturnError_nightScene_invalidMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "night_scene",
                "night_mode", "super_bright"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("night_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_thermalGain() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "thermal_gain",
                "gain_mode", "high"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("high", data.get("gain_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_gimbalReset() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "gimbal_reset",
                "reset_mode", 2
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("reset_mode"));
    }

    @Test
    void execute_shouldReturnError_gimbalReset_invalidMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "gimbal_reset",
                "reset_mode", 99
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("reset_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_zoomAtTarget() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "zoom_at_target");
        args.put("lens_type", "ZOOM");
        args.put("x", 0.3);
        args.put("y", 0.4);
        args.put("width", 0.2);
        args.put("height", 0.2);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("zoom_at_target", data.get("action"));
        assertEquals("ZOOM", data.get("lens_type"));
    }

    @Test
    void execute_shouldReturnError_zoomAtTarget_invalidCoordinates() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "zoom_at_target");
        args.put("lens_type", "ZOOM");
        args.put("x", 1.5);
        args.put("y", 0.4);
        args.put("width", 0.2);
        args.put("height", 0.2);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("x"));
    }

    @Test
    void execute_shouldReturnError_zoomAtTarget_missingLensType() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "zoom_at_target");
        args.put("x", 0.3);
        args.put("y", 0.4);
        args.put("width", 0.2);
        args.put("height", 0.2);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("lens_type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_getCameraState() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "get_camera_state"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("get_camera_state", data.get("action"));
        assertNotNull(data.get("camera_state"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_getGimbalState() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "get_gimbal_state"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("get_gimbal_state", data.get("action"));
        assertNotNull(data.get("gimbal_state"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_photoStorage() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "photo_storage");
        args.put("settings", List.of("INTERNAL", "SDCARD"));

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("photo_storage", data.get("action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_videoStorage() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "video_storage");
        args.put("settings", List.of("INTERNAL", "SDCARD"));

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("video_storage", data.get("action"));
    }

    @Test
    void execute_shouldReturnError_videoStorage_missingSettings() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "video_storage"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("settings"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_getLensState() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "get_lens_state"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("get_lens_state", data.get("action"));
        assertNotNull(data.get("lens_state"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void buildListenerRegistrationError_shouldContainHintAndNextSteps() {
        Map<String, Object> result = FhCameraTool.buildListenerRegistrationError("DRONE-TEST-001", -1);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("error_code=-1"));
        assertNotNull(result.get("hint"));

        List<Map<String, Object>> nextSteps =
                (List<Map<String, Object>>) result.get("next_steps");
        assertNotNull(nextSteps);
        assertEquals(3, nextSteps.size());
        assertEquals("1", nextSteps.get(0).get("step"));
        assertEquals("drone_power", nextSteps.get(0).get("check"));
        assertEquals("2", nextSteps.get(1).get("step"));
        assertEquals("payload_index", nextSteps.get(1).get("check"));
        assertEquals("3", nextSteps.get(2).get("step"));
        assertTrue(nextSteps.get(2).get("hint").toString().contains("DRONE-TEST-001"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnAutoModeCheckInMock_whenCameraPhoto() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "camera_photo"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("camera_photo", data.get("action"));
        assertEquals(true, data.get("auto_mode_check"));
        assertEquals(false, data.get("mode_switched"));
    }

    @Test
    void getToolDefinition_shouldContainPhotoWorkflowGuidance() {
        Map<String, Object> def = tool.getToolDefinition();
        String description = def.get("description").toString();

        assertTrue(description.contains("PHOTO WORKFLOW"),
                "description 应包含 PHOTO WORKFLOW 引导");
        assertTrue(description.contains("camera_photo automatically checks current camera mode"),
                "description 应说明 camera_photo 自动检查并切换模式");
    }

    @Test
    void getToolDefinition_shouldNotReferenceFhPayload() {
        Map<String, Object> def = tool.getToolDefinition();
        String description = def.get("description").toString();

        assertFalse(description.contains("fh_payload"),
                "description 不应包含误导性的 fh_payload 引用");
    }
}
