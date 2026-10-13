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
import type { TaskStatus } from '@/types/terra'
import { retryTask, stopTask } from '@/api/terra'

const props = defineProps<{
  task: TaskStatus
}>()

const emit = defineEmits<{
  viewReport: [taskId: string]
  updated: []
}>()

async function handleStop() {
  try {
    await stopTask(props.task.taskId)
    emit('updated')
  } catch (e) {
    console.error('停止任务失败:', e)
  }
}

async function handleRetry() {
  try {
    await retryTask(props.task.taskId)
    emit('updated')
  } catch (e) {
    console.error('重试任务失败:', e)
  }
}
</script>

<template>
  <div class="task-card" :class="task.state">
    <div class="task-header">
      <span class="task-id">{{ task.taskId }}</span>
      <span class="task-badge" :class="task.state">
        {{ task.state === 'processing' ? '处理中' : task.state === 'success' ? '已完成' : '失败' }}
      </span>
    </div>
    <div class="task-progress-section" v-if="task.state === 'processing'">
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: task.progress + '%' }"></div>
      </div>
      <span class="progress-text">{{ task.progress }}%</span>
    </div>
    <div class="task-info" v-if="task.state === 'success'">
      <span class="info-label">输出路径:</span>
      <span class="info-value">{{ task.sfmOutputObjectPrefix }}</span>
    </div>
    <div class="task-actions">
      <button v-if="task.state === 'processing'" class="btn btn-warning" @click="handleStop">
        停止
      </button>
      <button v-if="task.state === 'failed'" class="btn btn-retry" @click="handleRetry">
        重试
      </button>
      <button v-if="task.state === 'success'" class="btn btn-info" @click="emit('viewReport', task.taskId)">
        查看报告
      </button>
    </div>
  </div>
</template>

<style scoped>
.task-card {
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  padding: 16px;
  background: var(--app-bg-card);
  transition: box-shadow var(--transition-base);
}
.task-card:hover {
  box-shadow: var(--shadow-sm);
}
.task-card.processing {
  border-left: 3px solid var(--app-info);
}
.task-card.success {
  border-left: 3px solid var(--app-success);
}
.task-card.failed {
  border-left: 3px solid var(--app-danger);
}
.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.task-id {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--app-text);
}
.task-badge {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.task-badge.processing {
  background: var(--app-info-bg);
  color: var(--app-info);
}
.task-badge.success {
  background: var(--app-success-bg);
  color: var(--app-success);
}
.task-badge.failed {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}
.task-progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.progress-bar {
  flex: 1;
  height: 6px;
  background: var(--app-bg-elevated);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: var(--app-primary);
  transition: width 0.3s ease;
  border-radius: var(--radius-full);
}
.progress-text {
  font-size: 13px;
  font-weight: 500;
  min-width: 40px;
  text-align: right;
  color: var(--app-text);
}
.task-info {
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--app-text-muted);
}
.info-label {
  margin-right: 4px;
}
.info-value {
  font-family: var(--font-mono);
  color: var(--app-text-secondary);
}
.task-actions {
  display: flex;
  gap: 8px;
}
.btn {
  padding: 6px 14px;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 13px;
  transition: opacity var(--transition-base);
}
.btn:hover {
  opacity: 0.85;
}
.btn-warning {
  background: var(--app-warning-bg);
  color: var(--app-warning);
}
.btn-retry {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}
.btn-info {
  background: var(--app-info-bg);
  color: var(--app-info);
}
</style>
