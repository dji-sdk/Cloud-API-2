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

import java.util.Objects;

/**
 * 机场 OSD 推送事件。
 *
 * @author DJI DevKit Team
 */
public final class DockOsdUpdateEvent extends AbstractKernelEvent implements DeviceScopedEvent {

    private final String deviceSn;
    private final DockOsdData dockOsd;

    /**
     * @param source   触发来源
     * @param deviceSn 机场 SN
     * @param dockOsd  机场 OSD 数据；不得为 null（上游 adapter 应保证）
     */
    public DockOsdUpdateEvent(String source, String deviceSn, DockOsdData dockOsd) {
        super(source);
        this.deviceSn = Objects.requireNonNull(deviceSn, "deviceSn 不能为空");
        this.dockOsd = Objects.requireNonNull(dockOsd, "dockOsd 不能为空");
    }

    @Override
    public String deviceSn() {
        return deviceSn;
    }

    /**
     * @return 机场 OSD 数据
     */
    public DockOsdData dockOsd() {
        return dockOsd;
    }
}
