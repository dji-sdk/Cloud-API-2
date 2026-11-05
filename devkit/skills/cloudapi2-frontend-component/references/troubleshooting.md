# 错误码 + 调试 + 排错

## 一、错误码总表

`WaylineEditor` 的数字错误码是**全集**（通过实例的 `on('error', cb)` 上抛）；`FlyDataReplay`
复用同一套码值，但集成商 API 文档只列出其中 **`5000 / 5001 / 6000 / 6002`** 四个，不要拿
`4001 / 4002 / 6001 / 7000` 去排查回放器。

`WaylineView` 用的是**同一数字空间、但语义不同的码**（例如 `6002` 在它是 WASM 运行时依赖加载失败，
不是地图初始化失败；地图初始化失败是 `6003`，见 `wayline-view.md` 第八节）。`DJILivePlayer`
用的是**字符串错误码**。三者不要混着排查。

下表按 **`WaylineEditor`** 语义列出；`WaylineView` 的差异在「触发场景」列单独标注。

| 错误码 | 名称 | 触发场景 | 处理建议 |
|---|---|---|---|
| **4001** | `WS_AUTH_ERROR` | **仅 `WaylineEditor`**：WebSocket 鉴权失败（服务端返回 close code 4001） | 检查 `ws.auth.headers()` 返回的 token 是否有效；引导用户重新登录 |
| **4002** | `WS_DISCONNECTED` | **仅 `WaylineEditor`**：WebSocket 连接断开（网络抖动 / 服务端主动断） | 应用层做重连提示，必要时重建 SDK 实例 |
| **5000** | `API_ERROR` | 三个数字码组件通用：API 请求失败（网络错误、服务端 5xx、航线详情拉取失败等） | 看 `e.data` 里的具体错误；区分网络问题 vs 服务端 bug |
| **5001** | `API_AUTH_ERROR` | 三个数字码组件通用：API 鉴权失败（401 / 403） | token 过期/无效，刷新或重新登录 |
| **6000** | `PARSE_ERROR` | `WaylineEditor` / `FlyDataReplay`：航线 KMZ 数据解析失败（格式异常）。**`WaylineView` 的 `6000` 是「航线 KMZ 下载失败」** | 解析失败是数据问题，联系平台侧排查；`WaylineView` 的下载失败先查网络面板的请求地址与状态码 |
| **6001** | `WASM_LOAD_ERROR` | **仅 `WaylineEditor`**：WASM 模块加载失败。**`WaylineView` 的 `6001` 是「航线 KMZ 解析失败」** | 检查 frontHost 是否能访问到 WASM 静态资源；CORS / MIME 是否正确 |
| **6002** | `MAP_INIT_ERROR` | `WaylineEditor` / `FlyDataReplay`：地图初始化失败：`Cesium.js` 未加载 / 未在 SDK 前同步加载 / 未调 `loadMap()` / `map.container` 找不到。**`WaylineView` 的 `6002` 是「WASM 运行时依赖加载失败」** | 检查 Cesium 是否**同步**加载且早于 SDK、`map.env.cesiumBaseUrl` 是否可达、WebGL 是否可用；渲染前必须 `await loadMap()`（`WaylineEditor` 漏调时只派发 6002 后静默返回，`FlyDataReplay` 的 `mount()` 会直接抛错） |
| **6003** | `MAP_INIT_ERROR` | **仅 `WaylineView`**：地图初始化失败（内部 Cesium 实例创建异常）。`WaylineEditor` / `FlyDataReplay` 地图初始化失败统一报 `6002` | 检查 Cesium 是否同步加载、`map.env.cesiumBaseUrl` 是否可达、WebGL 是否可用 |
| **7000** | `UPLOAD_ERROR` | **仅 `WaylineEditor`**：OSS 上传失败 | 检查 OSS 配置 / 网络 / 文件大小 |

通用错误处理模板：

```javascript
function handleSdkError(e) {
  console.error('[SDK error]', e.code, e.message, e.data)
  switch (e.code) {
    case 5001:
    case 4001: /* 鉴权失败 */ promptRelogin(); break
    case 4002: /* WS 断开 */ showReconnectToast(); break
    case 6001:
    case 6002: /* 资源/地图加载失败 */ showLoadFailMask(); break
    default:   /* 兜底 */ showToast('错误 ' + e.code + '：' + e.message)
  }
}

editor.on('error', handleSdkError)
player.on('error', handleSdkError)
```

