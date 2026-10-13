# WaylineEditor SDK（航线创建 / 编辑 / 拼接）

`WaylineEditor` 是一个独立的 UMD bundle，**完全自包含**：内置 Cesium 地图（`internal` 模式），不依赖 `paas.js`，也不依赖 `FH2.initConfig`。

同一个实例即可完成**创建航线**、**编辑航线**、**航线拼接**三件事。

---

## 一、资源加载

需要加载两组资源：Cesium（同步）和 WaylineEditor SDK（defer）。

```html
<!-- 1. Cesium：必须同步引入，且早于 SDK -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Widgets/widgets.css">
<script src="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Cesium.js"></script>

<!-- 2. WaylineEditor SDK 产物 -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/wayline-edit-sdk/index.css">
<script defer src="http://[私有化部署地址]/paasjs-demo/wayline-edit-sdk/index.js"></script>
```

加载完成后：`window.WaylineEditor`（SDK 类）、`window.Cesium`（地图引擎）。

**关键时序**：Cesium 同步加载，SDK 用 `defer`，必须在 `window.onload` 之后再初始化：

```javascript
window.addEventListener('load', async function () {
  if (!window.Cesium) return console.error('window.Cesium 未加载')
  if (!window.WaylineEditor) return console.error('window.WaylineEditor 未加载')
  // ...初始化见下文
})
```

> **`hostUrl` 已废弃**，传入无效。替换为 `runtimeDepsBasePath` + `map.env.cesiumBaseUrl`。

---

## 二、DOM 骨架（必备）

SDK 内部多个组件靠 `<teleport>` 投递到固定选择器，**完整 DOM 骨架与样式必须按下例提供**，否则会出现地图不渲染、视频错位、表单浮空等问题。

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
      <div id="wayline-app-container">
        <div id="uranus-wayline-app"></div>
      </div>
    </div>
  </div>
</div>
```

**约束**：

| 选择器 / 结构 | 约束 |
|---|---|
| `#wayline-header` | 顶栏（保存/返回/设置）渲染目标，**必须在 `render()` 之前存在于页面上**，否则整个顶栏不会显示 |
| `#project-middle-container` 与 `.right-micro-app` | 必须是**相邻兄弟节点**，中间不插任何节点 |
| `.right-micro-app` -> `.maps-micro-app` -> `.map-app-placeholder` | 三层不能扁平化 |
| `#map-app-global` | 由 `editor.loadMap()` 接管，不需自行起图 |
| `editor.mount()` 目标 | 内层 `#uranus-wayline-app` |
| 所有类名 / ID | 不可更改 |

### 必备 CSS

```css
.fh2-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  padding-top: 48px;        /* 给 #wayline-header 顶栏留位，按需调整 */
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
  min-height: 0;            /* flex 子项需要显式设置，否则会被内容撑爆，地图算错高度 */
}

.project-details .right-micro-app #wayline-app-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}
```

---

## 三、构造函数

```typescript
new WaylineEditor(options: IWaylineEditorOptions): WaylineEditor
```

### IWaylineEditorOptions

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `api.baseUrl` | string | Y | 后端服务地址，例如 `https://<INT_FLHUB_XX>:30812/gateway`（`<INT_FLHUB_XX>` 为内网测试环境占位，开源 fork 请替换为自建环境域名） |
| `api.auth` | IAuth | Y | HTTP 鉴权配置，同时用于 WebSocket 与地图请求 |
| `map` | object | N | 地图配置（见下表）。不传则按 `internal` 模式使用默认容器 `#map-app-global` |
| `runtimeDepsBasePath` | string | N | 运行时依赖资源的根目录，默认取 SDK 自身脚本所在目录 |
| `ws.url` | string | N | WebSocket 地址，鉴权复用 `api.auth` |
| `ws.instance` | object | N | 与 `ws.url` 二选一，直接复用已有推送客户端实例 |
| `oss.objectKeyPrefix` | string | N | OSS 对象存储 key 前缀。不传则每次随机生成 UUID，**建议显式指定** |
| `locale.language` | string | N | 国际化语言，默认 `zh` |
| `locale.messages` | object | N | 自定义文案，一般传 `window.URANUS_LANGUAGE` |
| `supportedDrones` | `Array<{ drone: number, payloadList: number[] }>` | N | 限制可选机型/负载，不传或空数组 = 展示全部内置机型 |
| `enabledWaylineTypes` | number[] | N | 限制创建航线时可选的航线类型，不传或空数组 = 展示全部内置类型 |
| `waylineId` | string | N | 默认航线 ID，`render()` 未显式传入时使用 |
| `theme` | ITheme | N | 自定义主题色 |
| `hostUrl` | string | -- | **已废弃**，传入无效，可直接删除 |

