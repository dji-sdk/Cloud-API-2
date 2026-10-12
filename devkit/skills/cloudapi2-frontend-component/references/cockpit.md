# 驾驶舱（Cockpit）

驾驶舱提供实时视频直播、飞行状态监控与飞行控制功能，并支持**模块显隐配置**（`cockpitConfig`）与**主题定制**（`theme`）。是所有组件里 **DOM 结构最复杂** 的一个 —— 必须严格遵循嵌套层级，否则视频/地图/侧栏会错位或不显示。

> **PaaS 鉴权说明**：驾驶舱依赖 FH2 全局加载流程。调用 `loadCockpit` 之前需先调用 `FH2.initNotifyCenter(wssUrl, auth)` 与 `FH2.loadMap(serverUrl, auth, wssUrl)`，FH2 内部会把 `auth` 缓存并自动透传给驾驶舱、project-app 子应用以及 packages 内置 request 单例 —— 所有 HTTP / WebSocket 请求都会自动带上 `auth.headers()` 返回的鉴权头（例如 `X-User-Token`）。调用方**无需**逐个接口配置鉴权，但**必须**在加载前自行 `localStorage.setItem('x-user-token', token)` 让闭包能取到 token。

## 一、前置条件

- 已加载 `paas.js`
- 已调用 `FH2.initConfig`（见下方第五节完整初始化流程）
- 已在 `localStorage` 写入鉴权 token（供下方 `auth.headers` 闭包读取）

## 二、DOM 容器（**严格按此结构**）

```html
<div class="fh2-container">
  <!-- 驾驶舱头部组件 teleport 目标 -->
  <div id="cockpit-header-container"></div>
  <div class="project-details router-cockpit">
    <!-- 项目数据中转层容器 -->
    <div id="project-app-container"></div>
    <!-- 页面中间区域容器 -->
    <div id="project-middle-container"></div>
    <div id="project-right-micro-app" class="right-micro-app">
      <div class="maps-micro-app">
        <div class="cockpit-left-border-container"></div>
        <div id="project-map-app-placeholder" class="map-app-placeholder">
          <div class="cockpit-dock-live-container"></div>
          <!-- 地图组件全局容器 -->
          <div id="map-app-global" class="map-app-container"></div>
          <div class="cockpit-bottom-border-container"></div>
        </div>
      </div>
      <div class="project-right">
        <!-- 驾驶舱组件容器（loadCockpit 的挂载目标） -->
        <div id="cockpit-app-container"></div>
      </div>
    </div>
  </div>
</div>
```

**关键 ID（必须存在，不能改名）**：

| ID | 用途 |
|---|---|
| `cockpit-header-container` | 驾驶舱头部 teleport 目标 |
| `project-app-container` | 项目数据中转层（拓扑/项目数据），必须存在 |
| `project-middle-container` | 中间区域容器 |
| `map-app-global` | 地图（FH2.loadMap 挂这里） |
| `cockpit-app-container` | 驾驶舱主体（loadCockpit 的 domId） |

## 三、关键 CSS（容器必须撑开）

```css
/* qiankun 微应用容器高度修复 —— 必须加，否则驾驶舱内容塌陷 */
#__qiankun_microapp_wrapper_for_cockpit_app__,
#__qiankun_microapp_wrapper_for_cockpit_app__ > div { height: 100% !important; }

.fh2-container { display: flex; flex-direction: column; height: 100vh; width: 100vw; }
#project-app-container > div, .map-app-container > div { width: 100%; height: 100%; }
.project-details { position: relative; width: 100%; display: flex; flex: 1; overflow: hidden; }
.project-details .right-micro-app { position: relative; display: flex; flex: 1; }
/* 地图加载坞：地图 micro-app 先 mount 于此再被搬进驾驶舱。
   用 flex:0 1 auto（不抢空间），避免地图隐藏/未搬运时空坞预留全屏占位。 */
.project-details .right-micro-app .maps-micro-app { display: flex; flex: 0 1 auto; height: 100%; }
.project-details .right-micro-app .map-app-placeholder { flex: 1; position: relative; display: flex; flex-direction: column; height: 100%; }
.project-details .right-micro-app .map-app-container { height: 100%; min-height: 0; }
/* 驾驶舱容器恒定占满剩余宽度（min-width:0 允许内部 flex 收缩） */
.project-right { display: flex; flex-direction: column; height: 100%; flex: 1; min-width: 0; }
#cockpit-app-container { height: 100%; }
```

