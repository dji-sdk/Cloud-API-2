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

import { ref } from 'vue'
import { applyBaseImagery } from '@/utils/cesiumImagery'

export function useReconstructionMap(containerId: string) {
  const viewer = ref<Cesium.Viewer | null>(null)
  const ready = ref(false)
  const wkt = ref('')
  const drawingActive = ref(false)

  let handler: Cesium.ScreenSpaceEventHandler | null = null
  let polygonPositions: Cesium.Cartesian3[] = []
  let polygonEntity: Cesium.Entity | null = null

  async function initMap(): Promise<void> {
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
      console.error('[useReconstructionMap] Cesium Viewer 创建失败', err)
      return
    }

    try {
      await applyBaseImagery(instance)
    } catch (err) {
      console.error('[useReconstructionMap] 底图加载失败', err)
    }

    instance.camera.setView({
      destination: Cesium.Cartesian3.fromDegrees(114.06, 22.54, 50000),
    })

    viewer.value = instance
    ready.value = true
  }

  function startDrawPolygon(): void {
    if (!viewer.value || drawingActive.value) return
    drawingActive.value = true
    polygonPositions = []
    clearPolygon()

    handler = new Cesium.ScreenSpaceEventHandler(viewer.value.scene.canvas)

    handler.setInputAction((event: Cesium.ScreenSpaceEventHandler.PositionedEvent) => {
      const position = viewer.value!.scene.pickPosition(event.position)
      if (!position) return
      polygonPositions.push(position)
      updatePolygonPreview()
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    handler.setInputAction(() => {
      if (polygonPositions.length >= 3) {
        finishDraw()
      }
    }, Cesium.ScreenSpaceEventType.RIGHT_CLICK)
  }

  function updatePolygonPreview(): void {
    if (!viewer.value) return

    if (polygonEntity) {
      viewer.value.entities.remove(polygonEntity)
    }

    if (polygonPositions.length < 2) return

    polygonEntity = viewer.value.entities.add({
      polygon: {
        hierarchy: new Cesium.PolygonHierarchy(polygonPositions),
        material: Cesium.Color.fromCssColorString('#06b6d4').withAlpha(0.3),
        outline: true,
        outlineColor: Cesium.Color.fromCssColorString('#06b6d4'),
        outlineWidth: 2,
      },
    })
  }

  function finishDraw(): void {
    drawingActive.value = false
    if (handler) {
      handler.destroy()
      handler = null
    }

    if (polygonPositions.length >= 3) {
      const coords = polygonPositions.map((pos) => {
        const carto = Cesium.Cartographic.fromCartesian(pos)
        const lng = Cesium.Math.toDegrees(carto.longitude)
        const lat = Cesium.Math.toDegrees(carto.latitude)
        return `${lng} ${lat}`
      })
      const first = coords[0]
      coords.push(first)
      wkt.value = `POLYGON((${coords.join(', ')}))`
    }
  }

  function clearPolygon(): void {
    if (viewer.value && polygonEntity) {
      viewer.value.entities.remove(polygonEntity)
      polygonEntity = null
    }
    polygonPositions = []
    wkt.value = ''
  }

  function cancelDraw(): void {
    drawingActive.value = false
    if (handler) {
      handler.destroy()
      handler = null
    }
  }

  function destroy(): void {
    cancelDraw()
    clearPolygon()
    if (viewer.value && !viewer.value.isDestroyed()) {
      viewer.value.destroy()
    }
    viewer.value = null
    ready.value = false
  }

  return {
    viewer,
    ready,
    wkt,
    drawingActive,
    initMap,
    startDrawPolygon,
    clearPolygon,
    cancelDraw,
    destroy,
  }
}
