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

import { reactive, ref } from 'vue'
import type {
  FlightTaskFormData,
  FlightTaskFormErrors,
  CreateFlightTaskRequest,
  FlightTaskDetail,
  TaskStrategy,
} from '@/types/flightTask'
import { createFlightTask, editFlightTask, isTaskNameDuplicateError } from '@/api/flightTask'
import { resolveTaskStrategy } from '@/utils/taskStrategy'

const DEFAULT_FORM: FlightTaskFormData = {
  name: '',
  planType: 'normal',
  waylineUuid: null,
  waylineName: null,
  deviceSn: null,
  deviceName: null,
  vlmEnabled: false,
  precision: 'rtk',
  strategy: 'immediate',
  beginAt: null,
  endAt: null,
  repeatType: null,
  repeatInterval: null,
  minBatteryCapacity: null,
  continuousPeriods: null,
  resumableEnabled: false,
  smartRthEnabled: false,
  rthAltitude: 100,
  outOfControlAction: 'return_home',
}

/**
 * 协议约束常量（CreateTaskReq，秒级 Unix 时间戳）。
 *
 * - 定时任务（once/repeat）begin_at 须不早于当前时间 2 分钟
 * - repeat 策略 end_at 必填且须大于 begin_at（平台分钟粒度扫描，给最小保守窗口）
 * - once 策略无需 end_at（平台按 begin_at + wayline_duration 自动计算）
 */
const MIN_BEGIN_AT_LEAD_SECONDS = 120

/**
 * 将 strategy 映射为平台的 task_type。
 *
 * 对齐 es-task-management 的 `TaskType` 枚举（`app/model/po/task.go`）：
 * 1=ImmediateTask、2=RepeatedTask（重复规则父任务）、3=SingleTimedTask、
 * 5=ContinuousTask（连续执行虚拟任务）。
 *
 * 三者必须区分：`TimedTaskTypes = {3, 4}`，2 与 5 在平台侧是被展开/改写的
 * 虚拟类型（创建时由平台的 `AddRepeatedTask` 分发处理），并非常规定时任务。
 */
function mapTaskType(strategy: FlightTaskFormData['strategy']): number {
  switch (strategy) {
    case 'immediate': return 1
    case 'repeat': return 2
    case 'once': return 3
    case 'continuous': return 5
    default: return 1
  }
}

/**
 * 将详情接口返回的 taskType 反向映射为表单策略。
 *
 * 平台侧语义（es-task-management `TaskType` 枚举 + `getContinuousPeriodsFromTask`）：
 * - 1 即时任务（连续执行也被平台记为 1，以是否携带 min_battery_capacity 区分）
 * - 2 重复定时任务（父任务，返回的是重复规则本身）
 * - 3 单次定时任务
 * - 5 连续执行（平台在 ExtendedEndAt 非空时把 task_type 改写为 5）
 * - 4 重复定时子任务 / 6 连续执行子任务：详情接口对子任务返回父任务的规则，
 *   故子任务 type 只会出现在列表场景，此处保持即时以避免误解
 *
 * 判定规则与列表的 {@link resolveTaskStrategy} 同源，差异仅在子任务处理。
 */
function mapDetailTaskType(detail: FlightTaskDetail): TaskStrategy {
  if (detail.taskType === 4 || detail.taskType === 6) {
    return 'immediate'
  }
  return resolveTaskStrategy(detail.taskType, detail.minBatteryCapacity)
}

/** 将 planType 映射为 SDK plan_type 数值 */
function mapPlanType(planType: FlightTaskFormData['planType']): number {
  switch (planType) {
    case 'normal': return 0
    case 'leapfrog': return 1
    case 'manual': return 2
    default: return 0
  }
}

/** 将 strategy + repeatType 映射为 SDK repeat_type */
function mapRepeatType(form: FlightTaskFormData): number {
  if (form.strategy !== 'repeat') return 0
  switch (form.repeatType) {
    case 'daily': return 4
    case 'weekly': return 5
    case 'monthly': return 6
    default: return 0
  }
}

/**
 * 飞行任务创建/编辑表单 composable。
 *
 * 管理表单状态、校验、format 转换和提交。
 * 通过 editTaskUuid 区分创建与编辑模式：非 null 时提交走 editFlightTask。
 */