> **注意**：`.maps-micro-app` 使用 `flex: 0 1 auto`（而非旧版 `flex: 1`），避免地图未搬运时空坞占位全屏。`.project-right` 新增 `flex: 1; min-width: 0;` 确保驾驶舱占满剩余宽度且内部 flex 可收缩。

## 四、加载驾驶舱

```typescript
loadCockpit(domId: string, params: ICockpitParams): {
  getVideoType(): any
  captureVideoFrame(params?: any): any
}
```

**ICockpitParams**：

| 字段 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `gateway_sn` | string | Y | 基站/机场序列号 |
| `drone_sn` | string | Y | 无人机序列号 |
| `map` | boolean | N | 是否显示地图，默认 `true` |
| `cockpitConfig` | CockpitConfig | N | 模块显隐 / 子功能开关 / 自定义按钮 / 布局配置，详见下方「模块显隐配置」 |
| `theme` | string \| object | N | 主题定制：内置预设名或扁平 token 对象，详见下方「主题定制」 |

返回对象的两个方法：

| 方法 | 说明 |
|---|---|
| `getVideoType()` | 获取当前视频类型 |
| `captureVideoFrame(params?)` | 捕获视频帧 |

## 五、模块显隐配置（cockpitConfig）

驾驶舱由 5 个顶层模块组成，通过 `cockpitConfig` 可以控制显隐和子功能：

| 模块 ID | 说明 |
|---|---|
| `topBar` | 顶栏（航线入口、自定义按钮等） |
| `mainLive` | 主视频区 |
| `subLive` | 辅视频区 |
| `map` | 地图 |
| `deviceList` | 设备列表（koc3 阶段默认隐藏，待后续放开） |

### 配置能力

- **整体显隐某个模块**：`modules.<id>.visible: false`
- **开关模块内的子功能**：`modules.<id>.features.*`（如隐藏顶栏航线入口、飞行操控区）
- **注入 ISV 自定义按钮**：`customButtons`（支持图标、跳转 URL 和点击回调）
- **调整布局比例**：`layout.splitRatios`
- **配置 ISV 跳转地址**：顶层字段 `waylinePlanUrl`（航线库）、`cameraAuthVerifyUrl`（外接摄像头验密页）

未配置的字段保持默认打开（**缺省全开原则**）。

### cockpitConfig 示例

```javascript
cockpitConfig: {
  modules: {
    deviceList: { visible: false },        // 设备列表模块隐藏
    topBar: {
      features: {
        wayline: true,                     // 顶栏航线入口
        customButtons: [                   // ISV 自定义按钮（渲染于顶栏右侧）
          {
            id: 'doc',
            name: '文档',
            tips: '打开 ISV 文档站',
            forwardUrl: 'https://isv.example.com/docs',   // 点击后新窗口跳转
          },
          {
            id: 'hi',
            name: '问候',
            iconUrl: 'https://cdn.example.com/icons/hi.png',
            cb: function () { alert('hello from ISV button') },   // 点击回调
          },
        ],
      },
    },
  },
  waylinePlanUrl: 'https://isv.example.com/wayline',   // ISV 航线库地址（顶层字段）
  // cameraAuthVerifyUrl: 'https://isv.example.com/camera-auth',  // 外接摄像头验密页
}
```

> **注意**：`customButtons` 的 `cb` 是函数，无法通过 JSON 传递。需要回调的按钮请在 `loadCockpit` 时通过代码传入；仅需跳转的按钮可用 `forwardUrl` 在 JSON 中配置。

### 动态更新 API

驾驶舱挂载后可以随时动态更新配置，UI 即时响应：

