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
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.OnboardAiModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * fh_ai_subscribe — 订阅 AI 数据推送（ai_data / alarm_state）。
 *
 * <p>安全等级：L0（READ_ONLY），前置条件：SESSION。
 * start 注册回调后立即返回，数据写入 AiSubscriptionBuffer；
 * events 从缓冲区按 since_seq 增量读取；stop 取消订阅。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhAiSubscribeTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhAiSubscribeTool.class);

    private static final String TOOL_NAME = "fh_ai_subscribe";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";

    private static final String ACTION_START = "start";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_STOP = "stop";

    private static final String CHANNEL_AI_DATA = "ai_data";
    private static final String CHANNEL_ALARM_STATE = "alarm_state";

    private static final Set<String> VALID_ACTIONS = Set.of(ACTION_START, ACTION_EVENTS, ACTION_STOP);
    private static final List<String> VALID_CHANNELS = List.of(CHANNEL_AI_DATA, CHANNEL_ALARM_STATE);

    private static final AtomicInteger MOCK_ID_GEN = new AtomicInteger(9000);

    private final SdkService sdkService;
    private final AiSubscriptionBuffer buffer;

    public FhAiSubscribeTool(SessionManager sessionManager, SdkService sdkService,
                              AiSubscriptionBuffer buffer) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.buffer = buffer;
    }

    /**
     * 声明工具级静态安全等级：READ_ONLY。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description", "Subscribe to AI data push (ai_data / alarm_state). "
                + "start: register callback and return immediately. "
                + "events: pull buffered data incrementally via since_seq. "
                + "stop: cancel subscription.");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string", "description", "Target drone serial number"));
        props.put(PARAM_ACTION, Map.of(
                "type", "string", "enum", List.of("events", "start", "stop"),
                "description", "Subscription action"));
        props.put("channels", Map.of(
                "type", "array", "items", Map.of("type", "string"),
                "description", "Channels to subscribe: ai_data, alarm_state (for start, default both)"));
        props.put("channel", Map.of(
                "type", "string",
                "description", "Channel to read (for events, default ai_data)"));
        props.put("since_seq", Map.of(
                "type", "integer",
                "description", "Return events with _seq > since_seq (for events, default 0)"));
        props.put("limit", Map.of(
                "type", "integer",
                "description", "Max events to return (for events, default 50)"));

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
                    "Provide the target drone serial number.");
        }
        String action = getStringArg(args, PARAM_ACTION, "");
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action, "Valid actions: start, events, stop");
        }
        return switch (action) {
            case ACTION_START -> handleStart(session, sn, args);
            case ACTION_EVENTS -> handleEvents(session, sn, args);
            case ACTION_STOP -> handleStop(session, sn, args);
            default -> errorResult("Unhandled action: " + action, "This should not happen.");
        };
    }

    private Map<String, Object> handleStart(SessionContext session, String sn, Map<String, Object> args) {
        List<String> channels = resolveChannels(args);
        for (String ch : channels) {
            if (!VALID_CHANNELS.contains(ch)) {
                return errorResult("Invalid channel: " + ch, "Valid channels: " + VALID_CHANNELS);
            }
        }
        if (channels.contains(CHANNEL_ALARM_STATE)) {
            Map<String, Object> drcError = requireDrcLink(session, sn);
            if (drcError != null) {
                return drcError;
            }
        }

        boolean mock = sdkService.isMockMode();
        OnboardAiModule aiModule = null;
        if (!mock) {
            Drone drone = sdkService.getDrone(sn);
            if (drone == null) {
                return errorResult("Device instance not found: " + sn,
                        "Use fh_device_list to check device status.");
            }
            aiModule = drone.getOnboardAiModule();
        }

        Map<String, Integer> listenIds = new LinkedHashMap<>();
        for (String ch : channels) {
            int listenId = startChannel(aiModule, session, sn, ch);
            if (listenId < 0) {
                for (String started : listenIds.keySet()) {
                    cancelQuietly(session, registryKey(sn, started));
                }
                return errorResult("SDK refused subscription for channel " + ch + " (listenId=" + listenId + ")",
                        "Check device state/DRC link; no subscription was kept.");
            }
            listenIds.put(ch, listenId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        if (mock) {
            data.put("_mock", true);
        }
        data.put("listen_ids", listenIds);
        return successResult(data, (mock ? "Mock: " : "") + "AI subscriptions started for " + channels);
    }

    int startChannel(OnboardAiModule aiModule, SessionContext session, String sn, String channel) {
        String key = registryKey(sn, channel);
        cancelQuietly(session, key);
        String scope = AiSubscriptionBuffer.scope(session, sn);
        int listenId = listen(aiModule, scope, sn, channel);
        if (listenId < 0) {
            return listenId;
        }
        boolean mock = sdkService.isMockMode();
        session.registerListen(key, channel + ":" + sn, () -> {
            try {
                if (!mock) {
                    sdkCancel(aiModule, channel, listenId);
                }
            } finally {
                buffer.remove(scope, channel);
            }
        });
        return listenId;
    }

    int listen(OnboardAiModule aiModule, String scope, String sn, String channel) {
        if (sdkService.isMockMode()) {
            injectMockData(scope, channel);
            return MOCK_ID_GEN.incrementAndGet();
        }
        return sdkListen(aiModule, scope, channel);
    }

    @Generated
    private int sdkListen(OnboardAiModule aiModule, String scope, String channel) {
        return switch (channel) {
            case CHANNEL_AI_DATA -> aiModule.listenAIDataInfo(dataInfo ->
                    buffer.append(scope, CHANNEL_AI_DATA, AiCallbackMapper.mapAiDataInfo(dataInfo)));
            case CHANNEL_ALARM_STATE -> aiModule.listenDrcAIAlarmState(alarmInfo ->
                    buffer.append(scope, CHANNEL_ALARM_STATE, AiCallbackMapper.mapAlarmState(alarmInfo)));
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }

    @Generated
    private void sdkCancel(OnboardAiModule aiModule, String channel, int listenId) {
        switch (channel) {
            case CHANNEL_AI_DATA -> aiModule.cancelListenAIDataInfo(listenId);
            case CHANNEL_ALARM_STATE -> aiModule.cancelListenDrcAIAlarmState(listenId);
            default -> throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    private Map<String, Object> handleEvents(SessionContext session, String sn, Map<String, Object> args) {
        String channel = getStringArg(args, "channel", CHANNEL_AI_DATA);
        if (!VALID_CHANNELS.contains(channel)) {
            return errorResult("Invalid channel: " + channel, "Valid channels: " + VALID_CHANNELS);
        }
        long sinceSeq = getLongArg(args, "since_seq", 0);
        int limit = getIntArg(args, "limit", 50);

        AiSubscriptionBuffer.ReadResult readResult =
                buffer.read(AiSubscriptionBuffer.scope(session, sn), channel, sinceSeq, limit);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channel", channel);
        data.put("device_sn", sn);
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
            hint = "No events yet. Call again after the device starts pushing data.";
        } else {
            hint = "Use last_seq as since_seq in the next call for incremental reads.";
        }
        return successResult(data, hint);
    }

    private Map<String, Object> handleStop(SessionContext session, String sn, Map<String, Object> args) {
        List<String> channels = resolveChannels(args);
        for (String ch : channels) {
            if (!VALID_CHANNELS.contains(ch)) {
                return errorResult("Invalid channel: " + ch, "Valid channels: " + VALID_CHANNELS);
            }
        }
        List<String> stopped = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        for (String ch : channels) {
            try {
                if (session.cancelListen(registryKey(sn, ch))) {
                    stopped.add(ch);
                }
            } catch (RuntimeException e) {
                log.warn("Failed to cancel listen: sn={}, channel={}, error={}", sn, ch, e.getMessage());
                failed.add(ch);
            }
        }
        if (!failed.isEmpty()) {
            return errorResult("Failed to cancel SDK subscription for " + failed
                            + (stopped.isEmpty() ? "" : " (stopped: " + stopped + ")"),
                    "The local registration was removed; check device connectivity.");
        }
        String msg = stopped.isEmpty() ? "No active subscriptions to stop" : "Stopped: " + stopped;
        return successResult(Map.of("stopped", stopped), msg);
    }

    private void injectMockData(String scope, String channel) {
        if (CHANNEL_AI_DATA.equals(channel)) {
            buffer.append(scope, channel, Map.of(
                    "identify_on", 1,
                    "ai_model_list", List.of(Map.of("index", 0, "signed_name", "person")),
                    "selected_ai_model", Map.of("index", 0, "score", 80, "score_mode", 1),
                    "ai_models_config", List.of(),
                    "selected_ai_models_status", List.of(),
                    "ai_models_infos", List.of()));
        } else if (CHANNEL_ALARM_STATE.equals(channel)) {
            buffer.append(scope, channel, Map.of("alarm_on", false));
        }
    }

    private void cancelQuietly(SessionContext session, String key) {
        try {
            session.cancelListen(key);
        } catch (RuntimeException e) {
            log.warn("Failed to cancel listen: key={}, error={}", key, e.getMessage());
        }
    }

    private List<String> resolveChannels(Map<String, Object> args) {
        List<String> channels = getStringListArg(args, "channels");
        if (channels == null || channels.isEmpty()) {
            return new ArrayList<>(VALID_CHANNELS);
        }
        return channels;
    }

    private static String registryKey(String sn, String channel) {
        return "ai::" + sn + "::" + channel;
    }
}
