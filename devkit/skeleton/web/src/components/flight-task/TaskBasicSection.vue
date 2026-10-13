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
import type { PlanType } from '@/types/flightTask'
import { CHANNEL_UNSUPPORTED_HINT, isPlanTypeSupported } from '@/utils/taskCapability'
import SegmentedControl from './SegmentedControl.vue'
import { Info } from 'lucide-vue-next'

const props = defineProps<{
  name: string
  planType: PlanType
}>()

const emit = defineEmits<{
  'update:name': [value: string]
  'update:planType': [value: PlanType]
}>()

/**
 * 计划类型选项。
 *
 * 蛙跳 / 手飞当前在 PaaS 通道无法送达（`plan_type` 在 BFF 层即被丢弃，
 * 原因见 `@/utils/taskCapability`），置灰而非删除：SDK 补齐承载位后
 * 只需调整能力清单，组件本身不必再改。
 */
const PLAN_TYPE_OPTIONS = [
  { value: 'normal', label: '普通任务' },
  { value: 'leapfrog', label: '蛙跳任务', disabled: !isPlanTypeSupported('leapfrog') },
  { value: 'manual', label: '手飞任务', disabled: !isPlanTypeSupported('manual') },
]
</script>

<template>
  <div class="task-basic-section">
    <div class="form-field">
      <label class="field-label">计划名称</label>
      <input
        class="field-input"
        type="text"
        maxlength="50"
        placeholder="请输入计划名称"
        :value="name"
        @input="emit('update:name', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div class="form-field">
      <label class="field-label">
        任务类型
        <Info :size="14" class="info-icon" />
      </label>
      <SegmentedControl
        :options="PLAN_TYPE_OPTIONS"
        :model-value="planType"
        :disabled-hint="CHANNEL_UNSUPPORTED_HINT"
        @update:model-value="emit('update:planType', $event as PlanType)"
      />
    </div>
  </div>
</template>

<style scoped>
.task-basic-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.field-input {
  padding: 10px 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-base);
}

.field-input:focus {
  border-color: var(--app-primary);
}

.field-input::placeholder {
  color: var(--app-text-muted);
}
</style>
