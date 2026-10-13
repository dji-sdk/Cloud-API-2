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
import { computed } from 'vue'
import type { GridLayoutKey, LayoutGroup, FillMode } from '@/types/liveStream'
import { LAYOUT_CONFIGS, LAYOUT_GROUPS, LAYOUT_GROUP_LABELS } from './layoutConfig'

const props = defineProps<{
  modelValue: GridLayoutKey
  fillMode: FillMode
  showOsd: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: GridLayoutKey]
  'update:fillMode': [value: FillMode]
  'update:showOsd': [value: boolean]
}>()

const currentGroup = computed((): LayoutGroup => {
  for (const [group, keys] of Object.entries(LAYOUT_GROUPS) as [LayoutGroup, GridLayoutKey[]][]) {
    if (keys.includes(props.modelValue)) return group
  }
  return 'normal'
})
</script>

<template>
  <div class="settings-panel">
    <div class="settings-section">
      <div class="settings-section__title">布局设置</div>
      <div
        v-for="(keys, group) in LAYOUT_GROUPS"
        :key="group"
        class="settings-group"
      >
        <div class="settings-group__title">{{ LAYOUT_GROUP_LABELS[group as LayoutGroup] }}</div>
        <div class="settings-group__grid">
          <button
            v-for="key in keys"
            :key="key"
            class="grid-btn"
            :class="{ 'grid-btn--active': modelValue === key }"
            :title="`${LAYOUT_CONFIGS[key].max} 画面`"
            @click="emit('update:modelValue', key)"
          >
            <span class="grid-btn__num">{{ LAYOUT_CONFIGS[key].max }}</span>
          </button>
        </div>
      </div>
    </div>

    <div class="settings-divider" />

    <div class="settings-section">
      <div class="settings-section__title">画面设置</div>
      <div class="settings-row">
        <span class="settings-row__label">OSD 信息</span>
        <label class="toggle">
          <input
            type="checkbox"
            :checked="showOsd"
            @change="emit('update:showOsd', ($event.target as HTMLInputElement).checked)"
          />
          <span class="toggle__track" />
        </label>
      </div>
      <div class="settings-row">
        <span class="settings-row__label">画面显示</span>
        <div class="radio-group">
          <button
            class="radio-btn"
            :class="{ 'radio-btn--active': fillMode === 'contain' }"
            @click="emit('update:fillMode', 'contain')"
          >
            自适应
          </button>
          <button
            class="radio-btn"
            :class="{ 'radio-btn--active': fillMode === 'fill' }"
            @click="emit('update:fillMode', 'fill')"
          >
            填充
          </button>
        </div>
      </div>
    </div>

    <div class="settings-info">
      <span class="settings-info__group">{{ LAYOUT_GROUP_LABELS[currentGroup] }}</span>
      <span class="settings-info__count">{{ LAYOUT_CONFIGS[modelValue].max }} 画面</span>
    </div>
  </div>
</template>

<style scoped>
.settings-panel {
  padding: 12px;
}

.settings-section__title {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.settings-group {
  margin-bottom: 12px;
}

.settings-group__title {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-bottom: 6px;
}

.settings-group__grid {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.grid-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.grid-btn:hover {
  border-color: var(--app-border-strong);
  color: var(--app-text);
}

.grid-btn--active {
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
  color: var(--app-primary);
}

.settings-divider {
  height: 1px;
  background: var(--app-border);
  margin: 12px 0;
}

.settings-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.settings-row__label {
  font-size: 13px;
  color: var(--app-text);
}

.toggle {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.toggle input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle__track {
  display: block;
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background: var(--app-bg-elevated);
  transition: background var(--transition-base);
}

.toggle__track::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  transition: transform var(--transition-base);
}

.toggle input:checked + .toggle__track {
  background: var(--app-primary);
}

.toggle input:checked + .toggle__track::after {
  transform: translateX(16px);
}

.radio-group {
  display: flex;
  gap: 4px;
}

.radio-btn {
  padding: 4px 10px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.radio-btn:hover {
  border-color: var(--app-border-strong);
  color: var(--app-text);
}

.radio-btn--active {
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
  color: var(--app-primary);
}

.settings-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 12px;
  color: var(--app-text-muted);
}

.settings-info__group {
  padding: 2px 6px;
  background: var(--app-bg-surface);
  border-radius: var(--radius-sm);
}
</style>
