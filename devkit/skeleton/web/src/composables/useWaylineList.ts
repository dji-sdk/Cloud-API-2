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

import { ref, reactive, watch, computed } from 'vue'
import type { WaylineInfo } from '@/types/wayline'
import { fetchWaylineList } from '@/api/wayline'

/** 过滤条件。 */
export interface WaylineFilters {
  droneType: string
  payloadType: string
  keyword: string
}

/** 分页状态。 */
export interface WaylinePagination {
  page: number
  pageSize: number
  total: number
}

/** 后端允许的最大 pageSize（WaylineProperties.MAX_PAGE_SIZE = 100）。 */
const BACKEND_MAX_PAGE_SIZE = 100

/** 全量拉取最大页数（防止极端情况死循环）。 */
const MAX_FETCH_ALL_PAGES = 50

/**
 * 航线库列表状态管理 composable。
 *
 * 管理过滤、分页、多选、加载状态。
 * keyword 搜索：SDK 不支持服务端关键字过滤，改为前端全量拉取 + 本地过滤。
 */
export function useWaylineList() {
  const filters = reactive<WaylineFilters>({
    droneType: '',
    payloadType: '',
    keyword: '',
  })

  const pagination = reactive<WaylinePagination>({
    page: 1,
    pageSize: 20,
    total: 0,
  })

  const waylines = ref<WaylineInfo[]>([])
  const selectedUuids = ref<Set<string>>(new Set())
  const loading = ref(false)
  const error = ref<string | null>(null)
  let fetchId = 0

  /** 全量航线缓存（keyword 搜索时使用）。 */
  let allWaylinesCache: WaylineInfo[] = []
  /** 缓存对应的 droneType+payloadType 组合键，用于判断是否需要重新拉取。 */
  let cachedFilterKey: string | null = null
  /** 数据变更计数器，导入/删除后递增以强制缓存失效。 */
  let mutationCounter = 0

  /**
   * 构建缓存键：droneType + payloadType 变化时需要重新拉取全量数据。
   */
  function buildFilterKey(): string {
    return `${filters.droneType}|${filters.payloadType}|${mutationCounter}`
  }

  /**
   * 循环拉取所有航线（后端 pageSize 上限 100，需分页遍历）。
   */
  async function fetchAllWaylines(currentFetchId: number): Promise<WaylineInfo[]> {
    const all: WaylineInfo[] = []
    let page = 1

    while (page <= MAX_FETCH_ALL_PAGES) {
      const resp = await fetchWaylineList({
        page,
        pageSize: BACKEND_MAX_PAGE_SIZE,
        droneType: filters.droneType || undefined,
        payloadType: filters.payloadType || undefined,
      })
      if (currentFetchId !== fetchId) return []
      all.push(...resp.waylines)
      if (all.length >= resp.total || resp.waylines.length < BACKEND_MAX_PAGE_SIZE) {
        break
      }
      page++
    }
    return all
  }

  async function fetchWaylines(): Promise<void> {
    loading.value = true
    error.value = null
    const currentFetchId = ++fetchId
    try {
      const keyword = filters.keyword.trim()

      if (keyword) {
        const filterKey = buildFilterKey()
        if (cachedFilterKey !== filterKey) {
          allWaylinesCache = await fetchAllWaylines(currentFetchId)
          if (currentFetchId !== fetchId) return
          cachedFilterKey = filterKey
        }
        const kw = keyword.toLowerCase()
        const filtered = allWaylinesCache.filter((w) => w.name.toLowerCase().includes(kw))
        const start = (pagination.page - 1) * pagination.pageSize
        waylines.value = filtered.slice(start, start + pagination.pageSize)
        pagination.total = filtered.length
      } else {
        allWaylinesCache = []
        cachedFilterKey = null
        const resp = await fetchWaylineList({
          page: pagination.page,
          pageSize: pagination.pageSize,
          droneType: filters.droneType || undefined,
          payloadType: filters.payloadType || undefined,
        })
        if (currentFetchId !== fetchId) return
        waylines.value = resp.waylines
        pagination.total = resp.total
      }
    } catch (e) {
      if (currentFetchId !== fetchId) return
      error.value = e instanceof Error ? e.message : '加载航线列表失败'
    } finally {
      if (currentFetchId === fetchId) {
        loading.value = false
      }
    }
  }

  /** 总页数，最少为 1 页。 */
  const totalPages = computed(() => Math.max(1, Math.ceil(pagination.total / pagination.pageSize)))

  function nextPage(): void {
    if (pagination.page < totalPages.value) {
      pagination.page += 1
      clearSelection()
      fetchWaylines()
    }
  }

  function prevPage(): void {
    if (pagination.page > 1) {
      pagination.page -= 1
      clearSelection()
      fetchWaylines()
    }
  }

  function toggleSelect(uuid: string): void {
    const next = new Set(selectedUuids.value)
    if (next.has(uuid)) {
      next.delete(uuid)
    } else {
      next.add(uuid)
    }
    selectedUuids.value = next
  }

  /** 当前页全选状态。 */
  const isAllSelected = computed(
    () => waylines.value.length > 0 && selectedUuids.value.size === waylines.value.length,
  )

  function toggleSelectAll(): void {
    if (selectedUuids.value.size === waylines.value.length) {
      selectedUuids.value = new Set()
    } else {
      selectedUuids.value = new Set(waylines.value.map(w => w.uuid))
    }
  }

  function clearSelection(): void {
    selectedUuids.value = new Set()
  }

  /** 已选中航线的完整信息（computed，模板直接绑定，无需每次渲染重新过滤）。 */
  const selectedWaylines = computed(() =>
    waylines.value.filter(w => selectedUuids.value.has(w.uuid)),
  )

  /**
   * 使缓存失效并重新加载列表。
   * 航线导入/删除后调用此方法，确保 keyword 搜索使用最新数据。
   */
  function invalidateCache(): void {
    mutationCounter++
    allWaylinesCache = []
    cachedFilterKey = null
    fetchWaylines()
  }

  watch(
    filters,
    () => {
      pagination.page = 1
      clearSelection()
      fetchWaylines()
    },
    { immediate: true },
  )

  return {
    filters,
    pagination,
    totalPages,
    waylines,
    selectedUuids,
    isAllSelected,
    loading,
    error,
    fetchWaylines,
    nextPage,
    prevPage,
    toggleSelect,
    toggleSelectAll,
    clearSelection,
    selectedWaylines,
    invalidateCache,
  }
}
