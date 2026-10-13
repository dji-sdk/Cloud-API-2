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

package com.dji.fh.kernel.realsdk;

import com.dji.fh.kernel.impl.event.EventBusAutoConfiguration;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 真 SDK profile 的 AutoConfiguration。
 *
 * <p>仅当 {@code fh.skeleton.sdk.mode=sdk} 时装配；{@code mock} 或缺省时不装配,
 * 与 {@code com.dji.fh.kernel.mocksdk.MockSdkAutoConfiguration} 互斥
 * （同 property 不同 havingValue）。
 *
 * <p>{@code @AutoConfiguration(after = EventBusAutoConfiguration.class)} 确保
 * 在 SdkEventBus 就绪后才装配本配置。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration(after = EventBusAutoConfiguration.class)
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "sdk")
@EnableConfigurationProperties(RealSdkProperties.class)
@EnableScheduling
public class RealSdkAutoConfiguration {

    /**
     * SdkInvoker 默认实现，ISV 可通过自家 bean 覆盖。
     */
    @Bean
    @ConditionalOnMissingBean(SdkInvoker.class)
    public SdkInvoker defaultSdkInvoker() {
        return new DefaultSdkInvoker();
    }

    @Bean
    public RealSdkAdapter realSdkAdapter(SdkEventBus bus) {
        return new RealSdkAdapter(bus);
    }

    @Bean
    public OsdAggregator osdAggregator(SdkEventBus bus, Clock clock, RealSdkProperties props) {
        return new OsdAggregator(bus, clock, props);
    }

    /**
     * {@link RealSdkStarter} 通过 {@code @PreDestroy} 已自管 shutdown 生命周期。
     */
    @Bean
    public RealSdkStarter realSdkStarter(SdkInvoker invoker,
                                          RealSdkAdapter adapter,
                                          OsdAggregator agg,
                                          RealSdkProperties props,
                                          SdkEventBus bus) {
        return new RealSdkStarter(invoker, adapter, agg, props, bus);
    }

    /**
     * 设备列表查询服务：直接调 SDK getDeviceList()。
     */
    @Bean
    @ConditionalOnMissingBean(DeviceQueryService.class)
    public DeviceQueryService realDeviceQueryService(SdkInvoker invoker, RealSdkProperties props) {
        return new RealDeviceQueryService(invoker, props);
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clock() {
        return Clock.systemUTC();
    }
}
