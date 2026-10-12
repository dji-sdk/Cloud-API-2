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

import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'

// --- Cesium Mock ---
function createCesiumMock() {
  const mockHandler = {
    setInputAction: vi.fn(),
    destroy: vi.fn(),
  }

  const mockEntities = {
    add: vi.fn((e) => e),
    remove: vi.fn(),
    removeAll: vi.fn(),
  }

  const mockDataSource = { entities: mockEntities }

  const Cesium = {
    ScreenSpaceEventHandler: vi.fn(function () { return mockHandler }),
    CustomDataSource: vi.fn(function () { return mockDataSource }),
    ScreenSpaceEventType: { LEFT_CLICK: 0, MOUSE_MOVE: 1, RIGHT_CLICK: 2 },
    Cartographic: {
      fromCartesian: (cart: { x: number; y: number }) => ({
        longitude: cart.x * 0.01,
        latitude: cart.y * 0.01,
      }),
      fromDegrees: (lng: number, lat: number) => ({ longitude: lng, latitude: lat }),
    },
    Math: {
      toDegrees: (rad: number) => rad * 100,
    },
    Cartesian3: {
      fromDegreesArray: (arr: number[]) => arr,
    },
    PolygonHierarchy: vi.fn((positions) => ({ positions })),
    Color: {
      fromCssColorString: () => ({ withAlpha: () => 'mockColor' }),
    },
    // vitest 5 要求可 new 的 mock 必须用 function 表达式（箭头函数不可构造）
    EllipsoidGeodesic: vi.fn().mockImplementation(function (this: { longitude: number; latitude: number }[] | unknown[], c1: { longitude: number; latitude: number }, c2: { longitude: number; latitude: number }) {
      return {
        surfaceDistance:
          Math.abs(c1.longitude - c2.longitude) * 111000 +
          Math.abs(c1.latitude - c2.latitude) * 111000,
      }
    }),
  }

  return { Cesium, mockHandler, mockDataSource }
}

function createFakeViewer() {
  return {
    scene: { canvas: document.createElement('canvas') },
    camera: {
      pickEllipsoid: vi.fn((pos: { x: number; y: number }) => ({ x: pos.x, y: pos.y, z: 0 })),
    },
    dataSources: {
      add: vi.fn(),
      remove: vi.fn(),
    },
  }
}

