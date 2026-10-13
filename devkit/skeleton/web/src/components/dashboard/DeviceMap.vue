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
import { ref, onMounted, onUnmounted, watch } from 'vue'
import type { DeviceMarker } from '@/types/dashboard'
import { applyBaseImagery } from '@/utils/cesiumImagery'

const props = defineProps<{ devices: DeviceMarker[] }>()

const mapContainer = ref<HTMLDivElement | null>(null)
let viewer: Cesium.Viewer | null = null
let removeClickHandler: (() => void) | null = null

// Cesium.Color 延迟到 onMounted 内初始化，避免 CDN 未加载时模块求值失败
let DOCK_COLOR!: Cesium.Color
let DRONE_COLOR!: Cesium.Color
let LABEL_BG_COLOR!: Cesium.Color

const TYPE_LABEL: Record<string, string> = {
  dock: '机场',
  drone: '飞行器',
  relay: '中继',
  base_station: '基站',
}

// 自定义弹窗状态
const popupVisible = ref(false)
const popupStyle = ref({ left: '0px', top: '0px' })
const popupData = ref({
  name: '',
  sn: '',
  type: '',
  status: '',
  isOnline: false,
})

function clearEntities(): void {
  viewer?.entities.removeAll()
}

/** 关闭自定义弹窗 */
function closePopup(): void {
  popupVisible.value = false
}

/** 设备实体点击 → 显示自定义弹窗 */
function setupClickHandler(): void {
  if (!viewer) return
  const handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  handler.setInputAction((click: { position: Cesium.Cartesian2 }) => {
    if (!viewer) return
    const picked = viewer.scene.pick(click.position)
    if (Cesium.defined(picked) && picked.id?.properties) {
      const entity = picked.id
      const p = entity.properties
      popupData.value = {
        name: p.deviceName?.getValue() ?? '',
        sn: p.deviceSn?.getValue() ?? '',
        type: p.deviceType?.getValue() ?? '',
        status: p.deviceStatus?.getValue() ?? '',
        isOnline: p.isOnline?.getValue() ?? false,
      }

      // 定位弹窗到点击位置上方
      const canvasPos = click.position
      popupStyle.value = {
        left: `${canvasPos.x}px`,
        top: `${canvasPos.y - 20}px`,
      }
      popupVisible.value = true
    } else {
      closePopup()
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

  removeClickHandler = () => handler.destroy()

  // 拖动/缩放地图时关闭弹窗，避免弹窗悬在错误位置
  viewer.scene.camera.moveStart.addEventListener(closePopup)
}

function addDeviceEntities(): void {
  if (!viewer) return
  clearEntities()
  closePopup()

  const positions: Cesium.Cartesian3[] = []

  for (const device of props.devices) {
    if (device.lat === 0 && device.lng === 0) continue

    const position = Cesium.Cartesian3.fromDegrees(device.lng, device.lat)
    positions.push(position)

    const color = device.type === 'dock' ? DOCK_COLOR : DRONE_COLOR
    const typeLabel = TYPE_LABEL[device.type] ?? device.type
    // toLowerCase() 兼容后端返回 'ONLINE'/'Online' 等大小写变体
    const isOnline = device.status.toLowerCase() === 'online'
    const statusText = isOnline ? '● 在线' : '● 离线'
    const shortSn = device.sn.length > 8 ? '…' + device.sn.slice(-8) : device.sn
    // 多行标签：第1行=设备名（降级SN末8位）+状态，第2行=类型
    const labelText = `${device.name || shortSn}  ${statusText}\n[${typeLabel}]`

    viewer.entities.add({
      position,
      point: {
        pixelSize: 14,
        color,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2,
        heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
      },
      label: {
        text: labelText,
        font: 'bold 13px sans-serif',
        fillColor: Cesium.Color.WHITE,
        outlineColor: Cesium.Color.BLACK,
        outlineWidth: 3,
        backgroundColor: LABEL_BG_COLOR,
        backgroundPadding: new Cesium.Cartesian2(8, 5),
        showBackground: true,
        style: Cesium.LabelStyle.FILL_AND_OUTLINE,
        pixelOffset: new Cesium.Cartesian2(0, -36),
        heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
        horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
        verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
      },
      // 存储设备数据供点击弹窗使用
      properties: {
        deviceName: device.name || device.sn,
        deviceSn: device.sn,
        deviceType: typeLabel,
        deviceStatus: device.status,
        isOnline,
      },
    })
  }

  if (positions.length === 0 || !viewer) return

  if (positions.length === 1) {
    // 单设备时 Rectangle.fromCartesianArray 返回零面积矩形，会触发极端缩放
    const [pos] = positions
    const carto = Cesium.Ellipsoid.WGS84.cartesianToCartographic(pos)
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromRadians(carto.longitude, carto.latitude, 50000),
    })
  } else {
    viewer.camera.flyTo({
      destination: Cesium.Rectangle.fromCartesianArray(positions),
    })
  }
}

onMounted(async () => {
  if (!mapContainer.value) return

  DOCK_COLOR     = Cesium.Color.fromCssColorString('#3fb950')
  DRONE_COLOR    = Cesium.Color.fromCssColorString('#3b82f6')
  LABEL_BG_COLOR = Cesium.Color.fromCssColorString('rgba(14,18,28,0.82)')

  try {
    viewer = new Cesium.Viewer(mapContainer.value, {
      animation: false,
      baseLayer: false,
      baseLayerPicker: false,
      fullscreenButton: false,
      geocoder: false,
      homeButton: false,
      infoBox: false,
      navigationHelpButton: false,
      sceneModePicker: false,
      timeline: false,
      selectionIndicator: false,
      terrainProvider: new Cesium.EllipsoidTerrainProvider(),
    })
  } catch (err) {
    console.error('[DeviceMap] Cesium Viewer 创建失败（WebGL 不可用？）', err)
    return
  }

  // 移除默认 Bing Maps（需要 API key），改用卫星影像底图
  try {
    await applyBaseImagery(viewer)
  } catch (err) {
    console.error('[DeviceMap] 底图加载失败，地图将使用默认背景', err)
  }

  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(108, 34, 5000000),
  })

  setupClickHandler()
  addDeviceEntities()
})

