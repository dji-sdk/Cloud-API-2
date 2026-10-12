// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/** 大疆上云 API 2 前端入口 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './styles/cockpit.scss'

const app = createApp(App)
app.use(createPinia())
app.mount('#app')
