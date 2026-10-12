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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerTTSconfig;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.PsdkControlModule;
import com.dji.flighthubsdk.value.MegaphonePlayMode;
import com.dji.flighthubsdk.value.MegaphoneTTSLanguageType;
import com.dji.flighthubsdk.value.MegaphoneTTSVoiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_speaker — PSDK 喊话器控制（音频播放/TTS/实时喊话/音量/模式）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>audio_play — 播放音频文件（L1，需 confirm）</li>
 *   <li>tts_play — TTS 文本播报（L1，需 confirm）</li>
 *   <li>stop — 停止播放（L1，需 confirm）</li>
 *   <li>replay — 重播上次内容（L1，需 confirm）</li>
 *   <li>realtime_start — 开始实时喊话（L1，需 confirm）</li>
 *   <li>realtime_stop — 停止实时喊话（L1，需 confirm）</li>
 *   <li>set_volume — 设置播放音量（L1，需 confirm）</li>
 *   <li>set_mode — 设置播放模式（L1，需 confirm）</li>
 *   <li>tts_config — 配置 TTS 参数（L1，需 confirm）</li>
 * </ul>
 *
 * <p>所有 action 均为设备控制操作（L1），统一需要 confirm。
 * <p>支持 mock 模式（FH_MOCK=true）和真实 SDK 调用双路径。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhSpeakerTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhSpeakerTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_speaker";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            "audio_play", "tts_play", "stop", "replay",
            "realtime_start", "realtime_stop",
            "set_volume", "set_mode", "tts_config");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";
    private static final String PARAM_AUDIO_NAME = "audio_name";
    private static final String PARAM_AUDIO_URL = "audio_url";
    private static final String PARAM_TEXT = "text";
    private static final String PARAM_VOLUME = "volume";
    private static final String PARAM_PLAY_MODE = "play_mode";
    private static final String PARAM_VOICE_TYPE = "voice_type";
    private static final String PARAM_LANGUAGE_TYPE = "language_type";
    private static final String PARAM_SPEED = "speed";

    /** 音量范围 */
    private static final int VOLUME_MIN = 0;
    private static final int VOLUME_MAX = 100;

    /** 默认 payload 索引 */
    private static final int DEFAULT_PAYLOAD_INDEX = 0;

    /** 文本截断长度 */
    private static final int TEXT_TRUNCATE_LENGTH = 50;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhSpeakerTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Control PSDK speaker (megaphone): play audio files, TTS text-to-speech, "
                + "real-time announcement, volume/mode settings. "
                + "Actions: audio_play, tts_play, stop, replay, realtime_start, realtime_stop, "
                + "set_volume, set_mode, tts_config. All actions require confirm=true.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Speaker operation to perform"
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Drone serial number (required, identifies which drone's speaker to control)"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "integer",
                "description", "PSDK device index (default: 0)"
        ));
        props.put(PARAM_AUDIO_NAME, Map.of(
                "type", "string",
                "description", "Audio file name (for audio_play)"
        ));
        props.put(PARAM_AUDIO_URL, Map.of(
                "type", "string",
                "description", "Audio file URL (for audio_play)"
        ));
        props.put(PARAM_TEXT, Map.of(
                "type", "string",
                "description", "Text content for TTS (for tts_play)"
        ));
        props.put(PARAM_VOLUME, Map.of(
                "type", "integer",
                "description", "Volume level 0-100 (for set_volume / tts_config)"
        ));
        props.put(PARAM_PLAY_MODE, Map.of(
                "type", "integer",
                "description", "Play mode: 0=single, 1=loop (for set_mode)"
        ));
        props.put(PARAM_VOICE_TYPE, Map.of(
                "type", "integer",
                "description", "Voice type: 0=male, 1=female (for tts_config)"
        ));
        props.put(PARAM_LANGUAGE_TYPE, Map.of(
                "type", "integer",
                "description", "Language: 0=Chinese, 1=English (for tts_config)"
        ));
        props.put(PARAM_SPEED, Map.of(
                "type", "integer",
                "description", "Speech speed (for tts_config)"
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Set true to confirm the operation (required for all actions)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_DRONE_SN));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide the drone serial number to identify the target speaker.");
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action",
                    "Valid actions: " + VALID_ACTIONS);
        }

        // 所有 speaker 操作统一 L1 硬拦截
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        // dry_run 模式：参数校验通过，返回预览
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute speaker '" + action + "' on device " + droneSn,
                    buildDryRunPreview(TOOL_NAME, action, droneSn,
                            Map.of("action", action),
                            List.of("session_active", "security_level_allowed", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        switch (action) {
            case "audio_play":
                return executeAudioPlay(args, droneSn);
            case "tts_play":
                return executeTtsPlay(args, droneSn);
            case "stop":
                return executeStop(args, droneSn);
            case "replay":
                return executeReplay(args, droneSn);
            case "realtime_start":
                return executeRealtimeStart(args, droneSn);
            case "realtime_stop":
                return executeRealtimeStop(args, droneSn);
            case "set_volume":
                return executeSetVolume(args, droneSn);
            case "set_mode":
                return executeSetMode(args, droneSn);
            case "tts_config":
                return executeTtsConfig(args, droneSn);
            default:
                return errorResult("Unhandled action: " + action, null);
        }
    }

    /**
     * 所有 action 均为设备控制操作，统一 L1。
     * 但不在工具级拦截，由各 action 内部门控（保持一致的 pattern）。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== audio_play ====================

    private Map<String, Object> executeAudioPlay(Map<String, Object> args, String droneSn) {
        String audioName = getStringArg(args, PARAM_AUDIO_NAME, null);
        if (audioName == null) {
            return errorResult("Missing required parameter: audio_name",
                    "Provide audio file name.");
        }
        String audioUrl = getStringArg(args, PARAM_AUDIO_URL, null);
        if (audioUrl == null) {
            return errorResult("Missing required parameter: audio_url",
                    "Provide audio file URL.");
        }
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "audio_play", "speaker",
                "Play audio '" + audioName + "' on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] audio_play: droneSn={}, name={}, url={}, payloadIndex={}",
                TOOL_NAME, droneSn, audioName, audioUrl, payloadIndex);

        return sdkService.isMockMode()
                ? audioPlayInMockMode(audioName, payloadIndex)
                : audioPlayWithRealSdk(droneSn, payloadIndex, audioName, audioUrl);
    }

    private Map<String, Object> audioPlayInMockMode(String audioName, int payloadIndex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "playing");
        data.put("audio_name", audioName);
        data.put("payload_index", payloadIndex);
        data.put("_mock", true);
        return successResult(data, "Audio playback started.");
    }

    @Generated
    private Map<String, Object> audioPlayWithRealSdk(String droneSn, int payloadIndex,
                                                      String audioName, String audioUrl) {
        try {
            PsdkControlModule pcm = getPsdkModule(droneSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for drone: " + droneSn,
                        "Ensure drone is connected and SDK is initialized.");
            }

            PsdkSpeakerAudioPlayInfo info = new PsdkSpeakerAudioPlayInfo();
            info.setName(audioName);
            info.setUrl(audioUrl);

            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.speakerAudioPlayStart(payloadIndex, info, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "speakerAudioPlayStart failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "playing");
            data.put("audio_name", audioName);
            data.put("payload_index", payloadIndex);
            return successResult(data, "Audio playback started.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] audio_play timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] audio_play SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] audio_play error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to start audio playback: " + e.getMessage(), null);
        }
    }

    // ==================== tts_play ====================

    private Map<String, Object> executeTtsPlay(Map<String, Object> args, String droneSn) {
        String text = getStringArg(args, PARAM_TEXT, null);
        if (text == null) {
            return errorResult("Missing required parameter: text",
                    "Provide TTS text content.");
        }
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "tts_play", "speaker",
                "TTS broadcast on payload[" + payloadIndex + "]: " + truncateText(text));
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] tts_play: droneSn={}, text='{}', payloadIndex={}",
                TOOL_NAME, droneSn, truncateText(text), payloadIndex);

        return sdkService.isMockMode()
                ? ttsPlayInMockMode(text, payloadIndex)
                : ttsPlayWithRealSdk(droneSn, payloadIndex, text);
    }

    private Map<String, Object> ttsPlayInMockMode(String text, int payloadIndex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "playing");
        data.put("text", text);
        data.put("payload_index", payloadIndex);
        data.put("_mock", true);
        return successResult(data, "TTS playback started.");
    }

    @Generated
    private Map<String, Object> ttsPlayWithRealSdk(String droneSn, int payloadIndex, String text) {
        try {
            PsdkControlModule pcm = getPsdkModule(droneSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for drone: " + droneSn,
                        "Ensure drone is connected and SDK is initialized.");
            }

            PsdkSpeakerTTSPlayInfo info = new PsdkSpeakerTTSPlayInfo();
            info.setName("tts_" + System.currentTimeMillis());
            info.setText(text);

            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.speakerTTSPlayStart(payloadIndex, info, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "speakerTTSPlayStart failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "playing");
            data.put("text", text);
            data.put("payload_index", payloadIndex);
            return successResult(data, "TTS playback started.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] tts_play timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] tts_play SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] tts_play error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to start TTS playback: " + e.getMessage(), null);
        }
    }

    // ==================== stop ====================

    private Map<String, Object> executeStop(Map<String, Object> args, String droneSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "stop", "speaker", "Stop speaker on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] stop: droneSn={}, payloadIndex={}", TOOL_NAME, droneSn, payloadIndex);

        return sdkService.isMockMode()
                ? voidActionInMockMode("stopped", payloadIndex)
                : stopWithRealSdk(droneSn, payloadIndex);
    }

    @Generated
    private Map<String, Object> stopWithRealSdk(String droneSn, int payloadIndex) {
        return executeVoidSdkAction(droneSn, payloadIndex, "stop",
                (pcm, future) -> pcm.speakerPlayStop(payloadIndex, error -> {
                    if (!error.isSuccess()) {
                        future.completeExceptionally(new SdkCallbackBridge.SdkException(
                                error.getServerCode(), "speakerPlayStop failed: " + error.getServerMessage()));
                        return;
                    }
                    future.complete(null);
                }),
                "Speaker stopped.");
    }

    // ==================== replay ====================

    private Map<String, Object> executeReplay(Map<String, Object> args, String droneSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "replay", "speaker", "Replay last content on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] replay: droneSn={}, payloadIndex={}", TOOL_NAME, droneSn, payloadIndex);

        return sdkService.isMockMode()
                ? voidActionInMockMode("replaying", payloadIndex)
                : replayWithRealSdk(droneSn, payloadIndex);
    }

    @Generated
    private Map<String, Object> replayWithRealSdk(String droneSn, int payloadIndex) {
        return executeVoidSdkAction(droneSn, payloadIndex, "replay",
                (pcm, future) -> pcm.speakerReplay(payloadIndex, error -> {
                    if (!error.isSuccess()) {
                        future.completeExceptionally(new SdkCallbackBridge.SdkException(
                                error.getServerCode(), "speakerReplay failed: " + error.getServerMessage()));
                        return;
                    }
                    future.complete(null);
                }),
                "Replay started.");
    }

    // ==================== realtime_start ====================

    private Map<String, Object> executeRealtimeStart(Map<String, Object> args, String droneSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "realtime_start", "speaker",
                "Start real-time announcement on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] realtime_start: droneSn={}, payloadIndex={}", TOOL_NAME, droneSn, payloadIndex);

        return sdkService.isMockMode()
                ? voidActionInMockMode("realtime_active", payloadIndex)
                : realtimeStartWithRealSdk(droneSn, payloadIndex);
    }

    @Generated
    private Map<String, Object> realtimeStartWithRealSdk(String droneSn, int payloadIndex) {
        return errorResult("UNSUPPORTED",
                "Real-time announcement (speakerRealTimeStart) is no longer supported in SDK v3.0.0");
    }

    // ==================== realtime_stop ====================

    private Map<String, Object> executeRealtimeStop(Map<String, Object> args, String droneSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "realtime_stop", "speaker",
                "Stop real-time announcement on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] realtime_stop: droneSn={}, payloadIndex={}", TOOL_NAME, droneSn, payloadIndex);

        return sdkService.isMockMode()
                ? voidActionInMockMode("realtime_stopped", payloadIndex)
                : realtimeStopWithRealSdk(droneSn, payloadIndex);
    }

    @Generated
    private Map<String, Object> realtimeStopWithRealSdk(String droneSn, int payloadIndex) {
        return errorResult("UNSUPPORTED",
                "Real-time announcement (speakerRealTimeStop) is no longer supported in SDK v3.0.0");
    }

    // ==================== set_volume ====================

    private Map<String, Object> executeSetVolume(Map<String, Object> args, String droneSn) {
        int volume = getIntArg(args, PARAM_VOLUME, -1);
        if (volume < VOLUME_MIN || volume > VOLUME_MAX) {
            return errorResult("Invalid volume: must be 0-100",
                    "Current value: " + volume);
        }
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "set_volume", "speaker",
                "Set volume to " + volume + " on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] set_volume: droneSn={}, volume={}, payloadIndex={}", TOOL_NAME, droneSn, volume, payloadIndex);

        return sdkService.isMockMode()
                ? setVolumeInMockMode(volume, payloadIndex)
                : setVolumeWithRealSdk(droneSn, payloadIndex, volume);
    }

    private Map<String, Object> setVolumeInMockMode(int volume, int payloadIndex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("volume", volume);
        data.put("payload_index", payloadIndex);
        data.put("_mock", true);
        return successResult(data, "Volume set to " + volume + ".");
    }

    @Generated
    private Map<String, Object> setVolumeWithRealSdk(String droneSn, int payloadIndex, int volume) {
        try {
            PsdkControlModule pcm = getPsdkModule(droneSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for drone: " + droneSn,
                        "Ensure drone is connected and SDK is initialized.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.speakerPlayVolumeSet(payloadIndex, volume, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "speakerPlayVolumeSet failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("volume", volume);
            data.put("payload_index", payloadIndex);
            return successResult(data, "Volume set to " + volume + ".");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_volume timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_volume SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] set_volume error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set volume: " + e.getMessage(), null);
        }
    }

    // ==================== set_mode ====================

    private Map<String, Object> executeSetMode(Map<String, Object> args, String droneSn) {
        int playMode = getIntArg(args, PARAM_PLAY_MODE, -1);
        if (playMode != 0 && playMode != 1) {
            return errorResult("Invalid play_mode: must be 0 (single) or 1 (loop)",
                    "Current value: " + playMode);
        }
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "set_mode", "speaker",
                "Set play mode to " + (playMode == 0 ? "single" : "loop")
                + " on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] set_mode: droneSn={}, playMode={}, payloadIndex={}", TOOL_NAME, droneSn, playMode, payloadIndex);

        return sdkService.isMockMode()
                ? setModeInMockMode(playMode, payloadIndex)
                : setModeWithRealSdk(droneSn, payloadIndex, playMode);
    }

    private Map<String, Object> setModeInMockMode(int playMode, int payloadIndex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("play_mode", playMode);
        data.put("play_mode_name", playMode == 0 ? "single" : "loop");
        data.put("payload_index", payloadIndex);
        data.put("_mock", true);
        return successResult(data, "Play mode set.");
    }

    @Generated
    private Map<String, Object> setModeWithRealSdk(String droneSn, int payloadIndex, int playMode) {
        try {
            PsdkControlModule pcm = getPsdkModule(droneSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for drone: " + droneSn,
                        "Ensure drone is connected and SDK is initialized.");
            }

            // 0 = SINGLE_PLAY, 1 = LOOP_PLAYBACK
            MegaphonePlayMode mode = MegaphonePlayMode.fromValue(playMode);

            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.speakerPlayModeSet(payloadIndex, mode, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "speakerPlayModeSet failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("play_mode", playMode);
            data.put("play_mode_name", playMode == 0 ? "single" : "loop");
            data.put("payload_index", payloadIndex);
            return successResult(data, "Play mode set.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_mode timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_mode SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] set_mode error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set play mode: " + e.getMessage(), null);
        }
    }

    // ==================== tts_config ====================

    private Map<String, Object> executeTtsConfig(Map<String, Object> args, String droneSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);
        int volume = getIntArg(args, PARAM_VOLUME, 50);
        int voiceType = getIntArg(args, PARAM_VOICE_TYPE, 0);
        int languageType = getIntArg(args, PARAM_LANGUAGE_TYPE, 0);
        int speed = getIntArg(args, PARAM_SPEED, 5);

        // 参数校验
        if (volume < VOLUME_MIN || volume > VOLUME_MAX) {
            return errorResult("Invalid volume: must be 0-100",
                    "Current value: " + volume);
        }
        if (voiceType != 0 && voiceType != 1) {
            return errorResult("Invalid voice_type: must be 0 (male) or 1 (female)",
                    "Current value: " + voiceType);
        }
        if (languageType != 0 && languageType != 1) {
            return errorResult("Invalid language_type: must be 0 (Chinese) or 1 (English)",
                    "Current value: " + languageType);
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "tts_config", "speaker",
                "Update TTS configuration on payload[" + payloadIndex + "]");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] tts_config: droneSn={}, volume={}, voiceType={}, language={}, speed={}, payloadIndex={}",
                TOOL_NAME, droneSn, volume, voiceType, languageType, speed, payloadIndex);

        return sdkService.isMockMode()
                ? ttsConfigInMockMode(volume, voiceType, languageType, speed, payloadIndex)
                : ttsConfigWithRealSdk(droneSn, payloadIndex, volume, voiceType, languageType, speed);
    }

    private Map<String, Object> ttsConfigInMockMode(int volume, int voiceType, int languageType,
                                                     int speed, int payloadIndex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("volume", volume);
        data.put("voice_type", voiceType);
        data.put("voice_type_name", voiceType == 0 ? "male" : "female");
        data.put("language_type", languageType);
        data.put("language_name", languageType == 0 ? "Chinese" : "English");
        data.put("speed", speed);
        data.put("payload_index", payloadIndex);
        data.put("_mock", true);
        return successResult(data, "TTS configuration updated.");
    }

    @Generated
    private Map<String, Object> ttsConfigWithRealSdk(String droneSn, int payloadIndex,
                                                      int volume, int voiceType,
                                                      int languageType, int speed) {
        try {
            PsdkControlModule pcm = getPsdkModule(droneSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for drone: " + droneSn,
                        "Ensure drone is connected and SDK is initialized.");
            }

            PsdkSpeakerTTSconfig config = new PsdkSpeakerTTSconfig();
            config.setVolume(volume);
            config.setType(MegaphoneTTSVoiceType.fromValue(voiceType));
            config.setLanguage(MegaphoneTTSLanguageType.fromValue(languageType));
            config.setSpeed(speed);

            CompletableFuture<Void> future = new CompletableFuture<>();
            pcm.speakerTTSSet(payloadIndex, config, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "speakerTTSSet failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("volume", volume);
            data.put("voice_type", voiceType);
            data.put("voice_type_name", voiceType == 0 ? "male" : "female");
            data.put("language_type", languageType);
            data.put("language_name", languageType == 0 ? "Chinese" : "English");
            data.put("speed", speed);
            data.put("payload_index", payloadIndex);
            return successResult(data, "TTS configuration updated.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] tts_config timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] tts_config SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] tts_config error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to configure TTS: " + e.getMessage(), null);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取指定无人机的 PsdkControlModule 实例。
     *
     * @param droneSn 无人机 SN
     * @return PsdkControlModule，不可用时返回 null
     */
    private PsdkControlModule getPsdkModule(String droneSn) {
        Drone drone = sdkService.getDrone(droneSn);
        if (drone == null) {
            return null;
        }
        return drone.getPsdkControlModule();
    }

    /**
     * Mock 模式下通用的 void action 响应。
     *
     * @param status       状态描述
     * @param payloadIndex payload 索引
     * @return Mock 响应
     */
    private Map<String, Object> voidActionInMockMode(String status, int payloadIndex) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", status);
        data.put("payload_index", payloadIndex);
        data.put("_mock", true);
        return successResult(data, capitalizeFirst(status.replace("_", " ")) + ".");
    }

    /**
     * 通用的简单 void SDK 操作执行器。
     * 适用于只需要 payloadIndex 参数、没有额外输入的操作。
     *
     * @param droneSn        无人机 SN
     * @param payloadIndex   payload 索引
     * @param actionName     操作名称（用于日志）
     * @param sdkCall        SDK 调用逻辑
     * @param successMessage 成功消息
     * @return 执行结果
     */
    private Map<String, Object> executeVoidSdkAction(String droneSn, int payloadIndex,
                                                      String actionName, SdkVoidAction sdkCall,
                                                      String successMessage) {
        try {
            PsdkControlModule pcm = getPsdkModule(droneSn);
            if (pcm == null) {
                return errorResult("PsdkControlModule not available for drone: " + droneSn,
                        "Ensure drone is connected and SDK is initialized.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            sdkCall.invoke(pcm, future);

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "success");
            data.put("payload_index", payloadIndex);
            return successResult(data, successMessage);

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] {} timeout: {}", TOOL_NAME, actionName, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] {} SDK error: [{}] {}", TOOL_NAME, actionName, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] {} error: {}", TOOL_NAME, actionName, e.getMessage(), e);
            return errorResult("Failed to execute " + actionName + ": " + e.getMessage(), null);
        }
    }

    /**
     * 截断文本用于日志展示。
     *
     * @param text 原始文本
     * @return 截断后的文本（最多 50 字符）
     */
    private String truncateText(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= TEXT_TRUNCATE_LENGTH) {
            return text;
        }
        return text.substring(0, TEXT_TRUNCATE_LENGTH) + "...";
    }

    /**
     * 首字母大写工具方法。
     *
     * @param str 输入字符串
     * @return 首字母大写后的字符串
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * SDK void 操作的函数式接口。
     */
    @FunctionalInterface
    private interface SdkVoidAction {
        /**
         * 执行 SDK 操作。
         *
         * @param pcm    PsdkControlModule 实例
         * @param future CompletableFuture 用于异步结果通知
         */
        void invoke(PsdkControlModule pcm, CompletableFuture<Void> future);
    }
}
