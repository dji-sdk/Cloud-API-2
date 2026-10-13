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
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import type { MediaFileInfo } from '@/types/media'
import { buildMediaContentUrl } from '@/api/media'
import { formatDateTime } from '@/utils/format'
import { triggerDownload } from '@/utils/download'
import { X, ChevronLeft, ChevronRight, Download, ZoomIn, ZoomOut, RotateCw, Maximize } from 'lucide-vue-next'

interface Props {
  /** 仅照片文件列表（父组件负责过滤） */
  photos: MediaFileInfo[]
  currentIndex: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  navigate: [index: number]
}>()

const imageUrl = ref('')
const imageLoading = ref(false)
const imageError = ref(false)
const scale = ref(1)
const rotation = ref(0)
const isFullscreen = ref(false)

const currentFile = computed<MediaFileInfo | undefined>(() => props.photos[props.currentIndex])
const hasPrev = computed(() => props.currentIndex > 0)
const hasNext = computed(() => props.currentIndex < props.photos.length - 1)

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

/**
 * 装载当前图片。
 *
 * 地址由本地构造，不再请求后端换取预签名 URL —— 那个 URL 指向对象存储的 :30802，
 * 客户网段不可达，直接作 `src` 会加载失败。改走后端代理端点后
 * 浏览器只与本服务通信，由服务端回源对象存储。
 */
function loadImage(): void {
  const file = currentFile.value
  if (!file) {
    return
  }

  imageUrl.value = buildMediaContentUrl(file.fileId)
  imageError.value = false
  scale.value = 1
  rotation.value = 0
  // 真实字节仍由 `<img>` 异步拉取：先转圈，@load 收尾、@error 转错误态
  imageLoading.value = true
}

/**
 * 下载当前图片。
 *
 * 文件名由后端代理回传的 `Content-Disposition` 决定，前端不参与拼接。
 */
function handleDownload(): void {
  const file = currentFile.value
  if (!file) {
    return
  }
  triggerDownload(buildMediaContentUrl(file.fileId, true))
}

/** 图片解码完成，撤下加载浮层。 */
function handleImageLoad(): void {
  imageLoading.value = false
}

/** 图片拉取失败，转错误态并撤下加载浮层。 */
function handleImageError(): void {
  imageLoading.value = false
  imageError.value = true
}

function goPrev(): void {
  if (hasPrev.value) {
    emit('navigate', props.currentIndex - 1)
  }
}

function goNext(): void {
  if (hasNext.value) {
    emit('navigate', props.currentIndex + 1)
  }
}

function zoomIn(): void {
  scale.value = Math.min(scale.value * 1.25, 5)
}

function zoomOut(): void {
  scale.value = Math.max(scale.value / 1.25, 0.2)
}

function rotateRight(): void {
  rotation.value = (rotation.value + 90) % 360
}

function toggleFullscreen(): void {
  isFullscreen.value = !isFullscreen.value
}

function handleKeydown(e: KeyboardEvent): void {
  if (e.key === 'Escape') {
    if (isFullscreen.value) {
      isFullscreen.value = false
    } else {
      emit('close')
    }
  } else if (e.key === 'ArrowLeft') {
    e.preventDefault()
    goPrev()
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    goNext()
  }
}

function handleWheel(e: WheelEvent): void {
  e.preventDefault()
  if (e.deltaY < 0) {
    zoomIn()
  } else {
    zoomOut()
  }
}

function handleOverlayClick(e: MouseEvent): void {
  if ((e.target as HTMLElement).classList.contains('preview-overlay')) {
    emit('close')
  }
}

