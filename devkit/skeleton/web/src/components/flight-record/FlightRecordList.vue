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
import { useRouter } from 'vue-router'
import { RefreshCw, Play } from 'lucide-vue-next'
import { useFlightTaskList } from '@/composables/useFlightTaskList'
import MediaPagination from '@/components/media/MediaPagination.vue'
import TaskTypeLabel from '@/components/flight-task/TaskTypeLabel.vue'
import {
  FLIGHT_TASK_STATUS_COLORS as STATUS_COLORS,
  FLIGHT_TASK_STATUS_LABELS as STATUS_LABELS,
  FLIGHT_TASK_STATUS_OPTIONS as STATUS_OPTIONS,
} from '@/types/flightTask'
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

function goToReplay(taskUuid: string, waylineUuid: string): void {
  router.push({ path: '/flight-replay', query: { task_id: taskUuid, wayline_uuid: waylineUuid } })
}
</script>

<template>
  <div class="flight-record-list">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <select
          v-model="filters.status"
          class="status-filter"
        >
          <option
            v-for="opt in STATUS_OPTIONS"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>
        <span class="total-count">
          共 {{ pagination.totalCount }} 条记录
        </span>
      </div>
      <button
        class="refresh-btn"
        :disabled="loading"
        @click="fetchTasks()"
      >
        <RefreshCw :size="14" :stroke-width="2" :class="{ spinning: loading }" />
        <span>刷新</span>
      </button>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-bar">
      {{ error }}
    </div>

    <!-- 表格 -->
    <div class="table-wrapper">
      <table class="task-table">
        <thead>
          <tr>
            <th class="col-name">任务名称</th>
            <th class="col-sn">设备 SN</th>
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
            </td>
            <td class="col-sn">
              <code class="sn-code">{{ task.sn }}</code>
            </td>
            <td class="col-status">
              <span
                class="status-badge"
                :style="{ '--badge-color': STATUS_COLORS[task.status] }"
              >
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
            <td class="col-action">
              <button
                class="replay-btn"
                @click.stop="goToReplay(task.taskUuid, task.waylineUuid)"
              >
                <Play :size="12" :stroke-width="2" />
                回放
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 空状态 -->
      <div v-if="!loading && tasks.length === 0" class="empty-state">
        暂无飞行记录
      </div>

      <!-- 加载状态 -->
      <div v-if="loading && tasks.length === 0" class="loading-state">
        加载中…
      </div>
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
.flight-record-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--app-bg);
}

/* ── 工具栏 ── */

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

.status-filter {
  padding: 6px 10px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
  transition: border-color var(--transition-base);
}

.status-filter:focus {
  border-color: var(--app-primary);
}

.total-count {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.refresh-btn {
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

.refresh-btn:hover:not(:disabled) {
  background: var(--app-hover-bg);
  color: var(--app-text);
  border-color: var(--app-border-strong);
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ── 错误提示 ── */

.error-bar {
  padding: 8px 16px;
  background: rgba(239, 83, 80, 0.12);
  color: #ef5350;
  font-size: 13px;
  border-bottom: 1px solid var(--app-border);
}

/* ── 表格 ── */

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
  overflow: hidden;
  text-overflow: ellipsis;
}

.col-name { width: 30%; min-width: 200px; }
.col-sn { width: 20%; min-width: 180px; }
.col-status { width: 10%; min-width: 80px; }
.col-type { width: 10%; min-width: 70px; }
.col-time { width: 18%; min-width: 150px; }
.col-action { width: 12%; min-width: 80px; text-align: center; }

.task-row {
  cursor: pointer;
  transition: background var(--transition-base);
}

.task-row:hover {
  background: var(--app-hover-bg);
}

.task-row:hover .task-name {
  color: var(--app-primary);
}

.task-name {
  color: var(--app-text);
  font-weight: 500;
  transition: color var(--transition-base);
}

.sn-code {
  font-family: var(--font-mono, 'SF Mono', 'Fira Code', monospace);
  font-size: 12px;
  color: var(--app-text-secondary);
}

/* ── 状态徽章 ── */

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

/* ── 回放按钮 ── */

.replay-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border: 1px solid color-mix(in srgb, var(--app-primary) 40%, transparent);
  border-radius: 14px;
  background: transparent;
  color: var(--app-primary);
  font-size: 12px;
  cursor: pointer;
  transition: all var(--transition-base);
  opacity: 0.7;
}

.task-row:hover .replay-btn {
  opacity: 1;
}

.replay-btn:hover {
  background: var(--app-primary);
  border-color: var(--app-primary);
  color: #ffffff;
}

/* ── 空状态 / 加载状态 ── */

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
