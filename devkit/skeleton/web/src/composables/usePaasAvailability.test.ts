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
import {
  usePaasAvailability,
  describePaasUnavailability,
  resetPaasAvailability,
} from './usePaasAvailability'

/** window.__FH_CONFIG__ 的最小类型，便于在测试中直接赋值 */
type FhConfig = Record<string, string | undefined>

function setConfig(cfg: FhConfig): void {
  ;(window as unknown as { __FH_CONFIG__?: FhConfig }).__FH_CONFIG__ = cfg
}

/**
 * 会被开发者本地 .env 污染的可选配置项。
 * vitest 默认加载 .env，不置空会让本机填写的底座地址混入断言。
 */
const ENV_KEYS = ['FH2_ADDRESS', 'FH2_ASSET_HOST'] as const

describe('usePaasAvailability', () => {
  beforeEach(() => {
    resetPaasAvailability()
    setConfig({})
    for (const key of ENV_KEYS) {
      vi.stubEnv(`VITE_${key}`, '')
    }
  })

  afterEach(() => {
    resetPaasAvailability()
    vi.unstubAllEnvs()
    vi.unstubAllGlobals()
    delete (window as unknown as { __FH_CONFIG__?: FhConfig }).__FH_CONFIG__
  })

  it('usePaasAvailability_shouldMarkAvailable_whenProbeSucceeds', async () => {
    setConfig({ FH2_ADDRESS: 'http://10.0.0.1' })
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    const { availability, probed } = usePaasAvailability()

    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(availability.value.available).toBe(true)
    expect(availability.value.reason).toBeNull()
    expect(availability.value.address).toBe('http://10.0.0.1')
    // 探针用 no-cors：底座不回 CORS 头，默认模式会把「跨域可达」误判为不可达
    expect(fetchMock).toHaveBeenCalledWith(
      'http://10.0.0.1/paas.js',
      expect.objectContaining({ mode: 'no-cors' }),
    )
  })

  it('usePaasAvailability_shouldReportUnreachable_whenProbeRejects', async () => {
    setConfig({ FH2_ADDRESS: 'http://10.0.0.1' })
    // 端口不通 / DNS 失败 / 超时中断在浏览器里都是 fetch reject
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    const { availability, probed } = usePaasAvailability()

    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(availability.value.available).toBe(false)
    expect(availability.value.reason).toBe('unreachable')
    expect(availability.value.address).toBe('http://10.0.0.1')
  })

  it('usePaasAvailability_shouldReportNotConfigured_whenAddressMissing', async () => {
    const fetchMock = vi.fn()
    vi.stubGlobal('fetch', fetchMock)

    const { availability, probed } = usePaasAvailability()

    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(availability.value.available).toBe(false)
    expect(availability.value.reason).toBe('not-configured')
    expect(availability.value.address).toBe('')
    // 地址都没有就不该发请求，避免对 '/' 之类无意义目标产生噪音
    expect(fetchMock).not.toHaveBeenCalled()
  })

  it('usePaasAvailability_shouldPreferAssetHost_whenBothConfigured', async () => {
    setConfig({
      FH2_ADDRESS: 'http://10.0.0.1',
      FH2_ASSET_HOST: 'http://192.0.2.10:30080/fh2-assets',
    })
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    const { availability } = usePaasAvailability()

    await vi.waitFor(() => expect(availability.value.available).toBe(true))
    // 探的是静态资源地址（同源反代），不是底座门户地址
    expect(availability.value.address).toBe('http://192.0.2.10:30080/fh2-assets')
    expect(fetchMock).toHaveBeenCalledWith(
      'http://192.0.2.10:30080/fh2-assets/paas.js',
      expect.objectContaining({ mode: 'no-cors' }),
    )
  })

  it('usePaasAvailability_shouldProbeOnlyOnce_whenCalledFromMultipleComponents', async () => {
    setConfig({ FH2_ADDRESS: 'http://10.0.0.1' })
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 200 }))
    vi.stubGlobal('fetch', fetchMock)

    const first = usePaasAvailability()
    const second = usePaasAvailability()

    await vi.waitFor(() => expect(first.probed.value).toBe(true))
    await vi.waitFor(() => expect(second.probed.value).toBe(true))
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('usePaasAvailability_shouldResetState_whenResetCalled', async () => {
    setConfig({ FH2_ADDRESS: 'http://10.0.0.1' })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 200 })))

    const { availability, probed } = usePaasAvailability()
    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(availability.value.available).toBe(true)

    resetPaasAvailability()

    expect(availability.value.available).toBe(false)
    expect(availability.value.reason).toBeNull()
    expect(probed.value).toBe(false)
  })
})

describe('describePaasUnavailability', () => {
  it('describePaasUnavailability_shouldReturnEmptyString_whenAvailable', () => {
    expect(
      describePaasUnavailability({ available: true, reason: null, address: 'http://10.0.0.1' }),
    ).toBe('')
  })

  it('describePaasUnavailability_shouldMentionConfigGuide_whenNotConfigured', () => {
    const message = describePaasUnavailability({
      available: false,
      reason: 'not-configured',
      address: '',
    })

    expect(message).toContain('未配置底座地址')
    expect(message).toContain('VITE_FH2_ADDRESS')
  })

  it('describePaasUnavailability_shouldMentionAddress_whenUnreachable', () => {
    const message = describePaasUnavailability({
      available: false,
      reason: 'unreachable',
      address: 'http://10.0.0.1',
    })

    expect(message).toContain('http://10.0.0.1')
    expect(message).toContain('未响应')
  })
})
