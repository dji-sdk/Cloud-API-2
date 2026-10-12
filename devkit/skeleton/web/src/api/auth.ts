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

import type { AuthResponse, LoginRequest, RegisterRequest, UserInfo } from '@/types/auth'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import { API_BASE } from './baseUrl'

const AUTH_BASE = `${API_BASE}/api/auth`
const USERS_BASE = `${API_BASE}/api/users`

function isMockEnabled(): boolean {
  return getRuntimeConfig('MOCK_ENABLED') === 'true'
}

const MOCK_USER: UserInfo = {
  id: 1,
  username: 'admin',
  nickname: 'Mock Admin',
  role: 'admin',
  email: null,
  emailVerified: false,
  status: 'approved',
  createdAt: null,
}

const MOCK_TOKEN = 'mock-jwt-token-for-local-dev'

/**
 * 获取 localStorage 中的 JWT token。
 */
export function getToken(): string | null {
  try {
    const stored = localStorage.getItem('auth')
    if (!stored) {
      return null
    }
    const parsed: unknown = JSON.parse(stored)
    if (typeof parsed === 'object' && parsed !== null && 'token' in parsed) {
      return (parsed as { token: string | null }).token
    }
    return null
  } catch {
    return null
  }
}

/**
 * 带认证的 fetch 封装。401 时触发登出。
 */
export async function authFetch(url: string, init?: RequestInit): Promise<Response> {
  const headers = new Headers(init?.headers)
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const res = await fetch(url, { ...init, headers })
  if (res.status === 401) {
    localStorage.removeItem('auth')
    window.location.href = '/welcome'
    // 抛出异常阻止 caller 继续处理 response，避免二次错误提示
    throw new Error('登录已过期，正在跳转登录页...')
  }
  return res
}

/** 用户注册。 */
export async function register(req: RegisterRequest): Promise<AuthResponse> {
  if (isMockEnabled()) {
    return { token: MOCK_TOKEN, user: { ...MOCK_USER, username: req.username, nickname: req.nickname ?? req.username }, message: null }
  }
  const res = await fetch(`${AUTH_BASE}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '注册失败' })) as { error: string }
    throw new Error(body.error || '注册失败')
  }
  return res.json() as Promise<AuthResponse>
}

/** 用户登录。 */
export async function login(req: LoginRequest): Promise<AuthResponse> {
  if (isMockEnabled()) {
    return { token: MOCK_TOKEN, user: { ...MOCK_USER, username: req.username }, message: null }
  }
  const res = await fetch(`${AUTH_BASE}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '登录失败' })) as { error: string }
    throw new Error(body.error || '登录失败')
  }
  return res.json() as Promise<AuthResponse>
}

/** 获取当前用户信息。 */
export async function fetchMe(): Promise<UserInfo> {
  if (isMockEnabled()) {
    try {
      const stored = localStorage.getItem('auth')
      if (stored) {
        const parsed = JSON.parse(stored) as { user?: UserInfo }
        if (parsed.user) {
          return parsed.user
        }
      }
    } catch {
      // localStorage 解析失败时回退到默认 mock 用户
    }
    return MOCK_USER
  }
  const res = await authFetch(`${USERS_BASE}/me`)
  if (!res.ok) {
    throw new Error('获取用户信息失败')
  }
  return res.json() as Promise<UserInfo>
}

// ==================== 管理员 API ====================

/** 获取所有用户列表（仅 admin）。 */
export async function fetchUsers(): Promise<UserInfo[]> {
  if (isMockEnabled()) {
    return [
      MOCK_USER,
      { id: 2, username: 'alice', nickname: 'Alice', role: 'user', email: 'alice@example.com', emailVerified: true, status: 'approved', createdAt: '2026-07-20T10:30:00' },
      { id: 3, username: 'bob', nickname: 'Bob', role: 'user', email: 'bob@example.com', emailVerified: false, status: 'pending', createdAt: '2026-07-21T14:20:00' },
      { id: 4, username: 'charlie', nickname: null, role: 'user', email: null, emailVerified: false, status: 'rejected', createdAt: '2026-07-19T08:00:00' },
    ]
  }
  const res = await authFetch(USERS_BASE)
  if (!res.ok) {
    throw new Error('获取用户列表失败')
  }
  return res.json() as Promise<UserInfo[]>
}

/** 审核通过用户。 */
export async function approveUser(id: number): Promise<void> {
  const res = await authFetch(`${USERS_BASE}/${id}/approve`, { method: 'PUT' })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '操作失败' })) as { error: string }
    throw new Error(body.error || '审核通过失败')
  }
}

/** 审核拒绝用户。 */
export async function rejectUser(id: number): Promise<void> {
  const res = await authFetch(`${USERS_BASE}/${id}/reject`, { method: 'PUT' })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '操作失败' })) as { error: string }
    throw new Error(body.error || '审核拒绝失败')
  }
}

/** 变更用户角色。 */
export async function setUserRole(id: number, role: string): Promise<void> {
  const res = await authFetch(`${USERS_BASE}/${id}/role`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ role }),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '操作失败' })) as { error: string }
    throw new Error(body.error || '角色变更失败')
  }
}

/** 管理员重置用户密码。 */
export async function resetUserPassword(id: number, password: string): Promise<void> {
  const res = await authFetch(`${USERS_BASE}/${id}/reset-password`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ password }),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '操作失败' })) as { error: string }
    throw new Error(body.error || '密码重置失败')
  }
}

/**
 * 修改自己的密码（任何已认证用户，含管理员）。
 * 需校验旧密码，用于管理员自助重置自己密码等场景。
 */
export async function changeMyPassword(oldPassword: string, newPassword: string): Promise<void> {
  const res = await authFetch(`${USERS_BASE}/me/password`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ oldPassword, newPassword }),
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '操作失败' })) as { error: string }
    throw new Error(body.error || '密码修改失败')
  }
}

/** 删除用户。 */
export async function deleteUser(id: number): Promise<void> {
  const res = await authFetch(`${USERS_BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: '操作失败' })) as { error: string }
    throw new Error(body.error || '删除用户失败')
  }
}