watch(() => props.currentIndex, () => {
  loadImage()
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  loadImage()
  nextTick(() => {
    document.body.style.overflow = 'hidden'
  })
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div
      class="preview-overlay"
      :class="{ 'preview-overlay--fullscreen': isFullscreen }"
      @click="handleOverlayClick"
    >
      <!-- 关闭按钮 -->
      <button class="preview-close" title="关闭 (Esc)" @click="emit('close')">
        <X :size="20" :stroke-width="2" />
      </button>

      <div class="preview-layout">
        <!-- 左侧：图片展示区 -->
        <div class="preview-image-area">
          <!-- 文件名标题 -->
          <div v-if="currentFile" class="preview-title-bar">
            <span class="preview-file-name">{{ currentFile.fileName }}</span>
            <span class="preview-file-meta">
              {{ formatDateTime(currentFile.createTime, true) }} · {{ formatSize(currentFile.fileSize) }}
            </span>
          </div>

          <!-- 上一张 -->
          <button
            v-if="hasPrev"
            class="nav-btn nav-btn--prev"
            title="上一张 (←)"
            @click="goPrev"
          >
            <ChevronLeft :size="32" :stroke-width="2" />
          </button>

          <!-- 图片 -->
          <div class="preview-image-container" @wheel="handleWheel">
            <!-- 状态浮层：图片本体不等状态就挂载，浏览器才能立刻开始拉取 -->
            <div v-if="imageLoading" class="preview-loading">
              <div class="loading-spinner" />
              <span>加载中...</span>
            </div>

            <div v-else-if="imageError" class="preview-error">
              <span>图片加载失败</span>
            </div>

            <img
              v-if="imageUrl && !imageError"
              :src="imageUrl"
              :alt="currentFile?.fileName"
              class="preview-image"
              :style="{
                transform: `scale(${scale}) rotate(${rotation}deg)`,
              }"
              draggable="false"
              @load="handleImageLoad"
              @error="handleImageError"
            />
          </div>

          <!-- 下一张 -->
          <button
            v-if="hasNext"
            class="nav-btn nav-btn--next"
            title="下一张 (→)"
            @click="goNext"
          >
            <ChevronRight :size="32" :stroke-width="2" />
          </button>

          <!-- 底部工具栏 -->
          <div class="preview-toolbar">
            <button class="toolbar-btn" title="缩小" @click="zoomOut">
              <ZoomOut :size="16" :stroke-width="2" />
            </button>
            <span class="toolbar-zoom-text">{{ Math.round(scale * 100) }}%</span>
            <button class="toolbar-btn" title="放大" @click="zoomIn">
              <ZoomIn :size="16" :stroke-width="2" />
            </button>
            <div class="toolbar-divider" />
            <button class="toolbar-btn" title="旋转" @click="rotateRight">
              <RotateCw :size="16" :stroke-width="2" />
            </button>
            <button class="toolbar-btn" title="全屏" @click="toggleFullscreen">
              <Maximize :size="16" :stroke-width="2" />
            </button>
            <div class="toolbar-divider" />
            <button
              class="toolbar-btn"
              title="下载"
              @click="handleDownload"
            >
              <Download :size="16" :stroke-width="2" />
            </button>
          </div>
        </div>

        <!-- 右侧：详情面板 -->
        <div v-if="!isFullscreen && currentFile" class="preview-detail-panel">
          <h3 class="detail-title">详细信息</h3>

          <div class="detail-list">
            <div class="detail-item">
              <span class="detail-label">文件名</span>
              <span class="detail-value" :title="currentFile.fileName">
                {{ currentFile.fileName }}
              </span>
            </div>

            <div class="detail-item">
              <span class="detail-label">媒体类型</span>
              <span class="detail-value">
                {{ currentFile.fileType === 'photo' ? '照片' : '视频' }}
              </span>
            </div>

            <div class="detail-item">
              <span class="detail-label">文件大小</span>
              <span class="detail-value">{{ formatSize(currentFile.fileSize) }}</span>
            </div>

            <div class="detail-item">
              <span class="detail-label">拍摄负载</span>
              <span class="detail-value">{{ currentFile.payloadType || '--' }}</span>
            </div>

            <div class="detail-item">
              <span class="detail-label">设备 SN</span>
              <span class="detail-value">{{ currentFile.deviceSn }}</span>
            </div>

            <div class="detail-item">
              <span class="detail-label">任务 ID</span>
              <span class="detail-value" :title="currentFile.flightId">
                {{ currentFile.flightId || '--' }}
              </span>
            </div>

            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatDateTime(currentFile.createTime, true) }}</span>
            </div>
          </div>

          <!-- 缩略图导航 -->
          <div v-if="photos.length > 1" class="detail-thumbnail-section">
            <h4 class="detail-subtitle">
              照片列表
              <span class="detail-count">{{ currentIndex + 1 }} / {{ photos.length }}</span>
            </h4>
            <div class="thumbnail-list">
              <button
                v-for="(file, idx) in photos"
                :key="file.fileId"
                class="thumbnail-item"
                :class="{ 'thumbnail-item--active': idx === currentIndex }"
                :title="file.fileName"
                @click="emit('navigate', idx)"
              >
                <span class="thumbnail-placeholder">
                  {{ idx + 1 }}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.92);
  display: flex;
  flex-direction: column;
}

