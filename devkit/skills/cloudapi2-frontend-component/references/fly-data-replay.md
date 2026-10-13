# 飞行记录回放（FlyDataReplay）

回放历史飞行任务：轨迹、参数面板、媒体照片、SDR 信号图、飞机视角、航线叠加、飞行状态/设备告警/AI 目标告警/视频录制/高程图时间轴、拓扑设备连线。

**核心变化（相比旧版）**：新版 `FlyDataReplay` **完全自包含**——内置 Cesium 地图（`internal` 模式），**不依赖 `paas.js`**，也不依赖 `FH2.initConfig` / `FH2.loadMap`。只需引入 Cesium + SDK 自身两个文件即可。`hostUrl` 参数已废弃，传入无效。

## 一、资源加载

回放组件需要加载 **Cesium** 和 **SDK 的 CSS/JS**。运行时依赖库（WPMZ/PPE 等 WASM）已打包进 SDK，实例化时自动注入，无需手动引入。

**Cesium 必须在 SDK `index.js` 之前同步加载**（SDK 启动时读取 `window.Cesium`），否则 `loadMap()` 抛 `MAP_INIT_ERROR (6002)`。

```html
<!-- 1. Cesium 全局（提供 window.Cesium） -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Widgets/widgets.css">
<script src="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Cesium.js"></script>

<!-- 2. FlyDataReplay SDK -->
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/fly-data-sdk/index.css">
<script defer src="http://[私有化部署地址]/paasjs-demo/fly-data-sdk/index.js"></script>
```

加载完成后，SDK 类挂载在 `window.FlyDataReplay`，Cesium 挂载在 `window.Cesium`。

构建产物 UMD，`FlyDataEvent` / `FlyDataErrorCode` 枚举**不挂 window**，事件名 / 错误码用字符串 / 数字字面量直接传。

## 二、DOM 骨架

`FlyDataReplay` 沿用 `fh2-container` 布局（多个内部组件通过 Vue `<teleport>` 投递到固定选择器），**完整骨架必须按下例提供**，否则地图不渲染、视频错位、轨迹/参数面板浮空。

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

**说明：**

1. `#project-middle-container` 与 `.right-micro-app` 必须是**相邻兄弟节点**（CSS `+` 选择器），中间不能插任何节点。
2. `.right-micro-app` -> `.maps-micro-app` -> `.map-app-placeholder` 三层不能扁平化。
3. `player.mount()` 的目标是 `#fly-data-app-container`，**不需要**内层包裹节点。
4. `#wayline-header` 是顶栏 teleport 目标；`#map-app-global` 是地图容器，由 SDK 内部 `internal` 模式（map-sdk `createMapApp`）接管——把它的选择器通过 `map.container` 传给 SDK 即可。

### 2.1 必备 CSS

地图与回放面板靠 flex 撑满父容器；缺任何一条会出现**地图不渲染 / 视频错位 / 参数面板浮空**。直接复制：

```css
.fh2-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  padding-top: 48px;        /* 给 #wayline-header 顶栏留位，按需调整，不要顶栏可改 0 */
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

/* fly-data SDK 容器：绝对定位覆盖在地图上，不占 flex 空间 */
.project-details .right-micro-app #fly-data-app-container {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
}

/* TeleportShim 传送到 .right-micro-app 的内容脱离文档流 */
.project-details .right-micro-app > .fly-data-root {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
  background: transparent;
}
```

末两条规则把 `#fly-data-app-container` 与 TeleportShim 节点 `.fly-data-root` 绝对定位浮在地图上层，`pointer-events: none` 让鼠标事件穿透到底层地图。

## 三、构造函数 `IFlyDataReplayOptions`

```ts
new FlyDataReplay(options: IFlyDataReplayOptions)
```

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `taskId` | string | Y | 飞行任务 ID（UUID 格式） |
| `api.baseUrl` | string | Y | 后端服务地址，例如 `http://<INT_FLHUB_XX>:30812/gateway`（`<INT_FLHUB_XX>` 为内网测试环境占位，开源 fork 请替换为自建环境域名） |
| `api.auth` | `IAuth` | Y | HTTP 鉴权配置 |
| `map` | object | N | 地图配置（替代旧版 `hostUrl` + FH2 依赖），见下表 |
| `runtimeDepsBasePath` | string | N | 运行时依赖库（WPMZ/PPE 等 WASM）资源目录，默认取 SDK 自身脚本所在目录 |
| `locale.language` | string | N | 国际化语言，默认 `zh` |
| `locale.messages` | object | N | 英文词条对象，格式 `{ en: enMessages }`，中文已内置无需传入 |
| `features` | object | N | 功能开关，控制各按钮和轨道的初始状态（见 五） |
| `sidebarItems` | array | N | 自定义侧边栏面板（见 六） |
| `renderTaskInfo` | function | N | 自定义任务信息区域（见 七） |
| `theme` | object | N | 主题配色（运行时 CSS 变量），可在 mount 后通过 `setTheme()` 热切换（见 十一） |
| `hostUrl` | string | N | **已废弃**，传入无效；请用 `map.env.cesiumBaseUrl` + `runtimeDepsBasePath` 替代 |

