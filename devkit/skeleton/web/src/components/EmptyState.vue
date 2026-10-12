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
import type { Component } from 'vue'
import { FileX } from 'lucide-vue-next'

interface Props {
  icon?: Component
  title?: string
  description?: string
  actionText?: string
  actionTo?: string
}

withDefaults(defineProps<Props>(), {
  icon: () => FileX,
  title: '暂无数据',
  description: '',
  actionText: '',
  actionTo: '',
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-state__icon-wrap">
      <component :is="icon" :size="48" :stroke-width="1.25" class="empty-state__icon" />
    </div>
    <h3 class="empty-state__title">{{ title }}</h3>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
    <router-link v-if="actionText && actionTo" :to="actionTo" class="empty-state__action">
      {{ actionText }}
    </router-link>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl) var(--space-lg);
  min-height: 320px;
  text-align: center;
}

.empty-state__icon-wrap {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  background: var(--app-bg-surface);
  margin-bottom: var(--space-lg);
}

.empty-state__icon {
  color: var(--app-text-muted);
}

.empty-state__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0 0 var(--space-xs);
}

.empty-state__desc {
  font-size: 13px;
  color: var(--app-text-muted);
  margin: 0 0 var(--space-lg);
  max-width: 280px;
  line-height: 1.5;
}

.empty-state__action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  background: var(--app-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-base);
}

.empty-state__action:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}
</style>
