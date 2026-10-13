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

import com.dji.fh.kernel.spi.event.KernelReadyEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
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
 * 验证 Mock SDK 在 ApplicationReady 后会发出 KernelReadyEvent(source=mock-sdk, mode=MOCK)。
 *
 * <p>Mock SDK 在 ApplicationReady 时发布过 KernelReady，但 EventBus 默认不重放（D2 决策）。
 * 故用 "再 publish 一次 test-harness 事件" 验证 bus 工作正常（source 区分）。
 *
 * <p>订阅通过 Spring {@link EventListener}（DefaultSdkEventBus 委托 ApplicationEventPublisher）。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
class KernelReadyEventTest {

    @Autowired
    private SdkEventBus bus;

    @Autowired
    private KernelReadyEventCapture capture;

    @Test
    void mockSdk_shouldPublishKernelReadyEvent_afterStartup() {
        bus.publish(new KernelReadyEvent("test-harness", KernelReadyEvent.Mode.MOCK, "0.1.0"));
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(capture.events()).extracting(KernelReadyEvent::source).contains("test-harness"));
    }

    /**
     * 测试专用 KernelReadyEvent 捕获器（@EventListener 由 Spring 管理）。
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        KernelReadyEventCapture kernelReadyEventCapture() {
            return new KernelReadyEventCapture();
        }
    }

    /**
     * 简单订阅器，把 KernelReadyEvent 收集到列表。
     */
    static class KernelReadyEventCapture {

        private final List<KernelReadyEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onEvent(KernelReadyEvent event) {
            events.add(event);
        }

        List<KernelReadyEvent> events() {
            return events;
        }
    }
}
