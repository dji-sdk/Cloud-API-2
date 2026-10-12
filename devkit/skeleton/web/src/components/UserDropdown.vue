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
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTheme } from '@/composables/useTheme'
import { useThemeStore } from '@/stores/theme'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import { LogOut, ChevronDown, Users, Palette, Wrench, BookOpen } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const { setPrimaryColor } = useTheme()
const mockEnabled = getRuntimeConfig('MOCK_ENABLED') === 'true'
const open = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

function onColorChange(e: Event): void {
  const input = e.target as HTMLInputElement
  setPrimaryColor(input.value)
}

function toggle(): void {
  open.value = !open.value
}

function close(): void {
  open.value = false
}

function handleClickOutside(e: MouseEvent): void {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target as Node)) {
    close()
  }
}

onMounted(() => document.addEventListener('pointerdown', handleClickOutside))
onUnmounted(() => document.removeEventListener('pointerdown', handleClickOutside))

async function handleLogout(): Promise<void> {
  close()
  authStore.logout()
  await router.push('/welcome')
}
</script>

<template>
  <div v-if="authStore.isLoggedIn" ref="dropdownRef" class="user-dropdown">
    <button class="user-trigger" @click="toggle">
      <span class="user-avatar">
        {{ (authStore.user?.nickname || authStore.user?.username || '?').charAt(0).toUpperCase() }}
      </span>
      <span class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</span>
      <ChevronDown :size="14" :stroke-width="2" class="user-arrow" :class="{ 'user-arrow--open': open }" />
    </button>

    <Transition name="dropdown">
      <div v-if="open" class="dropdown-menu" @pointerdown.stop>
        <div class="dropdown-menu-inner">
          <div class="dropdown-header">
            <span class="dropdown-username">{{ authStore.user?.username }}</span>
            <span class="dropdown-role">{{ authStore.user?.role === 'admin' ? '管理员' : '普通用户' }}</span>
          </div>
          <div class="dropdown-divider" />
          <router-link
            v-if="authStore.isAdmin"
            to="/admin/users"
            class="dropdown-item"
            @click="close"
          >
            <Users :size="14" :stroke-width="1.75" />
            <span>用户管理</span>
          </router-link>
          <label class="dropdown-item dropdown-item--color">
            <Palette :size="14" :stroke-width="1.75" />
            <span>主题色</span>
            <input
              type="color"
              class="color-input"
              :value="themeStore.primaryColor"
              @change="onColorChange"
            />
          </label>
          <a
            class="dropdown-item"
            href="/docs/user-guide/index.html"
            target="_blank"
            rel="noopener noreferrer"
          >
            <BookOpen :size="14" :stroke-width="1.75" />
            <span>使用说明</span>
          </a>
          <router-link
            v-if="mockEnabled"
            to="/mock-debug"
            class="dropdown-item"
            @click="close"
          >
            <Wrench :size="14" :stroke-width="1.75" />
            <span>Mock Debug</span>
          </router-link>
          <div class="dropdown-divider" />
          <button class="dropdown-item dropdown-item--danger" @click="handleLogout">
            <LogOut :size="14" :stroke-width="1.75" />
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.user-dropdown {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--app-text);
  cursor: pointer;
  transition: background var(--transition-base);
}

.user-trigger:hover {
  background: var(--app-hover-bg);
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--app-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: 13px;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-arrow {
  color: var(--app-text-muted);
  transition: transform 0.2s;
}

.user-arrow--open {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  min-width: 180px;
  padding-top: 6px;
  background: transparent;
  z-index: 1000;
}

.dropdown-menu-inner {
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.dropdown-header {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dropdown-username {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
}

.dropdown-role {
  font-size: 11px;
  color: var(--app-text-muted);
}

.dropdown-divider {
  height: 1px;
  background: var(--app-border);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  border: none;
  background: transparent;
  color: var(--app-text-secondary);
  font-size: 13px;
  text-align: left;
  text-decoration: none;
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.dropdown-item:hover {
  background: var(--app-hover-bg);
}

.dropdown-item--danger:hover {
  background: rgba(239, 68, 68, 0.10);
  color: var(--app-danger);
}

.dropdown-item--color {
  cursor: pointer;
}

.color-input {
  width: 20px;
  height: 20px;
  margin-left: auto;
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

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.15s, transform 0.15s;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
