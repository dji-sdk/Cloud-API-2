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
import { ref, watch, computed } from 'vue'
import { Search, Wifi, WifiOff } from 'lucide-vue-next'
import type { DeviceState } from '@/types/api'
import { fetchDevices } from '@/api/devices'
import BaseModal from './BaseModal.vue'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  select: [sn: string, name: string]
}>()

const devices = ref<DeviceState[]>([])
const loading = ref(false)
const keyword = ref('')
const selectedSn = ref<string | null>(null)

const filteredDevices = computed(() => {
  const docks = devices.value.filter(d => d.deviceType === 'DOCK')
  if (!keyword.value) return docks
  const kw = keyword.value.toLowerCase()
  return docks.filter(d =>
    d.sn.toLowerCase().includes(kw) ||
    (d.name?.toLowerCase().includes(kw) ?? false),
  )
})

async function loadDevices(): Promise<void> {
  loading.value = true
  try {
    devices.value = await fetchDevices()
  } catch {
    devices.value = []
  } finally {
    loading.value = false
  }
}

function onConfirm(): void {
  const selected = filteredDevices.value.find(d => d.sn === selectedSn.value)
  if (selected) {
    emit('select', selected.sn, selected.name || selected.sn)
  }
}

function onCancel(): void {
  emit('update:visible', false)
}

watch(() => props.visible, (v) => {
  if (v) {
    selectedSn.value = null
    keyword.value = ''
    loadDevices()
  }
})
</script>

<template>
  <BaseModal
    :visible="visible"
    title="选择设备"
    width="560px"
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
        placeholder="搜索设备名称或 SN..."
      />
    </div>

    <div class="device-list">
      <div v-if="loading" class="list-loading">加载中...</div>
      <div v-else-if="filteredDevices.length === 0" class="list-empty">暂无可用设备</div>
      <div
        v-for="dev in filteredDevices"
        v-else
        :key="dev.sn"
        class="device-item"
        :class="{ selected: selectedSn === dev.sn }"
        @click="selectedSn = dev.sn"
      >
        <div class="item-main">
          <span class="item-name">{{ dev.name || dev.sn }}</span>
          <code class="item-sn">{{ dev.sn }}</code>
        </div>
        <div class="item-right">
          <span v-if="dev.model" class="item-model">{{ dev.model }}</span>
          <span class="item-status" :class="{ online: dev.online }">
            <Wifi v-if="dev.online" :size="12" />
            <WifiOff v-else :size="12" />
            {{ dev.online ? '在线' : '离线' }}
          </span>
        </div>
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

.device-list {
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

.device-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid transparent;
}

.device-item:hover {
  background: var(--app-hover-bg);
}

.device-item.selected {
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

.item-sn {
  font-size: 11px;
  color: var(--app-text-muted);
  font-family: var(--font-mono);
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-model {
  font-size: 12px;
  color: var(--app-text-secondary);
}

.item-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--app-text-muted);
}

.item-status.online {
  color: var(--app-success);
}
</style>
