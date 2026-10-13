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
import { ref, onMounted, onUnmounted } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useThemeStore } from '@/stores/theme'
import { Palette } from 'lucide-vue-next'

const store = useThemeStore()
const { setPrimaryColor } = useTheme()
const showColorPicker = ref(false)
const toggleRef = ref<HTMLElement | null>(null)

function onColorChange(e: Event): void {
  const input = e.target as HTMLInputElement
  setPrimaryColor(input.value)
}

function handleClickOutside(e: MouseEvent): void {
  if (toggleRef.value && !toggleRef.value.contains(e.target as Node)) {
    showColorPicker.value = false
  }
}

onMounted(() => document.addEventListener('pointerdown', handleClickOutside))
onUnmounted(() => document.removeEventListener('pointerdown', handleClickOutside))
</script>

<template>
  <div ref="toggleRef" class="theme-toggle">
    <button
      class="toggle-btn"
      title="自定义主题色"
      :class="{ 'toggle-btn--active': showColorPicker }"
      @click="showColorPicker = !showColorPicker"
    >
      <Palette :size="16" :stroke-width="1.75" />
    </button>

    <div v-if="showColorPicker" class="color-popover" @pointerdown.stop>
      <input
        type="color"
        class="color-input"
        :value="store.primaryColor"
        @input="onColorChange"
      />
      <span class="color-label">{{ store.primaryColor }}</span>
    </div>
  </div>
</template>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  position: relative;
}

.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: none;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  color: var(--app-text-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
}

.toggle-btn:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

.toggle-btn--active {
  background: var(--app-primary-bg);
  color: var(--app-primary);
  border-color: var(--app-primary);
}

.color-popover {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  z-index: 100;
}

.color-input {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  padding: 0;
  background: none;
}

.color-input::-webkit-color-swatch-wrapper {
  padding: 0;
}

.color-input::-webkit-color-swatch {
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
}

.color-label {
  font-size: 11px;
  font-family: var(--font-mono);
  color: var(--app-text-muted);
}
</style>
