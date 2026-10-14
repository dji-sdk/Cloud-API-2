# 从零实现：FHSDK 地图 · SEI FOV 可视化 Demo

> 本文档自包含。照着从上到下做，即可得到一个独立运行的前端项目：
> **左侧实时直播画面 + 右侧 Cesium 三维地图（飞机模型 / SEI 相机视锥 FOV / 视频贴地投影）**，
> 数据来自真实后端遥测（WebSocket + REST）。
>
> 技术栈：Vue 3 + Vite + Cesium + axios（不使用 Pinia，状态用普通 `reactive`）。
>
> **本文只讲前端。** 数据来自服务端，服务端如何用 FHSDK 拿到 OSD / SEI 并通过
> REST + WebSocket 推给前端，见配套文档 **《SEI_OSD_服务端数据获取指南.md》**（同目录）。
> 两份文档的衔接点（端口、消息格式）见 §3.1。

---

## 0. 名词速查

| 名词 | 含义 |
|---|---|
| **机场 / Dock** | 无人机机库（gateway），有自己的经纬度，是 SEI 相机偏移的 GND 原点 |
| **OSD** | 慢通道遥测（MQTT，~0.5Hz）：飞机位置、姿态、机场位置等 |
| **SEI** | 直播码流里嵌的帧级信息（开直播后才有）：相机姿态(yaw/pitch/roll)、相对机场的偏移、FOV/焦距 |
| **FOV 视锥** | 由相机姿态 + 视场角构成的四棱锥，表示相机在拍哪片区域 |
| **落地四边形 (footprint)** | 视锥与地面的交线围成的四边形 |
| **视频投影** | 把直播画面当纹理贴到落地四边形上，相当于把画面"投影"到地面 |
| **WHEP** | WebRTC 拉流协议，从 Oryx(SRS) 播放直播 |
| **payload** | 负载（相机），形如 `98-0-0` |

---

## 1. 成品效果与功能边界

**做什么：**
1. 顶栏填机场 SN → 「连接」：`init → selectDevice → WS → 拉 Payload 列表`
2. 选 Payload → 「开启直播」：`confirmPayload → startLive` → 后端推送 SEI
3. 左侧显示直播画面；右侧地图显示：
   - 机场点（青）、飞机 glTF 模型
   - SEI 相机三轴（红=光轴/绿/蓝）、光轴落地点（橙）
   - **视锥线框（青）+ 落地四边形（黄）+ 视频贴地投影**

**不做什么（已刻意砍掉）：** 设备列表、Pinia、DRC 激光测距、杆量/云台控制、OSD 表格。

---

## 2. 架构与数据流

```
后端 :8080                         Oryx :2022
   │  REST /api/v1/*                  │ WHEP 直播
   │  WS   /api/v1/ws/telemetry       │
   ▼                                  ▼
api/* + ws/telemetry.ts          utils/whep.ts
   │  消息 {mqtt_osd|sei}              │ MediaStream
   ▼                                  ▼
state.ts (reactive osd/session)   <video> 左侧
   │                                  │ 同一元素作纹理源
   ▼                                  ▼
views/MapDemoView.vue  ──updateMap()──▶ cesium/*（地图渲染）
```

- **WS 遥测**到达 → 写入 `state.ts` 的 reactive 对象 → 触发 `scheduleMapUpdate()`（rAF 合并）→ `updateMap()` 把数值喂给 `cesium/` 的纯函数。
- `cesium/` 模块不依赖任何框架，只吃 `viewer + 数值`。

**依赖顺序（建文件时按此序，后者依赖前者）：**

```
utils/seiNed.ts
utils/seiCamera.ts
cesium/seiFrustum.ts        → seiNed
cesium/seiRaycast.ts        → seiNed
cesium/seiFrustumFootprint  → seiFrustum, seiRaycast
cesium/dynamicPrimitives    → seiFrustum
cesium/groundVideoFootprint → seiRaycast
cesium/seiMapLayer          → 以上全部 + seiNed
cesium/map.ts               → seiMapLayer
```

---

## 3. 前置条件

- Node.js ≥ 18、npm
- **后端 `:8080` 已运行**（提供 `/api/v1/*` 与 `/api/v1/ws/telemetry`）
- **Oryx `:2022` 已运行**（提供 WHEP 直播流；不开则只有线框，无贴地画面）
- 一个在线机场 SN、可用 payload
- （可选）Cesium Ion Token —— 开「Ion 世界地形」用；不填则用椭球地球，FOV 旋转效果照样可见

---

## 3.1 配置与端口（前后端衔接）⭐

所有端口都**可配置**，但前端、后端必须对得上。下面把全部端口、配在哪、谁要跟谁一致列清楚。

### 端口全景

```
浏览器 ──5173──► 前端 Vite Dev Server
                  │  proxy /api  ──8080──► 后端 fhsdk-demo-server ──30812──► DJI CloudAPI2 服务器(<fhsdk-server-host>)
                  └  proxy /oryx ──2022──► Oryx(SRS) 流媒体  ◄──WHIP 推流── 后端
浏览器 ◄──WHEP 拉流(经 /oryx 代理 → 2022)── Oryx
```

### 后端 `application.yml`

| 配置项 | 默认值 | 作用 | 谁要跟它一致 |
|---|---|---|---|
| `server.port` | `8080` | 后端 HTTP + WebSocket 服务端口 | 前端 vite proxy `/api` 的 target |
| `fhsdk.address` | `http://<fhsdk-server-host>` | DJI CloudAPI2 服务器地址 | —（换环境时改，前端无感） |
| `fhsdk.port` | `30812` | DJI CloudAPI2 端口 | — |
| `fhsdk.notification-server` | `ws://<fhsdk-server-host>:30812/...` | FH 通知 WS（含 `x-auth-token`） | 与 address/port 同源 |
| `oryx.whip-url-template` | `…127.0.0.1:2022…` | 后端把直播 WHIP 推到 Oryx | Oryx 实际地址 |
| `oryx.whep-url-template` | `…127.0.0.1:2022…` | 返回给前端的 WHEP 拉流地址 | 前端 proxy `/oryx` + `whep.ts` 正则 |

### 前端 `vite.config.ts`

| 配置项 | 默认值 | 改它的时机 |
|---|---|---|
| `server.port` | `5173` | 想换前端访问端口，随意 |
| proxy `/api` → `target` | `http://localhost:8080` | **必须 = 后端 `server.port`** |
| proxy `/oryx` → `target` | `http://127.0.0.1:2022` | **必须 = Oryx 实际端口** |

### 三个"改一处必须同步改对面"的耦合点 ⚠️

1. **后端服务端口**：后端不跑 8080 → 改前端 `vite.config.ts` 里 `/api` 的 `target`。WebSocket 也走 `/api`（`ws: true`），一并生效。
2. **Oryx 端口/host（默认 2022）出现在三处**，改一处要三处一起改，否则左侧直播 + 视频投影拉不到流：
   - 后端 `oryx.whip-url-template`、`oryx.whep-url-template`
   - 前端 `vite.config.ts` 的 `/oryx` proxy target
   - 前端 `src/utils/whep.ts` 的正则 `^https?:\/\/127\.0\.0\.1:2022`
     （后端返回的 WHEP URL 里带 `127.0.0.1:2022`，前端靠这条正则把它改写成 `/oryx` 代理来规避跨域）
3. **DJI CloudAPI2 环境**（`fhsdk.address/port/notification-server`、token）：只在后端，换内网/正式环境时改；默认是内网测试环境写死值，详见服务端指南 §3。

### 前后端契约：WS 消息格式

前端 `ws/telemetry.ts` 解析的 `TelemetryMessage`，字段须与服务端 `TelemetryMapper`（服务端指南 §12）一致：

```jsonc
// type=mqtt_osd，子 field=location/dockLocation/attitude
{ "type": "mqtt_osd", "field": "location", "gatewaySn": "...", "data": { "latitude":.., "longitude":.., "height":.. } }
// type=sei（开直播后），data 为 AR+FOV 合并字段（cameraYaw/cameraPosX/fovHorizontal/...）
{ "type": "sei", "gatewaySn": "...", "data": { "cameraYaw":.., "cameraPitch":.., "fovHorizontal":.., ... } }
```

> 字段语义与单位（mm/μm/度）见服务端指南 §6.5 与本文 §12.1。改了服务端字段名，前端 `state.ts` 的 `handleTelemetry` 与 `updateMap` 取值处要同步。

---

## 4. 第一步：脚手架与依赖

```bash
mkdir fhsdk-map-demo && cd fhsdk-map-demo
mkdir -p src/api src/ws src/cesium src/utils src/views src/styles public/models
```

放置飞机模型：把任意 glTF 飞机模型保存为 `public/models/Cesium_Air.glb`
（可用 CesiumJS 官方示例资源 `Cesium_Air.glb`）。缺这个文件飞机模型不显示，但不影响其余功能。

---

## 5. 第二步：工程配置

### `package.json`

```json
{
  "name": "fhsdk-map-demo",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "axios": "^1.7.9",
    "cesium": "^1.125.0",
    "vue": "^3.5.13"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.1",
    "sass": "^1.83.0",
    "typescript": "~5.6.3",
    "vite": "^6.0.5",
    "vite-plugin-cesium": "^1.2.23",
    "vue-tsc": "^2.2.0"
  }
}
```

> `build` 只跑 `vite build`（esbuild 转译，不做类型检查）。如需类型检查另跑 `npx vue-tsc --noEmit`。

### `vite.config.ts`

`vite-plugin-cesium` 负责把 Cesium 静态资源拷进产物。`/api` 代理到后端（含 WebSocket），`/oryx` 代理到本机 Oryx 以规避 WHEP 跨域。

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import cesium from 'vite-plugin-cesium'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue(), cesium()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
      '/oryx': {
        target: 'http://127.0.0.1:2022',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/oryx/, ''),
      },
    },
  },
})
```

### `tsconfig.json`

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "preserve",
    "strict": true,
    "noUnusedLocals": false,
    "noUnusedParameters": false,
    "noFallthroughCasesInSwitch": true,
    "paths": { "@/*": ["./src/*"] }
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"]
}
```

### `index.html`

```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>FHSDK 地图 Demo</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

### `.env.example`（可选 token）

```
# Cesium Ion（从 https://cesium.com/ion/ 申请，可留空，仅影响 Ion 世界地形/影像）
VITE_CESIUM_ION_TOKEN=
```

要用地形就复制为 `.env.local` 填入 token；改 env 后**必须重启 dev**。

### `src/vite-env.d.ts`

```ts
/// <reference types="vite/client" />
```

---

## 6. 第三步：入口 `src/main.ts`

```ts
/** FHSDK 地图 Demo 前端入口（无 Pinia / 无 App 壳，状态见 src/state.ts） */
import { createApp } from 'vue'
import MapDemoView from './views/MapDemoView.vue'
import './styles/map-demo.scss'

createApp(MapDemoView).mount('#app')
```

---

## 7. 第四步：数据层

### `src/api/client.ts`

Axios 实例：`withCredentials` 保证 REST 与 WebSocket 共用同一 HttpSession；拦截器在"会话未初始化(409)"时自动补 `/session/init` 再重试。

```ts
import axios from 'axios'

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/** Axios 实例：withCredentials 保证与 WebSocket 共用同一 HttpSession */
const client = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
  timeout: 30000,
})

let initInFlight: Promise<void> | null = null

/** POST /session/init — 后端初始化 FHSDK 并标记会话就绪 */
export async function ensureSessionInitialized(): Promise<void> {
  if (!initInFlight) {
    initInFlight = client.post<ApiResponse<void>>('/session/init').then(() => undefined).finally(() => {
      initInFlight = null
    })
  }
  return initInFlight
}

client.interceptors.response.use(
  (res) => res,
  async (error) => {
    const config = error.config as (typeof error.config & { __sessionRetried?: boolean }) | undefined
    if (
      axios.isAxiosError(error)
      && error.response?.status === 409
      && config
      && !config.__sessionRetried
      && config.url !== '/session/init'
    ) {
      const msg = (error.response.data as ApiResponse<unknown> | undefined)?.message ?? ''
      if (msg.includes('session not initialized')) {
        config.__sessionRetried = true
        await ensureSessionInitialized()
        return client.request(config)
      }
    }
    return Promise.reject(error)
  },
)

export function formatApiError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const msg = e.response?.data?.message
    if (typeof msg === 'string' && msg) return msg
    if (e.response?.status) return `HTTP ${e.response.status}`
  }
  if (e instanceof Error) return e.message
  return String(e)
}

export default client
```

### `src/api/index.ts`

只保留 demo 用到的端点：会话初始化、选/取消设备、Payload 列表/确认、开/关直播。

```ts
import client, { ensureSessionInitialized, type ApiResponse } from './client'

/**
 * 后端 REST API 封装（前缀 /api/v1）。
 * 所有请求依赖 Cookie 中的 HttpSession，须先 initSession()。
 */
