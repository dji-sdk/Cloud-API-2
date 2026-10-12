<!--
  Copyright (c) 2026 SZ DJI Technology Co., Ltd.
  SPDX-License-Identifier: Apache-2.0
-->

<script setup lang="ts">
/**
 * 驾驶舱主页面 — 大疆上云 API 2 单页入口。
 *
 * 推荐操作顺序（与顶栏状态徽章对应）：
 * 1. 选择在线机场设备
 * 2. 连接 WebSocket（接收 OSD / SEI / Payload 列表）
 * 3. 选择并确认 Payload（开流与 DRC 的前置条件）
 * 4. 开启直播（SDK 拉流 → FFmpeg WHIP 推 Oryx → 前端 WHEP 播放）
 * 5. 连接 DRC（杆量、云台复位、快激光测距、SEI 订阅）
 *
 * 地图实体在 WS 遥测到达时通过 rAF 合并刷新；折线/视锥走 Primitive API。
 */
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import * as Cesium from 'cesium'
import {
  initSession,
  fetchDevices,
  selectDevice,
  deselectDevice,
  connectDrc,
  disconnectDrc,
  sendStick,
  resetGimbal,
  startLive,
  stopLive,
  fetchPayloads,
  confirmPayload,
  type DeviceVo,
  type PayloadListData,
} from '@/api'
import { formatApiError } from '@/api/client'
import { connectTelemetry, disconnectTelemetry } from '@/ws/telemetry'
import { useSessionStore } from '@/stores/session'
import { useOsdStore } from '@/stores/osd'
import { useUiStore } from '@/stores/ui'
import { useStickControl } from '@/composables/useStickControl'
import { useGimbalDrag } from '@/composables/useGimbalDrag'
import { playWhep } from '@/utils/whep'
import { useOsdTable } from '@/composables/useOsdTable'
import TrainingBanner from '@/components/training/TrainingBanner.vue'
import VideoCenterHint from '@/components/training/VideoCenterHint.vue'
import MapLegend from '@/components/training/MapLegend.vue'
import { createViewer, updateAircraft, updateCamera, removeCamera, updateDock, updateMeasurePoint, removeMeasurePoint, removeSeiOpticalAxis, flyToLonLat, setMapTerrainEnabled, setMapVideoProjectionEnabled, hasIonTerrainToken, requestMapRender, resetMapLineCache, destroySeiMapLayer, setMapLiveVideo, type CameraMapEntities } from '@/cesium/map'
import { computeSeiCameraWgs84, isSeiCameraReady } from '@/utils/seiCamera'

const session = useSessionStore()
const osd = useOsdStore()
const ui = useUiStore()
const { rows: osdRows } = useOsdTable()

const devices = ref<DeviceVo[]>([])
const loading = ref(false)
const liveLoading = ref(false)
const videoRef = ref<HTMLVideoElement | null>(null)
const videoDragRef = ref<HTMLElement | null>(null)
const mapRef = ref<HTMLDivElement | null>(null)
const terrainEnabled = ref(false)
const videoProjectionEnabled = ref(false)
const terrainAvailable = hasIonTerrainToken()
let stopWhep: (() => void) | undefined
let viewer: Cesium.Viewer | undefined
let aircraftEntity: Cesium.Entity | undefined
let dockEntity: Cesium.Entity | undefined
let cameraEntities: CameraMapEntities | undefined

const currentLabel = computed(() =>
  session.selectedGatewaySn ? `当前：${session.selectedGatewaySn}` : '未选择设备',
)

const stick = useStickControl({
  enabled: () => session.drcConnected,
  onSend: (s) => {
    sendStick({ ...s, durationMs: 50 }).catch((e) => ui.pushLog(`stick error: ${e}`))
    ui.logRest('POST', '/control/stick')
  },
})

const gimbalDrag = useGimbalDrag({
  layerRef: videoDragRef,
  enabled: () => session.drcConnected && session.liveStarted,
  setGimbalDrag: stick.setGimbalDrag,
  clampStick: stick.clampStick,
})

