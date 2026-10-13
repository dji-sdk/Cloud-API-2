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
import { watch, ref, onMounted } from 'vue'
import { useTerra } from '@/composables/useTerra'
import { deleteModel, batchDeleteModels } from '@/api/terra'
import TerraSidebar from '@/components/terra/TerraSidebar.vue'
import TerraToolbar from '@/components/terra/TerraToolbar.vue'
import TerraPagination from '@/components/terra/TerraPagination.vue'
import ModelGrid from '@/components/terra/ModelGrid.vue'
import ModelList from '@/components/terra/ModelList.vue'
import TaskCard from '@/components/terra/TaskCard.vue'
import ReconstructionPanel from '@/components/terra/reconstruction/ReconstructionPanel.vue'
import QualityReportModal from '@/components/terra/QualityReportModal.vue'
import ImageListModal from '@/components/terra/ImageListModal.vue'
import ModelViewerModal from '@/components/terra/ModelViewerModal.vue'

const {
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
  storageStats,
  loadModels,
  loadTasks,
  prependTask,
  connectSSE,
  disconnectSSE,
} = useTerra()

const selectedIds = ref<number[]>([])
const viewMode = ref<'grid' | 'list'>('grid')
const sidebarCollapsed = ref(false)
const showReconstructionPanel = ref(false)
const showReportModal = ref(false)
const showImagesModal = ref(false)
const showViewerModal = ref(false)
const activeTaskId = ref<string | null>(null)
const previewFileId = ref<number | null>(null)

function switchTab(tab: string) {
  const validTab = tab as typeof activeTab.value
  activeTab.value = validTab
  selectedIds.value = []
  if (validTab === 'tasks') {
    loadTasks()
    connectSSE()
  } else {
    disconnectSSE()
    loadModels(validTab)
  }
}

function handleSearch(keyword: string) {
  searchKeyword.value = keyword
}

function handleFilter(type: string | null) {
  if (type) {
    activeTab.value = type as typeof activeTab.value
    selectedIds.value = []
    loadModels(type)
  } else {
    loadModels(activeTab.value)
  }
}

function toggleSelect(fileId: number) {
  const idx = selectedIds.value.indexOf(fileId)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    selectedIds.value.push(fileId)
  }
}

function toggleSelectAll() {
  const list = filteredModels.value
  if (selectedIds.value.length === list.length) {
    selectedIds.value = []
  } else {
    selectedIds.value = list.map((m) => m.fileId)
  }
}

function handleDownload(_fileId: number) {
  alert('模型下载功能暂不支持，SDK 未提供有效的下载链接')
}

async function handleDelete(fileId: number) {
  if (!confirm('确定删除此模型？')) return
  try {
    await deleteModel(fileId)
    loadModels(activeTab.value)
  } catch (e) {
    console.error('删除失败:', e)
  }
}

async function handleBatchDelete() {
  if (selectedIds.value.length === 0) return
  if (!confirm(`确定删除选中的 ${selectedIds.value.length} 个模型？`)) return
  try {
    await batchDeleteModels(selectedIds.value)
    selectedIds.value = []
    loadModels(activeTab.value)
  } catch (e) {
    console.error('批量删除失败:', e)
  }
}

function openReport(taskId: string) {
  activeTaskId.value = taskId
  showReportModal.value = true
}

function openImages(taskId: string) {
  activeTaskId.value = taskId
  showImagesModal.value = true
}

function openPreview(fileId: number) {
  previewFileId.value = fileId
  showViewerModal.value = true
}

function handleCreated(task: { id: number; uuid: string; jobUuid: string; status: string }) {
  showReconstructionPanel.value = false
  activeTab.value = 'tasks'
  // 立即追加到任务列表头部，避免等待 SSE
  prependTask({
    taskId: task.uuid,
    state: task.status,
    progress: 0,
    mediaInputObjectKey: '',
    sfmOutputObjectPrefix: '',
  })
  loadTasks()
  connectSSE()
}

function handlePageChange(newPage: number) {
  page.value = newPage
}

function handlePageSizeChange(newSize: number) {
  pageSize.value = newSize
}

