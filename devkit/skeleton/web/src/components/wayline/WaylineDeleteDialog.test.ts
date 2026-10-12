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

import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import WaylineDeleteDialog from './WaylineDeleteDialog.vue'
import type { WaylineInfo } from '@/types/wayline'

function makeWayline(uuid: string, name = uuid): WaylineInfo {
  return {
    uuid,
    name,
    droneType: 'Matrice 350 RTK',
    payloadType: 'Zenmuse L2',
    waylineType: '航点航线',
    updatedAt: 1720000000,
  }
}

describe('WaylineDeleteDialog', () => {
  beforeEach(() => {
    // noop
  })

  function mountDialog(selected: WaylineInfo[], loading = false) {
    return mount(WaylineDeleteDialog, {
      props: { selectedWaylines: selected, loading },
    })
  }

  it('渲染待删除航线数量与名称列表', () => {
    const selected = [makeWayline('wl-1', '航线A'), makeWayline('wl-2', '航线B')]
    const wrapper = mountDialog(selected)
    expect(wrapper.find('.dialog-message').text()).toContain('2')
    const items = wrapper.findAll('.wayline-item')
    expect(items).toHaveLength(2)
    expect(items[0].text()).toBe('航线A')
    expect(items[1].text()).toBe('航线B')
  })

  it('点击确认 emit confirm', async () => {
    const wrapper = mountDialog([makeWayline('wl-1')])
    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('确认删除'))!
    await confirmBtn.trigger('click')
    expect(wrapper.emitted('confirm')).toBeTruthy()
  })

  it('点击取消 emit cancel', async () => {
    const wrapper = mountDialog([makeWayline('wl-1')])
    const cancelBtn = wrapper.findAll('button').find(b => b.text().includes('取消'))!
    await cancelBtn.trigger('click')
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })

  it('loading 状态下确认按钮文案变为「删除中…」且 disabled', () => {
    const wrapper = mountDialog([makeWayline('wl-1')], true)
    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('删除中'))!
    expect((confirmBtn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('点击遮罩层（非卡片）emit cancel', async () => {
    const wrapper = mountDialog([makeWayline('wl-1')])
    await wrapper.find('.dialog-overlay').trigger('click')
    expect(wrapper.emitted('cancel')).toBeTruthy()
  })
})
