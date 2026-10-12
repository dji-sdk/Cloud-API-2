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

package com.dji.fh.feature.flightarea;

import com.dji.fh.feature.flightarea.model.OfflineMapUpdateStatus;
import com.dji.fh.feature.flightarea.service.OfflineMapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 地形障碍物 REST API 控制器测试。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:offlinemap-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OfflineMapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfflineMapService service;

    @Configuration
    @EnableAutoConfiguration
    @Import(FlightAreaAutoConfiguration.class)
    static class TestConfig {
    }

    @BeforeEach
    void resetServiceState() {
        // 等待任何进行中的更新完成或取消
        var state = service.getUpdateState();
        if (state.status() == OfflineMapUpdateStatus.PROCESSING
                || state.status() == OfflineMapUpdateStatus.WAITING) {
            service.cancelUpdate();
        }
        service.resetUpdateState();
    }

    @Test
    void getModelInfo_shouldReturnData() throws Exception {
        mockMvc.perform(get("/api/v1/offline-maps/model-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.disabled").isBoolean())
                .andExpect(jsonPath("$.dataDescription").exists());
    }

    @Test
    void setEnabled_shouldToggleFlag() throws Exception {
        mockMvc.perform(put("/api/v1/offline-maps/enabled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/offline-maps/model-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disabled").value(true));

        mockMvc.perform(put("/api/v1/offline-maps/enabled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void setEnabled_shouldReturn400_whenMissingBody() throws Exception {
        mockMvc.perform(put("/api/v1/offline-maps/enabled")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void triggerUpdate_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/offline-maps/update"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("更新已触发"));
    }

    @Test
    void triggerUpdate_shouldReturn409_whenAlreadyRunning() throws Exception {
        mockMvc.perform(post("/api/v1/offline-maps/update"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/offline-maps/update"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("更新正在进行中"));

        // 等待完成后清理
        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.FINISH);
        service.resetUpdateState();
    }

    @Test
    void getUpdateStatus_shouldReturnState() throws Exception {
        mockMvc.perform(get("/api/v1/offline-maps/update-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IDLE"))
                .andExpect(jsonPath("$.progress").value(0));
    }

    @Test
    void cancelUpdate_shouldReturn409_whenNoActiveUpdate() throws Exception {
        mockMvc.perform(delete("/api/v1/offline-maps/update"))
                .andExpect(status().isConflict());
    }

    @Test
    void cancelUpdate_shouldReturn200_whenUpdateRunning() throws Exception {
        mockMvc.perform(post("/api/v1/offline-maps/update"))
                .andExpect(status().isOk());

        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.PROCESSING);

        mockMvc.perform(delete("/api/v1/offline-maps/update"))
                .andExpect(status().isOk());
    }

    @Test
    void resetUpdateState_shouldReturn200() throws Exception {
        service.triggerUpdate();

        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.FINISH);

        mockMvc.perform(post("/api/v1/offline-maps/update-reset"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/offline-maps/update-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IDLE"));
    }

    @Test
    void getUpdateStatus_shouldShowProgressDuringUpdate() throws Exception {
        mockMvc.perform(post("/api/v1/offline-maps/update"))
                .andExpect(status().isOk());

        await().atMost(3, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.PROCESSING);

        mockMvc.perform(get("/api/v1/offline-maps/update-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"))
                .andExpect(jsonPath("$.progress").isNumber());

        // 等待完成后清理
        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> service.getUpdateState().status() == OfflineMapUpdateStatus.FINISH);
        service.resetUpdateState();
    }
}
