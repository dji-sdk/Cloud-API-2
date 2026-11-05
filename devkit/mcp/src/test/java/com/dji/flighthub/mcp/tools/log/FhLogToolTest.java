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

package com.dji.flighthub.mcp.tools.log;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.DeviceLogListResultFileItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhLogTool Mock 模式单元测试。
 * 验证参数校验、安全确认、mock 数据结构。
 */
class FhLogToolTest {

    private FhLogTool tool;
    private SessionManager sessionManager;

    private static final String DEVICE_SN = "DRONE-LOG-001";

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService() {  public boolean isMockMode() { return true; } };
        tool = new FhLogTool(sessionManager, sdkService);
    }

    // ==================== getToolDefinition ====================

    @Test
    void getToolDefinition_shouldHaveCorrectNameAndRequired() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_log", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
    }

    // ==================== requiresHumanConfirmation ====================

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }

    // ==================== session 校验 ====================

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "action", "query",
                "device_sn", DEVICE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ==================== action 校验 ====================

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "invalid_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid"));
    }

    // ==================== query ====================

    @Test
    void execute_query_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "query"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_query_shouldReturnMockModules() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "query",
                "device_sn", DEVICE_SN
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DEVICE_SN, data.get("device_sn"));

        List<Map<String, Object>> modules = (List<Map<String, Object>>) data.get("modules");
        assertEquals(4, modules.size());
        assertEquals("Flight Controller", modules.get(0).get("module_name"));
        assertEquals(1, modules.get(0).get("module_id"));
        assertEquals("Battery", modules.get(1).get("module_name"));
        assertEquals("Gimbal", modules.get(2).get("module_name"));
        assertEquals("Remote Controller", modules.get(3).get("module_name"));
    }

    // ==================== create_report ====================

    @Test
    void execute_createReport_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "create_report"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_createReport_shouldReturnReportId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "create_report",
                "device_sn", DEVICE_SN
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("report_id"));
        assertTrue(data.get("report_id").toString().startsWith("RPT-"));
        assertEquals("generating", data.get("status"));
    }

    // ==================== list_reports ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_listReports_shouldReturnMockList() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "list_reports"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> reports = (List<Map<String, Object>>) data.get("reports");
        assertEquals(2, reports.size());
        assertEquals(2, data.get("total"));
    }

    // ==================== operate ====================

    @Test
    void execute_operate_shouldReturnError_whenMissingReportId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "operate",
                "operation", "download"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("report_id"));
    }

    @Test
    void execute_operate_delete_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "operate",
                "report_id", "RPT-123",
                "operation", "delete"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_operate_delete_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "operate",
                "report_id", "RPT-123",
                "operation", "delete",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("RPT-123", data.get("report_id"));
        assertEquals("deleted", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_operate_download_shouldNotRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "operate",
                "report_id", "RPT-456",
                "operation", "download"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("RPT-456", data.get("report_id"));
        assertNotNull(data.get("download_url"));
        assertTrue(data.get("download_url").toString().contains("RPT-456"));
    }

    // ==================== upload ====================

    @Test
    void execute_upload_shouldReturnError_whenMissingFilePath() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "upload"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_path"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_upload_shouldReturnMockSuccess() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "upload",
                "file_path", "/var/log/flighthub/drone-log-20250601.log"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("uploaded", data.get("status"));
        assertNotNull(data.get("object_key"));
        assertTrue(data.get("object_key").toString().startsWith("logs/"));
    }

    @Test
    void execute_upload_shouldReturnError_whenFilePathOutsideAllowedDir() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "upload",
                "file_path", "/tmp/drone-log-20250601.log"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_path must be within"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_upload_shouldReturnMockSuccess_whenCustomUploadDir() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessions = new SessionManager(properties);
        sessions.createSession("uuid", "ws");

        SdkService mockSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhLogTool customTool = new FhLogTool(sessions, mockSdk, "/var/log/test");

        Map<String, Object> result = customTool.execute(Map.of(
                "action", "upload",
                "file_path", "/var/log/test/drone.log"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("uploaded", data.get("status"));
    }

    // ==================== 日志文件列表截断 ====================

    /**
     * 构造指定数量的 SDK 日志文件条目。
     *
     * @param count 条目数量
     * @return SDK 文件列表
     */
    private List<DeviceLogListResultFileItem> buildSdkFiles(int count) {
        List<DeviceLogListResultFileItem> files = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DeviceLogListResultFileItem item = new DeviceLogListResultFileItem();
            item.setDeviceSn("SN-001");
            item.setModule("Flight Controller");
            item.setLogType(1);
            item.setObjectKey("logs/" + i + ".log");
            files.add(item);
        }
        return files;
    }

    @Test
    void buildLogFiles_shouldReturnAll_whenCountBelowLimit() {
        List<Map<String, Object>> files = tool.buildLogFiles(buildSdkFiles(10));

        assertEquals(10, files.size());
        assertEquals("logs/0.log", files.get(0).get("object_key"));
    }

    @Test
    void buildLogFiles_shouldTruncate_whenCountExceedsLimit() {
        List<Map<String, Object>> files = tool.buildLogFiles(buildSdkFiles(500));

        assertEquals(200, files.size(), "超过上限应截断到 MAX_QUERY_FILES");
    }

    @Test
    void buildLogFiles_shouldReturnEmpty_whenSdkFilesNull() {
        assertTrue(tool.buildLogFiles(null).isEmpty());
    }

    @Test
    void buildLogFiles_shouldSkipNullItems() {
        List<DeviceLogListResultFileItem> sdkFiles = new ArrayList<>();
        sdkFiles.add(buildSdkFiles(1).get(0));
        sdkFiles.add(null);

        assertEquals(1, tool.buildLogFiles(sdkFiles).size());
    }

    // ==================== 混合等级 per-action 门控 ====================

    @Test
    void execute_shouldDenyUpload_whenSessionLevelIsL0() {
        FhLogTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "upload",
                "file_path", "/var/log/flighthub/drone-log.log"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldDenyCreateReport_whenSessionLevelIsL0() {
        FhLogTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "create_report",
                "device_sn", "DRONE-LOG-001"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldAllowQuery_whenSessionLevelIsL0() {
        FhLogTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "query",
                "device_sn", "DRONE-LOG-001"
        ));

        assertEquals(true, result.get("success"));
    }

    @Test
    void execute_shouldDenyOperateDelete_whenSessionLevelIsL0() {
        FhLogTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "operate",
                "report_id", "RPT-123",
                "operation", "delete",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldAllowOperateDownload_whenSessionLevelIsL0() {
        FhLogTool l0Tool = createToolWithMaxLevel(0);

        Map<String, Object> result = l0Tool.execute(Map.of(
                "action", "operate",
                "report_id", "RPT-456",
                "operation", "download"
        ));

        assertEquals(true, result.get("success"));
    }

    /**
     * 构造指定 maxSecurityLevel 且已建立 session 的工具实例。
     *
     * @param maxLevel session 允许的最高安全等级
     * @return 使用该 session 的工具实例
     */
    private static FhLogTool createToolWithMaxLevel(int maxLevel) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(maxLevel);
        SessionManager sessions = new SessionManager(properties);
        sessions.createSession("uuid", "ws");

        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        return new FhLogTool(sessions, sdkService);
    }
}
