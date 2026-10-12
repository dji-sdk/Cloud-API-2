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
const props = defineProps<{
  modelValue: boolean
  label?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

function toggle(): void {
  emit('update:modelValue', !props.modelValue)
}
</script>

<template>
  <div class="toggle-switch-wrapper" @click="toggle">
    <span v-if="label" class="toggle-label">{{ label }}</span>
    <div class="toggle-track" :class="{ active: modelValue }">
      <div class="toggle-thumb" />
    </div>
  </div>
</template>

<style scoped>
.toggle-switch-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.toggle-label {
  font-size: 13px;
  color: var(--app-text);
}

.toggle-track {
  position: relative;
  width: 44px;
  height: 24px;
  border-radius: var(--radius-full);
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border);
  transition: all var(--transition-base);
}

.toggle-track.active {
  background: var(--app-primary);
  border-color: var(--app-primary);
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #ffffff;
  transition: transform var(--transition-base);
}

.toggle-track.active .toggle-thumb {
  transform: translateX(20px);
}
</style>
