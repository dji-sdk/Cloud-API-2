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
import type { CountStats } from '@/types/dashboard'
import { displayOrPlaceholder } from '@/utils/format'

defineProps<{ counts: CountStats }>()
</script>

<template>
  <div class="stat-grid">
    <div class="stat-grid__item">
      <span class="stat-grid__value">{{ counts.waylines }}</span>
      <span class="stat-grid__label">航线总数</span>
    </div>
    <div class="stat-grid__item">
      <span class="stat-grid__value">{{ displayOrPlaceholder(counts.personnel.value, counts.personnel._mock) }}</span>
      <span class="stat-grid__label">人员总数</span>
    </div>
  </div>
</template>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-grid__item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-grid__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--app-text);
  font-variant-numeric: tabular-nums;
}

.stat-grid__label {
  font-size: 12px;
  color: var(--app-text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
