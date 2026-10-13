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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import type { MediaFileInfo } from '@/types/media'
import { buildMediaContentUrl } from '@/api/media'
import { formatDateTime } from '@/utils/format'
import { triggerDownload } from '@/utils/download'
import { X, ChevronLeft, ChevronRight, Download } from 'lucide-vue-next'

interface Props {
  /** 仅视频文件列表（父组件负责过滤） */
  videos: MediaFileInfo[]
  currentIndex: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  close: []
  navigate: [index: number]
}>()

const videoUrl = ref('')
const videoLoading = ref(false)
const videoError = ref(false)

const currentFile = computed<MediaFileInfo | undefined>(() => props.videos[props.currentIndex])
const hasPrev = computed(() => props.currentIndex > 0)
const hasNext = computed(() => props.currentIndex < props.videos.length - 1)

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
 * 装载当前视频。
 *
 * 地址由本地构造，指向后端代理端点 —— **不能**用 SDK 返回的预签名 URL 直连：
 * 那个地址落在对象存储的内网端口 :30802，客户网段不可达，表现为点开全黑、进度条停在 0:00
 * （已知缺陷）。代理端点透传 `Range` 请求并回 `206 Partial Content`，
 * 因此 `<video>` 仍可随意拖动进度条。
 */
function loadVideo(): void {
  const file = currentFile.value
  if (!file) {
    return
  }

  videoUrl.value = buildMediaContentUrl(file.fileId)
  videoError.value = false
  // 真实字节仍由 `<video>` 异步拉取：先转圈，loadedmetadata 收尾、@error 转错误态
  videoLoading.value = true
}

/**
 * 下载当前视频。
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

/**
 * 视频元数据就绪，撤下加载浮层。
 *
 * 不用 `loadeddata`：`preload="metadata"` 下浏览器只保证拉到元数据，
 * 若 autoplay 被策略拦下，`loadeddata` 可能一直不触发，浮层会永久转圈。
 */
function handleVideoMetadata(): void {
  videoLoading.value = false
}

/** 视频拉取或解码失败，转错误态并撤下加载浮层。 */
function handleVideoError(): void {
  videoLoading.value = false
  videoError.value = true
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

function handleKeydown(e: KeyboardEvent): void {
  if (e.key === 'Escape') {
    emit('close')
  } else if (e.key === 'ArrowLeft') {
    e.preventDefault()
    goPrev()
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    goNext()
  }
}

function handleOverlayClick(e: MouseEvent): void {
  if ((e.target as HTMLElement).classList.contains('preview-overlay')) {
    emit('close')
  }
}

watch(() => props.currentIndex, () => {
  loadVideo()
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  loadVideo()
  document.body.style.overflow = 'hidden'
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div class="preview-overlay" @click="handleOverlayClick">
      <button class="preview-close" title="关闭 (Esc)" @click="emit('close')">
        <X :size="20" :stroke-width="2" />
      </button>

      <div class="preview-layout">
        <div class="preview-video-area">
          <div v-if="currentFile" class="preview-title-bar">
            <span class="preview-file-name">{{ currentFile.fileName }}</span>
            <span class="preview-file-meta">
              {{ formatDateTime(currentFile.createTime, true) }} · {{ formatSize(currentFile.fileSize) }}
            </span>
          </div>

          <button
            v-if="hasPrev"
            class="nav-btn nav-btn--prev"
            title="上一个 (←)"
            @click="goPrev"
          >
            <ChevronLeft :size="32" :stroke-width="2" />
          </button>

          <div class="preview-video-container">
            <!-- 状态浮层：视频本体不等状态就挂载，浏览器才能立刻开始拉取 -->
            <div v-if="videoLoading" class="preview-loading">
              <div class="loading-spinner" />
              <span>加载中...</span>
            </div>

            <div v-else-if="videoError" class="preview-error">
              <span>视频加载失败</span>
            </div>

            <video
              v-if="videoUrl && !videoError"
              :key="videoUrl"
              :src="videoUrl"
              class="preview-video"
              controls
              autoplay
              playsinline
              preload="metadata"
              @loadedmetadata="handleVideoMetadata"
              @error="handleVideoError"
            />
          </div>

          <button
            v-if="hasNext"
            class="nav-btn nav-btn--next"
            title="下一个 (→)"
            @click="goNext"
          >
            <ChevronRight :size="32" :stroke-width="2" />
          </button>
        </div>

        <div v-if="currentFile" class="preview-detail-panel">
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
              <span class="detail-value">视频</span>
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

          <button class="detail-download-btn" @click="handleDownload">
            <Download :size="14" :stroke-width="2" />
            下载原视频
          </button>

          <div v-if="videos.length > 1" class="detail-nav-section">
            <h4 class="detail-subtitle">
              视频列表
              <span class="detail-count">{{ currentIndex + 1 }} / {{ videos.length }}</span>
            </h4>
            <div class="nav-list">
              <button
                v-for="(file, idx) in videos"
                :key="file.fileId"
                class="nav-item"
                :class="{ 'nav-item--active': idx === currentIndex }"
                :title="file.fileName"
                @click="emit('navigate', idx)"
              >
                {{ idx + 1 }}
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

.preview-video-area {
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
  pointer-events: none;
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

.preview-video-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 80px;
}

.preview-video {
  max-width: 100%;
  max-height: 100%;
  background: #000;
  outline: none;
}

/* 状态浮层与视频本体同处一个容器，需绝对定位居中，不能作 flex 兄弟节点 */
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

.detail-download-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid var(--app-border, rgba(255, 255, 255, 0.08));
  border-radius: var(--radius-md);
  color: var(--app-text, #e6edf3);
  font-size: 13px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.detail-download-btn:hover {
  background: rgba(255, 255, 255, 0.12);
}

.detail-nav-section {
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

.nav-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.nav-item {
  width: 48px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-surface, #1a2332);
  border: 2px solid transparent;
  border-radius: var(--radius-sm);
  color: var(--app-text-muted, #6b7280);
  font-size: 11px;
  cursor: pointer;
  transition: all var(--transition-fast);
  opacity: 0.5;
}

.nav-item:hover {
  opacity: 0.8;
  border-color: rgba(255, 255, 255, 0.2);
}

.nav-item--active {
  opacity: 1;
  border-color: var(--app-primary, #06b6d4);
}
</style>
