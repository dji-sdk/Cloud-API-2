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
import com.dji.flighthubsdk.bean.HmsArgs;
import com.dji.flighthubsdk.bean.HmsData;
import com.dji.flighthubsdk.bean.HmsDataBasics;
import com.dji.flighthubsdk.bean.HmsDataItem;
import com.dji.flighthubsdk.bean.WorkspaceDeviceChange;
import com.dji.flighthubsdk.core.DeviceManageModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * fh_device_manage — 聚合设备管理操作。
 * 操作类型：detail / rename / unbind / hms_current / firmware_list，
 * 以及 workspace 级设备变更订阅三元组：listen_workspace_changed / events / listen_stop。
 * 安全等级：L0（查询类 + 订阅类）~ L1（rename/unbind）
 *
 * <p>混合等级工具：工具级声明最低等级 L0（READ_ONLY，门控基线），
 * {@code rename} / {@code unbind} 由 {@link #ACTION_SECURITY_LEVELS} 在 action 内抬升至 L1 硬拦截。
 * per-action 门控只能抬高、不能降低。
 */
@Component
public class FhDeviceManageTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDeviceManageTool.class);

    // ── 订阅类 action 常量 ──

    /** 订阅：开始监听 workspace 级设备变更事件 */
    private static final String ACTION_LISTEN_WORKSPACE_CHANGED = "listen_workspace_changed";
    /** 订阅：按游标轮询缓冲事件 */
    private static final String ACTION_EVENTS = "events";
    /** 订阅：停止监听 */
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 各 action 要求的安全等级（未列出的 action 为 L0，不需要拦截） */
    private static final Map<String, SecurityLevel> ACTION_SECURITY_LEVELS = Map.of(
            "rename", SecurityLevel.REVERSIBLE,
            "unbind", SecurityLevel.REVERSIBLE
    );

    private static final String PARAM_CHANNELS = "channels";
    private static final String PARAM_SINCE_SEQ = "since_seq";
    private static final String PARAM_LIMIT = "limit";

    /** 订阅注册表命名空间（与 Tool 名对应） */
    private static final String SUBSCRIBE_NAMESPACE = "device_manage";

    /** 订阅作用域标识：本工具的订阅是 workspace 级，不区分设备 */
    private static final String SUBSCRIBE_SCOPE_ID = "workspace";

    /** 唯一订阅 channel：workspace 设备变更事件 */
    private static final String CHANNEL_WORKSPACE_CHANGED = "workspace_changed";

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
    public FhDeviceManageTool(SessionManager sessionManager, SdkService sdkService,
                               ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 声明工具级静态安全等级：READ_ONLY（混合等级工具的门控基线）。
     *
     * <p>查询类与订阅类 action 为 L0，不应被 L1 门控误拦；
     * {@code rename} / {@code unbind} 在 {@link #execute} 内由 {@code enforceSecurityLevel} 抬升至 L1。
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
        def.put("name", "fh_device_manage");
        def.put("description", "Device management operations: get detail info, rename device, unbind device, "
                + "query HMS health alerts, query firmware list. "
                + "Rename/unbind are L1 operations requiring confirm=true. "
                + "Also supports workspace-level device-change subscription: " + ACTION_LISTEN_WORKSPACE_CHANGED
                + " starts listening for device add/remove changes across the workspace, " + ACTION_EVENTS
                + " polls buffered events since a cursor, and " + ACTION_LISTEN_STOP + " unsubscribes "
                + "(lifecycle: " + ACTION_LISTEN_WORKSPACE_CHANGED + " -> " + ACTION_EVENTS + " -> "
                + ACTION_LISTEN_STOP + ").");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of(
                "type", "string",
                "enum", List.of("detail", "rename", "unbind", "hms_current",
                        ACTION_LISTEN_WORKSPACE_CHANGED, ACTION_EVENTS, ACTION_LISTEN_STOP),
                "description", "detail/rename/unbind/hms_current: per-device management action (device_sn "
                        + "required) | " + ACTION_LISTEN_WORKSPACE_CHANGED + ": subscribe to workspace-level "
                        + "device add/remove change events (device_sn not needed) | " + ACTION_EVENTS
                        + ": poll buffered events for one/all active channels since a cursor | "
                        + ACTION_LISTEN_STOP + ": unsubscribe one/all active channels"
        ));
        props.put("device_sn", Map.of("type", "string",
                "description", "Target device SN (required for detail/rename/unbind/hms_current; "
                        + "not used by subscription actions)"));
        props.put("new_name", Map.of("type", "string",
                "description", "New display name (required for action=rename)"));
        props.put("confirm", Map.of("type", "boolean",
                "description", "Confirmation flag (required for rename/unbind)"));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Channel names for " + ACTION_EVENTS + "/" + ACTION_LISTEN_STOP
                        + " (optional; defaults to all currently active channels when omitted). "
                        + "Only channel is '" + CHANNEL_WORKSPACE_CHANGED + "'."
        ));
        props.put(PARAM_SINCE_SEQ, Map.of(
                "type", "integer",
                "description", "Cursor for " + ACTION_EVENTS
                        + ": only return events with _seq greater than this value (default 0)"
        ));
        props.put(PARAM_LIMIT, Map.of(
                "type", "integer",
                "description", "Max events to return per channel for " + ACTION_EVENTS
                        + " action (default " + DEFAULT_EVENTS_LIMIT + ")"
        ));
        schema.put("properties", props);
        schema.put("required", List.of("action"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = (String) args.get("action");

        if (action == null) {
            return errorResult("Missing action", "Provide action: detail/rename/unbind/hms_current/"
                    + ACTION_LISTEN_WORKSPACE_CHANGED + "/" + ACTION_EVENTS + "/" + ACTION_LISTEN_STOP);
        }

        // 统一安全等级拦截：rename/unbind=L1
        SecurityLevel requiredLevel = ACTION_SECURITY_LEVELS.get(action);
        if (requiredLevel != null) {
            Map<String, Object> denied = enforceSecurityLevel(requiredLevel);
            if (denied != null) {
                return denied;
            }
        }

        // 订阅类 action 是 workspace 级的，不依赖 device_sn，必须在 device_sn/workspace 校验之前分发
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(session, action, args);
        }

        String deviceSn = (String) args.get("device_sn");
        if (deviceSn == null || deviceSn.isBlank()) {
            return errorResult("Missing device_sn", "Provide the device serial number.");
        }

        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return errorResult("SDK not initialized", "Call fh_connect first.");
        }

        return switch (action) {
            case "detail" -> executeDetail(workspace, deviceSn);
            case "rename" -> executeRename(workspace, deviceSn, args);
            case "unbind" -> executeUnbind(workspace, deviceSn, args);
            case "hms_current" -> executeHmsCurrent(deviceSn);
            default -> errorResult("Unknown action: " + action,
                    "Valid actions: detail, rename, unbind, hms_current, "
                            + ACTION_LISTEN_WORKSPACE_CHANGED + ", " + ACTION_EVENTS + ", " + ACTION_LISTEN_STOP);
        };
    }

    @Generated
    private Map<String, Object> executeDetail(Workspace workspace, String deviceSn) {
        try {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            workspace.getDeviceManageModule().getDeviceDetailInfo(deviceSn, (error, detail) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getDeviceDetailInfo failed: " + error.getErrorCode()));
                    return;
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("device_callsign", detail.getNickname());
                data.put("firmware_version", detail.getDeviceFirmwareVersion());
                data.put("firmware_status", detail.getDeviceFirmwareStatus());
                data.put("online", detail.isOnline());
                data.put("working_status", detail.getDeviceWorkingStatus());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("message", "Device detail retrieved for " + deviceSn);
            return result;

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error", e.getMessage());
        } catch (Exception e) {
            return errorResult("Failed to get detail", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> executeRename(Workspace workspace, String deviceSn, Map<String, Object> args) {
        String newName = (String) args.get("new_name");
        Boolean confirm = (Boolean) args.get("confirm");

        if (newName == null || newName.isBlank()) {
            return errorResult("Missing new_name", "Provide the new display name.");
        }

        // rename 属 L1：安全等级已在 execute() 内统一拦截，此处无需重复校验
        // dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would rename device " + deviceSn + " to '" + newName + "'",
                    buildDryRunPreview("fh_device_manage", "rename", deviceSn,
                            Map.of("new_name", newName),
                            List.of("session_active", "security_level_allowed", "params_valid"),
                            SecurityLevel.REVERSIBLE, requiresHumanConfirmation()));
        }

        if (!Boolean.TRUE.equals(confirm)) {
            return errorResult("Confirmation required",
                    String.format("About to rename device %s to '%s'. Pass confirm=true to proceed.", deviceSn, newName));
        }

        try {
            CompletableFuture<Void> future = new CompletableFuture<>();

            var item = new com.dji.flighthubsdk.bean.ModifyDeviceNameItem();
            item.setDeviceSn(deviceSn);
            item.setDeviceCallsign(newName);
            var param = new com.dji.flighthubsdk.bean.ModifyDeviceNameParam();
            param.setDeviceList(List.of(item));
            workspace.getDeviceManageModule().modifyDeviceName(param, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "Rename failed: " + error.getErrorCode()));
                }
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", deviceSn, "new_name", newName));
            result.put("message", "Device " + deviceSn + " renamed to '" + newName + "'");
            return result;

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Rename failed", e.getMessage());
        } catch (Exception e) {
            return errorResult("Rename error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> executeUnbind(Workspace workspace, String deviceSn, Map<String, Object> args) {
        Boolean confirm = (Boolean) args.get("confirm");

        // unbind 不可逆，属 L1：安全等级已在 execute() 内统一拦截，此处无需重复校验
        // dry_run 模式：返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would unbind device " + deviceSn + " from workspace (irreversible)",
                    buildDryRunPreview("fh_device_manage", "unbind", deviceSn,
                            Map.of(),
                            List.of("session_active", "security_level_allowed"),
                            SecurityLevel.REVERSIBLE, requiresHumanConfirmation()));
        }

        if (!Boolean.TRUE.equals(confirm)) {
            return errorResult("Confirmation required",
                    String.format("About to unbind device %s from workspace. This action cannot be undone. Pass confirm=true.", deviceSn));
        }

        try {
            CompletableFuture<Void> future = new CompletableFuture<>();

            workspace.getDeviceManageModule().unbindDevice(List.of(deviceSn), (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "Unbind failed: " + error.getErrorCode()));
                }
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", deviceSn, "action", "unbound"));
            result.put("message", "Device " + deviceSn + " unbound from workspace.");
            return result;

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Unbind failed", e.getMessage());
        } catch (Exception e) {
            return errorResult("Unbind error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> executeHmsCurrent(String deviceSn) {
        // HMS 数据来自 Drone 或 Dock 的监听通道
        // 简化处理：先尝试 drone，再尝试 dock
        var drone = sdkService.getDrone(deviceSn);
        if (drone != null) {
            return collectDroneHms(drone, deviceSn);
        }

        var dock = sdkService.getDock(deviceSn);
        if (dock != null) {
            return collectDockHms(dock, deviceSn);
        }

        return errorResult("Device not found: " + deviceSn,
                "Cannot find drone or dock with this SN.");
    }

    @Generated
    private Map<String, Object> collectDroneHms(com.dji.flighthubsdk.core.Drone drone, String sn) {
        try {
            var hmsModule = drone.getDeviceHMSModule();
            if (hmsModule == null) {
                return errorResult("DeviceHMSModule not available for drone: " + sn,
                        "Drone firmware may not support HMS. Check firmware version.");
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            int listenId = hmsModule.listenDeviceHMS(hmsData -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", sn);
                data.put("device_type", "drone");
                data.put("hms_alerts", buildHmsAlerts(hmsData));
                future.complete(data);
            });

            Map<String, Object> data;
            try {
                data = SdkCallbackBridge.await(future, 10);
            } finally {
                if (listenId >= 0) {
                    hmsModule.cancelListenDeviceHMS(listenId);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("message", "HMS data collected for drone " + sn);
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", sn, "hms_alerts", List.of(), "note", "No HMS alerts within 10s (device may be healthy)"));
            result.put("message", "No HMS alerts reported for " + sn);
            return result;
        } catch (Exception e) {
            return errorResult("HMS query failed", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> collectDockHms(com.dji.flighthubsdk.core.Dock dock, String sn) {
        try {
            var hmsModule = dock.getDeviceHMSModule();
            if (hmsModule == null) {
                return errorResult("DeviceHMSModule not available for dock: " + sn,
                        "Dock firmware may not support HMS. Check firmware version.");
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            int listenId = hmsModule.listenDeviceHMS(hmsData -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", sn);
                data.put("device_type", "dock");
                data.put("hms_alerts", buildHmsAlerts(hmsData));
                future.complete(data);
            });

            Map<String, Object> data;
            try {
                data = SdkCallbackBridge.await(future, 10);
            } finally {
                if (listenId >= 0) {
                    hmsModule.cancelListenDeviceHMS(listenId);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("message", "HMS data collected for dock " + sn);
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", sn, "hms_alerts", List.of(), "note", "No HMS alerts within 10s"));
            result.put("message", "No HMS alerts reported for " + sn);
            return result;
        } catch (Exception e) {
            return errorResult("HMS query failed", e.getMessage());
        }
    }

    // ── 订阅分发 ──

    /**
     * 判断是否为订阅类 action（listen_workspace_changed/events/listen_stop）。
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_LISTEN_WORKSPACE_CHANGED.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

    /**
     * 订阅类 action 分发入口。
     *
     * @param session 当前 session 上下文
     * @param action  操作名
     * @param args    输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(SessionContext session, String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_LISTEN_WORKSPACE_CHANGED -> handleListenWorkspaceChanged(session);
            case ACTION_EVENTS -> handleEvents(session, args);
            case ACTION_LISTEN_STOP -> handleListenStop(session, args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 处理 listen_workspace_changed：订阅 workspace 级设备变更事件，返回 listen_id。
     */
    private Map<String, Object> handleListenWorkspaceChanged(SessionContext session) {
        String registryKey = ListenChannelSupport.registryKey(
                SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, CHANNEL_WORKSPACE_CHANGED);
        String scope = SubscriptionBuffer.scope(session, SUBSCRIBE_SCOPE_ID);
        boolean mock = sdkService.isMockMode();

        int listenId = listenSupport.startChannel(session, registryKey, CHANNEL_WORKSPACE_CHANGED,
                scope, CHANNEL_WORKSPACE_CHANGED, mock,
                this::mockWorkspaceDeviceChangedEvent,
                this::startRealListenWorkspaceChanged,
                this::cancelRealListenWorkspaceChanged);

        if (listenId < 0) {
            return errorResult("Failed to start listening on channel: " + CHANNEL_WORKSPACE_CHANGED,
                    "Check SDK connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_WORKSPACE_CHANGED);
        data.put("channel", CHANNEL_WORKSPACE_CHANGED);
        data.put("listen_id", listenId);
        return successResult(data, "Subscribed to workspace device change events. Poll with action="
                + ACTION_EVENTS + ".");
    }

    /**
     * 处理 events：按游标轮询一个或多个已订阅 channel 的缓冲事件。
     */
    private Map<String, Object> handleEvents(SessionContext session, Map<String, Object> args) {
        List<String> channels = resolveChannels(session, args);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (String channel : channels) {
            String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel);
            String scope = SubscriptionBuffer.scope(session, SUBSCRIBE_SCOPE_ID);
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
    private Map<String, Object> handleListenStop(SessionContext session, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<String> channels = (requested != null && !requested.isEmpty())
                ? requested
                : listenSupport.activeChannels(session, subscribePrefix());

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, channels,
                channel -> ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, SUBSCRIBE_SCOPE_ID, channel), log);

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
    private List<String> resolveChannels(SessionContext session, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested != null && !requested.isEmpty()) {
            return requested;
        }
        return listenSupport.activeChannels(session, subscribePrefix());
    }

    /**
     * 本工具订阅注册表 key 的前缀（namespace::scopeId::）。
     */
    private String subscribePrefix() {
        return SUBSCRIBE_NAMESPACE + "::" + SUBSCRIBE_SCOPE_ID + "::";
    }

    /**
     * Mock — workspace 设备变更事件（新增一台设备）。
     */
    private Map<String, Object> mockWorkspaceDeviceChangedEvent() {
        Map<String, Object> change = new LinkedHashMap<>();
        change.put("change_type", "add");
        change.put("change_devices", List.of("DRONE-001"));

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("device_changes", List.of(change));
        event.put("_mock", true);
        return event;
    }

    /**
     * 真实 SDK — 调用 DeviceManageModule.listenWorkspaceDeviceChanged 订阅 workspace 设备变更事件；
     * Workspace/DeviceManageModule 不可用时返回负数。
     */
    private int startRealListenWorkspaceChanged(Consumer<Map<String, Object>> onEvent) {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return -1;
        }
        DeviceManageModule deviceManageModule = workspace.getDeviceManageModule();
        if (deviceManageModule == null) {
            return -1;
        }
        return deviceManageModule.listenWorkspaceDeviceChanged(change -> onEvent.accept(toMap(change)));
    }

    /**
     * 真实 SDK — 调用 DeviceManageModule.cancelListenWorkspaceDeviceChanged 取消订阅。
     */
    private void cancelRealListenWorkspaceChanged(int listenId) {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return;
        }
        DeviceManageModule deviceManageModule = workspace.getDeviceManageModule();
        if (deviceManageModule == null) {
            return;
        }
        deviceManageModule.cancelListenWorkspaceDeviceChanged(listenId);
    }

    /**
     * 将 WorkspaceDeviceChange 转换为事件 Map。
     */
    private Map<String, Object> toMap(WorkspaceDeviceChange change) {
        List<Map<String, Object>> deviceChanges = new ArrayList<>();
        if (change.getDeviceChanges() != null) {
            change.getDeviceChanges().forEach(item -> {
                Map<String, Object> dc = new LinkedHashMap<>();
                dc.put("change_type", item.getChangeType());
                dc.put("change_devices", item.getChangeDevices());
                deviceChanges.add(dc);
            });
        }
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("device_changes", deviceChanges);
        return event;
    }

    // ==================== SDK bean → 结构化 Map（避免 toString 透传） ====================

    /**
     * 将 HMS 实时告警映射为结构化列表，避免 {@code toString()} 透传导致输出不可解析。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param hmsData SDK 推送的 HMS 数据，可为 null
     * @return 结构化告警列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildHmsAlerts(HmsData hmsData) {
        if (hmsData == null || hmsData.getHmsDataItems() == null) {
            return List.of();
        }
        List<Map<String, Object>> alerts = new ArrayList<>(hmsData.getHmsDataItems().size());
        for (HmsDataItem item : hmsData.getHmsDataItems()) {
            if (item == null) {
                continue;
            }
            Map<String, Object> alert = buildHmsBasics(item.getBasics());
            if (alert == null) {
                alert = new LinkedHashMap<>();
            }
            alert.put("sub_items", buildHmsBasicsList(item.getSubHmsData()));
            alerts.add(alert);
        }
        return alerts;
    }

    /**
     * 映射主告警条目。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param basics SDK 告警基础信息，可为 null
     * @return 结构化告警；入参为 null 时返回 null
     */
    Map<String, Object> buildHmsBasics(HmsDataBasics basics) {
        if (basics == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("level", basics.getLevel());
        row.put("module_id", basics.getModuleId());
        row.put("code", basics.getCode());
        row.put("device_type", basics.getDeviceType());
        row.put("domain_type", basics.getDomainType());
        row.put("in_the_sky", basics.getInTheSky());
        row.put("imminent", basics.getImminent());
        row.put("status_key", basics.getStatusKey());
        row.put("joint_key", basics.getJointKey());
        row.put("id", basics.getId());
        row.put("parent_id", basics.getParentId());
        row.put("args", buildHmsArgs(basics.getArgs()));
        return row;
    }

    /**
     * 映射子告警条目列表。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param subItems SDK 子告警列表，可为 null
     * @return 结构化子告警列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildHmsBasicsList(List<HmsDataBasics> subItems) {
        if (subItems == null) {
            return List.of();
        }
        List<Map<String, Object>> mapped = new ArrayList<>(subItems.size());
        for (HmsDataBasics subItem : subItems) {
            Map<String, Object> row = buildHmsBasics(subItem);
            if (row != null) {
                mapped.add(row);
            }
        }
        return mapped;
    }

    /**
     * 映射告警附加参数。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param args SDK 告警参数对象，可为 null
     * @return 结构化参数；入参为 null 时返回 null
     */
    Map<String, Object> buildHmsArgs(HmsArgs args) {
        if (args == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("component_index", args.getComponentIndex());
        row.put("sensor_index", args.getSensorIndex());
        row.put("device_id", args.getDeviceId());
        row.put("sender_host_id", args.getSenderHostId());
        row.put("ignore", args.getIgnore());
        row.put("alarm_info", args.getAlarmInfo());
        row.put("create_timestamp", args.getCreateTimestamp());
        row.put("display_scene", args.getDisplayScene());
        return row;
    }
}
