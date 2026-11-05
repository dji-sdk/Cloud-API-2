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

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthubsdk.bean.PsdkDeviceInfo;
import com.dji.flighthubsdk.bean.PsdkDeviceList;
import com.dji.flighthubsdk.bean.PsdkStateSearchlightInfo;
import com.dji.flighthubsdk.bean.PsdkStateSpeakerInfo;
import com.dji.flighthubsdk.bean.PsdkStateSummaryInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.PsdkControlModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * fh_psdk_subscribe — PSDK 负载状态订阅（只读）。
 *
 * <p>通过 {@code core.PsdkControlModule} 订阅 PSDK 设备列表与各类负载状态，
 * 提供统一的 listen → events → listen_stop 生命周期：
 * <ul>
 *   <li>listen_device_list — 订阅 PSDK 设备列表（设备级，无 psdk_index）</li>
 *   <li>listen_state_summary — 订阅指定 psdk_index 的状态概要</li>
 *   <li>listen_state_speaker — 订阅指定 psdk_index 的喊话器状态</li>
 *   <li>listen_state_searchlight — 订阅指定 psdk_index 的探照灯状态</li>
 *   <li>events — 按游标（since_seq/limit）轮询已订阅 channel 的缓冲事件</li>
 *   <li>listen_stop — 释放一个/多个/全部订阅</li>
 * </ul>
 *
 * <p>全部 action 均为只读订阅，安全等级 {@link SecurityLevel#READ_ONLY}，无需人工确认。
 * <p>作用域：{@code device_list} 以 drone_sn 为 scope；其余三个 channel 的
 * scope 编码为 {@code <droneSn>#<psdkIndex>}，同一无人机不同 psdk_index 的订阅互相独立。
 * <p>支持 mock 模式（FH_MOCK=true）与真实 SDK 双路径。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhPsdkSubscribeTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhPsdkSubscribeTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_psdk_subscribe";

    // ── 参数名常量 ──

    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_PSDK_INDEX = "psdk_index";
    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    // ── 订阅类 action 常量 ──

    private static final String ACTION_LISTEN_DEVICE_LIST = "listen_device_list";
    private static final String ACTION_LISTEN_STATE_SUMMARY = "listen_state_summary";
    private static final String ACTION_LISTEN_STATE_SPEAKER = "listen_state_speaker";
    private static final String ACTION_LISTEN_STATE_SEARCHLIGHT = "listen_state_searchlight";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_LISTEN_DEVICE_LIST, ACTION_LISTEN_STATE_SUMMARY,
            ACTION_LISTEN_STATE_SPEAKER, ACTION_LISTEN_STATE_SEARCHLIGHT,
            ACTION_EVENTS, ACTION_LISTEN_STOP);

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    // ── 订阅 channel 常量 ──

    /** PSDK 设备列表 channel（设备级，无 psdk_index） */
    private static final String CHANNEL_DEVICE_LIST = "device_list";

    /** PSDK 状态概要 channel（按 psdk_index 区分） */
    private static final String CHANNEL_STATE_SUMMARY = "state_summary";

    /** PSDK 喊话器状态 channel（按 psdk_index 区分） */
    private static final String CHANNEL_STATE_SPEAKER = "state_speaker";

    /** PSDK 探照灯状态 channel（按 psdk_index 区分） */
    private static final String CHANNEL_STATE_SEARCHLIGHT = "state_searchlight";

    /** 需要 psdk_index 的 channel 列表 */
    private static final List<String> INDEXED_CHANNELS = List.of(
            CHANNEL_STATE_SUMMARY, CHANNEL_STATE_SPEAKER, CHANNEL_STATE_SEARCHLIGHT);

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "psdk_subscribe";

    /** scopeId / 注册表 key 中 drone_sn 与 psdk_index 的分隔符 */
    private static final String SCOPE_SEPARATOR = "#";

    /** 注册表 key 中 scopeId 与 channel 的分隔符（与 ListenChannelSupport 保持一致） */
    private static final String KEY_SEPARATOR = "::";

    /** 非 0 号 psdk_index 的 stream key 后缀分隔符 */
    private static final String STREAM_INDEX_SEPARATOR = "@";

    // ── 默认值 / 上限常量 ──

    /** 默认 psdk_index */
    private static final int DEFAULT_PSDK_INDEX = 0;

    /** 设备级 channel 使用的 psdk_index 哨兵值（表示"无索引"） */
    private static final int PSDK_INDEX_NONE = -1;

    /** events 动作每个 channel 默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    // ── 事件字段名常量（重复使用的键） ──

    private static final String EVENT_KEY_PSDK_INDEX = "psdk_index";
    private static final String EVENT_KEY_DEVICE_INFOS = "device_infos";
    private static final String EVENT_KEY_MOCK = "_mock";

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    public FhPsdkSubscribeTool(SessionManager sessionManager, SdkService sdkService,
                               ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 全部 action 均为只读订阅，工具级安全等级为 {@link SecurityLevel#READ_ONLY}。
     *
     * @return READ_ONLY
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
    }

    /**
     * 只读订阅工具，不需要人工确认。
     *
     * @return false
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
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Read-only PSDK payload status subscription over core.PsdkControlModule. "
                + "Lifecycle: call one listen_* action to subscribe (returns a listen_id), then poll "
                + "with action=events (cursor: since_seq/limit), then release with action=listen_stop. "
                + "Channels: listen_device_list subscribes to the PSDK device list of a drone and takes "
                + "NO psdk_index (it is device-level); listen_state_summary, listen_state_speaker and "
                + "listen_state_searchlight subscribe to the state of one PSDK device and REQUIRE "
                + "psdk_index (default 0) - different psdk_index values on the same drone are "
                + "independent channels. No confirm is required; nothing here controls the aircraft.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "psdk_subscribe operation: "
                        + "listen_device_list (subscribe to the drone's PSDK device list; no psdk_index) | "
                        + "listen_state_summary (subscribe to state summary of psdk_index) | "
                        + "listen_state_speaker (subscribe to speaker state of psdk_index) | "
                        + "listen_state_searchlight (subscribe to searchlight state of psdk_index) | "
                        + "events (poll buffered events by since_seq cursor) | "
                        + "listen_stop (release one/many/all subscriptions)"
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Drone serial number (required; identifies which drone's PSDK devices to use)"
        ));
        props.put(PARAM_PSDK_INDEX, Map.of(
                "type", "integer",
                "description", "PSDK device index (default 0). Required to target a specific PSDK device for "
                        + "listen_state_summary/listen_state_speaker/listen_state_searchlight; ignored by "
                        + "listen_device_list. For events/listen_stop it is the default index used when a "
                        + "channel name is given without an '@<index>' suffix"
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Channel names for events/listen_stop (optional; omitted = all active channels "
                        + "for this drone_sn, across both the device-level and per-index scopes). Use "
                        + "'device_list' for the device-level channel, and 'state_summary', "
                        + "'state_speaker' or 'state_searchlight' for psdk_index=0 (or resolved from "
                        + "psdk_index). For a non-zero index use '<channel>@<psdk_index>', e.g. "
                        + "'state_summary@1'"
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
        schema.put("required", List.of(PARAM_ACTION, PARAM_DRONE_SN));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行订阅操作。
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

        // 2. 校验 drone_sn
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide the drone serial number. Use fh_device_list to get available drone SNs.");
        }

        // 3. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action",
                    "Valid actions: " + VALID_ACTIONS);
        }

        // 4. 全部 action 均为 READ_ONLY，无需安全等级硬拦截，直接分发
        return dispatch(action, session, args, droneSn);
    }

    /**
     * 按 action 分发订阅操作。
     *
     * @param action  操作名
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> dispatch(String action, SessionContext session,
                                         Map<String, Object> args, String droneSn) {
        return switch (action) {
            case ACTION_LISTEN_DEVICE_LIST, ACTION_LISTEN_STATE_SUMMARY,
                    ACTION_LISTEN_STATE_SPEAKER, ACTION_LISTEN_STATE_SEARCHLIGHT ->
                    handleListen(session, args, droneSn, action);
            case ACTION_EVENTS -> handleEvents(session, args, droneSn);
            case ACTION_LISTEN_STOP -> handleListenStop(session, args, droneSn);
            default -> errorResult("Unknown action: " + action, null);
        };
    }

    // ==================== listen_* ====================

    /**
     * 处理 listen_* 动作：订阅一个 channel 并返回 listen_id。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @param action  listen_* action 名
     * @return 执行结果
     */
    private Map<String, Object> handleListen(SessionContext session, Map<String, Object> args,
                                             String droneSn, String action) {
        int psdkIndex = getIntArg(args, PARAM_PSDK_INDEX, DEFAULT_PSDK_INDEX);
        String channel = channelOfListenAction(action);
        if (channel == null) {
            return errorResult("Unknown listen action: " + action, null);
        }

        // device_list 是设备级订阅，SDK 方法不接受 psdk_index
        ChannelRef ref;
        if (CHANNEL_DEVICE_LIST.equals(channel)) {
            ref = new ChannelRef(CHANNEL_DEVICE_LIST, PSDK_INDEX_NONE);
        } else {
            if (psdkIndex < 0) {
                return errorResult("Invalid parameter: psdk_index must be >= 0",
                        "Current value: " + psdkIndex);
            }
            ref = new ChannelRef(channel, psdkIndex);
        }

        int listenId = listenSupport.startChannel(session,
                ref.registryKey(droneSn), ref.streamKey(), ref.scope(session, droneSn), ref.streamKey(),
                sdkService.isMockMode(),
                () -> mockEventFor(ref),
                onEvent -> startRealListen(droneSn, ref, onEvent),
                id -> cancelRealListen(droneSn, ref, id));

        if (listenId < 0) {
            return errorResult("Failed to subscribe channel '" + ref.streamKey() + "': drone_sn=" + droneSn,
                    "Ensure the drone is connected, the SDK is initialized and psdk_index is valid.");
        }

        log.info("[{}] {}: droneSn={}, channel={}, psdkIndex={}, listenId={}",
                TOOL_NAME, action, droneSn, ref.streamKey(), ref.psdkIndex(), listenId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", action);
        data.put("channel", ref.streamKey());
        data.put(EVENT_KEY_PSDK_INDEX, ref.psdkIndex());
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to channel '" + ref.streamKey()
                + "'. Poll with action=events.");
    }

    /**
     * listen_* action 对应的基础 channel 名。
     *
     * @param action listen_* action 名
     * @return channel 名；未知 action 返回 null
     */
    private String channelOfListenAction(String action) {
        return switch (action) {
            case ACTION_LISTEN_DEVICE_LIST -> CHANNEL_DEVICE_LIST;
            case ACTION_LISTEN_STATE_SUMMARY -> CHANNEL_STATE_SUMMARY;
            case ACTION_LISTEN_STATE_SPEAKER -> CHANNEL_STATE_SPEAKER;
            case ACTION_LISTEN_STATE_SEARCHLIGHT -> CHANNEL_STATE_SEARCHLIGHT;
            default -> null;
        };
    }

    // ==================== events ====================

    /**
     * 处理 events：按游标轮询一个或多个 channel 的缓冲事件。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> handleEvents(SessionContext session, Map<String, Object> args,
                                             String droneSn) {
        List<ChannelRef> refs = resolveChannelRefs(session, args, droneSn);
        if (refs == null) {
            return invalidChannelError(args);
        }
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

    // ==================== listen_stop ====================

    /**
     * 处理 listen_stop：释放一个/多个/全部已订阅的 channel。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return 执行结果
     */
    private Map<String, Object> handleListenStop(SessionContext session, Map<String, Object> args,
                                                  String droneSn) {
        List<ChannelRef> refs = resolveChannelRefs(session, args, droneSn);
        if (refs == null) {
            return invalidChannelError(args);
        }

        Map<String, ChannelRef> refByStreamKey = new LinkedHashMap<>();
        for (ChannelRef ref : refs) {
            refByStreamKey.putIfAbsent(ref.streamKey(), ref);
        }

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session,
                new ArrayList<>(refByStreamKey.keySet()),
                streamKey -> refByStreamKey.get(streamKey).registryKey(droneSn), log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s).");
    }

    // ==================== channel 解析 ====================

    /**
     * 解析 events/listen_stop 的目标 channel：
     * 显式传入 channels 则逐个解析，否则枚举该无人机下全部已激活 channel
     * （同时覆盖设备级 scope 与 per-index scope）。
     *
     * @param session 当前 session
     * @param args    输入参数
     * @param droneSn 无人机 SN
     * @return channel 引用列表；含非法 channel 名时返回 null
     */
    private List<ChannelRef> resolveChannelRefs(SessionContext session, Map<String, Object> args,
                                                 String droneSn) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested == null || requested.isEmpty()) {
            return activeChannelRefs(session, droneSn);
        }

        int defaultIndex = getIntArg(args, PARAM_PSDK_INDEX, DEFAULT_PSDK_INDEX);
        List<ChannelRef> refs = new ArrayList<>(requested.size());
        for (String token : requested) {
            ChannelRef ref = ChannelRef.parse(token, defaultIndex);
            if (ref == null) {
                return null;
            }
            refs.add(ref);
        }
        return refs;
    }

    /**
     * 列出该无人机下所有活跃的 channel 引用。
     *
     * <p>同时扫描两种 scope 前缀：
     * {@code psdk_subscribe::<droneSn>::}（设备级，仅 device_list）与
     * {@code psdk_subscribe::<droneSn>#}（per-index，token 形如 {@code <index>::<channel>}）。
     *
     * @param session 当前 session
     * @param droneSn 无人机 SN
     * @return channel 引用列表
     */
    private List<ChannelRef> activeChannelRefs(SessionContext session, String droneSn) {
        List<ChannelRef> refs = new ArrayList<>();
        for (int i = 0; i < listenSupport.activeChannels(session, deviceScopePrefix(droneSn)).size(); i++) {
            refs.add(new ChannelRef(CHANNEL_DEVICE_LIST, PSDK_INDEX_NONE));
        }
        for (String token : listenSupport.activeChannels(session, indexScopePrefix(droneSn))) {
            ChannelRef ref = ChannelRef.parseRegistryToken(token);
            if (ref != null) {
                refs.add(ref);
            }
        }
        return refs;
    }

    /**
     * 设备级 scope 的注册表 key 前缀（形如 {@code psdk_subscribe::<droneSn>::}）。
     *
     * @param droneSn 无人机 SN
     * @return 前缀
     */
    private String deviceScopePrefix(String droneSn) {
        return SUBSCRIBE_NAMESPACE + KEY_SEPARATOR + droneSn + KEY_SEPARATOR;
    }

    /**
     * per-index scope 的注册表 key 前缀（形如 {@code psdk_subscribe::<droneSn>#}）。
     *
     * @param droneSn 无人机 SN
     * @return 前缀
     */
    private String indexScopePrefix(String droneSn) {
        return SUBSCRIBE_NAMESPACE + KEY_SEPARATOR + droneSn + SCOPE_SEPARATOR;
    }

    /**
     * 非法 channel 名的统一错误响应。
     *
     * @param args 输入参数
     * @return 错误响应
     */
    private Map<String, Object> invalidChannelError(Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        return errorResult("Invalid channel: " + requested,
                "Valid channels: " + CHANNEL_DEVICE_LIST + " or " + INDEXED_CHANNELS
                        + " (append '@<psdk_index>' for a non-zero index)");
    }

    // ==================== Mock / 真实 SDK ====================

    /**
     * 按 channel 生成一条代表性 mock 事件。
     *
     * @param ref channel 引用
     * @return mock 事件
     */
    private Map<String, Object> mockEventFor(ChannelRef ref) {
        return switch (ref.channel()) {
            case CHANNEL_DEVICE_LIST -> mockDeviceListEvent();
            case CHANNEL_STATE_SUMMARY -> mockStateSummaryEvent(ref.psdkIndex());
            case CHANNEL_STATE_SPEAKER -> mockStateSpeakerEvent();
            case CHANNEL_STATE_SEARCHLIGHT -> mockStateSearchlightEvent();
            default -> Map.of();
        };
    }

    /**
     * Mock — PSDK 设备列表事件。
     *
     * @return mock 事件
     */
    private Map<String, Object> mockDeviceListEvent() {
        Map<String, Object> device = new LinkedHashMap<>();
        device.put(EVENT_KEY_PSDK_INDEX, DEFAULT_PSDK_INDEX);
        device.put("object_key", "mock-key-0");

        Map<String, Object> event = new LinkedHashMap<>();
        event.put(EVENT_KEY_DEVICE_INFOS, List.of(device));
        event.put(EVENT_KEY_MOCK, true);
        return event;
    }

    /**
     * Mock — PSDK 状态概要事件。
     *
     * @param psdkIndex PSDK 设备索引
     * @return mock 事件
     */
    private Map<String, Object> mockStateSummaryEvent(int psdkIndex) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put(EVENT_KEY_PSDK_INDEX, psdkIndex);
        event.put("psdk_type", "mock");
        event.put("psdk_sub_type", "mock");
        event.put("psdk_name", "mock-psdk");
        event.put("psdk_sn", "mock-sn");
        event.put("psdk_version", "1.0");
        event.put("psdk_lib_version", "1.0");
        event.put("psdk_restrict_state", 1);
        event.put(EVENT_KEY_MOCK, true);
        return event;
    }

    /**
     * Mock — PSDK 喊话器状态事件。
     *
     * @return mock 事件
     */
    private Map<String, Object> mockStateSpeakerEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("work_mode", 0);
        event.put("play_mode", 0);
        event.put("system_state", 0);
        event.put("play_volume", 0);
        event.put("play_file_name", "mock.wav");
        event.put("play_file_md5", "mockmd5");
        event.put("tts_volume", 0);
        event.put("tts_type", 0);
        event.put("tts_language", 0);
        event.put("tts_speed", 0);
        event.put(EVENT_KEY_MOCK, true);
        return event;
    }

    /**
     * Mock — PSDK 探照灯状态事件。
     *
     * @return mock 事件
     */
    private Map<String, Object> mockStateSearchlightEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("work_mode", "mock");
        event.put("brightness", 0);
        event.put("calibration_status", 0);
        event.put("calibration_progress", 0);
        event.put("left_value", 0.0);
        event.put("right_value", 0.0);
        event.put("wide_field_mode", 0);
        event.put("light_gimbal_control", 0);
        event.put("lighting_type", 0);
        event.put(EVENT_KEY_MOCK, true);
        return event;
    }

    /**
     * 真实 SDK listen：按 channel 调用对应的 listenDrcPsdk* 方法。
     *
     * <p>per-index channel 的 psdkIndex 在闭包中捕获，供 cancel 时复用。
     *
     * @param droneSn 无人机 SN
     * @param ref     channel 引用
     * @param onEvent 事件消费回调
     * @return SDK listenId；模块不可用时返回 -1
     */
    private int startRealListen(String droneSn, ChannelRef ref, Consumer<Map<String, Object>> onEvent) {
        PsdkControlModule pcm = getPsdkModule(droneSn);
        if (pcm == null) {
            return -1;
        }
        return switch (ref.channel()) {
            case CHANNEL_DEVICE_LIST -> pcm.listenDrcPsdkDeviceList(
                    list -> onEvent.accept(toMap(list)));
            case CHANNEL_STATE_SUMMARY -> pcm.listenDrcPsdkStateSummaryInfo(ref.psdkIndex(),
                    info -> onEvent.accept(toMap(info)));
            case CHANNEL_STATE_SPEAKER -> pcm.listenDrcPsdkStateSpeakerInfo(ref.psdkIndex(),
                    info -> onEvent.accept(toMap(info)));
            case CHANNEL_STATE_SEARCHLIGHT -> pcm.listenDrcPsdkStateSearchlightInfo(ref.psdkIndex(),
                    info -> onEvent.accept(toMap(info)));
            default -> -1;
        };
    }

    /**
     * 真实 SDK cancel：按 channel 调用对应的 cancelListenDrcPsdk* 方法。
     *
     * <p>device_list 的 cancel 为单参；其余三个 channel 的 cancel 为
     * {@code (psdkIndex, listenId)} 两参，必须与 listen 时一致。
     *
     * @param droneSn  无人机 SN
     * @param ref      channel 引用
     * @param listenId listen 返回的监听 ID
     */
    private void cancelRealListen(String droneSn, ChannelRef ref, int listenId) {
        PsdkControlModule pcm = getPsdkModule(droneSn);
        if (pcm == null) {
            return;
        }
        switch (ref.channel()) {
            case CHANNEL_DEVICE_LIST -> pcm.cancelListenDrcPsdkDeviceList(listenId);
            case CHANNEL_STATE_SUMMARY -> pcm.cancelListenDrcPsdkStateSummaryInfo(ref.psdkIndex(), listenId);
            case CHANNEL_STATE_SPEAKER -> pcm.cancelListenDrcPsdkStateSpeakerInfo(ref.psdkIndex(), listenId);
            case CHANNEL_STATE_SEARCHLIGHT ->
                    pcm.cancelListenDrcPsdkStateSearchlightInfo(ref.psdkIndex(), listenId);
            default -> { }
        }
    }

    /**
     * 将 PsdkDeviceList 转换为事件 Map。
     *
     * @param list SDK 回调对象
     * @return 事件 Map
     */
    private Map<String, Object> toMap(PsdkDeviceList list) {
        List<Map<String, Object>> deviceInfos = new ArrayList<>();
        if (list != null && list.getDeviceInfos() != null) {
            for (PsdkDeviceInfo info : list.getDeviceInfos()) {
                if (info == null) {
                    continue;
                }
                Map<String, Object> device = new LinkedHashMap<>();
                device.put(EVENT_KEY_PSDK_INDEX, info.getPsdkIndex());
                device.put("object_key", info.getObjectKey());
                deviceInfos.add(device);
            }
        }
        Map<String, Object> event = new LinkedHashMap<>();
        event.put(EVENT_KEY_DEVICE_INFOS, deviceInfos);
        return event;
    }

    /**
     * 将 PsdkStateSummaryInfo 转换为事件 Map。
     *
     * @param info SDK 回调对象
     * @return 事件 Map
     */
    private Map<String, Object> toMap(PsdkStateSummaryInfo info) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put(EVENT_KEY_PSDK_INDEX, info.getPsdkIndex());
        event.put("psdk_type", String.valueOf(info.getPsdkType()));
        event.put("psdk_sub_type", String.valueOf(info.getPsdkSubType()));
        event.put("psdk_name", info.getPsdkName());
        event.put("psdk_sn", info.getPsdkSn());
        event.put("psdk_version", info.getPsdkVersion());
        event.put("psdk_lib_version", info.getPsdkLibVersion());
        event.put("psdk_restrict_state", info.getPsdkRestrictState());
        return event;
    }

    /**
     * 将 PsdkStateSpeakerInfo 转换为事件 Map。
     *
     * @param info SDK 回调对象
     * @return 事件 Map
     */
    private Map<String, Object> toMap(PsdkStateSpeakerInfo info) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("work_mode", info.getWorkMode());
        event.put("play_mode", info.getPlayMode());
        event.put("system_state", info.getSystemState());
        event.put("play_volume", info.getPlayVolume());
        event.put("play_file_name", info.getPlayFileName());
        event.put("play_file_md5", info.getPlayFileMd5());
        event.put("tts_volume", info.getTtsVolume());
        event.put("tts_type", info.getTtsType());
        event.put("tts_language", info.getTtsLanguage());
        event.put("tts_speed", info.getTtsSpeed());
        return event;
    }

    /**
     * 将 PsdkStateSearchlightInfo 转换为事件 Map。
     *
     * @param info SDK 回调对象
     * @return 事件 Map
     */
    private Map<String, Object> toMap(PsdkStateSearchlightInfo info) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("work_mode", String.valueOf(info.getWorkMode()));
        event.put("brightness", info.getBrightness());
        event.put("calibration_status", info.getCalibrationStatus());
        event.put("calibration_progress", info.getCalibrationProgress());
        event.put("left_value", info.getLeftValue());
        event.put("right_value", info.getRightValue());
        event.put("wide_field_mode", info.getWideFieldMode());
        event.put("light_gimbal_control", info.getLightGimbalControl());
        event.put("lighting_type", info.getLightingType());
        return event;
    }

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

    // ==================== channel 引用 ====================

    /**
     * 一个订阅 channel 的引用：基础 channel 名 + PSDK 设备索引。
     *
     * <p>{@code device_list} 为设备级订阅，使用哨兵 {@link #PSDK_INDEX_NONE}，
     * scopeId 即 drone_sn；其余三个 channel 的 scopeId 编码为
     * {@code <droneSn>#<psdkIndex>}，不同索引互相独立。
     * 默认索引（0）的 stream key 就是 channel 名，非 0 索引追加
     * {@code @<psdkIndex>} 后缀，使 events 响应中的 stream 键唯一且可直接用于
     * listen_stop。
     */
    private record ChannelRef(String channel, int psdkIndex) {

        /**
         * 是否为设备级 channel（无 psdk_index）。
         *
         * @return true 表示设备级
         */
        boolean deviceLevel() {
            return psdkIndex == PSDK_INDEX_NONE;
        }

        /**
         * 对外的 stream key：设备级或 0 号索引返回 channel 名，非 0 索引追加后缀。
         *
         * @return stream key
         */
        String streamKey() {
            if (deviceLevel() || psdkIndex == DEFAULT_PSDK_INDEX) {
                return channel;
            }
            return channel + STREAM_INDEX_SEPARATOR + psdkIndex;
        }

        /**
         * scopeId：设备级为 drone_sn，否则为 {@code <droneSn>#<psdkIndex>}。
         *
         * @param droneSn 无人机 SN
         * @return scopeId
         */
        String scopeId(String droneSn) {
            return deviceLevel() ? droneSn : droneSn + SCOPE_SEPARATOR + psdkIndex;
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
         * 解析对外的 channel 名。
         *
         * <p>支持两种写法：
         * <ul>
         *   <li>{@code device_list} — 设备级 channel，忽略 defaultIndex</li>
         *   <li>{@code <channel>} / {@code <channel>@<n>} — per-index channel；
         *       无后缀时使用 defaultIndex</li>
         * </ul>
         *
         * @param token        调用方传入的 channel 名
         * @param defaultIndex 无 {@code @<n>} 后缀时使用的 psdk_index
         * @return 引用；名称非法时返回 null
         */
        static ChannelRef parse(String token, int defaultIndex) {
            if (token == null) {
                return null;
            }
            if (CHANNEL_DEVICE_LIST.equals(token)) {
                return new ChannelRef(CHANNEL_DEVICE_LIST, PSDK_INDEX_NONE);
            }
            int sep = token.lastIndexOf(STREAM_INDEX_SEPARATOR);
            String channel = sep < 0 ? token : token.substring(0, sep);
            if (!INDEXED_CHANNELS.contains(channel)) {
                return null;
            }
            if (sep < 0) {
                return defaultIndex < 0 ? null : new ChannelRef(channel, defaultIndex);
            }
            try {
                int index = Integer.parseInt(token.substring(sep + 1));
                return index < 0 ? null : new ChannelRef(channel, index);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        /**
         * 解析 {@link ListenChannelSupport#activeChannels} 返回的 per-index token
         * （{@code <psdkIndex>::<channel>}）。
         *
         * @param token 去掉前缀后的注册表 key 后缀
         * @return 引用；格式不符时返回 null
         */
        static ChannelRef parseRegistryToken(String token) {
            if (token == null) {
                return null;
            }
            int sep = token.indexOf(KEY_SEPARATOR);
            if (sep < 0) {
                return null;
            }
            String channel = token.substring(sep + KEY_SEPARATOR.length());
            if (!INDEXED_CHANNELS.contains(channel)) {
                return null;
            }
            try {
                int index = Integer.parseInt(token.substring(0, sep));
                return index < 0 ? null : new ChannelRef(channel, index);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
