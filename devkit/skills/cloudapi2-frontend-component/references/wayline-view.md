# 航线预览组件（WaylineView）

在地图上只读预览/渲染一组航线，支持多航线同时渲染、拓扑设备连线叠加、地图回中。

**与 WaylineEditor 的区别**：WaylineView 是**只读预览**（只渲染、不编辑）；WaylineEditor 用于航线**编辑**。

## 一、核心特性

- **完全自包含**：内置 Cesium 地图（`internal` 模式），不依赖 `paas.js`，不依赖 `FH2`
- 全局类名：`window.WaylineView`
- 运行时依赖库（WPMZ/PPE 等 WASM）已打包进 SDK，实例化时自动注入

## 二、资源加载（顺序硬要求）

Cesium **必须**在 WaylineView SDK 之前**同步**加载，否则地图初始化失败（错误码 6003）。

```html
<!-- 1. Cesium 全局（提供 window.Cesium）— 同步加载 -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Widgets/widgets.css">
<script src="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Cesium.js"></script>

<!-- 2. WaylineView SDK — defer 加载 -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/wayline-view-sdk/index.css">
<script defer src="http://[私有化部署地址]/paasjs-demo/wayline-view-sdk/index.js"></script>
```

加载完成后：`window.WaylineView` 可用，`window.Cesium` 已就绪。

## 三、DOM 骨架

沿用 `fh2-container` 布局，地图容器 `#map-app-global`，预览组件挂载到 `#wayline-view-container`：

```html
<div class="fh2-container">
  <div class="project-details">
    <div id="project-middle-container"></div>
    <div id="project-right-micro-app" class="right-micro-app">
      <div class="maps-micro-app">
        <div id="project-map-app-placeholder" class="map-app-placeholder">
          <div id="map-app-global" class="map-app-container"></div>
        </div>
      </div>
      <div id="wayline-view-container"></div>
    </div>
  </div>
</div>
```

### 3.1 必备 CSS

与飞行记录组件共用同一套布局 CSS（将 `#fly-data-app-container` 选择器替换为 `#wayline-view-container`）：

```css
.fh2-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
}

.map-app-container > div {
  width: 100%;
  height: 100%;
}

.project-details {
  position: relative;
  width: 100%;
  display: flex;
  flex: 1;
  overflow: hidden;
}

.project-details .right-micro-app {
  position: relative;
  display: flex;
  flex: 1;
}

.project-details .right-micro-app .maps-micro-app {
  display: flex;
  flex: 1;
  height: 100%;
}

.project-details .right-micro-app .map-app-placeholder {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.project-details .right-micro-app .map-app-container {
  height: 100%;
  min-height: 0;
}

.project-details .right-micro-app #wayline-view-container {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
}
```

## 四、构造函数 `WaylineViewOptions`

```ts
new WaylineView(options: WaylineViewOptions): WaylineView
```

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `api.baseUrl` | string | Y | 后端服务地址，例 `http://<INT_FLHUB_XX>:30812/gateway`（`<INT_FLHUB_XX>` 为内网测试环境占位，开源 fork 请替换为自建环境域名） |
| `api.auth` | AuthConfig | Y | HTTP 鉴权配置 `{ authKeys, headers }` |
| `map.mode` | `'internal'` \| `'external'` | N | 地图模式，默认 `internal`（SDK 自建 Cesium） |
| `map.container` | string \| HTMLElement | N | `internal` 模式地图容器，默认 `#map-app-global` |
| `map.env.cesiumBaseUrl` | string | N | Cesium 资源目录 URL |
| `map.notifyWsUrl` | string | N | 推送中心 WebSocket 地址，doodle 插件据此实时更新地图元素 |
| `map.topo` | object | N | 拓扑设备连线配置（含 `snList` 字段） |
| `map.mapAppState` | any | N | `external` 模式**必填**，宿主已起图的地图实例 |
| `runtimeDepsBasePath` | string | N | 运行时依赖库资源目录，默认取 SDK 自身脚本所在目录 |
| `autoRecenter` | boolean | N | 渲染后是否自动回中，默认 `true` |
| `onlyShowMapElements` | boolean | N | 多航线时是否只渲染地图元素、隐藏辅助 DOM（如航线切换按钮），默认 `true` |
| `features.mappingPolygonColor` | boolean | N | 测绘航线多边形着色，默认 `true` |

**AuthConfig 结构**：

```ts
interface AuthConfig {
  authKeys: string[]          // 鉴权 header key 列表
  headers: () => Record<string, string>  // 返回鉴权 headers 的函数
}
```

## 五、生命周期

```
new WaylineView(options) → loadMap() → mount() → render({ waylineIds }) → destroy()
```

构造实例后，**必须按顺序**调用：

1. `loadMap()` — 加载/创建 Cesium 地图
2. `mount()` — 挂载预览组件到 DOM
3. `render()` — 渲染航线
4. 不再使用时 `destroy()` — 释放所有资源

