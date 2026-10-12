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
/**
 * 飞行记录视图 — FlyDataReplay SDK 集成
 *
 * 参考：paasjs-demo/fly-data-sdk/demo.html
 *
 * 关键约束：
 * 1. Cesium 必须在 fly-data-sdk/index.js 之前加载（由 ensureFH2Init 保证）
 * 2. 调用顺序：new → on('error') → await loadMap() → mount(mountEl)
 * 3. loadMap 必须在 mount 之前
 * 4. DOM 骨架 ID/class 不可修改（SDK 内部 Vue teleport 依赖）
 * 5. 同一页面不能同时挂多个 FlyDataReplay 实例
 * 6. 切换 taskId 需销毁后重建
 * 7. map.mode='internal' — SDK 自建 Cesium 地图，不依赖 FH2 地图管理
 */
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFH2Config } from '@/composables/useFH2Config'
import { useSDKLoader } from '@/composables/useSDKLoader'
import { ensureFH2Init } from '@/composables/useFH2'
import { useFH2ThemeBus } from '@/composables/useFH2ThemeBus'
import { injectLink, injectScriptUMD } from '@/utils/sdk'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'
import FlightRecordList from '@/components/flight-record/FlightRecordList.vue'
import { checkWaylineExists } from '@/api/wayline'
import type { FlyDataReplay, FH2SDKTheme } from '@/types/fh2'

const route  = useRoute()
const router = useRouter()

/** 是否处于回放模式（有 task_id 参数） */
const isReplayMode = computed(() => !!route.query.task_id)

const { status, errorMsg, markReady, markError } = useSDKLoader()
const { register: registerThemeHandler } = useFH2ThemeBus()

let player: FlyDataReplay | null = null

registerThemeHandler((theme: FH2SDKTheme) => {
  if (player?.setTheme) {
    player.setTheme(theme)
  }
})

onMounted(async () => {
  if (!isReplayMode.value) return

  try {
    const taskId = String(route.query.task_id ?? '')

    const cfg = useFH2Config()

    // ① 加载 paas.js（提供 window.Cesium，与其他视图共用）
    await ensureFH2Init()

    // ② 加载 FlyDataReplay SDK 资源
    const cssUrl = `${cfg.address}/paasjs-demo/fly-data-sdk/index.css`
    const jsUrl  = `${cfg.address}/paasjs-demo/fly-data-sdk/index.js`

    injectLink(cssUrl)
    await injectScriptUMD(jsUrl, 'FlyDataReplay')

    if (!window.FlyDataReplay || typeof window.FlyDataReplay !== 'function') {
      throw new Error('FlyDataReplay SDK 加载完毕，但 window.FlyDataReplay 未暴露。')
    }

    // ③ 鉴权配置
    const auth = {
      authKeys: ['X-User-Token', ...(cfg.sdkAuthKey ? [cfg.sdkAuthKey] : [])],
      headers: () => ({
        'X-User-Token': localStorage.getItem('x-user-token') ?? '',
        ...(cfg.sdkAuthKey ? { [cfg.sdkAuthKey]: cfg.sdkAuthValue } : {}),
      }),
    }

    // ④ 读取当前主题，用于初始化 SDK
    const cs = getComputedStyle(document.documentElement)
    const cssVar = (name: string) => cs.getPropertyValue(name).trim()
    const primaryColor = cssVar('--fh-brand-primary') || '#1890ff'
    const initTheme: Partial<FH2SDKTheme> = {
      primaryColor,
      primaryHoverColor: cssVar('--fh-brand-primary-hover') || '#40a9ff',
      primaryFocusColor: cssVar('--fh-brand-primary-active') || '#096dd9',
      bgBasic: cssVar('--app-bg') || '#232323',
      bgSub: cssVar('--app-bg-panel') || '#282828',
      bgModal: cssVar('--app-bg-card') || '#1c1c1c',
      borderColor: cssVar('--app-border') || '#4f4f4f',
      listActiveBg: cssVar('--app-bg-card') || 'rgba(24, 144, 255, 0.2)',
      listHoverBg: cssVar('--app-bg-card-hover') || 'rgba(64, 169, 255, 0.15)',
    }

    // ⑤ 预判航线是否已删除，避免 FlyDataReplay SDK 因航线缺失整页崩溃
    const waylineUuid = String(route.query.wayline_uuid ?? '')
    let waylineAvailable = true
    if (waylineUuid) {
      waylineAvailable = await checkWaylineExists(waylineUuid)
      if (!waylineAvailable) {
        console.warn('[FlyDataReplay] 航线文件已删除，降级为无航线回放模式：', waylineUuid)
      }
    }

    // ⑥ 构造 FlyDataReplay 实例（internal 地图模式，SDK 自建 Cesium 地图）
    player = new window.FlyDataReplay({
      taskId,
      hostUrl: cfg.address,
      api: {
        baseUrl: `${cfg.baseUrl}/gateway`,
        auth,
      },
      runtimeDepsBasePath: `${cfg.address}/paasjs-demo/sdk-runtime`,
      map: {
        mode: 'internal',
        container: '#map-app-global',
        env: {
          cesiumBaseUrl: (window as Record<string, unknown>).CESIUM_BASE_URL as string
            || `${cfg.address}/paasjs-demo/sdk-runtime/cesium/`,
          terrainKey: '',
          featureGateway: [],
        },
        // 推送中心 WebSocket 地址：透传给 map-sdk，doodle 插件据此订阅推送 topic 实时更新地图
        notifyWsUrl: cfg.wssUrl,
        // 设备拓扑连线插件：初始为空，运行时通过 player.showDeviceTopo([...]) 切换
        topo: { snList: [] },
      },
      locale: { language: 'zh' },
      features: {
        playerBarBtn: {
          params: true,
          media: false,
          media_list: false,
          sdr: true,
          fov: true,
          wayline: waylineAvailable,
        },
        playerTrack: {
          flight_status: true,
          device_warning: true,
          ai_target_warning: false,
          video_recording: true,
          height_chart: true,
        },
      },
      theme: initTheme,
    })

    // ⑦ 错误事件（在 loadMap/mount 之前注册）
    player.on('error', (e: unknown) => {
      const err = e as { code: number; message: string }
      console.error('[FlyDataReplay] error:', err.code, err.message)
    })

    player.on('back', () => {
      router.back()
    })

    player.on('wayline-preview', (payload: unknown) => {
      const p = payload as { waylineId: string }
      router.push(`/wayline?mode=edit&wayline_id=${p.waylineId}`)
    })

    player.on('device-selected', (payload: unknown) => {
      const p = payload as { deviceSn: string }
      console.log('[FlyDataReplay] 拓扑设备选中:', p.deviceSn)
    })

    // ⑧ loadMap 必须在 mount 之前
    await player.loadMap()

    // ⑨ 创建子 div 并挂载（与 demo.html 一致）
    const container = document.getElementById('fly-data-app-container')
    if (!container) {
      throw new Error('DOM 容器 #fly-data-app-container 不存在')
    }
    const mountEl = document.createElement('div')
    container.appendChild(mountEl)
    player.mount(mountEl)

    markReady()
  } catch (e: unknown) {
    markError(e)
  }
})

