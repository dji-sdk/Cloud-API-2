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

import type { PlanType, TaskStrategy } from '@/types/flightTask'

/**
 * PaaS 通道当前无法承载的任务能力清单（已知缺陷）。
 *
 * 与 SaaS 通道不同，PaaS 通道的创建/编辑请求最终由 SDK 的 `CreateTaskReq` /
 * `EditTaskReq` 表达，而这两个类没有 `repeat_type` / `interval` / `plan_type`
 * 等 setter（3.1.6~3.3.5 均无承载位，升级不解决），协议层没有承载位。
 * 前端提交的字段会在 BFF 层被直接丢弃（见 `SdkFlightTaskCommandService`
 * 的 `warnUnsupportedFields`），落到平台侧只剩一个 `task_type`：
 *
 * - `repeat`（重复定时）：只剩 task_type=2，平台把它定义为「条件任务」，
 *   服务端判别式 `== ImmediateTask` 判否后走 else 分支，提交的 begin_at 被原样保留，
 *   而该分支里展开重复调度的整段逻辑处于注释状态——重复周期被丢弃，落库成一条
 *   只执行一次的任务。注意**不是**「按立即执行处理」：只有立即任务分支才会用
 *   当前时间重写起止时间；
 * - `continuous`（连续执行）：task_type=5 落进同一个 else 分支，退化方式与上一条相同；
 * - `leapfrog` / `manual`（蛙跳/手飞）：`plan_type` 从未到达 SDK。
 *
 * 这里只做置灰、不做删除，原因有二：
 * 1. 这是**集成协议**缺口而非产品缺口——内核 FlightBody 携带完整的重复语义，
 *    SaaS 通道的定时任务工作正常，删掉入口会让人误以为 FlightHub 不支持定时任务；
 * 2. SDK 补齐承载位后，把对应条目从下面的清单里删掉即可恢复入口，无需改动组件。
 *
 * 注意：置灰项仍会参与选中态渲染。编辑一张历史重复任务时，`applyDetail` 会把
 * strategy 预填为 `repeat`，若把选项从列表中过滤掉，界面上将没有任何按钮处于选中态，
 * 用户既看不出任务原本是什么，一点击也会静默改成别的策略。
 *
 * 但反过来说，置灰只挡「新建」不挡「保存」：编辑历史重复任务时策略预填为 repeat，
 * 用户不动任何东西直接提交，请求仍会带上 task_type=2，服务端按「条件任务」处理，
 * 原有的重复周期被丢弃，任务退化成一条只执行一次的计划。因此编辑态还需要
 * {@link UNSUPPORTED_STRATEGY_SAVE_HINT} 明确告知后果。
 */

/** 通道不支持的策略，置灰但保留在选项列表中 */
const UNSUPPORTED_STRATEGIES: readonly TaskStrategy[] = ['repeat', 'continuous']

/** 通道不支持的机型计划类型，置灰但保留在选项列表中 */
const UNSUPPORTED_PLAN_TYPES: readonly PlanType[] = ['leapfrog', 'manual']

/**
 * 置灰入口的统一提示文案（渲染为按钮 title）。
 *
 * 措辞与 `FlightTaskList.vue` 的 `UNSUPPORTED_ACTION_TIP` 对齐，用「平台协议」
 * 而非「通道」：面向 ISV 用户，通道/链路的内部叫法没有解释力。
 */
export const CHANNEL_UNSUPPORTED_HINT = '当前平台协议暂不支持该选项'

/**
 * 编辑历史任务时的后果提示：当前策略已置灰但仍处于选中态，直接提交会丢失原有语义。
 *
 * 与 {@link CHANNEL_UNSUPPORTED_HINT} 分开，是因为两者的落点不同——前者解释
 * 「为什么点不动」，后者说明「不改会怎样」，后者才是会让用户踩坑的那一条。
 *
 * 措辞刻意写「只会执行一次」而不是「按立即执行处理」：服务端只会丢弃重复周期，
 * 计划起止时间仍取自表单（编辑态会重新计算结束时间），并不会变成马上起飞。
 */
export const UNSUPPORTED_STRATEGY_SAVE_HINT =
  '当前平台协议无法保留该任务策略，保存后重复规则将丢失，任务只会执行一次'

/**
 * 判断任务策略在 PaaS 通道是否可用。
 *
 * @param strategy 任务策略
 * @returns 可用返回 true；重复定时 / 连续执行返回 false
 */
export function isStrategySupported(strategy: TaskStrategy): boolean {
  return !UNSUPPORTED_STRATEGIES.includes(strategy)
}

/**
 * 判断计划类型在 PaaS 通道是否可用。
 *
 * @param planType 计划类型
 * @returns 可用返回 true；蛙跳 / 手飞返回 false
 */
export function isPlanTypeSupported(planType: PlanType): boolean {
  return !UNSUPPORTED_PLAN_TYPES.includes(planType)
}
