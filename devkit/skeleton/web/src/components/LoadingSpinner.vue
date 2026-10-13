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
interface Props {
  size?: 'sm' | 'md' | 'lg'
  label?: string
}

withDefaults(defineProps<Props>(), {
  size: 'md',
  label: '',
})
</script>

<template>
  <div class="loading-spinner" :class="`loading-spinner--${size}`">
    <div class="spinner-ring" />
    <span v-if="label" class="spinner-label">{{ label }}</span>
  </div>
</template>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.spinner-ring {
  border-radius: 50%;
  border: 2.5px solid var(--app-border);
  border-top-color: var(--app-primary);
  animation: spin 0.8s linear infinite;
}

.loading-spinner--sm .spinner-ring {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

.loading-spinner--md .spinner-ring {
  width: 32px;
  height: 32px;
  border-width: 2.5px;
}

.loading-spinner--lg .spinner-ring {
  width: 48px;
  height: 48px;
  border-width: 3px;
}

.spinner-label {
  font-size: 13px;
  color: var(--app-text-muted);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