onUnmounted(() => {
  if (player) {
    player.destroy()
    player = null
  }
})
</script>

<template>
  <div class="flight-replay-page">
    <!-- 列表模式：无 task_id 参数 -->
    <FlightRecordList v-if="!isReplayMode" />

    <!-- 回放模式：有 task_id 参数 -->
    <template v-else>
      <SDKLoadingMask
        :status="status"
        :error-msg="errorMsg"
        loading-text="正在加载飞行记录…"
      />

    <!--
      FH2 飞行记录 DOM 骨架（ID/class 不可修改！SDK 内部 teleport 依赖）
      参考：fly-data-replay.md 二
    -->
    <div class="fh2-container">
      <!-- 顶栏：teleport 目标 -->
      <div id="wayline-header"></div>

      <div class="project-details">
        <!-- 左侧面板：teleport 目标 -->
        <div id="project-middle-container"></div>

        <!-- 右侧地图 + 回放覆盖层 -->
        <div id="project-right-micro-app" class="right-micro-app">
          <div class="maps-micro-app">
            <div id="project-map-app-placeholder" class="map-app-placeholder">
              <div id="map-app-global" class="map-app-container"></div>
            </div>
          </div>
          <!-- FlyDataReplay mount 目标 -->
          <div id="fly-data-app-container"></div>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<style scoped>
.flight-replay-page {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* ── FH2 飞行记录布局（不可修改 class 名！） ── */

.fh2-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  padding-top: 48px;  /* 给 #wayline-header 顶栏留位 */
}

.fh2-container :deep(.map-app-container > div) {
  width: 100%;
  height: 100%;
}

.fh2-container :deep(.project-details) {
  position: relative;
  width: 100%;
  display: flex;
  flex: 1;
  overflow: hidden;
}

.fh2-container :deep(.project-details .right-micro-app) {
  position: relative;
  display: flex;
  flex: 1;
}

.fh2-container :deep(.project-details .right-micro-app .maps-micro-app) {
  display: flex;
  flex: 1;
  height: 100%;
}

.fh2-container :deep(.project-details .right-micro-app .map-app-placeholder) {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.fh2-container :deep(.project-details .right-micro-app .map-app-container) {
  height: 100%;
  min-height: 0;  /* flex 子项必须显式设置 */
}

/* fly-data SDK 容器：绝对定位覆盖在地图上 */
.fh2-container :deep(.project-details .right-micro-app #fly-data-app-container) {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* TeleportShim 传送到 .right-micro-app 的回放面板 */
.fh2-container :deep(.project-details .right-micro-app > .fly-data-root) {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: transparent;
}
</style>
