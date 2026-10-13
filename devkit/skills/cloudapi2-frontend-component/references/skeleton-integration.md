# skeleton/web 中的 FH2 集成架构

本文档描述 `skeleton/web`（Vue 3 + Vite + TypeScript）项目中集成 FH2 组件的标准模式。

## 架构分层

```
.env (VITE_FH2_ADDRESS / VITE_FH2_PORT / VITE_FH2_PRJ_ID)
  ↓
useFH2Config()         — 读取环境变量，返回 { address, port, baseUrl, wssUrl, prjId }
  ↓
ensureFH2Init()        — 单例加载 paas.js + 调用 FH2.initConfig()
  ↓
useSDKLoader()         — 管理 loading/ready/error 三态
  ↓
XxxView.vue            — 业务视图组件，调用 FH2.loadXxx() 挂载
```

## 关键文件

| 文件 | 职责 |
|------|------|
| `src/composables/useFH2Config.ts` | 从 `import.meta.env` 读取 FH2 服务地址和凭据 |
| `src/composables/useFH2.ts` | 单例加载 paas.js + 初始化 FH2 SDK |
| `src/composables/useSDKLoader.ts` | 通用 SDK 加载状态管理（loading/ready/error） |
| `src/components/SDKLoadingMask.vue` | 加载遮罩 UI 组件 |
| `src/types/fh2.d.ts` | FH2 全局类型声明（`window.FH2`） |
| `src/views/CockpitView.vue` | 驾驶舱示例 |
| `src/views/WaylineView.vue` | 航线编辑器示例 |

## 添加新 FH2 组件页面的标准步骤

### Step 1: 创建 View 组件

```vue
<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { ensureFH2Init } from '@/composables/useFH2'
import { useSDKLoader } from '@/composables/useSDKLoader'
import SDKLoadingMask from '@/components/SDKLoadingMask.vue'

const { status, errorMsg, markReady, markError } = useSDKLoader()

async function launch() {
  try {
    await ensureFH2Init()
    // 调用 FH2.loadXxx(containerId, options)
    // 订阅事件
    markReady()
  } catch (e: unknown) {
    markError(e)
  }
}

onMounted(launch)

onUnmounted(() => {
  // 必须：销毁实例 + 取消订阅
  try {
    window.FH2?.unsubscribe('xxx:event')
    window.FH2?.destroyXxx()
  } catch {
    // 忽略卸载阶段异常
  }
})
</script>

<template>
  <div class="xxx-page">
    <SDKLoadingMask :status="status" :error-msg="errorMsg" loading-text="加载中…" />
    <div v-show="status !== 'error'" class="fh2-container">
      <!-- FH2 SDK 要求的 DOM 结构（不可修改 ID） -->
      <div id="xxx-app-container" />
    </div>
  </div>
</template>

<style scoped>
.xxx-page { width: 100%; height: 100%; position: relative; }
.fh2-container { width: 100%; height: 100%; }
#xxx-app-container { width: 100%; height: 100%; }
</style>
```

### Step 2: 注册路由

在 `src/router/index.ts` 中添加：

```typescript
{
  path: '/xxx',
  name: 'XxxView',
  component: () => import('@/views/XxxView.vue'),
  meta: { title: 'Xxx', requiresAuth: true },
}
```

### Step 3: 配置环境变量

确保 `.env` 或 `.env.local` 中配置了：

```env
VITE_FH2_ADDRESS=http://your-fh2-server
VITE_FH2_PORT=30812
VITE_FH2_PRJ_ID=your-project-id
```

## 各组件的 DOM 结构要求

### 驾驶舱（Cockpit）

驾驶舱 DOM 结构最复杂，必须严格按 `CockpitView.vue` 模板：

```html
<div class="fh2-container">
  <div id="cockpit-header-container" />
  <div class="project-details router-cockpit">
    <div id="project-app-container" />
    <div id="project-middle-container" />
    <div id="project-right-micro-app" class="right-micro-app">
      <div class="maps-micro-app">
        <div class="cockpit-left-border-container" />
        <div id="project-map-app-placeholder" class="map-app-placeholder">
          <div class="cockpit-dock-live-container" />
          <div id="map-app-global" class="map-app-container" />
          <div class="cockpit-bottom-border-container" />
        </div>
      </div>
      <div class="project-right">
        <div id="cockpit-app-container" />  <!-- loadCockpit 挂载点 -->
      </div>
    </div>
  </div>
</div>
```

### 航线编辑器（WaylineEditor）

航线编辑器在 skeleton 中使用 UMD SDK 独立模式，不走 `FH2.loadWaylineView`。
参考 `WaylineView.vue` 的 `injectScriptUMD()` + `construct/mount/loadMap/render` 模式。

### 设备管理（DeviceManager）

设备管理使用 `FH2.loadDevice(containerId, options)` 模式：

```html
<div id="device-manager-container" style="width: 100%; height: 100%;" />
```

### 飞行回放（FlyDataReplay）

```html
<div class="fh2-container">
  <div class="right-micro-app">
    <div class="maps-micro-app">
      <div id="map-app-global" />
    </div>
  </div>
  <div id="fly-data-app-container" />  <!-- mount 挂载点 -->
</div>
```

## 鉴权 Token 注入

skeleton 中的鉴权 token 通过 `localStorage.getItem('x-user-token')` 获取。
FH2 SDK 的 auth 配置必须使用动态回调，确保每次取最新值：

```typescript
auth: {
  headers: () => ({
    'X-User-Token': localStorage.getItem('x-user-token') || '',
  }),
}
```

## 同路由参数变化处理

当用户在同一路由下切换参数（如 `/cockpit?drone_sn=AAA` → `?drone_sn=BBB`）时：

1. 使用 `watch` 监听 computed route params
2. 先销毁旧实例 + 取消订阅
3. 再重新调用 launch 函数

参考 `CockpitView.vue` 的 `watch([gatewaySn, droneSn], ...)` 模式。

## 错误处理

使用 `useSDKLoader` 的三态管理：
- `loading`：展示 `SDKLoadingMask` 加载动画
- `ready`：隐藏遮罩，展示 FH2 组件
- `error`：展示错误信息 + 重试/帮助提示

超时兜底：对于 SDK mounted 事件可能不触发的组件（如 Cockpit），设置 10s 超时后自动 markReady。
