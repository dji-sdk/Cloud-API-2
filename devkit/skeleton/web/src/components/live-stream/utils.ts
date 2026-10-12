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

import type { PlayerSlot } from '@/types/liveStream'

/**
 * 根据 slotId 生成播放器容器 DOM ID。
 * 当 slot 为空时返回随机占位 ID（不会实际挂载播放器）。
 */
export function getContainerId(slotOrId: string | PlayerSlot): string {
  if (typeof slotOrId === 'string') {
    return `live-player-${slotOrId}`
  }
  const id = slotOrId.id
  return `live-player-${id ?? `empty-${Math.random().toString(36).slice(2, 6)}`}`
}
