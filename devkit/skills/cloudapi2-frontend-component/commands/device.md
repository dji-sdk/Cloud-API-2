---
description: 生成DJI 上云API 2 设备管理（UranusDeviceManagerApp / FH2.loadDevice）的最小可运行 HTML
argument-hint: [mode: sdk|paas] [frontHost] [serverUrl]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/device-manager.md`，输出一份**最小可运行**的设备管理集成 HTML。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `mode` —— `sdk`（独立 SDK，推荐独立页面） / `paas`（paas.js 模式，复用驾驶舱配置），未指定默认 `sdk`
2. `frontHost` —— 前端服务地址（例 `http://<your-server>`，用于加载 SDK 资源）
3. `serverUrl` —— 后端地址（例 `http://<your-server>:30812`，**SDK 模式不带 `/gateway`**；paas 模式按 `snippets.md` 第 4 节传 `serverHost`）

未提供的参数：**在输出代码里用清晰占位符**（`http://[frontHost]` / `http://[serverUrl]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造 IP / token**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **按模式区分资源 / DOM / 调用**：
   - `sdk` 模式：引入 `paasjs-demo/device-manager-sdk/index.css` + `index.js`，DOM 用 `#uranus-device-manager-app`，调用 `window.UranusDeviceManagerApp.init({ containerId: '#uranus-device-manager-app', serverUrl, wsUrl, auth, ... })`
   - `paas` 模式：引入 `paas.js` + `FH2.initConfig`，DOM 用 `#device-manager-app-container` + `#map-app-global`，调用 `window.FH2.loadDevice('device-manager-app-container', { deviceType: 'airport' })`
3. **SDK 模式鉴权**：
   - `init` 之前必须 `localStorage.setItem('x-user-token', '[JWT_TOKEN]')`
   - 用 `auth = { authKeys: ['X-User-Token'], headers: function() { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } } }` 闭包形式
   - `wsUrl` 传**推送中心地址**（`ws://host:30812/openapi/v0.9/notification/ws/v1/notifications`）
   - 字段以 `references/device-manager.md` 1.3 表为准，不要凭印象加字段
4. **`containerId` 必须带 `#`**（按 1.5 强调），不要写成裸 id
5. **容器有显式宽高**（`width:100%; height:100%`），父级也要给高度
6. **协议一致性提醒**：HTTPS 页面必须用 `https://` + `wss://`
7. **错误处理**：`callbacks.onError` 至少给一个 `console.error`；paas 模式附 `window.FH2.destroyDevice()` 销毁示例

## 不要做

- 不要混用两种模式 —— 一份 HTML 只走一条路径
- 不要把 JWT 写死在 HTML 里 —— 用占位符 + `localStorage.setItem` 提示由应用层注入
- 不要给 SDK 模式调 `destroy` —— 没有该方法（按 1.5），重新 `init` 即可
- 不要超出 `references/device-manager.md` 1.3 字段表 —— 多余字段会被 SDK 忽略，徒增使用方困惑
