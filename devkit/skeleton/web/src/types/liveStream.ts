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

// DJILivePlayer SDK 类型声明 + 直播大屏业务类型

/** 播放器填充模式 */
export type FillMode = 'contain' | 'fill'

/** SDK 支持的语言 */
export type LivePlayerLocale = 'zh-CN' | 'en-US'

/** 播放器状态 */
export type PlayerState = 'idle' | 'connecting' | 'playing' | 'error' | 'closed'

// ── 网格布局 ────────────────────────────────────────────────

/** 普通布局键 */
export type NormalLayoutKey = '1' | '4' | '9' | '16' | '25'

/** L 型布局键 */
export type LShapeLayoutKey = 'l6' | 'l8' | 'l13'

/** 包围布局键 */
export type SurroundLayoutKey = 'c13'

/** 所有布局键联合类型 */
export type GridLayoutKey = NormalLayoutKey | LShapeLayoutKey | SurroundLayoutKey

/** 布局分组 */
export type LayoutGroup = 'normal' | 'lShape' | 'surround'

/** 单个布局配置 */
export interface GridLayoutConfig {
  /** 最大槽位数 */
  max: number
  /** CSS Grid repeat 值 */
  repeat: number
  /** 第一个 slot 的 span（L 型布局用） */
  firstItemSpan?: number
  /** 中心 slot 的 span（包围布局用） */
  centerSpan?: number
}

// ── 播放器槽位 ──────────────────────────────────────────────

/** 单个播放器槽位状态 */
export interface PlayerSlot {
  /** 唯一标识，null 表示空槽位 */
  id: string | null
  /** 设备 SN，null 表示未分配 */
  sn: string | null
  /** 播放状态 */
  state: PlayerState
  /** 解析到的流信息 */
  resolved: unknown
  /** 错误信息 */
  error: unknown
}

// ── 事件日志 ────────────────────────────────────────────────

/** 事件日志条目 */
export interface LogEntry {
  id: number
  timestamp: string
  sn: string | null
  event: string
  data?: unknown
  isError: boolean
}

// ── 设备信息 ────────────────────────────────────────────────

/** 在线设备简要信息（用于快速添加） */
export interface LiveDeviceInfo {
  name: string
  dock: {
    model: string
    sn: string
  }
  drone?: {
    model: string
    sn: string
  }
}

// ── SDK 接口类型 ─────────────────────────────────────────────

/** DJILivePlayer SDK 鉴权配置 */
export interface LivePlayerAuthConfig {
  authKeys: string[]
  headers: () => Record<string, string>
}

/** initLivePlayer 全局初始化参数 */
export interface LivePlayerInitOptions {
  /** 前端资源地址 */
  assetHost?: string
  /** 后端网关地址 */
  serverUrl: string
  /** 鉴权配置 */
  auth: LivePlayerAuthConfig
  /** 调试模式 */
  debug?: boolean
  /** UI 语言 */
  locale?: LivePlayerLocale
}

/** DjiLivePlayer 播放器实例构造参数 */
export interface DjiLivePlayerOptions {
  /** DOM 容器选择器，例如 '#player-1' */
  container: string
  /** 设备 SN */
  sn: string
  /** 填充模式 */
  fillMode?: FillMode
  /** 是否显示 OSD 叠加信息 */
  showOsd?: boolean
}

/** DjiLivePlayer 播放器实例接口 */
export interface DjiLivePlayerInstance {
  on(event: string, callback: (...args: unknown[]) => void): void
  setFillMode(mode: FillMode): void
  setShowOsd(show: boolean): void
  destroy(): void
}

/** DjiLivePlayer 构造函数类型 */
export type DjiLivePlayerConstructor = new (options: DjiLivePlayerOptions) => DjiLivePlayerInstance

/** window.DJILivePlayer 全局 SDK 对象 */
export interface DJILivePlayerSDK {
  DjiLivePlayer: DjiLivePlayerConstructor
  initLivePlayer(options: LivePlayerInitOptions): void
  isLivePlayerInitialized(): boolean
  getLivePlayerOptions(): LivePlayerInitOptions | null
}

// ── Window 全局扩展 ──────────────────────────────────────────

declare global {
  interface Window {
    DJILivePlayer?: DJILivePlayerSDK
  }
}