export function useFlightTaskForm() {
  const form = reactive<FlightTaskFormData>({ ...DEFAULT_FORM })
  const errors = reactive<FlightTaskFormErrors>({})
  const submitting = ref(false)
  /** 编辑模式下的任务 uuid；null 表示创建模式 */
  const editTaskUuid = ref<string | null>(null)
  /**
   * applyDetail 预填进表单的截止时间，用于判断用户是否实际改动过。
   *
   * applyDetail 不会用详情接口的 endAt 预填（见 applyDetail 内注释），故编辑
   * 场景下恒为 null；仅当用户真正输入了值，buildRequest 才会把 end_at 回传，
   * 避免空提交静默沿用某个默认值覆盖平台已有的重复规则。
   *
   * 必须导出：测试需要断言「未改动时间时不回传 end_at」这一行为。
   */
  const loadedEndAt = ref<number | null>(null)

  function clearErrors(): void {
    errors.name = undefined
    errors.waylineUuid = undefined
    errors.deviceSn = undefined
    errors.beginAt = undefined
    errors.endAt = undefined
    errors.general = undefined
  }

  /**
   * 清除单个字段的错误提示。
   *
   * 供表单控件在用户修改输入时调用：否则「名称已存在」这类服务端错误会在用户
   * 改完名字后仍挂在输入框下方，直到下次提交才消失。
   */
  function clearFieldError(field: keyof FlightTaskFormErrors): void {
    errors[field] = undefined
  }

  function validateField(field: keyof FlightTaskFormErrors): boolean {
    switch (field) {
      case 'name':
        if (!form.name.trim()) {
          errors.name = '请输入计划名称'
          return false
        }
        // 按 trim 后的长度校验，与 buildRequest() 的提交口径保持一致
        if (form.name.trim().length > 50) {
          errors.name = '计划名称不能超过 50 字'
          return false
        }
        errors.name = undefined
        return true

      case 'waylineUuid':
        if (form.planType !== 'manual' && !form.waylineUuid) {
          errors.waylineUuid = '请选择航线'
          return false
        }
        errors.waylineUuid = undefined
        return true

      case 'deviceSn':
        if (!form.deviceSn) {
          errors.deviceSn = '请选择设备'
          return false
        }
        errors.deviceSn = undefined
        return true

      case 'beginAt':
        if (form.strategy === 'once' || form.strategy === 'repeat') {
          if (!form.beginAt) {
            errors.beginAt = '请选择执行时间'
            return false
          }
          if (form.beginAt < Math.floor(Date.now() / 1000)) {
            errors.beginAt = '执行时间必须是未来时间'
            return false
          }
          // 协议要求定时任务 begin_at 不早于当前时间 2 分钟
          if (form.beginAt < Math.floor(Date.now() / 1000) + MIN_BEGIN_AT_LEAD_SECONDS) {
            errors.beginAt = '执行时间需至少晚于当前时间 2 分钟'
            return false
          }
        }
        errors.beginAt = undefined
        return true

      case 'endAt':
        if (form.strategy === 'repeat') {
          if (!form.endAt) {
            errors.endAt = '请选择截止时间'
            return false
          }
          if (form.beginAt && form.endAt <= form.beginAt) {
            errors.endAt = '截止时间必须晚于开始时间'
            return false
          }
        }
        errors.endAt = undefined
        return true

      default:
        return true
    }
  }

  function validate(): boolean {
    const fields: (keyof FlightTaskFormErrors)[] = ['name', 'waylineUuid', 'deviceSn', 'beginAt', 'endAt']
    let valid = true
    for (const field of fields) {
      if (!validateField(field)) {
        valid = false
      }
    }
    return valid
  }

  function buildRequest(): CreateFlightTaskRequest {
    const req: CreateFlightTaskRequest = {
      name: form.name.trim(),
      task_type: mapTaskType(form.strategy),
      repeat_type: mapRepeatType(form),
      plan_type: mapPlanType(form.planType),
      wayline_uuid: form.waylineUuid || '',
      sn: form.deviceSn || '',
      out_of_control_action_in_flight: form.outOfControlAction === 'return_home' ? 1 : 2,
      wayline_precision_type: form.precision === 'rtk' ? 1 : 0,
      rth_altitude: form.rthAltitude,
      resumable_status: form.resumableEnabled ? 1 : 0,
      rth_mode: form.smartRthEnabled ? 1 : 0,
      vlm_enabled: form.vlmEnabled,
    }

    // once（单次定时）：task_type=3，仅需 begin_at；end_at 由平台按 begin_at + wayline_duration 计算
    if (form.strategy === 'once' && form.beginAt) {
      req.begin_at = form.beginAt
    }

    // repeat（重复定时）：begin_at 必填；end_at 仅在用户实际改动过时回传
    // （详情接口返回的 endAt 是平台推导出的执行窗口，见 loadedEndAt 注释）
    if (form.strategy === 'repeat' && form.beginAt) {
      req.begin_at = form.beginAt
      if (form.endAt && form.endAt !== loadedEndAt.value) {
        req.end_at = form.endAt
      }
      if (form.repeatInterval) {
        req.interval = form.repeatInterval
      }
    }

    // continuous（连续执行）：task_type=5，附加电量阈值和时段配置
    if (form.strategy === 'continuous') {
      if (form.minBatteryCapacity !== null) {
        req.min_battery_capacity = form.minBatteryCapacity
      }
      if (form.continuousPeriods) {
        req.continuous_task_periods = form.continuousPeriods
      }
    }

    return req
  }

  async function submitForm(): Promise<string | null> {
    clearErrors()
    if (!validate()) return null

    submitting.value = true
    try {
      const req = buildRequest()
      // 编辑模式：提交走 edit 接口，返回原任务 uuid
      if (editTaskUuid.value) {
        await editFlightTask(editTaskUuid.value, req)
        return editTaskUuid.value
      }
      const result = await createFlightTask(req)
      return result.taskUuid
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e)
      // 重名是名称字段的问题，落到名称输入框下方；其余错误走通用横幅
      if (isTaskNameDuplicateError(e)) {
        errors.name = msg
      } else {
        errors.general = msg
      }
      return null
    } finally {
      submitting.value = false
    }
  }

  /**
   * 将任务详情预填到表单（编辑模式用）。
   *
   * 盲区：详情接口不返回 planType / repeatType / repeatInterval / vlmEnabled /
   * continuousPeriods，这些字段无法从后端还原。为避免「上一次编辑残留值」被静默
   * 带入本次提交，回填前先把它们复位为 DEFAULT_FORM 值，而不是沿用当前 reactive 状态。
   *
   * 策略以详情返回的 taskType 判定（见 {@link mapDetailTaskType}），不再用
   * beginAt/endAt 是否相等去猜；仅当重复任务窗口塌缩或缺失时退回单次定时。
   */
  function applyDetail(detail: FlightTaskDetail): void {
    // 详情接口无法提供的字段：先复位，防止跨任务的陈旧值污染本次提交
    form.planType = DEFAULT_FORM.planType
    form.vlmEnabled = DEFAULT_FORM.vlmEnabled
    form.repeatType = DEFAULT_FORM.repeatType
    form.repeatInterval = DEFAULT_FORM.repeatInterval
    form.continuousPeriods = DEFAULT_FORM.continuousPeriods

    editTaskUuid.value = detail.taskUuid
    form.name = detail.name
    form.deviceSn = detail.sn
    form.deviceName = detail.deviceName
    form.waylineUuid = detail.waylineUuid
    form.waylineName = detail.waylineName
    form.rthAltitude = detail.rthAltitude
    form.resumableEnabled = detail.isBreakPointResume
    form.minBatteryCapacity = detail.minBatteryCapacity > 0 ? detail.minBatteryCapacity : null
    form.precision = detail.waylinePrecisionType === 1 ? 'rtk' : 'gnss'
    form.outOfControlAction = detail.outOfControlActionInFlight === 1 ? 'return_home' : 'continue'
    form.smartRthEnabled = detail.rthMode === 1

    // 策略以详情返回的 taskType 判定，不再用 beginAt/endAt 是否相等去猜
    form.strategy = mapDetailTaskType(detail)

    // 重复任务的 end_at 是平台按 begin_at + 航线时长推导出的执行窗口，
    // 窗口塌缩（end_at == begin_at）或缺失时承载不了重复规则，退回单次定时，
    // 避免把零长度窗口写回平台。
    if (form.strategy === 'repeat' && (detail.endAt <= 0 || detail.endAt === detail.beginAt)) {
      form.strategy = 'once'
    }

    const isTimed = form.strategy === 'once' || form.strategy === 'repeat'
    form.beginAt = isTimed && detail.beginAt > 0 ? detail.beginAt : null
    // detail.endAt 不预填：它是平台按 begin_at + 航线时长推导出的单次执行窗口，
    // 不是用户创建时设置的重复规则截止时间（SDK TaskDetail bean 未暴露真值字段），
    // 回显它等于展示一个看似合理但错误的时间。留空交由用户重新确认，
    // 下方 validateField('endAt') 会在未填写时拦截提交，不会静默带着误导值保存。
    form.endAt = null
    // 记录预填值：buildRequest 据此判断用户是否真的改过截止时间
    loadedEndAt.value = form.endAt

    if (form.strategy === 'repeat') {
      // repeatType / repeatInterval 详情接口不返回，只能取保守默认值。
      // 必须显式写入而非留 null：StrategySection 以 `repeatType || 'daily'` /
      // `repeatInterval || 1` 渲染，留 null 会出现「界面显示每天/1、实际提交不含
      // interval」的不一致，且 task_type=2 却带 repeat_type=0 是自相矛盾的请求。
      // 已知代价：编辑一张「每周/每月」或间隔非 1 的重复任务时会被静默改写为每天/1，
      // 需等详情接口补齐这两个字段后才能正确回填。
      form.repeatType = 'daily'
      form.repeatInterval = 1
    }
  }

  function resetForm(): void {
    Object.assign(form, DEFAULT_FORM)
    editTaskUuid.value = null
    loadedEndAt.value = null
    clearErrors()
  }

  return {
    form,
    errors,
    submitting,
    editTaskUuid,
    loadedEndAt,
    validate,
    validateField,
    clearFieldError,
    buildRequest,
    submitForm,
    applyDetail,
    resetForm,
  }
}
