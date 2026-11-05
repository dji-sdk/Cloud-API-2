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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FhMediaUploadTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、get_upload_sts 只读路径、
 * upload_callback 确认门控与 Mock 路径、参数校验。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMediaUploadToolTest {

    private SessionManager sessionManager;
    private FhMediaUploadTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() { public boolean isMockMode() { return true; } };
        tool = new FhMediaUploadTool(sessionManager, sdkService);
    }

    // ==================== getToolDefinition ====================

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_media_upload", def.get("name"));
        assertNotNull(def.get("description"));
        assertTrue(def.get("description").toString().contains("get_upload_sts"));
        assertTrue(def.get("description").toString().contains("upload_callback"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actionEnum = (List<String>) actionProp.get("enum");
        assertTrue(actionEnum.contains("get_upload_sts"));
        assertTrue(actionEnum.contains("upload_callback"));

        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
        assertTrue(required.contains("sn"));
    }

    // ==================== session / action 校验 ====================

    @Test
    void execute_shouldReturnError_whenNoSession_getUploadSts() {
        Map<String, Object> result = tool.execute(Map.of("action", "get_upload_sts", "sn", "SN-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession_uploadCallback() {
        Map<String, Object> result = tool.execute(Map.of("action", "upload_callback", "sn", "SN-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "invalid", "sn", "SN-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    void execute_shouldReturnError_whenSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "get_upload_sts"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("sn"));
    }

    // ==================== get_upload_sts ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeGetUploadSts_shouldSucceed_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "get_upload_sts");
        args.put("sn", "SN-001");

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("bucket"));
        assertNotNull(data.get("endpoint"));
        assertNotNull(data.get("object_key_prefix"));
        Map<String, Object> credentials = (Map<String, Object>) data.get("credentials");
        assertNotNull(credentials.get("access_key_id"));
        assertNotNull(credentials.get("access_key_secret"));
        assertNotNull(credentials.get("security_token"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeGetUploadSts_shouldSucceed_withModuleFlightRecord() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "get_upload_sts");
        args.put("sn", "SN-001");
        args.put("module", 2);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2, data.get("module"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeGetUploadSts_shouldSucceed_withModuleSnEncryption() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "get_upload_sts");
        args.put("sn", "SN-001");
        args.put("module", 2001);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(2001, data.get("module"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeGetUploadSts_shouldSucceed_withModuleDefault() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "get_upload_sts");
        args.put("sn", "SN-001");
        args.put("module", 0);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(0, data.get("module"));
    }

    // ==================== upload_callback ====================

    @Test
    void executeUploadCallback_shouldReturnError_whenResultMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload_callback");
        args.put("sn", "SN-001");
        args.put("file", Map.of("object_key", "media/x.jpeg", "name", "x.jpeg"));

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("result"));
    }

    @Test
    void executeUploadCallback_shouldReturnError_whenFileMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload_callback");
        args.put("sn", "SN-001");
        args.put("result", 0);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("file"));
    }

    @Test
    void executeUploadCallback_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload_callback");
        args.put("sn", "SN-001");
        args.put("result", 0);
        args.put("file", Map.of("object_key", "media/x.jpeg", "name", "x.jpeg", "type", 1));

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeUploadCallback_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload_callback");
        args.put("sn", "SN-001");
        args.put("result", 0);
        args.put("file", Map.of("object_key", "media/x.jpeg", "name", "x.jpeg", "type", 1));
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("object_key"));
        assertNotNull(data.get("filename"));
        assertNotNull(data.get("flight_id"));
        assertNotNull(data.get("uuid"));
        assertNotNull(data.get("id"));
        assertNotNull(data.get("media_id"));
    }
}
