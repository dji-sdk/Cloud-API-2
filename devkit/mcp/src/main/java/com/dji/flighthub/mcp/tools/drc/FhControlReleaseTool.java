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
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_control_release — 释放无人机飞行控制权。
 * 无论飞行操作是否成功，都必须在操作完成后调用此工具释放控制权。
 *
 * <p>安全等级：L1（释放操作无需 confirm）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhControlReleaseTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhControlReleaseTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_control_release";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhControlReleaseTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

@Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

    /**
     * 返回本工具的 MCP 协议定义，包含名称、描述和 inputSchema。
     *
     * @return MCP 工具定义 Map
     */
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Release flight control authority. "
                        + "Always call this after flight operations complete, even on failure paths.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN) to release control authority for"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行释放飞行控制权操作。
     * 前置条件：持有控制权 + DRC 链路已断开。安全等级 L2。
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
                    "Provide drone_sn of the drone to release control from.");
        }

        // 3. 校验是否持有该设备的控制权
        if (!session.hasControlOf(droneSn)) {
            return errorResult("Not controlling drone: " + droneSn,
                    "You do not hold control authority for " + droneSn + ". Nothing to release.");
        }

        // 4. 校验 DRC 链路是否已断开
        if (session.hasDrcLinkTo(droneSn)) {
            return errorResult("DRC link still active",
                    "DRC link still active, call fh_drc_disconnect first.");
        }

        // 5. Mock 模式：直接释放
        if (sdkService.isMockMode()) {
            return releaseInMockMode(session, droneSn);
        }

        // 6. 真实 SDK 调用（生产路径，需要真实 SDK 环境）
        return releaseWithRealSdk(session, droneSn);
    }

    /**
     * Mock 模式下释放控制权。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 成功响应
     */
    private Map<String, Object> releaseInMockMode(SessionContext session, String droneSn) {
        session.clearControlDevice();
        session.setFlightPhase(FlightPhase.IDLE);
        log.info("[MOCK] Control authority released for drone: {}", droneSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("released", true);
        data.put("_mock", true);

        Map<String, Object> result = successResult(data,
                "[MOCK] Flight control authority released for " + droneSn + ".");
        result.put("next_steps", List.of(
                "Use fh_device_list to check available drones",
                "Call fh_control_acquire to acquire control of another drone if needed"
        ));
        return result;
    }

    /**
     * 真实 SDK 模式下释放控制权。
     * 无论 SDK 调用是否成功，都必须清理本地 session 状态。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> releaseWithRealSdk(SessionContext session, String droneSn) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            boolean droneOffline = (drone == null);

            if (!droneOffline) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                drone.getControlAuthModule().releaseFlightControlAuthority(error -> {
                    if (error.isSuccess()) {
                        future.complete(null);
                    } else {
                        future.completeExceptionally(
                                new SdkCallbackBridge.SdkException(
                                        error.getServerCode(), formatSdkError(error)));
                    }
                });
                SdkCallbackBridge.await(future);
            }

            session.clearControlDevice();
            session.setFlightPhase(FlightPhase.IDLE);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("released", true);
            if (droneOffline) {
                data.put("_warning", "Drone offline — local state cleared but server-side authority may still be held");
                log.warn("fh_control_release: drone {} offline, clearing local state only", droneSn);
            }

            Map<String, Object> result = successResult(data,
                    "Flight control authority released for " + droneSn + ".");
            result.put("next_steps", List.of(
                    "Use fh_device_list to check available drones",
                    "Call fh_control_acquire to acquire control of another drone if needed"
            ));
            return result;

        } catch (Exception e) {
            // 即使 SDK 调用失败，也必须清理本地 session 状态，避免控制权泄露
            log.error("fh_control_release SDK error for drone {}: {}, clearing local state anyway",
                    droneSn, e.getMessage(), e);
            session.clearControlDevice();
            session.setFlightPhase(FlightPhase.IDLE);
            return errorResult("SDK error during release: " + e.getMessage(),
                    "Local control state cleared. Verify drone status before next operation.");
        }
    }
}
