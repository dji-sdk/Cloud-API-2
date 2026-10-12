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

import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'
import { getRuntimeConfig } from '@/utils/runtimeConfig'
import type { LiveDeviceInfo } from '@/types/liveStream'
import type { DeviceState } from '@/types/api'

const DEVICES_BASE = `${API_BASE}/api/v1/devices`

function isMockEnabled(): boolean {
  return getRuntimeConfig('MOCK_ENABLED') === 'true'
}

const MOCK_DEVICES: LiveDeviceInfo[] = [
  {
    name: '机场-001',
    dock: { model: 'DJI Dock 2', sn: 'MOCK_DOCK_0001' },
    drone: { model: 'Matrice 3D', sn: 'MOCK_DRONE_0002' },
  },
  {
    name: '机场-002',
    dock: { model: 'DJI Dock 2', sn: 'MOCK_DOCK_0002' },
    drone: { model: 'Matrice 3D', sn: 'MOCK_DRONE_0003' },
  },
  {
    name: '机场-003',
    dock: { model: 'DJI Dock', sn: '7XYZM430010003' },
    drone: { model: 'Matrice 30T', sn: '1581F5HKC230003' },
  },
]

/**
 * 获取在线设备列表（用于直播大屏快速添加）。
 *
 * 后端返回 DeviceState[] 扁平列表，机场（DOCK）通过 childDevices 挂载飞行器。
 * 此处筛选在线机场，提取子设备中的飞行器信息。
 */
export async function fetchOnlineDevices(): Promise<LiveDeviceInfo[]> {
  if (isMockEnabled()) {
    return MOCK_DEVICES
  }

  const res = await authFetch(DEVICES_BASE)
  if (!res.ok) {
    throw new Error(`获取在线设备列表失败: ${res.status}`)
  }

  const data = await res.json() as DeviceState[]

  return data
    .filter((device) => device.online && device.deviceType === 'DOCK')
    .map((device): LiveDeviceInfo => {
      const drone = device.childDevices?.find((child) => child.deviceType === 'DRONE')
      return {
        name: device.name ?? device.sn,
        dock: {
          model: device.model ?? 'Unknown',
          sn: device.sn,
        },
        drone: drone
          ? {
              model: drone.model ?? 'Unknown',
              sn: drone.sn,
            }
          : undefined,
      }
    })
}
