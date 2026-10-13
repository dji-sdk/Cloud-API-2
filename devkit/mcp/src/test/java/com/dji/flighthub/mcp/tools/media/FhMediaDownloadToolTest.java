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
}
