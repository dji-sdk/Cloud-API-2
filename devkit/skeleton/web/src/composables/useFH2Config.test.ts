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

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { useFH2Config, toWebSocketUrl } from './useFH2Config'

/** window.__FH_CONFIG__ 的最小类型，便于在测试中直接赋值 */
type FhConfig = Record<string, string | undefined>

/**
 * 会被开发者本地 .env 污染的可选配置项。
 * vitest 默认加载 .env，若不置空，本地填写的反代前缀会混入断言，导致用例在不同机器上结果不一。
 */
const OPTIONAL_ENV_KEYS = [
  'FH2_ASSET_HOST',
  'FH2_BASE_URL',
  'FH2_WSS_URL',
  'FH2_PRJ_ID',
  'FH2_SDK_AUTH_KEY',
  'FH2_SDK_AUTH_VALUE',
] as const

function setConfig(cfg: FhConfig): void {
  ;(window as unknown as { __FH_CONFIG__?: FhConfig }).__FH_CONFIG__ = cfg
}

describe('useFH2Config', () => {
  beforeEach(() => {
    setConfig({})
    for (const key of OPTIONAL_ENV_KEYS) {
      vi.stubEnv(`VITE_${key}`, '')
    }
  })

  afterEach(() => {
    vi.unstubAllEnvs()
    delete (window as unknown as { __FH_CONFIG__?: FhConfig }).__FH_CONFIG__
  })

  it('throw_shouldRejectError_whenAddressMissing', () => {
    // 本地 .env 常配置 VITE_FH2_ADDRESS，这里显式置空以模拟「完全未配置」的部署环境
    vi.stubEnv('VITE_FH2_ADDRESS', '')
    setConfig({})
    expect(() => useFH2Config()).toThrow(/FH2_ADDRESS/)
  })

  it('throw_shouldMentionRestartAndHardRefresh_whenAddressMissing', () => {
    // 回归场景：容器里 config.js 已有值但页面仍报错时，用户需先怀疑浏览器缓存。
    // 报错文案必须同时给出「改环境变量后重启容器」和「强制刷新」两条可执行动作。
    vi.stubEnv('VITE_FH2_ADDRESS', '')
    setConfig({})

    let message = ''
    try {
      useFH2Config()
    } catch (e) {
      message = (e as Error).message
    }

    expect(message).toContain('VITE_FH2_ADDRESS')
    expect(message).toContain('docker compose')
    expect(message).toContain('强制刷新')
  })

  it('default_shouldDeriveAssetAndBaseFromAddress_whenNoOverride', () => {
    setConfig({ FH2_ADDRESS: 'http://10.0.0.1', FH2_PORT: '30812' })

    const cfg = useFH2Config()

    // 向后兼容：无覆盖时 assetHost=FH2_ADDRESS，baseUrl=address:port
    expect(cfg.address).toBe('http://10.0.0.1')
    expect(cfg.baseUrl).toBe('http://10.0.0.1:30812')
  })

  it('wss_shouldBuildFromAddressHostAndPort_whenNoExplicitWss', () => {
    setConfig({ FH2_ADDRESS: 'http://10.0.0.1', FH2_PORT: '30812' })

    const cfg = useFH2Config()

    expect(cfg.wssUrl).toBe(
      'ws://10.0.0.1:30812/openapi/v0.9/notification/ws/v1/notifications',
    )
  })

  it('wss_shouldUseWssAndKeepSameOriginPrefix_whenAddressIsHttps', () => {
    // HTTPS 部署（同源反代 / ingress）下仍拼 ws:// 会被 mixed content 拦截，必须切 wss 且保留前缀
    setConfig({
      FH2_ADDRESS: 'https://10.0.0.1',
      FH2_PORT: '30812',
      FH2_BASE_URL: 'https://skeleton.example.com/fh2-gw',
    })

    const cfg = useFH2Config()

    expect(cfg.wssUrl).toBe(
      'wss://skeleton.example.com/fh2-gw/openapi/v0.9/notification/ws/v1/notifications',
    )
  })

  it('assetHost_shouldUseOverride_whenAssetHostSet', () => {
    setConfig({
      FH2_ADDRESS: 'http://10.0.0.1',
      FH2_ASSET_HOST: 'http://192.0.2.10:30080/fh2-assets',
    })

    const cfg = useFH2Config()

    // assetHost 覆盖只改 address，不影响 baseUrl（仍回退到 address:port）
    expect(cfg.address).toBe('http://192.0.2.10:30080/fh2-assets')
    expect(cfg.baseUrl).toBe('http://10.0.0.1:30812')
  })

  it('baseUrl_shouldUseOverride_whenBaseUrlSet', () => {
    setConfig({
      FH2_ADDRESS: 'http://10.0.0.1',
      FH2_BASE_URL: 'http://192.0.2.10:30080/fh2-gw',
    })

    const cfg = useFH2Config()

    expect(cfg.baseUrl).toBe('http://192.0.2.10:30080/fh2-gw')
    // assetHost 未覆盖，仍取 FH2_ADDRESS
    expect(cfg.address).toBe('http://10.0.0.1')
  })

  it('wss_shouldUseExplicitOverride_whenWssUrlSet', () => {
    const sameOriginWss =
      'ws://192.0.2.10:30080/fh2-gw/openapi/v0.9/notification/ws/v1/notifications'
    setConfig({
      FH2_ADDRESS: 'http://10.0.0.1',
      FH2_WSS_URL: sameOriginWss,
    })

    const cfg = useFH2Config()

    expect(cfg.wssUrl).toBe(sameOriginWss)
  })

  it('sameOrigin_shouldRouteAllThreeToSkeletonOrigin_whenFullyOverridden', () => {
    setConfig({
      FH2_ADDRESS: 'http://10.0.0.1',
      FH2_ASSET_HOST: 'http://192.0.2.10:30080/fh2-assets',
      FH2_BASE_URL: 'http://192.0.2.10:30080/fh2-gw',
      FH2_WSS_URL:
        'ws://192.0.2.10:30080/fh2-gw/openapi/v0.9/notification/ws/v1/notifications',
    })

    const cfg = useFH2Config()

    expect(cfg.address).toBe('http://192.0.2.10:30080/fh2-assets')
    expect(cfg.baseUrl).toBe('http://192.0.2.10:30080/fh2-gw')
    expect(cfg.wssUrl).toContain('ws://192.0.2.10:30080/fh2-gw/')
  })
})

describe('toWebSocketUrl', () => {
  it('toWebSocketUrl_shouldSwitchScheme_whenBaseUrlIsHttps', () => {
    expect(toWebSocketUrl('https://skeleton.example.com', '/duplex/web'))
      .toBe('wss://skeleton.example.com/duplex/web')
  })

  it('toWebSocketUrl_shouldKeepPortAndPrefix_whenBaseUrlHasBoth', () => {
    expect(toWebSocketUrl('https://192.0.2.10:30080/fh2-gw', '/duplex/web'))
      .toBe('wss://192.0.2.10:30080/fh2-gw/duplex/web')
  })

  it('toWebSocketUrl_shouldStripTrailingSlash_whenBaseUrlEndsWithSlash', () => {
    expect(toWebSocketUrl('http://10.0.0.1:30812/', '/duplex/web'))
      .toBe('ws://10.0.0.1:30812/duplex/web')
  })

  it('toWebSocketUrl_shouldPrependSlash_whenPathNotStartingWithSlash', () => {
    expect(toWebSocketUrl('http://10.0.0.1:30812', 'duplex/web'))
      .toBe('ws://10.0.0.1:30812/duplex/web')
  })
})
