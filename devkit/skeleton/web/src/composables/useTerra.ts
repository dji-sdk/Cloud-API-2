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

import { ref, computed, onUnmounted } from 'vue'
import type { ModelFile, PageResult, TaskStatus, StorageStats } from '@/types/terra'
import { fetchModels, fetchTasks } from '@/api/terra'
import { getToken } from '@/api/auth'
import { API_BASE } from '@/api/baseUrl'

/** Terra 模型管理业务状态 composable */
export function useTerra() {
  const models = ref<ModelFile[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const activeTab = ref<'ortho_2d' | 'mesh_3d' | 'panorama' | 'tasks'>('ortho_2d')
  const tasks = ref<TaskStatus[]>([])

  const searchKeyword = ref('')
  const filterType = ref<string | null>(null)

  const storageStats = ref<StorageStats>({
    mediaTotalGB: 600.5,
    modelTotalGB: 150.7,
    model2dGB: 46.5,
    model3dGB: 104.1,
    gcpCount: 0,
    recycleBinMB: 0,
  })

  let eventSource: EventSource | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null

  async function loadModels(type?: string) {
    loading.value = true
    error.value = null
    try {
      const result: PageResult<ModelFile> = await fetchModels(page.value, pageSize.value, type)
      models.value = result.list
      total.value = result.total
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载模型列表失败'
      console.error('[useTerra] loadModels 失败:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 加载重建任务列表（任务页初始数据）。
   * SSE 只推送增量变更，首次进入 / 刷新需主动拉取。
   */
  async function loadTasks() {
    loading.value = true
    error.value = null
    try {
      const result: PageResult<TaskStatus> = await fetchTasks(page.value, pageSize.value)
      tasks.value = result.list
      total.value = result.total
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载任务列表失败'
      console.error('[useTerra] loadTasks 失败:', e)
    } finally {
      loading.value = false
    }
  }

  /**
   * 将新建任务追加到列表头部（创建后立即显示，无需等 SSE）。
   */
  function prependTask(task: TaskStatus) {
    const idx = tasks.value.findIndex((t) => t.taskId === task.taskId)
    if (idx >= 0) {
      tasks.value[idx] = task
    } else {
      tasks.value.unshift(task)
    }
  }

  const filteredModels = computed(() => {
    let list = models.value
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.trim().toLowerCase()
      list = list.filter((m) => m.name.toLowerCase().includes(kw))
    }
    return list
  })

  function connectSSE() {
    disconnectSSE()
    // EventSource 无法设置 Authorization 头，通过 query param 传递 token（后端已支持回退）
    const token = getToken()
    const url = token
      ? `${API_BASE}/api/v1/terra/tasks/events?token=${encodeURIComponent(token)}`
      : `${API_BASE}/api/v1/terra/tasks/events`
    eventSource = new EventSource(url)
    eventSource.onmessage = (event: MessageEvent) => {
      const data = JSON.parse(event.data as string) as TaskStatus
      const idx = tasks.value.findIndex((t) => t.taskId === data.taskId)
      if (idx >= 0) {
        tasks.value[idx] = data
      } else {
        tasks.value.unshift(data)
      }
    }
    eventSource.onerror = () => {
      eventSource?.close()
      eventSource = null
      reconnectTimer = setTimeout(connectSSE, 5000)
    }
  }

  function disconnectSSE() {
    if (reconnectTimer !== null) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    eventSource?.close()
    eventSource = null
  }

  onUnmounted(() => {
    disconnectSSE()
  })

  return {
    models,
    filteredModels,
    total,
    page,
    pageSize,
    loading,
    error,
    activeTab,
    tasks,
    searchKeyword,
    filterType,
    storageStats,
    loadModels,
    loadTasks,
    prependTask,
    connectSSE,
    disconnectSSE,
  }
}
