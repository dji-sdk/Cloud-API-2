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
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import type { ModelFile } from '@/types/terra'
import { formatDateTime } from '@/utils/format'
import { Map, Layers, CircleDot, ScanEye, Sparkles, Radar, MoreHorizontal } from 'lucide-vue-next'
import type { Component } from 'vue'

const props = defineProps<{
  model: ModelFile
  selected: boolean
}>()

const emit = defineEmits<{
  select: [fileId: number]
  preview: [fileId: number]
  download: [fileId: number]
  delete: [fileId: number]
  viewReport: [taskId: string]
  viewImages: [taskId: string]
}>()

const menuOpen = ref(false)
const thumbnailFailed = ref(false)
const moreRef = ref<HTMLElement | null>(null)

interface TypeMeta {
  label: string
  icon: Component
  color: string
}

const typeMetas: Record<string, TypeMeta> = {
  ortho_2d: { label: '正射影像', icon: Map, color: '#4CAF50' },
  mesh_3d: { label: '三维网格', icon: Layers, color: '#2196F3' },
  point_cloud: { label: '点云', icon: CircleDot, color: '#9C27B0' },
  panorama: { label: '全景', icon: ScanEye, color: '#FF9800' },
  '3dgs': { label: '3DGS', icon: Sparkles, color: '#E91E63' },
  lidar: { label: '激光雷达', icon: Radar, color: '#00BCD4' },
}

const meta = computed(() => typeMetas[props.model.reconstructionType] ?? {
  label: props.model.reconstructionType,
  icon: Map,
  color: '#757575',
})

function handleCardClick() {
  if (menuOpen.value) {
    menuOpen.value = false
    return
  }
  emit('preview', props.model.fileId)
}

function onDocumentClick(event: Event) {
  if (moreRef.value && !moreRef.value.contains(event.target as Node)) {
    menuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onDocumentClick)
})

function toggleMenu(event: Event) {
  event.stopPropagation()
  menuOpen.value = !menuOpen.value
}

function handleAction(action: string, event: Event) {
  event.stopPropagation()
  menuOpen.value = false
  switch (action) {
    case 'download':
      emit('download', props.model.fileId)
      break
    case 'report':
      emit('viewReport', props.model.uuid)
      break
    case 'images':
      emit('viewImages', props.model.uuid)
      break
    case 'delete':
      emit('delete', props.model.fileId)
      break
  }
}
</script>

<template>
  <div class="model-card" :class="{ 'model-card--selected': selected }" @click="handleCardClick">
    <!-- Checkbox -->
    <label class="card-checkbox" @click.stop>
      <input
        type="checkbox"
        :checked="selected"
        @change="emit('select', model.fileId)"
      />
    </label>

    <!-- More menu -->
    <div ref="moreRef" class="card-more">
      <button class="more-btn" @click="toggleMenu">
        <MoreHorizontal :size="18" />
      </button>
      <div v-if="menuOpen" class="card-menu">
        <button class="menu-item menu-item--disabled" disabled title="SDK 未提供有效下载链接">下载</button>
        <button class="menu-item" @click="handleAction('report', $event)">质量报告</button>
        <button class="menu-item" @click="handleAction('images', $event)">查看影像</button>
        <button class="menu-item menu-item--danger" @click="handleAction('delete', $event)">删除</button>
      </div>
    </div>

    <!-- Thumbnail -->
    <div class="card-thumbnail">
      <img
        v-if="model.thumbnailUrl && !thumbnailFailed"
        :src="model.thumbnailUrl"
        :alt="model.name"
        class="thumbnail-img"
        loading="lazy"
        @error="thumbnailFailed = true"
      />
      <component
        :is="meta.icon"
        v-else
        :size="40"
        :stroke-width="1.2"
        class="type-icon"
        :style="{ color: meta.color }"
      />
    </div>

    <!-- Info -->
    <div class="card-info">
      <div class="card-name" :title="model.name">{{ model.name }}</div>
      <div class="card-time">创建时间：{{ formatDateTime(model.createdAt) }}</div>
    </div>
  </div>
</template>

<style scoped>
.model-card {
  position: relative;
  border-radius: var(--radius-md);
  background: var(--app-bg-card);
  border: 1px solid var(--app-border);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
}

.model-card:hover {
  background: var(--app-bg-card-hover);
  border-color: var(--app-border-strong);
  box-shadow: var(--shadow-sm);
}

.model-card--selected {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 1px var(--app-primary);
}

.card-checkbox {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  cursor: pointer;
}

.card-checkbox input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: var(--app-primary);
  cursor: pointer;
}

.card-more {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  background: rgba(0, 0, 0, 0.4);
  border: none;
  color: var(--app-text);
  cursor: pointer;
  transition: background var(--transition-base);
}

.more-btn:hover {
  background: rgba(0, 0, 0, 0.6);
}

.card-menu {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  min-width: 120px;
  padding: var(--space-xs) 0;
  z-index: 30;
}

.menu-item {
  display: block;
  width: 100%;
  padding: 8px 14px;
  border: none;
  background: none;
  color: var(--app-text-secondary);
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-base);
}

.menu-item:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.menu-item--disabled {
  color: var(--app-text-muted);
  cursor: not-allowed;
  opacity: 0.5;
}

.menu-item--disabled:hover {
  background: none;
  color: var(--app-text-muted);
}

.menu-item--danger {
  color: var(--app-danger);
}

.menu-item--danger:hover {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}

.card-thumbnail {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-surface);
  overflow: hidden;
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.type-icon {
  opacity: 0.8;
}

.card-info {
  padding: 12px 14px;
}

.card-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 6px;
}

.card-time {
  font-size: 12px;
  color: var(--app-text-muted);
  line-height: 1.5;
}
</style>
