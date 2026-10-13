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
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import WaylineLibrary from './WaylineLibrary.vue'

// mock api：避免真实网络，同时验证 Mock 模式下的调用契约
const mockFetchWaylineList = vi.fn()
const mockDeleteWaylines = vi.fn(async () => {})
const mockDownloadWaylines = vi.fn(async () => {})
const mockImportWayline = vi.fn(async () => 'wl-import-mock')

vi.mock('@/api/wayline', () => ({
  fetchWaylineList: (...args: unknown[]) => mockFetchWaylineList(...args),
  deleteWaylines: (...args: unknown[]) => mockDeleteWaylines(...args),
  downloadWaylines: (...args: unknown[]) => mockDownloadWaylines(...args),
  importWayline: (...args: unknown[]) => mockImportWayline(...args),
}))

function makeWayline(uuid: string, name = uuid) {
  return {
    uuid,
    name,
    droneType: 'Matrice 350 RTK',
    payloadType: 'Zenmuse L2',
    waylineType: '航点航线',
    waypointCount: 10,
    estimatedDistance: 1000,
    updatedAt: 1720000000,
  }
}

async function mountLibrary(waylines: ReturnType<typeof makeWayline>[]) {
  mockFetchWaylineList.mockResolvedValue({
    waylines,
    total: waylines.length,
    page: 1,
    pageSize: 20,
  })
  const wrapper = mount(WaylineLibrary, {
    global: { plugins: [] },
  })
  // filters watch immediate → 触发首次 fetchWaylines；等待其 resolve
  await flushPromises()
  return wrapper
}

describe('WaylineLibrary 导入/导出/删除', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('未选中时导出/删除按钮为 disabled', async () => {
    const wrapper = await mountLibrary([makeWayline('wl-1'), makeWayline('wl-2')])

    const exportBtn = wrapper.find('button.action-btn--ghost:not(.action-btn--danger)')
    // 工具栏第二个 ghost 按钮是导出，最后一个是删除（danger）
    const buttons = wrapper.findAll('.toolbar-actions .action-btn')
    // 创建(0) 导入(1) 导出(2) 拼接(3) 删除(4)
    const exportBtnEl = buttons[2].element as HTMLButtonElement
    const deleteBtnEl = buttons[4].element as HTMLButtonElement
    expect(exportBtnEl.disabled).toBe(true)
    expect(deleteBtnEl.disabled).toBe(true)
  })

  it('选中航线后导出/删除按钮启用，且导出触发下载', async () => {
    const wrapper = await mountLibrary([makeWayline('wl-1'), makeWayline('wl-2')])

    // 勾选第一行
    const rowCheckbox = wrapper.findAll('.wayline-table tbody input[type="checkbox"]')[0]
    await rowCheckbox.setValue(true)

    const buttons = wrapper.findAll('.toolbar-actions .action-btn')
    const exportBtnEl = buttons[2].element as HTMLButtonElement
    const deleteBtnEl = buttons[4].element as HTMLButtonElement
    expect(exportBtnEl.disabled).toBe(false)
    expect(deleteBtnEl.disabled).toBe(false)

    // 点击导出
    await buttons[2].trigger('click')
    await flushPromises()
    expect(mockDownloadWaylines).toHaveBeenCalledWith(['wl-1'])
  })

  it('删除流程：弹框确认 → 调用 deleteWaylines → 列表刷新', async () => {
    const wrapper = await mountLibrary([makeWayline('wl-1'), makeWayline('wl-2')])

    // 勾选第一行
    await wrapper.findAll('.wayline-table tbody input[type="checkbox"]')[0].setValue(true)

    const buttons = wrapper.findAll('.toolbar-actions .action-btn')
    // 点击删除按钮
    await buttons[4].trigger('click')
    await flushPromises()

    // 删除确认弹框出现
    const deleteDialog = wrapper.findComponent({ name: 'WaylineDeleteDialog' })
    expect(deleteDialog.exists()).toBe(true)

    // 触发 confirm
    await deleteDialog.vm.$emit('confirm')
    await flushPromises()

    expect(mockDeleteWaylines).toHaveBeenCalledWith(['wl-1'])
    // 删除后应重新拉取列表
    expect(mockFetchWaylineList).toHaveBeenCalled()
    // 弹框关闭
    expect(wrapper.findComponent({ name: 'WaylineDeleteDialog' }).exists()).toBe(false)
  })

  it('导入流程：弹框确认 → 调用 importWayline → 列表刷新', async () => {
    const wrapper = await mountLibrary([makeWayline('wl-1')])

    // 点击导入按钮（工具栏第二个按钮）
    const buttons = wrapper.findAll('.toolbar-actions .action-btn')
    await buttons[1].trigger('click')
    await flushPromises()

    const importDialog = wrapper.findComponent({ name: 'WaylineImportDialog' })
    expect(importDialog.exists()).toBe(true)

    const file = new File(['x'], '新航线.kmz', { type: 'application/zip' })
    await importDialog.vm.$emit('confirm', file, '新航线名称')
    await flushPromises()

    expect(mockImportWayline).toHaveBeenCalledWith(file, '新航线名称')
    // 导入后刷新列表
    expect(mockFetchWaylineList).toHaveBeenCalled()
    expect(wrapper.findComponent({ name: 'WaylineImportDialog' }).exists()).toBe(false)
  })

  it('删除失败时显示错误横幅且不关闭弹框', async () => {
    mockDeleteWaylines.mockRejectedValueOnce(new Error('删除航线失败'))
    const wrapper = await mountLibrary([makeWayline('wl-1')])

    await wrapper.findAll('.wayline-table tbody input[type="checkbox"]')[0].setValue(true)
    const buttons = wrapper.findAll('.toolbar-actions .action-btn')
    await buttons[4].trigger('click')
    await flushPromises()

    const deleteDialog = wrapper.findComponent({ name: 'WaylineDeleteDialog' })
    await deleteDialog.vm.$emit('confirm')
    await flushPromises()

    expect(mockDeleteWaylines).toHaveBeenCalled()
    // 弹框仍保留（失败不关闭）
    expect(wrapper.findComponent({ name: 'WaylineDeleteDialog' }).exists()).toBe(true)
    // 错误横幅显示
    const banners = wrapper.findAll('.error-banner')
    const hasDeleteError = banners.some(b => b.text().includes('删除航线失败'))
    expect(hasDeleteError).toBe(true)
  })
})
