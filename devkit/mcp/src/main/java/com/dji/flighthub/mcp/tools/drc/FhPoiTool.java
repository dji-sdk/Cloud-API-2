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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.POIEnterParams;
import com.dji.flighthubsdk.bean.Waypoint;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
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
 * fh_poi — 命令无人机以指定半径和速度围绕兴趣点（POI）进行环绕飞行，也可停止正在进行的环绕。
 * 前置条件：必须已获取飞行控制权且 DRC 链路已建立（fh_control_acquire + fh_drc_connect）。
 *
 * <p>安全等级：L2（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhPoiTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhPoiTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_poi";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** action 参数名 */
    private static final String PARAM_ACTION = "action";

    /** confirm 参数名 */
    private static final String PARAM_CONFIRM = "confirm";

    /** latitude 参数名 */
    private static final String PARAM_LATITUDE = "latitude";

    /** longitude 参数名 */
    private static final String PARAM_LONGITUDE = "longitude";

    /** height 参数名 */
    private static final String PARAM_HEIGHT = "height";

    /** radius 参数名 */
    private static final String PARAM_RADIUS = "radius";

    /** speed 参数名 */
    private static final String PARAM_SPEED = "speed";

    /** direction 参数名 */
    private static final String PARAM_DIRECTION = "direction";

    /** 操作：开始环绕飞行 */
    private static final String ACTION_START = "start";

    /** 操作：停止环绕飞行 */
    private static final String ACTION_STOP = "stop";

    /** 操作：查询环绕状态 */
    private static final String ACTION_STATUS = "status";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_START, ACTION_STOP, ACTION_STATUS);

    /** 顺时针方向 */
    private static final String DIRECTION_CLOCKWISE = "clockwise";

    /** 逆时针方向 */
    private static final String DIRECTION_COUNTER_CLOCKWISE = "counter_clockwise";

    /** 有效方向列表 */
    private static final List<String> VALID_DIRECTIONS = List.of(DIRECTION_CLOCKWISE, DIRECTION_COUNTER_CLOCKWISE);

    /** 环绕半径最小值（米） */
    private static final double RADIUS_MIN = 5.0;

    /** 环绕半径最大值（米） */
    private static final double RADIUS_MAX = 500.0;

    /** 环绕速度最小值（m/s） */
    private static final double SPEED_MIN = 1.0;

    /** 环绕速度最大值（m/s） */
    private static final double SPEED_MAX = 15.0;

    /** 环绕速度默认值（m/s） */
    private static final double DEFAULT_SPEED = 5.0;

    /** 默认环绕方向 */
    private static final String DEFAULT_DIRECTION = DIRECTION_CLOCKWISE;

    /** Mock 无人机纬度（与 FhOsdSnapshotTool 一致） */
    private static final double MOCK_DRONE_LATITUDE = 22.5431;

    /** Mock 无人机经度（与 FhOsdSnapshotTool 一致） */
    private static final double MOCK_DRONE_LONGITUDE = 113.9465;

    /** 位置获取超时（秒） */
    private static final int POSITION_FETCH_TIMEOUT_SECONDS = 3;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhPoiTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
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
                "Orbit around a point of interest (POI). Drone circles the target coordinate "
                        + "at specified radius and speed. Use action=status to query orbit state without DRC link. "
                        + "CRITICAL CONSTRAINT: The drone must NOT be at or near the POI center when starting. "
                        + "The drone needs to be approximately 'radius' meters away from center to enter orbit. "
                        + "If the drone is directly above the center point, the device rejects the command "
                        + "(error 325003) because it cannot compute an orbit trajectory from radius=0. "
                        + "Recommended usage: set POI center to a ground landmark (e.g. dock position, building), "
                        + "NOT the drone's current GPS coordinates. "
                        + "If error 325003 persists: (1) choose a center point far from drone, or "
                        + "(2) use fh_fly_to to reposition drone away from center first. "
                        + "Prerequisites: fh_control_acquire + fh_drc_connect must succeed first.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "start: begin orbiting | stop: end orbit and hover | status: query orbit state"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the POI orbit command"
        ));
        props.put(PARAM_LATITUDE, Map.of(
                "type", "number",
                "description", "POI center latitude in degrees (-90 to 90). Required when action=start. "
                        + "IMPORTANT: Must NOT be the drone's current position — choose a ground target "
                        + "(dock, building, vehicle) for the drone to orbit around."
        ));
        props.put(PARAM_LONGITUDE, Map.of(
                "type", "number",
                "description", "POI center longitude in degrees (-180 to 180). Required when action=start. "
                        + "IMPORTANT: Must NOT be the drone's current position."
        ));
        props.put(PARAM_HEIGHT, Map.of(
                "type", "number",
                "description", "Drone altitude during orbit in meters (>=2). Required when action=start."
        ));
        props.put(PARAM_RADIUS, Map.of(
                "type", "number",
                "description", "Orbit radius in meters (5 to 500). Required when action=start. "
                        + "The drone should be approximately this distance from the POI center before starting. "
                        + "If drone is closer than radius, it will fly outward to reach orbit path first."
        ));
        props.put(PARAM_SPEED, Map.of(
                "type", "number",
                "description", "Orbit speed in m/s (1 to 15, default: 5)"
        ));
        props.put(PARAM_DIRECTION, Map.of(
                "type", "string",
                "enum", VALID_DIRECTIONS,
                "description", "Orbit direction: clockwise | counter_clockwise (default: clockwise)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_ACTION, PARAM_CONFIRM));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行环绕飞行操作。
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

        // 3. 校验 action 参数（先于 DRC / confirm）
        String action = getStringArg(args, PARAM_ACTION, ACTION_START);
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult(
                    "Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 3.5 status 是只读查询，不需要 DRC 链路和 confirm
        if (ACTION_STATUS.equals(action)) {
            return queryPoiStatus(session, droneSn);
        }

        // 4. 飞行操作前置人工确认：环绕飞行先确认再抢控
        //    dry_run 模式无需确认（见 step 5）
        if (!isDryRun(args)) {
            Map<String, Object> preConfirmError = requireHumanConfirm(args, "poi_" + action, droneSn,
                    String.format("⚠️ 无人机 %s 即将执行兴趣点环绕（%s），是否确认？", droneSn, action));
            if (preConfirmError != null) {
                return preConfirmError;
            }
        }

        // 5. 确保 DRC 链路就绪（自动获取控制权 + 连接 DRC）— 确认通过后才执行
        Map<String, Object> drcError = ensureDrcLink(session, droneSn, sdkService);
        if (drcError != null) {
            return drcError;
        }

        // 6. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute POI orbit '" + action + "' on drone " + droneSn,
                    buildDryRunPreview(TOOL_NAME, action, droneSn,
                            Map.of("action", action),
                            List.of("session_active", "security_level_allowed", "drc_linked", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 7. 人工确认已在 step 4 完成

        // 8. Mock 模式：直接执行
        if (sdkService.isMockMode()) {
            return executeInMockMode(session, droneSn, action, args);
        }

        // 9. 真实 SDK 调用（生产路径，需要真实 SDK 环境）
        return executeWithRealSdk(session, droneSn, action, args);
    }

    /**
     * 查询 POI 环绕状态。只读操作，不需要 DRC 链路和 confirm。
     * 返回 session 中记录的飞行阶段及 mock 遥测数据。
     *
     * @param session 当前 session
     * @param droneSn 目标无人机 SN
     * @return 环绕状态结果
     */
    private Map<String, Object> queryPoiStatus(SessionContext session, String droneSn) {
        FlightPhase phase = session.getFlightPhase();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", ACTION_STATUS);
        data.put("flight_phase", phase != null ? phase.name() : "UNKNOWN");

        if (phase != FlightPhase.ORBITING) {
            data.put("active", false);
            data.put("status", "idle");
            return successResult(data, "No active POI orbit in progress for " + droneSn);
        }

        data.put("active", true);
        data.put("status", "orbiting");

        if (sdkService.isMockMode()) {
            data.put("current_angle_deg", 135.0);
            data.put("laps_completed", 2);
            data.put("elapsed_seconds", 96);
            data.put("_mock", true);
        }

        return successResult(data,
                "POI orbit in progress for " + droneSn + " (phase=" + phase.name() + ")");
    }

    /**
     * Mock 模式下执行 POI 环绕指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / stop）
     * @param args     原始参数 Map
     * @return 执行结果
     */
    private Map<String, Object> executeInMockMode(
            SessionContext session, String droneSn, String action, Map<String, Object> args) {
        if (ACTION_STOP.equals(action)) {
            return stopOrbitInMockMode(session, droneSn);
        }
        return startOrbitInMockMode(session, droneSn, args);
    }

    /**
     * Mock 模式下停止环绕飞行，无人机切换为悬停状态。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 成功响应
     */
    private Map<String, Object> stopOrbitInMockMode(SessionContext session, String droneSn) {
        session.setFlightPhase(FlightPhase.HOVERING);
        log.info("[MOCK] POI orbit stopped: drone={}", droneSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", ACTION_STOP);
        data.put("status", "hovering");
        data.put("_mock", true);

        return successResult(data, "POI orbit stopped, drone hovering");
    }

    /**
     * Mock 模式下开始环绕飞行，校验所有入参后设置 session 飞行阶段为 ORBITING。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param args     原始参数 Map
     * @return 执行结果
     */
    private Map<String, Object> startOrbitInMockMode(
            SessionContext session, String droneSn, Map<String, Object> args) {
        // 校验坐标参数
        double lat = getDoubleArg(args, PARAM_LATITUDE, Double.NaN);
        double lng = getDoubleArg(args, PARAM_LONGITUDE, Double.NaN);
        double height = getDoubleArg(args, PARAM_HEIGHT, Double.NaN);

        Map<String, Object> coordError = validateGpsCoordinates(lat, lng, height);
        if (coordError != null) {
            return coordError;
        }

        // 校验半径参数（5~500 米）
        double radius = getDoubleArg(args, PARAM_RADIUS, Double.NaN);
        if (Double.isNaN(radius) || radius < RADIUS_MIN || radius > RADIUS_MAX) {
            return errorResult(
                    "Invalid radius: " + radius,
                    "radius must be between " + (int) RADIUS_MIN + " and " + (int) RADIUS_MAX + " meters.");
        }

        // 前置距离校验：无人机不能在 POI 中心点附近
        Map<String, Object> distError = checkDroneDistanceFromPoi(
                MOCK_DRONE_LATITUDE, MOCK_DRONE_LONGITUDE, lat, lng, radius);
        if (distError != null) {
            return distError;
        }

        // 校验速度参数（1~15 m/s）
        double speed = getDoubleArg(args, PARAM_SPEED, DEFAULT_SPEED);
        if (speed < SPEED_MIN || speed > SPEED_MAX) {
            return errorResult(
                    "Invalid speed: " + speed,
                    "speed must be between " + (int) SPEED_MIN + " and " + (int) SPEED_MAX + " m/s.");
        }

        // 校验方向参数
        String direction = getStringArg(args, PARAM_DIRECTION, DEFAULT_DIRECTION);
        if (!VALID_DIRECTIONS.contains(direction)) {
            return errorResult(
                    "Invalid direction: " + direction,
                    "direction must be one of: " + VALID_DIRECTIONS);
        }

        String orbitId = "orbit_" + System.currentTimeMillis();
        session.setFlightPhase(FlightPhase.ORBITING);
        log.info("[MOCK] POI orbit started: drone={}, lat={}, lng={}, height={}m, radius={}m, orbitId={}",
                droneSn, lat, lng, height, radius, orbitId);

        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("latitude", lat);
        poi.put("longitude", lng);
        poi.put("height", height);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("orbit_id", orbitId);
        data.put("action", ACTION_START);
        data.put("poi", poi);
        data.put("radius", radius);
        data.put("speed", speed);
        data.put("direction", direction);
        data.put("_mock", true);

        return successResult(data,
                "[MOCK] Drone " + droneSn + " orbiting POI at (" + lat + ", " + lng
                        + ") radius=" + (int) radius + "m speed=" + speed + "m/s " + direction + ".");
    }

    /**
     * 真实 SDK 模式下执行 POI 环绕指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / stop）
     * @param args     原始参数 Map
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(
            SessionContext session, String droneSn, String action, Map<String, Object> args) {
        try {
            return executeWithDrcRetry(session, droneSn, sdkService, () -> {
                Drone drone = sdkService.getDrone(droneSn);
                if (drone == null) {
                    return errorResult("Drone instance not found: " + droneSn,
                            "Ensure drone is online. Use fh_device_list to check.");
                }

                if (ACTION_STOP.equals(action)) {
                    return stopPoiWithSdk(session, droneSn, drone);
                } else {
                    return startPoiWithSdk(session, droneSn, drone, args);
                }
            });
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_poi timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_poi SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    buildSdkErrorHint(e.getErrorCode()));
        } catch (Exception e) {
            log.error("fh_poi error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 通过 SDK 停止 POI 环绕。
     */
    private Map<String, Object> stopPoiWithSdk(
            SessionContext session, String droneSn, Drone drone) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        drone.getPoiModule().exit(error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(
                                error.getServerCode(), formatSdkError(error)));
            }
        });
        SdkCallbackBridge.await(future);

        session.setFlightPhase(FlightPhase.HOVERING);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", ACTION_STOP);
        data.put("status", "hovering");

        return successResult(data, "POI orbit stopped, drone hovering");
    }

    /**
     * 通过 SDK 开始 POI 环绕。
     */
    private Map<String, Object> startPoiWithSdk(
            SessionContext session, String droneSn, Drone drone, Map<String, Object> args) {
        // 参数校验（与 mock 路径相同）
        double lat = getDoubleArg(args, PARAM_LATITUDE, Double.NaN);
        double lng = getDoubleArg(args, PARAM_LONGITUDE, Double.NaN);
        double height = getDoubleArg(args, PARAM_HEIGHT, Double.NaN);

        Map<String, Object> coordError = validateGpsCoordinates(lat, lng, height);
        if (coordError != null) {
            return coordError;
        }

        double radius = getDoubleArg(args, PARAM_RADIUS, Double.NaN);
        if (Double.isNaN(radius) || radius < RADIUS_MIN || radius > RADIUS_MAX) {
            return errorResult("Invalid radius: " + radius,
                    "radius must be between " + (int) RADIUS_MIN + " and " + (int) RADIUS_MAX + " meters.");
        }

        // 前置距离校验：获取无人机实时位置并计算与 POI 中心的距离
        double[] dronePos = fetchDronePosition(drone);
        if (dronePos != null) {
            Map<String, Object> distError = checkDroneDistanceFromPoi(
                    dronePos[0], dronePos[1], lat, lng, radius);
            if (distError != null) {
                return distError;
            }
        } else {
            log.warn("无法获取无人机位置，跳过前置距离校验: drone={}", droneSn);
        }

        double speed = getDoubleArg(args, PARAM_SPEED, DEFAULT_SPEED);
        if (speed < SPEED_MIN || speed > SPEED_MAX) {
            return errorResult("Invalid speed: " + speed,
                    "speed must be between " + (int) SPEED_MIN + " and " + (int) SPEED_MAX + " m/s.");
        }

        String payloadIndex = getStringArg(args, "payload_index", "0");

        // 构建 SDK 参数
        Waypoint centerPoint = new Waypoint(lat, lng, height);
        POIEnterParams params = new POIEnterParams();
        params.setPayloadIndex(payloadIndex);
        params.setPoiCenterPoint(centerPoint);
        int actualSpeed = (int) speed;
        int actualRadius = (int) radius;
        params.setSpeed(actualSpeed);
        params.setCircleRadius(actualRadius);

        CompletableFuture<String> future = new CompletableFuture<>();
        drone.getPoiModule().enter(params, (error, taskUuid) -> {
            if (error.isSuccess()) {
                future.complete(taskUuid);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(
                                error.getServerCode(), formatSdkError(error)));
            }
        });
        String orbitId = SdkCallbackBridge.await(future);

        session.setFlightPhase(FlightPhase.ORBITING);

        String direction = getStringArg(args, PARAM_DIRECTION, DEFAULT_DIRECTION);
        Map<String, Object> poi = new LinkedHashMap<>();
        poi.put("latitude", lat);
        poi.put("longitude", lng);
        poi.put("height", height);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("orbit_id", orbitId != null ? orbitId : "unknown");
        data.put("action", ACTION_START);
        data.put("poi", poi);
        data.put("radius", actualRadius);
        data.put("speed", actualSpeed);
        data.put("direction", direction);

        return successResult(data,
                "Drone " + droneSn + " orbiting POI at (" + lat + ", " + lng
                        + ") radius=" + actualRadius + "m speed=" + actualSpeed + "m/s " + direction + ".");
    }

    /** 325003 错误码：指令响应失败（常见原因是无人机在 POI 中心点正上方） */
    private static final int ERROR_CODE_COMMAND_REJECTED = 325003;

    /**
     * 根据 SDK 错误码构建针对性的排障提示。
     *
     * @param errorCode SDK 返回的错误码
     * @return 面向 AI Agent 的排障建议
     */
    private String buildSdkErrorHint(int errorCode) {
        if (errorCode == ERROR_CODE_COMMAND_REJECTED) {
            return "Error 325003: Command rejected by device. Most likely cause: the drone is at or near "
                    + "the POI center point, making orbit geometry impossible. "
                    + "Solution: (1) Set POI center to a different location (e.g. dock/takeoff point) "
                    + "so the drone is already ~radius meters away, or "
                    + "(2) Use fh_fly_to to move drone away from center first, then retry fh_poi.";
        }
        return "Check drone state and retry. If persistent, verify drone is not at the POI center point.";
    }

    /**
     * 检查无人机到 POI 中心的距离是否满足环绕要求。
     * 当无人机在 POI 中心附近（距离 < 半径）时，返回错误并引导 AI 重新定位。
     *
     * @param droneLat 无人机纬度
     * @param droneLon 无人机经度
     * @param poiLat   POI 中心纬度
     * @param poiLon   POI 中心经度
     * @param radius   环绕半径（米）
     * @return null=校验通过; 非 null=距离不足的错误响应
     */
    private Map<String, Object> checkDroneDistanceFromPoi(
            double droneLat, double droneLon, double poiLat, double poiLon, double radius) {
        double distance = haversineDistance(droneLat, droneLon, poiLat, poiLon);
        if (distance < radius) {
            log.warn("无人机距 POI 中心过近: distance={}m < radius={}m, drone=({}, {}), poi=({}, {})",
                    String.format("%.1f", distance), String.format("%.0f", radius),
                    droneLat, droneLon, poiLat, poiLon);
            return errorResult(
                    String.format("Drone is too close to POI center: %.1fm away, but radius is %.0fm. "
                            + "The drone must be outside the orbit circle to start.", distance, radius),
                    String.format("Use fh_fly_to to reposition the drone at least %.0fm away from the POI center "
                            + "(%.6f, %.6f) before starting the orbit. "
                            + "Alternatively, choose a POI center that is far from the drone's current position.",
                            radius, poiLat, poiLon));
        }
        return null;
    }

    /**
     * 通过 SDK 一次性订阅获取无人机当前 GPS 位置。
     * 使用 CountDownLatch + 超时模式，避免阻塞过久。
     *
     * @param drone SDK Drone 实例
     * @return [latitude, longitude] 数组；获取失败返回 null
     */
    @Generated
    private double[] fetchDronePosition(Drone drone) {
        DroneOsdModule osdModule = drone.getDroneOsdModule();
        if (osdModule == null) {
            return null;
        }

        AtomicReference<double[]> posRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = osdModule.listenAircraftLocation(location -> {
            posRef.set(new double[]{location.getLatitude(), location.getLongitude()});
            latch.countDown();
        });

        if (listenId < 0) {
            return null;
        }

        try {
            boolean received = latch.await(POSITION_FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return received ? posRef.get() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            osdModule.cancelListenAircraftLocation(listenId);
        }
    }
}
