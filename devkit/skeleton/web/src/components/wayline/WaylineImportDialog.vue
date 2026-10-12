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
import { Upload, FileArchive } from 'lucide-vue-next'

interface Props {
  loading: boolean
  error?: string
}

defineProps<Props>()

const emit = defineEmits<{
  confirm: [file: File, name: string]
  cancel: []
}>()

const selectedFile = ref<File | null>(null)
const waylineName = ref('')
const dragActive = ref(false)
const dropError = ref('')

function handleFileChange(event: Event): void {
  const input = event.target as HTMLInputElement
  if (input.files && input.files.length > 0) {
    dropError.value = ''
    selectedFile.value = input.files[0]
    if (!waylineName.value) {
      waylineName.value = input.files[0].name.replace(/\.kmz$/i, '')
    }
  }
}

function handleDrop(event: DragEvent): void {
  dragActive.value = false
  const files = event.dataTransfer?.files
  if (files && files.length > 0) {
    const file = files[0]
    if (file.name.toLowerCase().endsWith('.kmz')) {
      dropError.value = ''
      selectedFile.value = file
      if (!waylineName.value) {
        waylineName.value = file.name.replace(/\.kmz$/i, '')
      }
    } else {
      dropError.value = `不支持的文件格式「${file.name}」，仅支持 .kmz 文件`
    }
  }
}

function handleDragOver(event: DragEvent): void {
  event.preventDefault()
  dragActive.value = true
}

function handleDragLeave(): void {
  dragActive.value = false
}

function handleConfirm(): void {
  if (selectedFile.value) {
    emit('confirm', selectedFile.value, waylineName.value)
  }
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}
</script>

<template>
  <div class="dialog-overlay" @click.self="emit('cancel')">
    <div class="dialog-card">
      <div class="dialog-header">
        <Upload :size="20" class="dialog-icon" />
        <h3 class="dialog-title">导入航线</h3>
      </div>

      <div class="dialog-body">
        <!-- 拖拽上传区域 -->
        <div
          class="drop-zone"
          :class="{ 'drop-zone--active': dragActive, 'drop-zone--has-file': selectedFile }"
          @drop.prevent="handleDrop"
          @dragover="handleDragOver"
          @dragleave="handleDragLeave"
        >
          <template v-if="!selectedFile">
            <Upload :size="32" class="drop-icon" />
            <p class="drop-text">拖拽 KMZ 文件到此处，或</p>
            <label class="drop-btn">
              选择文件
              <input
                type="file"
                accept=".kmz"
                class="file-input"
                @change="handleFileChange"
              />
            </label>
          </template>
          <template v-else>
            <FileArchive :size="24" class="file-icon" />
            <div class="file-info">
              <span class="file-name">{{ selectedFile.name }}</span>
              <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
            </div>
            <label class="drop-btn drop-btn--small">
              重新选择
              <input
                type="file"
                accept=".kmz"
                class="file-input"
                @change="handleFileChange"
              />
            </label>
          </template>
        </div>

        <p v-if="dropError" class="drop-error">{{ dropError }}</p>
        <p v-if="error" class="drop-error">{{ error }}</p>

        <!-- 航线名称 -->
        <div class="form-field">
          <label class="form-label">航线名称</label>
          <input
            v-model="waylineName"
            type="text"
            class="form-input"
            placeholder="输入航线名称（可选，默认使用文件名）"
          />
        </div>
      </div>

      <div class="dialog-footer">
        <button class="btn btn--ghost" :disabled="loading" @click="emit('cancel')">
          取消
        </button>
        <button
          class="btn btn--primary"
          :disabled="!selectedFile || loading"
          @click="handleConfirm"
        >
          {{ loading ? '导入中…' : '确认导入' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 9000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}

.dialog-card {
  width: 460px;
  max-width: 90vw;
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 24px 0;
}

.dialog-icon {
  color: var(--app-primary);
  flex-shrink: 0;
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.dialog-body {
  padding: 16px 24px;
}

.drop-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px 20px;
  border: 2px dashed var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  transition: all var(--transition-base);
  cursor: pointer;
}

.drop-zone--active {
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
}

.drop-zone--has-file {
  flex-direction: row;
  padding: 16px 20px;
  gap: 12px;
}

.drop-error {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--app-danger);
}

.drop-icon {
  color: var(--app-text-muted);
}

.drop-text {
  font-size: 13px;
  color: var(--app-text-secondary);
  margin: 0;
}

.drop-btn {
  padding: 6px 14px;
  border-radius: var(--radius-md);
  background: var(--app-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
}

.drop-btn:hover {
  background: var(--app-primary-hover);
}

.drop-btn--small {
  padding: 4px 10px;
  font-size: 12px;
  background: var(--app-bg-card);
  color: var(--app-text-secondary);
  border: 1px solid var(--app-border);
  margin-left: auto;
}

.drop-btn--small:hover {
  background: var(--app-bg-surface);
  border-color: var(--app-border-strong);
  color: var(--app-text);
}

.file-input {
  display: none;
}

.file-icon {
  color: var(--app-primary);
  flex-shrink: 0;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.file-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 11px;
  color: var(--app-text-muted);
}

.form-field {
  margin-top: 16px;
}

.form-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--app-text-secondary);
  margin-bottom: 6px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
  transition: all var(--transition-base);
}

.form-input:hover {
  border-color: var(--app-border-strong);
}

.form-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 2px var(--app-primary-bg);
}

.form-input::placeholder {
  color: var(--app-text-muted);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px 20px;
}

.btn {
  padding: 8px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn--ghost {
  background: var(--app-bg-surface);
  border-color: var(--app-border);
  color: var(--app-text-secondary);
}

.btn--ghost:hover:not(:disabled) {
  background: var(--app-bg-card);
  border-color: var(--app-border-strong);
}

.btn--primary {
  background: var(--app-primary);
  color: #fff;
  box-shadow: 0 2px 8px rgba(6, 182, 212, 0.25);
}

.btn--primary:hover:not(:disabled) {
  background: var(--app-primary-hover);
  box-shadow: 0 4px 12px rgba(6, 182, 212, 0.35);
}
</style>
