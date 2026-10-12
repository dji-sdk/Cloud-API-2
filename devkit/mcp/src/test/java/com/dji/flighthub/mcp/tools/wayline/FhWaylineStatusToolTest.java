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

package com.dji.flighthub.mcp.tools.wayline;

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
 * FhWaylineStatusTool 单元测试。
 * 覆盖 session/workspace 校验、detail 查询、list 查询及分页参数验证。
 *
 * @author DJI FlightHub SDK Team
 */
class FhWaylineStatusToolTest {

    private static final String TASK_UUID = "task_1234567890";

    private SessionManager sessionManager;
    private FhWaylineStatusTool statusTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = createSdkServiceWithWorkspace();
        statusTool = new FhWaylineStatusTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = statusTool.getToolDefinition();

        assertEquals("fh_wayline_status", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = statusTool.execute(Map.of(
                "task_uuid", TASK_UUID
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        SdkService sdkServiceNoWorkspace = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhWaylineStatusTool tool = new FhWaylineStatusTool(sessionManager, sdkServiceNoWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "task_uuid", TASK_UUID
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnTaskDetail_whenTaskUuidProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = statusTool.execute(Map.of(
                "task_uuid", TASK_UUID
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(TASK_UUID, data.get("task_uuid"));
        assertEquals("EXECUTING", data.get("status"));
        assertEquals(45, data.get("progress"));
        assertNotNull(data.get("drone_sn"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnTaskList_whenNoTaskUuid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        // 不传 task_uuid → 列表模式

        Map<String, Object> result = statusTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertNotNull(data.get("tasks"));
        List<?> tasks = (List<?>) data.get("tasks");
        assertFalse(tasks.isEmpty());
        assertEquals(1, data.get("page"));
        assertEquals(20, data.get("page_size"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldApplyFilters_whenDroneSnAndStatusProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = statusTool.execute(Map.of(
                "drone_sn", "SPECIFIC-DRONE",
                "status", "PAUSED",
                "page", 2,
                "page_size", 10
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(2, data.get("page"));
        assertEquals(10, data.get("page_size"));

        // mock 数据应反映过滤参数
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) data.get("tasks");
        assertFalse(tasks.isEmpty());
        assertEquals("SPECIFIC-DRONE", tasks.get(0).get("drone_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUseDefaultPagination_whenInvalidValues() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = statusTool.execute(Map.of(
                "page", -1,
                "page_size", 999
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        // 应回退到默认值
        assertEquals(1, data.get("page"));
        assertEquals(20, data.get("page_size"));
    }

    // ================ 辅助方法 ================

    private static SdkService createSdkServiceWithWorkspace() {
        return WaylineTestHelper.createSdkServiceWithWorkspace();
    }
}
