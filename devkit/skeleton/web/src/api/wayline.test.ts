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

// 控制 Mock 开关：默认开启，验证 Mock 模式逻辑
vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: (key: string, fallback = '') => {
    if (key === 'MOCK_ENABLED') return mockEnabled
    return fallback
  },
}))

// 让 a.click() 在 jsdom 下不抛 "not implemented"
let mockEnabled = 'true'

/**
 * wayline API 的 Mock 模式单测。
 *
 * Mock 模式下不发起网络请求，操作在内存的 MOCK_WAYLINES 上进行：
 * - 导入 → unshift 新航线
 * - 删除 → 过滤掉选中 uuid
 * - 导出 → 触发浏览器下载（a.click）
 *
 * 由于 MOCK_WAYLINES 是模块级可变状态，每个用例前需 vi.resetModules()
 * 重新加载模块以获得干净初始数据。
 */
describe('wayline api (mock mode)', () => {
  let clickSpy: ReturnType<typeof vi.fn>

  beforeEach(async () => {
    mockEnabled = 'true'
    vi.resetModules()

    clickSpy = vi.fn()
    // jsdom 的 HTMLAnchorElement.click 未实现，stub 成空函数。
    // 注意：不可用 vi.stubGlobal('URL', {...}) 整体替换 URL —— vitest 5 的模块
    // 加载器在动态 import 时会 new URL()，整体替换会导致所有用例挂掉，
    // 因此只 spyOn 两个静态方法。
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:mock')
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})
    const OrigAnchor = window.HTMLAnchorElement
    vi.spyOn(OrigAnchor.prototype, 'click').mockImplementation(clickSpy)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  async function loadApi() {
    return (await import('@/api/wayline'))
  }

  it('导入航线：列表刷新出现新航线（新条目置顶）', async () => {
    const api = await loadApi()
    const before = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    const beforeTotal = before.total
    const beforeFirst = before.waylines[0]?.uuid

    const file = new File(['dummy-kmz'], '新航线.kmz', { type: 'application/zip' })
    const newUuid = await api.importWayline(file, '导入测试航线')

    const after = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    expect(newUuid).toBeTruthy()
    expect(after.total).toBe(beforeTotal + 1)
    // 新航线应置顶
    expect(after.waylines[0].uuid).toBe(newUuid)
    expect(after.waylines[0].name).toBe('导入测试航线')
    // 原首条被挤到第二位
    expect(after.waylines[1].uuid).toBe(beforeFirst)
  })

  it('导入航线：未提供名称时回退到文件名（去 .kmz 后缀）', async () => {
    const api = await loadApi()
    const file = new File(['x'], '我的航线.KMZ', { type: 'application/zip' })
    const uuid = await api.importWayline(file, '')
    const list = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    const created = list.waylines.find(w => w.uuid === uuid)
    expect(created?.name).toBe('我的航线')
  })

  it('导入航线：超过 100MB 的文件抛错且不写入列表', async () => {
    const api = await loadApi()
    const before = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    const huge = new File([new Uint8Array(0)], 'big.kmz')
    Object.defineProperty(huge, 'size', { value: 101 * 1024 * 1024 })

    await expect(api.importWayline(huge, '超大文件')).rejects.toThrow(/文件大小超过限制/)
    const after = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    expect(after.total).toBe(before.total)
  })

  it('删除航线：勾选条目删除后列表移除且 total 减少', async () => {
    const api = await loadApi()
    const before = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    const target = before.waylines[0]
    const target2 = before.waylines[1]

    await api.deleteWaylines([target.uuid, target2.uuid])

    const after = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    expect(after.total).toBe(before.total - 2)
    expect(after.waylines.find(w => w.uuid === target.uuid)).toBeUndefined()
    expect(after.waylines.find(w => w.uuid === target2.uuid)).toBeUndefined()
  })

  it('删除航线：传入空数组不改变列表', async () => {
    const api = await loadApi()
    const before = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    await api.deleteWaylines([])
    const after = await api.fetchWaylineList({ page: 1, pageSize: 50 })
    expect(after.total).toBe(before.total)
  })

  it('导出航线：单条触发 .kmz 文件下载', async () => {
    const api = await loadApi()
    const list = await api.fetchWaylineList({ page: 1, pageSize: 10 })
    const uuid = list.waylines[0].uuid

    await api.downloadWaylines([uuid])

    expect(clickSpy).toHaveBeenCalledTimes(1)
    // 通过 createObjectURL 创建的 <a> 由 spy 捕获 click，校验下载文件名
    const anchor = (clickSpy.mock.instances[0] as HTMLAnchorElement)
    expect(anchor.download).toBe(`${uuid}.kmz`)
  })

  it('导出航线：多条触发 zip 打包下载', async () => {
    const api = await loadApi()
    const list = await api.fetchWaylineList({ page: 1, pageSize: 10 })
    const uuids = list.waylines.slice(0, 3).map(w => w.uuid)

    await api.downloadWaylines(uuids)

    expect(clickSpy).toHaveBeenCalledTimes(1)
    const anchor = (clickSpy.mock.instances[0] as HTMLAnchorElement)
    expect(anchor.download).toBe('waylines_export.zip')
  })

  it('导出航线：空数组仍生成空 zip 下载（当前实现不拦截空入参）', async () => {
    const api = await loadApi()
    await api.downloadWaylines([])
    // 实现未对空入参短路，仍会下载一个空 waylines_export.zip
    expect(clickSpy).toHaveBeenCalledTimes(1)
    const anchor = (clickSpy.mock.instances[0] as HTMLAnchorElement)
    expect(anchor.download).toBe('waylines_export.zip')
  })

  it('Mock 关闭时走 fetch 真实分支（删除调用 batch_delete）', async () => {
    mockEnabled = 'false'
    vi.resetModules()
    const fetchSpy = vi.fn().mockResolvedValue(new Response(null, { status: 200 }))
    vi.stubGlobal('fetch', fetchSpy)
    const api = await loadApi()

    await api.deleteWaylines(['wl-x'])

    expect(fetchSpy).toHaveBeenCalledTimes(1)
    const [url, init] = fetchSpy.mock.calls[0]
    expect(String(url)).toContain('/api/v1/workspace/waylines/batch_delete')
    expect((init as RequestInit).method).toBe('POST')
  })

  /**
   * 拼接落库校验。
   *
   * concatWayline 返回的是 SDK 内部句柄而非落库 uuid，且平台侧列表接口的
   * 名称过滤不生效，故校验方式为「拉首页 + 前端精确比对名称」。
   */
  describe('verifyWaylinePersisted', () => {
    /** 构造一页列表响应（结构须与 WaylineListResponse 一致）。 */
    function listResponse(names: string[]): Response {
      const waylines = names.map((n, i) => ({
        uuid: `uuid-${i}`,
        name: n,
        droneType: 'M350',
        payloadType: 'L2',
        waylineType: '航点航线',
        updatedAt: 1788232586,
      }))
      return new Response(
        JSON.stringify({ waylines, total: waylines.length, page: 1, pageSize: 30 }),
        { status: 200, headers: { 'content-type': 'application/json' } },
      )
    }

    beforeEach(() => {
      mockEnabled = 'false'
      vi.resetModules()
      vi.useFakeTimers()
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('首次查询即命中名称时返回 true，且只请求一次', async () => {
      const fetchSpy = vi.fn().mockResolvedValue(listResponse(['其他航线', '航线拼接911116']))
      vi.stubGlobal('fetch', fetchSpy)
      const api = await loadApi()

      const result = await api.verifyWaylinePersisted('航线拼接911116')

      expect(result).toBe(true)
      expect(fetchSpy).toHaveBeenCalledTimes(1)
    })

    it('名称始终查不到时重试耗尽并返回 false', async () => {
      const fetchSpy = vi.fn().mockResolvedValue(listResponse(['不相干航线']))
      vi.stubGlobal('fetch', fetchSpy)
      const api = await loadApi()

      const promise = api.verifyWaylinePersisted('航线拼接911116')
      await vi.runAllTimersAsync()

      expect(await promise).toBe(false)
      expect(fetchSpy).toHaveBeenCalledTimes(3)
    })

    it('落库存在延迟时，重试后命中返回 true', async () => {
      const fetchSpy = vi
        .fn()
        .mockResolvedValueOnce(listResponse(['不相干航线']))
        .mockResolvedValueOnce(listResponse(['不相干航线', '航线拼接911116']))
      vi.stubGlobal('fetch', fetchSpy)
      const api = await loadApi()

      const promise = api.verifyWaylinePersisted('航线拼接911116')
      await vi.runAllTimersAsync()

      expect(await promise).toBe(true)
      expect(fetchSpy).toHaveBeenCalledTimes(2)
    })

    it('单次查询抛错不终止重试，后续命中仍返回 true', async () => {
      const fetchSpy = vi
        .fn()
        .mockRejectedValueOnce(new Error('network down'))
        .mockResolvedValueOnce(listResponse(['航线拼接911116']))
      vi.stubGlobal('fetch', fetchSpy)
      const api = await loadApi()

      const promise = api.verifyWaylinePersisted('航线拼接911116')
      await vi.runAllTimersAsync()

      expect(await promise).toBe(true)
    })

    it('名称精确比对，不匹配同前缀的其他航线', async () => {
      const fetchSpy = vi.fn().mockResolvedValue(listResponse(['航线拼接911116-副本']))
      vi.stubGlobal('fetch', fetchSpy)
      const api = await loadApi()

      const promise = api.verifyWaylinePersisted('航线拼接911116')
      await vi.runAllTimersAsync()

      expect(await promise).toBe(false)
    })
  })
})
