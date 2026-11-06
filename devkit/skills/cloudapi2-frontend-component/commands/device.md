---
description: 生成DJI Cloud API 2 设备管理（UranusDeviceManagerApp）的最小可运行 HTML
argument-hint: [frontHost] [serverUrl]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/device-manager.md`，输出一份**最小可运行**的设备管理集成 HTML。

> ⚠️ 设备管理**没有** `FH2.loadDevice` 这种 paas.js 集成模式——`window.FH2` 上不存在 `loadDevice`/`destroyDevice`。无论宿主页面是否已在用 `paas.js`，设备管理都走同一套独立 `UranusDeviceManagerApp` UMD SDK，只是存在新旧两个 API 版本（见下），不要生成假想的 `FH2.loadDevice` 调用。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `frontHost` —— 前端服务地址（例 `http://<your-server>`，用于加载 SDK 资源）
2. `serverUrl` —— 后端地址（例 `http://<your-server>:30812`，**不带 `/gateway`**，库内部自动追加）

未提供的参数：**在输出代码里用清晰占位符**（`http://[frontHost]` / `http://[serverUrl]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造 IP / token**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **资源加载**：引入 `paasjs-demo/device-manager-sdk/index.css` + `index.js`，加载完成后 SDK 挂载在 `window.UranusDeviceManagerApp`
3. **DOM**：`<div id="uranus-device-manager-app">`，容器有显式宽高（`width:100%; height:100vh`）
4. **自动检测新旧 API**（与 skeleton/web 的 `DeviceManagerView.vue` 一致的写法，不要只实现一个分支）：
   - 若 `window.UranusDeviceManagerApp.setupDeviceManager` 存在 → 走新版两层架构：`await setupDeviceManager(conf)` → `mountDeviceList({ container, props: { deviceType: 'airport' } })`
   - 否则 → 走旧版整包：`init({ containerId: '#uranus-device-manager-app', serverUrl, wsUrl, auth, ... })`（**两版都支持** `antThemeBaseUrl`，不要省略）
5. **鉴权**：
   - 初始化之前必须 `localStorage.setItem('x-user-token', '[JWT_TOKEN]')`
   - 用 `auth = { authKeys: ['X-User-Token'], headers: function() { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } } }` 闭包形式
   - `wsUrl` 传**推送中心地址**（`ws://host:30812/openapi/v0.9/notification/ws/v1/notifications`）
   - 字段以 `references/device-manager.md` 1.4 / 1.8 字段表为准，不要凭印象加字段
6. **`mountDeviceList` 必须传 `props: { deviceType: 'airport' }`**（1.7 强调）——不传时设备列表为空
7. **容器选择器必须带 `#`**（1.7 强调），不要写成裸 id
8. **协议一致性提醒**：HTTPS 页面必须用 `https://` + `wss://`
9. **错误处理**：`callbacks.onError` 至少给一个 `console.error`；`setupDeviceManager` 用 `try/catch`，`init` 用 `.catch()`

## 不要做

- **不要生成 `FH2.loadDevice` / `window.FH2.destroyDevice`** —— 这两个方法不存在，设备管理不依赖 `paas.js`、不调 `FH2.initConfig`
- 不要只写新版或只写旧版其中一个分支 —— 按运行时探测自动选择，兼容不同批次的 SDK 构建
- 不要把 JWT 写死在 HTML 里 —— 用占位符 + `localStorage.setItem` 提示由应用层注入
- 新版分支不要调 `destroy()` —— 用各模块 `mountXxx()` 返回的 `handle.unmount()`；旧版 `init()` 没有显式 destroy，重新 `init()` 即可
- 不要超出 `references/device-manager.md` 字段表 —— 多余字段会被 SDK 忽略，徒增使用方困惑
