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

import com.dji.fh.feature.terra.dto.BatchDeleteRequest;
import com.dji.fh.feature.terra.dto.CreatePanoramaRequest;
import com.dji.fh.feature.terra.dto.CreateReconstructionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TerraRestController 单元测试 — 使用 standalone MockMvc + MockTerraService。
 *
 * @author DJI DevKit Team
 */
class TerraRestControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TerraService service = new MockTerraService();
        TerraRestController controller = new TerraRestController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ── 模型浏览 ──

    @Test
    void listModels_shouldReturn200WithPageResult() throws Exception {
        mockMvc.perform(get("/api/v1/terra/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list", hasSize(6)))
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void listModels_shouldFilterByType() throws Exception {
        mockMvc.perform(get("/api/v1/terra/models").param("type", "ortho_2d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(2)))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.list[0].reconstructionType").value("ortho_2d"));
    }

    @Test
    void getModelUrl_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/terra/models/1/url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").isString())
                .andExpect(jsonPath("$.url", containsString("/terra/model/1/")))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void listFolders_shouldReturn200WithFolderArray() throws Exception {
        mockMvc.perform(get("/api/v1/terra/folders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].folderId").value(1))
                .andExpect(jsonPath("$[0].folderUuid").value("mock-folder-uuid-1"))
                .andExpect(jsonPath("$[0].folderName", containsString("面状航线_Site_A")));
    }

    @Test
    void listTasks_shouldReturn200WithPageResult() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list", hasSize(6)))
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.list[0].taskId").isString())
                .andExpect(jsonPath("$.list[0].state").value("success"));
    }

    @Test
    void listTasks_shouldHandlePagination() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tasks").param("page", "1").param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(2)))
                .andExpect(jsonPath("$.total").value(6));
    }

    // ── 模型删除 ──

    @Test
    void deleteModel_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/terra/models/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void batchDelete_shouldReturn204() throws Exception {
        BatchDeleteRequest request = new BatchDeleteRequest(List.of(1L, 2L, 3L));
        mockMvc.perform(delete("/api/v1/terra/models/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void batchDelete_shouldHandleEmptyList() throws Exception {
        BatchDeleteRequest request = new BatchDeleteRequest(List.of());
        mockMvc.perform(delete("/api/v1/terra/models/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    // ── 重建任务 ──

    @Test
    void createReconstruction_shouldReturn201() throws Exception {
        CreateReconstructionRequest request = new CreateReconstructionRequest(
                "Test Ortho", List.of("media-uuid-1"), "ortho_2d", null);
        mockMvc.perform(post("/api/v1/terra/tasks/reconstruction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    void createReconstruction_shouldAcceptFolderIds_field() throws Exception {
        CreateReconstructionRequest request = new CreateReconstructionRequest(
                "Test Ortho FolderIds", List.of(265), List.of(), "ortho_2d", null);
        mockMvc.perform(post("/api/v1/terra/tasks/reconstruction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    void createPanorama_shouldReturn201() throws Exception {
        CreatePanoramaRequest request = new CreatePanoramaRequest(
                "Test Panorama", List.of("media-uuid-2"), null);
        mockMvc.perform(post("/api/v1/terra/tasks/panorama")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.uuid").isString())
                .andExpect(jsonPath("$.status").value("processing"));
    }

    @Test
    void getTaskStatus_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tasks/test-id/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskId").value("test-id"))
                .andExpect(jsonPath("$.state").value("processing"))
                .andExpect(jsonPath("$.progress").value(65));
    }

    @Test
    void retryTask_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/terra/tasks/test-id/retry"))
                .andExpect(status().isOk());
    }

    @Test
    void stopTask_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/terra/tasks/test-id/stop"))
                .andExpect(status().isOk());
    }

    // ── 报告 & 影像 ──

    @Test
    void getQualityReport_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tasks/test-id/report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qualityReportUrl").isString());
    }

    @Test
    void getSfmResult_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tasks/test-id/sfm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outputPrefix", containsString("test-id")))
                .andExpect(jsonPath("$.cameraCount").value(120))
                .andExpect(jsonPath("$.pointCount").value(5000000));
    }

    @Test
    void getReconstructionImages_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/terra/tasks/test-id/images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].id").value("img-1"))
                .andExpect(jsonPath("$[0].name").value("DJI_0001.JPG"));
    }
}
