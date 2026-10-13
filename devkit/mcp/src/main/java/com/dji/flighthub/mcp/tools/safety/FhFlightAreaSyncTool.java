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

package com.dji.flighthub.mcp.tools.safety;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.DeviceSyncResults;
import com.dji.flighthubsdk.bean.DeviceSyncStatusResults;
import com.dji.flighthubsdk.bean.GetAreasSyncStatusResults;
import com.dji.flighthubsdk.bean.SyncFlightAreasResults;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_flight_area_sync — 同步自定义飞行区到设备或查询同步状态。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L1（sync 推送到设备后立即生效，影响飞行安全）
 * <p>注意：status 操作为只读查询，工具级 requiresHumanConfirmation() 返回 false，
 * 避免 SSE 传输层硬拦截；sync 操作在 execute() 内部通过 requireHumanConfirm() 拦截。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlightAreaSyncTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlightAreaSyncTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flight_area_sync";

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SNS = "device_sns";
    private static final String PARAM_CONFIRM = "confirm";

    /** 操作常量 */
    private static final String ACTION_SYNC = "sync";
    private static final String ACTION_STATUS = "status";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_SYNC, ACTION_STATUS);

    /** 设备 SN 列表上限 */
    private static final int MAX_DEVICE_COUNT = 20;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhFlightAreaSyncTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 工具级标志返回 false：status 操作是只读的，不应被传输层硬拦截。
     * sync 操作的确认在 execute() 内部通过 requireHumanConfirm() 实现。
     */
    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    /**
     * 返回本工具的 MCP 协议定义。
     *
     * @return MCP 工具定义 Map
     */
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Sync custom flight areas to devices or check sync status. "
                + "After sync, devices immediately enforce the flight restrictions.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "sync: push flight areas to devices | status: check sync progress"
        ));
        props.put(PARAM_DEVICE_SNS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Target device serial numbers (1-20 devices)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Required for sync action (not needed for status)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_DEVICE_SNS));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行同步操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        // 1. 校验 session + workspace
        Map<String, Object> preCheck = validateSessionAndWorkspace();
        if (preCheck != null) {
            return preCheck;
        }

        // 2. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 3. 解析并校验 device_sns
        DeviceValidation dv = validateDeviceSns(args);
        if (dv.error() != null) {
            return dv.error();
        }

        // 4. sync 操作需要安全等级 L1 + 人工确认；status 不需要
        if (ACTION_SYNC.equals(action)) {
            Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
            if (denied != null) {
                return denied;
            }

            // dry_run 模式：参数校验通过，返回预览（无需 confirm）
            if (isDryRun(args)) {
                return dryRunResult(
                        "Would sync flight areas to " + dv.devices().size() + " device(s)",
                        buildDryRunPreview(TOOL_NAME, "sync",
                                dv.devices().size() + " devices",
                                Map.of("device_count", dv.devices().size()),
                                List.of("session_active", "security_level_allowed", "devices_valid"),
                                SecurityLevel.REVERSIBLE, true));
            }

            Map<String, Object> confirmError = requireHumanConfirm(args,
                    "flight_area_sync", "sync",
                    buildConfirmMessage(dv.devices()));
            if (confirmError != null) {
                return confirmError;
            }
        }

        // 5. 分发执行
        return sdkService.isMockMode()
                ? dispatchMockMode(action, dv.devices())
                : dispatchRealSdk(action, dv.devices());
    }

    /**
     * 设备 SN 校验结果容器。
     *
     * @param devices 校验通过的设备列表（成功时非 null）
     * @param error   校验失败的错误响应（失败时非 null）
     */
    private record DeviceValidation(List<String> devices, Map<String, Object> error) {
    }

    /**
     * 校验 session 和 workspace 可用性。
     *
     * @return 校验失败返回错误 Map，成功返回 null
     */
    private Map<String, Object> validateSessionAndWorkspace() {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }
        return null;
    }

    /**
     * 解析并校验 device_sns 参数。
     *
     * @return DeviceValidation 包含设备列表或错误信息
     */
    private DeviceValidation validateDeviceSns(Map<String, Object> args) {
        List<String> deviceSns = parseDeviceSns(args);
        if (deviceSns == null) {
            return new DeviceValidation(null, errorResult("Missing or invalid device_sns",
                    "Provide a non-empty array of device serial numbers (max " + MAX_DEVICE_COUNT + ")."));
        }
        if (deviceSns.size() > MAX_DEVICE_COUNT) {
            return new DeviceValidation(null, errorResult(
                    "Too many devices: " + deviceSns.size() + " (max " + MAX_DEVICE_COUNT + ")",
                    "Reduce the device list to " + MAX_DEVICE_COUNT + " or fewer."));
        }
        return new DeviceValidation(deviceSns, null);
    }

    /**
     * 从参数中解析 device_sns 列表（去重）。
     *
     * @return 解析成功返回去重后的 List<String>，失败返回 null
     */
    @SuppressWarnings("unchecked")
    private List<String> parseDeviceSns(Map<String, Object> args) {
        Object value = args.get(PARAM_DEVICE_SNS);
        if (value == null) {
            return null;
        }
        if (!(value instanceof List)) {
            return null;
        }
        List<?> rawList = (List<?>) value;
        if (rawList.isEmpty()) {
            return null;
        }
        // 去重 + null 安全
        LinkedHashSet<String> unique = new LinkedHashSet<>(rawList.size());
        for (Object item : rawList) {
            if (item == null) {
                continue;
            }
            String sn = item.toString().trim();
            if (!sn.isEmpty()) {
                unique.add(sn);
            }
        }
        return unique.isEmpty() ? null : new ArrayList<>(unique);
    }

    /**
     * Mock 模式分发。
     */
    private Map<String, Object> dispatchMockMode(String action, List<String> deviceSns) {
        return switch (action) {
            case ACTION_SYNC -> syncInMockMode(deviceSns);
            case ACTION_STATUS -> statusInMockMode(deviceSns);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * Mock 模式 — 同步飞行区到设备。
     */
    private Map<String, Object> syncInMockMode(List<String> deviceSns) {
        log.info("[MOCK] Flight area sync initiated for {} devices: {}",
                deviceSns.size(), deviceSns);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_SYNC);
        data.put("device_sns", deviceSns);
        data.put("sync_count", deviceSns.size());
        data.put("message", "Sync initiated for " + deviceSns.size() + " devices");
        data.put("_mock", true);

        return successResult(data,
                "Flight areas sync initiated for " + deviceSns.size() + " device(s).");
    }

    /**
     * Mock 模式 — 查询同步状态。
     */
    private Map<String, Object> statusInMockMode(List<String> deviceSns) {
        log.info("[MOCK] Flight area sync status query for {} devices", deviceSns.size());

        List<Map<String, Object>> devices = new ArrayList<>();
        Instant now = Instant.now();

        for (int i = 0; i < deviceSns.size(); i++) {
            Map<String, Object> device = new LinkedHashMap<>();
            device.put("device_sn", deviceSns.get(i));
            // 交替模拟不同状态
            if (i % 2 == 0) {
                device.put("status", "synced");
                device.put("last_sync_time", now.minusSeconds(3600L * (i + 1)).toString());
            } else {
                device.put("status", "syncing");
                device.put("progress", Math.min(50 + i * 10, 95));
            }
            devices.add(device);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_STATUS);
        data.put("devices", devices);
        data.put("_mock", true);

        return successResult(data,
                "Sync status retrieved for " + deviceSns.size() + " device(s).");
    }

    /**
     * 真实 SDK 模式分发。
     */
    private Map<String, Object> dispatchRealSdk(String action, List<String> deviceSns) {
        try {
            return switch (action) {
                case ACTION_SYNC -> syncWithRealSdk(deviceSns);
                case ACTION_STATUS -> statusWithRealSdk(deviceSns);
                default -> errorResult("Unknown action: " + action, "");
            };
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_flight_area_sync timeout for action {}: {}", action, e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Flight area " + action + " timed out. Check device connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_flight_area_sync SDK error for action {}: {}", action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check device connectivity and try again.");
        } catch (Exception e) {
            log.error("fh_flight_area_sync error for action {}: {}", action, e.getMessage(), e);
            return errorResult("Failed to " + action + " flight areas: " + e.getMessage(),
                    "Check device connectivity and try again.");
        }
    }

    /**
     * 真实 SDK — 同步飞行区到设备。
     */
    @Generated
    private Map<String, Object> syncWithRealSdk(List<String> deviceSns) {
        SafetyServiceModule ssm = sdkService.getWorkspace().getSafetyServiceModule();

        CompletableFuture<SyncFlightAreasResults> future = new CompletableFuture<>();
        ssm.syncCustomizedFlightAreas(deviceSns, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "syncCustomizedFlightAreas failed: " + error.getErrorCode()));
                return;
            }
            future.complete(results);
        });

        SyncFlightAreasResults results = SdkCallbackBridge.await(future);
        List<Map<String, Object>> deviceResults = mapSyncResults(results);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_SYNC);
        data.put("device_sns", deviceSns);
        data.put("sync_count", deviceSns.size());
        data.put("results", deviceResults);
        data.put("message", "Sync initiated for " + deviceSns.size() + " devices");

        return successResult(data,
                "Flight areas sync initiated for " + deviceSns.size() + " device(s).");
    }

    /**
     * 真实 SDK — 查询同步状态。
     */
    @Generated
    private Map<String, Object> statusWithRealSdk(List<String> deviceSns) {
        SafetyServiceModule ssm = sdkService.getWorkspace().getSafetyServiceModule();

        CompletableFuture<GetAreasSyncStatusResults> future = new CompletableFuture<>();
        ssm.getFlightAreasSyncStatus(deviceSns, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "getFlightAreasSyncStatus failed: " + error.getErrorCode()));
                return;
            }
            future.complete(results);
        });

        GetAreasSyncStatusResults results = SdkCallbackBridge.await(future);
        List<Map<String, Object>> devices = mapSyncStatusResults(results);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_STATUS);
        data.put("devices", devices);

        return successResult(data,
                "Sync status retrieved for " + devices.size() + " device(s).");
    }

    /**
     * 将 SDK 同步结果映射为 MCP 响应格式。
     */
    private List<Map<String, Object>> mapSyncResults(SyncFlightAreasResults results) {
        List<Map<String, Object>> deviceResults = new ArrayList<>();
        if (results.getResults() == null) {
            return deviceResults;
        }
        for (DeviceSyncResults item : results.getResults()) {
            Map<String, Object> deviceResult = new LinkedHashMap<>();
            deviceResult.put("device_sn", item.getDeviceSn());
            deviceResult.put("code", item.getCode());
            deviceResult.put("success", item.getCode() == 0);
            deviceResults.add(deviceResult);
        }
        return deviceResults;
    }

    /**
     * 将 SDK 同步状态结果映射为 MCP 响应格式。
     */
    private List<Map<String, Object>> mapSyncStatusResults(GetAreasSyncStatusResults results) {
        List<Map<String, Object>> devices = new ArrayList<>();
        if (results.getList() == null) {
            return devices;
        }
        for (DeviceSyncStatusResults item : results.getList()) {
            Map<String, Object> device = new LinkedHashMap<>();
            device.put("device_sn", item.getSn());
            device.put("status", item.getStatus());
            device.put("reason", item.getReason());
            devices.add(device);
        }
        return devices;
    }

    /**
     * 构建 sync 操作的人工确认提示。
     */
    private String buildConfirmMessage(List<String> deviceSns) {
        String snsPreview = deviceSns.size() <= 3
                ? deviceSns.toString()
                : deviceSns.subList(0, 3) + "...";
        return String.format("即将将所有自定义飞行区同步到 %d 台设备（%s），设备将立即应用飞行限制，确认？",
                deviceSns.size(), snsPreview);
    }
}
