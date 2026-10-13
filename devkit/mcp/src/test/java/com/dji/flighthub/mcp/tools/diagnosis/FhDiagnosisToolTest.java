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

package com.dji.flighthub.mcp.tools.diagnosis;

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
 * FhDiagnosisTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、Mock 路径。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDiagnosisToolTest {

    private SessionManager sessionManager;
    private FhDiagnosisTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() { return true; }
        };
        tool = new FhDiagnosisTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_diagnosis", def.get("name"));
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
        Map<String, Object> result = tool.execute(Map.of("action", "create"));

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

    // ==================== create ====================

    @Test
    void executeCreate_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "create"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreate_shouldReturnMockResult() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "create",
                "device_sn", "DOCK-001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("DOCK-001", data.get("device_sn"));
        assertEquals("pending", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== get_report ====================

    @Test
    void executeGetReport_shouldReturnError_whenReportUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "get_report"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("report_uuid"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeGetReport_shouldReturnMockReport() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "get_report",
                "report_uuid", "diag_001"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("diag_001", data.get("report_uuid"));
        assertEquals("completed", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== list_reports ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListReports_shouldReturnMockList() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list_reports"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("reports"));
        List<Map<String, Object>> reports = (List<Map<String, Object>>) data.get("reports");
        assertEquals(2, reports.size());
        assertEquals(2, data.get("total"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== operate ====================

    @Test
    void executeOperate_shouldReturnError_whenReportUuidMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "operate");
        args.put("cmd_method", "cancel");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("report_uuid"));
    }

    @Test
    void executeOperate_shouldReturnError_whenCmdMethodMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "operate");
        args.put("report_uuid", "diag_001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("cmd_method"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeOperate_shouldSucceed_whenAllParamsProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "operate");
        args.put("report_uuid", "diag_001");
        args.put("cmd_method", "cancel");

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("diag_001", data.get("report_uuid"));
        assertEquals("cancel", data.get("cmd_method"));
        assertEquals("success", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }
}
