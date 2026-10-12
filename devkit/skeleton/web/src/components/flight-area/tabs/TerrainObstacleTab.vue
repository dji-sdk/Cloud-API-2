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
import { computed, onMounted, onUnmounted } from 'vue'
import { useOfflineMapStore } from '@/stores/offlineMap'

const store = useOfflineMapStore()

onMounted(() => {
  store.loadModelInfo()
})

onUnmounted(() => {
  store.stopPolling()
})

function formatTime(ts: number): string {
  return new Date(ts * 1000).toLocaleString('zh-CN')
}

const isUpdating = computed(() =>
  store.updateStatus === 'WAITING' || store.updateStatus === 'PROCESSING',
)

const isTerminalState = computed(() =>
  store.updateStatus === 'FINISH'
  || store.updateStatus === 'FAIL'
  || store.updateStatus === 'CANCEL'
  || store.updateStatus === 'TIMEOUT',
)

const isUpdateFailed = computed(() =>
  store.updateStatus === 'FAIL' || store.updateStatus === 'TIMEOUT',
)

const progressBarClass = computed(() => ({
  'progress-bar': true,
  'progress-bar--error': isUpdateFailed.value,
}))
</script>

<template>
  <div class="terrain-obstacle-tab">
    <div v-if="store.loading" class="loading">加载中...</div>
    <template v-else-if="store.modelInfo">
      <div class="terrain-card">
        <div class="card-header">
          <div class="card-title">地形障碍物</div>
          <label class="toggle-switch">
            <input
              type="checkbox"
              :checked="!store.modelInfo.disabled"
              :disabled="isUpdating"
              @change="store.toggleEnabled(!store.modelInfo!.disabled)"
            />
            <span class="toggle-slider" />
          </label>
        </div>

        <div class="card-meta">
          更新时间: {{ formatTime(store.modelInfo.updateTime) }}
        </div>

        <div class="card-version">
          版本: {{ store.modelInfo.version }}
        </div>

        <!-- 更新按钮区 -->
        <div v-if="!isUpdating && !isTerminalState" class="update-action">
          <button
            class="update-btn"
            :disabled="store.modelInfo.disabled"
            @click="store.triggerUpdate()"
          >
            数据更新
          </button>
        </div>

        <!-- 更新进度区 -->
        <div v-if="isUpdating" class="progress-section">
          <div class="progress-track">
            <div
              :class="progressBarClass"
              :style="{ width: store.updateProgress + '%' }"
            />
          </div>
          <div class="progress-info">
            <span class="progress-text">{{ store.updateMessage }}</span>
            <button class="cancel-link" @click="store.cancelUpdate()">
              取消
            </button>
          </div>
        </div>

        <!-- 终态展示区 -->
        <div v-if="isTerminalState" class="terminal-section">
          <div
            class="terminal-message"
            :class="{ 'terminal-message--error': isUpdateFailed }"
          >
            {{ store.updateMessage }}
          </div>
          <button class="reset-btn" @click="store.resetState()">
            确定
          </button>
        </div>

        <div v-if="store.modelInfo.dataDescription" class="card-desc">
          {{ store.modelInfo.dataDescription }}
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.terrain-obstacle-tab {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.terrain-card {
  padding: 16px;
  border-radius: 8px;
  background: var(--app-bg-card, #161b22);
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.08));
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: var(--app-text, #e6edf3);
}

.card-meta,
.card-version {
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
  margin-bottom: 8px;
}

.card-desc {
  font-size: 13px;
  color: var(--app-text-secondary, #8b949e);
  margin-top: 12px;
}

/* 更新按钮 */
.update-action {
  margin-top: 12px;
}

.update-btn {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 6px;
  background: var(--app-accent, #58a6ff);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.update-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 进度条 */
.progress-section {
  margin-top: 12px;
}

.progress-track {
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: var(--app-accent, #58a6ff);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-bar--error {
  background: #e74c3c;
}

.progress-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
}

.progress-text {
  color: var(--app-text-secondary, #8b949e);
}

.cancel-link {
  background: none;
  border: none;
  color: var(--app-accent, #58a6ff);
  cursor: pointer;
  font-size: 12px;
  padding: 0;
}

.cancel-link:hover {
  text-decoration: underline;
}

/* 终态 */
.terminal-section {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.terminal-message {
  flex: 1;
  font-size: 13px;
  color: #2ecc71;
}

.terminal-message--error {
  color: #e74c3c;
}

.reset-btn {
  padding: 6px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  background: transparent;
  color: var(--app-text, #e6edf3);
  font-size: 12px;
  cursor: pointer;
}

.reset-btn:hover {
  background: rgba(255, 255, 255, 0.05);
}

/* 开关 */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 22px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: #444;
  border-radius: 22px;
  transition: 0.2s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  height: 16px;
  width: 16px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.2s;
}

.toggle-switch input:checked + .toggle-slider {
  background: var(--app-accent, #58a6ff);
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(18px);
}

.loading {
  text-align: center;
  padding: 20px;
  color: var(--app-text-secondary, #8b949e);
}
</style>
