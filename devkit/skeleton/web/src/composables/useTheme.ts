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

import { useThemeStore } from '@/stores/theme'
import { useFH2ThemeBus } from './useFH2ThemeBus'
import { lighten, darken, hexToRgba } from '@/utils/color'
import type { FH2SDKTheme } from '@/types/fh2'

/**
 * 从 document.documentElement 读取当前计算 CSS 变量，构造 FH2SDKTheme 对象。
 */
function resolveSDKTheme(store: ReturnType<typeof useThemeStore>): FH2SDKTheme {
  const cs = getComputedStyle(document.documentElement)
  const v  = (name: string) => cs.getPropertyValue(name).trim()
  return {
    primaryColor:      store.primaryColor,
    primaryHoverColor: store.primaryHoverColor || lighten(store.primaryColor, 15),
    primaryFocusColor: lighten(store.primaryColor, 8),
    bgBasic:           v('--app-bg'),
    bgSub:             v('--app-bg-panel'),
    bgModal:           v('--app-bg-card'),
    borderColor:       v('--app-border'),
    listActiveBg:      v('--app-bg-card'),
    listHoverBg:       v('--app-bg-card-hover'),
  }
}

/**
 * 主题管理 composable（深色主题唯一，仅支持品牌色自定义）。
 * - applyTheme()：将 store 状态同步到 DOM（CSS 变量、ThemeBus emit）。
 * - setPrimaryColor(color)：更新品牌主色并 apply。
 */
export function useTheme() {
  const store = useThemeStore()
  const { emit } = useFH2ThemeBus()

  function applyTheme() {
    const root = document.documentElement
    const primary = store.primaryColor
    root.style.setProperty('--fh-brand-primary', primary)
    const hover = store.primaryHoverColor || lighten(primary, 15)
    root.style.setProperty('--fh-brand-primary-hover', hover)
    root.style.setProperty('--fh-brand-primary-active', darken(primary, 10))
    root.style.setProperty('--fh-brand-primary-bg', hexToRgba(primary, 0.10))
    root.style.setProperty('--fh-brand-primary-bg-hover', hexToRgba(primary, 0.18))
    root.style.setProperty('--fh-brand-primary-glow', `0 0 12px ${hexToRgba(primary, 0.30)}`)
    emit(resolveSDKTheme(store))
  }

  function setPrimaryColor(color: string) {
    store.primaryColor = color
    applyTheme()
  }

  return { applyTheme, setPrimaryColor }
}
