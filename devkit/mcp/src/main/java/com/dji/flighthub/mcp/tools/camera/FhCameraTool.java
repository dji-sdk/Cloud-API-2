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

package com.dji.flighthub.mcp.tools.camera;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.CameraFocalLengthParams;
import com.dji.flighthubsdk.bean.CameraState;
import com.dji.flighthubsdk.bean.CameraZoomLensState;
import com.dji.flighthubsdk.bean.GimbalState;
import com.dji.flighthubsdk.bean.LookAtTargetParams;
import com.dji.flighthubsdk.bean.PanoramaParam;
import com.dji.flighthubsdk.bean.SetNightSceneModeParams;
import com.dji.flighthubsdk.bean.SetPhotoStorageSettingsParams;
import com.dji.flighthubsdk.bean.SetVideoStorageSettingsParams;
import com.dji.flighthubsdk.bean.SwitchCameraModeParams;
import com.dji.flighthubsdk.bean.SwitchLiveLensParams;
import com.dji.flighthubsdk.bean.ThermalGainModeSetParams;
import com.dji.flighthubsdk.bean.ZoomAtTargetParams;
import com.dji.flighthubsdk.core.ControlAuthModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.value.CameraLensType;
import com.dji.flighthubsdk.value.CameraMode;
import com.dji.flighthubsdk.value.IrGainMode;
import com.dji.flighthubsdk.value.NightSceneMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * fh_camera — 相机和云台统一控制工具。
 * 覆盖拍照、录像、变焦、框选放大、模式切换、镜头切换、夜景模式、存储设置、
 * 红外增益、全景拍摄、云台旋转、云台指向、云台复位以及状态查询等所有相机/云台功能。
 *
 * <p>安全等级：L0（纯观测/相机类操作，无需人工确认）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhCameraTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhCameraTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_camera";

    // ==================== 参数常量 ====================
    private static final String PARAM_DRONE_SN = "drone_sn";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";
    private static final String PARAM_LENS_TYPE = "lens_type";
    private static final String PARAM_CAMERA_MODE = "camera_mode";
    private static final String PARAM_NIGHT_MODE = "night_mode";
    private static final String PARAM_GAIN_MODE = "gain_mode";
    private static final String PARAM_RESET_MODE = "reset_mode";
    private static final String PARAM_SETTINGS = "settings";
    private static final String PARAM_X = "x";
    private static final String PARAM_Y = "y";
    private static final String PARAM_WIDTH = "width";
    private static final String PARAM_HEIGHT = "height";
    private static final String PARAM_ZOOM_FACTOR = "zoom_factor";
    private static final String PARAM_PITCH = "pitch";
    private static final String PARAM_YAW = "yaw";
    private static final String PARAM_TARGET_LAT = "target_lat";
    private static final String PARAM_TARGET_LNG = "target_lng";
    private static final String PARAM_TARGET_HEIGHT = "target_height";

    // ==================== Action 常量 ====================
    private static final String ACTION_CAMERA_PHOTO = "camera_photo";
    private static final String ACTION_CAMERA_VIDEO_START = "camera_video_start";
    private static final String ACTION_CAMERA_VIDEO_STOP = "camera_video_stop";
    private static final String ACTION_CAMERA_ZOOM = "camera_zoom";
    private static final String ACTION_GIMBAL_ROTATE = "gimbal_rotate";
    private static final String ACTION_GIMBAL_LOOK_AT = "gimbal_look_at";
    private static final String ACTION_ZOOM_AT_TARGET = "zoom_at_target";
    private static final String ACTION_SWITCH_MODE = "switch_mode";
    private static final String ACTION_SWITCH_LENS = "switch_lens";
    private static final String ACTION_NIGHT_SCENE = "night_scene";
    private static final String ACTION_PHOTO_STORAGE = "photo_storage";
    private static final String ACTION_VIDEO_STORAGE = "video_storage";
    private static final String ACTION_THERMAL_GAIN = "thermal_gain";
    private static final String ACTION_PANORAMA_START = "panorama_start";
    private static final String ACTION_PANORAMA_STOP = "panorama_stop";
    private static final String ACTION_GIMBAL_RESET = "gimbal_reset";
    private static final String ACTION_GET_CAMERA_STATE = "get_camera_state";
    private static final String ACTION_GET_GIMBAL_STATE = "get_gimbal_state";
    private static final String ACTION_GET_LENS_STATE = "get_lens_state";

    /** 有效 action 集合 */
    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_CAMERA_PHOTO, ACTION_CAMERA_VIDEO_START, ACTION_CAMERA_VIDEO_STOP,
            ACTION_CAMERA_ZOOM, ACTION_GIMBAL_ROTATE, ACTION_GIMBAL_LOOK_AT,
            ACTION_ZOOM_AT_TARGET, ACTION_SWITCH_MODE, ACTION_SWITCH_LENS,
            ACTION_NIGHT_SCENE, ACTION_PHOTO_STORAGE, ACTION_VIDEO_STORAGE,
            ACTION_THERMAL_GAIN, ACTION_PANORAMA_START, ACTION_PANORAMA_STOP,
            ACTION_GIMBAL_RESET, ACTION_GET_CAMERA_STATE, ACTION_GET_GIMBAL_STATE,
            ACTION_GET_LENS_STATE
    );

    /** 有效 action 排序列表 */
    private static final List<String> VALID_ACTIONS_SORTED = VALID_ACTIONS.stream()
            .sorted().toList();

    // ==================== 枚举常量 ====================
    /** 有效 camera_mode 枚举 */
    private static final Set<String> VALID_CAMERA_MODES = Set.of(
            "PHOTO", "VIDEO", "SMART_PHOTO", "PANO_PHOTO", "INTERVAL_PHOTO"
    );

    /** 有效 lens_type 枚举 */
    private static final Set<String> VALID_LENS_TYPES = Set.of(
            "NORMAL", "WIDE", "ZOOM", "IR", "THERMAL"
    );

    /** 有效 night_mode 枚举 */
    private static final Set<String> VALID_NIGHT_MODES = Set.of("off", "on", "auto");

    /** 有效 gain_mode 枚举 */
    private static final Set<String> VALID_GAIN_MODES = Set.of("auto", "low", "high");

    /** 云台复位模式最大值：0=归中 1=朝下 2=偏航归中 3=俯仰朝下 */
    private static final int RESET_MODE_MAX = 3;

    /** zoom_factor 最小值 */
    private static final int ZOOM_FACTOR_MIN = 2;

    /** zoom_factor 最大值 */
    private static final int ZOOM_FACTOR_MAX = 200;

    /** 云台俯仰角范围 */
    private static final double PITCH_MIN = -120.0;
    private static final double PITCH_MAX = 30.0;

    /** 云台偏航角范围 */
    private static final double YAW_MIN = -180.0;
    private static final double YAW_MAX = 180.0;

    /** 等待拍照可用状态的超时时间（秒） */
    private static final int TAKE_PHOTO_AVAILABLE_TIMEOUT_SECONDS = 8;

    /** 归一化坐标范围下界 */
    private static final double NORMALIZED_MIN = 0.0;

    /** 归一化坐标范围上界 */
    private static final double NORMALIZED_MAX = 1.0;

    /** 默认负载索引（相机类型） */
    private static final String DEFAULT_PAYLOAD_INDEX = "81-0-0";

    /** 状态查询超时时间（秒） */
    private static final int STATE_QUERY_TIMEOUT_SECONDS = 5;

    /** 拍照前模式检测超时时间（秒），比状态查询短 */
    private static final int MODE_CHECK_TIMEOUT_SECONDS = 3;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhCameraTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Advanced camera control: box-select zoom, mode/lens switching, night scene, "
                        + "photo/video storage settings, thermal gain, panorama capture, "
                        + "gimbal reset, and camera/gimbal/lens state queries. "
                        + "PHOTO WORKFLOW: camera_photo automatically checks current camera mode "
                        + "and switches to PHOTO mode if needed — no manual switch_mode call required. "
                        + "If camera is busy (recording), stop recording first. "
                        + "VIDEO WORKFLOW: Before camera_video_start, you MUST call switch_mode with "
                        + "camera_mode=VIDEO first, otherwise INVALID_STATE error will occur. "
                        + "RETRY: camera_photo auto-retries once on SDK error "
                        + "(transient DRC link issues are common). "
                        + "IMPORTANT: Manual photo/video is BLOCKED while a wayline task is executing — "
                        + "firmware locks the camera. Check fh_wayline_status first if camera_photo fails.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Camera action to execute"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Payload index (e.g. '81-0-0'), default: primary camera"
        ));
        props.put(PARAM_LENS_TYPE, Map.of(
                "type", "string",
                "enum", List.of("NORMAL", "WIDE", "ZOOM", "IR", "THERMAL"),
                "description", "Lens type for switch_lens or zoom_at_target"
        ));
        props.put(PARAM_CAMERA_MODE, Map.of(
                "type", "string",
                "enum", List.of("PHOTO", "VIDEO", "SMART_PHOTO", "PANO_PHOTO", "INTERVAL_PHOTO"),
                "description", "Camera mode for switch_mode"
        ));
        props.put(PARAM_NIGHT_MODE, Map.of(
                "type", "string",
                "enum", List.of("off", "on", "auto"),
                "description", "Night scene mode for night_scene"
        ));
        props.put(PARAM_GAIN_MODE, Map.of(
                "type", "string",
                "enum", List.of("auto", "low", "high"),
                "description", "Thermal/IR gain mode for thermal_gain"
        ));
        props.put(PARAM_RESET_MODE, Map.of(
                "type", "integer",
                "description", "Gimbal reset mode: 0=center, 1=down, 2=yaw-center, 3=pitch-down"
        ));
        props.put(PARAM_SETTINGS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Storage setting items for photo_storage/video_storage"
        ));
        props.put(PARAM_X, Map.of(
                "type", "number",
                "description", "Normalized X coordinate (0~1) for zoom_at_target box"
        ));
        props.put(PARAM_Y, Map.of(
                "type", "number",
                "description", "Normalized Y coordinate (0~1) for zoom_at_target box"
        ));
        props.put(PARAM_WIDTH, Map.of(
                "type", "number",
                "description", "Normalized width (0~1) for zoom_at_target box"
        ));
        props.put(PARAM_HEIGHT, Map.of(
                "type", "number",
                "description", "Normalized height (0~1) for zoom_at_target box"
        ));
        props.put(PARAM_ZOOM_FACTOR, Map.of(
                "type", "integer",
                "description", "Zoom level (2~200, for camera_zoom)"
        ));
        props.put(PARAM_PITCH, Map.of(
                "type", "number",
                "description", "Gimbal pitch angle (-120~30, for gimbal_rotate)"
        ));
        props.put(PARAM_YAW, Map.of(
                "type", "number",
                "description", "Gimbal yaw angle (-180~180, for gimbal_rotate)"
        ));
        props.put(PARAM_TARGET_LAT, Map.of(
                "type", "number",
                "description", "Target latitude (for gimbal_look_at)"
        ));
        props.put(PARAM_TARGET_LNG, Map.of(
                "type", "number",
                "description", "Target longitude (for gimbal_look_at)"
        ));
        props.put(PARAM_TARGET_HEIGHT, Map.of(
                "type", "number",
                "description", "Target height (for gimbal_look_at)"
        ));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

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
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 3. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        // 4. 解析 payload_index（null 表示用户未传，真实 SDK 模式下自动探测）
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);

        // 5. Mock 模式（含 workspace 为 null 的降级场景）
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return dispatchMock(droneSn, action,
                    payloadIndex != null ? payloadIndex : DEFAULT_PAYLOAD_INDEX, args);
        }

        // 6. 真实 SDK 调用
        return executeWithRealSdk(droneSn, action, payloadIndex, args);
    }

    // ==================== Mock 模式分发 ====================

    /**
     * Mock 模式分发。
     */
    private Map<String, Object> dispatchMock(String sn, String action, String idx,
                                             Map<String, Object> args) {
        return switch (action) {
            case ACTION_CAMERA_PHOTO -> successResult(
                    Map.of("drone_sn", sn, "action", action, "payload_index", idx,
                            "auto_mode_check", true, "mode_switched", false),
                    "Photo taken (camera mode auto-verified as PHOTO)");
            case ACTION_CAMERA_VIDEO_START -> successResult(
                    Map.of("drone_sn", sn, "action", action, "recording", true),
                    "Recording started on " + sn);
            case ACTION_CAMERA_VIDEO_STOP -> successResult(
                    Map.of("drone_sn", sn, "action", action, "recording", false),
                    "Recording stopped on " + sn);
            case ACTION_CAMERA_ZOOM -> dispatchMockCameraZoom(sn, idx, args);
            case ACTION_GIMBAL_ROTATE -> dispatchMockGimbalRotate(sn, idx, args);
            case ACTION_GIMBAL_LOOK_AT -> dispatchMockGimbalLookAt(sn, idx, args);
            case ACTION_ZOOM_AT_TARGET -> dispatchMockZoomAtTarget(sn, idx, args);
            case ACTION_SWITCH_MODE -> dispatchMockSwitchMode(sn, idx, args);
            case ACTION_SWITCH_LENS -> dispatchMockSwitchLens(sn, idx, args);
            case ACTION_NIGHT_SCENE -> dispatchMockNightScene(sn, idx, args);
            case ACTION_PHOTO_STORAGE -> dispatchMockStorage(sn, ACTION_PHOTO_STORAGE, idx, args);
            case ACTION_VIDEO_STORAGE -> dispatchMockStorage(sn, ACTION_VIDEO_STORAGE, idx, args);
            case ACTION_THERMAL_GAIN -> dispatchMockThermalGain(sn, idx, args);
            case ACTION_PANORAMA_START -> successResult(
                    Map.of("drone_sn", sn, "action", action, "payload_index", idx, "status", "started"),
                    "Panoramic capture started");
            case ACTION_PANORAMA_STOP -> successResult(
                    Map.of("drone_sn", sn, "action", action, "payload_index", idx, "status", "stopped"),
                    "Panoramic capture stopped");
            case ACTION_GIMBAL_RESET -> dispatchMockGimbalReset(sn, idx, args);
            case ACTION_GET_CAMERA_STATE -> dispatchMockCameraState(sn, idx);
            case ACTION_GET_GIMBAL_STATE -> dispatchMockGimbalState(sn, idx);
            case ACTION_GET_LENS_STATE -> dispatchMockLensState(sn, idx);
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    /**
     * Mock: 框选放大。
     */
    private Map<String, Object> dispatchMockZoomAtTarget(String sn, String idx,
                                                         Map<String, Object> args) {
        String lensType = getStringArg(args, PARAM_LENS_TYPE, null);
        if (lensType == null || !VALID_LENS_TYPES.contains(lensType)) {
            return errorResult("Invalid or missing lens_type: " + lensType,
                    "Must be one of: " + VALID_LENS_TYPES);
        }
        double x = getDoubleArg(args, PARAM_X, -1.0);
        double y = getDoubleArg(args, PARAM_Y, -1.0);
        double w = getDoubleArg(args, PARAM_WIDTH, -1.0);
        double h = getDoubleArg(args, PARAM_HEIGHT, -1.0);
        Map<String, Object> boxError = validateNormalizedBox(x, y, w, h);
        if (boxError != null) {
            return boxError;
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_ZOOM_AT_TARGET, "payload_index", idx,
                        "lens_type", lensType, "x", x, "y", y, "width", w, "height", h),
                String.format("Box-select zoom applied: lens=%s, rect=(%.2f,%.2f,%.2f,%.2f)",
                        lensType, x, y, w, h));
    }

    /**
     * Mock: 相机模式切换。
     */
    private Map<String, Object> dispatchMockSwitchMode(String sn, String idx,
                                                       Map<String, Object> args) {
        String cameraMode = getStringArg(args, PARAM_CAMERA_MODE, null);
        if (cameraMode == null || !VALID_CAMERA_MODES.contains(cameraMode)) {
            return errorResult("Invalid or missing camera_mode: " + cameraMode,
                    "Must be one of: " + VALID_CAMERA_MODES);
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_SWITCH_MODE,
                        "payload_index", idx, "camera_mode", cameraMode),
                "Camera mode switched to " + cameraMode);
    }

    /**
     * Mock: 镜头切换。
     */
    private Map<String, Object> dispatchMockSwitchLens(String sn, String idx,
                                                       Map<String, Object> args) {
        String lensType = getStringArg(args, PARAM_LENS_TYPE, null);
        if (lensType == null || !VALID_LENS_TYPES.contains(lensType)) {
            return errorResult("Invalid or missing lens_type: " + lensType,
                    "Must be one of: " + VALID_LENS_TYPES);
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_SWITCH_LENS,
                        "payload_index", idx, "lens_type", lensType),
                "Live lens switched to " + lensType);
    }

    /**
     * Mock: 夜景模式。
     */
    private Map<String, Object> dispatchMockNightScene(String sn, String idx,
                                                       Map<String, Object> args) {
        String nightMode = getStringArg(args, PARAM_NIGHT_MODE, null);
        if (nightMode == null || !VALID_NIGHT_MODES.contains(nightMode)) {
            return errorResult("Invalid or missing night_mode: " + nightMode,
                    "Must be one of: " + VALID_NIGHT_MODES);
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_NIGHT_SCENE,
                        "payload_index", idx, "night_mode", nightMode),
                "Night scene mode set to " + nightMode);
    }

    /**
     * Mock: 存储设置（拍照/录像通用）。
     */
    private Map<String, Object> dispatchMockStorage(String sn, String action, String idx,
                                                    Map<String, Object> args) {
        List<String> settings = getStringListArg(args, PARAM_SETTINGS);
        if (settings == null || settings.isEmpty()) {
            return errorResult("Missing or empty settings parameter",
                    "Provide settings as a non-empty string array.");
        }
        return successResult(
                Map.of("drone_sn", sn, "action", action, "payload_index", idx,
                        "settings", settings),
                action.replace("_", " ") + " settings applied: " + settings);
    }

    /**
     * Mock: 红外增益模式。
     */
    private Map<String, Object> dispatchMockThermalGain(String sn, String idx,
                                                        Map<String, Object> args) {
        String gainMode = getStringArg(args, PARAM_GAIN_MODE, null);
        if (gainMode == null || !VALID_GAIN_MODES.contains(gainMode)) {
            return errorResult("Invalid or missing gain_mode: " + gainMode,
                    "Must be one of: " + VALID_GAIN_MODES);
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_THERMAL_GAIN,
                        "payload_index", idx, "gain_mode", gainMode),
                "Thermal gain mode set to " + gainMode);
    }

    /**
     * Mock: 云台复位。
     */
    private Map<String, Object> dispatchMockGimbalReset(String sn, String idx,
                                                        Map<String, Object> args) {
        int resetMode = getIntArg(args, PARAM_RESET_MODE, -1);
        if (resetMode < 0 || resetMode > RESET_MODE_MAX) {
            return errorResult("Invalid or missing reset_mode: " + resetMode,
                    "Must be 0~3 (0=center, 1=down, 2=yaw-center, 3=pitch-down).");
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_GIMBAL_RESET,
                        "payload_index", idx, "reset_mode", resetMode),
                "Gimbal reset: " + describeResetMode(resetMode));
    }

    /**
     * Mock: 相机状态查询。
     */
    private Map<String, Object> dispatchMockCameraState(String sn, String idx) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("camera_mode", "PHOTO");
        state.put("is_recording", false);
        state.put("is_shooting", false);
        state.put("remain_record_duration", 3600);
        state.put("remain_photo_num", 9999);
        state.put("record_time", 0);
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_GET_CAMERA_STATE,
                        "payload_index", idx, "camera_state", state),
                "Camera state retrieved");
    }

    /**
     * Mock: 云台状态查询。
     */
    private Map<String, Object> dispatchMockGimbalState(String sn, String idx) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("pitch", -30.5);
        state.put("roll", 0.0);
        state.put("yaw", 45.2);
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_GET_GIMBAL_STATE,
                        "payload_index", idx, "gimbal_state", state),
                "Gimbal state retrieved");
    }

    /**
     * Mock: 变焦镜头状态查询。
     */
    private Map<String, Object> dispatchMockLensState(String sn, String idx) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("zoom_factor", 5.0);
        state.put("zoom_focus_mode", 0);
        state.put("zoom_focus_state", 0);
        state.put("zoom_exposure_mode", 1);
        state.put("zoom_iso", 100);
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_GET_LENS_STATE,
                        "payload_index", idx, "lens_state", state),
                "Zoom lens state retrieved");
    }

    /**
     * Mock: 变焦。
     */
    private Map<String, Object> dispatchMockCameraZoom(String sn, String idx,
                                                       Map<String, Object> args) {
        int zoom = getIntArg(args, PARAM_ZOOM_FACTOR, 2);
        if (zoom < ZOOM_FACTOR_MIN || zoom > ZOOM_FACTOR_MAX) {
            return errorResult("Invalid zoom_factor: " + zoom,
                    "Must be between " + ZOOM_FACTOR_MIN + " and " + ZOOM_FACTOR_MAX + ".");
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_CAMERA_ZOOM,
                        "payload_index", idx, "zoom_factor", zoom),
                "Zoom set to " + zoom + "x");
    }

    /**
     * Mock: 云台旋转。
     */
    private Map<String, Object> dispatchMockGimbalRotate(String sn, String idx,
                                                         Map<String, Object> args) {
        double pitch = getDoubleArg(args, PARAM_PITCH, 0.0);
        double yaw = getDoubleArg(args, PARAM_YAW, 0.0);
        if (pitch < PITCH_MIN || pitch > PITCH_MAX) {
            return errorResult("Invalid pitch: " + pitch,
                    "Must be between " + PITCH_MIN + " and " + PITCH_MAX + ".");
        }
        if (yaw < YAW_MIN || yaw > YAW_MAX) {
            return errorResult("Invalid yaw: " + yaw,
                    "Must be between " + YAW_MIN + " and " + YAW_MAX + ".");
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_GIMBAL_ROTATE,
                        "payload_index", idx, "pitch", pitch, "yaw", yaw),
                String.format("Gimbal rotated to pitch=%.1f, yaw=%.1f", pitch, yaw));
    }

    /**
     * Mock: 云台指向。
     */
    private Map<String, Object> dispatchMockGimbalLookAt(String sn, String idx,
                                                         Map<String, Object> args) {
        double lat = getDoubleArg(args, PARAM_TARGET_LAT, Double.NaN);
        double lng = getDoubleArg(args, PARAM_TARGET_LNG, Double.NaN);
        double h = getDoubleArg(args, PARAM_TARGET_HEIGHT, Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lng) || Double.isNaN(h)) {
            return errorResult("gimbal_look_at requires target_lat, target_lng, target_height",
                    "Provide all three target coordinates.");
        }
        return successResult(
                Map.of("drone_sn", sn, "action", ACTION_GIMBAL_LOOK_AT,
                        "payload_index", idx, "target_lat", lat, "target_lng", lng, "target_height", h),
                "Gimbal pointing at target");
    }

    // ==================== 真实 SDK 调用 ====================

    /**
     * 真实 SDK 调用分发。
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(String droneSn, String action,
                                                   String payloadIndex, Map<String, Object> args) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            // 用户未指定 payload_index 时，自动探测相机负载
            String resolvedIndex = payloadIndex;
            if (resolvedIndex == null) {
                resolvedIndex = sdkService.resolvePayloadIndex(drone);
                if (resolvedIndex == null) {
                    return errorResult(
                            "Cannot determine camera payload_index for drone " + droneSn,
                            "Auto-detect failed. Pass payload_index explicitly (e.g. '53-0-0' for M3D, "
                            + "'42-0-0' for H20). Use fh_device_list to check device model.");
                }
                log.info("fh_camera auto-detected payload_index={} for drone {}", resolvedIndex, droneSn);
            }

            // 查询类 action 走独立分发
            if (isQueryAction(action)) {
                return executeSdkQuery(drone, droneSn, action, resolvedIndex);
            }
            return executeSdkCommand(drone, droneSn, action, resolvedIndex, args);

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("Camera timeout: drone={}, action={}: {}", droneSn, action, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("Camera SDK error: drone={}, action={}: [{}] {}",
                    droneSn, action, e.getErrorCode(), e.getMessage());
            if (isListenerRegistrationFailure(e)) {
                return buildListenerRegistrationError(droneSn, e.getErrorCode());
            }
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone connectivity and retry.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Camera interrupted: drone={}, action={}", droneSn, action);
            return errorResult("Operation interrupted", "Retry the operation.");
        } catch (Exception e) {
            log.error("Camera error: drone={}, action={}: {}", droneSn, action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(), "Check drone connectivity.");
        }
    }

    /**
     * 判断 SDK 异常是否为监听器注册失败。
     * 监听器注册失败时 listenId < 0，错误信息包含 "Failed to register"。
     *
     * @param e SDK 异常
     * @return true 表示是监听器注册失败
     */
    private static boolean isListenerRegistrationFailure(SdkCallbackBridge.SdkException e) {
        return e.getMessage() != null && e.getMessage().contains("Failed to register");
    }

    /**
     * 构建监听器注册失败的结构化错误响应，包含两类可能原因和可操作的排查步骤。
     *
     * @param droneSn   目标无人机 SN（用于生成 fh_drone_snapshot 提示）
     * @param errorCode SDK 返回的错误码（通常为 -1）
     * @return 结构化错误响应（含 hint 和 next_steps）
     */
    /* package-private for testing */
    static Map<String, Object> buildListenerRegistrationError(String droneSn, int errorCode) {
        List<Map<String, Object>> nextSteps = List.of(
                Map.of("step", "1",
                        "check", "drone_power",
                        "hint", "Confirm drone is powered on and camera module is active"),
                Map.of("step", "2",
                        "check", "payload_index",
                        "hint", "Verify payload_index for your model: M4/M30/M3 → '53-0-0', H20/H20T → '42-0-0', default '81-0-0'"),
                Map.of("step", "3",
                        "tool", "fh_drone_snapshot",
                        "hint", "Run fh_drone_snapshot(drone_sn='" + droneSn + "') to check connectivity")
        );
        return errorResultWithHint(
                "Camera listener registration failed (error_code=" + errorCode + ")",
                "Drone may be powered off, camera inactive, or payload_index does not match this drone model.",
                "Two possible causes: (1) drone not powered on / camera inactive; (2) payload_index mismatch.",
                nextSteps
        );
    }

    /**
     * 真实 SDK 命令类调用。
     */
    private Map<String, Object> executeSdkCommand(Drone drone, String droneSn, String action,
                                                  String payloadIndex,
                                                  Map<String, Object> args) throws Exception {
        // 相机控制操作需要先获取 Payload 控制权，否则 SDK 返回 ERROR_INVALID_STATE
        ControlAuthModule authModule = drone.getControlAuthModule();
        SdkCallbackBridge.awaitVoid(cb -> authModule.acquirePayloadControlAuthority(payloadIndex, cb));
        try {
            return doExecuteSdkCommand(drone, droneSn, action, payloadIndex, args);
        } finally {
            try {
                SdkCallbackBridge.awaitVoid(cb -> authModule.releasePayloadControlAuthority(payloadIndex, cb));
            } catch (Exception releaseEx) {
                log.warn("fh_camera: failed to release payload control authority after {} on drone {}",
                        action, droneSn, releaseEx);
            }
        }
    }

    private Map<String, Object> doExecuteSdkCommand(Drone drone, String droneSn, String action,
                                                    String payloadIndex,
                                                    Map<String, Object> args) throws Exception {
        return switch (action) {
            case ACTION_CAMERA_PHOTO -> executeSdkCameraPhoto(drone, droneSn, payloadIndex);
            case ACTION_CAMERA_VIDEO_START -> executeSdkCameraVideoStart(drone, droneSn, payloadIndex);
            case ACTION_CAMERA_VIDEO_STOP -> executeSdkCameraVideoStop(drone, droneSn, payloadIndex);
            case ACTION_CAMERA_ZOOM -> executeSdkCameraZoom(drone, droneSn, payloadIndex, args);
            case ACTION_GIMBAL_ROTATE -> executeSdkGimbalRotate(drone, droneSn, payloadIndex, args);
            case ACTION_GIMBAL_LOOK_AT -> executeSdkGimbalLookAt(drone, droneSn, payloadIndex, args);
            case ACTION_ZOOM_AT_TARGET -> executeSdkZoomAtTarget(drone, droneSn, payloadIndex, args);
            case ACTION_SWITCH_MODE -> executeSdkSwitchMode(drone, droneSn, payloadIndex, args);
            case ACTION_SWITCH_LENS -> executeSdkSwitchLens(drone, droneSn, payloadIndex, args);
            case ACTION_NIGHT_SCENE -> executeSdkNightScene(drone, droneSn, payloadIndex, args);
            case ACTION_PHOTO_STORAGE -> executeSdkStorage(drone, droneSn, payloadIndex, args, true);
            case ACTION_VIDEO_STORAGE -> executeSdkStorage(drone, droneSn, payloadIndex, args, false);
            case ACTION_THERMAL_GAIN -> executeSdkThermalGain(drone, droneSn, payloadIndex, args);
            case ACTION_PANORAMA_START -> {
                PanoramaParam params = new PanoramaParam();
                params.setPayloadIndex(payloadIndex);
                SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcStartPanorama(params, cb));
                yield successResult(
                        Map.of("drone_sn", droneSn, "action", action, "status", "started"),
                        "Panoramic capture started");
            }
            case ACTION_PANORAMA_STOP -> {
                SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcStopPanorama(payloadIndex, cb));
                yield successResult(
                        Map.of("drone_sn", droneSn, "action", action, "status", "stopped"),
                        "Panoramic capture stopped");
            }
            case ACTION_GIMBAL_RESET -> {
                int resetMode = getIntArg(args, PARAM_RESET_MODE, -1);
                if (resetMode < 0 || resetMode > RESET_MODE_MAX) {
                    yield errorResult("Invalid or missing reset_mode",
                            "Must be 0~3 (0=center, 1=down, 2=yaw-center, 3=pitch-down).");
                }
                SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcResetGimbal(payloadIndex, resetMode, cb));
                yield successResult(
                        Map.of("drone_sn", droneSn, "action", action, "reset_mode", resetMode),
                        "Gimbal reset: " + describeResetMode(resetMode));
            }
            default -> errorResult("Unhandled command action: " + action, "Report this bug.");
        };
    }

    /**
     * SDK: 拍照。
     * 自动检测当前相机模式并切换到 PHOTO 模式，等待就绪后拍照。
     * 拍照失败时自动重试一次（应对瞬态 DRC 链路问题）。
     * 注意：Payload 控制权已由外层 executeSdkCommand 统一管理。
     */
    private Map<String, Object> executeSdkCameraPhoto(Drone drone, String droneSn,
                                                      String payloadIndex) throws Exception {
        // 1. 自动检测并切换到拍照模式
        ensurePhotoMode(drone, droneSn, payloadIndex);

        // 2. 等待相机就绪
        if (!waitForTakePhotoAvailable(drone, droneSn, payloadIndex)) {
            return errorResult(
                    "Camera not ready to take photo (is_take_photo_available_ is false) for drone " + droneSn,
                    "DO NOT RETRY blindly — this usually means the camera is locked by an active operation. "
                            + "Common causes: "
                            + "(1) A wayline task is executing (most likely) — the firmware locks ALL manual camera "
                            + "operations during wayline execution. Check with fh_wayline_status; the only unlock is "
                            + "fh_wayline_control(action=cancel), but cancel triggers RTH (drone flies home). "
                            + "(2) Camera is recording video — stop with camera_video_stop first. "
                            + "(3) Camera is processing a previous photo — wait 2-3 seconds, then retry ONCE. "
                            + "If this error persists after one retry, do NOT keep retrying — investigate the cause.");
        }

        // 3. 拍照（首次失败自动重试一次）
        try {
            SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcTakePhoto(payloadIndex, cb));
        } catch (SdkCallbackBridge.SdkException firstError) {
            log.warn("fh_camera 拍照首次失败 [{}]: {}，自动重试: drone={}",
                    firstError.getErrorCode(), firstError.getMessage(), droneSn);
            SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcTakePhoto(payloadIndex, cb));
        }

        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_CAMERA_PHOTO, "payload_index", payloadIndex),
                "Photo taken");
    }

    /**
     * SDK: 开始录像。
     */
    private Map<String, Object> executeSdkCameraVideoStart(Drone drone, String droneSn,
                                                           String payloadIndex) throws Exception {
        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcStartRecord(payloadIndex, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_CAMERA_VIDEO_START, "recording", true),
                "Recording started");
    }

    /**
     * SDK: 停止录像。
     */
    private Map<String, Object> executeSdkCameraVideoStop(Drone drone, String droneSn,
                                                          String payloadIndex) throws Exception {
        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcStopRecord(payloadIndex, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_CAMERA_VIDEO_STOP, "recording", false),
                "Recording stopped");
    }

    /**
     * SDK: 变焦。
     */
    private Map<String, Object> executeSdkCameraZoom(Drone drone, String droneSn,
                                                     String payloadIndex,
                                                     Map<String, Object> args) throws Exception {
        int zoomFactor = getIntArg(args, PARAM_ZOOM_FACTOR, 2);
        if (zoomFactor < ZOOM_FACTOR_MIN || zoomFactor > ZOOM_FACTOR_MAX) {
            return errorResult("Invalid zoom_factor: " + zoomFactor,
                    "Must be between " + ZOOM_FACTOR_MIN + " and " + ZOOM_FACTOR_MAX + ".");
        }
        CameraFocalLengthParams focalParams = new CameraFocalLengthParams();
        focalParams.setPayloadIndex(payloadIndex);
        focalParams.setZoomFactor(zoomFactor);
        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSetFocalLength(focalParams, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_CAMERA_ZOOM, "zoom_factor", zoomFactor),
                "Zoom set to " + zoomFactor + "x");
    }

    /**
     * SDK: 云台旋转。
     */
    private Map<String, Object> executeSdkGimbalRotate(Drone drone, String droneSn,
                                                       String payloadIndex,
                                                       Map<String, Object> args) throws Exception {
        double pitch = getDoubleArg(args, PARAM_PITCH, 0.0);
        double yaw = getDoubleArg(args, PARAM_YAW, 0.0);
        if (pitch < PITCH_MIN || pitch > PITCH_MAX) {
            return errorResult("Invalid pitch: " + pitch,
                    "Must be between " + PITCH_MIN + " and " + PITCH_MAX + ".");
        }
        if (yaw < YAW_MIN || yaw > YAW_MAX) {
            return errorResult("Invalid yaw: " + yaw,
                    "Must be between " + YAW_MIN + " and " + YAW_MAX + ".");
        }
        // TODO: 真实 SDK gimbal_rotate 待接入（当前 SDK 接口未确认）
        log.warn("fh_camera: gimbal_rotate real SDK not yet implemented for drone {}", droneSn);
        return errorResult(
                "Action 'gimbal_rotate' is not yet implemented in real SDK mode",
                "Use gimbal_look_at for coordinate-based pointing, or gimbal_reset for preset positions.");
    }

    /**
     * SDK: 云台指向目标坐标。
     */
    private Map<String, Object> executeSdkGimbalLookAt(Drone drone, String droneSn,
                                                       String payloadIndex,
                                                       Map<String, Object> args) throws Exception {
        double lat = getDoubleArg(args, PARAM_TARGET_LAT, Double.NaN);
        double lng = getDoubleArg(args, PARAM_TARGET_LNG, Double.NaN);
        double height = getDoubleArg(args, PARAM_TARGET_HEIGHT, Double.NaN);
        if (Double.isNaN(lat) || Double.isNaN(lng) || Double.isNaN(height)) {
            return errorResult("Missing target coordinates",
                    "Provide target_lat, target_lng, and target_height.");
        }
        LookAtTargetParams lookParams = new LookAtTargetParams();
        lookParams.setPayloadIndex(payloadIndex);
        lookParams.setLatitude(lat);
        lookParams.setLongitude(lng);
        lookParams.setHeight(height);
        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().lookAtTarget(lookParams, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_GIMBAL_LOOK_AT,
                        "target_lat", lat, "target_lng", lng, "target_height", height),
                "Gimbal looking at target");
    }

    /**
     * SDK: 框选放大。
     */
    private Map<String, Object> executeSdkZoomAtTarget(Drone drone, String droneSn,
                                                       String payloadIndex,
                                                       Map<String, Object> args) throws Exception {
        String lensType = getStringArg(args, PARAM_LENS_TYPE, null);
        if (lensType == null || !VALID_LENS_TYPES.contains(lensType)) {
            return errorResult("Invalid or missing lens_type", "Must be one of: " + VALID_LENS_TYPES);
        }
        double x = getDoubleArg(args, PARAM_X, -1.0);
        double y = getDoubleArg(args, PARAM_Y, -1.0);
        double w = getDoubleArg(args, PARAM_WIDTH, -1.0);
        double h = getDoubleArg(args, PARAM_HEIGHT, -1.0);
        Map<String, Object> boxError = validateNormalizedBox(x, y, w, h);
        if (boxError != null) {
            return boxError;
        }

        ZoomAtTargetParams params = new ZoomAtTargetParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraLensType(CameraLensType.valueOf(lensType));
        params.setX(x);
        params.setY(y);
        params.setWidth(w);
        params.setHeight(h);

        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcZoomAtTarget(params, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_ZOOM_AT_TARGET,
                        "payload_index", payloadIndex, "lens_type", lensType,
                        "x", x, "y", y, "width", w, "height", h),
                "Box-select zoom applied");
    }

    /**
     * SDK: 相机模式切换。
     */
    private Map<String, Object> executeSdkSwitchMode(Drone drone, String droneSn,
                                                     String payloadIndex,
                                                     Map<String, Object> args) throws Exception {
        String mode = getStringArg(args, PARAM_CAMERA_MODE, null);
        if (mode == null || !VALID_CAMERA_MODES.contains(mode)) {
            return errorResult("Invalid or missing camera_mode", "Must be one of: " + VALID_CAMERA_MODES);
        }

        SwitchCameraModeParams params = new SwitchCameraModeParams();
        params.setPayloadIndex(payloadIndex);
        params.setCameraMode(CameraMode.valueOf(mode));

        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSwitchCameraMode(params, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_SWITCH_MODE, "camera_mode", mode),
                "Camera mode switched to " + mode);
    }

    /**
     * SDK: 镜头切换。
     */
    private Map<String, Object> executeSdkSwitchLens(Drone drone, String droneSn,
                                                     String payloadIndex,
                                                     Map<String, Object> args) throws Exception {
        String lens = getStringArg(args, PARAM_LENS_TYPE, null);
        if (lens == null || !VALID_LENS_TYPES.contains(lens)) {
            return errorResult("Invalid or missing lens_type", "Must be one of: " + VALID_LENS_TYPES);
        }

        SwitchLiveLensParams params = new SwitchLiveLensParams();
        params.setPayloadIndex(payloadIndex);
        params.setLensType(CameraLensType.valueOf(lens));

        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSwitchLiveLens(params, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_SWITCH_LENS, "lens_type", lens),
                "Live lens switched to " + lens);
    }

    /**
     * SDK: 夜景模式。
     */
    private Map<String, Object> executeSdkNightScene(Drone drone, String droneSn,
                                                     String payloadIndex,
                                                     Map<String, Object> args) throws Exception {
        String nightMode = getStringArg(args, PARAM_NIGHT_MODE, null);
        if (nightMode == null || !VALID_NIGHT_MODES.contains(nightMode)) {
            return errorResult("Invalid or missing night_mode", "Must be one of: " + VALID_NIGHT_MODES);
        }

        SetNightSceneModeParams params = new SetNightSceneModeParams();
        params.setPayloadIndex(payloadIndex);
        params.setMode(toNightSceneMode(nightMode));

        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSetNightSceneMode(params, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_NIGHT_SCENE, "night_mode", nightMode),
                "Night scene mode set to " + nightMode);
    }

    /**
     * SDK: 存储设置（拍照/录像通用）。
     */
    private Map<String, Object> executeSdkStorage(Drone drone, String droneSn,
                                                  String payloadIndex, Map<String, Object> args,
                                                  boolean isPhoto) throws Exception {
        List<String> settings = getStringListArg(args, PARAM_SETTINGS);
        if (settings == null || settings.isEmpty()) {
            return errorResult("Missing or empty settings parameter",
                    "Provide settings as a non-empty string array.");
        }

        if (isPhoto) {
            SetPhotoStorageSettingsParams params = new SetPhotoStorageSettingsParams();
            params.setPayloadIndex(payloadIndex);
            params.setPhotoStorageSettings(settings);
            SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSetPhotoStorageSettings(params, cb));
        } else {
            SetVideoStorageSettingsParams params = new SetVideoStorageSettingsParams();
            params.setPayloadIndex(payloadIndex);
            params.setVideoStorageSettings(settings);
            SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSetVideoStorageSettings(params, cb));
        }

        String action = isPhoto ? ACTION_PHOTO_STORAGE : ACTION_VIDEO_STORAGE;
        return successResult(
                Map.of("drone_sn", droneSn, "action", action, "settings", settings),
                action.replace("_", " ") + " settings applied");
    }

    /**
     * SDK: 红外增益模式。
     */
    private Map<String, Object> executeSdkThermalGain(Drone drone, String droneSn,
                                                      String payloadIndex,
                                                      Map<String, Object> args) throws Exception {
        String gainMode = getStringArg(args, PARAM_GAIN_MODE, null);
        if (gainMode == null || !VALID_GAIN_MODES.contains(gainMode)) {
            return errorResult("Invalid or missing gain_mode", "Must be one of: " + VALID_GAIN_MODES);
        }

        ThermalGainModeSetParams params = new ThermalGainModeSetParams();
        params.setPayloadIndex(payloadIndex);
        params.setThermalGainMode(toIrGainMode(gainMode));

        SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSetThermalGainMode(params, cb));
        return successResult(
                Map.of("drone_sn", droneSn, "action", ACTION_THERMAL_GAIN, "gain_mode", gainMode),
                "Thermal gain mode set to " + gainMode);
    }

    /**
     * 真实 SDK 查询类调用（使用快速 listen + cancel 模式）。
     */
    private Map<String, Object> executeSdkQuery(Drone drone, String droneSn, String action,
                                                String payloadIndex) throws InterruptedException {
        return switch (action) {
            case ACTION_GET_CAMERA_STATE -> {
                AtomicReference<CameraState> ref = new AtomicReference<>();
                CountDownLatch latch = new CountDownLatch(1);
                int listenId = drone.getCameraControlModule().listenDrcCameraState(payloadIndex, state -> {
                    ref.set(state);
                    latch.countDown();
                });
                if (listenId < 0) {
                    throw new SdkCallbackBridge.SdkException(listenId,
                            "Failed to register camera state listener");
                }
                try {
                    if (!latch.await(STATE_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new SdkCallbackBridge.SdkTimeoutException(
                                "Camera state query timed out after " + STATE_QUERY_TIMEOUT_SECONDS + "s");
                    }
                } finally {
                    drone.getCameraControlModule().cancelListenDrcCameraState(payloadIndex, listenId);
                }
                yield successResult(
                        Map.of("drone_sn", droneSn, "action", action,
                                "payload_index", payloadIndex,
                                "camera_state", formatCameraState(ref.get())),
                        "Camera state retrieved");
            }
            case ACTION_GET_GIMBAL_STATE -> {
                AtomicReference<GimbalState> ref = new AtomicReference<>();
                CountDownLatch latch = new CountDownLatch(1);
                int listenId = drone.getCameraControlModule().listenDrcGimbalState(payloadIndex, state -> {
                    ref.set(state);
                    latch.countDown();
                });
                if (listenId < 0) {
                    throw new SdkCallbackBridge.SdkException(listenId,
                            "Failed to register gimbal state listener");
                }
                try {
                    if (!latch.await(STATE_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new SdkCallbackBridge.SdkTimeoutException(
                                "Gimbal state query timed out after " + STATE_QUERY_TIMEOUT_SECONDS + "s");
                    }
                } finally {
                    drone.getCameraControlModule().cancelListenDrcGimbalState(payloadIndex, listenId);
                }
                yield successResult(
                        Map.of("drone_sn", droneSn, "action", action,
                                "payload_index", payloadIndex,
                                "gimbal_state", formatGimbalState(ref.get())),
                        "Gimbal state retrieved");
            }
            case ACTION_GET_LENS_STATE -> {
                AtomicReference<CameraZoomLensState> ref = new AtomicReference<>();
                CountDownLatch latch = new CountDownLatch(1);
                int listenId = drone.getCameraControlModule().listenDrcCameraZoomLensState(payloadIndex, state -> {
                    ref.set(state);
                    latch.countDown();
                });
                if (listenId < 0) {
                    throw new SdkCallbackBridge.SdkException(listenId,
                            "Failed to register lens state listener");
                }
                try {
                    if (!latch.await(STATE_QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        throw new SdkCallbackBridge.SdkTimeoutException(
                                "Lens state query timed out after " + STATE_QUERY_TIMEOUT_SECONDS + "s");
                    }
                } finally {
                    drone.getCameraControlModule().cancelListenDrcCameraZoomLensState(payloadIndex, listenId);
                }
                yield successResult(
                        Map.of("drone_sn", droneSn, "action", action,
                                "payload_index", payloadIndex,
                                "lens_state", formatLensState(ref.get())),
                        "Zoom lens state retrieved");
            }
            default -> errorResult("Unhandled query action: " + action, "Report this bug.");
        };
    }

    // ==================== 状态格式化辅助方法 ====================

    /**
     * 格式化相机状态为 Map。
     */
    private static Map<String, Object> formatCameraState(CameraState state) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("camera_mode", CameraMode.fromValue(state.getCameraMode()).name());
        result.put("is_recording", state.isRecordingState());
        result.put("record_time", state.getRecordTime());
        result.put("remain_record_duration", state.getRemainRecordDuration());
        result.put("is_shooting", state.isPhotoState());
        result.put("remain_photo_num", state.getRemainPhotoNum());
        result.put("interval_photo_interval", state.getIntervalPhotoInterval());
        return result;
    }

    /**
     * 格式化云台状态为 Map。
     */
    private static Map<String, Object> formatGimbalState(GimbalState state) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pitch", state.getPitch());
        result.put("roll", state.getRoll());
        result.put("yaw", state.getYaw());
        return result;
    }

    /**
     * 格式化变焦镜头状态为 Map。
     */
    private static Map<String, Object> formatLensState(CameraZoomLensState state) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("zoom_factor", state.getZoomFactor());
        result.put("zoom_focus_mode", state.getZoomFocusMode());
        result.put("zoom_focus_state", state.getZoomFocusState());
        result.put("zoom_focus_value", state.getZoomFocusValue());
        result.put("zoom_exposure_mode", state.getZoomExposureMode());
        result.put("zoom_exposure_value", state.getZoomExposureValue());
        result.put("zoom_iso", state.getZoomIso());
        result.put("zoom_shutter_speed", state.getZoomShutterSpeed());
        result.put("zoom_aperture_value", state.getZoomApertureValue());
        return result;
    }

    // ==================== 枚举转换辅助方法 ====================

    /**
     * 等待相机进入拍照可用状态（通过 listenDrcCameraState 监听 photoState）。
     *
     * @param drone        目标无人机实例
     * @param droneSn      无人机 SN（仅用于日志）
     * @param payloadIndex 负载索引
     * @return {@code true} 表示相机已就绪，可立即拍照
     */
    @Generated
    private boolean waitForTakePhotoAvailable(Drone drone, String droneSn, String payloadIndex) {
        var cameraModule = drone.getCameraControlModule();
        if (cameraModule == null) {
            log.warn("fh_camera waitForTakePhotoAvailable: CameraControlModule is null for {}", droneSn);
            return false;
        }

        AtomicBoolean availableRef = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = cameraModule.listenDrcCameraState(payloadIndex, state -> {
            if (state.isPhotoState()) {
                availableRef.set(true);
                latch.countDown();
            }
        });

        if (listenId < 0) {
            log.warn("fh_camera waitForTakePhotoAvailable: listenDrcCameraState failed (id={}) for {}",
                    listenId, droneSn);
            return false;
        }

        try {
            boolean signaled = latch.await(TAKE_PHOTO_AVAILABLE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!signaled) {
                log.warn("fh_camera waitForTakePhotoAvailable: timeout after {}s for drone {}",
                        TAKE_PHOTO_AVAILABLE_TIMEOUT_SECONDS, droneSn);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            cameraModule.cancelListenDrcCameraState(payloadIndex, listenId);
        }

        return availableRef.get();
    }

    /**
     * 查询当前相机模式。
     * 复用 listenCameraState + CountDownLatch 模式，超时返回 null（降级策略）。
     *
     * @param drone        无人机实例
     * @param payloadIndex 负载索引
     * @return 当前 CameraMode，查询失败或超时返回 null
     */
    @Generated
    private CameraMode queryCameraMode(Drone drone, String payloadIndex) {
        var cameraModule = drone.getCameraControlModule();
        if (cameraModule == null) {
            return null;
        }

        AtomicReference<CameraMode> modeRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        int listenId = cameraModule.listenDrcCameraState(payloadIndex, state -> {
            modeRef.set(CameraMode.fromValue(state.getCameraMode()));
            latch.countDown();
        });

        if (listenId < 0) {
            log.warn("fh_camera queryCameraMode: listenCameraState failed (id={}) for payload {}",
                    listenId, payloadIndex);
            return null;
        }

        try {
            if (!latch.await(MODE_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("fh_camera queryCameraMode: timeout after {}s for payload {}",
                        MODE_CHECK_TIMEOUT_SECONDS, payloadIndex);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            cameraModule.cancelListenDrcCameraState(payloadIndex, listenId);
        }

        return modeRef.get();
    }

    /**
     * 确保相机处于拍照模式。
     * 查询当前模式，如果不是 PHOTO 则自动切换。查询失败时降级跳过（不阻塞拍照流程）。
     *
     * @param drone        无人机实例
     * @param droneSn      无人机 SN（用于日志）
     * @param payloadIndex 负载索引
     */
    @Generated
    private void ensurePhotoMode(Drone drone, String droneSn, String payloadIndex) {
        CameraMode currentMode = queryCameraMode(drone, payloadIndex);

        if (currentMode == null) {
            log.warn("fh_camera ensurePhotoMode: 无法查询相机模式，降级跳过模式检查: drone={}", droneSn);
            return;
        }

        if (currentMode == CameraMode.PHOTO) {
            log.debug("fh_camera ensurePhotoMode: 相机已在 PHOTO 模式: drone={}", droneSn);
            return;
        }

        log.info("fh_camera ensurePhotoMode: 相机当前为 {} 模式，自动切换到 PHOTO: drone={}",
                currentMode.name(), droneSn);

        try {
            SwitchCameraModeParams params = new SwitchCameraModeParams();
            params.setPayloadIndex(payloadIndex);
            params.setCameraMode(CameraMode.PHOTO);
            SdkCallbackBridge.awaitVoid(cb -> drone.getCameraControlModule().drcSwitchCameraMode(params, cb));
            log.info("fh_camera ensurePhotoMode: 已自动切换到 PHOTO 模式: drone={}", droneSn);
        } catch (Exception e) {
            log.warn("fh_camera ensurePhotoMode: 切换 PHOTO 模式失败，降级继续拍照: drone={}, error={}",
                    droneSn, e.getMessage());
        }
    }

    /**
     * 字符串转 NightSceneMode 枚举。
     */
    private static NightSceneMode toNightSceneMode(String mode) {
        return switch (mode) {
            case "off" -> NightSceneMode.CLOSE;
            case "on" -> NightSceneMode.OPEN;
            case "auto" -> NightSceneMode.AUTO;
            default -> NightSceneMode.UNKNOWN;
        };
    }

    /**
     * 字符串转 IrGainMode 枚举。
     */
    private static IrGainMode toIrGainMode(String mode) {
        return switch (mode) {
            case "auto" -> IrGainMode.Auto;
            case "low" -> IrGainMode.LowGain;
            case "high" -> IrGainMode.HighGain;
            default -> IrGainMode.UNKNOWN;
        };
    }

    // ==================== 通用辅助方法 ====================

    /**
     * 校验归一化坐标框（0~1 范围）。
     *
     * @return null 表示校验通过，非 null 为错误响应
     */
    private static Map<String, Object> validateNormalizedBox(double x, double y,
                                                            double width, double height) {
        if (x < NORMALIZED_MIN || x > NORMALIZED_MAX) {
            return errorResult("Invalid x: " + x, "Must be 0~1 (normalized coordinate).");
        }
        if (y < NORMALIZED_MIN || y > NORMALIZED_MAX) {
            return errorResult("Invalid y: " + y, "Must be 0~1 (normalized coordinate).");
        }
        if (width <= NORMALIZED_MIN || width > NORMALIZED_MAX) {
            return errorResult("Invalid width: " + width, "Must be (0,1] (positive normalized size).");
        }
        if (height <= NORMALIZED_MIN || height > NORMALIZED_MAX) {
            return errorResult("Invalid height: " + height, "Must be (0,1] (positive normalized size).");
        }
        if (x + width > NORMALIZED_MAX) {
            return errorResult("Box overflow: x(" + x + ") + width(" + width + ") > 1",
                    "Ensure x + width <= 1.");
        }
        if (y + height > NORMALIZED_MAX) {
            return errorResult("Box overflow: y(" + y + ") + height(" + height + ") > 1",
                    "Ensure y + height <= 1.");
        }
        return null;
    }

    /**
     * 判断是否为查询类 action。
     */
    private static boolean isQueryAction(String action) {
        return action.startsWith("get_");
    }

    /**
     * 将云台复位模式数值转为人可读描述。
     */
    private static String describeResetMode(int mode) {
        return switch (mode) {
            case 0 -> "center";
            case 1 -> "down";
            case 2 -> "yaw-center";
            case 3 -> "pitch-down";
            default -> "unknown(" + mode + ")";
        };
    }
}
