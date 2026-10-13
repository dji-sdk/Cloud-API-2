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
import { useTcasStore } from '@/stores/tcas'
import type { TcasConfig } from '@/types/flightArea'

const store = useTcasStore()
const editing = ref(false)
const localConfig = ref<TcasConfig | null>(null)

watch(() => store.config, (cfg) => {
  if (cfg) {
    localConfig.value = JSON.parse(JSON.stringify(cfg)) as TcasConfig
  }
}, { immediate: true })

function startEdit(): void {
  editing.value = true
}

function cancelEdit(): void {
  editing.value = false
  if (store.config) {
    localConfig.value = JSON.parse(JSON.stringify(store.config)) as TcasConfig
  }
}

async function saveEdit(): Promise<void> {
  if (!localConfig.value) return
  try {
    await store.saveConfig(localConfig.value)
    editing.value = false
  } catch (e: unknown) {
    alert(e instanceof Error ? e.message : '保存失败')
  }
}
</script>

<template>
  <div class="tcas-threshold-panel">
    <div class="panel-header">
      <button v-if="!editing" class="edit-btn" @click="startEdit">编辑</button>
      <template v-else>
        <button class="save-btn" @click="saveEdit">保存</button>
        <button class="cancel-btn" @click="cancelEdit">取消</button>
      </template>
    </div>

    <table v-if="localConfig" class="threshold-table">
      <thead>
        <tr>
          <th />
          <th>水平距离</th>
          <th>垂直距离</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="t in localConfig.thresholds" :key="t.aircraftType">
          <tr class="type-header">
            <td colspan="3" class="type-name">{{ t.aircraftType }}</td>
          </tr>
          <tr>
            <td class="level-danger">高风险</td>
            <td>
              <input
                type="number"
                class="threshold-input"
                :value="t.horizontalDanger"
                :disabled="!editing"
                @input="t.horizontalDanger = Number(($event.target as HTMLInputElement).value)"
              /> m
            </td>
            <td>
              <input
                type="number"
                class="threshold-input"
                :value="t.verticalDanger"
                :disabled="!editing"
                @input="t.verticalDanger = Number(($event.target as HTMLInputElement).value)"
              /> m
            </td>
          </tr>
          <tr>
            <td class="level-warning">中风险</td>
            <td>
              <input
                type="number"
                class="threshold-input"
                :value="t.horizontalWarning"
                :disabled="!editing"
                @input="t.horizontalWarning = Number(($event.target as HTMLInputElement).value)"
              /> m
            </td>
            <td>
              <input
                type="number"
                class="threshold-input"
                :value="t.verticalWarning"
                :disabled="!editing"
                @input="t.verticalWarning = Number(($event.target as HTMLInputElement).value)"
              /> m
            </td>
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.tcas-threshold-panel {
  background: var(--app-bg-card, #161b22);
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.08));
  border-radius: 8px;
  padding: 12px;
}

.panel-header {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-bottom: 8px;
}

.edit-btn, .save-btn, .cancel-btn {
  padding: 4px 10px;
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 4px;
  background: transparent;
  color: var(--app-text, #e6edf3);
  cursor: pointer;
  font-size: 12px;
}

.save-btn {
  background: var(--app-accent, #58a6ff);
  border-color: transparent;
  color: #fff;
}

.threshold-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  color: var(--app-text, #e6edf3);
}

.threshold-table th {
  text-align: center;
  padding: 4px;
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
}

.threshold-table td {
  padding: 4px 6px;
  text-align: center;
}

.type-name {
  font-weight: 500;
  text-align: left !important;
  padding-top: 10px;
}

.level-danger {
  color: #f85149;
  font-size: 12px;
}

.level-warning {
  color: #d29922;
  font-size: 12px;
}

.threshold-input {
  width: 60px;
  padding: 3px 6px;
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.15));
  border-radius: 4px;
  background: var(--app-bg-card, #161b22);
  color: var(--app-text, #e6edf3);
  text-align: center;
  font-size: 13px;
}

.threshold-input:disabled {
  border-color: transparent;
  background: transparent;
}
</style>
