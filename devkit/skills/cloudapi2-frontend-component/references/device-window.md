# 设备小窗（Device Window）

设备小窗以 SDK 方式拉起无人机 / 机场（dock）/ 基站设备的直播小窗浮层，无需集成完整的 Uranus 前端应用。调用方自行维护设备列表 UI，SDK 负责展示地图底图 + 设备位置标记 + 设备直播小窗浮层（视频流、飞行状态、任务栏、天气等）。

> **依赖模式与驾驶舱相同**：需要 `paas.js` + `FH2.initConfig` + `FH2.initNotifyCenter` + `FH2.loadMap`，**不是**独立 SDK。

## 一、前置条件

- 已加载 `paas.js`
- 已调用 `FH2.initConfig`
- 已调用 `FH2.initNotifyCenter(wssUrl, auth)`
- 已调用 `FH2.loadMap(serverUrl, auth)`
- 已在 `localStorage` 写入鉴权 token

## 二、DOM 容器与必备样式

### DOM 结构（**严格按此层级**）

```html
<div class="fh2-container">
  <div id="map-app-global" class="map-app-container"></div>
  <div id="project-app-container" style="display:none"></div>
  <div id="real-time-app-container"></div>
</div>
```

**关键 ID 说明：**

| ID | 用途 | 备注 |
|---|---|---|
| `map-app-global` | 地图容器（`FH2.loadMap` 挂载点） | 绝对定位撑满 |
| `project-app-container` | 数据中转层（复用驾驶舱路由） | **必须存在但必须隐藏**（`display:none`） |
| `real-time-app-container` | 小窗浮层容器（`loadDeviceWindow` 的 domId） | `pointer-events: none` 让地图操作穿透 |

### 必备 CSS

```css
.fh2-container { position: relative; width: 100vw; height: 100vh; }
#map-app-global { position: absolute; inset: 0; z-index: 1; }
#project-app-container { display: none; }
#real-time-app-container { position: absolute; inset: 0; z-index: 10; pointer-events: none; }

/* qiankun 微应用容器高度修复 —— 必须加，否则小窗内嵌应用塌陷 */
#__qiankun_microapp_wrapper_for_real_time_app__,
#__qiankun_microapp_wrapper_for_real_time_app__ > div {
  height: 100% !important;
  pointer-events: none;
}
#__qiankun_microapp_wrapper_for_real_time_app__ .real-time-wrapper {
  pointer-events: auto;
}
```

> **重要**：`#real-time-app-container` 上的 `pointer-events: none` 让地图交互能穿透空白区域，仅小窗本身响应点击。qiankun wrapper 样式不加会导致小窗塌陷为 0 高度。

## 三、加载设备小窗

```typescript
FH2.loadDeviceWindow(domId: string, params?: IDeviceWindowParams): Promise<DeviceWindowInstance[]>
```

**IDeviceWindowParams：**

| 字段 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `devices` | string | 否 | 逗号分隔 SN 字符串（机场/机库/飞行器 SN 均可），默认为空 |
| `map` | boolean | 否 | 是否加载地图，默认 `true` |
| `cameraAuthVerifyUrl` | string | 否 | EA340 基站外接摄像头验密页面地址，缺省用 SDK 自带 demo 维护页 |

**调用示例：**

```javascript
// 1. 初始化 FH2
window.FH2.initConfig({
  serverUrl: 'http://example.flighthub.internal:30812',
  wssUrl: 'ws://example.flighthub.internal:30812/openapi/v0.9/notification/ws/v1/notifications',
  hostUrl: 'http://example.flighthub.internal',
})

var auth = {
  authKeys: ['X-User-Token'],
  headers: function () {
    return { 'X-User-Token': localStorage.getItem('x-user-token') || '' }
  },
}

window.FH2.initNotifyCenter(
  'ws://example.flighthub.internal:30812/openapi/v0.9/notification/ws/v1/notifications',
  auth
)
window.FH2.loadMap('http://example.flighthub.internal:30812', auth)

// 2. 加载设备小窗
var windows = await window.FH2.loadDeviceWindow('real-time-app-container', {
  devices: 'DOCK-001,DOCK-002',
  map: true,
})
```

