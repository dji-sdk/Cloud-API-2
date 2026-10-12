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
import type { DockStats } from '@/types/dashboard'

const props = defineProps<{ docks: DockStats }>()

interface StatusItem {
  label: string
  color: string
  count: number
  percent: string
}

const items = computed<StatusItem[]>(() => {
  const total = props.docks.total || 1
  return [
    { label: '空闲', color: '#3b82f6', count: props.docks.idle, percent: ((props.docks.idle / total) * 100).toFixed(1) },
    { label: '作业', color: '#3fb950', count: props.docks.working, percent: ((props.docks.working / total) * 100).toFixed(1) },
    { label: '调试', color: '#d29922', count: props.docks.debugging, percent: ((props.docks.debugging / total) * 100).toFixed(1) },
    { label: '离线', color: '#848d97', count: props.docks.offline, percent: ((props.docks.offline / total) * 100).toFixed(1) },
  ]
})
</script>

<template>
  <div class="dock-status">
    <h4 class="dock-status__title">机场</h4>
    <div v-for="item in items" :key="item.label" class="dock-status__row">
      <span class="dock-status__dot" :style="{ background: item.color }" />
      <span class="dock-status__label">{{ item.label }}</span>
      <span class="dock-status__value">{{ item.count }} ({{ item.percent }}%)</span>
    </div>
  </div>
</template>

<style scoped>
.dock-status__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 12px;
}

.dock-status__row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.dock-status__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dock-status__label {
  color: var(--app-text-secondary);
}

.dock-status__value {
  margin-left: auto;
  color: var(--app-text);
  font-variant-numeric: tabular-nums;
}
</style>
