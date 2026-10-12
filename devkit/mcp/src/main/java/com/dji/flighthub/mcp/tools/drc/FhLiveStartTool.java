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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * fh_live_start — 控制无人机/机场摄像头的直播推流（开始 / 停止 / 调整画质）。
 * 无需飞行控制权，直播为被动观测操作。
 *
 * <p>说明：MCP 仅控制推流的开始/停止/画质，视频帧（H.264）通过 SDK 侧信道传输，不经由 MCP。
 *
 * <p>安全等级：L1（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhLiveStartTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhLiveStartTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_live_start";

    /** device_sn 参数名（兼容无人机与机场） */
    private static final String PARAM_DEVICE_SN = "device_sn";

    /** action 参数名 */
    private static final String PARAM_ACTION = "action";

    /** confirm 参数名 */
    private static final String PARAM_CONFIRM = "confirm";

    /** payload_index 参数名 */
    private static final String PARAM_PAYLOAD_INDEX = "payload_index";

    /** quality 参数名 */
    private static final String PARAM_QUALITY = "quality";

    /** 主相机默认负载索引 */
    private static final String DEFAULT_PAYLOAD_INDEX = "81-0-0";

    /** 默认画质 */
    private static final String DEFAULT_QUALITY = "auto";

    /** Mock 直播推流基础地址 */
    private static final String MOCK_RTMP_BASE = "rtmp://mock-server/live/";

    // ==================== Action 常量 ====================
    private static final String ACTION_START = "start";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_QUALITY = "quality";
    private static final String ACTION_SWITCH_SOURCE = "switch_source";

    /** 有效 action 集合（Set 提供 O(1) 查找） */
    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_START, ACTION_STOP, ACTION_QUALITY, ACTION_SWITCH_SOURCE);

    /** 有效 action 列表（用于错误提示，保证可读性） */
    private static final List<String> VALID_ACTIONS_SORTED = List.of("quality", "start", "stop", "switch_source");

    /** 有效画质选项列表 */
    private static final List<String> VALID_QUALITY_OPTIONS = List.of("auto", "hd", "sd");

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhLiveStartTool(SessionManager sessionManager, SdkService sdkService) {
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
        return SecurityLevel.REVERSIBLE;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description", "Start or stop live video streaming from drone/dock camera. "
                + "Also supports switching camera source mid-stream (action=switch_source).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Target device serial number (SN) — supports both drones and docks"
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Streaming action: start/stop/quality/switch_source"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the live streaming command"
        ));
        props.put(PARAM_PAYLOAD_INDEX, Map.of(
                "type", "string",
                "description", "Camera payload index (e.g. '81-0-0'), default: primary camera"
        ));
        props.put(PARAM_QUALITY, Map.of(
                "type", "string",
                "enum", VALID_QUALITY_OPTIONS,
                "description", "Stream quality (for action=quality): auto/sd/hd, default: auto"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION, PARAM_CONFIRM));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行直播推流控制操作。
     * 前置条件：session 有效 + confirm。安全等级 L1。
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

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L1 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 2. 校验 device_sn 参数不为空
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device_sn. Use fh_device_list to get available device SNs.");
        }

        // 3. 校验 action 是否合法
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult(
                    "Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        // 4. 需要人工确认（L1 安全操作）：优先 client 原生弹窗，回退 confirm 参数
        Map<String, Object> confirmError = requireHumanConfirm(args, "live_" + action, deviceSn,
                String.format("⚠️ 即将对设备 %s 执行直播推流操作（%s），是否确认？", deviceSn, action));
        if (confirmError != null) {
            return confirmError;
        }

        // 5. Mock 模式：分发到 mock 处理器
        if (sdkService.isMockMode()) {
            return dispatchMock(deviceSn, action, args);
        }

        // 6. 真实 SDK 调用（生产路径，待接入）
        return executeWithRealSdk(deviceSn, action, args);
    }

    /**
     * Mock 模式下根据 action 分发直播控制指令。
     *
     * @param deviceSn 目标设备 SN
     * @param action   操作类型
     * @param args     原始参数 Map
     * @return 执行结果
     */
    private Map<String, Object> dispatchMock(String deviceSn, String action, Map<String, Object> args) {
        log.info("[MOCK] fh_live_start action={} on device={}", action, deviceSn);

        return switch (action) {
            case ACTION_START -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("streaming", true);
                data.put("stream_url", MOCK_RTMP_BASE + deviceSn);
                data.put("_mock", true);
                yield successResult(data, "Live stream started for " + deviceSn);
            }
            case ACTION_STOP -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("streaming", false);
                data.put("_mock", true);
                yield successResult(data, "Live stream stopped for " + deviceSn);
            }
            case ACTION_QUALITY -> {
                String quality = getStringArg(args, PARAM_QUALITY, DEFAULT_QUALITY);
                if (!VALID_QUALITY_OPTIONS.contains(quality)) {
                    yield errorResult("Invalid quality: " + quality,
                            "Must be one of: " + VALID_QUALITY_OPTIONS);
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("quality", quality);
                data.put("_mock", true);
                yield successResult(data, "Stream quality set to " + quality + " for " + deviceSn);
            }
            case ACTION_SWITCH_SOURCE -> {
                String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
                if (payloadIndex == null) {
                    yield errorResult("Missing required parameter: payload_index",
                            "Provide target payload_index to switch to (e.g. '53-0-0' for zoom camera).");
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("payload_index", payloadIndex);
                data.put("switched", true);
                data.put("streaming", true);
                data.put("stream_url", MOCK_RTMP_BASE + deviceSn + "/" + payloadIndex);
                data.put("_mock", true);
                yield successResult(data,
                        "Live stream source switched to " + payloadIndex + " for " + deviceSn);
            }
            default -> errorResult("Unhandled action: " + action, "Report this bug.");
        };
    }

    /**
     * 真实 SDK 模式下执行直播控制指令。
     * 通过 drone.getLiveStream(LiveInfo) 获取 LiveModule，执行 start/stop 操作。
     *
     * @param deviceSn 目标设备 SN
     * @param action   操作类型
     * @param args     原始参数 Map
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(String deviceSn, String action, Map<String, Object> args) {
        try {
            Drone drone = sdkService.getDrone(deviceSn);
            if (drone == null) {
                return errorResult("Device instance not found: " + deviceSn,
                        "Ensure device is online. Use fh_device_list to check.");
            }

            // 用户未指定 payload_index 时，自动探测相机负载
            String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
            if (payloadIndex == null) {
                payloadIndex = sdkService.resolvePayloadIndex(drone);
                if (payloadIndex == null) {
                    payloadIndex = DEFAULT_PAYLOAD_INDEX;
                    log.warn("fh_live_start: auto-detect payload_index failed, fallback to {}", payloadIndex);
                } else {
                    log.info("fh_live_start: auto-detected payload_index={} for device {}", payloadIndex, deviceSn);
                }
            }

            // 构建 LiveInfo
            LiveInfo liveInfo = new LiveInfo();
            liveInfo.setDeviceSn(deviceSn);
            liveInfo.setPayloadIndex(payloadIndex);
            liveInfo.setVideoId(deviceSn + "/" + payloadIndex);

            // 获取 LiveModule
            LiveModule liveModule = drone.getLiveStream(liveInfo);
            if (liveModule == null) {
                return errorResult("LiveModule unavailable for device: " + deviceSn,
                        "Cannot get live stream module. Ensure device is online and supports live streaming.");
            }

            return switch (action) {
                case ACTION_START -> startLiveReal(liveModule, deviceSn, payloadIndex);
                case ACTION_STOP -> stopLiveReal(liveModule, deviceSn);
                case ACTION_QUALITY -> setQualityReal(deviceSn, args);
                case ACTION_SWITCH_SOURCE -> switchSourceReal(drone, deviceSn, args);
                default -> errorResult("Unhandled action: " + action, "Report this bug.");
            };
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_live_start SDK error for device {} action {}: code={}, msg={}",
                    deviceSn, action, e.getErrorCode(), e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check device connectivity and try again.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_live_start timeout for device {} action {}: {}",
                    deviceSn, action, e.getMessage(), e);
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Live stream operation timed out. Check device connectivity.");
        } catch (Exception e) {
            log.error("fh_live_start error for device {}: {}", deviceSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check device connectivity and try again.");
        }
    }

    /**
     * 真实 SDK — 启动直播推流。
     * 调用 LiveModule.start(callback) 启动推流。
     *
     * @param liveModule   LiveModule 实例
     * @param deviceSn     设备 SN
     * @param payloadIndex 负载索引
     * @return 执行结果
     */
    private Map<String, Object> startLiveReal(LiveModule liveModule, String deviceSn, String payloadIndex) {
        // start 使用 ResultCallbackWithError (void onResult(FlightHubError error))
        SdkCallbackBridge.awaitVoid(liveModule::start);

        log.info("[REAL] Live stream started for device={}, payload={}", deviceSn, payloadIndex);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("payload_index", payloadIndex);
        data.put("streaming", true);

        return successResult(data, "Live stream started for " + deviceSn);
    }

    /**
     * 真实 SDK — 停止直播推流。
     * 调用 LiveModule.stop() 停止推流（同步方法，无回调）。
     *
     * @param liveModule LiveModule 实例
     * @param deviceSn   设备 SN
     * @return 执行结果
     */
    private Map<String, Object> stopLiveReal(LiveModule liveModule, String deviceSn) {
        // stop 是同步方法，直接调用
        liveModule.stop();

        log.info("[REAL] Live stream stopped for device={}", deviceSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("streaming", false);

        return successResult(data, "Live stream stopped for " + deviceSn);
    }

    /**
     * 真实 SDK — 调整直播画质。
     * LiveModule 当前无独立的 setQuality 接口，保留 mock 行为并标记。
     *
     * @param deviceSn 设备 SN
     * @param args     原始参数
     * @return 执行结果（带 _source=not_yet_supported 标记）
     */
    private Map<String, Object> setQualityReal(String deviceSn, Map<String, Object> args) {
        String quality = getStringArg(args, PARAM_QUALITY, DEFAULT_QUALITY);
        if (!VALID_QUALITY_OPTIONS.contains(quality)) {
            return errorResult("Invalid quality: " + quality,
                    "Must be one of: " + VALID_QUALITY_OPTIONS);
        }

        // LiveModule 无独立 setQuality 接口
        log.warn("[REAL] set_quality for device={}: SDK 无独立画质调节接口", deviceSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("quality", quality);
        data.put("_source", "not_yet_supported");
        data.put("_warning", "quality adjustment has no dedicated SDK API — quality parameter recorded but not applied");

        return successResult(data, "Stream quality set to " + quality + " for " + deviceSn + " (pending SDK support)");
    }

    /**
     * 真实 SDK — 切换直播视频源（停止当前推流 → 用新 payload_index 重启）。
     *
     * @param drone    设备实例
     * @param deviceSn 设备 SN
     * @param args     原始参数
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> switchSourceReal(Drone drone, String deviceSn, Map<String, Object> args) {
        String targetPayloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, null);
        if (targetPayloadIndex == null) {
            return errorResult("Missing required parameter: payload_index",
                    "Provide target payload_index to switch to (e.g. '53-0-0' for zoom camera).");
        }

        // 构建新的 LiveInfo
        LiveInfo liveInfo = new LiveInfo();
        liveInfo.setDeviceSn(deviceSn);
        liveInfo.setPayloadIndex(targetPayloadIndex);
        liveInfo.setVideoId(deviceSn + "/" + targetPayloadIndex);

        LiveModule liveModule = drone.getLiveStream(liveInfo);
        if (liveModule == null) {
            return errorResult("LiveModule unavailable for device: " + deviceSn,
                    "Cannot get live stream module for payload " + targetPayloadIndex);
        }

        // 停止当前推流再启动新源
        liveModule.stop();
        SdkCallbackBridge.awaitVoid(liveModule::start);

        log.info("[REAL] Live stream source switched: device={}, payload={}",
                deviceSn, targetPayloadIndex);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("payload_index", targetPayloadIndex);
        data.put("switched", true);
        data.put("streaming", true);

        return successResult(data,
                "Live stream source switched to " + targetPayloadIndex + " for " + deviceSn);
    }
}
