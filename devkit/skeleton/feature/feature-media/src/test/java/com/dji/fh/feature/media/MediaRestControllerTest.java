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

package com.dji.fh.feature.media;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MediaRestController.class)
class MediaRestControllerTest {

    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        MediaService mediaService() {
            return new MediaService(new MediaProperties(50, 500, true, ""));
        }

        @Bean
        MediaRestController mediaRestController(MediaService service) {
            return new MediaRestController(service);
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    void listMedia_shouldReturn200WithEmptyFiles() throws Exception {
        mvc.perform(get("/api/v1/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files").isArray())
                .andExpect(jsonPath("$.files").isEmpty())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.hasMore").value(false));
    }

    @Test
    void listMedia_shouldReturn200WithPhotoFilter() throws Exception {
        mvc.perform(get("/api/v1/media").param("file_type", "photo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files").isEmpty());
    }

    @Test
    void listMedia_shouldAcceptPagination() throws Exception {
        mvc.perform(get("/api/v1/media")
                .param("start_index", "0")
                .param("page_size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.files").isEmpty())
                .andExpect(jsonPath("$.pageSize").value(2));
    }

    @Test
    void getDownloadUrl_shouldReturn200() throws Exception {
        mvc.perform(get("/api/v1/media/10001/download-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId").value(10001))
                .andExpect(jsonPath("$.expiresIn").value(0));
    }

    @Test
    void batchDelete_shouldReturn200() throws Exception {
        mvc.perform(post("/api/v1/media/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[10001,10002]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletedCount").value(0));
    }

    @Test
    void batchDelete_shouldReturn400_whenEmptyList() throws Exception {
        mvc.perform(post("/api/v1/media/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileIds\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
