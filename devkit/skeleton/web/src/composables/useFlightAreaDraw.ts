/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import type { Ref } from 'vue'
import type { DrawnGeometry } from '@/types/flightArea'

export type DrawShape = 'polygon' | 'circle'

const MIN_POLYGON_POINTS = 3
const MIN_CIRCLE_RADIUS = 5

export function useFlightAreaDraw(viewer: Ref<Cesium.Viewer | null>) {
  let handler: Cesium.ScreenSpaceEventHandler | null = null
  let dataSource: Cesium.CustomDataSource | null = null
  let escListener: ((e: KeyboardEvent) => void) | null = null

  function cleanupDraw(): void {
    if (handler) { handler.destroy(); handler = null }
    const v = viewer.value
    if (dataSource && v) { v.dataSources.remove(dataSource, true); dataSource = null }
    if (escListener) { document.removeEventListener('keydown', escListener); escListener = null }
  }

  function cancelDraw(): void { cleanupDraw() }

  function enterDrawMode(shape: DrawShape, onDone: (g: DrawnGeometry) => void): void {
    const v = viewer.value
    if (!v) { console.warn('[useFlightAreaDraw] viewer 未就绪'); return }
    cleanupDraw()
    dataSource = new Cesium.CustomDataSource('fh-draw')
    v.dataSources.add(dataSource)
    handler = new Cesium.ScreenSpaceEventHandler(v.scene.canvas)
    if (shape === 'polygon') drawPolygon(handler, dataSource, onDone)
    else drawCircle(handler, dataSource, onDone)
    escListener = (e: KeyboardEvent) => { if (e.key === 'Escape') cancelDraw() }
    document.addEventListener('keydown', escListener)
  }

  function pickLngLat(v: Cesium.Viewer, windowPosition: Cesium.Cartesian2): [number, number] | null {
    const cartesian = v.camera.pickEllipsoid(windowPosition)
    if (!cartesian) return null
    const carto = Cesium.Cartographic.fromCartesian(cartesian)
    return [Cesium.Math.toDegrees(carto.longitude), Cesium.Math.toDegrees(carto.latitude)]
  }

  function groundDistance(lngLat1: [number, number], lngLat2: [number, number]): number {
    const c1 = Cesium.Cartographic.fromDegrees(lngLat1[0], lngLat1[1])
    const c2 = Cesium.Cartographic.fromDegrees(lngLat2[0], lngLat2[1])
    const geodesic = new Cesium.EllipsoidGeodesic(c1, c2)
    return geodesic.surfaceDistance
  }

  function drawPolygon(
    h: Cesium.ScreenSpaceEventHandler,
    ds: Cesium.CustomDataSource,
    onDone: (g: DrawnGeometry) => void,
  ): void {
    const v = viewer.value!
    const points: [number, number][] = []
    let previewEntity: Cesium.Entity | null = null

    h.setInputAction((evt: { position: Cesium.Cartesian2 }) => {
      const lngLat = pickLngLat(v, evt.position)
      if (!lngLat) return
      points.push(lngLat)
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    h.setInputAction((evt: { endPosition: Cesium.Cartesian2 }) => {
      if (points.length === 0) return
      const lngLat = pickLngLat(v, evt.endPosition)
      if (!lngLat) return
      if (previewEntity) ds.entities.remove(previewEntity)
      const ring = [...points, lngLat, points[0]]
      const flat = ring.map(c => [c[0], c[1]]).flat()
      previewEntity = ds.entities.add({
        polygon: {
          hierarchy: new Cesium.PolygonHierarchy(Cesium.Cartesian3.fromDegreesArray(flat)),
          material: Cesium.Color.fromCssColorString('#58a6ff').withAlpha(0.3),
          outline: true,
          outlineColor: Cesium.Color.fromCssColorString('#58a6ff'),
        },
      })
    }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)

    h.setInputAction(() => {
      if (points.length < MIN_POLYGON_POINTS) {
        console.warn(
          `[useFlightAreaDraw] 多边形至少需要 ${MIN_POLYGON_POINTS} 个点，当前 ${points.length}`,
        )
        return
      }
      const isClosed = points[0][0] === points[points.length - 1][0]
        && points[0][1] === points[points.length - 1][1]
      const ring = isClosed ? points : [...points, points[0]]
      const coordinates = [ring.map(p => [p[0], p[1]])]
      cleanupDraw()
      onDone({ type: 'Polygon', coordinates })
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
  }

  function drawCircle(
    h: Cesium.ScreenSpaceEventHandler,
    ds: Cesium.CustomDataSource,
    onDone: (g: DrawnGeometry) => void,
  ): void {
    const v = viewer.value!
    let center: [number, number] | null = null
    let previewEntity: Cesium.Entity | null = null

    h.setInputAction((evt: { position: Cesium.Cartesian2 }) => {
      const lngLat = pickLngLat(v, evt.position)
      if (!lngLat) return
      if (!center) { center = lngLat; return }
      const radius = groundDistance(center, lngLat)
      if (radius < MIN_CIRCLE_RADIUS) {
        console.warn(
          `[useFlightAreaDraw] 圆形半径过小（${radius.toFixed(1)}m < ${MIN_CIRCLE_RADIUS}m），重新选择圆心`,
        )
        center = null
        if (previewEntity) ds.entities.remove(previewEntity)
        previewEntity = null
        return
      }
      cleanupDraw()
      onDone({ type: 'Circle', coordinates: [center[0], center[1]], radius })
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    h.setInputAction((evt: { endPosition: Cesium.Cartesian2 }) => {
      if (!center) return
      const lngLat = pickLngLat(v, evt.endPosition)
      if (!lngLat) return
      const radius = groundDistance(center, lngLat)
      if (previewEntity) ds.entities.remove(previewEntity)
      previewEntity = ds.entities.add({
        position: Cesium.Cartesian3.fromDegrees(center[0], center[1]),
        ellipse: {
          semiMajorAxis: radius,
          semiMinorAxis: radius,
          material: Cesium.Color.fromCssColorString('#58a6ff').withAlpha(0.3),
          outline: true,
          outlineColor: Cesium.Color.fromCssColorString('#58a6ff'),
        },
      })
    }, Cesium.ScreenSpaceEventType.MOUSE_MOVE)
  }

  return { enterDrawMode, cancelDraw }
}
