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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * ModuleManifestLoader 全场景测试。
 *
 * <p>样本路径 {@code classpath*:META-INF/fh-modules-test/*.yaml}（区别于运行时的 fh-modules/）。
 * 通过传单个文件名 pattern 隔离每个用例。
 *
 * @author DJI DevKit Team
 */
class ModuleManifestLoaderTest {

    private static final String BASE = "classpath*:META-INF/fh-modules-test/";

    @Test
    void loadAll_shouldReturnSingleManifest_whenHappyPath() {
        ModuleManifestLoader loader = new ModuleManifestLoader(
                new PathMatchingResourcePatternResolver(),
                BASE + "module-happy.yaml");

        List<ModuleManifest> ms = loader.loadAll();

        assertThat(ms).hasSize(1);
        assertThat(ms.get(0).id()).isEqualTo("feature-device");
        assertThat(ms.get(0).toggle().defaultValue()).isTrue();
    }

    @Test
    void loadAll_shouldReturnEmpty_whenNoYamlsMatch() {
        ModuleManifestLoader loader = new ModuleManifestLoader(
                new PathMatchingResourcePatternResolver(),
                "classpath*:META-INF/none-such-dir/*.yaml");

        assertThat(loader.loadAll()).isEmpty();
    }

    @Test
    void loadAll_shouldThrow_whenSyntaxError() {
        ModuleManifestLoader loader = new ModuleManifestLoader(
                new PathMatchingResourcePatternResolver(),
                BASE + "module-bad-syntax.yaml");

        assertThatExceptionOfType(ManifestException.class)
                .isThrownBy(loader::loadAll)
                .withMessageContaining("解析");
    }

    @Test
    void loadAll_shouldThrow_whenRequiredFieldMissing() {
        ModuleManifestLoader loader = new ModuleManifestLoader(
                new PathMatchingResourcePatternResolver(),
                BASE + "module-missing-id.yaml");

        assertThatExceptionOfType(ManifestException.class)
                .isThrownBy(loader::loadAll)
                .withMessageContaining("id");
    }

    @Test
    void loadAll_shouldThrow_whenKernelVersionIncompatible() {
        ModuleManifestLoader loader = new ModuleManifestLoader(
                new PathMatchingResourcePatternResolver(),
                BASE + "module-incompatible-kernel.yaml");

        assertThatExceptionOfType(ManifestException.class)
                .isThrownBy(loader::loadAll)
                .withMessageContaining("不兼容");
    }

    @Test
    void loadAll_shouldThrow_whenDuplicateId() {
        ModuleManifestLoader loader = new ModuleManifestLoader(
                new PathMatchingResourcePatternResolver(),
                BASE + "module-dup-id-*.yaml");

        assertThatExceptionOfType(ManifestException.class)
                .isThrownBy(loader::loadAll)
                .withMessageContaining("冲突");
    }
}