### 3.1 `map`（地图配置）

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `mode` | `'internal'` \| `'external'` | N | 地图模式，默认 `internal`（SDK 自建 Cesium 实例）；`external` 表示宿主已有地图 |
| `container` | string \| HTMLElement | N | `internal` 模式地图挂载容器，默认 `#fly-data-map`（示例中传 `#map-app-global`） |
| `env.cesiumBaseUrl` | string | N | Cesium 资源目录 URL，通常与引入 `Cesium.js` 的路径一致 |
| `env.terrainKey` | string | N | 地形鉴权 key，通常传空串 |
| `env.featureGateway` | array | N | 特性开关网关，一般传 `[]` |
| `notifyWsUrl` | string | N | 推送中心 WebSocket 地址，doodle 插件据此订阅推送 topic 实时更新地图 |
| `topo` | object | N | 拓扑设备连线配置，不传则不加载拓扑插件（见 十） |
| `mapAppState` | any | N | `external` 模式必填，宿主已起图的地图实例 |

### 3.2 `map.topo`（拓扑设备连线，可选）

| 字段 | 类型 | 说明 |
|---|---|---|
| `snList` | string[] | 初始显示的设备 SN 列表，可通过 `showDeviceTopo()` 运行时切换 |
| `pollMs` | number | 轮询间隔（ms），不传或 `0` 则不轮询 |
| `showFullTopo` | boolean | `true` 点击展开整条拓扑，`false`（默认）仅显示当前设备 |

### 3.3 `IAuth`

| 字段 | 类型 | 说明 |
|---|---|---|
| `authKeys` | string[] | 鉴权请求头的名称列表 |
| `headers` | `() => Record<string, string>` | 动态返回鉴权请求头，每次请求时都会调用 |

## 四、生命周期方法与调用顺序

| 方法 | 签名 | 说明 |
|---|---|---|
| `loadMap()` | `(container?: HTMLElement \| string) => Promise<void>` | `internal` 模式内部幂等，重复调用无副作用。**必须在 `mount()` 之前调用** |
| `mount(container)` | `(container: HTMLElement) => void` | 初始化 HTTP / Vue 应用并挂到容器 |
| `destroy()` | `() => void` | 卸载 Vue 实例、清空事件监听、释放所有资源 |
| `setLocale(locale)` | `(locale: 'zh' \| 'en') => void` | 运行时切换语言 |
| `setTheme(theme)` | `(theme: Partial<FlyDataTheme>) => void` | 运行时热切换主题 |
| `showDeviceTopo(sns)` | `(sns: string[]) => void` | 显示指定设备拓扑；传空数组清除并停止轮询 |
| `selectedDeviceSn` | getter: `string \| null` | 当前选中的设备 SN（点击 marker 后） |
| `on(event, h)` / `off(event, h)` | --- | 事件订阅 / 取消订阅 |

**静态方法：**

| 方法 | 签名 | 说明 |
|---|---|---|
| `FlyDataReplay.getTopoList()` | `() => Promise<any[]>` | 从后端拉取全量设备拓扑列表 |

**调用顺序**：`new` -> `on('error', ...)` -> **`await loadMap()`** -> `mount(#fly-data-app-container)`

## 五、功能开关 `features`

控制各按钮和轨道的**初始激活状态**（选中/未选中，而非显示/隐藏）。优先级：`features` > localStorage 用户缓存 > 代码默认值。

