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

import { ref, onScopeDispose } from 'vue'
import type { Ref } from 'vue'

/** 语音识别语言，默认中文 */
const RECOGNITION_LANG = 'zh-CN'

/** 错误信息自动清除延迟（毫秒） */
const ERROR_AUTO_CLEAR_MS = 1500

/** 麦克风权限被拒的错误类型 */
const ERROR_NOT_ALLOWED = 'not-allowed'

/** useVoiceInput 返回值类型 */
export interface UseVoiceInputReturn {
  /** 当前是否正在录音 */
  isListening: Ref<boolean>
  /** 浏览器是否支持 Web Speech API */
  isSupported: Ref<boolean>
  /** 错误信息（null 表示无错误，1.5s 后自动清空） */
  error: Ref<string | null>
  /** 实时 interim 识别片段（录音结束或 final 落定后清空） */
  interimText: Ref<string>
  /** 最新一次 final 识别结果（原始文本，不含输入框已有内容） */
  finalText: Ref<string>
  /** 切换录音状态：未录音时开始，录音中时停止 */
  toggle: () => void
}

type SpeechRecognitionCtor = new () => SpeechRecognition

/** 获取 SpeechRecognition 构造函数（兼容 webkit 前缀） */
function getSpeechRecognitionCtor(): SpeechRecognitionCtor | null {
  if (typeof window === 'undefined') {
    return null
  }
  const w = window as Window & { webkitSpeechRecognition?: SpeechRecognitionCtor }
  return (window.SpeechRecognition ?? w.webkitSpeechRecognition) ?? null
}

/**
 * 封装浏览器 Web Speech API 的语音输入 composable。
 * 不关心输入框的现有文本（baseText 逻辑由调用方处理）。
 */
export function useVoiceInput(): UseVoiceInputReturn {
  const isListening = ref(false)
  const isSupported = ref(getSpeechRecognitionCtor() !== null)
  const error = ref<string | null>(null)
  const interimText = ref('')
  const finalText = ref('')

  let recognition: SpeechRecognition | null = null
  let errorTimer: ReturnType<typeof setTimeout> | null = null

  function toggle(): void {
    if (isListening.value) {
      recognition?.stop()
    } else {
      start()
    }
  }

  function start(): void {
    if (recognition !== null) {
      return
    }

    const Ctor = getSpeechRecognitionCtor()
    if (!Ctor) {
      return
    }

    recognition = new Ctor()
    recognition.lang = RECOGNITION_LANG
    recognition.continuous = false
    recognition.interimResults = true

    error.value = null
    interimText.value = ''
    finalText.value = ''
    isListening.value = true

    recognition.onresult = (event: SpeechRecognitionEvent) => {
      let interim = ''
      let final = ''
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i]
        if (result.isFinal) {
          final += result[0].transcript
        } else {
          interim += result[0].transcript
        }
      }
      if (final) {
        finalText.value = final
        interimText.value = ''
      } else {
        interimText.value = interim
      }
    }

    recognition.onend = () => {
      recognition = null
      isListening.value = false
      interimText.value = ''
      // 只在没有错误时才取消自动清除计时器
      // 若 onerror 触发，onend 会紧随其后，不能在此清除 errorTimer
      if (!error.value) {
        clearTimeout(errorTimer ?? undefined)
        errorTimer = null
      }
    }

    recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
      recognition = null
      isListening.value = false
      interimText.value = ''
      error.value = event.error === ERROR_NOT_ALLOWED
        ? '请允许麦克风权限'
        : `语音识别错误: ${event.error}`
      if (errorTimer !== null) {
        clearTimeout(errorTimer)
      }
      errorTimer = setTimeout(() => {
        error.value = null
      }, ERROR_AUTO_CLEAR_MS)
    }

    recognition.start()
  }

  onScopeDispose(() => {
    recognition?.abort()
    recognition = null
    if (errorTimer !== null) {
      clearTimeout(errorTimer)
      errorTimer = null
    }
  })

  return { isListening, isSupported, error, interimText, finalText, toggle }
}
