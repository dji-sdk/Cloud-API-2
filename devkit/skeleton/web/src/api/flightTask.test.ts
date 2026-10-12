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

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import type { CreateFlightTaskRequest } from '@/types/flightTask'

// 控制 Mock 开关：Mock 分支验证本地重名拦截，非 Mock 分支验证错误码透出
vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: (key: string, fallback = '') => {
    if (key === 'MOCK_ENABLED') return mockEnabled
    return fallback
  },
}))

let mockEnabled = 'true'

/** 重名错误码，与 BFF FlightTaskRestController 约定一致。 */
const ERROR_CODE_DUPLICATE = 'TASK_NAME_DUPLICATE'

/** 不可编辑错误码，与 BFF FlightTaskRestController 约定一致。 */
const ERROR_CODE_NOT_EDITABLE = 'TASK_NOT_EDITABLE'

/** mock 列表中已存在的任务名，用于构造重名场景。 */
const EXISTING_NAME = '厂区北侧巡检'
/** 与其他任务重名的另一个 mock 任务名。 */
const OTHER_EXISTING_NAME = '光伏板定期巡查'
/** 仅存在于 mock 列表的任务 uuid，用于验证编辑自排除。 */
const EXISTING_TASK_UUID = 'task-001-abc'

/** mock 列表中「待执行 + 非断点续飞」的任务，可编辑。 */
const EDITABLE_TASK_UUID = 'task-011-efg'
/** mock 列表中「待执行 + 断点续飞」的任务，平台禁止编辑。 */
const BREAK_POINT_TASK_UUID = 'task-008-vwx'

/** 构造一份最小可用的创建请求体。 */
function buildRequest(name: string): CreateFlightTaskRequest {
  return {
    name,
    task_type: 1,
    repeat_type: 0,
    plan_type: 0,
    wayline_uuid: 'wl-001',
    sn: 'MOCK_DRONE_0001',
    out_of_control_action_in_flight: 1,
    wayline_precision_type: 1,
    rth_altitude: 100,
  }
}

/** 构造一个后端错误响应（BFF 重名时返回 400 + code）。 */
function errorResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json' },
  })
}

describe('flightTask api', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  describe('mock 模式', () => {
    /** 重新加载模块以获得干净的 mock 列表。 */
    async function loadApi(): Promise<typeof import('@/api/flightTask')> {
      return import('@/api/flightTask')
    }

    beforeEach(() => {
      mockEnabled = 'true'
    })

    it('createFlightTask_shouldThrowDuplicateCode_whenNameExistsInMockList', async () => {
      const api = await loadApi()

      await expect(api.createFlightTask(buildRequest(EXISTING_NAME))).rejects.toMatchObject({
        code: ERROR_CODE_DUPLICATE,
      })
    })

    it('createFlightTask_shouldReturnTaskUuid_whenNameIsUnique', async () => {
      const api = await loadApi()

      const result = await api.createFlightTask(buildRequest('全新巡检任务'))

      expect(result.taskUuid).toContain('task-')
    })

    it('editFlightTask_shouldResolve_whenKeepingOwnName', async () => {
      const api = await loadApi()

      // 不改名直接保存不能被判为重名：必须排除任务自身
      await expect(
        api.editFlightTask(EDITABLE_TASK_UUID, buildRequest('单次定时巡检')),
      ).resolves.toBeUndefined()
    })

    it('editFlightTask_shouldThrowDuplicateCode_whenRenamingToAnotherTaskName', async () => {
      const api = await loadApi()

      await expect(
        api.editFlightTask(EDITABLE_TASK_UUID, buildRequest(OTHER_EXISTING_NAME)),
      ).rejects.toMatchObject({ code: ERROR_CODE_DUPLICATE })
    })

    it('editFlightTask_shouldThrowNotEditable_whenTaskIsBreakPointResume', async () => {
      // 复现断点续飞任务的编辑门禁：mock 下同样拦截并给出原因
      const api = await loadApi()

      await expect(
        api.editFlightTask(BREAK_POINT_TASK_UUID, buildRequest(EXISTING_NAME)),
      ).rejects.toMatchObject({
        code: ERROR_CODE_NOT_EDITABLE,
        message: '断点续飞任务不支持编辑',
      })
    })

    it('isTaskNotEditableError_shouldMatchByCode_whenErrorIsPlainObject', async () => {
      // 与 isTaskNameDuplicateError 同理，跨模块传递时只按 code 判定
      const api = await loadApi()

      expect(api.isTaskNotEditableError({ code: ERROR_CODE_NOT_EDITABLE })).toBe(true)
      expect(api.isTaskNotEditableError({ code: ERROR_CODE_DUPLICATE })).toBe(false)
      expect(api.isTaskNotEditableError(new Error('网络异常'))).toBe(false)
    })
  })

  describe('非 mock 模式', () => {
    /** 在关闭 mock 后重新加载模块，并桩掉 fetch 走真实 HTTP 分支。 */
    async function loadApiWithFetch(
      res: Response,
    ): Promise<{ api: typeof import('@/api/flightTask'); fetchSpy: ReturnType<typeof vi.fn> }> {
      const fetchSpy = vi.fn().mockResolvedValue(res)
      vi.stubGlobal('fetch', fetchSpy)
      return { api: await import('@/api/flightTask'), fetchSpy }
    }

    beforeEach(() => {
      mockEnabled = 'false'
    })

    it('createFlightTask_shouldExposeCode_whenBackendReturnsDuplicate', async () => {
      const message = `任务名称「${EXISTING_NAME}」已存在，请更换名称`
      const { api } = await loadApiWithFetch(
        errorResponse(400, { code: ERROR_CODE_DUPLICATE, message }),
      )

      await expect(api.createFlightTask(buildRequest(EXISTING_NAME))).rejects.toMatchObject({
        code: ERROR_CODE_DUPLICATE,
        message,
      })
    })

    it('editFlightTask_shouldExposeCode_whenBackendReturnsDuplicate', async () => {
      const { api, fetchSpy } = await loadApiWithFetch(
        errorResponse(400, { code: ERROR_CODE_DUPLICATE, message: '任务名称已存在' }),
      )

      await expect(
        api.editFlightTask(EXISTING_TASK_UUID, buildRequest(OTHER_EXISTING_NAME)),
      ).rejects.toMatchObject({ code: ERROR_CODE_DUPLICATE })

      // 编辑走 edit 子路径，不能误打到创建接口
      expect(fetchSpy).toHaveBeenCalledWith(
        expect.stringContaining(`/flight-tasks/${EXISTING_TASK_UUID}/edit`),
        expect.objectContaining({ method: 'POST' }),
      )
    })

    it('createFlightTask_shouldLeaveCodeUndefined_whenErrorBodyIsNotJson', async () => {
      const { api } = await loadApiWithFetch(new Response('<html>500</html>', { status: 500 }))

      await expect(api.createFlightTask(buildRequest(EXISTING_NAME))).rejects.toMatchObject({
        code: undefined,
        message: '创建飞行任务失败 (HTTP 500)',
      })
    })

    it('createFlightTask_shouldFallbackToErrorField_whenMessageAbsent', async () => {
      const { api } = await loadApiWithFetch(errorResponse(500, { error: '内部错误' }))

      await expect(api.createFlightTask(buildRequest(EXISTING_NAME))).rejects.toMatchObject({
        message: '内部错误',
      })
    })
  })
})
