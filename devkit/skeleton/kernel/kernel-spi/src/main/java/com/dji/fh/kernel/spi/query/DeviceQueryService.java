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

package com.dji.fh.kernel.spi.query;

import java.util.Collections;
import java.util.List;

/**
 * 设备列表查询 SPI。
 *
 * <p>feature 层通过此接口实时查询当前工作空间的设备列表，不依赖事件驱动的内存缓存。
 * kernel-real-sdk 实现调 SDK {@code getDeviceList()}；kernel-mock-sdk 实现从内存注册表返回。
 *
 * @author DJI DevKit Team
 */
public interface DeviceQueryService {

    /**
     * 设备快照（SPI 层 record，不依赖 SDK bean）。
     *
     * @param sn          设备 SN
     * @param deviceClass 设备类别（"DOCK" / "DRONE" 等，可能为 null）
     * @param modelKey    设备型号 key（如 "1-67-0"，可能为 null）
     * @param deviceName  设备名称（可能为 null）
     * @param nickname    设备昵称（可能为 null）
     * @param online      是否在线
     * @param subDevices  子设备列表（机场下挂载的飞行器等）
     */
    record DeviceSnapshot(
            String sn,
            String deviceClass,
            String modelKey,
            String deviceName,
            String nickname,
            boolean online,
            List<DeviceSnapshot> subDevices
    ) {
        /**
         * 构造无子设备的快照。
         */
        public DeviceSnapshot(String sn, String deviceClass, String modelKey,
                              String deviceName, String nickname, boolean online) {
            this(sn, deviceClass, modelKey, deviceName, nickname, online, Collections.emptyList());
        }
    }

    /**
     * 查询当前设备列表（同步阻塞，带超时）。
     *
     * <p>返回的列表为扁平结构：顶层设备和子设备各自独立出现，同时通过
     * {@link DeviceSnapshot#subDevices()} 保留拓扑关系。
     *
     * @return 设备快照列表，查询失败时返回空列表
     */
    List<DeviceSnapshot> listDevices();
}
