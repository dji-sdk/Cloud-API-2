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
import type { FlightArea } from '@/types/flightArea'
import { FLIGHT_AREA_COLORS, FLIGHT_AREA_TYPE_LABELS } from '@/types/flightArea'
import { useFlightAreaStore } from '@/stores/flightArea'

const props = defineProps<{
  area: FlightArea
  selected: boolean
}>()

const store = useFlightAreaStore()

function handleDelete(event: Event): void {
  event.stopPropagation()
  if (confirm(`确认删除「${props.area.name}」？删除后无法恢复。`)) {
    store.removeArea(props.area.id)
  }
}
</script>

<template>
  <div
    class="flight-area-list-item"
    :class="{ 'flight-area-list-item--selected': selected }"
  >
    <div class="item-indicator" :style="{ backgroundColor: FLIGHT_AREA_COLORS[area.type] }" />
    <div class="item-content">
      <div class="item-name">{{ area.name }}</div>
      <div class="item-meta">{{ FLIGHT_AREA_TYPE_LABELS[area.type] }}</div>
    </div>
    <button class="item-delete" title="删除" @click="handleDelete">×</button>
  </div>
</template>

<style scoped>
.flight-area-list-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.flight-area-list-item:hover {
  background: var(--app-bg-card-hover, #242d3d);
}

.flight-area-list-item--selected {
  background: var(--app-bg-card-hover, #242d3d);
  border: 1px solid var(--app-accent, #58a6ff);
}

.item-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: var(--app-text, #e6edf3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
  margin-top: 2px;
}

.item-delete {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--app-text-secondary, #8b949e);
  cursor: pointer;
  font-size: 16px;
  display: none;
  align-items: center;
  justify-content: center;
}

.flight-area-list-item:hover .item-delete {
  display: flex;
}

.item-delete:hover {
  background: rgba(248, 81, 73, 0.2);
  color: #f85149;
}
</style>
