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

package com.dji.flighthub.mcp.tools.wayline;

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.TestFlightModule;
import com.dji.flighthubsdk.generated.CreateTestFlightTaskParams;
import com.dji.flighthubsdk.generated.CreateTestFlightTaskResponse;
import com.dji.flighthubsdk.generated.GenerateTryFlightWaylineParams;
import com.dji.flighthubsdk.generated.GenerateTryFlightWaylineResponse;
import com.dji.flighthubsdk.generated.TryFlightWaylinePoint;
import com.dji.flighthubsdk.value.OutOfControlAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_testflight — 试飞航线生成 + 试飞任务创建（{@code core.TestFlightModule}，按无人机维度获取）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>generate_wayline — 根据目标点/观察点生成试飞航线，返回 {@code uuid}（可逆操作，
 *       不触发真实飞行，仅在云端生成航线数据）</li>
 *   <li>create_task — 用 generate_wayline 返回的 {@code uuid} 作为 {@code wayline_uuid}
 *       创建试飞任务，**会触发无人机真实起飞执行试飞航线**</li>
 * </ul>
 *
 * <p>调用顺序：必须先调用 generate_wayline 拿到 {@code uuid}，再作为
 * {@code wayline_uuid} 传入 create_task。
 *
 * <p>安全等级：generate_wayline 为 L1（{@link SecurityLevel#REVERSIBLE}，需 confirm=true）；
 * create_task 为 L2（{@link SecurityLevel#FLIGHT}，触发真实飞行，需 confirm=true）。
 *
 * <p>注意：{@code generated.*} 参数/响应均为 SWIG 原生对象，没有 POJO 构造器，必须用
 * getter/setter 手工赋值，并在返回前手工转换为纯 Map（参考 {@code FhDeploySubscribeTool}）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhTestflightTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhTestflightTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_testflight";

    // ── action 常量 ──

    private static final String ACTION_GENERATE_WAYLINE = "generate_wayline";
    private static final String ACTION_CREATE_TASK = "create_task";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_GENERATE_WAYLINE, ACTION_CREATE_TASK);

    // ── 参数名常量（通用） ──

    private static final String PARAM_ACTION = "action";
    private static final String PARAM_PROJECT_ID = "project_id";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_CONFIRM = "confirm";
    private static final String PARAM_SN = "sn";

    // ── 参数名常量（generate_wayline） ──

    private static final String PARAM_DEVICE_TYPE = "device_type";
    private static final String PARAM_TARGET_POINT = "target_point";
    private static final String PARAM_OBSERVER_POINT = "observer_point";
    private static final String PARAM_LNG = "lng";
    private static final String PARAM_LAT = "lat";
    private static final String PARAM_ALT = "alt";

    // ── 参数名常量（create_task） ──

    private static final String PARAM_WAYLINE_UUID = "wayline_uuid";
    private static final String PARAM_TASK_TYPE = "task_type";
    private static final String PARAM_OUT_OF_CONTROL_ACTION_IN_FLIGHT = "out_of_control_action_in_flight";
    private static final String PARAM_LANDING_DOCK_SN = "landing_dock_sn";
    private static final String PARAM_TIME_ZONE = "time_zone";
    private static final String PARAM_DEFAULT_NAME = "default_name";
    private static final String PARAM_RESUMABLE_STATUS = "resumable_status";
    private static final String PARAM_MEDIA_FOLDER_ID_PATHS = "media_folder_id_paths";
    private static final String PARAM_RTH_MODE = "rth_mode";
    private static final String PARAM_REPEAT_TYPE = "repeat_type";
    private static final String PARAM_WAYLINE_PRECISION_TYPE = "wayline_precision_type";
    private static final String PARAM_RTH_ALTITUDE = "rth_altitude";
    private static final String PARAM_WAYLINE_DURATION = "wayline_duration";
    private static final String PARAM_CREATE_TASK_DEVICE_TYPE = "create_task_device_type";

    // ── create_task 可选字段默认值 ──

    private static final int DEFAULT_TASK_TYPE = 0;
    private static final int DEFAULT_RTH_MODE = 0;
    private static final int DEFAULT_REPEAT_TYPE = 0;
    private static final int DEFAULT_WAYLINE_PRECISION_TYPE = 0;
    private static final int DEFAULT_RTH_ALTITUDE = 100;
    private static final double DEFAULT_WAYLINE_DURATION = 0.0;
    private static final int DEFAULT_CREATE_TASK_DEVICE_TYPE = 0;
    private static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";

    /** 失控动作：仅允许 RETURN_HOME 或 CONTINUE，不允许 HOVER（试飞安全策略，非 SDK 限制） */
    private static final int MIN_VALID_OOC_ACTION = OutOfControlAction.RETURN_HOME.getValue();
    private static final int MAX_VALID_OOC_ACTION = OutOfControlAction.CONTINUE.getValue();

    /** Mock 试飞航线 UUID 前缀 */
    private static final String MOCK_WAYLINE_UUID_PREFIX = "mock-wayline-";

    /** Mock 试飞任务 UUID 前缀 */
    private static final String MOCK_TASK_UUID_PREFIX = "mock-task-";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhTestflightTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 本工具整体要求人工确认（两个 action 均需要）。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    /**
     * 工具级安全等级取两个 action 中的较高值（L2），具体 action 在 execute 内再做细粒度拦截。
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.FLIGHT;
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
                "Test-flight wayline generation and task creation (per-drone TestFlightModule). "
                + "Required call sequence: first call action=generate_wayline to generate a test-flight "
                + "wayline from a target point and an observer point; take the 'uuid' from its response "
                + "and pass it as wayline_uuid into action=create_task. "
                + "WARNING: create_task TRIGGERS AN ACTUAL FLIGHT on the physical drone. "
                + "Both actions require confirm=true.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "generate_wayline: generate a test-flight wayline (no flight triggered) | "
                        + "create_task: create a test-flight task from a generated wayline_uuid "
                        + "(TRIGGERS ACTUAL FLIGHT)"
        ));
        props.put(PARAM_PROJECT_ID, Map.of(
                "type", "string",
                "description", "Target project ID"
        ));
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Name of the wayline (generate_wayline) or task (create_task)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm this operation"
        ));
        putGenerateWaylineSchema(props);
        putCreateTaskSchema(props);

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_PROJECT_ID, PARAM_NAME, PARAM_CONFIRM));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 填充 generate_wayline 相关的 schema 字段。
     *
     * @param props schema properties 容器
     */
    private void putGenerateWaylineSchema(Map<String, Object> props) {
        props.put(PARAM_DEVICE_TYPE, Map.of(
                "type", "string",
                "description", "Drone device type model code. Required for generate_wayline."
        ));
        props.put(PARAM_TARGET_POINT, pointSchema(
                "Target point for the test-flight wayline. Required for generate_wayline."));
        props.put(PARAM_OBSERVER_POINT, pointSchema(
                "Observer point for the test-flight wayline. Required for generate_wayline."));
        props.put(PARAM_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number. Optional for generate_wayline in mock mode, "
                        + "but required against the real SDK since TestFlightModule is per-drone. "
                        + "Always required for create_task."
        ));
    }

    /**
     * 填充 create_task 相关的 schema 字段。
     *
     * @param props schema properties 容器
     */
    private void putCreateTaskSchema(Map<String, Object> props) {
        props.put(PARAM_WAYLINE_UUID, Map.of(
                "type", "string",
                "description", "uuid returned by generate_wayline. Required for create_task."
        ));
        props.put(PARAM_TASK_TYPE, Map.of(
                "type", "integer",
                "description", "Task type, raw passthrough int (default " + DEFAULT_TASK_TYPE
                        + "). Required for create_task."
        ));
        props.put(PARAM_OUT_OF_CONTROL_ACTION_IN_FLIGHT, Map.of(
                "type", "integer",
                "description", "Action when the drone loses signal in flight. Only 1 (RETURN_HOME) or 2 "
                        + "(CONTINUE) are allowed for test flights; 0 (HOVER) is rejected by test-flight "
                        + "safety policy. Required for create_task."
        ));
        props.put(PARAM_LANDING_DOCK_SN, Map.of(
                "type", "string", "description", "Landing dock serial number (optional)"));
        props.put(PARAM_TIME_ZONE, Map.of(
                "type", "string",
                "description", "IANA time zone (optional, default '" + DEFAULT_TIME_ZONE + "')"));
        props.put(PARAM_DEFAULT_NAME, Map.of(
                "type", "string", "description", "Default task name (optional)"));
        props.put(PARAM_RESUMABLE_STATUS, Map.of(
                "type", "string", "description", "Resumable status (optional)"));
        props.put(PARAM_MEDIA_FOLDER_ID_PATHS, Map.of(
                "type", "string", "description", "Media folder id paths (optional)"));
        props.put(PARAM_RTH_MODE, Map.of(
                "type", "integer",
                "description", "RTH mode, raw passthrough int (optional, default " + DEFAULT_RTH_MODE + ")"));
        props.put(PARAM_REPEAT_TYPE, Map.of(
                "type", "integer",
                "description", "Repeat type, raw passthrough int; semantics not confirmed in SDK docs "
                        + "(optional, default " + DEFAULT_REPEAT_TYPE + ")"));
        props.put(PARAM_WAYLINE_PRECISION_TYPE, Map.of(
                "type", "integer",
                "description", "Wayline precision type, raw passthrough int (optional, default "
                        + DEFAULT_WAYLINE_PRECISION_TYPE + ")"));
        props.put(PARAM_RTH_ALTITUDE, Map.of(
                "type", "integer",
                "description", "RTH altitude in meters (optional, default " + DEFAULT_RTH_ALTITUDE + ")"));
        props.put(PARAM_WAYLINE_DURATION, Map.of(
                "type", "number",
                "description", "Wayline duration in seconds (optional, default "
                        + DEFAULT_WAYLINE_DURATION + ")"));
        props.put(PARAM_CREATE_TASK_DEVICE_TYPE, Map.of(
                "type", "integer",
                "description", "Device type, raw passthrough int (optional, default "
                        + DEFAULT_CREATE_TASK_DEVICE_TYPE + ")"));
    }

    /**
     * 构建坐标点（lng/lat/alt）的 schema 定义。
     *
     * @param description 字段描述
     * @return 坐标点 schema
     */
    private Map<String, Object> pointSchema(String description) {
        Map<String, Object> pointProps = new LinkedHashMap<>();
        pointProps.put(PARAM_LNG, Map.of("type", "number", "description", "Longitude in degrees"));
        pointProps.put(PARAM_LAT, Map.of("type", "number", "description", "Latitude in degrees"));
        pointProps.put(PARAM_ALT, Map.of("type", "number", "description", "Altitude in meters"));

        Map<String, Object> pointSchema = new LinkedHashMap<>();
        pointSchema.put("type", "object");
        pointSchema.put("description", description);
        pointSchema.put("properties", pointProps);
        pointSchema.put("required", List.of(PARAM_LNG, PARAM_LAT, PARAM_ALT));
        return pointSchema;
    }

    /**
     * 执行试飞相关操作。
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

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        String projectId = getStringArg(args, PARAM_PROJECT_ID, null);
        if (projectId == null) {
            return errorResult("Missing required parameter: project_id", "Provide project_id.");
        }

        String name = getStringArg(args, PARAM_NAME, null);
        if (name == null) {
            return errorResult("Missing required parameter: name", "Provide name.");
        }

        return ACTION_GENERATE_WAYLINE.equals(action)
                ? executeGenerateWayline(args, projectId, name)
                : executeCreateTask(args, projectId, name);
    }

    // ==================== generate_wayline ====================

    /**
     * 处理 generate_wayline action：校验参数、人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args      输入参数
     * @param projectId 项目 ID
     * @param name      航线名称
     * @return 执行结果
     */
    private Map<String, Object> executeGenerateWayline(Map<String, Object> args, String projectId, String name) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        String deviceType = getStringArg(args, PARAM_DEVICE_TYPE, null);
        if (deviceType == null) {
            return errorResult("Missing required parameter: device_type", "Provide device_type.");
        }

        double[] targetPoint = resolvePoint(args, PARAM_TARGET_POINT);
        if (targetPoint == null) {
            return errorResult("Missing or invalid required parameter: target_point",
                    "Provide target_point with lng/lat/alt (all numbers).");
        }

        double[] observerPoint = resolvePoint(args, PARAM_OBSERVER_POINT);
        if (observerPoint == null) {
            return errorResult("Missing or invalid required parameter: observer_point",
                    "Provide observer_point with lng/lat/alt (all numbers).");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_GENERATE_WAYLINE, projectId,
                "Generate test-flight wayline '" + name + "' for project " + projectId);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] generate_wayline: projectId={}, name={}, deviceType={}",
                TOOL_NAME, projectId, name, deviceType);

        return sdkService.isMockMode()
                ? generateWaylineInMockMode(projectId, name)
                : generateWaylineWithRealSdk(args, projectId, name, deviceType, targetPoint, observerPoint);
    }

    /**
     * 从参数 Map 中解析坐标点（lng/lat/alt）。
     *
     * @param args 输入参数
     * @param key  坐标点参数名（target_point / observer_point）
     * @return {lng, lat, alt} 三元数组；缺失或类型不对返回 null
     */
    @SuppressWarnings("unchecked")
    private double[] resolvePoint(Map<String, Object> args, String key) {
        Object raw = args != null ? args.get(key) : null;
        if (!(raw instanceof Map)) {
            return null;
        }
        Map<String, Object> point = (Map<String, Object>) raw;
        double lng = getDoubleArg(point, PARAM_LNG, Double.NaN);
        double lat = getDoubleArg(point, PARAM_LAT, Double.NaN);
        double alt = getDoubleArg(point, PARAM_ALT, Double.NaN);
        if (Double.isNaN(lng) || Double.isNaN(lat) || Double.isNaN(alt)) {
            return null;
        }
        return new double[]{lng, lat, alt};
    }

    /**
     * Mock 模式下生成试飞航线。
     *
     * @param projectId 项目 ID
     * @param name      航线名称
     * @return 成功响应
     */
    private Map<String, Object> generateWaylineInMockMode(String projectId, String name) {
        String uuid = MOCK_WAYLINE_UUID_PREFIX + System.currentTimeMillis();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project_id", projectId);
        data.put("key", "mock-key-" + uuid);
        data.put("name", name);
        data.put("uuid", uuid);
        data.put("request_id", "mock-req-" + uuid);
        data.put("_mock", true);
        return successResult(data,
                "Test-flight wayline generated. Use uuid='" + uuid + "' as wayline_uuid for create_task.");
    }

    /**
     * 通过真实 SDK 生成试飞航线。
     *
     * <p>{@code TestFlightModule} 挂在 {@code core.Drone} 上（按无人机维度获取），
     * 真实 SDK 路径需要定位到具体无人机实例，因此复用 create_task 的 {@code sn} 参数
     * （非 action 必填项，仅真实模式下用于解析 Drone；Mock 模式不依赖它）。
     *
     * @param args          输入参数（用于取 sn 解析 Drone 实例）
     * @param projectId     项目 ID
     * @param name          航线名称
     * @param deviceType    设备类型
     * @param targetPoint   目标点 {lng, lat, alt}
     * @param observerPoint 观察点 {lng, lat, alt}
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> generateWaylineWithRealSdk(
            Map<String, Object> args, String projectId, String name, String deviceType,
            double[] targetPoint, double[] observerPoint) {
        try {
            String sn = getStringArg(args, PARAM_SN, null);
            if (sn == null) {
                return errorResult("Missing sn for real SDK call",
                        "TestFlightModule is per-drone; provide sn to resolve the target drone instance.");
            }
            TestFlightModule module = resolveTestFlightModule(sn);
            if (module == null) {
                return errorResult("TestFlightModule not available",
                        "Drone may be offline. Use fh_device_list to check, or call fh_connect first.");
            }

            GenerateTryFlightWaylineParams params = buildGenerateWaylineParams(
                    name, deviceType, targetPoint, observerPoint);

            CompletableFuture<GenerateTryFlightWaylineResponse> future = new CompletableFuture<>();
            module.generateTryFlightWayline(projectId, params,
                    (FlightHubCallback.GenerateTryFlightWaylineCallback) (response, error) -> {
                        if (error == null || error.isSuccess()) {
                            future.complete(response);
                        } else {
                            future.completeExceptionally(
                                    new SdkCallbackBridge.SdkException(error.getServerCode(),
                                            "generateTryFlightWayline failed: " + formatSdkError(error)));
                        }
                    });

            GenerateTryFlightWaylineResponse response = SdkCallbackBridge.await(future);
            Map<String, Object> data = toMap(projectId, response);
            return successResult(data, "Test-flight wayline generated. Use the returned uuid as "
                    + "wayline_uuid for create_task.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] generate_wayline timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] generate_wayline SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] generate_wayline error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to generate test-flight wayline: " + e.getMessage(),
                    "Check logs for details.");
        }
    }

    /**
     * 构建 generate_wayline 的 SWIG 参数对象。
     *
     * @param name          航线名称
     * @param deviceType    设备类型
     * @param targetPoint   目标点 {lng, lat, alt}
     * @param observerPoint 观察点 {lng, lat, alt}
     * @return SWIG 参数对象
     */
    private GenerateTryFlightWaylineParams buildGenerateWaylineParams(
            String name, String deviceType, double[] targetPoint, double[] observerPoint) {
        GenerateTryFlightWaylineParams params = new GenerateTryFlightWaylineParams();
        params.setName(name);
        params.setDevice_type(deviceType);
        params.setTarget_point(buildPoint(targetPoint));
        params.setObserver_point(buildPoint(observerPoint));
        return params;
    }

    /**
     * 构建 SWIG 坐标点对象。
     *
     * @param point {lng, lat, alt} 三元数组
     * @return SWIG 坐标点对象
     */
    private TryFlightWaylinePoint buildPoint(double[] point) {
        TryFlightWaylinePoint swigPoint = new TryFlightWaylinePoint();
        swigPoint.setLng(point[0]);
        swigPoint.setLat(point[1]);
        swigPoint.setAlt(point[2]);
        return swigPoint;
    }

    /**
     * 将 GenerateTryFlightWaylineResponse（SWIG 原生对象）转换为纯 Map。
     *
     * @param projectId 项目 ID（回显）
     * @param response  SDK 返回的 SWIG 响应对象
     * @return 纯 Map 结果
     */
    private Map<String, Object> toMap(String projectId, GenerateTryFlightWaylineResponse response) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project_id", projectId);
        if (response != null) {
            data.put("key", response.getKey());
            data.put("name", response.getName());
            data.put("uuid", response.getUuid());
            data.put("request_id", response.getRequest_id());
        }
        return data;
    }

    // ==================== create_task ====================

    /**
     * 处理 create_task action：校验参数、人工确认后分发到 mock/真实 SDK 路径。
     *
     * <p>**此操作会触发无人机真实起飞执行试飞航线。**
     *
     * @param args      输入参数
     * @param projectId 项目 ID
     * @param name      任务名称
     * @return 执行结果
     */
    private Map<String, Object> executeCreateTask(Map<String, Object> args, String projectId, String name) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.FLIGHT);
        if (denied != null) {
            return denied;
        }

        String waylineUuid = getStringArg(args, PARAM_WAYLINE_UUID, null);
        if (waylineUuid == null) {
            return errorResult("Missing required parameter: wayline_uuid",
                    "Call generate_wayline first and pass its uuid as wayline_uuid.");
        }

        String sn = getStringArg(args, PARAM_SN, null);
        if (sn == null) {
            return errorResult("Missing required parameter: sn", "Provide sn (target drone serial number).");
        }

        if (!args.containsKey(PARAM_TASK_TYPE)) {
            return errorResult("Missing required parameter: task_type", "Provide task_type.");
        }
        int taskType = getIntArg(args, PARAM_TASK_TYPE, DEFAULT_TASK_TYPE);

        if (!args.containsKey(PARAM_OUT_OF_CONTROL_ACTION_IN_FLIGHT)) {
            return errorResult("Missing required parameter: out_of_control_action_in_flight",
                    "Provide out_of_control_action_in_flight (1=RETURN_HOME or 2=CONTINUE).");
        }
        int oocAction = getIntArg(args, PARAM_OUT_OF_CONTROL_ACTION_IN_FLIGHT, -1);
        if (oocAction < MIN_VALID_OOC_ACTION || oocAction > MAX_VALID_OOC_ACTION) {
            return errorResult(
                    "Invalid out_of_control_action_in_flight: " + oocAction,
                    "For test flights only 1 (RETURN_HOME) or 2 (CONTINUE) are allowed; "
                            + "0 (HOVER) is rejected by test-flight safety policy.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_CREATE_TASK, sn,
                "⚠️ Create test-flight task '" + name + "' on drone " + sn
                        + ". THIS WILL TRIGGER AN ACTUAL FLIGHT. Confirm?");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] create_task: projectId={}, name={}, sn={}, waylineUuid={}",
                TOOL_NAME, projectId, name, sn, waylineUuid);

        return sdkService.isMockMode()
                ? createTaskInMockMode(projectId, name, sn, waylineUuid)
                : createTaskWithRealSdk(args, projectId, name, sn, waylineUuid, taskType, oocAction);
    }

    /**
     * Mock 模式下创建试飞任务。
     *
     * @param projectId   项目 ID
     * @param name        任务名称
     * @param sn          目标无人机 SN
     * @param waylineUuid 试飞航线 UUID
     * @return 成功响应
     */
    private Map<String, Object> createTaskInMockMode(String projectId, String name, String sn, String waylineUuid) {
        String uuid = MOCK_TASK_UUID_PREFIX + System.currentTimeMillis();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project_id", projectId);
        data.put("name", name);
        data.put("sn", sn);
        data.put("wayline_uuid", waylineUuid);
        data.put("uuid", uuid);
        data.put("begin_at", (int) (System.currentTimeMillis() / 1000));
        data.put("end_at", (int) (System.currentTimeMillis() / 1000) + 600);
        data.put("_mock", true);
        return successResult(data, "[MOCK] Test-flight task created on drone " + sn + ".");
    }

    /**
     * 通过真实 SDK 创建试飞任务。
     *
     * @param args        输入参数（读取可选字段）
     * @param projectId   项目 ID
     * @param name        任务名称
     * @param sn          目标无人机 SN
     * @param waylineUuid 试飞航线 UUID
     * @param taskType    任务类型（raw passthrough）
     * @param oocAction   失控动作（1 或 2）
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> createTaskWithRealSdk(
            Map<String, Object> args, String projectId, String name, String sn,
            String waylineUuid, int taskType, int oocAction) {
        try {
            TestFlightModule module = resolveTestFlightModule(sn);
            if (module == null) {
                return errorResult("TestFlightModule not available",
                        "Drone may be offline. Use fh_device_list to check, or call fh_connect first.");
            }

            CreateTestFlightTaskParams params = buildCreateTaskParams(
                    args, name, sn, waylineUuid, taskType, oocAction);

            CompletableFuture<CreateTestFlightTaskResponse> future = new CompletableFuture<>();
            module.createTestFlightTask(projectId, params,
                    (FlightHubCallback.CreateTestFlightTaskCallback) (response, error) -> {
                        if (error == null || error.isSuccess()) {
                            future.complete(response);
                        } else {
                            future.completeExceptionally(
                                    new SdkCallbackBridge.SdkException(error.getServerCode(),
                                            "createTestFlightTask failed: " + formatSdkError(error)));
                        }
                    });

            CreateTestFlightTaskResponse response = SdkCallbackBridge.await(future);
            Map<String, Object> data = toMap(projectId, name, sn, waylineUuid, response);
            return successResult(data, "Test-flight task created on drone " + sn + ".");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] create_task timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] create_task SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] create_task error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to create test-flight task: " + e.getMessage(), "Check logs for details.");
        }
    }

    /**
     * 构建 create_task 的 SWIG 参数对象，必填字段直接赋值，可选字段取参数值或默认值。
     *
     * @param args        输入参数
     * @param name        任务名称
     * @param sn          目标无人机 SN
     * @param waylineUuid 试飞航线 UUID
     * @param taskType    任务类型
     * @param oocAction   失控动作
     * @return SWIG 参数对象
     */
    private CreateTestFlightTaskParams buildCreateTaskParams(
            Map<String, Object> args, String name, String sn, String waylineUuid, int taskType, int oocAction) {
        CreateTestFlightTaskParams params = new CreateTestFlightTaskParams();
        params.setName(name);
        params.setWayline_uuid(waylineUuid);
        params.setSn(sn);
        params.setTask_type(taskType);
        params.setOut_of_control_action_in_flight(oocAction);

        params.setLanding_dock_sn(getStringArg(args, PARAM_LANDING_DOCK_SN, ""));
        params.setTime_zone(getStringArg(args, PARAM_TIME_ZONE, DEFAULT_TIME_ZONE));
        params.setDefault_name(getStringArg(args, PARAM_DEFAULT_NAME, name));
        params.setResumable_status(getStringArg(args, PARAM_RESUMABLE_STATUS, ""));
        params.setMedia_folder_id_paths(getStringArg(args, PARAM_MEDIA_FOLDER_ID_PATHS, ""));
        params.setRth_mode(getIntArg(args, PARAM_RTH_MODE, DEFAULT_RTH_MODE));
        // repeat_type 语义未在 SDK 文档确认，照抄用户传入值
        params.setRepeat_type(getIntArg(args, PARAM_REPEAT_TYPE, DEFAULT_REPEAT_TYPE));
        params.setWayline_precision_type(getIntArg(args, PARAM_WAYLINE_PRECISION_TYPE,
                DEFAULT_WAYLINE_PRECISION_TYPE));
        params.setRth_altitude(getIntArg(args, PARAM_RTH_ALTITUDE, DEFAULT_RTH_ALTITUDE));
        params.setWayline_duration(getDoubleArg(args, PARAM_WAYLINE_DURATION, DEFAULT_WAYLINE_DURATION));
        params.setDevice_type(getIntArg(args, PARAM_CREATE_TASK_DEVICE_TYPE, DEFAULT_CREATE_TASK_DEVICE_TYPE));
        return params;
    }

    /**
     * 将 CreateTestFlightTaskResponse（SWIG 原生对象）转换为纯 Map。
     *
     * @param projectId   项目 ID（回显）
     * @param name        任务名称（回显）
     * @param sn          目标无人机 SN（回显）
     * @param waylineUuid 试飞航线 UUID（回显）
     * @param response    SDK 返回的 SWIG 响应对象
     * @return 纯 Map 结果
     */
    private Map<String, Object> toMap(
            String projectId, String name, String sn, String waylineUuid, CreateTestFlightTaskResponse response) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project_id", projectId);
        data.put("name", name);
        data.put("sn", sn);
        data.put("wayline_uuid", waylineUuid);
        if (response != null) {
            data.put("uuid", response.getUuid());
            data.put("begin_at", response.getBegin_at());
            data.put("end_at", response.getEnd_at());
        }
        return data;
    }

    // ==================== 工具方法 ====================

    /**
     * 按无人机 SN 获取对应的 TestFlightModule 实例。
     *
     * @param sn 目标无人机 SN
     * @return TestFlightModule，无人机不可用时返回 null
     */
    private TestFlightModule resolveTestFlightModule(String sn) {
        Drone drone = sdkService.getDrone(sn);
        return drone != null ? drone.getTestFlightModule() : null;
    }
}
