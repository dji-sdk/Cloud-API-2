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

package com.dji.fh.feature.device;

import com.dji.fh.kernel.spi.event.DeviceType;
import com.dji.fh.kernel.spi.event.OsdData;
import java.time.Instant;
import java.util.List;

/**
 * 单设备当前状态快照（不可变视图）。
 *
 * <p>由 {@link DeviceStatusService} 内部维护 mutable 状态，对外暴露时构造本 record。
 * REST API 直接序列化本 record（Jackson 原生支持 record）。
 *
 * @param sn            设备 SN
 * @param deviceType    设备类型（DRONE / DOCK）
 * @param model         设备型号（可能 null）
 * @param name          设备名称（可能 null）
 * @param online        当前是否在线
 * @param lastOsd       最近一帧 OSD（可能 null）
 * @param lastOsdAt     最近 OSD 更新时间（可能 null）
 * @param recentHms     最近 N 条 HMS 告警（有界，不可变 List）
 * @param onlineSince   上线时间（离线时 null）
 * @param childDevices  子设备列表（机场下挂载的飞行器），叶子设备为空列表
 * @author DJI DevKit Team
 */
public record DeviceState(
        String sn,
        DeviceType deviceType,
        String model,
        String name,
        boolean online,
        OsdData lastOsd,
        Instant lastOsdAt,
        List<HmsAlert> recentHms,
        Instant onlineSince,
        List<DeviceState> childDevices
) {}