const GIMBAL_RESET_MODES = [
  // resetMode 与 SDK resetGimbal 一致：0 回中 1 向下 2 偏航回中 3 俯仰向下
  { mode: 0, label: '回中' },
  { mode: 1, label: '向下' },
  { mode: 2, label: '偏航回中' },
  { mode: 3, label: '俯仰向下' },
] as const

async function onGimbalReset(mode: number) {
  if (!session.drcConnected) return
  try {
    ui.logRest('POST', '/control/gimbal-reset')
    await resetGimbal({ resetMode: mode })
    ui.pushLog(`云台复位 mode=${mode} 已发送`)
  } catch (e) {
    ui.pushLog(`云台复位失败: ${formatApiError(e)}`)
  }
}

function applyPayloadFromWs(data: Record<string, unknown>) {
  const list: PayloadListData = {
    payloads: (data.payloads as PayloadListData['payloads']) ?? [],
    selectedPayloadIndex: (data.selectedPayloadIndex as string | null) ?? null,
    configured: Boolean(data.configured),
    defaultPayloadIndex: data.defaultPayloadIndex as string | undefined,
  }
  session.applyPayloadList(list)
}

function onTelemetry(msg: Parameters<typeof osd.handleMessage>[0]) {
  if (msg.type === 'payload_range') {
    applyPayloadFromWs(msg.data)
    ui.logWs(msg)
    return
  }
  osd.handleMessage(msg)
  if (msg.type === 'sei' || msg.type === 'drc_camera' || msg.type === 'mqtt_osd') {
    scheduleMapUpdate()
  }
  if (msg.type !== 'sei' && msg.type !== 'drc_camera') {
    ui.logWs(msg)
  }
}

async function boot() {
  loading.value = true
  try {
    ui.logRest('POST', '/session/init')
    await initSession()
    session.sdkReady = true
    devices.value = await fetchDevices()
    ui.logRest('GET', '/devices')
  } catch (e) {
    ui.pushLog(`初始化失败: ${formatApiError(e)}`)
  } finally {
    loading.value = false
  }
}

async function onSelectDevice(d: DeviceVo) {
  if (!d.online || session.selectedGatewaySn === d.gatewaySn) return
  try {
    if (session.selectedGatewaySn) await onDeselect()
    ui.logRest('POST', '/devices/select')
    await selectDevice(d.gatewaySn)
    session.selectedGatewaySn = d.gatewaySn
    session.resetPayload()
    ui.pushLog(`已选择设备 ${d.gatewaySn}，下一步：连接 WS`)
  } catch (e) {
    ui.pushLog(`选择设备失败: ${formatApiError(e)}`)
  }
}

function onConnectWs() {
  if (session.wsConnected) return
  ui.logRest('WS', '/ws/telemetry')
  connectTelemetry(onTelemetry)
    .then(async () => {
      session.wsConnected = true
      ui.pushLog('WebSocket 已连接，等待 OSD 数据…')
      try {
        ui.logRest('GET', '/devices/payloads')
        const payloads = await fetchPayloads()
        session.applyPayloadList(payloads)
        ui.pushLog(`可用 Payload ${payloads.payloads.length} 项，请选择并确认`)
      } catch (e) {
        ui.pushLog(`拉取 Payload 列表失败: ${formatApiError(e)}`)
      }
    })
    .catch((e) => {
      session.wsConnected = false
      ui.pushLog(`WebSocket 连接失败: ${formatApiError(e)}`)
    })
}

function onDisconnectWs() {
  if (!session.wsConnected) return
  disconnectTelemetry(onTelemetry)
  session.wsConnected = false
  ui.pushLog('WebSocket 已断开')
}

async function onConfirmPayload() {
  if (!session.payloadDraft || session.payloadConfigured) return
  try {
    ui.logRest('POST', '/devices/payload')
    const data = await confirmPayload(session.payloadDraft)
    session.applyPayloadList(data)
    ui.pushLog(`Payload 已确认: ${data.selectedPayloadIndex}`)
  } catch (e) {
    ui.pushLog(`Payload 确认失败: ${formatApiError(e)}`)
  }
}

