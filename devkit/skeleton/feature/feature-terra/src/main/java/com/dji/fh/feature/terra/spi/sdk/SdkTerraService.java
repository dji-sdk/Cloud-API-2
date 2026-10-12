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

package com.dji.fh.feature.terra.spi.sdk;

import com.dji.fh.feature.terra.MediaUrlCache;
import com.dji.fh.kernel.spi.util.DateTimeUtils;
import com.dji.fh.feature.terra.TerraService;
import com.dji.fh.feature.terra.dto.CreatePanoramaRequest;
import com.dji.fh.feature.terra.dto.CreateReconstructionRequest;
import com.dji.fh.feature.terra.dto.FolderDto;
import com.dji.fh.feature.terra.dto.ImageDto;
import com.dji.fh.feature.terra.dto.ModelDetailDto;
import com.dji.fh.feature.terra.dto.ModelFileDto;
import com.dji.fh.feature.terra.dto.ModelUrlDto;
import com.dji.fh.feature.terra.dto.QualityReportDto;
import com.dji.fh.feature.terra.dto.ReconstructionTaskDto;
import com.dji.fh.feature.terra.dto.SfmResultDto;
import com.dji.fh.feature.terra.dto.TaskStatusDto;
import com.dji.fh.feature.terra.dto.TileStorageInfo;
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.bean.GetMediaFileDetailByIdResult;
import com.dji.flighthubsdk.bean.GetMediaFilesListParams;
import com.dji.flighthubsdk.bean.GetMediaFilesListResults;
import com.dji.flighthubsdk.bean.MediaFilesListItemInfoData;
import com.dji.flighthubsdk.bean.TerraBuildCreatePanoramaResult;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionParams;
import com.dji.flighthubsdk.bean.TerraBuildCreateReconstructionResult;
import com.dji.flighthubsdk.bean.TerraBuildModelFileItem;
import com.dji.flighthubsdk.bean.TerraBuildModelFileListResult;
import com.dji.flighthubsdk.bean.TerraBuildQualityReportResult;
import com.dji.flighthubsdk.bean.TerraModelDetailResult;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionImageItem;
import com.dji.flighthubsdk.bean.TerraBuildReconstructionImagesResult;
import com.dji.flighthubsdk.bean.TerraBuildSettingsResult;
import com.dji.flighthubsdk.bean.TerraBuildSfmResult;
import com.dji.flighthubsdk.core.MediaServiceModule;
import com.dji.flighthubsdk.core.TerraBuildModule;
import com.dji.flighthubsdk.core.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 真实 SDK 实现 — 通过 {@link TerraBuildModule} 调用 FlightHub 平台 API。
 *
 * <p>仅在 {@code fh.skeleton.sdk.mode=sdk} 且 SDK 类存在于 classpath 时激活。
 *
 * @author DJI DevKit Team
 */
public class SdkTerraService implements TerraService {

    private static final Logger LOG = LoggerFactory.getLogger(SdkTerraService.class);

    /** 媒体代理 URL 前缀. */
    private static final String MEDIA_PROXY_PREFIX = "/api/v1/terra/media";

    private final MediaUrlCache mediaUrlCache;

    /**
     * SDK 重建类型映射（create 方向）：前端字符串 → SDK int。
     * SDK 枚举：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
     */
    private static final Map<String, Integer> RECONSTRUCTION_TYPE_TO_SDK = Map.of(
            "ortho_2d", 2,
            "mesh_3d", 3,
            "point_cloud", 3,
            "panorama", 6
    );

    /**
     * SDK 重建类型映射（read 方向）：SDK int → 前端字符串。
     * 基于 SDK Javadoc 和线上实际返回值验证。
     */
    private static final Map<Integer, String> RECONSTRUCTION_TYPE_FROM_SDK = Map.of(
            2, "ortho_2d",
            3, "mesh_3d",
            4, "3dgs",
            6, "panorama",
            7, "lidar"
    );

    /** 默认质量报告度量单位 */
    private static final String DEFAULT_MEASURE_UNIT = "metric";

    /** MinIO 桶路径段（用于 URL 解析） */
    private static final String BUCKET_PATH = "/file-storage-privatization/";

    /** 预签名 URL 过期时间（秒） */
    private static final long URL_EXPIRY_SECONDS = 3600L;

    /** 遍历媒体文件时每页数量. */
    private static final int MEDIA_SCAN_PAGE_SIZE = 100;

