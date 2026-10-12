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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dji.fh.feature.terra.MediaUrlCache;
import com.dji.fh.feature.terra.dto.CreatePanoramaRequest;
import com.dji.fh.feature.terra.dto.CreateReconstructionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * SdkTerraService 单元测试.
 *
 * <p>SDK JAR 虽在测试 classpath，但未调用 {@code FlightHubSDK.initialize}，
 * {@code getWorkspaceInstance()} 返回 null，{@code requireTerraBuildModule()} 抛
 * {@link IllegalStateException}。本测试覆盖 SDK 未初始化时各公开方法的异常行为。
 * 回调分支依赖 SDK 初始化与原生库，由集成测试覆盖。
 *
 * @author DJI DevKit Team
 */
class SdkTerraServiceTest {

    private SdkTerraService service;

    @BeforeEach
    void setUp() {
        service = new SdkTerraService(new MediaUrlCache(), new FolderListCache());
    }

    @Test
    void listModels_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.listModels(1, 20, null))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void listModels_shouldThrow_whenTypeFilterProvided_butSdkNotInitialized() {
        assertThatThrownBy(() -> service.listModels(1, 20, "ortho_2d"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getModelUrl_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getModelUrl(100L))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void deleteModel_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.deleteModel(100L))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void batchDeleteModels_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.batchDeleteModels(List.of(100L, 200L)))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void createReconstruction_shouldThrow_whenSdkNotInitialized() {
        CreateReconstructionRequest request = new CreateReconstructionRequest(
                "测试任务", List.of("folder-1"), "ortho_2d", null);
        assertThatThrownBy(() -> service.createReconstruction(request))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void createReconstruction_shouldThrow_whenSdkNotInitialized_withFolderIds() {
        CreateReconstructionRequest request = new CreateReconstructionRequest(
                "测试任务", List.of(265), List.of(), "ortho_2d", null);
        assertThatThrownBy(() -> service.createReconstruction(request))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void createPanorama_shouldThrow_whenSdkNotInitialized() {
        CreatePanoramaRequest request = new CreatePanoramaRequest(
                "测试全景", List.of("folder-1"), null);
        assertThatThrownBy(() -> service.createPanorama(request))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getTaskStatus_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getTaskStatus("task-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void retryTask_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.retryTask("task-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void stopTask_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.stopTask("task-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getQualityReport_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getQualityReport("task-1", "zh"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getSfmResult_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getSfmResult("task-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getReconstructionImages_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getReconstructionImages("task-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void listFolders_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.listFolders())
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void listTasks_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.listTasks(1, 20))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void sdkQueryException_shouldBeRuntimeException_withMessage() {
        SdkBridge.SdkQueryException ex = new SdkBridge.SdkQueryException("超时");
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex).hasMessage("超时");
    }

    // ── 媒体代理 raw URL 获取 ──

    @Test
    void getModelThumbnailUrl_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getModelThumbnailUrl(100L))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getModelPreviewUrl_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getModelPreviewUrl(100L))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getReconstructionImageUrl_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getReconstructionImageUrl("task-1", "img-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }

    @Test
    void getQualityReportFileUrl_shouldThrow_whenSdkNotInitialized() {
        assertThatThrownBy(() -> service.getQualityReportFileUrl("task-1"))
                .isInstanceOfAny(IllegalStateException.class,
                        NoClassDefFoundError.class, UnsatisfiedLinkError.class);
    }
}
