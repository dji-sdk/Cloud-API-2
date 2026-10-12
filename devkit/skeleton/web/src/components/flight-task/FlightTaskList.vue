<!--
  Copyright 2025-2026 DJI. All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { RefreshCw, Play, Pause, XCircle, Plus, Pencil, Trash2, AlertTriangle } from 'lucide-vue-next'
import { useFlightTaskList } from '@/composables/useFlightTaskList'
import { pauseFlightTask, resumeFlightTask, deleteFlightTask } from '@/api/flightTask'
import type { FlightTaskInfo } from '@/types/flightTask'
import { resolveTaskStrategy, isScheduledStrategy } from '@/utils/taskStrategy'
import {
  FLIGHT_TASK_STATUS_COLORS as STATUS_COLORS,
  FLIGHT_TASK_STATUS_LABELS as STATUS_LABELS,
  FLIGHT_TASK_STATUS_OPTIONS as STATUS_OPTIONS,
} from '@/types/flightTask'
import MediaPagination from '@/components/media/MediaPagination.vue'
import TaskTypeLabel from './TaskTypeLabel.vue'
import { getEditBlockTip, isTaskEditable } from '@/utils/flightTaskEditability'
import { formatDateTime } from '@/utils/format'

const router = useRouter()

const {
  filters,
  pagination,
  tasks,
  loading,
  error,
  fetchTasks,
  nextPage,
  prevPage,
  changePageSize,
} = useFlightTaskList()

const actionLoading = ref<string | null>(null)

/**
 * 协议约束：任务状态接口（UpdateTaskStatusReq）仅允许下发 0（待开始）和
 * 6（已挂起），"立即执行"与"取消"无法通过该接口表达，因此 UI 层禁用
 * 这两个按钮并提示用户。
 */
const UNSUPPORTED_ACTION_TIP = '平台协议仅支持暂停/恢复任务，暂不支持立即执行与取消'

/** 定时类任务（单次定时/重复定时）由平台按预定时间触发，额外提示不支持手动执行。 */
const SCHEDULED_EXECUTE_TIP = '定时任务将在预定时间自动执行，不支持手动触发'

/** 判断任务是否属于定时类策略（连续执行按电量触发，不归入定时类）。 */
function isScheduledTask(task: FlightTaskInfo): boolean {
  return isScheduledStrategy(resolveTaskStrategy(task.taskType, task.minBatteryCapacity))
}

function goToCreate(): void {
  router.push('/flight-tasks/create')
}

/** 跳转到编辑模式（复用创建视图，通过 taskUuid query 区分）。 */
function goToEdit(taskUuid: string): void {
  router.push({ path: '/flight-tasks/create', query: { taskUuid } })
}

function goToReplay(taskUuid: string, waylineUuid: string): void {
  router.push({ path: '/flight-replay', query: { task_id: taskUuid, wayline_uuid: waylineUuid } })
}

async function handlePause(taskUuid: string): Promise<void> {
  actionLoading.value = taskUuid
  try {
    await pauseFlightTask(taskUuid)
    await fetchTasks()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '暂停任务失败'
  } finally {
    actionLoading.value = null
  }
}

async function handleDelete(taskUuid: string): Promise<void> {
  if (!window.confirm('确认删除此任务？')) {
    return
  }
  actionLoading.value = taskUuid
  try {
    await deleteFlightTask(taskUuid)
    await fetchTasks()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '删除任务失败'
  } finally {
    actionLoading.value = null
  }
}

async function handleResume(taskUuid: string): Promise<void> {
  actionLoading.value = taskUuid
  try {
    await resumeFlightTask(taskUuid)
    await fetchTasks()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '恢复任务失败'
  } finally {
    actionLoading.value = null
  }
}
</script>

