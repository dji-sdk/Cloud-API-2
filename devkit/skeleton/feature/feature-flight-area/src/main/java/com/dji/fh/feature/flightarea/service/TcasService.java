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

package com.dji.fh.feature.flightarea.service;

import com.dji.fh.feature.flightarea.model.TcasConfig;
import com.dji.fh.feature.flightarea.model.TcasThreshold;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TCAS（Traffic Collision Avoidance System）告警配置服务。
 *
 * <p>v0.1 内存 Mock 实现，后续对接 SDK FlightTaskModule。
 */
public class TcasService {

    private static final Logger LOG = LoggerFactory.getLogger(TcasService.class);

    private final AtomicReference<TcasConfig> config = new AtomicReference<>(defaultConfig());

    /**
     * 查询 TCAS 配置。
     */
    public TcasConfig getConfig() {
        return config.get();
    }

    /**
     * 更新 TCAS 配置。
     */
    public void updateConfig(TcasConfig newConfig) {
        config.set(newConfig);
        LOG.info("TCAS 配置已更新，阈值数量: {}", newConfig.thresholds().size());
    }

    private static TcasConfig defaultConfig() {
        return new TcasConfig(List.of(
                new TcasThreshold("高速有人机", 2000, 500, 1000, 250),
                new TcasThreshold("低速飞行器", 4000, 1000, 2000, 500),
                new TcasThreshold("无人机", 1000, 300, 500, 150)
        ));
    }
}
