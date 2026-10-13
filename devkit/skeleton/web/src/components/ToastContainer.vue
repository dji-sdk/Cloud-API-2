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
import { useToastStore } from '@/stores/toast'
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-vue-next'
import type { Component } from 'vue'
import type { ToastType } from '@/stores/toast'

const store = useToastStore()

const iconMap: Record<ToastType, Component> = {
  success: CheckCircle,
  error: XCircle,
  warning: AlertTriangle,
  info: Info,
}
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="toast">
        <div
          v-for="msg in store.messages"
          :key="msg.id"
          class="toast-item"
          :class="`toast-item--${msg.type}`"
        >
          <component :is="iconMap[msg.type]" :size="16" :stroke-width="2" class="toast-icon" />
          <span class="toast-text">{{ msg.text }}</span>
          <button
            v-if="msg.action"
            class="toast-action"
            @click="() => { msg.action!.onClick(); store.dismiss(msg.id) }"
          >
            {{ msg.action.label }}
          </button>
          <button class="toast-close" @click="store.dismiss(msg.id)">
            <X :size="14" :stroke-width="2" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-strong);
  box-shadow: var(--shadow-lg);
  font-size: 13px;
  color: var(--app-text);
  pointer-events: auto;
  min-width: 240px;
  max-width: 380px;
}

.toast-item--success .toast-icon { color: var(--app-success); }
.toast-item--error .toast-icon { color: var(--app-danger); }
.toast-item--warning .toast-icon { color: var(--app-warning); }
.toast-item--info .toast-icon { color: var(--app-info); }

.toast-text {
  flex: 1;
  line-height: 1.4;
}

.toast-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.toast-close:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.toast-action {
  flex-shrink: 0;
  padding: 2px 8px;
  border: 1px solid var(--app-primary);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.toast-action:hover {
  background: var(--app-primary);
  color: #fff;
}

/* 动画 */
.toast-enter-active {
  transition: all 0.25s ease;
}

.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(20px) scale(0.95);
}

.toast-move {
  transition: transform 0.25s ease;
}
</style>