<template>
  <div class="flight-task-list">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <select v-model="filters.status" class="status-filter">
          <option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
        <span class="total-count">共 {{ pagination.totalCount }} 条任务</span>
      </div>
      <div class="toolbar-right">
        <button class="refresh-btn" :disabled="loading" @click="fetchTasks()">
          <RefreshCw :size="14" :class="{ spinning: loading }" />
          <span>刷新</span>
        </button>
        <button class="create-btn" @click="goToCreate">
          <Plus :size="14" />
          <span>新建任务</span>
        </button>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-bar">{{ error }}</div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <table class="task-table">
        <thead>
          <tr>
            <th class="col-name">任务名称</th>
            <th class="col-sn">设备 SN</th>
            <th class="col-id">任务 ID</th>
            <th class="col-status">状态</th>
            <th class="col-type">类型</th>
            <th class="col-time">创建时间</th>
            <th class="col-action">操作</th>
          </tr>
        </thead>
        <tbody v-if="tasks.length > 0">
          <tr
            v-for="task in tasks"
            :key="task.taskUuid"
            class="task-row"
            @click="goToReplay(task.taskUuid, task.waylineUuid)"
          >
            <td class="col-name">
              <span class="task-name">{{ task.name }}</span>
              <!-- 冲突标识由后端下发（同设备执行时间窗重叠），前端不做计算 -->
              <span
                v-if="task.isConflicted"
                class="conflict-badge"
                title="该设备在此时段已有其他任务排期，同一机场同一时间只能执行一个架次"
              >
                <AlertTriangle :size="12" />
                时间冲突
              </span>
            </td>
            <td class="col-sn">
              <code class="sn-code">{{ task.sn }}</code>
            </td>
            <td class="col-id">
              <code class="task-id" :title="task.taskUuid">{{ task.taskUuid }}</code>
            </td>
            <td class="col-status">
              <span class="status-badge" :style="{ '--badge-color': STATUS_COLORS[task.status] }">
                {{ STATUS_LABELS[task.status] }}
              </span>
            </td>
            <td class="col-type">
              <TaskTypeLabel
                :task-type="task.taskType"
                :min-battery-capacity="task.minBatteryCapacity"
              />
            </td>
            <td class="col-time">{{ formatDateTime(task.createdAt) }}</td>
            <td class="col-action" @click.stop>
              <!-- CREATED: 执行（协议不支持手动执行；定时任务额外提示将按预定时间自动触发）/ 编辑 / 删除 -->
              <button
                v-if="task.status === 'CREATED'"
                class="action-btn execute"
                disabled
                :title="isScheduledTask(task) ? SCHEDULED_EXECUTE_TIP : UNSUPPORTED_ACTION_TIP"
              >
                <Play :size="12" />
                执行
              </button>
              <!-- 编辑：状态为待执行时展示；断点续飞等平台禁止编辑的任务置灰并给出原因 -->
              <button
                v-if="task.status === 'CREATED'"
                class="action-btn edit"
                :disabled="!isTaskEditable(task)"
                :title="getEditBlockTip(task)"
                @click="goToEdit(task.taskUuid)"
              >
                <Pencil :size="12" />
                编辑
              </button>
              <button
                v-if="task.status === 'CREATED'"
                class="action-btn delete"
                :disabled="actionLoading === task.taskUuid"
                @click="handleDelete(task.taskUuid)"
              >
                <Trash2 :size="12" />
                删除
              </button>
              <!-- EXECUTING: 暂停 / 取消（取消同理禁用并提示） -->
              <template v-if="task.status === 'EXECUTING'">
                <button
                  class="action-btn pause"
                  :disabled="actionLoading === task.taskUuid"
                  @click="handlePause(task.taskUuid)"
                >
                  <Pause :size="12" />
                  暂停
                </button>
                <button
                  class="action-btn cancel"
                  disabled
                  :title="UNSUPPORTED_ACTION_TIP"
                >
                  <XCircle :size="12" />
                  取消
                </button>
              </template>
              <!--
                PREPARING / QUEUED: 仅展示状态，不提供操作按钮（保守默认，待产品确认）。

                平台侧这两个状态在 SuspendableStatuses 内，挂起会通知设备取消已下发的
                准备动作，即「起飞前拦下」是平台支持的路径；同理它们也在
                EditableStatuses 内（可编辑）。DevKit 后端目前把编辑/删除硬锁在
                CREATED，一次性开放的操作与风险面超出本次改动范围，故此处先只做展示。

                另需注意：等到任务进入 EXECUTING 时无人机已经起飞，那时只能中断飞行，
                无法再「阻止执行」——若产品要求提供起飞前拦截手段，应加在这里。
              -->
              <!-- PAUSED: 恢复 -->
              <button
                v-if="task.status === 'PAUSED'"
                class="action-btn resume"
                :disabled="actionLoading === task.taskUuid"
                @click="handleResume(task.taskUuid)"
              >
                <Play :size="12" />
                恢复
              </button>
              <!-- SUCCESS/FAILED: 回放 -->
              <button
                v-if="task.status === 'SUCCESS' || task.status === 'FAILED'"
                class="action-btn replay"
                @click="goToReplay(task.taskUuid, task.waylineUuid)"
              >
                <Play :size="12" />
                回放
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="!loading && tasks.length === 0" class="empty-state">暂无飞行任务</div>
      <div v-if="loading && tasks.length === 0" class="loading-state">加载中…</div>
    </div>

    <!-- 分页 -->
    <MediaPagination
      v-if="pagination.totalCount > 0"
      :start-index="pagination.startIndex"
      :page-size="pagination.pageSize"
      :total-count="pagination.totalCount"
      :has-more="pagination.hasMore"
      @next-page="nextPage"
      @prev-page="prevPage"
      @change-page-size="changePageSize"
    />
  </div>