### map（地图配置）

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `mode` | `'internal'` | N | 地图模式，默认 `internal`，即由 SDK 自建地图实例 |
| `container` | string \| HTMLElement | N | `internal` 模式地图挂载容器，默认 `#map-app-global`；也可在 `loadMap()` 时传入 |
| `env.cesiumBaseUrl` | string | N | Cesium 资源目录 URL，通常与引入 `Cesium.js` 的路径一致 |
| `env.terrainKey` | string | N | 地形瓦片密钥，通常传空串 |
| `env.terrainHost` | string | N | 地形服务地址，不传则回退 `api.baseUrl` |
| `env.serverUrl` | string | N | 后端服务地址，不传则回退 `api.baseUrl` |
| `env.featureGateway` | string[] | N | 特性黑名单，一般传 `[]` |
| `env.mapEngine` | `'g'` \| `'t'` | N | 地图引擎，`g` 海外 / `t` 国内 |
| `topo` | object | N | 拓扑设备连线配置，**不传则不加载拓扑插件**（见第七节） |

### IAuth

| 字段 | 类型 | 说明 |
|---|---|---|
| `authKeys` | string[] | 鉴权请求头的名称列表 |
| `headers` | `() => Record<string, string>` | 动态返回鉴权请求头，每次请求时都会调用 |

> **token 会过期** -> `headers` 用函数形式，SDK 内部每次请求 / WS 重连都实时取值。

---

## 四、生命周期方法

### 调用顺序

`new WaylineEditor()` -> `loadMap()` -> `mount()` -> `render('edit')` / `create()`

### loadMap

```typescript
editor.loadMap(
  container?: HTMLElement | string,
  position?: { longitude: number, latitude: number, height?: number },
): Promise<void>
```

- `container` 不传则取 `map.container`，再不传则取 `#map-app-global`
- `position` 用于起图后把镜头定位到指定经纬度，仅 `internal` 模式生效

### mount

```typescript
editor.mount(container: HTMLElement): void
```

将编辑器挂载到 DOM 容器（通常是 `#uranus-wayline-app`）。

### destroy

```typescript
editor.destroy(): void
```

释放 SDK 占用的资源（同时会清除拓扑并停止轮询）。调用后需要重新 `new WaylineEditor(...)` 才能继续使用。

---

## 五、创建航线

```typescript
editor.create(options?: { centerPoint?: { longitude: number, latitude: number } }): Promise<{ waylineId: string }>
```

调用后弹出航线创建对话框；用户选择航线类型并保存后 Promise resolve 返回 `{ waylineId }`。

- `centerPoint` 用于计算默认飞行高度，可不传
- 用户**取消**时 reject `Error('User cancelled')`，务必 `.catch()`
- **必须先 `mount()`**，未挂载时直接 reject（`Editor not mounted. Call mount() first.`）

```javascript
editor.create({ centerPoint: { longitude: 113.9, latitude: 22.6 } })
  .then(function (res) { console.log('创建成功:', res.waylineId) })
  .catch(function (err) {
    if (err && err.message === 'User cancelled') return
    console.error('创建失败', err)
  })
```

### 限制可选航线类型 `enabledWaylineTypes`

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | Waypoint | 航点航线 |
| 1 | Area | 面状航线 |
| 2 | Facade | 斜面航线 |
| 3 | Solid | 几何体航线 |
| 4 | MappingStripe | 带状航线 |
| 7 | MappingObject | 贴近摄影航线 |
| 8 | TargetDetection | AI 巡逻航线 |

### 限制可选机型 / 负载 `supportedDrones`

```javascript
var DRONE = {
  M30: 67, WM265: 77 /* M3 系列 */, M10: 91,
  WA345: 99 /* M4 系列 345 */, EA230: 100 /* M4D 系列 230 */, PM440: 103,
}
var PAYLOAD = {
  M30D: 52, M30T: 53,
  M3E: 66, M3T: 67, M3TA: 129,
  M10E: 80, M10T: 81,
  M4E: 88, M4T: 89,
  M4D: 98, M4TD: 99,
}

new window.WaylineEditor({
  // ...
  supportedDrones: [
    { drone: DRONE.M30, payloadList: [PAYLOAD.M30T, PAYLOAD.M30D] },
    { drone: DRONE.WA345, payloadList: [PAYLOAD.M4E, PAYLOAD.M4T] },
  ],
})
```

不传 / 空数组 = 内置全部机型。

---

## 六、编辑航线

```typescript
editor.render('edit', params: IRenderParams): void
```

### IRenderParams

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `waylineId` | string | Y | 待编辑的航线 ID。不传则回退到构造参数的 `waylineId` |
| `defaultPosition` | `{ longitude: number, latitude: number, height?: number }` | N | 航线无航点时地图居中的兜底位置 |
| `from` | string | N | 入口来源标记，用于数据上报 |

