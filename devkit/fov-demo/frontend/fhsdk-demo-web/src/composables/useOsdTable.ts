// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

import { computed, type ComputedRef } from 'vue'
import { storeToRefs } from 'pinia'
import { useOsdStore } from '@/stores/osd'
import { useSessionStore } from '@/stores/session'
import { decodeSeiCoord, formatCoord, formatFlightTime, formatNum, formatSeiAltitudeMm, formatSeiFovCell, formatWindDir } from '@/utils/formatOsd'

/**
 * 遥测对比表行数据。
 * 列：MQTT 飞机 / MQTT 机场 / SEI（需直播）/ 快激光 DRC OSD（需 DRC）
 */
export interface OsdTableRow {
  label: string
  aircraft: string
  dock: string
  sei: string
  measureDrc: string
}

function cell(v: string, gateRequired = false, gateOpen = false): string {
  if (gateRequired && !gateOpen) return '—'
  return v || '—'
}

function gimbalVal(
  payloads: { gimbalPitch: number; gimbalRoll: number; gimbalYaw: number }[],
  pick: 'pitch' | 'roll' | 'yaw',
): string {
  if (!payloads.length) return '—'
  const p = payloads[0]
  const val = pick === 'pitch' ? p.gimbalPitch : pick === 'roll' ? p.gimbalRoll : p.gimbalYaw
  return formatNum(val, '°')
}

export function useOsdTable(): { rows: ComputedRef<OsdTableRow[]> } {
  const osd = useOsdStore()
  const session = useSessionStore()
  const {
    location,
    dockLocation,
    attitude,
    gimbalPayloads,
    batteryPercent,
    remainFlightTime,
    returnHomePower,
    landingPower,
    horizontalSpeed,
    verticalSpeed,
    windSpeed,
    windDirection,
    fastMeasure,
    sei,
  } = storeToRefs(osd)

  const rows = computed<OsdTableRow[]>(() => {
    const drc = session.drcConnected
    const live = session.liveStarted
    const seiNum = (key: string) => {
      const v = sei.value[key]
      return v != null && Number.isFinite(v) ? v : undefined
    }

    return [
      {
        label: '坐标',
        aircraft: formatCoord(location.value.latitude, location.value.longitude),
        dock: formatCoord(dockLocation.value.latitude, dockLocation.value.longitude),
        sei: cell(
          `${decodeSeiCoord(seiNum('latitude'), 'lat')}, ${decodeSeiCoord(seiNum('longitude'), 'lng')}`,
          true,
          live,
        ),
        measureDrc: cell(formatCoord(fastMeasure.value.latitude, fastMeasure.value.longitude), true, drc),
      },
      {
        label: '高度',
        aircraft: `椭球 ${formatNum(location.value.height, ' m')} · 相对 ${formatNum(location.value.elevation, ' m')}`,
        dock: formatNum(dockLocation.value.height, ' m'),
        sei: cell(formatSeiAltitudeMm(seiNum('altitude')), true, live),
        measureDrc: cell(formatNum(fastMeasure.value.altitude, ' m'), true, drc),
      },
      {
        label: '偏航',
        aircraft: formatNum(attitude.value.attitudeHead, '°'),
        dock: formatNum(dockLocation.value.heading, '°'),
        sei: cell(formatNum(seiNum('cameraYaw'), '°'), true, live),
        measureDrc: '—',
      },
      {
        label: '俯仰',
        aircraft: `机体 ${formatNum(attitude.value.attitudePitch, '°')} · 云台 ${gimbalVal(gimbalPayloads.value, 'pitch')}`,
        dock: '—',
        sei: cell(formatNum(seiNum('cameraPitch'), '°'), true, live),
        measureDrc: '—',
      },
      {
        label: '横滚',
        aircraft: `机体 ${formatNum(attitude.value.attitudeRoll, '°')} · 云台 ${gimbalVal(gimbalPayloads.value, 'roll')}`,
        dock: '—',
        sei: cell(formatNum(seiNum('cameraRoll'), '°'), true, live),
        measureDrc: '—',
      },
      {
        label: '速度',
        aircraft: `H ${horizontalSpeed.value} · V ${verticalSpeed.value}`,
        dock: '—',
        sei: '—',
        measureDrc: '—',
      },
      {
        label: '电量',
        aircraft: `${batteryPercent.value}% · 飞行 ${formatFlightTime(remainFlightTime.value)} · 返航 ${returnHomePower.value != null ? `${returnHomePower.value}%` : '—'} · 降落 ${landingPower.value != null ? `${landingPower.value}%` : '—'}`,
        dock: '—',
        sei: '—',
        measureDrc: '—',
      },
      {
        label: '风速/风向',
        aircraft: `${windSpeed.value} · ${formatWindDir(windDirection.value)}`,
        dock: '—',
        sei: '—',
        measureDrc: '—',
      },
      {
        label: '载荷',
        aircraft: gimbalPayloads.value[0]?.payloadIndex || '—',
        dock: '—',
        sei: '—',
        measureDrc: cell(fastMeasure.value.payloadIndex || '—', true, drc),
      },
      {
        label: '距离',
        aircraft: '—',
        dock: '—',
        sei: '—',
        measureDrc: cell(formatNum(fastMeasure.value.distance, ' m'), true, drc),
      },
      {
        label: '激光状态',
        aircraft: '—',
        dock: '—',
        sei: '—',
        measureDrc: cell(String(fastMeasure.value.laserMeasureState ?? '—'), true, drc),
      },
      {
        label: 'FOV / 焦距',
        aircraft: '—',
        dock: '—',
        sei: cell(
          formatSeiFovCell(seiNum('fovHorizontal'), seiNum('fovVertical'), seiNum('curActualFocalLength')),
          true,
          live,
        ),
        measureDrc: '—',
      },
    ]
  })

  return { rows }
}
