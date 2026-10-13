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
import type { TaskStrategy } from '@/types/flightTask'
import StrategySection from './StrategySection.vue'

/** StrategySection 的全部 props，测试里只覆盖关心的字段。 */
interface MountOptions {
  strategy?: TaskStrategy
  beginAt?: number | null
  endAt?: number | null
  repeatType?: 'daily' | 'weekly' | 'monthly' | null
  repeatInterval?: number | null
  minBatteryCapacity?: number | null
  isEdit?: boolean
}

/** 以指定策略挂载 StrategySection。 */
function mountSection(options: MountOptions = {}) {
  return mount(StrategySection, {
    props: {
      precision: 'rtk',
      strategy: options.strategy ?? 'immediate',
      beginAt: options.beginAt ?? null,
      endAt: options.endAt ?? null,
      repeatType: options.repeatType ?? null,
      repeatInterval: options.repeatInterval ?? null,
      minBatteryCapacity: options.minBatteryCapacity ?? null,
      isEdit: options.isEdit ?? false,
    },
  })
}

/** 任务策略分段控件是页内第二个（第一个是任务精度）。 */
function strategyButtons(wrapper: ReturnType<typeof mountSection>) {
  return wrapper.findAll('.segmented-control')[1].findAll('button')
}

/** 按可见文案取策略按钮。 */
function strategyButton(wrapper: ReturnType<typeof mountSection>, label: string) {
  const button = strategyButtons(wrapper).find((b) => b.text() === label)
  if (!button) {
    throw new Error(`未找到策略按钮：${label}`)
  }
  return button
}

describe('StrategySection', () => {
  it('灰掉 PaaS 通道无法承载的重复定时与连续执行', () => {
    // 这两个策略提交后字段会在 BFF 层被丢弃，服务端按「条件任务」处理，
    // 展开重复调度的分支被注释掉，任务退化成只执行一次
    const wrapper = mountSection()
    expect(strategyButton(wrapper, '重复定时').attributes('disabled')).toBeDefined()
    expect(strategyButton(wrapper, '连续执行').attributes('disabled')).toBeDefined()
  })

  it('保留立即与单次定时可选', () => {
    const wrapper = mountSection()
    expect(strategyButton(wrapper, '立即').attributes('disabled')).toBeUndefined()
    expect(strategyButton(wrapper, '单次定时').attributes('disabled')).toBeUndefined()
  })

  it('灰掉的按钮不派发 update:strategy', async () => {
    const wrapper = mountSection()
    await strategyButton(wrapper, '重复定时').trigger('click')
    await strategyButton(wrapper, '连续执行').trigger('click')
    expect(wrapper.emitted('update:strategy')).toBeUndefined()
  })

  it('可选按钮正常派发 update:strategy', async () => {
    const wrapper = mountSection()
    await strategyButton(wrapper, '单次定时').trigger('click')
    expect(wrapper.emitted('update:strategy')).toEqual([['once']])
  })

  it('在按钮上给出灰掉的原因提示', () => {
    const wrapper = mountSection()
    expect(strategyButton(wrapper, '重复定时').attributes('title')).toBe(
      '当前平台协议暂不支持该选项',
    )
    expect(strategyButton(wrapper, '立即').attributes('title')).toBeUndefined()
  })

  it('编辑历史重复任务时，重复定时仍显示为选中态', () => {
    // 选项被过滤掉的话，编辑页会呈现「没有选中任何策略」且点击即静默改策略
    const wrapper = mountSection({ strategy: 'repeat' })
    const repeatButton = strategyButton(wrapper, '重复定时')
    expect(repeatButton.classes()).toContain('active')
    expect(repeatButton.classes()).toContain('disabled')
  })

  it('编辑历史连续执行任务时，连续执行仍显示为选中态', () => {
    const wrapper = mountSection({ strategy: 'continuous', minBatteryCapacity: 30 })
    expect(strategyButton(wrapper, '连续执行').classes()).toContain('active')
  })

  it('编辑无承载位的策略时，提示保存后的后果', () => {
    // 置灰挡不住「带着历史值直接保存」：用户不动策略直接提交，请求仍是 task_type=2/5
    const wrapper = mountSection({ strategy: 'repeat', isEdit: true })
    expect(wrapper.find('.field-warning').text()).toBe(
      '当前平台协议无法保留该任务策略，保存后重复规则将丢失，任务只会执行一次',
    )
  })

  it('新建时不提示后果，只由置灰本身表达不可选', () => {
    // 新建态灰掉的按钮点不动，用户只会落到可选策略上，无需后果提示
    const wrapper = mountSection({ strategy: 'repeat' })
    expect(wrapper.find('.field-warning').exists()).toBe(false)
  })

  it('编辑可选策略时不提示后果', () => {
    const wrapper = mountSection({ strategy: 'once', isEdit: true, beginAt: 4102444800 })
    expect(wrapper.find('.field-warning').exists()).toBe(false)
  })

  it('选中历史重复任务时仍渲染其时间与重复周期配置', () => {
    const wrapper = mountSection({
      strategy: 'repeat',
      beginAt: 4102444800,
      endAt: 4102531200,
      repeatType: 'weekly',
      repeatInterval: 2,
    })
    expect(wrapper.text()).toContain('开始时间')
    expect(wrapper.text()).toContain('截止时间')
    expect(wrapper.text()).toContain('重复周期')
    expect(wrapper.find('input.interval-num').attributes('value')).toBe('2')
  })

  it('未选择策略时不渲染任何策略专属配置', () => {
    const wrapper = mountSection({ strategy: 'immediate' })
    expect(wrapper.text()).not.toContain('执行时间')
    expect(wrapper.text()).not.toContain('最低执行电量')
  })
})