export interface PayloadVo {
  payloadIndex: string
  deviceSn: string
  videoId: string
}

export interface PayloadListData {
  payloads: PayloadVo[]
  selectedPayloadIndex?: string | null
  configured: boolean
  defaultPayloadIndex?: string
}

export async function initSession() {
  await ensureSessionInitialized()
}

export async function selectDevice(gatewaySn: string) {
  await client.post('/devices/select', { gatewaySn })
}

export async function deselectDevice() {
  await client.delete('/devices/select')
}

export async function fetchPayloads() {
  const { data } = await client.get<ApiResponse<PayloadListData>>('/devices/payloads')
  return data.data!
}

export async function confirmPayload(payloadIndex: string) {
  const { data } = await client.post<ApiResponse<PayloadListData>>('/devices/payload', { payloadIndex })
  return data.data!
}

export async function startLive() {
  const { data } = await client.post<ApiResponse<{ whepUrl: string; streamKey: string; payloadIndex?: string }>>(
    '/live/start',
    {},
    { timeout: 60000 },
  )
  return data.data!
}

export async function stopLive() {
  await client.delete('/live/stop')
}
```

### `src/ws/telemetry.ts`

WebSocket 客户端：与 REST 同源走 Vite 代理，多处理函数共享一条连接。

```ts
export interface TelemetryMessage {
  uuid: string
  /** mqtt_osd | drc_camera | sei | payload_range */
  type: 'mqtt_osd' | 'drc_camera' | 'sei' | 'payload_range'
  ts: number
  gatewaySn: string
  /** mqtt_osd 子字段，如 location / attitude / gimbalAttitude */
  field?: string
  data: Record<string, unknown>
}

type Handler = (msg: TelemetryMessage) => void

let ws: WebSocket | null = null
const handlers = new Set<Handler>()

/** 与 REST 同源，走 Vite 代理 /api → :8080；须 withCredentials 以携带 JSESSIONID */
function wsUrl() {
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  return `${proto}://${location.host}/api/v1/ws/telemetry`
}

function attachHandlers(socket: WebSocket) {
  socket.onmessage = (ev) => {
    try {
      const msg = JSON.parse(ev.data) as TelemetryMessage
      handlers.forEach((h) => h(msg))
    } catch {
      /* ignore */
    }
  }
  socket.onclose = () => {
    if (ws === socket) ws = null
  }
}

export function connectTelemetry(onMessage: Handler): Promise<void> {
  handlers.add(onMessage)
  if (ws?.readyState === WebSocket.OPEN) {
    return Promise.resolve()
  }
  if (ws?.readyState === WebSocket.CONNECTING) {
    return new Promise((resolve, reject) => {
      ws!.addEventListener('open', () => resolve(), { once: true })
      ws!.addEventListener('error', () => reject(new Error('WebSocket 连接失败')), { once: true })
    })
  }
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(wsUrl())
    ws = socket
    attachHandlers(socket)
    socket.addEventListener('open', () => resolve(), { once: true })
    socket.addEventListener('error', () => reject(new Error('WebSocket 连接失败')), { once: true })
  })
}

export function disconnectTelemetry(onMessage?: Handler) {
  if (onMessage) handlers.delete(onMessage)
  if (handlers.size === 0 && ws) {
    ws.close()
    ws = null
  }
}

export function isWsOpen() {
  return ws?.readyState === WebSocket.OPEN
}
```

### `src/state.ts`

普通 `reactive` 状态（替代 Pinia），只保留地图/SEI 需要的字段，并把 WS 消息归约进来。

```ts
/**
 * 全局状态 —— 普通 reactive。地图模块（cesium/*）本就只吃数值，不依赖任何 store。
 */
import { reactive } from 'vue'
import type { TelemetryMessage } from '@/ws/telemetry'
import type { PayloadVo } from '@/api'

/** 遥测 OSD 状态（仅保留地图/SEI 可视化需要的字段） */
export const osd = reactive({
  /** 飞机位置（MQTT 慢通道，~0.5Hz） */
  location: { latitude: 0, longitude: 0, height: 0, elevation: 0 },
  /** 机场位置（SEI 相机偏移以此为 GND 原点） */
  dockLocation: { latitude: 0, longitude: 0, height: 0, heading: 0 },
  /** 飞机姿态 */
  attitude: { attitudeHead: 0, attitudePitch: 0, attitudeRoll: 0 },
  /** 直播 SEI：AR 相机姿态/位置 + FOV（开直播后才有） */
  sei: {} as Record<string, number>,
})

/** 会话/连接状态 */
export const session = reactive({
  sdkReady: false,
  wsConnected: false,
  liveStarted: false,
  gatewaySn: '' as string,
  payloads: [] as PayloadVo[],
  selectedPayloadIndex: null as string | null,
})

/** 处理一条遥测消息（只取地图需要的字段） */
export function handleTelemetry(msg: TelemetryMessage) {
  if (msg.type === 'mqtt_osd') {
    const d = msg.data
    switch (msg.field) {
      case 'location':
        Object.assign(osd.location, d)
        break
      case 'dockLocation':
        Object.assign(osd.dockLocation, d)
        break
      case 'attitude':
        Object.assign(osd.attitude, d)
        break
    }
  } else if (msg.type === 'sei') {
    Object.assign(osd.sei, msg.data)
  }
}

export function clearSei() {
  for (const k of Object.keys(osd.sei)) delete osd.sei[k]
}

export function resetOsd() {
  Object.assign(osd.location, { latitude: 0, longitude: 0, height: 0, elevation: 0 })
  Object.assign(osd.dockLocation, { latitude: 0, longitude: 0, height: 0, heading: 0 })
  Object.assign(osd.attitude, { attitudeHead: 0, attitudePitch: 0, attitudeRoll: 0 })
  clearSei()
}
```

> **SEI 字段约定**（由后端 `type=sei` 推送，单位见注释）：
> `cameraYaw/cameraPitch/cameraRoll`(度)、`cameraPosX/cameraPosY`(相对机场北/东偏移 mm)、
> `altitude`(相对起飞点高度 mm)、`fovHorizontal/fovVertical/curActualFocalLength`(μm)。

---

## 8. 第五步：几何工具

### `src/utils/seiNed.ts`

SEI 欧拉角（相机本体系，光轴=+Z）→ NED → ECEF 方向。是视锥姿态、光轴、三轴的共同基础。

```ts
/**
 * SEI 相机 NED 姿态 → ECEF 方向。
 * SEI 欧拉角描述的是相机本体系（光轴 = +Z）→ NED。
 */
import * as Cesium from 'cesium'

export type NedVec = [number, number, number]

const DEG2RAD = Math.PI / 180

/** R = Rz(yaw)·Ry(pitch)·Rx(roll)，角度为度，向量在 NED 下 */
export function rotateZYX(v: NedVec, yawDeg: number, pitchDeg: number, rollDeg: number): NedVec {
  const cy = Math.cos(yawDeg * DEG2RAD)
  const sy = Math.sin(yawDeg * DEG2RAD)
  const cp = Math.cos(pitchDeg * DEG2RAD)
  const sp = Math.sin(pitchDeg * DEG2RAD)
  const cr = Math.cos(rollDeg * DEG2RAD)
  const sr = Math.sin(rollDeg * DEG2RAD)
  const x1: NedVec = [v[0], cr * v[1] - sr * v[2], sr * v[1] + cr * v[2]]
  const x2: NedVec = [cp * x1[0] + sp * x1[2], x1[1], -sp * x1[0] + cp * x1[2]]
  return [cy * x2[0] - sy * x2[1], sy * x2[0] + cy * x2[1], x2[2]]
}

/** 画面中心光轴：相机本体系 +Z → NED */
export function opticalAxisNed(yawDeg: number, pitchDeg: number, rollDeg: number): NedVec {
  return rotateZYX([0, 0, 1], yawDeg, pitchDeg, rollDeg)
}

/** NED [北,东,地] → 该经纬度处 ECEF 单位方向 */
export function nedToEcefDirection(lon: number, lat: number, height: number, ned: NedVec): Cesium.Cartesian3 {
  const enu = new Cesium.Cartesian3(ned[1], ned[0], -ned[2])
  const rot = Cesium.Matrix4.getRotation(
    Cesium.Transforms.eastNorthUpToFixedFrame(Cesium.Cartesian3.fromDegrees(lon, lat, height)),
    new Cesium.Matrix3(),
  )
  const dir = Cesium.Matrix3.multiplyByVector(rot, enu, new Cesium.Cartesian3())
  return Cesium.Cartesian3.normalize(dir, dir)
}

export function nedAxisEndpoints(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
  lengths: { x: number; y: number; z: number },
): {
  origin: Cesium.Cartesian3
  endX: Cesium.Cartesian3
  endY: Cesium.Cartesian3
  endZ: Cesium.Cartesian3
} {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  // 红=光轴(+Z)，绿=Y，蓝=X（相机本体系 basis）
  const bodyAxes: NedVec[] = [[0, 0, 1], [0, 1, 0], [1, 0, 0]]
  const lens = [lengths.x, lengths.y, lengths.z]
  const ends = bodyAxes.map((axis, i) => {
    const ned = rotateZYX(axis, yawDeg, pitchDeg, rollDeg)
    const dir = nedToEcefDirection(lon, lat, height, ned)
    return Cesium.Cartesian3.add(
      origin,
      Cesium.Cartesian3.multiplyByScalar(dir, lens[i], new Cesium.Cartesian3()),
      new Cesium.Cartesian3(),
    )
  })
  return { origin, endX: ends[0]!, endY: ends[1]!, endZ: ends[2]! }
}
```

### `src/utils/seiCamera.ts`

把 SEI 的"相对机场偏移(mm)"换算成 WGS84 经纬高；无偏移时回退到飞机位置。

```ts
/**
 * SEI 相机 WGS84 坐标换算。
 * cameraPosX/Y 为相对机场 GND 原点的北/东偏移（毫米）；altitude 为相对起飞点高度（毫米）。
 */
const EARTH_R = 6371393

export interface Wgs84Position {
  lat: number
  lng: number
  height: number
}

export interface AircraftFallback {
  lat: number
  lng: number
  height: number
}

/** 以机场为 GND 原点，将 SEI 相机偏移换算为 WGS84；无 GND 偏移时回退到飞机位置 */
export function computeSeiCameraWgs84(
  dockLat: number,
  dockLng: number,
  dockHeight: number,
  cameraPosXmm: number | undefined,
  cameraPosYmm: number | undefined,
  altitudeMm: number | undefined,
  aircraft?: AircraftFallback,
): Wgs84Position | null {
  const hasGndOffset = Number.isFinite(cameraPosXmm) && Number.isFinite(cameraPosYmm)
  if (hasGndOffset && Number.isFinite(dockLat) && Number.isFinite(dockLng)) {
    const north = (cameraPosXmm as number) / 1000
    const east = (cameraPosYmm as number) / 1000
    const up = Number.isFinite(altitudeMm) ? (altitudeMm as number) / 1000 : 0
    const latRad = (dockLat * Math.PI) / 180
    const lat = dockLat + (north / EARTH_R) * (180 / Math.PI)
    const lng = dockLng + (east / (EARTH_R * Math.cos(latRad))) * (180 / Math.PI)
    const height = dockHeight + up
    return { lat, lng, height }
  }

  if (
    aircraft
    && Number.isFinite(aircraft.lat)
    && Number.isFinite(aircraft.lng)
    && (aircraft.lat !== 0 || aircraft.lng !== 0)
  ) {
    return { lat: aircraft.lat, lng: aircraft.lng, height: aircraft.height }
  }

  return null
}

/** 有 SEI 相机姿态即可绘制（位置可由 GND 偏移或飞机坐标回退） */
export function isSeiCameraReady(sei: Record<string, number>): boolean {
  if (!Object.keys(sei).length) return false
  return (
    Number.isFinite(sei.cameraYaw)
    && Number.isFinite(sei.cameraPitch)
    && Number.isFinite(sei.cameraRoll)
  )
}
```

### `src/utils/whep.ts`

WebRTC WHEP 拉流；开发环境把 `127.0.0.1:2022` 换成 `/oryx` 代理避免跨域。

```ts
/**
 * WHEP 拉流播放（Oryx SRS）。
 * 开发环境将 127.0.0.1:2022 替换为 Vite 代理 /oryx，避免浏览器 CORS。
 */
export function resolveWhepUrl(whepUrl: string): string {
  if (import.meta.env.DEV) {
    return whepUrl.replace(/^https?:\/\/127\.0\.0\.1:2022/, '/oryx')
  }
  return whepUrl
}