    /** 遍历媒体文件的最大页数，避免媒体过多时阻塞. */
    private static final int MEDIA_SCAN_MAX_PAGES = 200;

    /** 文件夹 id 纯数字校验正则（跳过 wayline-* / 多层路径 / 特殊值）. */
    private static final String FOLDER_ID_PATTERN = "\\d+";

    private final FolderListCache folderListCache;

    /**
     * 构造 SdkTerraService.
     *
     * @param mediaUrlCache 媒体 URL 缓存
     */
    public SdkTerraService(final MediaUrlCache mediaUrlCache) {
        this(mediaUrlCache, new FolderListCache());
    }

    /**
     * 构造 SdkTerraService（注入文件夹缓存，测试用）.
     *
     * @param mediaUrlCache    媒体 URL 缓存
     * @param folderListCache  文件夹列表缓存
     */
    public SdkTerraService(final MediaUrlCache mediaUrlCache,
                           final FolderListCache folderListCache) {
        this.mediaUrlCache = mediaUrlCache;
        this.folderListCache = folderListCache;
    }

    @Override
    public PageResult<ModelFileDto> listModels(int page, int pageSize, String type) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraBuildModelFileListResult> future = new CompletableFuture<>();
        tbm.getModelFileList(page, pageSize, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getModelFileList 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildModelFileListResult result = SdkBridge.await(future);
        List<ModelFileDto> models = mapModelFileList(result.getList());

        // SDK 不支持按 type 服务端过滤，客户端过滤后需同步修正 total
        long total = result.getTotal();
        if (type != null && !type.isBlank()) {
            models = models.stream()
                    .filter(m -> type.equals(m.reconstructionType()))
                    .toList();
            total = models.size();
        }

        LOG.info("SDK 模型列表查询完成：total={}, filtered={}, page={}", result.getTotal(), total, result.getPage());
        return new PageResult<>(models, total, result.getPage(), result.getPageSize());
    }

    @Override
    public ModelDetailDto getModelDetail(long fileId) {
        TerraModelDetailResult detail = fetchModelDetail(fileId);
        LOG.info("SDK 模型详情获取完成：fileId={}, name={}", fileId, detail.getName());
        return mapModelDetail(detail);
    }

    @Override
    public ModelUrlDto getModelUrl(long fileId) {
        // SDK getModelFileUrl 返回的 key 为纯数字 ID，MinIO 中不存在对应对象；
        // 改为通过 getModelDetail 获取实际的预签名下载链接（url 字段）
        TerraModelDetailResult detail = fetchModelDetail(fileId);
        LOG.info("SDK 模型下载链接获取完成：fileId={}, url长度={}", fileId,
                detail.getUrl() != null ? detail.getUrl().length() : 0);
        return new ModelUrlDto(detail.getUrl(), Instant.now().plusSeconds(URL_EXPIRY_SECONDS));
    }

    @Override
    public TileStorageInfo getTileStorageInfo(long fileId) {
        TerraModelDetailResult detail = fetchModelDetail(fileId);
        String url = detail.getUrl();
        int bucketEnd = url.indexOf(BUCKET_PATH);
        if (bucketEnd < 0) {
            throw new IllegalStateException("SDK url 不含桶路径段: " + url.substring(0, Math.min(url.length(), 60)));
        }
        String storageBase = url.substring(0, bucketEnd + BUCKET_PATH.length());
        String filePrefix = detail.getFilePrefix();
        if (filePrefix == null) {
            filePrefix = "";
        }
        LOG.info("瓦片存储信息：fileId={}, storageBase={}..., prefix={}", fileId,
                storageBase.substring(0, Math.min(storageBase.length(), 40)), filePrefix);
        return new TileStorageInfo(storageBase, filePrefix);
    }

