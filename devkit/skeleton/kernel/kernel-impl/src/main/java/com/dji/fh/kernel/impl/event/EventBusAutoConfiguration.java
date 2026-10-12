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

package com.dji.fh.kernel.impl.event;

import com.dji.fh.kernel.impl.async.AsyncAutoConfiguration;
import com.dji.fh.kernel.spi.bridge.BridgeErrorHandler;
import com.dji.fh.kernel.spi.bridge.EventBridge;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * EventBus 子系统的默认装配。
 *
 * <p>提供：
 * <ul>
 *   <li>{@link SdkEventBus} = {@link DefaultSdkEventBus}（薄 Spring 包装）</li>
 *   <li>{@link BridgeEventDispatcher}（异步 fan-out 到所有 ISV 注入的 EventBridge）</li>
 * </ul>
 *
 * <p>装配在 {@link AsyncAutoConfiguration} 之后，保证 {@code applicationTaskExecutor} 已就绪。
 *
 * @author DJI DevKit Team
 */
@AutoConfiguration(after = AsyncAutoConfiguration.class)
public class EventBusAutoConfiguration {

    /**
     * 装配默认 SdkEventBus。已存在则让位。
     *
     * @param publisher Spring 事件发布器
     * @return DefaultSdkEventBus 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public SdkEventBus sdkEventBus(ApplicationEventPublisher publisher) {
        return new DefaultSdkEventBus(publisher);
    }

    /**
     * 装配 BridgeEventDispatcher。已存在则让位。
     *
     * <p>{@code ObjectProvider} 用于在 ISV 未注入任何 EventBridge / BridgeErrorHandler 时
     * 优雅降级（前者拿到空列表，后者拿到 null）。
     *
     * @param bridges      可选的 EventBridge bean 列表
     * @param errorHandler 可选的 BridgeErrorHandler
     * @return BridgeEventDispatcher 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public BridgeEventDispatcher bridgeEventDispatcher(
            ObjectProvider<List<EventBridge>> bridges,
            ObjectProvider<BridgeErrorHandler> errorHandler) {
        return new BridgeEventDispatcher(
                bridges.getIfAvailable(List::of),
                errorHandler.getIfAvailable());
    }
}