## 二、常见跨域 / Mixed Content 问题

### Q：浏览器报 "Mixed Content: ... was loaded over HTTPS, but requested an insecure resource"

**根因**：页面是 HTTPS，但 `serverUrl` / `wssUrl` / `hostUrl` / SDK 资源用了 `http://` 或 `ws://`。

**修复**：
- 页面 HTTPS → 所有地址必须 `https://` + `wss://`
- 页面 HTTP → 所有地址也可以 HTTP，但不能 hostUrl 用 HTTPS 而页面用 HTTP（同样会 Mixed Content）
- 统一所有地址协议与页面一致

### Q：浏览器报 "Access-Control-Allow-Origin" 错误

**根因**：后端 CORS 未配置允许集成商域名。

**修复**：
- 联系平台侧配置后端 CORS
- 临时绕过：用 Whistle 把请求代理到本机（见下节）

### Q：`paas.js` 404

**根因**：`frontHost` 写错，或者前端服务没起来。

**修复**：浏览器直接访问 `<frontHost>/paas.js` 能下载到文件即说明地址正确。

### Q：SDK 资源（`wayline-edit-sdk/index.js` 等）404

**根因**：私有化部署没把 demo 目录放到前端服务上，或路径前缀错。

**修复**：浏览器直接访问 `<frontHost>/paasjs-demo/wayline-edit-sdk/index.js` 能下载即正确。也可以用 Whistle 把这个路径代理到本地（见 3.5）。

## 三、Whistle 本地代理调试

本地改 demo / SDK 时，把私有化部署上的资源路径代理到本机。

### 3.1 安装

```bash
# 需要 Node.js 18+
npm i -g whistle

# macOS 也可以：brew install whistle
# Windows 可下桌面客户端：https://github.com/avwo/whistle-client/releases
```

### 3.2 启动

```bash
w2 start
```

默认监听 `http://localhost:8899`。

### 3.3 安装根证书（HTTPS 抓包必需）

```bash
w2 ca
```

- macOS：可能需要输入开机密码或 Touch ID
- Windows：最后选"是 (Y)"确认

### 3.4 Chrome 切代理

推荐插件 **Proxy SwitchyOmega 3 (ZeroOmega)**：

1. 新建情景模式，名字随意（如 `whistle`）
2. 代理服务器：HTTP / `localhost` / `8899`
3. 后续点插件图标即可切换

### 3.5 配置代理规则

1. 在 demo 目录的 **父目录** 起静态服务器：

   ```bash
   npm i -g http-server
   http-server -p 8080   # 在 paasjs-demo 的同级目录
   ```

2. 浏览器打开 `http://localhost:8899/#rules`，加规则：

   ```
   # 把私有化部署上的 paasjs-demo 路径代理到本地
   https://<your-server>:10443/paasjs-demo/ http://127.0.0.1:8080/paasjs-demo/
   ```

3. Chrome 切到 whistle 代理 → 访问 `https://<your-server>:10443/paasjs-demo/index.html` 即加载本地文件。

**变体**：如果直接在 `paasjs-demo` 目录里起的 http-server，代理目标改成 `http://127.0.0.1:8080/`（末尾斜杠）。

## 四、WaylineEditor 专项 FAQ

### Q：`paas.js` 一定要在 SDK 之前加载吗？

**不需要，WaylineEditor 根本不加载 `paas.js`。** 它是**完全自包含**的 UMD bundle：内置 Cesium 地图（`internal` 模式），不依赖 `paas.js`，也不依赖 `FH2.initConfig` / `FH2.loadMap`。

唯一必须满足的加载顺序是 **Cesium 同步加载且早于 SDK**（`<script src=".../Cesium.js">` 不带 `defer`/`async`，SDK 本体用 `defer`）：

```html
<!-- ✅ 正确：Cesium 同步 → SDK defer -->
<link rel="stylesheet" href="https://[frontHost]/paasjs-demo/sdk-runtime/cesium/Widgets/widgets.css">
<script src="https://[frontHost]/paasjs-demo/sdk-runtime/cesium/Cesium.js"></script>
<link rel="stylesheet" href="https://[frontHost]/paasjs-demo/wayline-edit-sdk/index.css">
<script defer src="https://[frontHost]/paasjs-demo/wayline-edit-sdk/index.js"></script>
```

Cesium 没就绪 → `loadMap()` 内部 catch 后 `emit('error', { code: 6002 })` 并抛出（见下一条）。

