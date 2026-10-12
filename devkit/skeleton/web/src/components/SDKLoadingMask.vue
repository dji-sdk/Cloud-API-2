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
import type { SDKLoadStatus } from '@/composables/useSDKLoader'

/**
 * SDK 加载遮罩组件。
 * 提供三种状态的 UI 展示：loading（旋转动画）、error（错误信息）、ready（隐藏自身）。
 * 多个 SDK View 共用此组件，消除重复 loading/spinner/error 模板。
 */
interface SDKLoadingMaskProps {
  /** 当前加载状态 */
  status: SDKLoadStatus
  /** 错误信息（status='error' 时展示） */
  errorMsg?: string
  /** loading 时的提示文案 */
  loadingText?: string
}

withDefaults(defineProps<SDKLoadingMaskProps>(), {
  errorMsg: '',
  loadingText: '正在加载…',
})
</script>

<template>
  <!-- 加载遮罩 -->
  <div v-if="status === 'loading'" class="sdk-loading-mask">
    <div class="sdk-loading-spinner" />
    <p class="sdk-loading-text">{{ loadingText }}</p>
  </div>

  <!-- 错误面板 -->
  <div v-if="status === 'error'" class="sdk-error-panel">
    <p>{{ errorMsg }}</p>
  </div>
</template>

<style scoped>
.sdk-loading-mask {
  position: absolute;
  inset: 0;
  background: var(--app-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 99998;
  gap: 16px;
  color: var(--app-text-secondary);
}

.sdk-loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--app-border);
  border-top-color: var(--app-primary, #1a73e8);
  border-radius: 50%;
  animation: sdk-spin 0.8s linear infinite;
}

@keyframes sdk-spin { to { transform: rotate(360deg); } }

.sdk-loading-text {
  margin: 0;
  font-size: 14px;
}

.sdk-error-panel {
  padding: 40px;
  color: var(--app-danger, #ff4d4f);
  word-break: break-all;
}
</style>