    @Override
    public void deleteModel(long fileId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<Void> future = new CompletableFuture<>();
        tbm.deleteModelFile((int) fileId, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "deleteModelFile 失败: code=" + error.getErrorCode()));
            }
        });

        SdkBridge.awaitVoid(future);
        LOG.info("SDK 模型删除完成：fileId={}", fileId);
    }

    @Override
    public void batchDeleteModels(List<Long> fileIds) {
        TerraBuildModule tbm = requireTerraBuildModule();

        List<Integer> ids = fileIds.stream().map(Long::intValue).toList();
        CompletableFuture<Void> future = new CompletableFuture<>();
        tbm.batchDeleteModelFiles(ids, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "batchDeleteModelFiles 失败: code=" + error.getErrorCode()));
            }
        });

        SdkBridge.awaitVoid(future);
        LOG.info("SDK 批量删除模型完成：count={}", fileIds.size());
    }

    @Override
    public ReconstructionTaskDto createReconstruction(CreateReconstructionRequest request) {
        TerraBuildModule tbm = requireTerraBuildModule();

        TerraBuildCreateReconstructionParams params = new TerraBuildCreateReconstructionParams();
        params.setName(request.name());
        // 优先使用数字文件夹 id（按文件夹选择），否则回退旧版 UUID（按航线/手动输入）
        if (request.folderIds() != null && !request.folderIds().isEmpty()) {
            params.setFolderIds(request.folderIds());
        } else {
            params.setFolderUuids(request.folderUuids());
        }
        params.setReconstructionType(mapReconstructionTypeToSdk(request.reconstructionType()));
        if (request.wkt() != null && !request.wkt().isBlank()) {
            params.setWkt(request.wkt());
        }

        CompletableFuture<TerraBuildCreateReconstructionResult> future = new CompletableFuture<>();
        tbm.createReconstruction(params, (error, result) -> {
            if (!error.isSuccess()) {
                // 透传平台业务错误码与提示（如 222001 输入图像太少），便于前端展示
                String serverCode = String.valueOf(error.getServerCode());
                String serverMsg = error.getServerMessage();
                String detail = serverMsg != null && !serverMsg.isBlank()
                        ? "（" + serverCode + " " + serverMsg + "）"
                        : "（code=" + error.getErrorCode() + "）";
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "createReconstruction 失败: " + detail));
                return;
            }
            future.complete(result);
        });

        TerraBuildCreateReconstructionResult result = SdkBridge.await(future);
        // 新建重建任务会改变文件夹归属（folderList 引用新 model），主动失效缓存
        folderListCache.invalidate();
        LOG.info("SDK 重建任务创建完成：id={}, uuid={}, job_uuid={}",
                result.getId(), result.getUuid(), result.getJobUuid());
        return new ReconstructionTaskDto(
                result.getId(), result.getUuid(), result.getJobUuid(), "processing");
    }

    @Override
    public ReconstructionTaskDto createPanorama(CreatePanoramaRequest request) {
        TerraBuildModule tbm = requireTerraBuildModule();

        String wkt = request.wkt() != null ? request.wkt() : "";

        CompletableFuture<TerraBuildCreatePanoramaResult> future = new CompletableFuture<>();
        tbm.createPanorama(request.name(), request.folderUuids(), wkt, true, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "createPanorama 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildCreatePanoramaResult result = SdkBridge.await(future);
        LOG.info("SDK 全景任务创建完成：id={}, uuid={}, job_uuid={}",
                result.getId(), result.getUuid(), result.getJobUuid());
        return new ReconstructionTaskDto(
                result.getId(), result.getUuid(), result.getJobUuid(), "processing");
    }

    @Override
    public TaskStatusDto getTaskStatus(String taskId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraBuildSettingsResult> future = new CompletableFuture<>();
        tbm.getReconstructionSettings(taskId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getReconstructionSettings 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildSettingsResult result = SdkBridge.await(future);
        int stateCode = result.getState();
        String state = String.valueOf(stateCode);

        LOG.info("SDK 任务状态查询完成：taskId={}, state={}", taskId, stateCode);
        return new TaskStatusDto(
                taskId, state, stateCode,
                result.getMediaInputObjectKey(),
                result.getSfmOutputObjectPrefix());
    }

    @Override
    public void retryTask(String taskId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<Void> future = new CompletableFuture<>();
        tbm.retryReconstruction(taskId, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "retryReconstruction 失败: code=" + error.getErrorCode()));
            }
        });

        SdkBridge.awaitVoid(future);
        LOG.info("SDK 重建任务重试完成：taskId={}", taskId);
    }

    @Override
    public void stopTask(String taskId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<Void> future = new CompletableFuture<>();
        tbm.stopReconstruction(taskId, error -> {
            if (error.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "stopReconstruction 失败: code=" + error.getErrorCode()));
            }
        });

        SdkBridge.awaitVoid(future);
        LOG.info("SDK 重建任务停止完成：taskId={}", taskId);
    }

    @Override
    public QualityReportDto getQualityReport(String taskId, String language) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraBuildQualityReportResult> future = new CompletableFuture<>();
        tbm.getQualityReport(taskId, language, DEFAULT_MEASURE_UNIT, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getQualityReport 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildQualityReportResult result = SdkBridge.await(future);
        LOG.info("SDK 质量报告获取完成：taskId={}", taskId);
        String rawUrl = result.getQualityReportUrl();
        mediaUrlCache.put("report:" + taskId, rawUrl);
        String proxyUrl = rawUrl != null && !rawUrl.isBlank()
                ? MEDIA_PROXY_PREFIX + "/report/" + taskId
                : rawUrl;
        return new QualityReportDto(proxyUrl);
    }

    @Override
    public SfmResultDto getSfmResult(String taskId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraBuildSfmResult> future = new CompletableFuture<>();
        tbm.getSfmResult(taskId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getSfmResult 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildSfmResult result = SdkBridge.await(future);
        LOG.info("SDK SfM 结果获取完成：taskId={}", taskId);
        return new SfmResultDto(
                result.getSfmJsonUrl(), 0, 0L, 0.0);
    }

    @Override
    public List<ImageDto> getReconstructionImages(String taskId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraBuildReconstructionImagesResult> future = new CompletableFuture<>();
        tbm.getReconstructionImages(taskId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getReconstructionImages 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildReconstructionImagesResult result = SdkBridge.await(future);
        LOG.info("SDK 重建影像列表获取完成：taskId={}, total={}", taskId, result.getTotal());
        return mapImageList(taskId, result.getList());
    }

    @Override
    public PageResult<TaskStatusDto> listTasks(int page, int pageSize) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraBuildModelFileListResult> future = new CompletableFuture<>();
        tbm.getModelFileList(page, pageSize, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getModelFileList 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        TerraBuildModelFileListResult result = SdkBridge.await(future);
        List<TaskStatusDto> tasks = mapTaskList(result.getList());
        LOG.info("SDK 任务列表查询完成：total={}, page={}, returned={}",
                result.getTotal(), result.getPage(), tasks.size());
        return new PageResult<>(tasks, result.getTotal(), result.getPage(), result.getPageSize());
    }

    /**
     * 将 SDK 模型文件列表映射为任务状态视图.
     *
     * <p>SDK 中"模型文件"即"重建任务"，每个 file 对应一次重建。
     * state 字段直接透传 SDK 数值（前端按需映射展示）。
     *
     * @param items SDK 模型文件列表
     * @return 任务状态 DTO 列表
     */
    private List<TaskStatusDto> mapTaskList(List<TerraBuildModelFileItem> items) {
        if (items == null) {
            return List.of();
        }
        List<TaskStatusDto> result = new ArrayList<>(items.size());
        for (TerraBuildModelFileItem item : items) {
            String taskUuid = item.getUuid();
            if (taskUuid == null || taskUuid.isBlank()) {
                continue;
            }
            int stateCode = item.getState();
            result.add(new TaskStatusDto(
                    taskUuid,
                    mapTaskState(stateCode),
                    stateCode,
                    "",
                    ""));
        }
        return result;
    }

    @Override
    public List<FolderDto> listFolders() {
        // 文件夹列表变更频率低，短 TTL 缓存避免每次都 N+1 次 SDK 调用
        Optional<List<FolderDto>> cached = folderListCache.get();
        if (cached.isPresent()) {
            LOG.debug("文件夹列表命中缓存");
            return cached.get();
        }
        List<FolderDto> folders = aggregateFolders();
        folderListCache.put(folders);
        return folders;
    }

    /**
     * 遍历媒体文件聚合文件夹列表.
     *
     * <p>媒体文件 {@code MediaFilesListItemInfoData.filePath} 字段即其所属文件夹的数字 id。
     * 遍历 {@code getMediaFilesList} 分页结果收集纯数字 filePath（跳过 wayline-* / 多层路径 /
     * 特殊值），再对每个 folderId 调 {@code getMediaFileDetailById} 获取文件夹名称。
     *
     * @return 按 folderName 排序的去重文件夹列表
     */
    private List<FolderDto> aggregateFolders() {
        MediaServiceModule msm = requireMediaServiceModule();

        // 按 folderId 去重，先收集 id，再逐个取名称
        Set<Integer> folderIds = new LinkedHashSet<>();
        int scanned = 0;
        int startIndex = 0;
        while (scanned < MEDIA_SCAN_MAX_PAGES * MEDIA_SCAN_PAGE_SIZE) {
            GetMediaFilesListParams req = new GetMediaFilesListParams();
            req.setStartIndex(startIndex);
            req.setPageSize(MEDIA_SCAN_PAGE_SIZE);

            CompletableFuture<GetMediaFilesListResults> future = new CompletableFuture<>();
            msm.getMediaFilesList(req, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(new SdkBridge.SdkQueryException(
                            "getMediaFilesList 失败: code=" + error.getErrorCode()));
                    return;
                }
                future.complete(result);
            });

            GetMediaFilesListResults pageResult;
            try {
                pageResult = SdkBridge.await(future);
            } catch (SdkBridge.SdkQueryException e) {
                LOG.warn("聚合文件夹时媒体列表查询失败，已扫描 {} 条：{}", scanned, e.getMessage());
                break;
            }

            List<MediaFilesListItemInfoData> items = pageResult.getList();
            if (items == null || items.isEmpty()) {
                break;
            }

            for (MediaFilesListItemInfoData item : items) {
                String filePath = item.getFilePath();
                if (filePath == null || !filePath.matches(FOLDER_ID_PATTERN)) {
                    continue;
                }
                try {
                    folderIds.add(Integer.valueOf(filePath));
                } catch (NumberFormatException e) {
                    // 纯数字但超出 int 范围（异常数据），跳过避免整个列表接口 500
                    LOG.debug("文件夹 id 超出 int 范围，跳过：{}", filePath);
                }
            }

            scanned += items.size();
            // 用累计扫描条数作为下一页 startIndex（防御式），并以 nextIndex/末页判断结束
            int nextIndex = pageResult.getNextIndex();
            if (nextIndex <= 0 || nextIndex >= pageResult.getTotal()) {
                break;
            }
            startIndex = Math.max(nextIndex, scanned);
        }

        List<FolderDto> folders = new ArrayList<>(folderIds.size());
        for (Integer folderId : folderIds) {
            String name = fetchFolderName(msm, folderId);
            folders.add(new FolderDto(folderId, null, name));
        }
        folders.sort(Comparator.comparing(
                FolderDto::folderName,
                Comparator.nullsLast(Comparator.naturalOrder())));
        LOG.info("聚合文件夹完成：scanned={}, folders={}", scanned, folders.size());
        return folders;
    }

    /**
     * 查询单个文件夹的名称（folderUuid 无法从媒体层获取，置 null）.
     *
     * @param msm      MediaServiceModule 实例
     * @param folderId 文件夹 id
     * @return 文件夹名称；查询失败时返回 null
     */
    private String fetchFolderName(MediaServiceModule msm, int folderId) {
        CompletableFuture<GetMediaFileDetailByIdResult> future = new CompletableFuture<>();
        msm.getMediaFileDetailById(folderId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getMediaFileDetailById 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });

        try {
            return SdkBridge.await(future).getName();
        } catch (SdkBridge.SdkQueryException e) {
            LOG.debug("文件夹 {} 详情查询失败，name 置空：{}", folderId, e.getMessage());
            return null;
        }
    }

    // ── 私有方法 ──

    /**
     * 公共辅助：通过 SDK 获取模型详情原始结果。
     * 被 {@code getModelDetail}、{@code getModelUrl}、{@code getTileStorageInfo} 复用。
     */
    private TerraModelDetailResult fetchModelDetail(long fileId) {
        TerraBuildModule tbm = requireTerraBuildModule();

        CompletableFuture<TerraModelDetailResult> future = new CompletableFuture<>();
        tbm.getModelDetail((int) fileId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getModelDetail 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });
        return SdkBridge.await(future);
    }

    /**
     * 获取 {@link TerraBuildModule}，不可用时抛出异常。
     */
    TerraBuildModule requireTerraBuildModule() {
        Workspace ws = FlightHubSDK.getWorkspaceInstance();
        if (ws == null) {
            throw new IllegalStateException("Workspace 不可用，SDK 未初始化");
        }
        TerraBuildModule tbm = ws.getTerraBuildModule();
        if (tbm == null) {
            throw new IllegalStateException("TerraBuildModule 不可用");
        }
        return tbm;
    }

    /**
     * 获取 {@link MediaServiceModule}，不可用时抛出异常。
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
     * 将 SDK 模型文件列表转换为 DTO 列表。
     */
    private List<ModelFileDto> mapModelFileList(List<TerraBuildModelFileItem> items) {
        if (items == null) {
            return List.of();
        }
        List<ModelFileDto> result = new ArrayList<>(items.size());
        for (TerraBuildModelFileItem item : items) {
            String thumbProxy = cacheAndProxy("thumb", item.getId(), item.getThumbnailUrl());
            result.add(new ModelFileDto(
                    item.getId(),
                    item.getUuid(),
                    item.getName(),
                    mapFileType(item.getFileType()),
                    String.valueOf(item.getSubFileType()),
                    mapState(item.getState()),
                    mapReconstructionType(item.getReconstructionType()),
                    thumbProxy,
                    parseCreatedAt(item.getCreatedAt())));
        }
        return result;
    }

    /**
     * 将 SDK 影像列表转换为 DTO 列表。
     */
    private List<ImageDto> mapImageList(String taskId, List<TerraBuildReconstructionImageItem> items) {
        if (items == null) {
            return List.of();
        }
        List<ImageDto> result = new ArrayList<>(items.size());
        for (TerraBuildReconstructionImageItem item : items) {
            String rawUrl = item.getOriginImageUrl();
            if (rawUrl == null || rawUrl.isEmpty()) {
                rawUrl = item.getThumbnailImageUrl();
            }
            String cacheKey = "image:" + taskId + ":" + item.getImageId();
            mediaUrlCache.put(cacheKey, rawUrl);
            String proxyUrl = rawUrl != null && !rawUrl.isBlank()
                    ? MEDIA_PROXY_PREFIX + "/image/" + taskId + "/" + item.getImageId()
                    : null;
            result.add(new ImageDto(
                    item.getImageId(),
                    item.getFileName(),
                    proxyUrl));
        }
        return result;
    }

    /**
     * SDK fileType int → 字符串透传（与 MCP FhTerraTool 保持一致，不做猜测映射）。
     */
    private String mapFileType(int fileType) {
        return String.valueOf(fileType);
    }

    /**
     * SDK state int → 字符串透传（SDK 无公开枚举常量，直接透传 int 避免映射错误）。
     */
    private String mapState(int state) {
        return String.valueOf(state);
    }

    /**
     * SDK state int → 前端语义状态（用于任务列表卡片展示）.
     *
     * <p>SDK state 枚举：0=未知, 1-7=解压/格式转换中, 8=建图失败, 9=模型导入成功,
     * 10-13=校验/等待资源, 14=重建中, 15=重建成功, 16=重建取消, 17=等待图片上传,
     * 119-126=空三各阶段.
     *
     * @param state SDK state 数值
     * @return processing / success / failed
     */
    private String mapTaskState(int state) {
        if (state == 15 || state == 9) {
            return "success";
        }
        if (state == 8 || state == 3 || state == 7 || state == 11 || state == 13
                || state == 119 || state == 122 || state == 125) {
            return "failed";
        }
        return "processing";
    }

    /**
     * SDK reconstructionType List&lt;Integer&gt; → 前端字符串（取第一个值映射）。
     * 前端使用文本进行类型筛选，需映射为一致的文本值。
     */
    private String mapReconstructionType(List<Integer> types) {
        if (types == null || types.isEmpty()) {
            return "";
        }
        return RECONSTRUCTION_TYPE_FROM_SDK.getOrDefault(types.get(0), String.valueOf(types.get(0)));
    }

    /**
     * 字符串重建类型 → SDK 整数列表。
     * 基于 SDK Javadoc：2=二维, 3=三维, 4=3DGS RGB, 6=全景, 7=激光雷达
     */
    private List<Integer> mapReconstructionTypeToSdk(String type) {
        if (type == null) {
            return List.of(3);
        }
        int code = RECONSTRUCTION_TYPE_TO_SDK.getOrDefault(type, 3);
        return List.of(code);
    }

    /**
     * 解析创建时间字符串为 Unix 秒。
     * SDK 可能返回空字符串、UNIX 秒字符串、ISO 格式或纯日期，统一交给 {@link DateTimeUtils}。
     */
    private long parseCreatedAt(String createdAt) {
        return DateTimeUtils.toEpochSeconds(createdAt);
    }

    /**
     * SDK 模型详情 → DTO 映射。
     */
    private ModelDetailDto mapModelDetail(TerraModelDetailResult d) {
        String reconstructionType = RECONSTRUCTION_TYPE_FROM_SDK.getOrDefault(
                d.getSubFileType(), "mesh_3d");
        long createdAtEpoch = d.getCreateAt() > 0 ? d.getCreateAt() / 1000 : 0;
        String thumbProxy = cacheAndProxy("thumb", d.getId(), d.getThumbnailUrl());
        String previewProxy = cacheAndProxy("preview", d.getId(), d.getPreviewUrl());
        return new ModelDetailDto(
                d.getId(),
                d.getUuid(),
                d.getName(),
                reconstructionType,
                buildTileProxyUrl(d),
                thumbProxy,
                previewProxy,
                d.getLatitude(),
                d.getLongitude(),
                d.getAltitude(),
                d.getTransform(),
                createdAtEpoch
        );
    }

    /**
     * 构建通过后端代理访问 3D Tiles 的 URL。
     *
     * <p>Cesium 加载 tileset.json 后会按相对路径请求子瓦片，
     * 直接指向私有 MinIO 桶会因缺少签名而 403。
     * 改为返回后端代理路径 {@code /api/v1/terra/tiles/{fileId}/tileset.json}，
     * 由 {@link TileProxyController} 代理转发。
     */
    private String buildTileProxyUrl(TerraModelDetailResult d) {
        String indexFileKey = d.getIndexFileKey();
        if (indexFileKey == null || indexFileKey.isEmpty()) {
            return d.getUrl();
        }
        // 从 indexFileKey 中提取 tileset.json 相对于 filePrefix 的路径
        String filePrefix = d.getFilePrefix() != null ? d.getFilePrefix() : "";
        String relativePath = indexFileKey;
        if (!filePrefix.isEmpty() && indexFileKey.startsWith(filePrefix)) {
            relativePath = indexFileKey.substring(filePrefix.length());
        }
        return "/api/v1/terra/tiles/" + d.getId() + "/" + relativePath;
    }

    /**
     * 缓存原始 URL 并返回代理路径。
     *
     * @param type   类型标识（thumb / preview）
     * @param id     资源 ID（fileId）
     * @param rawUrl 原始 SDK URL
     * @return 代理路径；rawUrl 为空时返回 null
     */
    private String cacheAndProxy(String type, long id, String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        mediaUrlCache.put(type + ":" + id, rawUrl);
        return MEDIA_PROXY_PREFIX + "/" + type + "/" + id;
    }

    // ── 媒体代理 raw URL 获取（缓存 miss fallback） ──

    @Override
    public String getModelThumbnailUrl(long fileId) {
        TerraModelDetailResult detail = fetchModelDetail(fileId);
        return detail.getThumbnailUrl();
    }

    @Override
    public String getModelPreviewUrl(long fileId) {
        TerraModelDetailResult detail = fetchModelDetail(fileId);
        return detail.getPreviewUrl();
    }

    @Override
    public String getReconstructionImageUrl(String taskId, String imageId) {
        TerraBuildModule tbm = requireTerraBuildModule();
        CompletableFuture<TerraBuildReconstructionImagesResult> future = new CompletableFuture<>();
        tbm.getReconstructionImages(taskId, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getReconstructionImages 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });
        TerraBuildReconstructionImagesResult result = SdkBridge.await(future);
        if (result.getList() == null) {
            return null;
        }
        for (TerraBuildReconstructionImageItem item : result.getList()) {
            if (imageId.equals(item.getImageId())) {
                String url = item.getOriginImageUrl();
                if (url == null || url.isEmpty()) {
                    url = item.getThumbnailImageUrl();
                }
                return url;
            }
        }
        return null;
    }

    @Override
    public String getQualityReportFileUrl(String taskId) {
        TerraBuildModule tbm = requireTerraBuildModule();
        CompletableFuture<TerraBuildQualityReportResult> future = new CompletableFuture<>();
        tbm.getQualityReport(taskId, "zh", DEFAULT_MEASURE_UNIT, (error, result) -> {
            if (!error.isSuccess()) {
                future.completeExceptionally(new SdkBridge.SdkQueryException(
                        "getQualityReport 失败: code=" + error.getErrorCode()));
                return;
            }
            future.complete(result);
        });
        TerraBuildQualityReportResult result = SdkBridge.await(future);
        return result.getQualityReportUrl();
    }
}
