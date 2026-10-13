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
import type { FlightAreaType } from '@/types/flightArea'
import { FLIGHT_AREA_COLORS } from '@/types/flightArea'

const emit = defineEmits<{
  draw: [type: FlightAreaType, shape: 'polygon' | 'circle']
}>()

const showMenu = ref(false)

function handleDraw(type: FlightAreaType, shape: 'polygon' | 'circle'): void {
  showMenu.value = false
  emit('draw', type, shape)
}
</script>

<template>
  <div class="draw-tools">
    <button class="tool-btn" title="绘制区域" @click="showMenu = !showMenu">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/></svg>
    </button>

    <div v-if="showMenu" class="draw-menu">
      <div class="menu-title">选择类型和形状</div>
      <button class="menu-item" @click="handleDraw('dfence', 'polygon')">
        <span class="dot" :style="{ background: FLIGHT_AREA_COLORS.dfence }" />
        作业区 - 多边形
      </button>
      <button class="menu-item" @click="handleDraw('dfence', 'circle')">
        <span class="dot" :style="{ background: FLIGHT_AREA_COLORS.dfence }" />
        作业区 - 圆形
      </button>
      <button class="menu-item" @click="handleDraw('nfz', 'polygon')">
        <span class="dot" :style="{ background: FLIGHT_AREA_COLORS.nfz }" />
        限飞区 - 多边形
      </button>
      <button class="menu-item" @click="handleDraw('nfz', 'circle')">
        <span class="dot" :style="{ background: FLIGHT_AREA_COLORS.nfz }" />
        限飞区 - 圆形
      </button>
      <button class="menu-item" @click="handleDraw('noland', 'polygon')">
        <span class="dot" :style="{ background: FLIGHT_AREA_COLORS.noland }" />
        禁降区 - 多边形
      </button>
    </div>
  </div>
</template>

<style scoped>
.draw-tools {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.tool-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: var(--app-bg-card, #161b22);
  color: var(--app-text, #e6edf3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.tool-btn:hover {
  background: var(--app-bg-card-hover, #242d3d);
}

.draw-menu {
  position: absolute;
  top: 42px;
  right: 0;
  width: 180px;
  background: var(--app-bg-card, #161b22);
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  padding: 8px;
}

.menu-title {
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
  padding: 4px 8px;
  margin-bottom: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: var(--app-text, #e6edf3);
  cursor: pointer;
  font-size: 13px;
  text-align: left;
}

.menu-item:hover {
  background: var(--app-bg-card-hover, #242d3d);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
