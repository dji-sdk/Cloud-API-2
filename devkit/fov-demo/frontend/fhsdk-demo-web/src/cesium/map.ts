// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

/**
 * Cesium 地图层：机场/飞机/相机/激光测距等三维实体。
 * 高频折线与视锥走 Primitive（seiMapLayer），点与模型仍用 Entity。
 */
import * as Cesium from 'cesium'
import { SeiMapLayer } from '@/cesium/seiMapLayer'

const ionToken = import.meta.env.VITE_CESIUM_ION_TOKEN as string | undefined

const AIRCRAFT_MODEL_URI = `${import.meta.env.BASE_URL}models/Cesium_Air.glb`
const AIRCRAFT_MODEL_HEADING_OFFSET = Cesium.Math.toRadians(90 + 180)

let seiLayer: SeiMapLayer | undefined

function getSeiLayer(viewer: Cesium.Viewer): SeiMapLayer {
  if (!seiLayer) {
    seiLayer = new SeiMapLayer(viewer)
  }
  return seiLayer
}

function aircraftOrientation(
  position: Cesium.Cartesian3,
  heading: number,
  pitch: number,
  roll: number,
) {
  return Cesium.Transforms.headingPitchRollQuaternion(
    position,
    new Cesium.HeadingPitchRoll(
      Cesium.Math.toRadians(heading) + AIRCRAFT_MODEL_HEADING_OFFSET,
      Cesium.Math.toRadians(pitch),
      Cesium.Math.toRadians(roll),
    ),
  )
}

export function createViewer(container: HTMLElement) {
  if (ionToken) {
    Cesium.Ion.defaultAccessToken = ionToken
  }
  const viewer = new Cesium.Viewer(container, {
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    navigationHelpButton: false,
    fullscreenButton: false,
    infoBox: false,
    selectionIndicator: false,
    requestRenderMode: false,
    useDefaultRenderLoop: true,
  })
  viewer.scene.globe.depthTestAgainstTerrain = true
  return viewer
}

export function hasIonTerrainToken(): boolean {
  return Boolean(ionToken?.trim())
}

export function setMapTerrainEnabled(viewer: Cesium.Viewer, enabled: boolean): boolean {
  if (enabled) {
    if (!ionToken?.trim()) return false
    Cesium.Ion.defaultAccessToken = ionToken
    viewer.scene.setTerrain(Cesium.Terrain.fromWorldTerrain())
    viewer.scene.globe.depthTestAgainstTerrain = true
    return true
  }
  viewer.scene.setTerrain(
    new Cesium.Terrain(Promise.resolve(new Cesium.EllipsoidTerrainProvider())),
  )
  return true
}

export function flyToLonLat(
  viewer: Cesium.Viewer,
  lon: number,
  lat: number,
  height = 100,
  viewOffset = 800,
) {
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(lon, lat, height + viewOffset),
    duration: 0.8,
  })
}

function setEntityPosition(entity: Cesium.Entity, position: Cesium.Cartesian3) {
  const prop = entity.position
  if (prop instanceof Cesium.ConstantPositionProperty) {
    prop.setValue(position)
  } else {
    entity.position = new Cesium.ConstantPositionProperty(position)
  }
}

export function updateDock(
  viewer: Cesium.Viewer,
  entity: Cesium.Entity | undefined,
  lat: number,
  lon: number,
  height: number,
): Cesium.Entity {
  const position = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  if (!entity) {
    return viewer.entities.add({
      name: 'dock',
      position,
      point: { pixelSize: 11, color: Cesium.Color.CYAN, outlineColor: Cesium.Color.WHITE, outlineWidth: 1 },
    })
  }
  setEntityPosition(entity, position)
  return entity
}

export function updateAircraft(
  viewer: Cesium.Viewer,
  entity: Cesium.Entity | undefined,
  lat: number,
  lon: number,
  height: number,
  heading: number,
  pitch: number,
  roll: number,
): Cesium.Entity {
  const position = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const orientation = aircraftOrientation(position, heading, pitch, roll)

  if (entity && !entity.model) {
    viewer.entities.remove(entity)
    entity = undefined
  }

  if (!entity) {
    return viewer.entities.add({
      name: 'aircraft',
      position,
      orientation,
      model: {
        uri: AIRCRAFT_MODEL_URI,
        minimumPixelSize: 48,
        scale: 1.5,
        silhouetteColor: Cesium.Color.WHITE,
        silhouetteSize: 1,
      },
    })
  }
  setEntityPosition(entity, position)
  entity.orientation = new Cesium.ConstantProperty(orientation)
  return entity
}

export interface CameraMapEntities {
  origin: Cesium.Entity
}

