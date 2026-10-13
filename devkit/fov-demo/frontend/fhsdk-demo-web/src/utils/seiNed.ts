// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * SEI 相机 NED 姿态 → ECEF 方向。
 * SEI 欧拉角描述的是相机本体系（光轴 = +Z）→ NED；与逆投影里「相机系→云台系」后的 +X 不同。
 */
import * as Cesium from 'cesium'

export type NedVec = [number, number, number]

const DEG2RAD = Math.PI / 180

/** R = Rz(yaw)·Ry(pitch)·Rx(roll)，角度为度，向量在 NED 下 */
export function rotateZYX(v: NedVec, yawDeg: number, pitchDeg: number, rollDeg: number): NedVec {
  const cy = Math.cos(yawDeg * DEG2RAD)
  const sy = Math.sin(yawDeg * DEG2RAD)
  const cp = Math.cos(pitchDeg * DEG2RAD)
  const sp = Math.sin(pitchDeg * DEG2RAD)
  const cr = Math.cos(rollDeg * DEG2RAD)
  const sr = Math.sin(rollDeg * DEG2RAD)
  const x1: NedVec = [v[0], cr * v[1] - sr * v[2], sr * v[1] + cr * v[2]]
  const x2: NedVec = [cp * x1[0] + sp * x1[2], x1[1], -sp * x1[0] + cp * x1[2]]
  return [cy * x2[0] - sy * x2[1], sy * x2[0] + cy * x2[1], x2[2]]
}

/** 画面中心光轴：相机本体系 +Z → NED */
export function opticalAxisNed(yawDeg: number, pitchDeg: number, rollDeg: number): NedVec {
  return rotateZYX([0, 0, 1], yawDeg, pitchDeg, rollDeg)
}

/** NED [北,东,地] → 该经纬度处 ECEF 单位方向 */
export function nedToEcefDirection(lon: number, lat: number, height: number, ned: NedVec): Cesium.Cartesian3 {
  const enu = new Cesium.Cartesian3(ned[1], ned[0], -ned[2])
  const rot = Cesium.Matrix4.getRotation(
    Cesium.Transforms.eastNorthUpToFixedFrame(Cesium.Cartesian3.fromDegrees(lon, lat, height)),
    new Cesium.Matrix3(),
  )
  const dir = Cesium.Matrix3.multiplyByVector(rot, enu, new Cesium.Cartesian3())
  return Cesium.Cartesian3.normalize(dir, dir)
}

export function nedAxisEndpoints(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
  lengths: { x: number; y: number; z: number },
): {
  origin: Cesium.Cartesian3
  endX: Cesium.Cartesian3
  endY: Cesium.Cartesian3
  endZ: Cesium.Cartesian3
} {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  // 红=光轴(+Z)，绿=Y，蓝=X（相机本体系 basis）
  const bodyAxes: NedVec[] = [[0, 0, 1], [0, 1, 0], [1, 0, 0]]
  const lens = [lengths.x, lengths.y, lengths.z]
  const ends = bodyAxes.map((axis, i) => {
    const ned = rotateZYX(axis, yawDeg, pitchDeg, rollDeg)
    const dir = nedToEcefDirection(lon, lat, height, ned)
    return Cesium.Cartesian3.add(
      origin,
      Cesium.Cartesian3.multiplyByScalar(dir, lens[i], new Cesium.Cartesian3()),
      new Cesium.Cartesian3(),
    )
  })
  return { origin, endX: ends[0]!, endY: ends[1]!, endZ: ends[2]! }
}
