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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.FlightHubCallback.ResultCallbackWithError;
import com.dji.flighthubsdk.bean.DockSupplementLightState;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.value.DockCameraPosition;
import com.dji.flighthubsdk.value.DockCoverMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * fh_dock_control — 远程控制机场设备。
 * <p>操作类型：cover_open / cover_close / charge_start / charge_stop / light_on / light_off / camera_position
 * <p>安全等级：L1（需要 confirm=true）
 * <p>另含订阅类三元组 listen_light_state / events / listen_stop（READ_ONLY，无需 confirm），
 * 用于按 dock_sn 订阅机场补光灯状态变化（channel: supplement_light_state）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDockControlTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDockControlTool.class);

    /** camera_position 动作名常量 */
    private static final String ACTION_CAMERA_POSITION = "camera_position";

    /** 订阅类三元组动作名常量：订阅补光灯状态变化 */
    private static final String ACTION_LISTEN_LIGHT_STATE = "listen_light_state";

    /** 订阅类三元组动作名常量：轮询缓冲事件 */
    private static final String ACTION_EVENTS = "events";

    /** 订阅类三元组动作名常量：停止订阅 */
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    private static final List<String> VALID_ACTIONS = List.of(
            "cover_open", "cover_close", "charge_start", "charge_stop", "light_on", "light_off",
            ACTION_CAMERA_POSITION, ACTION_LISTEN_LIGHT_STATE, ACTION_EVENTS, ACTION_LISTEN_STOP);

    /** camera_position 动作下 gateway_sn 参数名（机场作为直播网关的 SN） */
    private static final String PARAM_GATEWAY_SN = "gateway_sn";

    /** camera_position 动作下 camera_position 参数名 */
    private static final String PARAM_CAMERA_POSITION = "camera_position";

    /** camera_position 动作下可选的 drone_sn 参数名（用于填充 LiveInfo.device_sn） */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** camera_position 的合法取值，对应 SDK 枚举 DockCameraPosition（不含内部的 UNKNOWN） */
    private static final List<String> VALID_CAMERA_POSITIONS = List.of("inside", "outside", "aux2");

    // ── 订阅类三元组常量 ──

    /** events/listen_stop 动作下 channels 参数名（可选，省略则默认全部已激活 channel） */
    private static final String PARAM_CHANNELS = "channels";

    /** events 动作下 since_seq 参数名（游标） */
    private static final String PARAM_SINCE_SEQ = "since_seq";

    /** events 动作下 limit 参数名（单次最大返回条数） */
    private static final String PARAM_LIMIT = "limit";

    /** 补光灯状态变化事件 channel 名 */
    private static final String CHANNEL_SUPPLEMENT_LIGHT_STATE = "supplement_light_state";

    /** 有效订阅 channel 列表（本工具目前仅支持补光灯状态变化） */
    private static final List<String> VALID_CHANNELS = List.of(CHANNEL_SUPPLEMENT_LIGHT_STATE);

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "dock_control";

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
    public FhDockControlTool(SessionManager sessionManager, SdkService sdkService, ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 工具级标志返回 false：listen_light_state/events/listen_stop 是只读订阅操作，不应被传输层硬拦截。
     * cover_open 等控制动作的确认在 execute() 内部通过 requireHumanConfirm() 实现。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    /**
     * 机场控制全部是物理硬件操作（开关舱盖、充断电、补光灯），统一按 L1 注册。
     *
     * <p>等级同时决定工具级限流的档位：若不覆写，基类默认 L0（300 rpm），
     * 会让这些硬件操作拿到比设计配额表宽 10 倍的配额。
     *
     * @return L1
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_dock_control");
        def.put("description", "Remote control dock: open/close cover, start/stop charging, toggle supplement light, "
                + "or switch the dock gimbal camera position (action=camera_position); L1 operations require "
                + "confirm=true. Also supports subscribing to supplement-light-state change events per dock "
                + "(listen_light_state -> events -> listen_stop), which are READ_ONLY and need no confirmation.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("dock_sn", Map.of("type", "string", "description", "Target dock SN"));
        props.put("action", Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Control action. camera_position additionally requires gateway_sn and "
                        + "camera_position parameters. "
                        + "listen_light_state: subscribe to this dock's supplement-light-state change channel | "
                        + "events: poll buffered events for one/all active channels of this dock since a cursor | "
                        + "listen_stop: unsubscribe one/all active channels of this dock"
        ));
        props.put("confirm", Map.of("type", "boolean",
                "description", "Must be true to execute cover_open/cover_close/charge_start/charge_stop/"
                        + "light_on/light_off/camera_position. Not required for listen_light_state/events/listen_stop."));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        props.put(PARAM_GATEWAY_SN, Map.of(
                "type", "string",
                "description", "Dock's gateway serial number. Required when action=camera_position; "
                        + "this is the dock acting as the live-stream gateway for the camera position switch."
        ));
        props.put(PARAM_CAMERA_POSITION, Map.of(
                "type", "string",
                "enum", VALID_CAMERA_POSITIONS,
                "description", "Target dock gimbal camera position. Required when action=camera_position. "
                        + "NOTE: this switches the dock's own gimbal camera lens position, NOT the drone's live "
                        + "stream payload source (use fh_live_start action=switch_source for that instead)."
        ));
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Optional drone serial number, used to populate LiveInfo.device_sn when "
                        + "action=camera_position. Omit if not applicable."
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
        schema.put("required", List.of("dock_sn", "action"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        // 安全等级硬拦截：session.maxSecurityLevel < L1 时直接拒绝（dry_run 之前执行，避免预览放大权限）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        String dockSn = (String) args.get("dock_sn");
        String action = (String) args.get("action");

        Map<String, Object> validationError = validateArgs(dockSn, action, args);
        if (validationError != null) {
            return validationError;
        }

        // 订阅类 action（READ_ONLY）绕过 dry_run/confirm 流程，单独分发
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(session, dockSn, action, args);
        }

        // dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return buildDryRunResponse(action, dockSn);
        }

        if (!getBooleanArg(args, "confirm")) {
            Map<String, Object> unconfirmed = requireHumanConfirm(args, action, dockSn, buildConfirmMessage(action, dockSn));
            if (unconfirmed != null) {
                return unconfirmed;
            }
        }

        return dispatchExecution(dockSn, action, args);
    }

    /**
     * 校验顶层参数：dock_sn 必填，action 必须合法；camera_position 动作需额外校验专属参数。
     *
     * @param dockSn 机场 SN
     * @param action 动作类型
     * @param args   原始参数
     * @return 校验失败时返回错误结果，校验通过返回 null
     */
    private Map<String, Object> validateArgs(String dockSn, String action, Map<String, Object> args) {
        if (dockSn == null || dockSn.isBlank()) {
            return errorResult("Missing dock_sn", "Provide the dock serial number.");
        }
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "Valid actions: " + String.join(", ", VALID_ACTIONS));
        }
        if (ACTION_CAMERA_POSITION.equals(action)) {
            return validateCameraPositionArgs(args);
        }
        return null;
    }

    /**
     * 构建 dry_run 预览响应。
     *
     * @param action 动作类型
     * @param dockSn 机场 SN
     * @return dry_run 预览结果
     */
    private Map<String, Object> buildDryRunResponse(String action, String dockSn) {
        return dryRunResult(
                "Would execute '" + action + "' on dock " + dockSn,
                buildDryRunPreview("fh_dock_control", action, dockSn,
                        Map.of("action", action),
                        List.of("session_active", "params_valid"),
                        getSecurityLevel(), requiresHumanConfirmation()));
    }

    /**
     * 构建人工确认提示文案。
     *
     * @param action 动作类型
     * @param dockSn 机场 SN
     * @return 确认提示文案
     */
    private String buildConfirmMessage(String action, String dockSn) {
        return String.format(
                "⚠️ Dangerous operation: About to execute '%s' on dock %s. Do you confirm?", action, dockSn);
    }

    /**
     * 完成 confirm 校验后的实际分发执行：camera_position 走独立分支（含 Mock 短路），
     * 其余动作沿用既有的 Dock 实例获取 + dispatchCommand 分发逻辑。
     *
     * @param dockSn 机场 SN
     * @param action 动作类型
     * @param args   原始参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchExecution(String dockSn, String action, Map<String, Object> args) {
        // camera_position 在 Mock 模式下无需真实 Dock 实例，直接返回模拟结果
        if (ACTION_CAMERA_POSITION.equals(action) && sdkService.isMockMode()) {
            return mockCameraPositionResult(args);
        }

        Dock dock = sdkService.getDock(dockSn);
        if (dock == null) {
            return errorResult("Dock not found: " + dockSn,
                    "Check the SN or call fh_device_list to see available docks.");
        }

        if (ACTION_CAMERA_POSITION.equals(action)) {
            return executeCameraPositionCommand(dock, args);
        }
        return executeDockCommand(dock, dockSn, action);
    }

    /**
     * 校验 camera_position 动作所需的参数：gateway_sn 必填，camera_position 必须是合法枚举值。
     *
     * @param args 输入参数
     * @return 校验失败时返回错误结果，校验通过返回 null
     */
    private Map<String, Object> validateCameraPositionArgs(Map<String, Object> args) {
        String gatewaySn = getStringArg(args, PARAM_GATEWAY_SN, null);
        if (gatewaySn == null || gatewaySn.isBlank()) {
            return errorResult("Missing gateway_sn",
                    "Provide gateway_sn (the dock acting as live-stream gateway) for action=camera_position.");
        }
        String cameraPosition = getStringArg(args, PARAM_CAMERA_POSITION, null);
        if (cameraPosition == null || !VALID_CAMERA_POSITIONS.contains(cameraPosition)) {
            return errorResult("Invalid camera_position: " + cameraPosition,
                    "Valid camera_position values: " + String.join(", ", VALID_CAMERA_POSITIONS));
        }
        return null;
    }

    /**
     * Mock 模式下模拟执行 camera_position 动作，返回回显的 gateway_sn 与数字化的 camera_position 值。
     *
     * @param args 输入参数
     * @return Mock 成功结果
     */
    private Map<String, Object> mockCameraPositionResult(Map<String, Object> args) {
        String gatewaySn = getStringArg(args, PARAM_GATEWAY_SN, null);
        String cameraPositionArg = getStringArg(args, PARAM_CAMERA_POSITION, null);
        DockCameraPosition cameraPosition = DockCameraPosition.valueOf(cameraPositionArg.toUpperCase(Locale.ROOT));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(PARAM_GATEWAY_SN, gatewaySn);
        data.put(PARAM_CAMERA_POSITION, cameraPosition.getValue());
        data.put("_mock", true);

        return successResult(data,
                String.format("Dock camera position switched to %s (mock).", cameraPositionArg));
    }

    /**
     * 真实 SDK 模式下执行 camera_position 动作。
     * 注意：此动作切换的是机场云台相机的物理安装位置（内仓/外仓/辅助镜头），
     * 与 fh_live_start(action=switch_source) 切换无人机直播负载源是完全不同的语义，不能相互替代。
     *
     * @param dock 机场实例
     * @param args 输入参数
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeCameraPositionCommand(Dock dock, Map<String, Object> args) {
        String gatewaySn = getStringArg(args, PARAM_GATEWAY_SN, null);
        String cameraPositionArg = getStringArg(args, PARAM_CAMERA_POSITION, null);
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        DockCameraPosition cameraPosition = DockCameraPosition.valueOf(cameraPositionArg.toUpperCase(Locale.ROOT));

        LiveInfo liveInfo = new LiveInfo();
        if (droneSn != null && !droneSn.isBlank()) {
            liveInfo.setDeviceSn(droneSn);
        }

        try {
            SdkCallbackBridge.awaitVoid(callback -> dock.getDockControlModule()
                    .changeDockCameraPosition(liveInfo, cameraPosition, gatewaySn, callback));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put(PARAM_GATEWAY_SN, gatewaySn);
            data.put(PARAM_CAMERA_POSITION, cameraPosition.getValue());
            data.put("status", "completed");

            return successResult(data,
                    String.format("Dock camera position switched to %s on gateway %s.",
                            cameraPositionArg, gatewaySn));

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_dock_control camera_position error on gateway {}", gatewaySn, e);
            return errorResult("Camera position switch failed", e.getMessage());
        }
    }

    /**
     * 执行机场控制命令。
     */
    @Generated
    private Map<String, Object> executeDockCommand(Dock dock, String dockSn, String action) {
        try {
            CompletableFuture<Void> future = new CompletableFuture<>();
            // SDK 回调统一处理：成功则 complete，失败则 completeExceptionally
            var callback = createSdkCallback(future, action);

            dispatchCommand(dock, action, callback);

            SdkCallbackBridge.await(future);

            return successResult(
                    Map.of("dock_sn", dockSn, "action", action, "status", "completed"),
                    String.format("Dock %s: %s executed successfully.", dockSn, action));

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_dock_control error: {} on {}", action, dockSn, e);
            return errorResult("Control failed", e.getMessage());
        }
    }

    /**
     * 根据 action 分发到对应的 SDK 方法。
     */
    private void dispatchCommand(Dock dock, String action, ResultCallbackWithError callback) {
        switch (action) {
            case "cover_open" -> dock.getDockControlModule().setDockCoverState(DockCoverMode.OPEN, callback);
            case "cover_close" -> dock.getDockControlModule().setDockCoverState(DockCoverMode.CLOSE, callback);
            case "charge_start" -> dock.getDockControlModule().setDockChargeState(true, callback);
            case "charge_stop" -> dock.getDockControlModule().setDockChargeState(false, callback);
            case "light_on" -> dock.getDockControlModule().setDockSupplementLightState(true, callback);
            case "light_off" -> dock.getDockControlModule().setDockSupplementLightState(false, callback);
            default -> throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    /**
     * 创建统一的 SDK 回调处理器。
     */
    private ResultCallbackWithError createSdkCallback(CompletableFuture<Void> future, String action) {
        return (error) -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                action + " failed: " + error.getErrorCode()));
            }
        };
    }

    // ── 订阅分发 ──

    /**
     * 判断是否为订阅类 action（listen_light_state/events/listen_stop）。
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_LISTEN_LIGHT_STATE.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

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
            case ACTION_LISTEN_LIGHT_STATE -> handleListenLightState(session, dockSn);
            case ACTION_EVENTS -> handleEvents(session, dockSn, args);
            case ACTION_LISTEN_STOP -> handleListenStop(session, dockSn, args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 处理 listen_light_state：订阅指定机场的补光灯状态变化 channel，返回 listen_id。
     */
    private Map<String, Object> handleListenLightState(SessionContext session, String dockSn) {
        String registryKey = ListenChannelSupport.registryKey(
                SUBSCRIBE_NAMESPACE, dockSn, CHANNEL_SUPPLEMENT_LIGHT_STATE);
        String scope = SubscriptionBuffer.scope(session, dockSn);
        boolean mock = sdkService.isMockMode();

        int listenId = listenSupport.startChannel(session, registryKey, CHANNEL_SUPPLEMENT_LIGHT_STATE,
                scope, CHANNEL_SUPPLEMENT_LIGHT_STATE, mock,
                () -> mockSupplementLightStateEvent(dockSn),
                onEvent -> startRealListen(dockSn, onEvent),
                listenId2 -> cancelRealListen(dockSn, listenId2));

        if (listenId < 0) {
            return errorResult("Failed to start listening on channel: " + CHANNEL_SUPPLEMENT_LIGHT_STATE,
                    "Check SDK connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_LIGHT_STATE);
        data.put("channel", CHANNEL_SUPPLEMENT_LIGHT_STATE);
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to dock " + dockSn
                + " supplement light state. Poll with action=events.");
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
     * 调用真实 SDK 的 listen 方法订阅补光灯状态变化，返回 listenId；Dock 不可用时返回 -1。
     */
    private int startRealListen(String dockSn, Consumer<Map<String, Object>> onEvent) {
        Dock dock = sdkService.getDock(dockSn);
        if (dock == null) {
            return -1;
        }
        return dock.getDockControlModule().listenDockSupplementLightState(
                state -> onEvent.accept(toMap(dockSn, state)));
    }

    /**
     * 调用真实 SDK 的 cancelListen 方法取消补光灯状态变化订阅。
     */
    private void cancelRealListen(String dockSn, int listenId) {
        Dock dock = sdkService.getDock(dockSn);
        if (dock == null) {
            return;
        }
        dock.getDockControlModule().cancelListenDockSupplementLightState(listenId);
    }

    /**
     * Mock — 补光灯状态变化事件。
     */
    private Map<String, Object> mockSupplementLightStateEvent(String dockSn) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("dock_sn", dockSn);
        event.put("supplement_light_state", 1);
        event.put("_mock", true);
        return event;
    }

    /**
     * 将 DockSupplementLightState 转换为事件 Map。
     */
    private Map<String, Object> toMap(String dockSn, DockSupplementLightState state) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("dock_sn", dockSn);
        event.put("supplement_light_state", state.getSupplementLightState());
        return event;
    }
}
