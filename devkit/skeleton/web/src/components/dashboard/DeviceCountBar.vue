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
import { displayOrPlaceholder } from '@/utils/format'

const props = defineProps<{
  label: string
  total: number
  online: number
  offline: number
  mock?: boolean
}>()

const barWidth = computed(() => props.total > 0 ? `${(props.online / props.total) * 100}%` : '0%')
</script>

<template>
  <div class="device-count">
    <div class="device-count__header">
      <span class="device-count__label">{{ label }}</span>
      <span class="device-count__total">{{ displayOrPlaceholder(total, !!mock) }}</span>
    </div>
    <template v-if="!mock">
      <div class="device-count__bar">
        <div class="device-count__bar-fill" :style="{ width: barWidth }" />
      </div>
      <div class="device-count__legend">
        <span class="device-count__online">{{ online }}</span>
        <span class="device-count__offline">{{ offline }}</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.device-count__header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 6px;
}

.device-count__label {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.device-count__total {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text);
  font-variant-numeric: tabular-nums;
}

.device-count__bar {
  height: 4px;
  background: var(--app-border);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 4px;
}

.device-count__bar-fill {
  height: 100%;
  background: var(--app-primary);
  border-radius: 2px;
  transition: width 0.3s;
}

.device-count__legend {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--app-text-muted);
  font-variant-numeric: tabular-nums;
}
</style>
