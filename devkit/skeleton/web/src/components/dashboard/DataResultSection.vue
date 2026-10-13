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
import type { DataResultStats } from '@/types/dashboard'
import { displayOrPlaceholder } from '@/utils/format'

defineProps<{ dataResults: DataResultStats }>()
</script>

<template>
  <div class="data-result">
    <h4 class="data-result__title">数据成果</h4>
    <div class="data-result__grid">
      <div class="data-result__item">
        <span class="data-result__value">{{ displayOrPlaceholder(dataResults.media.total, false, true) }}</span>
        <span class="data-result__label">媒体数</span>
      </div>
      <div class="data-result__item">
        <span class="data-result__value">{{ displayOrPlaceholder(dataResults.models.total, false) }}</span>
        <span class="data-result__label">模型数</span>
      </div>
      <div class="data-result__item">
        <span class="data-result__value">{{ displayOrPlaceholder(dataResults.reports.total, dataResults.reports._mock) }}</span>
        <span class="data-result__label">报告数</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.data-result__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text);
  margin-bottom: 12px;
}

.data-result__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.data-result__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.data-result__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--app-text);
  font-variant-numeric: tabular-nums;
}

.data-result__label {
  font-size: 12px;
  color: var(--app-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
