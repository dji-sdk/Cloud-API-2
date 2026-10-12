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
import { ref } from 'vue'
import type { WaylineInfo } from '@/types/wayline'
import { X } from 'lucide-vue-next'

interface Props {
  selectedWaylines: WaylineInfo[]
  /** 父组件拼接操作进行中时传入 true，禁用确认按钮防止重复提交。 */
  loading?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  confirm: [name: string]
  cancel: []
}>()

const concatName = ref('')
const concatError = ref('')

function handleConfirm(): void {
  const name = concatName.value.trim()
  if (!name) {
    concatError.value = '请输入拼接后的航线名称'
    return
  }
  concatError.value = ''
  emit('confirm', name)
}
</script>

<template>
  <div class="concat-overlay" @click.self="emit('cancel')">
    <div class="concat-dialog">
      <div class="dialog-header">
        <h3 class="dialog-title">拼接航线</h3>
        <button class="dialog-close" @click="emit('cancel')">
          <X :size="16" />
        </button>
      </div>

      <div class="dialog-body">
        <div class="selected-info">
          <span class="info-label">已选择 {{ props.selectedWaylines.length }} 条航线：</span>
          <ul class="selected-list">
            <li
              v-for="wl in props.selectedWaylines"
              :key="wl.uuid"
              class="selected-item"
            >
              {{ wl.name }}
            </li>
          </ul>
        </div>

        <div class="form-field">
          <label class="field-label">拼接后航线名称</label>
          <input
            v-model="concatName"
            type="text"
            class="field-input"
            placeholder="输入新航线名称…"
            @keyup.enter="handleConfirm"
          />
          <span v-if="concatError" class="field-error">{{ concatError }}</span>
        </div>
      </div>

      <div class="dialog-footer">
        <button class="btn btn--ghost" @click="emit('cancel')">取消</button>
        <button
          class="btn btn--primary"
          :disabled="props.loading"
          @click="handleConfirm"
        >
          确认拼接
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.concat-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(2px);
}

.concat-dialog {
  width: 440px;
  max-width: 90vw;
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--app-border);
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.dialog-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
}

.dialog-close:hover {
  background: var(--app-hover-bg);
  color: var(--app-danger);
}

.dialog-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.selected-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: var(--app-text-muted);
}

.selected-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 120px;
  overflow-y: auto;
}

.selected-item {
  font-size: 13px;
  color: var(--app-text);
  padding: 4px 8px;
  background: var(--app-bg-surface);
  border-radius: var(--radius-sm);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  color: var(--app-text-muted);
  font-weight: 500;
}

.field-input {
  padding: 8px 12px;
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
  opacity: 0.6;
}

.field-error {
  font-size: 12px;
  color: var(--app-danger);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--app-border);
}

.btn {
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.btn--ghost {
  background: transparent;
  border-color: var(--app-border);
  color: var(--app-text-muted);
}

.btn--ghost:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.btn--primary {
  background: var(--app-primary);
  color: #fff;
  border-color: var(--app-primary);
}

.btn--primary:hover {
  opacity: 0.9;
}

.btn--primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
