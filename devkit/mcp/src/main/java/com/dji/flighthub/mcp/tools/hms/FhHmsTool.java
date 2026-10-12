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

package com.dji.flighthub.mcp.tools.hms;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.HMSModule;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.bean.HistoryHmsDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_hms — HMS 健康管理系统告警历史查询。
 * 提供设备健康告警数据的历史查询能力，用于运维诊断和设备状态追踪。
 *
 * <p>安全等级：L0（只读查询，免确认）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhHmsTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhHmsTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_hms";

    // ==================== 参数常量 ====================
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_BEGIN_TIME = "begin_time";
    private static final String PARAM_END_TIME = "end_time";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 有效 action */
    private static final String ACTION_QUERY_HISTORY = "query_history";

    /** 默认分页参数 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhHmsTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Query HMS (Health Management System) historical alert data for device health diagnostics. "
                        + "Returns device health alerts with severity, timestamps, and error codes.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", List.of(ACTION_QUERY_HISTORY),
                "description", "Action to execute: query_history"
        ));
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Device serial number (drone or dock SN) to query HMS data for"
        ));
        props.put(PARAM_BEGIN_TIME, Map.of(
                "type", "integer",
                "description", "Start time as Unix timestamp (seconds). Defaults to 24h ago."
        ));
        props.put(PARAM_END_TIME, Map.of(
                "type", "integer",
                "description", "End time as Unix timestamp (seconds). Defaults to now."
        ));
        props.put(PARAM_PAGE, Map.of(
                "type", "integer",
                "description", "Page number (default: 1)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Page size (1~100, default: 20)"
        ));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_DEVICE_SN));
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

        // 2. 校验 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (!ACTION_QUERY_HISTORY.equals(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be: " + ACTION_QUERY_HISTORY);
        }

        // 3. 校验 device_sn
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide device_sn. Use fh_device_list to get available device SNs.");
        }

        // 4. 解析分页和时间参数
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return errorResult("Invalid page_size: " + pageSize,
                    "Must be between 1 and " + MAX_PAGE_SIZE + ".");
        }

        long now = System.currentTimeMillis() / 1000;
        long beginTime = getLongArg(args, PARAM_BEGIN_TIME, now - 86400);
        long endTime = getLongArg(args, PARAM_END_TIME, now);

        if (beginTime >= endTime) {
            return errorResult("begin_time must be less than end_time",
                    "Provide valid time range (Unix timestamps in seconds).");
        }

        // 5. Mock 模式（含 workspace 为 null 的降级场景）
        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return executeMockQuery(deviceSn, beginTime, endTime, page, pageSize);
        }

        // 6. 真实 SDK 调用
        return executeRealQuery(deviceSn, beginTime, endTime, page, pageSize);
    }

    /**
     * Mock 模式下返回模拟 HMS 告警数据。
     */
    private Map<String, Object> executeMockQuery(
            String deviceSn, long beginTime, long endTime, int page, int pageSize) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("begin_time", beginTime);
        data.put("end_time", endTime);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("total", 2);
        data.put("alerts", List.of(
                Map.of("alert_id", "hms_001",
                        "level", "warning",
                        "code", "0x16100001",
                        "message", "IMU data exception",
                        "timestamp", beginTime + 3600,
                        "device_sn", deviceSn),
                Map.of("alert_id", "hms_002",
                        "level", "notice",
                        "code", "0x16100002",
                        "message", "Compass calibration recommended",
                        "timestamp", beginTime + 7200,
                        "device_sn", deviceSn)
        ));
        return successResult(data, "Found 2 HMS alerts for " + deviceSn);
    }

    /**
     * 真实 SDK 模式下调用 queryHistoryHMSData。
     */
    @Generated
    private Map<String, Object> executeRealQuery(
            String deviceSn, long beginTime, long endTime, int page, int pageSize) {
        try {
            log.info("[REAL] queryHistoryHMSData: device={}, time=[{},{}], page={}/{}",
                    deviceSn, beginTime, endTime, page, pageSize);

            Workspace workspace = sdkService.getWorkspace();
            HMSModule hmsm = workspace.getHMSModule();

            HistoryHmsDataParam param = new HistoryHmsDataParam();
            param.setSnList(java.util.Collections.singletonList(deviceSn));
            param.setPageNum(page);
            param.setPageSize(pageSize);
            param.setCreateTimeBegin(beginTime);
            param.setCreateTimeEnd(endTime);

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            hmsm.queryHistoryHMSData(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "queryHistoryHMSData failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("device_sn", deviceSn);
                data.put("begin_time", beginTime);
                data.put("end_time", endTime);
                data.put("page", page);
                data.put("page_size", pageSize);
                data.put("result", result.toString());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "HMS history data retrieved for " + deviceSn);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "HMS query timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("HMS query error for device {}: {}", deviceSn, e.getMessage(), e);
            return errorResult("HMS query failed: " + e.getMessage(),
                    "Check device connectivity and retry.");
        }
    }
}
