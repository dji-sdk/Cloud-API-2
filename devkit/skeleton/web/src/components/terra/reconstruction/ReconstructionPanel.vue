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
import { ref, computed, watch } from 'vue'
import type { OutputType } from '@/types/reconstruction'
import type { Folder } from '@/types/terra'
import { createReconstruction, fetchFolders } from '@/api/terra'
import ReconstructionConfig from './ReconstructionConfig.vue'
import PhotoBrowser from './PhotoBrowser.vue'
import ReconstructionMap from './ReconstructionMap.vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
  created: [task: { id: number; uuid: string; jobUuid: string; status: string }]
}>()

const mapRef = ref<InstanceType<typeof ReconstructionMap> | null>(null)
const interestRegionEnabled = ref(false)
const selectedExecutionIds = ref<string[]>([])
const selectedFolderIds = ref<number[]>([])
const selectedMediaFolderIds = ref<number[]>([])
const selectedPhotoCount = ref(0)
const selectedFolderCount = ref(0)
const submitting = ref(false)
const errorMsg = ref('')
const folderUuidsText = ref('')

const hasFolderInput = computed(() => getFinalFolderUuids().length > 0 || selectedFolderIds.value.length > 0 || selectedMediaFolderIds.value.length > 0)

// 文件夹列表（供 PhotoBrowser "按文件夹选择" tab 使用）
const folders = ref<Folder[]>([])
const foldersLoading = ref(false)
const foldersError = ref('')

async function loadFolders() {
  if (folders.value.length > 0 || foldersLoading.value) return
  foldersLoading.value = true
  foldersError.value = ''
  try {
    folders.value = await fetchFolders()
  } catch (e) {
    // 记录错误供前端区分"无文件夹"与"加载失败"，并提供重试入口
    foldersError.value = e instanceof Error ? e.message : '文件夹加载失败'
  } finally {
    foldersLoading.value = false
  }
}

/** 面板打开时加载文件夹 */
watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      loadFolders()
    }
  },
  { immediate: true },
)

/** 中间面板选择变化时，自动同步到 textarea */
watch(selectedExecutionIds, (ids) => {
  folderUuidsText.value = ids.join('\n')
})

/**
 * 合并中间面板选择 + 手动输入，去重后作为最终提交的 folderUuids。
 * 兼容旧逻辑：按航线选中时 textarea 同步任务 UUID，但提交走 folderId，此处仅用于校验提示。
 */
function getFinalFolderUuids(): string[] {
  const fromPanel = selectedExecutionIds.value
  const fromText = folderUuidsText.value.split('\n').map((s) => s.trim()).filter(Boolean)
  return [...new Set([...fromPanel, ...fromText])]
}

function handleSelectionChange(payload: { ids: Array<string | number>; folderIds: number[]; photoCount: number; folderCount: number }) {
  selectedFolderIds.value = payload.folderIds ?? []
  selectedMediaFolderIds.value = payload.folderIds ?? []
  selectedExecutionIds.value = (payload.ids ?? []).map(String)
  selectedPhotoCount.value = payload.photoCount
  selectedFolderCount.value = payload.folderCount
}

function handleInterestRegionUpdate(value: boolean) {
  interestRegionEnabled.value = value
}

async function handleSubmit(form: {
  name: string
  outputType: OutputType
  useGcp: boolean
  interestRegion: boolean
  coordinateSystem: string
}) {
  // 按文件夹 tab 选中 → selectedFolderIds；按航线选中 → 从 execution.folderId 收集
  const folderIds = selectedFolderIds.value
  const execFolderIds = selectedMediaFolderIds.value
  // 手动输入 → 可能为 UUID
  const manualUuids = folderUuidsText.value.split('\n').map((s) => s.trim()).filter(Boolean)
  const allFolderIds = [...new Set([...folderIds, ...execFolderIds])]

  if (allFolderIds.length === 0 && manualUuids.length === 0) {
    errorMsg.value = '请在中间面板选择航线执行或文件夹，或手动输入媒体文件夹'
    return
  }
  submitting.value = true
  errorMsg.value = ''
  const wkt = mapRef.value?.wkt ?? ''
  try {
    const reconstructionType = form.outputType === '2d' ? 'ortho_2d' as const : 'mesh_3d' as const
    const result = await createReconstruction({
      name: form.name,
      folderIds: allFolderIds.length > 0 ? allFolderIds : undefined,
      folderUuids: allFolderIds.length > 0 ? undefined : manualUuids,
      reconstructionType,
      wkt: wkt || undefined,
    })
    emit('created', result)
  } catch (e) {
    errorMsg.value = e instanceof Error ? e.message : '创建失败'
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  interestRegionEnabled.value = false
  selectedExecutionIds.value = []
  selectedFolderIds.value = []
  selectedMediaFolderIds.value = []
  selectedPhotoCount.value = 0
  selectedFolderCount.value = 0
  folderUuidsText.value = ''
  errorMsg.value = ''
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="reconstruction-panel">
      <ReconstructionConfig
        :selected-photo-count="selectedPhotoCount"
        :selected-folder-count="selectedFolderCount"
        :has-folder-input="hasFolderInput"
        @close="handleClose"
        @submit="handleSubmit"
        @update:interest-region="handleInterestRegionUpdate"
      >
        <template #folder-input>
          <div class="folder-input-section">
            <label class="folder-label">媒体文件夹</label>
            <textarea
              v-model="folderUuidsText"
              class="folder-textarea"
              :placeholder="selectedExecutionIds.length > 0
                ? '已通过中间面板选择（可追加手动输入）'
                : '请在右侧面板选择航线或文件夹，或手动输入 UUID（每行一个）'"
              rows="3"
              :readonly="selectedExecutionIds.length > 0"
            ></textarea>
          </div>
        </template>
      </ReconstructionConfig>
      <PhotoBrowser
        :folders="folders"
        :folders-error="foldersError"
        @reload-folders="loadFolders"
        @selection-change="handleSelectionChange"
      />
      <ReconstructionMap
        ref="mapRef"
        :interest-region-enabled="interestRegionEnabled"
      />

      <div v-if="errorMsg" class="error-toast">
        {{ errorMsg }}
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.reconstruction-panel {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  background: var(--app-bg);
}

.error-toast {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  background: var(--app-danger);
  color: var(--app-text-inverse);
  border-radius: var(--radius-md);
  font-size: 13px;
  z-index: 110;
  animation: fadeIn 0.2s ease-out;
}

.folder-input-section {
  margin-top: 12px;
}

.folder-label {
  display: block;
  font-size: 12px;
  color: var(--app-text-muted);
  margin-bottom: 6px;
}

.folder-textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid var(--app-border-strong);
  border-radius: var(--radius-sm);
  background: var(--app-bg-surface);
  color: var(--app-text);
  font-family: var(--font-mono);
  font-size: 12px;
  resize: vertical;
  outline: none;
}

.folder-textarea:focus {
  border-color: var(--app-primary);
  box-shadow: 0 0 0 3px var(--app-primary-bg);
}

.folder-textarea[readonly] {
  opacity: 0.7;
  cursor: default;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateX(-50%) translateY(8px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
</style>
