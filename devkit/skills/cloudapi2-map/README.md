# cloudapi2-map — 地图域 SDK 接入助手

帮集成商开发者正确编写调用 DJI Cloud API 2 Java SDK **地图域模块**（`SafetyServiceModule` / `ElementModule`）的集成代码，核心是避开"云端定义 ≠ 设备生效"这个全域最集中的坑。

## 能力

| 能力 | 说明 |
|------|------|
| **自定义飞行区** | 限飞区（nfz）/ 作业区（dfence）/ 禁降区（noland）的增删改查 + 下发同步 |
| **地形障碍物离线地图** | 模型生成、进度轮询、开关、下发同步 |
| **Flysafe 解锁证书** | 上传 → 校验 → 提交三段式流程 + 查询/开关/清除 |
| **信号地图** | 4G / SDR 覆盖网格查询（H3 level 11） |
| **地图元素** | 点 / 线（`LineString`）/ 面 / 圆（复用 `Point`+`radius`）的增删改查 + 分组层级 + 3 组 WS 订阅 |
| **状态同步语义** | 讲清"改云端"与"下发设备生效"的两段式流程，避免用户以为功能坏了 |

## 安装

推荐通过 `cloudapi2@dji` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code
cp -r skills/cloudapi2-map ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-map .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-map .github/skills/
```

## 触发词

- SafetyServiceModule、ElementModule
- 飞行区、限飞区、作业区、禁降区、nfz、dfence、noland
- createCustomizedFlightAreas、syncCustomizedFlightAreas、getFlightAreasSyncStatus
- 离线地图、offlineMaps、OfflineMapsModel
- 解锁证书、unlockLicense、unlock-license、uploadUnlockLicenseFile
- 信号地图、signalMaps、H3
- 地图元素、element、polygon、polyline、地图标绘、GeoJSON Feature

## 不触发

- 运行时直接调用 MCP Tool（`fh_flysafe` / `fh_unlock_license` / `fh_offline_map` 等）做运维操作 → `cloudapi2-mcp-guide`
- 通用 SDK 规则、飞控/航线任务/媒体 → `cloudapi2-sdk-core`
- 设备生命周期、固件升级、HMS 告警 → `cloudapi2-device-management`
- SEI/OSD 遥测、视频流与地图可视化融合 → `cloudapi2-sei-osd`

## 目录结构

```
cloudapi2-map/
├── SKILL.md                      # 触发条件、核心规则、决策树路由
├── constraints.yaml               # 编码约束（继承 _shared/constraints-common.yaml）
├── references/
│   ├── safety-service.md          # SafetyServiceModule 全部 22 方法
│   └── element.md                 # ElementModule 全部 11 方法
└── eval/
    ├── routing.yaml                # 触发/不触发边界测试
    ├── param.yaml                  # 参数/签名正确性测试
    ├── quality.yaml                # 回答质量测试
    └── trajectory.yaml             # 多步骤流程测试
```
