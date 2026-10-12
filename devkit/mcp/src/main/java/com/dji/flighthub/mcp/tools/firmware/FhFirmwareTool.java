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

package com.dji.flighthub.mcp.tools.firmware;

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.FirmwareInfoParam;
import com.dji.flighthubsdk.bean.FirmwareStatusItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeDeviceItem;
import com.dji.flighthubsdk.bean.FirmwareUpgradeParam;
import com.dji.flighthubsdk.bean.FirmwareUpgradeResult;
import com.dji.flighthubsdk.bean.UploadFileResult;
import com.dji.flighthubsdk.core.FirmwareManageModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * fh_firmware — 固件管理（查询/升级/上传/发布）。
 *
 * <p>支持 action：
 * <ul>
 *   <li>check_status — 查询设备固件状态（L0）</li>
 *   <li>list — 分页查询固件列表（L0）</li>
 *   <li>query_progress — 查询升级进度（L0）</li>
 *   <li>release_note — 获取最新固件发行说明（L0）</li>
 *   <li>compatibility — 查询固件兼容性配置（L0）</li>
 *   <li>upgrade — 触发固件升级（L1，需 confirm）</li>
 *   <li>upload — 上传固件文件（L1，需 confirm）</li>
 *   <li>submit — 提交固件信息（L1，需 confirm）</li>
 *   <li>release — 发布固件（L1，需 confirm）</li>
 *   <li>unrelease — 取消发布固件（L1，需 confirm）</li>
 *   <li>delete — 删除固件（L2，需 confirm）</li>
 * </ul>
 *
 * <p>混合安全等级，不在工具级拦截 confirm。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhFirmwareTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhFirmwareTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_firmware";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            "check_status", "list", "query_progress", "release_note", "compatibility",
            "upgrade", "upload", "submit", "release", "unrelease", "delete");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SNS = "device_sns";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_FILE_ID = "file_id";
    private static final String PARAM_FILE_PATH = "file_path";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";
    private static final String PARAM_DEVICE_MODEL_KEY = "device_model_key";
    private static final String PARAM_VERSION = "version";
    private static final String PARAM_DEVICE_MODEL_KEYS = "device_model_keys";
    private static final String PARAM_LANGUAGE = "language";

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    /** 固件状态枚举描述（参考 Cloud-API-Demo-Web DeviceFirmwareStatusEnum） */
    private static final Map<Integer, String> FIRMWARE_STATUS_DESC = Map.of(
            1, "none",
            2, "available",
            3, "consistency_upgrade",
            4, "upgrading"
    );

    /** 固件上传超时时间（秒），大文件上传需要更长时间 */
    private static final int SDK_UPLOAD_TIMEOUT_SECONDS = 60;

    /** 允许上传的固件目录环境变量名（覆盖默认目录，便于本地/容器化部署与测试） */
    private static final String ENV_FIRMWARE_UPLOAD_DIR = "FH_FIRMWARE_UPLOAD_DIR";

    /** 允许上传的固件目录默认值（容器内挂载路径） */
    private static final String DEFAULT_FIRMWARE_UPLOAD_DIR = "/app/firmware";

    /** 允许上传的固件目录（构造时从环境变量读取，运行期间不变） */
    private final String firmwareUploadDir;

    /** 各 action 要求的安全等级（未列出的 action 为 L0，不需要拦截） */
    private static final Map<String, SecurityLevel> ACTION_SECURITY_LEVELS = Map.of(
            "upgrade", SecurityLevel.REVERSIBLE,
            "upload", SecurityLevel.REVERSIBLE,
            "submit", SecurityLevel.REVERSIBLE,
            "release", SecurityLevel.REVERSIBLE,
            "unrelease", SecurityLevel.REVERSIBLE,
            "delete", SecurityLevel.FLIGHT
    );

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    @Autowired
    public FhFirmwareTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.firmwareUploadDir = System.getenv()
                .getOrDefault(ENV_FIRMWARE_UPLOAD_DIR, DEFAULT_FIRMWARE_UPLOAD_DIR);
    }

    /**
     * 测试专用构造函数：直接指定允许上传目录，避免依赖环境变量与真实文件系统布局。
     *
     * @param sessionManager    session 管理器
     * @param sdkService        SDK 服务
     * @param firmwareUploadDir 允许上传的固件目录
     */
    FhFirmwareTool(SessionManager sessionManager, SdkService sdkService, String firmwareUploadDir) {
        super(sessionManager);
        this.sdkService = sdkService;
        this.firmwareUploadDir = firmwareUploadDir;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Firmware management: check device firmware status, list available firmware, "
                + "trigger upgrades, upload/submit/release firmware packages. "
                + "Actions: check_status, list, query_progress, release_note, compatibility, "
                + "upgrade, upload, submit, release, unrelease, delete. "
                + "IMPORTANT for check_status: Results are grouped into dock_firmware and drone_firmware arrays. "
                + "Each item has device_type (dock/drone), device_model_key, and firmware_version. "
                + "Use drone_sns/dock_sns params to explicitly label which SNs are drones vs docks.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of("type", "string", "enum", VALID_ACTIONS, "description", "Operation to perform"));
        props.put(PARAM_DEVICE_SNS, Map.of("type", "array", "items", Map.of("type", "string"),
                "description", "Device serial numbers (for check_status/upgrade)"));
        props.put("drone_sns", Map.of("type", "array", "items", Map.of("type", "string"),
                "description", "Drone serial numbers for check_status (optional, defaults to device_sns)"));
        props.put("dock_sns", Map.of("type", "array", "items", Map.of("type", "string"),
                "description", "Dock serial numbers for check_status (optional, defaults to device_sns)"));
        props.put(PARAM_DEVICE_SN, Map.of("type", "string", "description", "Single device SN (for query_progress)"));
        props.put(PARAM_FILE_ID, Map.of("type", "string", "description", "Firmware file ID (for delete/release/unrelease)"));
        props.put(PARAM_FILE_PATH, Map.of("type", "string", "description", "Local file path (for upload)"));
        props.put(PARAM_DEVICE_MODEL_KEY, Map.of("type", "string", "description", "Device model key (for submit)"));
        props.put(PARAM_VERSION, Map.of("type", "string", "description", "Firmware version (for submit)"));
        props.put(PARAM_DEVICE_MODEL_KEYS, Map.of("type", "array", "items", Map.of("type", "string"),
                "description", "Device model keys (for release_note)"));
        props.put(PARAM_LANGUAGE, Map.of("type", "string", "description", "Language code for release notes (default: zh)"));
        props.put(PARAM_PAGE, Map.of("type", "integer", "description", "Page number for list (default: 1)"));
        props.put(PARAM_PAGE_SIZE, Map.of("type", "integer", "description", "Items per page for list (default: 20)"));
        props.put("confirm", Map.of("type", "boolean", "description", "Set true to confirm write operations"));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action", "Valid actions: " + VALID_ACTIONS);
        }

        // 统一安全等级拦截
        SecurityLevel requiredLevel = ACTION_SECURITY_LEVELS.get(action);
        if (requiredLevel != null) {
            Map<String, Object> denied = enforceSecurityLevel(requiredLevel);
            if (denied != null) {
                return denied;
            }
        }

        switch (action) {
            case "check_status": return executeCheckStatus(args);
            case "list": return executeList(args);
            case "query_progress": return executeQueryProgress(args);
            case "release_note": return executeReleaseNote(args);
            case "compatibility": return executeCompatibility();
            case "upgrade": return executeUpgrade(args);
            case "upload": return executeUpload(args);
            case "submit": return executeSubmit(args);
            case "release": return executeRelease(args);
            case "unrelease": return executeUnrelease(args);
            case "delete": return executeDelete(args);
            default: return errorResult("Unhandled action: " + action, null);
        }
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== check_status（L0） ====================

    private Map<String, Object> executeCheckStatus(Map<String, Object> args) {
        List<String> deviceSns = getStringListArg(args, PARAM_DEVICE_SNS);
        if (deviceSns == null || deviceSns.isEmpty()) {
            return errorResult("Missing required parameter: device_sns", "Provide at least one device SN.");
        }

        List<String> droneSns = getStringListArg(args, "drone_sns");
        List<String> dockSns = getStringListArg(args, "dock_sns");

        log.info("[{}] check_status: deviceSns={}, droneSns={}, dockSns={}", TOOL_NAME, deviceSns, droneSns, dockSns);

        return sdkService.isMockMode()
                ? checkStatusInMockMode(deviceSns, droneSns, dockSns)
                : checkStatusWithRealSdk(deviceSns, droneSns, dockSns);
    }

    /**
     * Mock 模式下区分机场和飞行器固件状态。
     * 如果调用方提供了 drone_sns/dock_sns，则按声明分类；
     * 否则对 device_sns 中每个 SN 同时返回 dock 和 drone 两条记录（模拟机场关联设备场景）。
     */
    private Map<String, Object> checkStatusInMockMode(List<String> deviceSns,
                                                      List<String> droneSns, List<String> dockSns) {
        List<Map<String, Object>> dockDevices = new ArrayList<>();
        List<Map<String, Object>> droneDevices = new ArrayList<>();

        if (dockSns != null && !dockSns.isEmpty()) {
            for (String sn : dockSns) {
                dockDevices.add(buildMockDockFirmware(sn));
            }
        }
        if (droneSns != null && !droneSns.isEmpty()) {
            for (String sn : droneSns) {
                droneDevices.add(buildMockDroneFirmware(sn));
            }
        }

        if (dockSns == null && droneSns == null) {
            for (String sn : deviceSns) {
                dockDevices.add(buildMockDockFirmware(sn));
                droneDevices.add(buildMockDroneFirmware(sn + "-drone"));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dock_firmware", dockDevices);
        data.put("drone_firmware", droneDevices);
        data.put("_mock", true);
        return successResult(data, "Firmware status checked. Results are grouped by device type (dock vs drone).");
    }

    private Map<String, Object> buildMockDockFirmware(String sn) {
        Map<String, Object> device = new LinkedHashMap<>();
        device.put("device_sn", sn);
        device.put("device_type", "dock");
        device.put("device_model_key", "3-3-0");
        device.put("firmware_version", "v10.01.1264");
        device.put("firmware_status", 1);
        device.put("firmware_status_desc", "none");
        return device;
    }

    private Map<String, Object> buildMockDroneFirmware(String sn) {
        Map<String, Object> device = new LinkedHashMap<>();
        device.put("device_sn", sn);
        device.put("device_type", "drone");
        device.put("device_model_key", "0-100-0");
        device.put("firmware_version", "v08.01.0207");
        device.put("firmware_status", 1);
        device.put("firmware_status_desc", "none");
        return device;
    }

    /**
     * 真实 SDK 调用。SDK 签名：checkFirmwareStatus(deviceSns, statusFilter, callback)
     * 第二个参数为状态过滤器，传 null 表示不过滤（空列表语义未定义）。
     */
    @Generated
    private Map<String, Object> checkStatusWithRealSdk(List<String> deviceSns,
                                                       List<String> droneSns, List<String> dockSns) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            List<String> allSns = new ArrayList<>(deviceSns);
            if (droneSns != null) {
                for (String sn : droneSns) {
                    if (!allSns.contains(sn)) {
                        allSns.add(sn);
                    }
                }
            }
            if (dockSns != null) {
                for (String sn : dockSns) {
                    if (!allSns.contains(sn)) {
                        allSns.add(sn);
                    }
                }
            }

            CompletableFuture<List<FirmwareStatusItem>> future = new CompletableFuture<>();
            fmm.checkFirmwareStatus(allSns, null, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "checkFirmwareStatus failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            List<FirmwareStatusItem> result = SdkCallbackBridge.await(future);
            return buildStructuredFirmwareResponse(result, droneSns, dockSns);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] check_status timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] check_status SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] check_status error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to check status: " + e.getMessage(), null);
        }
    }

    /**
     * 将 SDK 返回的 FirmwareStatusItem 列表按设备类型分组。
     * 分组依据：deviceModelKey 包含 "dock" 归入 dock_firmware，其余归入 drone_firmware。
     * 如果调用方传了 dock_sns/drone_sns，优先以 SN 归属决定分组。
     */
    private Map<String, Object> buildStructuredFirmwareResponse(List<FirmwareStatusItem> items,
                                                                List<String> droneSns, List<String> dockSns) {
        List<Map<String, Object>> dockDevices = new ArrayList<>();
        List<Map<String, Object>> droneDevices = new ArrayList<>();

        if (items != null) {
            for (FirmwareStatusItem item : items) {
                Map<String, Object> deviceMap = new LinkedHashMap<>();
                deviceMap.put("device_sn", item.getSn());
                deviceMap.put("device_model_key", item.getDeviceModelKey());
                deviceMap.put("firmware_version", item.getFirmwareVersion());
                deviceMap.put("firmware_status", item.getFirmwareStatus());
                deviceMap.put("firmware_status_desc",
                        FIRMWARE_STATUS_DESC.getOrDefault(item.getFirmwareStatus(), "unknown"));

                if (isDockDevice(item, dockSns)) {
                    deviceMap.put("device_type", "dock");
                    dockDevices.add(deviceMap);
                } else {
                    deviceMap.put("device_type", "drone");
                    droneDevices.add(deviceMap);
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dock_firmware", dockDevices);
        data.put("drone_firmware", droneDevices);
        return successResult(data, "Firmware status checked. Results are grouped by device type (dock vs drone).");
    }

    /**
     * 判断设备是否为机场类型。
     * 判断优先级：1) 显式 dockSns 列表；2) deviceModelKey 的 domain 前缀。
     * deviceModelKey 格式为 "domain-type-subtype"（如 "3-3-0" = dock, "0-100-0" = drone）。
     * domain=3 对应机场/dock，domain=0 对应无人机。
     */
    private boolean isDockDevice(FirmwareStatusItem item, List<String> dockSns) {
        if (dockSns != null && dockSns.contains(item.getSn())) {
            return true;
        }
        String modelKey = item.getDeviceModelKey();
        if (modelKey == null) {
            return false;
        }
        // domain-type-subtype 格式：domain=3 为机场/dock
        if (modelKey.startsWith("3-")) {
            return true;
        }
        // 兼容可能的人类可读格式（如 "dock2"）
        String lower = modelKey.toLowerCase();
        return lower.contains("dock") || lower.contains("airport");
    }

    // ==================== list（L0） ====================

    private Map<String, Object> executeList(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        log.info("[{}] list: page={}, pageSize={}", TOOL_NAME, page, pageSize);

        return sdkService.isMockMode()
                ? listInMockMode(page, pageSize)
                : listWithRealSdk(page, pageSize);
    }

    private Map<String, Object> listInMockMode(int page, int pageSize) {
        Map<String, Object> fw1 = new LinkedHashMap<>();
        fw1.put("file_id", "fw_001");
        fw1.put("version", "v01.07.0100");
        fw1.put("device_model_key", "dock2");
        fw1.put("released", true);

        Map<String, Object> fw2 = new LinkedHashMap<>();
        fw2.put("file_id", "fw_002");
        fw2.put("version", "v01.06.0300");
        fw2.put("device_model_key", "dock2");
        fw2.put("released", false);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("firmwares", List.of(fw1, fw2));
        data.put("total", 2);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Firmware list retrieved.");
    }

    @Generated
    private Map<String, Object> listWithRealSdk(int page, int pageSize) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<List<?>> future = new CompletableFuture<>();
            fmm.queryFirmwareList(page, pageSize, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "queryFirmwareList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            List<?> result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("firmwares", result != null ? result.toString() : List.of());
            data.put("page", page);
            data.put("page_size", pageSize);
            return successResult(data, "Firmware list retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] list error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list firmware: " + e.getMessage(), null);
        }
    }

    // ==================== query_progress（L0） ====================

    private Map<String, Object> executeQueryProgress(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn", "Provide device SN to query progress.");
        }
        log.info("[{}] query_progress: deviceSn={}", TOOL_NAME, deviceSn);

        return sdkService.isMockMode()
                ? queryProgressInMockMode(deviceSn)
                : queryProgressWithRealSdk(deviceSn);
    }

    private Map<String, Object> queryProgressInMockMode(String deviceSn) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("progress", 72);
        data.put("step", "transferring");
        data.put("target_version", "v01.07.0100");
        data.put("_mock", true);
        return successResult(data, "Upgrade progress retrieved.");
    }

    @Generated
    private Map<String, Object> queryProgressWithRealSdk(String deviceSn) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<List<?>> future = new CompletableFuture<>();
            fmm.queryFirmwareUpgradeProgress(deviceSn, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "queryFirmwareUpgradeProgress failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            List<?> result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("progress_items", result != null ? result.toString() : List.of());
            return successResult(data, "Upgrade progress retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] query_progress timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] query_progress SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] query_progress error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to query progress: " + e.getMessage(), null);
        }
    }

    // ==================== release_note（L0） ====================

    private Map<String, Object> executeReleaseNote(Map<String, Object> args) {
        List<String> modelKeys = getStringListArg(args, PARAM_DEVICE_MODEL_KEYS);
        if (modelKeys == null || modelKeys.isEmpty()) {
            return errorResult("Missing required parameter: device_model_keys", "Provide at least one model key.");
        }
        String language = getStringArg(args, PARAM_LANGUAGE, "zh");
        log.info("[{}] release_note: modelKeys={}, language={}", TOOL_NAME, modelKeys, language);

        return sdkService.isMockMode()
                ? releaseNoteInMockMode(modelKeys, language)
                : releaseNoteWithRealSdk(modelKeys, language);
    }

    private Map<String, Object> releaseNoteInMockMode(List<String> modelKeys, String language) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("release_notes", modelKeys.stream().map(key -> {
            Map<String, Object> note = new LinkedHashMap<>();
            note.put("device_model_key", key);
            note.put("version", "v01.07.0100");
            note.put("content", "Bug fixes and performance improvements.");
            note.put("release_date", "2025-06-01");
            return note;
        }).toList());
        data.put("language", language);
        data.put("_mock", true);
        return successResult(data, "Release notes retrieved.");
    }

    @Generated
    private Map<String, Object> releaseNoteWithRealSdk(List<String> modelKeys, String language) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<List<?>> future = new CompletableFuture<>();
            fmm.getLatestFirmwareReleaseNote(modelKeys, language, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "getLatestFirmwareReleaseNote failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            List<?> result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("release_notes", result != null ? result.toString() : List.of());
            data.put("language", language);
            return successResult(data, "Release notes retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] release_note timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] release_note SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] release_note error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get release notes: " + e.getMessage(), null);
        }
    }

    // ==================== compatibility（L0） ====================

    private Map<String, Object> executeCompatibility() {
        log.info("[{}] compatibility: querying config", TOOL_NAME);

        return sdkService.isMockMode()
                ? compatibilityInMockMode()
                : compatibilityWithRealSdk();
    }

    private Map<String, Object> compatibilityInMockMode() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("configs", List.of(
                Map.of("model", "dock2", "min_version", "v01.05.0000", "max_version", "v01.07.9999"),
                Map.of("model", "m3d", "min_version", "v01.02.0000", "max_version", "v01.05.9999")
        ));
        data.put("_mock", true);
        return successResult(data, "Compatibility config retrieved.");
    }

    @Generated
    private Map<String, Object> compatibilityWithRealSdk() {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<List<?>> future = new CompletableFuture<>();
            fmm.queryFirmwareCompatibilityConfig((error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "queryFirmwareCompatibilityConfig failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            List<?> result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("configs", result != null ? result.toString() : List.of());
            return successResult(data, "Compatibility config retrieved.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] compatibility timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] compatibility SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] compatibility error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get compatibility: " + e.getMessage(), null);
        }
    }

    // ==================== upgrade（L1） ====================

    private Map<String, Object> executeUpgrade(Map<String, Object> args) {
        List<String> deviceSns = getStringListArg(args, PARAM_DEVICE_SNS);
        if (deviceSns == null || deviceSns.isEmpty()) {
            return errorResult("Missing required parameter: device_sns", "Provide device SNs for firmware upgrade.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "upgrade", "firmware", "Trigger firmware upgrade for " + deviceSns.size() + " device(s)");
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] upgrade: deviceSns={}", TOOL_NAME, deviceSns);

        return sdkService.isMockMode()
                ? upgradeInMockMode(deviceSns)
                : upgradeWithRealSdk(deviceSns);
    }

    private Map<String, Object> upgradeInMockMode(List<String> deviceSns) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "upgrade_triggered");
        data.put("device_count", deviceSns.size());
        data.put("device_sns", deviceSns);
        data.put("_mock", true);
        return successResult(data, "Firmware upgrade triggered.");
    }

    @Generated
    private Map<String, Object> upgradeWithRealSdk(List<String> deviceSns) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            FirmwareUpgradeParam params = new FirmwareUpgradeParam();
            List<FirmwareUpgradeDeviceItem> deviceItems = new ArrayList<>();
            for (String sn : deviceSns) {
                FirmwareUpgradeDeviceItem item = new FirmwareUpgradeDeviceItem();
                item.setSn(sn);
                deviceItems.add(item);
            }
            params.setDevices(deviceItems);

            CompletableFuture<FirmwareUpgradeResult> future = new CompletableFuture<>();
            fmm.invokeFirmwareUpgrade(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "invokeFirmwareUpgrade failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            FirmwareUpgradeResult result = SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "upgrade_triggered");
            data.put("device_count", deviceSns.size());
            data.put("result", result != null ? result.toString() : null);
            return successResult(data, "Firmware upgrade triggered.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] upgrade timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] upgrade SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] upgrade error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to upgrade: " + e.getMessage(), null);
        }
    }

    // ==================== upload（L1） ====================

    private Map<String, Object> executeUpload(Map<String, Object> args) {
        String filePath = getStringArg(args, PARAM_FILE_PATH, null);
        if (filePath == null) {
            return errorResult("Missing required parameter: file_path", "Provide local file path for upload.");
        }

        // 路径规范化校验：防止路径穿越攻击（如 ../../etc/passwd）与符号链接逃逸。
        // toRealPath() 解析符号链接，比 normalize() 更严格——后者仅词法归一，
        // 无法防御 /app/firmware/evil -> / 这类符号链接逃逸。
        Path allowedDir = Paths.get(firmwareUploadDir).toAbsolutePath().normalize();
        Path resolved = resolveWithinAllowedDir(filePath, allowedDir);
        if (resolved == null) {
            return errorResult(
                    "file_path must be within " + firmwareUploadDir + " directory.",
                    "Provide an absolute path under " + firmwareUploadDir
                            + ", e.g. " + firmwareUploadDir + "/update.bin");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "upload", "firmware", "Upload firmware file: " + filePath);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] upload: filePath={}", TOOL_NAME, filePath);

        return sdkService.isMockMode()
                ? uploadInMockMode(filePath)
                : uploadWithRealSdk(filePath);
    }

    /**
     * 将上传路径解析为允许目录内的绝对真实路径。
     *
     * <p>优先用 {@link Path#toRealPath()} 解析符号链接；若文件尚不存在（如 mock 模式下
     * 仅校验路径合法性），回退到词法归一并校验边界。两者都不在允许目录内则返回 null。
     *
     * @param filePath  原始文件路径
     * @param allowedDir 允许上传的目录（已归一化的绝对路径）
     * @return 允许目录内的解析路径，越界则返回 null
     */
    private Path resolveWithinAllowedDir(String filePath, Path allowedDir) {
        Path resolved;
        try {
            resolved = Paths.get(filePath).toRealPath();
        } catch (IOException e) {
            // 文件不存在或不可访问：回退到词法归一，真实读取错误交给后续 SDK 调用返回。
            resolved = Paths.get(filePath).normalize().toAbsolutePath();
        }
        return resolved.startsWith(allowedDir) ? resolved : null;
    }

    private Map<String, Object> uploadInMockMode(String filePath) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "uploaded");
        data.put("file_path", filePath);
        data.put("object_key", "firmware/uploaded_" + System.currentTimeMillis() % 10000 + ".bin");
        data.put("_mock", true);
        return successResult(data, "Firmware file uploaded.");
    }

    @Generated
    private Map<String, Object> uploadWithRealSdk(String filePath) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<UploadFileResult> future = new CompletableFuture<>();
            fmm.uploadFirmwareFile(filePath, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "uploadFirmwareFile failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            UploadFileResult result = SdkCallbackBridge.await(future, SDK_UPLOAD_TIMEOUT_SECONDS);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "uploaded");
            data.put("result", result != null ? result.toString() : null);
            return successResult(data, "Firmware file uploaded.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] upload timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Upload may take longer for large files.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] upload SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] upload error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to upload: " + e.getMessage(), null);
        }
    }

    // ==================== submit（L1） ====================

    private Map<String, Object> executeSubmit(Map<String, Object> args) {
        String modelKey = getStringArg(args, PARAM_DEVICE_MODEL_KEY, null);
        if (modelKey == null) {
            return errorResult("Missing required parameter: device_model_key", "Provide device model key.");
        }
        String version = getStringArg(args, PARAM_VERSION, null);
        if (version == null) {
            return errorResult("Missing required parameter: version", "Provide firmware version.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "submit", "firmware", "Submit firmware info: " + modelKey + " " + version);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] submit: modelKey={}, version={}", TOOL_NAME, modelKey, version);

        return sdkService.isMockMode()
                ? submitInMockMode(modelKey, version)
                : submitWithRealSdk(modelKey, version);
    }

    private Map<String, Object> submitInMockMode(String modelKey, String version) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "submitted");
        data.put("device_model_key", modelKey);
        data.put("version", version);
        data.put("file_id", "fw_new_" + System.currentTimeMillis() % 10000);
        data.put("_mock", true);
        return successResult(data, "Firmware info submitted.");
    }

    @Generated
    private Map<String, Object> submitWithRealSdk(String modelKey, String version) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            FirmwareInfoParam params = new FirmwareInfoParam();
            params.setDeviceModelKey(modelKey);
            params.setVersion(version);

            // SubmitFirmwareInfoCallback: void（仅 FlightHubError）
            CompletableFuture<Void> future = new CompletableFuture<>();
            fmm.submitFirmwareInfo(params, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "submitFirmwareInfo failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "submitted");
            data.put("device_model_key", modelKey);
            data.put("version", version);
            return successResult(data, "Firmware info submitted.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] submit timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] submit SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] submit error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to submit: " + e.getMessage(), null);
        }
    }

    // ==================== release（L1） ====================

    private Map<String, Object> executeRelease(Map<String, Object> args) {
        String fileId = getStringArg(args, PARAM_FILE_ID, null);
        if (fileId == null) {
            return errorResult("Missing required parameter: file_id", "Provide firmware file ID to release.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "release", "firmware", "Release firmware: " + fileId);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] release: fileId={}", TOOL_NAME, fileId);

        return sdkService.isMockMode()
                ? voidActionInMockMode(fileId, "released", "Firmware released.")
                : releaseWithRealSdk(fileId);
    }

    @Generated
    private Map<String, Object> releaseWithRealSdk(String fileId) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            fmm.releaseFirmware(fileId, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "releaseFirmware failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("file_id", fileId);
            data.put("status", "released");
            return successResult(data, "Firmware released.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] release timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] release SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] release error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to release: " + e.getMessage(), null);
        }
    }

    // ==================== unrelease（L1） ====================

    private Map<String, Object> executeUnrelease(Map<String, Object> args) {
        String fileId = getStringArg(args, PARAM_FILE_ID, null);
        if (fileId == null) {
            return errorResult("Missing required parameter: file_id", "Provide firmware file ID to unrelease.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "unrelease", "firmware", "Unrelease firmware: " + fileId);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] unrelease: fileId={}", TOOL_NAME, fileId);

        return sdkService.isMockMode()
                ? voidActionInMockMode(fileId, "unreleased", "Firmware unreleased.")
                : unreleaseWithRealSdk(fileId);
    }

    @Generated
    private Map<String, Object> unreleaseWithRealSdk(String fileId) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            fmm.unReleaseFirmware(fileId, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "unReleaseFirmware failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("file_id", fileId);
            data.put("status", "unreleased");
            return successResult(data, "Firmware unreleased.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] unrelease timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] unrelease SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] unrelease error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to unrelease: " + e.getMessage(), null);
        }
    }

    // ==================== delete（L2） ====================

    private Map<String, Object> executeDelete(Map<String, Object> args) {
        String fileId = getStringArg(args, PARAM_FILE_ID, null);
        if (fileId == null) {
            return errorResult("Missing required parameter: file_id", "Provide firmware file ID to delete.");
        }

        Map<String, Object> confirmResult = requireHumanConfirm(args,
                "delete", "firmware", "Delete firmware: " + fileId);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] delete: fileId={}", TOOL_NAME, fileId);

        return sdkService.isMockMode()
                ? voidActionInMockMode(fileId, "deleted", "Firmware deleted.")
                : deleteWithRealSdk(fileId);
    }

    @Generated
    private Map<String, Object> deleteWithRealSdk(String fileId) {
        try {
            FirmwareManageModule fmm = getFirmwareModule();
            if (fmm == null) {
                return errorResult("FirmwareManageModule not available", "Call fh_connect first.");
            }

            CompletableFuture<Void> future = new CompletableFuture<>();
            fmm.deleteFirmware(fileId, error -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getServerCode(), "deleteFirmware failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(null);
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("file_id", fileId);
            data.put("status", "deleted");
            return successResult(data, "Firmware deleted.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] delete timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), null);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] delete SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), null);
        } catch (Exception e) {
            log.error("[{}] delete error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to delete: " + e.getMessage(), null);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 FirmwareManageModule 实例。
     *
     * @return FirmwareManageModule，不可用时返回 null
     */
    private FirmwareManageModule getFirmwareModule() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getFirmwareManageModule();
    }

    /**
     * 生成 Mock 模式下 void 操作的统一响应。
     *
     * @param fileId  固件文件 ID
     * @param status  状态标识
     * @param message 成功消息
     * @return mock 响应
     */
    private Map<String, Object> voidActionInMockMode(String fileId, String status, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("file_id", fileId);
        data.put("status", status);
        data.put("_mock", true);
        return successResult(data, message);
    }
}
