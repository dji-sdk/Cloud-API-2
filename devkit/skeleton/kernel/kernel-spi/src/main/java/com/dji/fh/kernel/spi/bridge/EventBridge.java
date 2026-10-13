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
import java.util.Set;

/**
 * 把进程内 Spring Event 桥接到外部 broker 或反向。
 * 多个 EventBridge 可同时存在，每个对接一个 broker。
 *
 * <p>实现示例：
 * <ul>
 *   <li>{@code fh-skeleton-bridge-mqtt} — KernelEvent → MQTT</li>
 *   <li>{@code fh-skeleton-bridge-kafka} — KernelEvent → Kafka</li>
 *   <li>ISV 自家实现 — 对接公司事件平台</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public interface EventBridge {

    /**
     * Bridge 唯一标识，用于日志 / metrics / 错误归属。
     * 推荐 "mqtt" / "kafka" / "acme-bus" 等小写短串。
     *
     * @return bridge id
     */
    String id();

    /**
     * 声明本 bridge 关心的事件类型白名单。
     * Kernel 启动时按此清单注册 listener，未声明的事件不会进入 outbound。
     *
     * @return 关心的事件类型集合；返回空集表示本 bridge 不做 outbound（纯 inbound）
     */
    Set<Class<? extends KernelEvent>> outboundInterest();

    /**
     * Outbound：Kernel 把进程内事件转给 bridge 发出。
     * 实现内序列化 + 发往外部 broker。
     *
     * <p>失败不抛 —— Kernel 已在外层捕获，实现内部应记日志 + metrics。
     *
     * @param event 待发送事件
     */
    void send(KernelEvent event);
}
