/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/auth'
import {
  login as apiLogin,
  register as apiRegister,
  fetchMe,
} from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/types/auth'

/** 认证状态 store，持久化 token 到 localStorage。 */
export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const user = ref<UserInfo | null>(null)

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 是否是管理员 */
  const isAdmin = computed(() => user.value?.role === 'admin')

  /** 登录 */
  async function login(req: LoginRequest): Promise<void> {
    const res = await apiLogin(req)
    token.value = res.token
    user.value = res.user
    if (res.token) {
      localStorage.setItem('x-user-token', res.token)
    }
  }

  /** 注册。返回 message（待审核提示）或 null（直接登录成功） */
  async function register(req: RegisterRequest): Promise<string | null> {
    const res = await apiRegister(req)
    if (res.token) {
      token.value = res.token
      user.value = res.user
      localStorage.setItem('x-user-token', res.token)
      return null
    }
    // pending 用户：不保存 token，返回提示消息
    return res.message ?? '注册成功，请等待管理员审核'
  }

  /** 登出 */
  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('x-user-token')
  }

  /** 刷新用户信息（页面加载时调用，验证 token 有效性） */
  async function refreshUser(): Promise<void> {
    if (!token.value) {
      return
    }
    localStorage.setItem('x-user-token', token.value)
    try {
      user.value = await fetchMe()
    } catch {
      logout()
    }
  }

  return { token, user, isLoggedIn, isAdmin, login, register, logout, refreshUser }
}, { persist: true })
