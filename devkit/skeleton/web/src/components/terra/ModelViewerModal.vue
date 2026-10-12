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
import { ref, watch, onUnmounted, nextTick, computed } from 'vue'
import { X, ChevronLeft, ChevronRight, Loader2 } from 'lucide-vue-next'
import { fetchModelDetail } from '@/api/terra'
import { useModelViewer } from '@/composables/useModelViewer'
import { formatDateTime } from '@/utils/format'
import type { ModelDetail, ModelFile } from '@/types/terra'

const props = defineProps<{
  visible: boolean
  fileId: number | null
  models: ModelFile[]
}>()

const emit = defineEmits<{
  close: []
  navigate: [fileId: number]
}>()

const containerRef = ref<HTMLElement | null>(null)
const detail = ref<ModelDetail | null>(null)
const fetchLoading = ref(false)
const fetchError = ref<string | null>(null)

const { loading: viewerLoading, error: viewerError, initViewer, loadModel, clearError, destroy } = useModelViewer()

const currentIndex = computed(() => {
  if (!props.fileId) return -1
  return props.models.findIndex((m) => m.fileId === props.fileId)
})

const hasPrev = computed(() => currentIndex.value > 0)
const hasNext = computed(() => currentIndex.value >= 0 && currentIndex.value < props.models.length - 1)

function navigatePrev() {
  if (!hasPrev.value) return
  const prev = props.models[currentIndex.value - 1]
  emit('navigate', prev.fileId)
}

function navigateNext() {
  if (!hasNext.value) return
  const next = props.models[currentIndex.value + 1]
  emit('navigate', next.fileId)
}

function handleKeydown(event: KeyboardEvent) {
  if (!props.visible) return
  switch (event.key) {
    case 'Escape':
      emit('close')
      break
    case 'ArrowLeft':
      navigatePrev()
      break
    case 'ArrowRight':
      navigateNext()
      break
  }
}

const typeLabels: Record<string, string> = {
  ortho_2d: '正射影像',
  mesh_3d: '三维网格',
  point_cloud: '点云',
  panorama: '全景',
  '3dgs': '3DGS',
  lidar: '激光雷达',
}

let navGen = 0

watch(
  () => [props.visible, props.fileId] as const,
  async ([visible, fileId]) => {
    const gen = ++navGen

    if (!visible || !fileId) {
      destroy()
      detail.value = null
      return
    }

    await nextTick()

    if (gen !== navGen) return

    if (containerRef.value) {
      initViewer(containerRef.value)
    }

    fetchLoading.value = true
    fetchError.value = null
    clearError()
    try {
      const result = await fetchModelDetail(fileId)
      if (gen !== navGen) return
      detail.value = result
      await loadModel(result)
    } catch (e) {
      if (gen !== navGen) return
      fetchError.value = e instanceof Error ? e.message : '加载模型详情失败'
    } finally {
      if (gen === navGen) {
        fetchLoading.value = false
      }
    }
  },
)

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      document.addEventListener('keydown', handleKeydown)
    } else {
      document.removeEventListener('keydown', handleKeydown)
    }
  },
)

onUnmounted(() => {
  destroy()
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <Teleport to="body">
    <div v-if="visible" class="viewer-overlay">
      <!-- 顶部信息栏 -->
      <header class="viewer-header">
        <div class="header-info">
          <h2 class="model-name">{{ detail?.name ?? '加载中...' }}</h2>
          <span v-if="detail" class="model-meta">
            {{ formatDateTime(detail.createdAt) }}
          </span>
          <span v-if="detail" class="model-type-badge">
            {{ typeLabels[detail.reconstructionType] ?? detail.reconstructionType }}
          </span>
        </div>
        <button class="close-btn" title="关闭 (Esc)" @click="emit('close')">
          <X :size="22" />
        </button>
      </header>

      <!-- 3D 查看器主体 -->
      <div class="viewer-body">
        <!-- 左箭头 -->
        <button
          v-if="models.length > 1"
          class="nav-btn nav-btn--left"
          :disabled="!hasPrev"
          title="上一个 (←)"
          @click="navigatePrev"
        >
          <ChevronLeft :size="28" />
        </button>

        <!-- Cesium 容器 -->
        <div ref="containerRef" class="cesium-container" />

        <!-- 加载状态 -->
        <div v-if="fetchLoading || viewerLoading" class="viewer-loading">
          <Loader2 :size="32" class="spin" />
          <span>模型加载中...</span>
        </div>

        <!-- 错误状态 -->
        <div v-if="fetchError || viewerError" class="viewer-error">
          {{ fetchError || viewerError }}
        </div>

        <!-- 右箭头 -->
        <button
          v-if="models.length > 1"
          class="nav-btn nav-btn--right"
          :disabled="!hasNext"
          title="下一个 (→)"
          @click="navigateNext"
        >
          <ChevronRight :size="28" />
        </button>
      </div>

      <!-- 底部缩略图条 -->
      <footer v-if="models.length > 1" class="viewer-footer">
        <div class="thumbnail-strip">
          <button
            v-for="model in models"
            :key="model.fileId"
            class="thumb-item"
            :class="{ 'thumb-item--active': model.fileId === fileId }"
            @click="emit('navigate', model.fileId)"
          >
            <img
              v-if="model.thumbnailUrl"
              :src="model.thumbnailUrl"
              :alt="model.name"
              class="thumb-img"
            />
            <span v-else class="thumb-placeholder">{{ model.name.charAt(0) }}</span>
          </button>
        </div>
      </footer>
    </div>
  </Teleport>
</template>

<style scoped>
.viewer-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  background: #0a0a0f;
}

.viewer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(10, 10, 15, 0.95);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  z-index: 2;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.model-name {
  font-size: 16px;
  font-weight: 600;
  color: #e8e8e8;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 400px;
}

.model-meta {
  font-size: 12px;
  color: #888;
}

.model-type-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(33, 150, 243, 0.15);
  color: #64b5f6;
  white-space: nowrap;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
  color: #ccc;
  cursor: pointer;
  transition: background 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.viewer-body {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.cesium-container {
  width: 100%;
  height: 100%;
}

/* 隐藏 Cesium 默认 credit 和 logo */
.cesium-container :deep(.cesium-viewer-bottom),
.cesium-container :deep(.cesium-credit-logoContainer),
.cesium-container :deep(.cesium-credit-textContainer) {
  display: none !important;
}

.viewer-loading {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #aaa;
  font-size: 14px;
  pointer-events: none;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.viewer-error {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 8px 16px;
  background: rgba(244, 67, 54, 0.9);
  color: #fff;
  font-size: 13px;
  border-radius: 6px;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #ddd;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.nav-btn--left {
  left: 16px;
}

.nav-btn--right {
  right: 16px;
}

.viewer-footer {
  padding: 10px 20px;
  background: rgba(10, 10, 15, 0.95);
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  z-index: 2;
}

.thumbnail-strip {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 4px 0;
}

.thumbnail-strip::-webkit-scrollbar {
  height: 4px;
}

.thumbnail-strip::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.thumb-item {
  flex-shrink: 0;
  width: 64px;
  height: 48px;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.05);
  transition: border-color 0.2s;
  padding: 0;
}

.thumb-item:hover {
  border-color: rgba(255, 255, 255, 0.3);
}

.thumb-item--active {
  border-color: #2196f3;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 16px;
  color: #666;
  font-weight: 600;
}
</style>
