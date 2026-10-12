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
import com.dji.flighthubsdk.core.Drone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_drc_disconnect — 断开 DRC（Direct Remote Control）低延迟链路。
 * 飞行操作完成后必须调用此工具断开链路，然后再释放飞行控制权。
 *
 * <p>安全等级：L1（断开操作无需 confirm）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDrcDisconnectTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDrcDisconnectTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_drc_disconnect";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhDrcDisconnectTool(SessionManager sessionManager, SdkService sdkService) {
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
                "Disconnect DRC link. "
                        + "Call after flight operations complete, before releasing control authority.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DRONE_SN, Map.of(
                "type", "string",
                "description", "Target drone serial number (SN) to disconnect DRC link for"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DRONE_SN));

        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行断开 DRC 链路操作。
     * 前置条件：DRC 链路已建立。安全等级 L2。
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
                    "Provide drone_sn of the drone to disconnect DRC link from.");
        }

        // 3. 校验 DRC 链路是否存在
        if (!session.hasDrcLinkTo(droneSn)) {
            return errorResult("No DRC link to " + droneSn,
                    "No DRC link to " + droneSn + ", nothing to disconnect.");
        }

        // 4. Mock 模式：直接断开链路
        if (sdkService.isMockMode()) {
            return disconnectInMockMode(session, droneSn);
        }

        // 5. 真实 SDK 调用（生产路径，需要真实 SDK 环境）
        return disconnectWithRealSdk(session, droneSn);
    }

    /**
     * Mock 模式下断开 DRC 链路。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 成功响应
     */
    private Map<String, Object> disconnectInMockMode(SessionContext session, String droneSn) {
        session.clearDrcDevice();
        log.info("[MOCK] DRC link disconnected for drone: {}", droneSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("drc_active", false);
        data.put("_mock", true);

        Map<String, Object> result = successResult(data,
                "[MOCK] DRC link disconnected for " + droneSn + ".");
        result.put("next_steps", List.of(
                "Call fh_control_release(drone_sn='" + droneSn + "') to release flight control authority"
        ));
        return result;
    }

    /**
     * 真实 SDK 模式下断开 DRC 链路。
     * 无论 SDK 调用是否成功，都必须清理本地 session 状态，避免状态泄露。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> disconnectWithRealSdk(SessionContext session, String droneSn) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            boolean droneOffline = (drone == null);

            if (!droneOffline) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                drone.getDrcModule().disconnectDrcLink(error -> {
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

            session.clearDrcDevice();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("drone_sn", droneSn);
            data.put("drc_active", false);
            if (droneOffline) {
                data.put("_warning", "Drone offline — local DRC state cleared but server-side link may still exist");
                log.warn("fh_drc_disconnect: drone {} offline, clearing local state only", droneSn);
            }

            Map<String, Object> result = successResult(data,
                    "DRC link disconnected for " + droneSn + ".");
            result.put("next_steps", List.of(
                    "Call fh_control_release(drone_sn='" + droneSn + "') to release flight control authority"
            ));
            return result;

        } catch (Exception e) {
            // 即使 SDK 调用失败，也必须清理本地 session 状态，避免链路状态泄露
            log.error("fh_drc_disconnect SDK error for drone {}: {}, clearing local state anyway",
                    droneSn, e.getMessage(), e);
            session.clearDrcDevice();
            return errorResult("SDK error during disconnect: " + e.getMessage(),
                    "Local DRC state cleared. Verify drone status before next operation.");
        }
    }
}