```ts
interface Features {
  playerBarBtn?: {
    params?: boolean       // 数据记录/参数面板，默认 true（与 media 互斥，同时 true 时 params 优先）
    media?: boolean        // 媒体记录，默认 false
    media_list?: boolean   // 媒体，默认 false
    sdr?: boolean          // SDR 信号图，默认 true
    fov?: boolean          // 飞机视角，默认 true
  }
  playerTrack?: {
    flight_status?: boolean       // 飞行状态轨道，默认 true
    device_warning?: boolean      // 设备告警轨道，默认 true
    ai_target_warning?: boolean   // AI 目标告警轨道，默认 false
    video_recording?: boolean     // 视频录制轨道：仅"有视频数据 且 此值为 true"才自动展开，传 false 则有视频也不展开，默认 true
    height_chart?: boolean        // 高程图轨道，默认 true
  }
}
```

```javascript
features: {
  playerBarBtn: { params: true, media: false, media_list: false, sdr: true, fov: true },
  playerTrack:  { flight_status: true, device_warning: true, ai_target_warning: false, video_recording: true, height_chart: true },
}
```

> 说明：`playerBarBtn.wayline` 字段已废弃，传入无效，无需再配置。

## 六、自定义侧边栏 `sidebarItems`

向播放器右侧侧边栏注入自定义面板：

```ts
interface SidebarItem {
  key: string                                    // 唯一标识
  icon: string                                   // 侧边栏图标（SVG 字符串）
  label: string                                  // 鼠标悬浮提示
  renderPanel: (taskData) => HTMLElement | string // 返回面板 HTML 内容
}
```

```javascript
sidebarItems: [
  {
    key: 'my-custom-panel',
    icon: '<svg viewBox="0 0 24 24">...</svg>',
    label: '我的面板',
    renderPanel: function (taskData) {
      return '<div style="padding:12px; color:#fff;">'
           + '<h3>工单号：WO-001</h3>'
           + '<p>任务 ID：' + taskData.taskId + '</p>'
           + '</div>'
    },
  },
]
```

`renderPanel(taskData)` 拿到的 `taskData` 是当前任务的元数据（`flight_id` / `aircraft_name` / `begin_time` / `cumulative_flight_time` 等）。

## 七、自定义任务信息 `renderTaskInfo`

覆盖 SDK 默认的任务信息展示，注入您自己的业务信息。返回字段**全部可选**：

```javascript
renderTaskInfo: function (taskData) {
  return {
    taskInfoTitle: '工单信息',       // 可选：覆盖任务信息标题
    baseInfoTitle: '工单详情',       // 可选：覆盖基本信息标题
    content: '<p>工单号：WO-001</p><p>负责人：张三</p>',  // 可选：自定义内容（HTMLElement 或字符串）
  }
}
```

## 八、事件监听

```ts
player.on(eventName: string, callback: (data: any) => void): void
player.off(eventName: string, callback: (data: any) => void): void
```

| 事件名 | 参数 | 说明 |
|---|---|---|
| `'back'` | 无 | 用户点击播放器返回按钮时触发 |
| `'wayline-preview'` | `{ waylineId: string, waylineName: string }` | 用户点击跳转航线时触发 |
| `'device-selected'` | `{ deviceSn: string }` | 拓扑设备 marker 被点击选中时触发（需配置 `map.topo`） |
| `'error'` | `{ code: number, message: string, data?: any }` | SDK 内部发生错误时触发 |

```javascript
player.on('back', function () {
  window.history.back()
})

player.on('wayline-preview', function (payload) {
  window.open('/editor/edit?id=' + payload.waylineId)
})

player.on('device-selected', function (payload) {
  console.log('选中设备：', payload.deviceSn)
})

player.on('error', function (e) {
  console.error('[FlyDataReplay error]', e.code, e.message, e.data)
  switch (e.code) {
    case 5001: // API 鉴权失败
      // 提示用户重新登录
      break
    case 6002: // 地图初始化失败
      // 提示地图加载失败
      break
    default:
      console.warn('错误 ' + e.code + '：' + e.message)
  }
})
```

**已知错误码：**

| 错误码 | 含义 |
|---|---|
| `5001` | API 鉴权失败，通常是 token 过期或无效 |
| `6002` | 地图初始化失败（Cesium 未加载或 `map.container` 找不到） |

## 九、切换语言

SDK 内置中文，英文需要加载 `en.json` 词条文件：

```javascript
// 初始化前加载英文词条
var _enMessages = {}
var _enReady = fetch('./en.json')
  .then(function (r) { return r.json() })
  .then(function (en) { _enMessages = en })
  .catch(function () { console.warn('英文词条加载失败') })

await _enReady
```

创建实例时传入：

```javascript
locale: { language: 'zh', messages: { en: _enMessages } }
```

