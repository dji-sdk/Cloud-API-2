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
import { formatDateTime } from '@/utils/format'
import type { WaylineFolder } from '@/types/reconstruction'
import { ChevronRight, MapPin, Folder, Loader } from 'lucide-vue-next'

const props = defineProps<{
  wayline: WaylineFolder
}>()

const emit = defineEmits<{
  toggleExecution: [waylineId: string, executionId: string]
  expand: [waylineId: string]
}>()

const gsdDisplay = computed(() => {
  if (props.wayline.orthoGsd) return `正射GSD：${props.wayline.orthoGsd}`
  if (props.wayline.inclineGsd) return `倾斜GSD：${props.wayline.inclineGsd}`
  return ''
})

const selectedCount = computed(
  () => props.wayline.executions.filter((e) => e.selected).length
)


function toggle() {
  emit('expand', props.wayline.id)
}
</script>

<template>
  <div class="wayline-item" :class="{ 'wayline-item--active': selectedCount > 0 }">
    <div class="wayline-header" @click="toggle">
      <ChevronRight
        :size="14"
        class="expand-icon"
        :class="{ 'expand-icon--open': wayline.expanded }"
      />
      <MapPin :size="14" class="type-icon" />
      <div class="wayline-info">
        <div class="wayline-name">{{ wayline.name }}</div>
        <div class="wayline-meta">
          <span v-if="gsdDisplay">{{ gsdDisplay }}</span>
          <span>最新采集时间：{{ formatDateTime(wayline.recentFlightAt) }}</span>
        </div>
      </div>
    </div>

    <div v-if="wayline.expanded" class="execution-list">
      <div v-if="wayline.loading" class="execution-loading">
        <Loader :size="14" class="spin" />
        <span>加载中...</span>
      </div>
      <template v-else-if="wayline.executions.length > 0">
        <label
          v-for="exec in wayline.executions"
          :key="exec.id"
          class="execution-item"
          :class="[
            { 'execution-item--selected': exec.selected },
            { 'execution-item--disabled': exec.available === false },
          ]"
          @click.stop
        >
          <input
            type="checkbox"
            :checked="exec.selected"
            :disabled="exec.available === false"
            @change="emit('toggleExecution', wayline.id, exec.id)"
          />
          <Folder :size="14" class="folder-icon" />
          <span class="exec-name">{{ exec.name }}</span>
          <span v-if="exec.available === false" class="exec-unavailable" title="该执行无可用媒体文件夹，无法重建">
            无媒体
          </span>
        </label>
      </template>
      <div v-else class="execution-empty">暂无执行记录</div>
    </div>
  </div>
</template>

<style scoped>
.wayline-item {
  border-bottom: 1px solid var(--app-border);
}

.wayline-item--active {
  background: var(--app-primary-bg);
}

.wayline-header {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 14px;
  cursor: pointer;
  transition: background var(--transition-base);
}

.wayline-header:hover {
  background: var(--app-hover-bg);
}

.expand-icon {
  margin-top: 2px;
  color: var(--app-text-muted);
  transition: transform var(--transition-base);
  flex-shrink: 0;
}

.expand-icon--open {
  transform: rotate(90deg);
}

.type-icon {
  margin-top: 2px;
  color: var(--app-text-muted);
  flex-shrink: 0;
}

.wayline-info {
  flex: 1;
  min-width: 0;
}

.wayline-name {
  font-size: 13px;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.wayline-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 11px;
  color: var(--app-text-muted);
}

.execution-list {
  padding: 0 14px 8px 36px;
}

.execution-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-base);
}

.execution-item:hover {
  background: var(--app-hover-bg);
}

.execution-item--selected {
  background: var(--app-primary-bg);
  border: 1px solid var(--app-primary);
  border-radius: var(--radius-sm);
}

.execution-item input[type="checkbox"] {
  accent-color: var(--app-primary);
  width: 15px;
  height: 15px;
  flex-shrink: 0;
}

.folder-icon {
  color: var(--app-text-muted);
  flex-shrink: 0;
}

.exec-name {
  font-size: 12px;
  color: var(--app-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.execution-item--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.execution-item--disabled input[type="checkbox"] {
  cursor: not-allowed;
}

.exec-unavailable {
  font-size: 11px;
  color: var(--app-text-muted);
  background: var(--app-hover-bg);
  padding: 1px 6px;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.execution-loading,
.execution-empty {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  font-size: 12px;
  color: var(--app-text-muted);
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
