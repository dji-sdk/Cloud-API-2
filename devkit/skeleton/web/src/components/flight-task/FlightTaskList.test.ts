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

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import FlightTaskList from './FlightTaskList.vue'
import type { FlightTaskInfo, FlightTaskListResponse } from '@/types/flightTask'

// vi.mock 工厂会被提升到 import 之前执行，因此被引用的 mock 必须用 vi.hoisted 一并提升
const { mockPush, mockFetchFlightTaskList } = vi.hoisted(() => ({
  mockPush: vi.fn(),
  mockFetchFlightTaskList: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush }),
}))

vi.mock('@/api/flightTask', () => ({
  fetchFlightTaskList: (...args: unknown[]) => mockFetchFlightTaskList(...args),
  pauseFlightTask: vi.fn(),
  resumeFlightTask: vi.fn(),
  deleteFlightTask: vi.fn(),
}))

/** 构造飞行任务列表项，默认无冲突标识。 */
function buildTask(
  overrides: Partial<FlightTaskInfo> & Pick<FlightTaskInfo, 'taskUuid' | 'name'>,
): FlightTaskInfo {
  return {
    sn: 'SN001',
    status: 'CREATED',
    taskType: 2,
    waylineUuid: 'wl-001',
    createdAt: 1721030400,
    distanceMeters: 0,
    minBatteryCapacity: 0,
    ...overrides,
  }
}

/** 挂载列表组件并等待首屏请求（composable 的 immediate watch）返回。 */
async function mountList(tasks: FlightTaskInfo[]): Promise<VueWrapper> {
  const response: FlightTaskListResponse = {
    tasks,
    totalCount: tasks.length,
    hasMore: false,
  }
  mockFetchFlightTaskList.mockResolvedValue(response)

  const wrapper = mount(FlightTaskList)
  await flushPromises()
  return wrapper
}

describe('FlightTaskList 时间冲突标识', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders_shouldShowConflictBadge_whenTaskIsConflicted', async () => {
    const wrapper = await mountList([
      buildTask({ taskUuid: 'u1', name: '冲突任务', isConflicted: true }),
      buildTask({ taskUuid: 'u2', name: '正常任务' }),
    ])

    const badges = wrapper.findAll('.conflict-badge')
    expect(badges).toHaveLength(1)
    expect(badges[0].text()).toContain('时间冲突')

    // 标识必须挂在冲突任务所在行，不能串行
    const rows = wrapper.findAll('.task-row')
    expect(rows[0].find('.conflict-badge').exists()).toBe(true)
    expect(rows[0].text()).toContain('冲突任务')
    expect(rows[1].find('.conflict-badge').exists()).toBe(false)
  })

  it('renders_shouldNotShowConflictBadge_whenNoTaskIsConflicted', async () => {
    const wrapper = await mountList([
      buildTask({ taskUuid: 'u1', name: '正常任务' }),
      buildTask({ taskUuid: 'u2', name: '另一个正常任务' }),
    ])

    expect(wrapper.findAll('.conflict-badge')).toHaveLength(0)
    expect(wrapper.findAll('.task-row')).toHaveLength(2)
  })

  it('renders_shouldShowConflictBadgeForEach_whenMultipleTasksConflicted', async () => {
    const wrapper = await mountList([
      buildTask({ taskUuid: 'u1', name: '冲突任务甲', isConflicted: true }),
      buildTask({ taskUuid: 'u2', name: '正常任务' }),
      buildTask({ taskUuid: 'u3', name: '冲突任务乙', isConflicted: true }),
    ])

    expect(wrapper.findAll('.conflict-badge')).toHaveLength(2)
  })

  it('renders_shouldExplainConflict_whenBadgeHovered', async () => {
    const wrapper = await mountList([
      buildTask({ taskUuid: 'u1', name: '冲突任务', isConflicted: true }),
    ])

    const title = wrapper.find('.conflict-badge').attributes('title')
    expect(title).toContain('同一机场同一时间只能执行一个架次')
  })

  it('renders_shouldSupportTaskWithoutConflictField_whenFieldAbsent', async () => {
    // isConflicted 为可选字段，旧接口不返回该字段时不应报错或误标
    const wrapper = await mountList([buildTask({ taskUuid: 'u1', name: '旧接口任务' })])

    expect(wrapper.find('.conflict-badge').exists()).toBe(false)
    expect(wrapper.text()).toContain('旧接口任务')
  })
})
