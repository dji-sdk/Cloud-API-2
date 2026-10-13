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

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

// mock useVoiceInput（async factory，确保可以使用 Vue ref）
vi.mock('@/composables/useVoiceInput', async () => {
  const { ref } = await import('vue')
  const mockToggle = vi.fn()
  const _isListening = ref(false)
  const _isSupported = ref(true)
  const _error = ref<string | null>(null)
  const _interimText = ref('')
  const _finalText = ref('')

  return {
    useVoiceInput: () => ({
      isListening: _isListening,
      isSupported: _isSupported,
      error: _error,
      interimText: _interimText,
      finalText: _finalText,
      toggle: mockToggle,
    }),
    // 暴露给测试操控的内部状态
    __mock: {
      isListening: _isListening,
      isSupported: _isSupported,
      error: _error,
      interimText: _interimText,
      finalText: _finalText,
      toggle: mockToggle,
    },
  }
})

import ChatInput from './ChatInput.vue'
import * as voiceMod from '@/composables/useVoiceInput'

/** 获取可操控的 mock 状态 */
function getMock() {
  return (voiceMod as unknown as { __mock: ReturnType<typeof voiceMod.useVoiceInput> & { toggle: ReturnType<typeof vi.fn> } }).__mock
}

describe('ChatInput — voice input', () => {
  beforeEach(() => {
    const m = getMock()
    m.isListening.value = false
    m.isSupported.value = true
    m.error.value = null
    m.interimText.value = ''
    m.finalText.value = ''
    ;(m.toggle as ReturnType<typeof vi.fn>).mockClear()
  })

  it('isSupported=true 时渲染麦克风按钮', () => {
    const wrapper = mount(ChatInput, { props: { disabled: false } })
    expect(wrapper.find('.chat-input__mic').exists()).toBe(true)
  })

  it('isSupported=false 时不渲染麦克风按钮', async () => {
    getMock().isSupported.value = false
    const wrapper = mount(ChatInput, { props: { disabled: false } })
    await nextTick()
    expect(wrapper.find('.chat-input__mic').exists()).toBe(false)
  })

  it('点击麦克风按钮调用 toggle()', async () => {
    const wrapper = mount(ChatInput, { props: { disabled: false } })
    await wrapper.find('.chat-input__mic').trigger('click')
    expect(getMock().toggle).toHaveBeenCalledOnce()
  })

  it('disabled=true 时麦克风按钮被禁用', () => {
    const wrapper = mount(ChatInput, { props: { disabled: true } })
    const micBtn = wrapper.find('.chat-input__mic')
    expect((micBtn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('isListening=true 时麦克风按钮有 chat-input__mic--recording 类', async () => {
    getMock().isListening.value = true
    const wrapper = mount(ChatInput, { props: { disabled: false } })
    await nextTick()
    expect(wrapper.find('.chat-input__mic').classes()).toContain('chat-input__mic--recording')
  })

  it('finalText 变化后填入输入框', async () => {
    const wrapper = mount(ChatInput, { props: { disabled: false } })
    getMock().finalText.value = '起飞'
    await nextTick()
    const input = wrapper.find('.chat-input__field').element as HTMLInputElement
    expect(input.value).toBe('起飞')
  })

  it('finalText 变化追加到已有输入内容', async () => {
    const wrapper = mount(ChatInput, { props: { disabled: false } })
    const input = wrapper.find('.chat-input__field')
    // 模拟用户先输入了一些内容
    await input.setValue('任务：')
    // 点击麦克风（handleMicClick 同步记录 recordingBaseText = '任务：'）
    await wrapper.find('.chat-input__mic').trigger('click')
    // 模拟 toggle() 的效果（isListening = true）
    getMock().isListening.value = true
    await nextTick()
    // 语音识别结果落定
    getMock().finalText.value = '起飞'
    await nextTick()
    expect((input.element as HTMLInputElement).value).toBe('任务：起飞')
  })
})
