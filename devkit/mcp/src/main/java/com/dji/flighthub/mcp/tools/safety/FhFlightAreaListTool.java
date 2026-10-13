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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.ContentsGeometryData;
import com.dji.flighthubsdk.bean.ContentsPropertyData;
import com.dji.flighthubsdk.bean.FlightAreaInfoItem;
import com.dji.flighthubsdk.bean.FlightAreaPagination;
import com.dji.flighthubsdk.bean.FlightAreasContentData;
import com.dji.flighthubsdk.bean.GetCustomizedFlightAreasParams;
import com.dji.flighthubsdk.bean.GetFlightAreasResults;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_flight_area_list — 查询自定义飞行区列表。
 * 支持按名称、类型、状态、ID 过滤，分页返回。
 *
 * <p>安全等级：L0（只读查询）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlightAreaListTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlightAreaListTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flight_area_list";

    /** 参数名常量 */
    private static final String PARAM_NAME = "name";
    private static final String PARAM_TYPE = "type";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_IDS = "ids";
    private static final String PARAM_PAGE_INDEX = "page_index";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 分页默认值 */
    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 200;

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
    public FhFlightAreaListTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
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
                "List custom flight areas (no-fly zones, geofences, no-land zones). "
                + "Supports filtering by name, type, status, and IDs.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_NAME, Map.of(
                "type", "string",
                "description", "Filter by area name (fuzzy match)"
        ));
        props.put(PARAM_TYPE, Map.of(
                "type", "string",
                "enum", List.of("nfz", "dfence", "noland", "all"),
                "description", "Filter by type: nfz=no-fly zone, dfence=geofence, noland=no-land zone, all=no filter"
        ));
        props.put(PARAM_STATUS, Map.of(
                "type", "string",
                "description", "Filter by status (e.g. active, inactive)"
        ));
        props.put(PARAM_IDS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Filter by specific flight area IDs"
        ));
        props.put(PARAM_PAGE_INDEX, Map.of(
                "type", "integer",
                "description", "Page number (default 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Items per page (default 50, max 200)"
        ));
        schema.put("properties", props);
        schema.put("required", List.of());
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行飞行区列表查询。
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

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 解析参数
        ListParams params = parseParams(args);

        // 4. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? listInMockMode(params)
                : listWithRealSdk(params);
    }

    /**
     * 解析查询参数。
     */
    private ListParams parseParams(Map<String, Object> args) {
        String name = getStringArg(args, PARAM_NAME, null);
        String type = getStringArg(args, PARAM_TYPE, null);
        String status = getStringArg(args, PARAM_STATUS, null);

        // type=all 等同于不过滤
        if ("all".equals(type)) {
            type = null;
        }
        // 校验 type 有效性
        if (type != null && !VALID_TYPES.contains(type)) {
            type = null;
        }

        List<String> ids = parseStringList(args, PARAM_IDS);

        int pageIndex = getIntArg(args, PARAM_PAGE_INDEX, DEFAULT_PAGE_INDEX);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
        pageIndex = Math.max(1, pageIndex);

        return new ListParams(name, type, status, ids, pageIndex, pageSize);
    }

    /**
     * Mock 模式下返回飞行区列表。
     */
    private Map<String, Object> listInMockMode(ListParams params) {
        log.info("[MOCK] Flight area list query: name={}, type={}, pageIndex={}, pageSize={}",
                params.name, params.type, params.pageIndex, params.pageSize);

        List<Map<String, Object>> areas = buildMockAreas(params);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("flight_areas", areas);
        data.put("total_count", areas.size());
        data.put("page_index", params.pageIndex);
        data.put("page_size", params.pageSize);
        data.put("_mock", true);

        return successResult(data, "Found " + areas.size() + " flight areas.");
    }

    /**
     * 构建 Mock 飞行区数据。
     */
    private List<Map<String, Object>> buildMockAreas(ListParams params) {
        List<Map<String, Object>> areas = new ArrayList<>();
        Instant now = Instant.now();

        Map<String, Object> area1 = new LinkedHashMap<>();
        area1.put("id", "area_001");
        area1.put("name", "办公楼禁飞区");
        area1.put("type", "nfz");
        area1.put("type_display", AREA_TYPE_DISPLAY.get("nfz"));
        area1.put("status", "active");
        area1.put("content", Map.of(
                "type", "Feature",
                "geometry", Map.of(
                        "type", "Circle",
                        "center", List.of(113.943, 22.577),
                        "radius", 200.0),
                "properties", Map.of("color", "#FF0000", "clamp_to_ground", true)));
        area1.put("created_by", "admin");
        area1.put("created_at", now.minusSeconds(86400).toString());
        areas.add(area1);

        Map<String, Object> area2 = new LinkedHashMap<>();
        area2.put("id", "area_002");
        area2.put("name", "巡检作业区");
        area2.put("type", "dfence");
        area2.put("type_display", AREA_TYPE_DISPLAY.get("dfence"));
        area2.put("status", "active");
        area2.put("content", Map.of(
                "type", "Feature",
                "geometry", Map.of(
                        "type", "Polygon",
                        "coordinates", List.of(List.of(
                                List.of(113.940, 22.575),
                                List.of(113.950, 22.575),
                                List.of(113.950, 22.580),
                                List.of(113.940, 22.580),
                                List.of(113.940, 22.575)))),
                "properties", Map.of("color", "#00FF00", "clamp_to_ground", true)));
        area2.put("created_by", "operator");
        area2.put("created_at", now.minusSeconds(43200).toString());
        areas.add(area2);

        Map<String, Object> area3 = new LinkedHashMap<>();
        area3.put("id", "area_003");
        area3.put("name", "停机坪禁降区");
        area3.put("type", "noland");
        area3.put("type_display", AREA_TYPE_DISPLAY.get("noland"));
        area3.put("status", "active");
        area3.put("content", Map.of(
                "type", "Feature",
                "geometry", Map.of(
                        "type", "Circle",
                        "center", List.of(113.945, 22.578),
                        "radius", 50.0),
                "properties", Map.of("color", "#FFAA00", "clamp_to_ground", true)));
        area3.put("created_by", "admin");
        area3.put("created_at", now.minusSeconds(3600).toString());
        areas.add(area3);

        // 按 type 过滤
        if (params.type != null) {
            areas.removeIf(a -> !params.type.equals(a.get("type")));
        }

        return areas;
    }

    /**
     * 真实 SDK 模式下查询飞行区列表。
     */
    @Generated
    private Map<String, Object> listWithRealSdk(ListParams params) {
        try {
            SafetyServiceModule ssm = sdkService.getWorkspace().getSafetyServiceModule();

            GetCustomizedFlightAreasParams req = new GetCustomizedFlightAreasParams();
            if (params.name != null) {
                req.setName(params.name);
            }
            if (params.type != null) {
                req.setType(params.type);
            }
            if (params.status != null) {
                req.setStatus(params.status);
            }
            if (params.ids != null && !params.ids.isEmpty()) {
                req.setIds(params.ids);
            }
            req.setPageIndex(params.pageIndex);
            req.setPageSize(params.pageSize);

            CompletableFuture<GetFlightAreasResults> future = new CompletableFuture<>();
            ssm.getCustomizedFlightAreas(req, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getCustomizedFlightAreas failed: " + error.getErrorCode()));
                    return;
                }
                future.complete(results);
            });

            GetFlightAreasResults results = SdkCallbackBridge.await(future);
            List<Map<String, Object>> areas = mapFlightAreaResults(results);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("flight_areas", areas);
            data.put("total_count", extractTotalCount(results, areas.size()));
            data.put("page_index", params.pageIndex);
            data.put("page_size", params.pageSize);

            return successResult(data, "Found " + areas.size() + " flight areas.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_flight_area_list timeout: {}", e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Flight area list query timed out. Try narrowing filters or reducing page_size.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_flight_area_list SDK error: {}", e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check workspace connectivity and try again.");
        } catch (Exception e) {
            log.error("fh_flight_area_list error: {}", e.getMessage(), e);
            return errorResult("Failed to list flight areas: " + e.getMessage(),
                    "Check workspace connectivity and try again.");
        }
    }

    /**
     * 将 SDK 返回的飞行区列表映射为 MCP 响应格式。
     */
    private List<Map<String, Object>> mapFlightAreaResults(GetFlightAreasResults results) {
        List<Map<String, Object>> areas = new ArrayList<>();
        if (results.getList() == null) {
            return areas;
        }
        for (FlightAreaInfoItem item : results.getList()) {
            Map<String, Object> area = new LinkedHashMap<>();
            area.put("id", item.getId());
            area.put("name", item.getName());
            area.put("type", item.getType());
            area.put("type_display", AREA_TYPE_DISPLAY.getOrDefault(
                    item.getType() != null ? item.getType() : "", item.getType()));
            area.put("status", item.getStatus());
            area.put("content", mapContentData(item.getContent()));
            area.put("created_by", item.getCreatedBy());
            area.put("created_time", item.getCreatedTime());
            area.put("updated_by", item.getUpdatedBy());
            area.put("updated_time", item.getUpdatedTime());
            areas.add(area);
        }
        return areas;
    }

    /**
     * 映射飞行区内容数据（GeoJSON 几何 + 属性）。
     */
    private Map<String, Object> mapContentData(FlightAreasContentData content) {
        if (content == null) {
            return null;
        }
        Map<String, Object> contentMap = new LinkedHashMap<>();
        contentMap.put("type", content.getType());

        ContentsGeometryData geo = content.getGeometry();
        if (geo != null) {
            Map<String, Object> geometry = new LinkedHashMap<>();
            geometry.put("type", geo.getType());
            if ("Circle".equals(geo.getType())) {
                geometry.put("center", geo.getCenter());
                geometry.put("radius", geo.getRadius());
            } else {
                geometry.put("coordinates", geo.getCoordinates());
            }
            contentMap.put("geometry", geometry);
        }

        ContentsPropertyData props = content.getProperties();
        if (props != null) {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("color", props.getColor());
            if (props.getWidth() > 0) {
                properties.put("width", props.getWidth());
            }
            properties.put("clamp_to_ground", props.isClampToGround());
            contentMap.put("properties", properties);
        }

        return contentMap;
    }

    /**
     * 从分页信息中提取总数，若不可用则回退到列表大小。
     */
    private int extractTotalCount(GetFlightAreasResults results, int fallback) {
        FlightAreaPagination pagination = results.getPagination();
        if (pagination != null) {
            return pagination.getTotal();
        }
        return fallback;
    }

    /**
     * 查询参数封装记录。
     */
    private record ListParams(String name, String type, String status,
                              List<String> ids, int pageIndex, int pageSize) {
    }
}
