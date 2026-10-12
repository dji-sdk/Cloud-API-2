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

import { onUnmounted } from 'vue'
import type { FH2SDKTheme } from '@/types/fh2'

type ThemeHandler = (theme: FH2SDKTheme) => void

/** 模块级单例：所有注册的 FH2 SDK 主题处理函数。 */
const _handlers = new Set<ThemeHandler>()

/**
 * FH2 SDK 主题广播 bus。
 * - register(handler)：注册处理函数，组件卸载时自动清理。
 * - emit(theme)：广播到所有已注册实例，异常静默跳过。
 */
export function useFH2ThemeBus() {
  function register(handler: ThemeHandler) {
    _handlers.add(handler)
    onUnmounted(() => _handlers.delete(handler))
  }

  function emit(theme: FH2SDKTheme) {
    _handlers.forEach((h) => {
      try {
        h(theme)
      } catch {
        // SDK setTheme 尚未实现时静默跳过
      }
    })
  }

  return { register, emit }
}
