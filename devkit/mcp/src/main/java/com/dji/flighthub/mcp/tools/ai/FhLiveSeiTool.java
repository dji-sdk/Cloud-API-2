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

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.core.LiveModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * fh_live_sei — 直播可用范围查询、拉流、SEI 数据订阅。
 *
 * <p>安全等级：工具级声明 L1（REVERSIBLE，门控基线）。
 * start/stop 在 execute() 内单独执行 {@code enforceSecurityLevel(REVERSIBLE)} 并附带人工确认，
 * 其余只读订阅类 action 由派发层按工具级等级统一门控。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhLiveSeiTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhLiveSeiTool.class);

    private static final String TOOL_NAME = "fh_live_sei";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";

    private static final String ACTION_RANGES = "ranges";
    private static final String ACTION_START = "start";
    private static final String ACTION_SUBSCRIBE = "subscribe";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_STOP = "stop";

    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_RANGES, ACTION_START, ACTION_SUBSCRIBE, ACTION_EVENTS, ACTION_STOP);

    private static final String SEI_AI = "ai";
    private static final String SEI_AR = "ar";
    private static final String SEI_FOV = "fov";
    private static final String SEI_ASSISTANT = "assistant";
    private static final String SEI_EIS = "eis";
    private static final List<String> SEI_CHANNELS = List.of(SEI_AI, SEI_AR, SEI_FOV, SEI_ASSISTANT, SEI_EIS);

    private static final String SEI_SUFFIX = "_sei";

    private static final int RANGE_LISTEN_TIMEOUT_SECONDS = 10;

    private static final AtomicInteger MOCK_ID_GEN = new AtomicInteger(8000);

    private final SdkService sdkService;
    private final AiSubscriptionBuffer buffer;

    public FhLiveSeiTool(SessionManager sessionManager, SdkService sdkService,
                          AiSubscriptionBuffer buffer) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.buffer = buffer;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
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
        def.put("description", "Query live stream ranges, start/stop live streaming, "
                + "subscribe to SEI data (ai/ar/fov/assistant/eis), and read SEI events. "
                + "ranges/events/subscribe are read-only; start/stop change device state.");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string", "description", "Target device serial number"));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", List.of("events", "ranges", "start", "stop", "subscribe"),
                "description", "Live SEI action"));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Camera payload index (e.g. '81-0-0'), required for start/subscribe/events/stop"));
        props.put("video_id", Map.of(
                "type", "string",
                "description", "Video ID override (for start, optional)"));
        props.put("channels", Map.of(
                "type", "array", "items", Map.of("type", "string"),
                "description", "SEI channels: ai, ar, fov, assistant, eis (for subscribe, default ['ai'])"));
        props.put("channel", Map.of(
                "type", "string",
                "description", "SEI channel to read: ai/ar/fov/assistant/eis, optional _sei suffix (for events, default 'ai')"));
        props.put("since_seq", Map.of(
                "type", "integer",
                "description", "Return events with _seq > since_seq (for events, default 0)"));
        props.put("limit", Map.of(
                "type", "integer",
                "description", "Max events to return (for events, default 50)"));
        props.put("confirm", Map.of(
                "type", "boolean", "description", "Required for start/stop actions"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }
        String sn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (sn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide the target device serial number.");
        }
        String action = getStringArg(args, PARAM_ACTION, "");
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "Valid actions: ranges, start, subscribe, events, stop");
        }

        if (ACTION_START.equals(action) || ACTION_STOP.equals(action)) {
            Map<String, Object> secError = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
            if (secError != null) {
                return secError;
            }
            Map<String, Object> confirmError = requireHumanConfirm(args, TOOL_NAME + ":" + action, sn,
                    String.format("About to %s live stream on %s. Confirm?", action, sn));
            if (confirmError != null) {
                return confirmError;
            }
        }

        return switch (action) {
            case ACTION_RANGES -> handleRanges(sn);
            case ACTION_START -> handleStart(sn, args);
            case ACTION_SUBSCRIBE -> handleSubscribe(session, sn, args);
            case ACTION_EVENTS -> handleEvents(session, sn, args);
            case ACTION_STOP -> handleStop(session, sn, args);
            default -> errorResult("Unhandled action", "This should not happen.");
        };
    }

    private Map<String, Object> handleRanges(String sn) {
        if (sdkService.isMockMode()) {
            return successResult(Map.of(
                    "_mock", true,
                    "device_sn", sn,
                    "available_ranges", List.of(
                            Map.of("payload_index", "81-0-0", "video_id", "normal-0"),
                            Map.of("payload_index", "82-0-0", "video_id", "normal-0"))
            ), "Mock: live available ranges");
        }

        Drone drone = sdkService.getDrone(sn);
        if (drone == null) {
            return deviceNotFound(sn);
        }

        LiveDeviceModule liveDeviceModule = drone.getLiveDeviceModule();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean received = new AtomicBoolean(false);
        List<Map<String, Object>> rangesList = new CopyOnWriteArrayList<>();

        int listenId = sdkListenRanges(liveDeviceModule, batch -> {
            if (received.compareAndSet(false, true)) {
                rangesList.addAll(batch);
                latch.countDown();
            }
        });
        if (listenId < 0) {
            return errorResult("SDK refused live range listener (listenId=" + listenId + ")",
                    "Check device state.");
        }

        try {
            if (!latch.await(RANGE_LISTEN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                return errorResult("Timeout waiting for live available ranges",
                        "Device may not support live streaming or is disconnected.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return errorResult("Interrupted while waiting for ranges", "Try again.");
        } finally {
            sdkCancelRanges(liveDeviceModule, listenId);
        }

        List<Map<String, Object>> ranges = new ArrayList<>(rangesList);
        return successResult(Map.of("device_sn", sn, "available_ranges", ranges),
                "Found " + ranges.size() + " available streams");
    }

    @Generated
    private int sdkListenRanges(LiveDeviceModule module, Consumer<List<Map<String, Object>>> sink) {
        return module.listenLiveAvailableRange(liveInfos -> {
            List<Map<String, Object>> out = new ArrayList<>();
            if (liveInfos != null) {
                for (var info : liveInfos) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("payload_index", info.getPayloadIndex());
                    m.put("video_id", info.getVideoId());
                    m.put("device_sn", info.getDeviceSn());
                    out.add(m);
                }
            }
            sink.accept(out);
        });
    }

    @Generated
    private void sdkCancelRanges(LiveDeviceModule module, int listenId) {
        module.cancelListenLiveAvailableRange(listenId);
    }

    private Map<String, Object> handleStart(String sn, Map<String, Object> args) {
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
        if (payloadIndex == null) {
            return errorResult("Missing required parameter: payload_index",
                    "Use action=ranges first to discover available payload indices.");
        }

        if (sdkService.isMockMode()) {
            return successResult(Map.of("_mock", true, "device_sn", sn,
                    "payload_index", payloadIndex, "status", "streaming"),
                    "Mock: live stream started");
        }

        Drone drone = sdkService.getDrone(sn);
        if (drone == null) {
            return deviceNotFound(sn);
        }

        LiveInfo liveInfo = buildLiveInfo(sn, payloadIndex);
        String videoId = getStringArg(args, "video_id", null);
        if (videoId != null) {
            liveInfo.setVideoId(videoId);
        }

        try {
            sdkStartLive(drone.getLiveStream(liveInfo));
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout starting live: " + e.getMessage(), "Check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error starting live [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check device state.");
        }

        return successResult(Map.of("device_sn", sn, "payload_index", payloadIndex, "status", "streaming"),
                "Live stream started on " + payloadIndex);
    }

    @Generated
    private void sdkStartLive(LiveModule liveModule) {
        SdkCallbackBridge.awaitVoid(liveModule::start);
    }

    private Map<String, Object> handleSubscribe(SessionContext session, String sn, Map<String, Object> args) {
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
        if (payloadIndex == null) {
            return errorResult("Missing required parameter: payload_index",
                    "Provide the camera payload index.");
        }

        List<String> channels = getStringListArg(args, "channels");
        if (channels == null || channels.isEmpty()) {
            channels = List.of(SEI_AI);
        }
        for (String ch : channels) {
            if (!SEI_CHANNELS.contains(ch)) {
                return errorResult("Invalid SEI channel: " + ch, "Valid channels: " + SEI_CHANNELS);
            }
        }

        boolean mock = sdkService.isMockMode();
        LiveModule liveModule = null;
        if (!mock) {
            Drone drone = sdkService.getDrone(sn);
            if (drone == null) {
                return deviceNotFound(sn);
            }
            liveModule = drone.getLiveStream(buildLiveInfo(sn, payloadIndex));
        }

        Map<String, Integer> listenIds = new LinkedHashMap<>();
        for (String ch : channels) {
            int id = subscribeChannel(liveModule, session, sn, payloadIndex, ch);
            if (id < 0) {
                for (String started : listenIds.keySet()) {
                    cancelQuietly(session, registryKey(sn, seiStreamKey(started, payloadIndex)));
                }
                return errorResult("SDK refused SEI subscription for channel " + ch + " (listenId=" + id + ")",
                        "Check stream state; no subscription was kept.");
            }
            listenIds.put(ch, id);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        if (mock) {
            data.put("_mock", true);
        }
        data.put("listen_ids", listenIds);
        return successResult(data, (mock ? "Mock: " : "") + "SEI subscriptions started: " + channels);
    }

    int subscribeChannel(LiveModule liveModule, SessionContext session,
                         String sn, String payloadIndex, String channel) {
        String streamKey = seiStreamKey(channel, payloadIndex);
        String key = registryKey(sn, streamKey);
        cancelQuietly(session, key);
        String scope = AiSubscriptionBuffer.scope(session, sn);
        boolean mock = sdkService.isMockMode();
        int listenId;
        if (mock) {
            injectMockSeiData(scope, streamKey, channel);
            listenId = MOCK_ID_GEN.incrementAndGet();
        } else {
            listenId = sdkSubscribe(liveModule, scope, streamKey, channel);
        }
        if (listenId < 0) {
            return listenId;
        }
        session.registerListen(key, "sei_" + channel + ":" + sn + ":" + payloadIndex, () -> {
            try {
                if (!mock) {
                    sdkUnsubscribe(liveModule, channel, listenId);
                }
            } finally {
                buffer.remove(scope, streamKey);
            }
        });
        return listenId;
    }

    @Generated
    private int sdkSubscribe(LiveModule liveModule, String scope, String streamKey, String channel) {
        return switch (channel) {
            case SEI_AI -> liveModule.subscribeAiSeiInfo(sei ->
                    buffer.append(scope, streamKey, AiCallbackMapper.mapAiSeiInfo(sei)));
            case SEI_AR -> liveModule.subscribeArSeiInfo(ar ->
                    buffer.append(scope, streamKey, AiCallbackMapper.mapArSeiInfo(ar)));
            case SEI_FOV -> liveModule.subscribeFovSeiInfo(fov ->
                    buffer.append(scope, streamKey, AiCallbackMapper.mapFovSeiInfo(fov)));
            case SEI_ASSISTANT -> liveModule.subscribeAssistantSeiInfo(a ->
                    buffer.append(scope, streamKey, AiCallbackMapper.mapAssistantSeiInfo(a)));
            case SEI_EIS -> liveModule.subscribeEisSeiInfo(eis ->
                    buffer.append(scope, streamKey, AiCallbackMapper.mapEisSeiInfo(eis)));
            default -> throw new IllegalArgumentException("Unknown SEI channel: " + channel);
        };
    }

    @Generated
    private void sdkUnsubscribe(LiveModule liveModule, String channel, int listenId) {
        switch (channel) {
            case SEI_AI -> liveModule.unSubscribeAiSeiInfo(listenId);
            case SEI_AR -> liveModule.unSubscribeArSeiInfo(listenId);
            case SEI_FOV -> liveModule.unSubscribeFovSeiInfo(listenId);
            case SEI_ASSISTANT -> liveModule.unSubscribeAssistantSeiInfo(listenId);
            case SEI_EIS -> liveModule.unSubscribeEisSeiInfo(listenId);
            default -> throw new IllegalArgumentException("Unknown SEI channel: " + channel);
        }
    }

    private Map<String, Object> handleEvents(SessionContext session, String sn, Map<String, Object> args) {
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
        if (payloadIndex == null) {
            return errorResult("Missing required parameter: payload_index",
                    "Provide the camera payload index.");
        }
        String channel = getStringArg(args, "channel", SEI_AI);
        String normalized = channel.endsWith(SEI_SUFFIX)
                ? channel.substring(0, channel.length() - SEI_SUFFIX.length()) : channel;
        if (!SEI_CHANNELS.contains(normalized)) {
            return errorResult("Invalid SEI channel: " + channel, "Valid channels: " + SEI_CHANNELS);
        }
        long sinceSeq = getLongArg(args, "since_seq", 0);
        int limit = getIntArg(args, "limit", 50);

        AiSubscriptionBuffer.ReadResult readResult = buffer.read(
                AiSubscriptionBuffer.scope(session, sn), seiStreamKey(normalized, payloadIndex), sinceSeq, limit);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", sn);
        data.put("payload_index", payloadIndex);
        data.put("channel", normalized);
        data.put("events", readResult.events());
        data.put("count", readResult.events().size());
        data.put("dropped", readResult.dropped());
        data.put("truncated", readResult.truncated());
        if (!readResult.events().isEmpty()) {
            data.put("last_seq", readResult.events().get(readResult.events().size() - 1).get("_seq"));
        }

        String hint;
        if (readResult.truncated()) {
            hint = "Some events after since_seq were evicted from the buffer; poll more frequently.";
        } else if (readResult.events().isEmpty()) {
            hint = "No SEI events yet. Ensure subscribe was called and stream is active.";
        } else {
            hint = "Use last_seq as since_seq for incremental reads.";
        }
        return successResult(data, hint);
    }

    private Map<String, Object> handleStop(SessionContext session, String sn, Map<String, Object> args) {
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
        if (payloadIndex == null) {
            return errorResult("Missing required parameter: payload_index",
                    "Provide the camera payload index.");
        }

        List<String> stopped = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String ch : SEI_CHANNELS) {
            try {
                if (session.cancelListen(registryKey(sn, seiStreamKey(ch, payloadIndex)))) {
                    stopped.add(ch);
                }
            } catch (RuntimeException e) {
                log.warn("Failed to cancel SEI listen: sn={}, channel={}, error={}", sn, ch, e.getMessage());
                failed.add(ch);
            }
        }

        String liveStopError = null;
        if (!sdkService.isMockMode()) {
            Drone drone = sdkService.getDrone(sn);
            if (drone == null) {
                liveStopError = "device instance not found: " + sn;
            } else {
                try {
                    sdkStopLive(drone.getLiveStream(buildLiveInfo(sn, payloadIndex)));
                } catch (RuntimeException e) {
                    log.warn("Error stopping live stream: sn={}, payload={}, error={}",
                            sn, payloadIndex, e.getMessage());
                    liveStopError = e.getMessage();
                }
            }
        }

        if (!failed.isEmpty() || liveStopError != null) {
            StringBuilder msg = new StringBuilder("Stop incomplete:");
            if (!failed.isEmpty()) {
                msg.append(" failed to cancel SEI subscriptions ").append(failed).append(';');
            }
            if (liveStopError != null) {
                msg.append(" live stream stop failed (").append(liveStopError).append(");");
            }
            msg.append(" stopped subscriptions: ").append(stopped);
            return errorResult(msg.toString(), "Retry stop; check device connectivity.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", sn);
        data.put("payload_index", payloadIndex);
        data.put("stopped_subscriptions", stopped);
        data.put("live_stream_stopped", true);
        return successResult(data,
                stopped.isEmpty() ? "Live stream stopped; no active SEI subscriptions"
                        : "Stopped: " + stopped + " + live stream");
    }

    @Generated
    private void sdkStopLive(LiveModule liveModule) {
        liveModule.stop();
    }

    private void injectMockSeiData(String scope, String streamKey, String channel) {
        switch (channel) {
            case SEI_AI -> buffer.append(scope, streamKey, Map.of(
                    "targets", List.of(Map.of(
                            "target_id", 1, "target_type", 2, "target_state", 1,
                            "target_cx", 500, "target_cy", 300,
                            "target_w", 80, "target_h", 120, "target_distance", 150L))));
            case SEI_AR -> buffer.append(scope, streamKey, Map.of(
                    "camera_yaw", 45.0, "camera_pitch", -10.0, "camera_roll", 0.0,
                    "camera_pos_x", 100, "camera_pos_y", 200, "camera_pos_z", 50,
                    "latitude", 22654321, "longitude", 113987654, "gnd_origin_z", 0, "altitude", 30));
            case SEI_FOV -> buffer.append(scope, streamKey, Map.of(
                    "fov_horizontal", 84L, "fov_vertical", 53L, "cur_actual_focal_length", 24L));
            case SEI_ASSISTANT -> buffer.append(scope, streamKey, Map.of(
                    "auto_direction", false, "direction", 0));
            case SEI_EIS -> buffer.append(scope, streamKey, Map.of(
                    "version", 1,
                    "left_top", Map.of("x", 0, "y", 0),
                    "right_top", Map.of("x", 1920, "y", 0),
                    "right_bottom", Map.of("x", 1920, "y", 1080),
                    "left_bottom", Map.of("x", 0, "y", 1080)));
            default -> { /* unknown channel, no mock data */ }
        }
    }

    private static LiveInfo buildLiveInfo(String sn, String payloadIndex) {
        LiveInfo liveInfo = new LiveInfo();
        liveInfo.setDeviceSn(sn);
        liveInfo.setPayloadIndex(payloadIndex);
        return liveInfo;
    }

    private Map<String, Object> deviceNotFound(String sn) {
        return errorResult("Device instance not found: " + sn,
                "Use fh_device_list to check device status.");
    }

    private void cancelQuietly(SessionContext session, String key) {
        try {
            session.cancelListen(key);
        } catch (RuntimeException e) {
            log.warn("Failed to cancel SEI listen: key={}, error={}", key, e.getMessage());
        }
    }

    private static String seiStreamKey(String channel, String payloadIndex) {
        return channel + SEI_SUFFIX + ":" + payloadIndex;
    }

    private static String registryKey(String sn, String streamKey) {
        return "sei::" + sn + "::" + streamKey;
    }
}
