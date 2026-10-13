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
import { ref, watch } from 'vue'
import type { ImageItem } from '@/types/terra'
import { fetchReconstructionImages } from '@/api/terra'

const props = defineProps<{
  visible: boolean
  taskId: string | null
}>()

const emit = defineEmits<{
  close: []
}>()

const images = ref<ImageItem[]>([])
const loading = ref(false)
const error = ref('')
const failedIds = ref<Set<string>>(new Set())

watch(() => props.visible, async (val) => {
  if (val && props.taskId) {
    loading.value = true
    error.value = ''
    failedIds.value = new Set()
    try {
      images.value = await fetchReconstructionImages(props.taskId)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  } else {
    images.value = []
  }
})

function onImageError(id: string) {
  failedIds.value.add(id)
}
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>原始影像 ({{ images.length }})</h3>
        <button class="close-btn" @click="emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="images.length === 0" class="empty">暂无影像</div>
        <div v-else class="image-grid">
          <div v-for="img in images" :key="img.id" class="image-item">
            <div class="image-thumb">
              <img
                v-if="img.url && !failedIds.has(img.id)"
                :src="img.url"
                :alt="img.name"
                loading="lazy"
                @error="onImageError(img.id)"
              />
              <span v-else class="image-placeholder">{{ img.name.substring(0, 3) }}</span>
            </div>
            <span class="image-name">{{ img.name }}</span>
          </div>
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
  width: 600px;
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
}

.close-btn:hover {
  color: var(--app-text);
}

.modal-body {
  padding: 20px;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 24px 0;
  color: var(--app-text-muted);
}

.error {
  color: var(--app-danger);
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.image-item {
  text-align: center;
}

.image-thumb {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: var(--app-bg-surface);
}

.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-muted);
  background: var(--app-bg-surface);
}

.image-name {
  display: block;
  font-size: 11px;
  color: var(--app-text-muted);
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
