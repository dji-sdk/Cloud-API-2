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
import { ref, onMounted } from 'vue'
import type { DashboardStats, TimeRange } from '@/types/dashboard'
import { fetchDashboardStats } from '@/api/dashboard'
import AssetPanel from '@/components/dashboard/AssetPanel.vue'
import DeviceMap from '@/components/dashboard/DeviceMap.vue'
import TaskPanel from '@/components/dashboard/TaskPanel.vue'
import TimeRangeSelector from '@/components/dashboard/TimeRangeSelector.vue'

const stats = ref<DashboardStats | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const timeRange = ref<TimeRange>('month')

async function loadStats(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    stats.value = await fetchDashboardStats(timeRange.value)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function handleRangeChange(range: TimeRange): void {
  timeRange.value = range
  loadStats()
}

onMounted(() => {
  loadStats()
})
</script>

<template>
  <div class="dashboard-layout">
    <aside class="dashboard-layout__left">
      <AssetPanel v-if="stats" :stats="stats" />
    </aside>

    <main class="dashboard-layout__center">
      <TimeRangeSelector
        :model-value="timeRange"
        class="dashboard-layout__time-selector"
        @update:model-value="handleRangeChange"
      />
      <DeviceMap v-if="stats" :devices="stats.devices" />
      <div v-if="loading" class="dashboard-layout__loading">加载中...</div>
      <div v-if="error" class="dashboard-layout__error">{{ error }}</div>
    </main>

    <aside class="dashboard-layout__right">
      <TaskPanel v-if="stats" :stats="stats" />
    </aside>
  </div>
</template>

<style scoped>
.dashboard-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  background: var(--app-bg);
}

.dashboard-layout__left,
.dashboard-layout__right {
  flex-shrink: 0;
  overflow-y: auto;
  background: var(--app-bg-panel);
  border-right: 1px solid var(--app-border);
  padding: 16px;
}

.dashboard-layout__left {
  width: 320px;
}

.dashboard-layout__right {
  width: 360px;
  border-right: none;
  border-left: 1px solid var(--app-border);
}

.dashboard-layout__center {
  flex: 1;
  position: relative;
  min-width: 0;
}

.dashboard-layout__time-selector {
  position: absolute;
  top: 16px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.dashboard-layout__loading,
.dashboard-layout__error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--app-text-muted);
  font-size: 14px;
}

.dashboard-layout__error {
  color: var(--app-danger);
}
</style>
