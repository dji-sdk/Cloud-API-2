# 主题定制（WaylineEditor / FlyDataReplay）

两个组件都用 `theme` 字段 + `setTheme()` 方法。**调色板字段结构一致**（12 个色板），但**类型名不同**（`WaylineTheme` vs `FlyDataTheme`），**官方默认值也不同**。

| 组件 | 初始化字段 | 类型 | 运行时方法 | CSS 变量前缀 |
|---|---|---|---|---|
| `WaylineEditor` | `theme` | `Partial<WaylineTheme>` | `editor.setTheme(theme)` | `--fh2-*` / `--dji-wl-*` |
| `FlyDataReplay` | `theme` | `Partial<FlyDataTheme>` | `player.setTheme(theme)` | `--fh2-*` / `--fly-data-*` |
| `UranusDeviceManagerApp` | — | — | — | — |
| `FH2.loadCockpit` / `loadDevice` / `loadWaylineView` | — | — | — | — |

**踩坑提醒：**

1. **Ant Design 组件主色为构建期 Less 编译**，`setTheme()` 对 AntD 主色**不生效**；其它走 `var(--fh2-*)` CSS 变量的颜色会跟随。
2. SDK **不提供预置主题**，只暴露定制接口。下方 `OCEAN_THEME` 仅作字段填法演示，颜色值由 ISV 自行决定。
3. **UMD 模式不暴露** `applyTheme` / `DEFAULT_THEME` 工具；只能用 `setTheme()`，或自行 `document.documentElement.style.setProperty('--fh2-primary-color', ...)`。
4. 同一份调色板可以直接喂给两个组件（结构一致），但**默认值不同** —— 不传的字段各自沿用自己的默认值。

## 一、字段（12 个色板）

12 个字段不必全填；缺省字段沿用各自默认值。

### `WaylineTheme` 默认值

| 字段 | 默认值 | 说明 |
|---|---|---|
| `primaryColor` | `#2d8cf0` | 主色（按钮主色 / 链接 / 强调态） |
| `primaryHoverColor` | `#5cadff` | 主色 hover 态 |
| `primaryFocusColor` | `#2b85e4` | 主色按下 / focus 态 |
| `bgBasic` | `#232323` | 第一基础底色（编辑器主背景） |
| `bgSub` | `#282828` | 第二基础底色（面板 / 侧栏） |
| `bgModal` | `#1c1c1c` | 弹窗内容背景 |
| `bgHighlight` | `#101010` | 输入框 / 高亮区背景 |
| `controlBg` | `#3c3c3c` | 暗色按钮底色（兼禁用底） |
| `controlHoverBg` | `#5d5f61` | 暗色按钮 hover 底 |
| `borderColor` | `#4f4f4f` | 输入框 / 分隔边框色 |
| `listActiveBg` | `rgba(45,140,240,.2)` | 列表项 active 背景 |
| `listHoverBg` | `rgba(92,173,255,.15)` | 列表项 hover 背景 |

### `FlyDataTheme` 默认值

| 字段 | 默认值 | 说明 |
|---|---|---|
| `primaryColor` | `#1890ff` | 主色（按钮主色 / 链接 / 强调态） |
| `primaryHoverColor` | `#40a9ff` | 主色 hover 态 |
| `primaryFocusColor` | `#096dd9` | 主色按下 / focus 态 |
| `bgBasic` | `#232323` | 第一基础底色（回放器主背景） |
| `bgSub` | `#282828` | 第二基础底色（面板 / 侧栏） |
| `bgModal` | `#1c1c1c` | 弹窗内容背景 |
| `bgHighlight` | `#101010` | 输入框 / 高亮区背景 |
| `controlBg` | `#2a2a2a` | 暗色按钮底色（兼禁用底） |
| `controlHoverBg` | `#333333` | 暗色按钮 hover 底 |
| `borderColor` | `#4f4f4f` | 输入框 / 分隔边框色 |
| `listActiveBg` | `rgba(24,144,255,.2)` | 列表项 active 背景 |
| `listHoverBg` | `rgba(64,169,255,.15)` | 列表项 hover 背景 |

**和 `WaylineTheme` 不一样的字段**：`primaryColor` / `primaryHoverColor` / `primaryFocusColor` / `controlBg` / `controlHoverBg` / `listActiveBg` / `listHoverBg`（基础底色 / 弹窗 / 高亮 / 边框 5 个字段一致）。

## 二、WaylineEditor 主题定制

### 初始化时指定

```javascript
new window.WaylineEditor({
  // ...其他配置
  theme: { primaryColor: '#ff6a00' },          // 只传需要变更的字段
})
```

### 运行时切换（mount 后任意时刻）

```javascript
editor.setTheme({ primaryColor: '#ff6a00', bgBasic: '#1a1a1a' })
```

## 三、FlyDataReplay 主题定制

### 初始化时指定

```javascript
new window.FlyDataReplay({
  // ...其他配置
  theme: { primaryColor: '#9f67ff' },
})
```

### 运行时切换

```javascript
player.setTheme({ primaryColor: '#9f67ff', bgBasic: '#1a1528' })
```

**避免坑**：`player.mount()` 会注入 SDK 默认 CSS 变量；如果通过 CSS 变量（`--fh2-primary-color` 等）直接覆盖配色，需要在 `mount()` 之后重新设置。**推荐直接用 `theme` + `setTheme()`**，SDK 自动处理优先级。

## 四、自定义主题示例（两个组件通用）

> SDK 不提供预置主题，下面是字段填法演示；颜色值按自家 UI 设计自行决定。

```javascript
var OCEAN_THEME = {
  primaryColor:      '#0f3460',
  primaryHoverColor: '#1a4f8a',
  primaryFocusColor: '#0a264a',
  bgBasic:           '#1a1a2e',
  bgSub:             '#16213e',
  bgModal:           '#0a0a1a',
  bgHighlight:       '#0a0a1a',
  controlBg:         '#16213e',
  controlHoverBg:    '#1a2745',
  borderColor:       '#333',
  listActiveBg:      'rgba(15, 52, 96, 0.4)',
  listHoverBg:       'rgba(26, 79, 138, 0.3)',
}

// 用在 WaylineEditor
new window.WaylineEditor({ /* ... */ theme: OCEAN_THEME })
editor.setTheme(OCEAN_THEME)

// 用在 FlyDataReplay
new window.FlyDataReplay({ /* ... */ theme: OCEAN_THEME })
player.setTheme(OCEAN_THEME)
```

## 五、跟随系统主题示例

两个组件用法一致：

```javascript
var LIGHT = { primaryColor: '#1677ff', bgBasic: '#ffffff', bgSub: '#f5f5f5' }
var DARK  = { primaryColor: '#1890ff', bgBasic: '#232323', bgSub: '#282828' }

function applyMode (instance, mode /* 'light' | 'dark' */) {
  instance.setTheme(mode === 'light' ? LIGHT : DARK)
}

window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function (e) {
  applyMode(editor, e.matches ? 'dark' : 'light')   // 或 player
})
```

⚠️ 同样受 AntD 构建期主题限制：Light/Dark 切换时，AntD 控件的主色不变；其它 `var(--fh2-*)` / `var(--fly-data-*)` / `var(--dji-wl-*)` 颜色会跟随。

`paasjs-demo/wayline-edit-sdk/create.html` 里的主题切换器是 Demo 自定义的几套配色，不是 SDK 内置主题；要复用直接照搬里面的对象字面量即可。