async function onStartLive() {
  if (session.liveStarted || liveLoading.value || !session.payloadConfigured) return
  liveLoading.value = true
  try {
    ui.logRest('POST', '/live/start')
    ui.pushLog('正在开启直播（拉流 + WHIP 推 Oryx，约需数秒）…')
    const stream = await startLive()
    if (videoRef.value) {
      stopWhep?.()
      stopWhep = await playWhep(videoRef.value, stream.whepUrl)
      if (viewer) setMapLiveVideo(viewer, videoRef.value)
    }
    session.liveStarted = true
    ui.pushLog(`直播已开启 payload=${stream.payloadIndex ?? '-'} WHEP: ${stream.whepUrl}`)
    scheduleMapUpdate()
  } catch (e) {
    ui.pushLog(`开启直播失败: ${formatApiError(e)}`)
  } finally {
    liveLoading.value = false
  }
}

async function onStopLive() {
  if (!session.liveStarted && !liveLoading.value) return
  try {
    ui.logRest('DELETE', '/live/stop')
    await stopLive()
    stopWhep?.()
    stopWhep = undefined
    if (videoRef.value) videoRef.value.srcObject = null
    if (viewer) {
      setMapLiveVideo(viewer, null)
      videoProjectionEnabled.value = false
      setMapVideoProjectionEnabled(viewer, false)
    }
    session.liveStarted = false
    osd.clearSei()
    scheduleMapUpdate()
    ui.pushLog('直播已关闭')
  } catch (e) {
    ui.pushLog(`关闭直播失败: ${formatApiError(e)}`)
  } finally {
    liveLoading.value = false
  }
}

async function onDeselect() {
  try {
    if (session.liveStarted) await onStopLive()
    if (session.wsConnected) onDisconnectWs()
    ui.logRest('DELETE', '/devices/select')
    await deselectDevice()
    session.selectedGatewaySn = null
    session.drcConnected = false
    session.resetPayload()
    osd.reset()
    clearMap()
    ui.pushLog('已取消选择设备')
  } catch (e) {
    ui.pushLog(`取消选择失败: ${formatApiError(e)}`)
  }
}

async function onToggleDrc() {
  try {
    if (session.drcConnected) {
      ui.logRest('DELETE', '/control/drc-link')
      await disconnectDrc()
      session.drcConnected = false
    } else {
      ui.logRest('POST', '/control/drc-link')
      await connectDrc()
      session.drcConnected = true
    }
    scheduleMapUpdate()
  } catch (e) {
    ui.pushLog(`DRC 操作失败: ${formatApiError(e)}`)
  }
}

let mapUpdateScheduled = false

function scheduleMapUpdate() {
  if (mapUpdateScheduled) return
  mapUpdateScheduled = true
  requestAnimationFrame(() => {
    mapUpdateScheduled = false
    updateMap()
    requestMapRender(viewer)
  })
}

function initMap() {
  if (!mapRef.value || viewer) return
  viewer = createViewer(mapRef.value)
}

function onTerrainToggle() {
  if (!viewer) return
  if (!terrainAvailable) {
    ui.pushLog('未配置 VITE_CESIUM_ION_TOKEN，无法加载 Ion 地形')
    terrainEnabled.value = false
    return
  }
  const ok = setMapTerrainEnabled(viewer, terrainEnabled.value)
  if (!ok) {
    terrainEnabled.value = false
    ui.pushLog('Ion 地形加载失败，请检查 Token')
    return
  }
  ui.pushLog(terrainEnabled.value ? '已开启 Ion 世界地形' : '已关闭地形（椭球）')
  updateMap()
  requestMapRender(viewer)
}

function onVideoProjectionToggle() {
  if (!viewer) return
  if (!session.liveStarted) {
    videoProjectionEnabled.value = false
    return
  }
  setMapVideoProjectionEnabled(viewer, videoProjectionEnabled.value)
  ui.pushLog(videoProjectionEnabled.value ? '已开启地图视频投影' : '已关闭地图视频投影')
  scheduleMapUpdate()
}

