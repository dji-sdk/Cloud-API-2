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

import com.dji.fh.kernel.spi.bridge.BridgeErrorHandler;
import com.dji.fh.kernel.spi.bridge.EventBridge;
import com.dji.fh.kernel.spi.event.KernelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 把进程内 {@link KernelEvent} 异步分发到所有声明了 outbound interest 的 {@link EventBridge}。
 *
 * <p>触发链路：
 * <ol>
 *   <li>Kernel SDK 适配层 publish KernelEvent 到 Spring</li>
 *   <li>本类的 {@link #onKernelEvent} {@code @EventListener} 收到事件</li>
 *   <li>切到 {@code @Async} 线程池（避免阻塞 publisher 线程）</li>
 *   <li>对每个 {@link EventBridge}，若 {@code outboundInterest().contains(event.class)} 则 {@code send}</li>
 *   <li>任意 bridge 抛异常 → 调用 {@link BridgeErrorHandler#onSendFailure} 或 log warn</li>
 * </ol>
 *
 * <p>失败永远不重抛 —— 单点 bridge 故障不影响其他 bridge 或 SDK 主路径（D12）。
 *
 * @author DJI DevKit Team
 */
public class BridgeEventDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(BridgeEventDispatcher.class);

    private final List<EventBridge> bridges;
    private final BridgeErrorHandler errorHandler;

    /**
     * @param bridges      已注册的 EventBridge bean 列表（可空列表）
     * @param errorHandler 可选 BridgeErrorHandler，{@code null} 表示走默认 log warn
     */
    public BridgeEventDispatcher(List<EventBridge> bridges, BridgeErrorHandler errorHandler) {
        this.bridges = List.copyOf(Objects.requireNonNull(bridges, "bridges 不能为空"));
        this.errorHandler = errorHandler;
    }

    /**
     * Spring 事件监听入口。所有 KernelEvent 都进入此方法。
     *
     * <p>切到 {@code applicationTaskExecutor} 线程池异步执行，
     * 避免阻塞 SDK callback 线程。
     *
     * @param event Kernel 事件
     */
    @Async
    @EventListener
    public void onKernelEvent(KernelEvent event) {
        dispatch(event);
    }

    /**
     * 直接对外暴露的分发入口（测试 / 手工触发用）。
     *
     * @param event Kernel 事件
     */
    public void dispatch(KernelEvent event) {
        Objects.requireNonNull(event, "event 不能为空");
        for (EventBridge bridge : bridges) {
            Set<Class<? extends KernelEvent>> interest = bridge.outboundInterest();
            if (interest == null || !interest.contains(event.getClass())) {
                continue;
            }
            sendSafely(bridge, event);
        }
    }

    private void sendSafely(EventBridge bridge, KernelEvent event) {
        try {
            bridge.send(event);
        } catch (RuntimeException e) {
            if (errorHandler != null) {
                errorHandler.onSendFailure(bridge.id(), event, e);
            } else {
                LOG.warn("bridge {} 发送事件 {} 失败: {}",
                        bridge.id(), event.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
