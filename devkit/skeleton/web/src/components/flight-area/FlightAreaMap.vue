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
import { onMounted, onUnmounted, watch, inject } from 'vue'
import { useFlightAreaStore } from '@/stores/flightArea'
import { FLIGHT_AREA_MAP_KEY, type FlightAreaMapApi } from '@/composables/useFlightAreaMap'

const store = useFlightAreaStore()
const map = inject<FlightAreaMapApi>(FLIGHT_AREA_MAP_KEY)
if (!map) throw new Error('FlightAreaMap 必须在 FlightAreaView 内使用，且后者需 provide FLIGHT_AREA_MAP_KEY')
const { ready, initMap, renderAreas, flyTo, destroy } = map

onMounted(() => { initMap() })
onUnmounted(() => { destroy() })

watch(() => store.areas, (areas) => {
  if (ready.value) renderAreas(areas)
}, { deep: true })

watch(ready, (isReady) => {
  if (isReady) renderAreas(store.areas)
})

watch(() => store.selectedArea, (area) => {
  if (!area) return
  const geom = area.content?.geometry
  if (!geom) return

  if (geom.type === 'Circle') {
    const coords = geom.coordinates as number[]
    if (typeof coords[0] === 'number') flyTo(coords[0], coords[1])
    return
  }
  if (geom.type === 'Polygon' && Array.isArray(geom.coordinates?.[0]?.[0])) {
    const coords = geom.coordinates[0] as number[][]
    const lng = coords.reduce((s, c) => s + c[0], 0) / coords.length
    const lat = coords.reduce((s, c) => s + c[1], 0) / coords.length
    flyTo(lng, lat)
  }
})
</script>

<template>
  <div id="flight-area-map" class="flight-area-map" />
</template>

<style scoped>
.flight-area-map {
  width: 100%;
  height: 100%;
  min-height: 0;
}
</style>
