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

package com.dji.fh.feature.terra;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock 实现 -- 返回模拟数据，macOS/Windows 可用，无需 SDK 原生库.
 *
 * @author DJI DevKit Team
 */
public final class MockTerraService implements TerraService {

    /** ID 自增序列起始值. */
    private static final long ID_SEQ_INIT = 1000;

    /** 模拟 URL 有效期（秒）. */
    private static final long URL_EXPIRY_SECONDS = 3600;

    /** 模拟任务进度百分比. */
    private static final int MOCK_PROGRESS = 65;

    /** 模拟 SfM 相机数量. */
    private static final int MOCK_CAMERA_COUNT = 120;

    /** 模拟 SfM 点数量. */
    private static final long MOCK_POINT_COUNT = 5_000_000L;

    /** 模拟 SfM 匹配百分比. */
    private static final double MOCK_MATCHED_PERCENTAGE = 95.2;

    /** 模拟影像数量. */
    private static final int MOCK_IMAGE_COUNT = 6;

    /** 模拟文件夹数据集. */
    private static final List<FolderDto> MOCK_FOLDERS = List.of(
            new FolderDto(1, "mock-folder-uuid-1", "面状航线_Site_A 2026-07-01 10:00 (UTC+08)"),
            new FolderDto(2, "mock-folder-uuid-2", "面状航线_Site_B 2026-07-02 14:30 (UTC+08)"),
            new FolderDto(3, "mock-folder-uuid-3", "环绕飞行_检查区 2026-07-03 09:15 (UTC+08)")
    );

    /** ID 自增序列. */
    private static final AtomicLong ID_SEQ = new AtomicLong(ID_SEQ_INIT);

    /** 模拟模型数据集. */
    private static final List<ModelFileDto> MOCK_MODELS = List.of(
            new ModelFileDto(1L, "uuid-ortho-1",
                    "正射影像_Site_A", "ortho", "tiff",
                    "success", "ortho_2d", null,
                    1772592000L),
            new ModelFileDto(2L, "uuid-ortho-2",
                    "正射影像_Site_B", "ortho", "tiff",
                    "success", "ortho_2d", null,
                    1772574780L),
            new ModelFileDto(3L, "uuid-mesh-1",
                    "三维网格_Survey_B", "mesh", "b3dm",
                    "success", "mesh_3d", null,
                    1772592420L),
            new ModelFileDto(4L, "uuid-mesh-2",
                    "三维网格_Inspection", "mesh", "obj",
                    "success", "mesh_3d", null,
                    1772586180L),
            new ModelFileDto(5L, "uuid-pc-1",
                    "点云_Bridge", "pointcloud", "las",
                    "success", "point_cloud", null,
                    1772526060L),
            new ModelFileDto(6L, "uuid-pano-1",
                    "全景_Factory", "panorama", "jpg",
                    "success", "panorama", null,
                    1772465400L)
    );

