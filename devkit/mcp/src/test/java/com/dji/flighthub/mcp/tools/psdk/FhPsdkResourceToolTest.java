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

package com.dji.flighthub.mcp.tools.psdk;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FhPsdkResourceTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、confirm 门控、Mock 路径。
 *
 * @author DJI FlightHub SDK Team
 */
class FhPsdkResourceToolTest {

    private SessionManager sessionManager;
    private FhPsdkResourceTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {
            public boolean isMockMode() {
                return true;
            }
        };
        tool = new FhPsdkResourceTool(sessionManager, sdkService);
    }

    // ==================== getToolDefinition ====================

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_psdk_resource", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actionEnum = (List<String>) actionProp.get("enum");

        assertTrue(actionEnum.contains("list_speaker"));
        assertTrue(actionEnum.contains("create_speaker"));
        assertTrue(actionEnum.contains("edit_speaker"));
        assertTrue(actionEnum.contains("delete_speaker"));
        assertTrue(actionEnum.contains("upload_speaker_audio"));
        assertTrue(actionEnum.contains("list_widget"));
        assertEquals(6, actionEnum.size());
    }

    // ==================== session / action 校验 ====================

    @Test
    void execute_shouldReturnError_whenNoSession_readOnlyAction() {
        Map<String, Object> result = tool.execute(Map.of("action", "list_speaker"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession_reversibleAction() {
        Map<String, Object> result = tool.execute(Map.of("action", "create_speaker"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "invalid"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== list_speaker ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListSpeaker_shouldSucceed_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list_speaker"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> resources = (List<Map<String, Object>>) data.get("resources");
        assertFalse(resources.isEmpty());
        assertNotNull(resources.get(0).get("name"));
    }

    // ==================== create_speaker ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeCreateSpeaker_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_speaker");
        args.put("name", "greeting");
        args.put("file_type", "tts");
        args.put("content", "hello");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("greeting", data.get("name"));
        assertEquals("tts", data.get("file_type"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void executeCreateSpeaker_shouldRequireConfirm_whenNotConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_speaker");
        args.put("name", "greeting");
        args.put("file_type", "tts");
        args.put("content", "hello");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void executeCreateSpeaker_shouldReturnError_whenNameMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_speaker");
        args.put("file_type", "tts");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("name"));
    }

    // ==================== edit_speaker ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeEditSpeaker_shouldSucceed_withStringId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "edit_speaker");
        args.put("id", "1001");
        args.put("name", "updated-name");
        args.put("file_type", "audio");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("1001", data.get("id"));
        assertEquals("updated-name", data.get("name"));
    }

    // ==================== delete_speaker ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeDeleteSpeaker_shouldSucceed_withIntResourceId() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete_speaker");
        args.put("resource_id", 1001);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1001, data.get("resource_id"));
        assertEquals(true, data.get("deleted"));
    }

    @Test
    void executeDeleteSpeaker_shouldReturnError_whenResourceIdMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "delete_speaker");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("resource_id"));
    }

    // ==================== upload_speaker_audio ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeUploadSpeakerAudio_shouldReturnStsCredentials_withoutFileContentField() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "upload_speaker_audio");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("bucket"));
        assertNotNull(data.get("endpoint"));
        assertNotNull(data.get("object_key_prefix"));
        Map<String, Object> credentials = (Map<String, Object>) data.get("credentials");
        assertNotNull(credentials.get("access_key_id"));
        assertNotNull(credentials.get("access_key_secret"));

        assertFalse(data.containsKey("file_path"));
        assertFalse(data.containsKey("content"));
        assertFalse(data.containsKey("audio_data"));
    }

    @Test
    void executeUploadSpeakerAudio_shouldRequireConfirm_whenNotConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "upload_speaker_audio"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    // ==================== list_widget ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListWidget_shouldSucceed_withObjectKeysProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "list_widget");
        args.put("object_keys", List.of("widget/a.png", "widget/b.png"));

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> resources = (List<Map<String, Object>>) data.get("ui_resource_urls");
        assertEquals(2, resources.size());
        assertEquals("widget/a.png", resources.get(0).get("object_key"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeListWidget_shouldSucceed_withObjectKeysOmitted() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list_widget"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> resources = (List<Map<String, Object>>) data.get("ui_resource_urls");
        assertNotNull(resources);
    }

    // ==================== 安全等级硬拦截 ====================

    @Test
    void execute_shouldReturnError_whenSecurityLevelInsufficient() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(0);
        SessionManager limitedManager = new SessionManager(properties);
        limitedManager.createSession("uuid", "ws");

        SdkService sdkService = new SdkService() {
            public boolean isMockMode() {
                return true;
            }
        };
        FhPsdkResourceTool limitedTool = new FhPsdkResourceTool(limitedManager, sdkService);

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_speaker");
        args.put("name", "greeting");
        args.put("file_type", "tts");
        args.put("confirm", true);

        Map<String, Object> result = limitedTool.execute(args);

        assertEquals(false, result.get("success"));
        assertFalse(result.get("error_message").toString().isBlank());
    }
}
