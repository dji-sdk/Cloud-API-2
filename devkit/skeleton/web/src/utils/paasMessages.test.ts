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

import { describe, it, expect } from 'vitest'
import {
  formatPaasUnavailableMessage,
  formatPaasShortHint,
  formatPaasResourceErrorMessage,
  formatUnknownError,
} from './paasMessages'

describe('formatPaasUnavailableMessage', () => {
  it('formatPaasUnavailableMessage_shouldExplainHowToConfigure_whenNotConfigured', () => {
    const message = formatPaasUnavailableMessage('not-configured', '')

    expect(message).toContain('未配置底座地址')
    // 「怎么补」必须包含本地开发与容器部署两条路径
    expect(message).toContain('VITE_FH2_ADDRESS')
    expect(message).toContain('容器部署')
    // 回归场景：配置后仍需重启容器，且要提示强制刷新排除旧 config.js 缓存
    expect(message).toContain('重启容器')
    expect(message).toContain('强制刷新')
  })

  it('formatPaasUnavailableMessage_shouldIncludeAddress_whenUnreachable', () => {
    const message = formatPaasUnavailableMessage('unreachable', 'http://10.0.0.1')

    expect(message).toContain('无法连接底座')
    expect(message).toContain('http://10.0.0.1')
    // 给用户一条可自行验证的动作，避免只能干等
    expect(message).toContain('浏览器')
  })
})

describe('formatPaasShortHint', () => {
  it('formatPaasShortHint_shouldSayNotConfigured_whenReasonIsNotConfigured', () => {
    expect(formatPaasShortHint('not-configured', '')).toBe('需要底座：尚未配置 FH2 上云 API 地址')
  })

  it('formatPaasShortHint_shouldIncludeAddress_whenReasonIsUnreachable', () => {
    expect(formatPaasShortHint('unreachable', 'http://10.0.0.1'))
      .toBe('需要底座：http://10.0.0.1 未响应')
  })
})

describe('formatPaasResourceErrorMessage', () => {
  it('formatPaasResourceErrorMessage_shouldIncludeResourceAndUrl', () => {
    const message = formatPaasResourceErrorMessage(
      '设备管理 SDK',
      'http://10.0.0.1/paasjs-demo/device-manager-sdk/index.js',
    )

    expect(message).toContain('设备管理 SDK加载失败')
    expect(message).toContain('http://10.0.0.1/paasjs-demo/device-manager-sdk/index.js')
    // 同源反代部署是最常见的踩坑点，必须在文案里点名
    expect(message).toContain('FH2_ASSET_HOST')
  })
})

describe('formatUnknownError', () => {
  it('formatUnknownError_shouldReturnMessage_whenGivenError', () => {
    expect(formatUnknownError(new Error('boom'))).toBe('boom')
  })

  it('formatUnknownError_shouldFallbackToUnknown_whenGivenNonError', () => {
    // catch 到的可能是字符串或任意值，直接拼接会把 undefined 甩给用户
    expect(formatUnknownError('oops')).toBe('未知错误：oops')
    expect(formatUnknownError(undefined)).toBe('未知错误：undefined')
  })

  it('formatUnknownError_shouldFallback_whenErrorHasEmptyMessage', () => {
    expect(formatUnknownError(new Error(''))).toBe('未知错误：Error')
  })

  it('formatUnknownError_shouldTranslateToChinese_whenErrorIsBrowserNetworkError', () => {
    // Chrome / Firefox / Safari 三家的原生文案，都不该原样落到用户眼前
    const nativeMessages = [
      'Failed to fetch',
      'NetworkError when attempting to fetch resource.',
      'Load failed',
      'Network request failed',
    ]

    for (const message of nativeMessages) {
      const result = formatUnknownError(new TypeError(message))
      expect(result).toContain('网络请求失败')
      expect(result).not.toContain(message)
    }
  })

  it('formatUnknownError_shouldKeepChineseMessage_whenErrorIsSelfThrown', () => {
    // 仓库内自抛的中文错误带可操作指引，不能被兜底逻辑覆盖
    const message = 'FH2_ADDRESS 未配置。本地开发请复制 .env.example 为 .env 并填写。'

    expect(formatUnknownError(new Error(message))).toBe(message)
  })
})
