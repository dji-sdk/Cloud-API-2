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

import type { WaylineInfo, WaylineListParams, WaylineListResponse, WaylineExecutionsResponse } from '@/types/wayline'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'
import { getRuntimeConfig } from '@/utils/runtimeConfig'

const WAYLINE_BASE = `${API_BASE}/api/v1/workspace/waylines`

function isMockEnabled(): boolean {
  return getRuntimeConfig('MOCK_ENABLED') === 'true'
}

const MOCK_WAYLINES: WaylineInfo[] = [
  {
    uuid: 'wl-001-abc',
    name: '厂区北侧巡检航线',
    droneType: 'Matrice 350 RTK',
    payloadType: 'Zenmuse L2',
    waylineType: '航点航线',
    waypointCount: 24,
    estimatedDistance: 3850,
    updatedAt: 1721808000,
  },
  {
    uuid: 'wl-002-def',
    name: '光伏板定期巡查航线',
    droneType: 'Matrice 30T',
    payloadType: 'Zenmuse H20T',
    waylineType: '带状航线',
    waypointCount: 56,
    estimatedDistance: 8200,
    updatedAt: 1721721600,
  },
  {
    uuid: 'wl-003-ghi',
    name: '仓库周边安防巡逻',
    droneType: 'Matrice 350 RTK',
    payloadType: 'Zenmuse H20N',
    waylineType: '航点航线',
    waypointCount: 18,
    estimatedDistance: 2400,
    updatedAt: 1721635200,
  },
  {
    uuid: 'wl-004-jkl',
    name: '河道水质监测航线',
    droneType: 'Matrice 30T',
    payloadType: 'Zenmuse H20T',
    waylineType: '航点航线',
    waypointCount: 32,
    estimatedDistance: 5600,
    updatedAt: 1721548800,
  },
  {
    uuid: 'wl-005-mno',
    name: '建筑工地进度航拍',
    droneType: 'Mavic 3E',
    payloadType: 'Mavic 3E Camera',
    waylineType: '贴近摄影航线',
    waypointCount: 120,
    estimatedDistance: 12300,
    updatedAt: 1721462400,
  },
  {
    uuid: 'wl-006-pqr',
    name: '高压线塔通道巡检',
    droneType: 'Matrice 350 RTK',
    payloadType: 'Zenmuse L2',
    waylineType: '航点航线',
    waypointCount: 42,
    estimatedDistance: 9500,
    updatedAt: 1721376000,
  },
  {
    uuid: 'wl-007-stu',
    name: '农田植保航线-东区',
    droneType: 'Matrice 30T',
    payloadType: 'Zenmuse H20T',
    waylineType: '带状航线',
    waypointCount: 88,
    estimatedDistance: 15200,
    updatedAt: 1721289600,
  },
  {
    uuid: 'wl-008-vwx',
    name: '园区道路巡查（定时）',
    droneType: 'Mavic 3T',
    payloadType: 'Mavic 3T Camera',
    waylineType: '航点航线',
    waypointCount: 15,
    estimatedDistance: 1800,
    updatedAt: 1721203200,
  },
  {
    uuid: 'wl-009-yza',
    name: '应急响应：山体滑坡勘测',
    droneType: 'Matrice 350 RTK',
    payloadType: 'Zenmuse P1',
    waylineType: '贴近摄影航线',
    waypointCount: 96,
    estimatedDistance: 10800,
    updatedAt: 1721116800,
  },
  {
    uuid: 'wl-010-bcd',
    name: '港口船舶停泊监控',
    droneType: 'Matrice 30T',
    payloadType: 'Zenmuse H20T',
    waylineType: '航点航线',
    waypointCount: 28,
    estimatedDistance: 4100,
    updatedAt: 1721030400,
  },
]

function filterMockWaylines(params: WaylineListParams, page: number, pageSize: number): WaylineListResponse {
  let filtered = [...MOCK_WAYLINES]

  if (params.droneType) {
    filtered = filtered.filter(w => w.droneType === params.droneType)
  }
  if (params.payloadType) {
    filtered = filtered.filter(w => w.payloadType === params.payloadType)
  }
  if (params.keyword) {
    const kw = params.keyword.toLowerCase()
    filtered = filtered.filter(w => w.name.toLowerCase().includes(kw))
  }

  const start = (page - 1) * pageSize
  const paged = filtered.slice(start, start + pageSize)

  return {
    waylines: paged,
    total: filtered.length,
    page,
    pageSize,
  }
}

/**
 * 检查航线是否存在。
 *
 * 用于飞行回放页预判航线是否已被删除，
 * 避免 FlyDataReplay SDK 因航线缺失整页崩溃。
 * 任何异常均降级返回 true，不阻断回放加载。
 */
export async function checkWaylineExists(uuid: string): Promise<boolean> {
  if (isMockEnabled()) {
    return MOCK_WAYLINES.some((w) => w.uuid === uuid)
  }
  try {
    const url = `${WAYLINE_BASE}/${encodeURIComponent(uuid)}/exists`
    const res = await authFetch(url)
    if (!res.ok) {
      return true
    }
    const data = (await res.json()) as { exists: boolean }
    return data.exists
  } catch {
    return true
  }
}