运行时动态切换语言：

```javascript
player.setLocale('en')   // 切到英文
player.setLocale('zh')   // 切回中文
```

## 十、拓扑设备连线

配置 `map.topo` 后，可在地图上叠加设备拓扑连线（机场/飞机/中继/RC + 连线），并在运行时动态切换：

```typescript
player.showDeviceTopo(sns: string[]): void        // 传 SN 数组显示拓扑；传空数组清除并停止轮询
player.selectedDeviceSn: string | null            // getter：当前选中的设备 SN（点击 marker 后）
FlyDataReplay.getTopoList(): Promise<any[]>       // 静态方法：从后端拉取全量设备拓扑列表
```

```javascript
// 显示指定设备的拓扑
player.showDeviceTopo(['8UUXN5400A0776'])

// 清除拓扑
player.showDeviceTopo([])

// 拉取全量拓扑列表
var list = await window.FlyDataReplay.getTopoList()

// 监听拓扑设备选中
player.on('device-selected', function (payload) {
  console.log('选中设备：', payload.deviceSn)
})
```

## 十一、主题定制 `theme`

SDK 支持通过 `theme` 自定义配色。在初始化时传入，运行时可通过 `player.setTheme()` 热切换，立即生效无需重新挂载。

**theme 字段说明（12 个 token）：**

| 字段 | 说明 | 示例值 |
|---|---|---|
| `primaryColor` | 主色 | `#1890ff` |
| `primaryHoverColor` | 主色悬浮态 | `#40a9ff` |
| `primaryFocusColor` | 主色聚焦态 | `#096dd9` |
| `bgBasic` | 基础背景色 | `#232323` |
| `bgSub` | 次级背景色 | `#282828` |
| `bgModal` | 弹窗背景色 | `#1c1c1c` |
| `bgHighlight` | 高亮背景色 | `#101010` |
| `controlBg` | 控件背景色 | `#2a2a2a` |
| `controlHoverBg` | 控件悬浮背景色 | `#333333` |
| `borderColor` | 边框色 | `#4f4f4f` |
| `listActiveBg` | 列表选中背景色 | `rgba(24, 144, 255, 0.2)` |
| `listHoverBg` | 列表悬浮背景色 | `rgba(64, 169, 255, 0.15)` |

**初始化时传入：**

```javascript
theme: {
  primaryColor:      '#2d8cf0',
  primaryHoverColor: '#5cadff',
  primaryFocusColor: '#1a6fd1',
  bgBasic:           '#141e30',
  bgSub:             '#1b2a45',
  bgModal:           '#101828',
  bgHighlight:       '#0d1420',
  controlBg:         '#1e3050',
  controlHoverBg:    '#274060',
  borderColor:       '#2a4570',
  listActiveBg:      'rgba(45, 140, 240, 0.25)',
  listHoverBg:       'rgba(92, 173, 255, 0.18)',
}
```

**运行时动态切换：**

```javascript
player.setTheme({
  primaryColor: '#34d399',
  primaryHoverColor: '#5eead4',
  // ... 其他字段
})
```

> **注意**：`player.mount()` 会注入 SDK 默认的 CSS 变量，如果您通过 CSS 变量（`--fh2-primary-color` 等）自行覆盖了配色，需要在 `mount()` 之后重新设置。推荐使用 `theme` + `setTheme()` 方式，SDK 会自动处理优先级。
>
> Ant Design 组件主色为构建期 Less 编译，`setTheme()` 对 AntD 主色不生效；其它 `var(--fh2-*)` / `var(--fly-data-*)` 颜色会跟随。

## 十二、销毁与重建

```javascript
player.destroy()
```

销毁后释放所有资源。**同一页面不能同时挂多个 `FlyDataReplay` 实例**——`window.URANUS_LANGUAGE` / `mapAppState` 都是全局单例，多实例互相覆盖。切换 `taskId`、退出回放后再进入等场景用**销毁后重建**模式：

```javascript
async function recreatePlayer(oldPlayer, options) {
  if (oldPlayer) oldPlayer.destroy()
  var player = new window.FlyDataReplay(options)
  player.on('error', function (e) { console.error(e) })
  await player.loadMap()
  player.mount(document.getElementById('fly-data-app-container'))
  return player
}

// 用法
player = await recreatePlayer(player, { taskId: newTaskId, /* ... */ })
```

## 十三、完整初始化示例

