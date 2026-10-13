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
import { ensureFH2Init } from '@/composables/useFH2'
import { useSDKLoader } from '@/composables/useSDKLoader'
import { fetchDevices } from '@/api/devices'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'
import type { DeviceWindowInstance } from '@/types/fh2'
import type { DeviceState } from '@/types/api'

const { status, errorMsg, markReady, markError } = useSDKLoader()

const snInput = ref('')
const deviceWindows = ref<DeviceWindowInstance[]>([])
const isLoaded = ref(false)
const onlineDevices = ref<DeviceState[]>([])
const loadingDeviceList = ref(true)

let fallbackTimer: ReturnType<typeof setTimeout> | null = null

async function loadOnlineDevices(): Promise<void> {
  try {
    const allDevices = await fetchDevices()
    onlineDevices.value = allDevices.filter(d => d.online)
  } catch {
    // 静默处理，不影响主流程
  } finally {
    loadingDeviceList.value = false
  }
}

function handleQuickSelect(sn: string): void {
  if (snInput.value) {
    const existing = snInput.value.split(',').map(s => s.trim())
    if (!existing.includes(sn)) {
      snInput.value = `${snInput.value},${sn}`
    }
  } else {
    snInput.value = sn
  }
}

async function handleLoadDevices(): Promise<void> {
  const raw = snInput.value.trim()
  if (!raw) return

  if (isLoaded.value) {
    window.FH2.updateDeviceWindow({ devices: raw })
    return
  }

  status.value = 'loading'
  errorMsg.value = ''

  try {
    await ensureFH2Init()

    const windows = await window.FH2.loadDeviceWindow('real-time-app-container', {
      devices: raw,
      map: true,
    })

    deviceWindows.value = windows
    isLoaded.value = true
    clearFallbackTimer()
    markReady()
  } catch (e: unknown) {
    clearFallbackTimer()
    markError(e)
  }
}

function handleDestroy(): void {
  try {
    window.FH2?.destroyDeviceWindow()
  } catch {
    // 忽略卸载阶段异常
  }
  deviceWindows.value = []
  isLoaded.value = false
  status.value = 'ready'
}

function clearFallbackTimer(): void {
  if (fallbackTimer !== null) {
    clearTimeout(fallbackTimer)
    fallbackTimer = null
  }
}

onMounted(() => {
  void loadOnlineDevices()
  status.value = 'ready'
})

onUnmounted(() => {
  clearFallbackTimer()
  try {
    window.FH2?.destroyDeviceWindow()
  } catch {
    // 忽略
  }
})
</script>

<template>
  <div class="device-window-page">
    <!-- 顶部工具栏 -->
    <header class="dw-toolbar">
      <div class="toolbar-left">
        <h2 class="toolbar-title">设备小窗</h2>
        <span class="toolbar-desc">实时查看多设备画面与状态</span>
      </div>
      <div class="toolbar-right">
        <input
          v-model="snInput"
          class="sn-input"
          type="text"
          placeholder="输入设备 SN，多个用逗号分隔"
          @keyup.enter="handleLoadDevices"
        />
        <button class="btn btn-primary" @click="handleLoadDevices">
          {{ isLoaded ? '更新设备' : '加载小窗' }}
        </button>
        <button v-if="isLoaded" class="btn btn-secondary" @click="handleDestroy">
          销毁小窗
        </button>
      </div>
    </header>

    <!-- SDK 容器：始终挂载确保 SDK 渲染时有正确尺寸 -->
    <div class="dw-sdk-container">
      <SDKLoadingMask
        v-if="status === 'loading'"
        :status="status"
        :error-msg="errorMsg"
        loading-text="正在加载设备小窗…"
      />

      <!-- 未加载时显示设备选择引导（覆盖在 SDK 容器之上） -->
      <div v-if="!isLoaded && status !== 'loading'" class="dw-guide">
        <div class="guide-content">
          <div class="guide-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.2">
              <rect x="2" y="3" width="20" height="14" rx="2" />
              <line x1="8" y1="21" x2="16" y2="21" />
              <line x1="12" y1="17" x2="12" y2="21" />
              <line x1="7" y1="7" x2="7" y2="13" />
              <line x1="12" y1="3" x2="12" y2="10" />
            </svg>
          </div>
          <h3 class="guide-title">请选择设备</h3>
          <p class="guide-desc">输入设备 SN 或从下方在线设备列表中选取，加载实时小窗画面</p>
        </div>

        <!-- 在线设备快速选取 -->
        <div v-if="!loadingDeviceList && onlineDevices.length > 0" class="device-chips">
          <span class="chips-label">在线设备：</span>
          <button
            v-for="device in onlineDevices"
            :key="device.sn"
            class="device-chip"
            :title="device.name ?? device.sn"
            @click="handleQuickSelect(device.sn)"
          >
            <span class="chip-name">{{ device.name ?? device.model ?? '未命名' }}</span>
            <span class="chip-sn">{{ device.sn }}</span>
          </button>
        </div>
        <div v-else-if="loadingDeviceList" class="device-chips">
          <span class="chips-label chips-loading">加载设备列表...</span>
        </div>
      </div>

      <div id="map-app-global" class="dw-map" />
      <div id="project-app-container" style="display: none;" />
      <div id="real-time-app-container" class="dw-realtime" />
    </div>
  </div>
