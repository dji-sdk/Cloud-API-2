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

import type { FlightAreaContent, FlightAreaType, DrawnGeometry } from '@/types/flightArea'
import { FLIGHT_AREA_COLORS } from '@/types/flightArea'

/**
 * 将绘制产出的几何组装成 FlightAreaContent（GeoJSON Feature）。
 *
 * @param type     区域类型，决定 properties.color
 * @param geometry 绘制产出的几何（Polygon 或 Circle）
 * @returns GeoJSON Feature content，可直接作为创建/更新请求体
 */
export function buildFeature(type: FlightAreaType, geometry: DrawnGeometry): FlightAreaContent {
  return {
    type: 'Feature',
    properties: { color: FLIGHT_AREA_COLORS[type] },
    // 断言原因：DrawnGeometry 与 FlightAreaContent.geometry 结构兼容
    // （Polygon coordinates: number[][][]、Circle coordinates: number[] + radius），
    // 此处已知结构正确，绕过联合类型字面量赋值的窄化限制
    geometry: geometry as FlightAreaContent['geometry'],
  }
}
