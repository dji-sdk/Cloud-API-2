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
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.PsdkSpeakerAudioPlayInfo;
import com.dji.flighthubsdk.bean.PsdkSpeakerPlayProgressInfo;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
 *   <li>listen_progress — 订阅播放进度事件（只读，无需 confirm）</li>
 *   <li>events — 按游标轮询播放进度事件（只读）</li>
 *   <li>listen_stop — 释放播放进度订阅（只读）</li>
 * </ul>
 *
 * <p>前 9 个 action 均为设备控制操作（L1），统一需要 confirm；
 * 后 3 个为订阅三元组（READ_ONLY），不经过 L1 硬拦截，也不需 confirm。
 * <p>支持 mock 模式（FH_MOCK=true）和真实 SDK 调用双路径。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhSpeakerTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhSpeakerTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_speaker";

    // ── 订阅类 action / channel 常量 ──

    /** 订阅播放进度 */
    private static final String ACTION_LISTEN_PROGRESS = "listen_progress";

    /** 轮询已订阅 channel 的缓冲事件 */
    private static final String ACTION_EVENTS = "events";

    /** 释放订阅 */
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            "audio_play", "tts_play", "stop", "replay",
            "realtime_start", "realtime_stop",
            "set_volume", "set_mode", "tts_config",
            ACTION_LISTEN_PROGRESS, ACTION_EVENTS, ACTION_LISTEN_STOP);

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
    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    // ── 订阅 channel 常量 ──

    /** 播放进度 channel */
    private static final String CHANNEL_PLAY_PROGRESS = "speaker_play_progress";

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "speaker";

    /** scopeId / 注册表 key 中 drone_sn 与 payload_index 的分隔符 */
    private static final String SCOPE_SEPARATOR = "#";

    /** 注册表 key 中 scopeId 与 channel 的分隔符（与 ListenChannelSupport 保持一致） */
    private static final String KEY_SEPARATOR = "::";

    /** 非 0 号 payload 的 stream key 后缀分隔符 */
    private static final String STREAM_INDEX_SEPARATOR = "@";

    /** events 动作每个 channel 默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    /** 事件字段名：PSDK 设备索引 */
    private static final String EVENT_KEY_PSDK_INDEX = "psdk_index";
    private static final String EVENT_KEY_RESULT = "result";
    private static final String EVENT_KEY_STATUS = "status";
    private static final String EVENT_KEY_STEP_KEY = "step_key";
    private static final String EVENT_KEY_PERCENT = "percent";
    private static final String EVENT_KEY_MD5 = "md5";

    /** 音量范围 */
    private static final int VOLUME_MIN = 0;
    private static final int VOLUME_MAX = 100;

    /** 默认 payload 索引 */
    private static final int DEFAULT_PAYLOAD_INDEX = 0;

    /** 文本截断长度 */
    private static final int TEXT_TRUNCATE_LENGTH = 50;

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    public FhSpeakerTool(SessionManager sessionManager, SdkService sdkService,
                         ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 声明工具级静态安全等级：REVERSIBLE。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Control PSDK speaker (megaphone): play audio files, TTS text-to-speech, "
                + "real-time announcement, volume/mode settings. "
                + "Actions: audio_play, tts_play, stop, replay, realtime_start, realtime_stop, "
                + "set_volume, set_mode, tts_config (these control actions require confirm=true), "
                + "plus a read-only play-progress subscription triple: listen_progress -> poll with "
                + "events (cursor: since_seq/limit) -> release with listen_stop. "
                + "The subscription actions are read-only, need no confirm, and never affect the "
                + "other control actions.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Speaker operation to perform. Control actions (need confirm=true): "
                        + "audio_play | tts_play | stop | replay | realtime_start | realtime_stop | "
                        + "set_volume | set_mode | tts_config. "
                        + "Read-only subscription actions (no confirm): "
                        + "listen_progress (subscribe to play progress) | "
                        + "events (poll events by since_seq cursor) | "
                        + "listen_stop (release subscriptions)"
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Drone serial number (required, identifies which drone's speaker to control)"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "integer",
                "description", "PSDK device index (default: 0); also scopes listen_progress "
                        + "subscriptions — different indices are independent channels"
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
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Channel names for events/listen_stop (optional; omitted = all active "
                        + "channels for this drone_sn). Base channel is '" + CHANNEL_PLAY_PROGRESS + "'; "
                        + "for a non-zero payload_index it is '" + CHANNEL_PLAY_PROGRESS
                        + STREAM_INDEX_SEPARATOR + "<payload_index>'"
        ));
        props.put(PARAM_SINCE_SEQ, Map.of(
                "type", "integer",
                "description", "Cursor for events: only return events with _seq greater than this "
                        + "value (default 0)"
        ));
        props.put(PARAM_LIMIT, Map.of(
                "type", "integer",
                "description", "Max events to return per channel for events action (default "
                        + DEFAULT_EVENTS_LIMIT + ")"
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Set true to confirm the operation (required for the 9 control actions; "
                        + "ignored by listen_progress/events/listen_stop)"
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

        // 订阅三元组为 READ_ONLY，必须在 L1 硬拦截之前分发
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(action, args, droneSn);
        }

        // 所有 speaker 控制操作统一 L1 硬拦截
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

    // ==================== 订阅三元组（READ_ONLY） ====================

    /**
     * 判断是否为订阅类 action（listen_progress/events/listen_stop）。
     *
     * @param action 操作名
     * @return 是订阅类返回 true
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_LISTEN_PROGRESS.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

    /**
     * 订阅类 action 分发入口。
     *
     * @param action  操作名
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(String action, Map<String, Object> args, String droneSn) {
        SessionContext session = requireSession();
        return switch (action) {
            case ACTION_LISTEN_PROGRESS -> handleListenProgress(session, args, droneSn);
            case ACTION_EVENTS -> handleEvents(session, args, droneSn);
            case ACTION_LISTEN_STOP -> handleListenStop(session, args, droneSn);
            default -> errorResult("Unknown action: " + action, null);
        };
    }

    /**
     * 处理 listen_progress：订阅指定 payload 的播放进度，返回 listen_id。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> handleListenProgress(SessionContext session,
                                                      Map<String, Object> args, String droneSn) {
        int payloadIndex = getIntArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);
        ChannelRef ref = new ChannelRef(payloadIndex, CHANNEL_PLAY_PROGRESS);

        int listenId = listenSupport.startChannel(session,
                ref.registryKey(droneSn), ref.streamKey(), ref.scope(session, droneSn),
                ref.streamKey(), sdkService.isMockMode(),
                () -> mockPlayProgressEvent(payloadIndex),
                onEvent -> startRealProgressListen(droneSn, payloadIndex, onEvent),
                listenId2 -> cancelRealProgressListen(droneSn, payloadIndex, listenId2));

        if (listenId < 0) {
            return errorResult("Failed to subscribe speaker play progress: drone_sn=" + droneSn
                            + ", payload_index=" + payloadIndex,
                    "Ensure drone is connected, SDK is initialized and the PSDK device index is valid.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_PROGRESS);
        data.put("channel", ref.streamKey());
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to speaker play progress. Poll with action=events.");
    }

    /**
     * 处理 events：按游标轮询一个或多个 channel 的缓冲事件。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> handleEvents(SessionContext session,
                                             Map<String, Object> args, String droneSn) {
        List<ChannelRef> refs = resolveChannelRefs(session, args, droneSn);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (ChannelRef ref : refs) {
            streams.put(ref.streamKey(), listenSupport.readChannel(session,
                    ref.registryKey(droneSn), ref.scope(session, droneSn),
                    ref.streamKey(), sinceSeq, limit));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_EVENTS);
        data.put("streams", streams);
        return successResult(data, "Polled " + streams.size() + " channel(s).");
    }

    /**
     * 处理 listen_stop：释放一个/多个/全部已订阅的 channel。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> handleListenStop(SessionContext session,
                                                 Map<String, Object> args, String droneSn) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<ChannelRef> refs;
        if (requested != null && !requested.isEmpty()) {
            refs = new ArrayList<>();
            for (String streamKey : requested) {
                ChannelRef parsed = ChannelRef.parse(streamKey);
                if (parsed == null) {
                    return errorResult("Invalid channel: " + streamKey,
                            "Valid channels: " + CHANNEL_PLAY_PROGRESS + " or "
                                    + CHANNEL_PLAY_PROGRESS + STREAM_INDEX_SEPARATOR + "<payload_index>");
                }
                refs.add(parsed);
            }
        } else {
            refs = activeChannelRefs(session, droneSn);
        }

        List<String> streamKeys = new ArrayList<>();
        for (ChannelRef ref : refs) {
            streamKeys.add(ref.streamKey());
        }

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, streamKeys,
                streamKey -> {
                    ChannelRef ref = ChannelRef.parse(streamKey);
                    return ref == null ? streamKey : ref.registryKey(droneSn);
                }, log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s).");
    }

    /**
     * 解析 events 要轮询的 channel：显式传入则用它，否则取当前 drone_sn 下全部已激活 channel。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return channel 引用列表
     */
    private List<ChannelRef> resolveChannelRefs(SessionContext session,
                                                Map<String, Object> args, String droneSn) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested != null && !requested.isEmpty()) {
            List<ChannelRef> refs = new ArrayList<>();
            for (String streamKey : requested) {
                ChannelRef ref = ChannelRef.parse(streamKey);
                if (ref != null) {
                    refs.add(ref);
                }
            }
            return refs;
        }
        return activeChannelRefs(session, droneSn);
    }

    /**
     * 列出当前 drone_sn 下所有活跃的 channel 引用。
     *
     * <p>注册表 key 形如 {@code speaker::<droneSn>#<payloadIndex>::<channel>}，
     * 因此用 {@code speaker::<droneSn>#} 作前缀可精确匹配该无人机（不会误匹配 SN 前缀相同的其他无人机）。
     *
     * @param session 当前 session
     * @param droneSn 无人机 SN
     * @return channel 引用列表
     */
    private List<ChannelRef> activeChannelRefs(SessionContext session, String droneSn) {
        List<ChannelRef> refs = new ArrayList<>();
        for (String token : listenSupport.activeChannels(session, droneScopePrefix(droneSn))) {
            ChannelRef ref = ChannelRef.parseRegistryToken(token);
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    /**
     * 该无人机订阅注册表 key 的前缀。
     *
     * @param droneSn 无人机 SN
     * @return 形如 {@code speaker::<droneSn>#}
     */
    private String droneScopePrefix(String droneSn) {
        return SUBSCRIBE_NAMESPACE + KEY_SEPARATOR + droneSn + SCOPE_SEPARATOR;
    }

    /**
     * Mock — 播放进度事件（字段与 PsdkSpeakerPlayProgressInfo 对齐）。
     *
     * @param payloadIndex PSDK 设备索引
     * @return mock 事件
     */
    private Map<String, Object> mockPlayProgressEvent(int payloadIndex) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put(EVENT_KEY_PSDK_INDEX, payloadIndex);
        event.put(EVENT_KEY_RESULT, 0);
        event.put(EVENT_KEY_STATUS, "playing");
        event.put(EVENT_KEY_STEP_KEY, "mock-step");
        event.put(EVENT_KEY_PERCENT, 50);
        event.put(EVENT_KEY_MD5, "mock-md5");
        event.put("_mock", true);
        return event;
    }

    /**
     * 真实 SDK listen：播放进度回调。cancel 时需要同一个 psdkIndex，故在此闭包中捕获。
     *
     * @param droneSn      无人机 SN
     * @param payloadIndex PSDK 设备索引
     * @param onEvent      事件消费回调
     * @return SDK listenId；模块不可用时返回 -1
     */
    private int startRealProgressListen(String droneSn, int payloadIndex,
                                        Consumer<Map<String, Object>> onEvent) {
        PsdkControlModule pcm = getPsdkModule(droneSn);
        if (pcm == null) {
            return -1;
        }
        return pcm.listenDrcPsdkSpeakerPlayProgressInfo(payloadIndex,
                info -> onEvent.accept(toMap(info)));
    }

    /**
     * 真实 SDK cancel：停止播放进度监听。
     *
     * <p>SDK 的 cancel 签名为 {@code (psdkIndex, listenId)}，两参数必须与 listen 时一致。
     *
     * @param droneSn      无人机 SN
     * @param payloadIndex PSDK 设备索引
     * @param listenId     listen 返回的监听 ID
     */
    private void cancelRealProgressListen(String droneSn, int payloadIndex, int listenId) {
        PsdkControlModule pcm = getPsdkModule(droneSn);
        if (pcm == null) {
            return;
        }
        pcm.cancelListenDrcPsdkSpeakerPlayProgressInfo(payloadIndex, listenId);
    }

    /**
     * 将 PsdkSpeakerPlayProgressInfo 转换为事件 Map。
     *
     * @param info SDK 回调对象
     * @return 事件 Map
     */
    private Map<String, Object> toMap(PsdkSpeakerPlayProgressInfo info) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put(EVENT_KEY_PSDK_INDEX, info.getPsdkIndex());
        event.put(EVENT_KEY_RESULT, info.getResult());
        event.put(EVENT_KEY_STATUS, String.valueOf(info.getStatus()));
        event.put(EVENT_KEY_STEP_KEY, info.getStepKey());
        event.put(EVENT_KEY_PERCENT, info.getPercent());
        event.put(EVENT_KEY_MD5, info.getMd5());
        return event;
    }

    /**
     * 一个订阅 channel 的引用：PSDK 设备索引 + 基础 channel 名。
     *
     * <p>SDK 的 cancel 需要与 listen 相同的 {@code psdkIndex}，同一无人机上不同
     * payload_index 的订阅互相独立，因此 scopeId 编码为 {@code <droneSn>#<payloadIndex>}。
     * 为保持默认场景的 stream key 简洁，payload_index=0 时 stream key 就是 channel 名，
     * 非 0 时追加 {@code @<payloadIndex>} 后缀。
     */
    private record ChannelRef(int payloadIndex, String channel) {

        /**
         * stream key：默认 payload 直接返回 channel 名，非 0 payload 追加索引后缀。
         *
         * @return 对外的 channel 名
         */
        String streamKey() {
            return payloadIndex == DEFAULT_PAYLOAD_INDEX
                    ? channel
                    : channel + STREAM_INDEX_SEPARATOR + payloadIndex;
        }

        /**
         * scopeId：同时编码 drone_sn 与 payload_index。
         *
         * @param droneSn 无人机 SN
         * @return scopeId
         */
        String scopeId(String droneSn) {
            return droneSn + SCOPE_SEPARATOR + payloadIndex;
        }

        /**
         * 订阅注册表 key。
         *
         * @param droneSn 无人机 SN
         * @return registry key
         */
        String registryKey(String droneSn) {
            return ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, scopeId(droneSn), channel);
        }

        /**
         * 订阅缓冲区作用域。
         *
         * @param session 当前 session
         * @param droneSn 无人机 SN
         * @return buffer scope
         */
        String scope(SessionContext session, String droneSn) {
            return SubscriptionBuffer.scope(session, scopeId(droneSn));
        }

        /**
         * 解析对外 stream key。
         *
         * @param streamKey channel 名（可含 {@code @<index>} 后缀）
         * @return 引用；channel 名非法时返回 null
         */
        static ChannelRef parse(String streamKey) {
            if (streamKey == null) {
                return null;
            }
            int sep = streamKey.lastIndexOf(STREAM_INDEX_SEPARATOR);
            if (sep < 0) {
                return CHANNEL_PLAY_PROGRESS.equals(streamKey)
                        ? new ChannelRef(DEFAULT_PAYLOAD_INDEX, streamKey) : null;
            }
            String channel = streamKey.substring(0, sep);
            if (!CHANNEL_PLAY_PROGRESS.equals(channel)) {
                return null;
            }
            try {
                return new ChannelRef(Integer.parseInt(streamKey.substring(sep + 1)), channel);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * 解析 {@link ListenChannelSupport#activeChannels} 返回的 token（{@code <payloadIndex>::<channel>}）。
         *
         * @param token 去掉前缀后的注册表 key 后缀
         * @return 引用；格式不符时返回 null
         */
        static ChannelRef parseRegistryToken(String token) {
            int sep = token.indexOf(KEY_SEPARATOR);
            if (sep < 0) {
                return null;
            }
            try {
                return new ChannelRef(Integer.parseInt(token.substring(0, sep)),
                        token.substring(sep + KEY_SEPARATOR.length()));
            } catch (NumberFormatException e) {
                return null;
            }
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
