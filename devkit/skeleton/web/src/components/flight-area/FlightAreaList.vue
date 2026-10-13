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
import { useFlightAreaStore } from '@/stores/flightArea'
import FlightAreaListItem from './FlightAreaListItem.vue'

const store = useFlightAreaStore()
</script>

<template>
  <div class="flight-area-list">
    <div v-if="store.loading" class="list-loading">加载中...</div>
    <div v-else-if="store.areas.length === 0" class="list-empty">
      <div class="empty-text">暂无数据</div>
    </div>
    <template v-else>
      <FlightAreaListItem
        v-for="area in store.areas"
        :key="area.id"
        :area="area"
        :selected="area.id === store.selectedAreaId"
        @click="store.selectArea(area.id)"
      />
    </template>
  </div>
</template>

<style scoped>
.flight-area-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-loading,
.list-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--app-text-secondary, #8b949e);
}

.empty-text {
  font-size: 14px;
}
</style>
