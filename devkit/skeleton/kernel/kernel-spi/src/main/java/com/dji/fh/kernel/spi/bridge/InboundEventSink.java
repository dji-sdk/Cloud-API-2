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

package com.dji.fh.kernel.spi.bridge;

import com.dji.fh.kernel.spi.event.KernelEvent;

/**
 * 反向通道：bridge 实现从外部 broker 收到消息反序列化为 KernelEvent 后，
 * 通过这个 sink 注入 Kernel 内部 Event Bus。
 *
 * <p>Kernel 提供唯一实例，bridge 实现通过依赖注入获得。
 *
 * <p><b>重要</b>：bridge 实现负责标记 {@code event.source()} 为外部来源
 * （例如 {@code "bridge.mqtt.acme"}），避免本地 listener 误以为是 SDK 原生事件。
 *
 * @author DJI DevKit Team
 */
public interface InboundEventSink {

    /**
     * 把外部来源的事件回放进 Spring Event Bus。
     * 注入后该事件会被所有进程内 {@code @EventListener} 消费。
     *
     * @param event 已反序列化的事件
     */
    void inject(KernelEvent event);
}
