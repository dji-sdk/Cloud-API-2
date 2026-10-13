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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useFlightTaskForm } from '@/composables/useFlightTaskForm'
import { fetchFlightTaskDetail } from '@/api/flightTask'
import { applyBaseImagery } from '@/utils/cesiumImagery'
import TaskBasicSection from '@/components/flight-task/TaskBasicSection.vue'
import WaylineSection from '@/components/flight-task/WaylineSection.vue'
import DeviceSection from '@/components/flight-task/DeviceSection.vue'
import MultiModalToggle from '@/components/flight-task/MultiModalToggle.vue'
import StrategySection from '@/components/flight-task/StrategySection.vue'
import SafetySection from '@/components/flight-task/SafetySection.vue'

const router = useRouter()
const route = useRoute()
const { form, errors, submitting, submitForm, applyDetail, clearFieldError } = useFlightTaskForm()

/** 路由携带的 taskUuid；非空表示编辑模式（复用创建视图，预填任务详情） */
const initialTaskUuid = typeof route.query.taskUuid === 'string' && route.query.taskUuid.length > 0
  ? route.query.taskUuid
  : null
/** 详情加载中标记：加载完成前禁止提交，防止默认值覆盖任务参数 */
const detailLoading = ref(false)
/** 详情加载失败信息：非空时展示错误提示并禁止提交，防止以默认值提交产生脏数据 */
const detailError = ref<string | null>(null)

const mapContainer = ref<HTMLDivElement | null>(null)
let viewer: Cesium.Viewer | null = null

onMounted(() => {
  if (initialTaskUuid) {
    void loadDetail(initialTaskUuid)
  }
  if (!mapContainer.value) return
  try {
    viewer = new Cesium.Viewer(mapContainer.value, {
      animation: false,
      baseLayer: false,
      timeline: false,
      baseLayerPicker: false,
      geocoder: false,
      homeButton: false,
      sceneModePicker: false,
      navigationHelpButton: false,
      fullscreenButton: false,
      infoBox: false,
      selectionIndicator: false,
      creditContainer: document.createElement('div'),
    })
  } catch (err) {
    console.error('[FlightTaskCreate] Cesium Viewer 创建失败（WebGL 不可用？）', err)
    return
  }
  applyBaseImagery(viewer).catch((err) => {
    console.error('[FlightTaskCreate] 底图加载失败', err)
  })
})

onUnmounted(() => {
  if (viewer && !viewer.isDestroyed()) {
    viewer.destroy()
    viewer = null
  }
})

function goBack(): void {
  router.push('/flight-tasks')
}

