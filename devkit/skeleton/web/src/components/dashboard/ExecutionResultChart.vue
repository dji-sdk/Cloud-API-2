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
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { ExecutionDailyEntry } from '@/types/dashboard'

use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const props = defineProps<{ daily: ExecutionDailyEntry[] }>()

const option = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: {
    data: ['执行成功', '已挂起', '执行失败'],
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
      name: '执行成功',
      type: 'line',
      smooth: true,
      data: props.daily.map((d) => d.success),
      lineStyle: { color: '#3fb950' },
      itemStyle: { color: '#3fb950' },
    },
    {
      name: '已挂起',
      type: 'line',
      smooth: true,
      data: props.daily.map((d) => d.suspended),
      lineStyle: { color: '#d29922' },
      itemStyle: { color: '#d29922' },
    },
    {
      name: '执行失败',
      type: 'line',
      smooth: true,
      data: props.daily.map((d) => d.failed),
      lineStyle: { color: '#f85149' },
      itemStyle: { color: '#f85149' },
    },
  ],
}))
</script>

<template>
  <div class="execution-chart">
    <h4 class="execution-chart__title">执行结果</h4>
    <VChart :option="option" autoresize style="height: 180px" />
  </div>
</template>

<style scoped>
.execution-chart__title {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 8px;
}
</style>