export async function playWhep(video: HTMLVideoElement, whepUrl: string) {
  const url = resolveWhepUrl(whepUrl)
  const pc = new RTCPeerConnection()
  pc.addTransceiver('video', { direction: 'recvonly' })
  pc.ontrack = async (ev) => {
    video.playsInline = true
    video.muted = true
    video.crossOrigin = 'anonymous'
    video.srcObject = ev.streams[0] ?? new MediaStream([ev.track])
    try {
      await video.play()
    } catch {
      /* autoplay policy */
    }
  }
  const offer = await pc.createOffer()
  await pc.setLocalDescription(offer)
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/sdp' },
    body: offer.sdp,
  })
  if (!res.ok) throw new Error(`WHEP failed: ${res.status}`)
  const answer = await res.text()
  await pc.setRemoteDescription({ type: 'answer', sdp: answer })
  return () => {
    pc.close()
    video.srcObject = null
  }
}
```

---

## 9. 第六步：地图渲染模块 `src/cesium/`

> 严格按以下顺序创建（依赖关系所致）。

### 9.1 `src/cesium/seiFrustum.ts`

SEI 姿态 → Cesium `FrustumGeometry` 的 orientation；SEI 物理量(μm) → 视场角(弧度)。

```ts
/**
 * SEI NED 相机 → Cesium FrustumGeometry 姿态与 FOV。
 * 相机光轴 = 本体系 +Z；Cesium FrustumGeometry 沿 rotation 第 3 列（局部 +Z）张开。
 */
import * as Cesium from 'cesium'
import { nedToEcefDirection, opticalAxisNed, rotateZYX } from '@/utils/seiNed'

const DEFAULT_FAR_M = 2000
const DEFAULT_VERTICAL_FOV_RAD = Cesium.Math.toRadians(45)

export interface SeiFovAngles {
  horizontalRad: number
  verticalRad: number
  aspectRatio: number
}

/** SEI 传感器物理量（μm）→ 水平/垂直视场角（弧度） */
export function seiFovFromSensor(
  fovHorizontalUm: number | undefined,
  fovVerticalUm: number | undefined,
  focalLengthUm: number | undefined,
): SeiFovAngles | null {
  if (!focalLengthUm || focalLengthUm <= 0) return null
  const hUm = fovHorizontalUm ?? 0
  const vUm = fovVerticalUm ?? 0
  if (hUm <= 0 || vUm <= 0) return null
  const horizontalRad = 2 * Math.atan(hUm / (2 * focalLengthUm))
  const verticalRad = 2 * Math.atan(vUm / (2 * focalLengthUm))
  if (horizontalRad <= 0 || verticalRad <= 0) return null
  const aspectRatio = Math.tan(horizontalRad * 0.5) / Math.tan(verticalRad * 0.5)
  return { horizontalRad, verticalRad, aspectRatio }
}

/** NED 相机三轴在 ECEF 下的单位方向（+X 右 / +Y 下 / +Z 光轴） */
export function nedCameraBodyAxesInEcef(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): { optical: Cesium.Cartesian3; right: Cesium.Cartesian3; down: Cesium.Cartesian3 } {
  return {
    optical: nedToEcefDirection(lon, lat, height, opticalAxisNed(yawDeg, pitchDeg, rollDeg)),
    right: nedToEcefDirection(lon, lat, height, rotateZYX([1, 0, 0], yawDeg, pitchDeg, rollDeg)),
    down: nedToEcefDirection(lon, lat, height, rotateZYX([0, 1, 0], yawDeg, pitchDeg, rollDeg)),
  }
}

/**
 * NED 相机姿态 → FrustumGeometry orientation。
 * 与 Cesium DebugCameraPrimitive 一致：col0=−right，col1=up(−down)，col2=direction(光轴)。
 */
export function nedCameraFrustumOrientation(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): Cesium.Quaternion {
  const { optical, right, down } = nedCameraBodyAxesInEcef(lon, lat, height, yawDeg, pitchDeg, rollDeg)

  const col0 = Cesium.Cartesian3.negate(right, new Cesium.Cartesian3())
  const col1 = Cesium.Cartesian3.negate(down, new Cesium.Cartesian3())
  const col2 = Cesium.Cartesian3.clone(optical, new Cesium.Cartesian3())

  const rot = new Cesium.Matrix3()
  Cesium.Matrix3.setColumn(rot, 0, col0, rot)
  Cesium.Matrix3.setColumn(rot, 1, col1, rot)
  Cesium.Matrix3.setColumn(rot, 2, col2, rot)
  return Cesium.Quaternion.fromRotationMatrix(rot)
}

export function buildPerspectiveFrustum(
  fov: SeiFovAngles | null,
  far = DEFAULT_FAR_M,
): Cesium.PerspectiveFrustum {
  const vertical = fov?.verticalRad ?? DEFAULT_VERTICAL_FOV_RAD
  const aspect = fov?.aspectRatio ?? 4 / 3
  return new Cesium.PerspectiveFrustum({
    fov: vertical,
    aspectRatio: aspect,
    near: 0.1,
    far,
  })
}

export { DEFAULT_FAR_M }
```

### 9.2 `src/cesium/seiRaycast.ts`

光轴/射线与地形或椭球求交。`terrainSurfaceHeight` 与 `pickRayGround` 被落地四边形复用。

```ts
/**
 * SEI 光轴与地面/地形求交（画面中心 = 光轴，NED rotateZYX）。
 */
import * as Cesium from 'cesium'
import { nedToEcefDirection, opticalAxisNed } from '@/utils/seiNed'

/** 光轴未命中地面时，射线可视长度（米） */
const OPTICAL_AXIS_FALLBACK_LENGTH_M = 800

export interface OpticalAxisHit {
  point: Cesium.Cartesian3
  lat: number
  lng: number
  height: number
}

/** 光轴单位方向（ECEF） */
export function opticalAxisDirection(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): Cesium.Cartesian3 {
  return nedToEcefDirection(lon, lat, height, opticalAxisNed(yawDeg, pitchDeg, rollDeg))
}

/** 光轴与椭球交点（无地形 pick，供射线实时刷新） */
export function opticalAxisEllipsoidEnd(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): Cesium.Cartesian3 {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const direction = opticalAxisDirection(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const ray = new Cesium.Ray(origin, direction)
  const interval = Cesium.IntersectionTests.rayEllipsoid(ray, Cesium.Ellipsoid.WGS84)
  if (interval && interval.start >= 0) {
    return Cesium.Ray.getPoint(ray, interval.start)
  }
  return Cesium.Cartesian3.add(
    origin,
    Cesium.Cartesian3.multiplyByScalar(direction, OPTICAL_AXIS_FALLBACK_LENGTH_M, new Cesium.Cartesian3()),
    new Cesium.Cartesian3(),
  )
}

/** 光轴椭球落点（实时，无 terrain pick） */
export function opticalAxisEllipsoidHit(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): OpticalAxisHit {
  const end = opticalAxisEllipsoidEnd(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const carto = Cesium.Cartographic.fromCartesian(end)
  return {
    point: end,
    lat: Cesium.Math.toDegrees(carto.latitude),
    lng: Cesium.Math.toDegrees(carto.longitude),
    height: carto.height,
  }
}

/** 射线与地形/椭球求交（取射线正方向上的首个交点） */
export function pickRayGround(viewer: Cesium.Viewer, ray: Cesium.Ray): Cesium.Cartesian3 | null {
  const hit = viewer.scene.globe.pick(ray, viewer.scene)
  if (hit) return hit

  const interval = Cesium.IntersectionTests.rayEllipsoid(ray, viewer.scene.globe.ellipsoid)
  if (!interval || interval.start < 0) return null
  return Cesium.Ray.getPoint(ray, interval.start)
}

/** 地形高度；无 Ion 地形时 getHeight 为 undefined，回退椭球面高度 */
export function terrainSurfaceHeight(viewer: Cesium.Viewer, carto: Cesium.Cartographic): number {
  const h = viewer.scene.globe.getHeight(
    new Cesium.Cartographic(carto.longitude, carto.latitude, 0),
  )
  if (h !== undefined) return h
  return carto.height
}

/** 从 SEI 相机位置沿光轴求地面交点（含 Ion 地形 pick，供落点标记） */
export function raycastOpticalAxisGround(
  viewer: Cesium.Viewer,
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): OpticalAxisHit | null {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const direction = opticalAxisDirection(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const ray = new Cesium.Ray(origin, direction)

  const hit = pickRayGround(viewer, ray)
  if (!hit) return null

  const carto = Cesium.Cartographic.fromCartesian(hit)
  return {
    point: hit,
    lat: Cesium.Math.toDegrees(carto.latitude),
    lng: Cesium.Math.toDegrees(carto.longitude),
    height: carto.height,
  }
}

/** 光轴线段：射线端点用椭球求交（快），落点可另做地形 pick */
export function opticalAxisSegment(
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
): { origin: Cesium.Cartesian3; end: Cesium.Cartesian3 } {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const end = opticalAxisEllipsoidEnd(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  return { origin, end }
}
```

### 9.3 `src/cesium/seiFrustumFootprint.ts`

视锥远平面四角 → 射线打地面 → 落地四边形。斜视时上沿角打不到地的局限见 §12.6。

```ts
/**
 * 视锥远平面四角 → 射线与地面/地形求交 → 贴地四边形顶点。
 * 远平面角点与 FrustumGeometry 共用 Cesium 内部 _computeNearFarPlanes，保证与视锥一致。
 */
import * as Cesium from 'cesium'
import {
  buildPerspectiveFrustum,
  DEFAULT_FAR_M,
  nedCameraFrustumOrientation,
  seiFovFromSensor,
  type SeiFovAngles,
} from '@/cesium/seiFrustum'
import { pickRayGround } from '@/cesium/seiRaycast'

const FRUSTUM_TYPE_PERSPECTIVE = 0

/** Cesium NDC 远平面顺序 BL,BR,TR,TL → 纹理顺序 TL,TR,BR,BL */
function reorderFarCorners(corners: Cesium.Cartesian3[]): Cesium.Cartesian3[] {
  return [corners[3]!, corners[2]!, corners[1]!, corners[0]!]
}

/** 与 DynamicFrustum 相同参数，读取 FrustumGeometry 远平面四角（世界坐标） */
export function frustumFarPlaneCornersCesium(
  origin: Cesium.Cartesian3,
  orientation: Cesium.Quaternion,
  fov: SeiFovAngles | null,
  far = DEFAULT_FAR_M,
): Cesium.Cartesian3[] {
  const frustum = buildPerspectiveFrustum(fov, far)
  const positions = new Float64Array(24)
  ;(
    Cesium.FrustumGeometry as unknown as {
      _computeNearFarPlanes: (
        origin: Cesium.Cartesian3,
        orientation: Cesium.Quaternion,
        frustumType: number,
        frustum: Cesium.PerspectiveFrustum,
        positions: Float64Array,
      ) => void
    }
  )._computeNearFarPlanes(origin, orientation, FRUSTUM_TYPE_PERSPECTIVE, frustum, positions)

  const raw: Cesium.Cartesian3[] = []
  for (let j = 0; j < 4; j++) {
    const i = 12 + j * 3
    raw.push(new Cesium.Cartesian3(positions[i]!, positions[i + 1]!, positions[i + 2]!))
  }
  return reorderFarCorners(raw)
}

function rayFromOriginThrough(
  origin: Cesium.Cartesian3,
  target: Cesium.Cartesian3,
): Cesium.Ray | null {
  const dir = Cesium.Cartesian3.subtract(target, origin, new Cesium.Cartesian3())
  if (Cesium.Cartesian3.magnitudeSquared(dir) < 1e-6) return null
  return new Cesium.Ray(origin, Cesium.Cartesian3.normalize(dir, dir))
}

function raycastCorner(
  viewer: Cesium.Viewer,
  origin: Cesium.Cartesian3,
  farCorner: Cesium.Cartesian3,
): Cesium.Cartesian3 | null {
  const ray = rayFromOriginThrough(origin, farCorner)
  if (!ray) return null
  return pickRayGround(viewer, ray)
}

/** 四角射线与地形/椭球求交；顺序 TL,TR,BR,BL */
export function computeFrustumGroundFootprint(
  viewer: Cesium.Viewer,
  lon: number,
  lat: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
  fovHorizontalUm?: number,
  fovVerticalUm?: number,
  focalLengthUm?: number,
  far = DEFAULT_FAR_M,
): Cesium.Cartesian3[] | null {
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const orientation = nedCameraFrustumOrientation(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  const fov = seiFovFromSensor(fovHorizontalUm, fovVerticalUm, focalLengthUm)
  const farCorners = frustumFarPlaneCornersCesium(origin, orientation, fov, far)

  const hits: Cesium.Cartesian3[] = []
  for (const corner of farCorners) {
    const hit = raycastCorner(viewer, origin, corner)
    if (!hit) return null
    hits.push(hit)
  }
  return hits.length === 4 ? hits : null
}

export type FrustumFootprintState = {
  origin: Cesium.Cartesian3
  orientation: Cesium.Quaternion
  fov: SeiFovAngles | null
  far?: number
}

/** 已有视锥状态时复用 orientation，避免重复计算 */
export function computeFrustumGroundFootprintFromState(
  viewer: Cesium.Viewer,
  state: FrustumFootprintState,
): Cesium.Cartesian3[] | null {
  const farCorners = frustumFarPlaneCornersCesium(
    state.origin,
    state.orientation,
    state.fov,
    state.far ?? DEFAULT_FAR_M,
  )
  const hits: Cesium.Cartesian3[] = []
  for (const corner of farCorners) {
    const hit = raycastCorner(viewer, state.origin, corner)
    if (!hit) return null
    hits.push(hit)
  }
  return hits.length === 4 ? hits : null
}
```

### 9.4 `src/cesium/dynamicPrimitives.ts`

高频更新的 Primitive 封装：折线、视锥（线框+填充）、闭合折线。端点变化才重建，避免每帧 GC。

```ts
/**
 * 高频折线/视锥：scene.primitives + 同步重建。
 */
import * as Cesium from 'cesium'
import { buildPerspectiveFrustum, type SeiFovAngles } from '@/cesium/seiFrustum'

function positionsEqual(a: Cesium.Cartesian3, b: Cesium.Cartesian3, eps = 0.01): boolean {
  return Cesium.Cartesian3.distanceSquared(a, b) < eps * eps
}

/** 端点变化时才重建 Primitive */
export class DynamicPolyline {
  private primitive: Cesium.Primitive | undefined
  private readonly start = new Cesium.Cartesian3()
  private readonly end = new Cesium.Cartesian3()
  private show = false
  private dirty = true

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly color: Cesium.Color,
    private readonly width: number,
  ) {}

  setEndpoints(start: Cesium.Cartesian3, end: Cesium.Cartesian3) {
    const moved = !positionsEqual(this.start, start) || !positionsEqual(this.end, end)
    Cesium.Cartesian3.clone(start, this.start)
    Cesium.Cartesian3.clone(end, this.end)
    if (!this.show) {
      this.show = true
      this.dirty = true
    } else if (moved) {
      this.dirty = true
    }
    this.sync()
  }

  hide() {
    if (!this.show && !this.primitive) return
    this.show = false
    this.dirty = true
    this.sync()
  }

  sync() {
    if (!this.show) {
      if (this.primitive) {
        this.collection.remove(this.primitive)
        this.primitive = undefined
      }
      return
    }
    if (!this.dirty && this.primitive) return

    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
    this.dirty = false

    this.primitive = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.PolylineGeometry({
            positions: [this.start, this.end],
            width: this.width,
            vertexFormat: Cesium.PolylineColorAppearance.VERTEX_FORMAT,
            arcType: Cesium.ArcType.NONE,
          }),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.color),
          },
        }),
        appearance: new Cesium.PolylineColorAppearance(),
        asynchronous: false,
      }),
    )
  }

  destroy() {
    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
  }
}

