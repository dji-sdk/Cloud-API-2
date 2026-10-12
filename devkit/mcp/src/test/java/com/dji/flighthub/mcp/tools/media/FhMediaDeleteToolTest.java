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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhMediaDeleteTool 单元测试。
 * 覆盖 session/workspace 校验、file_ids 参数校验、批量上限及 mock 模式正常返回。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMediaDeleteToolTest {

    private SessionManager sessionManager;
    private FhMediaDeleteTool deleteTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = MediaTestHelper.createSdkServiceWithWorkspace();
        deleteTool = new FhMediaDeleteTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = deleteTool.getToolDefinition();

        assertEquals("fh_media_delete", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = deleteTool.execute(Map.of(
                "file_ids", List.of(10001),
                "confirm", true
        ));

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
        FhMediaDeleteTool tool = new FhMediaDeleteTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "file_ids", List.of(10001),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdsMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("confirm", true);

        Map<String, Object> result = deleteTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_ids"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdsEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deleteTool.execute(Map.of(
                "file_ids", List.of(),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file_ids cannot be empty"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdsExceedMax() {
        sessionManager.createSession("uuid", "ws");

        // 生成 101 个 ID
        List<Integer> tooMany = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            tooMany.add(10000 + i);
        }

        Map<String, Object> result = deleteTool.execute(Map.of(
                "file_ids", tooMany,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Too many file_ids"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdsContainInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deleteTool.execute(Map.of(
                "file_ids", List.of("not_a_number"),
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_ids"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdsContainNull() {
        sessionManager.createSession("uuid", "ws");

        List<Object> idsWithNull = new ArrayList<>();
        idsWithNull.add(10001);
        idsWithNull.add(null);
        idsWithNull.add(10003);

        Map<String, Object> args = new HashMap<>();
        args.put("file_ids", idsWithNull);
        args.put("confirm", true);

        Map<String, Object> result = deleteTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_ids"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldDeleteFiles_whenValidInput() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deleteTool.execute(Map.of(
                "file_ids", List.of(10001, 10002, 10003),
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(3, data.get("deleted_count"));
        List<Long> deletedIds = (List<Long>) data.get("file_ids");
        assertEquals(3, deletedIds.size());
        assertTrue(deletedIds.contains(10001L));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldDeleteSingleFile() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deleteTool.execute(Map.of(
                "file_ids", List.of(99999),
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1, data.get("deleted_count"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnTrue() {
        assertTrue(deleteTool.requiresHumanConfirmation());
    }
}
