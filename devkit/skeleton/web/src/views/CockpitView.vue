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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ensureFH2Init } from '@/composables/useFH2'
import { useSDKLoader } from '@/composables/useSDKLoader'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'
import DeviceSelector from '@/components/DeviceSelector.vue'
import type { DeviceWindowInstance } from '@/types/fh2'

const route = useRoute()
const router = useRouter()
const { status, errorMsg, markReady, markError } = useSDKLoader()

/** 响应式路由参数：同路由导航（/cockpit?drone_sn=AAA → BBB）也会触发更新 */
const gatewaySn = computed(() => String(route.query.gateway_sn ?? ''))
const droneSn   = computed(() => String(route.query.drone_sn ?? ''))

/** 是否已选择设备（至少有 gateway_sn） */
const hasDeviceSelected = computed(() => Boolean(gatewaySn.value))

/** 超时计时器 ID，组件卸载时需清理 */
let fallbackTimer: ReturnType<typeof setTimeout> | null = null

function handleDeviceSelect(gateway: string, drone: string): void {
  void router.push({
    path: '/cockpit',
    query: { gateway_sn: gateway, drone_sn: drone },
  })
}

function handleBackToSelector(): void {
  clearFallbackTimer()
  destroyCockpit()
  void router.push({ path: '/cockpit' })
}

function destroyCockpit(): void {
  try {
    window.FH2?.unsubscribe('cockpit:mounted')
    window.FH2?.unsubscribe('cockpit:drone-take-off')
    window.FH2?.destroyCockpit()
  } catch {
    // 忽略卸载阶段异常
  }
}

async function launchCockpit(): Promise<void> {
  if (!hasDeviceSelected.value) {
    return
  }

  status.value = 'loading'
  errorMsg.value = ''

  try {
    await ensureFH2Init()

    window.FH2.loadCockpit('cockpit-app-container', {
      gateway_sn: gatewaySn.value,
      drone_sn:   droneSn.value,
    })

    window.FH2.subscribe('cockpit:mounted', () => {
      clearFallbackTimer()
      markReady()
    })

    window.FH2.subscribe('cockpit:drone-take-off', (...args: unknown[]) => {
      console.error('[Cockpit] drone-take-off', ...args)
    })

    fallbackTimer = setTimeout(() => {
      if (status.value === 'loading') { markReady() }
    }, 10000)
  } catch (e: unknown) {
    clearFallbackTimer()
    markError(e)
  }
}

function clearFallbackTimer(): void {
  if (fallbackTimer !== null) {
    clearTimeout(fallbackTimer)
    fallbackTimer = null
  }
}

/* ── 设备小窗弹窗 ── */
const showDeviceWindow = ref(false)
const dwGatewaySn = ref('')
const dwDroneSn = ref('')
const dwInstances = ref<DeviceWindowInstance[]>([])
const dwLoading = ref(false)
const dwError = ref('')

async function handleDeviceWindow(gatewaySn: string, droneSn: string): Promise<void> {
  dwGatewaySn.value = gatewaySn
  dwDroneSn.value = droneSn
  showDeviceWindow.value = true
  dwLoading.value = true
  dwError.value = ''

  try {
    await ensureFH2Init()

    const devices = droneSn ? `${gatewaySn},${droneSn}` : gatewaySn
    const windows = await window.FH2.loadDeviceWindow('real-time-app-container', {
      devices,
      map: true,
    })

    dwInstances.value = windows
  } catch (e: unknown) {
    dwError.value = e instanceof Error ? e.message : String(e)
  } finally {
    dwLoading.value = false
  }
}

function closeDeviceWindow(): void {
  try {
    window.FH2?.destroyDeviceWindow()
  } catch {
    // 忽略卸载异常
  }
  dwInstances.value = []
  showDeviceWindow.value = false
  dwLoading.value = false
  dwError.value = ''
}

function onEscKey(e: KeyboardEvent): void {
  if (e.key === 'Escape' && showDeviceWindow.value) {
    closeDeviceWindow()
  }
}

