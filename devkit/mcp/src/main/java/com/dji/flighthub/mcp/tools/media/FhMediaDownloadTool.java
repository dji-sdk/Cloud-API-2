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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlListResults;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults;
import com.dji.flighthubsdk.bean.MediaFileDownloadInfo;
import com.dji.flighthubsdk.core.MediaServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_media_download — 获取媒体文件的预签名下载 URL。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L0（只读，获取链接不等于实际下载）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhMediaDownloadTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhMediaDownloadTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_media_download";

    /** 参数名常量 */
    private static final String PARAM_FILE_ID = "file_id";
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_OBJECT_KEYS = "object_keys";

    /** action 常量 */
    private static final String ACTION_GET_URL = "get_url";
    private static final String ACTION_BATCH_URL = "batch_url";

    /** 有效 action 集合 */
    private static final Set<String> VALID_ACTIONS = Set.of(ACTION_GET_URL, ACTION_BATCH_URL);

    /** 有效 action 排序列表（用于 schema 和错误提示） */
    private static final List<String> VALID_ACTIONS_SORTED = VALID_ACTIONS.stream().sorted().toList();

    /** batch_url 模式下 object_keys 数量下限 */
    private static final int MIN_BATCH_SIZE = 1;

    /** batch_url 模式下 object_keys 数量上限 */
    private static final int MAX_BATCH_SIZE = 100;

    /** 模拟下载 URL 有效时长（秒） */
    private static final long MOCK_URL_VALIDITY_SECONDS = 86400L;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhMediaDownloadTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    /**
     * 返回本工具的 MCP 协议定义。
     *
     * @return MCP 工具定义 Map
     */
    /**
     * 声明工具级静态安全等级：READ_ONLY。
     *
     * @return 静态安全等级
     */
    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.READ_ONLY;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Get pre-signed download URL(s) for media file(s). Two modes via 'action': "
                + "(1) get_url (default): single file lookup by file_id, returns one URL. "
                + "(2) batch_url: multiple files at once by object_keys (1-100 entries), returns one URL per key. "
                + "URL is temporary (typically valid 1-24 hours). Use fh_media_list to find file IDs and object_keys. "
                + "IMPORTANT: batch_url consumes object_key (the object storage key field returned by fh_media_list), "
                + "NOT file_id — passing file_id values as object_keys will fail or return incorrect results. "
                + "IMPORTANT: Response includes suggested_filename — always use it when downloading "
                + "(e.g. curl -o <suggested_filename> '<url>'). "
                + "Direct browser download may produce corrupted filenames with duplicate extensions "
                + "(e.g. .jpegjpeg) due to missing Content-Disposition header on the storage server.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_FILE_ID, Map.of(
                "type", "integer",
                "description", "Media file ID (obtained from fh_media_list). "
                        + "Required when action=get_url (default); ignored when action=batch_url."
        ));
        props.put(PARAM_ACTION, Map.of(
                "type", "string",
                "enum", VALID_ACTIONS_SORTED,
                "description", "Download mode: get_url (default) for a single file by file_id; "
                        + "batch_url for multiple files by object_keys."
        ));
        props.put(PARAM_OBJECT_KEYS, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Object storage keys — the object_key field returned by fh_media_list "
                        + "(NOT file_id). Required when action=batch_url, "
                        + MIN_BATCH_SIZE + "-" + MAX_BATCH_SIZE + " entries."
        ));
        schema.put("properties", props);
        schema.put("required", List.of());
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行下载 URL 获取操作，按 action 分发到单文件或批量模式。
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

        // 3. 校验 action
        String action = getStringArg(args, PARAM_ACTION, ACTION_GET_URL);
        if (!VALID_ACTIONS.contains(action)) {
            return errorResult("Invalid action: " + action,
                    "action must be one of: " + VALID_ACTIONS_SORTED);
        }

        // 4. 分发到单文件 / 批量模式
        return ACTION_BATCH_URL.equals(action) ? executeBatchUrl(args) : executeGetUrl(args);
    }

    /**
     * 单文件下载 URL 模式（action=get_url）。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeGetUrl(Map<String, Object> args) {
        long fileId = getLongArg(args, PARAM_FILE_ID, -1);
        if (fileId <= 0) {
            return errorResult("Missing or invalid file_id",
                    "Provide a valid file_id from fh_media_list results. "
                            + "Required when action=get_url (default).");
        }

        return sdkService.isMockMode()
                ? downloadInMockMode(fileId)
                : downloadWithRealSdk(fileId);
    }

    /**
     * 批量下载 URL 模式（action=batch_url）。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    private Map<String, Object> executeBatchUrl(Map<String, Object> args) {
        List<String> objectKeys = getStringListArg(args, PARAM_OBJECT_KEYS);
        Map<String, Object> validationError = validateObjectKeys(objectKeys);
        if (validationError != null) {
            return validationError;
        }

        return sdkService.isMockMode()
                ? fetchBatchUrlsInMockMode(objectKeys)
                : fetchBatchUrlsWithRealSdk(objectKeys);
    }

    /**
     * 校验 object_keys 参数的必填性与数量范围。
     *
     * @param objectKeys object_key 列表
     * @return 校验失败时返回错误响应，校验通过返回 null
     */
    private Map<String, Object> validateObjectKeys(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return errorResult("Missing or empty object_keys",
                    "Provide " + MIN_BATCH_SIZE + "-" + MAX_BATCH_SIZE
                            + " object_key values from fh_media_list results "
                            + "(the object_key field, not file_id). Required when action=batch_url.");
        }
        if (objectKeys.size() > MAX_BATCH_SIZE) {
            return errorResult("Too many object_keys: " + objectKeys.size(),
                    "Provide at most " + MAX_BATCH_SIZE + " object_key values per request.");
        }
        return null;
    }

    /**
     * Mock 模式下返回预签名下载 URL。
     */
    private Map<String, Object> downloadInMockMode(long fileId) {
        log.info("[MOCK] Media download URL requested: fileId={}", fileId);

        Instant expiration = Instant.now().plusSeconds(MOCK_URL_VALIDITY_SECONDS);
        String mockFilename = "DJI_20260801154829_" + String.format("%04d", fileId) + "_V.jpeg";

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("file_id", fileId);
        data.put("download_url",
                "https://storage.dji.com/media/" + mockFilename + "?token=mock_token_" + fileId
                + "&expires=" + expiration.getEpochSecond());
        data.put("suggested_filename", mockFilename);
        data.put("expiration", expiration.toString());
        data.put("_mock", true);

        return successResult(data,
                "Download URL generated. Use suggested_filename for download: " + mockFilename);
    }

    /**
     * 真实 SDK 模式下获取下载 URL。
     */
    @Generated
    private Map<String, Object> downloadWithRealSdk(long fileId) {
        try {
            MediaServiceModule msm = sdkService.getWorkspace().getMediaServiceModule();

            CompletableFuture<GetMediaFilesDownloadUrlResults> future = new CompletableFuture<>();
            msm.getMediaFilesDownloadURL(fileId, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getMediaFilesDownloadURL failed: " + error.getErrorCode()));
                    return;
                }
                future.complete(results);
            });

            GetMediaFilesDownloadUrlResults results = SdkCallbackBridge.await(future);
            String url = results.getUrl();
            String suggestedFilename = extractFilename(url);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("file_id", fileId);
            data.put("download_url", url);
            data.put("suggested_filename", suggestedFilename);

            return successResult(data,
                    "Download URL generated. Use suggested_filename for download: " + suggestedFilename);
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_media_download SDK error for fileId {}: {}", fileId, e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check file ID validity with fh_media_list.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_media_download timeout for fileId {}: {}", fileId, e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Download URL generation timed out. Try again.");
        } catch (Exception e) {
            log.error("fh_media_download error for fileId {}: {}", fileId, e.getMessage(), e);
            return errorResult("Failed to get download URL: " + e.getMessage(),
                    "Check file ID validity with fh_media_list.");
        }
    }

    /**
     * Mock 模式下为一批 object_key 生成预签名下载 URL。
     *
     * @param objectKeys object_key 列表
     * @return 执行结果
     */
    private Map<String, Object> fetchBatchUrlsInMockMode(List<String> objectKeys) {
        log.info("[MOCK] Batch media download URL requested: count={}", objectKeys.size());

        List<Map<String, Object>> files = new ArrayList<>();
        for (String objectKey : objectKeys) {
            Instant expiration = Instant.now().plusSeconds(MOCK_URL_VALIDITY_SECONDS);
            String mockFilename = deriveMockFilename(objectKey);

            Map<String, Object> file = new LinkedHashMap<>();
            file.put("object_key", objectKey);
            file.put("download_url",
                    "https://storage.dji.com/media/" + mockFilename + "?token=mock_token_batch"
                    + "&expires=" + expiration.getEpochSecond());
            file.put("suggested_filename", mockFilename);
            files.add(file);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("action", ACTION_BATCH_URL);
        data.put("count", files.size());
        data.put("files", files);
        data.put("_mock", true);

        return successResult(data, "Generated " + files.size() + " download URL(s).");
    }

    /**
     * 真实 SDK 模式下为一批 object_key 获取预签名下载 URL。
     *
     * @param objectKeys object_key 列表
     * @return 执行结果
     */
    @Generated
    private Map<String, Object> fetchBatchUrlsWithRealSdk(List<String> objectKeys) {
        try {
            MediaServiceModule msm = sdkService.getWorkspace().getMediaServiceModule();

            CompletableFuture<GetMediaFilesDownloadUrlListResults> future = new CompletableFuture<>();
            msm.getMediaFilesDownloadURL(objectKeys, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getMediaFilesDownloadURL (batch) failed: " + error.getErrorCode()));
                    return;
                }
                future.complete(results);
            });

            GetMediaFilesDownloadUrlListResults results = SdkCallbackBridge.await(future);
            List<Map<String, Object>> files = buildBatchFileEntries(results);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("action", ACTION_BATCH_URL);
            data.put("count", files.size());
            data.put("files", files);

            return successResult(data, "Generated " + files.size() + " download URL(s).");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_media_download batch SDK error for {} keys: {}",
                    objectKeys.size(), e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check object_key values with fh_media_list.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_media_download batch timeout for {} keys: {}",
                    objectKeys.size(), e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Batch download URL generation timed out. Try again.");
        } catch (Exception e) {
            log.error("fh_media_download batch error for {} keys: {}",
                    objectKeys.size(), e.getMessage(), e);
            return errorResult("Failed to get batch download URLs: " + e.getMessage(),
                    "Check object_key values with fh_media_list.");
        }
    }

    /**
     * 将 SDK 批量下载结果转换为响应中的 files 列表。
     * URL 为空时记录错误说明，避免 NPE。
     *
     * @param results SDK 批量下载结果
     * @return files 列表
     */
    private static List<Map<String, Object>> buildBatchFileEntries(
            GetMediaFilesDownloadUrlListResults results) {
        List<Map<String, Object>> files = new ArrayList<>();
        List<MediaFileDownloadInfo> infos = results.getList();
        if (infos == null) {
            return files;
        }

        for (MediaFileDownloadInfo info : infos) {
            Map<String, Object> file = new LinkedHashMap<>();
            String objectKey = info.getObjectKey();
            String url = info.getUrl();
            file.put("object_key", objectKey);
            if (url == null || url.isBlank()) {
                file.put("error", "No download URL returned for this object_key");
            } else {
                file.put("download_url", url);
                file.put("suggested_filename", extractFilename(url));
            }
            files.add(file);
        }
        return files;
    }

    /**
     * 从 object_key 中派生出 mock 文件名（取最后一段路径）。
     * object_key 示例：media/2026/08/01/xxx.jpeg
     *
     * @param objectKey object_key
     * @return 派生出的文件名
     */
    private static String deriveMockFilename(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return objectKey;
        }
        int lastSlash = objectKey.lastIndexOf('/');
        return (lastSlash >= 0 && lastSlash < objectKey.length() - 1)
                ? objectKey.substring(lastSlash + 1)
                : objectKey;
    }

    /**
     * 从下载 URL 中提取文件名（去掉 query 参数）。
     * URL 示例：https://host/path/.../DJI_20260801154829_0001_V.jpeg?X-Amz-Algorithm=...
     *
     * @param url 下载 URL
     * @return 提取到的文件名，无法解析时返回 null
     */
    static String extractFilename(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            String path = URI.create(url).getPath();
            if (path == null || path.isEmpty()) {
                return null;
            }
            int lastSlash = path.lastIndexOf('/');
            return (lastSlash >= 0 && lastSlash < path.length() - 1)
                    ? path.substring(lastSlash + 1)
                    : path;
        } catch (Exception e) {
            log.debug("fh_media_download 无法从 URL 提取文件名: {}", url);
            return null;
        }
    }

}
