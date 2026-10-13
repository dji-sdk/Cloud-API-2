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
import { fetchDevices } from '@/api/devices'
import { API_BASE } from '@/api/baseUrl'
import type { DeviceState } from '@/types/api'

const sn         = ref('')
const devices    = ref<DeviceState[]>([])
const lastResult = ref<string | null>(null)
const loading    = ref(false)
let   pollId: ReturnType<typeof setInterval> | null = null

async function refreshDevices() {
  try {
    devices.value = await fetchDevices()
  } catch {
    // 静默，调试面板不因轮询失败中断
  }
}

onMounted(() => {
  refreshDevices()
  pollId = setInterval(refreshDevices, 3000)
})

onUnmounted(() => {
  if (pollId !== null) {
    clearInterval(pollId)
  }
})

async function trigger(action: string) {
  if (!sn.value) {
    lastResult.value = '请先输入设备 SN'
    return
  }
  loading.value = true
  try {
    const res = await fetch(`${API_BASE}/mock/devices/${encodeURIComponent(sn.value)}/${action}`, { method: 'POST' })
    lastResult.value = `${action}: ${res.status} ${res.statusText}`
    await refreshDevices()
  } catch (e) {
    lastResult.value = `${action} 失败: ${String(e)}`
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mock-debug">
    <h2>Mock 调试面板</h2>
    <p class="hint">仅 VITE_MOCK_ENABLED=true（mock profile）可见，生产构建路由不存在。</p>

    <div class="sn-row">
      <label>设备 SN：</label>
      <input v-model="sn" placeholder="手动输入或选择" list="sn-list" class="sn-input" />
      <datalist id="sn-list">
        <option v-for="d in devices" :key="d.sn" :value="d.sn" />
      </datalist>
    </div>

    <div class="actions">
      <button v-for="action in ['online', 'offline', 'osd', 'flight-task', 'hms']"
              :key="action"
              :disabled="loading"
              @click="trigger(action)">
        {{ action }}
      </button>
    </div>

    <div v-if="lastResult" class="result">{{ lastResult }}</div>

    <h3>设备快照（3s 轮询）</h3>
    <pre class="json-view">{{ JSON.stringify(devices, null, 2) }}</pre>
  </div>
</template>

<style scoped>
.mock-debug { padding: 24px; }
.mock-debug h2 { font-size: 18px; margin-bottom: 6px; }
.hint { color: var(--app-text-muted); font-size: 12px; margin-bottom: 20px; }
.sn-row { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.sn-input {
  background: var(--app-bg-card);
  border: 1px solid var(--app-border);
  border-radius: 4px;
  color: var(--app-text);
  padding: 6px 10px;
  width: 260px;
}
.actions { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 16px; }
.actions button {
  padding: 6px 14px;
  background: var(--app-primary);
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}
.actions button:hover:not(:disabled) { background: var(--app-primary-hover); }
.actions button:disabled { opacity: 0.5; cursor: not-allowed; }
.result { color: var(--app-text-secondary); font-size: 12px; margin-bottom: 16px; }
.mock-debug h3 { font-size: 14px; margin-bottom: 8px; }
.json-view {
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  border-radius: 6px;
  padding: 12px;
  font-size: 12px;
  overflow: auto;
  max-height: 400px;
  color: var(--app-text-secondary);
}
</style>
