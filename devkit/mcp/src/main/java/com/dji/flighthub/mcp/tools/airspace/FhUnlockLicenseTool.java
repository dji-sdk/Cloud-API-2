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
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.core.SafetyServiceModule;
import com.dji.flighthubsdk.core.Workspace;
import com.dji.flighthubsdk.bean.CheckUnlockLicenseParam;
import com.dji.flighthubsdk.bean.ImportUnlockLicenseParam;
import com.dji.flighthubsdk.bean.QueryDeviceUnlockLicenseListParam;
import com.dji.flighthubsdk.bean.SwitchUnlockLicenseParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * fh_unlock_license — 解锁证书管理工具。
 * 统一管理设备飞行限制区域的解锁证书，支持查询、提交、上传、切换、检查和清除操作。
 *
 * <p>对应 SDK 接口（Workspace 模块）：
 * <ul>
 *   <li>getUnlockLicenseList — 查询解锁证书列表</li>
 *   <li>submitUnlockLicense — 提交解锁证书</li>
 *   <li>uploadUnlockLicenseFile — 上传证书文件</li>
 *   <li>switchUnlockLicense — 启用/禁用证书</li>
 *   <li>checkUnlockLicense — 检查证书有效性</li>
 *   <li>clearDeviceUnlockLicense — 清除设备所有证书</li>
 * </ul>
 *
 * <p>安全等级：list=L0（只读）；upload/check/submit/switch=L1（需 confirm）；clear=L2（需 confirm）
 * <p>注意：不覆写 requiresHumanConfirmation()，避免 SSE 层整体拦截只读操作。
 * 写操作在各自分支内部通过 requireHumanConfirm() 门控。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhUnlockLicenseTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhUnlockLicenseTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_unlock_license";

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_LICENSE_ID = "license_id";
    private static final String PARAM_LICENSE_TYPE = "license_type";
    private static final String PARAM_DEVICE_MODEL_DOMAIN = "device_model_domain";
    private static final String PARAM_CERT_URL = "cert_url";
    private static final String PARAM_FILE_PATH = "file_path";
    private static final String PARAM_FILE_KEY = "file_key";
    private static final String PARAM_ENABLE = "enable";
    private static final String PARAM_DETAIL = "detail";

    /** 有效 action 列表 */
    private static final List<String> VALID_ACTIONS = List.of(
            "list", "submit", "upload", "switch", "check", "clear");

    /** 各 action 要求的安全等级（未列出的 action 为 L0） */
    private static final Map<String, SecurityLevel> ACTION_SECURITY_LEVELS = Map.of(
            "submit", SecurityLevel.REVERSIBLE,
            "upload", SecurityLevel.REVERSIBLE,
            "switch", SecurityLevel.REVERSIBLE,
            "check", SecurityLevel.REVERSIBLE,
            "clear", SecurityLevel.FLIGHT  // L2：不可逆清除
    );

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhUnlockLicenseTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Manage device unlock licenses for restricted flight zones. "
                + "Actions: list (query licenses), submit (import certificate), "
                + "upload (upload license file), switch (enable/disable), "
                + "check (validate license), clear (remove all licenses from device). "
                + "Write operations (submit/upload/switch/check/clear) require confirm=true.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation to perform"
        ));
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Device serial number (required for list/submit/switch/check/clear)"
        ));
        props.put(PARAM_LICENSE_ID, Map.of(
                "type", "string",
                "description", "License ID (for switch/check/list filtering)"
        ));
        props.put(PARAM_LICENSE_TYPE, Map.of(
                "type", "string",
                "description", "License type filter (for list)"
        ));
        props.put(PARAM_DEVICE_MODEL_DOMAIN, Map.of(
                "type", "string",
                "description", "Device model domain filter (for list)"
        ));
        props.put(PARAM_CERT_URL, Map.of(
                "type", "string",
                "description", "Certificate URL (for submit)"
        ));
        props.put(PARAM_FILE_PATH, Map.of(
                "type", "string",
                "description", "Local file path of license file (for upload)"
        ));
        props.put(PARAM_FILE_KEY, Map.of(
                "type", "string",
                "description", "File key returned from upload (for check)"
        ));
        props.put(PARAM_ENABLE, Map.of(
                "type", "boolean",
                "description", "Enable or disable the license (for switch)"
        ));
        props.put(PARAM_DETAIL, Map.of(
                "type", "boolean",
                "description", "Include license detail in list results (default: false)",
                "default", false
        ));
        props.put("confirm", Map.of(
                "type", "boolean",
                "description", "Required for write operations (submit/upload/switch/check/clear)",
                "default", false
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
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

        // 2.5 统一安全等级拦截
        SecurityLevel requiredLevel = ACTION_SECURITY_LEVELS.get(action);
        if (requiredLevel != null) {
            Map<String, Object> denied = enforceSecurityLevel(requiredLevel);
            if (denied != null) {
                return denied;
            }
        }

        // 3. dry_run 模式（仅 L1 写操作生效）：参数校验通过，返回预览
        if (isDryRun(args) && requiredLevel != null) {
            return dryRunResult(
                    "Would execute unlock license '" + action + "' operation",
                    buildDryRunPreview(TOOL_NAME, action, "unlock_license",
                            Map.of("action", action),
                            List.of("session_active", "security_level_allowed"),
                            requiredLevel, true));
        }

        // 4. 分发 action
        return switch (action) {
            case "list" -> executeList(args);
            case "submit" -> executeSubmit(args);
            case "upload" -> executeUpload(args);
            case "switch" -> executeSwitch(args);
            case "check" -> executeCheck(args);
            case "clear" -> executeClear(args);
            default -> errorResult("Unknown action: " + action, "Must be one of: " + VALID_ACTIONS);
        };
    }

    // ==================== action 分发方法 ====================

    /**
     * list — 查询解锁证书列表（L0 只读）。
     */
    private Map<String, Object> executeList(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide a device serial number for list query.");
        }

        String licenseId = getStringArg(args, PARAM_LICENSE_ID, null);
        String licenseType = getStringArg(args, PARAM_LICENSE_TYPE, null);
        String modelDomain = getStringArg(args, PARAM_DEVICE_MODEL_DOMAIN, null);
        boolean detail = getBooleanArg(args, PARAM_DETAIL);

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockList(deviceSn, licenseType, detail);
        }
        return listWithRealSdk(deviceSn, licenseId, licenseType, modelDomain, detail);
    }

    /**
     * submit — 提交解锁证书（L1 写操作）。
     */
    private Map<String, Object> executeSubmit(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        String certUrl = getStringArg(args, PARAM_CERT_URL, null);

        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide a device serial number for submit.");
        }
        if (certUrl == null) {
            return errorResult("Missing required parameter: cert_url",
                    "Provide the certificate URL to submit.");
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "submit_unlock_license",
                deviceSn,
                String.format("即将为设备 %s 提交解锁证书，确认执行？", deviceSn));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockSubmit(deviceSn, certUrl);
        }
        return submitWithRealSdk(deviceSn, certUrl);
    }

    /**
     * upload — 上传证书文件（L1 写操作）。
     */
    private Map<String, Object> executeUpload(Map<String, Object> args) {
        String filePath = getStringArg(args, PARAM_FILE_PATH, null);
        if (filePath == null) {
            return errorResult("Missing required parameter: file_path",
                    "Provide the local file path of the license file.");
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "upload_unlock_license",
                filePath,
                String.format("即将上传解锁证书文件: %s，确认执行？", filePath));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockUpload(filePath);
        }
        return uploadWithRealSdk(filePath);
    }

    /**
     * switch — 启用/禁用证书（L1 写操作）。
     */
    private Map<String, Object> executeSwitch(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        String licenseId = getStringArg(args, PARAM_LICENSE_ID, null);
        boolean enable = getBooleanArg(args, PARAM_ENABLE);

        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide a device serial number.");
        }
        if (licenseId == null) {
            return errorResult("Missing required parameter: license_id",
                    "Provide the license ID to switch.");
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "switch_unlock_license",
                deviceSn,
                String.format("即将%s设备 %s 的解锁证书 %s，确认执行？",
                        enable ? "启用" : "禁用", deviceSn, licenseId));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockSwitch(deviceSn, licenseId, enable);
        }
        return switchWithRealSdk(deviceSn, licenseId, enable);
    }

    /**
     * check — 检查证书有效性（L1 操作）。
     */
    private Map<String, Object> executeCheck(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        String fileKey = getStringArg(args, PARAM_FILE_KEY, null);

        if (fileKey == null) {
            return errorResult("Missing required parameter: file_key",
                    "Provide the file key (returned from upload) to check.");
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "check_unlock_license",
                fileKey,
                String.format("即将校验解锁证书文件 (fileKey=%s)，确认执行？", fileKey));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockCheck(deviceSn, fileKey);
        }
        return checkWithRealSdk(deviceSn, fileKey);
    }

    /**
     * clear — 清除设备所有解锁证书（L2 危险操作）。
     */
    private Map<String, Object> executeClear(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        if (deviceSn == null) {
            return errorResult("Missing required parameter: device_sn",
                    "Provide a device serial number to clear licenses from.");
        }

        Map<String, Object> confirmError = requireHumanConfirm(args, "clear_unlock_licenses",
                deviceSn,
                String.format("⚠️ 即将清除设备 %s 上的所有解锁证书，此操作不可恢复！确认执行？", deviceSn));
        if (confirmError != null) {
            return confirmError;
        }

        if (sdkService.isMockMode() || sdkService.getWorkspace() == null) {
            return mockClear(deviceSn);
        }
        return clearWithRealSdk(deviceSn);
    }

    // ==================== 真实 SDK 调用 ====================

    @Generated
    private Map<String, Object> listWithRealSdk(String deviceSn, String licenseId,
                                                 String licenseType, String modelDomain, boolean detail) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            SafetyServiceModule ssm = workspace.getSafetyServiceModule();

            QueryDeviceUnlockLicenseListParam param = new QueryDeviceUnlockLicenseListParam();
            param.setDeviceSn(deviceSn);
            if (licenseId != null) {
                param.setLicenseId(licenseId);
            }
            if (licenseType != null) {
                param.setLicenseType(licenseType);
            }
            if (modelDomain != null) {
                try {
                    param.setDeviceModelDomain(Integer.parseInt(modelDomain));
                } catch (NumberFormatException e) {
                    return errorResult("Invalid device_model_domain: " + modelDomain,
                            "device_model_domain must be a numeric string (e.g. \"0\", \"1\").");
                }
            }
            param.setDetail(detail);

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ssm.getUnlockLicenseList(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "getUnlockLicenseList failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("licenses", result.getLicenses());
                data.put("result_code", result.getResult());
                data.put("device_model_domain", result.getDeviceModelDomain());
                data.put("consistence", result.isConsistence());
                data.put("device_sn", deviceSn);
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "Unlock license list retrieved for device " + deviceSn);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "License list query timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_unlock_license list error: {}", e.getMessage(), e);
            return errorResult("SDK error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> submitWithRealSdk(String deviceSn, String certUrl) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            SafetyServiceModule ssm = workspace.getSafetyServiceModule();

            ImportUnlockLicenseParam param = new ImportUnlockLicenseParam();
            param.setDeviceSn(deviceSn);
            param.setCertUrl(certUrl);

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.submitUnlockLicense(param, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "submitUnlockLicense failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("cert_url", certUrl);
            data.put("status", "submitted");
            return successResult(data, "Unlock license submitted for device " + deviceSn);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "License submit timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_unlock_license submit error: {}", e.getMessage(), e);
            return errorResult("SDK error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> uploadWithRealSdk(String filePath) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            SafetyServiceModule ssm = workspace.getSafetyServiceModule();

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ssm.uploadUnlockLicenseFile(filePath, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "uploadUnlockLicenseFile failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("file_path", filePath);
                data.put("result", result.toString());
                data.put("status", "uploaded");
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "License file uploaded successfully.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "License upload timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_unlock_license upload error: {}", e.getMessage(), e);
            return errorResult("SDK error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> switchWithRealSdk(String deviceSn, String licenseId, boolean enable) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            SafetyServiceModule ssm = workspace.getSafetyServiceModule();

            SwitchUnlockLicenseParam param = new SwitchUnlockLicenseParam();
            param.setDeviceSn(deviceSn);
            try {
                param.setLicenseId(Integer.parseInt(licenseId));
            } catch (NumberFormatException e) {
                return errorResult("Invalid license_id: " + licenseId,
                        "license_id must be a numeric string (e.g. \"12345\").");
            }
            param.setEnable(enable);

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.switchUnlockLicense(param, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "switchUnlockLicense failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("license_id", licenseId);
            data.put("enabled", enable);
            data.put("status", enable ? "enabled" : "disabled");
            return successResult(data, String.format("License %s %s for device %s.",
                    licenseId, enable ? "enabled" : "disabled", deviceSn));
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "License switch timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_unlock_license switch error: {}", e.getMessage(), e);
            return errorResult("SDK error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> checkWithRealSdk(String deviceSn, String fileKey) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            SafetyServiceModule ssm = workspace.getSafetyServiceModule();

            CheckUnlockLicenseParam param = new CheckUnlockLicenseParam();
            param.setFileKey(fileKey);
            if (deviceSn != null) {
                param.setDeviceSn(deviceSn);
            }

            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            ssm.checkUnlockLicense(param, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "checkUnlockLicense failed: " + formatSdkError(error)));
                    return;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("file_key", fileKey);
                data.put("device_sn", deviceSn);
                data.put("check_result", result.toString());
                future.complete(data);
            });

            Map<String, Object> data = SdkCallbackBridge.await(future);
            return successResult(data, "License file check completed.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "License check timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_unlock_license check error: {}", e.getMessage(), e);
            return errorResult("SDK error", e.getMessage());
        }
    }

    @Generated
    private Map<String, Object> clearWithRealSdk(String deviceSn) {
        try {
            Workspace workspace = sdkService.getWorkspace();
            SafetyServiceModule ssm = workspace.getSafetyServiceModule();

            CompletableFuture<Void> future = new CompletableFuture<>();
            ssm.clearDeviceUnlockLicense(deviceSn, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new SdkCallbackBridge.SdkException(
                            error.getHttpCode(), "clearDeviceUnlockLicense failed: " + formatSdkError(error)));
                }
            });

            SdkCallbackBridge.await(future);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("device_sn", deviceSn);
            data.put("status", "cleared");
            return successResult(data, "All unlock licenses cleared from device " + deviceSn);
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            return errorResult("SDK timeout", "License clear timed out.");
        } catch (SdkCallbackBridge.SdkException e) {
            return errorResult("SDK error (code=" + e.getErrorCode() + ")", e.getMessage());
        } catch (Exception e) {
            log.error("fh_unlock_license clear error: {}", e.getMessage(), e);
            return errorResult("SDK error", e.getMessage());
        }
    }

    // ==================== Mock 方法 ====================

    private Map<String, Object> mockList(String deviceSn, String licenseType, boolean detail) {
        log.info("[MOCK] getUnlockLicenseList: deviceSn={}, type={}, detail={}", deviceSn, licenseType, detail);

        List<Map<String, Object>> licenses = new ArrayList<>();
        Map<String, Object> lic1 = new LinkedHashMap<>();
        lic1.put("license_id", "LIC-20250101-001");
        lic1.put("device_sn", deviceSn);
        lic1.put("license_type", "height_limit");
        lic1.put("enabled", true);
        lic1.put("expire_time", "2026-12-31T23:59:59Z");
        lic1.put("area_name", "Zone-A Industrial Park");
        if (detail) {
            lic1.put("max_height_m", 500);
            lic1.put("area_coordinates", List.of(
                    Map.of("lat", 22.5432, "lng", 113.9461),
                    Map.of("lat", 22.5450, "lng", 113.9480)
            ));
        }
        licenses.add(lic1);

        Map<String, Object> lic2 = new LinkedHashMap<>();
        lic2.put("license_id", "LIC-20250201-002");
        lic2.put("device_sn", deviceSn);
        lic2.put("license_type", "nfz_unlock");
        lic2.put("enabled", false);
        lic2.put("expire_time", "2026-06-30T23:59:59Z");
        lic2.put("area_name", "Airport Buffer Zone");
        licenses.add(lic2);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("licenses", licenses);
        data.put("total", licenses.size());
        data.put("device_sn", deviceSn);
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] Found %d unlock license(s) for device %s.",
                licenses.size(), deviceSn));
    }

    private Map<String, Object> mockSubmit(String deviceSn, String certUrl) {
        log.info("[MOCK] submitUnlockLicense: deviceSn={}, certUrl={}", deviceSn, certUrl);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("cert_url", certUrl);
        data.put("status", "submitted");
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] Unlock license submitted for device %s.", deviceSn));
    }

    private Map<String, Object> mockUpload(String filePath) {
        log.info("[MOCK] uploadUnlockLicenseFile: filePath={}", filePath);

        String mockFileKey = "fk_" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("file_path", filePath);
        data.put("file_key", mockFileKey);
        data.put("status", "uploaded");
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] License file uploaded. file_key=%s", mockFileKey));
    }

    private Map<String, Object> mockSwitch(String deviceSn, String licenseId, boolean enable) {
        log.info("[MOCK] switchUnlockLicense: deviceSn={}, licenseId={}, enable={}", deviceSn, licenseId, enable);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("license_id", licenseId);
        data.put("enabled", enable);
        data.put("status", enable ? "enabled" : "disabled");
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] License %s %s for device %s.",
                licenseId, enable ? "enabled" : "disabled", deviceSn));
    }

    private Map<String, Object> mockCheck(String deviceSn, String fileKey) {
        log.info("[MOCK] checkUnlockLicense: deviceSn={}, fileKey={}", deviceSn, fileKey);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("file_key", fileKey);
        data.put("device_sn", deviceSn);
        data.put("valid", true);
        data.put("license_type", "height_limit");
        data.put("expire_time", "2026-12-31T23:59:59Z");
        data.put("_mock", true);

        return successResult(data, "[MOCK] License file is valid.");
    }

    private Map<String, Object> mockClear(String deviceSn) {
        log.info("[MOCK] clearDeviceUnlockLicense: deviceSn={}", deviceSn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("device_sn", deviceSn);
        data.put("status", "cleared");
        data.put("licenses_removed", 2);
        data.put("_mock", true);

        return successResult(data, String.format("[MOCK] All unlock licenses cleared from device %s.", deviceSn));
    }
}