### Q：进入 `render('edit', ...)` 时触发 `MAP_INIT_ERROR (6002)`

99% 是忘了 `loadMap()`。SDK 内部校验的是**自己的地图句柄**（`this.mapHandle`，或 `external` 模式下的 `options.map.mapAppState`）——**不是** `window.FH2.mapAppState`。

注意失败方式：`render()` **不会抛异常**，只派发一条 `error` 事件（`code: 6002`）后静默返回。所以不监听 `error` 就会表现为"点了没反应"。

```javascript
// ❌ 错误：没有 loadMap，render 静默返回 + 派发 error(6002)
editor.mount(container)
editor.render('edit', { waylineId })

// ✅ 正确
editor.on('error', function (e) { console.error(e) })   // 必须监听
editor.mount(container)
await editor.loadMap()
editor.render('edit', { waylineId })
```

**`create()` 不依赖地图**，可以跳过 `loadMap()`。

### Q：WebSocket 鉴权失败（`WS_AUTH_ERROR 4001`），token 过期怎么处理？

`api.auth.headers` 写成**函数形式**，SDK 每次请求 / WS 重连都实时取值：

```javascript
var auth = {
  authKeys: ['X-User-Token'],
  // ❌ 静态对象：token 过期后不会自动更新
  // headers: { 'X-User-Token': localStorage.getItem('x-user-token') },
  // ✅ 函数形式：实时取最新值
  headers: function () { return { 'X-User-Token': localStorage.getItem('x-user-token') || '' } },
}
```

配合监听做跳登录：

```javascript
editor.on('error', function (e) {
  if (e.code === 4001 || e.code === 5001) refreshTokenOrRedirect()
})
```

### Q：同一页面能挂多个 `WaylineEditor` 实例吗？

**推荐单实例。** 页面级挂载点是**固定选择器**：顶栏通过 Vue `<teleport>` 投递到 `#wayline-header`，编辑面板只认 `#project-middle-container` / `#project-middle-container + .right-micro-app > .maps-micro-app`。两个实例会往**同一批节点**里投递，UI 互相覆盖，地图也不会各画各的。

要"切换不同航线"用**单实例 + `destroy()` 后重建**模式（见 `wayline-editor.md` 1.11）。

### Q：能在 React / Vue 项目里集成吗？

**可以。** UMD 产物挂到 `window.WaylineEditor`，框架里当全局类用。

```jsx
useEffect(() => {
  const editor = new window.WaylineEditor(options)
  editor.mount(ref.current)
  return () => editor.destroy()
}, [])
```

注意：
- SDK `index.js`（和它依赖的 `Cesium.js`）仍**走 `index.html` 同步 `<script>`**，不要走 `import` / bundler。
- DOM 骨架（`#wayline-header` / `#project-middle-container` / `.right-micro-app` 等）放外层 layout，**别放路由组件**里，避免热更新时 teleport 目标丢失。

### Q：`editor.setTheme()` 改完主色，AntD 按钮颜色没跟着变

AntD 组件主色是**构建期 Less 编译**死的，热切换对 AntD 主色不生效。其它 `var(--fh2-*)` 颜色会跟随。**这是已知限制，不是 bug。**

### Q：`editor.create()` 被 `.catch()` 抓到 `Error('User cancelled')`

正常行为 —— 用户在创建弹窗里点取消时，`create()` 返回的 Promise reject `Error('User cancelled')`。区分用户取消 vs 真错误：

```javascript
editor.create().catch(function (err) {
  if (err && err.message === 'User cancelled') return
  console.error(err)
})
```

## 五、FlyDataReplay 专项 FAQ

### Q：`paas.js` 一定要在 SDK 之前加载吗？

**不需要，`FlyDataReplay` 根本不加载 `paas.js`。** 和 WaylineEditor 一样是**完全自包含**的 UMD bundle：内置 Cesium 地图（`internal` 模式），不依赖 `paas.js`，也不依赖 `FH2.initConfig` / `FH2.loadMap`、不读 `window.FH2.mapAppState`。

唯一必须满足的加载顺序是 **Cesium 同步加载且早于 SDK**（`Cesium.js` 不带 `defer`/`async`，SDK 本体用 `defer`）：

