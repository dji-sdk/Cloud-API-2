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
import TaskTypeLabel from './TaskTypeLabel.vue'

/** 以指定 taskType 与电量阈值挂载标签组件。 */
function mountLabel(taskType: 1 | 2 | 3 | 4 | 5 | 6, minBatteryCapacity = 0) {
  return mount(TaskTypeLabel, { props: { taskType, minBatteryCapacity } })
}

describe('TaskTypeLabel', () => {
  it('shows 立即 for taskType=1 without a battery threshold', () => {
    expect(mountLabel(1).text()).toBe('立即')
  })

  it('shows 连续执行 for taskType=1 with a battery threshold', () => {
    // 平台把连续执行任务也记为 taskType=1，缺少这条分支时列表会误显示为"立即"
    const wrapper = mountLabel(1, 30)
    expect(wrapper.text()).toBe('连续执行')
    expect(wrapper.attributes('data-strategy')).toBe('continuous')
  })

  it('defaults minBatteryCapacity to 0 when the prop is omitted', () => {
    const wrapper = mount(TaskTypeLabel, { props: { taskType: 1 } })
    expect(wrapper.text()).toBe('立即')
  })

  it('shows 重复定时 for taskType=2', () => {
    expect(mountLabel(2).text()).toBe('重复定时')
  })

  it('shows 重复定时 for the repeat subtask type 4', () => {
    expect(mountLabel(4).text()).toBe('重复定时')
  })

  it('shows 单次定时 for taskType=3', () => {
    expect(mountLabel(3).text()).toBe('单次定时')
  })

  it('shows 连续执行 for taskType=5', () => {
    expect(mountLabel(5).text()).toBe('连续执行')
  })

  it('shows 连续执行 for the continuous subtask type 6', () => {
    expect(mountLabel(6).text()).toBe('连续执行')
  })

  it('exposes the resolved strategy via data-strategy', () => {
    expect(mountLabel(3).attributes('data-strategy')).toBe('once')
    expect(mountLabel(2).attributes('data-strategy')).toBe('repeat')
    expect(mountLabel(5).attributes('data-strategy')).toBe('continuous')
    expect(mountLabel(1).attributes('data-strategy')).toBe('immediate')
  })

  it('renders a strategy icon alongside the label', () => {
    expect(mountLabel(2).find('svg').exists()).toBe(true)
  })
})
