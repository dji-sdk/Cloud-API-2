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
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhSpeakerTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、Mock 路径、confirm 门控。
 *
 * @author DJI FlightHub SDK Team
 */
class FhSpeakerToolTest {

    private SessionManager sessionManager;
    private FhSpeakerTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {  public boolean isMockMode() { return true; } };
        tool = new FhSpeakerTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_speaker", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
        assertTrue(required.contains("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("action", "stop", "drone_sn", "DRONE-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "stop"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "explode", "drone_sn", "DRONE-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== audio_play ====================

    @Test
    void executeAudioPlay_shouldReturnError_whenAudioNameMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "audio_play");
        args.put("drone_sn", "DRONE-001");
        args.put("audio_url", "https://example.com/audio.pcm");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("audio_name"));
    }

    @Test
    void executeAudioPlay_shouldReturnError_whenAudioUrlMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "audio_play");
        args.put("drone_sn", "DRONE-001");
        args.put("audio_name", "warning.pcm");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("audio_url"));
    }

    @Test
    void executeAudioPlay_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "audio_play");
        args.put("drone_sn", "DRONE-001");
        args.put("audio_name", "warning.pcm");
        args.put("audio_url", "https://example.com/audio.pcm");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeAudioPlay_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "audio_play");
        args.put("drone_sn", "DRONE-001");
        args.put("audio_name", "warning.pcm");
        args.put("audio_url", "https://example.com/audio.pcm");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("playing", data.get("status"));
        assertEquals("warning.pcm", data.get("audio_name"));
    }

    // ==================== tts_play ====================

    @Test
    void executeTtsPlay_shouldReturnError_whenTextMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "tts_play");
        args.put("drone_sn", "DRONE-001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("text"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeTtsPlay_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "tts_play");
        args.put("drone_sn", "DRONE-001");
        args.put("text", "请注意安全，无人机即将起飞");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("playing", data.get("status"));
        assertEquals("请注意安全，无人机即将起飞", data.get("text"));
    }

    // ==================== stop ====================

    @Test
    void executeStop_shouldRequireConfirm() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "stop", "drone_sn", "DRONE-001"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeStop_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "stop");
        args.put("drone_sn", "DRONE-001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("stopped", data.get("status"));
    }

    // ==================== replay ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeReplay_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "replay");
        args.put("drone_sn", "DRONE-001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("replaying", data.get("status"));
    }

    // ==================== realtime_start / realtime_stop ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeRealtimeStart_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "realtime_start");
        args.put("drone_sn", "DRONE-001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("realtime_active", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeRealtimeStop_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "realtime_stop");
        args.put("drone_sn", "DRONE-001");
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("realtime_stopped", data.get("status"));
    }

    // ==================== set_volume ====================

    @Test
    void executeSetVolume_shouldReturnError_whenVolumeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_volume");
        args.put("drone_sn", "DRONE-001");
        args.put("volume", 150);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("volume"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetVolume_shouldSucceed_whenValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_volume");
        args.put("drone_sn", "DRONE-001");
        args.put("volume", 80);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(80, data.get("volume"));
    }

    // ==================== set_mode ====================

    @Test
    void executeSetMode_shouldReturnError_whenPlayModeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_mode");
        args.put("drone_sn", "DRONE-001");
        args.put("play_mode", 5);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("play_mode"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeSetMode_shouldSucceed_whenValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "set_mode");
        args.put("drone_sn", "DRONE-001");
        args.put("play_mode", 1);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(1, data.get("play_mode"));
        assertEquals("loop", data.get("play_mode_name"));
    }

    // ==================== tts_config ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeTtsConfig_shouldSucceed_whenConfirmed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "tts_config");
        args.put("drone_sn", "DRONE-001");
        args.put("volume", 70);
        args.put("voice_type", 1);
        args.put("language_type", 0);
        args.put("speed", 3);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(70, data.get("volume"));
        assertEquals("female", data.get("voice_type_name"));
        assertEquals("Chinese", data.get("language_name"));
        assertEquals(3, data.get("speed"));
    }

    @Test
    void executeTtsConfig_shouldReturnError_whenVolumeOutOfBounds() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "tts_config");
        args.put("drone_sn", "DRONE-001");
        args.put("volume", 150);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("volume"));
    }

    @Test
    void executeTtsConfig_shouldReturnError_whenVoiceTypeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "tts_config");
        args.put("drone_sn", "DRONE-001");
        args.put("volume", 50);
        args.put("voice_type", 3);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("voice_type"));
    }

    @Test
    void executeTtsConfig_shouldReturnError_whenLanguageTypeInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "tts_config");
        args.put("drone_sn", "DRONE-001");
        args.put("volume", 50);
        args.put("voice_type", 0);
        args.put("language_type", 5);
        args.put("confirm", true);

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("language_type"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }
}
