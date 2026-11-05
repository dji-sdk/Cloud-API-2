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
 * FhMediaDownloadTool 单元测试。
 * 覆盖 session/workspace 校验、file_id 必填校验及 mock 模式正常返回。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMediaDownloadToolTest {

    private SessionManager sessionManager;
    private FhMediaDownloadTool downloadTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = MediaTestHelper.createSdkServiceWithWorkspace();
        downloadTool = new FhMediaDownloadTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = downloadTool.getToolDefinition();

        assertEquals("fh_media_download", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = downloadTool.execute(Map.of(
                "file_id", 10001
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
        FhMediaDownloadTool tool = new FhMediaDownloadTool(sessionManager, noWorkspace);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("file_id", 10001));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(new HashMap<>());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_id"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "file_id", "not_a_number"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_id"));
    }

    @Test
    void execute_shouldReturnError_whenFileIdZero() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "file_id", 0
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnDownloadUrl_whenValidFileId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "file_id", 10001
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(10001L, data.get("file_id"));
        assertNotNull(data.get("download_url"));
        assertTrue(data.get("download_url").toString().contains("storage.dji.com"));
        assertNotNull(data.get("expiration"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldAcceptLongFileId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "file_id", 99999999L
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(99999999L, data.get("file_id"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "not_a_real_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenGetUrlActionMissingFileId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "get_url"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or invalid file_id"));
    }

    @Test
    void execute_shouldReturnError_whenBatchUrlMissingObjectKeys() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "batch_url"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or empty object_keys"));
    }

    @Test
    void execute_shouldReturnError_whenBatchUrlObjectKeysEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "batch_url",
                "object_keys", List.of()
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing or empty object_keys"));
    }

    @Test
    void execute_shouldReturnError_whenBatchUrlExceedsMaxSize() {
        sessionManager.createSession("uuid", "ws");

        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            tooMany.add("object_key_" + i);
        }

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "batch_url",
                "object_keys", tooMany
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Too many object_keys"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnBatchUrls_whenValidObjectKeys() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "batch_url",
                "object_keys", List.of("media/2026/08/a.jpeg", "media/2026/08/b.jpeg")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("batch_url", data.get("action"));
        assertEquals(2, data.get("count"));
        assertEquals(true, data.get("_mock"));

        List<Map<String, Object>> files = (List<Map<String, Object>>) data.get("files");
        assertNotNull(files);
        assertEquals(2, files.size());
        assertEquals("media/2026/08/a.jpeg", files.get(0).get("object_key"));
        assertNotNull(files.get(0).get("download_url"));
        assertNotNull(files.get(0).get("suggested_filename"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldAcceptExactlyMaxBatchSize() {
        sessionManager.createSession("uuid", "ws");

        List<String> maxKeys = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            maxKeys.add("object_key_" + i);
        }

        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "batch_url",
                "object_keys", maxKeys
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(100, data.get("count"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession_forBatchUrl() {
        Map<String, Object> result = downloadTool.execute(Map.of(
                "action", "batch_url",
                "object_keys", List.of("media/2026/08/a.jpeg")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void getToolDefinition_shouldExposeActionAndObjectKeysSchema() {
        Map<String, Object> def = downloadTool.getToolDefinition();

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");

        assertTrue(props.containsKey("action"));
        assertTrue(props.containsKey("object_keys"));
        assertTrue(props.containsKey("file_id"));

        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertFalse(required.contains("file_id"));
    }
}