/** 加载任务详情并预填表单（编辑模式）。 */
async function loadDetail(taskUuid: string): Promise<void> {
  detailLoading.value = true
  try {
    const detail = await fetchFlightTaskDetail(taskUuid)
    applyDetail(detail)
    detailError.value = null
  } catch (e) {
    console.error('[FlightTaskCreate] 任务详情加载失败', e)
    detailError.value = e instanceof Error ? e.message : '任务详情加载失败，请返回列表重试'
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit(): Promise<void> {
  const taskUuid = await submitForm()
  if (taskUuid) {
    router.push('/flight-tasks')
  }
}

/**
 * 名称输入变更。
 *
 * 清除名称字段上残留的错误提示（如服务端回传的「任务名称已存在」），
 * 否则用户改成新名字后红字仍挂在输入框下方，直到下次提交才消失。
 */
function handleNameUpdate(value: string): void {
  form.name = value
  clearFieldError('name')
}
</script>

<template>
  <div class="create-view">
    <!-- 顶部导航栏 -->
    <header class="create-header">
      <button class="back-btn" type="button" @click="goBack">
        <ArrowLeft :size="18" />
      </button>
      <h1 class="page-title">{{ initialTaskUuid ? '编辑任务' : '新建任务' }}</h1>
    </header>

    <!-- 主体：左侧表单 + 右侧地图 -->
    <div class="create-main">
      <!-- 左侧表单面板 -->
      <div class="form-panel">
        <div class="form-scroll">
          <div class="form-container">
            <!-- 基础信息 -->
            <TaskBasicSection
              :name="form.name"
              :plan-type="form.planType"
              @update:name="handleNameUpdate"
              @update:plan-type="form.planType = $event"
            />

            <!-- 错误提示：名称 -->
            <p v-if="errors.name" class="field-error">{{ errors.name }}</p>

            <!-- 航线选择（手飞任务不显示） -->
            <template v-if="form.planType !== 'manual'">
              <WaylineSection
                :wayline-uuid="form.waylineUuid"
                :wayline-name="form.waylineName"
                @update:wayline-uuid="form.waylineUuid = $event"
                @update:wayline-name="form.waylineName = $event"
              />
              <p v-if="errors.waylineUuid" class="field-error">{{ errors.waylineUuid }}</p>
            </template>

            <!-- 设备选择 -->
            <DeviceSection
              :device-sn="form.deviceSn"
              :device-name="form.deviceName"
              @update:device-sn="form.deviceSn = $event"
              @update:device-name="form.deviceName = $event"
            />
            <p v-if="errors.deviceSn" class="field-error">{{ errors.deviceSn }}</p>

            <!-- 多模态识别 -->
            <MultiModalToggle
              :model-value="form.vlmEnabled"
              @update:model-value="form.vlmEnabled = $event"
            />

            <!-- 任务精度 + 策略 -->
            <StrategySection
              :precision="form.precision"
              :strategy="form.strategy"
              :begin-at="form.beginAt"
              :end-at="form.endAt"
              :repeat-type="form.repeatType"
              :repeat-interval="form.repeatInterval"
              :min-battery-capacity="form.minBatteryCapacity"
              :is-edit="initialTaskUuid !== null"
              @update:precision="form.precision = $event"
              @update:strategy="form.strategy = $event"
              @update:begin-at="form.beginAt = $event"
              @update:end-at="form.endAt = $event"
              @update:repeat-type="form.repeatType = $event"
              @update:repeat-interval="form.repeatInterval = $event"
              @update:min-battery-capacity="form.minBatteryCapacity = $event"
            />
            <p v-if="errors.beginAt" class="field-error">{{ errors.beginAt }}</p>
            <p v-if="errors.endAt" class="field-error">{{ errors.endAt }}</p>

            <!-- 安全设置 -->
            <SafetySection
              :resumable-enabled="form.resumableEnabled"
              :smart-rth-enabled="form.smartRthEnabled"
              :rth-altitude="form.rthAltitude"
              :out-of-control-action="form.outOfControlAction"
              @update:resumable-enabled="form.resumableEnabled = $event"
              @update:smart-rth-enabled="form.smartRthEnabled = $event"
              @update:rth-altitude="form.rthAltitude = $event"
              @update:out-of-control-action="form.outOfControlAction = $event"
            />
          </div>

          <!-- 底部操作栏 -->
          <div class="form-footer">
            <p v-if="detailError" class="general-error">{{ detailError }}</p>
            <p v-if="errors.general" class="general-error">{{ errors.general }}</p>
            <button class="btn btn-cancel" type="button" @click="goBack">取消</button>
            <button
              class="btn btn-confirm"
              type="button"
              :disabled="submitting || detailLoading || detailError !== null"
              @click="handleSubmit"
            >
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧地图 -->
      <div class="map-panel">
        <div ref="mapContainer" class="cesium-map" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.create-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--app-bg);
}

.create-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  border-bottom: 1px solid var(--app-border);
  flex-shrink: 0;
}

.back-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--app-border);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--app-text-secondary);
  cursor: pointer;
  transition: all var(--transition-base);
}

.back-btn:hover {
  color: var(--app-text);
  background: var(--app-hover-bg);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-text);
  margin: 0;
}

.create-main {
  flex: 1;
  display: flex;
  min-height: 0;
}

.form-panel {
  width: 480px;
  flex-shrink: 0;
  border-right: 1px solid var(--app-border);
  display: flex;
  flex-direction: column;
}

.form-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.form-container {
  flex: 1;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.field-error {
  margin: -16px 0 0;
  font-size: 12px;
  color: var(--app-danger);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--app-border);
  flex-shrink: 0;
}

.general-error {
  margin: 0;
  margin-right: auto;
  font-size: 13px;
  color: var(--app-danger);
}

.btn {
  padding: 10px 24px;
  border-radius: var(--radius-md);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.btn-cancel {
  border: 1px solid var(--app-border);
  background: transparent;
  color: var(--app-text-secondary);
}

.btn-cancel:hover {
  background: var(--app-hover-bg);
  color: var(--app-text);
}

.btn-confirm {
  border: none;
  background: var(--app-primary);
  color: #ffffff;
  font-weight: 500;
}

.btn-confirm:hover:not(:disabled) {
  background: var(--app-primary-hover);
}

.btn-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.map-panel {
  flex: 1;
  min-width: 0;
  position: relative;
}

.cesium-map {
  width: 100%;
  height: 100%;
}
</style>