onMounted(() => {
  if (hasDeviceSelected.value) {
    void launchCockpit()
  }
  window.addEventListener('keydown', onEscKey)
})

watch([gatewaySn, droneSn], ([newGw, newDrone], [oldGw, oldDrone]) => {
  if (!newGw) {
    clearFallbackTimer()
    destroyCockpit()
    return
  }
  if (newGw === oldGw && newDrone === oldDrone) {
    return
  }
  destroyCockpit()
  clearFallbackTimer()
  void launchCockpit()
})

onUnmounted(() => {
  clearFallbackTimer()
  destroyCockpit()
  if (showDeviceWindow.value) {
    closeDeviceWindow()
  }
  window.removeEventListener('keydown', onEscKey)
})
</script>

<template>
  <!-- 模式 1：未选择设备 → 显示设备选择面板 -->
  <div v-if="!hasDeviceSelected" class="cockpit-selector-page">
    <!--
      SDK 容器布局与 DeviceWindowView 完全对齐：
      - flex:1 + position:relative（不用 absolute + z-index，避免创建隔离层叠上下文）
      - 覆盖层放在容器内部（与 DeviceWindowView 的 .dw-guide 同层级）
    -->
    <div class="dw-sdk-container">
      <div id="map-app-global" class="dw-map-layer" />
      <div id="project-app-container" style="display: none;" />
      <div id="real-time-app-container" class="dw-realtime-layer" />

      <!-- 设备选择器覆盖层：未打开小窗时遮住 SDK 容器 -->
      <div v-if="!showDeviceWindow" class="dw-selector-cover">
        <DeviceSelector
          @select="handleDeviceSelect"
          @device-window="handleDeviceWindow"
        />
      </div>

      <!-- 设备小窗打开时：标题栏 + 状态层（SDK 容器在底层渲染） -->
      <div v-if="showDeviceWindow" class="dw-overlay-header">
        <span class="dw-overlay-title">设备小窗</span>
        <button class="dw-overlay-close" title="关闭" @click="closeDeviceWindow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </div>

      <div v-if="dwLoading" class="dw-overlay-state">
        <span class="dw-loading-dot" />
        加载中…
      </div>

      <div v-if="!dwLoading && dwError" class="dw-overlay-state dw-overlay-state--error">
        {{ dwError }}
      </div>
    </div>
  </div>

  <!-- 模式 2：已选择设备 → 加载驾驶舱 -->
  <div v-else class="cockpit-page">
    <SDKLoadingMask
      :status="status"
      :error-msg="errorMsg"
      loading-text="正在加载驾驶舱…"
    />

    <!-- 返回按钮（浮在驾驶舱左上角） -->
    <button class="btn-back" title="返回设备列表" @click="handleBackToSelector">
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="15 18 9 12 15 6" />
      </svg>
      切换设备
    </button>

    <!--
      FH2 SDK 官方 DOM 结构：必须预先存在于页面中，SDK 内部通过 getElementById 查找。
      不要修改 id 名称，顺序也需与参考项目保持一致。
    -->
    <div v-show="status !== 'error'" class="fh2-container">
      <div id="cockpit-header-container" />
      <div class="project-details router-cockpit">
        <div id="project-app-container" />
        <div id="project-middle-container" />
        <div id="project-right-micro-app" class="right-micro-app">
          <div class="maps-micro-app">
            <div class="cockpit-left-border-container" />
            <div id="project-map-app-placeholder" class="map-app-placeholder">
              <div class="cockpit-dock-live-container" />
              <div id="map-app-global" class="map-app-container" />
              <div class="cockpit-bottom-border-container" />
            </div>
          </div>
          <div class="project-right">
            <div id="cockpit-app-container" />
          </div>
        </div>
      </div>
    </div>

    <slot name="cockpit-overlay" />
    <div class="cockpit-footer-bar">
      <slot name="cockpit-footer" />
    </div>
  </div>
