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

/** 机场状态汇总 */
export interface DockStats {
  total: number
  idle: number
  working: number
  debugging: number
  offline: number
}

/** 飞行器汇总 */
export interface DroneStats {
  total: number
  online: number
  offline: number
}

/** 中继站汇总（mock） */
export interface RelayStats {
  total: number
  online: number
  offline: number
  _mock: true
}

/** 计数指标 */
export interface CountStats {
  waylines: number
  personnel: { value: number; _mock: boolean }
}

/** 数据成果 */
export interface DataResultStats {
  media: { total: number; trend: number[] }
  models: { total: number; trend: number[] }
  reports: { total: number; trend: number[]; _mock: true }
}

/** 日维度任务分布 */
export interface TaskDailyEntry {
  date: string
  planned: number
  manual: number
}

/** 日维度执行结果 */
export interface ExecutionDailyEntry {
  date: string
  success: number
  suspended: number
  failed: number
}

/** 日维度飞行数据 */
export interface FlightDailyEntry {
  date: string
  count: number
  minutes: number
  km: number
}

/**
 * 飞行数据汇总。
 *
 * 次数与时长来自 SDK 真实数据；里程由逐任务轨迹距离累加得出，当前以 `_mock`
 * 标记（SDK 查询成本待联调核实），由前端渲染为占位符，mock 时不绘制趋势图。
 */
export interface FlightStats {
  count: number
  durationMinutes: number
  distanceKm: { value: number; _mock: boolean }
  daily: FlightDailyEntry[]
}

/** 设备地图标注点 */
export interface DeviceMarker {
  sn: string
  type: 'dock' | 'drone'
  name: string
  status: string
  lat: number
  lng: number
}

/** 看板聚合响应 */
export interface DashboardStats {
  docks: DockStats
  drones: DroneStats
  relays: RelayStats
  counts: CountStats
  dataResults: DataResultStats
  tasks: { daily: TaskDailyEntry[] }
  execution: { daily: ExecutionDailyEntry[] }
  flight: FlightStats
  devices: DeviceMarker[]
}

/** 时间范围选项 */
export type TimeRange = 'week' | 'month' | 'recent_week' | 'recent_month' | 'half_year' | 'year'
