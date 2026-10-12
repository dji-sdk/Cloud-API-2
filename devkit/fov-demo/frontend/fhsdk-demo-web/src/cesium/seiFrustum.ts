// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * SEI NED 相机 → Cesium FrustumGeometry 姿态与 FOV。
 * 相机光轴 = 本体系 +Z；Cesium FrustumGeometry 沿 rotation 第 3 列（局部 +Z）张开。
 */
import * as Cesium from 'cesium'
import { nedToEcefDirection, opticalAxisNed, rotateZYX } from '@/utils/seiNed'

const DEFAULT_FAR_M = 2000
const DEFAULT_VERTICAL_FOV_RAD = Cesium.Math.toRadians(45)

export interface SeiFovAngles {
  horizontalRad: number
  verticalRad: number
  aspectRatio: number
}

/** SEI 传感器物理量（μm）→ 水平/垂直视场角（弧度） */
export function seiFovFromSensor(
  fovHorizontalUm: number | undefined,
  fovVerticalUm: number | undefined,
  focalLengthUm: number | undefined,
): SeiFovAngles | null {
  if (!focalLengthUm || focalLengthUm <= 0) return null
  const hUm = fovHorizontalUm ?? 0
  const vUm = fovVerticalUm ?? 0
  if (hUm <= 0 || vUm <= 0) return null
  const horizontalRad = 2 * Math.atan(hUm / (2 * focalLengthUm))
  const verticalRad = 2 * Math.atan(vUm / (2 * focalLengthUm))
  if (horizontalRad <= 0 || verticalRad <= 0) return null
  const aspectRatio = Math.tan(horizontalRad * 0.5) / Math.tan(verticalRad * 0.5)
  return { horizontalRad, verticalRad, aspectRatio }
}

/** NED 相机三轴在 ECEF 下的单位方向（+X 右 / +Y 下 / +Z 光轴） */
export function nedCameraBodyAxesInEcef(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): { optical: Cesium.Cartesian3; right: Cesium.Cartesian3; down: Cesium.Cartesian3 } {
  return {
    optical: nedToEcefDirection(lon, lat, height, opticalAxisNed(yawDeg, pitchDeg, rollDeg)),
    right: nedToEcefDirection(lon, lat, height, rotateZYX([1, 0, 0], yawDeg, pitchDeg, rollDeg)),
    down: nedToEcefDirection(lon, lat, height, rotateZYX([0, 1, 0], yawDeg, pitchDeg, rollDeg)),
  }
}

/**
 * NED 相机姿态 → FrustumGeometry orientation。
 * Cesium FrustumGeometry：视锥沿 rotation 第 3 列（局部 +Z）延伸；内部对第 1 列取反后作为 view right。
 */
export function nedCameraFrustumOrientation(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): Cesium.Quaternion {
  const { optical, right, down } = nedCameraBodyAxesInEcef(lon, lat, height, yawDeg, pitchDeg, rollDeg)

  // 与 Cesium DebugCameraPrimitive 一致：col0=−right，col1=up(−down)，col2=direction(光轴)
  const col0 = Cesium.Cartesian3.negate(right, new Cesium.Cartesian3())
  const col1 = Cesium.Cartesian3.negate(down, new Cesium.Cartesian3())
  const col2 = Cesium.Cartesian3.clone(optical, new Cesium.Cartesian3())

  const rot = new Cesium.Matrix3()
  Cesium.Matrix3.setColumn(rot, 0, col0, rot)
  Cesium.Matrix3.setColumn(rot, 1, col1, rot)
  Cesium.Matrix3.setColumn(rot, 2, col2, rot)
  return Cesium.Quaternion.fromRotationMatrix(rot)
}

export function buildPerspectiveFrustum(
  fov: SeiFovAngles | null,
  far = DEFAULT_FAR_M,
): Cesium.PerspectiveFrustum {
  const vertical = fov?.verticalRad ?? DEFAULT_VERTICAL_FOV_RAD
  const aspect = fov?.aspectRatio ?? 4 / 3
  return new Cesium.PerspectiveFrustum({
    fov: vertical,
    aspectRatio: aspect,
    near: 0.1,
    far,
  })
}

export { DEFAULT_FAR_M }
