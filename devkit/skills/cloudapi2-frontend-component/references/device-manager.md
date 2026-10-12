# 设备管理

设备管理 SDK（`UranusDeviceManagerApp`）是独立的 UMD bundle，**不依赖 `paas.js`**，可直接嵌入任何 HTML 页面使用。

有 **两种接入模式**，根据是否已经在用 `paas.js` 选其一：

| 模式 | 入口 | 依赖 paas.js | 适用场景 |
|---|---|---|---|
| SDK 模式（推荐） | `setupDeviceManager` + `mountXxx` | 否 | ISV 独立页面 / 宿主自有 tab 体系按需加载各模块 |
| paas.js 模式 | `window.FH2.loadDevice` | 是 | 与驾驶舱、航线等组件组合使用 |

---

## 一、SDK 模式 `UranusDeviceManagerApp`

### 1.1 资源加载

```html
<!-- 请将 [私有化部署地址] 替换为您的前端服务地址 -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/device-manager-sdk/index.css">
<script src="http://[私有化部署地址]/paasjs-demo/device-manager-sdk/index.js"></script>
```

加载完成后，SDK 挂载在 `window.UranusDeviceManagerApp`。

推荐用 `document.write` 注入（便于切环境）：

```html
<script>
  var DEMO_CONFIG = {
    sdkCss: 'http://[私有化部署地址]/paasjs-demo/device-manager-sdk/index.css',
    sdkJs:  'http://[私有化部署地址]/paasjs-demo/device-manager-sdk/index.js',
  }
  document.write('<link rel="stylesheet" href="' + DEMO_CONFIG.sdkCss + '">')
  document.write('<script defer src="' + DEMO_CONFIG.sdkJs + '"><\/script>')
</script>
```

### 1.2 DOM 容器

```html
<div id="uranus-device-manager-app"></div>
```

容器必须有显式宽高（建议 `width:100%; height:100vh`）。

> **注意**：容器不再由全局初始化传入，改为在挂载各模块时通过 `mountXxx({ container })` 传递。

---

### 1.3 两层架构（KOC3+ 推荐）

KOC3 起，初始化入口从旧版整包 `init()` 升级为 **两层架构**：

1. **全局初始化** — `setupDeviceManager(conf)` 完成鉴权、WebSocket、主题等全局配置（一次即可）
2. **模块挂载** — `mountXxx({ container, props, on })` 将具体功能模块渲染到指定 DOM 容器

```
setupDeviceManager()  ←── 全局配置，无容器概念，幂等
    ↓
mountDeviceList()     ←── 设备列表模块，传入 container
mountDeviceDetail()   ←── 设备详情模块，传入 container
mountHms()            ←── HMS 告警模块，传入 container
...
```

---

### 1.4 `setupDeviceManager` 接口

```typescript
window.UranusDeviceManagerApp.setupDeviceManager(conf: SetupParams): Promise<void>
```

> **幂等**：可多次调用。首次完整初始化 HTTP/WebSocket 连接；后续调用仅刷新 `callbacks` / `slots` / `theme`，不会重复创建连接。

**SetupParams 参数说明：**

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `serverUrl` | string | 是 | 后端服务地址，例 `http://host:30812`（库内部自动追加 `/gateway` 前缀） |
| `wsUrl` | string | 是 | 推送中心 WebSocket 地址，例 `ws://host:30812/openapi/v0.9/notification/ws/v1/notifications` |
| `auth` | AuthConfig | 是 | 鉴权配置（详见下表） |
| `locale` | string | 否 | 国际化语言，`'zh'`（默认）/ `'en'` |
| `theme` | `string \| ThemeTokens` | 否 | 内置名（`'light'` / `'dark'`）或自定义 ThemeTokens 对象，默认 `'light'`（详见第三节主题定制） |
| `antThemeBaseUrl` | string | 否 | Ant Design 双主题预编译产物路径，默认 `'/ant-theme'`。CDN 引入时使用绝对地址 |
| `callbacks.onError` | function | 否 | 运行时错误回调，参数为错误对象 |
| `slots` | SlotRegistry | 否 | 自留地插槽注册，详见模块化文档 |

