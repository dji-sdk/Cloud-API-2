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
import type { ModelFile } from '@/types/terra'
import ModelCard from './ModelCard.vue'

const props = defineProps<{
  models: ModelFile[]
  selectedIds: number[]
  total: number
}>()

const emit = defineEmits<{
  select: [fileId: number]
  selectAll: []
  preview: [fileId: number]
  download: [fileId: number]
  delete: [fileId: number]
  viewReport: [taskId: string]
  viewImages: [taskId: string]
}>()

const allSelected = computed(
  () => props.models.length > 0 && props.models.every((m) => props.selectedIds.includes(m.fileId))
)

const indeterminate = computed(
  () => props.selectedIds.length > 0 && !allSelected.value
)
</script>

<template>
  <div class="model-grid-container">
    <!-- Content Header -->
    <div class="content-header">
      <label class="select-all">
        <input
          type="checkbox"
          :checked="allSelected"
          :indeterminate="indeterminate"
          @change="emit('selectAll')"
        />
        <span>全选</span>
      </label>
      <span class="select-count">{{ selectedIds.length }}/{{ total }}</span>
    </div>

    <!-- Grid -->
    <div class="model-grid">
      <ModelCard
        v-for="model in models"
        :key="model.fileId"
        :model="model"
        :selected="selectedIds.includes(model.fileId)"
        @select="emit('select', $event)"
        @preview="emit('preview', $event)"
        @download="emit('download', $event)"
        @delete="emit('delete', $event)"
        @view-report="emit('viewReport', $event)"
        @view-images="emit('viewImages', $event)"
      />
    </div>

    <div v-if="models.length === 0" class="empty-grid">
      暂无模型数据
    </div>
  </div>
</template>

<style scoped>
.model-grid-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-sm) 0;
  margin-bottom: var(--space-sm);
}

.select-all {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--app-text-secondary);
  cursor: pointer;
}

.select-all input[type="checkbox"] {
  width: 15px;
  height: 15px;
  accent-color: var(--app-primary);
  cursor: pointer;
}

.select-count {
  font-size: 12px;
  color: var(--app-text-muted);
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-lg);
}

.empty-grid {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--app-text-muted);
  font-size: 14px;
}
</style>
