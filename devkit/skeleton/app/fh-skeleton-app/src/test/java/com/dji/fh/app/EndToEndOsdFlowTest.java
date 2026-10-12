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

import com.dji.fh.kernel.spi.event.OsdUpdateEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 端到端 OSD 流：Mock SDK ApplicationReady → 1Hz scheduler 推送 OSD →
 * MockSdkAdapter 转 OsdUpdateEvent → SdkEventBus.publish → 订阅者收到。
 *
 * <p>验收：8 秒内至少收到 3 个 OSD 事件（验证 scheduler 真的 tick）。
 *
 * <p>启动脚本在 t=3000ms 上 drone, t=4000ms 上 dock，1Hz scheduler 推 OSD，
 * 因此约 5s 内能凑齐 3 个事件。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
class EndToEndOsdFlowTest {

    @Autowired
    private OsdEventCapture capture;

    @Test
    void mockOsdScheduler_shouldEmitAtLeast3OsdEvents_within8Seconds() {
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(capture.events().size()).isGreaterThanOrEqualTo(3));

        assertThat(capture.events())
                .extracting(OsdUpdateEvent::deviceSn)
                .contains("MOCK-DRONE-001", "MOCK-DOCK-001");
    }

    /**
     * 测试专用 OsdUpdateEvent 捕获器（@EventListener 由 Spring 管理）。
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        OsdEventCapture osdEventCapture() {
            return new OsdEventCapture();
        }
    }

    /**
     * 简单订阅器，把 OsdUpdateEvent 收集到列表。
     */
    static class OsdEventCapture {

        private final List<OsdUpdateEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onEvent(OsdUpdateEvent event) {
            events.add(event);
        }

        List<OsdUpdateEvent> events() {
            return events;
        }
    }
}