```html
<!-- ✅ 正确：Cesium 同步 → SDK defer -->
<link rel="stylesheet" href="https://[frontHost]/paasjs-demo/sdk-runtime/cesium/Widgets/widgets.css">
<script src="https://[frontHost]/paasjs-demo/sdk-runtime/cesium/Cesium.js"></script>
<link rel="stylesheet" href="https://[frontHost]/paasjs-demo/fly-data-sdk/index.css">
<script defer src="https://[frontHost]/paasjs-demo/fly-data-sdk/index.js"></script>
```

### Q：进入 `mount()` 报 `MAP_INIT_ERROR (6002)`

99% 是忘了 `loadMap()`。**`FlyDataReplay` 的 `loadMap()` 始终必需**（不像 WaylineEditor 仅 edit 需要），且必须在 `mount()` **之前**调用。

注意失败方式**和 WaylineEditor 不同**：`mount()` 会 `emit('error', { code: 6002 })` **并且抛出异常**（`Error('[FlyDataReplay] mount() 失败：地图尚未就绪，请先调用 loadMap()')`），所以 try/catch 和 `on('error')` 都要有。

```javascript
// ❌ 错误
var player = new window.FlyDataReplay(opts)
player.mount(container)               // 抛 6002

// ✅ 正确
var player = new window.FlyDataReplay(opts)
player.on('error', function (e) { console.error(e) })
await player.loadMap()
player.mount(container)
```

### Q：切 `taskId` 时是否需要重新 `loadMap()`？

**需要**。切 `taskId` 必须**销毁旧实例 + 重新 new + 重新 loadMap + 重新 mount**（不要复用旧 player 也不要跳过 loadMap）。

```javascript
async function switchTask (newTaskId) {
  if (player) { player.destroy(); player = null }
  player = new window.FlyDataReplay({ taskId: newTaskId, /* ... */ })
  player.on('error', function (e) { console.error(e) })
  await player.loadMap()
  player.mount(document.getElementById('fly-data-app-container'))
}
```

### Q：能在一页同时挂多个 `FlyDataReplay` 实例吗？

**推荐单实例。** 回放组件同样依赖页面级固定挂载点（`#wayline-header` 顶栏、`#project-middle-container + .right-micro-app` 内的节点），多实例会往同一节点重复投递、互相覆盖。要"切任务"用单实例 + `destroy()` 后重建。

### Q：`mount()` 要传什么容器？

直接传 `#fly-data-app-container` 本身：

```javascript
player.mount(document.getElementById('fly-data-app-container'))
```

### Q：能在 React / Vue 项目里集成吗？

**可以**（同 WaylineEditor）。UMD 产物挂到 `window.FlyDataReplay`，框架里当全局类用：

```jsx
useEffect(() => {
  const player = new window.FlyDataReplay({ taskId, ...options })
  player.on('error', e => console.error(e))
  player.loadMap().then(() => player.mount(ref.current))
  return () => player.destroy()
}, [taskId])
```

注意 `Cesium.js` + SDK `index.js` 走 `index.html` `<script>`，DOM 骨架放外层 layout。

### Q：`features` 默认值？

```text
playerBarBtn.params:   true
playerBarBtn.media:    false
playerBarBtn.sdr:      false   (注意：不是 true)
playerBarBtn.fov:      true
playerBarBtn.wayline:  true
playerTrack.flight_status:  true
playerTrack.device_warning: true
```

### Q：FlyDataReplay 的错误码是不是和 WaylineEditor 一样？

**码值空间相同，但不是全集。** `FlyDataReplay` 的集成商 API 文档只列出四个：
`5000`（`API_ERROR`）、`5001`（`API_AUTH_ERROR`）、`6000`（`PARSE_ERROR`）、`6002`（`MAP_INIT_ERROR`）。
`4001 / 4002 / 6001 / 7000` 是 `WaylineEditor` 专有的，不要拿去排查回放器。
最常用的是 `5001`（鉴权失败）和 `6002`（地图初始化失败，见上面 `mount()` 那一条）。

### Q：`setLocale()` 支持哪些语言？

`'zh'` / `'en'` / `'es'`。`locale` 字段也可以传简写字符串 `locale: 'zh'`，或对象 `locale: { language: 'zh', messages: { /* 覆盖词条 */ } }`。

### Q：`renderTaskInfo` / `renderPanel` 能 return DOM 元素吗？

**可以**。两个回调都接受 `HTMLElement` 或 HTML 字符串返回值。优先用 DOM 元素，可以保留事件监听 + 避免 XSS 风险。

### Q：回放页媒体面板里照片点开全黑、视频进度条停在 `0:00`、下载没反应

