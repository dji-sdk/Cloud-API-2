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
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { TaskDailyEntry } from '@/types/dashboard'

use([BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps<{ daily: TaskDailyEntry[] }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: {
    data: ['计划任务', '手飞任务'],
    textStyle: { color: '#8b949e', fontSize: 11 },
    top: 0,
    right: 0,
  },
  grid: { top: 30, right: 8, bottom: 24, left: 32 },
  xAxis: {
    type: 'category',
    data: props.daily.map((d) => d.date.slice(5)),
    axisLabel: { color: '#848d97', fontSize: 10 },
    axisLine: { lineStyle: { color: '#30363d' } },
  },
  yAxis: {
    type: 'value',
    axisLabel: { color: '#848d97', fontSize: 10 },
    splitLine: { lineStyle: { color: '#30363d' } },
  },
  series: [
    {
      name: '计划任务',
      type: 'bar',
      stack: 'total',
      data: props.daily.map((d) => d.planned),
      itemStyle: { color: '#1a73e8' },
    },
    {
      name: '手飞任务',
      type: 'bar',
      stack: 'total',
      data: props.daily.map((d) => d.manual),
      itemStyle: { color: '#3fb950' },
    },
  ],
}))
</script>

<template>
  <div class="task-chart">
    <h4 class="task-chart__title">任务分布</h4>
    <VChart :option="option" autoresize style="height: 180px" />
  </div>
</template>

<style scoped>
.task-chart__title {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 8px;
}
</style>
