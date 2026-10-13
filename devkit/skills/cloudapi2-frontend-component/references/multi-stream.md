# 直播大屏（Multi-Stream）

直播大屏组件（`@dji/live-player`，CDN 全局名 `DJILivePlayer`）用于展示单路或多路设备的实时直播画面，内置 Agora / SRS-WHEP 两套拉流引擎。

> **与其他组件的关键区别**：本组件**完全独立**，不依赖 `paas.js`，不调用 `window.FH2` 任何方法，也**不受"同页面只能挂一个实例"的限制**——恰恰相反，多实例正是它拼出"直播大屏"网格的方式。

## 一、前置条件

- **无需** `paas.js`、`FH2.initConfig`、`FH2.initNotifyCenter`、`FH2.loadMap`
- 只需引入 SDK 自身两个文件

## 二、引入 SDK 资源

```html
<script src="http://[私有化部署地址]/paasjs-demo/multi-stream-cdn/sdk/sdk.umd.js"></script>
<link rel="stylesheet" href="http://[私有化部署地址]/paasjs-demo/multi-stream-cdn/sdk/style.css">
```

加载完成后，挂载在 `window.DJILivePlayer`，包含 `initLivePlayer` 函数和 `DjiLivePlayer` 类。

## 三、全局初始化

渲染任何播放器**之前必须调用一次**，多路播放器共用同一份配置：

```javascript
window.DJILivePlayer.initLivePlayer({
  serverUrl: 'http://example.flighthub.internal:30812', // 后端地址，不带 /gateway
  auth: {
    authKeys: ['X-User-Token'],
    headers: function () {
      return { 'X-User-Token': localStorage.getItem('x-user-token') || '' }
    },
  },
  locale: 'zh-CN', // 或 'en-US'
})
```

**initLivePlayer 参数表：**

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `serverUrl` | string | 是 | 后端服务地址，不带 `/gateway` 后缀 |
| `auth` | object | 是 | 鉴权配置对象 |
| `auth.authKeys` | string[] | 是 | 鉴权头 key 列表，如 `['X-User-Token']` |
| `auth.headers` | () => Record<string, string> | 是 | 返回鉴权请求头的函数 |
| `locale` | string | 否 | 语言，`'zh-CN'`（默认）或 `'en-US'` |

## 四、DOM 容器

每个播放器需要一个有**显式宽高**的容器：

```html
<div id="player-1" style="width:100%;height:100%"></div>
```

> 容器必须有确定的宽高（百分比、px、vh 均可），否则播放器无法渲染。

## 五、创建单路播放器

```javascript
var player = new window.DJILivePlayer.DjiLivePlayer({
  container: '#player-1',
  sn: '8UUXN5400A0776',
})

player.on('ready', function () { console.log('播放就绪') })
player.on('error', function (err) { console.error(err.code, err.message) })
```

**DjiLivePlayer 构造参数：**

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `container` | string | 是 | CSS 选择器，指向 DOM 容器 |
| `sn` | string | 是 | 设备序列号（机场/飞行器 SN 均可） |

## 六、组合成直播大屏（多路网格）

```javascript
var snList = ['8UUXN5400A0776', '7CTDL9K00A0040']
var players = {}

snList.forEach(function (sn) {
  var cell = document.createElement('div')
  cell.id = 'cell-' + sn
  cell.style.cssText = 'width:100%;height:100%'
  document.getElementById('screen-grid').appendChild(cell)

  players[sn] = new window.DJILivePlayer.DjiLivePlayer({
    container: '#cell-' + sn,
    sn: sn,
  })
})
```

**CSS Grid 布局（推荐）：**

```css
#screen-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);  /* 2列，可改为 repeat(3, 1fr) 等 */
  gap: 4px;
  width: 100%;
  height: 100vh;
}
```

## 七、事件监听

| 事件 | 回调参数 | 触发时机 |
|------|---------|---------|
| `ready` | — | 首帧渲染完成 |
| `error` | `LivePlayerError` (`{ code, message }`) | 播放异常 |
| `resolved` | `ResolvedInfo` | 设备解析完成 |
| `stateChange` | `LivePlayerState` | 播放器状态变更 |
| `deviceOffline` | — | 设备离线 |
| `close` | — | 用户点击关闭按钮 |

```javascript
player.on('error', function (err) {
  console.error('错误码:', err.code, '信息:', err.message)
})

player.on('stateChange', function (state) {
  console.log('状态变更:', state)
})

player.on('deviceOffline', function () {
  console.warn('设备已离线')
})
```

