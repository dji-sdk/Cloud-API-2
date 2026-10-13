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

package com.dji.fh.feature.flightarea.spi.sdk;

import com.dji.fh.feature.flightarea.FlightAreaAutoConfiguration;
import com.dji.fh.feature.flightarea.service.OfflineMapService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * SDK 模式下的离线地图管理 SPI 实现自动装配。
 *
 * <p>仅当 {@code fh.skeleton.sdk.mode=sdk} 且 SDK 类存在于 classpath 时才装配。
 * 通过 {@code @AutoConfigureBefore} 保证本配置先于 {@link FlightAreaAutoConfiguration}
 * 处理，使默认配置中的 {@code @ConditionalOnMissingBean} 自动退让。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration(before = FlightAreaAutoConfiguration.class)
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "sdk")
@ConditionalOnClass(name = "com.dji.flighthubsdk.FlightHubSDK")
public class SdkOfflineMapAutoConfiguration {

    /**
     * 注册 SDK 模式下的 {@link OfflineMapService} 实现。
     *
     * @return SdkOfflineMapService 实例
     */
    @Bean
    public OfflineMapService offlineMapService() {
        return new SdkOfflineMapService();
    }
}
