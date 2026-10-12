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
import com.dji.flighthubsdk.core.MediaServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_media_delete — 批量删除媒体文件（不可逆）。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L1（不可逆删除操作，需 confirm=true）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhMediaDeleteTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhMediaDeleteTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_media_delete";

    /** 参数名常量 */
    private static final String PARAM_FILE_IDS = "file_ids";
    private static final String PARAM_CONFIRM = "confirm";

    /** 批量删除上限 */
    private static final int MAX_DELETE_COUNT = 100;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhMediaDeleteTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return true;
    }

    @Override
    public SecurityLevel getSecurityLevel() {
        return SecurityLevel.REVERSIBLE;
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
                "Delete media files by IDs. This action is irreversible. "
                + "Use fh_media_list to find file IDs first.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_FILE_IDS, Map.of(
                "type", "array",
                "items", Map.of("type", "integer"),
                "description", "List of media file IDs to delete (max 100)"
        ));
        props.put(PARAM_CONFIRM, Map.of(
                "type", "boolean",
                "description", "Must be true to confirm deletion (irreversible)"
        ));
        props.put("dry_run", Map.of(
                "type", "boolean",
                "description", "If true, validate parameters and prerequisites without executing. Returns preview of what would happen."
        ));
        schema.put("properties", props);
        schema.put("required", List.of(PARAM_FILE_IDS, PARAM_CONFIRM));
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行媒体文件批量删除。
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

        // 1.5 安全等级硬拦截（session.maxSecurityLevel < L1 时直接拒绝）
        Map<String, Object> denied = enforceToolSecurityLevel();
        if (denied != null) {
            return denied;
        }

        // 2. 校验 workspace
        if (sdkService.getWorkspace() == null) {
            return errorResult("SDK not initialized or workspace unavailable",
                    "Call fh_connect first to initialize.");
        }

        // 3. 解析并校验 file_ids
        List<Long> fileIds = parseFileIds(args);
        if (fileIds == null) {
            return errorResult("Missing or invalid file_ids",
                    "Provide a non-empty array of file IDs (integers).");
        }
        if (fileIds.isEmpty()) {
            return errorResult("file_ids cannot be empty",
                    "Provide at least one file ID to delete.");
        }
        if (fileIds.size() > MAX_DELETE_COUNT) {
            return errorResult("Too many file_ids: " + fileIds.size(),
                    "Maximum " + MAX_DELETE_COUNT + " files per deletion request.");
        }

        // 4. dry_run 模式：参数校验通过，返回预览（无需 confirm）
        if (isDryRun(args)) {
            return dryRunResult(
                    "Would delete " + fileIds.size() + " media file(s)",
                    buildDryRunPreview(TOOL_NAME, "delete_media",
                            fileIds.size() + " files",
                            Map.of("file_ids", fileIds, "count", fileIds.size()),
                            List.of("session_active", "security_level_allowed", "workspace_ready", "params_valid"),
                            getSecurityLevel(), requiresHumanConfirmation()));
        }

        // 5. 人工确认
        Map<String, Object> confirmError = requireHumanConfirm(args, "media_delete",
                String.valueOf(fileIds.size()),
                buildConfirmMessage(fileIds));
        if (confirmError != null) {
            return confirmError;
        }

        // 6. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? deleteInMockMode(fileIds)
                : deleteWithRealSdk(fileIds);
    }

    /**
     * Mock 模式下删除媒体文件。
     */
    private Map<String, Object> deleteInMockMode(List<Long> fileIds) {
        log.info("[MOCK] Media files deleted: count={}, ids={}", fileIds.size(), fileIds);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("deleted_count", fileIds.size());
        data.put("file_ids", fileIds);
        data.put("_mock", true);

        return successResult(data, fileIds.size() + " media file(s) deleted successfully.");
    }

    /**
     * 真实 SDK 模式下删除媒体文件。
     */
    @Generated
    private Map<String, Object> deleteWithRealSdk(List<Long> fileIds) {
        try {
            MediaServiceModule msm = sdkService.getWorkspace().getMediaServiceModule();

            CompletableFuture<Void> future = new CompletableFuture<>();
            msm.deleteMediaFiles(fileIds, (error) -> {
                if (error.isSuccess()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "deleteMediaFiles failed: " + error.getErrorCode()));
                }
            });
            SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deleted_count", fileIds.size());
            data.put("file_ids", fileIds);

            return successResult(data, fileIds.size() + " media file(s) deleted successfully.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_media_delete SDK error: {}", e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Some files may not have been deleted. Verify with fh_media_list.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_media_delete timeout: {}", e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Delete operation timed out. Verify with fh_media_list.");
        } catch (Exception e) {
            log.error("fh_media_delete error: {}", e.getMessage(), e);
            return errorResult("Failed to delete media files: " + e.getMessage(),
                    "Some files may not have been deleted. Verify with fh_media_list.");
        }
    }

    /**
     * 从参数中解析 file_ids 数组。
     *
     * @return 解析成功返回 List<Long>，失败返回 null
     */
    @SuppressWarnings("unchecked")
    private List<Long> parseFileIds(Map<String, Object> args) {
        Object value = args.get(PARAM_FILE_IDS);
        if (value == null) {
            return null;
        }
        if (!(value instanceof List)) {
            return null;
        }
        List<?> rawList = (List<?>) value;
        List<Long> fileIds = new ArrayList<>(rawList.size());
        for (Object item : rawList) {
            if (item == null) {
                return null;
            }
            if (item instanceof Number) {
                fileIds.add(((Number) item).longValue());
            } else {
                try {
                    fileIds.add(Long.parseLong(item.toString().trim()));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return fileIds;
    }

    /**
     * 构建人工确认提示消息。
     */
    private String buildConfirmMessage(List<Long> fileIds) {
        String idsPreview = fileIds.size() <= 3
                ? fileIds.toString()
                : fileIds.subList(0, 3) + "...";
        return String.format("即将删除 %d 个媒体文件（ID: %s），删除后不可恢复，确认？",
                fileIds.size(), idsPreview);
    }
}
