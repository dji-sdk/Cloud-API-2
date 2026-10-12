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
import type { PlayerSlot } from '@/types/liveStream'

defineProps<{
  slot: PlayerSlot
  containerId: string
}>()

defineEmits<{
  close: []
}>()
</script>

<template>
  <div class="grid-slot" :class="{ 'grid-slot--empty': !slot.id }">
    <template v-if="slot.id && slot.sn">
      <div :id="containerId" class="grid-slot__player" />
      <div class="grid-slot__overlay">
        <span class="grid-slot__sn">{{ slot.sn }}</span>
        <span
          class="grid-slot__state"
          :class="`grid-slot__state--${slot.state}`"
        >
          {{ slot.state }}
        </span>
        <button class="grid-slot__close" title="关闭" @click="$emit('close')">
          &times;
        </button>
      </div>
      <div v-if="slot.error" class="grid-slot__error">
        {{ typeof slot.error === 'string' ? slot.error : '播放错误' }}
      </div>
    </template>
    <template v-else>
      <div class="grid-slot__empty">
        <span class="grid-slot__empty-dot" />
        <span class="grid-slot__empty-text">无视频</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.grid-slot {
  position: relative;
  background: var(--app-bg);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
  min-height: 0;
}

.grid-slot--empty {
  border-style: dashed;
}

.grid-slot__player {
  width: 100%;
  height: 100%;
  position: absolute;
  inset: 0;
}

.grid-slot__overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.6) 0%, transparent 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
  z-index: 2;
}

.grid-slot:hover .grid-slot__overlay {
  opacity: 1;
}

.grid-slot__sn {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
  font-family: var(--font-mono);
}

.grid-slot__state {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
}

.grid-slot__state--playing {
  background: var(--app-success-bg);
  color: var(--app-success);
}

.grid-slot__state--error {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}

.grid-slot__close {
  margin-left: auto;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition-base);
}

.grid-slot__close:hover {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}

.grid-slot__error {
  position: absolute;
  bottom: 4px;
  left: 4px;
  right: 4px;
  font-size: 10px;
  color: var(--app-danger);
  background: rgba(0, 0, 0, 0.7);
  padding: 2px 6px;
  border-radius: 2px;
  z-index: 2;
}

.grid-slot__empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.grid-slot__empty-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-border-strong);
}

.grid-slot__empty-text {
  font-size: 11px;
  color: var(--app-text-muted);
}
</style>
