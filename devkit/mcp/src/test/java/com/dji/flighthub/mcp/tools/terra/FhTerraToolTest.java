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

package com.dji.flighthub.mcp.tools.terra;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhTerraTool 单元测试。
 * 覆盖 session/workspace 校验、action 分发、per-action 参数校验及 mock 模式返回结构。
 *
 * @author DJI FlightHub SDK Team
 */
class FhTerraToolTest {

    private SessionManager sessionManager;
    private FhTerraTool terraTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = TerraTestHelper.createSdkServiceWithWorkspace();
        terraTool = new FhTerraTool(sessionManager, sdkService);
    }

    // ── 工具定义测试 ──

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = terraTool.getToolDefinition();

        assertEquals("fh_terra", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldHaveAllActions() {
        Map<String, Object> def = terraTool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertEquals(8, enumValues.size());
        assertTrue(enumValues.contains("create_reconstruction"));
        assertTrue(enumValues.contains("create_panorama"));
        assertTrue(enumValues.contains("status"));
        assertTrue(enumValues.contains("model_list"));
        assertTrue(enumValues.contains("model_url"));
        assertTrue(enumValues.contains("quality_report"));
        assertTrue(enumValues.contains("delete_model"));
        assertTrue(enumValues.contains("retry"));
    }

    // ── session / workspace 校验 ──

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = terraTool.execute(Map.of("action", "status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        SdkService noWorkspace = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhTerraTool tool = new FhTerraTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "status", "task_id", "t1"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    // ── action 校验 ──

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "invalid_action"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenActionMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ── create_reconstruction 参数校验 ──

    @Test
    void execute_shouldReturnError_whenCreateReconstructionMissingMediaIds() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_reconstruction",
                "reconstruction_type", "mesh_3d"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("media_ids"));
    }

    @Test
    void execute_shouldReturnError_whenCreateReconstructionMissingType() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_reconstruction",
                "media_ids", List.of("folder_001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("reconstruction_type"));
    }

    @Test
    void execute_shouldReturnError_whenCreateReconstructionInvalidType() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_reconstruction",
                "media_ids", List.of("folder_001"),
                "reconstruction_type", "invalid_type"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("reconstruction_type"));
    }

    // ── create_panorama 参数校验 ──

    @Test
    void execute_shouldReturnError_whenCreatePanoramaMissingMediaIds() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_panorama",
                "name", "Test Panorama"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("media_ids"));
    }

    @Test
    void execute_shouldReturnError_whenCreatePanoramaMissingName() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_panorama",
                "media_ids", List.of("folder_001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("name"));
    }

    // ── status 参数校验 ──

    @Test
    void execute_shouldReturnError_whenStatusMissingTaskId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_id"));
    }

    // ── model_url 参数校验 ──

    @Test
    void execute_shouldReturnError_whenModelUrlMissingFileId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "model_url"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_id"));
    }

    // ── quality_report 参数校验 ──

    @Test
    void execute_shouldReturnError_whenQualityReportMissingTaskId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "quality_report"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_id"));
    }

    // ── 正常路径 — create_reconstruction (需确认) ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenCreateReconstructionConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_reconstruction",
                "media_ids", List.of("folder_001", "folder_002"),
                "reconstruction_type", "mesh_3d",
                "name", "Site Survey 3D",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("create_reconstruction", data.get("action"));
        assertEquals("mesh_3d", data.get("reconstruction_type"));
        assertEquals("Site Survey 3D", data.get("name"));
        assertEquals("processing", data.get("status"));
        assertEquals(2, data.get("media_count"));
        assertNotNull(data.get("file_uuid"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void execute_shouldRequireConfirm_whenCreateReconstructionWithoutConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_reconstruction",
                "media_ids", List.of("folder_001"),
                "reconstruction_type", "ortho_2d"
        ));

        // 未确认应返回错误（confirm 缺失或 false）
        assertEquals(false, result.get("success"));
        String errorMsg = result.get("error_message").toString().toLowerCase();
        assertTrue(errorMsg.contains("confirm") || errorMsg.contains("confirmation"));
    }

    // ── 正常路径 — create_panorama (需确认) ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenCreatePanoramaConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "create_panorama",
                "media_ids", List.of("folder_001", "folder_002", "folder_003"),
                "name", "Site Panorama",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("create_panorama", data.get("action"));
        assertEquals("Site Panorama", data.get("name"));
        assertEquals("processing", data.get("status"));
        assertEquals(3, data.get("media_count"));
        assertNotNull(data.get("file_uuid"));
        assertEquals(true, data.get("_mock"));
    }

    // ── 正常路径 — status (只读，免确认) ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenStatusCalled() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "status",
                "task_id", "recon_12345"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("status", data.get("action"));
        assertEquals("recon_12345", data.get("task_id"));
        assertEquals("completed", data.get("status"));
        assertEquals(100, data.get("progress"));
        assertNotNull(data.get("reconstruction_type"));
        assertEquals(true, data.get("_mock"));
    }

    // ── 正常路径 — model_list (只读，免确认) ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenModelListCalled() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "model_list",
                "page", 1,
                "page_size", 10
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("model_list", data.get("action"));
        List<?> models = (List<?>) data.get("models");
        assertNotNull(models);
        assertFalse(models.isEmpty());
        assertEquals(1, data.get("page"));
        assertEquals(10, data.get("page_size"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldClampPageSize_whenExceedsMax() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "model_list",
                "page_size", 999
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(100, data.get("page_size"));
    }

    // ── 正常路径 — model_url (只读，免确认) ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenModelUrlCalled() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "model_url",
                "file_id", 1001,
                "model_type", 1
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("model_url", data.get("action"));
        assertEquals(1001, data.get("file_id"));
        assertEquals(1, data.get("model_type"));
        assertNotNull(data.get("url"));
        assertTrue(data.get("url").toString().contains("mock_1001"));
        assertNotNull(data.get("expires_at"));
        assertEquals(true, data.get("_mock"));
    }

    // ── 正常路径 — quality_report (只读，免确认) ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenQualityReportCalled() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "quality_report",
                "task_id", "recon_67890",
                "language", "en"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("quality_report", data.get("action"));
        assertEquals("recon_67890", data.get("task_id"));
        assertEquals(85, data.get("overall_score"));
        assertEquals("en", data.get("language"));
        assertNotNull(data.get("metrics"));
        Map<String, Object> metrics = (Map<String, Object>) data.get("metrics");
        assertNotNull(metrics.get("ground_resolution"));
        assertNotNull(metrics.get("coverage_percentage"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUseDefaultLanguage_whenLanguageNotProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "quality_report",
                "task_id", "recon_11111"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("zh", data.get("language"));
    }

    // ── requiresHumanConfirmation ──

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(terraTool.requiresHumanConfirmation());
    }

    // ── delete_model 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenDeleteModelMissingFileId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "delete_model",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_id"));
    }

    @Test
    void execute_shouldReturnError_whenDeleteModelFileIdNotInteger() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "delete_model",
                "confirm", true,
                "file_id", "not_a_number"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("must be an integer"));
    }

    @Test
    void execute_shouldReturnError_whenDeleteModelMissingConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "delete_model",
                "file_id", 123
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldDeleteModel_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "delete_model",
                "confirm", true,
                "file_id", 42
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("delete_model", data.get("action"));
        assertEquals(42, data.get("file_id"));
        assertEquals(true, data.get("deleted"));
        assertEquals(true, data.get("_mock"));
    }

    // ── retry 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenRetryMissingTaskId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "retry",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("task_id"));
    }

    @Test
    void execute_shouldReturnError_whenRetryMissingConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "retry",
                "task_id", "recon_failed_001"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldRetryReconstruction_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "retry",
                "confirm", true,
                "task_id", "recon_failed_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("retry", data.get("action"));
        assertEquals("recon_failed_001", data.get("task_id"));
        assertEquals("processing", data.get("state"));
        assertEquals(true, data.get("_mock"));
    }
}
