// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * 视锥远平面四角 → 射线与地面/地形求交 → 贴地四边形顶点。
 * 远平面角点与 FrustumGeometry 共用 Cesium 内部 _computeNearFarPlanes，保证与视锥一致。
 */
import * as Cesium from 'cesium'
import {
  buildPerspectiveFrustum,
  DEFAULT_FAR_M,
  nedCameraFrustumOrientation,
  seiFovFromSensor,
  type SeiFovAngles,
} from '@/cesium/seiFrustum'
import { pickRayGround } from '@/cesium/seiRaycast'

const FRUSTUM_TYPE_PERSPECTIVE = 0

/** Cesium NDC 远平面顺序 BL,BR,TR,TL → 纹理顺序 TL,TR,BR,BL */
function reorderFarCorners(corners: Cesium.Cartesian3[]): Cesium.Cartesian3[] {
  return [corners[3]!, corners[2]!, corners[1]!, corners[0]!]
}

/** 与 DynamicFrustum 相同参数，读取 FrustumGeometry 远平面四角（世界坐标） */
export function frustumFarPlaneCornersCesium(
  origin: Cesium.Cartesian3,
  orientation: Cesium.Quaternion,
  fov: SeiFovAngles | null,
  far = DEFAULT_FAR_M,
): Cesium.Cartesian3[] {
  const frustum = buildPerspectiveFrustum(fov, far)
  const positions = new Float64Array(24)
  // Cesium 内部 API，与 DebugCameraPrimitive / FrustumOutlineGeometry 一致
  ;(
    Cesium.FrustumGeometry as unknown as {
      _computeNearFarPlanes: (
        origin: Cesium.Cartesian3,
        orientation: Cesium.Quaternion,
        frustumType: number,
        frustum: Cesium.PerspectiveFrustum,
        positions: Float64Array,
      ) => void
    }
  )._computeNearFarPlanes(
    origin,
    orientation,
    FRUSTUM_TYPE_PERSPECTIVE,
    frustum,
    positions,
  )

  const raw: Cesium.Cartesian3[] = []
  for (let j = 0; j < 4; j++) {
    const i = 12 + j * 3
    raw.push(new Cesium.Cartesian3(positions[i]!, positions[i + 1]!, positions[i + 2]!))
  }
  return reorderFarCorners(raw)
}

function rayFromOriginThrough(
  origin: Cesium.Cartesian3,
  target: Cesium.Cartesian3,
): Cesium.Ray | null {
  const dir = Cesium.Cartesian3.subtract(target, origin, new Cesium.Cartesian3())
  if (Cesium.Cartesian3.magnitudeSquared(dir) < 1e-6) return null
  return new Cesium.Ray(origin, Cesium.Cartesian3.normalize(dir, dir))
}

function raycastCorner(
  viewer: Cesium.Viewer,
  origin: Cesium.Cartesian3,
  farCorner: Cesium.Cartesian3,
): Cesium.Cartesian3 | null {
  const ray = rayFromOriginThrough(origin, farCorner)
  if (!ray) return null
  return pickRayGround(viewer, ray)
}

/** 四角射线与地形/椭球求交；顺序 TL,TR,BR,BL */
export function computeFrustumGroundFootprint(
  viewer: Cesium.Viewer,
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
  fovHorizontalUm?: number,
  fovVerticalUm?: number,
  focalLengthUm?: number,
  far = DEFAULT_FAR_M,
): Cesium.Cartesian3[] | null {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const orientation = nedCameraFrustumOrientation(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const fov = seiFovFromSensor(fovHorizontalUm, fovVerticalUm, focalLengthUm)
  const farCorners = frustumFarPlaneCornersCesium(origin, orientation, fov, far)

  const hits: Cesium.Cartesian3[] = []
  for (const corner of farCorners) {
    const hit = raycastCorner(viewer, origin, corner)
    if (!hit) return null
    hits.push(hit)
  }
  return hits.length === 4 ? hits : null
}

export type FrustumFootprintState = {
  origin: Cesium.Cartesian3
  orientation: Cesium.Quaternion
  fov: SeiFovAngles | null
  far?: number
}

/** 已有视锥状态时复用 orientation，避免重复计算 */
export function computeFrustumGroundFootprintFromState(
  viewer: Cesium.Viewer,
  state: FrustumFootprintState,
): Cesium.Cartesian3[] | null {
  const farCorners = frustumFarPlaneCornersCesium(
    state.origin,
    state.orientation,
    state.fov,
    state.far ?? DEFAULT_FAR_M,
  )
  const hits: Cesium.Cartesian3[] = []
  for (const corner of farCorners) {
    const hit = raycastCorner(viewer, state.origin, corner)
    if (!hit) return null
    hits.push(hit)
  }
  return hits.length === 4 ? hits : null
}
