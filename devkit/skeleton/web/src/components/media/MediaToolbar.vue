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
import { Download, RefreshCw, Trash2 } from 'lucide-vue-next'

interface Props {
  selectedCount: number
  totalCount: number
  loading: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  batchDelete: []
  batchDownload: []
  refresh: []
}>()
</script>

<template>
  <div class="media-toolbar">
    <div class="toolbar-left">
      <template v-if="selectedCount > 0">
        <span class="selected-badge">已选 {{ selectedCount }} 项</span>
        <span class="toolbar-divider" />
        <button class="btn btn--primary-ghost" @click="emit('batchDownload')">
          <Download :size="13" :stroke-width="2" />
          <span>批量下载</span>
        </button>
        <button class="btn btn--danger" @click="emit('batchDelete')">
          <Trash2 :size="13" :stroke-width="2" />
          <span>批量删除</span>
        </button>
      </template>
      <template v-else>
        <span class="file-count">共 {{ totalCount }} 个文件</span>
      </template>
    </div>
    <div class="toolbar-right">
      <button class="btn btn--ghost" :disabled="loading" @click="emit('refresh')">
        <RefreshCw :size="13" :stroke-width="2" :class="{ 'spin': loading }" />
        <span>{{ loading ? '加载中' : '刷新' }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.media-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--app-border);
  min-height: 44px;
  background: var(--app-bg-panel);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-badge {
  font-size: 13px;
  color: var(--app-primary);
  font-weight: 500;
}

.file-count {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.toolbar-divider {
  width: 1px;
  height: 16px;
  background: var(--app-border-strong);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn--primary-ghost {
  background: var(--app-primary-bg);
  color: var(--app-primary);
}

.btn--primary-ghost:hover:not(:disabled) {
  background: var(--app-primary-bg-hover);
}

.btn--danger {
  background: rgba(239, 68, 68, 0.12);
  color: var(--app-danger);
}

.btn--danger:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.20);
}

.btn--ghost {
  background: transparent;
  color: var(--app-text-secondary);
  border: 1px solid var(--app-border);
}

.btn--ghost:hover:not(:disabled) {
  background: var(--app-hover-bg);
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spin {
  animation: spin 1s linear infinite;
}
</style>
