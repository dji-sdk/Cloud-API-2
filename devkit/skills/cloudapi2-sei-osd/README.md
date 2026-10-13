# cloudapi2-sei-osd — SEI/OSD 实时遥测数据接入助手

帮 ISV 开发者正确接入 DJI CloudAPI2 的 **SEI（帧级位置数据）** 和 **OSD（设备遥测数据）**，解决"怎么拿到每一帧画面对应的、准确的、足够高频的位置数据"这一最集中的 ISV 痛点。

## 能力

| 能力 | 说明 |
|------|------|
| **三层数据选型** | MQTT OSD（0.5Hz）vs DRC OSD（1~30Hz）vs SEI（帧级），自动判断推荐 |
| **服务端 SEI 接入** | FHSDK `LiveModule` + `subscribeArSeiInfo/FovSeiInfo` 完整生命周期 |
| **服务端 OSD 接入** | MQTT OSD 监听 + DRC 高频 OSD 配置 |
| **前端地图可视化** | Vue 3 + Cesium：飞机模型、相机视锥、FOV 落地、视频贴地投影 |
| **机型适配** | M10E/M3T/M4D/M4T 不同 SEI 协议 + 角度系数 + 畸变参数 |
| **ISV 排障** | SEI 拿不到、解析错误、OSD 频率不够等常见问题 |

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code
cp -r skills/cloudapi2-sei-osd ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-sei-osd .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-sei-osd .github/skills/
```

## 触发词

- SEI、OSD、帧级位置、帧同步、实时定位
- 视锥、FOV、相机姿态、视频投影
- ArSeiInfo、FovSeiInfo、subscribeArSeiInfo、subscribeFovSeiInfo
- LiveModule、DRC OSD、MQTT OSD、osd_frequency
- 遥测数据、位置对齐、像素反算经纬度、视锥中心点
- Cesium 地图、NED 坐标、相机位姿
- 畸变参数、HostDistortParam、CAM_COEFFICIENT

## 使用示例

### 场景 1：选择位置数据通道

```
你：我要在地图上实时显示无人机拍到的区域，OSD 够用吗？

AI：（自动加载 Skill）
→ OSD 0.5Hz 无法对齐视频帧
→ 推荐 SEI（~30fps，帧级同步）
→ 讲解前置条件（需开直播、WebRTC）
→ 给出 FHSDK 接入步骤
```

### 场景 2：服务端获取 SEI

```
你：用 FHSDK Java SDK 获取相机实时姿态

AI：（加载 server-side-guide）
→ 完整生命周期代码：
   init → getDrone → listenLiveAvailableRange → getLiveStream
   → live.start() → live.subscribeArSeiInfo(cb) → 处理回调
→ 退订清理代码
→ 提醒：必须 WebRTC、null guard、listenId 保存
```

### 场景 3：前端 Cesium 可视化

```
你：前端用 Cesium 显示无人机视锥和视频投影

AI：（加载 frontend-visualization）
→ 架构：后端 WS 推 SEI JSON → 前端 reactive state → Cesium 渲染
→ 代码：NED→ECEF 转换、四元数姿态、视锥线框、光线求交地面
→ 关键：坐标系 NED、高度基准 WGS84→EGM96
```

### 场景 4：OSD 频率不够

```
你：MQTT OSD 2 秒一次太慢，怎么提频？

AI：（加载排障路径）
→ MQTT OSD 固定 0.5Hz，不可调
→ 方案 1：DRC OSD（配 osd_frequency:30，最高 30Hz）
→ 方案 2：SEI（帧级，需拉流）
→ 根据用途推荐
```

### 场景 5：SEI 取不到

```
你：开了直播但 subscribeArSeiInfo 回调不触发

AI：（排障）
→ 检查 1：是否调用了 openDroneSeiMessageSend（ar_info_switch:1）
→ 检查 2：是否走 WebRTC（RTSP/声网拿不到）
→ 检查 3：设备/固件是否支持 SEI
→ 检查 4：LiveModule.start() 是否成功
```

## 核心约束

1. **SEI 必须走 WebRTC** — RTSP/声网/自建流拿不到
2. **subscribe/unSubscribe 配对** — listenId 必须释放
3. **LiveModule 前提** — SEI 前必须开直播成功
4. **DRC 前提** — DRC OSD 需先建立 DRC 链路
5. **不要追高频 MQTT OSD** — 固定 0.5Hz，需高频走 DRC/SEI
6. **机型适配** — 解析协议不同，角度系数不同
7. **null guard** — `getDroneInstance`/`getLiveStream` 可返回 null

## 三层位置数据速查

| 通道 | 频率 | 前置条件 | 用途 |
|------|------|----------|------|
| **MQTT OSD** | 0.5Hz | 设备在线即推 | 状态面板、粗轨迹 |
| **DRC OSD** | 1~30Hz（可配） | `connectDrcLink` | 操控反馈、中高频轨迹 |
| **SEI** | ~30fps | WebRTC 直播 | 画面+位置融合（AR/视锥/像素反算） |

## 目录结构

```
cloudapi2-sei-osd/
├── SKILL.md                          ← AI 主入口
├── constraints.yaml                  ← 约束规则
├── references/
│   ├── sei-osd-architecture.md       — SEI vs OSD 技术架构全梳理
│   ├── server-side-guide.md          — FHSDK 服务端获取 OSD/DRC/SEI（Java）
│   ├── frontend-visualization.md     — 前端 Cesium 地图可视化 Demo（Vue 3）
│   ├── faq-and-capability.md         — ISV 问题解答与 FHSDK 能力对照
│   ├── training-guide.md             — SEI/OSD 培训内容（概念+选型+接入）
│   └── isv-pain-points.md            — ISV 需求与困惑汇总（背景）
└── README.md                         ← 本文件
```

## 与其他 Skill 的关系

| Skill | 关系 |
|-------|------|
| `cloudapi2-sdk-core` | 本 Skill 依赖其核心规则（initialize/errorCode/null guard），SEI 接入是 SDK Skill 的场景特化 |
| `cloudapi2-frontend-component` | 前端组件 Skill。驾驶舱组件内部已处理 SEI/OSD，ISV 若用 PaaS 组件无需自行解析 |
| `cloudapi2-device-management` | OSD 数据用于设备状态展示时，与设备管理 Skill 有交集 |
| `cloudapi2-troubleshooting` | SEI/OSD 排障可路由到本 Skill 的排障章节 |

## 适用版本

- **FHSDK**: DJI CloudAPI2 SDK v2.0+（含 `LiveModule` + SEI 订阅 API）
- **设备**: 机场 3（M4D/M4TD/M4E）、M3T/M3E/M3TA、M10E/M10T
- **前端**: Chrome/Edge（SEI 解析需 Chromium），Vue 3 + Cesium（可视化）
