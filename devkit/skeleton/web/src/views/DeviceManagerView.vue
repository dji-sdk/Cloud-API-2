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
import { useFH2Config } from '@/composables/useFH2Config'
import { useSDKLoader } from '@/composables/useSDKLoader'
import { injectLink, injectScriptUMD } from '@/utils/sdk'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'
import DevicesView from '@/views/DevicesView.vue'

const { status, errorMsg, markReady, markError } = useSDKLoader()
const useFallback = ref(false)

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
    const ip  = new URL(cfg.address).hostname

    // ① 预设 SDK 运行时环境变量（SDK 在加载阶段会读取这些值）
    const win = window as Record<string, unknown>
    win.CURRENT_BE_ENV_CONFIG = {
      server_url: cfg.baseUrl,
      ws_url_web: `ws://${ip}:${cfg.port}/duplex/web`,
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

    // ④ 推送中心 WebSocket 地址
    const wsUrl = `ws://${ip}:${cfg.port}/openapi/v0.9/notification/ws/v1/notifications`

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
    console.warn('[DeviceManagerView] FH2 SDK 加载失败，回退到后端设备列表:', e)
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
    <DevicesView v-else />
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

/* SDK 容器：白色底色供 SDK 浅色主题渲染，不设 overflow hidden（SDK 弹窗需要溢出） */
.sdk-container {
  flex: 1;
  width: 100%;
  min-height: 0;
  background: #fff;
}
</style>
