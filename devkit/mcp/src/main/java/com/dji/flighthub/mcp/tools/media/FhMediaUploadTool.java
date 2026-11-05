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

package com.dji.flighthub.mcp.tools.media;

import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthubsdk.bean.GetMediaUploadStsParams;
import com.dji.flighthubsdk.bean.GetMediaUploadStsResult;
import com.dji.flighthubsdk.bean.MediaUploadCallbackFile;
import com.dji.flighthubsdk.bean.MediaUploadCallbackParams;
import com.dji.flighthubsdk.bean.MediaUploadCallbackResult;
import com.dji.flighthubsdk.bean.MediaUploadStsCredentials;
import com.dji.flighthubsdk.core.MediaServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_media_upload — 获取媒体上传 STS 临时凭证，以及上报媒体上传完成回调。
 *
 * <p>支持 action：
 * <ul>
 *   <li>get_upload_sts — 获取对象存储 STS 临时凭证，用于客户端直传文件</li>
 *   <li>upload_callback — 调用方已独立使用 STS 凭证将文件 PUT 到对象存储后，
 *       上报上传完成结果（类似 fh_psdk_resource.upload_speaker_audio 的
 *       "先发凭证、再单独上报"两段式流程，但两者实现互不复用）</li>
 * </ul>
 *
 * <p>安全等级：混合——get_upload_sts 为 L0（只读，仅签发凭证不产生实际写入），
 * upload_callback 为 L1（{@link SecurityLevel#REVERSIBLE}，向服务端写入媒体记录，
 * 需 confirm=true 或 client 弹窗人工确认）。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhMediaUploadTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhMediaUploadTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_media_upload";

    /** action: 获取上传 STS 临时凭证 */
    private static final String ACTION_GET_UPLOAD_STS = "get_upload_sts";

    /** action: 上报上传完成回调 */
    private static final String ACTION_UPLOAD_CALLBACK = "upload_callback";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(ACTION_GET_UPLOAD_STS, ACTION_UPLOAD_CALLBACK);

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_SN = "sn";
    private static final String PARAM_FILE_PATH = "file_path";
    private static final String PARAM_EXPIRE = "expire";
    private static final String PARAM_MODULE = "module";
    private static final String PARAM_RESULT = "result";
    private static final String PARAM_PROGRESS = "progress";
    private static final String PARAM_FILE = "file";
    private static final String PARAM_FILE_OBJECT_KEY = "object_key";
    private static final String PARAM_FILE_FINGERPRINT = "fingerprint";
    private static final String PARAM_FILE_NAME = "name";
    private static final String PARAM_FILE_TYPE = "type";
    private static final String PARAM_FILE_CLOUD_TO_CLOUD_ID = "cloud_to_cloud_id";
    private static final String PARAM_FILE_CREATED_TIME = "created_time";
    private static final String PARAM_FILE_SOURCE = "source";
    private static final String PARAM_EXPECTED_FILE_COUNT = "expected_file_count";
    private static final String PARAM_UPLOADED_FILE_COUNT = "uploaded_file_count";
    private static final String PARAM_FLIGHT_TYPE = "flight_type";
    private static final String PARAM_BIZ_CODE = "biz_code";
    private static final String PARAM_VERSION = "version";
    private static final String PARAM_CONFIRM = "confirm";

    /** STS 凭证默认有效期（秒） */
    private static final int DEFAULT_EXPIRE_SECONDS = 3600;

    /** module 默认值：其他(含0)=默认设备STS */
    private static final int DEFAULT_MODULE = 0;

    /** module 已知取值：飞行记录 STS */
    private static final int MODULE_FLIGHT_RECORD = 2;

    /** module 已知取值：SN 加密 */
    private static final int MODULE_SN_ENCRYPTION = 2001;

    /** module 参数说明：枚举含义仅来自 SDK 内部 Javadoc，无公开枚举类 */
    private static final String MODULE_DESCRIPTION =
            "Business module type for the STS credential (raw int, no SDK enum exists): "
            + MODULE_FLIGHT_RECORD + "=flight record STS, "
            + MODULE_SN_ENCRYPTION + "=SN encryption, "
            + "other values including 0=default device STS. Pass the value through as documented.";

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhMediaUploadTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
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
                "Media upload workflow: (1) get_upload_sts (read-only) issues object-storage STS "
                + "temporary credentials so the caller can PUT a file directly to object storage. "
                + "(2) upload_callback (reversible, requires confirm=true) reports upload completion "
                + "AFTER the caller has independently PUT the file to object storage using the "
                + "credentials returned by get_upload_sts — mirrors the two-step "
                + "'issue STS, then separately register/report' pattern used by "
                + "fh_psdk_resource.upload_speaker_audio. " + MODULE_DESCRIPTION);

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS,
                "description", "Operation to perform"
        ));
        props.put(PARAM_SN, Map.of(
                "type", "string",
                "description", "Device serial number. Required for both actions."
        ));
        props.put(PARAM_FILE_PATH, Map.of(
                "type", "string",
                "description", "Target file path/object key hint for get_upload_sts. Optional."
        ));
        props.put(PARAM_EXPIRE, Map.of(
                "type", "integer",
                "description", "STS credential validity in seconds for get_upload_sts. "
                        + "Optional, defaults to " + DEFAULT_EXPIRE_SECONDS + "."
        ));
        props.put(PARAM_MODULE, Map.of(
                "type", "integer",
                "description", MODULE_DESCRIPTION + " Optional for get_upload_sts, defaults to "
                        + DEFAULT_MODULE + "."
        ));
        props.put(PARAM_RESULT, Map.of(
                "type", "integer",
                "description", "Upload result code. Required for upload_callback."
        ));
        props.put(PARAM_PROGRESS, Map.of(
                "type", "integer",
                "description", "Upload progress percentage (0-100). Optional for upload_callback."
        ));
        props.put(PARAM_FILE, buildFileSchema());
        props.put(PARAM_EXPECTED_FILE_COUNT, Map.of(
                "type", "integer",
                "description", "Expected total number of files to upload. Optional for upload_callback."
        ));
        props.put(PARAM_UPLOADED_FILE_COUNT, Map.of(
                "type", "integer",
                "description", "Number of files already uploaded. Optional for upload_callback."
        ));
        props.put(PARAM_FLIGHT_TYPE, Map.of(
                "type", "integer",
                "description", "Flight type code. Optional for upload_callback."
        ));
        props.put(PARAM_BIZ_CODE, Map.of(
                "type", "string",
                "description", "Business code. Optional for upload_callback."
        ));
        props.put(PARAM_VERSION, Map.of(
                "type", "string",
                "description", "Protocol/client version. Optional for upload_callback."
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Set true to confirm upload_callback (write operation)."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION, PARAM_SN));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 构建 file 嵌套参数的 schema。
     *
     * <p>file.ext / file.metadata 为 SDK 内嵌套 bean（ext 含 11 个字段，metadata 含拍摄位置等信息），
     * 字段较多且对"上报上传完成"这一场景非必需，故此处不在 Tool 输入 schema 中逐字段展开，
     * 而是作为可选的原始嵌套对象（object）透传给调用方，由调用方按需填充、Tool 侧尽力转换，
     * 缺失时静默跳过，避免为一个次要场景引入 20+ 个扁平化参数增加 AI Agent 的认知负担。
     *
     * @return file 参数 schema
     */
    private static Map<String, Object> buildFileSchema() {
        Map<String, Object> fileProps = new LinkedHashMap<>();
        fileProps.put(PARAM_FILE_OBJECT_KEY, Map.of(
                "type", "string", "description", "Object storage key of the uploaded file."));
        fileProps.put(PARAM_FILE_FINGERPRINT, Map.of(
                "type", "string", "description", "File fingerprint/checksum."));
        fileProps.put(PARAM_FILE_NAME, Map.of(
                "type", "string", "description", "File name."));
        fileProps.put(PARAM_FILE_TYPE, Map.of(
                "type", "integer", "description", "File type code."));
        fileProps.put(PARAM_FILE_CLOUD_TO_CLOUD_ID, Map.of(
                "type", "string", "description", "Cloud-to-cloud transfer ID. Optional."));
        fileProps.put(PARAM_FILE_CREATED_TIME, Map.of(
                "type", "string", "description", "File creation time. Optional."));
        fileProps.put(PARAM_FILE_SOURCE, Map.of(
                "type", "string", "description", "File source. Optional."));
        fileProps.put("ext", Map.of(
                "type", "object",
                "description", "Optional raw nested extension info (project/workspace/flight/drone/payload "
                        + "identifiers etc.). Passed through as-is; omit if not available."));
        fileProps.put("metadata", Map.of(
                "type", "object",
                "description", "Optional raw nested shoot metadata (altitude/gimbal yaw/shoot position etc.). "
                        + "Passed through as-is; omit if not available."));

        Map<String, Object> fileSchema = new LinkedHashMap<>();
        fileSchema.put("type", "object");
        fileSchema.put("properties", fileProps);
        fileSchema.put("description", "Uploaded file info. Required for upload_callback. "
                + "ext/metadata are optional raw nested objects — see each field's description.");
        return fileSchema;
    }

    /**
     * 执行本工具的操作，按 action 分发。
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
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action", "Valid actions: " + VALID_ACTIONS);
        }

        String sn = getStringArg(args, PARAM_SN, null);
        if (sn == null) {
            return errorResult("Missing required parameter: sn",
                    "Provide sn. Use fh_device_list to get available device SNs.");
        }

        return ACTION_GET_UPLOAD_STS.equals(action)
                ? executeGetUploadSts(args, sn)
                : executeUploadCallback(args, sn);
    }

    // ==================== get_upload_sts ====================

    /**
     * 处理 get_upload_sts action：L0 只读，无需确认。
     *
     * @param args 输入参数
     * @param sn   目标设备 SN
     * @return 执行结果
     */
    private Map<String, Object> executeGetUploadSts(Map<String, Object> args, String sn) {
        String filePath = getStringArg(args, PARAM_FILE_PATH, "");
        int expire = getIntArg(args, PARAM_EXPIRE, DEFAULT_EXPIRE_SECONDS);
        int module = getIntArg(args, PARAM_MODULE, DEFAULT_MODULE);

        log.info("[{}] get_upload_sts: sn={}, module={}", TOOL_NAME, sn, module);

        return sdkService.isMockMode()
                ? getUploadStsInMockMode(sn, filePath, expire, module)
                : getUploadStsWithRealSdk(sn, filePath, expire, module);
    }

    /**
     * Mock 模式下返回 STS 凭证。
     *
     * @param sn       目标设备 SN
     * @param filePath 文件路径
     * @param expire   凭证有效期（秒）
     * @param module   业务模块类型
     * @return 成功响应
     */
    private Map<String, Object> getUploadStsInMockMode(String sn, String filePath, int expire, int module) {
        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("access_key_id", "mock_ak_" + sn);
        credentials.put("access_key_secret", "mock_sk_" + sn);
        credentials.put("expire", expire);
        credentials.put("security_token", "mock_token_" + sn);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bucket", "mock-flighthub-media-bucket");
        data.put("credentials", credentials);
        data.put("object_key_prefix", "media/" + sn + "/");
        data.put("endpoint", "https://mock-storage.dji.com");
        data.put("provider", "mock-provider");
        data.put("region", "mock-region");
        data.put("module", module);
        data.put("file_path", filePath);
        data.put("_mock", true);

        return successResult(data, "Upload STS credentials generated.");
    }

    /**
     * 真实 SDK 模式下获取 STS 凭证。
     *
     * @param sn       目标设备 SN
     * @param filePath 文件路径
     * @param expire   凭证有效期（秒）
     * @param module   业务模块类型
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> getUploadStsWithRealSdk(String sn, String filePath, int expire, int module) {
        try {
            MediaServiceModule msm = sdkService.getWorkspace().getMediaServiceModule();

            GetMediaUploadStsParams params = new GetMediaUploadStsParams(filePath, expire, sn, module);
            CompletableFuture<GetMediaUploadStsResult> future = new CompletableFuture<>();
            msm.getMediaUploadSts(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getMediaUploadSts failed: " + error.getErrorCode()));
                    return;
                }
                future.complete(result);
            });

            GetMediaUploadStsResult result = SdkCallbackBridge.await(future);
            Map<String, Object> data = buildStsResultData(result, module, filePath);

            return successResult(data, "Upload STS credentials generated.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] get_upload_sts SDK error for sn {}: {}", TOOL_NAME, sn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(), "Check sn validity and retry.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] get_upload_sts timeout for sn {}: {}", TOOL_NAME, sn, e.getMessage(), e);
            return errorResult("SDK timeout", "STS credential generation timed out. Try again.");
        } catch (Exception e) {
            log.error("[{}] get_upload_sts error for sn {}: {}", TOOL_NAME, sn, e.getMessage(), e);
            return errorResult("Failed to get upload STS: " + e.getMessage(), "Check sn validity and retry.");
        }
    }

    /**
     * 将 SDK STS 结果转换为响应 data。
     *
     * @param result   SDK 返回结果
     * @param module   业务模块类型
     * @param filePath 文件路径
     * @return data Map
     */
    private static Map<String, Object> buildStsResultData(GetMediaUploadStsResult result, int module,
            String filePath) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("bucket", result.getBucket());
        data.put("credentials", buildCredentialsData(result.getCredentials()));
        data.put("object_key_prefix", result.getObjectKeyPrefix());
        data.put("endpoint", result.getEndpoint());
        data.put("provider", result.getProvider());
        data.put("region", result.getRegion());
        data.put("module", module);
        data.put("file_path", filePath);
        return data;
    }

    /**
     * 将 SDK STS 临时凭证转换为响应 data。
     *
     * @param credentials SDK 返回的凭证
     * @return 凭证 data Map，credentials 为空时返回空 Map
     */
    private static Map<String, Object> buildCredentialsData(MediaUploadStsCredentials credentials) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (credentials == null) {
            return data;
        }
        data.put("access_key_id", credentials.getAccessKeyId());
        data.put("access_key_secret", credentials.getAccessKeySecret());
        data.put("expire", credentials.getExpire());
        data.put("security_token", credentials.getSecurityToken());
        return data;
    }

    // ==================== upload_callback ====================

    /**
     * 处理 upload_callback action：L1 可逆操作，人工确认后分发到 mock/真实 SDK 路径。
     *
     * @param args 输入参数
     * @param sn   目标设备 SN
     * @return 执行结果
     */
    private Map<String, Object> executeUploadCallback(Map<String, Object> args, String sn) {
        Map<String, Object> denied = enforceSecurityLevel(SecurityLevel.REVERSIBLE);
        if (denied != null) {
            return denied;
        }

        int result = getIntArg(args, PARAM_RESULT, Integer.MIN_VALUE);
        if (result == Integer.MIN_VALUE) {
            return errorResult("Missing required parameter: result",
                    "Provide the upload result code. Required for upload_callback.");
        }

        Object fileArg = args != null ? args.get(PARAM_FILE) : null;
        if (!(fileArg instanceof Map)) {
            return errorResult("Missing or invalid required parameter: file",
                    "Provide file as an object with at least object_key, name, type. "
                            + "Required for upload_callback.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> fileMap = (Map<String, Object>) fileArg;

        Map<String, Object> confirmResult = requireHumanConfirm(args, ACTION_UPLOAD_CALLBACK, sn,
                "Report media upload completion for " + sn);
        if (confirmResult != null) {
            return confirmResult;
        }

        log.info("[{}] upload_callback: sn={}, result={}", TOOL_NAME, sn, result);

        return sdkService.isMockMode()
                ? uploadCallbackInMockMode(sn, result, args, fileMap)
                : uploadCallbackWithRealSdk(sn, result, args, fileMap);
    }

    /**
     * Mock 模式下上报上传完成回调。
     *
     * @param sn      目标设备 SN
     * @param result  上传结果码
     * @param args    输入参数
     * @param fileMap 文件信息
     * @return 成功响应
     */
    private Map<String, Object> uploadCallbackInMockMode(String sn, int result,
            Map<String, Object> args, Map<String, Object> fileMap) {
        String objectKey = getStringArg(fileMap, PARAM_FILE_OBJECT_KEY, "");
        String name = getStringArg(fileMap, PARAM_FILE_NAME, "");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("object_key", objectKey);
        data.put("filename", name);
        data.put("flight_id", "mock-flight-" + sn);
        data.put("uuid", "mock-uuid-" + objectKey.hashCode());
        data.put("id", Math.abs(objectKey.hashCode()) % 100000);
        data.put("media_id", Math.abs((objectKey + name).hashCode()) % 100000);
        data.put("_mock", true);

        return successResult(data, "Upload callback reported.");
    }

    /**
     * 真实 SDK 模式下上报上传完成回调。
     *
     * @param sn      目标设备 SN
     * @param result  上传结果码
     * @param args    输入参数
     * @param fileMap 文件信息
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> uploadCallbackWithRealSdk(String sn, int result,
            Map<String, Object> args, Map<String, Object> fileMap) {
        try {
            MediaServiceModule msm = sdkService.getWorkspace().getMediaServiceModule();

            MediaUploadCallbackParams params = buildCallbackParams(sn, result, args, fileMap);
            CompletableFuture<MediaUploadCallbackResult> future = new CompletableFuture<>();
            msm.mediaUploadCallback(params, (error, callbackResult) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "mediaUploadCallback failed: " + error.getErrorCode()));
                    return;
                }
                future.complete(callbackResult);
            });

            MediaUploadCallbackResult callbackResult = SdkCallbackBridge.await(future);
            Map<String, Object> data = buildCallbackResultData(callbackResult);

            return successResult(data, "Upload callback reported.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] upload_callback SDK error for sn {}: {}", TOOL_NAME, sn, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(), "Check parameters and retry.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] upload_callback timeout for sn {}: {}", TOOL_NAME, sn, e.getMessage(), e);
            return errorResult("SDK timeout", "Upload callback reporting timed out. Try again.");
        } catch (Exception e) {
            log.error("[{}] upload_callback error for sn {}: {}", TOOL_NAME, sn, e.getMessage(), e);
            return errorResult("Failed to report upload callback: " + e.getMessage(),
                    "Check parameters and retry.");
        }
    }

    /**
     * 根据输入参数构建 MediaUploadCallbackParams。
     *
     * @param sn      目标设备 SN
     * @param result  上传结果码
     * @param args    输入参数
     * @param fileMap 文件信息
     * @return SDK 请求参数
     */
    private static MediaUploadCallbackParams buildCallbackParams(String sn, int result,
            Map<String, Object> args, Map<String, Object> fileMap) {
        int progress = getIntArg(args, PARAM_PROGRESS, 0);
        int expectedFileCount = getIntArg(args, PARAM_EXPECTED_FILE_COUNT, 0);
        int uploadedFileCount = getIntArg(args, PARAM_UPLOADED_FILE_COUNT, 0);
        int flightType = getIntArg(args, PARAM_FLIGHT_TYPE, 0);
        String bizCode = getStringArg(args, PARAM_BIZ_CODE, "");
        String version = getStringArg(args, PARAM_VERSION, "");

        MediaUploadCallbackFile file = buildFileBean(fileMap);

        return new MediaUploadCallbackParams(sn, result, progress, file,
                expectedFileCount, uploadedFileCount, flightType, bizCode, version);
    }

    /**
     * 根据 file 输入 Map 构建 MediaUploadCallbackFile。
     * ext/metadata 按设计决策不展开扁平参数，此处暂不转换为 SDK 嵌套对象，保持 null。
     *
     * @param fileMap 文件信息 Map
     * @return SDK 文件信息 bean
     */
    private static MediaUploadCallbackFile buildFileBean(Map<String, Object> fileMap) {
        String objectKey = getStringArg(fileMap, PARAM_FILE_OBJECT_KEY, "");
        String fingerprint = getStringArg(fileMap, PARAM_FILE_FINGERPRINT, "");
        String name = getStringArg(fileMap, PARAM_FILE_NAME, "");
        int type = getIntArg(fileMap, PARAM_FILE_TYPE, 0);
        String cloudToCloudId = getStringArg(fileMap, PARAM_FILE_CLOUD_TO_CLOUD_ID, "");
        String createdTime = getStringArg(fileMap, PARAM_FILE_CREATED_TIME, "");
        String source = getStringArg(fileMap, PARAM_FILE_SOURCE, "");

        return new MediaUploadCallbackFile(objectKey, fingerprint, name, type, cloudToCloudId,
                null, null, createdTime, source);
    }

    /**
     * 将 SDK 回调上报结果转换为响应 data。
     *
     * @param result SDK 返回结果
     * @return data Map
     */
    private static Map<String, Object> buildCallbackResultData(MediaUploadCallbackResult result) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("object_key", result.getObjectKey());
        data.put("filename", result.getFilename());
        data.put("flight_id", result.getFlightId());
        data.put("uuid", result.getUuid());
        data.put("id", result.getId());
        data.put("media_id", result.getMediaId());
        return data;
    }
}
