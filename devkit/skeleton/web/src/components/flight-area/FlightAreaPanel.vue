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
import { ref } from 'vue'
import CustomFlightAreaTab from './tabs/CustomFlightAreaTab.vue'
import TerrainObstacleTab from './tabs/TerrainObstacleTab.vue'
import AirspaceAlertTab from './tabs/AirspaceAlertTab.vue'

const activeTab = ref<'areas' | 'terrain' | 'airspace'>('areas')
</script>

<template>
  <div class="flight-area-panel">
    <div class="panel-tabs">
      <button
        class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === 'areas' }"
        title="自定义飞行区域"
        @click="activeTab = 'areas'"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>
      </button>
      <button
        class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === 'terrain' }"
        title="地形障碍物"
        @click="activeTab = 'terrain'"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M14 6l-3.75 5 2.85 3.8-1.6 1.2C9.81 13.75 7 10 7 10l-6 8h22L14 6z"/></svg>
      </button>
      <button
        class="tab-btn"
        :class="{ 'tab-btn--active': activeTab === 'airspace' }"
        title="空域告警"
        @click="activeTab = 'airspace'"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2L1 21h22L12 2zm0 3.99L19.53 19H4.47L12 5.99zM11 16h2v2h-2zm0-6h2v4h-2z"/></svg>
      </button>
    </div>

    <div class="panel-content">
      <CustomFlightAreaTab v-if="activeTab === 'areas'" />
      <TerrainObstacleTab v-if="activeTab === 'terrain'" />
      <AirspaceAlertTab v-if="activeTab === 'airspace'" />
    </div>
  </div>
</template>

<style scoped>
.flight-area-panel {
  width: 360px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--fh-bg-sidebar, #1a1a2e);
  border-right: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.08));
}

.panel-tabs {
  display: flex;
  justify-content: space-around;
  padding: 12px 16px;
  border-bottom: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.08));
}

.tab-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--app-text-secondary, #8b949e);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s, color 0.15s;
}

.tab-btn:hover {
  background: var(--app-bg-card-hover, #242d3d);
}

.tab-btn--active {
  color: var(--app-accent, #58a6ff);
  border-bottom: 2px solid var(--app-accent, #58a6ff);
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
</style>
