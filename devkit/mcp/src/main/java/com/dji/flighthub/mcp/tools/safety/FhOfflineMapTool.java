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
import com.dji.flighthubsdk.bean.DeviceSignalItem;
import com.dji.flighthubsdk.bean.DeviceSyncResults;
import com.dji.flighthubsdk.bean.DeviceSyncStatusResults;
import com.dji.flighthubsdk.bean.GetOfflineMapsModelInfoResults;
import com.dji.flighthubsdk.bean.GetOfflineMapsSyncStatusResults;
import com.dji.flighthubsdk.bean.GridSignalItem;
import com.dji.flighthubsdk.bean.ModelItemInfoData;
import com.dji.flighthubsdk.bean.OfflineMapsModelData;
import com.dji.flighthubsdk.bean.OfflineMapsModelInfoData;
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

    /** update_model_info 的 request_params 子字段 */
    private static final String PARAM_MODEL_3D_ITEMS = "model_3d_items";
    private static final String PARAM_MODEL_2D_ITEMS = "model_2d_items";

    /** 模型条目字段（同时兼容 snake_case 与 camelCase） */
    private static final String ITEM_UUID = "uuid";
    private static final String ITEM_OBJECT_KEY = "object_key";
    private static final String ITEM_OBJECT_KEY_CAMEL = "objectKey";
    private static final String ITEM_FILE_TYPE = "file_type";
    private static final String ITEM_FILE_TYPE_CAMEL = "fileType";

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
    /**
     * 声明工具级静态安全等级：REVERSIBLE。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
    }

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
        Map<String, Object> modelItemSchema = new LinkedHashMap<>();
        modelItemSchema.put("type", "object");
        modelItemSchema.put("properties", Map.of(
                ITEM_UUID, Map.of("type", "string", "description", "Model item UUID"),
                ITEM_OBJECT_KEY, Map.of("type", "string", "description", "Object storage key of the model file"),
                ITEM_FILE_TYPE, Map.of("type", "string", "description", "Model file type: tif | b3dm | pnts")
        ));
        modelItemSchema.put("required", List.of(ITEM_OBJECT_KEY, ITEM_FILE_TYPE));

        props.put(PARAM_REQUEST_PARAMS, Map.of(
                "type", "object",
                "description",
                "Parameters for update_model_info. Required for update_model_info; must contain a "
                        + "non-empty 'model_3d_items' and/or 'model_2d_items' array, otherwise the "
                        + "request is rejected before reaching the server (225101).",
                "properties", Map.of(
                        PARAM_MODEL_3D_ITEMS, Map.of(
                                "type", "array",
                                "items", modelItemSchema,
                                "description", "3D model items to generate. An omitted or empty array "
                                        + "means 'no 3D items' - the SDK always transmits both arrays, "
                                        + "so omitting one is identical to sending []."
                        ),
                        PARAM_MODEL_2D_ITEMS, Map.of(
                                "type", "array",
                                "items", modelItemSchema,
                                "description", "2D model items to generate. An omitted or empty array "
                                        + "means 'no 2D items' - the SDK always transmits both arrays, "
                                        + "so omitting one is identical to sending []."
                        )
                )
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
            data.put("offline_map", buildOfflineMapModel(result.getOfflineMap()));
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

        // SDK 侧空请求会返回 225101 bad request，必须在进入 SDK 前拦截
        UpdateModelInfoRequest request = parseUpdateModelInfoParams(args);
        if (request.error() != null) {
            return errorResult(request.error(), request.suggestion());
        }

        List<ModelItemInfoData> model3dItems = request.model3dItems();
        List<ModelItemInfoData> model2dItems = request.model2dItems();

        return sdkService.isMockMode()
                ? updateModelInfoInMockMode(model3dItems, model2dItems)
                : updateModelInfoWithRealSdk(model3dItems, model2dItems);
    }

    /**
     * 校验通过的 update_model_info 请求。
     *
     * @param model3dItems 3D 模型条目
     * @param model2dItems 2D 模型条目
     * @param error        错误信息，校验通过时为 null
     * @param suggestion   修复建议，校验通过时为 null
     */
    private record UpdateModelInfoRequest(
            List<ModelItemInfoData> model3dItems,
            List<ModelItemInfoData> model2dItems,
            String error,
            String suggestion) {
    }

    /**
     * 解析并校验 request_params。
     *
     * @param args 工具入参
     * @return 解析结果；校验失败时 {@code error()} 非 null
     */
    private UpdateModelInfoRequest parseUpdateModelInfoParams(Map<String, Object> args) {
        Object rawParams = args == null ? null : args.get(PARAM_REQUEST_PARAMS);
        if (!(rawParams instanceof Map<?, ?> requestParams)) {
            return invalidParams("Missing required parameter: request_params",
                    "Provide request_params with a non-empty 'model_3d_items' or 'model_2d_items' array.");
        }

        List<ModelItemInfoData> model3dItems = parseModelItems(
                requestParams.get(PARAM_MODEL_3D_ITEMS), PARAM_MODEL_3D_ITEMS);
        if (model3dItems == null) {
            return invalidParams("Invalid request_params.model_3d_items",
                    "'model_3d_items' must be an array of {uuid, object_key, file_type} objects.");
        }
        List<ModelItemInfoData> model2dItems = parseModelItems(
                requestParams.get(PARAM_MODEL_2D_ITEMS), PARAM_MODEL_2D_ITEMS);
        if (model2dItems == null) {
            return invalidParams("Invalid request_params.model_2d_items",
                    "'model_2d_items' must be an array of {uuid, object_key, file_type} objects.");
        }
        if (model3dItems.isEmpty() && model2dItems.isEmpty()) {
            return invalidParams("Empty request_params",
                    "At least one of 'model_3d_items' / 'model_2d_items' must be non-empty. "
                            + "The SDK transmits both arrays unconditionally (an omitted array is sent "
                            + "as []), so an empty request would be rejected by the server (225101).");
        }
        return new UpdateModelInfoRequest(model3dItems, model2dItems, null, null);
    }

    /**
     * 构造校验失败结果。
     */
    private static UpdateModelInfoRequest invalidParams(String error, String suggestion) {
        return new UpdateModelInfoRequest(List.of(), List.of(), error, suggestion);
    }

    /**
     * 把 MCP 层数组解析为 SDK 的 {@link ModelItemInfoData} 列表。
     *
     * @param raw     原始值，允许为 null（表示未提供该字段）
     * @param fieldName 字段名，仅用于日志
     * @return 解析结果；字段存在但结构非法时返回 null
     */
    private List<ModelItemInfoData> parseModelItems(Object raw, String fieldName) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            log.warn("[{}] {} 不是数组: {}", TOOL_NAME, fieldName, raw.getClass().getSimpleName());
            return null;
        }
        List<ModelItemInfoData> items = new ArrayList<>(list.size());
        for (Object element : list) {
            if (!(element instanceof Map<?, ?> map)) {
                log.warn("[{}] {} 的元素不是对象: {}", TOOL_NAME, fieldName, element);
                return null;
            }
            String objectKey = firstNonBlank(map.get(ITEM_OBJECT_KEY), map.get(ITEM_OBJECT_KEY_CAMEL));
            String fileType = firstNonBlank(map.get(ITEM_FILE_TYPE), map.get(ITEM_FILE_TYPE_CAMEL));
            if (objectKey == null || fileType == null) {
                log.warn("[{}] {} 的元素缺少 object_key / file_type: {}", TOOL_NAME, fieldName, map);
                return null;
            }
            ModelItemInfoData item = new ModelItemInfoData();
            item.setUuid(firstNonBlank(map.get(ITEM_UUID), null));
            item.setObjectKey(objectKey);
            item.setFileType(fileType);
            items.add(item);
        }
        return items;
    }

    /**
     * 取第一个非空白字符串值。
     *
     * @param candidates 候选值
     * @return 首个非空白字符串；没有则返回 null
     */
    private static String firstNonBlank(Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private Map<String, Object> updateModelInfoInMockMode(
            List<ModelItemInfoData> model3dItems, List<ModelItemInfoData> model2dItems) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "update_started");
        data.put("model_3d_count", model3dItems.size());
        data.put("model_2d_count", model2dItems.size());
        data.put("_mock", true);
        return successResult(data, "Offline map model update started.");
    }

    @Generated
    private Map<String, Object> updateModelInfoWithRealSdk(
            List<ModelItemInfoData> model3dItems, List<ModelItemInfoData> model2dItems) {
        try {
            SafetyServiceModule ssm = getSafetyModule();
            if (ssm == null) {
                return errorResult("SafetyServiceModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            UpdateOfflineMapsParams params = new UpdateOfflineMapsParams();
            params.setModel3dItems(model3dItems);
            params.setModel2dItems(model2dItems);

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
            data.put("model_3d_count", model3dItems.size());
            data.put("model_2d_count", model2dItems.size());
            return successResult(data, "Offline map model update started. "
                    + "Poll get_model_info until status is 'finish' before syncing to devices.");

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
        // sync 会将离线地图模型推送到多台设备，触发批量数据传输，按 L1（REVERSIBLE）门控
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

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
            data.put("results", buildDeviceSyncResults(result.getResults()));
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
            data.put("devices", buildDeviceSyncStatuses(result.getList()));
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
            data.put("signal_maps", buildDeviceSignalItems(result.getLists()));
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

    // ==================== SDK bean → 结构化 Map（避免 toString 透传） ====================

    /**
     * 将离线地图模型信息映射为结构化 Map，避免 {@code toString()} 透传导致输出不可解析。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param model SDK 返回的模型信息对象，可为 null
     * @return 结构化模型信息；入参为 null 时返回 null
     */
    Map<String, Object> buildOfflineMapModel(OfflineMapsModelData model) {
        if (model == null) {
            return null;
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", model.getId());
        row.put("updated_time", model.getUpdatedTime());
        row.put("percent", model.getPercent());
        row.put("status", model.getStatus());
        row.put("result", model.getResult());
        row.put("models", buildModelInfos(model.getModels()));
        return row;
    }

    /**
     * 映射模型 UUID 列表。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param models SDK 模型条目列表，可为 null
     * @return 结构化模型条目列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildModelInfos(List<OfflineMapsModelInfoData> models) {
        if (models == null) {
            return List.of();
        }
        List<Map<String, Object>> mapped = new ArrayList<>(models.size());
        for (OfflineMapsModelInfoData model : models) {
            if (model == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uuid", model.getUuid());
            mapped.add(row);
        }
        return mapped;
    }

    /**
     * 映射设备同步结果列表。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param results SDK 同步结果列表，可为 null
     * @return 结构化同步结果列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildDeviceSyncResults(List<DeviceSyncResults> results) {
        if (results == null) {
            return List.of();
        }
        List<Map<String, Object>> mapped = new ArrayList<>(results.size());
        for (DeviceSyncResults result : results) {
            if (result == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("device_sn", result.getDeviceSn());
            row.put("code", result.getCode());
            mapped.add(row);
        }
        return mapped;
    }

    /**
     * 映射设备同步状态列表。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param statuses SDK 同步状态列表，可为 null
     * @return 结构化同步状态列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildDeviceSyncStatuses(List<DeviceSyncStatusResults> statuses) {
        if (statuses == null) {
            return List.of();
        }
        List<Map<String, Object>> mapped = new ArrayList<>(statuses.size());
        for (DeviceSyncStatusResults status : statuses) {
            if (status == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("device_sn", status.getSn());
            row.put("status", status.getStatus());
            row.put("reason", status.getReason());
            mapped.add(row);
        }
        return mapped;
    }

    /**
     * 映射设备信号地图列表。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param items SDK 设备信号条目列表，可为 null
     * @return 结构化信号地图列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildDeviceSignalItems(List<DeviceSignalItem> items) {
        if (items == null) {
            return List.of();
        }
        List<Map<String, Object>> mapped = new ArrayList<>(items.size());
        for (DeviceSignalItem item : items) {
            if (item == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("device_sn", item.getDeviceSn());
            row.put("grids", buildGridSignals(item.getGrids()));
            mapped.add(row);
        }
        return mapped;
    }

    /**
     * 映射 H3 网格信号强度列表。
     *
     * <p>包可见以便单元测试直接验证字段映射。
     *
     * @param grids SDK 网格信号列表，可为 null
     * @return 结构化网格信号列表，入参为 null 时返回空列表
     */
    List<Map<String, Object>> buildGridSignals(List<GridSignalItem> grids) {
        if (grids == null) {
            return List.of();
        }
        List<Map<String, Object>> mapped = new ArrayList<>(grids.size());
        for (GridSignalItem grid : grids) {
            if (grid == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("h3_index", grid.getH3Index());
            row.put("sdr_avg", grid.getSdrAvg());
            row.put("sdr_max", grid.getSdrMax());
            row.put("sdr_min", grid.getSdrMin());
            row.put("signal_4g_avg", grid.getSignal4gAvg());
            row.put("signal_4g_max", grid.getSignal4gMax());
            row.put("signal_4g_min", grid.getSignal4gMin());
            mapped.add(row);
        }
        return mapped;
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