**AuthConfig：**

| 字段 | 类型 | 说明 |
|---|---|---|
| `authKeys` | string[] | 鉴权 header 名称列表（用于网关识别 `auth_keys` 头） |
| `headers` | `object \| () => Record<string, string>` | 鉴权 header 键值对；**推荐函数形式**，每次请求都会调用，适配 token 刷新场景 |

> **鉴权流程**：SDK 不接收 `token` 字段。调用方需要在 `setupDeviceManager` **之前**自行 `localStorage.setItem('x-user-token', token)`，让 `auth.headers` 闭包能读到 token。SDK 内部把外层 `auth` 同时注入到 axios 单例、NotifyCenter 客户端、packages 内置 request 单例 —— 所有 HTTP / WebSocket 请求都自动带上鉴权头。token 刷新只需写入新值，下次请求自动取到，不必重新调用 `setupDeviceManager`。

### 1.5 `mountXxx` 模块挂载

全局初始化完成后，按需调用各模块的挂载函数：

```typescript
var handle = window.UranusDeviceManagerApp.mountDeviceList({
  container: '#uranus-device-manager-app',   // CSS 选择器或 HTMLElement
  props: { deviceType: 'airport' },          // 模块级参数
  on: { /* 事件监听 */ },
})

// 页面卸载 / tab 切换时调用 handle.unmount() 释放资源
handle.unmount()
```

`mountXxx` 通用参数结构：

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `container` | string \| HTMLElement | 是 | 模块渲染的 DOM 容器（CSS 选择器带 `#`，或 DOM 元素） |
| `props` | object | 否 | 模块业务参数（如 `deviceType` 等） |
| `on` | object | 否 | 事件监听回调集合 |

返回值为 `MountHandle`，包含 `unmount()` 方法用于卸载。

> 完整模块列表（`mountDeviceList`、`mountDeviceDetail`、`mountHms` 等）及各模块 `props` / `on` 参数详见平台侧模块化文档（ISV 可向平台方索取）。

### 1.6 完整初始化示例（推荐）

```javascript
window.addEventListener('load', async function () {
  if (!window.UranusDeviceManagerApp) {
    console.error('SDK 加载失败，请检查 sdkJs 路径是否可访问')
    return
  }
  var UDM = window.UranusDeviceManagerApp

  // 1. 在 setupDeviceManager 之前把 token 写入 localStorage，让下方 auth.headers 闭包能读到
  localStorage.setItem('x-user-token', 'your-jwt-token')

  // 2. 构造鉴权配置（authKeys + headers 函数式闭包）
  var auth = {
    authKeys: ['X-User-Token'],
    headers: function () {
      return {
        'X-User-Token': localStorage.getItem('x-user-token') || '',
      }
    },
  }

  // 3. 全局初始化（一次即可，无容器概念）
  try {
    await UDM.setupDeviceManager({
      serverUrl: 'http://<your-server>:30812',
      wsUrl:     'ws://<your-server>:30812/openapi/v0.9/notification/ws/v1/notifications',
      auth:      auth,
      locale:    'zh',
      theme:     'light',
      antThemeBaseUrl: 'http://<your-server>/paasjs-demo/device-manager-sdk/ant-theme',
      callbacks: {
        onError: function (err) { console.error('[DeviceManager]', err) },
      },
    })
    console.log('设备管理初始化成功')
  } catch (err) {
    console.error('设备管理初始化失败', err)
    return
  }

  // 4. 按需挂载各模块（这里以设备列表为例）
  var handle = UDM.mountDeviceList({
    container: '#uranus-device-manager-app',
    props: { deviceType: 'airport' },
  })

  // 页面卸载 / tab 切换时调用 handle.unmount() 释放
})
```

### 1.7 注意事项

