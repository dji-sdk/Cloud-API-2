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

package com.dji.flighthub.mcp.tools.workspace;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhWorkspaceTool Mock 模式单元测试。
 * 验证参数校验、确认机制、mock 数据结构。
 */
class FhWorkspaceToolTest {

    private FhWorkspaceTool tool;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService();
        tool = new FhWorkspaceTool(sessionManager, sdkService);
    }

    // ==================== 工具定义测试 ====================

    @Test
    void getToolDefinition_shouldHaveCorrectName() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_workspace", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }

    // ==================== Session 校验测试 ====================

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ==================== Action 校验测试 ====================

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "invalid_action"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    // ==================== list 测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockWaylines_whenListAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("waylines"));
        List<?> waylines = (List<?>) data.get("waylines");
        assertEquals(2, waylines.size());
        assertEquals(2, data.get("total"));
    }

    // ==================== detail 测试 ====================

    @Test
    void execute_shouldReturnError_whenDetailMissingUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "detail"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockDetail_whenValidUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "detail",
                "wayline_uuid", "wl_test_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("wl_test_001", data.get("uuid"));
        assertNotNull(data.get("download_url"));
        assertNotNull(data.get("md5"));
        assertNotNull(data.get("kmz_key"));
    }

    // ==================== submit 测试 ====================

    @Test
    void execute_shouldReturnError_whenSubmitMissingName() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "file_path", "/tmp/route.kmz"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_name"));
    }

    @Test
    void execute_shouldReturnError_whenSubmitMissingFilePath() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Test Route"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_path"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnRequestId_whenSubmitValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "New Route",
                "file_path", "/tmp/route.kmz"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("request_id"));
        assertEquals("uploaded", data.get("status"));
        assertNotNull(data.get("wayline_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_submitSuccess_shouldReturnNextSteps() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Survey Route",
                "file_path", "/tmp/survey.kmz"
        ));

        assertEquals(true, result.get("success"));
        assertNotNull(result.get("next_steps"));
        List<Map<String, Object>> nextSteps = (List<Map<String, Object>>) result.get("next_steps");
        assertFalse(nextSteps.isEmpty());
        assertEquals("fh_flight_task", nextSteps.get(0).get("tool"));
        Map<String, Object> paramsHint = (Map<String, Object>) nextSteps.get(0).get("params_hint");
        assertEquals("create", paramsHint.get("action"));
        assertNotNull(paramsHint.get("wayline_uuid"));
    }

    // ==================== update 测试 ====================

    @Test
    void execute_shouldReturnError_whenUpdateMissingUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "update",
                "wayline_name", "Updated Name"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenUpdateValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "update",
                "wayline_uuid", "wl_test_001",
                "wayline_name", "Updated Route"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("wl_test_001", data.get("uuid"));
        assertEquals("Updated Route", data.get("name"));
        assertEquals("updated", data.get("status"));
    }

    // ==================== duplicate 测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnNewUuid_whenDuplicateValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "duplicate",
                "wayline_uuid", "wl_test_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("wl_test_001", data.get("source_uuid"));
        assertNotNull(data.get("new_uuid"));
        assertEquals("duplicated", data.get("status"));
    }

    // ==================== batch_delete 测试 ====================

    @Test
    void execute_shouldReturnError_whenBatchDeleteMissingUuids() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "batch_delete"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("wayline_uuids"));
    }

    @Test
    void execute_shouldRequireConfirm_whenBatchDeleteWithoutConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_delete",
                "wayline_uuids", List.of("wl_001", "wl_002")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenBatchDeleteWithConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_delete",
                "wayline_uuids", List.of("wl_001", "wl_002", "wl_003"),
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(3, data.get("deleted_count"));
        List<String> deletedUuids = (List<String>) data.get("deleted_uuids");
        assertEquals(3, deletedUuids.size());
        assertTrue(deletedUuids.contains("wl_001"));
        assertTrue(deletedUuids.contains("wl_002"));
        assertTrue(deletedUuids.contains("wl_003"));
    }

    // ==================== batch_download 测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnDownloadUrls_whenBatchDownloadValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_download",
                "wayline_uuids", List.of("wl_001", "wl_002")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("count"));
        List<Map<String, Object>> downloads = (List<Map<String, Object>>) data.get("downloads");
        assertEquals(2, downloads.size());
        assertNotNull(downloads.get(0).get("download_url"));
        assertEquals("wl_001", downloads.get(0).get("uuid"));
    }
}