**不是你的代码问题，也不是存储问题** —— 是媒体取流地址指向了对象存储的**内网端口 `:30802`**，客户网段不可达。

先看最强的那条信号：**媒体列表和缩略图是正常的，只有点开文件才失败**。因为列表走 FH2 网关 `:30812`（客户网段通），而取流直连 `:30802`（客户网段不通）。这个组合把故障锁定在取流这一跳。

**为什么集成商侧改不掉**：媒体面板由 FH2 托管的 `FlyDataReplay` UMD 渲染（`/paasjs-demo/` 下，不在你的仓库里）。它只吃 `api.baseUrl` + `api.auth`，然后自己拼详情接口、把返回的 `url` **原样**赋给 `<video src>` / `<img src>`。没有地址改写钩子，也没有可注入的 HttpClient —— 详情接口返回什么，浏览器就直连什么。

注意媒体面板默认是**关的**（`playerBarBtn.media` / `media_list` 默认 `false`，见上文 `features` 默认值），只有显式打开才会碰到这个问题。

**判定**：拿详情接口返回的那个绝对 URL 原样请求前 1KB，回 `206 Partial Content` + `Accept-Ranges: bytes` 就说明存储是健康的；再从客户网络 `nc -vz <存储主机> 30802` 不通 —— 两条合起来定案。

**修复方向在平台侧**：媒体详情接口的 `url` 应返回客户可达的对外地址（对象存储对外映射，或统一经 FH2 网关 `:30812` 反代）。

完整排查清单（含判定命令与提单话术）见 `cloudapi2-troubleshooting` 的 `references/replay-media-troubleshooting.md`。

> 对照：skeleton 自带的**媒体管理页**（`/media`）不走这条路 —— 预览/播放/下载统一经本服务代理端点 `/api/v1/media/{fileId}/content` 回源，客户网段只需可达本服务。

## 六、其他常见问题

### Q：驾驶舱黑屏 / 闪退（三大必查项）

| 症状 | 根因 | 修复 |
|---|---|---|
| 黑屏（加载动画后空白，`cockpit:mounted` 不触发） | `hostUrl` 设为 localhost / Vite dev server，qiankun 微应用子资源全部 404 | `hostUrl` 必须指向 FH2 前端服务器地址（端口 80），如 `https://host` |
| 黑屏（框架加载但内容空白） | `x-user-token` 不是合法 JWT（如裸字符串 `'paas_test'`），微应用内部 API 鉴权全部失败 | 使用合法 JWT token |
| 闪退（点击后页面被跳转走） | `loadCockpit` 触发 qiankun 微应用执行 `window.location` 全页面导航 | 在 `loadCockpit` 前安装 `beforeunload` 事件守卫，`cockpit:mounted` 后移除（详见 `cockpit.md` Q6） |

> 完整驾驶舱排错指引见 `cockpit.md` 常见问题（Q1–Q8）。

### Q：地图不显示 / 黑屏
1. 容器 ID 是否正确（特别是驾驶舱的 `#map-app-global`）
2. 容器是否有显式宽高（`width:100%; height:100%` + 父容器有高度）
3. `wssUrl` 是否可连通（DevTools Network → WS）
4. 浏览器是否启用 WebGL（`chrome://gpu`）

### Q：同时加载多个组件互相打架
不同组件用 **独立的容器 ID**，并按文档准备好对应 DOM 结构。SDK 会自动管理生命周期。但 **不要在同一个容器里轮流挂不同组件** —— 先 `destroy` 旧的再挂新的。

### Q：切页面后浏览器卡 / 内存涨
未销毁的 SDK 实例 + 未释放的 Cesium viewer 占大量内存。统一在 `beforeunload` 或路由切换前调 `instance.destroy()`。

### Q：driver/payload 枚举值（67/77/91/99/100/103 …）怎么记
不必记，查 `wayline-editor.md` 1.7 的 `DRONE` / `PAYLOAD` 常量表。

### Q：用户 token 怎么注入
demo 都从 `localStorage.getItem('x-user-token')` 取。生产环境一般：
- 集成商登录后把 token 写进 localStorage / cookie
- `auth.headers()` 每次请求读最新值
- token 刷新后 **无需重建 SDK**，因为 `headers` 是函数

### Q：飞行记录某些功能不可用
不是 bug。先确认是大疆机场 1/2/3 哪种，固件版本 ≥ `v14.01.04.07` 才支持时间轴等高级功能。详见 `fly-data-replay.md` 第十三节。
