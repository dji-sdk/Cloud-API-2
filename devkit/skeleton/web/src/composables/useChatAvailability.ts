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

import { ref, readonly, type Ref, type DeepReadonly } from 'vue'
import { fetchChatStatus } from '@/api/chat'

/** AI 助手是否可用（后端 fh.chat.enabled 为 true 且探测成功）。 */
const available = ref<boolean>(false)

/** 是否已完成一次探测（无论成功失败）。入口在探测完成前不渲染，避免闪现。 */
const probed = ref<boolean>(false)

/** 模块级单例 Promise：保证一次会话只探测一次，多个组件共享同一结果。 */
let probePromise: Promise<boolean> | null = null

/**
 * 执行一次探测，失败一律视为「不可用」。
 *
 * <p>失败原因包括：后端未启用该功能以外的任何异常（网络不通、网关 502、响应体非法、
 * 401）。对前端而言这些情况都无法提供聊天服务，统一降级为隐藏入口，
 * 不影响页面其余功能的可用性。
 *
 * @returns 探测结果
 */
async function runProbe(): Promise<boolean> {
  try {
    const status = await fetchChatStatus()
    available.value = status.enabled
  } catch {
    // 静默降级：探测失败不应在控制台之外打扰用户，入口隐藏即可
    available.value = false
  } finally {
    probed.value = true
  }
  return available.value
}

/**
 * AI 助手可用性探测 composable。
 *
 * <p>首次调用即触发探测，后续调用复用同一 Promise，因此可安全地在多个组件中
 * （顶部导航、App 根组件）分别调用，不会产生重复请求。
 *
 * <p>探测刻意不携带登录凭证，见 {@link fetchChatStatus} 的说明。
 *
 * @returns 不可变的 `available` / `probed` 状态与手动 `refresh` 方法
 */
export function useChatAvailability(): {
  available: DeepReadonly<Ref<boolean>>
  probed: DeepReadonly<Ref<boolean>>
  refresh: () => Promise<boolean>
} {
  if (!probePromise) {
    probePromise = runProbe()
  }

  return {
    available: readonly(available),
    probed: readonly(probed),
    refresh: async (): Promise<boolean> => {
      probePromise = runProbe()
      return probePromise
    },
  }
}

/**
 * 重置探测状态（供测试使用）。
 *
 * <p>模块级缓存无法随测试用例自动清空，不重置会让用例之间相互污染。
 */
export function resetChatAvailability(): void {
  probePromise = null
  available.value = false
  probed.value = false
}
