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
import FlightAreaCreateDialog from './FlightAreaCreateDialog.vue'
import type { DrawnGeometry, FlightAreaType } from '@/types/flightArea'

// mock authFetch，控制 createFlightArea 的成功/失败
vi.mock('@/api/auth', () => ({
  authFetch: vi.fn(),
}))

describe('FlightAreaCreateDialog', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const geometry: DrawnGeometry = { type: 'Circle', coordinates: [114.06, 22.54], radius: 500 }
  const type: FlightAreaType = 'nfz'

  it('名称为空时确认按钮禁用', () => {
    const wrapper = mount(FlightAreaCreateDialog, {
      props: { type, geometry },
    })
    const btn = wrapper.find('[data-testid="confirm-btn"]')
    expect((btn.element as HTMLButtonElement).disabled).toBe(true)
  })

  it('输入名称并提交：调用 store.addArea 且 emit created', async () => {
    const { authFetch } = await import('@/api/auth')
    ;(authFetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      json: async () => ({
        id: 'fa_9', name: '测试圆', type, status: 'enable',
        content: { type: 'Feature', properties: { color: '#FF0000' }, geometry },
        createdTime: 0, createdBy: 'system', updatedTime: 0, updatedBy: 'system',
      }),
    })
    const wrapper = mount(FlightAreaCreateDialog, { props: { type, geometry } })
    await wrapper.find('[data-testid="name-input"]').setValue('测试圆')
    await wrapper.find('[data-testid="confirm-btn"]').trigger('click')
    // 等待异步
    await Promise.resolve()
    await wrapper.vm.$nextTick()
    await Promise.resolve()
    await wrapper.vm.$nextTick()
    expect(authFetch).toHaveBeenCalled()
    expect(wrapper.emitted('created')).toBeTruthy()
  })

  it('提交失败：显示错误且不 emit created', async () => {
    const { authFetch } = await import('@/api/auth')
    ;(authFetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ error: '服务器错误' }),
    })
    const wrapper = mount(FlightAreaCreateDialog, { props: { type, geometry } })
    await wrapper.find('[data-testid="name-input"]').setValue('测试圆')
    await wrapper.find('[data-testid="confirm-btn"]').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('服务器错误')
    expect(wrapper.emitted('created')).toBeFalsy()
  })
})
