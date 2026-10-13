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
import type { TimeRange } from '@/types/dashboard'

defineProps<{ modelValue: TimeRange }>()
const emit = defineEmits<{ 'update:modelValue': [value: TimeRange] }>()

const options: Array<{ label: string; value: TimeRange }> = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '近一周', value: 'recent_week' },
  { label: '近一月', value: 'recent_month' },
  { label: '近半年', value: 'half_year' },
  { label: '近一年', value: 'year' },
]
</script>

<template>
  <div class="time-range-selector">
    <button
      v-for="opt in options"
      :key="opt.value"
      class="time-range-selector__btn"
      :class="{ 'time-range-selector__btn--active': modelValue === opt.value }"
      @click="emit('update:modelValue', opt.value)"
    >
      {{ opt.label }}
    </button>
  </div>
</template>

<style scoped>
.time-range-selector {
  display: flex;
  gap: 4px;
  background: rgba(13, 17, 23, 0.8);
  backdrop-filter: blur(8px);
  border: 1px solid var(--app-border);
  border-radius: 6px;
  padding: 4px;
}

.time-range-selector__btn {
  padding: 6px 12px;
  font-size: 12px;
  color: var(--app-text-secondary);
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
}

.time-range-selector__btn:hover {
  color: var(--app-text);
  background: rgba(255, 255, 255, 0.06);
}

.time-range-selector__btn--active {
  color: var(--app-text);
  background: var(--app-primary);
}
</style>
