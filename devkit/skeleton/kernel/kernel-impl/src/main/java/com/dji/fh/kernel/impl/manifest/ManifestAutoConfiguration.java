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

import com.dji.fh.kernel.impl.repository.RepositoryAutoConfiguration;
import com.dji.fh.kernel.spi.manifest.ModuleManifest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.List;

/**
 * Manifest 子系统的默认装配。
 *
 * <p>提供：
 * <ul>
 *   <li>{@link ModuleManifestLoader}（默认扫描 {@code classpath*:META-INF/fh-modules/*.yaml}）</li>
 *   <li>{@code List<ModuleManifest>} bean —— 启动期 fail-fast 触发点</li>
 * </ul>
 *
 * <p>装配在 {@link RepositoryAutoConfiguration} 之后，便于将来 manifest 注册场景用 DataSource。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration(after = RepositoryAutoConfiguration.class)
public class ManifestAutoConfiguration {

    /**
     * 装配默认 ModuleManifestLoader。
     *
     * @param resolver Spring 资源解析器（ApplicationContext 本身实现该接口）
     * @return ModuleManifestLoader 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public ModuleManifestLoader manifestLoader(ResourcePatternResolver resolver) {
        return new ModuleManifestLoader(resolver);
    }

    /**
     * 启动期触发加载。任何一份失败将抛 {@link ManifestException}，
     * 包装为 Spring bean 创建异常导致 context 启动失败（fail-fast）。
     *
     * <p>ISV 通过 {@code @Autowired List<ModuleManifest>} 获取已加载列表。
     *
     * @param loader manifest 加载器
     * @return 已加载的 manifest 列表
     */
    @Bean
    public List<ModuleManifest> loadedManifests(ModuleManifestLoader loader) {
        return loader.loadAll();
    }
}
