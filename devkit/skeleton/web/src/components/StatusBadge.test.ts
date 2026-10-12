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

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusBadge from './StatusBadge.vue'

describe('StatusBadge', () => {
  it('shows "在线" when online', () => {
    const wrapper = mount(StatusBadge, { props: { online: true } })
    expect(wrapper.text()).toBe('在线')
  })

  it('shows "离线" when offline', () => {
    const wrapper = mount(StatusBadge, { props: { online: false } })
    expect(wrapper.text()).toBe('离线')
  })

  it('has status-badge--online class when online', () => {
    const wrapper = mount(StatusBadge, { props: { online: true } })
    expect(wrapper.classes()).toContain('status-badge--online')
  })

  it('has status-badge--offline class when offline', () => {
    const wrapper = mount(StatusBadge, { props: { online: false } })
    expect(wrapper.classes()).toContain('status-badge--offline')
  })
})
