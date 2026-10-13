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

package com.dji.flighthub.mcp.tools.record;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhFlightRecordTool Mock 模式单元测试。
 * 验证参数校验、mock 数据结构。
 */
class FhFlightRecordToolTest {

    private FhFlightRecordTool tool;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService();
        tool = new FhFlightRecordTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldHaveCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_flight_record", def.get("name"));
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
        Map<String, Object> result = tool.execute(Map.of("action", "export"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "invalid"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenExportMissingUserId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "export",
                "flight_task_ids", List.of("task-001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("user_id"));
    }

    @Test
    void execute_shouldReturnError_whenExportMissingTaskIds() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "export",
                "user_id", "user-001"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("flight_task_ids"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockExport_whenValidParams() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "export",
                "user_id", "user-001",
                "flight_task_ids", List.of("task-001", "task-002")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("export_id"));
        assertEquals("user-001", data.get("user_id"));
        assertEquals(2, data.get("task_count"));
        assertEquals("processing", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockProgress_withExportId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "get_progress",
                "export_id", "exp_12345"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("exp_12345", data.get("export_id"));
        assertEquals("completed", data.get("status"));
        assertEquals(100, data.get("progress"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockProgressList_withoutExportId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "get_progress"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("records"));
        List<?> records = (List<?>) data.get("records");
        assertFalse(records.isEmpty());
    }
}
