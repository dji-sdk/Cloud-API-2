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
import type { TcasConfig } from '@/types/flightArea'
import { fetchTcasConfig, updateTcasConfig } from '@/api/tcas'

export const useTcasStore = defineStore('tcas', () => {
  const config = ref<TcasConfig | null>(null)
  const loading = ref(false)
  const airSenseEnabled = ref(true)
  const safeSEnabled = ref(true)

  async function loadConfig(): Promise<void> {
    loading.value = true
    try {
      config.value = await fetchTcasConfig()
    } finally {
      loading.value = false
    }
  }

  async function saveConfig(newConfig: TcasConfig): Promise<void> {
    await updateTcasConfig(newConfig)
    config.value = newConfig
  }

  return { config, loading, airSenseEnabled, safeSEnabled, loadConfig, saveConfig }
})