watch(() => props.devices, () => {
  addDeviceEntities()
}, { deep: true })

onUnmounted(() => {
  removeClickHandler?.()
  removeClickHandler = null
  if (viewer && !viewer.isDestroyed()) {
    viewer.destroy()
    viewer = null
  }
})
</script>

<template>
  <div ref="mapContainer" class="device-map">
    <!-- 自定义设备信息弹窗 -->
    <div
      v-if="popupVisible"
      class="device-popup"
      :style="popupStyle"
    >
      <button class="popup-close" @click="closePopup">&times;</button>
      <div class="popup-name">{{ popupData.name }}</div>
      <div class="popup-row">
        <span class="popup-label">SN</span>
        <span class="popup-value">{{ popupData.sn }}</span>
      </div>
      <div class="popup-row">
        <span class="popup-label">类型</span>
        <span class="popup-value">{{ popupData.type }}</span>
      </div>
      <div class="popup-row">
        <span class="popup-label">状态</span>
        <span
          class="popup-value"
          :class="popupData.isOnline ? 'status-online' : 'status-offline'"
        >
          {{ popupData.isOnline ? '● 在线' : '● 离线' }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.device-map {
  width: 100%;
  height: 100%;
  position: relative;
}

/* 隐藏 Cesium 默认 logo（开发/内部部署环境） */
.device-map :deep(.cesium-widget-credits) {
  display: none;
}

/* ── 自定义弹窗 ── */
.device-popup {
  position: absolute;
  z-index: 100;
  transform: translate(-50%, -100%);
  background: rgba(14, 18, 28, 0.92);
  border: 1px solid rgba(45, 140, 240, 0.5);
  border-radius: 8px;
  padding: 12px 16px;
  min-width: 200px;
  color: #e0e8f0;
  font-size: 13px;
  line-height: 1.6;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(6px);
  pointer-events: auto;
}

/* 底部小三角 */
.device-popup::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 6px solid rgba(45, 140, 240, 0.5);
}

.popup-close {
  position: absolute;
  top: 4px;
  right: 8px;
  background: none;
  border: none;
  color: #8a9ab5;
  font-size: 18px;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}
.popup-close:hover {
  color: #fff;
}

.popup-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 8px;
  padding-right: 16px;
  word-break: break-all;
}

.popup-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.popup-label {
  color: #6a7a8e;
  min-width: 30px;
  flex-shrink: 0;
}

.popup-value {
  color: #c8d8e8;
  word-break: break-all;
}

.status-online {
  color: #19be6b;
}

.status-offline {
  color: #f5222d;
}
</style>
