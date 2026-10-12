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
import { computed } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { FlightStats } from '@/types/dashboard'
import { displayOrPlaceholder } from '@/utils/format'

use([LineChart, GridComponent, CanvasRenderer])

const props = defineProps<{ flight: FlightStats }>()

/** 里程是否 mock：mock 时不绘制趋势图并展示占位符。 */
const isDistanceMock = computed(() => props.flight.distanceKm._mock)

function sparklineOption(data: number[]) {
  return {
    grid: { top: 2, right: 2, bottom: 2, left: 2 },
    xAxis: { type: 'category', show: false, data: data.map((_, i) => String(i)) },
    yAxis: { type: 'value', show: false },
    series: [
      {
        type: 'line',
        data,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 1.5, color: '#1a73e8' },
        areaStyle: { color: 'rgba(26, 115, 232, 0.1)' },
      },
    ],
  }
}

const countSpark = computed(() => sparklineOption(props.flight.daily.map((d) => d.count)))
const minutesSpark = computed(() => sparklineOption(props.flight.daily.map((d) => d.minutes)))
const distanceSpark = computed(() =>
  sparklineOption(props.flight.daily.map((d) => d.km)),
)
</script>

<template>
  <div class="flight-data">
    <h4 class="flight-data__title">飞行数据</h4>
    <div class="flight-data__grid">
      <div class="flight-data__item">
        <span class="flight-data__value">{{ flight.count }}</span>
        <span class="flight-data__label">飞行次数</span>
        <VChart :option="countSpark" autoresize style="height: 32px; width: 64px" />
      </div>
      <div class="flight-data__item">
        <span class="flight-data__value">{{ flight.durationMinutes }}</span>
        <span class="flight-data__label">飞行时长(min)</span>
        <VChart :option="minutesSpark" autoresize style="height: 32px; width: 64px" />
      </div>
      <div class="flight-data__item">
        <span class="flight-data__value">
          {{ displayOrPlaceholder(flight.distanceKm.value, isDistanceMock) }}
        </span>
        <span class="flight-data__label">飞行里程(km)</span>
        <VChart v-if="!isDistanceMock" :option="distanceSpark" autoresize style="height: 32px; width: 64px" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.flight-data__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 12px;
}

.flight-data__grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
}

.flight-data__item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.flight-data__value {
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text);
  font-variant-numeric: tabular-nums;
}

.flight-data__label {
  font-size: 11px;
  color: var(--app-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
