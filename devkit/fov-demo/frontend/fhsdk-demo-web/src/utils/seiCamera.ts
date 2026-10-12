// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * SEI 相机 WGS84 坐标换算。
 *
 * SEI AR 包中的 cameraPosX/Y 为相对机场 GND 原点的北/东偏移（毫米）；
 * altitude 为相对起飞点高度（毫米）。
 */
/** 与逆投影示例一致的地球半径（米） */
const EARTH_R = 6371393

export interface Wgs84Position {
  lat: number
  lng: number
  height: number
}

export interface AircraftFallback {
  lat: number
  lng: number
  height: number
}

function hasValidCoord(lat: number, lng: number): boolean {
  return Number.isFinite(lat) && Number.isFinite(lng) && !(lat === 0 && lng === 0)
}

/**
 * 以机场为 GND 原点，将 SEI 相机偏移换算为 WGS84。
 * cameraPosX/Y：北/东 mm；altitude：相对起飞点高度 mm（向上为正）。
 * 无有效机场坐标时回退到飞机位置（相机挂在机体上）。
 */
export function computeSeiCameraWgs84(
  dockLat: number,
  dockLng: number,
  dockHeight: number,
  cameraPosXmm: number | undefined,
  cameraPosYmm: number | undefined,
  altitudeMm: number | undefined,
  aircraft?: AircraftFallback,
): Wgs84Position | null {
  const hasGndOffset = Number.isFinite(cameraPosXmm) && Number.isFinite(cameraPosYmm)
  // 机场 (0,0) 视为不可用：否则会把视锥画到赤道附近
  if (hasGndOffset && hasValidCoord(dockLat, dockLng)) {
    const north = (cameraPosXmm as number) / 1000
    const east = (cameraPosYmm as number) / 1000
    const up = Number.isFinite(altitudeMm) ? (altitudeMm as number) / 1000 : 0
    const latRad = (dockLat * Math.PI) / 180
    const lat = dockLat + (north / EARTH_R) * (180 / Math.PI)
    const lng = dockLng + (east / (EARTH_R * Math.cos(latRad))) * (180 / Math.PI)
    const height = dockHeight + up
    return { lat, lng, height }
  }

  if (
    aircraft
    && Number.isFinite(aircraft.lat)
    && Number.isFinite(aircraft.lng)
    && (aircraft.lat !== 0 || aircraft.lng !== 0)
  ) {
    return {
      lat: aircraft.lat,
      lng: aircraft.lng,
      height: aircraft.height,
    }
  }

  return null
}

/** 有 SEI 相机姿态即可绘制（位置可由 GND 偏移或飞机坐标回退） */
export function isSeiCameraReady(sei: Record<string, number>): boolean {
  if (!Object.keys(sei).length) return false
  return (
    Number.isFinite(sei.cameraYaw)
    && Number.isFinite(sei.cameraPitch)
    && Number.isFinite(sei.cameraRoll)
  )
}
