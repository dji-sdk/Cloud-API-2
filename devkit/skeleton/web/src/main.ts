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

// Polyfill: crypto.randomUUID 仅在安全上下文（HTTPS / localhost）可用，
// HTTP 环境下 WaylineEditor SDK 等第三方库调用会抛 TypeError。
if (typeof crypto !== 'undefined' && typeof crypto.randomUUID !== 'function') {
  crypto.randomUUID = () =>
    'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = (Math.random() * 16) | 0
      return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16)
    }) as `${string}-${string}-${string}-${string}-${string}`
}

import { createApp }   from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import { router }      from './router'
import App             from './App.vue'
import { useTheme }    from './composables/useTheme'
import { useAuthStore } from './stores/auth'
import './styles/global.css'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

const app = createApp(App)
app.use(pinia)
app.use(router)

// 在 mount 前同步应用持久化主题，避免页面初始闪烁
// useTheme() 调用 useThemeStore()，此时 pinia 已注册，persisted state 已恢复
const { applyTheme } = useTheme()
applyTheme()

// 如果有持久化 token，启动时验证有效性
const authStore = useAuthStore()
if (authStore.isLoggedIn) {
  authStore.refreshUser()
}

app.mount('#app')