    @Override
    public PageResult<ModelFileDto> listModels(
            final int page,
            final int pageSize,
            final String type) {
        List<ModelFileDto> filtered =
                type == null || type.isBlank()
                ? MOCK_MODELS
                : MOCK_MODELS.stream()
                        .filter(m -> m.reconstructionType()
                                .equals(type))
                        .toList();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, filtered.size());
        List<ModelFileDto> slice = start < filtered.size()
                ? filtered.subList(start, end) : List.of();
        return new PageResult<>(
                slice, filtered.size(), page, pageSize);
    }

    @Override
    public ModelDetailDto getModelDetail(final long fileId) {
        return new ModelDetailDto(
                fileId,
                "mock-uuid-" + fileId,
                "Mock 模型 " + fileId,
                "mesh_3d",
                "https://mock.dji.com/terra/model/" + fileId + "/tileset.json",
                "https://mock.dji.com/terra/model/" + fileId + "/thumbnail.webp",
                "https://mock.dji.com/terra/model/" + fileId + "/preview.png",
                22.5431,
                113.9472,
                50.0,
                List.of(),
                Instant.now().getEpochSecond()
        );
    }

    @Override
    public ModelUrlDto getModelUrl(final long fileId) {
        return new ModelUrlDto(
                "https://mock.dji.com/terra/model/"
                        + fileId + "/download",
                Instant.now().plusSeconds(URL_EXPIRY_SECONDS));
    }

    @Override
    public TileStorageInfo getTileStorageInfo(final long fileId) {
        return new TileStorageInfo(
                "https://mock.dji.com/file-storage-privatization/",
                "terra/model/" + fileId + "/tiles/");
    }

    @Override
    public void deleteModel(final long fileId) {
        // Mock: no-op
    }

    @Override
    public void batchDeleteModels(final List<Long> fileIds) {
        // Mock: no-op
    }

    @Override
    public ReconstructionTaskDto createReconstruction(
            final CreateReconstructionRequest request) {
        long id = ID_SEQ.incrementAndGet();
        return new ReconstructionTaskDto(
                id, UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), "processing");
    }

    @Override
    public ReconstructionTaskDto createPanorama(
            final CreatePanoramaRequest request) {
        long id = ID_SEQ.incrementAndGet();
        return new ReconstructionTaskDto(
                id, UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), "processing");
    }

    @Override
    public TaskStatusDto getTaskStatus(final String taskId) {
        return new TaskStatusDto(taskId, "processing",
                MOCK_PROGRESS, "/mock/input",
                "/mock/sfm/output");
    }

    @Override
    public void retryTask(final String taskId) {
        // Mock: no-op
    }

    @Override
    public void stopTask(final String taskId) {
        // Mock: no-op
    }

    @Override
    public QualityReportDto getQualityReport(
            final String taskId,
            final String language) {
        return new QualityReportDto(
                "https://mock.dji.com/terra/report/" + taskId);
    }

    @Override
    public SfmResultDto getSfmResult(final String taskId) {
        return new SfmResultDto(
                "/mock/sfm/" + taskId, MOCK_CAMERA_COUNT,
                MOCK_POINT_COUNT, MOCK_MATCHED_PERCENTAGE);
    }

    @Override
    public List<ImageDto> getReconstructionImages(
            final String taskId) {
        List<ImageDto> images = new ArrayList<>();
        for (int i = 1; i <= MOCK_IMAGE_COUNT; i++) {
            images.add(new ImageDto(
                    "img-" + i,
                    "DJI_000" + i + ".JPG",
                    "https://mock.dji.com/terra/images/"
                            + taskId + "/" + i));
        }
        return images;
    }

    @Override
    public PageResult<TaskStatusDto> listTasks(final int page, final int pageSize) {
        List<TaskStatusDto> all = MOCK_MODELS.stream()
                .map(m -> new TaskStatusDto(
                        m.uuid(),
                        m.state(),
                        MOCK_PROGRESS,
                        "/mock/input/" + m.uuid(),
                        "/mock/sfm/" + m.uuid()))
                .toList();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, all.size());
        List<TaskStatusDto> slice = start < all.size()
                ? all.subList(start, end) : List.of();
        return new PageResult<>(slice, all.size(), page, pageSize);
    }

    @Override
    public List<FolderDto> listFolders() {
        return MOCK_FOLDERS;
    }

    // ── 媒体代理 raw URL 获取 ──

    @Override
    public String getModelThumbnailUrl(final long fileId) {
        return "https://mock.dji.com/terra/model/" + fileId + "/thumbnail.webp";
    }

    @Override
    public String getModelPreviewUrl(final long fileId) {
        return "https://mock.dji.com/terra/model/" + fileId + "/preview.png";
    }

    @Override
    public String getReconstructionImageUrl(
            final String taskId, final String imageId) {
        return "https://mock.dji.com/terra/images/" + taskId + "/" + imageId;
    }

    @Override
    public String getQualityReportFileUrl(final String taskId) {
        return "https://mock.dji.com/terra/report/" + taskId;
    }
}
