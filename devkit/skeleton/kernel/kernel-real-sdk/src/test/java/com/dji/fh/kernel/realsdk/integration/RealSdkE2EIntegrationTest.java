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

package com.dji.fh.kernel.realsdk.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.dji.fh.kernel.realsdk.SdkInvoker;
import com.dji.fh.kernel.spi.event.KernelReadyEvent;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.TestPropertySource;

/**
 * 真 SDK 集成测试：验证 {@code DefaultSdkInvoker} + 真 {@code FlightHubSDK} + native lib 联通。
 *
 * <p><strong>默认 CI 跳过</strong>（无 native lib + 无网络 + 无 token）。本地或预生产手动启用：
 * <pre>
 * FH_SKELETON_REAL_SDK_E2E=true \
 * FH_SKELETON_REAL_SDK_TOKEN=&lt;token&gt; \
 * FH_SKELETON_REAL_SDK_ADDRESS=api.example.com \
 * FH_SKELETON_REAL_SDK_PORT=443 \
 * FH_SKELETON_REAL_SDK_NOTIFICATION_SERVER=wss://api.example.com/ws \
 * mvn -pl kernel/kernel-real-sdk test \
 *     -Dtest=RealSdkE2EIntegrationTest
 * </pre>
 *
 * <p>测试仅验证：
 * <ol>
 *   <li>SDK initialize 成功（isInitialized() 在 15s 内变 true）；</li>
 *   <li>KernelReadyEvent 在 startup 后 publish。</li>
 * </ol>
 *
 * <p>不验业务事件触发（业务逻辑已在 RealSdkAdapterTest / OsdAggregatorTest /
 * RealSdkStarterTest 单测覆盖）。
 *
 * @author DJI DevKit Team
 */
@EnabledIfEnvironmentVariable(named = "FH_SKELETON_REAL_SDK_E2E", matches = "true")
@SpringBootTest(classes = RealSdkE2EIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
        "fh.skeleton.sdk.mode=sdk",
        "fh.skeleton.sdk.real.token=${FH_SKELETON_REAL_SDK_TOKEN:}",
        "fh.skeleton.sdk.real.address=${FH_SKELETON_REAL_SDK_ADDRESS:}",
        "fh.skeleton.sdk.real.port=${FH_SKELETON_REAL_SDK_PORT:443}",
        "fh.skeleton.sdk.real.notification-server-address=${FH_SKELETON_REAL_SDK_NOTIFICATION_SERVER:}"
})
class RealSdkE2EIntegrationTest {

    @Autowired
    private SdkInvoker invoker;

    @Autowired
    private List<KernelReadyEvent> capturedReady;

    @Test
    void initialize_shouldSucceed_whenValidToken() {
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(invoker.isInitialized()).isTrue());
        assertThat(invoker.getVersion()).isNotBlank();
    }

    @Test
    void onReady_shouldPublishKernelReadyEvent_afterInitialize() {
        await().atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(capturedReady).hasSizeGreaterThanOrEqualTo(1));
        assertThat(capturedReady.get(0).source()).isEqualTo("real-sdk");
        assertThat(capturedReady.get(0).mode()).isEqualTo(KernelReadyEvent.Mode.SDK);
    }

    /**
     * Spring Boot 测试启动配置：仅引入 RealSdkAutoConfiguration + 捕获 bean。
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        @Bean
        List<KernelReadyEvent> capturedReady() {
            return new CopyOnWriteArrayList<>();
        }

        @Bean
        ReadyEventCaptor readyEventCaptor(List<KernelReadyEvent> capture) {
            return new ReadyEventCaptor(capture);
        }
    }

    /**
     * 捕获 KernelReadyEvent 的 bean。
     */
    static class ReadyEventCaptor {

        private final List<KernelReadyEvent> capture;

        ReadyEventCaptor(List<KernelReadyEvent> capture) {
            this.capture = capture;
        }

        @EventListener
        public void onReady(KernelReadyEvent event) {
            capture.add(event);
        }
    }
}