</template>

<style scoped>
.flight-task-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--app-bg);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-filter {
  padding: 6px 10px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
}

.total-count {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.refresh-btn,
.create-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--app-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.refresh-btn:hover:not(:disabled),
.create-btn:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.create-btn {
  background: var(--app-primary);
  border-color: var(--app-primary);
  color: #ffffff;
}

.create-btn:hover {
  background: var(--app-primary-hover);
  color: #ffffff;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.error-bar {
  padding: 8px 16px;
  background: var(--app-danger-bg);
  color: var(--app-danger);
  font-size: 13px;
  border-bottom: 1px solid var(--app-border);
}

.table-wrapper {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  padding: 0 16px;
}

.task-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}

.task-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
}

.task-table th {
  padding: 8px 14px;
  text-align: left;
  font-weight: 500;
  font-size: 12px;
  color: var(--app-text-muted);
  background: var(--app-bg);
  border-bottom: 1px solid var(--app-border);
  white-space: nowrap;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.task-table td {
  padding: 0 14px;
  height: 44px;
  font-size: 13px;
  color: var(--app-text);
  border-bottom: 1px solid color-mix(in srgb, var(--app-border) 50%, transparent);
  white-space: nowrap;
}

.col-name { width: 25%; min-width: 180px; }
.col-sn { width: 15%; min-width: 160px; }
.col-id { width: 6%; min-width: 140px; }
.col-status { width: 10%; min-width: 80px; }
.col-type { width: 8%; min-width: 70px; }
.col-time { width: 16%; min-width: 140px; }
.col-action { width: 20%; min-width: 140px; }

.task-row {
  cursor: pointer;
  transition: background var(--transition-base);
}

.task-row:hover {
  background: var(--app-hover-bg);
}

.task-name {
  color: var(--app-text);
  font-weight: 500;
}

.task-row:hover .task-name {
  color: var(--app-primary);
}

.conflict-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  color: var(--app-danger);
  background: color-mix(in srgb, var(--app-danger) 15%, transparent);
  white-space: nowrap;
  vertical-align: middle;
}

.sn-code {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--app-text-secondary);
}

/* 任务 ID 较长（uuid），超长时截断，完整值通过 title 悬浮查看 */
.task-id {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--app-text-muted);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
  color: var(--badge-color);
  background: color-mix(in srgb, var(--badge-color) 15%, transparent);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: transparent;
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-base);
  margin-right: 4px;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.execute {
  color: var(--app-success);
  border-color: color-mix(in srgb, var(--app-success) 40%, transparent);
}

.action-btn.execute:hover:not(:disabled) {
  background: var(--app-success);
  color: #ffffff;
}

.action-btn.pause {
  color: var(--app-warning);
  border-color: color-mix(in srgb, var(--app-warning) 40%, transparent);
}

.action-btn.pause:hover:not(:disabled) {
  background: var(--app-warning);
  color: #ffffff;
}

.action-btn.resume {
  color: var(--app-success);
  border-color: color-mix(in srgb, var(--app-success) 40%, transparent);
}

.action-btn.resume:hover:not(:disabled) {
  background: var(--app-success);
  color: #ffffff;
}

.action-btn.cancel,
.action-btn.delete {
  color: var(--app-danger);
  border-color: color-mix(in srgb, var(--app-danger) 40%, transparent);
}

.action-btn.cancel:hover:not(:disabled),
.action-btn.delete:hover:not(:disabled) {
  background: var(--app-danger);
  color: #ffffff;
}

.action-btn.replay {
  color: var(--app-primary);
  border-color: color-mix(in srgb, var(--app-primary) 40%, transparent);
}

.action-btn.replay:hover {
  background: var(--app-primary);
  color: #ffffff;
}

.action-btn.edit {
  color: var(--app-primary);
  border-color: color-mix(in srgb, var(--app-primary) 40%, transparent);
}

.action-btn.edit:hover {
  background: var(--app-primary);
  color: #ffffff;
}

.empty-state,
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 64px 16px;
  font-size: 14px;
  color: var(--app-text-muted);
}
</style>
