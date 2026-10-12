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

import { ref, computed } from 'vue'
import type { WaylineFolder, FlightExecution, WaylineType } from '@/types/reconstruction'
import type { Folder } from '@/types/terra'
import type { WaylineInfo } from '@/types/wayline'
import { fetchWaylineList, fetchWaylineExecutions } from '@/api/wayline'

const WAYLINE_TYPE_MAP: Record<string, WaylineType> = {
  '面状航线': 'area',
  '斜面航线': 'incline',
  '几何体航线': 'incline',
  '带状航线': 'strip',
  '航点航线': 'ortho',
  '贴近摄影航线': 'ortho',
  'AI 巡逻航线': 'ortho',
}

function inferWaylineType(waylineTypeLabel: string): WaylineType {
  return WAYLINE_TYPE_MAP[waylineTypeLabel] ?? 'area'
}

/**
 * 重建面板数据管理 composable。
 *
 * 负责从 API 加载航线列表和执行列表，管理航线/文件夹两种选中状态。
 */
export function useReconstructionData() {
  const waylines = ref<WaylineFolder[]>([])
  const loading = ref(false)
  const error = ref('')

  /** 文件夹 tab 选中的 folder id 集合（数字） */
  const selectedFolderIds = ref<Set<number>>(new Set())

  const selectedCount = computed(() => {
    let count = 0
    for (const w of waylines.value) {
      for (const e of w.executions) {
        if (e.selected) count++
      }
    }
    return count
  })

  const selectedPhotoCount = computed(() => {
    let count = 0
    for (const w of waylines.value) {
      for (const e of w.executions) {
        if (e.selected) count += e.photoCount
      }
    }
    return count
  })

  const selectedIds = computed(() => {
    const ids: string[] = []
    for (const w of waylines.value) {
      for (const e of w.executions) {
        if (e.selected) ids.push(e.id)
      }
    }
    return ids
  })

  /** 选中的媒体文件夹 id（按航线选中时从 execution.folderId 收集） */
  const selectedMediaFolderIds = computed(() => {
    const set = new Set<number>()
    for (const w of waylines.value) {
      for (const e of w.executions) {
        if (e.selected && e.folderId != null) set.add(e.folderId)
      }
    }
    return Array.from(set)
  })

  async function loadWaylines(): Promise<void> {
    loading.value = true
    error.value = ''
    try {
      // 翻页拉取全部航线，避免只取第一页 100 条导致旧航线被截断
      const PAGE_SIZE = 100
      const all: WaylineInfo[] = []
      let page = 1
      let total = 0
      do {
        const res = await fetchWaylineList({ page, pageSize: PAGE_SIZE })
        all.push(...res.waylines)
        total = res.total
        page++
      } while (all.length < total)
      waylines.value = all.map((w) => ({
        id: w.uuid,
        name: w.name,
        type: inferWaylineType(w.waylineType),
        recentFlightAt: w.updatedAt != null && w.updatedAt > 0 ? w.updatedAt : 0,
        executions: [],
        loading: false,
        expanded: false,
        loaded: false,
      }))
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载航线列表失败'
    } finally {
      loading.value = false
    }
  }

  async function loadExecutions(waylineId: string): Promise<void> {
    const wl = waylines.value.find((w) => w.id === waylineId)
    if (!wl) return

    if (wl.loading) return

    if (wl.loaded) {
      wl.expanded = !wl.expanded
      return
    }

    wl.loading = true
    wl.expanded = true
    try {
      const res = await fetchWaylineExecutions(waylineId)
      wl.executions = res.executions.map((ex) => ({
        id: ex.id,
        name: ex.name,
        photoCount: ex.photoCount,
        createdAt: ex.createdAt,
        selected: false,
        // folderId 为媒体文件夹 id（用于提交）；有 media folder 且含照片才可重建
        folderId: ex.folderId ?? null,
        available: ex.folderId != null && ex.photoCount > 0,
      }))
    } catch (e) {
      wl.executions = []
      console.error('加载执行列表失败:', e)
    } finally {
      wl.loading = false
      wl.loaded = true
    }
  }

  function toggleExecution(waylineId: string, executionId: string): void {
    const wl = waylines.value.find((w) => w.id === waylineId)
    if (!wl) return
    const exec = wl.executions.find((e: FlightExecution) => e.id === executionId)
    if (!exec) return
    // 无可用媒体文件夹的执行不可选
    if (exec.available === false) return
    exec.selected = !exec.selected
  }

  /** 切换文件夹选中态 */
  function toggleFolder(folder: Folder): void {
    const next = new Set(selectedFolderIds.value)
    if (next.has(folder.folderId)) {
      next.delete(folder.folderId)
    } else {
      next.add(folder.folderId)
    }
    selectedFolderIds.value = next
  }

  /** 清空航线 tab 的所有选中 */
  function clearWaylineSelection(): void {
    for (const w of waylines.value) {
      for (const e of w.executions) {
        e.selected = false
      }
    }
  }

  /** 清空文件夹 tab 的所有选中 */
  function clearFolderSelection(): void {
    selectedFolderIds.value = new Set()
  }

  function getSelectionPayload(): { ids: Array<string | number>; folderIds: number[]; photoCount: number; folderCount: number } {
    if (selectedFolderIds.value.size > 0) {
      return {
        ids: Array.from(selectedFolderIds.value),
        folderIds: Array.from(selectedFolderIds.value),
        photoCount: 0,
        folderCount: selectedFolderIds.value.size,
      }
    }
    return {
      ids: selectedIds.value,
      folderIds: selectedMediaFolderIds.value,
      photoCount: selectedPhotoCount.value,
      folderCount: selectedCount.value,
    }
  }

  return {
    waylines,
    loading,
    error,
    selectedCount,
    selectedPhotoCount,
    selectedIds,
    selectedMediaFolderIds,
    selectedFolderIds,
    loadWaylines,
    loadExecutions,
    toggleExecution,
    toggleFolder,
    clearWaylineSelection,
    clearFolderSelection,
    getSelectionPayload,
  }
}
