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

package com.dji.fh.kernel.realsdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.dji.fh.kernel.impl.event.EventBusAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * RealSdkAutoConfiguration 装配切片测试。
 *
 * <p>通过 withBean 注入 mock {@link SdkInvoker}，防止真 SDK 类加载（避免 JNI 静态初始化）。
 *
 * @author DJI DevKit Team
 */
class RealSdkAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EventBusAutoConfiguration.class,
                    RealSdkAutoConfiguration.class))
            .withBean(SdkInvoker.class, () -> mock(SdkInvoker.class));

    @Test
    void shouldRegisterAllBeans_whenSdkModePropertyIsSdk() {
        runner.withPropertyValues(
                "fh.skeleton.sdk.mode=sdk",
                "fh.skeleton.sdk.real.token=t",
                "fh.skeleton.sdk.real.address=test.example.com",
                "fh.skeleton.sdk.real.port=443",
                "fh.skeleton.sdk.real.notification-server-address=wss://test")
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(RealSdkAdapter.class);
                    assertThat(ctx).hasSingleBean(OsdAggregator.class);
                    assertThat(ctx).hasSingleBean(RealSdkStarter.class);
                    assertThat(ctx).hasSingleBean(RealSdkProperties.class);
                });
    }

    @Test
    void shouldNotRegisterAnyBean_whenSdkModePropertyAbsent() {
        runner.run(ctx -> {
            assertThat(ctx).doesNotHaveBean(RealSdkAdapter.class);
            assertThat(ctx).doesNotHaveBean(RealSdkStarter.class);
            assertThat(ctx).doesNotHaveBean(OsdAggregator.class);
        });
    }

    @Test
    void shouldNotRegisterAnyBean_whenSdkModeIsMock() {
        runner.withPropertyValues("fh.skeleton.sdk.mode=mock")
                .run(ctx -> assertThat(ctx).doesNotHaveBean(RealSdkAdapter.class));
    }

    @Test
    void shouldAllowIsvOverrideSdkInvoker() {
        // 单独建一个 runner（不带默认 SdkInvoker），避免与 ISV 自定义 bean 冲突
        SdkInvoker custom = mock(SdkInvoker.class);
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        EventBusAutoConfiguration.class,
                        RealSdkAutoConfiguration.class))
                .withPropertyValues(
                        "fh.skeleton.sdk.mode=sdk",
                        "fh.skeleton.sdk.real.token=t",
                        "fh.skeleton.sdk.real.address=test.example.com",
                        "fh.skeleton.sdk.real.port=443",
                        "fh.skeleton.sdk.real.notification-server-address=wss://test")
                .withBean(SdkInvoker.class, () -> custom)
                .run(ctx -> assertThat(ctx.getBean(SdkInvoker.class)).isSameAs(custom));
    }

    @Test
    void shouldBindRealSdkProperties_fromPropertyValues() {
        runner.withPropertyValues(
                "fh.skeleton.sdk.mode=sdk",
                "fh.skeleton.sdk.real.token=tok",
                "fh.skeleton.sdk.real.address=api.example.com",
                "fh.skeleton.sdk.real.port=443",
                "fh.skeleton.sdk.real.notification-server-address=wss://x",
                "fh.skeleton.sdk.real.log-level=debug",
                "fh.skeleton.sdk.real.osd-interval-millis=500")
                .run(ctx -> {
                    RealSdkProperties props = ctx.getBean(RealSdkProperties.class);
                    assertThat(props.token()).isEqualTo("tok");
                    assertThat(props.logLevel()).isEqualTo("debug");
                    assertThat(props.osdIntervalMillis()).isEqualTo(500L);
                });
    }
}
