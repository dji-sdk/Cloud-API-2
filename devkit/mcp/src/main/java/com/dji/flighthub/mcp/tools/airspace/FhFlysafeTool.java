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

import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * fh_flysafe — 飞安地图数据库管理工具。
 * 查询设备飞安数据库版本并触发升级，确保飞行安全限制数据为最新版本。
 *
 * <p>对应 SDK 接口（Workspace 模块）：
 * <ul>
 *   <li>getFlysafeDatabaseUpgradeCheck — 检查飞安数据库是否需要升级</li>
 *   <li>invokeFlysafeDatabaseUpgrade — 触发飞安数据库升级</li>
 * </ul>
 *
 * <p>安全等级：check=L0（只读）；upgrade=L1（内部通过 requireHumanConfirm 门控）
 * <p>注意：不覆写 requiresHumanConfirmation()，避免 SSE 层整体拦截只读 check 操作。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFlysafeTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFlysafeTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_flysafe";

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_DEVICE_SNS = "device_sns";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of("check", "upgrade");

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhFlysafeTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "FlySafe database management tool. "
                + "action=check: query a device's flysafe database version and check if upgrade is available. "
                + "action=upgrade: trigger flysafe database upgrade for one or more devices (requires confirm=true). "
                + "The flysafe database contains geo-fencing data, no-fly zones, and altitude limits.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation: 'check' to query upgrade status, 'upgrade' to trigger update"
        ));
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Single device SN (for action=check)"
        ));
        props.put(PARAM_DEVICE_SNS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "List of device SNs to upgrade (for action=upgrade)"
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Required for action=upgrade. Pass true to confirm.",
                "default", false
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
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

        // 2. 解析 action
        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid or missing action",
                    "Must be one of: " + VALID_ACTIONS);
        }

        // 3. 分发 action
        return switch (action) {
            case "check" -> executeCheck(args);
            case "upgrade" -> executeUpgrade(args);
            default -> errorResult("Unknown action: " + action, "Must be one of: " + VALID_ACTIONS);
        };
    }

    // ==================== action 分发方法 ====================

    /**
     * check — 检查飞安数据库升级状态（L0 只读）。
     */
    private Map<String, Object> executeCheck(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide a device serial number for flysafe check.");
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockCheck(deviceSn);
        }
        return checkApiRemoved(deviceSn);
    }

    /**
     * upgrade — 触发飞安数据库升级（L1 写操作，需确认）。
     */
    private Map<String, Object> executeUpgrade(Map<String, Object> args) {
        List<String> deviceSns = parseDeviceSns(args);
        if (deviceSns == null || deviceSns.isEmpty()) {
            return errorResult("Missing required parameter: device_sns",
                    "Provide a list of device serial numbers to upgrade.");
        }

        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "flysafe_upgrade",
                String.join(",", deviceSns),
                String.format("即将触发 %d 台设备的飞安数据库升级，确认执行？", deviceSns.size()));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockUpgrade(deviceSns);
        }
        return upgradeApiRemoved(deviceSns);
    }

    // ==================== 真实 SDK 调用（API 已移除） ====================

    @Generated
    private Map<String, Object> checkApiRemoved(String deviceSn) {
        // getFlysafeDatabaseUpgradeCheck 已在 SDK 2.0 中移除，飞安数据库版本查询由固件升级流程统一管理。
        // 返回错误而非假成功，避免运营方误以为查询已执行。
        log.error("fh_flysafe check 不可用：getFlysafeDatabaseUpgradeCheck 已在 SDK 2.0 中移除，deviceSn={}", deviceSn);
        return errorResult(
                "fh_flysafe check 不可用：getFlysafeDatabaseUpgradeCheck 已在 SDK 2.0 中移除",
                "请通过固件升级流程（fh_firmware）管理飞安数据库版本。");
    }

    @Generated
    private Map<String, Object> upgradeApiRemoved(List<String> deviceSns) {
        // invokeFlysafeDatabaseUpgrade 已在 SDK 2.0 中移除，飞安数据库升级由固件升级流程统一管理。
        // 返回错误而非假成功，避免运营方误以为升级指令已下发至设备。
        log.error("fh_flysafe upgrade 不可用：invokeFlysafeDatabaseUpgrade 已在 SDK 2.0 中移除，deviceSns={}", deviceSns);
        return errorResult(
                "fh_flysafe upgrade 不可用：invokeFlysafeDatabaseUpgrade 已在 SDK 2.0 中移除",
                "请通过固件升级流程（fh_firmware）管理飞安数据库升级。");
    }

    // ==================== Mock 方法 ====================

    private Map<String, Object> mockCheck(String deviceSn) {
        log.info("[MOCK] getFlysafeDatabaseUpgradeCheck: deviceSn={}", deviceSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("current_version", "2025.03.15");
        data.put("latest_version", "2026.06.01");
        data.put("upgrade_available", true);
        data.put("database_size_mb", 128);
        data.put("last_updated", "2025-03-15T10:00:00Z");
        data.put("_mock", true);

        return successResult(data, String.format(
                "[MOCK] Device %s flysafe DB: v2025.03.15 → v2026.06.01 (upgrade available).", deviceSn));
    }

    private Map<String, Object> mockUpgrade(List<String> deviceSns) {
        log.info("[MOCK] invokeFlysafeDatabaseUpgrade: deviceSns={}", deviceSns);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sns", deviceSns);
        data.put("status", "upgrade_triggered");
        data.put("device_count", deviceSns.size());
        data.put("estimated_time_seconds", 300);
        data.put("_mock", true);

        return successResult(data, String.format(
                "[MOCK] Flysafe database upgrade triggered for %d device(s). Estimated time: ~5 minutes.",
                deviceSns.size()));
    }

    // ==================== 辅助方法 ====================

    /**
     * 解析 device_sns 参数，兼容传单个 device_sn 的场景。
     */
    private List<String> parseDeviceSns(Map<String, Object> args) {
        List<String> sns = getStringListArg(args, PARAM_DEVICE_SNS);
        if (sns != null && !sns.isEmpty()) {
            return sns;
        }
        // 兼容：若传了单个 device_sn 但没传 device_sns，自动转为列表
        String singleSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (singleSn != null) {
            return List.of(singleSn);
        }
        return null;
    }
}
