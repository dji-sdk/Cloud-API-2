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
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.bean.LiveQualitySettingParams;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * fh_live_start — 控制无人机/机场摄像头的直播推流（开始 / 停止 / 调整画质），
 * 并支持订阅解码帧元数据（subscribe_frames / events / listen_stop）。
 * 无需飞行控制权，直播为被动观测操作。
 *
 * <p>说明：MCP 仅控制推流的开始/停止/画质，视频帧（H.264）通过 SDK 侧信道传输，不经由 MCP；
 * subscribe_frames 订阅的也只是帧元数据（is_key_frame/frame_size），不传输原始帧字节。
 *
 * <p>安全等级：混合
 * <ul>
 *   <li>start / stop / quality / switch_source — L1（需要 confirm=true，无条件触发人工确认）</li>
 *   <li>subscribe_frames / events / listen_stop — L0（只读订阅，绕过 L1 拦截与人工确认）</li>
 * </ul>
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

    /** video_id 参数名（多路拉流时指定具体视频流） */
    private static final String PARAM_VIDEO_ID = "video_id";

    /** channels 参数名（events/listen_stop 用，可选） */
    private static final String PARAM_CHANNELS = "channels";

    /** since_seq 参数名（events 用，游标） */
    private static final String PARAM_SINCE_SEQ = "since_seq";

    /** limit 参数名（events 用，单次最多返回条数） */
    private static final String PARAM_LIMIT = "limit";

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
    private static final String ACTION_SUBSCRIBE_FRAMES = "subscribe_frames";
    private static final String ACTION_EVENTS = "events";
    private static final String ACTION_LISTEN_STOP = "listen_stop";

    /** 有效 action 集合（Set 提供 O(1) 查找） */
    private static final Set<String> VALID_ACTIONS = Set.of(
            ACTION_START, ACTION_STOP, ACTION_QUALITY, ACTION_SWITCH_SOURCE,
            ACTION_SUBSCRIBE_FRAMES, ACTION_EVENTS, ACTION_LISTEN_STOP);

    /** 有效 action 列表（用于错误提示，保证可读性） */
    private static final List<String> VALID_ACTIONS_SORTED = List.of(
            "events", "listen_stop", "quality", "start", "stop", "subscribe_frames", "switch_source");

    /** 有效画质选项列表 */
    private static final List<String> VALID_QUALITY_OPTIONS = List.of("auto", "hd", "sd");

    /** 订阅 channel：解码帧元数据（is_key_frame + frame_size），不落盘原始帧字节 */
    private static final String CHANNEL_FRAME_METADATA = "frame_metadata";

    /** 订阅注册表命名空间（与 Tool 名对应），scope 为 device_sn（按设备区分） */
    private static final String SUBSCRIBE_NAMESPACE = "live_start";

    /** events 动作默认读取条数 */
    private static final int DEFAULT_EVENTS_LIMIT = 50;

    /** mock 帧大小（字节），用于 subscribe_frames 的 mock 事件 */
    private static final int MOCK_FRAME_SIZE = 32_768;

    private final SdkService sdkService;
    private final ListenChannelSupport listenSupport;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param listenSupport  订阅 channel 通用支持组件
     */
    public FhLiveStartTool(SessionManager sessionManager, SdkService sdkService, ListenChannelSupport listenSupport) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.listenSupport = listenSupport;
    }

    /**
     * 工具级标志返回 false：subscribe_frames/events/listen_stop 是只读订阅操作，
     * 不应被传输层硬拦截（也不受 L1 安全等级门限约束）。
     * start/stop/quality/switch_source 这 4 个控制类 action 的人工确认仍在 execute()
     * 内部通过 requireHumanConfirm() 无条件触发（见 execute() 对应分支），不受本方法影响。
     *
     * @return false
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
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
                + "Also supports switching camera source mid-stream (action=switch_source). "
                + "Additionally supports subscribing to decoded frame metadata: "
                + "subscribe_frames (listen) -> events (poll via cursor since_seq/limit) -> "
                + "listen_stop (release). These 3 actions are read-only and do not affect "
                + "the start/stop/quality/switch_source control actions above.");

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
                "description", "start/stop/quality/switch_source: streaming control actions | "
                        + "subscribe_frames: subscribe to decoded frame metadata (is_key_frame/frame_size) | "
                        + "events: poll buffered frame metadata events since a cursor | "
                        + "listen_stop: unsubscribe frame metadata"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the live streaming command. "
                        + "Not required for subscribe_frames/events/listen_stop."
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
        props.put(PARAM_VIDEO_ID, Map.of(
                "type", "string",
                "description", "Video stream ID for multi-stream. "
                        + "Use fh_live_device action=available_range to get available video IDs. "
                        + "If omitted, defaults to device_sn/payload_index."
        ));
        props.put(PARAM_CHANNELS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Channel names for events/listen_stop (optional; defaults to all "
                        + "currently active channels when omitted). Only channel: 'frame_metadata'."
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
        schema.put("required", List.of(PARAM_DEVICE_SN, PARAM_ACTION));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行直播推流控制操作。
     * 前置条件：session 有效。start/stop/quality/switch_source 需 confirm（安全等级 L1）；
     * subscribe_frames/events/listen_stop 为只读订阅操作，绕过 L1 安全等级拦截与人工确认。
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

        // 3.5 订阅类 action（READ_ONLY）单独分发：绕过 L1 安全等级拦截与人工确认
        if (isSubscriptionAction(action)) {
            return dispatchSubscription(session, deviceSn, action, args);
        }

        // 4. 安全等级硬拦截（session.maxSecurityLevel < L1 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 5. 需要人工确认（L1 安全操作）：优先 client 原生弹窗，回退 confirm 参数
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
                String videoId = getStringArg(args, PARAM_VIDEO_ID, null);
                String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);
                if (videoId == null) {
                    videoId = deviceSn + "/" + payloadIndex;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("video_id", videoId);
                data.put("streaming", true);
                data.put("stream_url", MOCK_RTMP_BASE + videoId);
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

            String videoId = getStringArg(args, PARAM_VIDEO_ID, null);
            if (videoId != null) {
                liveInfo.setVideoId(videoId);
            } else {
                liveInfo.setVideoId(deviceSn + "/" + payloadIndex);
            }

            // 获取 LiveModule
            LiveModule liveModule = drone.getLiveStream(liveInfo);
            if (liveModule == null) {
                return errorResult("LiveModule unavailable for device: " + deviceSn,
                        "Cannot get live stream module. Ensure device is online and supports live streaming.");
            }

            return switch (action) {
                case ACTION_START -> startLiveReal(liveModule, deviceSn, payloadIndex, liveInfo.getVideoId());
                case ACTION_STOP -> stopLiveReal(liveModule, deviceSn);
                case ACTION_QUALITY -> setQualityReal(liveModule, deviceSn, payloadIndex, args);
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
    private Map<String, Object> startLiveReal(LiveModule liveModule, String deviceSn,
                                              String payloadIndex, String videoId) {
        SdkCallbackBridge.awaitVoid(liveModule::start);

        log.info("[REAL] Live stream started for device={}, payload={}, videoId={}",
                deviceSn, payloadIndex, videoId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("payload_index", payloadIndex);
        data.put("video_id", videoId);
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
    private Map<String, Object> setQualityReal(LiveModule liveModule, String deviceSn,
                                               String payloadIndex, Map<String, Object> args) {
        String quality = getStringArg(args, PARAM_QUALITY, DEFAULT_QUALITY);
        if (!VALID_QUALITY_OPTIONS.contains(quality)) {
            return errorResult("Invalid quality: " + quality,
                    "Must be one of: " + VALID_QUALITY_OPTIONS);
        }

        int qualityValue = switch (quality) {
            case "sd" -> 0;
            case "hd" -> 2;
            default -> 1; // auto
        };

        LiveQualitySettingParams params = new LiveQualitySettingParams();
        params.setSn(deviceSn);
        params.setCamera(payloadIndex);
        params.setVideoQuality(qualityValue);

        SdkCallbackBridge.awaitVoid(cb -> liveModule.setLiveQuality(params, cb));

        log.info("[REAL] Live quality set to {}({}) for device={}", quality, qualityValue, deviceSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("quality", quality);
        data.put("quality_value", qualityValue);

        return successResult(data, "Stream quality set to " + quality + " for " + deviceSn);
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

        String videoId = getStringArg(args, PARAM_VIDEO_ID, null);
        if (videoId != null) {
            liveInfo.setVideoId(videoId);
        } else {
            liveInfo.setVideoId(deviceSn + "/" + targetPayloadIndex);
        }

        LiveModule liveModule = drone.getLiveStream(liveInfo);
        if (liveModule == null) {
            return errorResult("LiveModule unavailable for device: " + deviceSn,
                    "Cannot get live stream module for payload " + targetPayloadIndex);
        }

        liveModule.stop();
        SdkCallbackBridge.awaitVoid(liveModule::start);

        log.info("[REAL] Live stream source switched: device={}, payload={}, videoId={}",
                deviceSn, targetPayloadIndex, liveInfo.getVideoId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("payload_index", targetPayloadIndex);
        data.put("video_id", liveInfo.getVideoId());
        data.put("switched", true);
        data.put("streaming", true);

        return successResult(data,
                "Live stream source switched to " + targetPayloadIndex + " for " + deviceSn);
    }

    // ── 订阅分发 ──

    /**
     * 订阅类 action（subscribe_frames/events/listen_stop）分发入口。
     *
     * @param session  当前 session
     * @param deviceSn 目标设备 SN
     * @param action   操作名
     * @param args     输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatchSubscription(SessionContext session, String deviceSn,
                                                       String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_SUBSCRIBE_FRAMES -> handleSubscribeFrames(session, deviceSn, args);
            case ACTION_EVENTS -> handleEvents(session, deviceSn, args);
            case ACTION_LISTEN_STOP -> handleListenStop(session, deviceSn, args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 处理 subscribe_frames：订阅解码帧元数据（is_key_frame + frame_size），返回 listen_id。
     * 不落盘原始帧字节，只保留元数据，避免缓冲区被大对象占满。
     */
    private Map<String, Object> handleSubscribeFrames(SessionContext session, String deviceSn,
                                                        Map<String, Object> args) {
        String payloadIndex = getStringArg(args, PARAM_PAYLOAD_INDEX, DEFAULT_PAYLOAD_INDEX);
        String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, deviceSn, CHANNEL_FRAME_METADATA);
        String scope = SubscriptionBuffer.scope(session, deviceSn);
        boolean mock = sdkService.isMockMode();

        int listenId = listenSupport.startChannel(session, registryKey, CHANNEL_FRAME_METADATA, scope,
                CHANNEL_FRAME_METADATA, mock,
                this::mockFrameMetadataEvent,
                onEvent -> startRealFrameListen(deviceSn, payloadIndex, onEvent),
                listenId2 -> cancelRealFrameListen(deviceSn, payloadIndex, listenId2));

        if (listenId < 0) {
            return errorResult("Failed to subscribe to live frame metadata for device: " + deviceSn,
                    "Check device connectivity and try again.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_SUBSCRIBE_FRAMES);
        data.put("channel", CHANNEL_FRAME_METADATA);
        data.put("listen_id", listenId);
        return successResult(data,
                "Subscribed to frame metadata for device " + deviceSn + ". Poll with action=events.");
    }

    /**
     * 处理 events：按游标轮询设备的帧元数据缓冲事件。
     */
    private Map<String, Object> handleEvents(SessionContext session, String deviceSn, Map<String, Object> args) {
        List<String> channels = resolveChannels(session, deviceSn, args);
        long sinceSeq = getLongArg(args, PARAM_SINCE_SEQ, 0L);
        int limit = getIntArg(args, PARAM_LIMIT, DEFAULT_EVENTS_LIMIT);

        Map<String, Object> streams = new LinkedHashMap<>();
        for (String channel : channels) {
            String registryKey = ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, deviceSn, channel);
            String scope = SubscriptionBuffer.scope(session, deviceSn);
            streams.put(channel, listenSupport.readChannel(session, registryKey, scope, channel, sinceSeq, limit));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_EVENTS);
        data.put("streams", streams);
        return successResult(data, "Polled " + streams.size() + " channel(s).");
    }

    /**
     * 处理 listen_stop：停止设备上一个/多个/全部已订阅的 channel。
     */
    private Map<String, Object> handleListenStop(SessionContext session, String deviceSn, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        List<String> channels = (requested != null && !requested.isEmpty())
                ? requested
                : listenSupport.activeChannels(session, subscribePrefix(deviceSn));

        ListenChannelSupport.StopResult result = listenSupport.stopChannels(session, channels,
                channel -> ListenChannelSupport.registryKey(SUBSCRIBE_NAMESPACE, deviceSn, channel), log);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_LISTEN_STOP);
        data.put("stopped", result.stopped());
        data.put("not_active", result.notActive());
        data.put("failed", result.failed());
        return successResult(data, "Stopped " + result.stopped().size() + " channel(s).");
    }

    /**
     * 解析 events 要轮询的 channel 列表：显式传入 channels 则用它，否则取该设备当前全部已激活 channel。
     */
    private List<String> resolveChannels(SessionContext session, String deviceSn, Map<String, Object> args) {
        List<String> requested = getStringListArg(args, PARAM_CHANNELS);
        if (requested != null && !requested.isEmpty()) {
            return requested;
        }
        return listenSupport.activeChannels(session, subscribePrefix(deviceSn));
    }

    /**
     * 本工具订阅注册表 key 的前缀（namespace::device_sn::）。
     */
    private String subscribePrefix(String deviceSn) {
        return SUBSCRIBE_NAMESPACE + "::" + deviceSn + "::";
    }

    /**
     * 判断是否为订阅类 action（subscribe_frames/events/listen_stop）。
     */
    private boolean isSubscriptionAction(String action) {
        return ACTION_SUBSCRIBE_FRAMES.equals(action)
                || ACTION_EVENTS.equals(action)
                || ACTION_LISTEN_STOP.equals(action);
    }

    /**
     * Mock — 一条解码帧元数据事件。
     */
    private Map<String, Object> mockFrameMetadataEvent() {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("is_key_frame", true);
        event.put("frame_size", MOCK_FRAME_SIZE);
        event.put("_mock", true);
        return event;
    }

    /**
     * 真实 SDK — 订阅解码帧元数据。
     * 调用 LiveModule.subscribeLiveStream(callback) 注册帧回调，仅提取 is_key_frame 和
     * frame_size（帧字节长度）写入缓冲区，不保留原始帧字节，避免内存膨胀。
     *
     * @param deviceSn     设备 SN
     * @param payloadIndex 负载索引
     * @param onEvent      事件落盘回调
     * @return SDK listenId；LiveModule 不可用时返回 -1
     */
    @Generated
    private int startRealFrameListen(String deviceSn, String payloadIndex, Consumer<Map<String, Object>> onEvent) {
        LiveModule liveModule = resolveLiveModuleOrNull(deviceSn, payloadIndex);
        if (liveModule == null) {
            return -1;
        }
        return liveModule.subscribeLiveStream((frameData, info) -> {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("is_key_frame", info.isKeyFrame());
            event.put("frame_size", frameData.length);
            onEvent.accept(event);
        });
    }

    /**
     * 真实 SDK — 取消订阅解码帧元数据。
     * 调用 LiveModule.unSubscribeLiveStream(listenId)。
     *
     * @param deviceSn     设备 SN
     * @param payloadIndex 负载索引
     * @param listenId     SDK 订阅返回的 listenId
     */
    @Generated
    private void cancelRealFrameListen(String deviceSn, String payloadIndex, int listenId) {
        LiveModule liveModule = resolveLiveModuleOrNull(deviceSn, payloadIndex);
        if (liveModule == null) {
            return;
        }
        liveModule.unSubscribeLiveStream(listenId);
    }

    /**
     * 按 device_sn + payload_index 获取 LiveModule；设备不可用时返回 null。
     */
    @Generated
    private LiveModule resolveLiveModuleOrNull(String deviceSn, String payloadIndex) {
        Drone drone = sdkService.getDrone(deviceSn);
        if (drone == null) {
            return null;
        }
        LiveInfo liveInfo = new LiveInfo();
        liveInfo.setDeviceSn(deviceSn);
        liveInfo.setPayloadIndex(payloadIndex);
        return drone.getLiveStream(liveInfo);
    }
}
