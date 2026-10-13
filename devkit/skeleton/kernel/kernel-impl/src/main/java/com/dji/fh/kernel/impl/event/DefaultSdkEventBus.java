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

import com.dji.fh.kernel.spi.event.KernelEvent;
import com.dji.fh.kernel.spi.event.SdkEventBus;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Objects;

/**
 * SdkEventBus 的默认实现 —— 薄包装 Spring {@link ApplicationEventPublisher}。
 *
 * <p>所有 {@code publish} 调用直接转发到 Spring 事件机制；
 * 同步 / 异步语义由订阅方 {@code @EventListener} 自身决定（默认同步；标注 {@code @Async} 异步）。
 *
 * <p>本 bean 故意保持极简，留出未来加埋点 / 限流 / 拦截的扩展空间（D12 决策）。
 *
 * @author DJI DevKit Team
 */
public class DefaultSdkEventBus implements SdkEventBus {

    private final ApplicationEventPublisher publisher;

    /**
     * @param publisher Spring 事件发布器（由 Spring 注入）
     */
    public DefaultSdkEventBus(ApplicationEventPublisher publisher) {
        this.publisher = Objects.requireNonNull(publisher, "publisher 不能为空");
    }

    @Override
    public void publish(KernelEvent event) {
        Objects.requireNonNull(event, "event 不能为空");
        publisher.publishEvent(event);
    }
}
