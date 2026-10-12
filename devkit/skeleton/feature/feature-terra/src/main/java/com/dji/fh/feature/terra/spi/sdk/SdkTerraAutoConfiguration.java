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

package com.dji.fh.feature.terra.spi.sdk;

import com.dji.fh.feature.terra.MediaUrlCache;
import com.dji.fh.feature.terra.TerraService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * SDK 模式下的 Terra 模型管理 SPI 实现自动装配。
 *
 * <p>仅当 {@code fh.skeleton.sdk.mode=sdk} 且 SDK 类存在于 classpath 时才装配。
 * 此配置中的 bean 会覆盖 {@code TerraAutoConfiguration} 中的
 * {@code @ConditionalOnMissingBean} 默认（Mock）实现。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "sdk")
@ConditionalOnClass(name = "com.dji.flighthubsdk.FlightHubSDK")
public class SdkTerraAutoConfiguration {

    /**
     * 注册文件夹列表 TTL 缓存（SDK 模式专属，Mock 模式无需缓存）。
     *
     * @return FolderListCache 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public FolderListCache folderListCache() {
        return new FolderListCache();
    }

    /**
     * 注册 SDK 模式下的 {@link TerraService} 实现。
     *
     * @param mediaUrlCache   媒体 URL 缓存（由 TerraAutoConfiguration 提供）
     * @param folderListCache 文件夹列表缓存
     * @return SdkTerraService 实例
     */
    @Bean
    public TerraService terraService(final MediaUrlCache mediaUrlCache,
                                     final FolderListCache folderListCache) {
        return new SdkTerraService(mediaUrlCache, folderListCache);
    }
}
