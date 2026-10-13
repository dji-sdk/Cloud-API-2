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

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { useMediaList } from './useMediaList'
import { probeMediaContent, fetchMediaList } from '@/api/media'
import { DOWNLOAD_INTERVAL_MS } from '@/utils/download'

vi.mock('@/api/media', async importOriginal => {
  const actual = await importOriginal<typeof import('@/api/media')>()
  return {
    ...actual,
    probeMediaContent: vi.fn(),
    fetchMediaList: vi.fn(),
    batchDeleteMedia: vi.fn(),
  }
})

const probeMock = vi.mocked(probeMediaContent)
const listMock = vi.mocked(fetchMediaList)

/** 记录被点击的下载地址。 */
let clicked: string[]

beforeEach(() => {
  localStorage.clear()
  clicked = []
  vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function mockClick(
    this: HTMLAnchorElement,
  ): void {
    clicked.push(this.href)
  })
  probeMock.mockReset()
  listMock.mockReset()
  vi.useFakeTimers()
})

afterEach(() => {
  vi.restoreAllMocks()
  vi.useRealTimers()
})

describe('downloadFile', () => {
  it('探测通过后触发下载，地址指向后端代理端点', async () => {
    probeMock.mockResolvedValue(undefined)
    const { downloadFile, error } = useMediaList()

    await downloadFile(123)

    expect(probeMock).toHaveBeenCalledWith(123)
    expect(clicked).toHaveLength(1)
    expect(clicked[0]).toContain('/api/v1/media/123/content')
    expect(clicked[0]).toContain('download=true')
    expect(clicked[0]).not.toContain('30802')
    expect(error.value).toBeNull()
  })

  it('探测失败时写入错误提示且不触发下载', async () => {
    probeMock.mockRejectedValue(new Error('媒体文件不可用（HTTP 502）'))
    const { downloadFile, error } = useMediaList()

    await downloadFile(123)

    expect(clicked).toHaveLength(0)
    expect(error.value).toBe('媒体文件不可用（HTTP 502）')
  })

  it('探测抛出非 Error 时回落到兜底文案', async () => {
    probeMock.mockRejectedValue('boom')
    const { downloadFile, error } = useMediaList()

    await downloadFile(123)

    expect(clicked).toHaveLength(0)
    expect(error.value).toBe('下载失败')
  })

  it('重新下载时清空上一次的错误', async () => {
    probeMock.mockRejectedValueOnce(new Error('媒体文件不可用（HTTP 502）'))
    probeMock.mockResolvedValueOnce(undefined)
    const { downloadFile, error } = useMediaList()

    await downloadFile(1)
    expect(error.value).toBe('媒体文件不可用（HTTP 502）')

    await downloadFile(2)

    expect(error.value).toBeNull()
    expect(clicked).toHaveLength(1)
  })
})

describe('downloadFiles', () => {
  it('全部可用时逐个触发，不报错', async () => {
    probeMock.mockResolvedValue(undefined)
    const { downloadFiles, error } = useMediaList()

    const pending = downloadFiles([1, 2, 3])
    await vi.advanceTimersByTimeAsync(1000)
    await pending

    expect(clicked).toHaveLength(3)
    expect(error.value).toBeNull()
  })

  it('部分不可用时跳过并统计失败数', async () => {
    probeMock.mockRejectedValueOnce(new Error('媒体文件不可用（HTTP 502）'))
    probeMock.mockResolvedValueOnce(undefined)
    const { downloadFiles, error } = useMediaList()

    const pending = downloadFiles([1, 2])
    await vi.advanceTimersByTimeAsync(1000)
    await pending

    expect(clicked).toHaveLength(1)
    expect(clicked[0]).toContain('/api/v1/media/2/content')
    expect(error.value).toBe('1 个文件不可用，已跳过下载')
  })

  it('每个文件之间错峰触发，避免被浏览器当作滥用', async () => {
    probeMock.mockResolvedValue(undefined)
    const { downloadFiles } = useMediaList()

    const pending = downloadFiles([1, 2])
    // 只推进微任务：第一个文件应已触发，第二个还卡在错峰等待里
    await vi.advanceTimersByTimeAsync(0)
    expect(clicked).toHaveLength(1)

    await vi.advanceTimersByTimeAsync(DOWNLOAD_INTERVAL_MS)
    await pending
    // 末尾不再空等一轮，两个文件推进一轮间隔即可全部触发
    expect(clicked).toHaveLength(2)
  })
})

describe('fetchFiles', () => {
  it('加载失败时写入错误提示', async () => {
    listMock.mockRejectedValue(new Error('fetchMediaList failed: 500'))
    const { fetchFiles, error } = useMediaList()

    await fetchFiles()

    expect(error.value).toBe('fetchMediaList failed: 500')
  })
})
