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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.FlightHubCallback.ResultCallbackWithError;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.value.DockCoverMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * fh_dock_control — 远程控制机场设备。
 * <p>操作类型：cover_open / cover_close / charge_start / charge_stop / light_on / light_off
 * <p>安全等级：L1（需要 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDockControlTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDockControlTool.class);

    private static final List<String> VALID_ACTIONS = List.of(
            "cover_open", "cover_close", "charge_start", "charge_stop", "light_on", "light_off");

    private final SdkService sdkService;

    public FhDockControlTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_dock_control");
        def.put("description", "Remote control dock: open/close cover, start/stop charging, toggle supplement light. "
                + "L1 operation - requires confirm=true.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("dock_sn", Map.of("type", "string", "description", "Target dock SN"));
        props.put("action", Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Control action"
        ));
        props.put("confirm", Map.of("type", "boolean", "description", "Must be true to execute"));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of("dock_sn", "action", "confirm"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String dockSn = (String) args.get("dock_sn");
        String action = (String) args.get("action");

        if (dockSn == null || dockSn.isBlank()) {
            return errorResult("Missing dock_sn", "Provide the dock serial number.");
        }
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "Valid actions: " + String.join(", ", VALID_ACTIONS));
        }

        // dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute '" + action + "' on dock " + dockSn,
                    buildDryRunPreview("fh_dock_control", action, dockSn,
                            Map.of("action", action),
                            List.of("session_active", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        if (!getBooleanArg(args, "confirm")) {
            String confirmMessage = String.format(
                    "⚠️ Dangerous operation: About to execute '%s' on dock %s. Do you confirm?", action, dockSn);
            Map<String, Object> denied = requireHumanConfirm(args, action, dockSn, confirmMessage);
            if (denied != null) {
                return denied;
            }
        }

        Dock dock = sdkService.getDock(dockSn);
        if (dock == null) {
            return errorResult("Dock not found: " + dockSn,
                    "Check the SN or call fh_device_list to see available docks.");
        }

        return executeDockCommand(dock, dockSn, action);
    }

    /**
     * 执行机场控制命令。
     */
    @Generated
    private Map<String, Object> executeDockCommand(Dock dock, String dockSn, String action) {
        try {
            CompletableFuture<Void> future = new CompletableFuture<>();
            // SDK 回调统一处理：成功则 complete，失败则 completeExceptionally
            var callback = createSdkCallback(future, action);

            dispatchCommand(dock, action, callback);

            SdkCallbackBridge.await(future);

            return successResult(
                    Map.of("dock_sn", dockSn, "action", action, "status", "completed"),
                    String.format("Dock %s: %s executed successfully.", dockSn, action));

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_dock_control error: {} on {}", action, dockSn, e);
            return errorResult("Control failed", e.getMessage());
        }
    }

    /**
     * 根据 action 分发到对应的 SDK 方法。
     */
    private void dispatchCommand(Dock dock, String action, ResultCallbackWithError callback) {
        switch (action) {
            case "cover_open" -> dock.getDockControlModule().setDockCoverState(DockCoverMode.OPEN, callback);
            case "cover_close" -> dock.getDockControlModule().setDockCoverState(DockCoverMode.CLOSE, callback);
            case "charge_start" -> dock.getDockControlModule().setDockChargeState(true, callback);
            case "charge_stop" -> dock.getDockControlModule().setDockChargeState(false, callback);
            case "light_on" -> dock.getDockControlModule().setDockSupplementLightState(true, callback);
            case "light_off" -> dock.getDockControlModule().setDockSupplementLightState(false, callback);
            default -> throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    /**
     * 创建统一的 SDK 回调处理器。
     */
    private ResultCallbackWithError createSdkCallback(CompletableFuture<Void> future, String action) {
        return (error) -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                action + " failed: " + error.getErrorCode()));
            }
        };
    }
}
