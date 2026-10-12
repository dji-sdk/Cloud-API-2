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
import { onMounted, onUnmounted, watch } from 'vue'
import { useReconstructionMap } from '@/composables/useReconstructionMap'

const props = defineProps<{
  interestRegionEnabled: boolean
}>()

const MAP_CONTAINER_ID = 'reconstruction-cesium-map'

const {
  ready,
  wkt,
  drawingActive,
  initMap,
  startDrawPolygon,
  clearPolygon,
  cancelDraw,
  destroy,
} = useReconstructionMap(MAP_CONTAINER_ID)

defineExpose({ wkt })

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  destroy()
})

watch(() => props.interestRegionEnabled, (enabled) => {
  if (enabled) {
    clearPolygon()
    if (ready.value) {
      startDrawPolygon()
    }
  } else {
    cancelDraw()
    clearPolygon()
  }
})

watch(ready, (isReady) => {
  if (isReady && props.interestRegionEnabled) {
    startDrawPolygon()
  }
})
</script>

<template>
  <div class="reconstruction-map">
    <div :id="MAP_CONTAINER_ID" class="cesium-container" />
    <div v-if="drawingActive" class="draw-hint">
      左键点击添加顶点，右键结束绘制
    </div>
    <div v-if="!ready" class="map-loading">
      <span class="loading-spinner" />
      <span>地图加载中...</span>
    </div>
  </div>
</template>

<style scoped>
.reconstruction-map {
  position: relative;
  flex: 1;
  min-width: 0;
  height: 100%;
}

.cesium-container {
  width: 100%;
  height: 100%;
}

.draw-hint {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 16px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: var(--radius-md);
  color: var(--app-text);
  font-size: 12px;
  pointer-events: none;
  z-index: 10;
}

.map-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--app-bg);
  color: var(--app-text-muted);
  font-size: 14px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--app-border-strong);
  border-top-color: var(--app-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
