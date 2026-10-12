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
import type { MediaFileInfo, MediaFileType } from '@/types/media'
import { formatDateTime } from '@/utils/format'
import { Download, Trash2, Image, Film, File as FileIcon } from 'lucide-vue-next'

/** 文件类型对应的中文标签。 */
const FILE_TYPE_LABELS: Record<MediaFileType, string> = {
  photo: '照片',
  video: '视频',
  other: '其他',
}

interface Props {
  files: MediaFileInfo[]
  selectedIds: Set<number>
  loading: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  toggleSelect: [fileId: number]
  selectAll: []
  download: [fileId: number]
  delete: [fileId: number]
  preview: [index: number]
}>()

function formatSize(bytes: number): string {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  if (bytes < 1024 * 1024 * 1024) {
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`
}

const allSelected = computed<boolean>(() => {
  if (props.files.length === 0) {
    return false
  }
  return props.files.every(f => props.selectedIds.has(f.fileId))
})

/** 照片走图片查看器、视频走播放器；other 类型无预览能力。 */
function isPreviewable(fileType: MediaFileType): boolean {
  return fileType === 'photo' || fileType === 'video'
}
</script>

<template>
  <div class="media-table-wrap">
    <table class="media-table">
      <thead>
        <tr>
          <th class="col-check">
            <label class="custom-check">
              <input
                type="checkbox"
                :checked="allSelected"
                @change="emit('selectAll')"
              />
              <span class="check-mark" />
            </label>
          </th>
          <th class="col-name">文件名</th>
          <th class="col-type">类型</th>
          <th class="col-size">大小</th>
          <th class="col-device">设备</th>
          <th class="col-time">创建时间</th>
          <th class="col-actions">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading && files.length === 0">
          <td colspan="7" class="empty-cell">加载中...</td>
        </tr>
        <tr v-else-if="files.length === 0">
          <td colspan="7" class="empty-cell">暂无媒体文件</td>
        </tr>
        <tr
          v-for="(file, idx) in files"
          :key="file.fileId"
          class="file-row"
          :class="{
            'file-row--selected': selectedIds.has(file.fileId),
            'file-row--stripe': idx % 2 === 1,
          }"
        >
          <td class="col-check">
            <label class="custom-check">
              <input
                type="checkbox"
                :checked="selectedIds.has(file.fileId)"
                @change="emit('toggleSelect', file.fileId)"
              />
              <span class="check-mark" />
            </label>
          </td>
          <td class="col-name">
            <span class="file-icon">
              <Film v-if="file.fileType === 'video'" :size="14" :stroke-width="1.75" />
              <Image v-else-if="file.fileType === 'photo'" :size="14" :stroke-width="1.75" />
              <FileIcon v-else :size="14" :stroke-width="1.75" />
            </span>
            <span
              class="file-name-text"
              :class="{ 'file-name-text--clickable': isPreviewable(file.fileType) }"
              @click="isPreviewable(file.fileType) && emit('preview', idx)"
            >
              {{ file.fileName }}
            </span>
          </td>
          <td class="col-type">
            <span class="type-badge" :class="`type-badge--${file.fileType}`">
              {{ FILE_TYPE_LABELS[file.fileType] }}
            </span>
          </td>
          <td class="col-size">{{ formatSize(file.fileSize) }}</td>
          <td class="col-device">{{ file.deviceSn }}</td>
          <td class="col-time">{{ formatDateTime(file.createTime) }}</td>
          <td class="col-actions">
            <button class="action-btn" title="下载" @click="emit('download', file.fileId)">
              <Download :size="14" :stroke-width="1.75" />
            </button>
            <button class="action-btn action-btn--delete" title="删除" @click="emit('delete', file.fileId)">
              <Trash2 :size="14" :stroke-width="1.75" />
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.media-table-wrap {
  flex: 1;
  overflow: auto;
}

.media-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.media-table th {
  text-align: left;
  padding: 10px 12px;
  color: var(--app-text-muted);
  font-weight: 600;
  font-size: 12px;
  letter-spacing: 0.3px;
  border-bottom: 1px solid var(--app-border);
  position: sticky;
  top: 0;
  background: var(--app-bg-panel);
  z-index: 1;
}

.media-table td {
  padding: 10px 12px;
  color: var(--app-text);
  border-bottom: 1px solid var(--app-border);
}

.file-row {
  transition: background var(--transition-fast);
}

.file-row--stripe {
  background: rgba(255, 255, 255, 0.015);
}

.file-row:hover {
  background: var(--app-hover-bg);
}

.file-row--selected {
  background: var(--app-primary-bg);
}

.col-check {
  width: 36px;
  text-align: center;
}

.col-name {
  min-width: 200px;
}

.col-type { width: 80px; }
.col-size { width: 90px; }
.col-device { width: 140px; }
.col-time { width: 150px; }
.col-actions { width: 80px; }

/* 自定义 Checkbox */
.custom-check {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
}

.custom-check input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.check-mark {
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  position: relative;
}

.custom-check input:checked + .check-mark {
  background: var(--app-primary);
  border-color: var(--app-primary);
}

.custom-check input:checked + .check-mark::after {
  content: '';
  position: absolute;
  left: 4.5px;
  top: 1.5px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 1.5px 1.5px 0;
  transform: rotate(45deg);
}

.custom-check:hover .check-mark {
  border-color: var(--app-primary);
}

/* 文件名列 */
.col-name {
  display: flex;
  align-items: center;
}

.file-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text-muted);
  margin-right: 8px;
  flex-shrink: 0;
}

.file-name-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-name-text--clickable {
  cursor: pointer;
  transition: color var(--transition-fast);
}

.file-name-text--clickable:hover {
  color: var(--app-primary);
}

/* 类型标签 */
.type-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.type-badge--photo {
  background: rgba(34, 197, 94, 0.10);
  color: #4ade80;
}

.type-badge--video {
  background: rgba(59, 130, 246, 0.10);
  color: #60a5fa;
}

.type-badge--other {
  background: rgba(148, 163, 184, 0.12);
  color: #94a3b8;
}

.empty-cell {
  text-align: center;
  padding: 40px 12px;
  color: var(--app-text-muted);
}

/* 操作按钮 */
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: none;
  border: none;
  cursor: pointer;
  border-radius: var(--radius-sm);
  color: var(--app-text-secondary);
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.action-btn--delete:hover {
  background: rgba(239, 68, 68, 0.12);
  color: var(--app-danger);
}
</style>
