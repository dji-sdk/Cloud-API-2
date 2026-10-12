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

package com.dji.fh.kernel.impl.e2e;

import com.dji.fh.kernel.spi.bridge.EventBridge;
import com.dji.fh.kernel.spi.event.AbstractKernelEvent;
import com.dji.fh.kernel.spi.event.KernelEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * 验证 publish → BridgeEventDispatcher → EventBridge.send 全链路 wiring。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(classes = {EventBridgeWiringTest.MinimalApp.class, EventBridgeWiringTest.TestBridgeConfig.class})
class EventBridgeWiringTest {

    @Autowired SdkEventBus bus;
    @Autowired TestBridge bridge;

    @Test
    void publish_shouldReachBridge_whenEventClassMatchesInterest() {
        TestEvent event = new TestEvent("e2e");

        bus.publish(event);

        await().untilAsserted(() ->
                assertThat(bridge.lastSent.get()).isSameAs(event));
    }

    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class MinimalApp {
        @org.springframework.context.annotation.Bean
        com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }

    @Configuration
    static class TestBridgeConfig {
        @Bean
        TestBridge testBridge() {
            return new TestBridge();
        }
    }

    static class TestBridge implements EventBridge {
        final AtomicReference<KernelEvent> lastSent = new AtomicReference<>();

        @Override
        public String id() {
            return "e2e-test-bridge";
        }

        @Override
        public Set<Class<? extends KernelEvent>> outboundInterest() {
            return Set.of(TestEvent.class);
        }

        @Override
        public void send(KernelEvent event) {
            lastSent.set(event);
        }
    }

    private static final class TestEvent extends AbstractKernelEvent {
        TestEvent(String source) {
            super(source);
        }
    }
}
