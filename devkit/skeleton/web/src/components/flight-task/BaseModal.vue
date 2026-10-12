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
import { onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  visible: boolean
  title?: string
  width?: string
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirm: []
  cancel: []
}>()

function close(): void {
  emit('update:visible', false)
  emit('cancel')
}

function confirm(): void {
  emit('confirm')
}

function onKeydown(e: KeyboardEvent): void {
  if (!props.visible) return
  if (e.key === 'Escape') {
    close()
  }
}

onMounted(() => {
  document.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="close">
        <div class="modal-panel" :style="{ width: width || '560px' }">
          <div v-if="title" class="modal-header">
            <h3 class="modal-title">{{ title }}</h3>
            <button class="modal-close" type="button" @click="close">
              &times;
            </button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div class="modal-footer">
            <slot name="footer">
              <button class="btn btn-cancel" type="button" @click="close">取消</button>
              <button class="btn btn-confirm" type="button" @click="confirm">确认</button>
            </slot>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-overlay);
}

.modal-panel {
  display: flex;
  flex-direction: column;
  max-height: 80vh;
  border-radius: var(--radius-lg);
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.modal-close {
  border: none;
  background: none;
  font-size: 20px;
  color: var(--app-text-muted);
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-base);
}

.modal-close:hover {
  color: var(--app-text);
  background: var(--app-hover-bg);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--app-border);
  flex-shrink: 0;
}

.btn {
  padding: 8px 20px;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-cancel {
  border: 1px solid var(--app-border);
  background: transparent;
  color: var(--app-text-secondary);
}

.btn-cancel:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.btn-confirm {
  border: none;
  background: var(--app-primary);
  color: #ffffff;
  font-weight: 500;
}

.btn-confirm:hover {
  background: var(--app-primary-hover);
}

/* Transition */
.modal-enter-active,
.modal-leave-active {
  transition: opacity var(--transition-slow);
}

.modal-enter-active .modal-panel,
.modal-leave-active .modal-panel {
  transition: transform var(--transition-slow);
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-panel,
.modal-leave-to .modal-panel {
  transform: scale(0.95) translateY(10px);
}
</style>