## 六、核心方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `loadMap` | `loadMap(container?: HTMLElement \| string): Promise<void>` | 加载地图（`internal` 模式创建 Cesium 实例） |
| `mount` | `mount(container: HTMLElement): void` | 挂载预览组件到 DOM |
| `render` | `render(input: { waylineIds: string[]; autoRecenter?: boolean }): Promise<void>` | 渲染一组航线，全部完成后 resolve |
| `recenter` | `recenter(waylineId?: string): Promise<void>` | 地图回中（可指定某条航线，不传则全部） |
| `clear` | `clear(): void` | 清空已渲染航线 |
| `showDeviceTopo` | `showDeviceTopo(sns: string[]): void` | 显示/清除拓扑设备连线（传空数组清除） |
| `destroy` | `destroy(): void` | 销毁实例，释放所有资源 |

### 静态方法

| 方法 | 说明 |
|------|------|
| `WaylineView.getTopoList()` | 从后端拉取全量设备拓扑列表，返回 `Promise` |

### Getter 属性

| 属性 | 类型 | 说明 |
|------|------|------|
| `view.selectedDeviceSn` | `string \| null` | 当前选中的拓扑设备 SN |
| `view.state` | `{ mounted: boolean, renderedWaylineIds: string[] }` | 实例状态 |

## 七、事件监听

```ts
view.on(eventName: string, callback: (data: any) => void): void
```

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `ready` | `{ waylineIds: string[] }` | 全部航线解析 + 渲染完成 |
| `wayline-rendered` | `{ waylineId, startWaypoints, total }` | 单条航线渲染完成 |
| `recentered` | 无 | 地图回中完成 |
| `device-selected` | `{ deviceSn: string }` | 拓扑设备 marker 被点击选中（需配置 `map.topo`） |
| `error` | `{ code, message, data }` | SDK 内部发生错误时触发 |

## 八、错误码

| 错误码 | 说明 | 排查建议 |
|--------|------|----------|
| `5000` | API 请求失败 | 检查 `api.baseUrl` 可达性、网络连通 |
| `5001` | API 鉴权失败 | 检查 `api.auth.headers()` 返回的 token 是否有效 |
| `6000` | 航线 KMZ 下载失败 | 检查 waylineId 是否有效、后端文件存储服务是否正常 |
| `6001` | 航线 KMZ 解析失败 | KMZ 文件格式损坏或不兼容 |
| `6002` | 运行时依赖（WASM）加载失败 | 检查 `runtimeDepsBasePath` 路径是否正确、文件是否存在 |
| `6003` | 地图初始化失败 | Cesium 未在 SDK 之前加载，或 `cesiumBaseUrl` 路径错误 |

## 九、完整初始化示例

```javascript
// 1. 鉴权配置
var auth = {
  authKeys: ['X-User-Token'],
  headers: function () {
    return { 'X-User-Token': localStorage.getItem('x-user-token') || '' }
  },
}

// 2. 构造实例
var view = new window.WaylineView({
  api: {
    baseUrl: 'http://example.flighthub.internal:30812/gateway',
    auth: auth,
  },
  runtimeDepsBasePath: 'http://example.flighthub.internal/paasjs-demo/sdk-runtime',
  map: {
    mode: 'internal',
    container: '#map-app-global',
    env: {
      cesiumBaseUrl: 'http://example.flighthub.internal/paasjs-demo/sdk-runtime/cesium/',
      terrainKey: '',
      featureGateway: [],
    },
    notifyWsUrl: 'ws://example.flighthub.internal:30812/openapi/v0.9/notification/ws/v1/notifications',
    topo: { snList: [] },
  },
  locale: { language: 'zh' },
  autoRecenter: true,
})

// 3. 监听事件
view.on('ready', function (payload) {
  console.log('全部航线渲染完成：', payload.waylineIds)
})

view.on('wayline-rendered', function (payload) {
  console.log('单条航线已渲染：', payload.waylineId)
})

view.on('error', function (err) {
  console.error('WaylineView 错误：', err.code, err.message)
})

// 4. 启动生命周期
await view.loadMap()
view.mount(document.getElementById('wayline-view-container'))

// 5. 渲染航线（可传多条 waylineId）
await view.render({
  waylineIds: ['809649d4-5635-4380-94d2-16e21ceb0c41']
})
```

## 十、销毁

```javascript
view.destroy()
view = null
```

销毁后释放地图、Vue 实例、事件监听等所有资源。页面卸载或组件 unmount 时**必须调用**，否则内存泄漏。

## 十一、常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| 地图白屏，控制台无错误 | Cesium 未加载或加载顺序错误 | 确保 `Cesium.js` 在 SDK `index.js` 之前同步加载 |
| 错误码 6003 | `cesiumBaseUrl` 路径错误或 Cesium 资源 404 | 检查路径末尾 `/`、资源文件是否存在 |
| 错误码 6002 | WASM 依赖加载失败 | 检查 `runtimeDepsBasePath` 是否指向正确的 `sdk-runtime` 目录 |
| 航线渲染为空 | waylineId 无效或后端无对应 KMZ | 确认 waylineId 存在，后端存储服务可达 |
| 拓扑设备不显示 | 未配置 `map.topo` 或 `snList` 为空 | 传入设备 SN 列表，或调用 `showDeviceTopo(sns)` |
| `external` 模式地图不渲染 | 未传 `map.mapAppState` | `external` 模式必须传入宿主已创建的地图实例 |
