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

// FH2 SDK 类型声明
// window.FH2 为 paas.js 模式（CockpitView），
// WaylineEditor / UranusDeviceManagerApp / FlyDataReplay 为独立 SDK 模式。

export interface FH2InitConfig {
  serverUrl: string
  wssUrl: string
  hostUrl: string
  orgId?: string
  prjId: string
}

/**
 * FH2 驾驶舱配置。
 * loadCockpit 第一个参数为挂载目标 DOM 元素的 id 字符串（如 'cockpit-app-container'），
 * 不是元素引用。
 */
export interface FH2CockpitConfig {
  gateway_sn: string
  drone_sn?: string
  map?: boolean
}

/** FH2 SDK 主题参数（setTheme 前瞻声明，当前 SDK 版本暂不支持，ThemeBus 就绪后自动生效） */
export interface FH2SDKTheme {
  primaryColor: string
  primaryHoverColor: string
  primaryFocusColor: string
  bgBasic: string
  bgSub: string
  bgModal: string
  borderColor: string
  listActiveBg: string
  listHoverBg: string
}

export type FH2EventName =
  | 'cesium-viewer-change'
  | 'cockpit:mounted'
  | 'cockpit:drone-take-off'
  | 'cockpit:camera-take-photo'
  | 'cockpit:camera-record-before'
  | 'cockpit:camera-record-after'
  | 'cockpit:camera-take-panoramic-before'
  | 'cockpit:camera-take-panoramic-after'
  | 'wayline-save'
  | 'wayline-cancel'
  | 'device-loaded'
  | 'device-error'
  | string

/** 设备小窗任务信息 */
export interface DeviceWindowTask {
  status?: string
  label?: string
  name?: string
}

/** 在线天气信息 */
export interface DeviceWindowWeather {
  windSpeed?: number
  precip?: number
  temp?: number
  humidity?: number
  icon?: number
  wns?: number
  ionosphereLevel?: string
}

/** 设备小窗实例，每个设备一个 */
export interface DeviceWindowInstance {
  setTask(task: DeviceWindowTask): void
  setHmsUrl(url: string): void
  setCockpitUrl(url: string): void
  setOnlineWeather(weather: DeviceWindowWeather): void
}

/** loadDeviceWindow 配置 */
export interface DeviceWindowConfig {
  /** 设备 SN，多个用逗号分隔 */
  devices: string
  /** 是否显示地图底图 */
  map?: boolean
}

export interface FH2API {
  /** 同步初始化 SDK 配置，无需 await */
  initConfig(config: FH2InitConfig): void

  /** 初始化推送中心（WebSocket 消息通道） */
  initNotifyCenter(wssUrl: string, auth: { authKeys: string[]; headers: () => Record<string, string> }): void

  /** 加载地图底图 */
  loadMap(serverUrl: string, auth: { authKeys: string[]; headers: () => Record<string, string> }, wssUrl?: string): void

  /** 以 DOM id 字符串定位挂载点，不接受元素引用 */
  loadCockpit(domId: string, config: FH2CockpitConfig): void
  destroyCockpit(isEffect?: boolean): void

  /** 航线预览 */
  loadWaylineView(domId: string, params: {
    waylineIdArray: string[]
    authConfig: { baseUrl: string; auth: SDKAuthConfig }
  }): void
  destroyPaasView(): void

  /** 加载设备小窗 */
  loadDeviceWindow(domId: string, config: DeviceWindowConfig): Promise<DeviceWindowInstance[]>
  /** 更新设备小窗的设备列表 */
  updateDeviceWindow(config: { devices: string }): void
  /** 销毁设备小窗 */
  destroyDeviceWindow(): void

  subscribe(eventName: FH2EventName, callback: (...args: unknown[]) => void): void
  unsubscribe(eventName: FH2EventName): void
  setTheme?(theme: FH2SDKTheme): void
  cesiumViewer?: Record<string, unknown>
}

// ── 模式 B 独立 SDK ─────────────────────────────────────

export interface IDeviceManagerAuth {
  authKeys: string[]
  headers: (() => Record<string, string>) | Record<string, string>
}

export interface IDeviceManagerSetupOptions {
  /** 后端地址，例 http://host:30812（不带 /gateway） */
  serverUrl: string
  /** 推送中心 WebSocket 地址 */
  wsUrl: string
  /** 鉴权配置 */
  auth: IDeviceManagerAuth
  /** 语言：'zh' | 'en'，默认 'zh' */
  locale?: string
  /** 主题：'light' | 'dark' 或自定义 ThemeTokens 对象 */
  theme?: string | FH2SDKTheme
  /** Ant Design 主题 CSS 加载基路径 */
  antThemeBaseUrl?: string
  /** 回调 */
  callbacks?: {
    onError?: (err: unknown) => void
  }
}

export interface IDeviceManagerMountOptions {
  /** CSS 选择器，必须带 #，例如 '#uranus-device-manager-app' */
  container: string
  /** 挂载参数 */
  props?: {
    /** 设备类型过滤：'airport' 等 */
    deviceType?: string
  }
}

