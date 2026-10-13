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

import type { ReconstructionType } from './terra'

export type WaylineType = 'ortho' | 'incline' | 'strip' | 'area'

export interface FlightExecution {
  /** 后端任务 UUID（唯一，作 :key） */
  id: string
  name: string
  photoCount: number
  createdAt: number
  selected: boolean
  /** 媒体文件夹 id（数字，重建提交时使用；无媒体文件夹时为 null） */
  folderId?: number | null
  /** 是否有可用媒体文件夹（folderId 非空且 photoCount>0 才可重建） */
  available?: boolean
}

export interface WaylineFolder {
  id: string
  name: string
  type: WaylineType
  orthoGsd?: string
  inclineGsd?: string
  recentFlightAt: number
  executions: FlightExecution[]
  loading?: boolean
  expanded?: boolean
  loaded?: boolean
}

export type OutputType = '2d' | '3d'

export interface ReconstructionFormData {
  name: string
  outputType: OutputType
  reconstructionType: ReconstructionType
  useGcp: boolean
  interestRegion: boolean
  coordinateSystem: string
  storageLocation: string
  wkt: string
}

export const WAYLINE_TYPE_LABELS: Record<WaylineType, string> = {
  ortho: '正射',
  incline: '倾斜',
  strip: '带状',
  area: '面状',
}