function hasMapCoord(lat: number, lng: number) {
  return Number.isFinite(lat) && Number.isFinite(lng) && (lat !== 0 || lng !== 0)
}

function updateMap() {
  if (!viewer || !session.selectedGatewaySn) return
  const { latitude, longitude, height } = osd.location
  const { latitude: dockLat, longitude: dockLng, height: dockH } = osd.dockLocation

  // 激光/DRC 连线起点：优先 SEI 相机位置（高频），回退 MQTT 飞机位置（~0.5Hz）
  let measureLineFrom: Cesium.Cartesian3 | undefined
  if (session.liveStarted && isSeiCameraReady(osd.sei)) {
    const cam = computeSeiCameraWgs84(
      dockLat,
      dockLng,
      dockH || 50,
      osd.sei.cameraPosX,
      osd.sei.cameraPosY,
      osd.sei.altitude,
      hasMapCoord(latitude, longitude)
        ? { lat: latitude, lng: longitude, height: height || 100 }
        : undefined,
    )
    if (cam) {
      measureLineFrom = Cesium.Cartesian3.fromDegrees(cam.lng, cam.lat, cam.height)
    }
  }
  if (!measureLineFrom && hasMapCoord(latitude, longitude)) {
    measureLineFrom = Cesium.Cartesian3.fromDegrees(longitude, latitude, height || 100)
  }

  // --- 飞机 glTF + 快激光（DRC）---
  if (hasMapCoord(latitude, longitude)) {
    aircraftEntity = updateAircraft(
      viewer,
      aircraftEntity,
      latitude,
      longitude,
      height || 100,
      osd.attitude.attitudeHead,
      osd.attitude.attitudePitch,
      osd.attitude.attitudeRoll,
    )
    const { latitude: mLat, longitude: mLng, altitude: mAlt } = osd.fastMeasure
    if (
      session.drcConnected
      && hasMapCoord(mLat, mLng)
      && measureLineFrom
    ) {
      updateMeasurePoint(
        viewer,
        'fast',
        mLat,
        mLng,
        mAlt || height || 100,
        Cesium.Color.LIME,
        measureLineFrom,
      )
    } else {
      removeMeasurePoint(viewer, 'fast')
    }
  } else {
    removeMeasurePoint(viewer, 'fast')
  }
  removeMeasurePoint(viewer, 'slow')
  // --- 机场标记 ---
  if (hasMapCoord(dockLat, dockLng)) {
    dockEntity = updateDock(viewer, dockEntity, dockLat, dockLng, dockH || 50)
  }

  // --- SEI 相机三轴（需已开直播 + SEI 姿态；位置见 seiCamera.ts）---
  if (session.liveStarted && isSeiCameraReady(osd.sei)) {
    const pos = computeSeiCameraWgs84(
      dockLat,
      dockLng,
      dockH || 50,
      osd.sei.cameraPosX,
      osd.sei.cameraPosY,
      osd.sei.altitude,
      hasMapCoord(latitude, longitude)
        ? { lat: latitude, lng: longitude, height: height || 100 }
        : undefined,
    )
    if (pos) {
      cameraEntities = updateCamera(
        viewer,
        cameraEntities,
        pos.lat,
        pos.lng,
        pos.height,
        osd.sei.cameraYaw,
        osd.sei.cameraPitch,
        osd.sei.cameraRoll,
        {
          fovHorizontalUm: osd.sei.fovHorizontal,
          fovVerticalUm: osd.sei.fovVertical,
          focalLengthUm: osd.sei.curActualFocalLength,
        },
      )
    } else {
      cameraEntities = removeCamera(viewer, cameraEntities)
      removeSeiOpticalAxis(viewer)
    }
  } else {
    cameraEntities = removeCamera(viewer, cameraEntities)
    removeSeiOpticalAxis(viewer)
  }
}

function onCenterAircraft() {
  if (!viewer) return
  const { latitude, longitude, height } = osd.location
  if (!hasMapCoord(latitude, longitude)) {
    ui.pushLog('飞机坐标不可用，请等待 OSD 数据')
    return
  }
  flyToLonLat(viewer, longitude, latitude, height || 100)
}

