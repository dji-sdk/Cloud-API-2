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

package com.dji.flighthub.mcp.tools.airspace;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.FlightTaskModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * fh_tcas — 防碰撞（TCAS）配置工具。
 * 支持查询和更新设备的防碰撞配置信息。
 *
 * <p>对应 SDK 接口：
 * <ul>
 *   <li>FlightTaskModule.getDeviceTcas(centerSn, callback) — 查询</li>
 *   <li>FlightTaskModule.updateDeviceTcas(centerSn, callback) — 更新</li>
 * </ul>
 *
 * <p>安全等级：get=L0（只读）；update=L1（内部通过 requireHumanConfirm 门控）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhTcasTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhTcasTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_tcas";

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_CENTER_SNS = "center_sns";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of("get", "update");

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhTcasTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "TCAS (Traffic Collision Avoidance System) configuration tool. "
                + "Query or update anti-collision settings for specified devices. "
                + "action=get: retrieve current TCAS configuration; "
                + "action=update: apply/refresh TCAS settings (requires confirm=true).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation: 'get' to query, 'update' to apply TCAS config"
        ));
        props.put(PARAM_CENTER_SNS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "List of center device serial numbers for TCAS computation"
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Required for action=update. Pass true to confirm the update.",
                "default", false
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_CENTER_SNS));
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

        // 2. 解析 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action",
                    "Must be one of: " + VALID_ACTIONS);
        }

        // 3. 解析 center_sns
        List<String> centerSns = getStringListArg(args, PARAM_CENTER_SNS);
        if (centerSns == null || centerSns.isEmpty()) {
            return errorResult("Missing required parameter: center_sns",
                    "Provide a non-empty list of center device serial numbers.");
        }

        // 4. 分发 action
        return switch (action) {
            case "get" -> executeGetTcas(centerSns);
            case "update" -> executeUpdateTcas(args, centerSns);
            default -> errorResult("Unknown action: " + action, "Must be one of: " + VALID_ACTIONS);
        };
    }

    // ==================== action 分发方法 ====================

    /**
     * 查询 TCAS 配置（L0 只读）。
     */
    private Map<String, Object> executeGetTcas(List<String> centerSns) {
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockGetTcas(centerSns);
        }
        return getWithRealSdk(centerSns);
    }

    /**
     * 更新 TCAS 配置（L1 写操作，需确认）。
     */
    private Map<String, Object> executeUpdateTcas(Map<String, Object> args, List<String> centerSns) {
        Map<String, Object> confirmError = requireHumanConfirm(args, "update_tcas",
                String.join(",", centerSns),
                String.format("即将更新 %d 台设备的 TCAS 防碰撞配置，确认执行？", centerSns.size()));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockUpdateTcas(centerSns);
        }
        return updateWithRealSdk(centerSns);
    }

    // ==================== 真实 SDK 调用 ====================

    @Generated
    private Map<String, Object> getWithRealSdk(List<String> centerSns) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ftm.getDeviceTcas(centerSns, (error, resp) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getDeviceTcas failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("tcas_configs", resp.getTcas());
                data.put("device_count", centerSns.size());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "TCAS configuration retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "TCAS query timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_tcas get error: {}", e.getMessage(), e);
            return errorResult("Internal error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> updateWithRealSdk(List<String> centerSns) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            CompletableFuture<Void> future = new CompletableFuture<>();
            ftm.updateDeviceTcas(centerSns, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "updateDeviceTcas failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("center_sns", centerSns);
            data.put("status", "updated");
            data.put("device_count", centerSns.size());
            return successResult(data, "TCAS configuration updated for " + centerSns.size() + " device(s).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "TCAS update timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_tcas update error: {}", e.getMessage(), e);
            return errorResult("Internal error", e.getMessage());
        }
    }

    // ==================== Mock 方法 ====================

    private Map<String, Object> mockGetTcas(List<String> centerSns) {
        log.info("[MOCK] getDeviceTcas: centerSns={}", centerSns);

        List<Map<String, Object>> tcasEntries = new ArrayList<>();
        for (String sn : centerSns) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("device_sn", sn);
            entry.put("tcas_enabled", true);
            entry.put("warning_distance_m", 50);
            entry.put("avoidance_distance_m", 20);
            entry.put("max_speed_limit_mps", 10.0);
            tcasEntries.add(entry);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tcas_configs", tcasEntries);
        data.put("device_count", centerSns.size());
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] Retrieved TCAS config for %d device(s).",
                centerSns.size()));
    }

    private Map<String, Object> mockUpdateTcas(List<String> centerSns) {
        log.info("[MOCK] updateDeviceTcas: centerSns={}", centerSns);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("center_sns", centerSns);
        data.put("status", "updated");
        data.put("device_count", centerSns.size());
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] TCAS configuration updated for %d device(s).",
                centerSns.size()));
    }
}
