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
import com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults;
import com.dji.flighthubsdk.bean.GetOfflineMapsSyncStatusResults;
import com.dji.flighthubsdk.bean.OfflineMapsSyncResults;
import com.dji.flighthubsdk.bean.SetOfflineMapsParams;
import com.dji.flighthubsdk.bean.SignalMapsResults;
import com.dji.flighthubsdk.bean.UpdateOfflineMapsParams;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_offline_map — 离线地图模型管理工具。
 * 支持查询/更新模型信息、启用/禁用、同步到设备、查询同步状态、搜索信号地图、触发飞行安全升级。
 *
 * <p>安全等级：混合
 * <ul>
 *   <li>cancel、invoke_flysafe_upgrade 为 L1 破坏性操作，需人工确认</li>
 *   <li>其余操作为只读或低风险写入</li>
 * </ul>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhOfflineMapTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhOfflineMapTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_offline_map";

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SNS = "device_sns";
    private static final String PARAM_ENABLED = "enabled";
    private static final String PARAM_DATA_TYPES = "data_types";
    private static final String PARAM_REQUEST_PARAMS = "request_params";
    private static final String PARAM_CONFIRM = "confirm";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            "get_model_info", "update_model_info", "cancel", "set_enabled",
            "sync", "get_sync_status", "search_signal_maps", "invoke_flysafe_upgrade"
    );

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhOfflineMapTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 工具级标志返回 false：大部分操作是只读或低风险的。
     * cancel 和 invoke_flysafe_upgrade 在 execute() 内部通过 requireHumanConfirm() 拦截。
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
                "Manage offline map models: query/update model info, enable/disable, "
                + "sync to devices, check sync status, search signal maps, or invoke flysafe upgrade.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "The offline map operation to perform"
        ));
        props.put(PARAM_DEVICE_SNS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Target device serial numbers (required for sync, get_sync_status, search_signal_maps, invoke_flysafe_upgrade)"
        ));
        props.put(PARAM_ENABLED, Map.of(
                "type", "boolean",
                "description", "Enable or disable offline map (for set_enabled action)"
        ));
        props.put(PARAM_DATA_TYPES, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Signal map data types to search (e.g. 4g_lte, 5g_nr)"
        ));
        props.put(PARAM_REQUEST_PARAMS, Map.of(
                "type", "object",
                "description", "Additional request parameters (for update_model_info)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Required for destructive actions: cancel, invoke_flysafe_upgrade"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行离线地图操作。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        return dispatch(action, args);
    }

    /**
     * 操作分发。
     *
     * @param action 操作名称
     * @param args   输入参数
     * @return 执行结果
     */
    private Map<String, Object> dispatch(String action, Map<String, Object> args) {
        return switch (action) {
            case "get_model_info" -> handleGetModelInfo();
            case "update_model_info" -> handleUpdateModelInfo(args);
            case "cancel" -> handleCancel(args);
            case "set_enabled" -> handleSetEnabled(args);
            case "sync" -> handleSync(args);
            case "get_sync_status" -> handleGetSyncStatus(args);
            case "search_signal_maps" -> handleSearchSignalMaps(args);
            case "invoke_flysafe_upgrade" -> handleInvokeFlysafeUpgrade(args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    // ==================== get_model_info ====================

    private Map<String, Object> handleGetModelInfo() {
        log.info("[{}] get_model_info", TOOL_NAME);

        return sdkService.isMockMode()
                ? getModelInfoInMockMode()
                : getModelInfoWithRealSdk();
    }

    private Map<String, Object> getModelInfoInMockMode() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("version", "3.2.1");
        data.put("size_mb", 256);
        data.put("coverage", "china_mainland");
        data.put("status", "ready");
        data.put("_mock", true);
        return successResult(data, "Offline map model info retrieved.");
    }

    @Generated
    private Map<String, Object> getModelInfoWithRealSdk() {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<GetOfflineMapsModelInfoResults> future = new CompletableFuture<>();
            ssm.getOfflineMapsModelInfo((error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getOfflineMapsModelInfo failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetOfflineMapsModelInfoResults result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("disabled", result.isDisable());
            data.put("offline_map", result.getOfflineMap() != null
                    ? result.getOfflineMap().toString() : null);
            return successResult(data, "Offline map model info retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] get_model_info timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] get_model_info SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] get_model_info error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get model info: " + e.getMessage(), null);
        }
    }

    // ==================== update_model_info ====================

    private Map<String, Object> handleUpdateModelInfo(Map<String, Object> args) {
        log.info("[{}] update_model_info", TOOL_NAME);

        return sdkService.isMockMode()
                ? updateModelInfoInMockMode()
                : updateModelInfoWithRealSdk();
    }

    private Map<String, Object> updateModelInfoInMockMode() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "update_started");
        data.put("_mock", true);
        return successResult(data, "Offline map model update started.");
    }

    @Generated
    private Map<String, Object> updateModelInfoWithRealSdk() {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            UpdateOfflineMapsParams params = new UpdateOfflineMapsParams();

            // ResultCallbackWithError: 只有 onResult(FlightHubError)
            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.updateOfflineMapsModelInfo(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "updateOfflineMapsModelInfo failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "update_started");
            return successResult(data, "Offline map model update started.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] update_model_info timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] update_model_info SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] update_model_info error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to update model info: " + e.getMessage(), null);
        }
    }

    // ==================== cancel（L1，需人工确认） ====================

    private Map<String, Object> handleCancel(Map<String, Object> args) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Map<String, Object> confirmError = requireHumanConfirm(args,
                "cancel", "offline map model update",
                "即将取消离线地图模型更新，已下载的数据可能丢失，确认？");
        if (confirmError != null) {
            return confirmError;
        }

        log.info("[{}] cancel", TOOL_NAME);

        return sdkService.isMockMode()
                ? cancelInMockMode()
                : cancelWithRealSdk();
    }

    private Map<String, Object> cancelInMockMode() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "cancelled");
        data.put("_mock", true);
        return successResult(data, "Offline map model update cancelled.");
    }

    @Generated
    private Map<String, Object> cancelWithRealSdk() {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.cancelOfflineMapsModelInfo(error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "cancelOfflineMapsModelInfo failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "cancelled");
            return successResult(data, "Offline map model update cancelled.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] cancel timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] cancel SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] cancel error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to cancel: " + e.getMessage(), null);
        }
    }

    // ==================== set_enabled ====================

    private Map<String, Object> handleSetEnabled(Map<String, Object> args) {
        if (args == null || !args.containsKey(PARAM_ENABLED)) {
            return errorResult("Missing required parameter: enabled",
                    "Provide 'enabled' (true/false) to enable or disable offline map.");
        }
        boolean enabled = getBooleanArg(args, PARAM_ENABLED);
        log.info("[{}] set_enabled: enabled={}", TOOL_NAME, enabled);

        return sdkService.isMockMode()
                ? setEnabledInMockMode(enabled)
                : setEnabledWithRealSdk(enabled);
    }

    private Map<String, Object> setEnabledInMockMode(boolean enabled) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("offline_map_enabled", enabled);
        data.put("_mock", true);
        return successResult(data, "Offline map " + (enabled ? "enabled" : "disabled") + ".");
    }

    @Generated
    private Map<String, Object> setEnabledWithRealSdk(boolean enabled) {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            SetOfflineMapsParams params = new SetOfflineMapsParams();
            params.setOfflineMapEnable(enabled);

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.setOfflineMaps(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "setOfflineMaps failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("offline_map_enabled", enabled);
            return successResult(data, "Offline map " + (enabled ? "enabled" : "disabled") + ".");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] set_enabled timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] set_enabled SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] set_enabled error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to set enabled: " + e.getMessage(), null);
        }
    }

    // ==================== sync ====================

    private Map<String, Object> handleSync(Map<String, Object> args) {
        List<String> deviceSns = parseStringList(args, PARAM_DEVICE_SNS);
        if (deviceSns == null) {
            return errorResult("Missing or empty device_sns",
                    "Provide a non-empty array of device serial numbers.");
        }

        log.info("[{}] sync: {} devices", TOOL_NAME, deviceSns.size());

        return sdkService.isMockMode()
                ? syncInMockMode(deviceSns)
                : syncWithRealSdk(deviceSns);
    }

    private Map<String, Object> syncInMockMode(List<String> deviceSns) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "sync_triggered");
        data.put("device_sns", deviceSns);
        data.put("device_count", deviceSns.size());
        data.put("_mock", true);
        return successResult(data, "Offline map sync triggered for " + deviceSns.size() + " device(s).");
    }

    @Generated
    private Map<String, Object> syncWithRealSdk(List<String> deviceSns) {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<OfflineMapsSyncResults> future = new CompletableFuture<>();
            ssm.syncOfflineMaps(deviceSns, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "syncOfflineMaps failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            OfflineMapsSyncResults result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "sync_triggered");
            data.put("device_count", deviceSns.size());
            data.put("results", result.getResults() != null
                    ? result.getResults().toString() : List.of());
            return successResult(data, "Offline map sync triggered for " + deviceSns.size() + " device(s).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] sync timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] sync SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] sync error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to sync: " + e.getMessage(), null);
        }
    }

    // ==================== get_sync_status ====================

    private Map<String, Object> handleGetSyncStatus(Map<String, Object> args) {
        List<String> deviceSns = parseStringList(args, PARAM_DEVICE_SNS);
        if (deviceSns == null) {
            return errorResult("Missing or empty device_sns",
                    "Provide a non-empty array of device serial numbers.");
        }

        log.info("[{}] get_sync_status: {} devices", TOOL_NAME, deviceSns.size());

        return sdkService.isMockMode()
                ? getSyncStatusInMockMode(deviceSns)
                : getSyncStatusWithRealSdk(deviceSns);
    }

    private Map<String, Object> getSyncStatusInMockMode(List<String> deviceSns) {
        List<Map<String, Object>> devices = new ArrayList<>();
        for (int i = 0; i < deviceSns.size(); i++) {
            Map<String, Object> device = new LinkedHashMap<>();
            device.put("device_sn", deviceSns.get(i));
            if (i % 2 == 0) {
                device.put("status", "synced");
                device.put("progress", 100);
            } else {
                device.put("status", "syncing");
                device.put("progress", 50 + i * 10);
            }
            devices.add(device);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("devices", devices);
        data.put("_mock", true);
        return successResult(data, "Sync status retrieved for " + deviceSns.size() + " device(s).");
    }

    @Generated
    private Map<String, Object> getSyncStatusWithRealSdk(List<String> deviceSns) {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<GetOfflineMapsSyncStatusResults> future = new CompletableFuture<>();
            ssm.getOfflineMapsSyncStatus(deviceSns, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getOfflineMapsSyncStatus failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            GetOfflineMapsSyncStatusResults result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("devices", result.getList() != null
                    ? result.getList().toString() : List.of());
            return successResult(data, "Sync status retrieved for " + deviceSns.size() + " device(s).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] get_sync_status timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] get_sync_status SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] get_sync_status error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get sync status: " + e.getMessage(), null);
        }
    }

    // ==================== search_signal_maps ====================

    private Map<String, Object> handleSearchSignalMaps(Map<String, Object> args) {
        List<String> deviceSns = parseStringList(args, PARAM_DEVICE_SNS);
        if (deviceSns == null) {
            return errorResult("Missing or empty device_sns",
                    "Provide a non-empty array of device serial numbers.");
        }

        List<String> dataTypes = parseStringList(args, PARAM_DATA_TYPES);
        if (dataTypes == null) {
            dataTypes = List.of("4g_lte", "5g_nr");
        }

        log.info("[{}] search_signal_maps: {} devices, types: {}", TOOL_NAME, deviceSns.size(), dataTypes);

        return sdkService.isMockMode()
                ? searchSignalMapsInMockMode(deviceSns, dataTypes)
                : searchSignalMapsWithRealSdk(deviceSns, dataTypes);
    }

    private Map<String, Object> searchSignalMapsInMockMode(List<String> deviceSns, List<String> dataTypes) {
        List<Map<String, Object>> signalMaps = new ArrayList<>();
        for (String type : dataTypes) {
            Map<String, Object> signalMap = new LinkedHashMap<>();
            signalMap.put("type", type);
            signalMap.put("coverage_area", "region_1");
            signalMap.put("quality", "good");
            signalMaps.add(signalMap);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sns", deviceSns);
        data.put("signal_maps", signalMaps);
        data.put("_mock", true);
        return successResult(data, "Signal maps found for " + deviceSns.size() + " device(s).");
    }

    @Generated
    private Map<String, Object> searchSignalMapsWithRealSdk(List<String> deviceSns, List<String> dataTypes) {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<SignalMapsResults> future = new CompletableFuture<>();
            ssm.searchSignalMaps(deviceSns, dataTypes, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "searchSignalMaps failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            SignalMapsResults result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sns", deviceSns);
            data.put("signal_maps", result.getLists() != null
                    ? result.getLists().toString() : List.of());
            return successResult(data, "Signal maps found for " + deviceSns.size() + " device(s).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] search_signal_maps timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] search_signal_maps SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] search_signal_maps error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to search signal maps: " + e.getMessage(), null);
        }
    }

    // ==================== invoke_flysafe_upgrade（L1，需人工确认） ====================

    private Map<String, Object> handleInvokeFlysafeUpgrade(Map<String, Object> args) {
        List<String> deviceSns = parseStringList(args, PARAM_DEVICE_SNS);
        if (deviceSns == null) {
            return errorResult("Missing or empty device_sns",
                    "Provide a non-empty array of device serial numbers.");
        }

        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }
        Map<String, Object> confirmError = requireHumanConfirm(args,
                "invoke_flysafe_upgrade", String.join(",", deviceSns),
                "即将对 " + deviceSns.size() + " 台设备触发飞行安全数据库升级，确认？");
        if (confirmError != null) {
            return confirmError;
        }

        log.info("[{}] invoke_flysafe_upgrade: {} devices", TOOL_NAME, deviceSns.size());

        return sdkService.isMockMode()
                ? invokeFlysafeUpgradeInMockMode(deviceSns)
                : invokeFlysafeUpgradeApiRemoved(deviceSns);
    }

    private Map<String, Object> invokeFlysafeUpgradeInMockMode(List<String> deviceSns) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "upgrade_triggered");
        data.put("device_sns", deviceSns);
        data.put("device_count", deviceSns.size());
        data.put("_mock", true);
        return successResult(data, "Flysafe upgrade triggered for " + deviceSns.size() + " device(s).");
    }

    @Generated
    private Map<String, Object> invokeFlysafeUpgradeApiRemoved(List<String> deviceSns) {
        // invokeFlysafeDatabaseUpgrade 已在 SDK 2.0 中移除，飞安数据库升级由固件升级流程统一管理。
        // 返回错误而非假成功，避免运营方误以为升级指令已下发至设备。
        log.error("invoke_flysafe_upgrade 不可用：invokeFlysafeDatabaseUpgrade 已在 SDK 2.0 中移除，deviceSns={}", deviceSns);
        return errorResult(
                "invoke_flysafe_upgrade 不可用：invokeFlysafeDatabaseUpgrade 已在 SDK 2.0 中移除",
                "请通过固件升级流程（fh_firmware）管理飞安数据库升级。");
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 SafetyServiceModule 实例。
     *
     * @return SafetyServiceModule，不可用时返回 null
     */
    private SafetyServiceModule getSafetyModule() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getSafetyServiceModule();
    }
}