</template>

<style scoped>
/* ── 设备选择页 ── */
.cockpit-selector-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--app-bg);
}

/* ── 驾驶舱页 ── */
.cockpit-page {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* ── 返回按钮 ── */
.btn-back {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px 6px 8px;
  font-size: 12px;
  font-weight: 500;
  color: var(--app-text-secondary);
  background: var(--app-bg-card);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  backdrop-filter: blur(8px);
}

.btn-back:hover {
  color: var(--app-text);
  background: var(--app-bg-card-hover);
  border-color: var(--app-border-strong);
}

/* ── FH2 容器 ── */
.fh2-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.fh2-container .project-details {
  position: relative;
  width: 100%;
  display: flex;
  flex: 1;
  overflow: hidden;
}

.fh2-container .right-micro-app {
  position: relative;
  display: flex;
  flex: 1;
}

.fh2-container .maps-micro-app {
  display: flex;
  flex: 1;
  height: 100%;
}

.fh2-container .map-app-placeholder {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.fh2-container .map-app-container {
  height: 100%;
  min-height: 0;
}

.fh2-container .project-right {
  display: flex;
  flex-direction: column;
  height: 100%;
}

#cockpit-app-container {
  height: 100%;
}

/* qiankun 微应用容器高度修复 */
.fh2-container :deep(#__qiankun_microapp_wrapper_for_cockpit_app__),
.fh2-container :deep(#__qiankun_microapp_wrapper_for_cockpit_app__ > div) {
  height: 100% !important;
}

.fh2-container :deep(#project-app-container > div),
.fh2-container :deep(.map-app-container > div) {
  width: 100%;
  height: 100%;
}

.cockpit-footer-bar { flex-shrink: 0; min-height: 0; }

/* ── 设备小窗 SDK 容器（与 DeviceWindowView 对齐：flex:1 + position:relative） ── */
.dw-sdk-container {
  flex: 1;
  position: relative;
  min-height: 0;
  overflow: hidden;
}

.dw-map-layer {
  position: absolute;
  inset: 0;
  z-index: 1;
}

.dw-map-layer :deep(> div) {
  width: 100%;
  height: 100%;
}

.dw-realtime-layer {
  position: absolute;
  inset: 0;
  z-index: 10;
  pointer-events: none;
}

.dw-realtime-layer :deep(#__qiankun_microapp_wrapper_for_real_time_app__),
.dw-realtime-layer :deep(#__qiankun_microapp_wrapper_for_real_time_app__ > div) {
  height: 100% !important;
  pointer-events: none;
}

.dw-realtime-layer :deep(#__qiankun_microapp_wrapper_for_real_time_app__ .real-time-wrapper) {
  pointer-events: auto;
}

/* 设备选择器覆盖层：遮住底层 SDK 容器 */
.dw-selector-cover {
  position: absolute;
  inset: 0;
  z-index: 50;
  background: var(--app-bg);
}

/* 设备小窗标题栏 */
.dw-overlay-header {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  z-index: 60;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: var(--app-bg-card, #1a1a2e);
  border-bottom: 1px solid var(--app-border);
}

.dw-overlay-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text);
}

.dw-overlay-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--app-text-muted);
  cursor: pointer;
  transition: all var(--transition-base);
}

.dw-overlay-close:hover {
  color: var(--app-text);
  background: var(--app-bg-card-hover, rgba(255, 255, 255, 0.08));
}

/* 状态遮罩层 */
.dw-overlay-state {
  position: absolute;
  inset: 0;
  z-index: 55;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  text-align: center;
  color: var(--app-text-muted);
  font-size: 13px;
  background: var(--app-bg, #0d0d1a);
}

.dw-overlay-state--error {
  color: var(--app-danger, #f56c6c);
}

.dw-loading-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-primary);
  margin-right: 8px;
  animation: dw-pulse 1s ease-in-out infinite;
}

@keyframes dw-pulse {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}
</style>
