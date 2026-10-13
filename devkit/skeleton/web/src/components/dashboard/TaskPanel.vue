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
import TaskDistributionChart from './TaskDistributionChart.vue'
import ExecutionResultChart from './ExecutionResultChart.vue'
import FlightDataSection from './FlightDataSection.vue'

defineProps<{ stats: DashboardStats }>()
</script>

<template>
  <div class="task-panel">
    <h3 class="task-panel__title">任务</h3>
    <TaskDistributionChart :daily="stats.tasks.daily" />

    <div class="task-panel__divider" />

    <ExecutionResultChart :daily="stats.execution.daily" />

    <div class="task-panel__divider" />

    <FlightDataSection :flight="stats.flight" />
  </div>
</template>

<style scoped>
.task-panel__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin-bottom: 16px;
}

.task-panel__divider {
  height: 1px;
  background: var(--app-border);
  margin: 16px 0;
}
</style>
