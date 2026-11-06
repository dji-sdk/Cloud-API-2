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
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import { formatPaasUnavailableMessage, type PaasUnavailableReason } from '@/utils/paasMessages'

/** 探针超时时间（毫秒）。 */
const PROBE_TIMEOUT_MS = 5000

/** 探测结果。 */
export interface PaasAvailability {
  /** 底座是否可用 */
  available: boolean
  /** 不可用原因；可用时为 null */
  reason: PaasUnavailableReason | null
  /** 探测使用的底座静态资源地址；未配置时为空串 */
  address: string
}

/** 模块级状态，多个组件共享同一次探测结果。 */
const state = ref<PaasAvailability>({
  available: false,
  reason: null,
  address: '',
})

/** 是否已完成一次探测。 */
const probed = ref<boolean>(false)

/** 模块级单例 Promise，保证一次会话只探测一次。 */
let probePromise: Promise<PaasAvailability> | null = null

/**
 * 读取底座静态资源地址。
 *
 * <p>与 {@code useFH2Config()} 的差别：后者在未配置时直接抛错，而入口置灰恰恰需要
 * 在「未配置」时也能拿到一个结果，因此这里只读原始配置项。
 *
 * @returns 底座静态资源地址，未配置时为空串
 */
function readPaasAddress(): string {
  return getRuntimeConfig('FH2_ASSET_HOST') || getRuntimeConfig('FH2_ADDRESS')
}

/**
 * 探测底座主机是否可达。
 *
 * <p>使用 {@code mode: 'no-cors'}：FH2 底座不返回 CORS 头，用默认 cors 模式请求会被
 * 浏览器拦截并抛 TypeError，从而把「可达但跨域」误判成「不可达」。no-cors 模式下
 * 只要 TCP/TLS 握手成功、任意状态码返回，Promise 就会 resolve（响应体为 opaque），
 * 足以回答「底座是否在线」这一个问题。
 *
 * <p>副作用：no-cors 读不到状态码，因此 404 也会被判为可达。这是有意的取舍——
 * 该探针只负责把「整台底座挂掉」这类硬故障挡在入口之前，细粒度的接口错误仍由
 * 各页面自己的中文错误提示兜底。
 *
 * @param address 底座静态资源地址
 * @returns 主机是否可达
 */
async function probeHostReachable(address: string): Promise<boolean> {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), PROBE_TIMEOUT_MS)

  try {
    await fetch(`${address}/paas.js`, {
      method: 'GET',
      mode: 'no-cors',
      cache: 'no-store',
      signal: controller.signal,
    })
    return true
  } catch {
    return false
  } finally {
    clearTimeout(timer)
  }
}

/** 执行一次探测并写入模块级状态。 */
async function runProbe(): Promise<PaasAvailability> {
  const address = readPaasAddress()

  if (!address) {
    state.value = { available: false, reason: 'not-configured', address: '' }
    probed.value = true
    return state.value
  }

  const reachable = await probeHostReachable(address)
  state.value = {
    available: reachable,
    reason: reachable ? null : 'unreachable',
    address,
  }
  probed.value = true
  return state.value
}

/**
 * 底座（FH2 上云 API）可用性探测 composable。
 *
 * <p>首次调用触发探测，后续调用复用同一 Promise。依赖底座的页面入口据此置灰，
 * 避免用户点进一个注定报错的页面。
 *
 * <p>文案不在这里生成，统一走 {@code utils/paasMessages}，保证与页面内报错口径一致。
 *
 * @returns 不可变的可用性状态与手动 `refresh` 方法
 */
export function usePaasAvailability(): {
  availability: DeepReadonly<Ref<PaasAvailability>>
  probed: DeepReadonly<Ref<boolean>>
  refresh: () => Promise<PaasAvailability>
} {
  if (!probePromise) {
    probePromise = runProbe()
  }

  return {
    availability: readonly(state),
    probed: readonly(probed),
    refresh: async (): Promise<PaasAvailability> => {
      probePromise = runProbe()
      return probePromise
    },
  }
}

/**
 * 生成当前底座不可用的完整中文提示。
 *
 * @param availability 探测结果
 * @returns 多行中文提示；底座可用时为空串
 */
export function describePaasUnavailability(availability: PaasAvailability): string {
  if (availability.available || !availability.reason) {
    return ''
  }
  return formatPaasUnavailableMessage(availability.reason, availability.address)
}

/**
 * 重置探测状态（供测试使用）。
 *
 * <p>模块级缓存不随用例自动清空，不重置会让用例相互污染。
 */
export function resetPaasAvailability(): void {
  probePromise = null
  probed.value = false
  state.value = { available: false, reason: null, address: '' }
}
