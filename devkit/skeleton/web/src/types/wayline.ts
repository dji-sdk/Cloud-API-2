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

/** 航线信息，对齐 workspace/wayline 列表接口返回结构。 */
export interface WaylineInfo {
  uuid: string
  name: string
  droneType: string
  payloadType: string
  /** 航线类型：航点航线、面状航线、带状航线等（SDK 列表已映射为中文标签） */
  waylineType: string
  /** 航点数量（服务端补拉航线详情填充，失败时为空，前端显示「—」） */
  waypointCount?: number
  /** 预计飞行距离（米），服务端补拉航线详情填充，失败时为空 */
  estimatedDistance?: number
  /** Unix timestamp（秒），更新时间 */
  updatedAt: number
}

/** 航线列表查询参数。 */
export interface WaylineListParams {
  page?: number
  pageSize?: number
  droneType?: string
  payloadType?: string
  keyword?: string
}

/** 航线列表响应。 */
export interface WaylineListResponse {
  waylines: WaylineInfo[]
  total: number
  page: number
  pageSize: number
}

/** 单次飞行执行信息（媒体文件夹）。 */
export interface WaylineExecutionItem {
  /** 任务 UUID（唯一，作 :key） */
  id: string
  name: string
  photoCount: number
  /** Unix timestamp（秒），执行时间 */
  createdAt: number
  /** 媒体文件夹 id（数字，重建提交用；无媒体文件夹时为 null） */
  folderId?: number | null
}

/** 航线关联的飞行执行列表响应。 */
export interface WaylineExecutionsResponse {
  waylineUuid: string
  waylineName: string
  executions: WaylineExecutionItem[]
}
