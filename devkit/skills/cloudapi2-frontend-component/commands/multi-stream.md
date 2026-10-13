---
description: 生成DJI 上云API 2 直播大屏（DJILivePlayer 多路直播网格）的最小可运行 HTML
argument-hint: [serverUrl] [sn1,sn2,...] [gridCols]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/multi-stream.md`，输出一份**最小可运行**的多路直播网格 HTML。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `serverUrl` —— 后端地址（例 `http://<your-server>:30812`，不带 `/gateway`）
2. `sn1,sn2,...` —— 逗号分隔的设备序列号列表（每个 SN 创建一个播放器实例）
3. `gridCols` —— 网格列数（默认 `2`，即 2x2 布局）

未提供的参数：**在输出代码里用清晰占位符**（`http://[serverUrl]` / `[DEVICE_SN_1]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造 IP / SN / token**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **资源引入**：只需 `live-player-sdk/index.css` + `live-player-sdk/index.js`，**不需要 paas.js / Cesium / 其他 SDK**
3. **全局初始化**：`DJILivePlayer.initLivePlayer({ serverUrl, auth, locale })` —— 必须在创建任何播放器实例之前执行且仅执行一次
4. **鉴权**：
   - 初始化前 `localStorage.setItem('x-user-token', '[JWT_TOKEN]')`
   - `auth = { authKeys: ['X-User-Token'], headers: function() { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } } }`
5. **多实例网格**：
   - 每个设备 SN 创建一个独立容器 + `new DJILivePlayer.DjiLivePlayer({ container, sn })`
   - 用 CSS Grid 布局（`grid-template-columns: repeat(N, 1fr)`）
   - 容器必须有显式宽高
6. **事件监听**：每个实例至少监听 `ready`、`error`、`stateChange` 事件
7. **销毁示例**：附带 `beforeunload` 逐个调 `player.destroy()` 的代码
8. **错误码说明**：在代码注释中标注关键字符串错误码（`NOT_INITIALIZED` / `DEVICE_OFFLINE` / `TOKEN_EXPIRED`）

## 不要做

- 不要引入 paas.js —— DJILivePlayer 完全独立，不依赖 FH2 生态
- 不要调 `initConfig` / `loadMap` / `initNotifyCenter` —— 这些是驾驶舱的流程
- 不要在一个容器里放多个播放器实例 —— 每个实例独占一个 DOM 容器
- 不要用 `setTimeout` 轮询设备状态 —— SDK 内部通过 WebSocket 自动推送
- 不要编造设备 SN —— 用占位符 `[DEVICE_SN_1]`、`[DEVICE_SN_2]` 等
