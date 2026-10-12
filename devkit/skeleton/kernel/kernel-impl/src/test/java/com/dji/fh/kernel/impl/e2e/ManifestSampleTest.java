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

import com.dji.fh.kernel.impl.manifest.ModuleManifestLoader;
import com.dji.fh.kernel.spi.manifest.ModuleManifest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证启动期 happy-path module.yaml 加载链路。
 *
 * <p>用 {@code @Primary} 覆盖 AutoConfig 装配的默认 loader，指向 test/resources 下的合法样本。
 *
 * @author DJI DevKit Team
 */
@SpringBootTest(classes = {ManifestSampleTest.MinimalApp.class, ManifestSampleTest.SampleLoaderConfig.class})
class ManifestSampleTest {

    @Autowired
    private List<ModuleManifest> loadedManifests;

    @Test
    void loadedManifests_shouldContainHappySample_whenLoaderPointsAtIt() {
        assertThat(loadedManifests).hasSize(1);
        assertThat(loadedManifests.get(0).id()).isEqualTo("feature-device");
    }

    @org.springframework.boot.autoconfigure.SpringBootApplication
    static class MinimalApp {
        @org.springframework.context.annotation.Bean
        com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }

    @Configuration
    static class SampleLoaderConfig {
        @Bean
        @Primary
        ModuleManifestLoader testLoader(ResourcePatternResolver resolver) {
            return new ModuleManifestLoader(
                    resolver, "classpath*:META-INF/fh-modules-test/module-happy.yaml");
        }
    }
}
