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
import { onMounted } from 'vue'
import { useTcasStore } from '@/stores/tcas'
import TcasThresholdPanel from '../TcasThresholdPanel.vue'

const store = useTcasStore()

onMounted(() => {
  store.loadConfig()
})
</script>

<template>
  <div class="airspace-alert-tab">
    <h4 class="section-title">空域告警</h4>

    <div class="alert-card">
      <div class="alert-row">
        <div class="alert-info">
          <div class="alert-name">DJI AirSense</div>
          <div class="alert-desc">来自机载ADS-B接收器的告警</div>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" v-model="store.airSenseEnabled" />
          <span class="toggle-slider" />
        </label>
      </div>
    </div>

    <div class="alert-card">
      <div class="alert-row">
        <div class="alert-info">
          <div class="alert-name">空域safeS</div>
          <div class="alert-desc">DJI 上云API 2Sync中配置的三方服务</div>
        </div>
        <label class="toggle-switch">
          <input type="checkbox" v-model="store.safeSEnabled" />
          <span class="toggle-slider" />
        </label>
      </div>
    </div>

    <h4 class="section-title">告警距离阈值</h4>

    <TcasThresholdPanel v-if="store.config" />
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<style scoped>
.airspace-alert-tab {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text, #e6edf3);
  margin: 4px 0;
}

.alert-card {
  padding: 12px 16px;
  border-radius: 8px;
  background: var(--app-bg-card, #161b22);
  border: 1px solid var(--fh-border-color, rgba(255, 255, 255, 0.08));
}

.alert-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.alert-name {
  font-size: 14px;
  color: var(--app-text, #e6edf3);
}

.alert-desc {
  font-size: 12px;
  color: var(--app-text-secondary, #8b949e);
  margin-top: 2px;
}

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
