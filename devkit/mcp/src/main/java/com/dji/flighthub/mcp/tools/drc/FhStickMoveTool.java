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
import com.dji.flighthub.mcp.sdk.StickControlExecutor;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.sdk.StickDirection;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.StickControlData;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * fh_stick_move — 按方向和时长移动无人机（内部以 50ms 间隔发送摇杆控制）。
 * 比原始摇杆值更安全 — AI 指定意图而非原始无线电数据。
 * 前置条件：DRC 链路已建立。
 *
 * <p>安全等级：L2（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhStickMoveTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhStickMoveTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_stick_move";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** direction 参数名 */
    private static final String PARAM_DIRECTION = "direction";

    /** confirm 参数名 */
    private static final String PARAM_CONFIRM = "confirm";

    /** speed_percent 参数名 */
    private static final String PARAM_SPEED_PERCENT = "speed_percent";

    /** duration_ms 参数名 */
    private static final String PARAM_DURATION_MS = "duration_ms";

    /** 操作标识，用于 requireConfirm 提示 */
    private static final String ACTION_STICK_MOVE = "stick_move";

    /** 默认速度百分比 */
    private static final int DEFAULT_SPEED_PERCENT = 30;

    /** 默认持续时间（毫秒） */
    private static final int DEFAULT_DURATION_MS = 1000;

    /** 有效方向列表（小写，不可变） */
    private static final List<String> VALID_DIRECTIONS = Arrays.stream(StickDirection.values())
            .map(d -> d.name().toLowerCase())
            .toList();

    private final SdkService sdkService;
    private final StickControlExecutor stickExecutor;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     * @param stickExecutor  摇杆控制执行器
     */
    public FhStickMoveTool(SessionManager sessionManager, SdkService sdkService,
                           StickControlExecutor stickExecutor) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.stickExecutor = stickExecutor;
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
                "Move drone by direction and duration (internally sends stick control at 50ms intervals). "
                        + "Safer than raw stick values — AI specifies intent, not raw radio data. "
                        + "Requires DRC link.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        props.put(PARAM_DIRECTION, Map.of(
                "type", "string",
                "enum", VALID_DIRECTIONS,
                "description", "Movement direction: forward/backward/left/right/up/down/rotate_cw/rotate_ccw"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm the stick move command"
        ));
        props.put(PARAM_SPEED_PERCENT, Map.of(
                "type", "integer",
                "description", "Speed as percentage of max (10~100, default: 30)"
        ));
        props.put(PARAM_DURATION_MS, Map.of(
                "type", "integer",
                "description", "Movement duration in milliseconds (100~5000, default: 1000)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN, PARAM_DIRECTION, PARAM_CONFIRM));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行受限摇杆移动操作。
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

        // 3. 飞行操作前置人工确认：摇杆控制先确认再抢控
        //    dry_run 模式无需确认（见 step 6）
        if (!isDryRun(args)) {
            Map<String, Object> preConfirmError = requireHumanConfirm(args, ACTION_STICK_MOVE, droneSn,
                    buildConfirmMessage(droneSn, args));
            if (preConfirmError != null) {
                return preConfirmError;
            }
        }

        // 4. 确保 DRC 链路就绪（自动获取控制权 + 连接 DRC）— 确认通过后才执行
        Map<String, Object> drcError = ensureDrcLink(session, droneSn, sdkService);
        if (drcError != null) {
            return drcError;
        }

        // 5. 解析并校验 direction 参数
        String dirStr = getStringArg(args, PARAM_DIRECTION, null);
        if (dirStr == null) {
            return errorResult("Missing required parameter: direction",
                    "Provide direction. Valid values: " + VALID_DIRECTIONS);
        }

        StickDirection direction = parseDirection(dirStr);
        if (direction == null) {
            return errorResult("Invalid direction: " + dirStr,
                    "direction must be one of: " + VALID_DIRECTIONS);
        }

        // 6. 读取可选参数（使用默认值）
        int speedPercent = getIntArg(args, PARAM_SPEED_PERCENT, DEFAULT_SPEED_PERCENT);
        int durationMs = getIntArg(args, PARAM_DURATION_MS, DEFAULT_DURATION_MS);

        log.info("Stick move: drone={}, direction={}, speed={}%, duration={}ms",
                droneSn, direction, speedPercent, durationMs);

        // 7. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would move drone " + droneSn + " " + dirStr
                            + " at " + speedPercent + "% speed for " + durationMs + "ms",
                    buildDryRunPreview(TOOL_NAME, ACTION_STICK_MOVE, droneSn,
                            Map.of("direction", dirStr, "speed_percent", speedPercent, "duration_ms", durationMs),
                            List.of("session_active", "security_level_allowed", "drc_linked", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 8. 人工确认已在 step 3 完成

        // 9. Mock 模式：使用空操作 sender 执行
        if (sdkService.isMockMode()) {
            return executeInMockMode(droneSn, direction, speedPercent, durationMs);
        }

        // 9. 真实 SDK 调用（生产路径，需要真实 SDK 环境）
        return executeWithRealSdk(session, droneSn, direction, speedPercent, durationMs);
    }

    /**
     * 构建摇杆移动人工确认提示。
     *
     * @param droneSn 目标无人机 SN
     * @param args    输入参数（读取方向/速度/时长）
     * @return 面向真人的确认提示
     */
    private String buildConfirmMessage(String droneSn, Map<String, Object> args) {
        String direction = getStringArg(args, PARAM_DIRECTION, "?");
        int speedPercent = getIntArg(args, PARAM_SPEED_PERCENT, DEFAULT_SPEED_PERCENT);
        int durationMs = getIntArg(args, PARAM_DURATION_MS, DEFAULT_DURATION_MS);
        return String.format(
                "⚠️ 无人机 %s 即将执行摇杆飞行：方向=%s，速度=%d%%，持续=%dms。是否确认？",
                droneSn, direction, speedPercent, durationMs);
    }

    /**
     * Mock 模式下执行摇杆移动指令（使用空操作 sender）。
     *
     * @param droneSn      目标无人机 SN
     * @param direction    移动方向
     * @param speedPercent 速度百分比
     * @param durationMs   持续时间
     * @return 成功响应
     */
    private Map<String, Object> executeInMockMode(String droneSn, StickDirection direction,
                                                   int speedPercent, int durationMs) {
        // 使用空操作 sender，避免真实发送摇杆数据
        int ticksSent = stickExecutor.execute(
                (r, p, t, y, g) -> { },
                direction, speedPercent, durationMs);

        log.info("[MOCK] Stick move completed: drone={}, direction={}, ticks={}", droneSn, direction, ticksSent);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("direction", direction.name().toLowerCase());
        data.put("speed_percent", speedPercent);
        data.put("duration_ms", durationMs);
        data.put("ticks_sent", ticksSent);
        data.put("_mock", true);

        return successResult(data,
                "[MOCK] Drone " + droneSn + " moved " + direction.name().toLowerCase()
                        + " for " + durationMs + "ms at " + speedPercent + "% speed"
                        + " (" + ticksSent + " ticks).");
    }

    /**
     * 真实 SDK 模式下执行摇杆移动指令。
     * 使用 drone.sendStickControl(StickControlData) 同步发送摇杆数据。
     * 通过 executeWithDrcRetry 实现 DRC 链路故障时自动重连重试。
     *
     * @param session      当前 session
     * @param droneSn      目标无人机 SN
     * @param direction    移动方向
     * @param speedPercent 速度百分比
     * @param durationMs   持续时间
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(SessionContext session, String droneSn,
                                                    StickDirection direction,
                                                    int speedPercent, int durationMs) {
        try {
            return executeWithDrcRetry(session, droneSn, sdkService, () -> {
                Drone drone = sdkService.getDrone(droneSn);
                if (drone == null) {
                    return errorResult("Drone instance not found: " + droneSn,
                            "Ensure drone is online. Use fh_device_list to check.");
                }

                StickControlExecutor.StickSender sender = (r, p, t, y, g) -> {
                    StickControlData data = new StickControlData();
                    data.setRoll(r);
                    data.setPitch(p);
                    data.setThrottle(t);
                    data.setYaw(y);
                    data.setGimbalPitch(g);
                    drone.getFlightControlModule().drcSendStickControl(data);
                };

                int ticksSent = stickExecutor.execute(sender, direction, speedPercent, durationMs);

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("drone_sn", droneSn);
                data.put("direction", direction.name().toLowerCase());
                data.put("speed_percent", speedPercent);
                data.put("duration_ms", durationMs);
                data.put("ticks_sent", ticksSent);

                return successResult(data,
                        "Drone " + droneSn + " moved " + direction.name().toLowerCase()
                                + " for " + durationMs + "ms at " + speedPercent + "% speed"
                                + " (" + ticksSent + " ticks).");
            });
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_stick_move timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_stick_move SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and DRC link, then try again.");
        } catch (Exception e) {
            log.error("fh_stick_move error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and DRC link, then try again.");
        }
    }

    /**
     * 解析并校验方向参数。
     *
     * @param dirStr 方向字符串
     * @return StickDirection 枚举，解析失败时返回 null
     */
    private StickDirection parseDirection(String dirStr) {
        try {
            return StickDirection.valueOf(dirStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
