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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Terra 模型管理 feature 的 AutoConfiguration.
 *
 * <p>默认启用；设 {@code fh.skeleton.feature.terra.enabled=false} 禁用.
 * Mock/Real 通过 {@code fh.skeleton.feature.terra.mock=true/false} 切换.
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@ConditionalOnProperty(
        name = "fh.skeleton.feature.terra.enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(TerraProperties.class)
public class TerraAutoConfiguration {

    /**
     * Mock 模式下注册 MockTerraService.
     *
     * <p>作为 fallback：仅当没有更高优先级的 {@link TerraService} bean（如 SDK 模式的
     * {@code SdkTerraAutoConfiguration}）存在时才注册。无需额外配置即可回退到 mock。
     *
     * @return MockTerraService 实例
     */
    @Bean
    @ConditionalOnMissingBean(TerraService.class)
    public TerraService mockTerraService() {
        return new MockTerraService();
    }

    /**
     * 注册 Terra REST 控制器.
     *
     * <p>仅在 TerraService 存在时创建.
     *
     * @param terraService 业务实现
     * @return TerraRestController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TerraService.class)
    public TerraRestController terraRestController(
            final TerraService terraService) {
        return new TerraRestController(terraService);
    }

    /**
     * 注册 Terra SSE 事件控制器.
     *
     * @param terraProperties Terra 配置属性
     * @return TerraEventController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public TerraEventController terraEventController(
            final TerraProperties terraProperties) {
        return new TerraEventController(terraProperties);
    }

    /**
     * 注册媒体 URL 缓存 — 存储 SDK 返回的内网预签名 URL.
     *
     * @return MediaUrlCache 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MediaUrlCache mediaUrlCache() {
        return new MediaUrlCache();
    }

    /**
     * 注册 3D Tiles 瓦片代理控制器.
     *
     * @param terraService 业务服务
     * @return TileProxyController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TerraService.class)
    public TileProxyController tileProxyController(
            final TerraService terraService) {
        return new TileProxyController(terraService);
    }

    /**
     * 注册媒体代理控制器（缩略图/预览图/影像/报告）.
     *
     * @param mediaUrlCache 媒体 URL 缓存
     * @param terraService  业务服务
     * @return MediaProxyController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TerraService.class)
    public MediaProxyController mediaProxyController(
            final MediaUrlCache mediaUrlCache,
            final TerraService terraService) {
        return new MediaProxyController(mediaUrlCache, terraService);
    }
}
