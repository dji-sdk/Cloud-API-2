<!--
  Copyright 2025-2026 DJI. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import MediaSidebar from '@/components/media/MediaSidebar.vue'
import MediaToolbar from '@/components/media/MediaToolbar.vue'
import MediaFileTable from '@/components/media/MediaFileTable.vue'
import MediaPagination from '@/components/media/MediaPagination.vue'
import PhotoPreview from '@/components/media/PhotoPreview.vue'
import VideoPreview from '@/components/media/VideoPreview.vue'
import { useMediaList } from '@/composables/useMediaList'

const {
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
} = useMediaList()

const photoFiles = computed(() => files.value.filter(f => f.fileType === 'photo'))
const videoFiles = computed(() => files.value.filter(f => f.fileType === 'video'))

/** 当前打开的预览器类型，null 表示未打开。 */
const previewMode = ref<'photo' | 'video' | null>(null)
const previewIndex = ref(0)

/**
 * 打开预览。
 *
 * 入参是文件在整表中的下标，而预览组件接收的是同类型子集下标，需要换算。
 * `other` 类型无预览能力，直接忽略。
 */
function openPreview(fileIndex: number): void {
  const file = files.value[fileIndex]
  if (!file) {
    return
  }
  const siblings = file.fileType === 'photo'
    ? photoFiles.value
    : file.fileType === 'video' ? videoFiles.value : null
  if (!siblings) {
    return
  }
  const idx = siblings.findIndex(f => f.fileId === file.fileId)
  if (idx < 0) {
    return
  }
  previewIndex.value = idx
  previewMode.value = file.fileType
}

function closePreview(): void {
  previewMode.value = null
}

function handleBatchDelete(): void {
  const ids = [...selectedIds.value]
  if (ids.length === 0) {
    return
  }
  if (!window.confirm(`确认删除选中的 ${ids.length} 个文件？`)) {
    return
  }
  deleteFiles(ids)
}

function handleSingleDelete(fileId: number): void {
  if (!window.confirm('确认删除此文件？')) {
    return
  }
  deleteFiles([fileId])
}

async function handleBatchDownload(): Promise<void> {
  const ids = [...selectedIds.value]
  if (ids.length === 0) {
    return
  }
  await downloadFiles(ids)
}

function handleSelectAll(): void {
  const allSelected = files.value.length > 0 && files.value.every(f => selectedIds.value.has(f.fileId))
  if (allSelected) {
    clearSelection()
  } else {
    selectAll()
  }
}

onMounted(() => {
  fetchFiles()
})
</script>

<template>
  <div class="media-manager">
    <MediaToolbar
      :selected-count="selectedIds.size"
      :total-count="pagination.totalCount"
      :loading="loading"
      @batch-delete="handleBatchDelete"
      @batch-download="handleBatchDownload"
      @refresh="fetchFiles"
    />

    <div class="media-body">
      <MediaSidebar
        :active-type="filters.fileType"
        :device-sn="filters.deviceSn"
        :start-time="filters.startTime"
        :end-time="filters.endTime"
        @update:active-type="filters.fileType = $event"
        @update:device-sn="filters.deviceSn = $event"
        @update:start-time="filters.startTime = $event"
        @update:end-time="filters.endTime = $event"
      />

      <div class="media-content">
        <div v-if="error" class="error-banner">{{ error }}</div>

        <MediaFileTable
          :files="files"
          :selected-ids="selectedIds"
          :loading="loading"
          @toggle-select="toggleSelect"
          @select-all="handleSelectAll"
          @download="downloadFile"
          @delete="handleSingleDelete"
          @preview="openPreview"
        />

        <MediaPagination
          :start-index="pagination.startIndex"
          :page-size="pagination.pageSize"
          :total-count="pagination.totalCount"
          :has-more="pagination.hasMore"
          @next-page="nextPage"
          @prev-page="prevPage"
          @change-page-size="changePageSize"
        />
      </div>
    </div>

    <PhotoPreview
      v-if="previewMode === 'photo'"
      :photos="photoFiles"
      :current-index="previewIndex"
      @close="closePreview"
      @navigate="previewIndex = $event"
    />

    <VideoPreview
      v-else-if="previewMode === 'video'"
      :videos="videoFiles"
      :current-index="previewIndex"
      @close="closePreview"
      @navigate="previewIndex = $event"
    />
  </div>
</template>

<style scoped>
.media-manager {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.media-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.media-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.error-banner {
  padding: 10px 16px;
  background: rgba(229, 57, 53, 0.12);
  color: #ef5350;
  font-size: 13px;
  border-bottom: 1px solid rgba(229, 57, 53, 0.2);
}
</style>
