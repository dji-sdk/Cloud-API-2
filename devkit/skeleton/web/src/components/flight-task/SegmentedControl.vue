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
interface SegmentOption {
  value: string
  label: string
  /** 置灰该项：仍参与选中态展示，但不响应点击 */
  disabled?: boolean
}

defineProps<{
  options: SegmentOption[]
  modelValue: string
  /** 置灰项的统一提示文案，渲染为按钮 title；不传则不渲染提示 */
  disabledHint?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

/**
 * 选中某一项。
 *
 * 置灰项已在模板上绑定原生 disabled，此处再拦一道是为了让「置灰不可选」
 * 这一约束不依赖 DOM 的事件派发语义——测试与程序化触发同样走这条路径。
 */
function select(option: SegmentOption): void {
  if (option.disabled) {
    return
  }
  emit('update:modelValue', option.value)
}
</script>

<template>
  <div class="segmented-control">
    <button
      v-for="opt in options"
      :key="opt.value"
      class="segment-btn"
      :class="{ active: modelValue === opt.value, disabled: opt.disabled }"
      type="button"
      :disabled="opt.disabled"
      :title="opt.disabled ? disabledHint : undefined"
      @click="select(opt)"
    >
      {{ opt.label }}
    </button>
  </div>
</template>

<style scoped>
.segmented-control {
  display: inline-flex;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--app-border);
  background: var(--app-bg-surface);
}

.segment-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: var(--app-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
}

.segment-btn:not(:last-child) {
  border-right: 1px solid var(--app-border);
}

.segment-btn:hover:not(.active):not(:disabled) {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.segment-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

/* 置灰但已选中的项：保留选中态配色，仅降低不透明度，
   否则编辑历史任务时会被误读成「当前未选中任何策略」 */
.segment-btn.active:disabled {
  opacity: 0.7;
}

.segment-btn.active {
  background: var(--app-primary);
  color: #ffffff;
  font-weight: 500;
}
</style>
