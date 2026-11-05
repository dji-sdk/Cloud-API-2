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

import { API_BASE } from './baseUrl'

/** AI 助手可用性探测结果。 */
export interface ChatStatus {
  /** 后端是否启用 AI 助手（对应配置项 fh.chat.enabled） */
  enabled: boolean
}

/** 探测超时时间（毫秒）。超时按「不可用」处理，避免入口永远悬在「探测中」。 */
const PROBE_TIMEOUT_MS = 5000

/**
 * 查询后端 AI 助手开关状态。
 *
 * <p><b>刻意不使用 {@link authFetch}：</b>该函数在收到 401 时会清空登录态并跳转
 * `/welcome`。而本探测可能发生在登录之前，且携带的是可能已过期的旧 token——
 * 一旦被 401 拦下，用户会在打开首页时被无端踢到登录页。
 * 因此这里用原生 `fetch` 且不附加 `Authorization` 头，后端该接口标注为
 * `@PublicEndpoint`，匿名可访问。
 *
 * @returns 后端返回的开关状态
 * @throws Error 网络异常、非 2xx 响应或响应体结构不符时抛出，由调用方降级为「不可用」
 */
export async function fetchChatStatus(): Promise<ChatStatus> {
  // 手写超时而非 AbortSignal.timeout()：后者在部分测试环境（jsdom）中缺失，
  // 且需要兼容较老的浏览器内核。
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), PROBE_TIMEOUT_MS)

  try {
    const res = await fetch(`${API_BASE}/api/v1/chat/status`, {
      method: 'GET',
      headers: { Accept: 'application/json' },
      signal: controller.signal,
    })

    if (!res.ok) {
      throw new Error(`fetchChatStatus failed: ${res.status}`)
    }

    // 断言原因：res.json() 返回 unknown，无法直接校验字段；先窄化为可空的部分结构，
    // 下方的 typeof 检查才是真正的运行时校验，断言不会掩盖任何错误。
    const body = (await res.json()) as Partial<ChatStatus> | null
    if (!body || typeof body.enabled !== 'boolean') {
      throw new Error('fetchChatStatus failed: 响应体缺少 enabled 字段')
    }

    return { enabled: body.enabled }
  } finally {
    clearTimeout(timer)
  }
}
