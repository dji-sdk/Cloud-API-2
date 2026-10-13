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

const MOCK_PLACEHOLDER = '-'

/**
 * mock 数据返回占位符 "-"，否则返回原始值（支持 toLocaleString 格式化）。
 */
export function displayOrPlaceholder(value: number, isMock: boolean, locale = false): string {
  if (isMock) {
    return MOCK_PLACEHOLDER
  }
  return locale ? value.toLocaleString() : String(value)
}

/** 将 Unix 秒时间戳格式化为 YYYY-MM-DD HH:mm 字符串 */
export function formatDateTime(timestamp: number | null | undefined, includeSeconds = false): string {
  if (!timestamp || timestamp <= 0) return ''
  try {
    const date = new Date(timestamp * 1000)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')

    if (includeSeconds) {
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    return `${year}-${month}-${day} ${hours}:${minutes}`
  } catch {
    return ''
  }
}

/** 将 Unix 秒时间戳格式化为 YYYY-MM-DD 字符串 */
export function formatDate(timestamp: number | null | undefined): string {
  if (!timestamp || timestamp <= 0) return ''
  try {
    const date = new Date(timestamp * 1000)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  } catch {
    return ''
  }
}

/** 将 Unix 秒时间戳格式化为 HH:mm 字符串 */
export function formatTime(timestamp: number | null | undefined): string {
  if (!timestamp || timestamp <= 0) return ''
  try {
    const date = new Date(timestamp * 1000)
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
  } catch {
    return ''
  }
}
