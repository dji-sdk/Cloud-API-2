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

package com.dji.fh.feature.wayline;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WaylineRestController.class)
class WaylineRestControllerTest {

    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        WaylineService waylineService() {
            return new WaylineService(new WaylineProperties(20, 100, true));
        }

        @Bean
        WaylineRestController waylineRestController(WaylineService service) {
            return new WaylineRestController(service);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void listWaylines_shouldReturn200WithEmptyList() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waylines").isArray())
                .andExpect(jsonPath("$.waylines").isEmpty())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void listWaylines_shouldAcceptPagination() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines")
                .param("page", "2")
                .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void listWaylines_shouldClampPageSize_atMax() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines")
                .param("page_size", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(100));
    }

    @Test
    void listWaylines_shouldUseDefaultPageSize_whenZero() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines")
                .param("page_size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void listWaylines_shouldAcceptFilters() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines")
                .param("drone_type", "Matrice 350 RTK")
                .param("payload_type", "Zenmuse H20T")
                .param("keyword", "巡检"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waylines").isEmpty());
    }

    @Test
    void listExecutions_shouldReturnMockData_whenKnownUuid() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines/wl-001/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waylineUuid").value("wl-001"))
                .andExpect(jsonPath("$.waylineName").value("新建面状航线(50)"))
                .andExpect(jsonPath("$.executions").isArray())
                .andExpect(jsonPath("$.executions[0].id").value("ex-001-1"))
                .andExpect(jsonPath("$.executions[0].photoCount").value(50));
    }

    @Test
    void listExecutions_shouldReturnEmptyList_whenUnknownUuid() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines/unknown-uuid/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waylineUuid").value("unknown-uuid"))
                .andExpect(jsonPath("$.waylineName").value("未知航线"))
                .andExpect(jsonPath("$.executions").isArray())
                .andExpect(jsonPath("$.executions").isEmpty());
    }

    @Test
    void listExecutions_shouldReturnMultipleExecutions() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines/wl-003/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executions.length()").value(2))
                .andExpect(jsonPath("$.executions[0].id").value("ex-003-1"))
                .andExpect(jsonPath("$.executions[1].id").value("ex-003-2"));
    }

    @Test
    void batchDelete_shouldReturn200_whenValidUuids() throws Exception {
        mvc.perform(post("/api/v1/workspace/waylines/batch_delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wayline_uuids\":[\"uuid-1\",\"uuid-2\"]}"))
                .andExpect(status().isOk());
    }

    @Test
    void batchDelete_shouldReturn400_whenEmptyList() throws Exception {
        mvc.perform(post("/api/v1/workspace/waylines/batch_delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wayline_uuids\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void batchDelete_shouldReturn400_whenNullList() throws Exception {
        mvc.perform(post("/api/v1/workspace/waylines/batch_delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void batchDownload_shouldReturn200_whenValidUuids() throws Exception {
        mvc.perform(post("/api/v1/workspace/waylines/batch_download")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wayline_uuids\":[\"uuid-1\",\"uuid-2\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloads").isArray());
    }

    @Test
    void batchDownload_shouldReturn400_whenEmptyList() throws Exception {
        mvc.perform(post("/api/v1/workspace/waylines/batch_download")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"wayline_uuids\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void batchDownload_shouldReturn400_whenNullList() throws Exception {
        mvc.perform(post("/api/v1/workspace/waylines/batch_download")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importWayline_shouldReturn200_whenValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.kmz", "application/octet-stream", new byte[]{1, 2, 3});
        mvc.perform(multipart("/api/v1/workspace/waylines/submit")
                .file(file)
                .param("wayline_name", "测试航线"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wayline_uuid").isNotEmpty());
    }

    @Test
    void importWayline_shouldReturn400_whenEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.kmz", "application/octet-stream", new byte[0]);
        mvc.perform(multipart("/api/v1/workspace/waylines/submit")
                .file(file)
                .param("wayline_name", "测试航线"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void waylineExists_shouldReturnTrue_forDefaultImplementation() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines/any-uuid/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void waylineExists_shouldReturnTrue_forDifferentUuid() throws Exception {
        mvc.perform(get("/api/v1/workspace/waylines/wl-nonexistent/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }
}
