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

import type { FlightTaskType, TaskStrategy } from '@/types/flightTask'

/**
 * 任务策略中文标签。
 *
 * 取值与新建/编辑任务页 {@code StrategySection} 的策略选项逐字一致，
 * 列表、详情与表单三处共用同一份文案，避免各自维护出现口径漂移。
 */
export const STRATEGY_LABELS: Record<TaskStrategy, string> = {
  immediate: '立即',
  once: '单次定时',
  repeat: '重复定时',
  continuous: '连续执行',
}

/**
 * 由平台 taskType 与最低电量阈值判定任务创建策略。
 *
 * <p>平台 TaskType 枚举：1=即时, 2=重复定时（父任务）, 3=单次定时,
 * 4=重复定时子任务, 5=连续执行, 6=连续执行子任务。
 *
 * <p>注意 {@code taskType=1} 同时被平台用于"立即"与"连续执行"两类任务，
 * 必须结合 {@code minBatteryCapacity > 0} 才能区分——这是判定连续执行的唯一依据，
 * 与编辑表单预填规则同源。
 *
 * <p>与编辑表单的 {@code mapDetailTaskType} 存在一处有意的差异：子任务
 * （4=重复定时子任务 / 6=连续执行子任务）在列表中直接按其自身类型展示，
 * 而详情接口对子任务返回的是父任务规则，故表单侧不处理 4/6。
 *
 * @param taskType            平台任务类型
 * @param minBatteryCapacity  最低电量阈值（百分比），无数据为 0
 * @returns 任务创建策略
 */
export function resolveTaskStrategy(
  taskType: FlightTaskType,
  minBatteryCapacity: number,
): TaskStrategy {
  switch (taskType) {
    case 2:
    case 4:
      return 'repeat'
    case 3:
      return 'once'
    case 5:
    case 6:
      return 'continuous'
    case 1:
      return minBatteryCapacity > 0 ? 'continuous' : 'immediate'
    default:
      return 'immediate'
  }
}

/**
 * 判断策略是否属于"定时类"（由平台按预定时间自动触发）。
 *
 * <p>连续执行虽然同样不支持手动触发，但触发条件是电量阈值而非时间，
 * 因此不归入定时类。
 *
 * @param strategy 任务策略
 * @returns 单次定时 / 重复定时返回 true
 */
export function isScheduledStrategy(strategy: TaskStrategy): boolean {
  return strategy === 'once' || strategy === 'repeat'
}
