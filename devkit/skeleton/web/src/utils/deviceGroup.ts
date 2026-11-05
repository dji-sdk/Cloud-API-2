/*
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

import type { DeviceState } from '@/types/api'

/** 设备展示类别：基站 / 机场 / 飞行器。比 DeviceType 更细，可区分基站与机场 */
export type DeviceCategory = 'base_station' | 'dock' | 'drone'

/** 一个机场分组：顶层设备及其子树中解析出的基站 / 机场 / 飞行器节点 */
export interface DeviceGroup {
  /** 分组的顶层设备（340 拓扑下为基站，普通拓扑下为机场） */
  root: DeviceState
  /** 子树中的基站节点（无则 undefined） */
  baseStation?: DeviceState
  /** 子树中的机场节点（无则 undefined） */
  dock?: DeviceState
  /** 子树中的全部飞行器节点 */
  drones: DeviceState[]
  /**
   * 进驾驶舱 / 直播 RTC 使用的网关 SN。
   *
   * - 普通机场拓扑：机场 SN（缺机场时回退顶层 SN）；
   * - 340 / O4 组网拓扑（存在基站节点）：飞行器 SN —— 此时飞行器为网络无人机、直连云端，
   *   直播 RTC 的网关是飞行器自身而非机场/基站（经现场原生门户验证：gateway_sn 与 drone_sn
   *   均需填飞行器 SN 才能连上 RTC）。
   *
   * <p>注意：此字段仅用于驾驶舱/直播，不用于航线等指令下发（指令网关为域内领域问题，另行处理）。
   */
  gatewaySn: string
}

/**
 * 解析设备展示类别。
 *
 * <p>优先用 SDK 原始 deviceClass（dock / base_station / drone），大小写不敏感；
 * deviceClass 缺省（后端事件缓存兜底路径）时回退到 deviceType —— 此时无法区分基站与机场，
 * 一律归为 dock。
 */
export function deviceCategory(d: DeviceState): DeviceCategory {
  const cls = d.deviceClass?.toLowerCase()
  if (cls === 'base_station') {
    return 'base_station'
  }
  if (cls === 'dock' || cls === 'airport') {
    return 'dock'
  }
  if (cls === 'drone') {
    return 'drone'
  }
  // deviceClass 缺省：回退到 deviceType
  return d.deviceType === 'DOCK' ? 'dock' : 'drone'
}

/** 深度优先展开设备子树为扁平列表（含自身） */
function flatten(d: DeviceState): DeviceState[] {
  return [d, ...(d.childDevices ?? []).flatMap(flatten)]
}

/**
 * 比较两架飞行器谁更像机场「当前绑定机」，返回负数表示 a 更优（应排在前）。
 *
 * <p>优先级：在线 &gt; 有 OSD 遥测 &gt; 最近上线。用于在一对多异常时选出主飞行器。
 */
function compareDroneByActiveness(a: DeviceState, b: DeviceState): number {
  // 1. 在线优先
  if (a.online !== b.online) {
    return a.online ? -1 : 1
  }
  // 2. 有遥测（lastOsd 非空）优先 —— 真机在持续上报，ghost 从未上报过 OSD
  // 用布尔化而非 != null：后端可能返回 null/undefined，二者都视为「无遥测」
  const aHasOsd = Boolean(a.lastOsd)
  const bHasOsd = Boolean(b.lastOsd)
  if (aHasOsd !== bHasOsd) {
    return aHasOsd ? -1 : 1
  }
  // 3. 最近上线优先（onlineSince 越新越可能是当前真机）
  const aSince = a.onlineSince ? Date.parse(a.onlineSince) : 0
  const bSince = b.onlineSince ? Date.parse(b.onlineSince) : 0
  return bSince - aSince
}

/**
 * 将同一机场分组下解析出的候选飞行器收敛为唯一的当前绑定机。
 *
 * <p>机场（dock）与飞行器是<b>一对一</b>绑定。当平台 / 模拟器因陈旧绑定对同一机场返回多架
 * 飞行器时（现场：模拟机场 PF60BK6HTCJF5Y 同时挂着在报遥测的真机 PFYQLBPLSCHFM9 与一架
 * 从未上报 OSD 的 ghost PFYE8HAG4EC2XVT99QL9，两者 online 均为 true），驾驶舱列表会出现
 * 重复的「驾驶舱」入口。这里先按 sn 去重，若仍有多架则按
 * {@link compareDroneByActiveness}「在线 &gt; 有遥测 &gt; 最近上线」选出主飞行器，只保留它。
 *
 * <p><b>单架不筛选</b>：机场在线但飞行器待机离线是正常状态（飞机落舱断电），此时唯一候选
 * 原样返回，不会被「过滤离线」误伤。
 *
 * @param drones 子树中解析出的全部飞行器节点
 * @return 收敛后的飞行器列表（0 或 1 架）
 */
