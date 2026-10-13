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
import type { ModelDetail } from '@/types/terra'

/** Cesium 3D 模型查看器 composable */
export function useModelViewer() {
  const viewer = ref<Cesium.Viewer | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  let currentTileset: Cesium.Cesium3DTileset | null = null
  let loadGeneration = 0

  function initViewer(container: HTMLElement): void {
    if (viewer.value) return

    if (typeof Cesium === 'undefined') {
      console.error('[useModelViewer] Cesium 全局对象不存在，检查 index.html 中的 script 是否加载成功')
      error.value = 'Cesium 库未加载，请检查网络连接'
      return
    }

    try {
      viewer.value = new Cesium.Viewer(container, {
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
      console.error('[useModelViewer] Viewer 创建失败', err)
      error.value = 'Cesium Viewer 初始化失败: ' + (err instanceof Error ? err.message : String(err))
      return
    }

    viewer.value.scene.backgroundColor = Cesium.Color.BLACK
    viewer.value.scene.globe.baseColor = Cesium.Color.fromCssColorString('#1a1a2e')

    applyBaseImagery(viewer.value).catch((err) => {
      console.warn('[useModelViewer] 底图加载失败', err)
    })
  }

  async function loadModel(detail: ModelDetail): Promise<void> {
    if (!viewer.value) {
      if (!error.value) {
        error.value = 'Viewer 未初始化'
      }
      return
    }

    const gen = ++loadGeneration
    loading.value = true
    error.value = null

    removeTileset()

    try {
      const tileset = await Cesium.Cesium3DTileset.fromUrl(detail.url)

      if (gen !== loadGeneration) return

      if (!viewer.value || viewer.value.isDestroyed()) return

      viewer.value.scene.primitives.add(tileset)
      currentTileset = tileset

      // SDK transform 可能为行主序或列主序，Cesium.Matrix4.fromArray 默认列主序
      // TODO: 真数据验证后确认是否需要转置
      if (detail.transform && detail.transform.length === 16) {
        tileset.modelMatrix = Cesium.Matrix4.fromArray(detail.transform)
      }

      await viewer.value.flyTo(tileset, {
        duration: 1.5,
        offset: new Cesium.HeadingPitchRange(0, Cesium.Math.toRadians(-45), 0),
      })
    } catch (err) {
      if (gen !== loadGeneration) return
      if (!viewer.value || viewer.value.isDestroyed()) return

      console.error('[useModelViewer] 3D Tiles 加载失败', err)
      error.value = '模型加载失败，请检查网络连接'

      if (detail.latitude && detail.longitude) {
        viewer.value.camera.flyTo({
          destination: Cesium.Cartesian3.fromDegrees(
            detail.longitude,
            detail.latitude,
            (detail.altitude || 0) + 500,
          ),
          duration: 1.0,
        })
      }
    } finally {
      if (gen === loadGeneration) {
        loading.value = false
      }
    }
  }

  function clearError(): void {
    error.value = null
  }

  function removeTileset(): void {
    if (currentTileset && viewer.value && !viewer.value.isDestroyed()) {
      viewer.value.scene.primitives.remove(currentTileset)
      currentTileset = null
    }
  }

  function destroy(): void {
    ++loadGeneration
    removeTileset()
    if (viewer.value && !viewer.value.isDestroyed()) {
      viewer.value.destroy()
    }
    viewer.value = null
    loading.value = false
    error.value = null
  }

  return {
    viewer,
    loading,
    error,
    initViewer,
    loadModel,
    clearError,
    destroy,
  }
}
