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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthubsdk.core.DeployModule;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.generated.AircraftVisualScoutAbnormalInfo;
import com.dji.flighthubsdk.generated.AircraftVisualScoutObstacleInfo;
import com.dji.flighthubsdk.generated.AircraftVisualScoutStatus;
import com.dji.flighthubsdk.generated.EphemerisDownloadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * fh_deploy_subscribe — 订阅机场部署（DeployModule）相关事件。
 *
 * <p>覆盖 4 个只读订阅 channel，均挂在 {@code core.DeployModule} 上，按机场（dock）维度获取，
 * 不区分无人机：
 * <ul>
 *   <li>visual_scout_status — 进离场航线侦察任务状态变化</li>
 *   <li>visual_scout_obstacle — 踏勘任务障碍物信息</li>
 *   <li>visual_scout_abnormal — 踏勘任务异常信息</li>
 *   <li>ephemeris_download_state — 星历下载状态</li>
 * </ul>
 *
 * <p>安全等级：全部 READ_ONLY（L0），无需人工确认。
 * 使用 listen_xxx -> events -> listen_stop 三段式生命周期：
 * 先订阅 channel 得到 listen_id，再轮询 events 读取缓冲事件，最后 listen_stop 取消订阅。
 *
 * <p>注意：{@code generated.*} 回调载荷是 SWIG 原生对象，仅有 getter/setter，没有 bean 转换层，
 * 也没有可用的 {@code toString()}，不能直接存入缓冲区（会导致 JSON 序列化失败或泄漏 SWIG 内部状态），
 * 必须通过 {@link #toMap} 系列方法手工转换为纯 Map。
 *
 * <p>对应 SDK 模块：DeployModule（Dock.getDeployModule()，按机场获取，非按无人机获取）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDeploySubscribeTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDeploySubscribeTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_deploy_subscribe";

    // ── 参数名常量 ──

    private static final String PARAM_DOCK_SN = "dock_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    // ── 操作常量 ──

    private static final String ACTION_LISTEN_VISUAL_SCOUT_STATUS = "listen_visual_scout_status";
    private static final String ACTION_LISTEN_VISUAL_SCOUT_OBSTACLE = "listen_visual_scout_obstacle";
    private static final String ACTION_LISTEN_VISUAL_SCOUT_ABNORMAL = "listen_visual_scout_abnormal";
    private static final String ACTION_LISTEN_EPHEMERIS_DOWNLOAD_STATE = "listen_ephemeris_download_state";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_LISTEN_VISUAL_SCOUT_STATUS, ACTION_LISTEN_VISUAL_SCOUT_OBSTACLE,
            ACTION_LISTEN_VISUAL_SCOUT_ABNORMAL, ACTION_LISTEN_EPHEMERIS_DOWNLOAD_STATE,
            ACTION_EVENTS, ACTION_LISTEN_STOP
    );

    // ── 订阅 channel 常量 ──

    private static final String CHANNEL_VISUAL_SCOUT_STATUS = "visual_scout_status";
    private static final String CHANNEL_VISUAL_SCOUT_OBSTACLE = "visual_scout_obstacle";
    private static final String CHANNEL_VISUAL_SCOUT_ABNORMAL = "visual_scout_abnormal";
    private static final String CHANNEL_EPHEMERIS_DOWNLOAD_STATE = "ephemeris_download_state";

    /** 有效订阅 channel 列表 */
    private static final List<String> VALID_CHANNELS = List.of(
            CHANNEL_VISUAL_SCOUT_STATUS, CHANNEL_VISUAL_SCOUT_OBSTACLE,
            CHANNEL_VISUAL_SCOUT_ABNORMAL, CHANNEL_EPHEMERIS_DOWNLOAD_STATE
    );

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "deploy_subscribe";

    /** events 动作默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    /** Mock 踏勘任务 ID */
    private static final String MOCK_TASK_ID = "mock-task-001";

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    public FhDeploySubscribeTool(SessionManager sessionManager, SdkService sdkService,
                                  ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 本工具所有 action 均为 READ_ONLY 订阅操作，不应被传输层硬拦截。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    /**
     * 本工具整体安全等级为 L0（只读）。
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
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
                "Subscribe to dock-level DeployModule events (READ_ONLY, no confirmation needed): "
                + "visual scout (in/out wayline inspection) task status / obstacle info / abnormal info, "
                + "and aircraft ephemeris download state. Each channel follows the "
                + "listen_<channel> -> events -> listen_stop lifecycle: call listen_<channel> to subscribe "
                + "and get a listen_id, poll with action=events to read buffered events since a cursor, "
                + "then listen_stop to unsubscribe. Scope is per-dock (dock_sn), not per-drone. "
                + "Raw SWIG payloads are flattened into plain JSON-safe maps before being buffered.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DOCK_SN, Map.of(
                "type", "string",
                "description", "Target dock serial number. This tool is dock-scoped (not drone-scoped)."
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "listen_visual_scout_status: subscribe to in/out wayline scout task status changes | "
                        + "listen_visual_scout_obstacle: subscribe to scout task obstacle info | "
                        + "listen_visual_scout_abnormal: subscribe to scout task abnormal info | "
                        + "listen_ephemeris_download_state: subscribe to aircraft ephemeris download state | "
                        + "events: poll buffered events for one/all active channels of this dock since a cursor | "
                        + "listen_stop: unsubscribe one/all active channels of this dock"
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string", "enum", VALID_CHANNELS),
                "description", "Channel names for events/listen_stop (optional; defaults to all "
                        + "currently active channels of this dock when omitted)"
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
        schema.put("required", List.of(PARAM_DOCK_SN, PARAM_ACTION));
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
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String dockSn = getStringArg(args, PARAM_DOCK_SN, null);
        if (dockSn == null || dockSn.isBlank()) {
            return errorResult("Missing dock_sn", "Provide the dock serial number.");
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        return dispatchSubscription(session, dockSn, action, args);
    }

    // ── 订阅分发 ──

    /**
     * 订阅类 action 分发入口。
     *
     * @param session 当前 session
     * @param dockSn  机场 SN（作为订阅作用域标识）
     * @param action  操作名
     * @param args    输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(SessionContext session, String dockSn,
                                                       String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_LISTEN_VISUAL_SCOUT_STATUS ->
                    handleListen(session, dockSn, CHANNEL_VISUAL_SCOUT_STATUS, action);
            case ACTION_LISTEN_VISUAL_SCOUT_OBSTACLE ->
                    handleListen(session, dockSn, CHANNEL_VISUAL_SCOUT_OBSTACLE, action);
            case ACTION_LISTEN_VISUAL_SCOUT_ABNORMAL ->
                    handleListen(session, dockSn, CHANNEL_VISUAL_SCOUT_ABNORMAL, action);
            case ACTION_LISTEN_EPHEMERIS_DOWNLOAD_STATE ->
                    handleListen(session, dockSn, CHANNEL_EPHEMERIS_DOWNLOAD_STATE, action);
            case ACTION_EVENTS -> handleEvents(session, dockSn, args);
            case ACTION_LISTEN_STOP -> handleListenStop(session, dockSn, args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 处理 listen_xxx：订阅指定机场的某个 DeployModule channel，返回 listen_id。
     *
     * @param session 当前 session
     * @param dockSn  机场 SN
     * @param channel 订阅 channel 名
     * @param action  调用方传入的原始 action 名（回显到结果中）
     * @return 执行结果
     */
    private Map<String, Object> handleListen(SessionContext session, String dockSn, String channel, String action) {
        String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, dockSn, channel);
        String scope = SubscriptionBuffer.scope(session, dockSn);
        boolean mock = sdkService.isMockMode();

        int listenId = listenSupport.startChannel(session, registryKey, channel, scope, channel, mock,
                mockSupplierFor(channel),
                onEvent -> startRealListen(dockSn, channel, onEvent),
                listenId2 -> cancelRealListen(dockSn, channel, listenId2));

        if (listenId < 0) {
            return errorResult("Failed to start listening on channel: " + channel,
                    "Check SDK connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", action);
        data.put("channel", channel);
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to dock " + dockSn + " channel '" + channel
                + "'. Poll with action=events.");
    }

    /**
     * 处理 events：按游标轮询指定机场一个或多个已订阅 channel 的缓冲事件。
     */
    private Map<String, Object> handleEvents(SessionContext session, String dockSn, Map<String, Object> args) {
        List<String> channels = resolveChannels(session, dockSn, args);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (String channel : channels) {
            String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, dockSn, channel);
            String scope = SubscriptionBuffer.scope(session, dockSn);
            streams.put(channel, listenSupport.readChannel(session, registryKey, scope, channel, sinceSeq, limit));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_EVENTS);
        data.put("streams", streams);
        return successResult(data, "Polled " + streams.size() + " channel(s) for dock " + dockSn + ".");
    }

    /**
     * 处理 listen_stop：停止指定机场一个/多个/全部已订阅的 channel。
     */
    private Map<String, Object> handleListenStop(SessionContext session, String dockSn, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<String> channels = (requested != null && !requested.isEmpty())
                ? requested
                : listenSupport.activeChannels(session, subscribePrefix(dockSn));

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, channels,
                channel -> ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, dockSn, channel), log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s) for dock " + dockSn + ".");
    }

    /**
     * 解析 events 要轮询的 channel 列表：显式传入 channels 则用它，否则取该机场当前全部已激活 channel。
     */
    private List<String> resolveChannels(SessionContext session, String dockSn, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested != null && !requested.isEmpty()) {
            return requested;
        }
        return listenSupport.activeChannels(session, subscribePrefix(dockSn));
    }

    /**
     * 本工具订阅注册表 key 的前缀（namespace::dockSn::），用于按机场维度枚举已激活 channel。
     */
    private String subscribePrefix(String dockSn) {
        return SUBSCRIBE_NAMESPACE + "::" + dockSn + "::";
    }

    /**
     * 按 channel 选择对应的 mock 事件生成器。
     */
    private ListenChannelSupport.MockEventSupplier mockSupplierFor(String channel) {
        return switch (channel) {
            case CHANNEL_VISUAL_SCOUT_STATUS -> this::mockVisualScoutStatusEvent;
            case CHANNEL_VISUAL_SCOUT_OBSTACLE -> this::mockVisualScoutObstacleEvent;
            case CHANNEL_VISUAL_SCOUT_ABNORMAL -> this::mockVisualScoutAbnormalEvent;
            case CHANNEL_EPHEMERIS_DOWNLOAD_STATE -> this::mockEphemerisDownloadStateEvent;
            default -> Map::of;
        };
    }

    /**
     * 按 channel 调用真实 SDK 的 listen 方法，返回 listenId；Dock/DeployModule 不可用时返回 -1。
     */
    private int startRealListen(String dockSn, String channel, Consumer<Map<String, Object>> onEvent) {
        DeployModule deployModule = resolveDeployModule(dockSn);
        if (deployModule == null) {
            return -1;
        }
        return switch (channel) {
            case CHANNEL_VISUAL_SCOUT_STATUS -> deployModule.listenAircraftVisualScoutStatus(
                    msg -> onEvent.accept(toMap(msg)));
            case CHANNEL_VISUAL_SCOUT_OBSTACLE -> deployModule.listenAircraftVisualScoutObstacleInfo(
                    msg -> onEvent.accept(toMap(msg)));
            case CHANNEL_VISUAL_SCOUT_ABNORMAL -> deployModule.listenAircraftVisualScoutAbnormalInfo(
                    msg -> onEvent.accept(toMap(msg)));
            case CHANNEL_EPHEMERIS_DOWNLOAD_STATE -> deployModule.listenAircraftEphemerisDownloadState(
                    msg -> onEvent.accept(toMap(msg)));
            default -> -1;
        };
    }

    /**
     * 按 channel 调用真实 SDK 的 cancelListen 方法。
     */
    private void cancelRealListen(String dockSn, String channel, int listenId) {
        DeployModule deployModule = resolveDeployModule(dockSn);
        if (deployModule == null) {
            return;
        }
        switch (channel) {
            case CHANNEL_VISUAL_SCOUT_STATUS -> deployModule.cancelListenAircraftVisualScoutStatus(listenId);
            case CHANNEL_VISUAL_SCOUT_OBSTACLE -> deployModule.cancelListenAircraftVisualScoutObstacleInfo(listenId);
            case CHANNEL_VISUAL_SCOUT_ABNORMAL -> deployModule.cancelListenAircraftVisualScoutAbnormalInfo(listenId);
            case CHANNEL_EPHEMERIS_DOWNLOAD_STATE ->
                    deployModule.cancelListenAircraftEphemerisDownloadState(listenId);
            default -> { }
        }
    }

    /**
     * 按机场 SN 获取对应的 DeployModule 实例；机场不存在时返回 null。
     *
     * @param dockSn 机场 SN
     * @return DeployModule 实例，机场不存在返回 null
     */
    private DeployModule resolveDeployModule(String dockSn) {
        Dock dock = sdkService.getDock(dockSn);
        return dock != null ? dock.getDeployModule() : null;
    }

    // ── Mock 事件构造 ──

    /**
     * Mock — 进离场航线侦察任务状态变化事件。
     */
    private Map<String, Object> mockVisualScoutStatusEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("task_id", MOCK_TASK_ID);
        event.put("visual_scout_stage", 1);
        event.put("scout_wayline_generate_remain_time", 30L);
        event.put("_mock", true);
        return event;
    }

    /**
     * Mock — 踏勘任务障碍物信息事件。
     */
    private Map<String, Object> mockVisualScoutObstacleEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("task_id", MOCK_TASK_ID);
        event.put("obstacle_tag", true);
        event.put("_mock", true);
        return event;
    }

    /**
     * Mock — 踏勘任务异常信息事件。
     */
    private Map<String, Object> mockVisualScoutAbnormalEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("task_id", MOCK_TASK_ID);
        event.put("err_code", 0);
        event.put("_mock", true);
        return event;
    }

    /**
     * Mock — 星历下载状态事件。
     */
    private Map<String, Object> mockEphemerisDownloadStateEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("state", 1);
        event.put("_mock", true);
        return event;
    }

    // ── 真实 SDK 载荷转换 ──

    /**
     * 将 AircraftVisualScoutStatus（SWIG 原生对象）转换为纯 Map。
     */
    private Map<String, Object> toMap(AircraftVisualScoutStatus msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("task_id", msg.getTask_id());
        event.put("visual_scout_stage", msg.getVisual_scout_stage());
        event.put("scout_wayline_generate_remain_time", msg.getScout_wayline_generate_remain_time());
        return event;
    }

    /**
     * 将 AircraftVisualScoutObstacleInfo（SWIG 原生对象）转换为纯 Map。
     */
    private Map<String, Object> toMap(AircraftVisualScoutObstacleInfo msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("task_id", msg.getTask_id());
        event.put("obstacle_tag", msg.getObstacle_tag());
        return event;
    }

    /**
     * 将 AircraftVisualScoutAbnormalInfo（SWIG 原生对象）转换为纯 Map。
     */
    private Map<String, Object> toMap(AircraftVisualScoutAbnormalInfo msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("task_id", msg.getTask_id());
        event.put("err_code", msg.getErr_code());
        return event;
    }

    /**
     * 将 EphemerisDownloadState（SWIG 原生对象）转换为纯 Map。
     */
    private Map<String, Object> toMap(EphemerisDownloadState msg) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("state", msg.getState());
        return event;
    }
}
