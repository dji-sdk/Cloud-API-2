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
import type { DashboardStats } from '@/types/dashboard'
import DockStatusSection from './DockStatusSection.vue'
import DeviceCountBar from './DeviceCountBar.vue'
import StatGrid from './StatGrid.vue'
import DataResultSection from './DataResultSection.vue'

defineProps<{ stats: DashboardStats }>()
</script>

<template>
  <div class="asset-panel">
    <h3 class="asset-panel__title">资产</h3>

    <DockStatusSection :docks="stats.docks" />

    <div class="asset-panel__divider" />

    <div class="asset-panel__devices">
      <DeviceCountBar
        label="飞行器"
        :total="stats.drones.total"
        :online="stats.drones.online"
        :offline="stats.drones.offline"
      />
      <DeviceCountBar
        label="中继站"
        :total="stats.relays.total"
        :online="stats.relays.online"
        :offline="stats.relays.offline"
        :mock="stats.relays._mock"
      />
    </div>

    <div class="asset-panel__divider" />

    <StatGrid :counts="stats.counts" />

    <div class="asset-panel__divider" />

    <DataResultSection :data-results="stats.dataResults" />
  </div>
</template>

<style scoped>
.asset-panel__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin-bottom: 16px;
}

.asset-panel__divider {
  height: 1px;
  background: var(--app-border);
  margin: 16px 0;
}

.asset-panel__devices {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
