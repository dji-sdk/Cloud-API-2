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
import type { StorageStats } from '@/types/terra'
import {
  Box,
  Map,
  Layers,
  PanelLeftClose,
  PanelLeftOpen,
  ListTodo,
} from 'lucide-vue-next'
import { ref } from 'vue'

interface Props {
  storageStats: StorageStats
  activeTab: string
  collapsed: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:activeTab': [value: string]
  'update:collapsed': [value: boolean]
}>()

const modelExpanded = ref(true)

function selectTab(tab: string) {
  emit('update:activeTab', tab)
}

function toggleCollapse() {
  emit('update:collapsed', !props.collapsed)
}
</script>

<template>
  <aside class="terra-sidebar" :class="{ 'terra-sidebar--collapsed': collapsed }">
    <div class="sidebar-header">
      <span v-if="!collapsed" class="sidebar-title">资源库</span>
      <button class="collapse-btn" @click="toggleCollapse">
        <PanelLeftClose v-if="!collapsed" :size="16" />
        <PanelLeftOpen v-else :size="16" />
      </button>
    </div>

    <template v-if="!collapsed">
      <!-- 模型（可展开） -->
      <div
        class="nav-item nav-item--expandable"
        :class="{ 'nav-item--active': ['ortho_2d', 'mesh_3d', 'panorama'].includes(activeTab) }"
        @click="modelExpanded = !modelExpanded"
      >
        <Box :size="18" :stroke-width="1.5" class="nav-icon" />
        <span class="nav-label">模型</span>
        <span class="nav-size">{{ storageStats.modelTotalGB }} GB</span>
      </div>

      <div v-if="modelExpanded" class="nav-children">
        <div
          class="nav-child"
          :class="{ 'nav-child--active': activeTab === 'ortho_2d' }"
          @click="selectTab('ortho_2d')"
        >
          <Map :size="14" :stroke-width="1.5" class="nav-child-icon" />
          <span>二维模型</span>
          <span class="nav-size">{{ storageStats.model2dGB }} GB</span>
        </div>
        <div
          class="nav-child"
          :class="{ 'nav-child--active': activeTab === 'mesh_3d' }"
          @click="selectTab('mesh_3d')"
        >
          <Layers :size="14" :stroke-width="1.5" class="nav-child-icon" />
          <span>三维模型</span>
          <span class="nav-size">{{ storageStats.model3dGB }} GB</span>
        </div>
      </div>

      <!-- 重建任务 -->
      <div
        class="nav-item"
        :class="{ 'nav-item--active': activeTab === 'tasks' }"
        @click="selectTab('tasks')"
      >
        <ListTodo :size="18" :stroke-width="1.5" class="nav-icon" />
        <span class="nav-label">重建任务</span>
      </div>
    </template>
  </aside>
</template>

<style scoped>
.terra-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: var(--app-bg-panel);
  border-right: 1px solid var(--app-border);
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  overflow-y: auto;
  transition: width var(--transition-slow);
}

.terra-sidebar--collapsed {
  width: 48px;
  padding: var(--space-sm);
  align-items: center;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-md);
}

.sidebar-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.collapse-btn {
  background: none;
  border: none;
  color: var(--app-text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.collapse-btn:hover {
  color: var(--app-text);
  background: var(--app-hover-bg);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 10px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--app-text-secondary);
  font-size: 14px;
  transition: all var(--transition-base);
  position: relative;
}

.nav-item:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.nav-item--active {
  background: var(--app-primary-bg);
  color: var(--app-primary);
}

.nav-item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--app-primary);
}

.nav-icon {
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
}

.nav-size {
  font-size: 12px;
  color: var(--app-text-muted);
}

.nav-children {
  padding-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-child {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 8px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--app-text-secondary);
  font-size: 13px;
  transition: all var(--transition-base);
  position: relative;
}

.nav-child:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.nav-child--active {
  background: var(--app-primary-bg);
  color: var(--app-primary);
  font-weight: 500;
}

.nav-child--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--app-primary);
}

.nav-child-icon {
  flex-shrink: 0;
}
</style>
