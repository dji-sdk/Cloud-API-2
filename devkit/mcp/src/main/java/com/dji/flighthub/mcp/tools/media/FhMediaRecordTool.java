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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveRecordItem;
import com.dji.flighthubsdk.bean.LiveRecordStateInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveRecordModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * fh_media_record — 直播录制控制：开始/停止录制或列出录制记录。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：混合
 * <ul>
 *   <li>start/stop — L1（影响录制状态，需 confirm）</li>
 *   <li>list — L0（只读，合并在同一工具）</li>
 *   <li>listen_state / events / listen_stop — L0（录制状态变更订阅三元组，只读）</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhMediaRecordTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhMediaRecordTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_media_record";

    /** 参数名常量 */
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";
    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    /** 操作常量 */
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_LIST = "list";
    private static final String ACTION_LISTEN_STATE = "listen_state";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_START, ACTION_STOP, ACTION_LIST,
            ACTION_LISTEN_STATE, ACTION_EVENTS, ACTION_LISTEN_STOP
    );

    /** 默认 payload 索引（与 FhLiveStartTool 一致） */
    private static final String DEFAULT_PAYLOAD_INDEX = "81-0-0";

    /** 订阅 channel 常量：录制状态变更（本工具只有这一个 channel） */
    private static final String CHANNEL_RECORD_STATE = "record_state";

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "media_record";

    /** events 动作默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    public FhMediaRecordTool(SessionManager sessionManager, SdkService sdkService,
                              ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 工具级标志返回 false：list 操作是只读的，不应被传输层硬拦截。
     * start/stop 的确认在 execute() 内部通过 requireHumanConfirm() 实现。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    /**
     * 返回本工具的 MCP 协议定义。
     *
     * @return MCP 工具定义 Map
     */
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
                "Control live stream recording: start/stop recording, "
                + "or list recorded sessions for a device. "
                + "IMPORTANT: This records the LIVE STREAM (server-side storage), NOT onboard SD card. "
                + "fh_live_start MUST be called first to start the live stream before recording. "
                + "For onboard SD card recording, use fh_camera(camera_video_start) instead. "
                + "Subscription actions for the record_state channel: listen_state (subscribe once), "
                + "events (poll buffered events via a since_seq cursor), "
                + "listen_stop (release the subscription).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Device serial number (dock or drone)"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "start: begin recording | stop: end recording | list: show recording history | "
                        + "listen_state: subscribe to record-state change events (channel: record_state) | "
                        + "events: poll buffered events for the record_state channel via since_seq cursor | "
                        + "listen_stop: unsubscribe the record_state channel"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Required for start/stop actions (not needed for list/listen_state/events/listen_stop)"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Camera payload index (e.g. '81-0-0'), default: primary camera. "
                        + "Use fh_camera(get_camera_state) to discover the actual value."
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string", "enum", List.of(CHANNEL_RECORD_STATE)),
                "description", "Channel names for events/listen_stop (optional; defaults to all "
                        + "currently active channels for this device when omitted)"
        ));
        props.put(PARAM_SINCE_SEQ, Map.of(
                "type", "integer",
                "description", "Cursor for events: only return events with _seq greater than this value (default 0)"
        ));
        props.put(PARAM_LIMIT, Map.of(
                "type", "integer",
                "description", "Max events to return per channel for events action (default "
                        + DEFAULT_EVENTS_LIMIT + ")"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行直播录制控制操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 校验必填参数
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device_sn. Use fh_device_list to find available devices.");
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 4. 订阅类 action（READ_ONLY，不进入 start/stop 确认流程）单独分发
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(action, deviceSn, args);
        }

        // 5. start/stop 需要安全等级 L1 + 人工确认；list 不需要
        if (!ACTION_LIST.equals(action)) {
            Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
            if (denied != null) {
                return denied;
            }
            Map<String, Object> confirmError = requireHumanConfirm(args,
                    "media_record_" + action, deviceSn,
                    buildConfirmMessage(deviceSn, action));
            if (confirmError != null) {
                return confirmError;
            }
        }

        // 6. 获取 payload_index 参数（null 表示用户未传，后续自动发现）
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);

        // 7. 分发到对应路径
        return sdkService.isMockMode()
                ? dispatchMockMode(deviceSn, action)
                : dispatchRealSdk(deviceSn, action, payloadIndex);
    }

    /**
     * Mock 模式分发。
     */
    private Map<String, Object> dispatchMockMode(String deviceSn, String action) {
        return switch (action) {
            case ACTION_START -> startRecordingMock(deviceSn);
            case ACTION_STOP -> stopRecordingMock(deviceSn);
            case ACTION_LIST -> listRecordingsMock(deviceSn);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * Mock 模式 — 开始录制。
     */
    private Map<String, Object> startRecordingMock(String deviceSn) {
        String recordId = "rec_" + Instant.now().getEpochSecond();
        log.info("[MOCK] Live recording started: device={}, recordId={}", deviceSn, recordId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("action", ACTION_START);
        data.put("record_id", recordId);
        data.put("status", "recording");
        data.put("start_time", Instant.now().toString());
        data.put("_mock", true);

        return successResult(data, "Live recording started for device " + deviceSn + ".");
    }

    /**
     * Mock 模式 — 停止录制。
     */
    private Map<String, Object> stopRecordingMock(String deviceSn) {
        log.info("[MOCK] Live recording stopped: device={}", deviceSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("action", ACTION_STOP);
        data.put("status", "completed");
        data.put("_mock", true);

        return successResult(data, "Live recording stopped for device " + deviceSn + ".");
    }

    /**
     * Mock 模式 — 列出录制记录。
     */
    private Map<String, Object> listRecordingsMock(String deviceSn) {
        log.info("[MOCK] List recordings for device: {}", deviceSn);

        List<Map<String, Object>> records = new ArrayList<>();

        Map<String, Object> record1 = new LinkedHashMap<>();
        record1.put("record_id", "rec_001");
        record1.put("device_sn", deviceSn);
        record1.put("camera", DEFAULT_PAYLOAD_INDEX);
        record1.put("state", 0);
        record1.put("duration", 1800L);
        record1.put("created_by", "mock-user");
        records.add(record1);

        Map<String, Object> record2 = new LinkedHashMap<>();
        record2.put("record_id", "rec_002");
        record2.put("device_sn", deviceSn);
        record2.put("camera", DEFAULT_PAYLOAD_INDEX);
        record2.put("state", 0);
        record2.put("duration", 900L);
        record2.put("created_by", "mock-user");
        records.add(record2);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("records", records);
        data.put("total_count", records.size());
        data.put("_mock", true);

        return successResult(data, "Found " + records.size() + " recordings for device " + deviceSn + ".");
    }

    /**
     * 真实 SDK 模式分发。
     *
     * @param deviceSn     目标设备 SN
     * @param action       操作类型
     * @param payloadIndex 相机负载索引，null 时自动发现
     */
    @Generated
    private Map<String, Object> dispatchRealSdk(String deviceSn, String action, String payloadIndex) {
        try {
            Drone drone = sdkService.getDrone(deviceSn);
            if (drone == null) {
                return errorResult("Device not found: " + deviceSn,
                        "Cannot get drone instance. Verify device_sn with fh_device_list.");
            }

            // 用户未指定 payload_index 时，自动从设备发现可用相机
            String resolvedIndex = payloadIndex;
            if (resolvedIndex == null) {
                resolvedIndex = sdkService.resolvePayloadIndex(drone);
                if (resolvedIndex == null) {
                    resolvedIndex = DEFAULT_PAYLOAD_INDEX;
                    log.warn("Auto-discover payload_index failed, fallback to default: {}", resolvedIndex);
                } else {
                    log.info("Auto-discovered payload_index: {}", resolvedIndex);
                }
            }

            // 构建 LiveInfo 用于获取 LiveRecordModule
            LiveInfo liveInfo = new LiveInfo();
            liveInfo.setDeviceSn(deviceSn);
            liveInfo.setPayloadIndex(resolvedIndex);
            liveInfo.setVideoId(deviceSn + "/" + resolvedIndex);

            LiveRecordModule liveRecordModule = drone.getLiveRecord(liveInfo);
            if (liveRecordModule == null) {
                return errorResult("LiveRecordModule unavailable for device: " + deviceSn,
                        "Cannot get live record module. Ensure device is online and live stream is active. "
                        + "Call fh_live_start first.");
            }

            return switch (action) {
                case ACTION_START -> startRecordingReal(liveRecordModule, deviceSn);
                case ACTION_STOP -> stopRecordingReal(liveRecordModule, deviceSn);
                case ACTION_LIST -> listRecordingsReal(liveRecordModule, deviceSn);
                default -> errorResult("Unknown action: " + action, "");
            };
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_media_record SDK error for device {} action {}: {}",
                    deviceSn, action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check device connectivity and try again.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_media_record timeout for device {} action {}: {}",
                    deviceSn, action, e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Recording operation timed out. Check device connectivity.");
        } catch (Exception e) {
            log.error("fh_media_record error for device {} action {}: {}",
                    deviceSn, action, e.getMessage(), e);
            return errorResult("Failed: " + e.getMessage(),
                    "Check device connectivity and try again.");
        }
    }

    /**
     * 真实 SDK — 开始录制。
     */
    @Generated
    private Map<String, Object> startRecordingReal(LiveRecordModule module, String deviceSn) {
        String timezone = ZoneId.systemDefault().getId();

        CompletableFuture<Void> future = new CompletableFuture<>();
        module.startLiveRecord(timezone, (error) -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "startLiveRecord failed: " + error.getErrorCode()));
            }
        });
        SdkCallbackBridge.await(future);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("action", ACTION_START);
        data.put("status", "recording");
        data.put("start_time", Instant.now().toString());

        return successResult(data, "Live recording started for device " + deviceSn + ".");
    }

    /**
     * 真实 SDK — 停止录制。
     */
    @Generated
    private Map<String, Object> stopRecordingReal(LiveRecordModule module, String deviceSn) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        module.stopLiveRecord((error) -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "stopLiveRecord failed: " + error.getErrorCode()));
            }
        });
        SdkCallbackBridge.await(future);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("action", ACTION_STOP);
        data.put("status", "completed");

        return successResult(data, "Live recording stopped for device " + deviceSn + ".");
    }

    /**
     * 真实 SDK — 列出录制记录。
     */
    @Generated
    private Map<String, Object> listRecordingsReal(LiveRecordModule module, String deviceSn) {
        CompletableFuture<List<LiveRecordItem>> future = new CompletableFuture<>();
        module.getDeviceLiveRecords(deviceSn, (error, records) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "getDeviceLiveRecords failed: " + error.getErrorCode()));
                return;
            }
            future.complete(records != null ? records : List.of());
        });

        List<LiveRecordItem> records = SdkCallbackBridge.await(future);

        List<Map<String, Object>> recordList = new ArrayList<>();
        for (LiveRecordItem item : records) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("record_id", item.getBid());
            record.put("device_sn", deviceSn);
            record.put("camera", item.getCamera());
            record.put("state", item.getState());
            record.put("duration", item.getDuration());
            record.put("created_by", item.getCreatedBy());
            recordList.add(record);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("records", recordList);
        data.put("total_count", recordList.size());

        return successResult(data,
                "Found " + recordList.size() + " recordings for device " + deviceSn + ".");
    }

    // ── 订阅分发 ──

    /**
     * 判断是否为订阅类 action（listen_state/events/listen_stop）。
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_LISTEN_STATE.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

    /**
     * 订阅类 action 分发入口。
     *
     * @param action   操作名
     * @param deviceSn 目标设备 SN（订阅作用域按设备区分）
     * @param args     输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(String action, String deviceSn, Map<String, Object> args) {
        SessionContext session = requireSession();
        return switch (action) {
            case ACTION_LISTEN_STATE -> handleListenState(session, deviceSn);
            case ACTION_EVENTS -> handleEvents(session, deviceSn, args);
            case ACTION_LISTEN_STOP -> handleStopListen(session, deviceSn, args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 处理 listen_state：订阅 record_state channel，返回 listen_id。
     */
    private Map<String, Object> handleListenState(SessionContext session, String deviceSn) {
        String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, deviceSn, CHANNEL_RECORD_STATE);
        String scope = SubscriptionBuffer.scope(session, deviceSn);
        boolean mock = sdkService.isMockMode();

        int listenId = listenSupport.startChannel(session, registryKey, CHANNEL_RECORD_STATE, scope,
                CHANNEL_RECORD_STATE, mock,
                this::mockRecordStateEvent,
                onEvent -> startRealListenRecordState(deviceSn, onEvent),
                listenId2 -> cancelRealListenRecordState(deviceSn, listenId2));

        if (listenId < 0) {
            return errorResult("Failed to start listening on channel: " + CHANNEL_RECORD_STATE,
                    "Check SDK connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STATE);
        data.put("channel", CHANNEL_RECORD_STATE);
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to channel '" + CHANNEL_RECORD_STATE + "'. Poll with action=events.");
    }

    /**
     * 处理 events：按游标轮询一个或多个已订阅 channel 的缓冲事件。
     */
    private Map<String, Object> handleEvents(SessionContext session, String deviceSn, Map<String, Object> args) {
        List<String> channels = resolveChannels(session, deviceSn, args);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (String channel : channels) {
            String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, deviceSn, channel);
            String scope = SubscriptionBuffer.scope(session, deviceSn);
            streams.put(channel, listenSupport.readChannel(session, registryKey, scope, channel, sinceSeq, limit));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_EVENTS);
        data.put("streams", streams);
        return successResult(data, "Polled " + streams.size() + " channel(s).");
    }

    /**
     * 处理 listen_stop：停止一个/多个/全部已订阅的 channel。
     */
    private Map<String, Object> handleStopListen(SessionContext session, String deviceSn, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<String> channels = (requested != null && !requested.isEmpty())
                ? requested
                : listenSupport.activeChannels(session, subscribePrefix(deviceSn));

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, channels,
                channel -> ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, deviceSn, channel), log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s).");
    }

    /**
     * 解析 events 要轮询的 channel 列表：显式传入 channels 则用它，否则取当前全部已激活 channel。
     */
    private List<String> resolveChannels(SessionContext session, String deviceSn, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested != null && !requested.isEmpty()) {
            return requested;
        }
        return listenSupport.activeChannels(session, subscribePrefix(deviceSn));
    }

    /**
     * 本工具订阅注册表 key 的前缀（namespace::deviceSn::）。
     */
    private String subscribePrefix(String deviceSn) {
        return SUBSCRIBE_NAMESPACE + "::" + deviceSn + "::";
    }

    /**
     * Mock — 录制状态变更事件，字段镜像 LiveRecordStateInfo。
     */
    private Map<String, Object> mockRecordStateEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("bid", "mock_record_bid");
        event.put("sn", "mock_sn");
        event.put("camera", DEFAULT_PAYLOAD_INDEX);
        event.put("video", "mock_video_id");
        event.put("state", "recording");
        event.put("created_by", "mock-user");
        event.put("duration", 0L);
        event.put("create_time", Instant.now().toEpochMilli());
        event.put("_mock", true);
        return event;
    }

    /**
     * 按 dispatchRealSdk 中已验证的方式获取指定设备的 LiveRecordModule：
     * 自动发现（或回退默认）payload_index，构建 LiveInfo 后取模块。
     * 设备不存在时返回 null。
     */
    private LiveRecordModule resolveLiveRecordModule(String deviceSn) {
        Drone drone = sdkService.getDrone(deviceSn);
        if (drone == null) {
            return null;
        }

        String payloadIndex = sdkService.resolvePayloadIndex(drone);
        if (payloadIndex == null) {
            payloadIndex = DEFAULT_PAYLOAD_INDEX;
        }

        LiveInfo liveInfo = new LiveInfo();
        liveInfo.setDeviceSn(deviceSn);
        liveInfo.setPayloadIndex(payloadIndex);
        liveInfo.setVideoId(deviceSn + "/" + payloadIndex);

        return drone.getLiveRecord(liveInfo);
    }

    /**
     * 真实 SDK — 调用 LiveRecordModule.listenRecordState 订阅录制状态变更，返回 listenId；
     * 模块不可用时返回负数。
     */
    private int startRealListenRecordState(String deviceSn, Consumer<Map<String, Object>> onEvent) {
        LiveRecordModule module = resolveLiveRecordModule(deviceSn);
        if (module == null) {
            return -1;
        }
        return module.listenRecordState(msg -> onEvent.accept(toMap(msg)));
    }

    /**
     * 真实 SDK — 调用 LiveRecordModule.cancelListenRecordState 取消订阅。
     */
    private void cancelRealListenRecordState(String deviceSn, int listenId) {
        LiveRecordModule module = resolveLiveRecordModule(deviceSn);
        if (module == null) {
            return;
        }
        module.cancelListenRecordState(listenId);
    }

    /**
     * 将 LiveRecordStateInfo 转换为事件 Map。
     * state 为 SDK 枚举类型，统一转为字符串存储，避免非 JSON 可序列化问题。
     */
    private Map<String, Object> toMap(LiveRecordStateInfo msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("bid", msg.getBid());
        event.put("sn", msg.getSn());
        event.put("camera", msg.getCamera());
        event.put("video", msg.getVideo());
        event.put("state", String.valueOf(msg.getState()));
        event.put("created_by", msg.getCreatedBy());
        event.put("duration", msg.getDuration());
        event.put("create_time", msg.getCreateTime());
        return event;
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String deviceSn, String action) {
        return switch (action) {
            case ACTION_START -> String.format("即将开始录制设备 %s 的直播流，确认？", deviceSn);
            case ACTION_STOP -> String.format("即将停止设备 %s 的直播录制，确认？", deviceSn);
            default -> "确认执行操作？";
        };
    }
}
