---
name: cloudapi2-frontend-component
description: Cloud API 2.0 前端组件（DJI 上云API 2 / paas.js / FH2）前端组件接入助手。当用户要在 skeleton/web（Vue 3）项目或独立 HTML 页面中接入 7 大前端组件——WaylineEditor（航线创建/编辑/拼接）、WaylineView（航线预览）、UranusDeviceManagerApp（设备管理）、FH2.loadCockpit（驾驶舱）、FlyDataReplay（飞行记录回放）、DJILivePlayer（直播大屏）、FH2.loadDeviceWindow（设备小窗）任一组件，或问 initConfig/auth/销毁/事件订阅/错误码/cockpitConfig/主题定制/跨域(Mixed Content)/Whistle 代理调试时调用。触发词：DJI 上云API 2、paas.js、FH2、WaylineEditor、WaylineView、FlyDataReplay、UranusDeviceManagerApp、DJILivePlayer、DjiLivePlayer、loadCockpit、loadDevice、loadDeviceWindow、loadWaylineView、loadFlightPath、initConfig、initLivePlayer、setupDeviceManager、mountDeviceList、concatWayline、cockpitConfig、setCockpitConfig、setCockpitTheme、setCockpitPreset、runtimeDepsBasePath、frontHost/serverHost/wssHost、useFH2、ensureFH2Init、CockpitView、useSDKLoader、航线编辑器、航线预览、航线规划、航线拼接、航点编辑、画航线、wayline editor、wayline view、route planner、航线页面、飞行路径编辑、驾驶舱页面、设备管理页面、飞行回放、直播大屏、设备小窗、多路直播、地图组件、嵌入地图、设备拓扑。
version: 2.0.1
author: DJI CloudAPI2 Team
---

# DJI 上云API 2 前端组件接入助手

## 这个 Skill 是干什么的

帮 ISV 开发者在 **skeleton/web（Vue 3 + Vite + TypeScript）** 项目或 **独立 HTML 页面** 中正确接入 DJI 上云API 2 **7 大组件**，并准确处理初始化、鉴权、销毁、跨域等关键细节。

## 7 大组件一览

| 组件 | SDK 全局名 | 是否依赖 paas.js | 用途 |
|------|-----------|-----------------|------|
| 航线编辑 | `window.WaylineEditor` | ❌ 自包含 | 航线创建 / 编辑 / 拼接 |
| 航线预览 | `window.WaylineView` | ❌ 自包含 | 航线只读预览（多航线同时渲染） |
| 设备管理 | `window.UranusDeviceManagerApp` | ❌ 自包含 | 设备列表、状态、管理 |
| 驾驶舱 | `window.FH2.loadCockpit` | ✅ 需要 paas.js | 实时视频直播 + 飞行控制 |
| 飞行记录 | `window.FlyDataReplay` | ❌ 自包含 | 飞行轨迹回放与分析 |
| 直播大屏 | `window.DJILivePlayer` | ❌ 完全独立 | 多路直播网格展示 |
| 设备小窗 | `window.FH2.loadDeviceWindow` | ✅ 需要 paas.js | 设备直播小窗浮层 |

## 何时触发

- 用户要"接入 / 集成 / 嵌入"DJI 上云API 2 / FH2 / paas.js 任一组件
- 用户提到 skeleton/web 中的 FH2 相关文件（`useFH2`、`ensureFH2Init`、`CockpitView`、`WaylineView`、`useSDKLoader`）
- 用户问 `WaylineEditor` / `WaylineView` / `FlyDataReplay` / `UranusDeviceManagerApp` / `DJILivePlayer` / `FH2.loadCockpit` / `FH2.loadDevice` / `FH2.loadDeviceWindow` 的用法
- 用户问 `initConfig` / `frontHost` / `serverHost` / `wssHost` / `paas.js` 加载
- 用户问 `cockpitConfig` / `setCockpitConfig` / 驾驶舱模块显隐
- 用户问 `setupDeviceManager` / `mountDeviceList` / 设备管理模块化挂载
- 用户问 `concatWayline` / 航线拼接
- 用户问 SDK 错误码（4001/4002/5000/5001/6000/6001/6002/6003/7000）
- 用户问主题定制 / `setTheme` / `setCockpitTheme` / 主题 token
- 用户问设备拓扑 / `showDeviceTopo` / `map.topo`
- 用户问跨域 / Mixed Content / Whistle 代理 / 本地调试

**不触发**：通用前端问题（React/Vue/Webpack 之类）、DJI Mobile SDK、其他非 FH2 的 SDK。

## 工作流

本 Skill 遵循 `skills/_shared/sop-template.md` 定义的 7 章节 SOP。
根据用户请求自动选择执行深度（L1/L2/L3）。

### 第 1 步：识别组件 + 判断场景

```
IF 用户在 skeleton/web 项目中工作 → Vue 集成模式
ELIF 用户需要独立 HTML 页面 → 独立 HTML 模式
ELSE → 先问清楚
```

