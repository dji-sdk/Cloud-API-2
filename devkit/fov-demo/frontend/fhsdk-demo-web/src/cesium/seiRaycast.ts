// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * SEI 光轴与地面/地形求交（画面中心 = 光轴，NED rotateZYX）。
 * 更新频率跟随直播 SEI，通常快于 DRC OSD 激光测距。
 */
import * as Cesium from 'cesium'
import { nedToEcefDirection, opticalAxisNed } from '@/utils/seiNed'

/** 光轴未命中地面时，射线可视长度（米） */
const OPTICAL_AXIS_FALLBACK_LENGTH_M = 800

export interface OpticalAxisHit {
  point: Cesium.Cartesian3
  lat: number
  lng: number
  height: number
}

/** 光轴单位方向（ECEF） */
export function opticalAxisDirection(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): Cesium.Cartesian3 {
  return nedToEcefDirection(lon, lat, height, opticalAxisNed(yawDeg, pitchDeg, rollDeg))
}

/** 光轴与椭球交点（无地形 pick，供射线实时刷新） */
export function opticalAxisEllipsoidEnd(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): Cesium.Cartesian3 {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const direction = opticalAxisDirection(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const ray = new Cesium.Ray(origin, direction)
  const interval = Cesium.IntersectionTests.rayEllipsoid(ray, Cesium.Ellipsoid.WGS84)
  if (interval && interval.start >= 0) {
    return Cesium.Ray.getPoint(ray, interval.start)
  }
  return Cesium.Cartesian3.add(
    origin,
    Cesium.Cartesian3.multiplyByScalar(direction, OPTICAL_AXIS_FALLBACK_LENGTH_M, new Cesium.Cartesian3()),
    new Cesium.Cartesian3(),
  )
}

/** 光轴椭球落点（实时，无 terrain pick） */
export function opticalAxisEllipsoidHit(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): OpticalAxisHit {
  const end = opticalAxisEllipsoidEnd(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const carto = Cesium.Cartographic.fromCartesian(end)
  return {
    point: end,
    lat: Cesium.Math.toDegrees(carto.latitude),
    lng: Cesium.Math.toDegrees(carto.longitude),
    height: carto.height,
  }
}

/** 射线与地形/椭球求交（取射线正方向上的首个交点） */
export function pickRayGround(viewer: Cesium.Viewer, ray: Cesium.Ray): Cesium.Cartesian3 | null {
  const hit = viewer.scene.globe.pick(ray, viewer.scene)
  if (hit) return hit

  const interval = Cesium.IntersectionTests.rayEllipsoid(ray, viewer.scene.globe.ellipsoid)
  if (!interval || interval.start < 0) return null
  return Cesium.Ray.getPoint(ray, interval.start)
}

/** 地形高度；无 Ion 地形时 getHeight 为 undefined，回退椭球面高度 */
export function terrainSurfaceHeight(viewer: Cesium.Viewer, carto: Cesium.Cartographic): number {
  const h = viewer.scene.globe.getHeight(
    new Cesium.Cartographic(carto.longitude, carto.latitude, 0),
  )
  if (h !== undefined) return h
  return carto.height
}

/**
 * 从 SEI 相机位置沿光轴求地面交点（含 Ion 地形 pick，供落点标记）。
 */
export function raycastOpticalAxisGround(
  viewer: Cesium.Viewer,
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): OpticalAxisHit | null {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const direction = opticalAxisDirection(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const ray = new Cesium.Ray(origin, direction)

  const hit = pickRayGround(viewer, ray)
  if (!hit) return null

  const carto = Cesium.Cartographic.fromCartesian(hit)
  return {
    point: hit,
    lat: Cesium.Math.toDegrees(carto.latitude),
    lng: Cesium.Math.toDegrees(carto.longitude),
    height: carto.height,
  }
}

/** 光轴线段：射线端点用椭球求交（快），落点可另做地形 pick */
export function opticalAxisSegment(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): { origin: Cesium.Cartesian3; end: Cesium.Cartesian3 } {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const end = opticalAxisEllipsoidEnd(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  return { origin, end }
}