/** SDK 模式设备管理初始化参数（旧版 init 一步式 API） */
export interface IDeviceManagerInitOptions {
  /** CSS 选择器，例 '#uranus-device-manager-app'（带 #） */
  containerId: string
  /** 后端地址，例 http://host:30812（不带 /gateway） */
  serverUrl: string
  /** 推送中心 WebSocket 地址 */
  wsUrl: string
  /** 鉴权配置 */
  auth: IDeviceManagerAuth
  /** 语言：'zh' | 'en'，默认 'zh' */
  locale?: string
  /** 主题：'light' | 'dark' 或自定义 ThemeTokens 对象 */
  theme?: string | FH2SDKTheme
  /** Ant Design 主题 CSS 加载基路径 */
  antThemeBaseUrl?: string
  /** 回调 */
  callbacks?: {
    onError?: (err: unknown) => void
  }
}

/** SDK 模式设备管理（兼容新旧两版 API） */
export interface UranusDeviceManagerApp {
  /** 旧版一步式初始化（containerId + 配置合并传入） */
  init?(options: IDeviceManagerInitOptions): Promise<void>
  /** 新版环境初始化（鉴权、网络、主题等全局配置） */
  setupDeviceManager?(options: IDeviceManagerSetupOptions): Promise<void>
  /** 新版挂载设备列表组件到指定 DOM 容器 */
  mountDeviceList?(options: IDeviceManagerMountOptions): void
  /** 打开主题配置面板 */
  showThemePanel?(): void
}

export interface SDKAuthConfig {
  authKeys: string[]
  headers: Record<string, string> | (() => Record<string, string>)
}

export interface SDKBaseOptions {
  serverUrl: string
  wssUrl: string
  auth: SDKAuthConfig
}

// ── WaylineEditor SDK ───────────────────────────────────

/** WaylineEditor 地图运行期环境配置（透传给 map-sdk） */
export interface WaylineEditorMapEnv {
  /** Cesium 资源 base 路径 */
  cesiumBaseUrl?: string
  /** 地形服务 key */
  terrainKey?: string
  /** 地形/要素网关列表 */
  featureGateway?: unknown[]
}

/** WaylineEditor 地图配置（internal 模式：SDK 自建 Cesium 地图） */
export interface WaylineEditorMapConfig {
  /** 地图模式，目前仅支持 'internal' */
  mode: 'internal'
  /** 地图容器 CSS 选择器（若 loadMap 不传容器则使用此配置） */
  container?: string
  /** 运行期环境配置，透传给 map-sdk */
  env?: WaylineEditorMapEnv
  /** 设备拓扑连线插件配置（可选） */
  topo?: {
    snList: string[]
    showFullTopo?: boolean
  }
}

/** WaylineEditor 功能开关 */
export interface WaylineEditorFeatures {
  /** 第三方算法接入 */
  thirdPartyAlgo?: boolean
}

export interface WaylineEditorOptions {
  /** paas.js 所在站点地址，FH2 加载微应用资源用 */
  hostUrl: string
  /** API 网关配置 */
  api: {
    /** 后端网关 baseUrl，例 http://host:30812/gateway */
    baseUrl: string
    /** 鉴权配置，同时给 HTTP / FH2 / WebSocket 复用 */
    auth: SDKAuthConfig
  }
  /** WebSocket 配置 */
  ws?: {
    /** WebSocket 地址（推送中心） */
    url: string
  }
  /**
   * 运行时依赖库（PPE/WPMZ/AR/LiDAR/Mine 等 WASM+JS）资源 base 路径。
   * 不传则默认从 SDK 自身目录加载，FH2 服务器该目录缺失会 load failed。
   */
  runtimeDepsBasePath?: string
  /** 地图配置（internal 模式：SDK 自建 Cesium 地图） */
  map?: WaylineEditorMapConfig
  /** 功能开关 */
  features?: WaylineEditorFeatures
  /** 初始航线 ID（edit 模式；render 第二参数也可覆盖） */
  waylineId?: string
  /** OSS 上传 key 前缀 */
  oss?: { objectKeyPrefix?: string }
  /** 语言配置 */
  locale?: { language?: string; messages?: unknown }
  /** 度量单位 */
  units?: unknown
  /** 限制机型/负载 */
  supportedDrones?: Array<{ drone: number; payloadList: number[] }>
  /** 限制可创建的航线类型 */
  enabledWaylineTypes?: number[]
  /** 主题 */
  theme?: Partial<FH2SDKTheme>
}

/** WaylineEditor.loadMap 初始视角坐标 */
export interface WaylineEditorMapCenter {
  longitude: number
  latitude: number
  height?: number
}

export interface WaylineCreateResult {
  waylineId: string
}

export interface WaylineRenderOptions {
  waylineId?: string
  from?: string
  defaultPosition?: { longitude: number; latitude: number; height?: number }
}

export interface WaylineErrorEvent {
  code: number
  message: string
  data?: Record<string, unknown>
}

