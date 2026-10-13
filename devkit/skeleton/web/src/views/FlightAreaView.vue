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
import { ref, provide } from 'vue'
import FlightAreaPanel from '@/components/flight-area/FlightAreaPanel.vue'
import FlightAreaMap from '@/components/flight-area/FlightAreaMap.vue'
import FlightAreaDrawTools from '@/components/flight-area/FlightAreaDrawTools.vue'
import FlightAreaCreateDialog from '@/components/flight-area/FlightAreaCreateDialog.vue'
import { useFlightAreaMap, FLIGHT_AREA_MAP_KEY } from '@/composables/useFlightAreaMap'
import type { FlightAreaType, DrawnGeometry } from '@/types/flightArea'

const map = useFlightAreaMap('flight-area-map')
provide(FLIGHT_AREA_MAP_KEY, map)
const { enterDrawMode, cancelDraw } = map

/** 待命名的绘制结果（非空即弹框打开） */
const drawState = ref<{ type: FlightAreaType; geometry: DrawnGeometry } | null>(null)

function handleDraw(type: FlightAreaType, shape: 'polygon' | 'circle'): void {
  enterDrawMode(shape, (geometry: DrawnGeometry) => {
    drawState.value = { type, geometry }
  })
}

function handleCreated(): void {
  drawState.value = null
}

function handleCancelDialog(): void {
  drawState.value = null
  cancelDraw()
}
</script>

<template>
  <div class="flight-area-view">
    <FlightAreaPanel />
    <div class="map-container">
      <FlightAreaMap />
      <FlightAreaDrawTools @draw="handleDraw" />
    </div>
    <FlightAreaCreateDialog
      v-if="drawState"
      :type="drawState.type"
      :geometry="drawState.geometry"
      @created="handleCreated"
      @cancel="handleCancelDialog"
    />
  </div>
</template>

<style scoped>
.flight-area-view {
  display: flex;
  width: 100%;
  height: 100%;
}

.map-container {
  flex: 1;
  position: relative;
  min-width: 0;
}
</style>
