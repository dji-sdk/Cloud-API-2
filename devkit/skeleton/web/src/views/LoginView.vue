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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { LogIn } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin(): Promise<void> {
  error.value = ''
  loading.value = true
  try {
    await authStore.login({ username: username.value, password: password.value })
    await router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-bg-orb auth-bg-orb--1" />
    <div class="auth-bg-orb auth-bg-orb--2" />
    <div class="auth-bg-orb auth-bg-orb--3" />

    <div class="auth-card">
      <div class="auth-brand">
        <img src="@/assets/logo.svg" alt="DJI Cloud API 2" class="brand-logo" />
        <p class="brand-subtitle">无人机管理平台</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            required
            autocomplete="username"
          />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            required
            autocomplete="current-password"
          />
        </div>

        <p v-if="error" class="auth-error">{{ error }}</p>

        <button type="submit" class="auth-btn" :disabled="loading">
          <LogIn v-if="!loading" :size="16" :stroke-width="2" />
          <span>{{ loading ? '登录中...' : '登录' }}</span>
        </button>
      </form>

      <div class="auth-footer">
        <router-link to="/register" class="auth-link-btn">创建新账号</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #080c14;
  position: relative;
  overflow: hidden;
}

/* 背景光效 */
.auth-bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  pointer-events: none;
}

.auth-bg-orb--1 {
  width: 400px;
  height: 400px;
  background: var(--app-primary, #06b6d4);
  top: -120px;
  right: -80px;
  opacity: 0.15;
}

.auth-bg-orb--2 {
  width: 300px;
  height: 300px;
  background: #7c3aed;
  bottom: -100px;
  left: -60px;
  opacity: 0.12;
}

.auth-bg-orb--3 {
  width: 200px;
  height: 200px;
  background: var(--app-primary, #06b6d4);
  top: 50%;
  left: 60%;
  opacity: 0.08;
}

/* 卡片 */
.auth-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 32px;
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-lg);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

/* 品牌区 */
.auth-brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand-logo {
  height: 48px;
  width: auto;
  margin-bottom: 12px;
}

.brand-subtitle {
  font-size: 13px;
  color: #8b949e;
  margin: 0;
}

/* 表单 */
.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  color: #8b949e;
  font-size: 12px;
  font-weight: 500;
}

.form-group input {
  padding: 12px 14px;
  border: none;
  border-bottom: 1.5px solid rgba(255, 255, 255, 0.12);
  border-radius: 0;
  background: transparent;
  color: #e6edf3;
  font-size: 14px;
  outline: none;
  transition: border-color var(--transition-base);
}

.form-group input:focus {
  border-bottom-color: var(--app-primary);
}

.form-group input::placeholder {
  color: #6b7280;
}

.auth-error {
  color: #f87171;
  font-size: 13px;
  margin: 0;
  padding: 8px 12px;
  background: rgba(248, 113, 113, 0.08);
  border-radius: var(--radius-sm);
  border-left: 3px solid #f87171;
}

.auth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--app-primary), var(--app-primary-active));
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  margin-top: 4px;
}

.auth-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--app-primary-glow);
}

.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 底部链接 */
.auth-footer {
  margin-top: 24px;
  text-align: center;
}

.auth-link-btn {
  display: inline-block;
  width: 100%;
  padding: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: #8b949e;
  font-size: 13px;
  text-decoration: none;
  transition: all var(--transition-base);
}

.auth-link-btn:hover {
  border-color: var(--app-primary);
  color: var(--app-primary);
  background: var(--app-primary-bg);
}
</style>