> `loadDeviceWindow` 入口异步、操作同步：`await` 返回的实例数组保证 micro-app 已挂载、设备拓扑已就绪，后续实例方法调用同步生效。

## 四、实例方法

每个返回的 `DeviceWindowInstance` 包含以下方法：

| 方法 | 参数 | 说明 |
|------|------|------|
| `setTask(task)` | `{ status: string, label?: string }` | 设置任务栏状态/标签；任务名称由 SDK 自动取机场名称填充，无需传入；不调用时显示"无任务" |
| `setHmsUrl(url?)` | string | 设置 HMS 健康告警入口跳转链接；传空字符串或不调用则隐藏该入口 |
| `setCockpitUrl(url?)` | string | 设置驾驶舱入口跳转链接；传空字符串或不调用则隐藏该入口 |
| `setOnlineWeather(data)` | `WeatherOnlineData` | 推送在线天气快照，立即生效；不调用时天气字段显示 `--` |

**使用示例：**

```javascript
windows[0].setTask({ status: '执行中', label: '12:30' })
windows[0].setHmsUrl('https://example.com/#/hms')
windows[0].setCockpitUrl('https://example.com/#/cockpit')
windows[0].setOnlineWeather({
  windSpeed: 3.5,
  precip: 0,
  temp: 22,
  humidity: 60,
  icon: 100,
  wns: 180,
  ionosphereLevel: '0',
})
```

## 五、WeatherOnlineData 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `windSpeed` | number | 风速，单位 m/s（>=10 m/s 黄色警告，超阈值红色阻飞） |
| `precip` | number | 降水量，单位 mm/h（0=无雨, (0,2)=小雨, [2,4)=中雨, >=4=大雨） |
| `temp` | number | 温度，单位 C（<=-20 黄色；<=-20 且湿度>=90% 联合黄色） |
| `humidity` | number | 相对湿度 0-100（与低温联合判定，>=90% 且低温时黄色） |
| `icon` | number | 天气状态码，沿用心知天气 icon 编码（如 `100`=晴 `300`=阵雨 `500`=薄雾） |
| `wns` | number | 风向角度 0-360（0=北, 90=东, 180=南, 270=西） |
| `ionosphereLevel` | string（可选） | 电离层活跃度：`-1`=无数据 `0`=平静 `1`=中等 `2`=强(黄色) `3`=剧烈(黄色) |

## 六、动态更新设备列表

```typescript
FH2.updateDeviceWindow(params: { devices: string }): void
```

全量替换当前设备列表（逗号分隔 SN 字符串），SDK 内部自动 diff，增删对应小窗。

> **切换设备优先用 `updateDeviceWindow`**，无需 `destroyDeviceWindow` 再重新 `loadDeviceWindow`，避免不必要的卸载重挂载开销和潜在的 unmount 报错。

```javascript
// 从 DOCK-001,DOCK-002 变更为只保留 DOCK-001
window.FH2.updateDeviceWindow({ devices: 'DOCK-001' })

// 新增 DOCK-003
window.FH2.updateDeviceWindow({ devices: 'DOCK-001,DOCK-003' })
```

## 七、销毁

```typescript
FH2.destroyDeviceWindow(self?: boolean): Promise<void>
```

| `self` 取值 | 行为 |
|------|------|
| `true` | 仅销毁小窗浮层，保留地图和 `project-app` |
| `false`（默认） | 同时销毁地图 + `project-app` + 小窗浮层 |

```javascript
// 销毁全部（地图 + project-app + 小窗）
await window.FH2.destroyDeviceWindow()

// 仅销毁小窗浮层，保留地图
await window.FH2.destroyDeviceWindow(true)
```

## 八、错误处理

> **暂无独立错误码，也没有 `error` 事件**（与 `WaylineEditor`/`FlyDataReplay`/直播大屏均不同）。

只能通过 `loadDeviceWindow(...).catch(err => ...)` 捕获初始化失败：

```javascript
try {
  var windows = await window.FH2.loadDeviceWindow('real-time-app-container', {
    devices: 'DOCK-001',
  })
} catch (err) {
  console.error('设备小窗加载失败:', err)
}
```