export interface FrustumState {
  origin: Cesium.Cartesian3
  orientation: Cesium.Quaternion
  fov: SeiFovAngles | null
  far?: number
}

/** 视锥线框 + 半透明填充 */
export class DynamicFrustum {
  private outline: Cesium.Primitive | undefined
  private fill: Cesium.Primitive | undefined
  private show = false
  private dirty = true
  private lastKey = ''

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly outlineColor: Cesium.Color,
    private readonly fillColor: Cesium.Color,
  ) {}

  update(state: FrustumState | null) {
    if (!state) {
      this.hide()
      return
    }
    const key = [
      state.origin.x, state.origin.y, state.origin.z,
      state.orientation.x, state.orientation.y, state.orientation.z, state.orientation.w,
      state.fov?.horizontalRad ?? 0,
      state.fov?.verticalRad ?? 0,
      state.far ?? 0,
    ].join(',')
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
    }
    this.show = true
    this.sync(state)
  }

  hide() {
    if (!this.show && !this.outline && !this.fill) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync(null)
  }

  private sync(state: FrustumState | null) {
    if (!this.show || !state) {
      if (this.outline) {
        this.collection.remove(this.outline)
        this.outline = undefined
      }
      if (this.fill) {
        this.collection.remove(this.fill)
        this.fill = undefined
      }
      return
    }
    if (!this.dirty && this.outline && this.fill) return

    if (this.outline) {
      this.collection.remove(this.outline)
      this.outline = undefined
    }
    if (this.fill) {
      this.collection.remove(this.fill)
      this.fill = undefined
    }
    this.dirty = false

    const geometryOptions = {
      frustum: buildPerspectiveFrustum(state.fov, state.far),
      origin: state.origin,
      orientation: state.orientation,
    }

    this.fill = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.FrustumGeometry(geometryOptions),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.fillColor),
          },
        }),
        appearance: new Cesium.PerInstanceColorAppearance({ flat: true }),
        asynchronous: false,
      }),
    )

    this.outline = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.FrustumOutlineGeometry(geometryOptions),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.outlineColor),
          },
        }),
        appearance: new Cesium.PerInstanceColorAppearance({ flat: true }),
        asynchronous: false,
      }),
    )
  }

  destroy() {
    if (this.outline) this.collection.remove(this.outline)
    if (this.fill) this.collection.remove(this.fill)
    this.outline = undefined
    this.fill = undefined
  }
}

/** 闭合折线（视锥地面交线） */
export class DynamicClosedPolyline {
  private primitive: Cesium.Primitive | undefined
  private readonly points: Cesium.Cartesian3[] = [
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
  ]
  private show = false
  private dirty = true
  private lastKey = ''

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly color: Cesium.Color,
    private readonly width: number,
  ) {}

  update(positions: Cesium.Cartesian3[] | null) {
    if (!positions || positions.length < 4) {
      this.hide()
      return
    }
    const key = positions.map((p) => `${p.x},${p.y},${p.z}`).join('|')
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
      for (let i = 0; i < 4; i++) {
        Cesium.Cartesian3.clone(positions[i]!, this.points[i]!)
      }
    }
    this.show = true
    this.sync()
  }

  hide() {
    if (!this.show && !this.primitive) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync()
  }

  private sync() {
    if (!this.show) {
      if (this.primitive) {
        this.collection.remove(this.primitive)
        this.primitive = undefined
      }
      return
    }
    if (!this.dirty && this.primitive) return

    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
    this.dirty = false

    const loop = [
      this.points[0]!,
      this.points[1]!,
      this.points[2]!,
      this.points[3]!,
      this.points[0]!,
    ]

    this.primitive = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.PolylineGeometry({
            positions: loop,
            width: this.width,
            vertexFormat: Cesium.PolylineColorAppearance.VERTEX_FORMAT,
            arcType: Cesium.ArcType.NONE,
          }),
          attributes: {
            color: Cesium.ColorGeometryInstanceAttribute.fromColor(this.color),
          },
        }),
        appearance: new Cesium.PolylineColorAppearance(),
        asynchronous: false,
      }),
    )
  }

  destroy() {
    if (this.primitive) {
      this.collection.remove(this.primitive)
      this.primitive = undefined
    }
  }
}
```

### 9.5 `src/cesium/groundVideoFootprint.ts`

把落地四边形细分成网格、逐顶点贴地，并用 `<video>` 作 `Image` 材质 → 实时画面贴地。

> 注：该文件含一处 Cesium `GeometryAttributes` 类型声明的历史告警（缺 normal/tangent），运行时无碍；`build` 用 esbuild 不做类型检查，故不报错。

```ts
/**
 * WHEP 直播贴地：细分网格 + terrainSurfaceHeight（仅地形/椭球，不采飞机模型）。
 */
import * as Cesium from 'cesium'
import { terrainSurfaceHeight } from '@/cesium/seiRaycast'

const GRID = 10
const HEIGHT_OFFSET_M = 0.2
const MIN_EDGE_M = 0.3

function bilinearCartographic(
  tl: Cesium.Cartographic,
  tr: Cesium.Cartographic,
  br: Cesium.Cartographic,
  bl: Cesium.Cartographic,
  u: number,
  v: number,
): Cesium.Cartographic {
  const w00 = (1 - u) * (1 - v)
  const w10 = u * (1 - v)
  const w11 = u * v
  const w01 = (1 - u) * v
  return new Cesium.Cartographic(
    w00 * tl.longitude + w10 * tr.longitude + w11 * br.longitude + w01 * bl.longitude,
    w00 * tl.latitude + w10 * tr.latitude + w11 * br.latitude + w01 * bl.latitude,
    w00 * tl.height + w10 * tr.height + w11 * br.height + w01 * bl.height,
  )
}

function footprintKey(corners: Cesium.Cartesian3[]): string {
  return corners.map((p) => `${p.x.toFixed(2)},${p.y.toFixed(2)},${p.z.toFixed(2)}`).join('|')
}

function isValidFootprint(corners: Cesium.Cartesian3[]): boolean {
  for (const c of corners) {
    if (!Number.isFinite(c.x) || !Number.isFinite(c.y) || !Number.isFinite(c.z)) return false
    if (Cesium.Cartesian3.magnitudeSquared(c) < 1e10) return false
  }
  for (let i = 0; i < 4; i++) {
    if (Cesium.Cartesian3.distance(corners[i]!, corners[(i + 1) % 4]!) < MIN_EDGE_M) return false
  }
  return true
}

function buildDrapedMesh(
  viewer: Cesium.Viewer,
  corners: Cesium.Cartesian3[],
): {
  positions: Float64Array
  sts: Float32Array
  indices: Uint16Array
} | null {
  const cartos = corners.map((c) => Cesium.Cartographic.fromCartesian(c))
  const [tl, tr, br, bl] = cartos as [
    Cesium.Cartographic,
    Cesium.Cartographic,
    Cesium.Cartographic,
    Cesium.Cartographic,
  ]

  const verts = GRID + 1
  const positions = new Float64Array(verts * verts * 3)
  const sts = new Float32Array(verts * verts * 2)

  let pi = 0
  let si = 0
  for (let row = 0; row < verts; row++) {
    const v = row / GRID
    for (let col = 0; col < verts; col++) {
      const u = col / GRID
      const carto = bilinearCartographic(tl, tr, br, bl, u, v)
      const h = terrainSurfaceHeight(viewer, carto) + HEIGHT_OFFSET_M
      if (!Number.isFinite(h)) return null
      const pos = Cesium.Cartesian3.fromRadians(carto.longitude, carto.latitude, h)
      if (!Number.isFinite(pos.x) || !Number.isFinite(pos.y) || !Number.isFinite(pos.z)) return null
      positions[pi++] = pos.x
      positions[pi++] = pos.y
      positions[pi++] = pos.z
      sts[si++] = u
      sts[si++] = 1 - v
    }
  }

  const indices: number[] = []
  for (let row = 0; row < GRID; row++) {
    for (let col = 0; col < GRID; col++) {
      const i = row * verts + col
      indices.push(i, i + 1, i + verts)
      indices.push(i + 1, i + verts + 1, i + verts)
    }
  }

  return { positions, sts, indices: new Uint16Array(indices) }
}

export class GroundVideoFootprint {
  private mesh: Cesium.Primitive | undefined
  private show = false
  private dirty = true
  private lastKey = ''
  private readonly corners = [
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
    new Cesium.Cartesian3(),
  ]
  private readonly material: Cesium.Material

  constructor(
    private readonly collection: Cesium.PrimitiveCollection,
    private readonly viewer: Cesium.Viewer,
  ) {
    const legacy = viewer.entities.getById('sei-footprint-video')
    if (legacy) viewer.entities.remove(legacy)

    const placeholder = document.createElement('canvas')
    placeholder.width = 2
    placeholder.height = 2
    const ctx = placeholder.getContext('2d')!
    ctx.fillStyle = '#000'
    ctx.fillRect(0, 0, 2, 2)
    this.material = Cesium.Material.fromType('Image', { image: placeholder })
  }

  bindVideo(video: HTMLVideoElement | null | undefined) {
    if (video) {
      this.material.uniforms.image = video
    }
  }

  update(
    _viewer: Cesium.Viewer,
    positions: Cesium.Cartesian3[] | null,
    video: HTMLVideoElement | null | undefined,
  ) {
    if (!positions || positions.length < 4) return

    this.bindVideo(video)
    const key = footprintKey(positions)
    if (key !== this.lastKey) {
      this.lastKey = key
      this.dirty = true
      for (let i = 0; i < 4; i++) {
        Cesium.Cartesian3.clone(positions[i]!, this.corners[i]!)
      }
    }
    this.show = true
    this.sync()
  }

  hide(_viewer: Cesium.Viewer) {
    if (!this.show && !this.mesh) return
    this.show = false
    this.dirty = true
    this.lastKey = ''
    this.sync()
  }

  private removeMesh() {
    if (this.mesh) {
      this.collection.remove(this.mesh)
      this.mesh = undefined
    }
  }

