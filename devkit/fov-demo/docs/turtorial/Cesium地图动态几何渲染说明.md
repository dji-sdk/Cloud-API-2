# Cesium 地图动态几何渲染说明

> Demo 地图上 SEI 三轴、光轴射线、测距连线、视锥等**高频更新**元素的 Cesium 渲染方式与选型说明。

## 目录

- [1. 背景：Demo 地图要画什么](#1-背景demo-地图要画什么)
- [2. 两种 Cesium 渲染路径](#2-两种-cesium-渲染路径)
- [3. 为什么 Entity 折线容易「点快线慢 / 线不显示」](#3-为什么-entity-折线容易点快线慢--线不显示)
- [4. Demo 采用的 Primitive 方案](#4-demo-采用的-primitive-方案)
- [5. 数据如何驱动刷新](#5-数据如何驱动刷新)
- [6. 视锥怎么画](#6-视锥怎么画)
- [7. 地面 footprint 与直播贴地](#7-地面-footprint-与直播贴地)
- [8. 选型建议（何时用 Entity、何时用 Primitive）](#8-选型建议何时用-entity何时用-primitive)
- [9. 常见问题](#9-常见问题)

---

## 1. 背景：Demo 地图要画什么

连接 DRC 且直播 SEI 有效时，地图上有大量**随 SEI 高频变化**的几何：

| 元素 | 更新频率 | 几何类型 |
|------|----------|----------|
| 相机三轴 | ~30 Hz | 3 条折线 |
| 光轴射线 | ~30 Hz | 1 条折线 |
| 光轴 / 激光落点连线 | ~30 Hz / ~10 Hz | 折线 |
| 视锥 | ~30 Hz | 视锥体 + 线框 |
| 视锥地面交线 | ~30 Hz | 黄色闭合折线 |
| 直播贴地（可选） | ~30 Hz | 细分三角网格 + WHEP 纹理 |
| 机场 / 飞机 / 落点 | 较低 | 点、模型 |

其中折线与视锥属于**动态几何**：端点或姿态几乎每帧都在变。这类场景对 Cesium 的**更新路径**很敏感，选型不当会出现刷新滞后，甚至整组线不显示。

坐标含义见同目录 [SEI相机坐标系说明.md](./SEI相机坐标系说明.md)。

---

## 2. 两种 Cesium 渲染路径

Cesium 提供两条常用路径，抽象层级不同：

### 2.1 Entity API（高层）

通过 `viewer.entities.add()` 声明「有一条线、一个点」。折线端点常用 `CallbackProperty`：Cesium 在渲染前**回调你的函数**取当前坐标。

```text
优点：写法简单，适合点、模型、低频标注
缺点：高频动态折线/视锥会走完整的 Entity 属性管线，更新时机不完全可控
```

### 2.2 Primitive API（偏底层）

通过 `viewer.scene.primitives.add()` 直接提交 `PolylineGeometry`、`FrustumGeometry` 等几何批次。数据变化时由业务代码 **remove 旧 Primitive → add 新 Primitive**（或仅在 dirty 时重建）。

```text
优点：更新链路短、行为可预期；视锥等复杂几何有原生 Geometry
缺点：需自行管理生命周期，代码量略多
```

可以概括为：**Entity 是声明式「描述对象」；Primitive 是命令式「管理几何块」**。并非 Primitive 画得更准，而是**谁负责、何时更新**更清楚。

---

## 3. 为什么 Entity 折线容易「点快线慢 / 线不显示」

Demo 早期曾用 `Entity.polyline` + `CallbackProperty` 从内存缓存读端点。实践中常见三类问题：

### 3.1 CallbackProperty 返回空数组

`show=false` 时在 `positions` 回调里 `return []`。Cesium 会**复用**上次返回的数组作为下次的 `result` 参数。再次显示时若 `result` 仍是空数组，端点写入失败，折线**永久不绘制**，而独立的 `Entity.point` 不受影响 → 出现「点还在、线没了」。

**原则**：`positions` 回调应始终返回至少两个有效 `Cartesian3`；显示/隐藏只用 `show` 控制，不要在 positions 里用空数组表达隐藏。

### 3.2 更新时机：属性求值 vs 业务写数据

Entity 动态属性依赖 Cesium 内部的**属性求值顺序**。若再用 `scene.preUpdate` 每帧全量刷新地图，会在一帧内多次触发求值与 Entity 更新，折线往往比「直接 `setValue` 的点」晚半拍，体感为**点跟手、线发粘**。

### 3.3 视锥无法用 Entity 简洁表达

标准视锥应使用 `FrustumGeometry` / `FrustumOutlineGeometry`，天然属于 Geometry + Primitive 用法。用 Entity 折线拼视锥既繁琐也不稳定。

---

## 4. Demo 采用的 Primitive 方案

Demo 将**高频折线与视锥**抽到 `SeiMapLayer`（`scene.primitives` 下的子集合），核心约定：

| 组件 | 职责 |
|------|------|
| `DynamicPolyline` | 一条折线；端点存入普通 `Cartesian3`；变化时 `sync()` 同步重建 Primitive |
| `DynamicFrustum` | 视锥半透明体 + 线框；姿态/FOV 变化时重建 |
| `DynamicGroundClosedPolyline` | 闭合贴地折线（视锥地面交线黄框，Classification TERRAIN） |
| `GroundVideoFootprint` | WHEP 贴地三角网格；仅「视频投影」开时更新 |
| `SeiMapLayer` | 统一管理三轴、光轴、测距线、视锥、footprint |

重建时使用 `asynchronous: false`，保证**同一帧**内几何已进入场景，避免异步加载带来的闪烁或延迟。

**仍用 Entity 的部分**（低频、API 更合适）：

- 机场 / 落点：`Entity.point`
- 飞机：`Entity.model`（glTF）

即：**动静分离**——高频几何 Primitive，低频标注 Entity。

---

## 5. 数据如何驱动刷新

```text
WebSocket（sei / drc_camera / mqtt_osd）
    ↓
Pinia osd 状态更新
    ↓
requestAnimationFrame 合并（一帧最多 updateMap 一次）
    ↓
updateMap：写 Cartesian3 端点 / 姿态 / FOV
    ↓
SeiMapLayer：端点或 FOV 变化 → sync() 重建 Primitive
    ↓
scene.requestRender()（若开启 requestRenderMode）
```

要点：

1. **事件驱动**，不在 `preUpdate` 里每帧轮询全量 `updateMap`。
2. **rAF 合并**：同一帧内多条 WS 消息只刷新一次地图。
3. **dirty 检测**：端点未变则跳过 rebuild，降低 30 Hz 下的 CPU 开销。

---

## 6. 视锥怎么画

视锥需要：**相机位置**、**NED 相机姿态**、**视场角 FOV**。

| 输入 | 来源 |
|------|------|
| 位置 | SEI 相机 WGS84（GND 偏移或飞机位置回退） |
| 姿态 | SEI `cameraYaw / cameraPitch / cameraRoll`（NED，光轴 = 本体系 +Z） |
| FOV | SEI `fovHorizontal / fovVertical / curActualFocalLength`（传感器物理量 μm） |

FOV 换算（与逆投影文档一致）：

```text
horizontalFov = 2 × atan( fovHorizontal / (2 × focalLength) )
verticalFov   = 2 × atan( fovVertical   / (2 × focalLength) )
```

相机光轴为本体系 **+Z**。`FrustumGeometry` 的 orientation 与 `DebugCameraPrimitive` 相同：旋转矩阵三列为 **−右、上（−下）、光轴**（Demo 用 `Matrix3.setColumn` 写入，避免列主序数组填错）。FOV 换算见下。

视锥默认远平面约 2000 m，与光轴辅助线尺度一致；FOV SEI 未到时使用默认垂直 FOV 占位，数据到达后自动切换。

---

## 7. 地面 footprint 与直播贴地

### 7.1 footprint 怎么算

1. 用与 `DynamicFrustum` 相同参数调用 Cesium 内部 `FrustumGeometry._computeNearFarPlanes`，得到**远平面四角**（与青色视锥严格对齐）。
2. 从 SEI 相机原点向四角各发一条射线，用 `pickRayGround`（`globe.pick` → 失败则椭球求交，且要求 `interval.start ≥ 0`）得地面四点。
3. `SeiMapLayer` 用 `DynamicGroundClosedPolyline` 画黄色闭合交线；**与「视频投影」开关无关**，只要 DRC + SEI 视锥在更新就会画。

相关文件：`seiFrustumFootprint.ts`、`seiRaycast.ts`。

### 7.2 直播贴地（默认关闭）

驾驶舱地图标题栏 **「视频投影」** 默认关（性能考虑）；需 **已开启直播** 才可勾选。

| 项 | 实现 |
|----|------|
| 几何 | 20×20 细分网格 + 显式 UV（TL/TR/BR/BL 与视频对齐）；`Primitive` + `polygonOffset` |
| 纹理 | 直接绑定 WHEP `<video>` 元素 |
| 高度 | `max(角点双线性高度, globe.getHeight)` + 0.5m；**不用** `sampleHeight` |
| 黄框 | `DynamicGroundClosedPolyline`：`GroundPolylinePrimitive` + `TERRAIN` |
| 开关 | `setMapVideoProjectionEnabled`；关时只隐藏贴图，黄框仍更新 |

**为何视频不用 `GroundPrimitive`**：Classification 贴地无法自定义 UV，纹理按经纬度包围盒映射，会导致画面上下/左右颠倒且与视锥不对齐；黄框仍走 Classification。

**为何不用 `scene.sampleHeight`**：会采到飞机 glTF，画面贴在机身上。

**Ion 地形关 / 椭球模式**：footprint 射线仍可用；贴图网格在椭球高度上内插，飞机离地后黄框与贴图均应正常（需视频投影开关打开才有贴图）。

---

## 8. 选型建议（何时用 Entity、何时用 Primitive）

| 场景 | 推荐 | 理由 |
|------|------|------|
| 静态或低频点、文字、模型 | Entity | API 简单，性能够用 |
| 直播 SEI 驱动的折线（>10 Hz） | Primitive | 更新可控、避免 CallbackProperty 坑 |
| 视锥、棱锥、贴地 footprint | Primitive | 原生 Geometry；贴图可用细分三角网格 + Image 材质 |
| 需要 infoBox、时间轴、KML 式描述 | Entity | 生态配套好 |

**经验法则**：若几何**每帧或每秒数十次**变，且对延迟敏感，优先 Primitive + 显式 sync；若**偶尔变一次**，Entity 足够。

---

## 9. 常见问题

1. **`CallbackProperty` 返回 `[]`**：见 §3.1，隐藏折线请用 `show: false`。
2. **`result` 数组复用**：回调里应保证 `result.length >= 2` 再写入，或自行分配新数组。
3. **每帧 remove/add 且无 dirty**：30 Hz 下 CPU 偏高；端点相等时应跳过重建。
4. **preUpdate 全量刷新**：与 WS 事件驱动叠加时易浪费；改为 rAF + 数据变更触发。
5. **混淆 MQTT 云台角与 SEI 欧拉角**：地图视锥/三轴应使用 SEI NED 角，见 [SEI相机坐标系说明.md](./SEI相机坐标系说明.md)。
6. **Primitive 忘记 destroy**：切换设备或卸载地图时需从 `scene.primitives` 移除集合并销毁，避免泄漏。
7. **`scene.sampleHeight` 采到飞机**：贴地网格应使用 `globe.getHeight` / `terrainSurfaceHeight`，勿用 `sampleHeight`。
8. **地面交线绑在视频开关上**：黄框应随视锥常开；贴图单独受「视频投影」控制。
9. **椭球求交未校验方向**：`rayEllipsoid` 的 `interval.start < 0` 表示相机后方，应丢弃，否则 footprint 全帧无效。
10. **Entity + CallbackProperty 贴 WHEP**：几何每帧重建易闪烁；Canvas 中转不一定触发纹理上传；推荐稳定 `Primitive` + 直接绑 `video`。
11. **`GroundPrimitive` 退化多边形**：角点过近会触发渲染崩溃；Demo 用边长/面积校验 + `createGeometry` 预检 + try/catch 跳过该帧。

---

## 相关文档

- [SEI相机坐标系说明.md](./SEI相机坐标系说明.md) — 三轴颜色、NED 含义、与 MQTT 云台角区别
- [2_前端方案.md](../2_前端方案.md) — Demo 前端整体结构与 Cesium 模块划分
