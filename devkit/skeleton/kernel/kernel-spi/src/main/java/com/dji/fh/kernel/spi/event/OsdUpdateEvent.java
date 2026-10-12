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
 * 飞行器 OSD 推送事件。
 *
 * <p>高频事件（典型 1~10 Hz），listener 应保持轻量。
 *
 * @author DJI DevKit Team
 */
public final class OsdUpdateEvent extends AbstractKernelEvent implements DeviceScopedEvent {

    private final String deviceSn;
    private final OsdData osd;

    /**
     * @param source   触发来源
     * @param deviceSn 设备 SN
     * @param osd      OSD 数据；不得为 null（上游 adapter 应保证）
     */
    public OsdUpdateEvent(String source, String deviceSn, OsdData osd) {
        super(source);
        this.deviceSn = Objects.requireNonNull(deviceSn, "deviceSn 不能为空");
        this.osd = Objects.requireNonNull(osd, "osd 不能为空");
    }

    @Override
    public String deviceSn() {
        return deviceSn;
    }

    /**
     * @return OSD 数据
     */
    public OsdData osd() {
        return osd;
    }
}
