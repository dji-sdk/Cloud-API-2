// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * SEI 相机三轴、光轴、视锥、测距连线 — Primitive 图层。
 */
import * as Cesium from 'cesium'
import { DynamicFrustum, DynamicGroundClosedPolyline, DynamicPolyline, type FrustumState } from '@/cesium/dynamicPrimitives'
import { GroundVideoFootprint } from '@/cesium/groundVideoFootprint'
import {
  DEFAULT_FAR_M,
  nedCameraFrustumOrientation,
  seiFovFromSensor,
} from '@/cesium/seiFrustum'
import { computeFrustumGroundFootprintFromState, type FrustumFootprintState } from '@/cesium/seiFrustumFootprint'
import { opticalAxisEllipsoidHit, opticalAxisSegment } from '@/cesium/seiRaycast'
import { nedAxisEndpoints } from '@/utils/seiNed'

const CAMERA_AXIS_LENGTH = { x: 14, y: 10, z: 10 }

export class SeiMapLayer {
  private readonly collection: Cesium.PrimitiveCollection
  private readonly axisX: DynamicPolyline
  private readonly axisY: DynamicPolyline
  private readonly axisZ: DynamicPolyline
  private readonly seiRay: DynamicPolyline
  private readonly fastLine: DynamicPolyline
  private readonly seiLine: DynamicPolyline
  private readonly frustum: DynamicFrustum
  private readonly footprintOutline: DynamicGroundClosedPolyline
  private readonly groundVideo: GroundVideoFootprint
  private liveVideo: HTMLVideoElement | null = null
  private lastFootprint: Cesium.Cartesian3[] | null = null
  private videoProjectionEnabled = false

  constructor(private readonly viewer: Cesium.Viewer) {
    this.collection = viewer.scene.primitives.add(new Cesium.PrimitiveCollection())
    this.axisX = new DynamicPolyline(this.collection, Cesium.Color.RED, 6)
    this.axisY = new DynamicPolyline(this.collection, Cesium.Color.LIME, 4)
    this.axisZ = new DynamicPolyline(this.collection, Cesium.Color.DODGERBLUE, 4)
    this.seiRay = new DynamicPolyline(this.collection, Cesium.Color.CYAN.withAlpha(0.75), 3)
    this.fastLine = new DynamicPolyline(this.collection, Cesium.Color.LIME.withAlpha(0.85), 3)
    this.seiLine = new DynamicPolyline(this.collection, Cesium.Color.ORANGE.withAlpha(0.85), 3)
    this.frustum = new DynamicFrustum(
      this.collection,
      Cesium.Color.CYAN.withAlpha(0.9),
      Cesium.Color.CYAN.withAlpha(0.12),
    )
    this.footprintOutline = new DynamicGroundClosedPolyline(
      this.viewer,
      Cesium.Color.YELLOW.withAlpha(0.95),
      2,
    )
    this.groundVideo = new GroundVideoFootprint(this.collection, this.viewer)
  }

  setVideoProjectionEnabled(enabled: boolean) {
    this.videoProjectionEnabled = enabled
    if (!enabled) {
      this.groundVideo.hide(this.viewer)
      return
    }
    if (this.lastFootprint && this.liveVideo) {
      this.groundVideo.update(this.viewer, this.lastFootprint, this.liveVideo)
    }
  }

  setLiveVideo(video: HTMLVideoElement | null) {
    this.liveVideo = video
    if (!video || !this.videoProjectionEnabled) {
      this.groundVideo.hide(this.viewer)
      return
    }
    this.groundVideo.bindVideo(video)
    if (this.lastFootprint) {
      this.groundVideo.update(this.viewer, this.lastFootprint, video)
    }
  }

  updateCameraAxes(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
  ) {
    const { origin, endX, endY, endZ } = nedAxisEndpoints(
      lon, lat, height, yawDeg, pitchDeg, rollDeg, CAMERA_AXIS_LENGTH,
    )
    this.axisX.setEndpoints(origin, endX)
    this.axisY.setEndpoints(origin, endY)
    this.axisZ.setEndpoints(origin, endZ)
  }

  hideCameraAxes() {
    this.axisX.hide()
    this.axisY.hide()
    this.axisZ.hide()
  }

  updateSeiOpticalAxis(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
  ) {
    const { origin, end } = opticalAxisSegment(lon, lat, height, yawDeg, pitchDeg, rollDeg)
    this.seiRay.setEndpoints(origin, end)
  }

  hideSeiOpticalAxis() {
    this.seiRay.hide()
    this.seiLine.hide()
  }

  updateMeasureLine(
    id: 'fast' | 'sei',
    from: Cesium.Cartesian3,
    to: Cesium.Cartesian3,
  ) {
    const line = id === 'fast' ? this.fastLine : this.seiLine
    line.setEndpoints(from, to)
  }

  hideMeasureLine(id: 'fast' | 'sei') {
    if (id === 'fast') this.fastLine.hide()
    else this.seiLine.hide()
  }

  updateFrustum(
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
  ) {
    const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
    const orientation = nedCameraFrustumOrientation(lon, lat, height, yawDeg, pitchDeg, rollDeg)
    const fov = seiFovFromSensor(fovHorizontalUm, fovVerticalUm, focalLengthUm)
    const state: FrustumState = { origin, orientation, fov, far }
    this.frustum.update(state)
    this.updateFootprint(state)
  }

  private updateFootprint(state: FrustumFootprintState) {
    let footprint = computeFrustumGroundFootprintFromState(this.viewer, state)

    if (footprint) {
      this.lastFootprint = footprint.map((p) => Cesium.Cartesian3.clone(p))
    } else if (this.lastFootprint) {
      footprint = this.lastFootprint
    } else {
      this.footprintOutline.hide()
      this.hideGroundFootprint()
      return
    }

    this.footprintOutline.update(footprint)

    if (!this.videoProjectionEnabled) {
      this.groundVideo.hide(this.viewer)
      return
    }

    this.groundVideo.update(this.viewer, footprint, this.liveVideo)
  }

  hideFrustum() {
    this.frustum.hide()
    this.hideGroundFootprint()
  }

  hideGroundFootprint() {
    this.lastFootprint = null
    this.footprintOutline.hide()
    this.groundVideo.hide(this.viewer)
  }

  opticalAxisHit(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
  ) {
    return opticalAxisEllipsoidHit(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  }

  hideAllSei() {
    this.hideCameraAxes()
    this.hideSeiOpticalAxis()
    this.hideFrustum()
  }

  reset() {
    this.hideAllSei()
    this.fastLine.hide()
  }

  destroy() {
    this.axisX.destroy()
    this.axisY.destroy()
    this.axisZ.destroy()
    this.seiRay.destroy()
    this.fastLine.destroy()
    this.seiLine.destroy()
    this.frustum.destroy()
    this.footprintOutline.destroy()
    this.groundVideo.destroy(this.viewer)
    if (!this.collection.isDestroyed()) {
      this.viewer.scene.primitives.remove(this.collection)
    }
  }
}