export interface SeiCameraUpdate {
  lat: number
  lon: number
  height: number
  yawDeg: number
  pitchDeg: number
  rollDeg: number
  fovHorizontalUm?: number
  fovVerticalUm?: number
  focalLengthUm?: number
}

export function updateCamera(
  viewer: Cesium.Viewer,
  entities: CameraMapEntities | undefined,
  lat: number,
  lon: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
  sei?: Pick<SeiCameraUpdate, 'fovHorizontalUm' | 'fovVerticalUm' | 'focalLengthUm'>,
): CameraMapEntities {
  const layer = getSeiLayer(viewer)
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)

  layer.updateCameraAxes(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  layer.updateSeiOpticalAxis(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  layer.updateFrustum(
    lon, lat, height, yawDeg, pitchDeg, rollDeg,
    sei?.fovHorizontalUm, sei?.fovVerticalUm, sei?.focalLengthUm,
  )

  const hit = layer.opticalAxisHit(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  if (hit) {
    updateMeasurePoint(viewer, 'sei', hit.lat, hit.lng, hit.height, Cesium.Color.ORANGE, origin)
  } else {
    layer.hideMeasureLine('sei')
    removeMeasurePoint(viewer, 'sei')
  }

  if (!entities) {
    const originEntity = viewer.entities.add({
      name: 'camera-origin',
      position: origin,
      point: {
        pixelSize: 10,
        color: Cesium.Color.WHITE,
        outlineColor: Cesium.Color.MEDIUMPURPLE,
        outlineWidth: 2,
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
      },
    })
    return { origin: originEntity }
  }

  setEntityPosition(entities.origin, origin)
  return entities
}

export function removeCamera(viewer: Cesium.Viewer, entities: CameraMapEntities | undefined) {
  getSeiLayer(viewer).hideAllSei()
  if (!entities) return undefined
  viewer.entities.remove(entities.origin)
  return undefined
}

export function removeSeiOpticalAxis(viewer: Cesium.Viewer) {
  const layer = getSeiLayer(viewer)
  layer.hideSeiOpticalAxis()
  layer.hideFrustum()
  removeMeasurePoint(viewer, 'sei')
}

export function updateSeiOpticalAxis(
  viewer: Cesium.Viewer,
  lat: number,
  lon: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
) {
  const layer = getSeiLayer(viewer)
  layer.updateSeiOpticalAxis(lon, lat, height, yawDeg, pitchDeg, rollDeg)

  const hit = layer.opticalAxisHit(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  if (hit) {
    updateMeasurePoint(
      viewer,
      'sei',
      hit.lat,
      hit.lng,
      hit.height,
      Cesium.Color.ORANGE,
      Cesium.Cartesian3.fromDegrees(lon, lat, height),
    )
  } else {
    layer.hideMeasureLine('sei')
    removeMeasurePoint(viewer, 'sei')
  }
}

export function removeMeasurePoint(viewer: Cesium.Viewer, id: string) {
  const layer = getSeiLayer(viewer)
  if (id === 'fast') layer.hideMeasureLine('fast')
  if (id === 'sei') layer.hideMeasureLine('sei')
  const entity = viewer.entities.getById(id)
  if (entity) viewer.entities.remove(entity)
}

export function updateMeasurePoint(
  viewer: Cesium.Viewer,
  id: string,
  lat: number,
  lon: number,
  height: number,
  color: Cesium.Color,
  from?: Cesium.Cartesian3,
) {
  const pos = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  let entity = viewer.entities.getById(id)
  if (!entity) {
    entity = viewer.entities.add({
      id,
      position: pos,
      point: { pixelSize: 10, color },
    })
  } else {
    setEntityPosition(entity, pos)
  }

  const layer = getSeiLayer(viewer)
  if (from && (id === 'fast' || id === 'sei')) {
    layer.updateMeasureLine(id, from, pos)
  } else if (id === 'fast' || id === 'sei') {
    layer.hideMeasureLine(id)
  }
}

/** 取消选设备时清空 Primitive 图层缓存 */
export function resetMapLineCache() {
  seiLayer?.reset()
}

export function destroySeiMapLayer() {
  seiLayer?.destroy()
  seiLayer = undefined
}

/** 绑定 WHEP 视频元素，用于视锥贴地四边形纹理（需同时开启视频投影） */
export function setMapLiveVideo(viewer: Cesium.Viewer, video: HTMLVideoElement | null) {
  getSeiLayer(viewer).setLiveVideo(video)
}

/** 地图视频投影开关（默认关；开启后才有 footprint 求交与贴地网格） */
export function setMapVideoProjectionEnabled(viewer: Cesium.Viewer, enabled: boolean) {
  getSeiLayer(viewer).setVideoProjectionEnabled(enabled)
}

/** 地图更新后请求重绘 */
export function requestMapRender(viewer: Cesium.Viewer | undefined) {
  viewer?.scene.requestRender()
}