/** 落库校验的重试次数。拼接为异步落库，需覆盖短暂延迟。 */
const PERSIST_CHECK_MAX_ATTEMPTS = 3
/** 落库校验的重试间隔（毫秒）。 */
const PERSIST_CHECK_INTERVAL_MS = 800
/** 落库校验拉取的列表页大小。新航线 updatedAt 最新，默认排序下位于首页。 */
const PERSIST_CHECK_PAGE_SIZE = 30

/**
 * 按名称校验航线是否已落库。
 *
 * 背景：WaylineEditor SDK 的 concatWayline 返回的是内部句柄（如 987c2a07），
 * 并非平台落库的 wayline_uuid（平台侧统一为 36 位标准 UUID），因此无法用
 * checkWaylineExists 校验。且列表接口的名称过滤参数在平台侧不生效，
 * 必须拉取列表后在前端精确比对名称。
 *
 * 航线库不允许重名，故精确匹配即可唯一确定。
 *
 * @param name 航线名称
 * @returns 落库返回 true，重试耗尽仍未查到返回 false
 */
export async function verifyWaylinePersisted(name: string): Promise<boolean> {
  for (let attempt = 0; attempt < PERSIST_CHECK_MAX_ATTEMPTS; attempt++) {
    if (attempt > 0) {
      await new Promise((resolve) => setTimeout(resolve, PERSIST_CHECK_INTERVAL_MS))
    }
    try {
      const list = await fetchWaylineList({ page: 1, pageSize: PERSIST_CHECK_PAGE_SIZE })
      if (list.waylines.some((w) => w.name === name)) {
        return true
      }
    } catch {
      // 单次查询失败不终止重试，继续下一次
    }
  }
  return false
}

/** 查询航线库列表。 */
export async function fetchWaylineList(
  params: WaylineListParams = {},
): Promise<WaylineListResponse> {
  const page = params.page ?? 1
  const pageSize = params.pageSize ?? 20

  if (isMockEnabled()) {
    return filterMockWaylines(params, page, pageSize)
  }

  const query = new URLSearchParams()
  query.set('page', String(page))
  query.set('page_size', String(pageSize))

  if (params.droneType) {
    query.set('drone_type', params.droneType)
  }
  if (params.payloadType) {
    query.set('payload_type', params.payloadType)
  }
  if (params.keyword) {
    query.set('keyword', params.keyword)
  }

  const url = `${WAYLINE_BASE}?${query.toString()}`
  const res = await authFetch(url)
  if (!res.ok) {
    throw new Error('获取航线列表失败')
  }
  // 断言原因：后端约定返回 WaylineListResponse 结构，前端无法静态校验运行时 JSON
  return res.json() as Promise<WaylineListResponse>
}

/**
 * 从 Mock 数据中提取指定字段的唯一值列表。
 * 结果在模块加载时计算一次（数据为静态常量）。
 */
function getMockUniqueValues<K extends keyof WaylineInfo>(field: K): string[] {
  return [...new Set(MOCK_WAYLINES.map(w => String(w[field])))]
}

/** 获取可用机型列表（去重，仅 Mock 模式）。 */
export const getMockDroneTypes = (): string[] => getMockUniqueValues('droneType')

/** 获取可用负载列表（去重，仅 Mock 模式）。 */
export const getMockPayloadTypes = (): string[] => getMockUniqueValues('payloadType')

/** 批量删除航线。 */
// TODO: 后端 BFF 需实现 POST /api/v1/workspace/waylines/batch_delete 端点
export async function deleteWaylines(uuids: string[]): Promise<void> {
  if (isMockEnabled()) {
    const uuidSet = new Set(uuids)
    const remaining = MOCK_WAYLINES.filter(w => !uuidSet.has(w.uuid))
    MOCK_WAYLINES.length = 0
    MOCK_WAYLINES.push(...remaining)
    return
  }

  const res = await authFetch(`${WAYLINE_BASE}/batch_delete`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ wayline_uuids: uuids }),
  })
  if (!res.ok) {
    throw new Error('删除航线失败')
  }
}

interface DownloadItem {
  wayline_uuid: string
  wayline_name: string
  url: string
  size: number
}

interface BatchDownloadResponse {
  downloads: DownloadItem[]
}

/** 批量导出航线（下载 KMZ 文件）。 */
// TODO: 后端 BFF 需实现 POST /api/v1/workspace/waylines/batch_download 端点
export async function downloadWaylines(uuids: string[]): Promise<void> {
  if (isMockEnabled()) {
    const blob = new Blob(['mock-kmz-content'], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = uuids.length === 1 ? `${uuids[0]}.kmz` : 'waylines_export.zip'
    a.click()
    URL.revokeObjectURL(url)
    return
  }

  const res = await authFetch(`${WAYLINE_BASE}/batch_download`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ wayline_uuids: uuids }),
  })
  if (!res.ok) {
    const contentType = res.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const errBody = await res.json() as { message?: string }
      throw new Error(errBody.message || '导出航线失败')
    }
    throw new Error('导出航线失败')
  }

  // 断言原因：后端约定返回 {downloads: DownloadItem[]} 结构
  const data = await res.json() as BatchDownloadResponse
  for (const item of data.downloads) {
    const link = document.createElement('a')
    link.href = item.url
    link.download = item.wayline_name ? `${item.wayline_name}.kmz` : `${item.wayline_uuid}.kmz`
    link.click()
  }
}

