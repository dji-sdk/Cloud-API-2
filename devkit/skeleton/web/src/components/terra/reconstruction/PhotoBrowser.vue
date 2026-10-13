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
import { onMounted, computed, ref, watch } from 'vue'
import type { Folder } from '@/types/terra'
import { useReconstructionData } from '@/composables/useReconstructionData'
import { Search, ArrowUpDown, Map, Layers, Grid3x3, ScanLine, Loader, FolderOpen } from 'lucide-vue-next'
import WaylineItem from './WaylineItem.vue'

const props = defineProps<{
  folders: Folder[]
  foldersError: string
}>()

const emit = defineEmits<{
  selectionChange: [payload: { ids: Array<string | number>; folderIds: number[]; photoCount: number; folderCount: number }]
  reloadFolders: []
}>()

const {
  waylines,
  loading,
  error,
  selectedCount,
  selectedPhotoCount,
  selectedFolderIds,
  loadWaylines,
  loadExecutions,
  toggleExecution,
  toggleFolder,
  clearWaylineSelection,
  clearFolderSelection,
  getSelectionPayload,
} = useReconstructionData()

const activeTab = ref<'wayline' | 'folder'>('wayline')
const searchKeyword = ref('')
const sortOrder = ref<'desc' | 'asc'>('desc')

/** 切换 tab 时清空另一个 tab 的选择 */
watch(activeTab, (tab) => {
  if (tab === 'wayline') {
    clearFolderSelection()
  } else {
    clearWaylineSelection()
  }
  emit('selectionChange', getSelectionPayload())
})

const filteredWaylines = computed(() => {
  let list = waylines.value
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((w) => w.name.toLowerCase().includes(kw))
  }
  list = [...list].sort((a, b) => {
    const ta = new Date(a.recentFlightAt).getTime()
    const tb = new Date(b.recentFlightAt).getTime()
    return sortOrder.value === 'desc' ? tb - ta : ta - tb
  })
  return list
})

const filteredFolders = computed(() => {
  if (!searchKeyword.value.trim()) return props.folders
  const kw = searchKeyword.value.toLowerCase()
  return props.folders.filter((f) => (f.folderName ?? '').toLowerCase().includes(kw))
})

function handleToggleExecution(waylineId: string, executionId: string) {
  toggleExecution(waylineId, executionId)
  emit('selectionChange', getSelectionPayload())
}

function handleToggleFolder(folder: Folder) {
  toggleFolder(folder)
  emit('selectionChange', getSelectionPayload())
}

function handleExpand(waylineId: string) {
  loadExecutions(waylineId)
}

function toggleSort() {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
}

onMounted(() => {
  loadWaylines()
})
</script>

