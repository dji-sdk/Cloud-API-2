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

package com.dji.fh.feature.media.spi.sdk;

import com.dji.fh.feature.media.BatchDeleteResponse;
import com.dji.fh.feature.media.DownloadUrlResponse;
import com.dji.fh.feature.media.MediaFileInfo;
import com.dji.fh.feature.media.MediaListResponse;
import com.dji.fh.feature.media.MediaProperties;
import com.dji.fh.feature.media.MediaService;
import com.dji.fh.kernel.spi.util.DateTimeUtils;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.GetMediaFilesDownloadUrlResults;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.core.Workspace;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 媒体文件管理真实 SDK 实现。
 *
 * <p>通过 {@code MediaServiceModule} 调用 FlightHub 平台 API，
 * 替代 {@link MediaService} 中的 mock 实现。
 * 仅在 {@code fh.skeleton.sdk.mode=sdk} 且 SDK JAR 存在时激活。
 *
 * @author DJI DevKit Team
 */
public class SdkMediaService extends MediaService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkMediaService.class);

    /** SDK 文件类型编码：photo=2, video=3 */
    private static final Map<String, String> FILE_TYPE_TO_SDK = Map.of(
            "photo", "2",
            "video", "3"
    );

    /** 下载 URL 默认有效期（秒），SDK 未返回时使用 */
    private static final int DEFAULT_DOWNLOAD_EXPIRES = 3600;

    /**
     * 构造函数。
     *
     * @param props 媒体配置属性
     */
    public SdkMediaService(MediaProperties props) {
        super(props);
    }

    @Override
    public MediaListResponse listFiles(String deviceSn, String fileType,
                                       String startTime, String endTime,
                                       int startIndex, int pageSize) {
        MediaServiceModule msm = requireMediaServiceModule();
        int effectivePageSize = clampPageSize(pageSize);

        GetMediaFilesListParams req = new GetMediaFilesListParams();
        req.setDeviceSn(deviceSn != null ? deviceSn : "");
        req.setFileTypes(buildSdkFileTypes(fileType));
        req.setStartIndex(Math.max(startIndex, 0));
        req.setPageSize(effectivePageSize);
        req.setStartCreateTime(parseStartTimeToEpochSeconds(startTime));
        req.setEndCreateTime(parseEndTimeToEpochSeconds(endTime));

        CompletableFuture<GetMediaFilesListResults> future = new CompletableFuture<>();
        msm.getMediaFilesList(req, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getMediaFilesList 失败: " + error.getErrorCode()));
                return;
            }
            future.complete(results);
        });

        try {
            GetMediaFilesListResults results = SdkBridge.await(future);
            List<MediaFileInfo> files = mapToMediaFileInfoList(results);
            int total = results.getTotal();
            boolean hasMore = results.getNextIndex() > 0 && results.getNextIndex() < total;

            LOG.info("SDK 媒体列表查询完成：total={}, returned={}", total, files.size());
            return new MediaListResponse(files, total, startIndex, effectivePageSize, hasMore);
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 媒体列表查询失败", e);
            throw e;
        }
    }

    @Override
    public DownloadUrlResponse getDownloadUrl(long fileId) {
        MediaServiceModule msm = requireMediaServiceModule();

        CompletableFuture<GetMediaFilesDownloadUrlResults> future = new CompletableFuture<>();
        msm.getMediaFilesDownloadURL(fileId, (error, results) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getMediaFilesDownloadURL 失败: " + error.getErrorCode()));
                return;
            }
            future.complete(results);
        });

        try {
            GetMediaFilesDownloadUrlResults results = SdkBridge.await(future);
            LOG.info("SDK 下载链接获取完成：fileId={}", fileId);
            return new DownloadUrlResponse(fileId, results.getUrl(), DEFAULT_DOWNLOAD_EXPIRES);
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 下载链接获取失败：fileId={}", fileId, e);
            throw e;
        }
    }

    @Override
    public BatchDeleteResponse batchDelete(List<Long> fileIds) {
        MediaServiceModule msm = requireMediaServiceModule();

        CompletableFuture<Void> future = new CompletableFuture<>();
        msm.deleteMediaFiles(fileIds, (error) -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "deleteMediaFiles 失败: " + error.getErrorCode()));
            }
        });

        try {
            SdkBridge.await(future);
            LOG.info("SDK 批量删除完成：count={}", fileIds.size());
            return new BatchDeleteResponse(fileIds.size());
        } catch (SdkBridge.SdkQueryException e) {
            LOG.error("SDK 批量删除失败：count={}", fileIds.size(), e);
            throw e;
        }
    }

    /**
     * 获取 {@link MediaServiceModule}，Workspace 或模块不可用时抛出异常。
     *
     * @return MediaServiceModule 实例（非 null）
     * @throws IllegalStateException SDK 未初始化或模块不可用
     */
    private MediaServiceModule requireMediaServiceModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 不可用，SDK 未初始化");
        }
        MediaServiceModule msm = ws.getMediaServiceModule();
        if (msm == null) {
            throw new IllegalStateException("MediaServiceModule 不可用");
        }
        return msm;
    }

    /**
     * 将 SDK 返回的文件列表映射为 DTO。
     *
     * @param results SDK 查询结果
     * @return MediaFileInfo 列表
     */
    private List<MediaFileInfo> mapToMediaFileInfoList(GetMediaFilesListResults results) {
        if (results.getList() == null) {
            return List.of();
        }
        List<MediaFileInfo> files = new ArrayList<>(results.getList().size());
        boolean firstLogged = false;
        for (MediaFilesListItemInfoData item : results.getList()) {
            long createEpoch = DateTimeUtils.toEpochSeconds(item.getCreatedAt());
            if (!firstLogged) {
                LOG.info("媒体文件时间解析诊断（首条）：rawCreatedAt=[{}], parsedEpoch={}",
                        item.getCreatedAt(), createEpoch);
                firstLogged = true;
            }
            files.add(new MediaFileInfo(
                    item.getFileId(),
                    item.getFileName(),
                    deriveFileType(item.getSuffix()),
                    item.getSize(),
                    item.getDeviceSn(),
                    item.getFlightId(),
                    createEpoch,
                    item.getPayloadType() != null ? item.getPayloadType() : ""
            ));
        }
        return files;
    }

    /**
     * 根据文件后缀推断类型。
     *
     * @param suffix 文件后缀
     * @return photo / video / other
     */
    private String deriveFileType(String suffix) {
        if (suffix == null || suffix.isBlank()) {
            return "other";
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
     * 构建 SDK 层文件类型列表。
     * "all" 或 null → 空列表（SDK 空列表表示不过滤）；"photo" → ["2"]；"video" → ["3"]
     *
     * @param fileType 前端传入的文件类型
     * @return SDK 文件类型编码列表
     */
    private List<String> buildSdkFileTypes(String fileType) {
        if (fileType == null || "all".equals(fileType)) {
            return List.of();
        }
        String sdkCode = FILE_TYPE_TO_SDK.get(fileType);
        return sdkCode != null ? List.of(sdkCode) : List.of();
    }

    /** 一天的秒数，用于纯日期结束时间补齐当天末尾 */
    private static final long SECONDS_PER_DAY = 86400L;

    /**
     * 解析起始时间为 UNIX 秒。null / 空 → 0（SDK 中 0 表示不限制）；
     * 纯日期（yyyy-MM-dd）→ 系统时区当天零点；其余 ISO 8601 → 原样解析。
     *
     * @param time 起始时间字符串
     * @return UNIX 秒时间戳
     */
    private long parseStartTimeToEpochSeconds(String time) {
        return DateTimeUtils.toEpochSeconds(time);
    }

    /**
     * 解析结束时间为 UNIX 秒。与起始时间不同，纯日期（yyyy-MM-dd）表示"当天结束"，
     * 归一化到当天 23:59:59（系统时区），使日期区间为闭区间；其余语义同起始时间。
     *
     * @param time 结束时间字符串
     * @return UNIX 秒时间戳
     */
    private long parseEndTimeToEpochSeconds(String time) {
        long base = DateTimeUtils.toEpochSeconds(time);
        if (base == 0 || isIsoLike(time)) {
            return base;
        }
        // 纯日期：零点 + 一天 - 1 秒 = 当天 23:59:59
        return base + SECONDS_PER_DAY - 1;
    }

    /**
     * 判断输入是否为带时间部分的 ISO 8601 字符串（含日期时间分隔符 'T' 或时间冒号）。
     *
     * @param time 时间字符串
     * @return true 表示完整日期时间格式，false 表示纯日期或其他
     */
    private boolean isIsoLike(String time) {
        return time != null && (time.contains("T") || time.contains(":"));
    }
}