- **容器选择器必须带 `#`**（CSS 选择器），写成 `'uranus-device-manager-app'` 会找不到容器。
- **没有 `token` 字段**：token 通过 `localStorage` + `auth.headers` 闭包注入。
- **`wsUrl` 是推送中心地址**：路径为 `/openapi/v0.9/notification/ws/v1/notifications`。
- **`serverUrl` 不带 `/gateway`**：库内部自动追加。
- **`serverUrl` 直连 FH2 后端**：即使在本地开发环境（如 Vite dev server），`serverUrl` 也应指向 FH2 后端真实地址（如 `https://host:30812`），**不需要走本地代理**。FH2 后端自身已配置 CORS，允许跨域调用。
- **`mountDeviceList` 必须传 `props: { deviceType: 'airport' }`**：不传 `deviceType` 时设备列表为空（不会默认展示所有设备）。可选值：`'airport'`（机场列表）、`'aircraft'`（飞行器列表）。
- **`antThemeBaseUrl` 路径**：标准路径为 `http://[前端服务地址]/paasjs-demo/device-manager-sdk/ant-theme`（无 `-v2` 后缀）。
- **`setupDeviceManager` 幂等**：首次完整初始化，后续仅刷新回调/插槽/主题。切换主题也通过重新调用此方法实现。

---

### 1.8 旧版 `init()` 接口（已废弃，仅兼容）

> **Deprecated**：KOC3 起不再推荐使用。新宿主请直接使用 `setupDeviceManager` + `mountXxx` 两层架构。旧版 `init()` 仍然可用，但不支持模块化挂载、插槽、`antThemeBaseUrl` 等新能力。

```typescript
window.UranusDeviceManagerApp.init(options: IDeviceManagerOptions): Promise<void>
```

**IDeviceManagerOptions**：

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `containerId` | string | 是 | CSS 选择器，例 `'#uranus-device-manager-app'`（**带 `#`**） |
| `serverUrl` | string | 是 | 后端地址，例 `http://host:30812` |
| `wsUrl` | string | 是 | 推送中心 WebSocket 地址 |
| `auth` | IAuth | 是 | 鉴权配置（与新版 AuthConfig 一致） |
| `locale` | string | 否 | `'zh'`（默认）/ `'en'` |
| `theme` | `string \| ThemeTokens` | 否 | 内置名或自定义 ThemeTokens 对象，默认 `'light'` |
| `callbacks.onError` | function | 否 | 运行时错误回调 |

**旧版初始化示例：**

```javascript
window.addEventListener('load', function () {
  if (!window.UranusDeviceManagerApp) {
    console.error('SDK 加载失败，请检查 sdkJs 路径是否可访问')
    return
  }

  // 1. 在 init 之前把 token 写入 localStorage
  localStorage.setItem('x-user-token', 'your-jwt-token')

  // 2. 构造鉴权配置
  var auth = {
    authKeys: ['X-User-Token'],
    headers: function () {
      return {
        'X-User-Token': localStorage.getItem('x-user-token') || '',
      }
    },
  }

  // 3. 调用 init（旧版整包模式，容器在此处传入）
  window.UranusDeviceManagerApp.init({
    containerId: '#uranus-device-manager-app',
    serverUrl: 'http://<your-server>:30812',
    wsUrl:     'ws://<your-server>:30812/openapi/v0.9/notification/ws/v1/notifications',
    auth:      auth,
    locale:    'zh',
    callbacks: {
      onError: function (err) { console.error('[DeviceManager] error:', err) },
    },
  })
    .then(function () { console.log('设备管理初始化成功') })
    .catch(function (err) { console.error('设备管理初始化失败', err) })
})
```

---

## 二、主题定制

设备管理 SDK 内置 `'light'`（默认）和 `'dark'` 两套主题，同时支持传入**自定义 ThemeTokens 对象**实现品牌化定制。主题通过 `setupDeviceManager({ theme })` 一次性注入到所有已挂载/后续挂载的模块。

### 2.1 内置主题

```javascript
await window.UranusDeviceManagerApp.setupDeviceManager({
  // ...其它字段
  theme: 'light',  // 或 'dark'
})
```

