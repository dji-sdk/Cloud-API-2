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
 * 设备相关事件子标记。便于 listener 按 deviceSn 过滤。
 *
 * <pre>
 *   @EventListener(condition = "#e.deviceSn == 'XXXX'")
 *   public void onSpecific(OsdUpdateEvent e) { ... }
 * </pre>
 *
 * @author DJI DevKit Team
 */
public interface DeviceScopedEvent extends KernelEvent {

    /**
     * @return 设备 SN
     */
    String deviceSn();
}
