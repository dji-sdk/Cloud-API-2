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
import { Zap, Clock, Repeat, BatteryCharging } from 'lucide-vue-next'
import type { Component } from 'vue'
import type { FlightTaskType, TaskStrategy } from '@/types/flightTask'
import { STRATEGY_LABELS, resolveTaskStrategy } from '@/utils/taskStrategy'

const props = withDefaults(defineProps<{
  taskType: FlightTaskType
  /** 最低电量阈值（百分比），无数据为 0；用于区分平台同样记为 taskType=1 的连续执行任务 */
  minBatteryCapacity?: number
  /** 图标描边宽度 */
  strokeWidth?: number
}>(), {
  minBatteryCapacity: 0,
  strokeWidth: 2,
})

/** 各策略对应的图标，"立即/连续执行"复用闪电，"单次/重复定时"用时钟与循环以示区分。 */
const STRATEGY_ICONS: Record<TaskStrategy, Component> = {
  immediate: Zap,
  once: Clock,
  repeat: Repeat,
  continuous: BatteryCharging,
}

const strategy = computed<TaskStrategy>(
  () => resolveTaskStrategy(props.taskType, props.minBatteryCapacity),
)

const label = computed<string>(() => STRATEGY_LABELS[strategy.value])

const icon = computed<Component>(() => STRATEGY_ICONS[strategy.value])
</script>

<template>
  <span class="type-label" :data-strategy="strategy">
    <component :is="icon" :size="12" :stroke-width="strokeWidth" />
    {{ label }}
  </span>
</template>

<style scoped>
.type-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--app-text-secondary);
}
</style>
