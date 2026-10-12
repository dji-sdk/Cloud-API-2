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
import type { MediaFilterType } from '@/types/media'
import { Folder, Image, Film } from 'lucide-vue-next'
import type { Component } from 'vue'

interface Props {
  activeType: MediaFilterType
  deviceSn: string
  startTime: string
  endTime: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:activeType': [value: MediaFilterType]
  'update:deviceSn': [value: string]
  'update:startTime': [value: string]
  'update:endTime': [value: string]
}>()

interface TypeOption {
  value: MediaFilterType
  label: string
  icon: Component
}

const typeOptions: TypeOption[] = [
  { value: 'all', label: '全部', icon: Folder },
  { value: 'photo', label: '照片', icon: Image },
  { value: 'video', label: '视频', icon: Film },
]
</script>

<template>
  <aside class="media-sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">媒体类型</h3>
      <ul class="type-list">
        <li
          v-for="opt in typeOptions"
          :key="opt.value"
          class="type-item"
          :class="{ 'type-item--active': props.activeType === opt.value }"
          @click="emit('update:activeType', opt.value)"
        >
          <component :is="opt.icon" :size="16" :stroke-width="1.75" class="type-icon" />
          <span class="type-label">{{ opt.label }}</span>
        </li>
      </ul>
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">设备</h3>
      <input
        class="filter-input"
        type="text"
        placeholder="设备 SN（可选）"
        :value="props.deviceSn"
        @input="emit('update:deviceSn', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div class="sidebar-section">
      <h3 class="section-title">日期范围</h3>
      <input
        class="filter-input"
        type="date"
        :value="props.startTime"
        @input="emit('update:startTime', ($event.target as HTMLInputElement).value)"
      />
      <input
        class="filter-input"
        type="date"
        :value="props.endTime"
        @input="emit('update:endTime', ($event.target as HTMLInputElement).value)"
      />
    </div>
  </aside>
</template>

<style scoped>
.media-sidebar {
  width: 220px;
  padding: 16px;
  border-right: 1px solid var(--app-border);
  background: var(--app-bg-panel);
  flex-shrink: 0;
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.type-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--app-text-secondary);
  font-size: 13px;
  transition: all var(--transition-base);
  position: relative;
  margin-bottom: 2px;
}

.type-item:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.type-item--active {
  background: var(--app-primary-bg);
  color: var(--app-primary);
  font-weight: 500;
}

.type-item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--app-primary);
}

.type-icon {
  flex-shrink: 0;
}

.type-label {
  flex: 1;
}

.filter-input {
  width: 100%;
  padding: 8px 10px;
  margin-bottom: 8px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
  transition: border-color var(--transition-base), box-shadow var(--transition-base);
  box-sizing: border-box;
}

.filter-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px var(--app-primary-bg);
}

.filter-input::placeholder {
  color: var(--app-text-muted);
}
</style>
