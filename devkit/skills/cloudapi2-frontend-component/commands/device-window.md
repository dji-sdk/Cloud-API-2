---
description: 生成DJI 上云API 2 设备小窗（FH2.loadDeviceWindow）的最小可运行 HTML
argument-hint: [frontHost] [sn1,sn2,...]
---

调用 **cloudapi2-frontend-component** skill，阅读 `references/device-window.md` 和 `references/cockpit.md`（初始化流程相同），输出一份**最小可运行**的设备小窗集成 HTML。

## 用户提供的参数

$ARGUMENTS

参数按位置解析（任意可省）：
1. `frontHost` —— 前端服务地址（例 `http://<your-server>`，能访问到 `/paas.js`）
2. `sn1,sn2,...` —— 逗号分隔的设备序列号（传给 `devices` 参数）

未提供的参数：**在输出代码里用清晰占位符**（`http://[frontHost]` / `[DEVICE_SN_1],[DEVICE_SN_2]` / `[JWT_TOKEN]`），并在末尾用一句话告知用户需要替换。**不要编造 IP / SN / token**。

## 输出要求

1. **完整单文件 HTML**，可直接保存运行（包含 DOCTYPE、`<head>`、`<style>`、`<body>`、`<script>`）
2. **DOM 结构严格按** `references/device-window.md` 第二节：
   - `#map-app-global`（地图容器）
   - `#project-app-container`（隐藏，paas.js 内部使用）
   - `#real-time-app-container`（小窗挂载点）
3. **qiankun CSS 修复**：必须包含 `#__qiankun_microapp_wrapper_for_real_time_app__` 高度修复规则
4. **加载顺序**：`paas.js` → `localStorage.setItem` → `initConfig` → `initNotifyCenter(wssUrl, auth)` → `loadMap(serverUrl, auth, wssUrl)` → `loadDeviceWindow`
5. **鉴权**：
   - `localStorage.setItem('x-user-token', '[JWT_TOKEN]')` 必须在 `initConfig` 之前
   - `auth = { authKeys: ['X-User-Token'], headers: function() { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } } }`
   - `loadMap` 传 3 个参数（第 3 个 `wssUrl` 启用设备位置推送）
6. **`loadDeviceWindow` 调用**：
   - `FH2.loadDeviceWindow('real-time-app-container', { devices: 'SN1,SN2', map: true })`
   - 返回 `Promise<DeviceWindowInstance[]>`，用 `.then()` 获取实例数组
7. **实例方法示例**：展示 `setTask()`、`setHmsUrl()`、`setCockpitUrl()` 的典型用法
8. **动态更新**：附带 `FH2.updateDeviceWindow({ devices: 'NEW_SN' })` 示例
9. **销毁示例**：`beforeunload` 调 `FH2.destroyDeviceWindow()`
10. **协议一致性提醒**：HTTPS 页面必须用 `https://` + `wss://`

## 不要做

- 不要省略 `#project-app-container` —— 即使设置 `display:none`，paas.js 内部仍然需要此容器
- 不要遗漏 `loadMap` 的第三个参数 `wssUrl` —— 否则小窗内设备位置不实时更新
- 不要为每个设备单独调 `loadDeviceWindow` —— 用逗号分隔的 SN 字符串一次传入
- 不要把 `loadDeviceWindow` 的返回当同步值 —— 它是 Promise，必须 `.then()` 或 `await`
- 不要混淆 `destroyDeviceWindow(true)` 和 `destroyDeviceWindow()` —— `true` 仅销毁小窗保留地图，默认全部销毁
