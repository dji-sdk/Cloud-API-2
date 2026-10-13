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

const props = defineProps<{
  modelValue: number
  min?: number
  max?: number
  steps?: number[]
  unit?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: number]
}>()

const effectiveSteps = computed(() => props.steps ?? [-100, -10, -1, 1, 10, 100])

function adjust(delta: number): void {
  const minVal = props.min ?? -Infinity
  const maxVal = props.max ?? Infinity
  const next = Math.max(minVal, Math.min(maxVal, props.modelValue + delta))
  emit('update:modelValue', next)
}

function formatStep(step: number): string {
  return step > 0 ? `+${step}` : String(step)
}
</script>

<template>
  <div class="number-stepper">
    <button
      v-for="step in effectiveSteps"
      :key="step"
      class="step-btn"
      type="button"
      @click="adjust(step)"
    >
      {{ formatStep(step) }}
    </button>
    <div class="value-display">
      <span class="value-num">{{ modelValue }}</span>
      <span v-if="unit" class="value-unit">{{ unit }}</span>
    </div>
  </div>
</template>

<style scoped>
.number-stepper {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.step-btn {
  padding: 6px 10px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  font-size: 12px;
  font-family: var(--font-mono);
  cursor: pointer;
  transition: all var(--transition-base);
}

.step-btn:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

.value-display {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: var(--app-bg-panel);
  min-width: 60px;
  justify-content: center;
}

.value-num {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  font-family: var(--font-mono);
}

.value-unit {
  font-size: 12px;
  color: var(--app-text-muted);
}
</style>
