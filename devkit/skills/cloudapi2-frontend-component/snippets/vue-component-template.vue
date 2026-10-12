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
 * [组件名]View — FH2 [组件中文名]集成模板
 *
 * 使用步骤：
 * 1. 复制本文件到 src/views/[组件名]View.vue
 * 2. 替换所有 [xxx] 占位符
 * 3. 在 router/index.ts 注册路由
 * 4. 确认 .env 中 FH2 配置项已填写
 */
import { onMounted, onUnmounted } from 'vue'
import { ensureFH2Init } from '@/composables/useFH2'
import { useSDKLoader } from '@/composables/useSDKLoader'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'

const { status, errorMsg, markReady, markError } = useSDKLoader()

/** 超时兜底：SDK mounted 事件可能不触发时使用 */
let fallbackTimer: ReturnType<typeof setTimeout> | null = null

async function launch() {
  try {
    await ensureFH2Init()

    // TODO: 替换为实际的 FH2 加载调用
    // window.FH2.loadXxx('xxx-app-container', { ...options })

    // TODO: 订阅 SDK 事件
    // window.FH2.subscribe('xxx:mounted', () => {
    //   clearFallback()
    //   markReady()
    // })

    // 兜底超时
    fallbackTimer = setTimeout(() => {
      if (status.value === 'loading') { markReady() }
    }, 10000)
  } catch (e: unknown) {
    clearFallback()
    markError(e)
  }
}

function clearFallback(): void {
  if (fallbackTimer !== null) {
    clearTimeout(fallbackTimer)
    fallbackTimer = null
  }
}

onMounted(launch)

onUnmounted(() => {
  clearFallback()
  try {
    // TODO: 取消所有事件订阅
    // window.FH2?.unsubscribe('xxx:mounted')
    // TODO: 销毁组件实例
    // window.FH2?.destroyXxx()
  } catch {
    // 忽略卸载阶段异常
  }
})
</script>

<template>
  <div class="xxx-page">
    <SDKLoadingMask
      :status="status"
      :error-msg="errorMsg"
      loading-text="正在加载…"
    />

    <div v-show="status !== 'error'" class="fh2-container">
      <!--
        FH2 SDK 官方 DOM 结构
        不要修改 id 名称，SDK 内部通过 getElementById 查找
      -->
      <div id="xxx-app-container" />
    </div>
  </div>
</template>

<style scoped>
.xxx-page {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.fh2-container {
  width: 100%;
  height: 100%;
}

#xxx-app-container {
  width: 100%;
  height: 100%;
}
</style>
