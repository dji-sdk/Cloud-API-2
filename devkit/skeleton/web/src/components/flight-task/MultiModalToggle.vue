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
import { Info } from 'lucide-vue-next'
import ToggleSwitch from './ToggleSwitch.vue'

defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

/**
 * SDK 3.3.7 的创建 / 编辑请求均无 vlm_enabled 承载位，原生库请求字符串表里也没有
 * 任何 vlm 字段。开关保留是因为接口契约仍接收该字段，但必须明确告知「当前不生效」，
 * 否则用户会以为选中就已在设备端启用了多模态识别。
 */
const VLM_UNSUPPORTED_HINT = '当前平台协议暂不支持多模态识别，开关仅保留展示，选中不会下发到设备'
</script>

<template>
  <div class="multi-modal-toggle">
    <div class="toggle-row">
      <span class="toggle-text">
        多模态识别
        <Info :size="14" class="info-icon" :title="VLM_UNSUPPORTED_HINT" />
        <span class="unsupported-badge">暂不支持</span>
      </span>
      <ToggleSwitch
        :model-value="modelValue"
        disabled
        @update:model-value="emit('update:modelValue', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.multi-modal-toggle {
  padding: 12px 14px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
}

.toggle-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toggle-text {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--app-text-muted);
}

.info-icon {
  color: var(--app-text-muted);
  cursor: help;
}

.unsupported-badge {
  margin-left: 4px;
  padding: 1px 6px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-full);
  font-size: 11px;
  color: var(--app-text-muted);
}
</style>
