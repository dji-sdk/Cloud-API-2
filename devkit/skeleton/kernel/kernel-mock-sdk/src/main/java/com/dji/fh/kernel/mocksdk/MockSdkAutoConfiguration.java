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

package com.dji.fh.kernel.mocksdk;

import com.dji.fh.kernel.impl.event.EventBusAutoConfiguration;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import com.dji.fh.kernel.spi.query.DeviceQueryService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mock SDK 子系统的默认装配。
 *
 * <p>仅当 {@code fh.skeleton.sdk.mode=mock}（matchIfMissing=true）时装配。
 * 生产 sdk profile 下整体不存在，REST 端点、scheduler、adapter 全部 0 泄漏。
 *
 * <p>装配在 {@link EventBusAutoConfiguration}（A2）之后，保证 {@link SdkEventBus} 已就绪。
 *
 * <p>启动序列见 {@link MockSdkStarter}（独立 bean 以避免循环依赖）。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration(after = EventBusAutoConfiguration.class)
@ConditionalOnProperty(name = "fh.skeleton.sdk.mode", havingValue = "mock", matchIfMissing = false)
@EnableScheduling
@EnableConfigurationProperties(MockSdkProperties.class)
public class MockSdkAutoConfiguration {

    /**
     * 当前 Mock SDK 版本。沿用骨架 v0.1.0。
     */
    public static final String MOCK_KERNEL_VERSION = MockSdkStarter.MOCK_KERNEL_VERSION;

    /**
     * 设备列表查询服务：从内存注册表返回。
     */
    @Bean
    @ConditionalOnMissingBean(DeviceQueryService.class)
    public DeviceQueryService mockDeviceQueryService(MockDeviceRegistry registry) {
        return new MockDeviceQueryService(registry);
    }

    /**
     * @return Mock 虚拟设备注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public MockDeviceRegistry mockDeviceRegistry() {
        return new MockDeviceRegistry();
    }

    /**
     * @return Mock SDK 入口
     */
    @Bean
    @ConditionalOnMissingBean
    public MockFlightHubSdk mockFlightHubSdk() {
        return new MockFlightHubSdk();
    }

    /**
     * @param sdk      Mock SDK
     * @param registry 设备注册表
     * @return 1Hz OSD 推送调度器
     */
    @Bean
    @ConditionalOnMissingBean
    public MockOsdScheduler mockOsdScheduler(MockFlightHubSdk sdk, MockDeviceRegistry registry) {
        return new MockOsdScheduler(sdk, registry);
    }

    /**
     * @param sdk      Mock SDK
     * @param registry 设备注册表
     * @return 触发器（脚本 + REST 共享）
     */
    @Bean
    @ConditionalOnMissingBean
    public MockEventTrigger mockEventTrigger(MockFlightHubSdk sdk, MockDeviceRegistry registry) {
        return new MockEventTrigger(sdk, registry);
    }

    /**
     * @param trigger 触发器
     * @param props   Mock 配置
     * @return 启动脚本回放器
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public MockEventScripter mockEventScripter(MockEventTrigger trigger, MockSdkProperties props) {
        return new MockEventScripter(trigger, props);
    }

    /**
     * @param sdk      Mock SDK
     * @param registry 设备注册表
     * @param bus      事件总线
     * @return Mock callback → KernelEvent 适配器
     */
    @Bean(destroyMethod = "unsubscribe")
    @ConditionalOnMissingBean
    public MockSdkAdapter mockSdkAdapter(MockFlightHubSdk sdk, MockDeviceRegistry registry, SdkEventBus bus) {
        return new MockSdkAdapter(sdk, registry, bus);
    }

    /**
     * @param trigger 触发器
     * @return Mock REST 控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public MockRestController mockRestController(MockEventTrigger trigger) {
        return new MockRestController(trigger);
    }

    /**
     * @param props    Mock 配置
     * @param registry 设备注册表
     * @param adapter  适配器
     * @param scripter 脚本回放器
     * @param bus      事件总线
     * @return Mock SDK 启动钩子（ApplicationReady 触发）
     */
    @Bean
    @ConditionalOnMissingBean
    public MockSdkStarter mockSdkStarter(MockSdkProperties props,
                                         MockDeviceRegistry registry,
                                         MockSdkAdapter adapter,
                                         MockEventScripter scripter,
                                         SdkEventBus bus) {
        return new MockSdkStarter(props, registry, adapter, scripter, bus);
    }
}
