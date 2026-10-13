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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.DockLocation;
import com.dji.flighthubsdk.bean.TakeoffParams;
import com.dji.flighthubsdk.bean.TakeoffResult;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.value.OutOfControlAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * fh_takeoff — 命令无人机起飞。
 * 起飞后无人机将悬停在目标高度，等待下一条指令。
 * 前置条件：必须已获取飞行控制权且 DRC 链路已建立（fh_control_acquire + fh_drc_connect）。
 *
 * <p>安全等级：L2（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhTakeoffTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhTakeoffTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_takeoff";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** dock_sn 参数名（用于获取机场坐标，填充 TakeoffParams.targetLatitude/Longitude） */
    private static final String PARAM_DOCK_SN = "dock_sn";

    /** target_height 参数名 */
    private static final String PARAM_TARGET_HEIGHT = "target_height";

    /** confirm 参数名 */
    private static final String PARAM_CONFIRM = "confirm";

    /** security_takeoff_height 参数名 */
    private static final String PARAM_SECURITY_TAKEOFF_HEIGHT = "security_takeoff_height";

    /** rth_altitude 参数名 */
    private static final String PARAM_RTH_ALTITUDE = "rth_altitude";

    /** max_speed 参数名 */
    private static final String PARAM_MAX_SPEED = "max_speed";

    /** out_of_control_action 参数名 */
    private static final String PARAM_OUT_OF_CONTROL_ACTION = "out_of_control_action";

    /** 操作标识，用于 requireConfirm 提示 */
    private static final String ACTION_TAKEOFF = "takeoff";

    /** 目标高度最小值（米） */
    private static final double TARGET_HEIGHT_MIN = 2.0;

    /** 目标高度最大值（米） */
    private static final double TARGET_HEIGHT_MAX = 10000.0;

    /** 目标悬停高度默认值（米） */
    private static final int DEFAULT_TARGET_HEIGHT = 100;

    /** 安全起飞高度默认值（米） */
    private static final int DEFAULT_SECURITY_TAKEOFF_HEIGHT = 100;

    /** 返航高度默认值（米） */
    private static final int DEFAULT_RTH_ALTITUDE = 100;

    /** 返航高度最大值（米），SDK 约束 */
    private static final int RTH_ALTITUDE_MAX = 1500;

    /** 安全起飞高度最大值（米），SDK 约束 */
    private static final int SECURITY_TAKEOFF_HEIGHT_MAX = 1500;

    /** 返航高度模式：设定高度（而非智能高度），确保设备尊重 rthAltitude 参数值 */
    private static final int RTH_MODE_PRESET = 1;

    /** 最大速度默认值（m/s） */
    private static final int DEFAULT_MAX_SPEED = 15;

    /** 失控动作默认值 */
    private static final String DEFAULT_OUT_OF_CONTROL_ACTION = "return_home";

    /** 有效的失控动作列表 */
    private static final List<String> VALID_OUT_OF_CONTROL_ACTIONS =
            List.of("return_home", "hover", "continue");

    /** 获取机场坐标的订阅超时（秒） */
    private static final int DOCK_LOCATION_TIMEOUT_SECONDS = 10;

    private final SdkService sdkService;
    private final FlightHubProperties properties;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param properties     FlightHub 配置（含 cockpit URL）
     */
    public FhTakeoffTool(SessionManager sessionManager, SdkService sdkService,
                         FlightHubProperties properties) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.properties = properties;
    }

    /**
     * 返回本工具的 MCP 协议定义，包含名称、描述和 inputSchema。
     *
     * @return MCP 工具定义 Map
     */

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.FLIGHT;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Command drone to take off. After takeoff, drone hovers at target_height "
                        + "awaiting next command. "
                        + "ALTITUDE REFERENCE: All height parameters in this tool use ALT "
                        + "(relative to takeoff point / home point), NOT ASL (above sea level) or "
                        + "AGL (above ground level). The OSD telemetry reports: "
                        + "elevation=ALT (relative to takeoff), height=ASL (ellipsoid height). "
                        + "IMPORTANT: rth_altitude and security_takeoff_height default to 100m. "
                        + "If target_height > 100m, you MUST also set rth_altitude >= target_height, "
                        + "otherwise the device may constrain the drone to rth_altitude after takeoff, "
                        + "causing an unexpected descent (e.g. target_height=150 but drone drops to 100). "
                        + "DOCK-BASED TAKEOFF: The drone does NOT need to be online. "
                        + "Drones housed in airports appear offline (powered down in storage) — "
                        + "this is normal. Only the airport/dock (gateway) needs to be online. "
                        + "Never block takeoff solely because drone.online is false; "
                        + "always check airport.online instead. "
                        + "Prerequisites: control authority + DRC link established.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_DOCK_SN, Map.of(
                "type", "string",
                "description", "Dock serial number that the drone is housed in. "
                        + "Required in real SDK mode to fetch dock GPS coordinates "
                        + "(targetLatitude/targetLongitude for home point). "
                        + "If not provided by the user, call fh_device_list first to look up "
                        + "the airport/dock that contains the target drone_sn — do NOT ask the user."
        ));
        props.put(PARAM_TARGET_HEIGHT, Map.of(
                "type", "number",
                "description", "Target hover height in meters RELATIVE TO TAKEOFF POINT (ALT), "
                        + "range: 2~10000, default: 100. This is NOT ASL (sea level) or AGL (ground level). "
                        + "When setting this > 100, also set rth_altitude >= this value to avoid auto-descent."
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the takeoff command"
        ));
        props.put(PARAM_SECURITY_TAKEOFF_HEIGHT, Map.of(
                "type", "number",
                "description", "Security takeoff height in meters (ALT, range: 8~1500, default: 50). "
                        + "Drone ascends to this height first before lateral movement. "
                        + "Should be <= target_height."
        ));
        props.put(PARAM_RTH_ALTITUDE, Map.of(
                "type", "integer",
                "description", "Return-to-home altitude in meters (ALT, range: 15~1500, default: 100). "
                        + "CRITICAL: If target_height > 100, set this >= target_height. "
                        + "Otherwise the device may cap the drone's altitude at rth_altitude "
                        + "after takeoff completes, causing unexpected descent."
        ));
        props.put(PARAM_MAX_SPEED, Map.of(
                "type", "integer",
                "description", "Maximum flight speed in m/s (default: 15)"
        ));
        props.put(PARAM_OUT_OF_CONTROL_ACTION, Map.of(
                "type", "string",
                "enum", VALID_OUT_OF_CONTROL_ACTIONS,
                "description", "Action when signal lost: return_home | hover | continue (default: return_home)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_DOCK_SN, PARAM_TARGET_HEIGHT, PARAM_CONFIRM));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行一键起飞操作。
     * 前置条件：持有控制权 + DRC 链路 + confirm。安全等级 L2。
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

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L2 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 2. 校验 drone_sn 参数
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 2.5 读取 dock_sn（真实模式必填，mock 模式可选）
        String dockSn = getStringArg(args, PARAM_DOCK_SN, null);

        // 3. 飞行操作前置人工确认
        //    先要求操作员显式确认，再做 DRC 链路就绪/抢控，杜绝"静默抢控 + 起飞"链路
        //    dry_run 模式无需确认，直接走预览分支（见 step 8）
        if (!isDryRun(args)) {
            Map<String, Object> preConfirmError = requireHumanConfirm(args, ACTION_TAKEOFF, droneSn,
                    buildConfirmMessage(droneSn, args, 0, false, false));
            if (preConfirmError != null) {
                return preConfirmError;
            }
        }

        // 4. 确保 DRC 链路就绪（自动获取控制权 + 连接 DRC）— 确认通过后才执行
        Map<String, Object> drcError = ensureDrcLink(session, droneSn, sdkService);
        if (drcError != null) {
            return drcError;
        }

        // 4. 校验 target_height 范围
        double targetHeight = getDoubleArg(args, PARAM_TARGET_HEIGHT, DEFAULT_TARGET_HEIGHT);
        Map<String, Object> heightError = validateTargetHeight(targetHeight);
        if (heightError != null) {
            return heightError;
        }

        // 5. 读取可选参数（使用默认值）
        int securityTakeoffHeight = getIntArg(args, PARAM_SECURITY_TAKEOFF_HEIGHT, DEFAULT_SECURITY_TAKEOFF_HEIGHT);
        int rthAltitude = getIntArg(args, PARAM_RTH_ALTITUDE, DEFAULT_RTH_ALTITUDE);

        // 5.5 自动修正：高度参数 < target_height 会导致起飞后自动降高
        boolean rthAutoAdjusted = false;
        if (rthAltitude < (int) targetHeight) {
            int adjusted = Math.min((int) targetHeight, RTH_ALTITUDE_MAX);
            log.warn("rth_altitude({}) < target_height({}), auto-adjusting to {} to avoid post-takeoff descent",
                    rthAltitude, (int) targetHeight, adjusted);
            rthAltitude = adjusted;
            rthAutoAdjusted = true;
        }
        // security_takeoff_height 同样需要 >= target_height，否则设备可能用它约束高度
        boolean securityAutoAdjusted = false;
        if (securityTakeoffHeight < (int) targetHeight) {
            int adjusted = Math.min((int) targetHeight, SECURITY_TAKEOFF_HEIGHT_MAX);
            log.warn("security_takeoff_height({}) < target_height({}), auto-adjusting to {}",
                    securityTakeoffHeight, (int) targetHeight, adjusted);
            securityTakeoffHeight = adjusted;
            securityAutoAdjusted = true;
        }
        int maxSpeed = getIntArg(args, PARAM_MAX_SPEED, DEFAULT_MAX_SPEED);
        String outOfControlAction = getStringArg(args, PARAM_OUT_OF_CONTROL_ACTION, DEFAULT_OUT_OF_CONTROL_ACTION);

        // 6. 校验失控动作参数
        if (!VALID_OUT_OF_CONTROL_ACTIONS.contains(outOfControlAction)) {
            return errorResult("Invalid out_of_control_action: " + outOfControlAction,
                    "Must be one of: " + VALID_OUT_OF_CONTROL_ACTIONS);
        }

        // 8. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would take off drone " + droneSn + " to altitude " + (int) targetHeight + "m",
                    buildDryRunPreview(TOOL_NAME, ACTION_TAKEOFF, droneSn,
                            Map.of("target_height", targetHeight,
                                    "security_takeoff_height", securityTakeoffHeight,
                                    "rth_altitude", rthAltitude,
                                    "max_speed", maxSpeed,
                                    "out_of_control_action", outOfControlAction),
                            List.of("session_active", "security_level_allowed", "drc_linked", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 9. 人工确认已在 step 3 完成
        //    此处不再重复弹窗，直接进入执行阶段

        // 9. Mock 模式：直接执行起飞
        if (sdkService.isMockMode()) {
            return takeoffInMockMode(session, droneSn, dockSn, targetHeight, outOfControlAction,
                    securityTakeoffHeight, rthAltitude, maxSpeed);
        }

        // 10. 真实 SDK 调用：dock_sn 必填（用于获取机场坐标）
        if (dockSn == null) {
            return errorResult("Missing required parameter: dock_sn",
                    "dock_sn is required in real SDK mode to fetch dock GPS coordinates "
                            + "(TakeoffParams.targetLatitude/targetLongitude). "
                            + "Use fh_device_list to get the dock SN associated with this drone.");
        }
        return takeoffWithRealSdk(session, droneSn, dockSn, targetHeight, outOfControlAction,
                securityTakeoffHeight, rthAltitude, maxSpeed);
    }

    /**
     * 构建起飞人工确认提示。
     *
     * @param droneSn              目标无人机 SN
     * @param args                 输入参数（读取目标高度）
     * @param rthAltitude          修正后的返航高度
     * @param rthAutoAdjusted      返航高度是否发生了自动修正
     * @param securityAutoAdjusted 安全起飞高度是否发生了自动修正
     * @return 面向真人的确认提示
     */
    private String buildConfirmMessage(String droneSn, Map<String, Object> args,
                                       int rthAltitude, boolean rthAutoAdjusted,
                                       boolean securityAutoAdjusted) {
        double targetHeight = getDoubleArg(args, PARAM_TARGET_HEIGHT, DEFAULT_TARGET_HEIGHT);
        boolean usingDefault = !args.containsKey(PARAM_TARGET_HEIGHT);
        String heightNote = usingDefault ? "（默认值，未显式指定）" : "";

        String adjustNote = "";
        if (rthAutoAdjusted && securityAutoAdjusted) {
            adjustNote = String.format("（返航高度和安全起飞高度已自动调整为 %dm）", rthAltitude);
        } else if (rthAutoAdjusted) {
            adjustNote = String.format("（返航高度已自动调整为 %dm）", rthAltitude);
        } else if (securityAutoAdjusted) {
            adjustNote = String.format("（安全起飞高度已自动调整为 %dm）", rthAltitude);
        }

        return String.format(
                "⚠️ 无人机 %s 即将起飞至 %.1f 米高度%s%s，是否确认？",
                droneSn, targetHeight, heightNote, adjustNote);
    }

    /**
     * Mock 模式下执行起飞指令。
     *
     * @param session              当前 session
     * @param droneSn              目标无人机 SN
     * @param dockSn               机场 SN（用于构建 cockpit URL，可为 null）
     * @param targetHeight         目标悬停高度
     * @param outOfControlAction   失控动作
     * @param securityTakeoffHeight 安全起飞高度
     * @param rthAltitude          返航高度
     * @param maxSpeed             最大速度
     * @return 成功响应
     */
    private Map<String, Object> takeoffInMockMode(
            SessionContext session,
            String droneSn,
            String dockSn,
            double targetHeight,
            String outOfControlAction,
            int securityTakeoffHeight,
            int rthAltitude,
            int maxSpeed) {
        String flightId = "flight_" + System.currentTimeMillis();
        session.setActiveFlightId(flightId);
        session.setFlightPhase(FlightPhase.TAKING_OFF);
        log.info("[MOCK] Takeoff initiated: drone={}, height={}m, flightId={}", droneSn, targetHeight, flightId);

        Map<String, Object> result = buildTakeoffResult(
                droneSn, dockSn, flightId, targetHeight, outOfControlAction,
                securityTakeoffHeight, rthAltitude, maxSpeed, "[MOCK] ");
        // 向 data 注入 mock 标记
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        data.put("_mock", true);
        return result;
    }

    /**
     * 真实 SDK 模式下执行起飞指令。
     * 起飞前先订阅 DockOsdModule.listenDockLocation() 获取机场 GPS 坐标，
     * 填入 TakeoffParams.targetLatitude / targetLongitude，确保 SDK 能正确设置 home point。
     * 通过 executeWithDrcRetry 实现 DRC 链路故障时自动重连重试。
     *
     * @param session              当前 session
     * @param droneSn              目标无人机 SN
     * @param dockSn               无人机所在机场 SN（用于获取坐标）
     * @param targetHeight         目标悬停高度
     * @param outOfControlAction   失控动作字符串
     * @param securityTakeoffHeight 安全起飞高度
     * @param rthAltitude          返航高度
     * @param maxSpeed             最大速度
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> takeoffWithRealSdk(
            SessionContext session,
            String droneSn,
            String dockSn,
            double targetHeight,
            String outOfControlAction,
            int securityTakeoffHeight,
            int rthAltitude,
            int maxSpeed) {
        try {
            return executeWithDrcRetry(session, droneSn, sdkService, () -> {
                Drone drone = sdkService.getDrone(droneSn);
                if (drone == null) {
                    return errorResult("Drone instance not found: " + droneSn,
                            "Drone SN not registered in SDK. Ensure the dock/airport is online "
                                    + "and the drone SN is correct. "
                                    + "Note: drone.online=false is normal for docked drones — "
                                    + "only the airport/dock (gateway) must be online to execute takeoff. "
                                    + "Use fh_device_list to verify airport online status.");
                }

                DockLocation dockLocation = fetchDockLocation(dockSn);
                if (dockLocation == null) {
                    return errorResult("Failed to fetch dock location for dock: " + dockSn,
                            "Dock GPS coordinates are required for takeoff. "
                                    + "Ensure dock is online and has valid RTK/GPS coordinates. "
                                    + "If dock location is (0,0), perform RTK calibration first "
                                    + "via fh_device_debug(rtk_calibrate_manual or rtk_calibrate_network).");
                }
                if (dockLocation.getLatitude() == 0.0 && dockLocation.getLongitude() == 0.0) {
                    return errorResult("Dock GPS coordinates are (0, 0) for dock: " + dockSn,
                            "Dock has not acquired valid GPS fix. "
                                    + "Perform RTK calibration first via fh_device_debug "
                                    + "(action=rtk_calibrate_manual or rtk_calibrate_network).");
                }

                TakeoffParams params = new TakeoffParams();
                params.setTargetHeight(targetHeight);
                params.setSecurityTakeoffHeight(securityTakeoffHeight);
                params.setMaxSpeed(maxSpeed);
                params.setRthAltitude(rthAltitude);
                params.setRthMode(RTH_MODE_PRESET);
                params.setTargetLatitude(dockLocation.getLatitude());
                params.setTargetLongitude(dockLocation.getLongitude());
                params.setOutOfControlAction(mapOutOfControlAction(outOfControlAction));

                log.info("fh_takeoff: drone={}, dock={}, lat={}, lon={}, "
                                + "targetHeight={}m, securityTakeoffHeight={}m, "
                                + "rthAltitude={}m, rthMode={}, maxSpeed={}m/s",
                        droneSn, dockSn,
                        dockLocation.getLatitude(), dockLocation.getLongitude(),
                        targetHeight, securityTakeoffHeight,
                        rthAltitude, RTH_MODE_PRESET, maxSpeed);

                CompletableFuture<TakeoffResult> future = new CompletableFuture<>();
                drone.getTakeoffModule().takeoff(params, (error, result) -> {
                    if (error.isSuccess()) {
                        future.complete(result);
                    } else {
                        future.completeExceptionally(
                                new SdkCallbackBridge.SdkException(
                                        error.getServerCode(), formatSdkError(error)));
                    }
                });
                TakeoffResult takeoffResult = SdkCallbackBridge.await(future);

                String flightId = takeoffResult != null ? takeoffResult.getFlightId() : "unknown";
                session.setActiveFlightId(flightId);
                session.setFlightPhase(FlightPhase.TAKING_OFF);

                return buildTakeoffResult(
                        droneSn, dockSn, flightId, targetHeight, outOfControlAction,
                        securityTakeoffHeight, rthAltitude, maxSpeed, "");
            });
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_takeoff timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_takeoff SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            log.error("fh_takeoff error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 订阅机场位置，等待第一次有效回调后取消订阅并返回坐标。
     * 超时时间 {@value DOCK_LOCATION_TIMEOUT_SECONDS} 秒。
     *
     * @param dockSn 机场序列号
     * @return 机场位置，超时或 Dock 实例不存在时返回 null
     */
    private DockLocation fetchDockLocation(String dockSn) {
        Dock dock = sdkService.getDock(dockSn);
        if (dock == null) {
            log.warn("fh_takeoff: dock instance not found for sn={}", dockSn);
            return null;
        }

        // F2：getDockOsdModule() 可能返回 null（不支持 OSD 模块的机场型号）
        var osdModule = dock.getDockOsdModule();
        if (osdModule == null) {
            log.warn("fh_takeoff: dock OSD module not available for dock={}", dockSn);
            return null;
        }

        AtomicReference<DockLocation> locationRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = osdModule.listenDockLocation(location -> {
            locationRef.set(location);
            latch.countDown();
        });

        // F1：listenId < 0 表示订阅失败，回调永远不会触发，无需等待 latch
        if (listenId < 0) {
            log.warn("fh_takeoff: failed to subscribe dock location for dock={}, listenId={}", dockSn, listenId);
            return null;
        }

        try {
            boolean received = latch.await(DOCK_LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!received) {
                log.warn("fh_takeoff: dock location timeout after {}s for dock={}", DOCK_LOCATION_TIMEOUT_SECONDS, dockSn);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("fh_takeoff: dock location fetch interrupted for dock={}", dockSn);
        } finally {
            osdModule.cancelListenDockLocation(listenId);
        }
        return locationRef.get();
    }

    /**
     * 将字符串失控动作映射到 SDK 枚举。
     *
     * @param action 字符串失控动作（return_home / hover / continue）
     * @return 对应的 OutOfControlAction 枚举值
     */
    private OutOfControlAction mapOutOfControlAction(String action) {
        return switch (action) {
            case "hover" -> OutOfControlAction.HOVER;
            case "continue" -> OutOfControlAction.CONTINUE;
            default -> OutOfControlAction.RETURN_HOME;
        };
    }

    /**
     * 构建起飞成功响应（mock 和真实 SDK 路径复用）。
     * 当 FH_COCKPIT_URL 已配置时，自动在 data 中注入 cockpit_url 字段，
     * AI Agent 会将该链接展示给用户，用于实时观察无人机画面。
     *
     * @param droneSn              目标无人机 SN
     * @param dockSn               机场 SN（用于构建 cockpit URL，可为 null）
     * @param flightId             本次飞行 ID
     * @param targetHeight         目标悬停高度（米）
     * @param outOfControlAction   失控动作
     * @param securityTakeoffHeight 安全起飞高度（米）
     * @param rthAltitude          返航高度（米）
     * @param maxSpeed             最大速度（m/s）
     * @param messagePrefix        消息前缀（mock 模式传 "[MOCK] "，真实模式传 ""）
     * @return 成功结果 Map（含 next_steps 和可选的 cockpit_url）
     */
    private Map<String, Object> buildTakeoffResult(
            String droneSn, String dockSn, String flightId,
            double targetHeight, String outOfControlAction,
            int securityTakeoffHeight, int rthAltitude, int maxSpeed,
            String messagePrefix) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("flight_id", flightId);
        data.put("target_height", targetHeight);
        data.put("security_takeoff_height", securityTakeoffHeight);
        data.put("rth_altitude", rthAltitude);
        data.put("max_speed", maxSpeed);
        data.put("out_of_control_action", outOfControlAction);
        data.put("status", "taking_off");

        // 注入 cockpit 直播链接（仅当 FH_COCKPIT_URL 已配置时）
        String cockpitUrl = buildCockpitUrl(droneSn, dockSn);
        if (cockpitUrl != null) {
            data.put("cockpit_url", cockpitUrl);
        }

        // 构建消息文本，当 cockpit URL 可用时追加链接提示
        String message = messagePrefix + "Drone " + droneSn + " taking off to " + (int) targetHeight + "m"
                + " (fail-safe: " + outOfControlAction + ").";
        if (cockpitUrl != null) {
            message += " Live cockpit: " + cockpitUrl;
        }

        Map<String, Object> result = successResult(data, message);

        // next_steps 中也包含 cockpit 链接，确保 AI Agent 展示给用户
        List<String> nextSteps = new java.util.ArrayList<>();
        if (cockpitUrl != null) {
            nextSteps.add("🎥 Real-time cockpit view: " + cockpitUrl);
        }
        nextSteps.add("Drone will hover at " + (int) targetHeight + "m after takeoff");
        nextSteps.add("Call fh_flyto(...) to navigate to a target location");
        nextSteps.add("Call fh_return_home(drone_sn='" + droneSn + "', confirm=true) to return");
        result.put("next_steps", nextSteps);
        return result;
    }

    /**
     * 根据配置的 FH_COCKPIT_URL 构建驾驶舱直播链接。
     * URL 格式：{base}/cockpit?gateway_sn={dockSn}&drone_sn={droneSn}
     *
     * @param droneSn 目标无人机 SN
     * @param dockSn  机场 SN（作为 gateway_sn 参数），为 null 时不生成链接
     * @return cockpit URL 字符串，未配置或参数不足时返回 null
     */
    private String buildCockpitUrl(String droneSn, String dockSn) {
        String baseUrl = properties.getCockpit().getUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        if (droneSn == null || droneSn.isBlank()) {
            return null;
        }
        if (dockSn == null || dockSn.isBlank()) {
            return null;
        }
        // 移除末尾所有斜杠，防止 "http://host//" 场景产生双斜杠
        String base = baseUrl.replaceAll("/+$", "");
        return base + "/cockpit?gateway_sn="
                + java.net.URLEncoder.encode(dockSn, java.nio.charset.StandardCharsets.UTF_8)
                + "&drone_sn="
                + java.net.URLEncoder.encode(droneSn, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 校验目标高度参数范围。
     *
     * @param targetHeight 目标高度（米）
     * @return 错误响应 Map，校验通过时返回 null
     */
    private Map<String, Object> validateTargetHeight(double targetHeight) {
        if (targetHeight < TARGET_HEIGHT_MIN || targetHeight > TARGET_HEIGHT_MAX) {
            return errorResult(
                    "Invalid target_height: " + targetHeight,
                    "target_height must be between " + (int) TARGET_HEIGHT_MIN
                            + " and " + (int) TARGET_HEIGHT_MAX + " meters.");
        }
        return null;
    }
}
