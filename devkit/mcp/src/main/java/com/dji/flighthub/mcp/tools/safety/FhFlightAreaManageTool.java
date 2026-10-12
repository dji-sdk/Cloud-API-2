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
import com.dji.flighthubsdk.bean.ContentsGeometryData;
import com.dji.flighthubsdk.bean.ContentsPropertyData;
import com.dji.flighthubsdk.bean.CreateCustomizedFlightAreasParams;
import com.dji.flighthubsdk.bean.CreateFlightAreasResults;
import com.dji.flighthubsdk.bean.FlightAreasContentData;
import com.dji.flighthubsdk.bean.UpdateCustomizedFlightAreasParams;
import com.dji.flighthubsdk.bean.UpdateFlightAreasResults;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_flight_area_manage — 管理自定义飞行区（创建/更新/删除）。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L1（写操作需确认；delete 不可逆）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlightAreaManageTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlightAreaManageTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flight_area_manage";

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_FLIGHT_AREA_ID = "flight_area_id";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_CONTENT = "content";
    private static final String PARAM_CREATED_BY = "created_by";
    private static final String PARAM_CONFIRM = "confirm";

    /** 操作常量 */
    private static final String ACTION_CREATE = "create";
    private static final String ACTION_UPDATE = "update";
    private static final String ACTION_DELETE = "delete";

    /** 有效操作列表 */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_CREATE, ACTION_UPDATE, ACTION_DELETE);

    /** 有效飞行区类型（引用共享常量） */
    private static final List<String> VALID_TYPES = FlightAreaConstants.VALID_TYPES;

    /** 飞行区类型显示名（引用共享常量） */
    private static final Map<String, String> AREA_TYPE_DISPLAY = FlightAreaConstants.AREA_TYPE_DISPLAY;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhFlightAreaManageTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
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
                "Create, update, or delete custom flight areas. "
                + "Types: nfz (no-fly zone), dfence (geofence/work area), noland (no-land zone).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "create: new flight area | update: modify existing | delete: remove permanently"
        ));
        props.put(PARAM_FLIGHT_AREA_ID, Map.of(
                "type", "string",
                "description", "Flight area ID (required for update/delete, from fh_flight_area_list)"
        ));
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Area name (required for create, optional for update)"
        ));
        props.put(PARAM_TYPE, Map.of(
                "type", "string",
                "enum", VALID_TYPES,
                "description", "Area type (required for create): nfz/dfence/noland"
        ));
        props.put(PARAM_CONTENT, Map.of(
                "type", "object",
                "description", "GeoJSON geometry content (required for create, optional for update). "
                    + "Structure: {type: 'Feature', geometry: {...}, properties: {color, clamp_to_ground, width}}. "
                    + "Circle geometry: {type: 'Circle', center: [lng, lat], radius: meters}. "
                    + "Polygon geometry: {type: 'Polygon', coordinates: [[[lng,lat],...,[first_point]]]} "
                    + "(closed ring, ≥3 points, <255 points). "
                    + "Polyline geometry (noland only): {type: 'Polyline', coordinates: [[[lng,lat],...]]}, "
                    + "must set properties.width (10-100)."
        ));
        props.put(PARAM_CREATED_BY, Map.of(
                "type", "string",
                "description", "Creator user ID (optional, for create)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Human confirmation required for all actions"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_CONFIRM));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行飞行区管理操作。
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

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L1 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action: " + action,
                    "action must be one of: " + VALID_ACTIONS);
        }

        // 4. 按 action 校验参数
        Map<String, Object> validationError = validateActionParams(action, args);
        if (validationError != null) {
            return validationError;
        }

        // 5. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would execute flight area '" + action + "' operation",
                    buildDryRunPreview(TOOL_NAME, action, "flight_area",
                            Map.of("action", action),
                            List.of("session_active", "security_level_allowed", "workspace_ready", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 6. 人工确认
        Map<String, Object> confirmError = requireHumanConfirm(args,
                "flight_area_" + action, action,
                buildConfirmMessage(action, args));
        if (confirmError != null) {
            return confirmError;
        }

        // 7. 分发执行
        return sdkService.isMockMode()
                ? dispatchMockMode(action, args)
                : dispatchRealSdk(action, args);
    }

    /**
     * 按 action 类型校验必填参数。
     */
    private Map<String, Object> validateActionParams(String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_CREATE -> validateCreateParams(args);
            case ACTION_UPDATE -> validateUpdateParams(args);
            case ACTION_DELETE -> validateDeleteParams(args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * 校验 create 操作参数。
     */
    private Map<String, Object> validateCreateParams(Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, null);
        if (name == null) {
            return errorResult("Missing required parameter: name",
                    "Provide a name for the new flight area.");
        }
        String type = getStringArg(args, PARAM_TYPE, null);
        if (type == null || !VALID_TYPES.contains(type)) {
            return errorResult("Missing or invalid type: " + type,
                    "type must be one of: " + VALID_TYPES);
        }
        Object content = args.get(PARAM_CONTENT);
        if (content == null) {
            return errorResult("Missing required parameter: content",
                    "Provide GeoJSON geometry content for the flight area.");
        }
        return null;
    }

    /**
     * 校验 update 操作参数。
     */
    private Map<String, Object> validateUpdateParams(Map<String, Object> args) {
        String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, null);
        if (id == null) {
            return errorResult("Missing required parameter: flight_area_id",
                    "Provide the flight area ID to update. Use fh_flight_area_list to find IDs.");
        }
        // update 至少要有 name 或 content 之一
        String name = getStringArg(args, PARAM_NAME, null);
        Object content = args.get(PARAM_CONTENT);
        if (name == null && content == null) {
            return errorResult("Nothing to update",
                    "Provide at least one of: name, content.");
        }
        return null;
    }

    /**
     * 校验 delete 操作参数。
     */
    private Map<String, Object> validateDeleteParams(Map<String, Object> args) {
        String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, null);
        if (id == null) {
            return errorResult("Missing required parameter: flight_area_id",
                    "Provide the flight area ID to delete. Use fh_flight_area_list to find IDs.");
        }
        return null;
    }

    /**
     * Mock 模式分发。
     */
    private Map<String, Object> dispatchMockMode(String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_CREATE -> createInMockMode(args);
            case ACTION_UPDATE -> updateInMockMode(args);
            case ACTION_DELETE -> deleteInMockMode(args);
            default -> errorResult("Unknown action: " + action, "");
        };
    }

    /**
     * Mock 模式 — 创建飞行区。
     */
    private Map<String, Object> createInMockMode(Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, "");
        String type = getStringArg(args, PARAM_TYPE, "");
        String createdBy = getStringArg(args, PARAM_CREATED_BY, "system");
        log.info("[MOCK] Flight area created: name={}, type={}", name, type);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_CREATE);
        data.put("flight_area_id", "area_new_" + System.currentTimeMillis() % 10000);
        data.put("name", name);
        data.put("type", type);
        data.put("type_display", AREA_TYPE_DISPLAY.getOrDefault(type, type));
        data.put("status", "active");
        data.put("created_by", createdBy);
        data.put("_mock", true);

        return successResult(data,
                "Flight area '" + name + "' (" + AREA_TYPE_DISPLAY.getOrDefault(type, type) + ") created.");
    }

    /**
     * Mock 模式 — 更新飞行区。
     */
    private Map<String, Object> updateInMockMode(Map<String, Object> args) {
        String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, "");
        String name = getStringArg(args, PARAM_NAME, null);
        log.info("[MOCK] Flight area updated: id={}", id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_UPDATE);
        data.put("flight_area_id", id);
        if (name != null) {
            data.put("name", name);
        }
        data.put("updated", true);
        data.put("_mock", true);

        return successResult(data, "Flight area " + id + " updated.");
    }

    /**
     * Mock 模式 — 删除飞行区。
     */
    private Map<String, Object> deleteInMockMode(Map<String, Object> args) {
        String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, "");
        log.info("[MOCK] Flight area deleted: id={}", id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_DELETE);
        data.put("flight_area_id", id);
        data.put("deleted", true);
        data.put("_mock", true);

        return successResult(data, "Flight area " + id + " deleted permanently.");
    }

    /**
     * 真实 SDK 模式分发。
     */
    private Map<String, Object> dispatchRealSdk(String action, Map<String, Object> args) {
        try {
            return switch (action) {
                case ACTION_CREATE -> createWithRealSdk(args);
                case ACTION_UPDATE -> updateWithRealSdk(args);
                case ACTION_DELETE -> deleteWithRealSdk(args);
                default -> errorResult("Unknown action: " + action, "");
            };
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_flight_area_manage timeout for action {}: {}", action, e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Flight area " + action + " timed out. Try again later.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_flight_area_manage SDK error for action {}: {}", action, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check workspace connectivity and try again.");
        } catch (Exception e) {
            log.error("fh_flight_area_manage error for action {}: {}", action, e.getMessage(), e);
            return errorResult("Failed to " + action + " flight area: " + e.getMessage(),
                    "Check workspace connectivity and try again.");
        }
    }

    /**
     * 真实 SDK — 创建飞行区。
     */
    @Generated
    private Map<String, Object> createWithRealSdk(Map<String, Object> args) {
        SafetyServiceModule ssm = sdkService.getWorkspace().getSafetyServiceModule();

        String name = getStringArg(args, PARAM_NAME, "");
        String type = getStringArg(args, PARAM_TYPE, "");
        String createdBy = getStringArg(args, PARAM_CREATED_BY, "system");
        Object contentObj = args.get(PARAM_CONTENT);

        CreateCustomizedFlightAreasParams req = new CreateCustomizedFlightAreasParams();
        req.setName(name);
        req.setType(type);
        req.setCreatedBy(createdBy);
        req.setContent(convertToFlightAreasContentData(contentObj));

        CompletableFuture<CreateFlightAreasResults> future = new CompletableFuture<>();
        ssm.createCustomizedFlightAreas(req, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "createCustomizedFlightAreas failed: " + error.getErrorCode()));
                return;
            }
            future.complete(results);
        });

        CreateFlightAreasResults results = SdkCallbackBridge.await(future);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_CREATE);
        data.put("flight_area_id", results.getId());
        data.put("name", results.getName());
        data.put("type", type);
        data.put("type_display", AREA_TYPE_DISPLAY.getOrDefault(type, type));
        data.put("status", "active");
        data.put("created_by", createdBy);

        return successResult(data,
                "Flight area '" + results.getName() + "' (" + AREA_TYPE_DISPLAY.getOrDefault(type, type) + ") created.");
    }

    /**
     * 真实 SDK — 更新飞行区。
     */
    @Generated
    private Map<String, Object> updateWithRealSdk(Map<String, Object> args) {
        SafetyServiceModule ssm = sdkService.getWorkspace().getSafetyServiceModule();

        String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, "");
        String name = getStringArg(args, PARAM_NAME, null);
        Object contentObj = args.get(PARAM_CONTENT);

        UpdateCustomizedFlightAreasParams req = new UpdateCustomizedFlightAreasParams();
        req.setFlightAreaId(id);
        if (name != null) {
            req.setName(name);
        }
        if (contentObj != null) {
            req.setContent(convertToFlightAreasContentData(contentObj));
        }

        CompletableFuture<UpdateFlightAreasResults> future = new CompletableFuture<>();
        ssm.updateCustomizedFlightAreas(req, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(
                        new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                "updateCustomizedFlightAreas failed: " + error.getErrorCode()));
                return;
            }
            future.complete(results);
        });

        SdkCallbackBridge.await(future);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_UPDATE);
        data.put("flight_area_id", id);
        if (name != null) {
            data.put("name", name);
        }
        data.put("updated", true);

        return successResult(data, "Flight area " + id + " updated.");
    }

    /**
     * 真实 SDK — 删除飞行区。
     */
    @Generated
    private Map<String, Object> deleteWithRealSdk(Map<String, Object> args) {
        SafetyServiceModule ssm = sdkService.getWorkspace().getSafetyServiceModule();

        String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, "");

        SdkCallbackBridge.awaitVoid(cb -> ssm.deleteCustomizedFlightAreas(id, cb));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_DELETE);
        data.put("flight_area_id", id);
        data.put("deleted", true);

        return successResult(data, "Flight area " + id + " deleted permanently.");
    }

    /**
     * 将输入的 content 对象转换为 SDK 的 FlightAreasContentData。
     * 手动解析 Map 结构，兼容 snake_case 和 camelCase 字段名。
     *
     * @throws IllegalArgumentException content 格式无效时抛出，上层 catch 会转为 MCP 错误响应
     */
    @SuppressWarnings("unchecked")
    private FlightAreasContentData convertToFlightAreasContentData(Object contentObj) {
        if (contentObj == null) {
            return null;
        }
        if (!(contentObj instanceof Map)) {
            throw new IllegalArgumentException(
                    "content must be a JSON object, got: " + contentObj.getClass().getSimpleName());
        }
        Map<String, Object> map = (Map<String, Object>) contentObj;

        FlightAreasContentData content = new FlightAreasContentData();
        Object typeObj = map.get("type");
        content.setType(typeObj instanceof String ? (String) typeObj : "Feature");

        Object geoObj = map.get("geometry");
        if (geoObj instanceof Map) {
            content.setGeometry(parseGeometry((Map<String, Object>) geoObj));
        }

        Object propsObj = map.get("properties");
        if (propsObj instanceof Map) {
            content.setProperties(parseProperties((Map<String, Object>) propsObj));
        }

        return content;
    }

    /**
     * 解析 geometry 数据为 SDK ContentsGeometryData。
     */
    @SuppressWarnings("unchecked")
    private ContentsGeometryData parseGeometry(Map<String, Object> geoMap) {
        ContentsGeometryData geo = new ContentsGeometryData();
        Object typeObj = geoMap.get("type");
        String geoType = typeObj instanceof String ? (String) typeObj : "";
        geo.setType(geoType);

        if ("Circle".equals(geoType)) {
            Object centerObj = geoMap.get("center");
            if (centerObj instanceof List) {
                geo.setCenter(toDoubleList((List<?>) centerObj));
            }
            Object radiusObj = geoMap.get("radius");
            if (radiusObj instanceof Number) {
                geo.setRadius(((Number) radiusObj).doubleValue());
            }
        } else {
            Object coordsObj = geoMap.get("coordinates");
            if (coordsObj instanceof List) {
                geo.setCoordinates(toCoordinates3D((List<?>) coordsObj));
            }
        }
        return geo;
    }

    /**
     * 解析 properties 数据为 SDK ContentsPropertyData。
     * 兼容 clampToGround（Java 命名）和 clamp_to_ground（MCP 命名）两种格式。
     */
    private ContentsPropertyData parseProperties(Map<String, Object> propsMap) {
        ContentsPropertyData props = new ContentsPropertyData();

        Object colorObj = propsMap.get("color");
        if (colorObj instanceof String) {
            props.setColor((String) colorObj);
        }

        Object widthObj = propsMap.get("width");
        if (widthObj instanceof Number) {
            props.setWidth(((Number) widthObj).intValue());
        }

        Object clampObj = propsMap.get("clampToGround");
        if (clampObj == null) {
            clampObj = propsMap.get("clamp_to_ground");
        }
        if (clampObj instanceof Boolean) {
            props.setClampToGround((Boolean) clampObj);
        }

        return props;
    }

    /**
     * 将原始列表转换为 Double 列表（坐标点）。
     */
    private List<Double> toDoubleList(List<?> raw) {
        List<Double> result = new ArrayList<>(raw.size());
        for (Object item : raw) {
            if (item instanceof Number) {
                result.add(((Number) item).doubleValue());
            }
        }
        return result;
    }

    /**
     * 将原始三维列表转换为坐标数组（Polygon/Polyline）。
     */
    @SuppressWarnings("unchecked")
    private List<List<List<Double>>> toCoordinates3D(List<?> raw) {
        List<List<List<Double>>> result = new ArrayList<>();
        for (Object ring : raw) {
            if (ring instanceof List) {
                List<List<Double>> ringResult = new ArrayList<>();
                for (Object point : (List<?>) ring) {
                    if (point instanceof List) {
                        ringResult.add(toDoubleList((List<?>) point));
                    }
                }
                result.add(ringResult);
            }
        }
        return result;
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(String action, Map<String, Object> args) {
        return switch (action) {
            case ACTION_CREATE -> {
                String name = getStringArg(args, PARAM_NAME, "?");
                String type = getStringArg(args, PARAM_TYPE, "?");
                yield String.format("即将创建%s类型飞行区「%s」，确认？",
                        AREA_TYPE_DISPLAY.getOrDefault(type, type), name);
            }
            case ACTION_UPDATE -> {
                String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, "?");
                yield String.format("即将更新飞行区 %s，确认？", id);
            }
            case ACTION_DELETE -> {
                String id = getStringArg(args, PARAM_FLIGHT_AREA_ID, "?");
                yield String.format("即将删除飞行区 %s，删除后不可恢复且已同步设备将失去该限制，确认？", id);
            }
            default -> "确认执行操作？";
        };
    }
}