```html
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>FlyDataReplay</title>
  <!-- 1. Cesium -->
  <link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Widgets/widgets.css">
  <script src="http://[私有化部署地址]/paasjs-demo/sdk-runtime/cesium/Cesium.js"></script>
  <!-- 2. FlyDataReplay SDK -->
  <link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/fly-data-sdk/index.css">
  <script defer src="http://[私有化部署地址]/paasjs-demo/fly-data-sdk/index.js"></script>
  <style>
    .fh2-container { display:flex; flex-direction:column; height:100vh; width:100vw; padding-top:48px; }
    .map-app-container > div { width:100%; height:100%; }
    .project-details { position:relative; width:100%; display:flex; flex:1; overflow:hidden; }
    .project-details .right-micro-app { position:relative; display:flex; flex:1; }
    .project-details .right-micro-app .maps-micro-app { display:flex; flex:1; height:100%; }
    .project-details .right-micro-app .map-app-placeholder { flex:1; position:relative; display:flex; flex-direction:column; height:100%; }
    .project-details .right-micro-app .map-app-container { height:100%; min-height:0; }
    .project-details .right-micro-app #fly-data-app-container { position:absolute; top:0; left:0; width:100%; height:100%; pointer-events:none; }
    .project-details .right-micro-app > .fly-data-root { position:absolute; top:0; left:0; width:100%; height:100%; pointer-events:none; background:transparent; }
  </style>
</head>
<body>
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

  <script>
    (async function () {
      // 加载英文词条（可选）
      var _enMessages = {}
      try {
        var resp = await fetch('./en.json')
        _enMessages = await resp.json()
      } catch (e) { console.warn('英文词条加载失败') }

      var auth = {
        authKeys: ['X-User-Token'],
        headers: function () {
          return { 'X-User-Token': localStorage.getItem('x-user-token') || '' }
        },
      }

      var player = new window.FlyDataReplay({
        taskId: '你的飞行任务ID',
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
          topo: {
            snList: [],
          },
        },
        locale: { language: 'zh', messages: { en: _enMessages } },
        features: {
          playerBarBtn: { params: true, media: false, media_list: false, sdr: true, fov: true },
          playerTrack: { flight_status: true, device_warning: true, ai_target_warning: false, video_recording: true, height_chart: true },
        },
        theme: {
          primaryColor: '#1890ff',
          bgBasic: '#232323',
        },
      })

      // 事件绑定（建议在 loadMap 之前绑 error）
      player.on('error', function (e) {
        console.error('[FlyDataReplay error]', e.code, e.message)
      })
      player.on('back', function () { window.history.back() })
      player.on('wayline-preview', function (p) {
        window.open('/editor/edit?id=' + p.waylineId)
      })
      player.on('device-selected', function (p) {
        console.log('选中设备：', p.deviceSn)
      })

      // 加载地图 + 挂载
      await player.loadMap()
      player.mount(document.getElementById('fly-data-app-container'))
    })()
  </script>
</body>
</html>
```

## 十四、React / Vue 集成要点

UMD 产物挂到 `window.FlyDataReplay`，框架内当普通全局类使用。

```jsx
// React 示例
import { useEffect, useRef } from 'react'

export function FlyDataReplayView({ taskId, options }) {
  const containerRef = useRef(null)
  useEffect(() => {
    const player = new window.FlyDataReplay({ ...options, taskId })
    player.on('error', e => console.error(e))
    player.loadMap().then(() => {
      player.mount(containerRef.current)
    })
    return () => player.destroy()
  }, [taskId])
  return <div ref={containerRef} id="fly-data-app-container" />
}
```

注意：
1. Cesium + SDK 资源走 `index.html` 同步 `<script>`，不要走 import / bundler。
2. DOM 骨架（`.right-micro-app` / `#map-app-global` 等）放**外层 layout**，不要放路由组件里，避免热更新时 teleport 目标丢失。

## 十五、设备兼容性（重要）

**飞行记录组件针对不同的大疆机场型号在功能表现上存在差异：**

| 设备型号 | 兼容性 |
|---|---|
| **大疆机场 1** | 仅支持飞行轨迹和任务详情，**不支持时间轴等其他功能** |
| **大疆机场 2** | 固件 `>= v14.01.04.07` 支持全部功能；否则同机场 1（仅轨迹 + 任务详情） |
| **大疆机场 3** | 所有固件版本支持全部功能 |

ISV 接入时若发现某些按钮 / 轨道不可用，先确认设备型号 + 固件版本，**不是 SDK bug**。
