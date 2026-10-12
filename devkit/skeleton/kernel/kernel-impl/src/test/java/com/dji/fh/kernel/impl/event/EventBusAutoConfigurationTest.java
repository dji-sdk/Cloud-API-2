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

package com.dji.fh.kernel.impl.event;

import com.dji.fh.kernel.impl.async.AsyncAutoConfiguration;
import com.dji.fh.kernel.spi.event.AbstractKernelEvent;
import com.dji.fh.kernel.spi.event.KernelEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * EventBusAutoConfiguration 装配测试。
 *
 * @author DJI DevKit Team
 */
class EventBusAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    AsyncAutoConfiguration.class,
                    EventBusAutoConfiguration.class));

    @Test
    void sdkEventBus_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(SdkEventBus.class);
            assertThat(ctx.getBean(SdkEventBus.class))
                    .isInstanceOf(DefaultSdkEventBus.class);
        });
    }

    @Test
    void bridgeEventDispatcher_shouldExist_whenAutoConfigured() {
        runner.run(ctx -> assertThat(ctx).hasSingleBean(BridgeEventDispatcher.class));
    }

    @Test
    void publish_shouldReachListener_whenWiredEndToEnd() {
        runner.withUserConfiguration(ListenerConfig.class).run(ctx -> {
            SdkEventBus bus = ctx.getBean(SdkEventBus.class);
            ListenerConfig.Recorder recorder = ctx.getBean(ListenerConfig.Recorder.class);
            TestEvent event = new TestEvent("kernel-test");

            bus.publish(event);

            await().untilAsserted(() ->
                    assertThat(recorder.lastSeen.get()).isSameAs(event));
        });
    }

    @Configuration
    static class ListenerConfig {
        @Bean
        Recorder recorder() {
            return new Recorder();
        }

        static class Recorder {
            final AtomicReference<KernelEvent> lastSeen = new AtomicReference<>();

            @EventListener
            void onEvent(KernelEvent event) {
                lastSeen.set(event);
            }
        }
    }

    private static final class TestEvent extends AbstractKernelEvent {
        TestEvent(String source) {
            super(source);
        }
    }
}