<template>
  <div class="photo-browser">
    <!-- Header -->
    <div class="browser-header">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'wayline' }"
        @click="activeTab = 'wayline'"
      >
        按航线选择
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'folder' }"
        @click="activeTab = 'folder'"
      >
        按文件夹选择
      </button>
    </div>

    <!-- Filter bar -->
    <div class="filter-bar">
      <div class="filter-icons">
        <button class="icon-btn" title="面状"><Grid3x3 :size="16" /></button>
        <button class="icon-btn" title="倾斜"><Layers :size="16" /></button>
        <button class="icon-btn" title="带状"><ScanLine :size="16" /></button>
        <button class="icon-btn" title="正射"><Map :size="16" /></button>
      </div>
      <button class="icon-btn sort-btn" title="时间排序" @click="toggleSort">
        <ArrowUpDown :size="14" />
        <span class="sort-label">时间排序</span>
      </button>
      <div class="search-box">
        <Search :size="14" class="search-icon" />
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索"
          class="search-input"
        />
      </div>
    </div>

    <!-- Selection summary -->
    <div v-if="selectedCount > 0 || selectedFolderIds.size > 0" class="selection-summary">
      <template v-if="activeTab === 'wayline'">
        已选 {{ selectedCount }} 个执行，{{ selectedPhotoCount }} 张照片
      </template>
      <template v-else>
        已选 {{ selectedFolderIds.size }} 个文件夹
      </template>
    </div>

    <!-- Content list -->
    <div class="wayline-list">
      <!-- 按航线选择 tab -->
      <template v-if="activeTab === 'wayline'">
        <div v-if="loading" class="loading-state">
          <Loader :size="18" class="spin" />
          <span>加载航线列表...</span>
        </div>
        <div v-else-if="error" class="error-state">{{ error }}</div>
        <template v-else>
          <WaylineItem
            v-for="wl in filteredWaylines"
            :key="wl.id"
            :wayline="wl"
            @toggle-execution="handleToggleExecution"
            @expand="handleExpand"
          />
          <div v-if="filteredWaylines.length === 0" class="empty-state">
            无匹配航线
          </div>
        </template>
      </template>

      <!-- 按文件夹选择 tab -->
      <template v-else>
        <div v-if="foldersError" class="error-state">
          <span>{{ foldersError }}</span>
          <button class="retry-btn" @click="$emit('reload-folders')">重试</button>
        </div>
        <div v-else-if="filteredFolders.length === 0" class="empty-state">
          暂无可用文件夹
        </div>
        <label
          v-for="folder in filteredFolders"
          :key="folder.folderId"
          class="folder-item"
          :class="{ selected: selectedFolderIds.has(folder.folderId) }"
        >
          <input
            type="checkbox"
            :checked="selectedFolderIds.has(folder.folderId)"
            @change="handleToggleFolder(folder)"
          />
          <FolderOpen :size="16" class="folder-icon" />
          <span class="folder-name" :title="folder.folderName ?? ''">{{ folder.folderName ?? '' }}</span>
        </label>
      </template>
    </div>
  </div>
</template>

<style scoped>
.photo-browser {
  width: 380px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--app-border);
  background: var(--app-bg-panel);
  overflow: hidden;
}

.browser-header {
  display: flex;
  padding: 0;
  border-bottom: 1px solid var(--app-border);
}

.tab-btn {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: none;
  color: var(--app-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-base);
  border-bottom: 2px solid transparent;
}

.tab-btn.active {
  color: var(--app-primary);
  border-bottom-color: var(--app-primary);
  background: var(--app-primary-bg);
}

.tab-btn:hover:not(.active) {
  background: var(--app-hover-bg);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--app-border);
}

.filter-icons {
  display: flex;
  gap: 4px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 6px;
  border: none;
  background: none;
  color: var(--app-text-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: all var(--transition-base);
}

.icon-btn:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.sort-btn {
  margin-left: auto;
  font-size: 11px;
}

.sort-label {
  font-size: 11px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 6px;
  color: var(--app-text-muted);
  pointer-events: none;
}

.search-input {
  padding: 5px 8px 5px 24px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 12px;
  width: 80px;
  outline: none;
  transition: all var(--transition-base);
}

.search-input:focus {
  width: 120px;
  border-color: var(--app-primary);
}

.search-input::placeholder {
  color: var(--app-text-muted);
}

.selection-summary {
  padding: 8px 14px;
  font-size: 12px;
  color: var(--app-primary);
  background: var(--app-primary-bg);
  border-bottom: 1px solid var(--app-border);
}

.wayline-list {
  flex: 1;
  overflow-y: auto;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.error-state {
  text-align: center;
  padding: 48px 0;
  color: var(--app-danger);
  font-size: 13px;
}

.retry-btn {
  margin-top: 8px;
  padding: 4px 14px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: var(--app-bg-panel);
  color: var(--app-primary);
  font-size: 12px;
  cursor: pointer;
}

.retry-btn:hover {
  background: var(--app-hover-bg);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.spin {
  animation: spin 1s linear infinite;
}

.folder-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  border-bottom: 1px solid var(--app-border);
  transition: background var(--transition-base);
}

.folder-item:hover {
  background: var(--app-hover-bg);
}

.folder-item.selected {
  background: var(--app-primary-bg);
}

.folder-icon {
  color: var(--app-text-muted);
  flex-shrink: 0;
}

.folder-item.selected .folder-icon {
  color: var(--app-primary);
}

.folder-name {
  font-size: 13px;
  color: var(--app-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
