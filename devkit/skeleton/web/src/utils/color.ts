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

/**
 * 解析六位 hex 颜色为 0-255 RGB 三元组。
 */
function parseHex(hex: string): [number, number, number] {
  return [
    parseInt(hex.slice(1, 3), 16),
    parseInt(hex.slice(3, 5), 16),
    parseInt(hex.slice(5, 7), 16),
  ]
}

/**
 * 通过 HSL 亮度值调整 hex 颜色。
 * @param hex - 六位 hex 颜色，如 "#1a73e8"
 * @param percent - 亮度偏移量（正值亮化、负值暗化）
 * @returns 调整后的六位 hex 颜色
 */
function adjustLightness(hex: string, percent: number): string {
  const [ri, gi, bi] = parseHex(hex)
  const r = ri / 255
  const g = gi / 255
  const b = bi / 255

  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  let h = 0
  let s = 0
  let l = (max + min) / 2

  if (max !== min) {
    const d = max - min
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
    switch (max) {
      case r: h = ((g - b) / d + (g < b ? 6 : 0)) / 6; break
      case g: h = ((b - r) / d + 2) / 6; break
      case b: h = ((r - g) / d + 4) / 6; break
    }
  }

  l = Math.max(0, Math.min(1, l + percent / 100))

  function htr(p: number, q: number, t: number): number {
    if (t < 0) { t += 1 }
    if (t > 1) { t -= 1 }
    if (t < 1 / 6) { return p + (q - p) * 6 * t }
    if (t < 1 / 2) { return q }
    if (t < 2 / 3) { return p + (q - p) * (2 / 3 - t) * 6 }
    return p
  }

  let ro: number, go: number, bo: number
  if (s === 0) {
    ro = go = bo = l
  } else {
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    ro = htr(p, q, h + 1 / 3)
    go = htr(p, q, h)
    bo = htr(p, q, h - 1 / 3)
  }

  const toHex = (n: number) => Math.round(n * 255).toString(16).padStart(2, '0')
  return `#${toHex(ro)}${toHex(go)}${toHex(bo)}`
}

/**
 * 将 hex 颜色亮化指定百分比。
 * @param hex - 六位 hex 颜色，如 "#1a73e8"
 * @param percent - 亮化量（0-100）；传入 15 = 亮度 +15%
 * @returns 亮化后的六位 hex 颜色
 */
export function lighten(hex: string, percent: number): string {
  return adjustLightness(hex, percent)
}

/**
 * 将 hex 颜色暗化指定百分比。
 * @param hex - 六位 hex 颜色，如 "#06b6d4"
 * @param percent - 暗化量（0-100）；传入 10 = 亮度 -10%
 * @returns 暗化后的六位 hex 颜色
 */
export function darken(hex: string, percent: number): string {
  return adjustLightness(hex, -percent)
}

/**
 * 将 hex 颜色转为 rgba() 字符串。
 * @param hex - 六位 hex 颜色
 * @param alpha - 透明度（0-1）
 * @returns rgba 字符串，如 "rgba(6, 182, 212, 0.10)"
 */
export function hexToRgba(hex: string, alpha: number): string {
  const [r, g, b] = parseHex(hex)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}
