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
import { computed } from 'vue'
import type { ModelFile } from '@/types/terra'
import { formatDateTime } from '@/utils/format'

const props = defineProps<{
  models: ModelFile[]
  selectedIds: number[]
  total: number
}>()

const emit = defineEmits<{
  select: [fileId: number]
  selectAll: []
  preview: [fileId: number]
  download: [fileId: number]
  delete: [fileId: number]
  viewReport: [taskId: string]
  viewImages: [taskId: string]
}>()

const allSelected = computed(
  () => props.models.length > 0 && props.models.every((m) => props.selectedIds.includes(m.fileId))
)

const typeLabels: Record<string, string> = {
  ortho_2d: '正射影像',
  mesh_3d: '三维网格',
  point_cloud: '点云',
  panorama: '全景',
  '3dgs': '3DGS',
  lidar: '激光雷达',
}
</script>

<template>
  <div class="model-list">
    <table>
      <thead>
        <tr>
          <th class="col-check">
            <input type="checkbox" :checked="allSelected" @change="emit('selectAll')" />
          </th>
          <th class="col-name">名称</th>
          <th class="col-type">类型</th>
          <th class="col-state">状态</th>
          <th class="col-date">创建时间</th>
          <th class="col-actions">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="model in models"
          :key="model.fileId"
          :class="{ 'row-selected': selectedIds.includes(model.fileId) }"
        >
          <td class="col-check">
            <input
              type="checkbox"
              :checked="selectedIds.includes(model.fileId)"
              @change="emit('select', model.fileId)"
            />
          </td>
          <td class="col-name">{{ model.name }}</td>
          <td class="col-type">{{ typeLabels[model.reconstructionType] || model.reconstructionType }}</td>
          <td class="col-state">
            <span class="state-badge" :class="model.state">
              {{ model.state }}
            </span>
          </td>
          <td class="col-date">{{ formatDateTime(model.createdAt) }}</td>
          <td class="col-actions">
            <button class="action-link" @click="emit('preview', model.fileId)">预览</button>
            <button class="action-link action-link--disabled" disabled title="SDK 未提供有效下载链接">下载</button>
            <button class="action-link" @click="emit('viewReport', model.uuid)">报告</button>
            <button class="action-link" @click="emit('viewImages', model.uuid)">影像</button>
            <button class="action-link action-link--danger" @click="emit('delete', model.fileId)">删除</button>
          </td>
        </tr>
        <tr v-if="models.length === 0">
          <td colspan="6" class="empty-row">暂无模型数据</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.model-list {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

thead {
  background: var(--app-bg-surface);
}

th,
td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--app-border);
}

th {
  font-weight: 500;
  color: var(--app-text-muted);
  font-size: 12px;
}

td {
  color: var(--app-text-secondary);
}

.col-check {
  width: 40px;
}

.col-check input[type="checkbox"] {
  accent-color: var(--app-primary);
}

.col-name {
  min-width: 180px;
  color: var(--app-text);
}

.col-type {
  width: 100px;
}

.col-state {
  width: 80px;
}

.col-date {
  width: 160px;
}

.col-actions {
  width: 200px;
}

tr:hover td {
  background: var(--app-hover-bg);
}

.row-selected td {
  background: var(--app-primary-bg);
}

.state-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.state-badge.success {
  background: var(--app-success-bg);
  color: var(--app-success);
}

.state-badge.processing {
  background: var(--app-info-bg);
  color: var(--app-info);
}

.state-badge.failed {
  background: var(--app-danger-bg);
  color: var(--app-danger);
}

.action-link {
  background: none;
  border: none;
  color: var(--app-primary);
  cursor: pointer;
  font-size: 12px;
  padding: 2px 4px;
  transition: opacity var(--transition-base);
}

.action-link:hover {
  opacity: 0.8;
}

.action-link--disabled {
  color: var(--app-text-muted);
  cursor: not-allowed;
  opacity: 0.5;
}

.action-link--disabled:hover {
  opacity: 0.5;
}

.action-link--danger {
  color: var(--app-danger);
}

.empty-row {
  text-align: center;
  padding: 32px 0;
  color: var(--app-text-muted);
}
</style>
