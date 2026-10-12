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

package com.dji.fh.feature.media;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class MediaAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MediaAutoConfiguration.class));

    @Test
    void autoConfig_shouldRegisterBeans_byDefault() {
        runner.run(ctx -> {
            assertThat(ctx).hasSingleBean(MediaService.class);
            assertThat(ctx).hasSingleBean(MediaRestController.class);
            assertThat(ctx).hasSingleBean(MediaUrlCache.class);
            assertThat(ctx).hasSingleBean(MediaProxyController.class);
        });
    }

    @Test
    void autoConfig_shouldBeDisabled_whenPropertyFalse() {
        runner.withPropertyValues("fh.skeleton.feature.media.enabled=false")
                .run(ctx -> {
                    assertThat(ctx).doesNotHaveBean(MediaService.class);
                    assertThat(ctx).doesNotHaveBean(MediaRestController.class);
                    assertThat(ctx).doesNotHaveBean(MediaProxyController.class);
                });
    }

    @Test
    void autoConfig_shouldBackOff_whenCustomBeanPresent() {
        runner.withBean(MediaService.class, () -> new MediaService(new MediaProperties(10, 100, true, "")))
                .run(ctx -> assertThat(ctx).hasSingleBean(MediaService.class));
    }

    @Test
    void autoConfig_shouldBackOffProxy_whenCustomMediaUrlCachePresent() {
        runner.withBean(MediaUrlCache.class, MediaUrlCache::new)
                .run(ctx -> assertThat(ctx).hasSingleBean(MediaUrlCache.class));
    }

    @Test
    void autoConfig_shouldFailFast_whenProxyTrustedCertPathInvalid() {
        runner.withPropertyValues("fh.skeleton.feature.media.proxy-trusted-cert-path=/no/such/file.pem")
                .run(ctx -> assertThat(ctx).hasFailed());
    }

    /**
     * 断言两个 Bean 使用显式命名的注册名。
     *
     * <p>方法名派生的默认 Bean 名（{@code mediaUrlCache} / {@code mediaProxyController}）
     * 与 feature-terra 的 {@code TerraAutoConfiguration} 完全同名，而后者在本 app 中
     * 同样生效 —— Spring Boot 2.1+ 默认禁止 Bean 覆盖，同名会直接抛
     * {@code BeanDefinitionOverrideException} 让整个上下文加载失败。
     * 本用例锁住显式命名，防止有人把 {@code name=} 删掉。
     * 跨模块的真实回归网是 {@code FhSkeletonApplicationTest}（加载完整上下文）。
     */
    @Test
    void autoConfig_shouldRegisterBeansUnderFeatureScopedNames() {
        runner.run(ctx -> {
            assertThat(ctx).hasBean("mediaContentUrlCache");
            assertThat(ctx).hasBean("mediaContentProxyController");
            // 默认名不得被占用
            assertThat(ctx).doesNotHaveBean("mediaUrlCache");
            assertThat(ctx).doesNotHaveBean("mediaProxyController");
        });
    }
}