export declare class WaylineEditor {
  constructor(options: WaylineEditorOptions)
  /** 挂载到 DOM 容器 */
  mount(container: HTMLElement): void
  /**
   * 加载地图（edit/create 模式必须在 mount 之前调用）。
   * @param container - 地图容器元素；不传则使用 map.container 配置
   * @param centerPoint - 初始视角坐标；不传使用默认
   */
  loadMap(container?: HTMLElement, centerPoint?: WaylineEditorMapCenter): Promise<void>
  /** 渲染编辑模式 */
  render(mode: 'edit', options?: WaylineRenderOptions): void
  /** 打开创建航线弹窗 */
  create(options?: { centerPoint?: { longitude: number; latitude: number } }): Promise<WaylineCreateResult>
  /** 运行时热切换主题 */
  setTheme?(theme: Partial<FH2SDKTheme>): void
  /** 事件监听 */
  on(event: string, handler: (...args: unknown[]) => void): void
  off(event: string, handler: (...args: unknown[]) => void): void
  /** 卸载 Vue 实例、清空事件监听 */
  destroy(): void

  /**
   * 拼接多条航线为一条新航线（静态方法）。
   * @param name - 拼接后的航线名称
   * @param uuids - 子航线 UUID 数组（至少 2 条）
   * @returns 新航线的 UUID
   */
  static concatWayline(name: string, uuids: string[]): Promise<string>
}

// ── FlyDataReplay SDK ───────────────────────────────────

export interface FlyDataReplayFeatures {
  playerBarBtn?: {
    params?: boolean
    media?: boolean
    ai_warning?: boolean
    sdr?: boolean
    fov?: boolean
    wayline?: boolean
  }
  playerTrack?: {
    flight_status?: boolean
    device_warning?: boolean
    ai_target_warning?: boolean
  }
}

export interface FlyDataReplaySidebarItem {
  key: string
  icon: string
  label: string
  renderPanel: (taskData: Record<string, unknown>) => HTMLElement | string
}

export interface FlyDataReplayMapConfig {
  /** SDK 自建地图模式 */
  mode: 'internal'
  /** 地图容器 CSS 选择器 */
  container: string
  /** 运行期环境配置，透传给 map-sdk */
  env?: {
    cesiumBaseUrl?: string
    terrainKey?: string
    featureGateway?: unknown[]
  }
  /** 拓扑设备连线插件 */
  topo?: {
    snList: string[]
    showFullTopo?: boolean
  }
}

export interface FlyDataReplayOptions {
  /** 飞行任务 UUID */
  taskId: string
  /** paas.js 所在站点地址 */
  hostUrl: string
  /** API 网关配置 */
  api: {
    /** 后端网关 baseUrl，例 http://host:30812/gateway */
    baseUrl: string
    /** 鉴权配置 */
    auth: SDKAuthConfig
  }
  /** 运行时依赖库（PPE/WPMZ 等 WASM+JS）资源路径前缀 */
  runtimeDepsBasePath?: string
  /** 地图配置（internal 模式：SDK 自建 Cesium 地图） */
  map?: FlyDataReplayMapConfig
  /** 语言：字符串简写 'zh' 或对象形式 */
  locale?: string | { language: string; messages?: unknown }
  /** WebSocket 配置（AI 报告导出完成通知） */
  websocket?: {
    url: string
    auth: SDKAuthConfig
  }
  /** 主题 */
  theme?: Partial<FH2SDKTheme>
  /** 功能开关 */
  features?: FlyDataReplayFeatures
  /** 自定义侧边栏面板 */
  sidebarItems?: FlyDataReplaySidebarItem[]
  /** 自定义任务信息面板 */
  renderTaskInfo?: (taskData: Record<string, unknown>) => {
    taskInfoTitle?: string
    baseInfoTitle?: string
    content?: HTMLElement | string
  }
}

export interface FlyDataErrorEvent {
  code: number
  message: string
  data?: Record<string, unknown>
}

export declare class FlyDataReplay {
  constructor(options: FlyDataReplayOptions)
  /** 加载地图（必须在 mount 之前调用） */
  loadMap(): Promise<void>
  /** 挂载到 DOM 容器（必须在 loadMap 之后调用） */
  mount(container: HTMLElement): void
  /** 运行时热切换主题 */
  setTheme?(theme: Partial<FH2SDKTheme>): void
  /** 运行时切换语言 */
  setLocale?(locale: 'zh' | 'en' | 'es'): void
  /** 运行时切换拓扑设备连线显示 */
  showDeviceTopo?(snList: string[]): void
  /** 事件监听 */
  on(event: string, handler: (...args: unknown[]) => void): void
  off(event: string, handler: (...args: unknown[]) => void): void
  /** 卸载 Vue 实例、清空事件监听 */
  destroy(): void
  /** 获取全量拓扑设备列表（静态方法） */
  static getTopoList(): Promise<unknown[]>
}

// ── window 全局扩展 ──────────────────────────────────────

declare global {
  interface Window {
    FH2: FH2API
    Cesium: unknown
    WaylineEditor: typeof WaylineEditor
    UranusDeviceManagerApp: UranusDeviceManagerApp
    FlyDataReplay: typeof FlyDataReplay
  }
}
