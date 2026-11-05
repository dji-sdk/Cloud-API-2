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
import com.dji.flighthubsdk.bean.FlyToPointParams;
import com.dji.flighthubsdk.bean.FlyToTargetPoint;
import com.dji.flighthubsdk.bean.FlyToUpdateTargetPointsParams;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import com.dji.flighthubsdk.core.FlyToPointModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * fh_flyto — 命令无人机飞往目标 GPS 坐标，也可在飞行途中停止或更新目标坐标。
 * 前置条件：必须已获取飞行控制权且 DRC 链路已建立（fh_control_acquire + fh_drc_connect）。
 *
 * <p>安全等级：L2（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlytoTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlytoTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flyto";

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

    /** max_speed 参数名 */
    private static final String PARAM_MAX_SPEED = "max_speed";

    /** dry_run 参数名 */
    private static final String PARAM_DRY_RUN = "dry_run";

    /** 读取当前飞机位置作为更新目标点起点的超时时间（秒） */
    private static final int START_POINT_TIMEOUT_SECONDS = 2;

    /** 操作：开始指点飞行 */
    private static final String ACTION_START = "start";

    /** 操作：停止指点飞行 */
    private static final String ACTION_STOP = "stop";

    /** 操作：更新目标坐标 */
    private static final String ACTION_UPDATE = "update";

    /** 操作：查询飞行进度 */
    private static final String ACTION_PROGRESS = "progress";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            ACTION_START, ACTION_STOP, ACTION_UPDATE, ACTION_PROGRESS);

    /** 最大速度默认值（m/s） */
    private static final int DEFAULT_MAX_SPEED = 15;

    /** 本地内部错误码：SDK 目标点 API 与当前 jar 架构不匹配时返回 */
    private static final int ERR_INTERNAL = -1;

    /** arm64 版 SDK 的批量目标点设置方法 */
    private static final String METHOD_SET_POINTS = "setPoints";

    /** amd64 版 SDK 的单点坐标设置方法 */
    private static final String METHOD_SET_LATITUDE = "setLatitude";

    /** amd64 版 SDK 的单点坐标设置方法 */
    private static final String METHOD_SET_LONGITUDE = "setLongitude";

    /** amd64 版 SDK 的单点坐标设置方法 */
    private static final String METHOD_SET_HEIGHT = "setHeight";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhFlytoTool(SessionManager sessionManager, SdkService sdkService) {
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
                "Fly drone to a target GPS coordinate. Can also stop or update target mid-flight. "
                        + "If the user specifies a named location (e.g. 'fly to the dock', 'return to airport') "
                        + "instead of raw coordinates, call fh_dock_snapshot to get the dock's GPS coordinates "
                        + "rather than asking the user. "
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
                "description", "start: initiate fly-to | stop: halt fly-to | "
                        + "update: change target mid-flight | progress: query fly-to progress"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the fly-to command"
        ));
        props.put(PARAM_LATITUDE, Map.of(
                "type", "number",
                "description", "Target latitude in degrees (-90 to 90). Required when action=start or update."
        ));
        props.put(PARAM_LONGITUDE, Map.of(
                "type", "number",
                "description", "Target longitude in degrees (-180 to 180). Required when action=start or update."
        ));
        props.put(PARAM_HEIGHT, Map.of(
                "type", "number",
                "description", "Target height above ground in meters (>=2). Required when action=start or update."
        ));
        props.put(PARAM_MAX_SPEED, Map.of(
                "type", "integer",
                "description", "Maximum flight speed in m/s (default: 15). Also applied when action=update."
        ));
        props.put(PARAM_DRY_RUN, Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_ACTION, PARAM_CONFIRM));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行指点飞行操作。
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

        // 3.5 progress 是只读查询，不需要 DRC 链路和 confirm
        if (ACTION_PROGRESS.equals(action)) {
            return queryProgress(session, droneSn);
        }

        // 4. start/update 需要坐标：解析（含弹表单补输入）+ 校验
        //    先解析坐标再确认，确保 confirm 弹窗能展示有效坐标信息（安全修复后顺序）
        Object[] coordResult = resolveAndValidateCoords(args, action);
        if (coordResult[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> err = (Map<String, Object>) coordResult[0];
            return err;
        }
        double[] coords = (double[]) coordResult[0];

        // 5. 飞行操作前置人工确认：start/update 需先确认再抢控
        //    dry_run 模式无需确认（见 step 6）
        if (!isDryRun(args)) {
            Map<String, Object> preConfirmError = requireHumanConfirm(args, "flyto_" + action, droneSn,
                    buildConfirmMessage(droneSn, action, coords));
            if (preConfirmError != null) {
                return preConfirmError;
            }
        }

        // 6. dry_run 模式：仅做参数校验 + 返回预览，不抢控、不连 DRC，
        //    保证干跑不产生任何真实副作用（预览中的前置条件只列已实际检查的项）
        if (isDryRun(args)) {
            return buildDryRunResponse(droneSn, action, coords);
        }

        // 7. 确保 DRC 链路就绪（自动获取控制权 + 连接 DRC）— 确认通过后才执行
        Map<String, Object> drcError = ensureDrcLink(session, droneSn, sdkService);
        if (drcError != null) {
            return drcError;
        }

        // 8. 人工确认已在 step 5 完成

        // 9. Mock 模式 / 真实 SDK 分发
        return sdkService.isMockMode()
                ? executeInMockMode(session, droneSn, action, args, coords)
                : executeWithRealSdk(session, droneSn, action, args, coords);
    }

    /**
     * 构建 dry_run 预览响应。
     * 注意：此方法在 {@code ensureDrcLink} 之前调用，因此前置条件列表中不能声明
     * {@code drc_linked} —— 干跑不应触发抢控 / 建立 DRC 链路。
     *
     * @param droneSn 目标无人机 SN
     * @param action  操作类型（start / update / stop）
     * @param coords  已解析并校验的坐标（stop 时为 null）
     * @return dry_run 预览结果
     */
    private Map<String, Object> buildDryRunResponse(String droneSn, String action, double[] coords) {
        Map<String, Object> params = coords != null
                ? Map.of(PARAM_LATITUDE, coords[0], PARAM_LONGITUDE, coords[1], PARAM_HEIGHT, coords[2])
                : Map.of(PARAM_ACTION, action);
        String desc = coords != null
                ? String.format("Would fly drone %s to (%.6f, %.6f) at height %.0fm",
                        droneSn, coords[0], coords[1], coords[2])
                : "Would stop fly-to on drone " + droneSn;
        return dryRunResult(desc,
                buildDryRunPreview(TOOL_NAME, action, droneSn, params,
                        List.of("session_active", "security_level_allowed", "params_valid"),
                        getSecurityLevel(), requiresHumanConfirmation()));
    }

    /**
     * Mock 模式下执行指点飞行指令。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / stop / update）
     * @param args     原始参数 Map
     * @param coords   已解析的坐标（stop 时为 null）
     * @return 执行结果
     */
    private Map<String, Object> executeInMockMode(
            SessionContext session, String droneSn, String action,
            Map<String, Object> args, double[] coords) {
        if (ACTION_STOP.equals(action)) {
            return stopFlyToInMockMode(session, droneSn);
        }
        return startOrUpdateFlyToInMockMode(session, droneSn, action, args, coords);
    }

    /**
     * Mock 模式下停止指点飞行，无人机切换为悬停状态。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 成功响应
     */
    private Map<String, Object> stopFlyToInMockMode(SessionContext session, String droneSn) {
        session.setActiveFlyToId(null);
        session.setFlightPhase(FlightPhase.HOVERING);
        log.info("[MOCK] Fly-to stopped: drone={}", droneSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", ACTION_STOP);
        data.put("status", "hovering");
        data.put("_mock", true);

        return successResult(data, "Fly-to stopped, drone hovering");
    }

    /**
     * Mock 模式下开始或更新指点飞行。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / update）
     * @param args     原始参数 Map
     * @param coords   已解析并校验的坐标 {lat, lng, height}
     * @return 执行结果
     */
    private Map<String, Object> startOrUpdateFlyToInMockMode(
            SessionContext session, String droneSn, String action,
            Map<String, Object> args, double[] coords) {
        double lat = coords[0];
        double lng = coords[1];
        double height = coords[2];

        int maxSpeed = getIntArg(args, PARAM_MAX_SPEED, DEFAULT_MAX_SPEED);
        String flyToId = "flyto_" + System.currentTimeMillis();
        session.setActiveFlyToId(flyToId);
        session.setFlightPhase(FlightPhase.FLYING_TO);
        // 日志只输出 3 位小数坐标，避免全精度经纬度敏感信息外泄（返回响应仍保留全精度）
        log.info("[MOCK] Fly-to {}: drone={}, lat={}, lng={}, height={}m, flyToId={}",
                action, droneSn, String.format("%.3f", lat), String.format("%.3f", lng),
                height, flyToId);

        Map<String, Object> target = new LinkedHashMap<>();
        target.put("latitude", lat);
        target.put("longitude", lng);
        target.put("height", height);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("flyto_id", flyToId);
        data.put("action", action);
        data.put("target", target);
        data.put("max_speed", maxSpeed);
        data.put("_mock", true);

        return successResult(data,
                "[MOCK] Drone " + droneSn + " flying to (" + lat + ", " + lng + ") at " + (int) height + "m.");
    }

    /**
     * 真实 SDK 模式下执行指点飞行指令。
     * 使用 FlyToPointModule 的 start/stop/updateTargetPoints 方法。
     * 通过 executeWithDrcRetry 实现 DRC 链路故障时自动重连重试。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @param action   操作类型（start / stop / update）
     * @param args     原始参数 Map
     * @param coords   已解析的坐标（stop 时为 null）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(
            SessionContext session, String droneSn, String action,
            Map<String, Object> args, double[] coords) {
        try {
            return executeWithDrcRetry(session, droneSn, sdkService, () -> {
                Drone drone = sdkService.getDrone(droneSn);
                if (drone == null) {
                    return errorResult("Drone instance not found: " + droneSn,
                            "Ensure drone is online. Use fh_device_list to check.");
                }

                FlyToPointModule flyToModule = drone.getFlyToPointModule();
                if (flyToModule == null) {
                    return errorResult("FlyToPoint module not available for: " + droneSn,
                            "Drone may not support fly-to-point. Check device capability.");
                }

                if (ACTION_STOP.equals(action)) {
                    return stopFlyToWithRealSdk(session, droneSn, flyToModule);
                }
                if (ACTION_UPDATE.equals(action)) {
                    return updateFlyToWithRealSdk(session, droneSn, drone, flyToModule, args, coords);
                }
                return startFlyToWithRealSdk(session, droneSn, flyToModule, args, coords);
            });
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_flyto timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_flyto SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            log.error("fh_flyto error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 真实 SDK 模式下启动指点飞行。
     */
    @Generated
    private Map<String, Object> startFlyToWithRealSdk(
            SessionContext session, String droneSn, FlyToPointModule flyToModule,
            Map<String, Object> args, double[] coords) {
        double lat = coords[0];
        double lng = coords[1];
        double height = coords[2];
        int maxSpeed = getIntArg(args, PARAM_MAX_SPEED, DEFAULT_MAX_SPEED);

        FlyToPointParams params = new FlyToPointParams();
        applyFlyToTarget(params, lat, lng, height);
        params.setMaxSpeed(maxSpeed);

        CompletableFuture<String> future = new CompletableFuture<>();
        flyToModule.start(params, (error, flyToId) -> {
            if (error.isSuccess()) {
                future.complete(flyToId);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(
                                error.getServerCode(), formatSdkError(error)));
            }
        });
        String flyToId = SdkCallbackBridge.await(future);

        session.setActiveFlyToId(flyToId);
        session.setFlightPhase(FlightPhase.FLYING_TO);

        Map<String, Object> target = new LinkedHashMap<>();
        target.put("latitude", lat);
        target.put("longitude", lng);
        target.put("height", height);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("flyto_id", flyToId);
        data.put("action", ACTION_START);
        data.put("target", target);
        data.put("max_speed", maxSpeed);

        return successResult(data,
                "Drone " + droneSn + " flying to (" + lat + ", " + lng + ") at " + (int) height + "m.");
    }

    /**
     * 把目标点写入 SDK 参数对象，兼容 amd64 与 arm64 两个 SDK 包。
     *
     * <p>两个包版本号与 Maven 坐标完全相同，但 {@code FlyToPointParams} 的 API 不同：
     * arm64 包用 {@code setPoints(List<FlyToTargetPoint>)}，amd64 包用
     * {@code setLatitude/setLongitude/setHeight}，两边的 setter 互相不存在。
     * 因此这里反射探测：有 {@code setPoints} 用批量接口，否则退回单点接口。</p>
     *
     * <p>参数声明为 {@link Object} 且方法包级可见，是为了让单元测试能传入伪造的
     * 参数对象，从而覆盖两条分支——真实 SDK 类只提供其中一套 API，无法同时构造两种形态。</p>
     *
     * @param params SDK 参数对象（生产路径为 {@link FlyToPointParams}）
     * @param lat    目标纬度
     * @param lng    目标经度
     * @param height 目标相对高度（米）
     * @throws SdkCallbackBridge.SdkException 当前 jar 的 API 与预期均不匹配时抛出
     */
    void applyFlyToTarget(Object params, double lat, double lng, double height) {
        Method setPoints = findMethod(params, METHOD_SET_POINTS, List.class);
        if (setPoints != null) {
            FlyToTargetPoint targetPoint = new FlyToTargetPoint();
            targetPoint.setLatitude(lat);
            targetPoint.setLongitude(lng);
            targetPoint.setHeight(height);
            invokeQuietly(setPoints, params, List.of(targetPoint));
            return;
        }

        Method setLatitude = findMethod(params, METHOD_SET_LATITUDE, double.class);
        Method setLongitude = findMethod(params, METHOD_SET_LONGITUDE, double.class);
        Method setHeight = findMethod(params, METHOD_SET_HEIGHT, double.class);
        if (setLatitude == null || setLongitude == null || setHeight == null) {
            throw new SdkCallbackBridge.SdkException(ERR_INTERNAL,
                    "Unsupported fly-to SDK API: neither setPoints nor setLatitude/setLongitude/setHeight found");
        }
        invokeQuietly(setLatitude, params, lat);
        invokeQuietly(setLongitude, params, lng);
        invokeQuietly(setHeight, params, height);
    }

    /**
     * 在当前 SDK jar 提供的方法中查找签名匹配的一个，不存在返回 {@code null}。
     *
     * @param target       查找目标对象
     * @param name         方法名
     * @param paramType    首个参数类型
     * @return 匹配的方法，未找到时为 {@code null}
     */
    private Method findMethod(Object target, String name, Class<?> paramType) {
        try {
            return target.getClass().getMethod(name, paramType);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 反射调用已知存在的方法，把受检异常统一转换为 SDK 异常。
     *
     * <p>反射调用会把被调方法抛出的异常包装成 {@link InvocationTargetException}，
     * 这里拆包取出真实原因，避免错误信息里出现无意义的 {@code invocation failed}。</p>
     *
     * @param method 待调用方法
     * @param target 调用目标
     * @param arg    方法参数
     * @throws SdkCallbackBridge.SdkException 反射调用失败时抛出
     */
    private void invokeQuietly(Method method, Object target, Object arg) {
        try {
            method.invoke(target, arg);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            throw new SdkCallbackBridge.SdkException(ERR_INTERNAL,
                    "Failed to set fly-to target via " + method.getName() + ": " + cause.getMessage(), cause);
        } catch (ReflectiveOperationException e) {
            throw new SdkCallbackBridge.SdkException(ERR_INTERNAL,
                    "Failed to set fly-to target via " + method.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * 真实 SDK 模式下停止指点飞行。
     */
    @Generated
    private Map<String, Object> stopFlyToWithRealSdk(
            SessionContext session, String droneSn, FlyToPointModule flyToModule) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        flyToModule.drcStop(error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(
                                error.getServerCode(), formatSdkError(error)));
            }
        });
        SdkCallbackBridge.await(future);

        session.setActiveFlyToId(null);
        session.setFlightPhase(FlightPhase.HOVERING);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", ACTION_STOP);
        data.put("status", "hovering");

        return successResult(data, "Fly-to stopped, drone hovering");
    }

    /**
     * 真实 SDK 模式下更新指点飞行目标坐标。
     *
     * <p>注意 SDK 转换器 {@code FlightHubSdkConvert.toNativeFlyToUpdateTargetPointsParams}
     * 的行为：{@code fly_to_id} 与 {@code max_speed} 是无条件下发的，
     * 而 {@code start_point} 仅在非 null 时下发。因此：
     * <ul>
     *   <li>必须显式设置 maxSpeed，否则会给云端下发 0，服务端判定为非法参数（FTP-023/011）；</li>
     *   <li>startPoint 取飞机当前 OSD 位置，读不到时留空（转换器会跳过该字段，不影响更新）。</li>
     * </ul>
     *
     * @param session      当前 session
     * @param droneSn      目标无人机 SN
     * @param drone        无人机实例（用于读取当前定位）
     * @param flyToModule  指点飞行模块
     * @param args         原始参数 Map
     * @param coords       已解析并校验的新目标坐标 {lat, lng, height}
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> updateFlyToWithRealSdk(
            SessionContext session, String droneSn, Drone drone, FlyToPointModule flyToModule,
            Map<String, Object> args, double[] coords) {
        String activeFlyToId = session.getActiveFlyToId();
        if (activeFlyToId == null || activeFlyToId.isBlank()) {
            return errorResult("No active fly-to session",
                    "Must call fh_flyto(action='start') before action='update'. "
                    + "Start a fly-to first, then update the target.");
        }

        double lat = coords[0];
        double lng = coords[1];
        double height = coords[2];
        // 未显式指定时沿用 start 的默认速度：转换器无条件下发 max_speed，传 0 会被服务端拒绝
        int maxSpeed = getIntArg(args, PARAM_MAX_SPEED, DEFAULT_MAX_SPEED);

        FlyToUpdateTargetPointsParams updateParams = new FlyToUpdateTargetPointsParams();
        updateParams.setFlyToId(activeFlyToId);
        updateParams.setMaxSpeed(maxSpeed);
        // 起点取飞机当前位置；读不到时留空，转换器会跳过 start_point 字段
        double[] startPoint = fetchAircraftPosition(drone);
        if (startPoint != null) {
            updateParams.setStartPoint(buildTargetPoint(startPoint[0], startPoint[1], startPoint[2]));
        }
        updateParams.setPoints(List.of(buildTargetPoint(lat, lng, height)));

        CompletableFuture<Void> future = new CompletableFuture<>();
        flyToModule.updateTargetPoints(updateParams, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(
                                error.getServerCode(), formatSdkError(error)));
            }
        });
        SdkCallbackBridge.await(future);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("flyto_id", activeFlyToId);
        data.put("action", ACTION_UPDATE);
        data.put("target", toCoordinateMap(lat, lng, height));
        data.put("max_speed", maxSpeed);
        if (startPoint != null) {
            data.put("start_point", toCoordinateMap(startPoint[0], startPoint[1], startPoint[2]));
        }

        return successResult(data,
                "Fly-to target updated to (" + lat + ", " + lng + ") at " + (int) height + "m.");
    }

    /**
     * 构造 SDK 目标点对象。
     *
     * @param lat    纬度
     * @param lng    经度
     * @param height 高度（米）
     * @return FlyToTargetPoint 实例
     */
    private FlyToTargetPoint buildTargetPoint(double lat, double lng, double height) {
        FlyToTargetPoint point = new FlyToTargetPoint();
        point.setLatitude(lat);
        point.setLongitude(lng);
        point.setHeight(height);
        return point;
    }

    /**
     * 把坐标转换为响应 Map（保持 lat/lng/height 字段名稳定）。
     *
     * @param lat    纬度
     * @param lng    经度
     * @param height 高度（米）
     * @return 坐标 Map
     */
    private Map<String, Object> toCoordinateMap(double lat, double lng, double height) {
        Map<String, Object> map = new LinkedHashMap<>(3);
        map.put(PARAM_LATITUDE, lat);
        map.put(PARAM_LONGITUDE, lng);
        map.put(PARAM_HEIGHT, height);
        return map;
    }

    /**
     * 读取飞机当前位置（一次性订阅 + 超时），用于填充 update 的 start_point。
     * SDK 的 OSD 只有订阅式接口，没有同步 getter，故沿用 CountDownLatch 模式。
     *
     * @param drone 无人机实例
     * @return {lat, lng, height}；模块不可用或超时返回 null
     */
    private double[] fetchAircraftPosition(Drone drone) {
        DroneOsdModule osdModule = drone.getDroneOsdModule();
        if (osdModule == null) {
            log.warn("fetchAircraftPosition: DroneOsdModule unavailable, start_point will be omitted");
            return null;
        }

        AtomicReference<double[]> posRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        int listenId = osdModule.listenAircraftLocation(location -> {
            posRef.set(new double[]{
                    location.getLatitude(), location.getLongitude(), location.getHeight()});
            latch.countDown();
        });
        if (listenId < 0) {
            log.warn("fetchAircraftPosition: listenAircraftLocation failed, listenId={}", listenId);
            return null;
        }

        try {
            boolean received = latch.await(START_POINT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!received) {
                log.warn("fetchAircraftPosition: timeout waiting for aircraft location");
            }
            return received ? posRef.get() : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("fetchAircraftPosition: interrupted");
            return null;
        } finally {
            osdModule.cancelListenAircraftLocation(listenId);
        }
    }

    /**
     * 解析并校验坐标（提取自 execute 以满足 40 行限制）。
     * stop 动作不需要坐标，直接返回 null；其他动作解析+校验。
     *
     * @param args   原始参数
     * @param action 当前操作
     * @return 单元素数组：{double[] coords} 或 {Map errorResult}
     */
    private Object[] resolveAndValidateCoords(Map<String, Object> args, String action) {
        if (ACTION_STOP.equals(action)) {
            return new Object[]{null};
        }
        double[] coords = resolveCoordinates(args);
        Map<String, Object> coordError = validateGpsCoordinates(coords[0], coords[1], coords[2]);
        if (coordError != null) {
            return new Object[]{coordError};
        }
        return new Object[]{coords};
    }

    /**
     * 解析目标坐标：入参齐全时直接返回；缺失时优先向真人弹结构化表单补输入，
     * 未补到则返回 NaN（由 {@link #validateGpsCoordinates} 报错）。
     *
     * @param args 原始参数 Map
     * @return {lat, lng, height} 三元数组
     */
    private double[] resolveCoordinates(Map<String, Object> args) {
        double lat = getDoubleArg(args, PARAM_LATITUDE, Double.NaN);
        double lng = getDoubleArg(args, PARAM_LONGITUDE, Double.NaN);
        double height = getDoubleArg(args, PARAM_HEIGHT, Double.NaN);

        boolean missing = Double.isNaN(lat) || Double.isNaN(lng) || Double.isNaN(height);
        if (!missing) {
            return new double[]{lat, lng, height};
        }

        Map<String, Object> input = requireInput(
                "指点飞行需要目标坐标，请填写纬度、经度和高度。",
                buildCoordSchema());
        if (input == null) {
            return new double[]{lat, lng, height};
        }

        return new double[]{
                toDouble(input.get(PARAM_LATITUDE), lat),
                toDouble(input.get(PARAM_LONGITUDE), lng),
                toDouble(input.get(PARAM_HEIGHT), height)
        };
    }

    /**
     * 构建指点飞行坐标补输入的 requestedSchema（type:object + 三个 number 字段）。
     *
     * @return 结构化坐标表单 schema
     */
    private Map<String, Object> buildCoordSchema() {
        Map<String, Object> props = new LinkedHashMap<>(3);
        props.put(PARAM_LATITUDE, Map.of(
                "type", "number", "title", "纬度",
                "description", "目标纬度，范围 -90 ~ 90"));
        props.put(PARAM_LONGITUDE, Map.of(
                "type", "number", "title", "经度",
                "description", "目标经度，范围 -180 ~ 180"));
        props.put(PARAM_HEIGHT, Map.of(
                "type", "number", "title", "高度（米）",
                "description", "目标飞行高度，范围 2 ~ 10000 米"));

        Map<String, Object> schema = new LinkedHashMap<>(3);
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_LATITUDE, PARAM_LONGITUDE, PARAM_HEIGHT));
        return schema;
    }

    /**
     * 构建指点飞行人工确认提示。
     *
     * @param droneSn 目标无人机 SN
     * @param action  操作类型（start / stop / update）
     * @param coords  已解析坐标（stop 时为 null）
     * @return 面向真人的确认提示
     */
    private String buildConfirmMessage(String droneSn, String action, double[] coords) {
        if (ACTION_STOP.equals(action)) {
            return String.format("⚠️ 无人机 %s 即将停止指点飞行（悬停），是否确认？", droneSn);
        }
        return String.format(
                "⚠️ 无人机 %s 即将飞往坐标 (%.6f, %.6f)，高度 %.1f 米。是否确认？",
                droneSn, coords[0], coords[1], coords[2]);
    }

    /**
     * 查询指点飞行进度（只读，无需 DRC 链路和 confirm）。
     * Mock 模式返回模拟进度；真实模式依赖 session 状态。
     *
     * @param session 当前 session
     * @param droneSn 目标无人机 SN
     * @return 飞行进度信息
     */
    private Map<String, Object> queryProgress(SessionContext session, String droneSn) {
        String activeFlyToId = session.getActiveFlyToId();
        FlightPhase phase = session.getFlightPhase();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("action", ACTION_PROGRESS);
        data.put("flyto_id", activeFlyToId);
        data.put("flight_phase", phase != null ? phase.name() : "UNKNOWN");

        if (activeFlyToId == null || phase != FlightPhase.FLYING_TO) {
            data.put("active", false);
            data.put("status", "idle");
            return successResult(data, "No active fly-to in progress for " + droneSn);
        }

        // Mock 进度数据（真实模式也返回 session 状态 + OSD 遥测数据需后续集成）
        data.put("active", true);
        data.put("status", "in_progress");
        if (sdkService.isMockMode()) {
            data.put("progress_percent", 45);
            data.put("remaining_distance_m", 120.5);
            data.put("eta_seconds", 8);
            data.put("_mock", true);
        }

        return successResult(data,
                "Fly-to in progress: " + activeFlyToId + " (phase=" + phase.name() + ")");
    }
}
