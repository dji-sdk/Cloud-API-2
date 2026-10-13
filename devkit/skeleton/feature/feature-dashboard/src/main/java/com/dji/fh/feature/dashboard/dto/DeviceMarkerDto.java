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

package com.dji.fh.feature.dashboard.dto;

/**
 * 设备地图标记 DTO。
 *
 * @param sn     设备 SN
 * @param type   设备类型（dock / drone）
 * @param name   设备名称
 * @param status 设备状态
 * @param lat    纬度
 * @param lng    经度
 * @author DJI DevKit Team
 */
public record DeviceMarkerDto(String sn, String type, String name, String status, double lat, double lng) {}
