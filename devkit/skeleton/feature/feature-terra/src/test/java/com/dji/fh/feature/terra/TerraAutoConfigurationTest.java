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

package com.dji.fh.feature.terra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TerraAutoConfiguration 条件装配测试。
 *
 * @author DJI DevKit Team
 */
class TerraAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TerraAutoConfiguration.class));

    @Test
    void shouldLoadMockService_whenMockTrue() {
        contextRunner
                .withPropertyValues("fh.skeleton.feature.terra.mock=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(TerraService.class);
                    assertThat(context.getBean(TerraService.class)).isInstanceOf(MockTerraService.class);
                });
    }

    @Test
    void shouldLoadMockService_whenDefaultConfig() {
        // MockTerraService 作为 fallback，@ConditionalOnMissingBean 默认注册
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(TerraService.class);
                    assertThat(context.getBean(TerraService.class)).isInstanceOf(MockTerraService.class);
                });
    }

    @Test
    void shouldNotLoad_whenFeatureDisabled() {
        contextRunner
                .withPropertyValues("fh.skeleton.feature.terra.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TerraService.class);
                });
    }

    @Test
    void shouldStillLoadMockAsFallback_whenMockFalse() {
        // 新设计：MockTerraService 是 @ConditionalOnMissingBean fallback，
        // 只有当 SdkTerraAutoConfiguration 提供了真实 bean 时才不注册
        contextRunner
                .withPropertyValues("fh.skeleton.feature.terra.mock=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(TerraService.class);
                    assertThat(context.getBean(TerraService.class)).isInstanceOf(MockTerraService.class);
                });
    }

    @Test
    void shouldNotOverrideExistingBean() {
        contextRunner
                .withBean(TerraService.class, MockTerraService::new)
                .withPropertyValues("fh.skeleton.feature.terra.mock=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(TerraService.class);
                });
    }

    @Test
    void shouldRegisterMediaUrlCache() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(MediaUrlCache.class);
                });
    }

    @Test
    void shouldRegisterTileProxyController() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(TileProxyController.class);
                });
    }

    @Test
    void shouldRegisterMediaProxyController() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(MediaProxyController.class);
                });
    }

    @Test
    void shouldNotRegisterControllers_whenFeatureDisabled() {
        contextRunner
                .withPropertyValues("fh.skeleton.feature.terra.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TileProxyController.class);
                    assertThat(context).doesNotHaveBean(MediaProxyController.class);
                    assertThat(context).doesNotHaveBean(MediaUrlCache.class);
                });
    }
}
