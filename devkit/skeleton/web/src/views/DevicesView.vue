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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDevices } from '@/api/devices'
import StatusBadge from '@/components/StatusBadge.vue'
import type { DeviceState } from '@/types/api'

const router = useRouter()

const devices = ref<DeviceState[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
let intervalId: ReturnType<typeof setInterval> | null = null

/** 构建 drone SN → 所属 dock SN 的映射表 */
const droneToGateway = computed<Map<string, string>>(() => {
  const map = new Map<string, string>()
  for (const d of devices.value) {
    if (d.deviceType === 'DOCK' && d.childDevices) {
      for (const child of d.childDevices) {
        map.set(child.sn, d.sn)
      }
    }
  }
  return map
})

async function loadDevices() {
  try {
    devices.value = await fetchDevices()
    error.value = null
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

function openCockpit(drone: DeviceState) {
  const gateway = droneToGateway.value.get(drone.sn) ?? ''
  void router.push({
    path: '/cockpit',
    query: { gateway_sn: gateway, drone_sn: drone.sn },
  })
}

onMounted(() => {
  loadDevices()
  intervalId = setInterval(loadDevices, 10000)
})

onUnmounted(() => {
  if (intervalId !== null) {
    clearInterval(intervalId)
  }
})

function getBattery(d: DeviceState): string {
  const v = d.lastOsd?.attributes['battery']
  return typeof v === 'number' ? `${v}%` : '—'
}

function getCoords(d: DeviceState): string {
  const lat = d.lastOsd?.attributes['latitude']
  const lng = d.lastOsd?.attributes['longitude']
  if (typeof lat === 'number' && typeof lng === 'number') {
    return `${lat.toFixed(5)}, ${lng.toFixed(5)}`
  }
  return '—'
}

/** 格式化 ISO 8601 时间戳为本地 YYYY-MM-DD HH:mm:ss */
function formatIso(iso: string | undefined | null): string {
  if (!iso) return '—'
  try {
    const d = new Date(iso)
    if (Number.isNaN(d.getTime())) return '—'
    const pad = (n: number): string => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch {
    return '—'
  }
}
</script>

<template>
  <div class="devices-view">
    <div class="devices-view__header">
      <h2>设备列表</h2>
      <slot name="device-list-actions" />
    </div>

    <div v-if="loading" class="state-message">加载中…</div>
    <div v-else-if="error" class="state-message state-message--error">{{ error }}</div>

    <table v-else class="device-table">
      <thead>
        <tr>
          <th>SN</th>
          <th>类型</th>
          <th>名称</th>
          <th>状态</th>
          <th>最近在线</th>
          <th>电量</th>
          <th>坐标</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="d in devices" :key="d.sn">
          <td class="sn">{{ d.sn }}</td>
          <td>{{ d.deviceType }}</td>
          <td>{{ d.name ?? '—' }}</td>
          <td><StatusBadge :online="d.online" /></td>
          <td class="time">{{ formatIso(d.onlineSince) }}</td>
          <td>{{ getBattery(d) }}</td>
          <td>{{ getCoords(d) }}</td>
          <td>
            <button
              v-if="d.deviceType === 'DRONE' && droneToGateway.get(d.sn)"
              class="btn-cockpit"
              @click="openCockpit(d)"
            >
              驾驶舱
            </button>
            <span v-else class="no-action">—</span>
            <slot name="device-row-extra" :device="d" />
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && !error && devices.length === 0" class="state-message">
      暂无设备数据。请确认 Spring Boot 已启动。
    </div>
  </div>
</template>

<style scoped>
.devices-view { padding: 24px; }
.devices-view__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.devices-view__header h2 { font-size: 20px; font-weight: 600; color: var(--app-text); }
.device-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--app-bg-card);
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--app-border);
}
.device-table th,
.device-table td {
  padding: 10px 14px;
  text-align: left;
  border-bottom: 1px solid var(--app-border);
  font-size: 13px;
}
.device-table th { color: var(--app-text-muted); font-weight: 500; }
.device-table td { color: var(--app-text); }
.sn { font-family: monospace; font-size: 12px; }
.time { font-family: monospace; font-size: 12px; white-space: nowrap; }
.state-message {
  padding: 32px;
  text-align: center;
  color: var(--app-text-muted);
}
.state-message--error { color: #ff4d4f; }
.btn-cockpit {
  padding: 3px 10px;
  font-size: 12px;
  border: 1px solid var(--app-primary, #1a73e8);
  background: transparent;
  color: var(--app-primary, #1a73e8);
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-cockpit:hover { background: rgba(26, 115, 232, 0.1); }
.no-action { color: var(--app-text-muted); font-size: 12px; }
</style>