describe('useFlightAreaDraw', () => {
  let Cesium: ReturnType<typeof createCesiumMock>['Cesium']
  let mockHandler: ReturnType<typeof createCesiumMock>['mockHandler']
  let mockDataSource: ReturnType<typeof createCesiumMock>['mockDataSource']

  beforeEach(() => {
    vi.resetModules()
    const mock = createCesiumMock()
    Cesium = mock.Cesium
    mockHandler = mock.mockHandler
    mockDataSource = mock.mockDataSource
    vi.stubGlobal('Cesium', Cesium)
  })

  async function loadComposable() {
    return await import('./useFlightAreaDraw')
  }

  it('viewer null → no handler created', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const viewer = ref(null)
    const { enterDrawMode } = useFlightAreaDraw(viewer as any)
    const onDone = vi.fn()
    enterDrawMode('polygon', onDone)
    expect(Cesium.ScreenSpaceEventHandler).not.toHaveBeenCalled()
  })

  it('polygon <3 points then RIGHT_CLICK → onDone NOT called', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const fakeViewer = createFakeViewer()
    const viewer = ref(fakeViewer as any)
    const { enterDrawMode } = useFlightAreaDraw(viewer)
    const onDone = vi.fn()
    enterDrawMode('polygon', onDone)

    // Get callbacks: LEFT_CLICK=0, MOUSE_MOVE=1, RIGHT_CLICK=2
    const leftClick = mockHandler.setInputAction.mock.calls[0][0]
    const rightClick = mockHandler.setInputAction.mock.calls[2][0]

    // Add only 2 points
    leftClick({ position: { x: 114, y: 22 } })
    leftClick({ position: { x: 115, y: 23 } })
    rightClick({})

    expect(onDone).not.toHaveBeenCalled()
  })

  it('polygon ≥3 points then RIGHT_CLICK → onDone called with Polygon', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const fakeViewer = createFakeViewer()
    const viewer = ref(fakeViewer as any)
    const { enterDrawMode } = useFlightAreaDraw(viewer)
    const onDone = vi.fn()
    enterDrawMode('polygon', onDone)

    const leftClick = mockHandler.setInputAction.mock.calls[0][0]
    const rightClick = mockHandler.setInputAction.mock.calls[2][0]

    leftClick({ position: { x: 114, y: 22 } })
    leftClick({ position: { x: 115, y: 23 } })
    leftClick({ position: { x: 116, y: 22 } })
    rightClick({})

    expect(onDone).toHaveBeenCalledTimes(1)
    const geo = onDone.mock.calls[0][0]
    expect(geo.type).toBe('Polygon')
    expect(geo.coordinates[0].length).toBeGreaterThanOrEqual(4) // 3 points + closing
    expect(geo.coordinates[0][0]).toEqual(geo.coordinates[0][geo.coordinates[0].length - 1])
  })

  it('circle: 1st click sets center, 2nd click with radius≥5 → onDone', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const fakeViewer = createFakeViewer()
    const viewer = ref(fakeViewer as any)
    const { enterDrawMode } = useFlightAreaDraw(viewer)
    const onDone = vi.fn()
    enterDrawMode('circle', onDone)

    const leftClick = mockHandler.setInputAction.mock.calls[0][0]

    // 1st click: center
    leftClick({ position: { x: 114, y: 22 } })
    expect(onDone).not.toHaveBeenCalled()

    // 2nd click: edge (far enough for radius≥5)
    leftClick({ position: { x: 115, y: 22 } })
    expect(onDone).toHaveBeenCalledTimes(1)
    const geo = onDone.mock.calls[0][0]
    expect(geo.type).toBe('Circle')
    expect(geo.coordinates[0]).toBeCloseTo(114)
    expect(geo.coordinates[1]).toBeCloseTo(22)
    expect(geo.radius).toBeGreaterThanOrEqual(5)
  })

  it('circle: radius<5 resets center, does not call onDone', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const fakeViewer = createFakeViewer()
    const viewer = ref(fakeViewer as any)
    const { enterDrawMode } = useFlightAreaDraw(viewer)
    const onDone = vi.fn()
    enterDrawMode('circle', onDone)

    const leftClick = mockHandler.setInputAction.mock.calls[0][0]

    // Use very close points so distance < 5m
    // With our mock: distance = |lng1-lng2|*111000 + |lat1-lat2|*111000
    // x=100, y=100 → lng=100, lat=100; x=100, y=100 → same point → distance=0
    leftClick({ position: { x: 100, y: 100 } })
    leftClick({ position: { x: 100, y: 100 } }) // same point, radius=0

    expect(onDone).not.toHaveBeenCalled()
  })

  it('cancelDraw → handler.destroy called', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const fakeViewer = createFakeViewer()
    const viewer = ref(fakeViewer as any)
    const { enterDrawMode, cancelDraw } = useFlightAreaDraw(viewer)
    enterDrawMode('polygon', vi.fn())

    cancelDraw()
    expect(mockHandler.destroy).toHaveBeenCalled()
    expect(fakeViewer.dataSources.remove).toHaveBeenCalled()
  })

  it('Escape key cancels draw', async () => {
    const { useFlightAreaDraw } = await loadComposable()
    const fakeViewer = createFakeViewer()
    const viewer = ref(fakeViewer as any)
    const { enterDrawMode } = useFlightAreaDraw(viewer)
    enterDrawMode('polygon', vi.fn())

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    expect(mockHandler.destroy).toHaveBeenCalled()
  })
})
