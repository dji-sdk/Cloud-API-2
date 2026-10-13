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
import { Search } from 'lucide-vue-next'
import type { WaylineInfo } from '@/types/wayline'
import { fetchWaylineList } from '@/api/wayline'
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  select: [uuid: string, name: string]
}>()

const waylines = ref<WaylineInfo[]>([])
const loading = ref(false)
const keyword = ref('')
const selectedUuid = ref<string | null>(null)

async function loadWaylines(): Promise<void> {
  loading.value = true
  try {
    const resp = await fetchWaylineList({ keyword: keyword.value, pageSize: 50 })
    waylines.value = resp.waylines
  } catch {
    waylines.value = []
  } finally {
    loading.value = false
  }
}

function onConfirm(): void {
  const selected = waylines.value.find(w => w.uuid === selectedUuid.value)
  if (selected) {
    emit('select', selected.uuid, selected.name)
  }
}

function onCancel(): void {
  emit('update:visible', false)
}

watch(() => props.visible, (v) => {
  if (v) {
    selectedUuid.value = null
    keyword.value = ''
    loadWaylines()
  }
})
</script>

<template>
  <BaseModal
    :visible="visible"
    title="选择航线"
    width="640px"
    @update:visible="emit('update:visible', $event)"
    @confirm="onConfirm"
    @cancel="onCancel"
  >
    <div class="search-bar">
      <Search :size="14" class="search-icon" />
      <input
        v-model="keyword"
        class="search-input"
        type="text"
        placeholder="搜索航线名称..."
        @input="loadWaylines()"
      />
    </div>

    <div class="wayline-list">
      <div v-if="loading" class="list-loading">加载中...</div>
      <div v-else-if="waylines.length === 0" class="list-empty">暂无航线</div>
      <div
        v-for="wl in waylines"
        v-else
        :key="wl.uuid"
        class="wayline-item"
        :class="{ selected: selectedUuid === wl.uuid }"
        @click="selectedUuid = wl.uuid"
      >
        <div class="item-main">
          <span class="item-name">{{ wl.name }}</span>
          <span class="item-meta">{{ wl.droneType }} · {{ wl.payloadType }}</span>
        </div>
        <span class="item-type">{{ wl.waylineType }}</span>
      </div>
    </div>
  </BaseModal>
</template>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: var(--app-bg-surface);
  margin-bottom: 12px;
}

.search-icon {
  color: var(--app-text-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--app-text);
  font-size: 14px;
  outline: none;
}

.search-input::placeholder {
  color: var(--app-text-muted);
}

.wayline-list {
  max-height: 400px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.list-loading,
.list-empty {
  padding: 32px;
  text-align: center;
  font-size: 14px;
  color: var(--app-text-muted);
}

.wayline-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.wayline-item:hover {
  background: var(--app-hover-bg);
}

.wayline-item.selected {
  background: var(--app-primary-bg);
  border-color: var(--app-primary);
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-name {
  font-size: 14px;
  color: var(--app-text);
  font-weight: 500;
}

.item-meta {
  font-size: 12px;
  color: var(--app-text-muted);
}

.item-type {
  font-size: 12px;
  color: var(--app-text-secondary);
  background: var(--app-bg-elevated);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}
</style>
