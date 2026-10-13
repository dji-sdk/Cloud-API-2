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
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

interface Props {
  startIndex: number
  pageSize: number
  totalCount: number
  hasMore: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  nextPage: []
  prevPage: []
  changePageSize: [size: number]
}>()

const PAGE_SIZES = [20, 50, 100]

function currentRange(): string {
  if (props.totalCount === 0) {
    return '0 条'
  }
  const start = props.startIndex + 1
  const end = Math.min(props.startIndex + props.pageSize, props.totalCount)
  return `${start}-${end} / 共 ${props.totalCount} 条`
}
</script>

<template>
  <div class="media-pagination">
    <span class="pagination-info">{{ currentRange() }}</span>
    <div class="pagination-controls">
      <select
        class="page-size-select"
        :value="props.pageSize"
        @change="emit('changePageSize', Number(($event.target as HTMLSelectElement).value))"
      >
        <option v-for="size in PAGE_SIZES" :key="size" :value="size">
          {{ size }} 条/页
        </option>
      </select>
      <button
        class="page-btn"
        :disabled="props.startIndex === 0"
        @click="emit('prevPage')"
      >
        <ChevronLeft :size="14" :stroke-width="2" />
        <span>上一页</span>
      </button>
      <button
        class="page-btn"
        :disabled="!props.hasMore"
        @click="emit('nextPage')"
      >
        <span>下一页</span>
        <ChevronRight :size="14" :stroke-width="2" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.media-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-top: 1px solid var(--app-border);
  font-size: 13px;
}

.pagination-info {
  color: var(--app-text-secondary);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-select {
  padding: 5px 8px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 12px;
  outline: none;
  transition: border-color var(--transition-base);
}

.page-size-select:focus {
  border-color: var(--app-primary);
}

.page-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: transparent;
  color: var(--app-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.page-btn:hover:not(:disabled) {
  background: var(--app-hover-bg);
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
</style>
