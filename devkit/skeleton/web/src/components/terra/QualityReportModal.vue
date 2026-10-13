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
import type { QualityReport } from '@/types/terra'
import { fetchQualityReport } from '@/api/terra'

const props = defineProps<{
  visible: boolean
  taskId: string | null
}>()

const emit = defineEmits<{
  close: []
}>()

const report = ref<QualityReport | null>(null)
const loading = ref(false)
const error = ref('')

watch(() => props.visible, async (val) => {
  if (val && props.taskId) {
    loading.value = true
    error.value = ''
    try {
      report.value = await fetchQualityReport(props.taskId)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  } else {
    report.value = null
  }
})
</script>

<template>
  <div v-if="visible" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>质量报告</h3>
        <button class="close-btn" @click="emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="report" class="report">
          <div v-if="report.qualityReportUrl" class="report-ready">
            <div class="report-icon">&#x1F4CB;</div>
            <p class="report-hint">质量报告已生成，点击下方链接查看详细评分和指标</p>
            <a
              :href="report.qualityReportUrl"
              target="_blank"
              class="download-link"
            >
              下载完整报告
            </a>
          </div>
          <div v-else class="report-empty">
            <p>暂无质量报告</p>
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
  width: 420px;
  max-height: 70vh;
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
.error {
  text-align: center;
  padding: 24px 0;
  color: var(--app-text-muted);
}

.error {
  color: var(--app-danger);
}

.report-ready {
  text-align: center;
  padding: 20px 0;
}

.report-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.report-hint {
  font-size: 13px;
  color: var(--app-text-secondary);
  margin: 0 0 20px;
  line-height: 1.6;
}

.report-empty {
  text-align: center;
  padding: 24px 0;
  color: var(--app-text-muted);
  font-size: 13px;
}

.download-link {
  display: inline-block;
  padding: 8px 20px;
  background: var(--app-primary);
  color: #fff;
  text-decoration: none;
  font-size: 13px;
  border-radius: var(--radius-md);
  transition: opacity var(--transition-base);
}

.download-link:hover {
  opacity: 0.85;
}
</style>
