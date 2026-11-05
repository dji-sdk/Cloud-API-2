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
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhWorkspaceTool Mock 模式单元测试。
 * 验证参数校验、确认机制、mock 数据结构、订阅三元组（listen_import/events/listen_stop）。
 */
class FhWorkspaceToolTest {

    private static final String CHANNEL_IMPORT = "import_wayline_event";

    /** 测试用允许上传目录：位于系统临时目录下，与默认配置一致 */
    private static final String TEST_UPLOAD_DIR = System.getProperty("java.io.tmpdir");

    private FhWorkspaceTool tool;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService();
        ListenChannelSupport listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        tool = new FhWorkspaceTool(sessionManager, sdkService, listenSupport);
    }

    /**
     * 构造使用指定允许上传目录的工具实例，避免依赖宿主机环境变量。
     *
     * @param uploadDir 允许上传目录
     * @return 工具实例
     */
    private FhWorkspaceTool newLocalTool(String uploadDir) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager localSessions = new SessionManager(properties);
        localSessions.createSession("uuid", "ws");
        SdkService sdkService = new SdkService();
        ListenChannelSupport listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        return new FhWorkspaceTool(localSessions, sdkService, listenSupport, uploadDir);
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
        // mock 与真实路径结构保持一致：kmz_data / ext_data 必须是结构化对象而非 toString()
        Map<String, Object> kmzData = (Map<String, Object>) data.get("kmz_data");
        assertNotNull(kmzData);
        assertEquals(42, kmzData.get("wayline_point_count"));
        assertEquals(List.of("Zenmuse L2"), kmzData.get("payload_types"));
        Map<String, Object> extData = (Map<String, Object>) data.get("ext_data");
        assertNotNull(extData);
        assertEquals(1048576L, extData.get("content_length"));
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
        // 顶层 status 与真实路径一致：只反映提交结果
        assertEquals("submitted", data.get("status"));
        assertNotNull(data.get("wayline_uuid"));
        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
        // mock 下导入瞬时完成，单文件状态等价于收到导入成功事件
        assertEquals("imported", results.get(0).get("status"));
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

    // ==================== submit 多文件 / AsyncRequestId 测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSubmitEachFile_whenFilePathsGiven() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Batch Route",
                "file_paths", List.of("waylines/a.kmz", "waylines/b.kmz", "waylines/c.kmz")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(3, data.get("submitted_count"));
        assertEquals(0, data.get("failed_count"));
        assertEquals("submitted", data.get("status"));

        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
        assertEquals(3, results.size());
        assertEquals("waylines/a.kmz", results.get(0).get("file_path"));
        assertEquals("waylines/c.kmz", results.get(2).get("file_path"));
        // AsyncRequestId 由 MCP 自动生成并回显，供服务端配对导入事件
        for (Map<String, Object> item : results) {
            assertNotNull(item.get("async_request_id"));
            assertNotNull(item.get("wayline_uuid"));
        }
        assertNotEquals(results.get(0).get("async_request_id"), results.get(1).get("async_request_id"));
        assertNotNull(data.get("wayline_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldDeduplicate_whenFilePathsOverlapFilePath() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Dedup Route",
                "file_paths", List.of("waylines/a.kmz", "waylines/b.kmz"),
                "file_path", "waylines/a.kmz"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("submitted_count"));
        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
        assertEquals(2, results.size());
        assertEquals("waylines/a.kmz", results.get(0).get("file_path"));
        assertEquals("waylines/b.kmz", results.get(1).get("file_path"));
    }

    @Test
    void execute_shouldReturnError_whenSubmitExceedsFileLimit() {
        sessionManager.createSession("uuid", "ws");

        List<String> objectKeys = new ArrayList<>();
        // 上限为 5，构造 6 个触发拒绝，确保边界不会随实现漂移而失效
        for (int i = 0; i < 6; i++) {
            objectKeys.add("waylines/" + i + ".kmz");
        }

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Too Many",
                "file_paths", objectKeys
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Too many files"));
    }

    @Test
    void execute_shouldReturnError_whenSubmitFilePathsEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Empty Route",
                "file_paths", List.of()
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Empty file_paths"));
    }

    // ==================== submit 本地路径上传测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUploadLocalPath_whenFilePathIsAbsolute() {
        sessionManager.createSession("uuid", "ws");
        FhWorkspaceTool localTool = newLocalTool(TEST_UPLOAD_DIR);

        Map<String, Object> result = localTool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Local Route",
                "file_path", TEST_UPLOAD_DIR + "/wayline-ab12cd34.kmz"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
        assertEquals(1, results.size());
        Map<String, Object> item = results.get(0);
        assertEquals(TEST_UPLOAD_DIR + "/wayline-ab12cd34.kmz", item.get("file_path"));
        // 本地路径通道会先换取对象 key，mock 下给出结构等价的占位值
        assertNotNull(item.get("kmz_object_key"));
        assertEquals("imported", item.get("status"));
        assertNotNull(item.get("wayline_uuid"));
    }

    @Test
    void execute_shouldReject_whenLocalPathEscapesUploadDir() {
        sessionManager.createSession("uuid", "ws");
        FhWorkspaceTool localTool = newLocalTool(TEST_UPLOAD_DIR);

        Map<String, Object> result = localTool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Escape Route",
                "file_path", TEST_UPLOAD_DIR + "/../etc/passwd"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString()
                .contains("must be within " + TEST_UPLOAD_DIR));
    }

    @Test
    void execute_shouldReject_whenLocalPathOutsideUploadDir() {
        sessionManager.createSession("uuid", "ws");
        FhWorkspaceTool localTool = newLocalTool(TEST_UPLOAD_DIR);

        Map<String, Object> result = localTool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Outside Route",
                "file_path", "/app/waylines/route.kmz"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString()
                .contains("must be within " + TEST_UPLOAD_DIR));
    }

    @Test
    void execute_shouldRejectBatch_whenAnyLocalPathEscapes() {
        sessionManager.createSession("uuid", "ws");
        FhWorkspaceTool localTool = newLocalTool(TEST_UPLOAD_DIR);

        Map<String, Object> result = localTool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Mixed Route",
                "file_paths", List.of(
                        TEST_UPLOAD_DIR + "/wayline-a.kmz",
                        TEST_UPLOAD_DIR + "/../../etc/passwd")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("must be within"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldTreatNonAbsoluteRefAsObjectKey() {
        sessionManager.createSession("uuid", "ws");
        FhWorkspaceTool localTool = newLocalTool(TEST_UPLOAD_DIR);

        Map<String, Object> result = localTool.execute(Map.of(
                "action", "submit",
                "wayline_name", "Key Route",
                "file_path", "waylines/existing.kmz"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
        Map<String, Object> item = results.get(0);
        // 对象 key 通道不经过 importKmz，不会产生 kmz_object_key
        assertNull(item.get("kmz_object_key"));
        assertNotNull(item.get("async_request_id"));
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

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldEchoTargetParams_whenDuplicateWithNameAndPrefix() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "duplicate",
                "wayline_uuid", "wl_test_001",
                "target_wayline_name", "Route Copy",
                "target_object_key_prefix", "waylines/copies/"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("wl_test_001", data.get("source_uuid"));
        assertEquals("Route Copy", data.get("new_name"));
        assertEquals("waylines/copies/", data.get("target_object_key_prefix"));
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

    // ==================== concat 测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldIncludeConcatAction() {
        Map<String, Object> def = tool.getToolDefinition();

        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertTrue(enumValues.contains("concat"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession_forConcatAction() {
        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "target_wayline_name", "Merged Route",
                "kmz_object_key", "waylines/base.kmz",
                "sub_wayline_uuids", List.of("wl_a", "wl_b")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenConcatMissingTargetWaylineName() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "kmz_object_key", "waylines/base.kmz",
                "sub_wayline_uuids", List.of("wl_a", "wl_b")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("target_wayline_name"));
    }

    @Test
    void execute_shouldReturnError_whenConcatMissingKmzObjectKey() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "target_wayline_name", "Merged Route",
                "sub_wayline_uuids", List.of("wl_a", "wl_b")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("kmz_object_key"));
    }

    @Test
    void execute_shouldReturnError_whenConcatMissingSubWaylineUuids() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "target_wayline_name", "Merged Route",
                "kmz_object_key", "waylines/base.kmz"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("sub_wayline_uuids"));
    }

    @Test
    void execute_shouldReturnError_whenConcatSubWaylineUuidsEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "target_wayline_name", "Merged Route",
                "kmz_object_key", "waylines/base.kmz",
                "sub_wayline_uuids", List.of()
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("sub_wayline_uuids"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockWaylineUuid_whenConcatValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "target_wayline_name", "Merged Route",
                "kmz_object_key", "waylines/base.kmz",
                "sub_wayline_uuids", List.of("wl_a", "wl_b")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        assertNotNull(data.get("wayline_uuid"));
        assertEquals("Merged Route", data.get("target_wayline_name"));
        assertEquals("waylines/base.kmz", data.get("kmz_object_key"));
        List<String> subUuids = (List<String>) data.get("sub_wayline_uuids");
        assertEquals(2, subUuids.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldEchoEditorWarnings_whenConcatWithEditorWarnings() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "concat",
                "target_wayline_name", "Merged Route",
                "kmz_object_key", "waylines/base.kmz",
                "sub_wayline_uuids", List.of("wl_a", "wl_b"),
                "editor_warnings", Map.of("wl_a", "altitude mismatch")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> editorWarnings = (Map<String, Object>) data.get("editor_warnings");
        assertNotNull(editorWarnings);
        assertEquals("altitude mismatch", editorWarnings.get("wl_a"));
    }

    // ==================== 订阅三元组测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldIncludeSubscriptionActions() {
        Map<String, Object> def = tool.getToolDefinition();

        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertEquals(11, enumValues.size());
        assertTrue(enumValues.contains("listen_import"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));
        assertTrue(props.containsKey("channels"));
        assertTrue(props.containsKey("since_seq"));
        assertTrue(props.containsKey("limit"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession_forListenImport() {
        Map<String, Object> result = tool.execute(Map.of("action", "listen_import"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnListenId_whenListenImportInMock() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "listen_import"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_import", data.get("action"));
        assertEquals(CHANNEL_IMPORT, data.get("channel"));
        assertNotNull(data.get("listen_id"));
        assertTrue(((Number) data.get("listen_id")).intValue() > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsWithoutSubscription() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "events",
                "channels", List.of(CHANNEL_IMPORT)
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertEquals(1, streams.size());
        Map<String, Object> stream = (Map<String, Object>) streams.get(CHANNEL_IMPORT);
        assertEquals(false, stream.get("active"));
        assertEquals(0, stream.get("count"));
        assertTrue(((List<?>) stream.get("events")).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnActiveStream_whenEventsAfterListenImport() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_import"));

        Map<String, Object> result = tool.execute(Map.of("action", "events"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get(CHANNEL_IMPORT);
        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertEquals(1, events.size());
        Map<String, Object> event = events.get(0);
        assertNotNull(event.get("kmz_object_key"));
        assertNotNull(event.get("wayline_uuid"));
        assertNotNull(event.get("_seq"));
        assertEquals(event.get("_seq"), stream.get("last_seq"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldRespectLimitAndSinceSeq_whenEventsCursor() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_import"));
        // 再订阅两次以累积多条事件
        tool.execute(Map.of("action", "listen_import"));
        tool.execute(Map.of("action", "listen_import"));

        Map<String, Object> first = tool.execute(Map.of("action", "events", "limit", 1));
        Map<String, Object> firstData = (Map<String, Object>) first.get("data");
        Map<String, Object> firstStream = (Map<String, Object>) ((Map<?, ?>) firstData.get("streams")).get(CHANNEL_IMPORT);
        assertEquals(1, firstStream.get("count"));
        long firstSeq = ((Number) firstStream.get("last_seq")).longValue();

        Map<String, Object> second = tool.execute(Map.of(
                "action", "events",
                "since_seq", firstSeq,
                "limit", 10
        ));
        Map<String, Object> secondData = (Map<String, Object>) second.get("data");
        Map<String, Object> secondStream = (Map<String, Object>) ((Map<?, ?>) secondData.get("streams")).get(CHANNEL_IMPORT);
        // 首个 limit=1 的读取 + 后续续读应覆盖全部 3 条事件
        assertEquals(2, secondStream.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) secondStream.get("events");
        assertTrue(((Number) events.get(0).get("_seq")).longValue() > firstSeq);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopSpecifiedChannel_whenListenStopWithChannels() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_import"));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "listen_stop",
                "channels", List.of(CHANNEL_IMPORT)
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of(CHANNEL_IMPORT), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
        assertTrue(((List<?>) data.get("failed")).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAll_whenListenStopWithoutChannels() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_import"));

        Map<String, Object> result = tool.execute(Map.of("action", "listen_stop"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of(CHANNEL_IMPORT), data.get("stopped"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotLeakSubscription_whenListenImportRepeated() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_import"));
        tool.execute(Map.of("action", "listen_import"));

        Map<String, Object> stopResult = tool.execute(Map.of("action", "listen_stop"));
        Map<String, Object> stopData = (Map<String, Object>) stopResult.get("data");
        assertEquals(1, ((List<?>) stopData.get("stopped")).size());
        assertTrue(((List<?>) stopData.get("not_active")).isEmpty());

        Map<String, Object> events = tool.execute(Map.of("action", "events", "channels", List.of(CHANNEL_IMPORT)));
        Map<String, Object> eventsData = (Map<String, Object>) events.get("data");
        Map<String, Object> stream = (Map<String, Object>) ((Map<?, ?>) eventsData.get("streams")).get(CHANNEL_IMPORT);
        assertEquals(false, stream.get("active"));
    }

    // ==================== 混合等级 per-action 门控 ====================

    @Test
    void execute_shouldDenyWriteAction_whenSessionLevelIsL0() {
        FhWorkspaceTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "batch_delete",
                "wayline_uuids", List.of("wl_001"),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldDenySubmit_whenSessionLevelIsL0() {
        FhWorkspaceTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "submit",
                "file_path", "/tmp/wayline.kmz"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldAllowReadOnlyAction_whenSessionLevelIsL0() {
        FhWorkspaceTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of("action", "list"));

        assertEquals(true, result.get("success"));
    }

    /**
     * 构造指定 maxSecurityLevel 且已建立 session 的工具实例。
     *
     * @param maxLevel session 允许的最高安全等级
     * @return 使用该 session 的工具实例
     */
    private static FhWorkspaceTool createToolWithMaxLevel(int maxLevel) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(maxLevel);
        SessionManager sessions = new SessionManager(properties);
        sessions.createSession("uuid", "ws");
        ListenChannelSupport listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        return new FhWorkspaceTool(sessions, new SdkService(), listenSupport);
    }
}
