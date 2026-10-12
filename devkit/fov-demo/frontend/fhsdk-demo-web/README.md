# fhsdk-demo-web

Vue 3 + Cesium + WHEP 前端。

完整运行依赖（Java 17、FFmpeg、Docker/Oryx、FHSDK 平台等）见项目根目录 [`README.md`](../../README.md) §依赖环境。

## 启动

```bash
# 先启动后端 :8080
npm install
npm run dev
```

浏览器：`http://localhost:5173`

Vite 已将 `/api` 代理到 `http://localhost:8080`（含 WebSocket）；`/oryx` 代理到本机 Oryx `:2022`（WHEP 播放）。

## 操控

- **杆量**：WASD 平移，R/F 升降，Q/E 偏航；需先连接 DRC
- **云台拖拽**：直播画面上按住拖动（需 DRC + 已开直播）
- **云台**：底部复位按钮（回中/向下/偏航回中/俯仰向下），需 DRC
- 操作流程：选设备 → ①连接 WS → ②开启直播 → ③连接 DRC（见 `docs/2_前端方案.md`）

### Cesium Ion

复制 `.env.example` 为 `.env.local`，填入 `VITE_CESIUM_ION_TOKEN`（已配置则无需改动）。修改后需重启 `npm run dev`。

地图标题栏开关：

- **Ion 地形**：Cesium World Terrain ↔ 椭球；无 Token 时不可用。
- **视频投影**：默认关；需先开启直播。开时才更新 WHEP 贴地网格（黄框不受此开关影响）。

顶栏 **「培训说明」**（默认开）：三层数据摘要、直播画面中心标注、地图图例、遥测表频率/脚注；关闭后仅保留核心 UI。

## 说明

- 布局与交互继承自 `../prototype/index.html`
- 直播 WHEP 依赖 Oryx；未启动时设备/OSD/地图仍可用，日志会提示 WHEP 失败
