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
import type { MediaFileInfo, MediaFilterType } from '@/types/media'
import { fetchMediaList, buildMediaContentUrl, probeMediaContent, batchDeleteMedia } from '@/api/media'
import { triggerDownload, delay, DOWNLOAD_INTERVAL_MS } from '@/utils/download'

/** 过滤条件。 */
export interface MediaFilters {
  fileType: MediaFilterType
  deviceSn: string
  startTime: string
  endTime: string
}

/** 分页状态。 */
export interface MediaPagination {
  startIndex: number
  pageSize: number
  totalCount: number
  hasMore: boolean
}

/**
 * 媒体列表页面状态管理 composable。
 *
 * 管理过滤、分页、选择、加载状态，提供列表刷新、下载、批量删除等方法。
 */
export function useMediaList() {
  const filters = reactive<MediaFilters>({
    fileType: 'all',
    deviceSn: '',
    startTime: '',
    endTime: '',
  })

  const pagination = reactive<MediaPagination>({
    startIndex: 0,
    pageSize: 50,
    totalCount: 0,
    hasMore: false,
  })

  const files = ref<MediaFileInfo[]>([])
  const selectedIds = ref<Set<number>>(new Set())
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchFiles(): Promise<void> {
    loading.value = true
    error.value = null
    try {
      const resp = await fetchMediaList({
        deviceSn: filters.deviceSn || undefined,
        fileType: filters.fileType,
        startTime: filters.startTime || undefined,
        endTime: filters.endTime || undefined,
        startIndex: pagination.startIndex,
        pageSize: pagination.pageSize,
      })
      files.value = resp.files
      pagination.totalCount = resp.totalCount
      pagination.hasMore = resp.hasMore
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载媒体列表失败'
    } finally {
      loading.value = false
    }
  }

  /**
   * 下载单个文件。
   *
   * 地址由本地构造，指向后端代理端点。不可用 SDK 的预签名 URL 直连 —— 它指向对象存储的
   * 内网 :30802，客户网段不可达，点击后只会静默失败。
   */
  async function downloadFile(fileId: number): Promise<void> {
    error.value = null
    try {
      await probeMediaContent(fileId)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '下载失败'
      return
    }
    triggerDownload(buildMediaContentUrl(fileId, true))
  }

  /**
   * 批量下载。
   *
   * 逐个错峰触发：浏览器对同源并发下载有上限，一次性全点会被丢弃一部分。
   * 触发后失败与否浏览器不再回传（下载走 `<a>` 导航而非 `fetch`），故失败只在预检阶段
   * 统计 —— 预检通过但下载中途断流的情况仍会被漏掉。
   */
  async function downloadFiles(fileIds: number[]): Promise<void> {
    error.value = null
    let failed = 0
    for (const [index, fileId] of fileIds.entries()) {
      try {
        await probeMediaContent(fileId)
        triggerDownload(buildMediaContentUrl(fileId, true))
      } catch {
        failed++
      }
      // 只在文件之间错峰；最后一个之后再等一轮毫无意义，只会拖慢调用方收尾
      if (index < fileIds.length - 1) {
        await delay(DOWNLOAD_INTERVAL_MS)
      }
    }
    if (failed > 0) {
      error.value = `${failed} 个文件不可用，已跳过下载`
    }
  }

  async function deleteFiles(fileIds: number[]): Promise<void> {
    loading.value = true
    error.value = null
    try {
      await batchDeleteMedia(fileIds)
      selectedIds.value.clear()
      await fetchFiles()
    } catch (e) {
      error.value = e instanceof Error ? e.message : '删除失败'
    } finally {
      loading.value = false
    }
  }

  function toggleSelect(fileId: number): void {
    if (selectedIds.value.has(fileId)) {
      selectedIds.value.delete(fileId)
    } else {
      selectedIds.value.add(fileId)
    }
  }

  function selectAll(): void {
    files.value.forEach(f => selectedIds.value.add(f.fileId))
  }

  function clearSelection(): void {
    selectedIds.value.clear()
  }

  function nextPage(): void {
    if (pagination.hasMore) {
      pagination.startIndex += pagination.pageSize
      fetchFiles()
    }
  }

  function prevPage(): void {
    if (pagination.startIndex > 0) {
      pagination.startIndex = Math.max(0, pagination.startIndex - pagination.pageSize)
      fetchFiles()
    }
  }

  function changePageSize(size: number): void {
    pagination.pageSize = size
    pagination.startIndex = 0
    fetchFiles()
  }

  watch(
    () => ({ ...filters }),
    () => {
      pagination.startIndex = 0
      selectedIds.value.clear()
      fetchFiles()
    },
  )

  return {
    filters,
    pagination,
    files,
    selectedIds,
    loading,
    error,
    fetchFiles,
    downloadFile,
    downloadFiles,
    deleteFiles,
    toggleSelect,
    selectAll,
    clearSelection,
    nextPage,
    prevPage,
    changePageSize,
  }
}
