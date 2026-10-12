---
description: 生成DJI 上云API 2 驾驶舱（FH2.loadCockpit）的最小可运行 HTML
argument-hint: [frontHost] [gatewaySn] [droneSn]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/cockpit.md`，输出一份**最小可运行**的驾驶舱集成 HTML。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `frontHost` —— 前端服务地址（例 `http://<your-server>`，能访问到 `/paas.js`）
2. `gatewaySn` —— 基站序列号
3. `droneSn` —— 无人机序列号

未提供的参数：**在输出代码里用清晰占位符**（`http://[frontHost]` / `[gatewaySn]` / `[droneSn]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造 IP / SN / token**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **DOM 结构严格按** `references/cockpit.md` 第二节（不能简化，否则视频/地图错位）
3. **CSS 包含** qiankun 微应用容器高度修复（第三节）
4. **鉴权（必须）**：调用 `loadMap` / `initNotifyCenter` 之前先 `localStorage.setItem('x-user-token', '[JWT_TOKEN]')`，并构造 `auth = { authKeys: ['X-User-Token'], headers: function() { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } } }`，把 `auth` 传给 `FH2.initNotifyCenter(wssUrl, auth)` 与 `FH2.loadMap(serverUrl, auth)` —— FH2 内部缓存为 `_defaultAuth`，自动透传给驾驶舱 / project-app / packages request 单例，调用方无需再为单个接口配鉴权
5. **加载顺序**：`paas.js` → `initConfig` → `initNotifyCenter(wssUrl, auth)` → `loadMap(serverUrl, auth)` → `loadCockpit`
6. **协议一致性提醒**：HTTPS 页面必须用 `https://` + `wss://`
7. **销毁示例**：附带 `beforeunload` 调 `destroyCockpit()` 的代码

## 不要做

- 不要把 SDK 参数细节再讲一遍 —— 用户要的是能跑的代码，不是文档
- 不要写 React/Vue 包装 —— DJI 上云API 2 是 vanilla JS / paas.js 模式
- 不要省略 DOM 嵌套层级 —— 驾驶舱对 DOM 结构强依赖
- 不要漏掉 `localStorage.setItem('x-user-token', ...)` —— SDK 不会自己写，闭包取不到 token 整套鉴权失效
- 不要省略 `loadMap` / `initNotifyCenter` 的第二个 `auth` 参数 —— 否则后续请求无鉴权头
