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

import com.dji.fh.feature.terra.dto.TaskStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * TerraEventController 单元测试。
 *
 * <p>使用 standalone MockMvc，不启动 Spring 上下文。
 * mock=false 避免测试中启动定时器。
 */
class TerraEventControllerTest {

    private TerraEventController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // mock=false 避免在测试中启动定时器
        TerraProperties props = new TerraProperties(true, false);
        controller = new TerraEventController(props);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void subscribe_shouldReturnSseEmitter() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/terra/tasks/events"))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertThat(controller.getActiveEmitterCount()).isEqualTo(1);
    }

    @Test
    void broadcast_shouldNotFailWithNoEmitters() {
        // 无订阅者时 broadcast 应为 no-op
        TaskStatusDto status = new TaskStatusDto("task-1", "processing", 50, "/input", "/sfm");
        controller.broadcast(status);
        assertThat(controller.getActiveEmitterCount()).isZero();
    }

    @Test
    void getActiveEmitterCount_shouldReturnZeroInitially() {
        assertThat(controller.getActiveEmitterCount()).isZero();
    }
}
