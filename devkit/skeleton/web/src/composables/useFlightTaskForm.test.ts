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
import type { FlightTaskDetail, FlightTaskFormData } from '@/types/flightTask'

// 只桩掉发起网络请求的两个命令接口，保留真实的 FlightTaskApiError
// 与 isTaskNameDuplicateError，确保「错误码 → 字段槽」的分流逻辑经过真实判定。
vi.mock('@/api/flightTask', async importOriginal => {
  const actual = await importOriginal<typeof import('@/api/flightTask')>()
  return {
    ...actual,
    createFlightTask: vi.fn(),
    editFlightTask: vi.fn(),
  }
})

import { FlightTaskApiError } from '@/api/flightTask'

/** 重名错误码，与 BFF FlightTaskRestController 约定一致。 */
const ERROR_CODE_DUPLICATE = 'TASK_NAME_DUPLICATE'
const DUPLICATE_MESSAGE = '任务名称「巡检任务A」已存在，请更换名称'

/** useFlightTaskForm 的返回类型，供内部辅助函数标注参数类型。 */
type FlightTaskFormReturn = Awaited<ReturnType<typeof import('./useFlightTaskForm').useFlightTaskForm>>

/** 测试用合成设备 SN；真实 SN 不得落库到代码或测试中。 */
const MOCK_DEVICE_SN = 'MOCK_DRONE_0001'

/** 填写一份可通过校验的表单（strategy 取默认的 immediate，无需时间字段）。 */
function fillValidForm(form: FlightTaskFormReturn['form']): void {
  form.name = '巡检任务A'
  form.waylineUuid = 'wl-001'
  form.deviceSn = MOCK_DEVICE_SN
}

/** 构造一份编辑预填用的任务详情。 */
function buildDetail(overrides: Partial<FlightTaskDetail> = {}): FlightTaskDetail {
  return {
    taskUuid: 'task-001',
    name: '巡检任务A',
    sn: MOCK_DEVICE_SN,
    deviceName: null,
    status: 'CREATED',
    taskType: 1,
    waylineUuid: 'wl-001',
    waylineName: null,
    beginAt: 0,
    endAt: 0,
    rthAltitude: 100,
    rthMode: 0,
    outOfControlActionInFlight: 1,
    waylinePrecisionType: 0,
    minBatteryCapacity: 0,
    isBreakPointResume: false,
    ...overrides,
  }
}

