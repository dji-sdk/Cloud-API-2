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

import { ref } from 'vue'
import { defineStore } from 'pinia'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface ToastMessage {
  id: number
  type: ToastType
  text: string
  duration: number
  /** 可选操作按钮，点击后自动关闭 toast */
  action?: { label: string; onClick: () => void }
}

let nextId = 0

export const useToastStore = defineStore('toast', () => {
  const messages = ref<ToastMessage[]>([])

  function show(
    text: string,
    type: ToastType = 'info',
    duration = 3000,
    action?: { label: string; onClick: () => void },
  ): void {
    const id = nextId++
    messages.value.push({ id, type, text, duration, action })

    if (duration > 0) {
      setTimeout(() => {
        dismiss(id)
      }, duration)
    }
  }

  function dismiss(id: number): void {
    const idx = messages.value.findIndex(m => m.id === id)
    if (idx !== -1) {
      messages.value.splice(idx, 1)
    }
  }

  function success(text: string, duration?: number): void {
    show(text, 'success', duration)
  }

  function error(text: string, duration?: number): void {
    show(text, 'error', duration)
  }

  function warning(text: string, duration?: number): void {
    show(text, 'warning', duration)
  }

  function info(text: string, duration?: number): void {
    show(text, 'info', duration)
  }

  return { messages, show, dismiss, success, error, warning, info }
})
