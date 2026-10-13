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
import UserDropdown from './UserDropdown.vue'
import { useChatStore } from '@/stores/chat'
import {
  LayoutDashboard,
  Cpu,
  Gamepad2,
  Route,
  PlayCircle,
  Camera,
  Globe,
  Box,
  MonitorPlay,

} from 'lucide-vue-next'
import type { Component } from 'vue'

interface NavItem {
  path: string
  label: string
  icon: Component
}

const route = useRoute()
const chatStore = useChatStore()

const navItems: NavItem[] = [
  { path: '/', label: '数据看板', icon: LayoutDashboard },
  { path: '/live-stream', label: '直播大屏', icon: MonitorPlay },
  { path: '/devices', label: '设备管理', icon: Cpu },
  { path: '/cockpit', label: '驾驶舱', icon: Gamepad2 },
  { path: '/wayline', label: '航线编辑器', icon: Route },
  { path: '/flight-tasks', label: '任务管理', icon: PlayCircle },
  { path: '/media', label: '媒体管理', icon: Camera },
  { path: '/flight-areas', label: '空域管理', icon: Globe },

  { path: '/models', label: '模型库', icon: Box },
]
</script>

<template>
  <header class="top-nav">
    <div class="top-nav__logo">
      <img src="@/assets/logo.svg" alt="CloudAPI2" class="logo-text" />
    </div>

    <nav class="top-nav__items">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ 'nav-item--active': route.path === item.path }"
      >
        <component :is="item.icon" class="nav-icon" :size="16" :stroke-width="1.75" />
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="top-nav__actions">
      <button
        class="ai-btn"
        :class="{ 'ai-btn--active': chatStore.isOpen }"
        title="AI 助手 (Ctrl+/)"
        @click="chatStore.toggleDrawer"
      >
        <img src="@/assets/ai-assistant.png" alt="AI 助手" class="ai-btn__icon" />
        <span class="ai-btn__label">AI 助手</span>
      </button>
      <UserDropdown />
    </div>
  </header>
</template>

<style scoped>
.top-nav {
  display: flex;
  align-items: center;
  height: 48px;
  padding: 0 20px;
  background: var(--app-bg-panel);
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
  gap: 8px;
}

.top-nav__logo {
  flex-shrink: 0;
  margin-right: 12px;
}

.logo-text {
  height: 28px;
  width: auto;
  display: block;
  white-space: nowrap;
}

.top-nav__items {
  display: flex;
  align-items: center;
  gap: 2px;
  flex: 1;
  justify-content: center;
  overflow-x: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 0 14px;
  height: 48px;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
  transition: color var(--transition-base);
  position: relative;
  border: none;
  background: none;
}

.nav-item:hover {
  color: #ffffff;
}

.nav-item--active {
  color: var(--app-primary-hover);
  font-weight: 600;
}

.nav-item--active::after {
  content: '';
  position: absolute;
  left: 14px;
  right: 14px;
  bottom: 0;
  height: 2px;
  border-radius: 1px 1px 0 0;
  background: var(--app-primary-hover);
}

.nav-item--active .nav-icon {
  color: var(--app-primary-hover);
}

.nav-icon {
  flex-shrink: 0;
  transition: color var(--transition-base);
}

.top-nav__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 12px;
}

.ai-btn {
  height: 32px;
  padding: 0 10px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all var(--transition-base);
}

.ai-btn__label {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

.ai-btn__icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
}

.ai-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
}

.ai-btn--active {
  background: rgba(124, 58, 237, 0.12);
  color: #a78bfa;
}
</style>
