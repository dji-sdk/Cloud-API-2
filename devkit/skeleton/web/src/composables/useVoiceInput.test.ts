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

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { nextTick, effectScope } from 'vue'

/** mock SpeechRecognition 实例，供各 test 用 */
interface MockRecognition {
  lang: string
  continuous: boolean
  interimResults: boolean
  onresult: ((e: SpeechRecognitionEvent) => void) | null
  onend: (() => void) | null
  onerror: ((e: SpeechRecognitionErrorEvent) => void) | null
  start: ReturnType<typeof vi.fn>
  stop: ReturnType<typeof vi.fn>
  abort: ReturnType<typeof vi.fn>
}

describe('useVoiceInput', () => {
  let mockRec: MockRecognition

  beforeEach(() => {
    vi.resetModules()
    vi.useFakeTimers()

    mockRec = {
      lang: '',
      continuous: false,
      interimResults: false,
      onresult: null,
      onend: null,
      onerror: null,
      start: vi.fn(),
      stop: vi.fn(),
      abort: vi.fn(),
    }

    Object.defineProperty(window, 'SpeechRecognition', {
      value: vi.fn(function () { return mockRec }),
      writable: true,
      configurable: true,
    })
  })

  afterEach(() => {
    vi.useRealTimers()
    Object.defineProperty(window, 'SpeechRecognition', {
      value: undefined,
      writable: true,
      configurable: true,
    })
  })

  it('isSupported 在 SpeechRecognition 可用时为 true', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { isSupported } = scope.run(() => useVoiceInput())!
    expect(isSupported.value).toBe(true)
    scope.stop()
  })

  it('isSupported 在 SpeechRecognition 不可用时为 false', async () => {
    Object.defineProperty(window, 'SpeechRecognition', { value: undefined, writable: true, configurable: true })
    Object.defineProperty(window, 'webkitSpeechRecognition', { value: undefined, writable: true, configurable: true })
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { isSupported } = scope.run(() => useVoiceInput())!
    expect(isSupported.value).toBe(false)
    scope.stop()
  })

  it('toggle() 开始录音：isListening 变为 true，recognition.start 被调用', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { isListening, toggle } = scope.run(() => useVoiceInput())!
    toggle()
    await nextTick()
    expect(isListening.value).toBe(true)
    expect(mockRec.start).toHaveBeenCalledOnce()
    scope.stop()
  })

  it('toggle() 录音中再次调用：recognition.stop 被调用', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { toggle } = scope.run(() => useVoiceInput())!
    toggle()
    toggle()
    expect(mockRec.stop).toHaveBeenCalledOnce()
    scope.stop()
  })

  it('recognition 配置：lang=zh-CN, continuous=false, interimResults=true', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { toggle } = scope.run(() => useVoiceInput())!
    toggle()
    expect(mockRec.lang).toBe('zh-CN')
    expect(mockRec.continuous).toBe(false)
    expect(mockRec.interimResults).toBe(true)
    scope.stop()
  })

  it('onresult 收到 final 结果：finalText 更新，interimText 清空', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { finalText, interimText, toggle } = scope.run(() => useVoiceInput())!
    toggle()
    mockRec.onresult?.({
      resultIndex: 0,
      results: Object.assign(
        [Object.assign([{ transcript: '起飞', confidence: 1 }], { isFinal: true })],
        { length: 1 },
      ),
    } as unknown as SpeechRecognitionEvent)
    await nextTick()
    expect(finalText.value).toBe('起飞')
    expect(interimText.value).toBe('')
    scope.stop()
  })

  it('onresult 收到 interim 结果：interimText 更新，finalText 不变', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { finalText, interimText, toggle } = scope.run(() => useVoiceInput())!
    toggle()
    mockRec.onresult?.({
      resultIndex: 0,
      results: Object.assign(
        [Object.assign([{ transcript: '起', confidence: 0.5 }], { isFinal: false })],
        { length: 1 },
      ),
    } as unknown as SpeechRecognitionEvent)
    await nextTick()
    expect(interimText.value).toBe('起')
    expect(finalText.value).toBe('')
    scope.stop()
  })

  it('onend：isListening 变为 false，interimText 清空', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { isListening, interimText, toggle } = scope.run(() => useVoiceInput())!
    toggle()
    mockRec.onend?.()
    await nextTick()
    expect(isListening.value).toBe(false)
    expect(interimText.value).toBe('')
    scope.stop()
  })

  it('onerror not-allowed：isListening=false, error 提示文本，1.5s 后清空', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { error, isListening, toggle } = scope.run(() => useVoiceInput())!
    toggle()
    mockRec.onerror?.({ error: 'not-allowed' } as SpeechRecognitionErrorEvent)
    await nextTick()
    expect(isListening.value).toBe(false)
    expect(error.value).toBe('请允许麦克风权限')
    vi.advanceTimersByTime(1500)
    await nextTick()
    expect(error.value).toBeNull()
    scope.stop()
  })

  it('onerror 其他错误：error 包含错误类型', async () => {
    const { useVoiceInput } = await import('./useVoiceInput')
    const scope = effectScope()
    const { error, toggle } = scope.run(() => useVoiceInput())!
    toggle()
    mockRec.onerror?.({ error: 'network' } as SpeechRecognitionErrorEvent)
    await nextTick()
    expect(error.value).toContain('network')
    scope.stop()
  })

  it('scope dispose 时中止正在进行的录音', async () => {
    const { useVoiceInput } = await import('@/composables/useVoiceInput')
    const scope = effectScope()
    let voice: ReturnType<typeof useVoiceInput> | undefined
    scope.run(() => {
      voice = useVoiceInput()
    })
    voice!.toggle()
    expect(voice!.isListening.value).toBe(true)
    scope.stop()
    expect(mockRec.abort).toHaveBeenCalledTimes(1)
  })
})
