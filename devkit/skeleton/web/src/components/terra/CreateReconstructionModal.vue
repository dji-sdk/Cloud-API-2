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
import type { ReconstructionType } from '@/types/terra'
import { createReconstruction, createPanorama } from '@/api/terra'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
  created: []
}>()

const step = ref(1)
const selectedType = ref<ReconstructionType>('mesh_3d')
const folderUuidsText = ref('')
const taskName = ref('')
const wkt = ref('')
const submitting = ref(false)
const error = ref('')

const typeOptions: Array<{ value: ReconstructionType; label: string }> = [
  { value: 'ortho_2d', label: '2D 正射影像' },
  { value: 'mesh_3d', label: '3D 网格模型' },
  { value: 'point_cloud', label: '点云' },
  { value: 'panorama', label: '全景' },
]

const folderUuids = computed(() =>
  folderUuidsText.value.split('\n').map((s) => s.trim()).filter(Boolean)
)

const canSubmit = computed(() => taskName.value.trim() && folderUuids.value.length > 0)

function nextStep() {
  if (step.value < 3) {
    step.value++
  }
}

function prevStep() {
  if (step.value > 1) {
    step.value--
  }
}

async function handleSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  error.value = ''
  try {
    if (selectedType.value === 'panorama') {
      await createPanorama({
        name: taskName.value.trim(),
        folderUuids: folderUuids.value,
        wkt: wkt.value.trim() || undefined,
      })
    } else {
      await createReconstruction({
        name: taskName.value.trim(),
        folderUuids: folderUuids.value,
        reconstructionType: selectedType.value,
        wkt: wkt.value.trim() || undefined,
      })
    }
    emit('created')
    resetForm()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  step.value = 1
  selectedType.value = 'mesh_3d'
  folderUuidsText.value = ''
  taskName.value = ''
  wkt.value = ''
  error.value = ''
}

function handleClose() {
  resetForm()
  emit('close')
}
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <div class="modal-header">
        <h3>新建重建任务</h3>
        <button class="close-btn" @click="handleClose">&times;</button>
      </div>

      <div class="modal-body">
        <!-- Step 1: 选择类型 -->
        <div v-if="step === 1" class="step">
          <p class="step-title">选择重建类型</p>
          <div class="type-options">
            <label
              v-for="opt in typeOptions"
              :key="opt.value"
              class="type-option"
              :class="{ active: selectedType === opt.value }"
            >
              <input
                type="radio"
                :value="opt.value"
                v-model="selectedType"
              />
              {{ opt.label }}
            </label>
          </div>
        </div>

        <!-- Step 2: 输入媒体 ID -->
        <div v-if="step === 2" class="step">
          <p class="step-title">输入媒体文件夹 UUID（每行一个）</p>
          <textarea
            v-model="folderUuidsText"
            class="media-input"
            placeholder="uuid-folder-1&#10;uuid-folder-2"
            rows="6"
          ></textarea>
          <p class="hint">已输入 {{ folderUuids.length }} 个文件夹</p>
        </div>

        <!-- Step 3: 填写信息 -->
        <div v-if="step === 3" class="step">
          <p class="step-title">填写任务信息</p>
          <div class="form-group">
            <label>任务名称 <span class="required">*</span></label>
            <input v-model="taskName" type="text" placeholder="输入任务名称" />
          </div>
          <div class="form-group">
            <label>WKT 范围（可选）</label>
            <textarea v-model="wkt" rows="3" placeholder="POLYGON((...))"></textarea>
          </div>
          <div v-if="error" class="error-msg">{{ error }}</div>
        </div>
      </div>

      <div class="modal-footer">
        <div class="step-indicator">步骤 {{ step }} / 3</div>
        <div class="footer-actions">
          <button v-if="step > 1" class="btn" @click="prevStep">上一步</button>
          <button v-if="step < 3" class="btn btn-primary" @click="nextStep">下一步</button>
          <button
            v-if="step === 3"
            class="btn btn-primary"
            :disabled="!canSubmit || submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '确认创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--app-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--app-bg-panel);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-lg);
  width: 480px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--app-border);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: var(--app-text);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: var(--app-text-muted);
  transition: color var(--transition-base);
}

.close-btn:hover {
  color: var(--app-text);
}

.modal-body {
  padding: 20px;
}

.step-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
  color: var(--app-text-secondary);
}

.type-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.type-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--app-text-secondary);
  transition: all var(--transition-base);
}

.type-option:hover {
  border-color: var(--app-border-strong);
  background: var(--app-hover-bg);
}

.type-option.active {
  border-color: var(--app-primary);
  background: var(--app-primary-bg);
  color: var(--app-text);
}

.type-option input {
  margin: 0;
  accent-color: var(--app-primary);
}

.media-input {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-family: var(--font-mono);
  font-size: 13px;
  resize: vertical;
  outline: none;
}

.media-input:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px var(--app-primary-bg);
}

.hint {
  font-size: 12px;
  color: var(--app-text-muted);
  margin-top: 4px;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  font-size: 13px;
  margin-bottom: 4px;
  color: var(--app-text-secondary);
}

.required {
  color: var(--app-danger);
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-size: 13px;
  outline: none;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px var(--app-primary-bg);
}

.error-msg {
  color: var(--app-danger);
  font-size: 13px;
  margin-top: 8px;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid var(--app-border);
}

.step-indicator {
  font-size: 12px;
  color: var(--app-text-muted);
}

.footer-actions {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 8px 16px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text-secondary);
  cursor: pointer;
  font-size: 13px;
  transition: all var(--transition-base);
}

.btn:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.btn-primary {
  background: var(--app-primary);
  color: var(--app-text-inverse);
  border-color: var(--app-primary);
}

.btn-primary:hover {
  background: var(--app-primary-hover);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
