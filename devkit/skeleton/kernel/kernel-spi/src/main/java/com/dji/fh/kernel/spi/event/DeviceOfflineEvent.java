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
 * 设备下线事件。
 *
 * @author DJI DevKit Team
 */
public final class DeviceOfflineEvent extends AbstractKernelEvent implements DeviceScopedEvent {

    private final String deviceSn;
    private final DeviceType deviceType;
    private final String reason;

    /**
     * @param source     触发来源
     * @param deviceSn   设备 SN
     * @param deviceType 设备类型
     * @param reason     下线原因（自由文本；常见值 "normal" / "timeout" / "error"）
     */
    public DeviceOfflineEvent(String source, String deviceSn, DeviceType deviceType, String reason) {
        super(source);
        this.deviceSn = deviceSn;
        this.deviceType = deviceType;
        this.reason = reason;
    }

    @Override
    public String deviceSn() {
        return deviceSn;
    }

    /**
     * @return 设备类型
     */
    public DeviceType deviceType() {
        return deviceType;
    }

    /**
     * @return 下线原因；可能为 null（SDK 未提供时）
     */
    public String reason() {
        return reason;
    }
}
