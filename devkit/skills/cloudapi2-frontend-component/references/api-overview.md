# 4 组件总览 + 字段名差异对比

DJI 上云API 2 4 个组件的初始化字段名 **不统一**，是 ISV 接入时最容易出错的地方。先看这张表，再去读具体组件文档。

## 一、组件 × 依赖关系

| 组件 | 入口 | 依赖 `paas.js` | 需调 `FH2.initConfig` | 额外 SDK 资源（CSS+JS） |
|---|---|---|---|---|
| 航线 - 创建/编辑 | `new WaylineEditor(opts)` | ✓ 需要（SDK 内部用 `window.FH2` 加载地图） | ✗ 不需要（SDK 内部已封装 initConfig + loadMap） | ✓ `wayline-edit-sdk/index.{css,js}` |
| 航线 - 预览 | `window.FH2.loadWaylineView(domId, params)` | ✓ 需要 | ✓ 需要 | ✗ 不需要 |
| 设备管理 - SDK 模式 | `window.UranusDeviceManagerApp.init(opts)` | ✗ 不需要 | ✗ 不需要 | ✓ `device-manager-sdk/index.{css,js}` |
| 设备管理 - paas.js 模式 | `window.FH2.loadDevice(domId, params)` | ✓ 需要 | ✓ 需要 | ✗ 不需要 |
| 驾驶舱 | `window.FH2.loadCockpit(domId, params)` | ✓ 需要 | ✓ 需要 | ✗ 不需要 |
| 飞行记录回放 | `new FlyDataReplay(opts)` | ✓ 需要 | ✗ 不需要 | ✓ `fly-data-sdk/index.{css,js}` |

**速记**：
- **完全独立**（只引一个 SDK 文件）：`UranusDeviceManagerApp`
- **依赖 paas.js + 不调 initConfig**：`WaylineEditor`（创建/编辑）、`FlyDataReplay`
- **依赖 paas.js + initConfig**：航线预览、设备管理 paas.js 模式、驾驶舱

⚠️ `paas.js` 必须**先于** SDK `index.js` 加载，否则 `window.FH2` 未就绪。

## 二、初始化字段名对照表

**同一个"前端服务地址"在不同组件里叫不同名字**，对照下表：

| 含义 | `FH2.initConfig` | `WaylineEditor` | `FlyDataReplay` | `UranusDeviceManagerApp.init` |
|---|---|---|---|---|
| 前端服务地址 | `hostUrl` | `hostUrl` | `hostUrl` | —（不需要） |
| 后端 API | `serverUrl` | `api.baseUrl` | `api.baseUrl` | `serverUrl` |
| WebSocket | `wssUrl` | `ws.url` | —（不需要） | `wsUrl`（通知中心） |
| 鉴权 | —（看 `loadXxx` 内部） | `api.auth`（自动复用给 WS） | `api.auth` | `auth`（与 `AuthConfig` 同结构） |

⚠️ `WaylineEditor` 的 WS 鉴权由 SDK **自动复用 `api.auth`**，不要单独传 `ws.auth`。

### 后端 API 路径差异（容易写错）

| 组件 | `baseUrl` / `serverUrl` 的写法 |
|---|---|
| `FH2.initConfig` | `http://host:30812`（**不带** `/gateway`） |
| `WaylineEditor` `api.baseUrl` | `http://host:30812/gateway`（**带** `/gateway`） |
| `FlyDataReplay` `api.baseUrl` | `http://host:30812/gateway`（**带** `/gateway`） |
| `UranusDeviceManagerApp` `serverUrl` | `http://host:30812`（**不带** `/gateway`） |

### WebSocket 路径差异（容易写错）

| 组件 | `wssUrl` / `ws.url` / `wsUrl` 的写法 |
|---|---|
| `FH2.initConfig` | `ws://host:30812/openapi/v0.9/notification/ws/v1/notifications`（推送中心地址）；**航线预览不需要 `wssUrl`** |
| `WaylineEditor` `ws.url` | `ws://host:30812/openapi/v0.9/notification/ws/v1/notifications` |
| `UranusDeviceManagerApp` `wsUrl` | `ws://host:30812/openapi/v0.9/notification/ws/v1/notifications`（推送中心地址） |

## 三、鉴权（auth）模式

DJI 上云API 2 用动态请求头鉴权（每次请求时回调拿当前 token）。类型名 `AuthConfig`（`WaylineEditor` / `FlyDataReplay` 同结构）。

```ts
interface AuthConfig {
  authKeys: string[]
  headers: Record<string, string> | (() => Record<string, string>)   // 推荐函数形式
}
```

```javascript
var auth = {
  authKeys: ['X-User-Token'],
  headers: function () {        // 函数形式：token 刷新后自动取最新值
    return {
      'X-User-Token': localStorage.getItem('x-user-token') || '',
      // 如需 user-id 等附加头，也在这里返回
    }
  },
}
```

