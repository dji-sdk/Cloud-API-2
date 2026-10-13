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

import type { GridLayoutKey, GridLayoutConfig, LayoutGroup } from '@/types/liveStream'

/** 所有布局配置 */
export const LAYOUT_CONFIGS: Record<GridLayoutKey, GridLayoutConfig> = {
  '1':   { max: 1,  repeat: 1 },
  '4':   { max: 4,  repeat: 2 },
  '9':   { max: 9,  repeat: 3 },
  '16':  { max: 16, repeat: 4 },
  '25':  { max: 25, repeat: 5 },
  'l6':  { max: 6,  repeat: 3, firstItemSpan: 2 },
  'l8':  { max: 8,  repeat: 4, firstItemSpan: 3 },
  'l13': { max: 13, repeat: 4, firstItemSpan: 2 },
  'c13': { max: 13, repeat: 4, centerSpan: 2 },
}

/** 布局分组 */
export const LAYOUT_GROUPS: Record<LayoutGroup, GridLayoutKey[]> = {
  normal:   ['1', '4', '9', '16', '25'],
  lShape:   ['l6', 'l8', 'l13'],
  surround: ['c13'],
}

/** 布局分组中文名 */
export const LAYOUT_GROUP_LABELS: Record<LayoutGroup, string> = {
  normal:   '普通布局',
  lShape:   'L 型布局',
  surround: '包围布局',
}
