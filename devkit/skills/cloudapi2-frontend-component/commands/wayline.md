---
description: 生成DJI 上云API 2 航线编辑器（WaylineEditor）或航线预览（WaylineView / FH2.loadWaylineView）的最小可运行 HTML
argument-hint: [mode: create|edit|preview] [serverUrl] [waylineId?]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/wayline-editor.md`（create/edit）或 `references/wayline-view.md`（preview），输出一份**最小可运行**的航线集成 HTML。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `mode` —— `create` / `edit` / `preview`
2. `serverUrl` —— 后端网关地址（例 `http://<your-server>:30812/gateway`，对应 `api.baseUrl`）
3. `waylineId` —— `edit` / `preview` 模式必填的航线 ID

## 第一步：确认模式（强制）

航线有 **3 种用法**，对应的 SDK / DOM / 初始化流程完全不同，**不能默认猜测**。

- 如果 `$ARGUMENTS` 里第一个参数已经是 `create` / `edit` / `preview` 之一 → 直接采用，跳过询问
- 否则**必须先用 `AskUserQuestion` 工具**问用户要哪种模式，再继续生成代码：
  - `create` —— 创建新航线（`WaylineEditor` 自包含 SDK）
  - `edit` —— 编辑已有航线（`WaylineEditor` 自包含 SDK，需要 `waylineId`）
  - `preview` —— 只预览航线（`WaylineView` 自包含 SDK，需要 `waylineId`）

用户选完之后，如果选的是 `edit` / `preview` 但参数里没给 `waylineId`，**在输出代码里用 `[waylineId]` 占位**并在末尾提示替换，不要再追问。

其余未提供的参数：**在输出代码里用清晰占位符**（`http://[serverUrl]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造地址 / ID / token**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **资源加载（三种模式均为自包含，不需要 paas.js）**：
   - Cesium CSS + JS 同步加载（无 defer/async）
   - SDK JS 使用 `defer`（确保 Cesium 先就绪）
   - `create` / `edit`：加载 `wayline-edit-sdk/index.css` + `index.js`
   - `preview`：加载 `wayline-view-sdk/index.css` + `index.js`
   ```html
   <!-- create / edit 模式 -->
   <link rel="stylesheet" href="https://[frontHost]/cesium/Widgets/widgets.css">
   <script src="https://[frontHost]/cesium/Cesium.js"></script>
   <link rel="stylesheet" href="https://[frontHost]/paasjs-demo/wayline-edit-sdk/index.css">
   <script defer src="https://[frontHost]/paasjs-demo/wayline-edit-sdk/index.js"></script>
   ```
3. **DOM 骨架与样式**：
   - `create`：仅需 `mount()` 目标容器 + `#wayline-header` + `#map-app-global`
   - `edit`：**完整骨架按 `references/wayline-editor.md`** —— `#wayline-header` / `#project-middle-container` / `.right-micro-app` / `.maps-micro-app` / `.map-app-placeholder` / `#map-app-global` / 挂载容器
   - `preview`：**按 `references/wayline-view.md`** —— `#map-app-global` / `#wayline-view-container`
   - **`#project-middle-container` 与 `.right-micro-app` 必须相邻兄弟**（edit 模式）
   - 必须输出必备 CSS（flex 撑高、min-height: 0 等）
4. **初始化顺序（自包含模式，在 `window.addEventListener('load', ...)` 中）**：
   - 先检查 `window.Cesium` 和 SDK 全局对象是否就绪
   - `create`：`new WaylineEditor(opts)` → `loadMap()` → `mount()` → `editor.create({ centerPoint? })`
   - `edit`：`new WaylineEditor(opts)` → `loadMap()` → `mount()` → `editor.render('edit', { waylineId })`
   - `preview`：`new WaylineView(opts)` → `loadMap()` → `mount()` → `view.render({ waylineIds: [waylineId] })`
5. **构造参数**：
   - `api: { baseUrl, auth }`（必填）
   - `map: { mode: 'internal' }`（使用内置 Cesium）
   - `runtimeDepsBasePath`（WPMZ/PPE WASM 资源根路径）
   - **不要传 `hostUrl`** —— 已废弃
6. **`AuthConfig` 用真实结构**：`authKeys` + `headers: () => ({...})`（推荐函数形式）。**不要传 `ws.auth`** —— SDK 自动复用 `api.auth`
7. **协议一致性提醒**：HTTPS 页面必须用 `https://` + `wss://`
8. **销毁示例**：`beforeunload` 调 `editor.destroy()` 或 `view.destroy()`
9. **错误处理**：`instance.on('error', ...)`，至少处理 `5001 / 6002`
10. **常用事件**（按模式取舍）：
    - `create`：`.create().catch(err => { if (err.message === 'User cancelled') return })`
    - `edit`：监听 `'wayline-save'` / `'wayline-cancel'` / `'wayline-back'`
    - `preview`：监听 `'wayline-rendered'` / `'ready'`

## 不要做

- **不要引入 `paas.js`** —— WaylineEditor 和 WaylineView 都是自包含 SDK，不依赖 FH2 生态
- 不要调 `FH2.initConfig` / `FH2.loadMap` / `FH2.loadWaylineView` —— 那是旧 API，已被独立 WaylineView SDK 替代
- 不要写 React/Vue 包装 —— vanilla JS / SDK 模式
- 不要为 `create()` 后跟 `render('edit')` 都新建实例 —— 同一 `editor` 实例复用
- 不要把 `enabledWaylineTypes` / `supportedDrones` / `theme` 全塞进最小模板，除非用户明确要
- **不要在同一页面挂多个 WaylineEditor 实例** —— `mapAppState` 是全局单例
- **不要用 `hostUrl`** —— 已废弃，使用 `runtimeDepsBasePath` + `map.env.cesiumBaseUrl`
