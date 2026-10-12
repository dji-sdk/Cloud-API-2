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

/** 飞行区域类型 */
export type FlightAreaType = 'nfz' | 'dfence' | 'noland'

/** 飞行区域启用状态 */
export type FlightAreaStatus = 'enable' | 'disable'

/** 几何类型 */
export type FlightAreaGeometryType = 'Polygon' | 'Circle' | 'LineString'

/** GeoJSON Feature 内容 */
export interface FlightAreaContent {
  type: 'Feature'
  properties: {
    color: string
    clampToGround?: boolean
  }
  geometry: {
    type: FlightAreaGeometryType
    coordinates: number[] | number[][] | number[][][]
    radius?: number
  }
}

/** 飞行区域实体 */
export interface FlightArea {
  id: string
  name: string
  type: FlightAreaType
  status: FlightAreaStatus
  content: FlightAreaContent
  createdTime: number
  createdBy: string
  updatedTime: number
  updatedBy: string
}

/** 创建飞行区域请求 */
export interface CreateFlightAreaRequest {
  name: string
  type: FlightAreaType
  content: FlightAreaContent
}

/** 更新飞行区域请求 */
export interface UpdateFlightAreaRequest {
  name?: string
  content?: FlightAreaContent
}

/** 地形障碍物模型信息 */
export interface OfflineMapModelInfo {
  disabled: boolean
  version: string
  updateTime: number
  dataDescription: string
}

/** 地形障碍物数据更新状态 */
export type OfflineMapUpdateStatus =
  | 'IDLE'
  | 'WAITING'
  | 'PROCESSING'
  | 'FINISH'
  | 'FAIL'
  | 'CANCEL'
  | 'TIMEOUT'

/** 地形障碍物更新进度快照 */
export interface OfflineMapUpdateState {
  status: OfflineMapUpdateStatus
  progress: number
  message: string
}

/** TCAS 单类型阈值 */
export interface TcasThreshold {
  aircraftType: string
  horizontalWarning: number
  verticalWarning: number
  horizontalDanger: number
  verticalDanger: number
}

/** TCAS 配置 */
export interface TcasConfig {
  thresholds: TcasThreshold[]
}

/** 飞行区域类型的颜色映射 */
export const FLIGHT_AREA_COLORS: Record<FlightAreaType, string> = {
  dfence: '#00FF00',
  nfz: '#FF0000',
  noland: '#FF9900',
}

/** 禁用状态颜色 */
export const DISABLED_AREA_COLOR = '#606060'

/** 飞行区域类型中文名 */
export const FLIGHT_AREA_TYPE_LABELS: Record<FlightAreaType, string> = {
  dfence: '自定义作业区',
  nfz: '自定义限飞区',
  noland: '自定义禁降区',
}

/** 绘制产出的几何。Circle 保持独立类型，不采样为多边形，与 SDK/MCP fh_flight_area_manage 一致 */
export type DrawnGeometry =
  | { type: 'Polygon'; coordinates: number[][][] }
  | { type: 'Circle'; coordinates: [number, number]; radius: number }
