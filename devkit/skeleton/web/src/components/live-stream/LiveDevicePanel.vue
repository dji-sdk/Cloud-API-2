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
import type { LiveDeviceInfo } from '@/types/liveStream'
import { fetchOnlineDevices } from '@/api/liveStream'

const emit = defineEmits<{
  addDevice: [sn: string]
}>()

/** 在线设备列表轮询间隔，与 DeviceSelector.vue / DevicesView.vue 保持一致 */
const POLL_INTERVAL = 10000

const devices = ref<LiveDeviceInfo[]>([])
/** 仅首次加载展示 loading，轮询时静默刷新，避免列表每 10s 闪一次 */
const loading = ref(true)
const manualSn = ref('')
let pollTimer: ReturnType<typeof setInterval> | null = null

async function loadDevices(): Promise<void> {
  try {
    devices.value = await fetchOnlineDevices()
  } catch {
    devices.value = []
  } finally {
    loading.value = false
  }
}

function handleManualAdd(): void {
  const sn = manualSn.value.trim()
  if (sn) {
    emit('addDevice', sn)
    manualSn.value = ''
  }
}

function handleDeviceClick(sn: string): void {
  emit('addDevice', sn)
}

onMounted(() => {
  void loadDevices()
  pollTimer = setInterval(() => void loadDevices(), POLL_INTERVAL)
})

onUnmounted(() => {
  if (pollTimer !== null) {
    clearInterval(pollTimer)
  }
})
</script>

<template>
  <div class="device-panel">
    <div class="device-panel__section">
      <div class="device-panel__title">添加播放器</div>
      <div class="device-panel__input-row">
        <input
          v-model="manualSn"
          type="text"
          class="device-panel__input"
          placeholder="输入设备 SN"
          @keydown.enter.prevent="handleManualAdd"
        />
        <button class="device-panel__add-btn" @click="handleManualAdd">添加</button>
      </div>
    </div>

    <div class="device-panel__section">
      <div class="device-panel__title">
        在线设备
        <button class="device-panel__refresh" title="刷新" @click="loadDevices">⟳</button>
      </div>

      <div v-if="loading" class="device-panel__loading">加载中...</div>
      <div v-else-if="devices.length === 0" class="device-panel__empty">暂无在线设备</div>
      <div v-else class="device-panel__list">
        <div
          v-for="device in devices"
          :key="device.dock.sn"
          class="device-card"
        >
          <div class="device-card__name">{{ device.name }}</div>
          <div class="device-card__row">
            <span class="device-card__tag">机场</span>
            <button class="device-card__btn" @click="handleDeviceClick(device.dock.sn)">
              {{ device.dock.model }} · {{ device.dock.sn }}
            </button>
          </div>
          <div v-if="device.drone" class="device-card__row">
            <span class="device-card__tag device-card__tag--drone">飞机</span>
            <button class="device-card__btn" @click="handleDeviceClick(device.drone!.sn)">
              {{ device.drone.model }} · {{ device.drone.sn }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.device-panel {
  padding: 12px;
}

.device-panel__section {
  margin-bottom: 16px;
}

.device-panel__title {
  font-size: 11px;
  font-weight: 600;
  color: var(--app-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.device-panel__refresh {
  border: none;
  background: transparent;
  color: var(--app-text-muted);
  font-size: 14px;
  cursor: pointer;
  padding: 2px;
  border-radius: var(--radius-sm);
  transition: color var(--transition-base);
}

.device-panel__refresh:hover {
  color: var(--app-primary);
}

.device-panel__input-row {
  display: flex;
  gap: 4px;
}

.device-panel__input {
  flex: 1;
  height: 30px;
  padding: 0 8px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 12px;
  outline: none;
  transition: border-color var(--transition-base);
}

.device-panel__input:focus {
  border-color: var(--app-primary);
}

.device-panel__input::placeholder {
  color: var(--app-text-muted);
}

.device-panel__add-btn {
  height: 30px;
  padding: 0 12px;
  border: 1px solid var(--app-primary);
  border-radius: var(--radius-sm);
  background: var(--app-primary-bg);
  color: var(--app-primary);
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-base);
}

.device-panel__add-btn:hover {
  background: var(--app-primary-bg-hover);
}

.device-panel__loading,
.device-panel__empty {
  font-size: 12px;
  color: var(--app-text-muted);
  padding: 12px 0;
  text-align: center;
}

.device-panel__list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-card {
  padding: 8px;
  background: var(--app-bg-surface);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
}

.device-card__name {
  font-size: 12px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 6px;
}

.device-card__row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
}

.device-card__tag {
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
  background: var(--app-info-bg);
  color: var(--app-info);
  flex-shrink: 0;
}

.device-card__tag--drone {
  background: var(--app-success-bg);
  color: var(--app-success);
}

.device-card__btn {
  border: none;
  background: transparent;
  color: var(--app-text-secondary);
  font-size: 11px;
  font-family: var(--font-mono);
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 2px;
  transition: all var(--transition-base);
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-card__btn:hover {
  background: var(--app-primary-bg);
  color: var(--app-primary);
}
</style>
