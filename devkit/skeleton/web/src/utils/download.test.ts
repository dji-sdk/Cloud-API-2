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

import { describe, it, expect, vi, afterEach } from 'vitest'
import { triggerDownload, delay, DOWNLOAD_INTERVAL_MS } from './download'

afterEach(() => {
  vi.restoreAllMocks()
  vi.useRealTimers()
})

describe('triggerDownload', () => {
  it('点击一个 href 指向目标地址的锚点', () => {
    const clicked: string[] = []
    const spy = vi.spyOn(HTMLAnchorElement.prototype, 'click')
      .mockImplementation(function mockClick(this: HTMLAnchorElement): void {
        clicked.push(this.href)
      })

    triggerDownload('https://example.com/a.mp4')

    expect(spy).toHaveBeenCalledOnce()
    expect(clicked).toEqual(['https://example.com/a.mp4'])
  })

  it('触发后从 DOM 中移除锚点，不残留节点', () => {
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)

    triggerDownload('https://example.com/a.mp4')

    expect(document.querySelectorAll('a')).toHaveLength(0)
  })

  it('多次调用互不干扰，每次都会触发一次点击', () => {
    const clicked: string[] = []
    vi.spyOn(HTMLAnchorElement.prototype, 'click')
      .mockImplementation(function mockClick(this: HTMLAnchorElement): void {
        clicked.push(this.href)
      })

    triggerDownload('https://example.com/a.mp4')
    triggerDownload('https://example.com/b.jpg')
    triggerDownload('https://example.com/c.mp4')

    expect(clicked).toEqual([
      'https://example.com/a.mp4',
      'https://example.com/b.jpg',
      'https://example.com/c.mp4',
    ])
  })

  it('不使用 window.open，避免被弹窗拦截器拦截', () => {
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)

    triggerDownload('https://example.com/a.mp4')

    expect(openSpy).not.toHaveBeenCalled()
  })
})

describe('delay', () => {
  it('等待指定毫秒数后 resolve', async () => {
    vi.useFakeTimers()
    const onResolved = vi.fn()

    const pending = delay(100).then(onResolved)

    await vi.advanceTimersByTimeAsync(99)
    expect(onResolved).not.toHaveBeenCalled()

    await vi.advanceTimersByTimeAsync(1)
    await pending
    expect(onResolved).toHaveBeenCalledOnce()
  })
})

describe('DOWNLOAD_INTERVAL_MS', () => {
  it('是一个正数间隔，保证批量下载被逐个触发', () => {
    expect(DOWNLOAD_INTERVAL_MS).toBeGreaterThan(0)
  })
})
