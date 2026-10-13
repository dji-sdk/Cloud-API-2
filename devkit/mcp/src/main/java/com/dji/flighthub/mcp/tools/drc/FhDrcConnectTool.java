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
 * fh_drc_connect — 建立 DRC（Direct Remote Control）低延迟链路。
 * 执行起飞、指点飞行、兴趣点环绕或摇杆控制命令前必须先建立 DRC 链路。
 * 前置条件：必须已获取飞行控制权（fh_control_acquire）。
 *
 * <p>安全等级：L0（建立 DRC 链路是可逆操作，可通过 disconnect 断开；无需确认）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDrcConnectTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDrcConnectTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_drc_connect";

    /** drone_sn 参数名 */
    private static final String PARAM_DRONE_SN = "drone_sn";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhDrcConnectTool(SessionManager sessionManager, SdkService sdkService) {
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
                "Establish DRC (Direct Remote Control) low-latency link. "
                        + "Required before takeoff, flyto, poi, or stick commands. "
                        + "Prerequisite: flight control authority acquired.");

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
     * 执行建立 DRC 链路操作。
     * 前置条件：持有控制权。
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

        // 3. 校验飞行控制权
        Map<String, Object> controlError = requireControlAuthority(session, droneSn);
        if (controlError != null) {
            return controlError;
        }

        // 4. 幂等保护：DRC 链路已激活
        if (session.hasDrcLinkTo(droneSn)) {
            return errorResult("DRC link already active for " + droneSn,
                    "DRC link already active for " + droneSn + ", proceed with flight commands.");
        }

        // 5. Mock 模式：直接建立链路
        if (sdkService.isMockMode()) {
            return connectInMockMode(session, droneSn);
        }

        // 6. 真实 SDK 调用
        return connectWithRealSdk(session, droneSn);
    }

    /**
     * Mock 模式下建立 DRC 链路。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 成功响应
     */
    private Map<String, Object> connectInMockMode(SessionContext session, String droneSn) {
        session.setActiveDrcDevice(droneSn);
        log.info("[MOCK] DRC link established for drone: {}", droneSn);
        return buildConnectResult(droneSn, true);
    }

    /**
     * 真实 SDK 模式下建立 DRC 链路。
     *
     * @param session  当前 session
     * @param droneSn  目标无人机 SN
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> connectWithRealSdk(SessionContext session, String droneSn) {
        try {
            Drone drone = sdkService.getDrone(droneSn);
            if (drone == null) {
                return errorResult("Drone instance not found: " + droneSn,
                        "Ensure drone is online. Use fh_device_list to check.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            drone.getDrcModule().connectDrcLink(error -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(
                                    error.getServerCode(), formatSdkError(error)));
                }
            });
            SdkCallbackBridge.await(future);

            session.setActiveDrcDevice(droneSn);
            return buildConnectResult(droneSn, false);

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_drc_connect timeout for drone {}: {}", droneSn, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(),
                    "Device may be unreachable. Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_drc_connect SDK error for drone {}: [{}] {}", droneSn, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(),
                    "Check drone state and retry.");
        } catch (Exception e) {
            log.error("fh_drc_connect error for drone {}: {}", droneSn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check drone connectivity and try again.");
        }
    }

    /**
     * 构建 DRC 连接成功响应。
     *
     * @param droneSn  目标无人机 SN
     * @param isMock   是否为 mock 模式（影响消息前缀和 _mock 标记）
     * @return 成功结果 Map（含 next_steps）
     */
    private Map<String, Object> buildConnectResult(String droneSn, boolean isMock) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("drone_sn", droneSn);
        data.put("drc_active", true);
        if (isMock) {
            data.put("_mock", true);
        }
        String messagePrefix = isMock ? "[MOCK] " : "";
        Map<String, Object> result = successResult(data,
                messagePrefix + "DRC link established for " + droneSn + ".");
        result.put("next_steps", List.of(
                "Call fh_takeoff(drone_sn='" + droneSn + "', confirm=true) to take off",
                "Call fh_flyto(...) to fly to a target location",
                "Call fh_drc_disconnect(drone_sn='" + droneSn + "') when done"
        ));
        return result;
    }
}
