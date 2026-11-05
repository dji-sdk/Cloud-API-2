/**
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

// Open schema for OSD telemetry. REST serialization: { lastOsd: { attributes: { key: value } } }
// Common keys: lat, lng, altitude, battery_percent, speed, mode, etc. (set by SDK mapping layer)
export interface OsdData {
  attributes: Record<string, unknown>
}

export interface HmsAlert {
  alertCode: string                                  // Java: alertCode field
  level: 'INFO' | 'WARNING' | 'ERROR' | 'CRITICAL'  // Java: HmsAlertLevel enum (all 4 values)
  message?: string                                   // Java: message (nullable)
  timestamp?: string                                 // Java: Instant serialized as ISO 8601
}

export type DeviceType = 'DRONE' | 'DOCK'

export interface DeviceState {
  sn: string
  deviceType: DeviceType
  // SDK 原始设备类别（dock / base_station / drone），用于区分 deviceType 无法区分的基站与机场；
  // 事件缓存兜底路径下后端可能返回 null/缺省，消费方需回退到 deviceType
  deviceClass?: string | null
  model?: string
  name?: string
  online: boolean
  lastOsd?: OsdData
  lastOsdAt?: string   // ISO 8601
  recentHms: HmsAlert[]
  onlineSince?: string // ISO 8601
  childDevices?: DeviceState[]
}
