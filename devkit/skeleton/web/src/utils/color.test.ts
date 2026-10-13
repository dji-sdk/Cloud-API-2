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
import { lighten, darken, hexToRgba } from './color'

describe('lighten', () => {
  it('返回合法 hex 颜色字符串', () => {
    const result = lighten('#1a73e8', 15)
    expect(result).toMatch(/^#[0-9a-f]{6}$/)
  })

  it('亮化后颜色不等于原色（percent > 0 且原色非白）', () => {
    expect(lighten('#1a73e8', 15)).not.toBe('#1a73e8')
  })

  it('percent=0 时返回与原色相同（四舍五入误差允许）', () => {
    const result = lighten('#1a73e8', 0)
    expect(result).toMatch(/^#[0-9a-f]{6}$/)
  })

  it('black + 100% → white', () => {
    expect(lighten('#000000', 100)).toBe('#ffffff')
  })

  it('white + 50% 不超过 #ffffff', () => {
    expect(lighten('#ffffff', 50)).toBe('#ffffff')
  })

  it('lighten(#ff0000, 20) 红色变亮', () => {
    const result = lighten('#ff0000', 20)
    expect(result).toMatch(/^#[0-9a-f]{6}$/)
    expect(result).not.toBe('#ff0000')
  })
})

describe('darken', () => {
  it('返回合法 hex 颜色字符串', () => {
    const result = darken('#06b6d4', 10)
    expect(result).toMatch(/^#[0-9a-f]{6}$/)
  })

  it('暗化后颜色不等于原色（percent > 0 且原色非黑）', () => {
    expect(darken('#06b6d4', 10)).not.toBe('#06b6d4')
  })

  it('black 暗化不低于 #000000', () => {
    expect(darken('#000000', 50)).toBe('#000000')
  })
})

describe('hexToRgba', () => {
  it('转换 teal 色为正确的 rgba', () => {
    expect(hexToRgba('#06b6d4', 0.10)).toBe('rgba(6, 182, 212, 0.1)')
  })

  it('转换黑色', () => {
    expect(hexToRgba('#000000', 0.5)).toBe('rgba(0, 0, 0, 0.5)')
  })

  it('转换白色', () => {
    expect(hexToRgba('#ffffff', 1)).toBe('rgba(255, 255, 255, 1)')
  })
})