.preview-close {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-md);
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.preview-close:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.preview-layout {
  display: flex;
  flex: 1;
  min-height: 0;
}

/* 左侧图片区 */
.preview-image-area {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
}

.preview-title-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  padding: 16px 24px;
  z-index: 5;
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent);
}

.preview-file-name {
  font-size: 15px;
  font-weight: 500;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-file-meta {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* 导航按钮 */
.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 5;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  border-radius: var(--radius-full);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.nav-btn--prev {
  left: 24px;
}

.nav-btn--next {
  right: 24px;
}

/* 图片容器 */
.preview-image-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 60px 80px;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  transition: transform 0.15s ease;
  user-select: none;
}

/* 状态浮层与图片本体同处一个容器，需绝对定位居中，不能作 flex 兄弟节点 */
.preview-loading,
.preview-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: var(--app-primary, #06b6d4);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 底部工具栏 */
.preview-toolbar {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  backdrop-filter: blur(8px);
}

.toolbar-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.toolbar-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.toolbar-zoom-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  min-width: 40px;
  text-align: center;
  user-select: none;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.12);
  margin: 0 4px;
}

/* 右侧详情面板 */
.preview-detail-panel {
  width: 320px;
  flex-shrink: 0;
  background: var(--app-bg-panel, #111827);
  border-left: 1px solid var(--app-border, rgba(255, 255, 255, 0.08));
  padding: 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #e6edf3);
  margin: 0;
  padding-right: 40px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  color: var(--app-text-muted, #6b7280);
}

.detail-value {
  font-size: 13px;
  color: var(--app-text, #e6edf3);
  word-break: break-all;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 缩略图导航 */
.detail-thumbnail-section {
  border-top: 1px solid var(--app-border, rgba(255, 255, 255, 0.08));
  padding-top: 16px;
}

.detail-subtitle {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text-secondary, #8b949e);
  margin: 0 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-count {
  font-size: 12px;
  color: var(--app-text-muted, #6b7280);
  font-weight: 400;
}

.thumbnail-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.thumbnail-item {
  width: 48px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-surface, #1a2332);
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  opacity: 0.5;
}

.thumbnail-item:hover {
  opacity: 0.8;
  border-color: rgba(255, 255, 255, 0.2);
}

.thumbnail-item--active {
  opacity: 1;
  border-color: var(--app-primary, #06b6d4);
}

.thumbnail-placeholder {
  font-size: 11px;
  color: var(--app-text-muted, #6b7280);
}

/* 全屏模式隐藏详情面板 */
.preview-overlay--fullscreen .preview-detail-panel {
  display: none;
}

.preview-overlay--fullscreen .preview-image-container {
  padding: 40px;
}

.preview-overlay--fullscreen .nav-btn--next {
  right: 24px;
}
</style>