`WaylineEditor` 和 `FlyDataReplay` 都用这个结构。**`WaylineEditor` 的 WS 鉴权 SDK 自动复用 `api.auth`，不要再传 `ws.auth`。**

`UranusDeviceManagerApp` 也用同一套 `auth` 对象（字段名为 `auth`，结构与 `AuthConfig` 一致）；调用方需在 `init` 之前把 token 写入 `localStorage`，让 `auth.headers` 闭包能读到。

**驾驶舱（`FH2.loadCockpit`）鉴权**：调用方把 `auth` 传给 `FH2.initNotifyCenter(wssUrl, auth)` 与 `FH2.loadMap(serverUrl, auth)`，FH2 内部缓存为 `_defaultAuth`，自动透传给驾驶舱、`project-app`、packages request 单例 —— 所有 HTTP / WebSocket 请求自动带鉴权头。详见 `cockpit.md` 五。

## 四、销毁方法对照

| 组件 | 销毁 |
|---|---|
| `WaylineEditor` 实例 | `editor.destroy()` —— 释放后必须重新 `new`；**同页面不能同时挂多实例**（FH2 / mapAppState 是全局单例） |
| `FH2.loadWaylineView` | `window.FH2.destroyPaasView()` |
| `UranusDeviceManagerApp` | 暂无显式 destroy，由 SDK 内部管理（重新 `init()` 会复用容器） |
| `FH2.loadDevice` | `window.FH2.destroyDevice()` → 返回 Promise |
| `FH2.loadCockpit` | `window.FH2.destroyCockpit(self?)` → 返回 Promise；`self=true` 仅卸载驾驶舱保留地图 |
| `FlyDataReplay` 实例 | `player.destroy()` —— 切 `taskId` 必须先销毁旧实例再 new；**同页面不能同时挂多实例**（同上） |

## 五、统一错误码

`WaylineEditor` 和 `FlyDataReplay` 使用**同一份**错误码集合（通过 `editor.on('error', cb)` / `player.on('error', cb)` 抛出）：

| 错误码 | 名称 | 含义 |
|---|---|---|
| 4001 | `WS_AUTH_ERROR` | WebSocket 鉴权失败（WS close code = 401） |
| 4002 | `WS_DISCONNECTED` | WebSocket 连接断开 |
| 5000 | `API_ERROR` | API 请求失败（网络 / 5xx） |
| 5001 | `API_AUTH_ERROR` | API 鉴权失败（401/403） |
| 6000 | `PARSE_ERROR` | 航线 KMZ 解析失败 |
| 6001 | `WASM_LOAD_ERROR` | WASM 加载失败 |
| 6002 | `MAP_INIT_ERROR` | 地图初始化失败 |
| 7000 | `UPLOAD_ERROR` | OSS 上传失败 |

详细处理建议见 `troubleshooting.md`。

## 六、关键调用顺序

### `WaylineEditor`

`new` → `mount()` → `loadMap()`（仅 edit 需要） → `render('edit')` / `create()`。

| 场景 | 顺序 |
|---|---|
| 创建航线 | `new` → `mount` → `create()` —— **不需要 `loadMap`** |
| 编辑航线 | `new` → `mount` → `await loadMap()` → `render('edit', { waylineId })` |
| 切换主题 | mount 后任意时刻 `setTheme(partial)` |
| 卸载 | `destroy()` |

详见 `wayline-editor.md` 1.5。

### `FlyDataReplay`

`new` → `on('error', ...)` → **`await loadMap()`**（始终必需） → `mount(container)`。

| 场景 | 顺序 |
|---|---|
| 回放飞行数据 | `new` → `on('error')` → `await loadMap()` → `mount(container)` |
| 切换主题 | mount 后任意时刻 `setTheme(partial)` |
| 切换语言 | mount 后任意时刻 `setLocale('zh' \| 'en' \| 'es')` |
| 切 `taskId` | `destroy()` → `new` → `await loadMap()` → `mount()` —— 不要复用旧实例 |

⚠️ `FlyDataReplay` 与 `WaylineEditor` 的关键差异：
- `loadMap()` **始终必需**（WaylineEditor 仅 edit 模式需要）
- `loadMap()` 在 `mount()` **之前**调用（WaylineEditor 在之后）
- `mount` 目标直接是 `#fly-data-app-container`

详见 `fly-data-replay.md` 四。

## 七、主题定制（仅 SDK 类组件支持）

| 组件 | 初始化字段 | 类型 | 运行时方法 |
|---|---|---|---|
| `WaylineEditor` | `theme` | `Partial<WaylineTheme>` | `editor.setTheme(theme)` |
| `FlyDataReplay` | `theme` | `Partial<FlyDataTheme>` | `player.setTheme(theme)` |
| `UranusDeviceManagerApp` | — | — | — |
| `FH2.loadCockpit` / `loadDevice` / `loadWaylineView` | — | — | — |

**字段名一致（都叫 `theme`），调色板结构一致，但默认值不同。** SDK **不提供预置主题**，只暴露定制接口；12 个色板字段、两套默认值、自定义示例见 `theme.md`。

