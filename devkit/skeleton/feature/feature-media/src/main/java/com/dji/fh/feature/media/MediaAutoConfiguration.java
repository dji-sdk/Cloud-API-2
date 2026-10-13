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

import com.dji.fh.feature.media.spi.sdk.SdkMediaAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 媒体管理 feature 自动装配。
 *
 * <p>默认启用；ISV 设 {@code fh.skeleton.feature.media.enabled=false} 可禁用。
 * SDK 模式下由 {@link SdkMediaAutoConfiguration} 提供真实实现，覆盖 mock 默认。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@AutoConfigureAfter(SdkMediaAutoConfiguration.class)
@ConditionalOnProperty(name = "fh.skeleton.feature.media.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MediaProperties.class)
public class MediaAutoConfiguration {

    /**
     * 注册 MediaService Bean。
     *
     * @param props 媒体配置属性
     * @return MediaService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MediaService mediaService(MediaProperties props) {
        return new MediaService(props);
    }

    /**
     * 注册 MediaRestController Bean。
     *
     * @param service 媒体服务
     * @return MediaRestController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public MediaRestController mediaRestController(MediaService service) {
        return new MediaRestController(service);
    }

    /**
     * 注册 MediaUrlCache Bean —— 缓存预签名 URL，避免每个 Range 请求都回源 SDK。
     *
     * <p><b>Bean 名必须显式指定，不能用方法名派生。</b>feature-terra 的
     * {@code TerraAutoConfiguration} 也有一个 {@code mediaUrlCache()} 方法，
     * 二者在 app 中同时生效。{@code @ConditionalOnMissingBean} 按<b>类型</b>退让，
     * 而两个 {@code MediaUrlCache} 是不同模块的不同类型，因此谁都不会退让；
     * 若 Bean 名也相同（Spring Boot 2.1+ 默认禁止覆盖），启动时直接抛
     * {@code BeanDefinitionOverrideException}，整个应用上下文加载失败。
     *
     * @return MediaUrlCache 实例
     */
    @Bean(name = "mediaContentUrlCache")
    @ConditionalOnMissingBean
    public MediaUrlCache mediaUrlCache() {
        return new MediaUrlCache();
    }

    /**
     * 注册 MediaProxyController Bean —— 媒体内容代理端点。
     *
     * <p>Bean 名显式指定的原因同 {@link #mediaUrlCache()}。
     *
     * @param service 媒体服务
     * @param cache   预签名 URL 缓存
     * @param props   媒体配置属性（含代理回源的受信证书路径）
     * @return MediaProxyController 实例
     */
    @Bean(name = "mediaContentProxyController")
    @ConditionalOnMissingBean
    public MediaProxyController mediaProxyController(MediaService service, MediaUrlCache cache, MediaProperties props) {
        return new MediaProxyController(service, cache, props);
    }
}
