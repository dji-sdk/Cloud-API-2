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

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { OfflineMapModelInfo, OfflineMapUpdateState, OfflineMapUpdateStatus } from '@/types/flightArea'
import {
  fetchOfflineMapModelInfo,
  fetchOfflineMapUpdateStatus,
  setOfflineMapEnabled,
  triggerOfflineMapUpdate,
  cancelOfflineMapUpdate,
  resetOfflineMapUpdateState,
} from '@/api/offlineMap'

/** 轮询间隔（毫秒） */
const POLL_INTERVAL_MS = 1000

/** 进度轮询需要继续的状态集合 */
const ACTIVE_STATUSES: OfflineMapUpdateStatus[] = ['WAITING', 'PROCESSING']

/** 409 表示后端已有更新在进行中 */
const HTTP_CONFLICT = '409'

export const useOfflineMapStore = defineStore('offlineMap', () => {
  const modelInfo = ref<OfflineMapModelInfo | null>(null)
  const loading = ref(false)
  const updateStatus = ref<OfflineMapUpdateStatus>('IDLE')
  const updateProgress = ref(0)
  const updateMessage = ref('')

  let pollTimer: ReturnType<typeof setInterval> | null = null

  async function loadModelInfo(): Promise<void> {
    loading.value = true
    try {
      modelInfo.value = await fetchOfflineMapModelInfo()
    } catch (e) {
      console.error('[offlineMap] loadModelInfo failed:', e)
      loading.value = false
      return
    }

    try {
      const state = await fetchOfflineMapUpdateStatus()
      applyUpdateState(state)
      if (ACTIVE_STATUSES.includes(state.status)) {
        startPolling()
      }
    } catch (e) {
      console.warn('[offlineMap] fetchUpdateStatus failed, treating as IDLE:', e)
    } finally {
      loading.value = false
    }
  }

  async function toggleEnabled(enabled: boolean): Promise<void> {
    await setOfflineMapEnabled(enabled)
    if (modelInfo.value) {
      modelInfo.value = { ...modelInfo.value, disabled: !enabled }
    }
  }

  async function triggerUpdate(): Promise<void> {
    try {
      await triggerOfflineMapUpdate()
      updateStatus.value = 'WAITING'
      updateProgress.value = 0
      updateMessage.value = '服务准备中...'
      startPolling()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '更新失败'
      if (isConflictError(msg)) {
        await syncUpdateStatus()
      } else {
        updateStatus.value = 'FAIL'
        updateMessage.value = msg
      }
    }
  }

  async function cancelUpdate(): Promise<void> {
    try {
      await cancelOfflineMapUpdate()
      stopPolling()
      updateStatus.value = 'CANCEL'
      updateProgress.value = 0
      updateMessage.value = '已取消'
    } catch {
      // 取消失败时不影响状态
    }
  }

  async function resetState(): Promise<void> {
    try {
      await resetOfflineMapUpdateState()
    } catch {
      // 静默失败
    }
    updateStatus.value = 'IDLE'
    updateProgress.value = 0
    updateMessage.value = ''
    await loadModelInfo()
  }

  function startPolling(): void {
    stopPolling()
    pollTimer = setInterval(async () => {
      try {
        const state = await fetchOfflineMapUpdateStatus()
        applyUpdateState(state)

        if (!ACTIVE_STATUSES.includes(state.status)) {
          stopPolling()
          if (state.status === 'FINISH') {
            modelInfo.value = await fetchOfflineMapModelInfo()
          }
        }
      } catch {
        stopPolling()
        updateStatus.value = 'FAIL'
        updateMessage.value = '状态查询失败'
      }
    }, POLL_INTERVAL_MS)
  }

  function stopPolling(): void {
    if (pollTimer !== null) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  function applyUpdateState(state: OfflineMapUpdateState): void {
    updateStatus.value = state.status
    updateProgress.value = state.progress
    updateMessage.value = state.message
  }

  /**
   * 409 表示更新已在进行中，此时不应标记为 FAIL，
   * 而是重新从后端同步状态。
   */
  async function syncUpdateStatus(): Promise<void> {
    try {
      const state = await fetchOfflineMapUpdateStatus()
      applyUpdateState(state)
      if (ACTIVE_STATUSES.includes(state.status)) {
        startPolling()
      }
    } catch {
      // 同步失败不影响
    }
  }

  function isConflictError(message: string): boolean {
    return message.includes(HTTP_CONFLICT) || message.includes('正在进行中')
  }

  return {
    modelInfo,
    loading,
    updateStatus,
    updateProgress,
    updateMessage,
    loadModelInfo,
    toggleEnabled,
    triggerUpdate,
    cancelUpdate,
    resetState,
    stopPolling,
  }
})
