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

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import DashboardView     from '@/views/DashboardView.vue'
import DeviceManagerView from '@/views/DeviceManagerView.vue'
import CockpitView       from '@/views/CockpitView.vue'
import WaylineView       from '@/views/WaylineView.vue'
import FlightReplayView  from '@/views/FlightReplayView.vue'
import MediaManagerView  from '@/views/MediaManagerView.vue'
import FlightAreaView    from '@/views/FlightAreaView.vue'
import LiveStreamView    from '@/views/LiveStreamView.vue'
import DeviceWindowView  from '@/views/DeviceWindowView.vue'
import UserManagementView from '@/views/UserManagementView.vue'
import LoginView         from '@/views/LoginView.vue'
import RegisterView      from '@/views/RegisterView.vue'
import FlightTaskView    from '@/views/FlightTaskView.vue'
import FlightTaskCreateView from '@/views/FlightTaskCreateView.vue'
import WelcomeView       from '@/views/WelcomeView.vue'

const routes: RouteRecordRaw[] = [
  { path: '/welcome',         component: WelcomeView,       meta: { public: true } },
  { path: '/login',           component: LoginView,         meta: { public: true } },
  { path: '/register',        component: RegisterView,      meta: { public: true } },
  { path: '/',                component: DashboardView },
  { path: '/devices',         component: DeviceManagerView },
  { path: '/device-manager',  redirect: '/devices' },
  { path: '/cockpit',         component: CockpitView },
  { path: '/wayline',         component: WaylineView },
  { path: '/flight-tasks',    component: FlightTaskView },
  { path: '/flight-tasks/create', component: FlightTaskCreateView },
  { path: '/flight-replay',   component: FlightReplayView },
  { path: '/media',           component: MediaManagerView },
  { path: '/flight-areas',   component: FlightAreaView },
  { path: '/live-stream',    component: LiveStreamView },
  { path: '/device-window',  component: DeviceWindowView },
  { path: '/admin/users',    component: UserManagementView, meta: { requiresAdmin: true } },
  { path: '/models',          component: () => import('@/views/ModelsView.vue') },
]

// MOCK_ENABLED=true 时条件注册 mock-debug 路由
if (getRuntimeConfig('MOCK_ENABLED') === 'true') {
  routes.push({
    path: '/mock-debug',
    component: () => import('@/views/MockDebugView.vue'),
  })
}

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：未登录用户重定向到引导着陆页，admin 路由检查角色
router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }

  // 检查 localStorage 中是否有 token（不依赖 store 避免循环引用）
  try {
    const stored = localStorage.getItem('auth')
    if (stored) {
      const parsed: unknown = JSON.parse(stored)
      if (typeof parsed === 'object' && parsed !== null && 'token' in parsed) {
        const authData = parsed as { token: string | null; user?: { role?: string } }
        if (authData.token) {
          // admin 路由检查角色
          if (to.meta.requiresAdmin && authData.user?.role !== 'admin') {
            return { path: '/' }
          }
          return true
        }
      }
    }
  } catch {
    // parse 失败当未登录处理
  }

  // 未登录：根路径展示引导着陆页，其余受保护路由统一跳转 /welcome
  return { path: '/welcome' }
})
