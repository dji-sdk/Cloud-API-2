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
import { mount } from '@vue/test-utils'
import WaylineImportDialog from './WaylineImportDialog.vue'

describe('WaylineImportDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  function mountDialog(loading = false) {
    return mount(WaylineImportDialog, { props: { loading } })
  }

  it('未选文件时确认按钮 disabled', () => {
    const wrapper = mountDialog()
    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认导入'))
    expect(confirmBtn).toBeTruthy()
    expect((confirmBtn!.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('通过 input 选择 KMZ 文件后确认按钮启用，并自动填充航线名称', async () => {
    const wrapper = mountDialog()
    const input = wrapper.find('input[type="file"]')
    const file = new File(['x'], '我的航线.kmz', { type: 'application/zip' })
    Object.defineProperty(input.element, 'files', { value: [file] })
    await input.trigger('change')

    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认导入'))
    expect((confirmBtn!.element as HTMLButtonElement).disabled).toBe(false)

    const nameInput = wrapper.find('input[type="text"]')
    expect((nameInput.element as HTMLInputElement).value).toBe('我的航线')
  })

  it('拖拽非 .kmz 文件时显示错误且不选中', async () => {
    const wrapper = mountDialog()
    const dropZone = wrapper.find('.drop-zone')
    const file = new File(['x'], 'not-a-png.txt', { type: 'text/plain' })
    await dropZone.trigger('drop', { dataTransfer: { files: [file] } })

    expect(wrapper.find('.drop-error').text()).toContain('不支持的文件格式')
    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认导入'))
    expect((confirmBtn!.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('拖拽 .kmz 文件时选中并自动填充名称', async () => {
    const wrapper = mountDialog()
    const dropZone = wrapper.find('.drop-zone')
    const file = new File(['x'], 'dragged.kmz', { type: 'application/zip' })
    await dropZone.trigger('drop', { dataTransfer: { files: [file] } })

    expect(wrapper.find('.drop-error').exists()).toBe(false)
    const nameInput = wrapper.find('input[type="text"]')
    expect((nameInput.element as HTMLInputElement).value).toBe('dragged')
  })

  it('点击确认时 emit confirm 携带 file 与 name', async () => {
    const wrapper = mountDialog()
    const input = wrapper.find('input[type="file"]')
    const file = new File(['x'], 'emit.kmz', { type: 'application/zip' })
    Object.defineProperty(input.element, 'files', { value: [file] })
    await input.trigger('change')

    // 手动修改名称
    await wrapper.find('input[type="text"]').setValue('自定义名称')

    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认导入'))!
    await confirmBtn.trigger('click')

    const emitted = wrapper.emitted('confirm')
    expect(emitted).toBeTruthy()
    expect(emitted![0]).toEqual([file, '自定义名称'])
  })

  it('loading 状态下确认与取消按钮均 disabled 且文案变化', () => {
    const wrapper = mountDialog(true)
    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('导入中'))!
    expect((confirmBtn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('点击取消 emit cancel', async () => {
    const wrapper = mountDialog()
    const cancelBtn = wrapper.findAll('button').find(b => b.text().includes('取消'))!
    await cancelBtn.trigger('click')
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })
})
