---
description: 生成DJI 上云API 2 飞行记录回放（FlyDataReplay）的最小可运行 HTML
argument-hint: [serverUrl] [taskId]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/fly-data-replay.md`，输出一份**最小可运行**的飞行记录回放集成 HTML。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `serverUrl` —— 后端网关地址（例 `http://<your-server>:30812/gateway`，对应 `api.baseUrl`）
2. `taskId` —— 飞行任务 UUID

未提供的参数：**在输出代码里用清晰占位符**（`http://[serverUrl]` / `[taskId]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造地址 / ID**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **资源加载顺序硬要求**（自包含架构，**不需要 paas.js**）：
   - Cesium CSS + JS 必须**同步加载**（无 defer/async）
   - FlyDataReplay SDK 的 `index.css` + `index.js` 使用 `defer` 确保在 Cesium 之后执行
   ```html
   <link rel="stylesheet" href="https://[frontHost]/cesium/Widgets/widgets.css">
   <script src="https://[frontHost]/cesium/Cesium.js"></script>
   <link rel="stylesheet" href="https://[frontHost]/paasjs-demo/fly-data-sdk/index.css">
   <script defer src="https://[frontHost]/paasjs-demo/fly-data-sdk/index.js"></script>
   ```
3. **DOM 骨架与样式（按 `references/fly-data-replay.md`）**：
   ```html
   <div class="fh2-container">
     <div id="wayline-header"></div>
     <div class="project-details">
       <div id="project-middle-container"></div>
       <div id="project-right-micro-app" class="right-micro-app">
         <div class="maps-micro-app">
           <div id="project-map-app-placeholder" class="map-app-placeholder">
             <div id="map-app-global" class="map-app-container"></div>
           </div>
         </div>
         <div id="fly-data-app-container"></div>
       </div>
     </div>
   </div>
   ```
   - **`#project-middle-container` 与 `.right-micro-app` 必须相邻兄弟**
   - `.right-micro-app` → `.maps-micro-app` → `.map-app-placeholder` 三层不能扁平化
   - **必须输出必备 CSS**（flex 撑高、map-app-container min-height: 0 等）
4. **初始化顺序（自包含模式）**：
   - 在 `window.addEventListener('load', ...)` 中执行
   - 先检查 `window.Cesium` 和 `window.FlyDataReplay` 是否就绪
   - `new FlyDataReplay({ taskId, api: { baseUrl, auth }, map: { mode: 'internal' }, runtimeDepsBasePath, locale })`
   - `player.on('error', ...)` —— **建议在 loadMap 之前绑**
   - **`await player.loadMap()`** —— 始终必需
   - `player.mount(document.getElementById('fly-data-app-container'))`
   - `player.render({ taskId })`
5. **鉴权**：
   - `auth = { authKeys: ['X-User-Token'], headers: function() { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } } }`
6. **不要配 WS** —— 回放只走 HTTP，构造参数没有 `ws` 字段
7. **协议一致性提醒**：HTTPS 页面必须用 `https://`
8. **销毁示例**：`beforeunload` 或切 `taskId` 时调 `player.destroy(); player = null`
9. **错误处理**：`player.on('error', ...)`，至少处理 `5001 / 6002`
10. **常用事件**（按需取舍）：`'back'` / `'wayline-preview'` / `'device-selected'` / `'error'`

## 不要做

- 不要引入 `paas.js` —— FlyDataReplay 是自包含 SDK，不依赖 FH2 生态
- 不要调 `FH2.initConfig` / `FH2.loadMap` —— 那是驾驶舱/设备小窗的流程
- 不要省 DOM 嵌套 —— 回放组件对 DOM 结构强依赖
- 不要写 React/Vue 包装 —— vanilla JS / SDK 模式
- 不要把 `features` / `sidebarItems` / `renderTaskInfo` / `theme` 全塞进最小模板，除非用户明确要
- **不要跳过 `loadMap()`** —— FlyDataReplay 始终必需
- **不要在同一页面挂多个 FlyDataReplay 实例** —— `mapAppState` 是全局单例
- **不要用 `hostUrl`** —— 已废弃，使用 `runtimeDepsBasePath` + `map.env.cesiumBaseUrl`
