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
 * Bridge 错误回调（可选实现）。
 * Kernel 检测到 {@link EventBridge#send(KernelEvent)} 抛异常时调用，
 * 便于 ISV 统一监控所有 bridge 的 outbound 失败。
 *
 * <p>未提供实现时，Kernel 走默认行为（日志记录）。
 *
 * @author DJI DevKit Team
 */
public interface BridgeErrorHandler {

    /**
     * @param bridgeId 失败的 bridge id
     * @param event    导致失败的事件
     * @param error    异常
     */
    void onSendFailure(String bridgeId, KernelEvent event, Throwable error);
}
