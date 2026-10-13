/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { ref } from 'vue'

/** SDK 加载状态类型 */
export type SDKLoadStatus = 'loading' | 'ready' | 'error'

/**
 * SDK 加载状态管理 composable。
 * 提取三个 SDK View（DeviceManager、Wayline、FlightReplay）的公共加载状态逻辑，
 * 避免在每个 View 中重复 loading/ready/error 状态定义和错误处理。
 *
 * @example
 * ```ts
 * const { status, errorMsg, markReady, markError } = useSDKLoader()
 *
 * onMounted(async () => {
 *   try {
 *     // ... 加载 SDK
 *     markReady()
 *   } catch (e: unknown) {
 *     markError(e)
 *   }
 * })
 * ```
 */
export function useSDKLoader() {
  const status   = ref<SDKLoadStatus>('loading')
  const errorMsg = ref('')

  /** 标记加载完成 */
  function markReady(): void {
    status.value = 'ready'
  }

  /** 标记加载失败，自动提取 Error.message */
  function markError(e: unknown): void {
    status.value = 'error'
    errorMsg.value = e instanceof Error ? e.message : String(e)
  }

  return { status, errorMsg, markReady, markError }
}