```javascript
// 运行时推送新配置（与默认配置做 explicit-only 深度合并）
window.FH2.setCockpitConfig({
  modules: {
    mainLive: {
      features: {
        flightControls: { visible: false },   // 动态隐藏飞行操控区（只读模式）
      },
    },
  },
})

// 读取最近一次写入的配置（只读快照）
var current = window.FH2.getCockpitConfig()
```

| 方法 | 说明 |
|---|---|
| `FH2.setCockpitConfig(config)` | 运行时推送新配置（深度合并），UI 即时响应 |
| `FH2.getCockpitConfig()` | 读取最近一次写入的配置快照（只读） |

## 六、主题定制（theme）

驾驶舱的主题体系基于一套 **snake_case 语义化设计 token**（约 90 个），支持三种定制方式：

1. **初始化时传入**：`loadCockpit` 的 `theme` 参数
2. **运行时切换**：`FH2.setCockpitTheme()` / `FH2.setCockpitPreset()` / `FH2.resetCockpitTheme()`，即时生效无需重新 `loadCockpit`
3. **可视化调试**：驾驶舱内置主题面板，支持逐个 token 调色并导出 JSON

### theme 参数的两种形态

| 形态 | 类型 | 说明 |
|---|---|---|
| 预设名 | string | 内置预设：`'default'`（默认深色）、`'tech-blue'`（科技蓝）。未知预设名回退到 `'default'` 并 console.warn |
| 扁平 token 对象 | `Record<string, string>` | key 为 token 名（snake_case），值为 CSS 颜色字符串。**支持部分覆盖**：只传需要修改的 token，缺失 key 用默认主题兜底；未知 key 忽略并 warn |

```javascript
// 方式一：使用内置预设
window.FH2.loadCockpit('cockpit-app-container', {
  gateway_sn: 'GW12345678',
  drone_sn: 'DV12345678',
  map: true,
  theme: 'tech-blue',
})

// 方式二：部分覆盖 token（只改品牌色，其余保持默认）
window.FH2.loadCockpit('cockpit-app-container', {
  gateway_sn: 'GW12345678',
  drone_sn: 'DV12345678',
  map: true,
  theme: {
    function_brand_primary_enabled: '#06b6d4',
    function_brand_primary_hover:   '#22d3ee',
    function_brand_primary_pressed: '#0891b2',
  },
})
```

### 运行时主题 API

| 方法 | 说明 |
|---|---|
| `FH2.setCockpitTheme(tokens)` | 通过扁平 token 对象覆盖主题（部分覆盖语义），即时生效 |
| `FH2.setCockpitPreset(preset)` | 切换内置预设，有效值 `'default'` / `'tech-blue'` |
| `FH2.resetCockpitTheme()` | 重置主题至默认预设 |

```javascript
// 运行时把品牌色换成 ISV 品牌绿
window.FH2.setCockpitTheme({
  function_brand_primary_enabled: '#34d399',
  function_brand_primary_hover:   '#5eead4',
  function_brand_primary_pressed: '#10b981',
})

// 切回内置科技蓝预设
window.FH2.setCockpitPreset('tech-blue')

// 恢复默认
window.FH2.resetCockpitTheme()
```

### Token 分组说明

token 命名规律为 `<分组>_<层级>_<状态>`（如 `bg_primary_hover`、`function_danger_primary_enabled`），完整清单可在驾驶舱主题面板中查看/导出。

| 分组前缀 | 数量 | 控制范围 | 示例 token |
|---|---|---|---|
| `bg_*` | 14 | 各层级背景色（主/次/三级 + hover/pressed/selected 态）、面板与工具栏蒙层 | `bg_primary_default`、`bg_panel_overlay` |
| `border_*` | 8 | 各层级边框色 | `border_primary_default`、`border_secondary_hover` |
| `component_*` | 14 | 控件（按钮/输入框等）各状态填充色 | `component_primary_enabled`、`component_tertiary_hover` |
| `divider_*` | 1 | 分割线 | `divider_primary_default` |
| `function_*` | 36 | 功能色：品牌色（brand）、危险（danger）、成功（success）、警告（warning）及对应半透明变体（opacity） | `function_brand_primary_enabled`、`function_danger_primary_hover` |
| `icon_*` | 6 | 图标色（主/次/三级 + disabled） | `icon_primary_default` |
| `mask_*` | 3 | 遮罩色 | `mask_primary_default` |
| `text_*` | 9 | 文本色（主/次/三级 + disabled + pressed） | `text_primary_default` |

