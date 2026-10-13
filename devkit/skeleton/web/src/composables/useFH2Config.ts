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

import { getRuntimeConfig } from '@/utils/runtimeConfig'

/**
 * FH2 SDK 配置项接口。
 * 优先从运行时注入的 window.__FH_CONFIG__ 读取，fallback 到 Vite 环境变量。
 */
export interface FH2ConfigResult {
  /** 前端服务地址（不含端口），例 "http://192.168.1.100" */
  address: string
  /** API 端口，默认 30812 */
  port: string
  /** 完整后端 baseUrl = address:port */
  baseUrl: string
  /** WebSocket 通知推送完整地址（含路径），SDK 直接使用不自动拼接 */
  wssUrl: string
  /** 项目 ID */
  prjId: string
  /** SDK 附加鉴权 header key（如 "user_id"），为空则不附加 */
  sdkAuthKey: string
  /** SDK 附加鉴权 header value（如 "your-auth-value"，占位示例需按平台实际配置） */
  sdkAuthValue: string
}

/**
 * 读取 FH2 SDK 服务器地址与凭据。
 * 优先从运行时配置读取（容器环境变量注入），fallback 到 import.meta.env（本地开发）。
 *
 * @throws Error 当必填配置未提供时抛出明确错误
 */
export function useFH2Config(): FH2ConfigResult {
  const address = getRuntimeConfig('FH2_ADDRESS')
  const port    = getRuntimeConfig('FH2_PORT', '30812')

  if (!address) {
    throw new Error(
      '[useFH2Config] FH2_ADDRESS 未配置。' +
      '本地开发请复制 .env.example 为 .env 并填写；' +
      '容器部署请设置 VITE_FH2_ADDRESS 环境变量。'
    )
  }

  const baseUrl = `${address}:${port}`

  const explicitWss = getRuntimeConfig('FH2_WSS_URL')
  const ip = new URL(address).hostname
  const wssUrl = explicitWss || `ws://${ip}:${port}/openapi/v0.9/notification/ws/v1/notifications`

  return {
    address,
    port,
    baseUrl,
    wssUrl,
    prjId:        getRuntimeConfig('FH2_PRJ_ID'),
    sdkAuthKey:   getRuntimeConfig('FH2_SDK_AUTH_KEY'),
    sdkAuthValue: getRuntimeConfig('FH2_SDK_AUTH_VALUE'),
  }
}