  private sync() {
    if (!this.show) {
      this.removeMesh()
      return
    }
    if (!this.dirty && this.mesh) return

    this.removeMesh()

    if (!isValidFootprint(this.corners)) {
      this.dirty = true
      return
    }

    const meshData = buildDrapedMesh(this.viewer, this.corners)
    if (!meshData) {
      this.dirty = true
      return
    }

    this.dirty = false
    const { positions, sts, indices } = meshData
    const boundingSphere = Cesium.BoundingSphere.fromVertices(positions)

    this.mesh = this.collection.add(
      new Cesium.Primitive({
        geometryInstances: new Cesium.GeometryInstance({
          geometry: new Cesium.Geometry({
            attributes: {
              position: new Cesium.GeometryAttribute({
                componentDatatype: Cesium.ComponentDatatype.DOUBLE,
                componentsPerAttribute: 3,
                values: positions,
              }),
              st: new Cesium.GeometryAttribute({
                componentDatatype: Cesium.ComponentDatatype.FLOAT,
                componentsPerAttribute: 2,
                values: sts,
              }),
            },
            indices,
            primitiveType: Cesium.PrimitiveType.TRIANGLES,
            boundingSphere,
          }),
        }),
        appearance: new Cesium.MaterialAppearance({
          material: this.material,
          flat: true,
          faceForward: true,
          translucent: false,
        }),
        asynchronous: false,
      }),
    )
  }

  destroy(_viewer: Cesium.Viewer) {
    this.removeMesh()
  }
}
```

### 9.6 `src/cesium/seiMapLayer.ts`

把上面的 Primitive 组装成一个图层：相机三轴、光轴、视锥、落地四边形、视频投影、测距连线的统一管理。

```ts
/**
 * SEI 相机三轴、光轴、视锥、测距连线 — Primitive 图层。
 */
import * as Cesium from 'cesium'
import { DynamicClosedPolyline, DynamicFrustum, DynamicPolyline, type FrustumState } from '@/cesium/dynamicPrimitives'
import { GroundVideoFootprint } from '@/cesium/groundVideoFootprint'
import {
  DEFAULT_FAR_M,
  nedCameraFrustumOrientation,
  seiFovFromSensor,
} from '@/cesium/seiFrustum'
import { computeFrustumGroundFootprintFromState, type FrustumFootprintState } from '@/cesium/seiFrustumFootprint'
import { opticalAxisEllipsoidHit, opticalAxisSegment } from '@/cesium/seiRaycast'
import { nedAxisEndpoints } from '@/utils/seiNed'

const CAMERA_AXIS_LENGTH = { x: 14, y: 10, z: 10 }

export class SeiMapLayer {
  private readonly collection: Cesium.PrimitiveCollection
  private readonly axisX: DynamicPolyline
  private readonly axisY: DynamicPolyline
  private readonly axisZ: DynamicPolyline
  private readonly seiRay: DynamicPolyline
  private readonly fastLine: DynamicPolyline
  private readonly seiLine: DynamicPolyline
  private readonly frustum: DynamicFrustum
  private readonly footprintOutline: DynamicClosedPolyline
  private readonly groundVideo: GroundVideoFootprint
  private liveVideo: HTMLVideoElement | null = null
  private lastFootprint: Cesium.Cartesian3[] | null = null
  private videoProjectionEnabled = false

  constructor(private readonly viewer: Cesium.Viewer) {
    this.collection = viewer.scene.primitives.add(new Cesium.PrimitiveCollection())
    this.axisX = new DynamicPolyline(this.collection, Cesium.Color.RED, 6)
    this.axisY = new DynamicPolyline(this.collection, Cesium.Color.LIME, 4)
    this.axisZ = new DynamicPolyline(this.collection, Cesium.Color.DODGERBLUE, 4)
    this.seiRay = new DynamicPolyline(this.collection, Cesium.Color.CYAN.withAlpha(0.75), 3)
    this.fastLine = new DynamicPolyline(this.collection, Cesium.Color.LIME.withAlpha(0.85), 3)
    this.seiLine = new DynamicPolyline(this.collection, Cesium.Color.ORANGE.withAlpha(0.85), 3)
    this.frustum = new DynamicFrustum(
      this.collection,
      Cesium.Color.CYAN.withAlpha(0.9),
      Cesium.Color.CYAN.withAlpha(0.12),
    )
    this.footprintOutline = new DynamicClosedPolyline(
      this.collection,
      Cesium.Color.YELLOW.withAlpha(0.95),
      2,
    )
    this.groundVideo = new GroundVideoFootprint(this.collection, this.viewer)
  }

  setVideoProjectionEnabled(enabled: boolean) {
    this.videoProjectionEnabled = enabled
    if (!enabled) {
      this.groundVideo.hide(this.viewer)
      return
    }
    if (this.lastFootprint && this.liveVideo) {
      this.groundVideo.update(this.viewer, this.lastFootprint, this.liveVideo)
    }
  }

  setLiveVideo(video: HTMLVideoElement | null) {
    this.liveVideo = video
    if (!video || !this.videoProjectionEnabled) {
      this.groundVideo.hide(this.viewer)
      return
    }
    this.groundVideo.bindVideo(video)
    if (this.lastFootprint) {
      this.groundVideo.update(this.viewer, this.lastFootprint, video)
    }
  }

  updateCameraAxes(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
  ) {
    const { origin, endX, endY, endZ } = nedAxisEndpoints(
      lon, lat, height, yawDeg, pitchDeg, rollDeg, CAMERA_AXIS_LENGTH,
    )
    this.axisX.setEndpoints(origin, endX)
    this.axisY.setEndpoints(origin, endY)
    this.axisZ.setEndpoints(origin, endZ)
  }

  hideCameraAxes() {
    this.axisX.hide()
    this.axisY.hide()
    this.axisZ.hide()
  }

  updateSeiOpticalAxis(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
  ) {
    const { origin, end } = opticalAxisSegment(lon, lat, height, yawDeg, pitchDeg, rollDeg)
    this.seiRay.setEndpoints(origin, end)
  }

  hideSeiOpticalAxis() {
    this.seiRay.hide()
    this.seiLine.hide()
  }

  updateMeasureLine(
    id: 'fast' | 'sei',
    from: Cesium.Cartesian3,
    to: Cesium.Cartesian3,
  ) {
    const line = id === 'fast' ? this.fastLine : this.seiLine
    line.setEndpoints(from, to)
  }

  hideMeasureLine(id: 'fast' | 'sei') {
    if (id === 'fast') this.fastLine.hide()
    else this.seiLine.hide()
  }

  updateFrustum(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
    fovHorizontalUm?: number,
    fovVerticalUm?: number,
    focalLengthUm?: number,
    far = DEFAULT_FAR_M,
  ) {
    const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)
    const orientation = nedCameraFrustumOrientation(lon, lat, height, yawDeg, pitchDeg, rollDeg)
    const fov = seiFovFromSensor(fovHorizontalUm, fovVerticalUm, focalLengthUm)
    const state: FrustumState = { origin, orientation, fov, far }
    this.frustum.update(state)
    this.updateFootprint(state)
  }

  private updateFootprint(state: FrustumFootprintState) {
    let footprint = computeFrustumGroundFootprintFromState(this.viewer, state)

    if (footprint) {
      this.lastFootprint = footprint.map((p) => Cesium.Cartesian3.clone(p))
    } else if (this.lastFootprint) {
      footprint = this.lastFootprint
    } else {
      this.footprintOutline.hide()
      this.hideGroundFootprint()
      return
    }

    this.footprintOutline.update(footprint)

    if (!this.videoProjectionEnabled) {
      this.groundVideo.hide(this.viewer)
      return
    }

    this.groundVideo.update(this.viewer, footprint, this.liveVideo)
  }

  hideFrustum() {
    this.frustum.hide()
    this.hideGroundFootprint()
  }

  hideGroundFootprint() {
    this.lastFootprint = null
    this.footprintOutline.hide()
    this.groundVideo.hide(this.viewer)
  }

  opticalAxisHit(
    lon: number,
    lat: number,
    height: number,
    yawDeg: number,
    pitchDeg: number,
    rollDeg: number,
  ) {
    return opticalAxisEllipsoidHit(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  }

  hideAllSei() {
    this.hideCameraAxes()
    this.hideSeiOpticalAxis()
    this.hideFrustum()
  }

  reset() {
    this.hideAllSei()
    this.fastLine.hide()
  }

  destroy() {
    this.axisX.destroy()
    this.axisY.destroy()
    this.axisZ.destroy()
    this.seiRay.destroy()
    this.fastLine.destroy()
    this.seiLine.destroy()
    this.frustum.destroy()
    this.footprintOutline.destroy()
    this.groundVideo.destroy(this.viewer)
    if (!this.collection.isDestroyed()) {
      this.viewer.scene.primitives.remove(this.collection)
    }
  }
}
```

### 9.7 `src/cesium/map.ts`

对外的薄封装：创建 Viewer、地形开关、飞机/机场 Entity、相机（含视锥）更新、视频投影开关。`SeiMapLayer` 单例随 viewer 懒创建。

```ts
/**
 * Cesium 地图层：机场/飞机/相机/激光测距等三维实体。
 * 高频折线与视锥走 Primitive（seiMapLayer），点与模型仍用 Entity。
 */
import * as Cesium from 'cesium'
import { SeiMapLayer } from '@/cesium/seiMapLayer'

const ionToken = import.meta.env.VITE_CESIUM_ION_TOKEN as string | undefined

const AIRCRAFT_MODEL_URI = `${import.meta.env.BASE_URL}models/Cesium_Air.glb`
const AIRCRAFT_MODEL_HEADING_OFFSET = Cesium.Math.toRadians(90 + 180)

let seiLayer: SeiMapLayer | undefined

function getSeiLayer(viewer: Cesium.Viewer): SeiMapLayer {
  if (!seiLayer) {
    seiLayer = new SeiMapLayer(viewer)
  }
  return seiLayer
}

function aircraftOrientation(
  position: Cesium.Cartesian3,
  heading: number,
  pitch: number,
  roll: number,
) {
  return Cesium.Transforms.headingPitchRollQuaternion(
    position,
    new Cesium.HeadingPitchRoll(
      Cesium.Math.toRadians(heading) + AIRCRAFT_MODEL_HEADING_OFFSET,
      Cesium.Math.toRadians(pitch),
      Cesium.Math.toRadians(roll),
    ),
  )
}

export function createViewer(container: HTMLElement) {
  if (ionToken) {
    Cesium.Ion.defaultAccessToken = ionToken
  }
  const viewer = new Cesium.Viewer(container, {
    animation: false,
    timeline: false,
    baseLayerPicker: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    navigationHelpButton: false,
    fullscreenButton: false,
    infoBox: false,
    selectionIndicator: false,
    requestRenderMode: false,
    useDefaultRenderLoop: true,
  })
  viewer.scene.globe.depthTestAgainstTerrain = true
  return viewer
}

export function hasIonTerrainToken(): boolean {
  return Boolean(ionToken?.trim())
}

export function setMapTerrainEnabled(viewer: Cesium.Viewer, enabled: boolean): boolean {
  if (enabled) {
    if (!ionToken?.trim()) return false
    Cesium.Ion.defaultAccessToken = ionToken
    viewer.scene.setTerrain(Cesium.Terrain.fromWorldTerrain())
    viewer.scene.globe.depthTestAgainstTerrain = true
    return true
  }
  viewer.scene.setTerrain(
    new Cesium.Terrain(Promise.resolve(new Cesium.EllipsoidTerrainProvider())),
  )
  return true
}

export function flyToLonLat(
  viewer: Cesium.Viewer,
  lon: number,
  lat: number,
  height = 100,
  viewOffset = 800,
) {
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(lon, lat, height + viewOffset),
    duration: 0.8,
  })
}

function setEntityPosition(entity: Cesium.Entity, position: Cesium.Cartesian3) {
  const prop = entity.position
  if (prop instanceof Cesium.ConstantPositionProperty) {
    prop.setValue(position)
  } else {
    entity.position = new Cesium.ConstantPositionProperty(position)
  }
}

export function updateDock(
  viewer: Cesium.Viewer,
  entity: Cesium.Entity | undefined,
  lat: number,
  lon: number,
  height: number,
): Cesium.Entity {
  const position = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  if (!entity) {
    return viewer.entities.add({
      name: 'dock',
      position,
      point: { pixelSize: 11, color: Cesium.Color.CYAN, outlineColor: Cesium.Color.WHITE, outlineWidth: 1 },
    })
  }
  setEntityPosition(entity, position)
  return entity
}

