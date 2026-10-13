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
 * 设备上线事件。
 *
 * <p>由 Kernel SDK 适配器在收到 SDK 上线 callback 后触发。
 *
 * @author DJI DevKit Team
 */
public final class DeviceOnlineEvent extends AbstractKernelEvent implements DeviceScopedEvent {

    private final String deviceSn;
    private final DeviceType deviceType;
    private final String deviceModel;
    private final String deviceName;

    /**
     * @param source      触发来源
     * @param deviceSn    设备 SN
     * @param deviceType  设备类型（飞行器 / 机场）
     * @param deviceModel 设备型号
     * @param deviceName  设备名称
     */
    public DeviceOnlineEvent(String source, String deviceSn, DeviceType deviceType,
                             String deviceModel, String deviceName) {
        super(source);
        this.deviceSn = deviceSn;
        this.deviceType = deviceType;
        this.deviceModel = deviceModel;
        this.deviceName = deviceName;
    }

    @Override
    public String deviceSn() {
        return deviceSn;
    }

    /**
     * @return 设备类型（飞行器 / 机场）
     */
    public DeviceType deviceType() {
        return deviceType;
    }

    /**
     * @return 设备型号
     */
    public String deviceModel() {
        return deviceModel;
    }

    /**
     * @return 设备名称
     */
    public String deviceName() {
        return deviceName;
    }
}
