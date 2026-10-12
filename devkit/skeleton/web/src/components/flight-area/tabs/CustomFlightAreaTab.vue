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
import { onMounted, watch } from 'vue'
import { useFlightAreaStore } from '@/stores/flightArea'
import FlightAreaList from '../FlightAreaList.vue'
import type { FlightAreaType, FlightAreaStatus } from '@/types/flightArea'

const store = useFlightAreaStore()

onMounted(() => {
  store.loadAreas()
})

watch([() => store.filterType, () => store.filterStatus, () => store.searchName], () => {
  store.loadAreas()
})
</script>

<template>
  <div class="custom-flight-area-tab">
    <div class="filter-row">
      <select
        class="filter-select"
        :value="store.filterType"
        @change="store.filterType = ($event.target as HTMLSelectElement).value as FlightAreaType | 'all'"
      >
        <option value="all">全部类型</option>
        <option value="dfence">自定义作业区</option>
        <option value="nfz">自定义限飞区</option>
        <option value="noland">自定义禁降区</option>
      </select>

      <select
        class="filter-select"
        :value="store.filterStatus"
        @change="store.filterStatus = ($event.target as HTMLSelectElement).value as FlightAreaStatus | 'all'"
      >
        <option value="all">全部状态</option>
        <option value="enable">启用</option>
        <option value="disable">禁用</option>
      </select>
    </div>

    <div class="search-row">
      <input
        class="search-input"
        type="text"
        placeholder="搜索区域名称..."
        :value="store.searchName"
        @input="store.searchName = ($event.target as HTMLInputElement).value"
      />
    </div>

    <FlightAreaList />
  </div>
</template>

<style scoped>
.custom-flight-area-tab {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-row {
  display: flex;
  gap: 8px;
}

.filter-select {
  flex: 1;
  padding: 6px 8px;
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 6px;
  background: var(--app-bg-card, #161b22);
  color: var(--app-text, #e6edf3);
  font-size: 13px;
}

.search-row {
  display: flex;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 6px;
  background: var(--app-bg-card, #161b22);
  color: var(--app-text, #e6edf3);
  font-size: 13px;
}

.search-input::placeholder {
  color: var(--app-text-secondary, #8b949e);
}
</style>
