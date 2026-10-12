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
import { ref, watch, nextTick } from 'vue'
import type { LogEntry } from '@/types/liveStream'

const props = defineProps<{
  logs: LogEntry[]
}>()

const panelOpen = ref(false)
const unreadCount = ref(0)
const listRef = ref<HTMLElement | null>(null)

watch(
  () => props.logs.length,
  () => {
    if (!panelOpen.value) {
      unreadCount.value++
    }
    nextTick(() => {
      if (listRef.value) {
        listRef.value.scrollTop = listRef.value.scrollHeight
      }
    })
  },
)

function togglePanel(): void {
  panelOpen.value = !panelOpen.value
  if (panelOpen.value) {
    unreadCount.value = 0
  }
}

defineExpose({ togglePanel })
</script>

<template>
  <div class="log-container">
    <button class="log-fab" @click="togglePanel">
      📋
      <span v-if="unreadCount > 0" class="log-fab__badge">
        {{ unreadCount > 99 ? '99+' : unreadCount }}
      </span>
    </button>

    <Transition name="log-panel">
      <div v-if="panelOpen" class="log-panel">
        <div class="log-panel__header">
          <span class="log-panel__title">事件日志</span>
          <div class="log-panel__actions">
            <button class="log-panel__btn" @click="panelOpen = false">×</button>
          </div>
        </div>
        <ul ref="listRef" class="log-panel__list">
          <li
            v-for="entry in logs"
            :key="entry.id"
            class="log-item"
          >
            <span class="log-item__time">{{ entry.timestamp }}</span>
            <span v-if="entry.sn" class="log-item__sn">[{{ entry.sn }}]</span>
            <span
              class="log-item__event"
              :class="{ 'log-item__event--error': entry.isError }"
            >
              {{ entry.event }}
            </span>
            <span v-if="entry.data != null" class="log-item__data">
              {{ typeof entry.data === 'object' ? JSON.stringify(entry.data) : String(entry.data) }}
            </span>
          </li>
        </ul>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.log-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 100;
}

.log-fab {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-base);
  position: relative;
}

.log-fab:hover {
  background: var(--app-bg-card);
  border-color: var(--app-border-strong);
}

.log-fab__badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: var(--app-danger);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.log-panel {
  position: absolute;
  bottom: 52px;
  right: 0;
  width: 420px;
  max-height: 320px;
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.log-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
}

.log-panel__title {
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text);
}

.log-panel__actions {
  display: flex;
  gap: 4px;
}

.log-panel__btn {
  border: none;
  background: transparent;
  color: var(--app-text-muted);
  font-size: 16px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  transition: color var(--transition-base);
}

.log-panel__btn:hover {
  color: var(--app-text);
}

.log-panel__list {
  list-style: none;
  margin: 0;
  padding: 0;
  overflow-y: auto;
  flex: 1;
}

.log-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 4px 12px;
  font-size: 11px;
  border-bottom: 1px solid var(--app-border-light);
}

.log-item__time {
  color: var(--app-text-muted);
  font-family: var(--font-mono);
  flex-shrink: 0;
}

.log-item__sn {
  color: var(--app-info);
  font-family: var(--font-mono);
  flex-shrink: 0;
}

.log-item__event {
  color: var(--app-text-secondary);
  flex-shrink: 0;
}

.log-item__event--error {
  color: var(--app-danger);
}

.log-item__data {
  color: var(--app-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.log-panel-enter-active,
.log-panel-leave-active {
  transition: all var(--transition-slow);
}

.log-panel-enter-from,
.log-panel-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
