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
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

interface Props {
  page: number
  pageSize: number
  total: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:page': [value: number]
  'update:pageSize': [value: number]
}>()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))
const hasPrev = computed(() => props.page > 1)
const hasNext = computed(() => props.page < totalPages.value)

function setPageSize(event: Event) {
  const value = Number((event.target as HTMLSelectElement).value)
  emit('update:pageSize', value)
  emit('update:page', 1)
}

function prevPage() {
  if (hasPrev.value) emit('update:page', props.page - 1)
}

function nextPage() {
  if (hasNext.value) emit('update:page', props.page + 1)
}
</script>

<template>
  <div v-if="total > 0" class="terra-pagination">
    <div class="pagination-left">
      <select class="page-size-select" :value="pageSize" @change="setPageSize">
        <option :value="20">20 / 页</option>
        <option :value="50">50 / 页</option>
        <option :value="100">100 / 页</option>
      </select>
    </div>

    <div class="pagination-center">
      <button class="page-btn" :disabled="!hasPrev" @click="prevPage">
        <ChevronLeft :size="16" />
      </button>
      <span class="page-indicator">{{ page }}</span>
      <button class="page-btn" :disabled="!hasNext" @click="nextPage">
        <ChevronRight :size="16" />
      </button>
    </div>

    <div class="pagination-right">
      <span class="page-info">共 {{ total }} 条</span>
      <span class="page-info">{{ totalPages }} 页</span>
    </div>
  </div>
</template>

<style scoped>
.terra-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md) 0;
  border-top: 1px solid var(--app-border);
  margin-top: auto;
}

.pagination-left,
.pagination-center,
.pagination-right {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.page-size-select {
  padding: 6px 10px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  font-size: 12px;
  outline: none;
  cursor: pointer;
}

.page-size-select:focus {
  border-color: var(--app-primary);
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
}

.page-btn:hover:not(:disabled) {
  background: var(--app-hover-bg);
  color: var(--app-text);
  border-color: var(--app-primary);
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-indicator {
  min-width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--app-primary-bg);
  color: var(--app-primary);
  font-size: 13px;
  font-weight: 500;
}

.page-info {
  font-size: 12px;
  color: var(--app-text-muted);
}
</style>
