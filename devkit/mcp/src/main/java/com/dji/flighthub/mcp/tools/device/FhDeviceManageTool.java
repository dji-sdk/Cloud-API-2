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
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * fh_device_manage — 聚合设备管理操作。
 * 操作类型：detail / rename / unbind / hms_current / firmware_list
 * 安全等级：L0（查询类）~ L1（rename/unbind）
 */
@Component
public class FhDeviceManageTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDeviceManageTool.class);

    private final SdkService sdkService;

    public FhDeviceManageTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", "fh_device_manage");
        def.put("description", "Device management operations: get detail info, rename device, unbind device, "
                + "query HMS health alerts, query firmware list. "
                + "Rename/unbind are L1 operations requiring confirm=true.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("action", Map.of(
                "type", "string",
                "enum", List.of("detail", "rename", "unbind", "hms_current"),
                "description", "Management action"
        ));
        props.put("device_sn", Map.of("type", "string", "description", "Target device SN"));
        props.put("new_name", Map.of("type", "string",
                "description", "New display name (required for action=rename)"));
        props.put("confirm", Map.of("type", "boolean",
                "description", "Confirmation flag (required for rename/unbind)"));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of("action", "device_sn"));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = (String) args.get("action");
        String deviceSn = (String) args.get("device_sn");

        if (action == null) {
            return errorResult("Missing action", "Provide action: detail/rename/unbind/hms_current");
        }
        if (deviceSn == null || deviceSn.isBlank()) {
            return errorResult("Missing device_sn", "Provide the device serial number.");
        }

        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return errorResult("SDK not initialized", "Call fh_connect first.");
        }

        return switch (action) {
            case "detail" -> executeDetail(workspace, deviceSn);
            case "rename" -> executeRename(workspace, deviceSn, args);
            case "unbind" -> executeUnbind(workspace, deviceSn, args);
            case "hms_current" -> executeHmsCurrent(deviceSn);
            default -> errorResult("Unknown action: " + action,
                    "Valid actions: detail, rename, unbind, hms_current");
        };
    }

    @Generated
    private Map<String, Object> executeDetail(Workspace workspace, String deviceSn) {
        try {
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            workspace.getDeviceManageModule().getDeviceDetailInfo(deviceSn, (error, detail) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getDeviceDetailInfo failed: " + error.getErrorCode()));
                    return;
                }

                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("device_callsign", detail.getNickname());
                data.put("firmware_version", detail.getDeviceFirmwareVersion());
                data.put("firmware_status", detail.getDeviceFirmwareStatus());
                data.put("online", detail.isOnline());
                data.put("working_status", detail.getDeviceWorkingStatus());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("message", "Device detail retrieved for " + deviceSn);
            return result;

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error", e.getMessage());
        } catch (Exception e) {
            return errorResult("Failed to get detail", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> executeRename(Workspace workspace, String deviceSn, Map<String, Object> args) {
        String newName = (String) args.get("new_name");
        Boolean confirm = (Boolean) args.get("confirm");

        if (newName == null || newName.isBlank()) {
            return errorResult("Missing new_name", "Provide the new display name.");
        }

        // dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would rename device " + deviceSn + " to '" + newName + "'",
                    buildDryRunPreview("fh_device_manage", "rename", deviceSn,
                            Map.of("new_name", newName),
                            List.of("session_active", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        if (!Boolean.TRUE.equals(confirm)) {
            return errorResult("Confirmation required",
                    String.format("About to rename device %s to '%s'. Pass confirm=true to proceed.", deviceSn, newName));
        }

        try {
            CompletableFuture<Void> future = new CompletableFuture<>();

            var item = new com.dji.flighthubsdk.bean.ModifyDeviceNameItem();
            item.setDeviceSn(deviceSn);
            item.setDeviceCallsign(newName);
            var param = new com.dji.flighthubsdk.bean.ModifyDeviceNameParam();
            param.setDeviceList(List.of(item));
            workspace.getDeviceManageModule().modifyDeviceName(param, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "Rename failed: " + error.getErrorCode()));
                }
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", deviceSn, "new_name", newName));
            result.put("message", "Device " + deviceSn + " renamed to '" + newName + "'");
            return result;

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Rename failed", e.getMessage());
        } catch (Exception e) {
            return errorResult("Rename error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> executeUnbind(Workspace workspace, String deviceSn, Map<String, Object> args) {
        Boolean confirm = (Boolean) args.get("confirm");

        // dry_run 模式：返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would unbind device " + deviceSn + " from workspace (irreversible)",
                    buildDryRunPreview("fh_device_manage", "unbind", deviceSn,
                            Map.of(),
                            List.of("session_active"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        if (!Boolean.TRUE.equals(confirm)) {
            return errorResult("Confirmation required",
                    String.format("About to unbind device %s from workspace. This action cannot be undone. Pass confirm=true.", deviceSn));
        }

        try {
            CompletableFuture<Void> future = new CompletableFuture<>();

            workspace.getDeviceManageModule().unbindDevice(List.of(deviceSn), (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "Unbind failed: " + error.getErrorCode()));
                }
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", deviceSn, "action", "unbound"));
            result.put("message", "Device " + deviceSn + " unbound from workspace.");
            return result;

        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Unbind failed", e.getMessage());
        } catch (Exception e) {
            return errorResult("Unbind error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> executeHmsCurrent(String deviceSn) {
        // HMS 数据来自 Drone 或 Dock 的监听通道
        // 简化处理：先尝试 drone，再尝试 dock
        var drone = sdkService.getDrone(deviceSn);
        if (drone != null) {
            return collectDroneHms(drone, deviceSn);
        }

        var dock = sdkService.getDock(deviceSn);
        if (dock != null) {
            return collectDockHms(dock, deviceSn);
        }

        return errorResult("Device not found: " + deviceSn,
                "Cannot find drone or dock with this SN.");
    }

    @Generated
    private Map<String, Object> collectDroneHms(com.dji.flighthubsdk.core.Drone drone, String sn) {
        try {
            var hmsModule = drone.getDeviceHMSModule();
            if (hmsModule == null) {
                return errorResult("DeviceHMSModule not available for drone: " + sn,
                        "Drone firmware may not support HMS. Check firmware version.");
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            int listenId = hmsModule.listenDeviceHMS(hmsData -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", sn);
                data.put("device_type", "drone");
                data.put("hms_raw", hmsData.toString());
                future.complete(data);
            });

            Map<String, Object> data;
            try {
                data = SdkCallbackBridge.await(future, 10);
            } finally {
                if (listenId >= 0) {
                    hmsModule.cancelListenDeviceHMS(listenId);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("message", "HMS data collected for drone " + sn);
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", sn, "hms_alerts", List.of(), "note", "No HMS alerts within 10s (device may be healthy)"));
            result.put("message", "No HMS alerts reported for " + sn);
            return result;
        } catch (Exception e) {
            return errorResult("HMS query failed", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> collectDockHms(com.dji.flighthubsdk.core.Dock dock, String sn) {
        try {
            var hmsModule = dock.getDeviceHMSModule();
            if (hmsModule == null) {
                return errorResult("DeviceHMSModule not available for dock: " + sn,
                        "Dock firmware may not support HMS. Check firmware version.");
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();

            int listenId = hmsModule.listenDeviceHMS(hmsData -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", sn);
                data.put("device_type", "dock");
                data.put("hms_raw", hmsData.toString());
                future.complete(data);
            });

            Map<String, Object> data;
            try {
                data = SdkCallbackBridge.await(future, 10);
            } finally {
                if (listenId >= 0) {
                    hmsModule.cancelListenDeviceHMS(listenId);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("message", "HMS data collected for dock " + sn);
            return result;

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("data", Map.of("device_sn", sn, "hms_alerts", List.of(), "note", "No HMS alerts within 10s"));
            result.put("message", "No HMS alerts reported for " + sn);
            return result;
        } catch (Exception e) {
            return errorResult("HMS query failed", e.getMessage());
        }
    }
}
