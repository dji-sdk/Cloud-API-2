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

package com.dji.flighthub.mcp.tools.safety;

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
 * FhFlightAreaListTool 单元测试。
 * 覆盖 session/workspace 校验、参数解析、type 过滤及 mock 模式返回结构。
 *
 * @author DJI FlightHub SDK Team
 */
class FhFlightAreaListToolTest {

    private SessionManager sessionManager;
    private FhFlightAreaListTool listTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = SafetyTestHelper.createSdkServiceWithWorkspace();
        listTool = new FhFlightAreaListTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = listTool.getToolDefinition();

        assertEquals("fh_flight_area_list", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = listTool.execute(Map.of());

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
        FhFlightAreaListTool tool = new FhFlightAreaListTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnAllAreas_whenNoFilter() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of());

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        List<?> areas = (List<?>) data.get("flight_areas");
        assertEquals(3, areas.size());
        assertEquals(3, data.get("total_count"));
        assertEquals(1, data.get("page_index"));
        assertEquals(50, data.get("page_size"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterByType_whenTypeProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of("type", "nfz"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> areas = (List<Map<String, Object>>) data.get("flight_areas");
        assertEquals(1, areas.size());
        assertEquals("nfz", areas.get(0).get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnAll_whenTypeIsAll() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of("type", "all"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<?> areas = (List<?>) data.get("flight_areas");
        assertEquals(3, areas.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldRespectPageSize() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "page_index", 2,
                "page_size", 10
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("page_index"));
        assertEquals(10, data.get("page_size"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldClampPageSize_whenExceedsMax() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of("page_size", 999));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(200, data.get("page_size"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldIgnoreInvalidType() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of("type", "invalid_type"));

        assertEquals(true, result.get("success"));
        // 无效 type 被忽略，返回全部
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<?> areas = (List<?>) data.get("flight_areas");
        assertEquals(3, areas.size());
    }
}