export function updateAircraft(
  viewer: Cesium.Viewer,
  entity: Cesium.Entity | undefined,
  lat: number,
  lon: number,
  height: number,
  heading: number,
  pitch: number,
  roll: number,
): Cesium.Entity {
  const position = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  const orientation = aircraftOrientation(position, heading, pitch, roll)

  if (entity && !entity.model) {
    viewer.entities.remove(entity)
    entity = undefined
  }

  if (!entity) {
    return viewer.entities.add({
      name: 'aircraft',
      position,
      orientation,
      model: {
        uri: AIRCRAFT_MODEL_URI,
        minimumPixelSize: 48,
        scale: 1.5,
        silhouetteColor: Cesium.Color.WHITE,
        silhouetteSize: 1,
      },
    })
  }
  setEntityPosition(entity, position)
  entity.orientation = new Cesium.ConstantProperty(orientation)
  return entity
}

export interface CameraMapEntities {
  origin: Cesium.Entity
}

export interface SeiCameraUpdate {
  lat: number
  lon: number
  height: number
  yawDeg: number
  pitchDeg: number
  rollDeg: number
  fovHorizontalUm?: number
  fovVerticalUm?: number
  focalLengthUm?: number
}

export function updateCamera(
  viewer: Cesium.Viewer,
  entities: CameraMapEntities | undefined,
  lat: number,
  lon: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
  sei?: Pick<SeiCameraUpdate, 'fovHorizontalUm' | 'fovVerticalUm' | 'focalLengthUm'>,
): CameraMapEntities {
  const layer = getSeiLayer(viewer)
  const origin = Cesium.Cartesian3.fromDegrees(lon, lat, height)

  layer.updateCameraAxes(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  layer.updateSeiOpticalAxis(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  layer.updateFrustum(
    lon, lat, height, yawDeg, pitchDeg, rollDeg,
    sei?.fovHorizontalUm, sei?.fovVerticalUm, sei?.focalLengthUm,
  )

  const hit = layer.opticalAxisHit(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  if (hit) {
    updateMeasurePoint(viewer, 'sei', hit.lat, hit.lng, hit.height, Cesium.Color.ORANGE, origin)
  } else {
    layer.hideMeasureLine('sei')
    removeMeasurePoint(viewer, 'sei')
  }

  if (!entities) {
    const originEntity = viewer.entities.add({
      name: 'camera-origin',
      position: origin,
      point: {
        pixelSize: 10,
        color: Cesium.Color.WHITE,
        outlineColor: Cesium.Color.MEDIUMPURPLE,
        outlineWidth: 2,
        disableDepthTestDistance: Number.POSITIVE_INFINITY,
      },
    })
    return { origin: originEntity }
  }

  setEntityPosition(entities.origin, origin)
  return entities
}

export function removeCamera(viewer: Cesium.Viewer, entities: CameraMapEntities | undefined) {
  getSeiLayer(viewer).hideAllSei()
  if (!entities) return undefined
  viewer.entities.remove(entities.origin)
  return undefined
}

export function removeSeiOpticalAxis(viewer: Cesium.Viewer) {
  const layer = getSeiLayer(viewer)
  layer.hideSeiOpticalAxis()
  layer.hideFrustum()
  removeMeasurePoint(viewer, 'sei')
}

export function updateSeiOpticalAxis(
  viewer: Cesium.Viewer,
  lat: number,
  lon: number,
  height: number,
  yawDeg: number,
  pitchDeg: number,
  rollDeg: number,
) {
  const layer = getSeiLayer(viewer)
  layer.updateSeiOpticalAxis(lon, lat, height, yawDeg, pitchDeg, rollDeg)

  const hit = layer.opticalAxisHit(lon, lat, height, yawDeg, pitchDeg, rollDeg)
  if (hit) {
    updateMeasurePoint(
      viewer,
      'sei',
      hit.lat,
      hit.lng,
      hit.height,
      Cesium.Color.ORANGE,
      Cesium.Cartesian3.fromDegrees(lon, lat, height),
    )
  } else {
    layer.hideMeasureLine('sei')
    removeMeasurePoint(viewer, 'sei')
  }
}

export function removeMeasurePoint(viewer: Cesium.Viewer, id: string) {
  const layer = getSeiLayer(viewer)
  if (id === 'fast') layer.hideMeasureLine('fast')
  if (id === 'sei') layer.hideMeasureLine('sei')
  const entity = viewer.entities.getById(id)
  if (entity) viewer.entities.remove(entity)
}

export function updateMeasurePoint(
  viewer: Cesium.Viewer,
  id: string,
  lat: number,
  lon: number,
  height: number,
  color: Cesium.Color,
  from?: Cesium.Cartesian3,
) {
  const pos = Cesium.Cartesian3.fromDegrees(lon, lat, height)
  let entity = viewer.entities.getById(id)
  if (!entity) {
    entity = viewer.entities.add({
      id,
      position: pos,
      point: { pixelSize: 10, color },
    })
  } else {
    setEntityPosition(entity, pos)
  }

  const layer = getSeiLayer(viewer)
  if (from && (id === 'fast' || id === 'sei')) {
    layer.updateMeasureLine(id, from, pos)
  } else if (id === 'fast' || id === 'sei') {
    layer.hideMeasureLine(id)
  }
}

/** 取消选设备时清空 Primitive 图层缓存 */
export function resetMapLineCache() {
  seiLayer?.reset()
}

export function destroySeiMapLayer() {
  seiLayer?.destroy()
  seiLayer = undefined
}

/** 绑定 WHEP 视频元素，用于视锥贴地四边形纹理（需同时开启视频投影） */
export function setMapLiveVideo(viewer: Cesium.Viewer, video: HTMLVideoElement | null) {
  getSeiLayer(viewer).setLiveVideo(video)
}

/** 地图视频投影开关（默认关；开启后才有 footprint 求交与贴地网格） */
export function setMapVideoProjectionEnabled(viewer: Cesium.Viewer, enabled: boolean) {
  getSeiLayer(viewer).setVideoProjectionEnabled(enabled)
}

/** 地图更新后请求重绘 */
export function requestMapRender(viewer: Cesium.Viewer | undefined) {
  viewer?.scene.requestRender()
}
```

---

## 10. 第七步：视图与样式

### `src/views/MapDemoView.vue`

页面：顶栏（状态徽章 + SN 输入 + 连接/开播/停止）、左侧直播、右侧地图（工具条 + 日志）。核心是 `updateMap()` 把 `state` 喂给 `cesium/map`。

```vue
<script setup lang="ts">
/**
 * FHSDK 地图 Demo —— 在地图上展示飞机 + SEI FOV 视锥可视化。
 */
import { computed, onMounted, onUnmounted, ref } from 'vue'
import * as Cesium from 'cesium'
import {
  initSession,
  selectDevice,
  deselectDevice,
  startLive,
  stopLive,
  fetchPayloads,
  confirmPayload,
} from '@/api'
import { formatApiError } from '@/api/client'
import { connectTelemetry, disconnectTelemetry } from '@/ws/telemetry'
import { osd, session, handleTelemetry, clearSei, resetOsd } from '@/state'
import { playWhep } from '@/utils/whep'
import {
  createViewer,
  updateAircraft,
  updateCamera,
  removeCamera,
  updateDock,
  removeSeiOpticalAxis,
  flyToLonLat,
  setMapTerrainEnabled,
  setMapLiveVideo,
  setMapVideoProjectionEnabled,
  hasIonTerrainToken,
  requestMapRender,
  resetMapLineCache,
  destroySeiMapLayer,
  type CameraMapEntities,
} from '@/cesium/map'
import { computeSeiCameraWgs84, isSeiCameraReady } from '@/utils/seiCamera'

// 默认设备（飞机 SN 由后端从机场解析，前端流程仅需机场 SN）
const DEFAULT_GATEWAY_SN = 'YOUR_GATEWAY_SN'
const DEFAULT_PAYLOAD = '98-0-0'

const snInput = ref(DEFAULT_GATEWAY_SN)
const busy = ref(false)
const mapRef = ref<HTMLDivElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
const terrainEnabled = ref(false)
const videoProjectionEnabled = ref(true)
const terrainAvailable = hasIonTerrainToken()

let viewer: Cesium.Viewer | undefined
let aircraftEntity: Cesium.Entity | undefined
let dockEntity: Cesium.Entity | undefined
let cameraEntities: CameraMapEntities | undefined
let stopWhep: (() => void) | undefined

const logs = ref<string[]>([])
function pushLog(msg: string) {
  logs.value.unshift(`[${new Date().toLocaleTimeString()}] ${msg}`)
  if (logs.value.length > 200) logs.value.length = 200
}

/** SEI 是否就绪（决定 FOV 视锥能否绘制） */
const seiReady = computed(() => session.liveStarted && isSeiCameraReady(osd.sei))

function onTelemetry(msg: Parameters<typeof handleTelemetry>[0]) {
  handleTelemetry(msg)
  if (msg.type === 'sei' || msg.type === 'mqtt_osd') scheduleMapUpdate()
}

// ---------- 连接流程 ----------
async function onConnect() {
  const sn = snInput.value.trim()
  if (!sn || busy.value) return
  busy.value = true
  try {
    await initSession()
    session.sdkReady = true
    await selectDevice(sn)
    session.gatewaySn = sn
    pushLog(`已选择机场 ${sn}`)

    await connectTelemetry(onTelemetry)
    session.wsConnected = true
    pushLog('WebSocket 已连接，等待 OSD…')

    const data = await fetchPayloads()
    session.payloads = data.payloads ?? []
    const hasDefault = !session.payloads.length || session.payloads.some((p) => p.payloadIndex === DEFAULT_PAYLOAD)
    session.selectedPayloadIndex =
      data.selectedPayloadIndex ||
      (hasDefault ? DEFAULT_PAYLOAD : null) ||
      data.defaultPayloadIndex ||
      data.payloads?.[0]?.payloadIndex ||
      null
    pushLog(`可用 Payload ${session.payloads.length} 项，已选 ${session.selectedPayloadIndex}，下一步：开启直播`)
  } catch (e) {
    pushLog(`连接失败: ${formatApiError(e)}`)
    await onStop()
  } finally {
    busy.value = false
  }
}

async function onStartLive() {
  if (!session.wsConnected || session.liveStarted || busy.value) return
  const payload = session.selectedPayloadIndex
  if (!payload) {
    pushLog('无可用 Payload')
    return
  }
  busy.value = true
  try {
    await confirmPayload(payload)
    pushLog(`Payload 已确认: ${payload}`)
    pushLog('正在开启直播（约数秒）…SEI 视锥将随之出现')
    const stream = await startLive()
    session.liveStarted = true
    pushLog(`直播已开启 payload=${stream.payloadIndex ?? payload}`)
    if (videoRef.value) {
      stopWhep?.()
      try {
        stopWhep = await playWhep(videoRef.value, stream.whepUrl)
        pushLog('WHEP 播放已建立')
        // 同一 <video> 既用于左侧显示，也作为地图视频投影的纹理源
        if (viewer) {
          setMapLiveVideo(viewer, videoRef.value)
          setMapVideoProjectionEnabled(viewer, videoProjectionEnabled.value)
        }
      } catch (e) {
        pushLog(`WHEP 播放失败（不影响地图 SEI 视锥）: ${formatApiError(e)}`)
      }
    }
    scheduleMapUpdate()
  } catch (e) {
    pushLog(`开启直播失败: ${formatApiError(e)}`)
  } finally {
    busy.value = false
  }
}

async function onStop() {
  try {
    if (session.liveStarted) {
      await stopLive().catch(() => {})
      session.liveStarted = false
    }
    stopWhep?.()
    stopWhep = undefined
    if (viewer) {
      setMapLiveVideo(viewer, null)
      setMapVideoProjectionEnabled(viewer, false)
    }
    if (videoRef.value) videoRef.value.srcObject = null
    if (session.wsConnected) {
      disconnectTelemetry(onTelemetry)
      session.wsConnected = false
    }
    if (session.gatewaySn) {
      await deselectDevice().catch(() => {})
      session.gatewaySn = ''
    }
    session.payloads = []
    session.selectedPayloadIndex = null
    resetOsd()
    clearMap()
    pushLog('已停止并清空')
  } catch (e) {
    pushLog(`停止失败: ${formatApiError(e)}`)
  }
}

// ---------- 地图 ----------
let mapUpdateScheduled = false
function scheduleMapUpdate() {
  if (mapUpdateScheduled) return
  mapUpdateScheduled = true
  requestAnimationFrame(() => {
    mapUpdateScheduled = false
    updateMap()
    requestMapRender(viewer)
  })
}

function hasMapCoord(lat: number, lng: number) {
  return Number.isFinite(lat) && Number.isFinite(lng) && (lat !== 0 || lng !== 0)
}

function updateMap() {
  if (!viewer || !session.gatewaySn) return
  const { latitude, longitude, height } = osd.location
  const { latitude: dockLat, longitude: dockLng, height: dockH } = osd.dockLocation

  // --- 机场标记（青点）---
  if (hasMapCoord(dockLat, dockLng)) {
    dockEntity = updateDock(viewer, dockEntity, dockLat, dockLng, dockH || 50)
  }

  // --- 飞机 glTF ---
  if (hasMapCoord(latitude, longitude)) {
    aircraftEntity = updateAircraft(
      viewer,
      aircraftEntity,
      latitude,
      longitude,
      height || 100,
      osd.attitude.attitudeHead,
      osd.attitude.attitudePitch,
      osd.attitude.attitudeRoll,
    )
  }

  // --- 核心：SEI 相机三轴 / 视锥 / 光轴落地点（需开直播 + SEI 姿态）---
  if (session.liveStarted && isSeiCameraReady(osd.sei)) {
    const pos = computeSeiCameraWgs84(
      dockLat,
      dockLng,
      dockH || 50,
      osd.sei.cameraPosX,
      osd.sei.cameraPosY,
      osd.sei.altitude,
      hasMapCoord(latitude, longitude)
        ? { lat: latitude, lng: longitude, height: height || 100 }
        : undefined,
    )
    if (pos) {
      cameraEntities = updateCamera(
        viewer,
        cameraEntities,
        pos.lat,
        pos.lng,
        pos.height,
        osd.sei.cameraYaw,
        osd.sei.cameraPitch,
        osd.sei.cameraRoll,
        {
          fovHorizontalUm: osd.sei.fovHorizontal,
          fovVerticalUm: osd.sei.fovVertical,
          focalLengthUm: osd.sei.curActualFocalLength,
        },
      )
    } else {
      cameraEntities = removeCamera(viewer, cameraEntities)
      removeSeiOpticalAxis(viewer)
    }
  } else {
    cameraEntities = removeCamera(viewer, cameraEntities)
    removeSeiOpticalAxis(viewer)
  }
}

function onCenterAircraft() {
  if (!viewer) return
  const { latitude, longitude, height } = osd.location
  if (!hasMapCoord(latitude, longitude)) return pushLog('飞机坐标不可用，请等待 OSD')
  flyToLonLat(viewer, longitude, latitude, height || 100)
}

function onCenterDock() {
  if (!viewer) return
  const { latitude, longitude, height } = osd.dockLocation
  if (!hasMapCoord(latitude, longitude)) return pushLog('机场坐标不可用，请等待 OSD')
  flyToLonLat(viewer, longitude, latitude, height || 50)
}

function onTerrainToggle() {
  if (!viewer) return
  if (!terrainAvailable) {
    terrainEnabled.value = false
    return pushLog('未配置 VITE_CESIUM_ION_TOKEN，无法加载 Ion 地形')
  }
  const ok = setMapTerrainEnabled(viewer, terrainEnabled.value)
  if (!ok) {
    terrainEnabled.value = false
    return pushLog('Ion 地形加载失败，请检查 Token')
  }
  pushLog(terrainEnabled.value ? '已开启 Ion 世界地形' : '已关闭地形（椭球）')
  updateMap()
  requestMapRender(viewer)
}

function onVideoProjectionToggle() {
  if (!viewer) return
  if (!session.liveStarted) {
    videoProjectionEnabled.value = false
    return pushLog('需先开启直播才能投影视频')
  }
  setMapVideoProjectionEnabled(viewer, videoProjectionEnabled.value)
  pushLog(videoProjectionEnabled.value ? '已开启视频投影（实时画面贴地）' : '已关闭视频投影')
  scheduleMapUpdate()
}

function clearMap() {
  if (!viewer) return
  resetMapLineCache()
  destroySeiMapLayer()
  viewer.entities.removeAll()
  aircraftEntity = undefined
  dockEntity = undefined
  cameraEntities = undefined
}

onMounted(() => {
  if (mapRef.value) viewer = createViewer(mapRef.value)
})

onUnmounted(() => {
  stopWhep?.()
  disconnectTelemetry(onTelemetry)
  destroySeiMapLayer()
  viewer?.destroy()
})
</script>

<template>
  <div class="map-demo">
    <header class="topbar">
      <h1>FHSDK 地图 · SEI FOV 可视化</h1>
      <span class="badge" :class="{ on: session.wsConnected }">WS · {{ session.wsConnected ? '已连接' : '未连接' }}</span>
      <span class="badge" :class="{ on: session.liveStarted }">直播 · {{ session.liveStarted ? '已开启' : '未开启' }}</span>
      <span class="badge" :class="{ on: seiReady }">SEI · {{ seiReady ? 'FOV 就绪' : '无' }}</span>

      <div class="conn">
        <input
          v-model="snInput"
          placeholder="机场 SN"
          :disabled="session.wsConnected || busy"
          @keyup.enter="onConnect"
        >
        <button v-if="!session.wsConnected" :disabled="!snInput.trim() || busy" @click="onConnect">连接</button>
        <template v-else>
          <select v-model="session.selectedPayloadIndex" :disabled="session.liveStarted" v-if="session.payloads.length">
            <option v-for="p in session.payloads" :key="p.payloadIndex" :value="p.payloadIndex">{{ p.payloadIndex }}</option>
          </select>
          <button
            class="primary"
            v-if="!session.liveStarted"
            :disabled="busy || !session.selectedPayloadIndex"
            @click="onStartLive"
          >开启直播</button>
          <button class="danger" @click="onStop">停止</button>
        </template>
      </div>
    </header>

    <div class="main">
      <!-- 左：直播 -->
      <section class="live-wrap">
        <video ref="videoRef" autoplay muted playsinline></video>
        <div class="live-empty" v-if="!session.liveStarted">未开启直播</div>
      </section>

      <!-- 右：地图 -->
      <section class="map-wrap">
        <div ref="mapRef" class="map"></div>
        <div class="map-toolbar">
          <label :class="{ disabled: !terrainAvailable }">
            <input v-model="terrainEnabled" type="checkbox" :disabled="!terrainAvailable" @change="onTerrainToggle">
            Ion 地形
          </label>
          <label :class="{ disabled: !session.liveStarted }">
            <input v-model="videoProjectionEnabled" type="checkbox" :disabled="!session.liveStarted" @change="onVideoProjectionToggle">
            视频投影
          </label>
          <button @click="onCenterAircraft">定位飞机</button>
          <button @click="onCenterDock">定位机场</button>
        </div>
        <div class="logs">
          <div v-for="(l, i) in logs" :key="i" class="log-line">{{ l }}</div>
        </div>
      </section>
    </div>
  </div>
</template>
```

### `src/styles/map-demo.scss`

```scss
/* FHSDK 地图 Demo —— 精简样式 */
:root {
  --bg: #0e1116;
  --panel: #161b22;
  --border: #2a3038;
  --text: #e6edf3;
  --muted: #8b949e;
  --accent: #2f81f7;
  --on: #2ea043;
  --danger: #da3633;
}

* { box-sizing: border-box; }
html, body, #app { height: 100%; margin: 0; }

body {
  background: var(--bg);
  color: var(--text);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 14px;
}

.map-demo { display: flex; flex-direction: column; height: 100%; }

.topbar {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 14px;
  background: var(--panel);
  border-bottom: 1px solid var(--border);
  h1 { font-size: 16px; margin: 0 8px 0 0; }
}

.badge {
  font-size: 12px; padding: 2px 8px; border-radius: 10px;
  background: #21262d; color: var(--muted); border: 1px solid var(--border);
  &.on {
    background: rgba(46, 160, 67, 0.18); color: #4ec46e; border-color: rgba(46, 160, 67, 0.4);
  }
}

.conn { margin-left: auto; display: flex; align-items: center; gap: 6px; }

input, select, button {
  font: inherit; color: var(--text);
  background: #21262d; border: 1px solid var(--border);
  border-radius: 6px; padding: 6px 10px;
}
input { width: 220px; }
button { cursor: pointer; &:hover:not(:disabled) { border-color: var(--accent); } }
input:disabled, select:disabled, button:disabled { opacity: 0.45; cursor: not-allowed; }
button.primary { background: var(--accent); border-color: var(--accent); color: #fff; }
button.danger { background: rgba(218, 54, 51, 0.16); border-color: rgba(218, 54, 51, 0.5); color: #ff7b72; }

.main { flex: 1; display: flex; min-height: 0; }

.live-wrap {
  position: relative; width: 42%; min-width: 320px; flex-shrink: 0;
  background: #000; border-right: 1px solid var(--border);
  video { width: 100%; height: 100%; object-fit: contain; background: #000; }
}
.live-empty {
  position: absolute; inset: 0; display: flex; align-items: center; justify-content: center;
  color: var(--muted); font-size: 13px; pointer-events: none;
}

.map-wrap { position: relative; flex: 1; min-width: 0; }
.map { position: absolute; inset: 0; }

.map-toolbar {
  position: absolute; top: 12px; right: 12px; z-index: 5;
  display: flex; gap: 8px; align-items: center;
  padding: 6px 10px;
  background: rgba(22, 27, 34, 0.85);
  border: 1px solid var(--border); border-radius: 8px;
  backdrop-filter: blur(4px);
  label {
    display: flex; align-items: center; gap: 4px; font-size: 12px; cursor: pointer;
    &.disabled { opacity: 0.5; cursor: not-allowed; }
  }
}

.logs {
  position: absolute; left: 12px; bottom: 12px; z-index: 5;
  width: 360px; max-height: 30%; overflow-y: auto;
  padding: 8px 10px;
  background: rgba(22, 27, 34, 0.8);
  border: 1px solid var(--border); border-radius: 8px;
  backdrop-filter: blur(4px);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 11px; line-height: 1.6; color: var(--muted);
}
.log-line { white-space: pre-wrap; word-break: break-all; }

/* 隐藏 Cesium 版权信息条 */
.cesium-widget-credits { display: none !important; }
```

---

## 11. 第八步：运行与验证

```bash
npm install
npm run dev          # http://localhost:5173
```

1. 顶栏填机场 SN（默认已填）→「连接」→ 日志出现"WebSocket 已连接"、"可用 Payload …"
2. 选 payload →「开启直播」→ 左侧出画面，日志"WHEP 播放已建立"
3. 右侧地图：飞机模型、机场青点、相机三轴 + 青色视锥 + 黄色落地四边形 + 贴地视频
4. 转动云台：视锥与黄色四边形、贴地视频应**一起旋转**
5. 顶栏 `SEI · FOV 就绪` 徽章亮 = 视锥在渲染

类型检查（可选）：`npx vue-tsc --noEmit`（会有 `groundVideoFootprint.ts` 一处 Cesium 类型告警，可忽略）。

---

## 12. 关键实现原理（重点，建议通读）

> 这一章解释"**为什么这么算**"。即使没接触过三维/测绘，也能跟下来。

### 12.0 先建立四个坐标系的概念 ⭐

整个 demo 的难点就一句话：**SEI 给的数据是"以飞机/相机自己为中心"的，而地图要的是"地球上的绝对位置和朝向"，中间要做一连串坐标转换。** 涉及四个坐标系：

```
①经纬度(WGS84)      ②ECEF 笛卡尔        ③ENU 局部站心     ④NED 机体/导航系
 lon/lat/height      X/Y/Z (米)          东/北/天          北/东/地
 "地球上哪一点"       "以地心为原点的       "站在某点，朝东/   "无人机视角：机头
                     三维直角坐标"         朝北/朝天"         前=北? 下=地"
```

- **① 经纬度 WGS84**：日常说的"东经 116°、北纬 40°、海拔 50m"。Cesium 里是 `Cartographic`。直观但不能直接做向量加减/旋转。
- **② ECEF（地心地固）**：把地球放进一个 XYZ 直角坐标系，原点在地心，单位米。Cesium 内部**一切三维运算都用它**（`Cartesian3`）。两点连线、射线求交、旋转都在这里做。
- **③ ENU（东-北-天，局部站心）**：站在地表某一点，建一个"东 x / 北 y / 天 z"的小直角坐标系。它是 ① 和 ② 之间的桥梁——Cesium 的 `Transforms.eastNorthUpToFixedFrame(点)` 给出"ENU → ECEF"的旋转矩阵。
- **④ NED（北-东-地）**：航空惯例的机体/导航系，"北 x / 东 y / 地 z"。**SEI 的姿态角就是在这个系里描述的。** 注意它和 ENU 只是轴的顺序和方向不同：`NED[北,东,地] ↔ ENU[东,北,天] = [N的第2位, N的第1位, -N的第3位]`（代码里 `new Cartesian3(ned[1], ned[0], -ned[2])` 就是干这个）。

**为什么要倒来倒去？** 因为：
- 传感器（SEI）天然用 NED 说"我朝北偏 30°、向下俯 45°"；
- 但 Cesium 渲染只认 ECEF；
- 而 NED→ECEF 没有现成函数，得借道 ENU（Cesium 有 ENU→ECEF）。

所以固定套路是：**NED 向量 →（换轴）→ ENU 向量 →（乘 eastNorthUpToFixedFrame 矩阵）→ ECEF 向量**。`utils/seiNed.ts` 的 `nedToEcefDirection` 就是这套路的封装，后面所有"朝向"计算都靠它。

---

### 12.1 SEI 到底给了什么数据

开直播后，后端逐帧推 `type=sei` 消息，关键字段（单位务必看清）：

| 字段 | 含义 | 单位/参考系 |
|---|---|---|
| `cameraYaw / cameraPitch / cameraRoll` | 相机**姿态**（朝哪） | 度，NED 机体欧拉角 |
| `cameraPosX / cameraPosY` | 相机**位置**相对机场的偏移 | 毫米，北/东 |
| `altitude` | 相机相对**起飞点**的高度 | 毫米，向上为正 |
| `fovHorizontal / fovVertical` | 传感器**靶面**水平/垂直尺寸 | 微米(μm) |
| `curActualFocalLength` | 当前**焦距** | 微米(μm) |

注意两件反直觉的事：
1. **位置是"相对机场的偏移"，不是经纬度**——因为 SEI 用机场停机坪作为本地原点(GND)。所以要先知道机场经纬度，再把偏移加上去，才得到相机的绝对经纬度（§12.2）。
2. **FOV 不是直接给角度，而是给"靶面尺寸 + 焦距"**——这是相机的物理参数，要用针孔相机模型换算成视场角（§12.4）。

---

### 12.2 SEI 偏移 → 经纬度（`seiCamera.ts`）

已知机场在 `(dockLat, dockLng, dockHeight)`，相机相对它向北 `north` 米、向东 `east` 米、向上 `up` 米，求相机经纬度。

地球近似成半径 `R` 的球，"沿地表走 d 米，纬度/经度变多少"是初中级几何：

```
纬度方向：每米对应的度数 = (1/R)·(180/π)
          → lat = dockLat + (north / R)·(180/π)

经度方向：同一纬度圈半径缩小为 R·cos(lat)，所以
          → lng = dockLng + (east / (R·cos(lat)))·(180/π)

高度：    height = dockHeight + up
```

代码里 `EARTH_R = 6371393`、`north = cameraPosX/1000`（mm→m）。这是"等距矩形近似"，几百米范围误差可忽略。**没有偏移数据时**（某些机型不给 GND 偏移），回退用飞机自身经纬度（相机就挂在机体上，差几十厘米无所谓）。

> 类比：你站在操场角落(机场)，说"往北走 50 米、往东走 30 米"，别人就能在地图上点出你的位置——前提是知道操场角落在哪。

---

### 12.3 相机"朝哪" → 三维朝向向量（`seiNed.ts` + `seiFrustum.ts`）

SEI 给的是欧拉角 `yaw/pitch/roll`。要把它变成 Cesium 能用的 ECEF 朝向，分两步：

**第一步：欧拉角 → NED 向量（`rotateZYX`）**

把相机想象成有一组自身的"本体轴"：光轴(看出去的方向)=+Z、右=+X、下=+Y。静止时这组轴和 NED 重合。相机转动 = 这组轴按 `R = Rz(yaw)·Ry(pitch)·Rx(roll)` 旋转（先 roll 再 pitch 最后 yaw，航空惯例顺序）。

所以"光轴现在朝哪"就是把本体 +Z 向量 `[0,0,1]` 过一遍这个旋转：`opticalAxisNed = rotateZYX([0,0,1], yaw,pitch,roll)`，得到一个 NED 向量。

```
        yaw(绕地轴转)        pitch(俯仰)        roll(翻滚)
  机头 ──►  转到某方位  ──►  抬头/低头  ──►  左右倾斜
  对应          Rz              Ry             Rx
```

**第二步：NED 向量 → ECEF 方向（`nedToEcefDirection`）**

就是 §12.0 那套路：NED 换轴成 ENU，再乘 `eastNorthUpToFixedFrame(相机位置)` 的旋转部分。结果是一个 ECEF 单位向量——"在地球直角坐标里，光轴指向哪"。三轴(红光轴/绿/蓝)、光轴落地线、视锥朝向，全用它算。

---

### 12.4 FOV：针孔相机模型（`seiFrustum.ts`）

相机能拍多宽的范围（视场角 FOV）由两个物理量决定：**靶面尺寸**和**焦距**。针孔模型下是一个简单的直角三角形：

```
        靶面(传感器)
        │←—— w ——→│
        │         │
        └────┬────┘
             │ f (焦距)
             │
             ●  针孔/镜头
            ╱ ╲
           ╱ θ ╲      tan(θ/2) = (w/2) / f
          ╱     ╲   ⟹ θ = 2·atan( w / (2f) )
         场景
```

代码：`horizontalRad = 2·atan(fovHorizontal / (2·focalLength))`，垂直同理。
变焦时焦距 `f` 变大 → θ 变小 → 视锥变窄（拍得更远更小），这也是为什么变焦时地图上的视锥会跟着收窄/张开。`aspectRatio` = 水平/垂直张角的 tan 之比，喂给 Cesium 的 `PerspectiveFrustum`（它只吃"垂直 FOV + 宽高比 + near/far"）。

---

### 12.5 视锥与 Cesium 的 orientation 约定（`seiFrustum.ts`）

有了相机位置(origin)、朝向、FOV，就能画一个**四棱锥(frustum)**：锥顶在相机，向光轴方向张开到 `far`(默认 2000m) 处的远平面。Cesium 用 `FrustumGeometry` 画，它需要一个四元数 `orientation` 来摆放。

Cesium 的约定（与它自带的 `DebugCameraPrimitive` 一致）：用一个 3×3 旋转矩阵，三列分别是
`列0 = −right`、`列1 = up(即 −down)`、`列2 = 光轴方向`。
我们把 §12.3 算出的 right/down/optical 三个 ECEF 向量按此摆进矩阵，再 `Quaternion.fromRotationMatrix` 转成四元数即可。**这就是 `nedCameraFrustumOrientation` 在做的事。**

> 不必死记这个列顺序——它是 Cesium 内部定义，照抄即可。重点是理解：orientation 完整编码了"相机怎么摆"，所以**只要 SEI 角度一变，视锥线框立刻跟着转**（线框不依赖地面，永远准）。

---

### 12.6 落地四边形（`seiFrustumFootprint.ts`）

视锥是空中的四棱锥，"它在地面圈出多大范围"才是用户关心的——这就是**落地四边形(footprint)**。算法：

1. 取视锥**远平面的四个角点**。这里用 Cesium 内部函数 `_computeNearFarPlanes`，保证四角和上面画的视锥线框**严丝合缝**（不会出现框和投影对不上）。重排成纹理顺序 `TL,TR,BR,BL`（左上→右上→右下→左下）。
2. 从相机原点**朝每个角点发一条射线**，求它和地面的交点（`pickRayGround`：先试地形，再退椭球面）。四个交点就是落地四边形。

```
       相机●
       ╱│╲          ← 四条射线（穿过远平面四角）
      ╱ │ ╲
  ───┌──┼──┐───── 地面
     │  ▼  │       ← 四个交点围成落地四边形
     └─────┘
```

> ⚠️ **已知限制（当前实现行为）**：当云台**斜视**（接近水平、画面上沿看到天空）时，视锥上面两个角的射线指向**地平线以上、打不到地面**，求交返回 null；当前实现"只要一个角失败就整块作废，退回上一帧 `lastFootprint`"，于是斜视时会出现**线框在转、但贴地四边形和视频冻住不动**。本指南按此行为编写。
>
> 如需修复：可在 `raycastCorner` 外再包一层"地平线裁剪"——对打不到地的角，取该射线的水平投影方向，沿地面截到某个最大距离（如 4000m）当落地点，使四角永远凑齐、斜视时四边形随云台旋转。**默认未启用。**

---

### 12.7 视频投影：把画面"贴"到地面（`groundVideoFootprint.ts`）

有了落地四边形，把直播画面当"贴纸"糊上去，就是视频投影。两个要点：

1. **细分网格 + 贴地**：四边形不是直接铺一张平面（地形有起伏会穿模），而是按 `GRID=10` 切成 10×10 小格(`bilinearCartographic` 双线性插值算每个格点的经纬度)，每个格点再用 `terrainSurfaceHeight` 拍到真实地面高度上（抬高 0.2m 防止和地面"z-fighting"闪烁）。
2. **纹理坐标(st)**：每个格点除了三维位置，还有一对 `(u,v)` 纹理坐标(0~1)，告诉 GPU"这个点该取视频画面的哪个像素"。材质用 `Material.fromType('Image', { image: <video元素> })`——直接把 HTML `<video>` 当贴图，Cesium **每帧自动重采样视频**，于是地面上是实时流动的画面。

```
 视频画面(0~1)         落地四边形(地面)
 (0,0)──(1,0)          TL─────TR
   │  img  │     映射    │  贴上  │
 (0,1)──(1,1)          BL─────BR
```

> 关键设计：左侧 `<video>` 和地图纹理**是同一个元素**。一个视频，既在左边正常播放，又被 GPU 当贴图采样，互不冲突。

**视频投影 = SEI(决定贴哪、什么形状) + 直播流(决定贴什么内容)**，缺一不可。

---

### 12.8 端到端：一帧 SEI 是怎么变成屏幕上的视锥的

把上面串起来，一条 SEI 消息到达后发生的事：

```
WS 收到 type=sei
   │ state.ts: Object.assign(osd.sei, data)   写入 reactive
   ▼
onTelemetry → scheduleMapUpdate()             用 rAF 合并，避免每条消息都重画
   ▼ (下一帧)
updateMap():
   ① computeSeiCameraWgs84  偏移(mm) → 相机经纬度      [§12.2]
   ② updateCamera → updateCameraAxes/OpticalAxis      三轴、光轴线   [§12.3]
   ③            → updateFrustum:
        seiFovFromSensor       靶面/焦距 → 视场角        [§12.4]
        nedCameraFrustumOrientation  姿态 → orientation  [§12.3/12.5]
        DynamicFrustum.update  画视锥线框+填充
        updateFootprint:
          四角射线求交 → 落地四边形                       [§12.6]
          footprintOutline 画黄框 / groundVideo 贴视频   [§12.7]
   ④ opticalAxisHit  光轴落地点(橙) + 连线
   ▼
requestMapRender → Cesium 渲染这一帧
```

记住三条主线就不会乱：
- **位置链**：机场经纬度 + SEI 偏移 → 相机经纬度（§12.2）
- **朝向链**：欧拉角 → NED → ECEF orientation（§12.3、12.5）
- **范围链**：朝向 + FOV → 视锥 → 射线求交 → 落地四边形 → 贴视频（§12.4、12.6、12.7）

---

## 13. 常见问题

| 现象 | 原因 / 处理 |
|---|---|
| 「Ion 地形」开关灰色 | 未配 `VITE_CESIUM_ION_TOKEN`。建 `.env.local` 填 token 并**重启 dev** |
| 改了 `.env.local` 没生效 | Vite 只在启动读 env，必须重启 |
| 左侧无画面 | Oryx `:2022` 未启动，或 WHEP 失败（看日志）；地图 SEI 视锥不受影响 |
| 只有视锥线框、无贴地视频 | 没开直播 / 视频投影关 / 该角度求交全失败 |
| 视锥转、贴地视频不转 | 斜视时视锥上沿角射线打不到地，footprint 退回上一帧（§12.6 已知限制，默认未修）。改正下方俯拍可正常旋转 |
| 飞机模型不显示 | 缺 `public/models/Cesium_Air.glb` |
| 视锥/飞机不出现 | 需先「开启直播」拿到 SEI；OSD 坐标为 0 时不绘制 |
| WS 报 409 / session not initialized | `client.ts` 拦截器会自动补 `/session/init` 重试；若持续失败查后端 |

---

## 附录：完整文件清单

```
fhsdk-map-demo/
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── .env.example                  # 复制为 .env.local 填 token（可选）
├── public/
│   └── models/Cesium_Air.glb     # 飞机 glTF（自备）
└── src/
    ├── main.ts
    ├── vite-env.d.ts
    ├── state.ts                  # reactive 状态 + WS 归约
    ├── styles/map-demo.scss
    ├── api/
    │   ├── client.ts             # axios 实例 + session 拦截
    │   └── index.ts              # REST 端点
    ├── ws/
    │   └── telemetry.ts          # WebSocket 客户端
    ├── utils/
    │   ├── seiNed.ts             # 姿态 NED→ECEF
    │   ├── seiCamera.ts          # SEI 偏移→WGS84
    │   └── whep.ts               # WHEP 拉流
    ├── cesium/
    │   ├── seiFrustum.ts         # 姿态/FOV→视锥
    │   ├── seiRaycast.ts         # 射线求交
    │   ├── seiFrustumFootprint.ts# 落地四边形
    │   ├── dynamicPrimitives.ts  # 折线/视锥 Primitive
    │   ├── groundVideoFootprint.ts# 视频贴地
    │   ├── seiMapLayer.ts        # SEI 图层装配
    │   └── map.ts                # 对外封装（Viewer/实体/开关）
    └── views/
        └── MapDemoView.vue       # 页面 + updateMap()
```
