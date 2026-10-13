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

package com.dji.fh.kernel.impl.manifest;

import com.dji.fh.kernel.spi.manifest.ModuleManifest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ManifestAutoConfiguration 装配测试。
 *
 * @author DJI DevKit Team
 */
class ManifestAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ManifestAutoConfiguration.class));

    @Test
    void loadedManifests_shouldBeEmpty_whenNoModuleYaml() {
        // 默认扫描 META-INF/fh-modules/*.yaml；测试 classpath 没有 → 空 list
        runner.run(ctx -> {
            assertThat(ctx).hasBean("loadedManifests");
            @SuppressWarnings("unchecked")
            List<ModuleManifest> ms = ctx.getBean("loadedManifests", List.class);
            assertThat(ms).isEmpty();
        });
    }

    @Test
    void loadedManifests_shouldLoadHappy_whenLoaderOverriddenToHappySample() {
        runner.withUserConfiguration(HappyConfig.class).run(ctx -> {
            @SuppressWarnings("unchecked")
            List<ModuleManifest> ms = ctx.getBean("loadedManifests", List.class);
            assertThat(ms).hasSize(1);
            assertThat(ms.get(0).id()).isEqualTo("feature-device");
        });
    }

    @Test
    void contextLoad_shouldFail_whenBadManifestPresent() {
        runner.withUserConfiguration(BadSyntaxConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasFailed();
                    assertThat(causeChain(ctx.getStartupFailure()))
                            .anyMatch(ManifestException.class::isInstance);
                });
    }

    /**
     * 沿 cause 链向下展开，返回包括自身的全部 Throwable 列表，
     * 用于断言深层 cause 是某个具体异常类型。
     */
    private static java.util.List<Throwable> causeChain(Throwable t) {
        java.util.List<Throwable> chain = new java.util.ArrayList<>();
        Throwable current = t;
        while (current != null && !chain.contains(current)) {
            chain.add(current);
            current = current.getCause();
        }
        return chain;
    }

    @Configuration
    static class HappyConfig {
        @Bean
        ModuleManifestLoader manifestLoader(ResourcePatternResolver resolver) {
            return new ModuleManifestLoader(
                    resolver, "classpath*:META-INF/fh-modules-test/module-happy.yaml");
        }
    }

    @Configuration
    static class BadSyntaxConfig {
        @Bean
        ModuleManifestLoader manifestLoader(ResourcePatternResolver resolver) {
            return new ModuleManifestLoader(
                    resolver, "classpath*:META-INF/fh-modules-test/module-bad-syntax.yaml");
        }
    }
}
