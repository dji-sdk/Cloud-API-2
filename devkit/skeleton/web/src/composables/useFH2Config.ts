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

/** FH2 通知推送 WebSocket 路径（相对于 FH2 网关根） */
const NOTIFICATION_WS_PATH = '/openapi/v0.9/notification/ws/v1/notifications'

/**
 * 把 HTTP(S) 基地址转换为同源的 WebSocket 地址。
 *
 * <p>协议随基地址而定：https → wss，http → ws。页面以 HTTPS 部署时（同上反代 / k8s ingress）
 * 若仍用 ws:// 会被浏览器按 mixed content 直接拦截，FH2 通知与设备管理长连接随之全部失效。
 *
 * <p>基地址可带路径前缀（同源反代，如 https://host/fh2-gw），转换后前缀保留，
 * 与 HTTP 请求走同一入口，反向代理侧的 Upgrade 透传与鉴权注入同时生效。
 *
 * @param baseUrl HTTP(S) 基地址，形如 http://10.0.0.1:30812 或 https://host/fh2-gw
 * @param path    追加到基地址之后的路径（以 / 开头）
 * @return WebSocket 地址
 */
export function toWebSocketUrl(baseUrl: string, path: string): string {
  const url = new URL(baseUrl)
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
  const prefix = url.pathname.replace(/\/+$/, '')
  const suffix = path.startsWith('/') ? path : `/${path}`
  url.pathname = `${prefix}${suffix}`
  url.search = ''
  url.hash = ''
  return url.toString()
}

/**
 * FH2 SDK 配置项接口。
 * 优先从运行时注入的 window.__FH_CONFIG__ 读取，fallback 到 Vite 环境变量。
 */
export interface FH2ConfigResult {
  /**
   * SDK 静态资源主机（assetHost），用于加载 paas.js / live-player sdk / wayline sdk /
   * MineDecoder.wasm 等。默认取 FH2_ADDRESS；可用 FH2_ASSET_HOST 覆盖为同源反代前缀，
   * 例 "http://192.0.2.10:30080/fh2-assets"（骨架 nginx 反代到 FH2:443），规避跨域。
   */
  address: string
  /** API 端口，默认 30812 */
  port: string
  /**
   * 后端 API baseUrl（serverUrl）。默认 = FH2_ADDRESS:FH2_PORT；
   * 可用 FH2_BASE_URL 覆盖为同源反代前缀，例 "http://192.0.2.10:30080/fh2-gw"
   * （骨架 nginx 反代到 FH2:30812），规避 XHR 跨域。
   */
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
  const rawAddress = getRuntimeConfig('FH2_ADDRESS')
  const port       = getRuntimeConfig('FH2_PORT', '30812')

  if (!rawAddress) {
    // 容器部署下 config.js 由 docker-entrypoint.sh 在启动时按环境变量生成，浏览器可能缓存
    // 了旧的空配置。文案需同时覆盖「确实没配」与「配了但页面是旧的」两种
    // 情形，并提示用 Ctrl/Cmd+Shift+R 强制刷新排除缓存干扰。
    throw new Error(
      '[useFH2Config] FH2_ADDRESS 未配置。' +
      '本地开发请复制 .env.example 为 .env 并填写；' +
      '容器部署请在 docker compose 的 environment（或 deploy.env）中设置 VITE_FH2_ADDRESS，' +
      '改完需重启容器（docker compose up -d --force-recreate）。' +
      '若确认已配置，请先在浏览器按 Ctrl/Cmd+Shift+R 强制刷新，排除旧 config.js 缓存。'
    )
  }

  // assetHost 与 baseUrl 默认直连 FH2，可分别用 FH2_ASSET_HOST / FH2_BASE_URL 覆盖为
  // 同源反代前缀（骨架 nginx 反代），使浏览器只与自身源通信，规避 FH2 无 CORS 头的跨域拦截。
  const address = getRuntimeConfig('FH2_ASSET_HOST') || rawAddress
  const baseUrl = getRuntimeConfig('FH2_BASE_URL') || `${rawAddress}:${port}`

  // wss 默认从 baseUrl 推导（协议随 https 切 wss、保留同源反代前缀），
  // 可用 FH2_WSS_URL 覆盖为任意地址。
  const explicitWss = getRuntimeConfig('FH2_WSS_URL')
  const wssUrl = explicitWss || toWebSocketUrl(baseUrl, NOTIFICATION_WS_PATH)

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
