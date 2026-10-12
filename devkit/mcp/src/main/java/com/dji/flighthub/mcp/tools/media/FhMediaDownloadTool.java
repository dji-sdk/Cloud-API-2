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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults;
import com.dji.flighthubsdk.core.MediaServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Get a pre-signed download URL for a media file. "
                + "URL is temporary (typically valid 1-24 hours). Use fh_media_list to find file IDs. "
                + "IMPORTANT: Response includes suggested_filename — always use it when downloading "
                + "(e.g. curl -o <suggested_filename> '<url>'). "
                + "Direct browser download may produce corrupted filenames with duplicate extensions "
                + "(e.g. .jpegjpeg) due to missing Content-Disposition header on the storage server.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_FILE_ID, Map.of(
                "type", "integer",
                "description", "Media file ID (obtained from fh_media_list)"
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_FILE_ID));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行下载 URL 获取操作。
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

        // 3. 校验 file_id
        long fileId = getLongArg(args, PARAM_FILE_ID, -1);
        if (fileId <= 0) {
            return errorResult("Missing or invalid file_id",
                    "Provide a valid file_id from fh_media_list results.");
        }

        // 4. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? downloadInMockMode(fileId)
                : downloadWithRealSdk(fileId);
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