function onCenterDock() {
  if (!viewer) return
  const { latitude, longitude, height } = osd.dockLocation
  if (!hasMapCoord(latitude, longitude)) {
    ui.pushLog('机场坐标不可用，请等待 OSD 数据')
    return
  }
  flyToLonLat(viewer, longitude, latitude, height || 50)
}

function clearMap() {
  if (!viewer) return
  resetMapLineCache()
  destroySeiMapLayer()
  viewer.entities.removeAll()
  aircraftEntity = undefined
  dockEntity = undefined
  cameraEntities = undefined
}

onMounted(() => {
  initMap()
  stick.mount()
  void nextTick(() => gimbalDrag.mount())
  boot()
})

onUnmounted(() => {
  stick.unmount()
  gimbalDrag.unmount()
  stopWhep?.()
  disconnectTelemetry(onTelemetry)
  destroySeiMapLayer()
  viewer?.destroy()
})

watch(() => session.selectedGatewaySn, () => {
  if (!session.selectedGatewaySn) clearMap()
})
</script>

<template>
  <div class="cockpit">
    <header class="topbar">
      <div class="brand">
        <img class="brand-logo" src="/with-dji-logo_48.png" alt="DJI" />
        <h1>大疆上云 API 2</h1>
      </div>
      <span class="badge" :class="{ on: session.sdkReady }">SDK · {{ session.sdkReady ? '已就绪' : '未初始化' }}</span>
      <span class="badge" :class="{ on: session.wsConnected }">WS · {{ session.wsConnected ? '已连接' : '未连接' }}</span>
      <span class="badge" :class="{ on: session.liveStarted }">直播 · {{ session.liveStarted ? '已开启' : '未开启' }}</span>
      <span class="badge" :class="{ on: session.drcConnected }">DRC · {{ session.drcConnected ? '已连接' : '未连接' }}</span>
      <label class="training-toggle" title="显示/隐藏培训说明（图例、画面中心、三层数据摘要）">
        <input v-model="ui.showTrainingHints" type="checkbox">
        培训说明
      </label>
      <span style="margin-left:auto;font-size:12px;color:var(--muted)">{{ currentLabel }}</span>
    </header>

    <div class="hint-banner" v-if="loading">正在连接 FH 环境…</div>
    <TrainingBanner v-if="ui.showTrainingHints && !loading" />

    <div class="main">
      <section class="panel">
        <div class="panel-hd">设备列表</div>
        <div style="overflow-y:auto;flex:1">
          <div
            v-for="d in devices"
            :key="d.gatewaySn"
            class="device-item"
            :class="{ active: session.selectedGatewaySn === d.gatewaySn, disabled: !d.online }"
            @click="onSelectDevice(d)"
          >
            <div class="sn">{{ d.gatewayName || d.gatewaySn }}</div>
            <div class="device-row">
              <span class="device-row-label">机场 SN</span>
              <span class="device-row-value">{{ d.gatewaySn }}</span>
              <span class="device-status" :class="d.online ? 'on' : 'off'">
                {{ d.online ? '在线' : '离线' }}
              </span>
            </div>
            <div class="device-row">
              <span class="device-row-label">飞机 SN</span>
              <span class="device-row-value">{{ d.droneSn || '—' }}</span>
              <span
                v-if="d.droneSn"
                class="device-status"
                :class="d.droneOnline ? 'on' : 'off'"
              >
                {{ d.droneOnline ? '在线' : '离线' }}
              </span>
              <span v-else class="device-status na">无</span>
            </div>
          </div>
        </div>
        <div style="padding:8px;border-top:1px solid var(--border)">
          <button style="width:100%" :disabled="!session.selectedGatewaySn" @click="onDeselect">取消选择</button>
        </div>
      </section>

      <div class="center-col">
        <section class="panel" style="flex:1">
          <div class="live-toolbar">
            <button :disabled="!session.selectedGatewaySn || session.wsConnected" @click="onConnectWs">
              ① 连接 WS
            </button>
            <button :disabled="!session.wsConnected" @click="onDisconnectWs">断开 WS</button>
            <button
              class="primary"
              :disabled="!session.wsConnected || !session.payloadConfigured || session.liveStarted || liveLoading"
              @click="onStartLive"
            >
              {{ liveLoading ? '开启中…' : '② 开启直播' }}
            </button>
            <button
              class="danger"
              :disabled="!session.liveStarted"
              @click="onStopLive"
            >
              关闭直播
            </button>
            <button
              :class="session.drcConnected ? 'danger' : 'primary'"
              :disabled="!session.wsConnected || !session.payloadConfigured"
              @click="onToggleDrc"
            >
              {{ session.drcConnected ? '断开 DRC' : '③ 连接 DRC' }}
            </button>
          </div>
          <div class="live-wrap">
            <video v-show="session.liveStarted" ref="videoRef" autoplay playsinline muted />
            <div
              v-show="session.liveStarted"
              ref="videoDragRef"
              class="drag-layer"
              :class="{ disabled: !session.drcConnected }"
              title="按住拖动：云台偏航/俯仰（需 DRC）"
            />
            <VideoCenterHint
              v-if="ui.showTrainingHints && session.liveStarted"
              :drc-connected="session.drcConnected"
            />
            <div v-if="!session.selectedGatewaySn" class="live-placeholder">
              步骤：选择左侧设备 → 连接 WS → 开启直播
            </div>
            <div v-else-if="!session.liveStarted" class="live-placeholder">
              已选设备：连接 WS → 选择 Payload 并确认 → 开启直播
            </div>
          </div>
        </section>
        <section v-if="session.selectedGatewaySn" class="panel payload-panel">
          <div class="panel-hd">Payload 配置</div>
          <div class="payload-bar">
            <template v-if="!session.wsConnected">
              <span class="hint">请先「① 连接 WS」以获取可用 Payload 列表</span>
            </template>
            <template v-else>
              <label class="payload-label" for="payload-select">可用 Payload</label>
              <select
                id="payload-select"
                v-model="session.payloadDraft"
                class="payload-select"
                :disabled="session.payloadConfigured || !session.payloadOptions.length"
              >
                <option v-if="!session.payloadOptions.length" value="">等待设备上报…</option>
                <option
                  v-for="p in session.payloadOptions"
                  :key="`${p.payloadIndex}-${p.videoId}`"
                  :value="p.payloadIndex"
                >
                  {{ p.payloadIndex }} · {{ p.videoId || '—' }} · {{ p.deviceSn }}
                </option>
              </select>
              <button
                class="primary"
                :disabled="!session.payloadDraft || session.payloadConfigured"
                @click="onConfirmPayload"
              >
                确认 Payload
              </button>
              <span v-if="session.payloadConfigured" class="payload-confirmed">
                当前：{{ session.confirmedPayloadIndex }}
              </span>
              <span v-else class="hint">确认后才可开启直播 / 连接 DRC</span>
            </template>
          </div>
        </section>
        <section class="panel osd-table-panel">
          <div class="panel-hd">遥测对比</div>
          <div class="osd-table-wrap">
            <table class="osd-table">
              <caption v-if="ui.showTrainingHints" class="osd-table-caption">
                SEI 列需「② 开启直播」；DRC 激光列需「③ 连接 DRC」。低频 MQTT 不适合与画面对齐，视锥/光轴靠 SEI。
              </caption>
              <thead>
                <tr>
                  <th class="col-label">指标</th>
                  <th>
                    <span class="tag tag-slow">MQTT</span> 飞机
                    <div v-if="ui.showTrainingHints" class="th-freq">~0.5Hz</div>
                  </th>
                  <th>
                    <span class="tag tag-slow">MQTT</span> 机场
                    <div v-if="ui.showTrainingHints" class="th-freq">~0.5Hz</div>
                  </th>
                  <th>
                    <span class="tag tag-sei">SEI</span>
                    <div v-if="ui.showTrainingHints" class="th-freq">~30fps · 需直播</div>
                  </th>
                  <th>
                    <span class="tag tag-fast">DRC</span> 激光测距
                    <div v-if="ui.showTrainingHints" class="th-freq">需 DRC</div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in osdRows" :key="row.label">
                  <td class="col-label">{{ row.label }}</td>
                  <td>{{ row.aircraft }}</td>
                  <td>{{ row.dock }}</td>
                  <td :class="{ dim: !session.liveStarted }">{{ row.sei }}</td>
                  <td :class="{ dim: !session.drcConnected }">{{ row.measureDrc }}</td>
                </tr>
              </tbody>
            </table>
            <p v-if="ui.showTrainingHints" class="osd-table-footnote">
              单位：MQTT 高度 m；SEI 高度 mm；风速 dm/s；速度 m/s；FOV 行为 μm 原始值 + HFOV/VFOV/DFOV 换算（°）。
            </p>
          </div>
        </section>
      </div>

      <section class="panel map-panel">
        <div class="panel-hd map-hd">
          <span>Cesium 地图</span>
          <div class="map-actions">
            <label class="terrain-toggle" :class="{ disabled: !terrainAvailable }" :title="terrainAvailable ? 'Cesium Ion 世界地形' : '请在 .env.local 配置 VITE_CESIUM_ION_TOKEN'">
              <input
                v-model="terrainEnabled"
                type="checkbox"
                :disabled="!terrainAvailable"
                @change="onTerrainToggle"
              >
              Ion 地形
            </label>
            <label
              class="terrain-toggle"
              :class="{ disabled: !session.liveStarted }"
              title="将 WHEP 直播画面贴到视锥地面投影（较耗性能，默认关闭）"
            >
              <input
                v-model="videoProjectionEnabled"
                type="checkbox"
                :disabled="!session.liveStarted"
                @change="onVideoProjectionToggle"
              >
              视频投影
            </label>
            <button :disabled="!session.selectedGatewaySn" @click="onCenterDock">机场居中</button>
            <button :disabled="!session.selectedGatewaySn" @click="onCenterAircraft">飞机居中</button>
          </div>
        </div>
        <div class="map-body-wrap">
          <div class="map-body" ref="mapRef" />
          <MapLegend
            v-if="ui.showTrainingHints"
            :drc-connected="session.drcConnected"
            :live-started="session.liveStarted"
            :video-projection-enabled="videoProjectionEnabled"
          />
        </div>
      </section>

      <div class="bottom-bar">
        <section class="panel">
          <div class="panel-hd">杆量</div>
          <div class="keys">
            <div class="key" :class="{ active: stick.isActive('W') }">W</div>
            <div class="key" :class="{ active: stick.isActive('A') }">A</div>
            <div class="key" :class="{ active: stick.isActive('S') }">S</div>
            <div class="key" :class="{ active: stick.isActive('D') }">D</div>
            <span class="key-sep" />
            <div class="key" :class="{ active: stick.isActive('R') }">R</div>
            <div class="key" :class="{ active: stick.isActive('F') }">F</div>
            <span class="key-sep" />
            <div class="key" :class="{ active: stick.isActive('Q') }">Q</div>
            <div class="key" :class="{ active: stick.isActive('E') }">E</div>
            <span class="hint">平移·升降·偏航 · 需 DRC · 直播区拖动 → 云台</span>
          </div>
        </section>
        <section class="panel">
          <div class="panel-hd">云台复位</div>
          <div class="gimbal-reset" :class="{ disabled: !session.drcConnected }">
            <button
              v-for="item in GIMBAL_RESET_MODES"
              :key="item.mode"
              :disabled="!session.drcConnected"
              @click="onGimbalReset(item.mode)"
            >{{ item.label }}</button>
            <span class="hint">需 DRC · resetGimbal</span>
          </div>
        </section>
        <section class="panel">
          <div class="panel-hd">消息日志</div>
          <div class="log">
            <div v-for="(line, i) in ui.logs" :key="i">{{ line }}</div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
