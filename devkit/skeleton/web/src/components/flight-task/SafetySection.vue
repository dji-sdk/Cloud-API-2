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
import type { LostControlAction } from '@/types/flightTask'
import SegmentedControl from './SegmentedControl.vue'
import ToggleSwitch from './ToggleSwitch.vue'
import NumberStepper from './NumberStepper.vue'
import { Info } from 'lucide-vue-next'

defineProps<{
  resumableEnabled: boolean
  smartRthEnabled: boolean
  rthAltitude: number
  outOfControlAction: LostControlAction
}>()

const emit = defineEmits<{
  'update:resumableEnabled': [value: boolean]
  'update:smartRthEnabled': [value: boolean]
  'update:rthAltitude': [value: number]
  'update:outOfControlAction': [value: LostControlAction]
}>()

const LOST_CONTROL_OPTIONS = [
  { value: 'return_home', label: '返航' },
  { value: 'continue', label: '继续执行' },
]
</script>

<template>
  <div class="safety-section">
    <div class="toggle-field">
      <div class="toggle-label-row">
        <span class="field-label">
          自动断点续飞
          <Info :size="14" class="info-icon" />
        </span>
        <ToggleSwitch
          :model-value="resumableEnabled"
          @update:model-value="emit('update:resumableEnabled', $event)"
        />
      </div>
    </div>

    <div class="toggle-field">
      <div class="toggle-label-row">
        <span class="field-label">
          智能规划最佳返航路线
          <Info :size="14" class="info-icon" />
        </span>
        <ToggleSwitch
          :model-value="smartRthEnabled"
          @update:model-value="emit('update:smartRthEnabled', $event)"
        />
      </div>
    </div>

    <div class="form-field">
      <label class="field-label">
        最低返航高度（相对机场）
        <Info :size="14" class="info-icon" />
      </label>
      <NumberStepper
        :model-value="rthAltitude"
        :min="20"
        :max="1500"
        unit="m"
        @update:model-value="emit('update:rthAltitude', $event)"
      />
    </div>

    <div class="form-field">
      <label class="field-label">
        航线飞行中失联
        <Info :size="14" class="info-icon" />
      </label>
      <SegmentedControl
        :options="LOST_CONTROL_OPTIONS"
        :model-value="outOfControlAction"
        @update:model-value="emit('update:outOfControlAction', $event as LostControlAction)"
      />
    </div>
  </div>
</template>

<style scoped>
.safety-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toggle-field {
  padding: 0;
}

.toggle-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.field-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--app-text-secondary);
  font-weight: 500;
}

.info-icon {
  color: var(--app-text-muted);
  cursor: help;
}
</style>
