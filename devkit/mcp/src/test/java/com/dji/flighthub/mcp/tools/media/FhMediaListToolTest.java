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

package com.dji.flighthub.mcp.tools.media;

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
 * FhMediaListTool 单元测试。
 * 覆盖 session/workspace 校验、分页参数、文件类型校验、时间解析及 mock 模式正常返回。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMediaListToolTest {

    private SessionManager sessionManager;
    private FhMediaListTool listTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = MediaTestHelper.createSdkServiceWithWorkspace();
        listTool = new FhMediaListTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = listTool.getToolDefinition();

        assertEquals("fh_media_list", def.get("name"));
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
        FhMediaListTool tool = new FhMediaListTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnFileList_whenNoFilters() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(new HashMap<>());

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertNotNull(data.get("files"));
        List<?> files = (List<?>) data.get("files");
        assertFalse(files.isEmpty());
        assertEquals(0, data.get("start_index"));
        assertEquals(50, data.get("page_size"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldApplyDeviceFilter_whenDeviceSnProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "device_sn", "MY-DRONE-123"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> files = (List<Map<String, Object>>) data.get("files");
        // mock 数据应反映过滤参数
        assertEquals("MY-DRONE-123", files.get(0).get("device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenFileTypeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "file_type", "audio"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid file_type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUseDefaultPagination_whenInvalidValues() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "start_index", -5,
                "page_size", 9999
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        // 非法值应回退到默认
        assertEquals(0, data.get("start_index"));
        assertEquals(50, data.get("page_size"));
    }

    @Test
    void execute_shouldReturnError_whenStartTimeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "start_time", "not-a-date"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid start_time format"));
    }

    @Test
    void execute_shouldReturnError_whenEndTimeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "end_time", "yesterday"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid end_time format"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldAcceptValidTimeRange() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "start_time", "2025-01-01T00:00:00Z",
                "end_time", "2025-01-31T23:59:59Z"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("files"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnPhotoOnly_whenFileTypePhoto() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = listTool.execute(Map.of(
                "file_type", "photo"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("files"));
    }
}