function selectBoundDrones(drones: DeviceState[]): DeviceState[] {
  const uniqueBySn = new Map<string, DeviceState>()
  for (const drone of drones) {
    if (!uniqueBySn.has(drone.sn)) {
      uniqueBySn.set(drone.sn, drone)
    }
  }
  const unique = [...uniqueBySn.values()]
  if (unique.length <= 1) {
    return unique
  }
  // 多架 => 机场一对一绑定异常，选出最像当前真机的那一架；相等时保留原顺序首个（稳定）
  const primary = unique.reduce((best, cur) =>
    (compareDroneByActiveness(cur, best) < 0 ? cur : best))
  return [primary]
}

/**
 * 解析进驾驶舱 / 直播 RTC 的网关 SN。
 *
 * <p>340 / O4 组网拓扑（分组内存在基站节点）下，飞行器是网络无人机、直连云端，直播 RTC 的
 * 网关是飞行器自身——必须用飞行器 SN，否则 RTC 连不上（现场原生门户用 gateway_sn=drone_sn=
 * 飞行器 SN 才能连通）。普通机场拓扑仍用机场 SN，缺机场时回退顶层设备 SN。
 *
 * @param root 分组顶层设备
 * @param baseStation 基站节点（存在即判定为组网拓扑）
 * @param dock 机场节点
 * @param drones 收敛后的飞行器列表
 * @return 网关 SN
 */
function resolveGatewaySn(
  root: DeviceState,
  baseStation: DeviceState | undefined,
  dock: DeviceState | undefined,
  drones: DeviceState[],
): string {
  if (baseStation && drones.length > 0) {
    return drones[0].sn
  }
  return dock?.sn ?? root.sn
}

/** 从单个顶层设备的子树构造机场分组 */
function buildGroup(root: DeviceState): DeviceGroup {
  const all = flatten(root)
  const baseStation = all.find(d => deviceCategory(d) === 'base_station')
  const dock = all.find(d => deviceCategory(d) === 'dock')
  const drones = selectBoundDrones(all.filter(d => deviceCategory(d) === 'drone'))
  return {
    root,
    baseStation,
    dock,
    drones,
    gatewaySn: resolveGatewaySn(root, baseStation, dock, drones),
  }
}

/**
 * 将设备列表构造为机场分组列表。
 *
 * <p>只保留在线、且本身是机场或基站的顶层设备作为分组根（裸飞行器顶层设备不构成机场分组）。
 * 每个分组从其子树中解析出基站 / 机场 / 飞行器节点，各自保留自己的 SN —— 修复「340 机场卡片
 * 显示基站 SN」的问题。
 *
 * @param devices 后端返回的顶层设备列表（childDevices 为嵌套子树）
 * @return 机场分组列表
 */
export function buildDeviceGroups(devices: DeviceState[]): DeviceGroup[] {
  return devices
    .filter(d => d.online && deviceCategory(d) !== 'drone')
    .map(buildGroup)
}

/**
 * 构建飞行器 SN → 驾驶舱网关 SN 的索引。
 *
 * <p>与 {@link buildDeviceGroups} 的区别：<b>不按分组根的在线状态过滤</b>。设备列表页的
 * 「驾驶舱」入口是否展示，取决于飞行器自身是否有网关可连，而不是机场此刻是否在线 ——
 * 机场临时掉线时其下属飞行器仍应保留入口（否则回退到旧实现会直接消失）。
 *
 * <p>同一飞行器若出现在多个分组中，在线分组优先（在线根排序在前，且先到先得、不被后续覆盖）。
 *
 * @param devices 后端返回的顶层设备列表（childDevices 为嵌套子树）
 * @return 飞行器 SN 到网关 SN 的映射
 */
export function buildDroneGatewayIndex(devices: DeviceState[]): Map<string, string> {
  const roots = devices.filter(d => deviceCategory(d) !== 'drone')
  // 稳定排序：在线根优先，保证同一飞行器归属冲突时取在线分组
  const ordered = [...roots.filter(d => d.online), ...roots.filter(d => !d.online)]

  const index = new Map<string, string>()
  for (const root of ordered) {
    const group = buildGroup(root)
    for (const drone of group.drones) {
      // 先到先得：ordered 中在线根排在前，故已存在的映射不会被后到的离线分组覆盖
      if (!index.has(drone.sn)) {
        index.set(drone.sn, group.gatewaySn)
      }
    }
  }
  return index
}