### 2.2 自定义主题（推荐流程）

不建议手写 ThemeTokens，SDK 提供可视化的主题配置面板，导出即用：

1. 在任意已挂载页面调用 `window.UranusDeviceManagerApp.showThemePanel()` 打开配置面板。
2. 在面板中调整背景色/主色/强调色/分隔线/文本色等 token，直到满意。
3. 点击面板底部「导出 JSON」按钮，得到完整的 ThemeTokens 对象。
4. 把导出的 JSON 对象作为 `theme` 传入 `setupDeviceManager`。

```javascript
// 将导出的 JSON 对象粘贴至此
var CUSTOM_THEME = {
  bg_primary_default:       '#ffffff',
  bg_light_primary_default: '#1a1a2e',
  bg_secondary_default:     '#fafafa',
  // ... 其余 token
}

await window.UranusDeviceManagerApp.setupDeviceManager({
  // ...其它字段
  theme: (CUSTOM_THEME && typeof CUSTOM_THEME === 'object') ? CUSTOM_THEME : 'light',
})
```

### 2.3 Ant Design 双主题产物路径

设备管理内部使用 Ant Design 组件，`light` / `dark` 两套预编译产物按需切换加载，路径由 `antThemeBaseUrl` 指定：

```javascript
await window.UranusDeviceManagerApp.setupDeviceManager({
  // ...其它字段
  theme: 'dark',
  antThemeBaseUrl: 'http://[私有化部署地址]/paasjs-demo/device-manager-sdk-v2/ant-theme',
})
```

### 2.4 主题切换

设备管理**不提供**组件级 `setTheme()` 热切换 API。切换主题需重新调用 `setupDeviceManager({ theme })`。

因为 `setupDeviceManager` 是幂等的，后续调用只会刷新主题与回调，不会重复初始化 HTTP/WebSocket 连接，所以性能开销极低。

```javascript
// 切换到暗色主题
await window.UranusDeviceManagerApp.setupDeviceManager({
  serverUrl: 'http://<your-server>:30812',
  wsUrl:     'ws://<your-server>:30812/openapi/v0.9/notification/ws/v1/notifications',
  auth:      auth,
  theme:     'dark',
  antThemeBaseUrl: 'http://<your-server>/paasjs-demo/device-manager-sdk-v2/ant-theme',
})
```

---

## 三、paas.js 模式 `FH2.loadDevice`

### 3.1 前置条件

- 已加载 `paas.js`
- 已调用 `FH2.initConfig`（见 paas.js 初始化流程）

### 3.2 DOM 容器

```html
<div id="device-manager-app-container"></div>
<div id="map-app-global"></div>
```

### 3.3 调用

```javascript
window.FH2.loadDevice('device-manager-app-container', {
  deviceType: 'airport',   // 'airport' = 机场列表，'aircraft' = 无人机列表
})
```

`deviceType` 可选；不传默认显示机场列表。

### 3.4 销毁

```javascript
window.FH2.destroyDevice()  // 返回 Promise
  .then(function () { console.log('设备管理已卸载') })
```

---

## 四、SDK 模式与 paas.js 模式对比

| 对比项 | SDK 模式（`setupDeviceManager` + `mountXxx`） | paas.js 模式（`FH2.loadDevice`） |
|---|---|---|
| 依赖 paas.js | 否，完全独立 | 是，需先加载 paas.js |
| 接入复杂度 | 低，单文件引入 + 全局 `setupDeviceManager` 一次 + 按需 `mountXxx` | 较高，需配合 FH2 完整初始化流程 |
| 模块化挂载 | 支持，各模块独立 mount/unmount | 不支持，整体加载 |
| 主题定制 | 完整支持（light/dark/自定义 ThemeTokens） | 受限于 FH2 全局主题 |
| 适用场景 | ISV 独立页面 / 宿主自有 tab 体系按需加载各模块 | 与驾驶舱、航线等组件组合使用 |
