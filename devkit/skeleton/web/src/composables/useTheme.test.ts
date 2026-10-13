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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('./useFH2ThemeBus', () => ({
  useFH2ThemeBus: () => ({ register: vi.fn(), emit: vi.fn() }),
}))

vi.mock('@/utils/color', () => ({
  lighten: (hex: string, _p: number) => hex,
  darken: (hex: string, _p: number) => hex,
  hexToRgba: (hex: string, alpha: number) => `rgba(0,0,0,${alpha})`,
}))

vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: (key: string, fallback = '') => {
    const defaults: Record<string, string> = {
      PRIMARY_COLOR: '#06b6d4',
      PRIMARY_HOVER_COLOR: '',
    }
    return defaults[key] ?? fallback
  },
}))

describe('useTheme', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    const props = ['--fh-brand-primary', '--fh-brand-primary-hover',
      '--fh-brand-primary-active', '--fh-brand-primary-bg',
      '--fh-brand-primary-bg-hover', '--fh-brand-primary-glow']
    props.forEach(p => document.documentElement.style.removeProperty(p))
    document.body.classList.remove('set-change-color')
    vi.resetModules()
  })

  it('applyTheme 设置 --fh-brand-primary CSS 变量', async () => {
    const { useTheme } = await import('./useTheme')
    const { applyTheme } = useTheme()
    applyTheme()
    expect(document.documentElement.style.getPropertyValue('--fh-brand-primary')).toBe('#06b6d4')
  })

  it('applyTheme 不再给 body 添加 set-change-color 类（避免触发 SDK 暗色模式检测）', async () => {
    const { useTheme } = await import('./useTheme')
    const { applyTheme } = useTheme()
    applyTheme()
    expect(document.body.classList.contains('set-change-color')).toBe(false)
  })

  it('setPrimaryColor 更新 CSS 变量', async () => {
    const { useTheme } = await import('./useTheme')
    const { setPrimaryColor } = useTheme()
    setPrimaryColor('#ff0000')
    expect(document.documentElement.style.getPropertyValue('--fh-brand-primary')).toBe('#ff0000')
  })

  it('setPrimaryColor 设置 hover 变量', async () => {
    const { useTheme } = await import('./useTheme')
    const { setPrimaryColor } = useTheme()
    setPrimaryColor('#06b6d4')
    const hover = document.documentElement.style.getPropertyValue('--fh-brand-primary-hover')
    expect(hover).toBe('#06b6d4')
  })

  it('applyTheme 设置 active / bg / glow 派生变量', async () => {
    const { useTheme } = await import('./useTheme')
    const { applyTheme } = useTheme()
    applyTheme()
    const root = document.documentElement
    expect(root.style.getPropertyValue('--fh-brand-primary-active')).toBeTruthy()
    expect(root.style.getPropertyValue('--fh-brand-primary-bg')).toBeTruthy()
    expect(root.style.getPropertyValue('--fh-brand-primary-bg-hover')).toBeTruthy()
    expect(root.style.getPropertyValue('--fh-brand-primary-glow')).toBeTruthy()
  })
})
