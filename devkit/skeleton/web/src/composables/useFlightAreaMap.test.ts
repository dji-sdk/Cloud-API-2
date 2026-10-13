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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import type { FlightArea } from '@/types/flightArea'

function mockCesium() {
  const entityApi = { id: 'e1' }
  const viewer = {
    entities: { add: vi.fn(() => entityApi), removeAll: vi.fn(), values: [] },
    camera: { flyTo: vi.fn(), setView: vi.fn(), pickEllipsoid: vi.fn(() => ({ x: 1, y: 1, z: 0 })) },
    scene: { canvas: document.createElement('canvas'), globe: {} },
    dataSources: { add: vi.fn(), remove: vi.fn(() => true) },
    isDestroyed: vi.fn(() => false),
    destroy: vi.fn(),
  }
  const Cesium = {
    Viewer: vi.fn(function () { return viewer }),
    EllipsoidTerrainProvider: vi.fn(),
    Cartesian3: { fromDegrees: vi.fn(() => ({ x: 1, y: 2, z: 3 })), fromDegreesArray: vi.fn(() => [{}]) },
    Color: { fromCssColorString: vi.fn(() => ({ withAlpha: vi.fn(() => ({})) })) },
    HeightReference: { CLAMP_TO_GROUND: 1 },
    ClassificationType: { TERRAIN: 2 },
    PolygonHierarchy: vi.fn((p: unknown) => ({ positions: p })),
    CustomDataSource: vi.fn(function () { return { entities: { add: vi.fn(() => ({})), removeAll: vi.fn(), remove: vi.fn(), values: [] }, show: true } }),
    ScreenSpaceEventHandler: vi.fn(function () { return { setInputAction: vi.fn(), destroy: vi.fn() } }),
    ScreenSpaceEventType: { LEFT_CLICK: 0, MOUSE_MOVE: 1, RIGHT_CLICK: 2 },
    Cartographic: { fromCartesian: vi.fn(() => ({ longitude: 0, latitude: 0 })), fromDegrees: vi.fn(() => ({ longitude: 0, latitude: 0 })) },
    Math: { toDegrees: vi.fn((r: number) => r) },
    EllipsoidGeodesic: vi.fn(function () { return { surfaceDistance: 100 } }),
  }
  return { Cesium, viewer, entityApi }
}

describe('useFlightAreaMap renderAreas', () => {
  let stub: ReturnType<typeof mockCesium>
  beforeEach(() => {
    stub = mockCesium()
    vi.stubGlobal('Cesium', stub.Cesium)
    vi.resetModules()
  })

  it('Circle 类型 area：以 ellipse 配置调用 entities.add', async () => {
    const { useFlightAreaMap } = await import('./useFlightAreaMap')
    const div = document.createElement('div')
    div.id = 'test-map-c1'
    document.body.appendChild(div)
    const { initMap, renderAreas, ready } = useFlightAreaMap('test-map-c1')
    await initMap()
    expect(ready.value).toBe(true)

    const area: FlightArea = {
      id: 'fa_1', name: '圆形限飞', type: 'nfz', status: 'enable',
      content: {
        type: 'Feature',
        properties: { color: '#FF0000' },
        geometry: { type: 'Circle', coordinates: [114.06, 22.54], radius: 500 },
      },
      createdTime: 0, createdBy: 'system', updatedTime: 0, updatedBy: 'system',
    }
    renderAreas([area])
    const addCall = stub.viewer.entities.add.mock.calls[0][0]
    expect(addCall.ellipse).toBeDefined()
    expect(addCall.ellipse.semiMajorAxis).toBe(500)
    expect(addCall.ellipse.semiMinorAxis).toBe(500)
    document.body.removeChild(div)
  })

  it('disable 状态的 Circle：颜色取灰色', async () => {
    const { useFlightAreaMap } = await import('./useFlightAreaMap')
    const div = document.createElement('div')
    div.id = 'test-map-c2'
    document.body.appendChild(div)
    const { initMap, renderAreas } = useFlightAreaMap('test-map-c2')
    await initMap()
    const area: FlightArea = {
      id: 'fa_2', name: '禁用圆', type: 'nfz', status: 'disable',
      content: {
        type: 'Feature', properties: { color: '#FF0000' },
        geometry: { type: 'Circle', coordinates: [114, 22], radius: 100 },
      },
      createdTime: 0, createdBy: '', updatedTime: 0, updatedBy: '',
    }
    renderAreas([area])
    expect(Cesium.Color.fromCssColorString).toHaveBeenCalledWith('#606060')
    document.body.removeChild(div)
  })

  it('enterDrawMode 委托到 useFlightAreaDraw（不抛错）', async () => {
    const { useFlightAreaMap } = await import('./useFlightAreaMap')
    const div = document.createElement('div')
    div.id = 'test-map-c3'
    document.body.appendChild(div)
    const { initMap, enterDrawMode, cancelDraw } = useFlightAreaMap('test-map-c3')
    await initMap()
    expect(() => {
      enterDrawMode('polygon', () => {})
      cancelDraw()
    }).not.toThrow()
    document.body.removeChild(div)
  })
})
