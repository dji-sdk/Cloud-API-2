// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import axios from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/** Axios 实例：withCredentials 保证与 WebSocket 共用同一 HttpSession */
const client = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  timeout: 30000,
})

let initInFlight: Promise<void> | null = null

/** POST /session/init — 后端初始化 FHSDK 并标记会话就绪 */
export async function ensureSessionInitialized(): Promise<void> {
  if (!initInFlight) {
    initInFlight = client.post<ApiResponse<void>>('/session/init').then(() => undefined).finally(() => {
      initInFlight = null
    })
  }
  return initInFlight
}

client.interceptors.response.use(
  (res) => res,
  async (error) => {
    const config = error.config as (typeof error.config & { __sessionRetried?: boolean }) | undefined
    if (
      axios.isAxiosError(error)
      && error.response?.status === 409
      && config
      && !config.__sessionRetried
      && config.url !== '/session/init'
    ) {
      const msg = (error.response.data as ApiResponse<unknown> | undefined)?.message ?? ''
      if (msg.includes('session not initialized')) {
        config.__sessionRetried = true
        await ensureSessionInitialized()
        return client.request(config)
      }
    }
    return Promise.reject(error)
  },
)

export function formatApiError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const msg = e.response?.data?.message
    if (typeof msg === 'string' && msg) return msg
    if (e.response?.status) return `HTTP ${e.response.status}`
  }
  if (e instanceof Error) return e.message
  return String(e)
}

export default client