### 常用定制场景速查

| 场景 | 需要覆盖的 token |
|---|---|
| 换品牌主色（按钮、选中态、链接） | `function_brand_primary_enabled` / `_hover` / `_pressed` / `_selected`，必要时加 `function_opacity_brand_*` 半透明变体 |
| 整体换背景基调 | `bg_primary_default`、`bg_secondary_default`、`bg_tertiary_default` 及对应交互态 |
| 调整文本对比度 | `text_primary_default`、`text_secondary_default`、`text_tertiary_default` |

### 内置主题面板（可视化调试）

驾驶舱内置了主题编辑面板，适合在接入阶段可视化调色：

- 面板支持切换内置预设、逐个 token 调色、实时预览
- 调好后可**导出 JSON**（扁平 token 键值对），该 JSON 与 `theme` / `setCockpitTheme` 的入参格式完全一致，可直接固化到 ISV 代码中随 `loadCockpit` 传入
- 也可从本地 JSON 文件导入主题：读取 JSON 解析为 token 对象后直接调用 `FH2.setCockpitTheme(tokens)` 即可

> **与页面级 CSS 变量的关系**：驾驶舱 token 会同步派生出 `--fh2-*` CSS 变量（如 `--fh2-primary-color`），与宿主页面通过 CSS 覆盖 `--fh2-*` 的旧方案兼容。推荐统一使用 token 方式，由主题引擎处理优先级与 portal（弹窗/下拉）同步。

## 七、完整初始化流程（含 notifyCenter + map 预加载）

> **⚠️ 三个必须正确的关键配置（否则驾驶舱黑屏或闪退）**：
>
> 1. **`hostUrl` 必须指向 FH2 前端服务器**（如 `https://host`，端口 80），**不能**用 `localhost`、`window.location.origin` 或 Vite dev server 地址。qiankun 微应用根据 `hostUrl` 解析所有内部资源路径（子 chunk、CSS、图片），设为 localhost 后嵌套资源全部 404，cockpit 挂载失败（黑屏）。
> 2. **`x-user-token` 必须是合法 JWT**（三段 base64url），**不能**是裸字符串如 `'paas_test'`。驾驶舱微应用内部用此 token 调 FH2 API，裸字符串过不了 JWT 校验，导致组件框架加载但内容全部空白。
> 3. **`auth.headers` 必须同时包含 `X-User-Token`（JWT）和 `user_id`**，`authKeys` 数组需声明两个 key。缺 `user_id` 导致部分接口鉴权不通过。

```javascript
window.addEventListener('load', function () {
  // 0. 在 init 之前把 token 写入 localStorage，让下方 auth.headers 闭包能读到
  localStorage.setItem('x-user-token', 'your-jwt-token')

  // 1. 初始化 FH2 基础配置（serverUrl / wssUrl / hostUrl 三项地址）
  window.FH2.initConfig({
    serverUrl: 'http://<your-server>:30812',
    wssUrl:    'ws://<your-server>:30812/openapi/v0.9/notification/ws/v1/notifications',
    hostUrl:   'http://<your-server>',
  })

  // 2. auth 对象（authKeys + 函数式 headers 闭包）
  var auth = {
    authKeys: ['X-User-Token', 'user_id'],
    headers: function () {
      return {
        'X-User-Token': localStorage.getItem('x-user-token') || '',
        'user_id': 'paas_test',
      }
    },
  }

  // 3. 初始化推送中心（传 auth，FH2 内部缓存为默认 auth）
  window.FH2.initNotifyCenter(
    'ws://<your-server>:30812/openapi/v0.9/notification/ws/v1/notifications',
    auth,
  )

  // 4. 加载地图。第 3 个参数为地图推送 ws 地址（独立于 initNotifyCenter），
  //    不传则地图不建立推送订阅（设备位置等地图元素不会实时更新）
  window.FH2.loadMap(
    'http://<your-server>:30812',
    auth,
    'ws://<your-server>:30812/openapi/v0.9/notification/ws/v1/notifications',
  )

  // 5. 加载驾驶舱：支持传入 cockpitConfig（模块显隐配置）和 theme（主题定制）
  var cockpitApi = window.FH2.loadCockpit('cockpit-app-container', {
    gateway_sn: 'YOUR_GATEWAY_SN',
    drone_sn:   'YOUR_DRONE_SN',
    map: true,
    cockpitConfig: {
      modules: {
        deviceList: { visible: false },
        topBar: {
          features: {
            wayline: true,
            customButtons: [
              {
                id: 'doc',
                name: '文档',
                tips: '打开 ISV 文档站',
                forwardUrl: 'https://isv.example.com/docs',
              },
            ],
          },
        },
      },
      waylinePlanUrl: 'https://isv.example.com/wayline',
    },
    // theme: 'tech-blue',   // 可选：主题定制
  })

  // 6. （可选）取视频类型
  console.log('视频类型:', cockpitApi.getVideoType())
})
```

