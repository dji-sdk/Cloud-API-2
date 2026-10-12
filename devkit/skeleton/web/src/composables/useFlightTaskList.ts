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

import { ref, reactive, watch } from 'vue'
import type { FlightTaskInfo, FlightTaskStatus } from '@/types/flightTask'
import { fetchFlightTaskList } from '@/api/flightTask'

/** 过滤条件。 */
export interface FlightTaskFilters {
  status: FlightTaskStatus | 'all'
}

/** 分页状态（与 MediaPagination 组件对齐）。 */
export interface FlightTaskPagination {
  startIndex: number
  pageSize: number
  totalCount: number
  hasMore: boolean
}

/**
 * 飞行任务列表状态管理 composable。
 *
 * 管理过滤、分页、加载状态，提供列表刷新等方法。
 */
export function useFlightTaskList() {
  const filters = reactive<FlightTaskFilters>({
    status: 'all',
  })

  const pagination = reactive<FlightTaskPagination>({
    startIndex: 0,
    pageSize: 20,
    totalCount: 0,
    hasMore: false,
  })

  const tasks = ref<FlightTaskInfo[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchTasks(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const resp = await fetchFlightTaskList({
        status: filters.status,
        startIndex: pagination.startIndex,
        pageSize: pagination.pageSize,
      })
      tasks.value = resp.tasks
      pagination.totalCount = resp.totalCount
      pagination.hasMore = resp.hasMore
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载飞行任务列表失败'
    } finally {
      loading.value = false
    }
  }

  function nextPage(): void {
    if (pagination.hasMore) {
      pagination.startIndex += pagination.pageSize
      fetchTasks()
    }
  }

  function prevPage(): void {
    if (pagination.startIndex > 0) {
      pagination.startIndex = Math.max(0, pagination.startIndex - pagination.pageSize)
      fetchTasks()
    }
  }

  function changePageSize(size: number): void {
    pagination.pageSize = size
    pagination.startIndex = 0
    fetchTasks()
  }

  watch(
    () => ({ ...filters }),
    () => {
      pagination.startIndex = 0
      fetchTasks()
    },
    { immediate: true },
  )

  return {
    filters,
    pagination,
    tasks,
    loading,
    error,
    fetchTasks,
    nextPage,
    prevPage,
    changePageSize,
  }
}
