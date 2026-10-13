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
import { useRoute } from 'vue-router'
import ThemeToggle from './ThemeToggle.vue'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import {
  LayoutDashboard,
  Cpu,
  Gamepad2,
  Route,
  PlayCircle,
  Camera,
  Wrench,
  Globe,
  Box,

} from 'lucide-vue-next'
import type { Component } from 'vue'

interface NavItem {
  path: string
  label: string
  icon: Component
}

const route = useRoute()
const mockEnabled = getRuntimeConfig('MOCK_ENABLED') === 'true'

const navItems: NavItem[] = [
  { path: '/', label: '数据看板', icon: LayoutDashboard },
  { path: '/devices', label: '设备管理', icon: Cpu },
  { path: '/cockpit', label: '飞行舱', icon: Gamepad2 },
  { path: '/wayline', label: '航线库', icon: Route },
  { path: '/flight-tasks', label: '任务管理', icon: PlayCircle },
  { path: '/media', label: '媒体管理', icon: Camera },
  { path: '/flight-areas', label: '空域管理', icon: Globe },

  { path: '/models', label: '模型库', icon: Box },
]
</script>

<template>
  <aside class="app-sidebar">
    <div class="sidebar-header">
      <img src="@/assets/logo.svg" alt="CloudAPI2" class="sidebar-logo" />
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ 'nav-item--active': route.path === item.path }"
      >
        <component :is="item.icon" class="nav-icon" :size="18" :stroke-width="1.75" />
        <span class="nav-label">{{ item.label }}</span>
      </router-link>

      <router-link
        v-if="mockEnabled"
        to="/mock-debug"
        class="nav-item nav-item--debug"
        :class="{ 'nav-item--active': route.path === '/mock-debug' }"
      >
        <Wrench class="nav-icon" :size="16" :stroke-width="1.75" />
        <span class="nav-label">Mock Debug</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <ThemeToggle />
    </div>
  </aside>
</template>

<style scoped>
.app-sidebar {
  width: 220px;
  min-height: 100vh;
  background: var(--sidebar-bg);
  color: var(--app-text);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  border-right: 1px solid var(--app-border);
}

.sidebar-header {
  padding: 20px 16px 16px;
  border-bottom: 1px solid var(--app-border);
}

.sidebar-logo {
  height: 24px;
  width: auto;
  display: block;
}

.sidebar-nav {
  flex: 1;
  padding: 8px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  transition: all var(--transition-base);
  border-radius: var(--radius-md);
  margin-bottom: 2px;
  position: relative;
}

.nav-item:hover {
  background: var(--app-hover-bg);
  color: #ffffff;
}

.nav-item--active {
  background: var(--app-primary-bg-hover);
  color: var(--app-primary-hover);
  font-weight: 500;
}

.nav-item--active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--app-primary);
}

.nav-item--active .nav-icon {
  color: var(--app-primary);
}

.nav-item--debug {
  margin-top: 8px;
  border-top: 1px solid var(--app-border);
  padding-top: 18px;
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
}

.nav-icon {
  width: 20px;
  flex-shrink: 0;
  transition: color var(--transition-base);
}

.nav-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--app-border);
}
</style>
