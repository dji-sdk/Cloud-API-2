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
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import type { GridLayoutKey, FillMode, PlayerSlot, PlayerState, LogEntry } from '@/types/liveStream'
import { LAYOUT_CONFIGS } from '@/components/live-stream/layoutConfig'
import { getContainerId } from '@/components/live-stream/utils'
import { useLivePlayer } from '@/composables/useLivePlayer'
import { useSDKLoader } from '@/composables/useSDKLoader'
import LiveSettingsPanel from '@/components/live-stream/LiveSettingsPanel.vue'
import LiveGridLayout from '@/components/live-stream/LiveGridLayout.vue'
import LiveDevicePanel from '@/components/live-stream/LiveDevicePanel.vue'
import LiveLogPanel from '@/components/live-stream/LiveLogPanel.vue'

const { status, errorMsg, markReady, markError } = useSDKLoader()
const { initialized, initSDK, createPlayer, destroyPlayer, destroyAll, setAllFillMode, setAllShowOsd } = useLivePlayer()

const layout = ref<GridLayoutKey>('9')
const fillMode = ref<FillMode>('contain')
const showOsd = ref(true)
const slots = ref<PlayerSlot[]>(createEmptySlots(LAYOUT_CONFIGS['9'].max))
const logs = ref<LogEntry[]>([])

let logIdCounter = 0
let isMounted = true

function createEmptySlots(count: number): PlayerSlot[] {
  return Array.from({ length: count }, (): PlayerSlot => ({
    id: null,
    sn: null,
    state: 'idle',
    resolved: null,
    error: null,
  }))
}

function addLog(sn: string | null, event: string, data?: unknown, isError = false): void {
  const timestamp = new Date().toTimeString().split(' ')[0]
  logs.value.push({
    id: ++logIdCounter,
    timestamp,
    sn,
    event,
    data,
    isError,
  })
  if (logs.value.length > 500) {
    logs.value.splice(0, logs.value.length - 500)
  }
}

function generateSlotId(sn: string): string {
  return `${sn}-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`
}

const VALID_PLAYER_STATES: ReadonlySet<string> = new Set(['idle', 'connecting', 'playing', 'error', 'closed'])

function toPlayerState(raw: unknown): PlayerState {
  const str = String(raw)
  return VALID_PLAYER_STATES.has(str) ? (str as PlayerState) : 'error'
}

function handleAddDevice(sn: string): void {
  if (slots.value.some(s => s.sn === sn)) {
    addLog(sn, 'skip', '设备已在播放列表中')
    return
  }

  const emptyIndex = slots.value.findIndex(s => s.id === null)
  if (emptyIndex === -1) {
    addLog(sn, 'skip', '当前布局已满，无空闲槽位')
    return
  }

  const slotId = generateSlotId(sn)
  slots.value[emptyIndex] = {
    id: slotId,
    sn,
    state: 'connecting',
    resolved: null,
    error: null,
  }

  addLog(sn, 'create', '播放器已创建')

  if (initialized.value) {
    nextTick(() => {
      if (!isMounted) return
      const containerId = getContainerId(slotId)
      const player = createPlayer(containerId, sn, fillMode.value, showOsd.value)
      if (player) {
        player.on('ready', () => {
          updateSlotState(slotId, 'playing')
          addLog(sn, 'ready')
        })
        player.on('stateChange', (state: unknown) => {
          const validState = toPlayerState(state)
          updateSlotState(slotId, validState)
          addLog(sn, 'stateChange', state)
        })
        player.on('resolved', (data: unknown) => {
          const idx = slots.value.findIndex(s => s.id === slotId)
          if (idx !== -1) {
            slots.value[idx] = { ...slots.value[idx], resolved: data, error: null }
          }
          addLog(sn, 'resolved', data)
        })
        player.on('error', (err: unknown) => {
          const idx = slots.value.findIndex(s => s.id === slotId)
          if (idx !== -1) {
            slots.value[idx] = { ...slots.value[idx], error: err, state: 'error' }
          }
          addLog(sn, 'error', err, true)
        })
        player.on('close', () => {
          addLog(sn, 'close')
          removeSlot(slotId)
        })
      }
    })
  }
}

