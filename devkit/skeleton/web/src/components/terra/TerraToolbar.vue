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
import { ref } from 'vue'
import {
  Search,
  Filter,
  Plus,
  Upload,
  FolderPlus,
  Trash2,
  RefreshCw,
  LayoutGrid,
  List,
} from 'lucide-vue-next'

interface Props {
  searchKeyword: string
  activeTab: string
  selectedCount?: number
  viewMode?: 'grid' | 'list'
}

const props = withDefaults(defineProps<Props>(), {
  selectedCount: 0,
  viewMode: 'grid',
})

const emit = defineEmits<{
  'update:searchKeyword': [value: string]
  'update:viewMode': [value: 'grid' | 'list']
  create: []
  import: []
  createFolder: []
  filter: [type: string | null]
  refresh: []
  batchDelete: []
}>()

const filterOpen = ref(false)
const filterOptions = [
  { value: null, label: '全部类型' },
  { value: 'ortho_2d', label: '正射影像' },
  { value: 'mesh_3d', label: '三维网格' },
  { value: 'point_cloud', label: '点云' },
  { value: 'panorama', label: '全景' },
]

let debounceTimer: ReturnType<typeof setTimeout> | null = null

function handleSearch(event: Event) {
  const value = (event.target as HTMLInputElement).value
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    emit('update:searchKeyword', value)
  }, 300)
}

function selectFilter(value: string | null) {
  emit('filter', value)
  filterOpen.value = false
}
</script>

<template>
  <div class="terra-toolbar">
    <div class="toolbar-left">
      <button
        v-if="activeTab !== 'tasks'"
        class="toolbar-btn toolbar-btn--primary"
        @click="emit('create')"
      >
        <Plus :size="16" :stroke-width="2" />
        <span>模型重建</span>
      </button>
      <button class="toolbar-btn toolbar-btn--secondary" disabled>
        <Upload :size="16" :stroke-width="1.75" />
        <span>导入</span>
      </button>
      <button class="toolbar-btn toolbar-btn--secondary" disabled>
        <FolderPlus :size="16" :stroke-width="1.75" />
        <span>创建文件夹</span>
      </button>
    </div>

    <div class="toolbar-right">
      <!-- 批量删除 -->
      <button
        v-if="activeTab !== 'tasks' && selectedCount > 0"
        class="toolbar-btn toolbar-btn--danger"
        @click="emit('batchDelete')"
      >
        <Trash2 :size="16" :stroke-width="1.75" />
        <span>批量删除 ({{ selectedCount }})</span>
      </button>

      <!-- 刷新 -->
      <button
        v-if="activeTab !== 'tasks'"
        class="toolbar-btn toolbar-btn--icon"
        title="刷新"
        @click="emit('refresh')"
      >
        <RefreshCw :size="16" :stroke-width="1.75" />
      </button>

      <!-- 视图切换 -->
      <div v-if="activeTab !== 'tasks'" class="view-toggle">
        <button
          class="toolbar-btn toolbar-btn--icon"
          :class="{ 'toolbar-btn--icon-active': props.viewMode === 'grid' }"
          title="网格视图"
          @click="emit('update:viewMode', 'grid')"
        >
          <LayoutGrid :size="16" :stroke-width="1.75" />
        </button>
        <button
          class="toolbar-btn toolbar-btn--icon"
          :class="{ 'toolbar-btn--icon-active': props.viewMode === 'list' }"
          title="列表视图"
          @click="emit('update:viewMode', 'list')"
        >
          <List :size="16" :stroke-width="1.75" />
        </button>
      </div>

      <!-- 筛选 -->
      <div v-if="activeTab !== 'tasks'" class="filter-wrapper">
        <button class="toolbar-btn toolbar-btn--icon" @click="filterOpen = !filterOpen">
          <Filter :size="16" :stroke-width="1.75" />
        </button>
        <div v-if="filterOpen" class="filter-dropdown">
          <button
            v-for="opt in filterOptions"
            :key="String(opt.value)"
            class="filter-option"
            @click="selectFilter(opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <!-- 搜索 -->
      <div v-if="activeTab !== 'tasks'" class="search-wrapper">
        <Search :size="16" :stroke-width="1.75" class="search-icon" />
        <input
          type="text"
          class="search-input"
          placeholder="按文件名称搜索"
          :value="props.searchKeyword"
          @input="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.terra-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md) 0;
  gap: var(--space-lg);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.toolbar-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.toolbar-btn--primary {
  background: var(--app-primary);
  color: #fff;
  border-color: var(--app-primary);
}

.toolbar-btn--primary:hover {
  background: var(--app-primary-hover);
  border-color: var(--app-primary-hover);
}

.toolbar-btn--secondary {
  background: transparent;
  color: var(--app-text-secondary);
  border-color: var(--app-border-strong);
}

.toolbar-btn--secondary:hover:not(:disabled) {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.toolbar-btn--secondary:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.toolbar-btn--icon {
  background: transparent;
  color: var(--app-text-secondary);
  border-color: var(--app-border-strong);
  padding: 8px;
}

.toolbar-btn--icon:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.toolbar-btn--icon-active {
  background: var(--app-primary-bg);
  color: var(--app-primary);
  border-color: var(--app-primary);
}

.toolbar-btn--danger {
  background: transparent;
  color: var(--app-danger);
  border-color: var(--app-danger);
}

.toolbar-btn--danger:hover {
  background: rgba(239, 68, 68, 0.1);
}

.view-toggle {
  display: flex;
  gap: 2px;
}

.filter-wrapper {
  position: relative;
}

.filter-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  z-index: 20;
  min-width: 140px;
  padding: var(--space-xs) 0;
}

.filter-option {
  display: block;
  width: 100%;
  padding: 8px 14px;
  border: none;
  background: none;
  color: var(--app-text-secondary);
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-base);
}

.filter-option:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 10px;
  color: var(--app-text-muted);
  pointer-events: none;
}

.search-input {
  padding: 8px 12px 8px 34px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  width: 220px;
  outline: none;
  transition: border-color var(--transition-base), box-shadow var(--transition-base);
}

.search-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px var(--app-primary-bg);
}

.search-input::placeholder {
  color: var(--app-text-muted);
}
</style>
