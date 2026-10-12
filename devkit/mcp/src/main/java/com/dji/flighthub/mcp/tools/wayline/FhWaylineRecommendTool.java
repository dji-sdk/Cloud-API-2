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

package com.dji.flighthub.mcp.tools.wayline;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.DeviceTopo;
import com.dji.flighthubsdk.bean.GetWaylineDetailResult;
import com.dji.flighthubsdk.bean.GetWaylineListParam;
import com.dji.flighthubsdk.bean.GetWaylineListResult;
import com.dji.flighthubsdk.bean.WaylineDetailKmzData;
import com.dji.flighthubsdk.bean.WaylineListItemResult;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * fh_wayline_recommend — 航线推荐工具。
 * 给定 dock/airport SN，综合匹配机型兼容性、航线类型、dock 到航线起点距离，
 * 返回排序后的可飞航线列表 + 被排除航线及原因。
 *
 * <p>安全等级：L0（只读查询，无需 confirm）
 *
 * <p>数据流：
 * <ol>
 *   <li>取设备树找 dock 的 drone 子设备 modelKey + 订阅 dock 遥测拿坐标（超时降级）</li>
 *   <li>list 航线（不用 droneTypes 后端过滤，要在 excluded 露反例）</li>
 *   <li>机型硬过滤（客户端严格相等）</li>
 *   <li>类型硬过滤（可选）</li>
 *   <li>距离计算 + 硬过滤（幸存者受 max_results 上限保护）</li>
 *   <li>按距离排序，取前 max_results 条为 recommended</li>
 * </ol>
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhWaylineRecommendTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhWaylineRecommendTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_wayline_recommend";

    // ==================== 参数名常量 ====================
    private static final String PARAM_DOCK_SN = "dock_sn";
    private static final String PARAM_MAX_DISTANCE_KM = "max_distance_km";
    private static final String PARAM_WAYLINE_TYPES = "wayline_types";
    private static final String PARAM_MAX_RESULTS = "max_results";

    // ==================== 默认值常量 ====================
    /** 默认距离阈值（公里），dock 到航线起点直线距离 */
    private static final double DEFAULT_MAX_DISTANCE_KM = 10.0;
    /** 默认返回推荐结果上限，同时是 detail 调用数硬上限 */
    private static final int DEFAULT_MAX_RESULTS = 10;

    // ==================== 位置来源常量 ====================
    /** 位置来源：遥测成功获取 */
    private static final String LOCATION_SOURCE_TELEMETRY = "telemetry";
    /** 位置来源：遥测不可用（降级） */
    private static final String LOCATION_SOURCE_UNAVAILABLE = "unavailable";

    // ==================== Mock 数据常量 ====================
    /** Mock dock 纬度（深圳） */
    private static final double MOCK_DOCK_LAT = 22.5435;
    /** Mock dock 经度（深圳） */
    private static final double MOCK_DOCK_LNG = 113.9470;
    /** Mock dock drone 机型 key */
    private static final String MOCK_DRONE_MODEL_KEY = "0-67-0";
    /** Mock 航线机型不兼容用机型 key */
    private static final String MOCK_INCOMPATIBLE_DRONE_MODEL_KEY = "0-89-0";
    /** Mock 航线 wl-A 起点纬度（深圳，近 dock） */
    private static final double MOCK_WL_A_LAT = 22.560;
    /** Mock 航线 wl-A 起点经度 */
    private static final double MOCK_WL_A_LNG = 113.970;
    /** Mock 航线 wl-B 起点纬度（惠州，远超距离阈值） */
    private static final double MOCK_WL_B_LAT = 22.900;
    /** Mock 航线 wl-B 起点经度 */
    private static final double MOCK_WL_B_LNG = 114.400;
    /** Mock 航线 wl-C 起点纬度（深圳，近 dock） */
    private static final double MOCK_WL_C_LAT = 22.570;
    /** Mock 航线 wl-C 起点经度 */
    private static final double MOCK_WL_C_LNG = 113.980;

    // ==================== 计算与格式化常量 ====================
    /** 米转公里除数 */
    private static final double METERS_PER_KILOMETER = 1000.0;
    /** 距离保留一位小数的舍入因子 */
    private static final double DISTANCE_ROUND_FACTOR = 10.0;

    // ==================== 真实 SDK 调用常量 ====================
    /** list 航线每页大小（首页覆盖绝大多数场景） */
    private static final int LIST_PAGE_SIZE = 100;
    /** list 航线页码（从 1 开始） */
    private static final int LIST_PAGE_NUMBER = 1;
    /** dock 遥测订阅超时（秒） */
    private static final int DOCK_TELEMETRY_TIMEOUT_SECONDS = 10;
    /** 设备树中 drone 子设备的 deviceClass */
    private static final String DEVICE_CLASS_DRONE = "drone";

    // ==================== next_steps 引导常量 ====================
    /** next_steps 指向的飞行任务工具名 */
    private static final String HINT_TOOL_FLIGHT_TASK = "fh_flight_task";
    /** next_steps 指向的航线推荐工具名（无推荐时引导放宽过滤） */
    private static final String HINT_TOOL_WAYLINE_RECOMMEND = "fh_wayline_recommend";
    /** next_steps 引导的飞行任务类型（1=立即） */
    private static final int HINT_TASK_TYPE_IMMEDIATE = 1;
    /** next_steps 引导的返航高度（米） */
    private static final double HINT_RTH_ALTITUDE = 120.0;
    /** 无推荐时放宽距离阈值的倍率 */
    private static final double HINT_DISTANCE_RELAX_FACTOR = 2.0;
    /** 无推荐时放宽距离阈值的兜底值（公里） */
    private static final double HINT_DISTANCE_RELAX_FALLBACK_KM = 20.0;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhWaylineRecommendTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Recommend waylines flyable by a specific dock. Matches drone model "
                        + "(strict model-key equality), optional wayline type, and dock-to-wayline-startpoint "
                        + "distance (haversine, default 10km). Returns ranked waylines (nearest first) plus an "
                        + "excluded list with reasons. "
                        + "WHEN TO USE: user gives a dock/airport SN and asks which waylines it can fly, "
                        + "or wants a wayline suitable for a specific device. "
                        + "DO NOT USE WHEN: user just wants to browse all workspace waylines without a "
                        + "specific dock → use fh_workspace(action=list); or already has a wayline_uuid "
                        + "→ use fh_flight_task(action=create).");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DOCK_SN, Map.of(
                "type", "string",
                "description", "Target dock/airport serial number (SN) to recommend waylines for"
        ));
        props.put(PARAM_MAX_DISTANCE_KM, Map.of(
                "type", "number",
                "description", "Max distance (km) between dock and wayline start point; "
                        + "waylines farther than this are excluded (default: 10)",
                "default", DEFAULT_MAX_DISTANCE_KM
        ));
        props.put(PARAM_WAYLINE_TYPES, Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "Optional wayline type filter; a wayline is excluded if its waylineTypes "
                        + "have no intersection with this list"
        ));
        props.put(PARAM_MAX_RESULTS, Map.of(
                "type", "integer",
                "description", "Max number of recommended waylines to return; also caps detail calls "
                        + "(default: 10)",
                "default", DEFAULT_MAX_RESULTS
        ));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_DOCK_SN));
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

        // 2. 解析必填参数
        String dockSn = getStringArg(args, PARAM_DOCK_SN, null);
        if (dockSn == null) {
            return errorResult("Missing required parameter: dock_sn",
                    "Provide the dock/airport serial number to recommend waylines for.");
        }

        // 3. 解析可选参数
        double maxDistanceKm = getDoubleArg(args, PARAM_MAX_DISTANCE_KM, DEFAULT_MAX_DISTANCE_KM);
        if (!(maxDistanceKm > 0) || Double.isInfinite(maxDistanceKm)) {
            // !(x>0) 同时拒绝 NaN（所有 NaN 比较为 false）和非正数；isInfinite 拒绝 Infinity
            return errorResult("Invalid max_distance_km: " + maxDistanceKm,
                    "Must be a finite number greater than 0.");
        }
        int maxResults = getIntArg(args, PARAM_MAX_RESULTS, DEFAULT_MAX_RESULTS);
        if (maxResults < 1) {
            return errorResult("Invalid max_results: " + maxResults,
                    "Must be greater than or equal to 1.");
        }
        List<Integer> waylineTypes = getIntegerListArg(args, PARAM_WAYLINE_TYPES);
        if (waylineTypes != null && waylineTypes.isEmpty()) {
            // 显式传空列表视为校验错误，避免静默不过滤或静默排除全部
            return errorResult("Invalid wayline_types: empty list",
                    "Provide at least one wayline type, or omit the parameter to skip type filtering.");
        }

        // 4. Mock 模式（isMockMode=true 走 mock；否则走 real 路径，由 real 路径处理 workspace 未初始化等错误）
        if (sdkService.isMockMode()) {
            return mockRecommend(dockSn, maxDistanceKm, waylineTypes, maxResults);
        }

        // 5. 真实 SDK 调用
        return executeWithRealSdk(dockSn, maxDistanceKm, waylineTypes, maxResults);
    }

    /**
     * 从参数 Map 中获取 Integer 列表参数（wayline_types 用）。
     * 委托 {@link #toIntegerList}，区分缺失参数（null）与空列表（空，交由调用方校验）。
     *
     * @param args 参数 Map
     * @param key  参数名
     * @return 参数缺失/非 List 时返回 null；List 为空或元素全无效时返回空列表
     */
    private static List<Integer> getIntegerListArg(Map<String, Object> args, String key) {
        if (args == null) {
            return null;
        }
        return toIntegerList(args.get(key));
    }

    // ==================== 真实 SDK 调用 ====================

    /**
     * 真实 SDK 调用：取设备树找 drone 机型 + 订阅 dock 遥测拿坐标 + list/detail 算距离。
     *
     * <p>流程：
     * <ol>
     *   <li>{@code getDeviceList} → 找 sn==dockSn 的设备 → 取 children 里 deviceClass=="drone" 的 modelKey</li>
     *   <li>{@code listenDockLocation} (CountDownLatch + 超时, finally 取消) → lat/lng；超时降级 NaN</li>
     *   <li>{@code getWaylineList} (page=1, pageSize=100)</li>
     *   <li>客户端机型/类型过滤（不传 droneTypes 后端过滤，以便在 excluded 露反例）</li>
     *   <li>对幸存者（受 max_results 上限保护）逐条 {@code getWaylineDetail} → kmzData 起点坐标；
     *       单条 detail 失败 → excluded，不影响其他条目</li>
     *   <li>调 {@link #filterSortAndAssemble} 算距离 + 排序 + 组装</li>
     * </ol>
     *
     * @param dockSn        dock SN
     * @param maxDistanceKm 距离阈值
     * @param waylineTypes  类型过滤
     * @param maxResults    返回上限（同时是 detail 调用数硬上限）
     * @return 推荐结果
     */
    private Map<String, Object> executeWithRealSdk(String dockSn, double maxDistanceKm,
                                                   List<Integer> waylineTypes, int maxResults) {
        // 调用方 execute() 已保证 isMockMode=false；workspace 可能为 null（未 initialize）
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return errorResult("SDK not initialized", "Call fh_connect first.");
        }

        // ① 设备树解析：找 dock 的 drone 子设备 modelKey
        String droneModelKey;
        try {
            droneModelKey = resolveDroneModelKey(workspace, dockSn);
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Failed to fetch device list (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Device list request timed out.");
        }
        if (droneModelKey == null) {
            return errorResult("No drone sub-device for dock " + dockSn,
                    "Target must be an airport or dock housing a drone. Use fh_device_list to verify.");
        }

        // ② dock 遥测拿坐标（超时降级，不报错）
        double[] dockCoords = fetchDockLocation(dockSn);
        double dockLat = dockCoords[0];
        double dockLng = dockCoords[1];
        boolean hasDockLocation = !Double.isNaN(dockLat) && !Double.isNaN(dockLng);
        String locationSource = hasDockLocation
                ? LOCATION_SOURCE_TELEMETRY : LOCATION_SOURCE_UNAVAILABLE;
        if (!hasDockLocation) {
            log.warn("Dock {} telemetry unavailable; distance filtering skipped.", dockSn);
        }

        // ③ list 航线
        List<WaylineListItemResult> waylineItems;
        try {
            waylineItems = fetchWaylineList(workspace);
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("Failed to fetch wayline list (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Wayline list request timed out.");
        }

        // ④ 客户端机型/类型过滤（保留被排除者进 excluded，需在调 detail 前过滤以控制 N+1）
        List<WaylineListItemResult> survivors = new ArrayList<>();
        List<Map<String, Object>> excluded = new ArrayList<>();
        for (WaylineListItemResult item : waylineItems) {
            String excludeReason = preDetailExcludeReason(item, droneModelKey, waylineTypes);
            if (excludeReason != null) {
                excluded.add(excludedItem(item.getWaylineUuid(), item.getWaylineName(), excludeReason));
            } else {
                survivors.add(item);
            }
        }

        // ⑤ 对幸存者逐条 getWaylineDetail 拿起点坐标（受 max_results 硬上限保护）
        List<Map<String, Object>> waylinesWithCoords = new ArrayList<>();
        boolean truncated = false;
        int detailBudget = maxResults;
        for (WaylineListItemResult item : survivors) {
            if (detailBudget <= 0) {
                // 超出 detail 预算的幸存者不调 detail，标记 truncated
                truncated = true;
                break;
            }
            Map<String, Object> waylineWithCoords = fetchDetailAndEnrich(workspace, item);
            if (waylineWithCoords == null) {
                // detail 失败 → excluded，不消耗 detail 预算的"成功名额"但仍计入调用次数
                excluded.add(excludedItem(item.getWaylineUuid(), item.getWaylineName(),
                        "detail fetch failed: see logs"));
            } else {
                waylinesWithCoords.add(waylineWithCoords);
            }
            detailBudget--;
        }

        // ⑥ 距离过滤 + 排序 + 组装（复用 mock 同款逻辑）
        return filterSortAndAssemble(dockSn, dockLat, dockLng, locationSource, droneModelKey,
                maxDistanceKm, waylineTypes, maxResults, waylinesWithCoords, false, excluded, truncated);
    }

    /**
     * 从设备树解析 dock 挂载的 drone 子设备 modelKey。
     *
     * @param workspace 当前 workspace
     * @param dockSn    dock/airport SN
     * @return drone modelKey；dock 不在设备树或无 drone 子设备时返回 null
     */
    private String resolveDroneModelKey(Workspace workspace, String dockSn) {
        CompletableFuture<List<DeviceTopo>> future = new CompletableFuture<>();
        workspace.getDeviceManageModule().getDeviceList((devices, error) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getHttpCode(), "getDeviceList failed: " + formatSdkError(error)));
                return;
            }
            future.complete(devices);
        });
        List<DeviceTopo> topos = SdkCallbackBridge.await(future);
        if (topos == null) {
            return null;
        }
        for (DeviceTopo topo : topos) {
            if (topo.getDevice() != null
                    && dockSn.equals(topo.getDevice().getDeviceSn())) {
                List<com.dji.flighthubsdk.bean.Device> subs = topo.getSubDevices();
                if (subs == null) {
                    return null;
                }
                for (com.dji.flighthubsdk.bean.Device sub : subs) {
                    if (DEVICE_CLASS_DRONE.equals(sub.getDeviceClass())) {
                        return sub.getModelKey();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 订阅 dock 位置遥测，CountDownLatch 同步等待一个数据点后取消订阅。
     * 超时返回 [NaN, NaN]，不抛异常（降级）。
     *
     * @param dockSn dock SN
     * @return [lat, lng]；遥测不可用时为 [NaN, NaN]
     */
    private double[] fetchDockLocation(String dockSn) {
        Dock dock = sdkService.getDock(dockSn);
        if (dock == null) {
            return new double[] {Double.NaN, Double.NaN};
        }
        double[] coords = {Double.NaN, Double.NaN};
        CountDownLatch latch = new CountDownLatch(1);
        int listenId = dock.getDockOsdModule().listenDockLocation(location -> {
            coords[0] = location.getLatitude();
            coords[1] = location.getLongitude();
            latch.countDown();
        });
        try {
            latch.await(DOCK_TELEMETRY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Dock {} location listen interrupted.", dockSn);
        } finally {
            if (listenId >= 0) {
                dock.getDockOsdModule().cancelListenDockLocation(listenId);
            }
        }
        return coords;
    }

    /**
     * 拉取航线列表首页（pageSize=100）。
     *
     * @param workspace 当前 workspace
     * @return 航线列表项；空列表表示无航线
     */
    private List<WaylineListItemResult> fetchWaylineList(Workspace workspace) {
        GetWaylineListParam param = new GetWaylineListParam();
        param.setPage(LIST_PAGE_NUMBER);
        param.setPageSize(LIST_PAGE_SIZE);

        CompletableFuture<GetWaylineListResult> future = new CompletableFuture<>();
        workspace.getWaylineManageModule().getWaylineList(param, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getHttpCode(), "getWaylineList failed: " + formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        GetWaylineListResult result = SdkCallbackBridge.await(future);
        return result != null && result.getItems() != null ? result.getItems() : Collections.emptyList();
    }

    /**
     * 机型/类型预过滤：返回排除原因，null 表示通过。
     * 在调 detail 前过滤，避免对不兼容航线浪费 detail 调用。
     *
     * @param item          航线列表项
     * @param droneModelKey dock drone 机型 key
     * @param waylineTypes  类型过滤（可 null）
     * @return 排除原因；null 表示通过预过滤
     */
    private String preDetailExcludeReason(WaylineListItemResult item, String droneModelKey,
                                          List<Integer> waylineTypes) {
        String droneType = item.getDroneType();
        if (droneModelKey != null && !droneModelKey.equals(droneType)) {
            return String.format("drone type '%s' does not match dock drone '%s'",
                    droneType, droneModelKey);
        }
        if (waylineTypes != null && !waylineTypes.isEmpty()) {
            List<Integer> wTypes = toIntegerList(item.getWaylineTypes());
            if (wTypes == null || Collections.disjoint(waylineTypes, wTypes)) {
                return "wayline types " + wTypes + " have no intersection with filter " + waylineTypes;
            }
        }
        return null;
    }

    /**
     * 对单条航线调 getWaylineDetail，提取起点坐标并组装为含坐标的 Map。
     * 单条 detail 失败返回 null（调用方进 excluded）。
     *
     * @param workspace 当前 workspace
     * @param item      航线列表项
     * @return 含 start_latitude/start_longitude 的航线 Map；detail 失败返回 null
     */
    private Map<String, Object> fetchDetailAndEnrich(Workspace workspace, WaylineListItemResult item) {
        CompletableFuture<GetWaylineDetailResult> future = new CompletableFuture<>();
        workspace.getWaylineManageModule().getWaylineDetail(item.getWaylineUuid(), (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkCallbackBridge.SdkException(
                        error.getHttpCode(), "getWaylineDetail failed: " + formatSdkError(error)));
                return;
            }
            future.complete(result);
        });
        GetWaylineDetailResult detail;
        try {
            detail = SdkCallbackBridge.await(future);
        } catch (SdkCallbackBridge.SdkException e) {
            log.warn("getWaylineDetail failed for {}: {}", item.getWaylineUuid(), e.getMessage());
            return null;
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.warn("getWaylineDetail timeout for {}", item.getWaylineUuid());
            return null;
        }
        if (detail == null || detail.getKmzData() == null) {
            return null;
        }
        WaylineDetailKmzData kmz = detail.getKmzData();
        Map<String, Object> w = new LinkedHashMap<>();
        w.put("wayline_uuid", item.getWaylineUuid());
        w.put("wayline_name", item.getWaylineName());
        w.put("drone_type", item.getDroneType());
        w.put("wayline_types", item.getWaylineTypes());
        w.put("start_latitude", kmz.getStartLatitude());
        w.put("start_longitude", kmz.getStartLongitude());
        return w;
    }

    // ==================== Mock 模式 ====================

    /**
     * Mock 模式推荐：返回 3 条航线覆盖机型不兼容、距离超限、正常匹配三个分支。
     *
     * @param dockSn        dock SN
     * @param maxDistanceKm 距离阈值
     * @param waylineTypes  类型过滤（可 null）
     * @param maxResults    返回上限
     * @return 推荐结果
     */
    private Map<String, Object> mockRecommend(String dockSn, double maxDistanceKm,
                                              List<Integer> waylineTypes, int maxResults) {
        // 3 条 mock 航线（uuid, name, droneType, waylineTypes, startLat, startLng）
        List<Map<String, Object>> waylines = new java.util.ArrayList<>();
        waylines.add(mockWayline("wl-A", "Shenzhen Survey A", MOCK_DRONE_MODEL_KEY,
                List.of(0), MOCK_WL_A_LAT, MOCK_WL_A_LNG));
        waylines.add(mockWayline("wl-B", "Huizhou Survey B", MOCK_DRONE_MODEL_KEY,
                List.of(0), MOCK_WL_B_LAT, MOCK_WL_B_LNG));
        waylines.add(mockWayline("wl-C", "Shenzhen Inspect C", MOCK_INCOMPATIBLE_DRONE_MODEL_KEY,
                List.of(1), MOCK_WL_C_LAT, MOCK_WL_C_LNG));

        return filterSortAndAssemble(dockSn, MOCK_DOCK_LAT, MOCK_DOCK_LNG, LOCATION_SOURCE_TELEMETRY,
                MOCK_DRONE_MODEL_KEY, maxDistanceKm, waylineTypes, maxResults, waylines, true);
    }

    /**
     * 构造单条 mock 航线 Map（含起点坐标，模拟 detail 解析后的形态）。
     *
     * @param uuid         航线 UUID
     * @param name         航线名称
     * @param droneType    机型 key
     * @param waylineTypes 航线类型列表
     * @param startLat     起点纬度
     * @param startLng     起点经度
     * @return 航线 Map
     */
    private static Map<String, Object> mockWayline(String uuid, String name, String droneType,
                                                   List<Integer> waylineTypes,
                                                   double startLat, double startLng) {
        Map<String, Object> w = new LinkedHashMap<>();
        w.put("wayline_uuid", uuid);
        w.put("wayline_name", name);
        w.put("drone_type", droneType);
        w.put("wayline_types", waylineTypes);
        w.put("start_latitude", startLat);
        w.put("start_longitude", startLng);
        return w;
    }

    // ==================== 通用过滤/排序/组装（Mock 与 Real 共用） ====================

    /**
     * 对航线列表执行机型过滤 → 类型过滤 → 距离过滤 → 排序 → 组装。
     * 各阶段被排除的航线收集到 excluded 列表并附原因。
     *
     * <p>距离需调 detail 才知道，无法在调 detail 前预排序。因此本方法处理的 waylines
     * 列表应已含 start_latitude/start_longitude（mock 直填，real 路径在调用前已取 detail）。
     *
     * <p>real 路径已在调用前完成机型/类型预过滤（见 {@link #preDetailExcludeReason}），
     * 本方法对 preDetail 的幸存者重做一次机型/类型过滤是幂等的兜底（防御 detail 返回的 drone_type
     * 与 list 项不一致的边界情况），不会产生重复 excluded。
     *
     * @param dockSn        dock SN
     * @param dockLat       dock 纬度（遥测降级时 NaN）
     * @param dockLng       dock 经度（遥测降级时 NaN）
     * @param locationSource 位置来源标记（LOCATION_SOURCE_TELEMETRY / LOCATION_SOURCE_UNAVAILABLE）
     * @param droneModelKey dock drone 机型 key
     * @param maxDistanceKm 距离阈值
     * @param waylineTypes  类型过滤（可 null）
     * @param maxResults    返回上限
     * @param waylines      已含起点坐标的航线列表
     * @param isMock        是否 mock 数据
     * @return 推荐结果响应 Map
     */
    private Map<String, Object> filterSortAndAssemble(String dockSn, double dockLat, double dockLng,
                                                      String locationSource, String droneModelKey,
                                                      double maxDistanceKm, List<Integer> waylineTypes,
                                                      int maxResults,
                                                      List<Map<String, Object>> waylines, boolean isMock) {
        return filterSortAndAssemble(dockSn, dockLat, dockLng, locationSource, droneModelKey,
                maxDistanceKm, waylineTypes, maxResults, waylines, isMock,
                new ArrayList<>(), false);
    }

    /**
     * filterSortAndAssemble 的完整重载，支持外部传入已收集的 excluded 列表与 truncated 标记。
     * real 路径在调本方法前已对部分航线（机型/类型不兼容、detail 失败）生成 excluded，
     * 这些条目需并入最终响应而非丢弃。
     *
     * @param preExcluded   调用前已收集的 excluded 条目（机型/类型不兼容、detail 失败）
     * @param preTruncated  调用前是否已因 detail 预算截断
     */
    private Map<String, Object> filterSortAndAssemble(String dockSn, double dockLat, double dockLng,
                                                      String locationSource, String droneModelKey,
                                                      double maxDistanceKm, List<Integer> waylineTypes,
                                                      int maxResults,
                                                      List<Map<String, Object>> waylines, boolean isMock,
                                                      List<Map<String, Object>> preExcluded,
                                                      boolean preTruncated) {
        boolean hasDockLocation = !Double.isNaN(dockLat) && !Double.isNaN(dockLng);

        List<Map<String, Object>> recommended = new ArrayList<>();
        List<Map<String, Object>> excluded = new ArrayList<>(preExcluded);

        for (Map<String, Object> w : waylines) {
            String uuid = (String) w.get("wayline_uuid");
            String name = (String) w.get("wayline_name");
            String droneType = (String) w.get("drone_type");

            // ① 机型硬过滤（严格相等）— 幂等兜底
            if (droneModelKey != null && !droneModelKey.equals(droneType)) {
                excluded.add(excludedItem(uuid, name,
                        String.format("drone type '%s' does not match dock drone '%s'",
                                droneType, droneModelKey)));
                continue;
            }

            // ② 类型硬过滤（可选）— 幂等兜底
            if (waylineTypes != null && !waylineTypes.isEmpty()) {
                List<Integer> wTypes = toIntegerList(w.get("wayline_types"));
                if (wTypes == null || Collections.disjoint(waylineTypes, wTypes)) {
                    excluded.add(excludedItem(uuid, name,
                            "wayline types " + wTypes + " have no intersection with filter " + waylineTypes));
                    continue;
                }
            }

            // ③ 距离硬过滤（dock 位置可用时）
            if (hasDockLocation) {
                double startLat = toDouble(w.get("start_latitude"), Double.NaN);
                double startLng = toDouble(w.get("start_longitude"), Double.NaN);
                if (!Double.isNaN(startLat) && !Double.isNaN(startLng)) {
                    double distanceKm = haversineDistance(dockLat, dockLng, startLat, startLng) / METERS_PER_KILOMETER;
                    if (distanceKm > maxDistanceKm) {
                        excluded.add(excludedItem(uuid, name,
                                String.format("distance %.1fkm exceeds max_distance_km %.1f",
                                        distanceKm, maxDistanceKm)));
                        continue;
                    }
                    // 进入候选，附距离
                    recommended.add(recommendedItem(w, distanceKm, maxDistanceKm));
                } else {
                    excluded.add(excludedItem(uuid, name, "wayline start coordinates missing"));
                }
            } else {
                // dock 位置不可用，降级：机型/类型匹配的航线全部进候选（无距离、无排序）
                recommended.add(recommendedItem(w, Double.NaN, maxDistanceKm));
            }
        }

        // ④ 排序：有距离的按距离升序；无距离（降级模式）保持原序
        if (hasDockLocation) {
            recommended.sort((a, b) -> {
                double da = toDouble(a.get("distance_km"), Double.MAX_VALUE);
                double db = toDouble(b.get("distance_km"), Double.MAX_VALUE);
                return Double.compare(da, db);
            });
        }

        // ⑤ 截断 max_results
        boolean truncated = preTruncated;
        if (recommended.size() > maxResults) {
            truncated = true;
            recommended = new ArrayList<>(recommended.subList(0, maxResults));
        }

        return buildResponse(dockSn, dockLat, dockLng, hasDockLocation, locationSource, droneModelKey,
                maxDistanceKm, recommended, excluded, truncated, isMock);
    }

    /**
     * 构造 recommended 列表条目。
     *
     * @param w             航线原始 Map
     * @param distanceKm    距离（公里），降级时 NaN
     * @param maxDistanceKm 距离阈值
     * @return recommended 条目
     */
    private static Map<String, Object> recommendedItem(Map<String, Object> w, double distanceKm,
                                                       double maxDistanceKm) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("wayline_uuid", w.get("wayline_uuid"));
        item.put("wayline_name", w.get("wayline_name"));
        item.put("drone_type", w.get("drone_type"));
        item.put("wayline_types", w.get("wayline_types"));
        if (!Double.isNaN(distanceKm)) {
            item.put("distance_km", Math.round(distanceKm * DISTANCE_ROUND_FACTOR) / DISTANCE_ROUND_FACTOR);
            item.put("start_latitude", w.get("start_latitude"));
            item.put("start_longitude", w.get("start_longitude"));
            item.put("match_reason", String.format(
                    "drone model matches, %.1fkm from dock (within %.1fkm)", distanceKm, maxDistanceKm));
        } else {
            item.put("match_reason", "drone model matches (dock location unavailable, distance skipped)");
        }
        return item;
    }

    /**
     * 构造 excluded 列表条目。
     *
     * @param uuid   航线 UUID
     * @param name   航线名称
     * @param reason 排除原因
     * @return excluded 条目
     */
    private static Map<String, Object> excludedItem(String uuid, String name, String reason) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("wayline_uuid", uuid);
        item.put("wayline_name", name);
        item.put("exclude_reason", reason);
        return item;
    }

    /**
     * 组装最终响应。
     *
     * @param dockSn        dock SN
     * @param dockLat       dock 纬度
     * @param dockLng       dock 经度
     * @param hasDockLocation dock 位置是否可用（同时校验 lat 与 lng，避免半可用坐标写入 NaN）
     * @param locationSource 位置来源标记
     * @param droneModelKey dock drone 机型 key
     * @param maxDistanceKm 距离阈值
     * @param recommended   推荐列表
     * @param excluded      排除列表
     * @param truncated     是否因 max_results 截断
     * @param isMock        是否 mock 数据
     * @return 响应 Map
     */
    private static Map<String, Object> buildResponse(String dockSn, double dockLat, double dockLng,
                                                     boolean hasDockLocation, String locationSource,
                                                     String droneModelKey,
                                                     double maxDistanceKm,
                                                     List<Map<String, Object>> recommended,
                                                     List<Map<String, Object>> excluded,
                                                     boolean truncated, boolean isMock) {
        Map<String, Object> dockLocation = new LinkedHashMap<>();
        dockLocation.put("source", locationSource);
        if (hasDockLocation) {
            dockLocation.put("latitude", dockLat);
            dockLocation.put("longitude", dockLng);
        } else {
            // 遥测不可用降级：附 _warning 提示消费者距离过滤被跳过
            dockLocation.put("_warning", "Dock telemetry unavailable; distance filtering skipped.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dock_sn", dockSn);
        data.put("dock_location", dockLocation);
        data.put("drone_model_key", droneModelKey);
        data.put("max_distance_km", maxDistanceKm);
        data.put("recommended", recommended);
        data.put("excluded", excluded);
        data.put("truncated", truncated);
        if (isMock) {
            data.put("_mock", true);
        }

        // 推荐为空时不发 next_steps（避免引导 AI 用占位符 UUID 建任务），改给"放宽过滤"建议
        List<Map<String, Object>> nextSteps;
        if (recommended.isEmpty()) {
            double relaxedKm = maxDistanceKm * HINT_DISTANCE_RELAX_FACTOR;
            if (!(relaxedKm > 0) || Double.isInfinite(relaxedKm)) {
                relaxedKm = HINT_DISTANCE_RELAX_FALLBACK_KM;
            }
            nextSteps = List.of(
                    Map.of("action", "No wayline recommended; relax filters and retry",
                            "tool", HINT_TOOL_WAYLINE_RECOMMEND,
                            "params_hint", Map.of(
                                    "dock_sn", dockSn,
                                    "max_distance_km", relaxedKm))
            );
        } else {
            Object firstUuid = recommended.get(0).get("wayline_uuid");
            nextSteps = List.of(
                    Map.of("action", "Create a flight task using the top recommended wayline",
                            "tool", HINT_TOOL_FLIGHT_TASK,
                            "params_hint", Map.of(
                                    "action", "create",
                                    "wayline_uuid", firstUuid,
                                    "sn", dockSn,
                                    "name", recommended.get(0).getOrDefault("wayline_name", "Flight Task"),
                                    "task_type", HINT_TASK_TYPE_IMMEDIATE,
                                    "rth_altitude", HINT_RTH_ALTITUDE))
            );
        }

        return successResultWithNextSteps(data,
                String.format("Recommended %d wayline(s) for dock %s (%d excluded).",
                        recommended.size(), dockSn, excluded.size()),
                nextSteps);
    }

    /**
     * 把 Object 安全转为 List<Integer>（wayline_types 字段或入参）。
     * 容忍 Number 和可解析为整数的 String 元素，过滤 null 与无法解析项。
     *
     * @param val 原始值
     * @return 非 List 时返回 null；List 为空或元素全无效时返回空列表（不折叠为 null，以便调用方区分"缺失"与"空"）
     */
    private static List<Integer> toIntegerList(Object val) {
        if (!(val instanceof List<?> list)) {
            return null;
        }
        List<Integer> result = new java.util.ArrayList<>();
        for (Object item : list) {
            if (item instanceof Number n) {
                result.add(n.intValue());
            } else if (item instanceof String s && !s.isBlank()) {
                try {
                    result.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException ignored) {
                    // 跳过无效元素
                }
            }
        }
        return result;
    }
}