onMounted(() => {
  loadModels('ortho_2d')
})

watch(page, () => {
  selectedIds.value = []
  if (activeTab.value !== 'tasks') {
    loadModels(activeTab.value)
  } else {
    loadTasks()
  }
})

watch(pageSize, () => {
  page.value = 1
  selectedIds.value = []
  if (activeTab.value !== 'tasks') {
    loadModels(activeTab.value)
  } else {
    loadTasks()
  }
})
</script>

<template>
  <div class="models-page">
    <!-- Sidebar -->
    <TerraSidebar
      :storage-stats="storageStats"
      :active-tab="activeTab"
      :collapsed="sidebarCollapsed"
      @update:active-tab="switchTab"
      @update:collapsed="sidebarCollapsed = $event"
    />

    <!-- Main Content -->
    <div class="models-content">
      <!-- Toolbar -->
      <TerraToolbar
        :search-keyword="searchKeyword"
        :active-tab="activeTab"
        :selected-count="selectedIds.length"
        :view-mode="viewMode"
        @update:search-keyword="handleSearch"
        @update:view-mode="viewMode = $event"
        @create="showReconstructionPanel = true"
        @filter="handleFilter"
        @refresh="loadModels(activeTab)"
        @batch-delete="handleBatchDelete"
      />

      <!-- Model Content -->
      <div v-if="activeTab !== 'tasks'" class="content-body">
        <div v-if="loading" class="loading-state">
          <span class="loading-spinner" />
          <span>加载中...</span>
        </div>
        <div v-else-if="error" class="error-state">{{ error }}</div>
        <template v-else>
          <ModelGrid
            v-if="viewMode === 'grid'"
            :models="filteredModels"
            :selected-ids="selectedIds"
            :total="total"
            @select="toggleSelect"
            @select-all="toggleSelectAll"
            @preview="openPreview"
            @download="handleDownload"
            @delete="handleDelete"
            @view-report="openReport"
            @view-images="openImages"
          />
          <ModelList
            v-else
            :models="filteredModels"
            :selected-ids="selectedIds"
            :total="total"
            @select="toggleSelect"
            @select-all="toggleSelectAll"
            @preview="openPreview"
            @download="handleDownload"
            @delete="handleDelete"
            @view-report="openReport"
            @view-images="openImages"
          />
        </template>
      </div>

      <!-- Tasks Tab -->
      <div v-else class="content-body">
        <div v-if="tasks.length === 0" class="empty-state">暂无重建任务</div>
        <div v-else class="task-list">
          <TaskCard
            v-for="task in tasks"
            :key="task.taskId"
            :task="task"
            @view-report="openReport"
          />
        </div>
      </div>

      <!-- Pagination -->
      <TerraPagination
        v-if="activeTab !== 'tasks'"
        :page="page"
        :page-size="pageSize"
        :total="total"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <!-- Reconstruction Panel (full-screen overlay) -->
    <ReconstructionPanel
      :visible="showReconstructionPanel"
      @close="showReconstructionPanel = false"
      @created="handleCreated"
    />
    <QualityReportModal
      :visible="showReportModal"
      :task-id="activeTaskId"
      @close="showReportModal = false"
    />
    <ImageListModal
      :visible="showImagesModal"
      :task-id="activeTaskId"
      @close="showImagesModal = false"
    />
    <ModelViewerModal
      :visible="showViewerModal"
      :file-id="previewFileId"
      :models="filteredModels"
      @close="showViewerModal = false"
      @navigate="previewFileId = $event"
    />
  </div>
</template>

<style scoped>
.models-page {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: var(--app-bg);
}

.models-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: var(--space-xl);
  overflow: hidden;
}

.content-body {
  flex: 1;
  overflow-y: auto;
  padding-right: var(--space-xs);
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  padding: 48px 0;
  color: var(--app-text-muted);
  font-size: 14px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--app-border-strong);
  border-top-color: var(--app-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  text-align: center;
  padding: 48px 0;
  color: var(--app-danger);
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 48px 0;
  color: var(--app-text-muted);
  font-size: 14px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
