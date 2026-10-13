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

package com.dji.fh.feature.wayline;

import com.dji.fh.feature.wayline.spi.sdk.SdkWaylineAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 航线库管理 feature 自动装配。
 *
 * <p>默认启用；ISV 设 {@code fh.skeleton.feature.wayline.enabled=false} 可禁用。
 * SDK 模式下由 {@link SdkWaylineAutoConfiguration} 提供真实实现，覆盖默认空实现。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration
@AutoConfigureAfter(SdkWaylineAutoConfiguration.class)
@ConditionalOnProperty(name = "fh.skeleton.feature.wayline.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(WaylineProperties.class)
public class WaylineAutoConfiguration {

    /**
     * 注册 WaylineService Bean（默认空实现，SDK 模式下被覆盖）。
     *
     * @param props 航线配置属性
     * @return WaylineService 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public WaylineService waylineService(WaylineProperties props) {
        return new WaylineService(props);
    }

    /**
     * 注册 WaylineRestController Bean。
     *
     * @param service 航线服务
     * @return WaylineRestController 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public WaylineRestController waylineRestController(WaylineService service) {
        return new WaylineRestController(service);
    }
}
