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

package com.dji.fh.feature.wayline.spi.sdk;

import com.dji.fh.feature.wayline.WaylineProperties;
import com.dji.fh.feature.wayline.WaylineService;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * SDK 模式下的航线库管理 SPI 实现自动装配。
 *
 * <p>仅当 {@code fh.skeleton.sdk.mode=sdk} 且 SDK 类存在于 classpath 时才装配。
 * 此配置中的 bean 会覆盖 {@code WaylineAutoConfiguration} 中的
 * {@code @ConditionalOnMissingBean} 默认实现。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "sdk")
@ConditionalOnClass(name = "com.dji.flighthubsdk.FlightHubSDK")
public class SdkWaylineAutoConfiguration {

    /**
     * 注册 SDK 模式下的 {@link WaylineService} 实现。
     *
     * @param props              航线配置属性
     * @param deviceQueryService 设备查询服务
     * @return SdkWaylineService 实例
     */
    @Bean
    public WaylineService waylineService(WaylineProperties props,
                                         DeviceQueryService deviceQueryService) {
        return new SdkWaylineService(props, deviceQueryService);
    }
}
