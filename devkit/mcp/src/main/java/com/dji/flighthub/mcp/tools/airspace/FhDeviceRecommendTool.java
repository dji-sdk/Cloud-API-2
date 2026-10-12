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
import com.dji.flighthubsdk.bean.DeviceRecommendationReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * fh_device_recommend — 智能选机工具。
 * 根据候选设备列表、选机策略和目标位置，推荐最优执飞设备。
 *
 * <p>对应 SDK 接口：FlightTaskModule.getDeviceRecommendation(req, callback)
 *
 * <p>安全等级：L0（只读查询，无需 confirm）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhDeviceRecommendTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhDeviceRecommendTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_device_recommend";

    /** 参数名常量 */
    private static final String PARAM_CANDIDATE_SNS = "candidate_sns";
    private static final String PARAM_STRATEGY_ID = "strategy_id";
    private static final String PARAM_FLY_TO_LATITUDE = "fly_to_latitude";
    private static final String PARAM_FLY_TO_LONGITUDE = "fly_to_longitude";
    private static final String PARAM_FLY_TO_HEIGHT = "fly_to_height";

    /** 默认策略 ID */
    private static final int DEFAULT_STRATEGY_ID = 0;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhDeviceRecommendTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Intelligent device recommendation. Given a list of candidate device SNs, "
                + "a selection strategy, and an optional target fly-to position, "
                + "returns the recommended device(s) best suited for the mission. "
                + "Use this before dispatching a flight task to pick the optimal drone/dock.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_CANDIDATE_SNS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "List of candidate device serial numbers to evaluate"
        ));
        props.put(PARAM_STRATEGY_ID, Map.of(
                "type", "integer",
                "description", "Selection strategy ID (0=default/nearest, platform-defined)",
                "default", DEFAULT_STRATEGY_ID
        ));
        props.put(PARAM_FLY_TO_LATITUDE, Map.of(
                "type", "number",
                "description", "Target position latitude (WGS84, -90 to 90). Required if specifying target."
        ));
        props.put(PARAM_FLY_TO_LONGITUDE, Map.of(
                "type", "number",
                "description", "Target position longitude (WGS84, -180 to 180). Required if specifying target."
        ));
        props.put(PARAM_FLY_TO_HEIGHT, Map.of(
                "type", "number",
                "description", "Target position height in meters (relative altitude). Required if specifying target."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_CANDIDATE_SNS));
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

        // 2. 解析参数
        List<String> candidateSns = getStringListArg(args, PARAM_CANDIDATE_SNS);
        if (candidateSns == null || candidateSns.isEmpty()) {
            return errorResult("Missing required parameter: candidate_sns",
                    "Provide a non-empty list of device serial numbers.");
        }

        int strategyId = getIntArg(args, PARAM_STRATEGY_ID, DEFAULT_STRATEGY_ID);

        // 3. 解析目标位置（可选）
        double flyToLat = getDoubleArg(args, PARAM_FLY_TO_LATITUDE, Double.NaN);
        double flyToLng = getDoubleArg(args, PARAM_FLY_TO_LONGITUDE, Double.NaN);
        double flyToHeight = getDoubleArg(args, PARAM_FLY_TO_HEIGHT, Double.NaN);
        boolean hasFlyToPosition = !Double.isNaN(flyToLat) && !Double.isNaN(flyToLng) && !Double.isNaN(flyToHeight);

        // 4. 若提供了目标位置，校验坐标合法性
        if (hasFlyToPosition) {
            Map<String, Object> coordError = validateGpsCoordinates(flyToLat, flyToLng, flyToHeight);
            if (coordError != null) {
                return coordError;
            }
        }

        // 5. Mock 模式
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockRecommendation(candidateSns, strategyId, hasFlyToPosition);
        }

        // 6. 调用 SDK
        return executeWithRealSdk(candidateSns, strategyId, flyToLat, flyToLng, flyToHeight, hasFlyToPosition);
    }

    // ==================== 私有业务方法 ====================

    /**
     * 真实 SDK 调用。
     */
    @Generated
    private Map<String, Object> executeWithRealSdk(List<String> candidateSns, int strategyId,
                                                    double flyToLat, double flyToLng, double flyToHeight,
                                                    boolean hasFlyToPosition) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            FlightTaskModule ftm = workspace.getFlightTaskModule();

            DeviceRecommendationReq req = new DeviceRecommendationReq();
            req.setSn(candidateSns);
            req.setStrategyId(strategyId);
            if (hasFlyToPosition) {
                req.setFlyToLatitude(flyToLat);
                req.setFlyToLongitude(flyToLng);
                req.setFlyToHeight(flyToHeight);
                req.setHasFlyToPosition(true);
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ftm.getDeviceRecommendation(req, (error, resp) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getDeviceRecommendation failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("devices", resp.getDevices());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Device recommendation completed.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "Device recommendation request timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_device_recommend error: {}", e.getMessage(), e);
            return errorResult("Internal error", e.getMessage());
        }
    }

    /**
     * Mock 模式返回模拟推荐结果。
     */
    private Map<String, Object> mockRecommendation(List<String> candidateSns, int strategyId,
                                                    boolean hasFlyToPosition) {
        log.info("[MOCK] getDeviceRecommendation: candidates={}, strategy={}, hasFlyTo={}",
                candidateSns, strategyId, hasFlyToPosition);

        String recommendedSn = candidateSns.get(0);

        List<Map<String, Object>> scores = new ArrayList<>();
        for (int i = 0; i < candidateSns.size(); i++) {
            Map<String, Object> score = new LinkedHashMap<>();
            score.put("sn", candidateSns.get(i));
            score.put("score", 100 - i * 15);
            score.put("distance_km", 1.2 + i * 0.8);
            score.put("battery_percent", 95 - i * 10);
            scores.add(score);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recommended_sn", recommendedSn);
        data.put("strategy_id", strategyId);
        data.put("has_fly_to_position", hasFlyToPosition);
        data.put("scores", scores);
        data.put("reason", "Nearest device with highest battery level");
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] Recommended device: %s (score: %d, strategy: %d)",
                recommendedSn, 100, strategyId));
    }
}
