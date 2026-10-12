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
import { ref, computed } from 'vue'
import type { OutputType } from '@/types/reconstruction'
import { ArrowLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps<{
  selectedPhotoCount: number
  selectedFolderCount: number
  hasFolderInput: boolean
}>()

const emit = defineEmits<{
  close: []
  submit: [form: { name: string; outputType: OutputType; useGcp: boolean; interestRegion: boolean; coordinateSystem: string }]
  'update:interestRegion': [value: boolean]
}>()

const now = new Date()
const defaultName = `新建模型重建-${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')} (UTC+08)`

const modelName = ref(defaultName)
const outputType = ref<OutputType>('3d')
const useGcp = ref(false)
const interestRegion = ref(false)
const coordinateSystem = ref('WGS 84')

const canSubmit = computed(() => props.hasFolderInput || props.selectedPhotoCount >= 5)

function toggleInterestRegion() {
  interestRegion.value = !interestRegion.value
  emit('update:interestRegion', interestRegion.value)
}

function handleSubmit() {
  if (!canSubmit.value) return
  emit('submit', {
    name: modelName.value,
    outputType: outputType.value,
    useGcp: useGcp.value,
    interestRegion: interestRegion.value,
    coordinateSystem: coordinateSystem.value,
  })
}
</script>

<template>
  <div class="reconstruction-config">
    <!-- Header -->
    <div class="config-header">
      <button class="back-btn" @click="emit('close')">
        <ArrowLeft :size="18" />
      </button>
      <h3 class="header-title">新建模型重建</h3>
    </div>

    <!-- Scrollable content -->
    <div class="config-body">
      <!-- Model Name -->
      <div class="form-section">
        <label class="section-label">模型名称</label>
        <input
          v-model="modelName"
          type="text"
          class="text-input"
          maxlength="50"
        />
      </div>

      <!-- Data Selection Summary -->
      <div class="form-section">
        <div class="section-header">
          <span class="section-label">选择数据</span>
        </div>
        <div class="data-summary">
          <div class="summary-row">
            <span class="summary-label">重建照片</span>
            <span class="summary-value">
              照片数量: {{ selectedPhotoCount }}
            </span>
          </div>
          <div v-if="selectedFolderCount > 0" class="summary-detail">
            已选择 {{ selectedFolderCount }} 个文件夹
          </div>
          <div v-else class="summary-hint">请在中间面板选择照片文件夹</div>
        </div>
      </div>

      <!-- Folder UUID Input (from parent) -->
      <slot name="folder-input" />

      <!-- GCP Toggle -->
      <div class="form-section toggle-section">
        <span class="section-label">像控点</span>
        <button
          class="toggle-btn"
          :class="{ active: useGcp }"
          @click="useGcp = !useGcp"
        >
          <span class="toggle-knob" />
        </button>
      </div>

      <!-- Output Type -->
      <div class="form-section">
        <span class="section-label">模型成果</span>
        <div class="output-tabs">
          <button
            class="output-tab"
            :class="{ active: outputType === '2d' }"
            @click="outputType = '2d'"
          >
            二维
          </button>
          <button
            class="output-tab"
            :class="{ active: outputType === '3d' }"
            @click="outputType = '3d'"
          >
            三维
          </button>
        </div>
      </div>

      <!-- Interest Region Toggle -->
      <div class="form-section toggle-section">
        <span class="section-label">兴趣区域</span>
        <button
          class="toggle-btn"
          :class="{ active: interestRegion }"
          @click="toggleInterestRegion"
        >
          <span class="toggle-knob" />
        </button>
      </div>

      <!-- Coordinate System -->
      <div class="form-section">
        <span class="section-label">坐标系选择</span>
        <div class="coord-select">
          <div class="coord-label">已知坐标系</div>
          <div class="coord-value">
            <span>{{ coordinateSystem }}</span>
            <span class="coord-code">EPSG:4326</span>
            <ChevronRight :size="14" class="coord-arrow" />
          </div>
        </div>
      </div>

      <!-- Storage Location -->
      <div class="form-section">
        <div class="storage-row">
          <span class="section-label">模型存储位置</span>
          <span class="storage-value">云端建模</span>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="config-footer">
      <div v-if="!canSubmit" class="submit-hint">
        请输入媒体文件夹 UUID 后开始重建
      </div>
      <button
        class="submit-btn"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        开始重建
      </button>
    </div>
  </div>
</template>

<style scoped>
.reconstruction-config {
  width: 320px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--app-border);
  background: var(--app-bg-panel);
  overflow: hidden;
}

.config-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--app-border);
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: var(--app-text);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-base);
}

.back-btn:hover {
  background: var(--app-hover-bg);
}

.header-title {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: var(--app-text);
}

.config-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.form-section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.section-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text);
  display: block;
  margin-bottom: 8px;
}

.toggle-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toggle-section .section-label {
  margin-bottom: 0;
}

.text-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
  transition: border-color var(--transition-base);
}

.text-input:focus {
  border-color: var(--app-primary);
}

.data-summary {
  padding: 10px 12px;
  background: var(--app-bg-surface);
  border-radius: var(--radius-sm);
  border: 1px solid var(--app-border);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.summary-value {
  font-size: 12px;
  color: var(--app-text-muted);
}

.summary-detail {
  margin-top: 6px;
  font-size: 12px;
  color: var(--app-primary);
}

.summary-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--app-text-muted);
}

.toggle-btn {
  position: relative;
  width: 40px;
  height: 22px;
  border-radius: 11px;
  border: none;
  background: var(--app-bg-elevated);
  cursor: pointer;
  transition: background var(--transition-base);
}

.toggle-btn.active {
  background: var(--app-primary);
}

.toggle-knob {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: white;
  transition: transform var(--transition-base);
}

.toggle-btn.active .toggle-knob {
  transform: translateX(18px);
}

.output-tabs {
  display: flex;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.output-tab {
  flex: 1;
  padding: 8px 0;
  border: none;
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.output-tab + .output-tab {
  border-left: 1px solid var(--app-border-strong);
}

.output-tab.active {
  background: var(--app-primary);
  color: var(--app-text-inverse);
}

.output-tab:hover:not(.active) {
  background: var(--app-hover-bg);
}

.coord-select {
  border: 1px solid var(--app-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.coord-label {
  padding: 8px 12px;
  font-size: 12px;
  color: var(--app-text-muted);
  background: var(--app-bg-surface);
  border-bottom: 1px solid var(--app-border);
}

.coord-value {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  font-size: 13px;
  color: var(--app-text);
  cursor: pointer;
}

.coord-code {
  margin-left: 8px;
  font-size: 11px;
  color: var(--app-text-muted);
}

.coord-arrow {
  margin-left: auto;
  color: var(--app-text-muted);
}

.storage-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.storage-row .section-label {
  margin-bottom: 0;
}

.storage-value {
  font-size: 12px;
  color: var(--app-text-secondary);
}

.config-footer {
  padding: 16px;
  border-top: 1px solid var(--app-border);
}

.submit-btn {
  width: 100%;
  padding: 12px 0;
  border: none;
  border-radius: var(--radius-md);
  background: var(--app-primary);
  color: var(--app-text-inverse);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-base);
}

.submit-btn:hover:not(:disabled) {
  background: var(--app-primary-hover);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-hint {
  font-size: 12px;
  color: var(--app-warning, #e6a817);
  text-align: center;
  margin-bottom: 8px;
}
</style>
