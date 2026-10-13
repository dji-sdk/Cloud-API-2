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

import { describe, it, expect } from 'vitest'
import { buildFeature } from './flightArea'
import type { DrawnGeometry, FlightAreaType } from '@/types/flightArea'
import { FLIGHT_AREA_COLORS } from '@/types/flightArea'

describe('buildFeature', () => {
  it('多边形：写入正确颜色且 coordinates 闭合', () => {
    const type: FlightAreaType = 'dfence'
    const geometry: DrawnGeometry = {
      type: 'Polygon',
      coordinates: [[[114.0, 22.5], [114.1, 22.5], [114.1, 22.6], [114.0, 22.6], [114.0, 22.5]]],
    }
    const feature = buildFeature(type, geometry)
    expect(feature.type).toBe('Feature')
    expect(feature.properties.color).toBe(FLIGHT_AREA_COLORS.dfence)
    expect(feature.geometry.type).toBe('Polygon')
    const ring = (feature.geometry.coordinates as number[][][])[0]
    expect(ring[0]).toEqual(ring[ring.length - 1])
  })

  it('圆形：geometry.type===Circle 且 radius 透传', () => {
    const geometry: DrawnGeometry = { type: 'Circle', coordinates: [114.06, 22.54], radius: 500 }
    const feature = buildFeature('nfz', geometry)
    expect(feature.geometry.type).toBe('Circle')
    expect(feature.geometry.coordinates).toEqual([114.06, 22.54])
    expect((feature.geometry as { radius: number }).radius).toBe(500)
    expect(feature.properties.color).toBe(FLIGHT_AREA_COLORS.nfz)
  })

  it('三种 type 各自颜色映射', () => {
    const g: DrawnGeometry = { type: 'Circle', coordinates: [0, 0], radius: 10 }
    expect(buildFeature('dfence', g).properties.color).toBe('#00FF00')
    expect(buildFeature('nfz', g).properties.color).toBe('#FF0000')
    expect(buildFeature('noland', g).properties.color).toBe('#FF9900')
  })
})
