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

import com.dji.fh.feature.terra.TerraService.PageResult;
import com.dji.fh.feature.terra.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MockTerraService 单元测试。
 *
 * @author DJI DevKit Team
 */
class MockTerraServiceTest {

    private MockTerraService service;

    @BeforeEach
    void setUp() {
        service = new MockTerraService();
    }

    @Test
    void listModels_shouldReturnAllModels_whenNoFilter() {
        PageResult<ModelFileDto> result = service.listModels(1, 20, null);

        assertThat(result.list()).hasSize(6);
        assertThat(result.total()).isEqualTo(6);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(20);
    }

    @Test
    void listModels_shouldFilterByType() {
        PageResult<ModelFileDto> result = service.listModels(1, 20, "ortho_2d");

        assertThat(result.list()).hasSize(2);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.list()).allMatch(m -> m.reconstructionType().equals("ortho_2d"));
    }

    @Test
    void listModels_shouldHandlePagination() {
        PageResult<ModelFileDto> result = service.listModels(1, 2, null);

        assertThat(result.list()).hasSize(2);
        assertThat(result.total()).isEqualTo(6);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(2);
    }

    @Test
    void listModels_shouldReturnEmpty_whenPageExceedsTotal() {
        PageResult<ModelFileDto> result = service.listModels(10, 20, null);

        assertThat(result.list()).isEmpty();
        assertThat(result.total()).isEqualTo(6);
    }

    @Test
    void listModels_shouldFilterByBlankType_asNoFilter() {
        PageResult<ModelFileDto> result = service.listModels(1, 20, "  ");

        assertThat(result.list()).hasSize(6);
    }

    @Test
    void getModelUrl_shouldReturnValidUrl() {
        ModelUrlDto url = service.getModelUrl(1L);

        assertThat(url).isNotNull();
        assertThat(url.url()).contains("/terra/model/1/download");
        assertThat(url.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void createReconstruction_shouldReturnProcessingStatus() {
        CreateReconstructionRequest request = new CreateReconstructionRequest(
                "test-task", List.of("media-1"), "ortho_2d", null);

        ReconstructionTaskDto result = service.createReconstruction(request);

        assertThat(result.status()).isEqualTo("processing");
        assertThat(result.uuid()).isNotBlank();
        assertThat(result.jobUuid()).isNotBlank();
        assertThat(result.id()).isGreaterThan(0);
    }

    @Test
    void createReconstruction_shouldAcceptFolderIds() {
        CreateReconstructionRequest request = new CreateReconstructionRequest(
                "test-task", List.of(265), List.of(), "ortho_2d", null);

        ReconstructionTaskDto result = service.createReconstruction(request);

        assertThat(result.status()).isEqualTo("processing");
        assertThat(result.uuid()).isNotBlank();
    }

    @Test
    void createPanorama_shouldReturnProcessingStatus() {
        CreatePanoramaRequest request = new CreatePanoramaRequest(
                "pano-task", List.of("media-1"), null);

        ReconstructionTaskDto result = service.createPanorama(request);

        assertThat(result.status()).isEqualTo("processing");
        assertThat(result.uuid()).isNotBlank();
    }

    @Test
    void getTaskStatus_shouldReturnStatus() {
        TaskStatusDto status = service.getTaskStatus("task-123");

        assertThat(status.taskId()).isEqualTo("task-123");
        assertThat(status.state()).isEqualTo("processing");
        assertThat(status.progress()).isEqualTo(65);
    }

    @Test
    void getQualityReport_shouldReturnReportUrl() {
        QualityReportDto report = service.getQualityReport("task-123", "zh");

        assertThat(report.qualityReportUrl()).contains("task-123");
    }

    @Test
    void getSfmResult_shouldReturnData() {
        SfmResultDto result = service.getSfmResult("task-456");

        assertThat(result.cameraCount()).isEqualTo(120);
        assertThat(result.pointCount()).isEqualTo(5_000_000L);
        assertThat(result.matchedPercentage()).isEqualTo(95.2);
        assertThat(result.outputPrefix()).contains("task-456");
    }

    @Test
    void getReconstructionImages_shouldReturnList() {
        List<ImageDto> images = service.getReconstructionImages("task-789");

        assertThat(images).hasSize(6);
        assertThat(images.get(0).id()).isEqualTo("img-1");
        assertThat(images.get(0).name()).isEqualTo("DJI_0001.JPG");
        assertThat(images.get(0).url()).contains("task-789");
    }

    @Test
    void listFolders_shouldReturnMockFolders() {
        List<FolderDto> folders = service.listFolders();

        assertThat(folders).hasSize(3);
        assertThat(folders.get(0).folderId()).isEqualTo(1);
        assertThat(folders.get(0).folderUuid()).isEqualTo("mock-folder-uuid-1");
        assertThat(folders.get(0).folderName()).contains("面状航线_Site_A");
    }

    @Test
    void listFolders_shouldReturnUniqueUuids() {
        List<FolderDto> folders = service.listFolders();

        long uniqueCount = folders.stream()
                .map(FolderDto::folderUuid)
                .distinct()
                .count();
        assertThat(uniqueCount).isEqualTo(folders.size());
    }

    @Test
    void listTasks_shouldReturnAllTasks_whenNoFilter() {
        PageResult<TaskStatusDto> result = service.listTasks(1, 20);

        assertThat(result.list()).hasSize(6);
        assertThat(result.total()).isEqualTo(6);
        assertThat(result.list().get(0).state()).isEqualTo("success");
    }

    @Test
    void listTasks_shouldHandlePagination() {
        PageResult<TaskStatusDto> result = service.listTasks(1, 2);

        assertThat(result.list()).hasSize(2);
        assertThat(result.total()).isEqualTo(6);
    }

    // ── 媒体代理 raw URL ──

    @Test
    void getModelThumbnailUrl_shouldReturnMockUrl() {
        String url = service.getModelThumbnailUrl(42L);

        assertThat(url).contains("/terra/model/42/thumbnail.webp");
    }

    @Test
    void getModelPreviewUrl_shouldReturnMockUrl() {
        String url = service.getModelPreviewUrl(42L);

        assertThat(url).contains("/terra/model/42/preview.png");
    }

    @Test
    void getReconstructionImageUrl_shouldReturnMockUrl() {
        String url = service.getReconstructionImageUrl("task-1", "img-5");

        assertThat(url).contains("/terra/images/task-1/img-5");
    }

    @Test
    void getQualityReportFileUrl_shouldReturnMockUrl() {
        String url = service.getQualityReportFileUrl("task-1");

        assertThat(url).contains("/terra/report/task-1");
    }
}
