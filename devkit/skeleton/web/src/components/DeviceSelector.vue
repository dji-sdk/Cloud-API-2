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
import { fetchDevices } from '@/api/devices'
import StatusBadge from '@/components/StatusBadge.vue'
import DeviceMap from '@/components/dashboard/DeviceMap.vue'
import type { DeviceState } from '@/types/api'
import type { DeviceMarker } from '@/types/dashboard'

interface DockGroup {
  dock: DeviceState
  drones: DeviceState[]
}

const emit = defineEmits<{
  select: [gatewaySn: string, droneSn: string]
  'device-window': [gatewaySn: string, droneSn: string]
}>()

const devices = ref<DeviceState[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
let pollTimer: ReturnType<typeof setInterval> | null = null

const POLL_INTERVAL = 10000

const onlineDockGroups = computed<DockGroup[]>(() => {
  const onlineDocks = devices.value.filter(d => d.deviceType === 'DOCK' && d.online)
  return onlineDocks.map(dock => ({
    dock,
    drones: (dock.childDevices ?? []).filter(c => c.deviceType === 'DRONE'),
  }))
})

const hasOnlineDevices = computed(() => onlineDockGroups.value.length > 0)

/** 将在线设备转为地图标注点 */
const deviceMarkers = computed<DeviceMarker[]>(() => {
  return devices.value
    .filter(d => d.online)
    .map(d => ({
      sn: d.sn,
      type: d.deviceType === 'DOCK' ? 'dock' as const : 'drone' as const,
      name: d.name ?? d.sn,
      status: d.online ? 'online' : 'offline',
      lat: typeof d.lastOsd?.attributes['lat'] === 'number'
        ? (d.lastOsd.attributes['lat'] as number) : 0,
      lng: typeof d.lastOsd?.attributes['lng'] === 'number'
        ? (d.lastOsd.attributes['lng'] as number) : 0,
    }))
})

async function loadDevices(): Promise<void> {
  try {
    devices.value = await fetchDevices()
    error.value = null
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

function handleCardClick(group: DockGroup): void {
  const droneSn = group.drones.length > 0 ? group.drones[0].sn : ''
  emit('select', group.dock.sn, droneSn)
}

function handleSelect(dock: DeviceState, drone: DeviceState): void {
  emit('select', dock.sn, drone.sn)
}

function handleDeviceWindow(dock: DeviceState, drone: DeviceState): void {
  emit('device-window', dock.sn, drone.sn)
}

function getBattery(d: DeviceState): string {
  const v = d.lastOsd?.attributes['battery']
  return typeof v === 'number' ? `${v}%` : '—'
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
  <div class="device-selector">
    <!-- 左侧：设备列表面板 -->
    <div class="selector-panel">
      <div class="selector-header">
        <h2 class="selector-title">选择设备</h2>
        <p class="selector-subtitle">选择在线机场进入驾驶舱</p>
      </div>

      <div v-if="loading" class="selector-state">
        <span class="loading-dot" />
        加载设备列表…
      </div>

      <div v-else-if="error" class="selector-state selector-state--error">
        {{ error }}
      </div>

      <div v-else-if="!hasOnlineDevices" class="selector-state">
        暂无在线机场设备
      </div>

      <div v-else class="dock-list">
        <div
          v-for="group in onlineDockGroups"
          :key="group.dock.sn"
          class="dock-card"
          @click="handleCardClick(group)"
        >
          <div class="dock-info">
            <div class="dock-header">
              <span class="dock-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <rect x="2" y="6" width="20" height="12" rx="2" />
                  <line x1="6" y1="10" x2="6" y2="14" />
                  <line x1="18" y1="10" x2="18" y2="14" />
                  <line x1="10" y1="3" x2="14" y2="3" />
                  <line x1="12" y1="3" x2="12" y2="6" />
                </svg>
              </span>
              <span class="dock-name">{{ group.dock.name ?? '机场' }}</span>
              <StatusBadge :online="group.dock.online" />
            </div>
            <div class="dock-meta">
              <span class="meta-label">SN</span>
              <span class="meta-value mono">{{ group.dock.sn }}</span>
            </div>
            <div v-if="group.dock.model" class="dock-meta">
              <span class="meta-label">型号</span>
              <span class="meta-value">{{ group.dock.model }}</span>
            </div>
          </div>

          <div class="dock-divider" />

          <div v-if="group.drones.length > 0" class="drone-section">
            <div
              v-for="drone in group.drones"
              :key="drone.sn"
              class="drone-row"
            >
              <div class="drone-info">
                <span class="drone-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M12 12m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0" />
                    <path d="M3 9l3 3l-3 3" />
                    <path d="M21 9l-3 3l3 3" />
                    <path d="M9 3l3 3l3 -3" />
                    <path d="M9 21l3 -3l3 3" />
                  </svg>
                </span>
                <div class="drone-detail">
                  <span class="drone-name">{{ drone.name ?? drone.model ?? drone.sn }}</span>
                  <span class="drone-sn mono">{{ drone.sn }}</span>
                </div>
                <div class="drone-status">
                  <StatusBadge :online="drone.online" />
                  <span class="battery">{{ getBattery(drone) }}</span>
                </div>
              </div>
              <div class="drone-actions">
                <button
                  class="btn-action btn-action--secondary"
                  @click.stop="handleDeviceWindow(group.dock, drone)"
                >
                  设备小窗
                </button>
                <button
                  class="btn-action btn-action--primary"
                  @click.stop="handleSelect(group.dock, drone)"
                >
                  驾驶舱
                </button>
              </div>
            </div>
          </div>

          <div v-else class="no-drone">
            飞行器未连接
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：卫星地图 -->
    <div class="map-panel">
      <DeviceMap :devices="deviceMarkers" />
    </div>
  </div>
</template>

<style scoped>
.device-selector {
  height: 100%;
  display: flex;
  flex-direction: row;
  overflow: hidden;
}

.selector-panel {
  width: 340px;
  min-width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-right: 1px solid var(--app-border);
  background: var(--app-bg);
}

.map-panel {
  flex: 1;
  min-width: 0;
  position: relative;
}

.selector-header {
  padding: var(--space-xl) var(--space-xl) var(--space-lg);
  flex-shrink: 0;
}

.selector-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0 0 4px;
}

.selector-subtitle {
  font-size: 13px;
  color: var(--app-text-muted);
  margin: 0;
}

.selector-state {
  padding: 40px var(--space-xl);
  text-align: center;
  color: var(--app-text-muted);
  font-size: 14px;
}

.selector-state--error {
  color: var(--app-danger);
}

.loading-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-primary);
  margin-right: 8px;
  animation: pulse 1s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

.dock-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 var(--space-xl) var(--space-xl);
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.dock-card {
  flex-shrink: 0;
  background: var(--app-bg-card);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  transition: border-color var(--transition-base);
  cursor: pointer;
}

.dock-card:hover {
  border-color: var(--app-border-strong);
}

.dock-info {
  padding: var(--space-md) var(--space-lg);
}

.dock-header {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.dock-icon {
  color: var(--app-text-secondary);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.dock-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dock-meta {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 12px;
  margin-top: 4px;
  padding-left: 26px;
}

.meta-label {
  color: var(--app-text-muted);
  min-width: 28px;
}

.meta-value {
  color: var(--app-text-secondary);
}

.mono {
  font-family: var(--font-mono);
  font-size: 11px;
}

.dock-divider {
  height: 1px;
  background: var(--app-border);
  margin: 0 var(--space-lg);
}

.drone-section {
  padding: var(--space-md) var(--space-lg);
}

.drone-row {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.drone-info {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.drone-icon {
  color: var(--app-primary);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.drone-detail {
  flex: 1;
  min-width: 0;
}

.drone-name {
  display: block;
  font-size: 13px;
  color: var(--app-text);
  font-weight: 500;
}

.drone-sn {
  display: block;
  font-size: 11px;
  color: var(--app-text-muted);
  margin-top: 1px;
}

.drone-status {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-shrink: 0;
}

.battery {
  font-size: 12px;
  color: var(--app-text-secondary);
  font-family: var(--font-mono);
}

.drone-actions {
  display: flex;
  gap: var(--space-sm);
}

.btn-action {
  flex: 1;
  padding: 8px 0;
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-action--primary {
  border: 1px solid var(--app-primary);
  background: var(--app-primary-bg);
  color: var(--app-primary);
}

.btn-action--primary:hover {
  background: var(--app-primary);
  color: var(--app-text-inverse);
}

.btn-action--secondary {
  border: 1px solid var(--app-border);
  background: transparent;
  color: var(--app-text-secondary);
}

.btn-action--secondary:hover {
  border-color: var(--app-border-strong);
  color: var(--app-text);
  background: var(--app-bg-card-hover);
}

.no-drone {
  padding: var(--space-md) var(--space-lg);
  font-size: 12px;
  color: var(--app-text-muted);
  text-align: center;
}
</style>
