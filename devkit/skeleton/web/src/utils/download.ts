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

/** 批量下载时相邻两个文件的触发间隔，避免浏览器把连续下载判定为滥用而拦截。 */
export const DOWNLOAD_INTERVAL_MS = 300

/**
 * 触发浏览器下载。
 *
 * 不能用 `window.open`：它在 `await` 之后调用已脱离用户手势窗口，
 * 弹窗拦截器会静默丢弃除第一个之外的所有请求。构造隐藏 `<a>` 并点击不受此限制。
 * 文件名由响应头 `Content-Disposition` 决定（地址与页面同源，不存在 `download` 属性被忽略的问题）。
 */
export function triggerDownload(url: string): void {
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.rel = 'noopener'
  anchor.style.display = 'none'
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
}

/** 延迟指定毫秒数。 */
export function delay(ms: number): Promise<void> {
  return new Promise(resolve => window.setTimeout(resolve, ms))
}