```javascript
editor.render('edit', {
  waylineId: '510a56b9-4d6d-4c49-bd48-05269cfb432d',
  defaultPosition: { longitude: 113.9, latitude: 22.6, height: 500000 },
})
```

> **必须先 `loadMap()`**：未加载地图时 `render()` **不会抛异常**，只会派发 `error` 事件（`code: 6002`）后静默返回。
>
> **必须先 `mount()`**：未挂载时 `render()` 直接静默返回，不发任何事件。

---

## 七、航线拼接（静态方法）

```typescript
WaylineEditor.concatWayline(name: string, waylineIds: string[]): Promise<string>
```

把多条航线首尾合并成一条新航线，返回新航线 UUID。

```javascript
var newId = await window.WaylineEditor.concatWayline('拼接航线-01', [
  'uuid-of-wayline-1',
  'uuid-of-wayline-2',
])
```

> **虽然是静态方法，但依赖已初始化的实例**。请在 `new WaylineEditor()` -> `loadMap()` -> `mount()` 之后再调用。OSS 前缀取自构造参数 `oss.objectKeyPrefix`。

### 拼接校验规则

| 规则 | 失败提示 |
|------|----------|
| 至少 2 条航线 | `航线拼接至少需要 2 条航线` |
| 仅支持航点航线 | `仅支持航点航线拼接` |
| 各航线的设备/负载类型必须一致 | `航线设备/负载类型不一致` |
| 使用"相对起飞点高度"模式的航线必须有起飞点信息 | `相对起飞点高度模式的航线缺少起飞点信息` |
| 各航线的第三方 AI 算法必须一致 | `第三方AI算法类型不一致` |

**合并语义**：全局参数（高度模式、全局飞行高度、飞行速度、偏航/转弯模式等）以第一条航线为准，其余航线的航点会按第一条的高度模式做换算。

---

## 八、设备拓扑连线

在地图上渲染设备（机场/飞机/中继/遥控器）及其连线关系。

**启用**：构造时传 `map.topo`（不传该字段则完全不加载拓扑插件）。

### topo 配置

| 字段 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `snList` | string[] | `[]` | 初始显示的设备 SN 列表，可用 `showDeviceTopo()` 运行时切换 |
| `pollMs` | number | `0` | 轮询间隔（ms），`0` 或不传则不轮询 |
| `showFullTopo` | boolean | `false` | `true` 初始显示单设备、点击后展开整条拓扑；`false` 始终只显示单设备，点击仅选中 |
| `highlightMode` | `'show-related'` \| `'dim-others'` \| `'always'` | `'show-related'` | 点击设备后连线的高亮方式 |
| `highlightStyle` | object | -- | 覆盖 `normal` / `highlight` / `dim` 三态连线样式（`cssColor` / `width` / `depthFailAlpha`） |
| `iconResolver` | function | -- | 设备类型 -> 图标映射，`(node) => ({ icon, selectedIcon, hoverIcon, labelIcon })` |
| `deselectOnClickEmpty` | boolean | `true` | 点击地图空白处是否取消选中 |

### 运行时接口

```typescript
editor.showDeviceTopo(sns: string[]): void       // 传空数组 = 清除拓扑并停止轮询
editor.selectedDeviceSn: string | null           // 只读，当前选中的设备 SN
WaylineEditor.getTopoList(): Promise<any[]>      // 静态方法，拉取全量设备拓扑列表
```

```javascript
// 显示指定设备的拓扑
editor.showDeviceTopo(['SN-A', 'SN-B'])

// 设备被点选时回调
editor.on('wayline-device-selected', function (data) {
  console.log('选中设备:', data.deviceSn)
})

// 清除拓扑（同时停止轮询）
editor.showDeviceTopo([])

// 拉取全量拓扑：每项含 host（飞机）与 parents（网关/机场）
var list = await window.WaylineEditor.getTopoList()
```

---

## 九、事件监听

```typescript
editor.on(eventName: string, callback: (data: any) => void): void
editor.off(eventName: string, callback: Function): void
```

### 事件列表

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `wayline-save` | `waylineId` | 航线保存成功 |
| `wayline-back` | `waylineId` | 用户点击返回 |
| `collision-back` | `waylineId` | **航线存在碰撞时的返回**，此时不会再派发 `wayline-back` |
| `wayline-cancel` | `waylineId` | 用户取消编辑 |
| `wayline-creation-saved` | `{ id: string }` | 创建航线保存成功（`create()` 已处理，一般无需自行监听） |
| `wayline-creation-cancel` | 无 | 创建航线被取消 |
| `jump-to-wayline-list` | 无 | 用户请求返回航线列表，可据此关闭编辑器 |
| `wayline-device-selected` | `{ deviceSn: string }` | 拓扑设备被点选 |
| `set-router-destroy-confirm-tips` | `boolean` | 编辑器脏状态变更，可用于拦截页面离开 |
| `click-to-user-manual` | `{ action: string }` | 用户点击"使用说明"按钮，可打开自己的帮助页 |
| `error` | `{ code: number, message: string, data?: any }` | SDK 运行时错误，错误码见下表 |