</template>

<style scoped>
.device-window-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--app-bg);
}

/* ── 顶部工具栏 ── */
.dw-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--app-border);
  background: var(--app-bg-card);
  flex-shrink: 0;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-shrink: 0;
}

.toolbar-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.toolbar-desc {
  font-size: 12px;
  color: var(--app-text-muted);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.sn-input {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--app-border);
  background: var(--app-bg);
  color: var(--app-text);
  font-size: 13px;
  width: 320px;
  outline: none;
  transition: border-color var(--transition-base);
}

.sn-input:focus {
  border-color: var(--app-primary);
}

.sn-input::placeholder {
  color: var(--app-text-muted);
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid var(--app-border);
  transition: all var(--transition-base);
  white-space: nowrap;
}

.btn-primary {
  background: var(--app-primary);
  color: var(--app-text-inverse, #fff);
  border-color: var(--app-primary);
}

.btn-primary:hover {
  opacity: 0.9;
}

.btn-secondary {
  background: transparent;
  color: var(--app-text-secondary);
  border-color: var(--app-border);
}

.btn-secondary:hover {
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

/* ── 引导区域（覆盖在 SDK 容器之上） ── */
.dw-guide {
  position: absolute;
  inset: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 40px;
  background: var(--app-bg);
}

.guide-content {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.guide-icon {
  color: var(--app-text-muted);
  opacity: 0.5;
}

.guide-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.guide-desc {
  font-size: 13px;
  color: var(--app-text-muted);
  margin: 0;
  max-width: 400px;
}

/* ── 设备快选 ── */
.device-chips {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  max-width: 800px;
}

.chips-label {
  font-size: 12px;
  color: var(--app-text-muted);
  flex-shrink: 0;
}

.chips-loading {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.device-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--app-border);
  background: var(--app-bg-card);
  cursor: pointer;
  transition: all var(--transition-base);
  font-size: 12px;
}

.device-chip:hover {
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
}

.chip-name {
  color: var(--app-text);
  font-weight: 500;
}

.chip-sn {
  color: var(--app-text-muted);
  font-family: var(--font-mono);
  font-size: 11px;
}

/* ── SDK 容器 ── */
.dw-sdk-container {
  flex: 1;
  position: relative;
  min-height: 0;
  overflow: hidden;
}

.dw-map {
  position: absolute;
  inset: 0;
  z-index: 1;
}

.dw-realtime {
  position: absolute;
  inset: 0;
  z-index: 10;
  pointer-events: none;
}

/* qiankun 微应用容器修复 */
.dw-realtime :deep(#__qiankun_microapp_wrapper_for_real_time_app__),
.dw-realtime :deep(#__qiankun_microapp_wrapper_for_real_time_app__ > div) {
  height: 100% !important;
  pointer-events: none;
}

.dw-realtime :deep(#__qiankun_microapp_wrapper_for_real_time_app__ .real-time-wrapper) {
  pointer-events: auto;
}

.dw-map :deep(> div) {
  width: 100%;
  height: 100%;
}
</style>
