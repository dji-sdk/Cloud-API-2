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

package com.dji.fh.app;

import com.dji.fh.kernel.spi.event.DeviceOnlineEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 验证 mock REST 端点能手动触发 device-online 事件 → KernelEvent。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
class RestMockTriggerTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private DeviceOnlineCapture capture;

    @Test
    void postDeviceOnline_shouldEmitDeviceOnlineEvent() {
        // TestRestTemplate 在 RANDOM_PORT 下会自动拼接 server.servlet.context-path，
        // 故此处传相对路径，实际 wire 请求为 /pf-devkit/ai-plugin/skeleton/mock/devices/MOCK-DRONE-001/online
        ResponseEntity<Void> resp = rest.postForEntity(
                "/mock/devices/MOCK-DRONE-001/online", null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(capture.events()).extracting(DeviceOnlineEvent::deviceSn).contains("MOCK-DRONE-001"));
    }

    @Test
    void postUnknownDevice_shouldReturn404() {
        // 同上，context-path 由 TestRestTemplate 自动拼接
        ResponseEntity<Void> resp = rest.postForEntity(
                "/mock/devices/UNKNOWN-SN/online", null, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * 测试专用 DeviceOnlineEvent 捕获器（@EventListener 由 Spring 管理）。
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        DeviceOnlineCapture deviceOnlineCapture() {
            return new DeviceOnlineCapture();
        }
    }

    /**
     * 简单订阅器，把 DeviceOnlineEvent 收集到列表。
     */
    static class DeviceOnlineCapture {

        private final List<DeviceOnlineEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onEvent(DeviceOnlineEvent event) {
            events.add(event);
        }

        List<DeviceOnlineEvent> events() {
            return events;
        }
    }
}
