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

import { ref, type InjectionKey } from 'vue'
import type { FlightArea } from '@/types/flightArea'
import type { DrawnGeometry } from '@/types/flightArea'
import { FLIGHT_AREA_COLORS, DISABLED_AREA_COLOR } from '@/types/flightArea'
import { applyBaseImagery } from '@/utils/cesiumImagery'
import { useFlightAreaDraw } from '@/composables/useFlightAreaDraw'

export function useFlightAreaMap(containerId: string) {
  const viewer = ref<Cesium.Viewer | null>(null)
  const ready = ref(false)
  const { enterDrawMode: drawEnter, cancelDraw: drawCancel } = useFlightAreaDraw(viewer)

  async function initMap(): Promise<void> {
    // 防止重复调用泄漏 Viewer 实例
    if (viewer.value) return

    const container = document.getElementById(containerId)
    if (!container) return

    let instance: Cesium.Viewer
    try {
      instance = new Cesium.Viewer(container, {
        animation: false,
        baseLayer: false,
        baseLayerPicker: false,
        fullscreenButton: false,
        geocoder: false,
        homeButton: false,
        infoBox: false,
        navigationHelpButton: false,
        sceneModePicker: false,
        timeline: false,
        selectionIndicator: false,
        terrainProvider: new Cesium.EllipsoidTerrainProvider(),
      })
    } catch (err) {
      console.error('[useFlightAreaMap] Cesium Viewer 创建失败（WebGL 不可用？）', err)
      return
    }

    // 移除默认 Bing Maps，改用配置的底图
    try {
      await applyBaseImagery(instance)
    } catch (err) {
      console.error('[useFlightAreaMap] 底图加载失败，地图将使用默认背景', err)
    }

    instance.camera.setView({
      destination: Cesium.Cartesian3.fromDegrees(114.06, 22.54, 50000),
    })

    viewer.value = instance
    ready.value = true
  }

  /** 计算区域填充/描边颜色（disable 取灰） */
  function areaColor(area: FlightArea): string {
    return area.status === 'disable'
      ? DISABLED_AREA_COLOR
      : (FLIGHT_AREA_COLORS[area.type] ?? '#FFFFFF')
  }

  function renderAreas(areas: FlightArea[]): void {
    const v = viewer.value
    if (!v || !ready.value) return

    v.entities.removeAll()

    // 显式只取 [lng, lat] 两个分量，避免三维 GeoJSON 高程分量污染 fromDegreesArray 的解析
    const toFlat = (ring: number[][]): number[] => ring.map(c => [c[0], c[1]]).flat()

    for (const area of areas) {
      const geom = area.content?.geometry
      if (!geom) continue

      const colorHex = areaColor(area)
      const fillColor = Cesium.Color.fromCssColorString(colorHex).withAlpha(0.3)
      const outlineColor = Cesium.Color.fromCssColorString(colorHex)

      if (geom.type === 'Circle') {
        const coords = geom.coordinates as number[]
        const radius = (geom as { radius?: number }).radius ?? 0
        if (typeof coords[0] !== 'number' || radius <= 0) continue
        v.entities.add({
          id: area.id,
          position: Cesium.Cartesian3.fromDegrees(coords[0], coords[1]),
          ellipse: {
            semiMajorAxis: radius,
            semiMinorAxis: radius,
            material: fillColor,
            outline: true,
            outlineColor,
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
            classificationType: Cesium.ClassificationType.TERRAIN,
          },
        })
        continue
      }

      if (geom.type !== 'Polygon') continue

      const rings = geom.coordinates as number[][][]
      if (!Array.isArray(rings?.[0]?.[0])) continue

      const outerPositions = Cesium.Cartesian3.fromDegreesArray(toFlat(rings[0]))
      // 支持 GeoJSON Polygon 内孔（holes），如禁飞区内的豁免子区域
      const holes = rings.slice(1).map(
        ring => new Cesium.PolygonHierarchy(Cesium.Cartesian3.fromDegreesArray(toFlat(ring))),
      )

      v.entities.add({
        id: area.id,
        polygon: {
          hierarchy: new Cesium.PolygonHierarchy(outerPositions, holes),
          material: fillColor,
          outline: true,
          outlineColor,
          outlineWidth: 2,
          heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
          classificationType: Cesium.ClassificationType.TERRAIN,
        },
      })
    }
  }

  /** 进入绘制模式，委托给 useFlightAreaDraw */
  function enterDrawMode(shape: 'polygon' | 'circle', onDone: (g: DrawnGeometry) => void): void {
    drawEnter(shape, onDone)
  }

  /** 取消绘制 */
  function cancelDraw(): void {
    drawCancel()
  }

  function flyTo(lng: number, lat: number, height = 10000): void {
    viewer.value?.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(lng, lat, height),
      duration: 1.5,
    })
  }

  function destroy(): void {
    if (viewer.value && !viewer.value.isDestroyed()) {
      viewer.value.destroy()
    }
    viewer.value = null
    ready.value = false
  }

  return { viewer, ready, initMap, renderAreas, flyTo, destroy, enterDrawMode, cancelDraw }
}

/** useFlightAreaMap 返回值类型，供 provide/inject 共享单一 Viewer 实例 */
export type FlightAreaMapApi = ReturnType<typeof useFlightAreaMap>

/** provide/inject key：FlightAreaView 创建实例并 provide，FlightAreaMap 注入使用 */
export const FLIGHT_AREA_MAP_KEY: InjectionKey<FlightAreaMapApi> = Symbol('FlightAreaMap')
