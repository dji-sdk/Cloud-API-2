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
import { ref } from 'vue'
import { getRuntimeConfig } from '@/utils/runtimeConfig'

/** 主题状态 store，持久化到 localStorage（pinia-plugin-persistedstate）。 */
export const useThemeStore = defineStore('theme', () => {
  const primaryColor = ref<string>(
    getRuntimeConfig('PRIMARY_COLOR', '#06b6d4'),
  )
  const primaryHoverColor = ref<string>(
    getRuntimeConfig('PRIMARY_HOVER_COLOR'),
  )
  return { primaryColor, primaryHoverColor }
}, { persist: true })