### initConfig 字段说明

| 字段 | 类型 | 说明 |
|---|---|---|
| `serverUrl` | string | 后端网关地址（如 `http://host:30812`） |
| `wssUrl` | string | WebSocket 通知中心地址 |
| `hostUrl` | string | 前端静态资源地址（如 `http://host`） |

### loadMap 参数说明

```typescript
FH2.loadMap(serverUrl: string, auth: IAuth, wssUrl?: string): void
```

| 参数 | 类型 | 必需 | 说明 |
|---|---|---|---|
| `serverUrl` | string | Y | 后端网关地址 |
| `auth` | IAuth | Y | 鉴权配置对象 |
| `wssUrl` | string | N | 地图推送 ws 地址（第 3 个参数）。传入后地图会建立独立的推送订阅，设备位置等地图元素实时更新；不传则不建立推送订阅 |

## 八、卸载

```typescript
destroyCockpit(self?: boolean): Promise<boolean>
```

| `self` 取值 | 行为 |
|---|---|
| `false`（默认） | 卸载驾驶舱 + 地图 + 项目视图 |
| `true` | 仅卸载驾驶舱应用，保留地图和项目视图（适用于切机场不切页面） |

```javascript
window.FH2.destroyCockpit()
  .then(function () { console.log('驾驶舱已卸载') })
  .catch(function (err) { console.error('卸载失败:', err) })

// 仅卸载驾驶舱本身
window.FH2.destroyCockpit(true)
```

## 九、参考示例

| 示例 | 说明 |
|---|---|
| `paasjs-demo/cockpit.html` | 驾驶舱完整 Demo —— 含 DOM 骨架与样式、SN 输入弹窗、`cockpitConfig` 在线编辑面板（JSON 编辑器 + 字段标注）、主题面板 / 导入主题 JSON、`setCockpitConfig` 动态更新演示 |

## 十、常见问题

### Q1：进入驾驶舱后视频不显示
检查：
1. `gateway_sn` 和 `drone_sn` 是否正确（去设备管理页确认）
2. 无人机是否已和基站建联（红/黄/绿状态指示灯）
3. `wssUrl` 是否能连通（DevTools Network -> WS 看连接状态）

### Q2：地图区域空白
1. `#map-app-global` 容器是否存在且有高度
2. 是否提前调了 `FH2.loadMap()`（驾驶舱依赖 map 上下文）
3. WebGL 是否被禁用（驾驶舱用 Cesium，依赖 WebGL）
4. `loadMap` 第 3 个参数（wssUrl）是否正确 —— 不传则地图元素不实时更新

### Q3：从驾驶舱切其他页面后地图残留
切页面前必须 `await window.FH2.destroyCockpit()`（不传 `self`，让它清地图）。

### Q4：cockpitConfig 动态更新后 UI 未响应
- `setCockpitConfig` 采用 explicit-only 深度合并语义 —— 只有显式传入的字段才会变更，未传入的字段保持原值
- `customButtons` 的 `cb` 回调是函数，无法通过 JSON 序列化传递；需要回调的按钮必须在 `loadCockpit` 时通过代码传入

