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

package com.dji.fh.kernel.spi.event;

/**
 * 事件发布入口。Kernel 统一管理事件派发，ISV 禁止绕过此接口直接操作底层事件总线，
 * 以保留日后加入埋点 / 限流 / hook 的扩展空间。
 *
 * <p>具体派发策略（同步 / 异步、线程模型）由实现层定义，不进 SPI 契约。
 *
 * @author DJI DevKit Team
 */
public interface SdkEventBus {

    /**
     * 将事件分发给所有已注册的监听者。
     *
     * @param event 待发布的事件，不能为 null
     */
    void publish(KernelEvent event);
}