function updateSlotState(slotId: string, state: PlayerSlot['state']): void {
  const idx = slots.value.findIndex(s => s.id === slotId)
  if (idx !== -1) {
    slots.value[idx] = { ...slots.value[idx], state }
  }
}

function removeSlot(slotId: string): void {
  const idx = slots.value.findIndex(s => s.id === slotId)
  if (idx !== -1) {
    destroyPlayer(getContainerId(slotId))
    slots.value[idx] = { id: null, sn: null, state: 'idle', resolved: null, error: null }
  }
}

function handleCloseSlot(index: number): void {
  const slot = slots.value[index]
  if (slot.id) {
    addLog(slot.sn, 'close', '用户关闭')
    removeSlot(slot.id)
  }
}

async function handleInitSDK(): Promise<void> {
  try {
    await initSDK()
    markReady()
    addLog(null, 'init', 'SDK 初始化成功')
  } catch (e: unknown) {
    markError(e)
    addLog(null, 'init-error', e instanceof Error ? e.message : String(e), true)
  }
}

watch(layout, (newLayout) => {
  const newMax = LAYOUT_CONFIGS[newLayout].max
  const current = slots.value

  if (newMax < current.length) {
    for (let i = newMax; i < current.length; i++) {
      if (current[i].id) {
        destroyPlayer(getContainerId(current[i].id!))
      }
    }
    slots.value = current.slice(0, newMax)
  } else {
    const padding = createEmptySlots(newMax - current.length)
    slots.value = [...current, ...padding]
  }
})

watch(fillMode, (mode) => {
  setAllFillMode(mode)
})

watch(showOsd, (show) => {
  setAllShowOsd(show)
})

onBeforeUnmount(() => {
  isMounted = false
  destroyAll()
})

handleInitSDK()
</script>

<template>
  <div class="live-stream-view">
    <aside class="live-stream-view__sidebar">
      <div class="sidebar-section">
        <div class="sidebar-header">
          <span class="sidebar-header__title">直播大屏</span>
          <span
            class="sidebar-header__status"
            :class="{
              'sidebar-header__status--ready': status === 'ready',
              'sidebar-header__status--error': status === 'error',
            }"
          >
            {{ status === 'ready' ? '已就绪' : status === 'loading' ? '初始化中...' : '初始化失败' }}
          </span>
        </div>
        <div v-if="status === 'error'" class="sidebar-error">
          {{ errorMsg }}
          <button class="sidebar-error__retry" @click="handleInitSDK">重试</button>
        </div>
      </div>

      <LiveSettingsPanel
        v-model="layout"
        :fill-mode="fillMode"
        :show-osd="showOsd"
        @update:fill-mode="fillMode = $event"
        @update:show-osd="showOsd = $event"
      />

      <div class="sidebar-divider" />

      <LiveDevicePanel @add-device="handleAddDevice" />
    </aside>

    <main class="live-stream-view__main">
      <LiveGridLayout
        :layout="layout"
        :slots="slots"
        @close="handleCloseSlot"
      />
    </main>

    <LiveLogPanel :logs="logs" />
  </div>
</template>

<style scoped>
.live-stream-view {
  display: flex;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background: var(--app-bg);
}

.live-stream-view__sidebar {
  width: 280px;
  flex-shrink: 0;
  background: var(--app-bg-panel);
  border-right: 1px solid var(--app-border);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.live-stream-view__main {
  flex: 1;
  min-width: 0;
  padding: 4px;
}

.sidebar-section {
  padding: 12px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-header__title {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
}

.sidebar-header__status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  background: var(--app-warning-bg);
  color: var(--app-warning);
}

.sidebar-header__status--ready {
  background: var(--app-success-bg);
  color: var(--app-success);
}

.sidebar-header__status--error {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}

.sidebar-error {
  margin-top: 8px;
  padding: 8px;
  background: var(--app-danger-bg);
  border-radius: var(--radius-sm);
  font-size: 11px;
  color: var(--app-danger);
  display: flex;
  align-items: center;
  gap: 8px;
}

.sidebar-error__retry {
  margin-left: auto;
  border: 1px solid var(--app-danger);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-danger);
  font-size: 11px;
  padding: 2px 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.sidebar-divider {
  height: 1px;
  background: var(--app-border);
  margin: 0 12px;
}
</style>
