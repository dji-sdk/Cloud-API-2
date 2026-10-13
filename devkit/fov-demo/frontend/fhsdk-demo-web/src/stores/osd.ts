// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'
import type { TelemetryMessage } from '@/ws/telemetry'
import { formatSpeedMps, formatWindSpeed } from '@/utils/formatOsd'

/**
 * 遥测 OSD 状态（Pinia）。
 *
 * WS 消息类型与字段：
 * - mqtt_osd + field：慢通道（飞机位置/姿态/电量、机场位置、云台 MQTT 等）
 * - drc_camera：DRC 快通道激光测距 → fastMeasure
 * - sei：直播码流 SEI（AR 姿态/位置 + FOV），需先开直播
 */
export interface GimbalPayloadOsd {
  payloadIndex: string
  gimbalPitch: number
  gimbalRoll: number
  gimbalYaw: number
}

export interface MeasureTargetOsd {
  payloadIndex: string
  latitude: number
  longitude: number
  altitude: number
  distance: number
  laserMeasureState: number
}

const emptyMeasure = (): MeasureTargetOsd => ({
  payloadIndex: '',
  latitude: 0,
  longitude: 0,
  altitude: 0,
  distance: 0,
  laserMeasureState: 0,
})

export const useOsdStore = defineStore('osd', () => {
  const batteryPercent = ref('--')
  const remainFlightTime = ref<number | null>(null)
  const returnHomePower = ref<number | null>(null)
  const landingPower = ref<number | null>(null)
  const horizontalSpeed = ref('--')
  const verticalSpeed = ref('--')
  const windSpeed = ref('--')
  const windDirection = ref<number | null>(null)
  const location = reactive({ latitude: 0, longitude: 0, height: 0, elevation: 0 })
  const dockLocation = reactive({ latitude: 0, longitude: 0, height: 0, heading: 0 })
  const attitude = reactive({ attitudeHead: 0, attitudePitch: 0, attitudeRoll: 0 })
  const gimbalPayloads = ref<GimbalPayloadOsd[]>([])
  const slowMeasure = reactive<MeasureTargetOsd>(emptyMeasure()) // MQTT payloads OSD 激光（慢）
  const fastMeasure = reactive<MeasureTargetOsd>(emptyMeasure()) // DRC listenCameraMeasureTarget（快）
  const sei = reactive<Record<string, number>>({}) // 直播 SEI，由后端 type=sei 推送

  function handleMessage(msg: TelemetryMessage) {
    if (msg.type === 'mqtt_osd') {
      const d = msg.data
      switch (msg.field) {
        case 'battery':
          batteryPercent.value = String(d.capacityPercent ?? '--')
          remainFlightTime.value = d.remainFlightTime != null ? Number(d.remainFlightTime) : null
          returnHomePower.value = d.returnHomePower != null ? Number(d.returnHomePower) : null
          landingPower.value = d.landingPower != null ? Number(d.landingPower) : null
          break
        case 'location':
          Object.assign(location, d)
          break
        case 'dockLocation':
          Object.assign(dockLocation, d)
          break
        case 'speed':
          horizontalSpeed.value = formatSpeedMps(d.horizontalSpeed)
          verticalSpeed.value = formatSpeedMps(d.verticalSpeed)
          break
        case 'wind':
          windSpeed.value = formatWindSpeed(d.windSpeed)
          windDirection.value = d.windDirection != null ? Number(d.windDirection) : null
          break
        case 'attitude':
          Object.assign(attitude, d)
          break
        case 'gimbalAttitude': {
          const list = (d.payloads as GimbalPayloadOsd[] | undefined) ?? []
          gimbalPayloads.value = list.map((p) => ({
            payloadIndex: String(p.payloadIndex ?? ''),
            gimbalPitch: Number(p.gimbalPitch ?? 0),
            gimbalRoll: Number(p.gimbalRoll ?? 0),
            gimbalYaw: Number(p.gimbalYaw ?? 0),
          }))
          break
        }
        case 'measureTarget':
          Object.assign(slowMeasure, d)
          break
      }
    } else if (msg.type === 'drc_camera') {
      Object.assign(fastMeasure, msg.data)
    } else if (msg.type === 'sei') {
      Object.assign(sei, msg.data)
    }
  }

  function clearSei() {
    for (const k of Object.keys(sei)) delete sei[k]
  }

  function reset() {
    batteryPercent.value = '--'
    remainFlightTime.value = null
    returnHomePower.value = null
    landingPower.value = null
    horizontalSpeed.value = '--'
    verticalSpeed.value = '--'
    windSpeed.value = '--'
    windDirection.value = null
    Object.assign(location, { latitude: 0, longitude: 0, height: 0, elevation: 0 })
    Object.assign(dockLocation, { latitude: 0, longitude: 0, height: 0, heading: 0 })
    Object.assign(attitude, { attitudeHead: 0, attitudePitch: 0, attitudeRoll: 0 })
    gimbalPayloads.value = []
    Object.assign(slowMeasure, emptyMeasure())
    Object.assign(fastMeasure, emptyMeasure())
    for (const k of Object.keys(sei)) delete sei[k]
  }

  return {
    batteryPercent,
    remainFlightTime,
    returnHomePower,
    landingPower,
    horizontalSpeed,
    verticalSpeed,
    windSpeed,
    windDirection,
    location,
    dockLocation,
    attitude,
    gimbalPayloads,
    slowMeasure,
    fastMeasure,
    sei,
    handleMessage,
    reset,
    clearSei,
  }
})
