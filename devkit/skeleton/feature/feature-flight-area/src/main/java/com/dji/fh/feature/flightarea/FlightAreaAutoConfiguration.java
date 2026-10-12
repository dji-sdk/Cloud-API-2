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

package com.dji.fh.feature.flightarea;

import com.dji.fh.feature.flightarea.controller.FlightAreaController;
import com.dji.fh.feature.flightarea.controller.OfflineMapController;
import com.dji.fh.feature.flightarea.controller.TcasController;
import com.dji.fh.feature.flightarea.repository.FlightAreaRepository;
import com.dji.fh.feature.flightarea.service.FlightAreaService;
import com.dji.fh.feature.flightarea.service.OfflineMapService;
import com.dji.fh.feature.flightarea.service.TcasService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 空域安全管理 feature 的 AutoConfiguration。
 *
 * <p>默认启用；ISV 设 {@code fh.skeleton.feature.flight-area.enabled=false} 可完全禁用。
 */
@AutoConfiguration
@ConditionalOnProperty(name = "fh.skeleton.feature.flight-area.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FlightAreaProperties.class)
@Import(FlightAreaWebConfig.class)
@EntityScan(basePackages = "com.dji.fh.feature.flightarea.entity")
@EnableJpaRepositories(basePackages = "com.dji.fh.feature.flightarea.repository")
public class FlightAreaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlightAreaService flightAreaService(FlightAreaRepository repository) {
        return new FlightAreaService(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    public OfflineMapService offlineMapService() {
        return new OfflineMapService();
    }

    @Bean
    @ConditionalOnMissingBean
    public TcasService tcasService() {
        return new TcasService();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlightAreaController flightAreaController(FlightAreaService service) {
        return new FlightAreaController(service);
    }

    @Bean
    @ConditionalOnMissingBean
    public OfflineMapController offlineMapController(OfflineMapService service) {
        return new OfflineMapController(service);
    }

    @Bean
    @ConditionalOnMissingBean
    public TcasController tcasController(TcasService service) {
        return new TcasController(service);
    }
}