### Q5：主题 token 传入后部分颜色没变化
- 确认 token 名拼写正确（snake_case），未知 key 会被忽略（控制台有 warn）
- `function_*` 系列包含 opacity 半透明变体，修改品牌色时可能需要同时覆盖 `function_opacity_brand_*`
- 优先使用内置主题面板可视化调色 + 导出 JSON，避免手写遗漏

### Q6：点击进入驾驶舱后页面"闪退"（被跳转走）

**根因**：`loadCockpit` 触发 qiankun 加载驾驶舱微应用时，微应用内部可能执行 `window.location` 全页面跳转（而非 pushState），导致宿主 SPA 被卸载。这不是 bug，而是在非 FH2 门户宿主页面中集成驾驶舱时的已知行为。

**修复**：在 `loadCockpit` 调用前安装 `beforeunload` 事件守卫，用户在弹出的"离开此页面"对话框中点击"留下/取消"后，微应用继续在当前页渲染。`cockpit:mounted` 事件触发后移除守卫：

```javascript
// 安装守卫
function onBeforeUnload(e) { e.preventDefault() }
window.addEventListener('beforeunload', onBeforeUnload)

// 加载驾驶舱
window.FH2.loadCockpit('cockpit-app-container', { gateway_sn, drone_sn })

// cockpit:mounted 后移除守卫
window.FH2.subscribe('cockpit:mounted', function () {
  window.removeEventListener('beforeunload', onBeforeUnload)
})

// 兜底：15 秒后移除（防止事件未触发时守卫残留）
setTimeout(function () {
  window.removeEventListener('beforeunload', onBeforeUnload)
}, 15000)
```

> **注意**：此问题仅在 ISV 宿主页面（非 FH2 门户）中出现。FH2 门户本身已处理此跳转。确保 `hostUrl` 指向 FH2 前端服务器可减少跳转概率，但 `beforeunload` 守卫仍建议保留作为安全网。

### Q7：驾驶舱主题/配色不对（FH2 CSS 变量未生效）

**根因**：驾驶舱依赖宿主页面 `<body>` 上的 `set-change-color` class 来注入 `--fh2-*` CSS 自定义属性。

**修复**：进入驾驶舱页面时在 `<body>` 上添加 `set-change-color` class，并设置 FH2 主题 CSS 变量：

```css
body.set-change-color {
  --fh2-basic-bg-color: #111d33;
  --fh2-sub-bg-color: #152238;
  --fh2-primary-color: #3b82f6;
  --fh2-primary-hover-color: #3b82f6;
  --fh2-primary-focus-color: #3b82f6;
  --fh2-primary-disabled-color: #1e3a5f;
  --fh2-list-primary-active-color: rgba(59,130,246,.4);
  --fh2-list-primary-hover-color: rgba(59,130,246,.3);
  --fh2-sub-primary-color: #111d33;
  --fh2-sub-primary-hover-color: #1a2d4a;
  --fh2-model-content-bg-color: #0a1628;
  --fh2-input-highlight-color: #070f1e;
  --fh2-input-border-color: #334155;
  background: #0a1628 !important;
  color: #e2e8f0;
}
```

### Q8：驾驶舱黑屏（加载动画结束后空白）

按优先级排查：

1. **`hostUrl` 是否指向 FH2 前端服务器？** — 最常见原因。设为 `localhost` 或 Vite dev server 地址会导致微应用资源全部 404。必须是 FH2 前端地址（端口 80）。
2. **`x-user-token` 是否是合法 JWT？** — 裸字符串如 `'paas_test'` 过不了 JWT 校验，微应用内部 API 全部静默失败。
3. **`auth.headers` 是否同时包含 `X-User-Token` 和 `user_id`？** — 缺 `user_id` 导致部分接口鉴权不通过。
4. **`#cockpit-app-container` 是否有高度？** — 容器无高度则内容塌陷不可见。
5. **是否调了 `loadMap` 和 `initNotifyCenter`？** — 驾驶舱依赖 map 上下文和推送中心。
