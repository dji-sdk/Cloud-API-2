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

package com.dji.fh.kernel.mocksdk;

import com.dji.fh.kernel.spi.event.DeviceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockRestController.class)
@Import(MockRestControllerTest.TestConfig.class)
class MockRestControllerTest {

    private static final ObjectMapper OM = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MockDeviceRegistry registry;

    @Test
    void online_shouldReturn202_whenValidSn() throws Exception {
        registry.register("DRONE-1", DeviceType.DRONE, "drone");

        mvc.perform(post("/mock/devices/DRONE-1/online"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("fired"));
    }

    @Test
    void online_shouldReturn404_whenUnknownSn() throws Exception {
        mvc.perform(post("/mock/devices/UNKNOWN/online"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void osd_shouldAcceptCustomData_whenJsonPosted() throws Exception {
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        String body = OM.writeValueAsString(Map.of("battery_percent", 88));

        mvc.perform(post("/mock/devices/DRONE-1/osd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());
    }

    @Test
    void offline_shouldReturn202AndMarkDeviceOffline_whenValidSn() throws Exception {
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        registry.markOnline("DRONE-1");
        // 前置：设备在线
        assertThat(registry.find("DRONE-1").orElseThrow().online()).isTrue();

        mvc.perform(post("/mock/devices/DRONE-1/offline").param("reason", "timeout"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("fired"));

        // 触发后：状态切到 offline
        assertThat(registry.find("DRONE-1").orElseThrow().online()).isFalse();
    }

    @Test
    void offline_shouldReturn404_whenUnknownSn() throws Exception {
        mvc.perform(post("/mock/devices/UNKNOWN/offline"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void flightTask_shouldReturn202_whenPayloadPosted() throws Exception {
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        String body = OM.writeValueAsString(Map.of(
                "taskId", "task-42",
                "status", "RUNNING",
                "progress", 50));

        mvc.perform(post("/mock/devices/DRONE-1/flight-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("fired"));
    }

    @Test
    void hms_shouldReturn202_whenPayloadPosted() throws Exception {
        registry.register("DRONE-1", DeviceType.DRONE, "drone");
        String body = OM.writeValueAsString(Map.of(
                "alertCode", "BATT-LOW",
                "level", "WARNING",
                "message", "battery 20%"));

        mvc.perform(post("/mock/devices/DRONE-1/hms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("fired"));
    }

    /**
     * 测试专用最小 Spring Boot 配置（@WebMvcTest 需要 @SpringBootConfiguration 才能引导 slice）。
     */
    @SpringBootConfiguration
    static class TestConfig {

        @Bean
        MockDeviceRegistry mockDeviceRegistry() {
            return new MockDeviceRegistry();
        }

        @Bean
        MockFlightHubSdk mockFlightHubSdk() {
            return new MockFlightHubSdk();
        }

        @Bean
        MockEventTrigger mockEventTrigger(MockFlightHubSdk sdk, MockDeviceRegistry registry) {
            return new MockEventTrigger(sdk, registry);
        }

        @Bean
        MockRestController mockRestController(MockEventTrigger trigger) {
            return new MockRestController(trigger);
        }
    }
}
