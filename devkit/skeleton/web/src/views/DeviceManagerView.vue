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
import { ref, onMounted } from 'vue'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import { useFH2Config, toWebSocketUrl } from '@/composables/useFH2Config'
import { useSDKLoader } from '@/composables/useSDKLoader'
import { injectLink, injectScriptUMD } from '@/utils/sdk'
import { formatUnknownError } from '@/utils/paasMessages'
import { usePaasAvailability, describePaasUnavailability } from '@/composables/usePaasAvailability'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'
import DevicesView from '@/views/DevicesView.vue'

const { status, errorMsg, markReady } = useSDKLoader()
// 与 AppTopNav 共用同一份探测结果，横幅文案才能说清「缺什么 + 当前配置 + 怎么补」
const paas = usePaasAvailability()
const useFallback = ref(false)
/** 回退到后端设备列表的原因；为空表示主动按配置关闭 SDK，无需告警 */
const fallbackReason = ref('')

function isDeviceSdkEnabled(): boolean {
  return getRuntimeConfig('FH2_DEVICE_SDK_ENABLED', 'true') !== 'false'
}

onMounted(async () => {
  if (!isDeviceSdkEnabled()) {
    useFallback.value = true
    markReady()
    return
  }

  try {
    const cfg = useFH2Config()

    // ① 预设 SDK 运行时环境变量（SDK 在加载阶段会读取这些值）
    const win = window as Record<string, unknown>
    win.CURRENT_BE_ENV_CONFIG = {
      server_url: cfg.baseUrl,
      // ws 地址从 baseUrl 推导：协议随部署走 https→wss（避免 HTTPS 页面被 mixed content 拦截），
      // 同源反代时保留 /fh2-gw 前缀，与 HTTP 请求走同一入口。
      ws_url_web: toWebSocketUrl(cfg.baseUrl, '/duplex/web'),
      zone_id: '',
      zone_name: '',
      map_engine: 't',
    }

    // ② 加载 SDK 资源（UMD 格式，需要 injectScriptUMD 确保挂载到 window）
    const cssUrl = `${cfg.address}/paasjs-demo/device-manager-sdk/index.css`
    const jsUrl  = `${cfg.address}/paasjs-demo/device-manager-sdk/index.js`

    injectLink(cssUrl)
    await injectScriptUMD(jsUrl, 'UranusDeviceManagerApp')

    // ③ 校验 SDK 是否正确挂载（兼容新旧两版 API）
    const sdk = window.UranusDeviceManagerApp
    const hasNewApi = sdk && typeof sdk.setupDeviceManager === 'function'
    const hasLegacyApi = sdk && typeof sdk.init === 'function'
    if (!hasNewApi && !hasLegacyApi) {
      throw new Error(
        'SDK 加载完毕，但 window.UranusDeviceManagerApp 未暴露 init 或 setupDeviceManager。' +
        '可能原因：UMD 导出被其他模块系统拦截，或 SDK 版本不兼容。',
      )
    }

    // ④ 推送中心 WebSocket 地址：直接复用配置解析结果（协议随 https 切 wss、支持 FH2_WSS_URL 覆盖）
    const wsUrl = cfg.wssUrl

    // ⑤ 鉴权配置（闭包形式，每次请求动态读取 token）
    const auth = {
      authKeys: ['X-User-Token', ...(cfg.sdkAuthKey ? [cfg.sdkAuthKey] : [])],
      headers: () => ({
        'X-User-Token': localStorage.getItem('x-user-token') ?? '',
        ...(cfg.sdkAuthKey ? { [cfg.sdkAuthKey]: cfg.sdkAuthValue } : {}),
      }),
    }

    // ⑥ 初始化设备管理（自动检测 API 版本：新版两步 setupDeviceManager→mountDeviceList / 旧版一步 init）
    const antThemeBaseUrl = `${cfg.address}/paasjs-demo/device-manager-sdk/ant-theme`
    const errorCallback = { onError: (err: unknown) => { console.error('[DeviceManager] onError:', err) } }

    if (hasNewApi) {
      await sdk.setupDeviceManager!({
        serverUrl: cfg.baseUrl,
        wsUrl,
        auth,
        locale: 'zh',
        theme: 'light',
        antThemeBaseUrl,
        callbacks: errorCallback,
      })
      sdk.mountDeviceList!({
        container: '#device-manager-container',
        props: { deviceType: 'airport' },
      })
    } else {
      await sdk.init!({
        containerId: '#device-manager-container',
        serverUrl: cfg.baseUrl,
        wsUrl,
        auth,
        locale: 'zh',
        theme: 'light',
        antThemeBaseUrl,
        callbacks: errorCallback,
      })
    }

    markReady()
  } catch (e: unknown) {
    // 回退到后端设备列表仍然可用，属降级而非失败：记录原因后继续渲染，
    // 不调用 markError（否则遮罩会盖住本可用的回退页面）。
    console.warn('[DeviceManagerView] 底座设备管理 SDK 加载失败，回退到后端设备列表:', e)
    // 优先用底座探测的结论：它知道是「未配置地址」还是「地址不通」，能给出可操作指引。
    // 探测尚未完成（reason 为 null）或底座其实可达时返回空串，此时退回异常本身的信息。
    fallbackReason.value = describePaasUnavailability(paas.availability.value) || formatUnknownError(e)
    useFallback.value = true
    markReady()
  }
})
</script>

<template>
  <div class="device-manager-page">
    <!-- FH2 SDK 模式 -->
    <template v-if="!useFallback">
      <SDKLoadingMask
        :status="status"
        :error-msg="errorMsg"
        loading-text="正在加载设备管理…"
      />
      <div id="device-manager-container" class="sdk-container" />
    </template>

    <!-- 回退模式：FH2 SDK 不可用时展示后端设备列表 -->
    <template v-else>
      <!--
        降级提示：回退页面本身可用，但用户看到的信息比 SDK 模式少，
        必须说明「为什么降级」，否则会被当成正常状态。
      -->
      <div v-if="fallbackReason" class="fallback-banner" role="status">
        <span class="fallback-banner__title">已降级为后端设备列表</span>
        <span class="fallback-banner__reason">{{ fallbackReason }}</span>
      </div>
      <DevicesView />
    </template>
  </div>
</template>

<style scoped>
.device-manager-page {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.fallback-banner {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 16px;
  background: var(--app-warning-bg, #fffbe6);
  border-bottom: 1px solid var(--app-warning-border, #ffe58f);
  font-size: 12px;
  color: var(--app-warning, #d48806);
  flex-shrink: 0;
}

.fallback-banner__title {
  font-weight: 600;
}

/* 错误文案含换行与长 URL，必须保留换行并允许断词 */
.fallback-banner__reason {
  white-space: pre-line;
  word-break: break-all;
  line-height: 1.6;
}

/* SDK 容器：白色底色供 SDK 浅色主题渲染，不设 overflow hidden（SDK 弹窗需要溢出） */
.sdk-container {
  flex: 1;
  width: 100%;
  min-height: 0;
  background: #fff;
}
</style>
