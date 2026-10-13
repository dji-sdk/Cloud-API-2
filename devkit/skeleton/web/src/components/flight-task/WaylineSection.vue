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
import { Plus, RefreshCw, X } from 'lucide-vue-next'
import WaylineSelectModal from './WaylineSelectModal.vue'

defineProps<{
  waylineUuid: string | null
  waylineName: string | null
}>()

const emit = defineEmits<{
  'update:waylineUuid': [value: string | null]
  'update:waylineName': [value: string | null]
}>()

const showModal = ref(false)

function onSelect(uuid: string, name: string): void {
  emit('update:waylineUuid', uuid)
  emit('update:waylineName', name)
  showModal.value = false
}

function clear(): void {
  emit('update:waylineUuid', null)
  emit('update:waylineName', null)
}
</script>

<template>
  <div class="wayline-section">
    <label class="field-label">执行航线</label>

    <div v-if="!waylineUuid" class="select-trigger" @click="showModal = true">
      <Plus :size="16" />
      <span>选择航线</span>
    </div>

    <div v-else class="selected-card">
      <div class="card-info">
        <span class="card-name">{{ waylineName }}</span>
        <span class="card-uuid">{{ waylineUuid }}</span>
      </div>
      <div class="card-actions">
        <button class="action-btn" type="button" @click="showModal = true">
          <RefreshCw :size="14" />
        </button>
        <button class="action-btn" type="button" @click="clear">
          <X :size="14" />
        </button>
      </div>
    </div>

    <WaylineSelectModal
      :visible="showModal"
      @update:visible="showModal = $event"
      @select="onSelect"
    />
  </div>
</template>

<style scoped>
.wayline-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  color: var(--app-text-secondary);
  font-weight: 500;
}

.select-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  border: 1px dashed var(--app-primary);
  border-radius: var(--radius-md);
  background: var(--app-primary-bg);
  color: var(--app-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.select-trigger:hover {
  background: var(--app-primary-bg-hover);
}

.selected-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-name {
  font-size: 14px;
  color: var(--app-text);
  font-weight: 500;
}

.card-uuid {
  font-size: 11px;
  color: var(--app-text-muted);
  font-family: var(--font-mono);
}

.card-actions {
  display: flex;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
}

.action-btn:hover {
  color: var(--app-text);
  background: var(--app-hover-bg);
}
</style>
