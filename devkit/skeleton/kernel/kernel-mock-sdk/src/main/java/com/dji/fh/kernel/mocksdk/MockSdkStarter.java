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

import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.KernelReadyEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Objects;

/**
 * Mock SDK 启动钩子。
 *
 * <p>独立于 {@link MockSdkAutoConfiguration} 以避免循环依赖：AutoConfig 通过 {@code @Bean} 产出
 * Mock 组件，本类按需 inject 这些组件再在 Spring app ready 后串起来调用。
 *
 * <p>启动序列：
 * <ol>
 *   <li>注册虚拟 drone + 虚拟 dock 到 {@link MockDeviceRegistry}</li>
 *   <li>{@link MockSdkAdapter#subscribe()} 注册全局 listener</li>
 *   <li>给两个设备各加 OSD listener</li>
 *   <li>publish {@link KernelReadyEvent}(source="mock-sdk", mode=MOCK)</li>
 *   <li>启动 {@link MockEventScripter#play()} 回放</li>
 * </ol>
 *
 * @author DJI DevKit Team
 */
public class MockSdkStarter {

    /**
     * 当前 Mock SDK 版本。沿用骨架 v0.1.0。
     */
    public static final String MOCK_KERNEL_VERSION = "0.1.0";

    private static final Logger LOG = LoggerFactory.getLogger(MockSdkStarter.class);

    private final MockSdkProperties props;
    private final MockDeviceRegistry registry;
    private final MockSdkAdapter adapter;
    private final MockEventScripter scripter;
    private final SdkEventBus bus;

    /**
     * @param props    Mock 配置
     * @param registry 设备注册表
     * @param adapter  Mock SDK 适配器
     * @param scripter 启动脚本回放器
     * @param bus      事件总线
     */
    public MockSdkStarter(MockSdkProperties props,
                          MockDeviceRegistry registry,
                          MockSdkAdapter adapter,
                          MockEventScripter scripter,
                          SdkEventBus bus) {
        this.props = Objects.requireNonNull(props, "props 不能为空");
        this.registry = Objects.requireNonNull(registry, "registry 不能为空");
        this.adapter = Objects.requireNonNull(adapter, "adapter 不能为空");
        this.scripter = Objects.requireNonNull(scripter, "scripter 不能为空");
        this.bus = Objects.requireNonNull(bus, "bus 不能为空");
    }

    /**
     * 启动钩子：在 Spring app ready 后注册虚拟设备、订阅 callback、发布 KernelReady、启动脚本。
     *
     * @param event Spring 应用 ready 事件
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!props.isEnabled()) {
            LOG.warn("mock SDK 被禁用 (fh.skeleton.mock.enabled=false)，跳过初始化");
            return;
        }
        LOG.info("Mock SDK 正在初始化（device sn={}, dock sn={}）",
                props.getDevice().getSn(), props.getDock().getSn());

        // 1. 注册虚拟设备
        registry.register(props.getDevice().getSn(), DeviceType.DRONE, props.getDevice().getModel());
        registry.register(props.getDock().getSn(), DeviceType.DOCK, props.getDock().getModel());

        // 2. 适配器订阅 SDK callback
        adapter.subscribe();
        adapter.subscribeOsd(props.getDevice().getSn());
        adapter.subscribeOsd(props.getDock().getSn());

        // 3. 发布 KernelReady
        bus.publish(new KernelReadyEvent(MockSdkAdapter.SOURCE, KernelReadyEvent.Mode.MOCK, MOCK_KERNEL_VERSION));

        // 4. 启动脚本回放
        scripter.play();
    }
}