> **碰撞航线只发 `collision-back`**。如果只监听了 `wayline-back`，在存在碰撞的航线上会收不到返回事件。需要覆盖全部返回场景时，两个事件都要监听。

```javascript
editor.on('wayline-save', function (id) { console.log('航线已保存:', id) })
editor.on('wayline-back', closeEditor)
editor.on('collision-back', closeEditor)

editor.on('set-router-destroy-confirm-tips', function (isDirty) {
  // isDirty=true 时拦截页面离开
  window.onbeforeunload = isDirty ? function () { return '编辑未保存' } : null
})

editor.on('error', function (e) {
  console.error('[WaylineEditor error]', e.code, e.message, e.data)
})
```

---

## 十、错误码

| 错误码 | 名称 | 说明 |
|--------|------|------|
| 4001 | `WS_AUTH_ERROR` | WebSocket 鉴权失败（服务端返回 close code 4001） |
| 4002 | `WS_DISCONNECTED` | WebSocket 连接断开 |
| 5000 | `API_ERROR` | API 请求失败（网络错误或服务端 5xx，含航线详情拉取失败等） |
| 5001 | `API_AUTH_ERROR` | API 鉴权失败（401 / 403） |
| 6000 | `PARSE_ERROR` | 航线数据解析失败（KMZ 格式异常） |
| 6001 | `WASM_LOAD_ERROR` | WASM 模块加载失败 |
| 6002 | `MAP_INIT_ERROR` | 地图初始化失败（Cesium 加载异常） |
| 7000 | `UPLOAD_ERROR` | OSS 上传失败 |

---

## 十一、主题热切换

```typescript
editor.setTheme(theme: Partial<ITheme>): void
```

只传需要变更的字段，未传字段保持当前值。可在 `mount()` 之后随时调用。

```javascript
editor.setTheme({ primaryColor: '#00b96b', bgBasic: '#1a1a1a' })
```

---

## 十二、完整初始化示例

```javascript
var auth = {
  authKeys: ['user_id'],
  headers: function () {
    return { user_id: 'your-user-id' }   // 占位示例，请替换为实际平台分配的鉴权值
  },
}

var RUNTIME_DEPS_BASE = 'https://<INT_FLHUB_XX>/sdk-demo/sdk-runtime'   // <INT_FLHUB_XX> 为内网测试环境占位

var editor = new window.WaylineEditor({
  api: {
    baseUrl: 'https://<INT_FLHUB_XX>:30812/gateway',
    auth: auth,
  },
  ws: {
    url: 'wss://<INT_FLHUB_XX>:30812/openapi/v0.9/notification/ws/v1/notifications',
  },
  runtimeDepsBasePath: RUNTIME_DEPS_BASE,
  map: {
    mode: 'internal',
    env: {
      cesiumBaseUrl: RUNTIME_DEPS_BASE + '/cesium/',
      terrainKey: '',
      featureGateway: [],
    },
    topo: {
      snList: [],
      showFullTopo: true,
    },
  },
  oss: { objectKeyPrefix: 'pass_tests' },
  locale: { language: 'zh', messages: window.URANUS_LANGUAGE },
})

// 错误事件建议在 loadMap / mount 之前注册
editor.on('error', function (e) {
  console.error('[WaylineEditor error]', e.code, e.message, e.data)
})

// 起图并定位到深圳
await editor.loadMap(document.getElementById('map-app-global'), {
  longitude: 114.0579,
  latitude: 22.5431,
  height: 5000,
})

editor.mount(document.getElementById('uranus-wayline-app'))

// 编辑已有航线
editor.render('edit', {
  waylineId: '510a56b9-4d6d-4c49-bd48-05269cfb432d',
  defaultPosition: { longitude: 113.9, latitude: 22.6, height: 500000 },
})
```

---

## 十三、关键注意事项

1. **Cesium 必须同步加载且早于 SDK**，SDK 脚本用 `defer`。
2. **`#wayline-header` 必须在 `render()` 之前存在**，否则顶栏不渲染。
3. **同一页面不能同时挂多个 `WaylineEditor` 实例**，切航线时用"销毁后重建"模式。
4. **`concatWayline` 虽然是静态方法但依赖已初始化的实例**（`new` -> `loadMap()` -> `mount()` 之后才可调用）。
5. **`loadMap()` 不调则 `render('edit')` 会静默失败**（只派发 `error` code 6002，不抛异常）。
6. **用户取消创建会 reject**，务必在 `create()` 后挂 `.catch()`。
7. **碰撞航线返回只发 `collision-back` 不发 `wayline-back`**，两个事件都要监听。