## 八、常用方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `refresh()` | void | 重新查询设备 + 尝试开播 |
| `retry()` | void | 手动重试（网络断开后使用） |
| `stop()` | void | 停止播放，回到 idle 状态 |
| `switchQuality(q)` | void | 切换画质（如 `'sd'`/`'hd'`/`'auto'`） |
| `destroy()` | void | 销毁播放器（幂等，可重复调用） |
| `getState()` | LivePlayerState | 获取当前状态快照 |

## 九、销毁

```javascript
// 销毁单个播放器
player.destroy()

// 销毁全部播放器
Object.keys(players).forEach(function (sn) { players[sn].destroy() })
```

## 十、错误码

> **重要**：本组件使用**独立的字符串错误码**，与 `WaylineEditor`/`FlyDataReplay` 共用的数字错误码（`4001/5000/6002...`）**不是**同一套体系。

| 错误码（string） | 含义 |
|------|------|
| `NOT_INITIALIZED` | 未调用 `initLivePlayer()`，必须先全局初始化 |
| `INVALID_SN` | SN 格式无效 |
| `DEVICE_NOT_FOUND` | 设备未找到（SN 不存在于平台） |
| `AMBIGUOUS_DEVICE_MATCH` | 多个设备匹配同一 SN，无法确定唯一设备 |
| `DEVICE_OFFLINE` | 设备离线 |
| `NO_LIVE_CAPACITY` | 设备无直播能力（如未配置直播模块） |
| `START_LIVE_FAILED` | 开播失败（平台侧拉流启动失败） |
| `TOKEN_EXPIRED` | Token 过期，需刷新鉴权 |
| `AUTH_FAILED` | 认证失败（Token 无效或权限不足） |
| `DUPLICATE_INSTANCE` | 同 SN/Camera/Video 重复实例（同一设备只能有一个播放器） |
| `DRTC_SDK_LOAD_FAILED` | DRTC SDK 加载失败 |

错误通过 `error` 事件回调返回：

```javascript
player.on('error', function (err) {
  // err.code 为上表字符串之一
  // err.message 为可读描述
  if (err.code === 'TOKEN_EXPIRED') {
    refreshToken().then(function () { player.retry() })
  }
})
```

## 十一、完整示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>直播大屏</title>
  <link rel="stylesheet" href="http://[部署地址]/paasjs-demo/multi-stream-cdn/sdk/style.css">
  <style>
    html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }
    #screen-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 4px;
      width: 100%;
      height: 100vh;
    }
    .grid-cell { width: 100%; height: 100%; }
  </style>
</head>
<body>
  <div id="screen-grid"></div>

  <script src="http://[部署地址]/paasjs-demo/multi-stream-cdn/sdk/sdk.umd.js"></script>
  <script>
    // 1. 全局初始化（只调一次）
    window.DJILivePlayer.initLivePlayer({
      serverUrl: 'http://YOUR_SERVER:30812',
      auth: {
        authKeys: ['X-User-Token'],
        headers: function () {
          return { 'X-User-Token': localStorage.getItem('x-user-token') || '' }
        },
      },
      locale: 'zh-CN',
    })

    // 2. 批量创建播放器
    var snList = ['DOCK-SN-001', 'DOCK-SN-002', 'DRONE-SN-003', 'DRONE-SN-004']
    var players = {}

    snList.forEach(function (sn) {
      var cell = document.createElement('div')
      cell.className = 'grid-cell'
      cell.id = 'cell-' + sn
      document.getElementById('screen-grid').appendChild(cell)

      var player = new window.DJILivePlayer.DjiLivePlayer({
        container: '#cell-' + sn,
        sn: sn,
      })

      player.on('ready', function () { console.log(sn, '播放就绪') })
      player.on('error', function (err) { console.error(sn, err.code, err.message) })
      player.on('deviceOffline', function () { console.warn(sn, '设备离线') })

      players[sn] = player
    })

    // 3. 页面卸载时销毁
    window.addEventListener('beforeunload', function () {
      Object.keys(players).forEach(function (sn) { players[sn].destroy() })
    })
  </script>
</body>
</html>
```

## 十二、注意事项

1. **initLivePlayer 必须在创建任何 DjiLivePlayer 实例之前调用**，否则所有实例会收到 `NOT_INITIALIZED` 错误
2. **同一个 SN 只能有一个播放器实例**，重复创建会触发 `DUPLICATE_INSTANCE` 错误
3. **容器必须有显式宽高**，否则播放器无法正确渲染
4. **与 paas.js 组件互不干扰**——可以在同一页面同时使用直播大屏和 FH2 驾驶舱，两者独立运行
5. 布局切换（如 2x2 → 3x3）只需修改 CSS Grid 的 `grid-template-columns`，播放器实例无需重建
