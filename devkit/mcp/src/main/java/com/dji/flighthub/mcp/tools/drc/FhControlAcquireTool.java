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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_control_acquire — 获取无人机飞行控制权。
 * 飞行控制权是全局独占资源，同一时刻只有一个操作者可以控制设备。
 * 操作完成后必须调用 fh_control_release 释放控制权。
 *
 * <p>安全等级：L0（获取控制权是可逆操作，可通过 release 释放；无需确认）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhControlAcquireTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhControlAcquireTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_control_acquire";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    /** 控制权授予后的警告信息 */
    private static final String WARNING_RELEASE_REQUIRED =
            "Control authority granted. You MUST call fh_control_release after operations complete.";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhControlAcquireTool(SessionManager sessionManager, SdkService sdkService) {
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
        return false;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Acquire flight control authority for a drone. "
                        + "Control authority is a globally exclusive resource — "
                        + "only one operator can control a device at a time. "
                        + "Must release with fh_control_release after operations complete.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN)"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行获取飞行控制权操作。
     * 前置条件：session 有效。
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

        // 2. 校验 drone_sn 参数
        String droneSn = getStringArg(args, PARAM_DRONE_SN, null);
        if (droneSn == null) {
            return errorResult("Missing required parameter: drone_sn",
                    "Provide drone_sn. Use fh_device_list to get available drone SNs.");
        }

        // 3. 跨 session 冲突检测（多 session 模式下，另一个用户可能已持有控制权）
        SessionContext conflict = sessionManager.findSessionControlling(droneSn);
        if (conflict != null && !conflict.getSessionId().equals(session.getSessionId())) {
            String conflictUser = conflict.getUserId() != null ? conflict.getUserId() : "unknown";
            return errorResult("Control authority conflict",
                    "Drone " + droneSn + " is currently controlled by another session (user=" + conflictUser
                    + ", session=" + conflict.getSessionId() + "). "
                    + "That user must call fh_control_release first.");
        }

        // 4. 检查当前 session 是否已控制其他设备
        if (session.hasControlAuthority() && !session.hasControlOf(droneSn)) {
            String currentSn = session.getActiveControlDeviceSn();
            return errorResult("Already controlling device: " + currentSn,
                    "Call fh_control_release(drone_sn='" + currentSn + "') first, then acquire for " + droneSn + ".");
        }

        // 5. 检查是否已控制同一设备（幂等保护）
        if (session.hasControlOf(droneSn)) {
            return errorResult("Already holding control of " + droneSn,
                    "You already have control authority for this drone. Proceed with flight operations.");
        }

        // 6. Mock 模式：直接授权
        if (sdkService.isMockMode()) {
            return acquireInMockMode(session, droneSn);
        }

        // 7. 真实 SDK 调用
        return acquireWithRealSdk(session, droneSn);
    }

    /**
     * Mock 模式下授予控制权。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 成功响应
     */
    private Map<String, Object> acquireInMockMode(SessionContext session, String droneSn) {
        session.setActiveControlDevice(droneSn);
        log.info("[MOCK] Control authority acquired for drone: {}", droneSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("granted", true);
        data.put("_mock", true);

        Map<String, Object> result = successResult(data,
                "[MOCK] Flight control authority granted for " + droneSn + ".");
        result.put("warning", WARNING_RELEASE_REQUIRED);
        result.put("next_steps", List.of(
                "Call fh_drc_connect(drone_sn='" + droneSn + "') to establish DRC link",
                "Call fh_control_release(drone_sn='" + droneSn + "') after operations complete"
        ));
        return result;
    }

    /**
     * 真实 SDK 模式下获取控制权。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> acquireWithRealSdk(SessionContext session, String droneSn) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            drone.getControlAuthModule().acquireFlightControlAuthority(error -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(
                                    error.getServerCode(), formatSdkError(error)));
                }
            });
            SdkCallbackBridge.await(future);

            session.setActiveControlDevice(droneSn);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("granted", true);

            Map<String, Object> result = successResult(data,
                    "Flight control authority granted for " + droneSn + ".");
            result.put("warning", WARNING_RELEASE_REQUIRED);
            result.put("next_steps", List.of(
                    "Call fh_drc_connect(drone_sn='" + droneSn + "') to establish DRC link",
                    "Call fh_control_release(drone_sn='" + droneSn + "') after operations complete"
            ));
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_control_acquire timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_control_acquire SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            log.error("fh_control_acquire error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }
}
