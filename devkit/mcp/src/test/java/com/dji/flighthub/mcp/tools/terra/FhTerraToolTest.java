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
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    private SdkService sdkService;
    private ListenChannelSupport listenSupport;
    private FhTerraTool terraTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        sdkService = TerraTestHelper.createSdkServiceWithWorkspace();
        listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        terraTool = new FhTerraTool(sessionManager, sdkService, listenSupport);
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

        assertEquals(15, enumValues.size());
        assertTrue(enumValues.contains("create_reconstruction"));
        assertTrue(enumValues.contains("create_panorama"));
        assertTrue(enumValues.contains("status"));
        assertTrue(enumValues.contains("model_list"));
        assertTrue(enumValues.contains("model_url"));
        assertTrue(enumValues.contains("quality_report"));
        assertTrue(enumValues.contains("delete_model"));
        assertTrue(enumValues.contains("retry"));
        assertTrue(enumValues.contains("stop"));
        assertTrue(enumValues.contains("get_sfm"));
        assertTrue(enumValues.contains("batch_delete_model"));
        assertTrue(enumValues.contains("get_images"));
        assertTrue(enumValues.contains("listen_status"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldExposeChannelSchema() {
        Map<String, Object> def = terraTool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");

        Map<String, Object> channelProp = (Map<String, Object>) props.get("channel");
        List<String> channelEnum = (List<String>) channelProp.get("enum");
        assertEquals(List.of("status_changed", "unzip_progress", "model_status_changed"), channelEnum);

        assertNotNull(props.get("channels"));
        assertNotNull(props.get("since_seq"));
        assertNotNull(props.get("limit"));
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
        FhTerraTool tool = new FhTerraTool(sessionManager, noWorkspace, listenSupport);
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

    // ── stop 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenStopMissingFileUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "stop",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_uuid"));
    }

    @Test
    void execute_shouldReturnError_whenStopMissingConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "stop",
                "file_uuid", "recon_stop_001"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopReconstruction_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "stop",
                "confirm", true,
                "file_uuid", "recon_stop_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("stop", data.get("action"));
        assertEquals("recon_stop_001", data.get("file_uuid"));
        assertEquals(true, data.get("stopped"));
        assertEquals(true, data.get("_mock"));
    }

    // ── get_sfm 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenGetSfmMissingFileUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "get_sfm"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSfmResult_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "get_sfm",
                "file_uuid", "recon_sfm_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("get_sfm", data.get("action"));
        assertEquals("recon_sfm_001", data.get("file_uuid"));
        assertNotNull(data.get("sfm_json_url"));
        assertNotNull(data.get("sfm_report_json_url"));
        assertNotNull(data.get("gcp_out_json_url"));
        assertNotNull(data.get("re_projection_error_pic_url_list"));
        assertEquals(true, data.get("_mock"));
    }

    // ── batch_delete_model 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenBatchDeleteModelMissingFileIds() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "batch_delete_model",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_ids"));
    }

    @Test
    void execute_shouldReturnError_whenBatchDeleteModelFileIdsExceedsMax() {
        sessionManager.createSession("uuid", "ws");

        List<Integer> tooMany = new ArrayList<>();
        for (int i = 1; i <= 101; i++) {
            tooMany.add(i);
        }

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "batch_delete_model",
                "confirm", true,
                "file_ids", tooMany
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_ids"));
    }

    @Test
    void execute_shouldReturnError_whenBatchDeleteModelMissingConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "batch_delete_model",
                "file_ids", List.of(101, 102)
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldBatchDeleteModel_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "batch_delete_model",
                "confirm", true,
                "file_ids", List.of(101, 102, 103)
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("batch_delete_model", data.get("action"));
        assertEquals(3, data.get("deleted_count"));
        assertEquals(true, data.get("_mock"));
    }

    // ── get_images 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenGetImagesMissingFileUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "get_images"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnImages_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "get_images",
                "file_uuid", "recon_images_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("get_images", data.get("action"));
        assertEquals("recon_images_001", data.get("file_uuid"));
        List<?> images = (List<?>) data.get("images");
        assertNotNull(images);
        assertFalse(images.isEmpty());
        assertEquals(true, data.get("_mock"));
    }

    // ── listen_status 参数校验及 mock ──

    @Test
    void execute_shouldReturnError_whenListenStatusMissingChannel() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "listen_status"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("channel"));
    }

    @Test
    void execute_shouldReturnError_whenListenStatusChannelInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "listen_status",
                "channel", "not_a_channel"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("channel"));
    }

    @Test
    void execute_shouldReturnError_whenListenStatusHasNoSession() {
        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "listen_status",
                "channel", "status_changed"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenStatus_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "listen_status",
                "channel", "status_changed"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_status", data.get("action"));
        assertEquals("status_changed", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    void execute_shouldNotLeakRegistration_whenListenRepeated() {
        sessionManager.createSession("uuid", "ws");

        terraTool.execute(Map.of("action", "listen_status", "channel", "unzip_progress"));
        terraTool.execute(Map.of("action", "listen_status", "channel", "unzip_progress"));

        Map<String, Object> stop = terraTool.execute(Map.of("action", "listen_stop"));
        Map<String, Object> data = (Map<String, Object>) stop.get("data");

        assertEquals(List.of("unzip_progress"), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
    }

    // ── events 游标读取 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReadEvents_whenChannelActive() {
        sessionManager.createSession("uuid", "ws");
        terraTool.execute(Map.of("action", "listen_status", "channel", "status_changed"));

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "events",
                "channels", List.of("status_changed")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("events", data.get("action"));
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get("status_changed");
        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertEquals("Reconstruction_mock", events.get(0).get("name"));
        assertTrue(events.get(0).containsKey("_seq"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsCalledWithNoActiveChannels() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of("action", "events"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterBySinceSeq_whenEventsCursorAdvanced() {
        sessionManager.createSession("uuid", "ws");
        terraTool.execute(Map.of("action", "listen_status", "channel", "model_status_changed"));
        terraTool.execute(Map.of("action", "listen_status", "channel", "model_status_changed"));

        Map<String, Object> first = terraTool.execute(Map.of(
                "action", "events",
                "channels", List.of("model_status_changed"),
                "limit", 1
        ));
        Map<String, Object> firstStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) first.get("data")).get("streams"))
                        .get("model_status_changed");
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> second = terraTool.execute(Map.of(
                "action", "events",
                "channels", List.of("model_status_changed"),
                "since_seq", lastSeq
        ));
        Map<String, Object> secondStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) second.get("data")).get("streams"))
                        .get("model_status_changed");
        List<Map<String, Object>> secondEvents = (List<Map<String, Object>>) secondStream.get("events");

        assertEquals(1, secondEvents.size());
        assertTrue((long) secondEvents.get(0).get("_seq") > lastSeq);
    }

    // ── listen_stop 显式 channel 与"全部"两条路径 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopOnlyRequestedChannel_whenChannelsGiven() {
        sessionManager.createSession("uuid", "ws");
        terraTool.execute(Map.of("action", "listen_status", "channel", "status_changed"));
        terraTool.execute(Map.of("action", "listen_status", "channel", "unzip_progress"));

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "listen_stop",
                "channels", List.of("status_changed")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_stop", data.get("action"));
        assertEquals(List.of("status_changed"), data.get("stopped"));
        assertTrue(((List<?>) data.get("failed")).isEmpty());

        Map<String, Object> remaining = terraTool.execute(Map.of("action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) remaining.get("data")).get("streams");
        assertEquals(List.of("unzip_progress"), new ArrayList<>(streams.keySet()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAllChannels_whenChannelsOmitted() {
        sessionManager.createSession("uuid", "ws");
        terraTool.execute(Map.of("action", "listen_status", "channel", "status_changed"));
        terraTool.execute(Map.of("action", "listen_status", "channel", "model_status_changed"));

        Map<String, Object> result = terraTool.execute(Map.of("action", "listen_stop"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of("model_status_changed", "status_changed"), data.get("stopped"));

        Map<String, Object> after = terraTool.execute(Map.of("action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReportNotActive_whenStopUnknownChannel() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = terraTool.execute(Map.of(
                "action", "listen_stop",
                "channels", List.of("unzip_progress")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(((List<?>) data.get("stopped")).isEmpty());
        assertEquals(List.of("unzip_progress"), data.get("not_active"));
    }
}
