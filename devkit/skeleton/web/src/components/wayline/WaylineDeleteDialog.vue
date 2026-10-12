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
import type { WaylineInfo } from '@/types/wayline'
import { AlertTriangle } from 'lucide-vue-next'

interface Props {
  selectedWaylines: WaylineInfo[]
  loading: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <div class="dialog-overlay" @click.self="emit('cancel')">
    <div class="dialog-card">
      <div class="dialog-header">
        <AlertTriangle :size="20" class="dialog-icon" />
        <h3 class="dialog-title">确认删除航线</h3>
      </div>

      <div class="dialog-body">
        <p class="dialog-message">
          即将删除以下 <strong>{{ selectedWaylines.length }}</strong> 条航线，删除后不可恢复：
        </p>
        <ul class="wayline-list">
          <li v-for="wl in selectedWaylines" :key="wl.uuid" class="wayline-item">
            {{ wl.name }}
          </li>
        </ul>
      </div>

      <div class="dialog-footer">
        <button class="btn btn--ghost" :disabled="loading" @click="emit('cancel')">
          取消
        </button>
        <button class="btn btn--danger" :disabled="loading" @click="emit('confirm')">
          {{ loading ? '删除中…' : '确认删除' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 9000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}

.dialog-card {
  width: 420px;
  max-width: 90vw;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 24px 0;
}

.dialog-icon {
  color: var(--app-danger);
  flex-shrink: 0;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.dialog-body {
  padding: 16px 24px;
  overflow-y: auto;
  min-height: 0;
}

.dialog-message {
  font-size: 13px;
  color: var(--app-text-secondary);
  margin: 0 0 12px;
  line-height: 1.5;
}

.wayline-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 180px;
  overflow-y: auto;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
}

.wayline-item {
  padding: 8px 12px;
  font-size: 13px;
  color: var(--app-text);
  border-bottom: 1px solid var(--app-border);
}

.wayline-item:last-child {
  border-bottom: none;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px 20px;
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

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn--ghost {
  background: var(--app-bg-surface);
  border-color: var(--app-border);
  color: var(--app-text-secondary);
}

.btn--ghost:hover:not(:disabled) {
  background: var(--app-bg-card);
  border-color: var(--app-border-strong);
}

.btn--danger {
  background: var(--app-danger);
  color: #fff;
  box-shadow: 0 2px 8px rgba(248, 113, 113, 0.3);
}

.btn--danger:hover:not(:disabled) {
  filter: brightness(1.1);
  box-shadow: 0 4px 12px rgba(248, 113, 113, 0.4);
}
</style>