运行时问题需通过 Console 报错和 Network 面板排查。

## 九、已知限制

1. **WebSocket 通知能力使用 `initMock()` 空实现**，DRC/摇杆/PSDK 等实时通知功能暂不可用
2. **无独立错误码和 error 事件**，无法通过事件系统监听运行时错误
3. `project-app-container` 必须存在于 DOM 中（即使隐藏），否则内部路由注册失败

## 十、完整示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>设备小窗</title>
  <style>
    html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }
    .fh2-container { position: relative; width: 100vw; height: 100vh; }
    #map-app-global { position: absolute; inset: 0; z-index: 1; }
    #project-app-container { display: none; }
    #real-time-app-container { position: absolute; inset: 0; z-index: 10; pointer-events: none; }

    /* qiankun 微应用容器高度修复 */
    #__qiankun_microapp_wrapper_for_real_time_app__,
    #__qiankun_microapp_wrapper_for_real_time_app__ > div {
      height: 100% !important;
      pointer-events: none;
    }
    #__qiankun_microapp_wrapper_for_real_time_app__ .real-time-wrapper {
      pointer-events: auto;
    }
  </style>
</head>
<body>
  <div class="fh2-container">
    <div id="map-app-global" class="map-app-container"></div>
    <div id="project-app-container" style="display:none"></div>
    <div id="real-time-app-container"></div>
  </div>

  <script src="http://[部署地址]/paasjs-demo/paas.js"></script>
  <script>
    var auth = {
      authKeys: ['X-User-Token'],
      headers: function () {
        return { 'X-User-Token': localStorage.getItem('x-user-token') || '' }
      },
    }

    // 1. FH2 初始化
    window.FH2.initConfig({
      serverUrl: 'http://YOUR_SERVER:30812',
      wssUrl: 'ws://YOUR_SERVER:30812/openapi/v0.9/notification/ws/v1/notifications',
      hostUrl: 'http://YOUR_SERVER',
    })
    window.FH2.initNotifyCenter(
      'ws://YOUR_SERVER:30812/openapi/v0.9/notification/ws/v1/notifications',
      auth
    )
    window.FH2.loadMap('http://YOUR_SERVER:30812', auth)

    // 2. 加载设备小窗
    async function loadWindows() {
      try {
        var windows = await window.FH2.loadDeviceWindow('real-time-app-container', {
          devices: 'DOCK-SN-001,DOCK-SN-002',
          map: true,
        })

        // 3. 设置实例信息
        windows.forEach(function (win) {
          win.setTask({ status: '待命', label: '--' })
          win.setHmsUrl('https://example.com/#/hms')
          win.setCockpitUrl('https://example.com/#/cockpit')
          win.setOnlineWeather({
            windSpeed: 2.1,
            precip: 0,
            temp: 25,
            humidity: 55,
            icon: 100,
            wns: 90,
            ionosphereLevel: '0',
          })
        })
      } catch (err) {
        console.error('设备小窗加载失败:', err)
      }
    }

    loadWindows()

    // 4. 动态更新设备
    function switchDevices(snString) {
      window.FH2.updateDeviceWindow({ devices: snString })
    }

    // 5. 页面卸载时销毁
    window.addEventListener('beforeunload', function () {
      window.FH2.destroyDeviceWindow()
    })
  </script>
</body>
</html>
```

## 十一、注意事项

1. **初始化顺序严格**：`initConfig` → `initNotifyCenter` → `loadMap` → `loadDeviceWindow`，任一步骤缺失或乱序都会导致加载失败
2. **`project-app-container` 必须存在于 DOM**，即使隐藏也不能删除
3. **优先使用 `updateDeviceWindow` 切换设备**，避免 destroy + reload 的开销和潜在 unmount 报错
4. **qiankun wrapper CSS 必须添加**，否则小窗高度为 0 不可见
5. **`destroyDeviceWindow(true)` vs `destroyDeviceWindow(false)`**：如果后续还要加载其他 FH2 组件（如驾驶舱），用 `true` 保留地图；如果彻底退出，用 `false`（默认）
6. **天气数据需调用方自行获取并推送**，SDK 不会自动拉取天气信息
