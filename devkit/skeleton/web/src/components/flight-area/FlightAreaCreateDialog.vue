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
import { ref, computed } from 'vue'
import type { FlightArea, FlightAreaType, DrawnGeometry } from '@/types/flightArea'
import { FLIGHT_AREA_TYPE_LABELS, FLIGHT_AREA_COLORS } from '@/types/flightArea'
import { useFlightAreaStore } from '@/stores/flightArea'

const props = defineProps<{
  type: FlightAreaType
  geometry: DrawnGeometry
}>()

const emit = defineEmits<{
  created: [area: FlightArea]
  cancel: []
}>()

const store = useFlightAreaStore()

const name = ref('')
const submitting = ref(false)
const errorMsg = ref('')

const canSubmit = computed(() => name.value.trim().length > 0 && !submitting.value)

async function handleSubmit(): Promise<void> {
  if (!canSubmit.value) return
  submitting.value = true
  errorMsg.value = ''
  try {
    const area = await store.addArea({
      name: name.value.trim(),
      type: props.type,
      content: {
        type: 'Feature',
        properties: { color: FLIGHT_AREA_COLORS[props.type] },
        geometry: props.geometry as FlightArea['content']['geometry'],
      },
    })
    emit('created', area)
  } catch (err) {
    errorMsg.value = err instanceof Error ? err.message : '创建失败'
  } finally {
    submitting.value = false
  }
}

function handleCancel(): void {
  emit('cancel')
}
</script>

<template>
  <div class="dialog-mask" @click.self="handleCancel">
    <div class="dialog">
      <div class="dialog-title">创建{{ FLIGHT_AREA_TYPE_LABELS[type] }}</div>
      <div class="dialog-body">
        <label class="field-label">区域名称</label>
        <input
          data-testid="name-input"
          v-model="name"
          class="field-input"
          type="text"
          placeholder="请输入区域名称"
          @keyup.enter="handleSubmit"
        />
        <div v-if="errorMsg" class="field-error">{{ errorMsg }}</div>
      </div>
      <div class="dialog-footer">
        <button class="btn btn-cancel" @click="handleCancel">取消</button>
        <button
          data-testid="confirm-btn"
          class="btn btn-confirm"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          {{ submitting ? '创建中...' : '确认创建' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  width: 360px;
  background: var(--app-bg-card, #161b22);
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 10px;
  padding: 20px;
}

.dialog-title {
  font-size: 16px;
  color: var(--app-text, #e6edf3);
  margin-bottom: 16px;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.field-label {
  font-size: 13px;
  color: var(--app-text-secondary, #8b949e);
}

.field-input {
  padding: 8px 12px;
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 6px;
  background: var(--app-bg-input, #0d1117);
  color: var(--app-text, #e6edf3);
  font-size: 14px;
  outline: none;
}

.field-input:focus {
  border-color: var(--fh-accent, #58a6ff);
}

.field-error {
  font-size: 12px;
  color: #f85149;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn {
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: none;
}

.btn-cancel {
  background: transparent;
  color: var(--app-text-secondary, #8b949e);
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
}

.btn-confirm {
  background: var(--fh-accent, #58a6ff);
  color: #fff;
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