**Vue 集成模式**：参考 `references/skeleton-integration.md`，使用现有 composable 模式。
**独立 HTML 模式**：参考 `snippets/standalone-html-template.html` + 各组件 reference，输出完整 HTML。

### 第 2 步：收集环境信息

弄清楚 3 个地址（缺一不可，自包含 SDK 除外）：

| 信息 | 例子 | 说明 |
|------|------|------|
| `frontHost` | `http://<your-server>` | 前端服务地址，能访问到 `/paas.js` 和 SDK 资源 |
| `serverHost` | `http://<your-server>:30812` | 后端接口地址（一般 frontHost + `:30812`） |
| `wssHost` | `ws://<your-server>:30812/...` | WebSocket 地址 |

**自包含 SDK**（WaylineEditor / WaylineView / FlyDataReplay / DeviceManager / DJILivePlayer）不需要 `paas.js`，但仍需 `serverHost`（api.baseUrl）和 `wssHost`（ws.url）。

如果用户没提供，先问。**不要编造 IP / 端口 / token**。

Vue 模式下这些通过 `.env` 环境变量配置（`VITE_FH2_ADDRESS` / `VITE_FH2_PORT`），参考 `useFH2Config.ts`。

### 第 3 步：读对应组件的 references

- 航线编辑（创建/编辑/拼接）→ `references/wayline-editor.md`
- 航线预览（只读渲染）→ `references/wayline-view.md`
- 设备管理 → `references/device-manager.md`
- 驾驶舱 → `references/cockpit.md`
- 飞行记录回放 → `references/fly-data-replay.md`
- 直播大屏 → `references/multi-stream.md`
- 设备小窗 → `references/device-window.md`
- 主题定制 → `references/theme.md`
- 设备拓扑（`showDeviceTopo` / `map.topo`）→ 参考当前使用组件的 reference（`wayline-editor.md` 第八节 / `wayline-view.md` / `fly-data-replay.md` 第十节）
- 7 组件初始化参数对照 → `references/api-overview.md`
- skeleton/web 集成架构 → `references/skeleton-integration.md`
- 排障 → `references/troubleshooting.md`

### 第 4 步：输出代码 + 标注必读约束

输出代码时必须附带以下提醒（constraints.yaml 中 `critical` 级别）：

1. **协议一致**：HTTPS 页面必须用 `https://` + `wss://`，HTTP 用 `http://` + `ws://`
2. **加载顺序**：依赖 paas.js 的组件（驾驶舱 / 设备小窗）必须 `paas.js` 先于 `initConfig`，`initConfig` 先于 `loadXxx`
3. **自包含 SDK 独立**：WaylineEditor / WaylineView / FlyDataReplay / DeviceManager / DJILivePlayer 不需要 `paas.js`，不调 `initConfig`
4. **容器尺寸**：DOM 容器必须有显式宽高
5. **销毁时机**：切页面/路由前必须 `destroy()` 旧实例
6. **DOM 结构不可修改**：依赖 paas.js 的组件需要特定 DOM ID 和类名结构
7. **单实例**：同页面不能挂多个 WaylineEditor 或 FlyDataReplay 实例（DJILivePlayer 除外，它就是多实例拼网格）
8. **hostUrl 已废弃**：WaylineEditor / FlyDataReplay 的 `hostUrl` 字段已废弃，应使用 `runtimeDepsBasePath` + `map.env.cesiumBaseUrl`
9. **Cesium 同步加载**：自包含 SDK（WaylineEditor / WaylineView / FlyDataReplay）必须先同步加载 Cesium.js

### 第 5 步：排查问题

报错或行为异常时查 `references/troubleshooting.md`（错误码表 + 常见坑 + Whistle 调试）。

## 核心约束（critical 级别，不可违反）

参见 `constraints.yaml`，所有 `level: critical` 的规则在生成代码时必须满足。

## Vue 集成模式要点

在 skeleton/web 中添加新 FH2 组件页面的标准步骤：

1. 在 `src/views/` 创建新 View 组件
2. 使用 `ensureFH2Init()` 确保 FH2 初始化（仅 paas.js 依赖组件）
3. 调用 `FH2.loadXxx(containerId, options)` 或 `new XxxSDK(options)` 挂载
4. 在 `onUnmounted` 中执行 `destroy()` + 取消事件订阅
5. 在 `src/router/` 注册路由

详见 `references/skeleton-integration.md`。

## 权威参考

`references/` 下的内容是DJI 上云API 2 7 大组件 ISV 接入的完整向导。
`snippets/` 下有 Vue 组件模板和独立 HTML 模板供参考。
`commands/` 下有 6 个快捷命令可直接调用。

> 本 Skill 的 constraints.yaml 仅包含前端领域特有规则；通用 SDK 约束见 `skills/_shared/constraints-common.yaml`（生成代码时需同时遵守）。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
