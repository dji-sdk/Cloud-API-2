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
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.core.MediaServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * fh_media_list — 查询媒体文件列表（照片/视频），支持按设备、类型、航线任务、时间范围过滤。
 * 前置条件：已建立 session 且 workspace 可用。
 *
 * <p>安全等级：L0（只读查询，无需 confirm）
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhMediaListTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhMediaListTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_media_list";

    /** 参数名常量 */
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_FILE_TYPE = "file_type";
    private static final String PARAM_FLIGHT_ID = "flight_id";
    private static final String PARAM_START_TIME = "start_time";
    private static final String PARAM_END_TIME = "end_time";
    private static final String PARAM_START_INDEX = "start_index";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 文件类型映射：MCP 友好名 → SDK 编码 */
    private static final Map<String, String> FILE_TYPE_TO_SDK = Map.of(
            "photo", "2",
            "video", "3"
    );

    /** 有效文件类型（含 "all"） */
    private static final List<String> VALID_FILE_TYPES = List.of("photo", "video", "all");

    /** 默认分页参数 */
    private static final int DEFAULT_START_INDEX = 0;
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 500;
    private static final int MIN_PAGE_SIZE = 1;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhMediaListTool(SessionManager sessionManager, SdkService sdkService) {
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
                "List media files (photos/videos) captured by drones. "
                + "Supports filtering by device, file type, flight ID, and time range.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_DEVICE_SN, Map.of(
                "type", "string",
                "description", "Filter by device serial number"
        ));
        props.put(PARAM_FILE_TYPE, Map.of(
                "type", "string",
                "enum", VALID_FILE_TYPES,
                "description", "File type filter: photo, video, or all (default: all)"
        ));
        props.put(PARAM_FLIGHT_ID, Map.of(
                "type", "string",
                "description", "Filter by flight/mission ID"
        ));
        props.put(PARAM_START_TIME, Map.of(
                "type", "string",
                "description", "Start time in ISO 8601 format (e.g. 2025-01-01T00:00:00Z)"
        ));
        props.put(PARAM_END_TIME, Map.of(
                "type", "string",
                "description", "End time in ISO 8601 format (e.g. 2025-01-31T23:59:59Z)"
        ));
        props.put(PARAM_START_INDEX, Map.of(
                "type", "integer",
                "description", "Cursor position for pagination (default: 0)"
        ));
        props.put(PARAM_PAGE_SIZE, Map.of(
                "type", "integer",
                "description", "Number of results per page (default: 50, range: 1-500)"
        ));
        schema.put("properties", props);
        schema.put("required", List.of());
        def.put("inputSchema", schema);
        return def;
    }

    /**
     * 执行媒体文件列表查询。
     *
     * @param args 输入参数
     * @return 执行结果
     */
    @Override
    @SuppressWarnings("unchecked")
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

        // 3. 解析并校验参数
        Object parsed = parseAndValidateParams(args);
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        ListParams params = (ListParams) parsed;

        // 4. 分发到 mock / real 路径
        return sdkService.isMockMode()
                ? listInMockMode(params)
                : listWithRealSdk(params);
    }

    /**
     * 解析并校验列表查询参数。
     * 成功返回 ListParams，失败返回错误 Map。
     */
    private Object parseAndValidateParams(Map<String, Object> args) {
        String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
        String fileType = getStringArg(args, PARAM_FILE_TYPE, "all");
        String flightId = getStringArg(args, PARAM_FLIGHT_ID, null);
        String startTimeStr = getStringArg(args, PARAM_START_TIME, null);
        String endTimeStr = getStringArg(args, PARAM_END_TIME, null);
        int startIndex = getIntArg(args, PARAM_START_INDEX, DEFAULT_START_INDEX);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);

        // 校验 file_type
        if (!VALID_FILE_TYPES.contains(fileType)) {
            return errorResult("Invalid file_type: " + fileType,
                    "Must be one of: " + VALID_FILE_TYPES);
        }

        // 校验分页参数
        if (startIndex < 0) {
            startIndex = DEFAULT_START_INDEX;
        }
        if (pageSize < MIN_PAGE_SIZE || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        // 解析时间参数
        long startTime = parseTimestamp(startTimeStr);
        long endTime = parseTimestamp(endTimeStr);
        if (startTimeStr != null && startTime == Long.MIN_VALUE) {
            return errorResult("Invalid start_time format: " + startTimeStr,
                    "Use ISO 8601 format, e.g. 2025-01-01T00:00:00Z");
        }
        if (endTimeStr != null && endTime == Long.MIN_VALUE) {
            return errorResult("Invalid end_time format: " + endTimeStr,
                    "Use ISO 8601 format, e.g. 2025-01-31T23:59:59Z");
        }

        // 转换文件类型为 SDK 编码列表
        List<String> sdkFileTypes = buildSdkFileTypes(fileType);

        return new ListParams(deviceSn, sdkFileTypes, fileType, flightId,
                startTime, endTime, startIndex, pageSize);
    }

    /**
     * Mock 模式下返回媒体文件列表。
     */
    private Map<String, Object> listInMockMode(ListParams params) {
        log.info("[MOCK] Media list query: deviceSn={}, fileType={}, startIndex={}, pageSize={}",
                params.deviceSn, params.fileTypeDisplay, params.startIndex, params.pageSize);

        List<Map<String, Object>> files = buildMockFiles(params);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("files", files);
        data.put("total_count", 128);
        data.put("start_index", params.startIndex);
        data.put("page_size", params.pageSize);
        data.put("has_more", params.startIndex + files.size() < 128);
        data.put("_mock", true);

        return successResult(data, "Found " + files.size() + " media files.");
    }

    /**
     * 真实 SDK 模式下查询媒体文件列表。
     */
    @Generated
    private Map<String, Object> listWithRealSdk(ListParams params) {
        try {
            MediaServiceModule msm = sdkService.getWorkspace().getMediaServiceModule();

            GetMediaFilesListParams req = new GetMediaFilesListParams();
            req.setDeviceSn(params.deviceSn != null ? params.deviceSn : "");
            req.setFlightId(params.flightId != null ? params.flightId : "");
            req.setFileTypes(params.sdkFileTypes);
            req.setStartIndex(params.startIndex);
            req.setPageSize(params.pageSize);
            req.setStartCreateTime(params.startTime > 0 ? params.startTime : 0);
            req.setEndCreateTime(params.endTime > 0 ? params.endTime : 0);

            CompletableFuture<GetMediaFilesListResults> future = new CompletableFuture<>();
            msm.getMediaFilesList(req, (error, results) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getHttpCode(),
                                    "getMediaFilesList failed: " + error.getErrorCode()));
                    return;
                }
                future.complete(results);
            });

            GetMediaFilesListResults results = SdkCallbackBridge.await(future);
            List<Map<String, Object>> files = mapMediaFileResults(results);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("files", files);
            data.put("total_count", results.getTotal());
            data.put("start_index", params.startIndex);
            data.put("page_size", results.getPageSize());
            data.put("has_more", results.getNextIndex() > 0
                    && results.getNextIndex() < results.getTotal());

            return successResult(data, "Found " + files.size() + " media files.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("fh_media_list SDK error: {}", e.getMessage(), e);
            return errorResult("SDK error: " + e.getMessage(),
                    "Check connectivity and try again.");
        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("fh_media_list timeout: {}", e.getMessage(), e);
            return errorResult("SDK timeout",
                    "Media list query timed out. Try reducing page_size or narrowing filters.");
        } catch (Exception e) {
            log.error("fh_media_list error: {}", e.getMessage(), e);
            return errorResult("Failed to list media files: " + e.getMessage(),
                    "Check connectivity and try again.");
        }
    }

    /**
     * 将 SDK 返回的媒体文件列表映射为 MCP 响应格式。
     */
    private List<Map<String, Object>> mapMediaFileResults(GetMediaFilesListResults results) {
        List<Map<String, Object>> files = new ArrayList<>();
        if (results.getList() == null) {
            return files;
        }
        for (MediaFilesListItemInfoData item : results.getList()) {
            Map<String, Object> file = new LinkedHashMap<>();
            file.put("file_id", item.getFileId());
            file.put("file_name", item.getFileName());
            file.put("file_type", deriveFileType(item.getSuffix()));
            file.put("file_size", item.getSize());
            file.put("device_sn", item.getDeviceSn());
            file.put("flight_id", item.getFlightId());
            file.put("create_time", item.getCreatedAt());
            file.put("object_key", item.getObjectKey());
            files.add(file);
        }
        return files;
    }

    /**
     * 根据文件后缀推断文件类型。
     */
    private String deriveFileType(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return "unknown";
        }
        String lower = suffix.toLowerCase();
        if (lower.endsWith("jpg") || lower.endsWith("jpeg") || lower.endsWith("png")
                || lower.endsWith("dng") || lower.endsWith("tiff")) {
            return "photo";
        }
        if (lower.endsWith("mp4") || lower.endsWith("mov") || lower.endsWith("avi")) {
            return "video";
        }
        return "other";
    }

    /**
     * 构建 mock 文件列表数据。
     */
    private List<Map<String, Object>> buildMockFiles(ListParams params) {
        List<Map<String, Object>> files = new ArrayList<>();
        int count = Math.min(params.pageSize, 3);
        String deviceSn = (params.deviceSn != null) ? params.deviceSn : "DRONE-MEDIA-001";

        for (int i = 0; i < count; i++) {
            Map<String, Object> file = new LinkedHashMap<>();
            long fileId = 10001L + params.startIndex + i;
            boolean isPhoto = (i % 2 == 0);

            file.put("file_id", fileId);
            file.put("file_name", isPhoto
                    ? "DJI_20250101_" + (120000 + i) + ".jpg"
                    : "DJI_20250101_" + (120000 + i) + ".mp4");
            file.put("file_type", isPhoto ? "photo" : "video");
            file.put("file_size", isPhoto ? 5242880L : 104857600L);
            file.put("device_sn", deviceSn);
            file.put("flight_id", "flight_mock_" + (100 + i));
            file.put("create_time", "2025-01-01T12:00:" + String.format("%02d", i) + "Z");
            files.add(file);
        }
        return files;
    }

    /**
     * 解析 ISO 8601 时间字符串为 UNIX 秒。
     * 解析失败返回 Long.MIN_VALUE 作为错误标记。
     * null 输入返回 -1（SDK 中 -1 表示不限制）。
     */
    private long parseTimestamp(String isoTime) {
        if (isoTime == null) {
            return -1;
        }
        try {
            return Instant.parse(isoTime).getEpochSecond();
        } catch (DateTimeParseException e) {
            return Long.MIN_VALUE;
        }
    }

    /**
     * 构建 SDK 层文件类型列表。
     * "all" → 空列表（SDK 空列表表示不过滤）；"photo" → ["2"]；"video" → ["3"]
     */
    private List<String> buildSdkFileTypes(String fileType) {
        if ("all".equals(fileType)) {
            return List.of();
        }
        String sdkCode = FILE_TYPE_TO_SDK.get(fileType);
        return (sdkCode != null) ? List.of(sdkCode) : List.of();
    }

    /** 列表查询参数值对象 */
    private record ListParams(String deviceSn, List<String> sdkFileTypes, String fileTypeDisplay,
                              String flightId, long startTime, long endTime,
                              int startIndex, int pageSize) {
    }
}