describe('useFlightTaskForm', () => {
  let api: typeof import('@/api/flightTask')
  let useForm: typeof import('./useFlightTaskForm').useFlightTaskForm

  beforeEach(async () => {
    vi.resetModules()
    api = await import('@/api/flightTask')
    useForm = (await import('./useFlightTaskForm')).useFlightTaskForm
    vi.mocked(api.createFlightTask).mockReset()
    vi.mocked(api.editFlightTask).mockReset()
  })

  describe('submitForm / 校验', () => {
    it('submitForm_shouldPutErrorOnNameField_whenTaskNameDuplicate', async () => {
      vi.mocked(api.createFlightTask).mockImplementation(() =>
        Promise.reject(new FlightTaskApiError(DUPLICATE_MESSAGE, ERROR_CODE_DUPLICATE)),
      )
      const { form, errors, submitForm } = useForm()
      fillValidForm(form)

      const result = await submitForm()

      expect(result).toBeNull()
      // 重名是名称字段的问题，错误必须落到名称输入框下方而非通用横幅
      expect(errors.name).toBe(DUPLICATE_MESSAGE)
      expect(errors.general).toBeUndefined()
    })

    it('submitForm_shouldPutErrorOnGeneralField_whenNonDuplicateApiError', async () => {
      vi.mocked(api.createFlightTask).mockImplementation(() =>
        Promise.reject(new FlightTaskApiError('创建飞行任务失败 (HTTP 500)')),
      )
      const { form, errors, submitForm } = useForm()
      fillValidForm(form)

      const result = await submitForm()

      expect(result).toBeNull()
      expect(errors.general).toBe('创建飞行任务失败 (HTTP 500)')
      expect(errors.name).toBeUndefined()
    })

    it('submitForm_shouldPutErrorOnNameField_whenEditingToDuplicatedName', async () => {
      vi.mocked(api.editFlightTask).mockImplementation(() =>
        Promise.reject(new FlightTaskApiError(DUPLICATE_MESSAGE, ERROR_CODE_DUPLICATE)),
      )
      const { form, errors, submitForm, applyDetail } = useForm()
      applyDetail(buildDetail())
      form.name = '重名任务'

      const result = await submitForm()

      expect(result).toBeNull()
      expect(errors.name).toBe(DUPLICATE_MESSAGE)
      expect(errors.general).toBeUndefined()
      expect(api.editFlightTask).toHaveBeenCalledWith('task-001', expect.objectContaining({ name: '重名任务' }))
    })

    it('submitForm_shouldReturnTaskUuid_whenCreatingSuccessfully', async () => {
      vi.mocked(api.createFlightTask).mockResolvedValue({ taskUuid: 'task-new' })
      const { form, submitForm } = useForm()
      fillValidForm(form)

      await expect(submitForm()).resolves.toBe('task-new')
    })

    it('submitForm_shouldReturnEditTaskUuid_whenEditingWithoutRenaming', async () => {
      vi.mocked(api.editFlightTask).mockResolvedValue(undefined)
      const { submitForm, applyDetail } = useForm()
      applyDetail(buildDetail())

      await expect(submitForm()).resolves.toBe('task-001')
      // 编辑沿用原名不应被判为重名：excludeTaskUuid 由 API 层负责，此处仅确认走 edit 分支
      expect(api.editFlightTask).toHaveBeenCalledWith('task-001', expect.objectContaining({ name: '巡检任务A' }))
    })

    it('submitForm_shouldSkipApiCall_whenValidationFails', async () => {
      const { form, errors, submitForm } = useForm()
      form.name = '   '

      const result = await submitForm()

      expect(result).toBeNull()
      expect(errors.name).toBe('请输入计划名称')
      expect(api.createFlightTask).not.toHaveBeenCalled()
    })

    it('submitForm_shouldClearPreviousNameError_whenResubmittingAfterFix', async () => {
      vi.mocked(api.createFlightTask).mockImplementationOnce(() =>
        Promise.reject(new FlightTaskApiError(DUPLICATE_MESSAGE, ERROR_CODE_DUPLICATE)),
      )
      const { form, errors, submitForm } = useForm()
      fillValidForm(form)

      await submitForm()
      expect(errors.name).toBe(DUPLICATE_MESSAGE)

      // 改名后重新提交应清掉上一次的重名提示
      vi.mocked(api.createFlightTask).mockResolvedValue({ taskUuid: 'task-new' })
      form.name = '巡检任务B'
      await submitForm()

      expect(errors.name).toBeUndefined()
      expect(errors.general).toBeUndefined()
    })

    it('resetForm_shouldClearNameError_whenCalledAfterDuplicate', async () => {
      vi.mocked(api.createFlightTask).mockImplementation(() =>
        Promise.reject(new FlightTaskApiError(DUPLICATE_MESSAGE, ERROR_CODE_DUPLICATE)),
      )
      const { form, errors, submitForm, resetForm } = useForm()
      fillValidForm(form)
      await submitForm()

      resetForm()

      expect(errors.name).toBeUndefined()
      expect(errors.general).toBeUndefined()
      expect(form.name).toBe('')
    })

    it('isTaskNameDuplicateError_shouldMatchByCode_whenErrorIsPlainObject', () => {
      // 跨模块/序列化后错误对象可能不是 FlightTaskApiError 实例，判定必须只依赖 code 字段
      expect(api.isTaskNameDuplicateError({ code: ERROR_CODE_DUPLICATE })).toBe(true)
      expect(api.isTaskNameDuplicateError(new FlightTaskApiError('其它错误', 'OTHER'))).toBe(false)
      expect(api.isTaskNameDuplicateError(new Error('网络异常'))).toBe(false)
      expect(api.isTaskNameDuplicateError(null)).toBe(false)
    })

    it('clearFieldError_shouldClearNameError_whenUserEditsNameField', async () => {
      vi.mocked(api.createFlightTask).mockImplementation(() =>
        Promise.reject(new FlightTaskApiError(DUPLICATE_MESSAGE, ERROR_CODE_DUPLICATE)),
      )
      const { form, errors, submitForm, clearFieldError } = useForm()
      fillValidForm(form)
      await submitForm()
      expect(errors.name).toBe(DUPLICATE_MESSAGE)

      // 用户在名称输入框改字后，残留的重名提示必须立即消失，而不是等到下次提交
      clearFieldError('name')

      expect(errors.name).toBeUndefined()
    })

    it('clearFieldError_shouldKeepOtherFieldErrors_whenClearingName', async () => {
      vi.mocked(api.createFlightTask).mockImplementation(() =>
        Promise.reject(new FlightTaskApiError('创建飞行任务失败 (HTTP 500)')),
      )
      const { form, errors, submitForm, clearFieldError } = useForm()
      fillValidForm(form)
      await submitForm()
      expect(errors.general).toBe('创建飞行任务失败 (HTTP 500)')

      // 只清名称字段，非名称字段的错误与通用横幅需保留
      clearFieldError('name')

      expect(errors.general).toBe('创建飞行任务失败 (HTTP 500)')
    })

    it('validateField_shouldMeasureTrimmedLength_whenNameHasSurroundingSpaces', () => {
      const { form, validateField } = useForm()
      form.name = `  ${'a'.repeat(50)}  `

      // 提交的是 trim 后的值，长度校验必须用同一口径，否则合法名称被误判超长
      expect(validateField('name')).toBe(true)

      form.name = `  ${'a'.repeat(51)}  `
      expect(validateField('name')).toBe(false)
    })
  })

  describe('applyDetail', () => {
    /** 详情接口返回的基准数据（已落库的重复定时任务） */
    const DETAIL_BASE: FlightTaskDetail = {
      taskUuid: 'task-uuid-001',
      name: '厂区北侧巡检',
      sn: MOCK_DEVICE_SN,
      deviceName: null,
      status: 'CREATED',
      taskType: 2,
      waylineUuid: 'wl-001',
      waylineName: null,
      beginAt: 1800000000,
      endAt: 1800172800,
      rthAltitude: 120,
      rthMode: 0,
      outOfControlActionInFlight: 1,
      waylinePrecisionType: 1,
      minBatteryCapacity: 0,
      isBreakPointResume: false,
    }

    /** 构造任务详情，覆盖默认值。 */
    function makeDetail(overrides: Partial<FlightTaskDetail>): FlightTaskDetail {
      return { ...DETAIL_BASE, ...overrides }
    }

    /**
     * 把表单置为「上一次编辑遗留」的脏状态。
     *
     * 详情接口不返回 planType / vlmEnabled / repeatType / repeatInterval /
     * continuousPeriods，回填若沿用当前 reactive 状态，这些值会被静默带进本次提交。
     */
    function dirtyForm(form: FlightTaskFormReturn['form']): void {
      form.planType = 'leapfrog'
      form.vlmEnabled = true
      form.repeatType = 'weekly'
      form.repeatInterval = 3
      form.continuousPeriods = [[0, 3600]]
    }

    it('applyDetail：应把详情接口不提供的字段复位，避免跨任务陈旧值污染提交', () => {
      const { form, applyDetail } = useForm()
      dirtyForm(form)

      // taskType=1 且无电量阈值 → 立即执行
      applyDetail(makeDetail({ taskType: 1, minBatteryCapacity: 0, beginAt: 0, endAt: 0 }))

      expect(form.planType).toBe('normal')
      expect(form.vlmEnabled).toBe(false)
      expect(form.repeatType).toBeNull()
      expect(form.repeatInterval).toBeNull()
      expect(form.continuousPeriods).toBeNull()
      expect(form.strategy).toBe('immediate')
    })

    it('applyDetail：重复定时应显式回填 repeatType=daily / repeatInterval=1，与界面展示保持一致', () => {
      const { form, applyDetail } = useForm()
      dirtyForm(form)

      applyDetail(makeDetail({ taskType: 2, beginAt: 1800000000, endAt: 1800172800 }))

      expect(form.strategy).toBe('repeat')
      expect(form.beginAt).toBe(1800000000)
      // 详情接口的 endAt 是平台推导出的单次执行窗口，非用户设置的重复规则截止时间，
      // 不预填，交由用户在编辑页重新确认
      expect(form.endAt).toBeNull()
      // 详情接口不返回周期配置，只能取保守默认值；必须显式写入，
      // 否则 StrategySection 显示「每天 / 1」而实际提交不含 interval
      expect(form.repeatType).toBe('daily')
      expect(form.repeatInterval).toBe(1)
    })

    it('applyDetail：截止时间等于开始时间时按单次定时处理，不残留周期配置', () => {
      const { form, applyDetail } = useForm()
      dirtyForm(form)

      applyDetail(makeDetail({ taskType: 2, beginAt: 1800000000, endAt: 1800000000 }))

      expect(form.strategy).toBe('once')
      expect(form.endAt).toBeNull()
      expect(form.repeatType).toBeNull()
      expect(form.repeatInterval).toBeNull()
    })

    it('applyDetail：无起止时间时按单次定时处理，不残留周期配置', () => {
      const { form, applyDetail } = useForm()
      dirtyForm(form)

      applyDetail(makeDetail({ taskType: 2, beginAt: 0, endAt: 0 }))

      expect(form.strategy).toBe('once')
      expect(form.beginAt).toBeNull()
      expect(form.endAt).toBeNull()
      expect(form.repeatType).toBeNull()
      expect(form.repeatInterval).toBeNull()
    })

    it('applyDetail：taskType=1 且带电量阈值时识别为连续执行', () => {
      const { form, applyDetail } = useForm()
      dirtyForm(form)

      applyDetail(makeDetail({ taskType: 1, minBatteryCapacity: 30 }))

      expect(form.strategy).toBe('continuous')
      expect(form.minBatteryCapacity).toBe(30)
      // 连续执行同样无法从详情还原周期配置
      expect(form.repeatType).toBeNull()
      expect(form.repeatInterval).toBeNull()
    })

    it('applyDetail：应回填其余详情字段并进入编辑模式', () => {
      const { form, applyDetail, editTaskUuid } = useForm()

      applyDetail(makeDetail({}))

      expect(editTaskUuid.value).toBe('task-uuid-001')
      expect(form.name).toBe('厂区北侧巡检')
      expect(form.deviceSn).toBe(MOCK_DEVICE_SN)
      expect(form.waylineUuid).toBe('wl-001')
      expect(form.rthAltitude).toBe(120)
      expect(form.precision).toBe('rtk')
      expect(form.outOfControlAction).toBe('return_home')
      expect(form.smartRthEnabled).toBe(false)
      expect(form.resumableEnabled).toBe(false)
    })

    it('applyDetail：重复策略下不应从详情预填截止时间，loadedEndAt 保持 null', () => {
      const { form, applyDetail, loadedEndAt } = useForm()

      applyDetail(makeDetail({ taskType: 2, beginAt: 1800000000, endAt: 1800172800 }))

      // 平台返回的 end_at 是推导出的执行窗口而非用户的重复规则截止时间，不可信，
      // 因此不预填 form.endAt，loadedEndAt 也保持 null——用户填入任意值都会被
      // buildRequest 视为「已改动」而正常回传
      expect(form.endAt).toBeNull()
      expect(loadedEndAt.value).toBeNull()
    })

    it('applyDetail：非重复策略下 loadedEndAt 应保持为 null', () => {
      const { applyDetail, loadedEndAt } = useForm()

      applyDetail(makeDetail({ taskType: 3, beginAt: 1800000000, endAt: 0 }))

      // 单次定时不需要比对截止时间，残留在 loadedEndAt 里会干扰下一次编辑
      expect(loadedEndAt.value).toBeNull()
    })

    it('applyDetail：子任务类型（4/6）按立即执行回填，不臆造周期配置', () => {
      const { form, applyDetail } = useForm()
      dirtyForm(form)

      applyDetail(makeDetail({ taskType: 4, beginAt: 1800000000, endAt: 1800172800 }))

      // 详情接口对子任务返回父任务规则，无法还原其自身策略，保守按立即处理
      expect(form.strategy).toBe('immediate')
      expect(form.beginAt).toBeNull()
      expect(form.endAt).toBeNull()
      expect(form.repeatType).toBeNull()
      expect(form.repeatInterval).toBeNull()
    })

    it('resetForm：应清空编辑模式并恢复默认表单', () => {
      const { form, applyDetail, resetForm, editTaskUuid, loadedEndAt } = useForm()

      applyDetail(makeDetail({}))
      resetForm()

      expect(editTaskUuid.value).toBeNull()
      expect(loadedEndAt.value).toBeNull()
      expect(form.name).toBe('')
      expect(form.strategy).toBe('immediate')
      expect(form.waylineUuid).toBeNull()
      expect(form.repeatType).toBeNull()
    })
  })

  describe('buildRequest', () => {
    /** 铺一份可通过校验的定时任务表单数据 */
    function fillTimedForm(form: FlightTaskFormReturn['form']): void {
      form.name = '  XH重复任务1  '
      form.waylineUuid = 'wl-001'
      form.deviceSn = MOCK_DEVICE_SN
      form.beginAt = 1800000000
      form.endAt = 1800172800
    }

    it('buildRequest：重复定时应生成 task_type=2 / repeat_type=4 / interval=1', () => {
      const { form, buildRequest } = useForm()
      fillTimedForm(form)
      form.strategy = 'repeat'
      form.repeatType = 'daily'
      form.repeatInterval = 1

      const req = buildRequest()

      expect(req.task_type).toBe(2)
      expect(req.repeat_type).toBe(4)
      expect(req.interval).toBe(1)
      expect(req.begin_at).toBe(1800000000)
      expect(req.end_at).toBe(1800172800)
      // 名称去首尾空格后再提交
      expect(req.name).toBe('XH重复任务1')
      expect(req.plan_type).toBe(0)
      expect(req.out_of_control_action_in_flight).toBe(1)
      expect(req.wayline_precision_type).toBe(1)
      expect(req.vlm_enabled).toBe(false)
    })

    it('buildRequest：编辑时未填写截止时间不应写回 end_at', () => {
      const { applyDetail, buildRequest } = useForm()
      applyDetail(buildDetail({
        taskType: 2,
        beginAt: 1800000000,
        endAt: 1800172800,
      }))

      const req = buildRequest()

      expect(req.task_type).toBe(2)
      expect(req.begin_at).toBe(1800000000)
      // applyDetail 不预填 endAt，用户未重新选择就不应带着 undefined/null 提交 end_at
      expect(req.end_at).toBeUndefined()
    })

    it('buildRequest：用户重新选择截止时间后应写回 end_at', () => {
      const { form, applyDetail, buildRequest } = useForm()
      applyDetail(buildDetail({
        taskType: 2,
        beginAt: 1800000000,
        endAt: 1800172800,
      }))

      form.endAt = 1800259200
      const req = buildRequest()

      expect(req.end_at).toBe(1800259200)
    })

    it('buildRequest：单次定时应只带 begin_at，且 repeat_type 归零', () => {
      const { form, buildRequest } = useForm()
      fillTimedForm(form)
      form.strategy = 'once'
      // 即使表单里残留了周期配置，非 repeat 策略也必须归零（详情回填的另一道保护）
      form.repeatType = 'weekly'
      form.repeatInterval = 3

      const req = buildRequest()

      expect(req.task_type).toBe(3)
      expect(req.repeat_type).toBe(0)
      expect(req.begin_at).toBe(1800000000)
      expect(req.end_at).toBeUndefined()
      expect(req.interval).toBeUndefined()
    })

    it('buildRequest：立即执行不应带起止时间与周期字段', () => {
      const { form, buildRequest } = useForm()
      fillTimedForm(form)
      form.strategy = 'immediate'

      const req = buildRequest()

      expect(req.task_type).toBe(1)
      expect(req.repeat_type).toBe(0)
      expect(req.begin_at).toBeUndefined()
      expect(req.end_at).toBeUndefined()
      expect(req.interval).toBeUndefined()
    })

    it('buildRequest：重复策略缺少 interval 时请求不含该字段（周期语义不完整）', () => {
      const { form, buildRequest } = useForm()
      fillTimedForm(form)
      form.strategy = 'repeat'
      form.repeatType = 'daily'
      form.repeatInterval = null

      const req = buildRequest()

      expect(req.repeat_type).toBe(4)
      expect(req.interval).toBeUndefined()
    })

    it('buildRequest：连续执行应带电量阈值与时段配置', () => {
      const { form, buildRequest } = useForm()
      fillTimedForm(form)
      form.strategy = 'continuous'
      form.minBatteryCapacity = 30
      form.continuousPeriods = [[0, 3600]]

      const req = buildRequest()

      expect(req.task_type).toBe(5)
      expect(req.min_battery_capacity).toBe(30)
      expect(req.continuous_task_periods).toEqual([[0, 3600]])
    })

    it('buildRequest：每周/每月周期应映射为对应 repeat_type', () => {
      const { form, buildRequest } = useForm()
      fillTimedForm(form)
      form.strategy = 'repeat'
      form.repeatInterval = 1

      form.repeatType = 'weekly'
      expect(buildRequest().repeat_type).toBe(5)

      form.repeatType = 'monthly'
      expect(buildRequest().repeat_type).toBe(6)

      // repeatType 未选择时降级为「不重复」，不臆造周期
      form.repeatType = null
      expect(buildRequest().repeat_type).toBe(0)
    })
  })

  it('buildRequest_shouldMapStrategyToPlatformTaskType', () => {
    const { form, buildRequest } = useForm()

    // 对齐 es-task-management 的 TaskType 枚举：三种定时策略必须各占一个类型值，
    // 全部塌缩成 2 会让平台把单次/连续任务当成重复规则父任务处理
    const expected: Array<[FlightTaskFormData['strategy'], number]> = [
      ['immediate', 1],
      ['repeat', 2],
      ['once', 3],
      ['continuous', 5],
    ]

    for (const [strategy, taskType] of expected) {
      form.strategy = strategy
      expect(buildRequest().task_type).toBe(taskType)
    }
  })

  it('applyDetail_shouldPreferRepeatStrategy_whenDetailTaskTypeIsTwo', () => {
    const { form, applyDetail } = useForm()

    applyDetail(buildDetail({ taskType: 2, beginAt: 1800000000, endAt: 1800172800 }))

    // 2 是重复规则父任务，详情返回的 beginAt/endAt 只是该规则的边界。
    // 窗口必须非塌缩（endAt > beginAt）：塌缩或缺失时 applyDetail 会退回单次定时，
    // 避免把零长度窗口写回平台（对应 master 侧的「截止时间等于开始时间」用例）。
    expect(form.strategy).toBe('repeat')
  })

  it('applyDetail_shouldPreferOnceStrategy_whenDetailTaskTypeIsThree', () => {
    const { form, applyDetail } = useForm()

    applyDetail(buildDetail({ taskType: 3, beginAt: 1700000000 }))

    expect(form.strategy).toBe('once')
    expect(form.beginAt).toBe(1700000000)
  })

  it('applyDetail_shouldPreferContinuousStrategy_whenDetailTaskTypeIsFive', () => {
    const { form, applyDetail } = useForm()

    applyDetail(buildDetail({ taskType: 5, minBatteryCapacity: 30 }))

    expect(form.strategy).toBe('continuous')
  })
})
