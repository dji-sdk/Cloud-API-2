# cloudapi2-frontend-component — DJI 上云API 2 前端组件接入助手

帮 ISV 开发者在 **skeleton/web（Vue 3）** 项目或**独立 HTML 页面**中正确接入 DJI 上云API 2 4 大前端组件。

## 能力

| 组件 | 说明 |
|------|------|
| **WaylineEditor** | 航线创建/编辑/预览 |
| **FH2.loadCockpit** | 实时驾驶舱 |
| **UranusDeviceManagerApp** | 设备管理 |
| **FlyDataReplay** | 飞行记录回放 |

额外覆盖：`initConfig` 初始化、鉴权 token 注入、销毁/事件订阅、错误码排查、跨域/Mixed Content 调试、Whistle 代理配置。

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code
cp -r skills/cloudapi2-frontend-component ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-frontend-component .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-frontend-component .github/skills/
```

或保持在项目内，Claude Code 自动发现。

## 触发词

以下关键词出现在对话中时，Skill 自动加载：

- DJI 上云API 2、paas.js、FH2
- WaylineEditor、FlyDataReplay、UranusDeviceManagerApp
- loadCockpit、loadDevice、loadWaylineView、loadFlightPath
- initConfig、frontHost/serverHost/wssHost
- useFH2、ensureFH2Init、CockpitView、WaylineView、useSDKLoader

## 使用示例

### 场景 1：在 skeleton/web 中添加驾驶舱页面

```
你：帮我在 skeleton/web 里添加一个驾驶舱页面

AI：（自动加载 cloudapi2-frontend-component Skill）
→ 识别为 Vue 集成模式
→ 创建 CockpitView.vue，使用 ensureFH2Init() + FH2.loadCockpit()
→ 注册路由
→ 提醒：销毁时机、容器尺寸、协议一致性
```

### 场景 2：生成独立 HTML 页面

```
你：给我一个独立的航线预览 HTML 页面

AI：（加载 cloudapi2-frontend-component Skill）
→ 识别为独立 HTML 模式
→ 输出完整 HTML（含 paas.js 加载 + initConfig + WaylineEditor）
→ 标注必须配置的 3 个地址（frontHost/serverHost/wssHost）
```

### 场景 3：排查前端错误

```
你：WaylineEditor 白屏，控制台报 6002

AI：（加载排障参考）
→ 6002 = MAP_INIT_ERROR
→ 检查 paas.js 加载顺序、WebGL 支持
→ 给出 Whistle 代理调试步骤
```

## 核心约束（违反会导致组件异常）

1. **协议一致**：HTTPS 页面必须用 `https://` + `wss://`
2. **加载顺序**：`paas.js` → `initConfig` → `loadXxx`
3. **设备管理独立**：`UranusDeviceManagerApp` 不需要 `paas.js`
4. **容器尺寸**：DOM 容器必须有显式宽高
5. **销毁时机**：切页面前必须 `destroy()` 旧实例
6. **DOM 结构不可修改**：SDK 依赖特定 DOM ID
7. **单实例**：同页面不能挂多个 WaylineEditor 或 FlyDataReplay

## 目录结构

```
cloudapi2-frontend-component/
├── SKILL.md                  ← AI 主入口（触发词 + 工作流 + 约束）
├── constraints.yaml          ← 结构化约束规则
├── references/
│   ├── api-overview.md       — 4 组件初始化参数对照
│   ├── cockpit.md            — 驾驶舱 API
│   ├── device-manager.md     — 设备管理 API
│   ├── wayline-editor.md     — 航线编辑器 API
│   ├── fly-data-replay.md    — 飞行回放 API
│   ├── theme.md              — 主题定制
│   ├── troubleshooting.md    — 错误码 + 调试
│   └── skeleton-integration.md — skeleton/web 集成模式
├── snippets/
│   ├── vue-component-template.vue    — Vue 标准模板
│   └── standalone-html-template.html — 独立 HTML 模板
└── commands/
    ├── cockpit.md            — /cloudapi2-frontend-component:cockpit
    ├── device.md             — /cloudapi2-frontend-component:device
    ├── wayline.md            — /cloudapi2-frontend-component:wayline
    └── replay.md             — /cloudapi2-frontend-component:replay
```

## 适用版本

- **FH2 PaaS SDK**: v2.x（paas.js）
- **框架**: Vue 3 + Vite（skeleton/web）或纯 HTML
- **浏览器**: Chrome 90+（需 WebGL 支持）