const MAX_IMPORT_FILE_SIZE = 100 * 1024 * 1024

/** 导入航线（上传 KMZ 文件）。 */
// TODO: 后端 BFF 需实现 POST /api/v1/workspace/waylines/submit 端点
export async function importWayline(file: File, waylineName: string): Promise<string> {
  if (file.size > MAX_IMPORT_FILE_SIZE) {
    throw new Error(`文件大小超过限制（最大 ${MAX_IMPORT_FILE_SIZE / 1024 / 1024} MB）`)
  }

  if (isMockEnabled()) {
    const mockUuid = `wl-import-${Date.now()}`
    MOCK_WAYLINES.unshift({
      uuid: mockUuid,
      name: waylineName || file.name.replace(/\.kmz$/i, ''),
      droneType: 'Matrice 350 RTK',
      payloadType: 'Zenmuse L2',
      waylineType: '航点航线',
      waypointCount: 20,
      estimatedDistance: 3000,
      updatedAt: Math.floor(Date.now() / 1000),
    })
    return mockUuid
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('wayline_name', waylineName || file.name.replace(/\.kmz$/i, ''))

  const res = await authFetch(`${WAYLINE_BASE}/submit`, {
    method: 'POST',
    body: formData,
  })
  if (!res.ok) {
    const contentType = res.headers.get('content-type') ?? ''
    if (contentType.includes('application/json')) {
      const errBody = await res.json() as { message?: string }
      throw new Error(errBody.message || '导入航线失败')
    }
    throw new Error('导入航线失败')
  }
  // 断言原因：后端约定返回 {wayline_uuid: string} 结构
  const data = await res.json() as { wayline_uuid: string }
  return data.wayline_uuid
}

const MOCK_EXECUTIONS: Record<string, WaylineExecutionsResponse> = {
  'wl-001-abc': {
    waylineUuid: 'wl-001-abc',
    waylineName: '厂区北侧巡检航线',
    executions: [
      { id: 'ex-001-1', name: '巡检执行 2025-07-24 09:30', photoCount: 48, createdAt: '2025-07-24T09:30:00+08:00' },
      { id: 'ex-001-2', name: '巡检执行 2025-07-22 14:15', photoCount: 52, createdAt: '2025-07-22T14:15:00+08:00' },
    ],
  },
  'wl-002-def': {
    waylineUuid: 'wl-002-def',
    waylineName: '光伏板定期巡查航线',
    executions: [
      { id: 'ex-002-1', name: '光伏巡查 2025-07-23 10:00', photoCount: 120, createdAt: '2025-07-23T10:00:00+08:00' },
    ],
  },
  'wl-003-ghi': {
    waylineUuid: 'wl-003-ghi',
    waylineName: '仓库周边安防巡逻',
    executions: [
      { id: 'ex-003-1', name: '安防巡逻 2025-07-21 22:00', photoCount: 36, createdAt: '2025-07-21T22:00:00+08:00' },
      { id: 'ex-003-2', name: '安防巡逻 2025-07-20 22:00', photoCount: 38, createdAt: '2025-07-20T22:00:00+08:00' },
      { id: 'ex-003-3', name: '安防巡逻 2025-07-19 22:00', photoCount: 35, createdAt: '2025-07-19T22:00:00+08:00' },
    ],
  },
  'wl-004-jkl': {
    waylineUuid: 'wl-004-jkl',
    waylineName: '河道水质监测航线',
    executions: [
      { id: 'ex-004-1', name: '水质监测 2025-07-20 08:00', photoCount: 64, createdAt: '2025-07-20T08:00:00+08:00' },
    ],
  },
  'wl-005-mno': {
    waylineUuid: 'wl-005-mno',
    waylineName: '建筑工地进度航拍',
    executions: [
      { id: 'ex-005-1', name: '工地航拍 2025-07-19 15:00', photoCount: 240, createdAt: '2025-07-19T15:00:00+08:00' },
      { id: 'ex-005-2', name: '工地航拍 2025-07-12 15:00', photoCount: 235, createdAt: '2025-07-12T15:00:00+08:00' },
    ],
  },
}

/** 获取航线关联的飞行执行列表。 */
export async function fetchWaylineExecutions(
  waylineUuid: string,
): Promise<WaylineExecutionsResponse> {
  if (isMockEnabled()) {
    const mock = MOCK_EXECUTIONS[waylineUuid]
    if (mock) {
      return mock
    }
    return { waylineUuid, waylineName: '未知航线', executions: [] }
  }

  const url = `${WAYLINE_BASE}/${encodeURIComponent(waylineUuid)}/executions`
  const res = await authFetch(url)
  if (!res.ok) {
    throw new Error('获取航线执行列表失败')
  }
  return res.json() as Promise<WaylineExecutionsResponse>
}
