# cloudapi2-ai — AI 域 SDK 接入助手

帮集成商开发者正确编写调用 DJI Cloud API 2 Java SDK **AI 域四个模块**（`AIBoxModule` / `OnboardAiModule` / `AIInsideManageModule` / `AiAlertModule`）的集成代码，核心是守住本域最贵的坑：**命名不规则 + 参数体系不统一**——`OnboardAiModule` 里 14 个方法首字母大写、AI 识别 6 个方法用 `generated` 包 snake_case 参数而非常规 bean、`AIBoxModule.getAIBoxModule()` 判空时不记日志（与另外三个 getter 行为相反）、`AiAlertModule` 的 `/drc/` 路径前缀不代表依赖 DRC 链路。

## 能力

| 能力 | 说明 |
|------|------|
| **AI Box 管理** | 查询信息、应用列表/安装/操作、导出文件、内部 STS 直传上传文件 |
| **机载 AI 识别控制** | 识别开关、过滤条件、分数阈值、分数模式、重置、模型选择（6 个 DRC action） |
| **AI 目标跟踪** | Spotlight Zoom 框选、确认、跟随切换、停止（5 个 DRC action） |
| **AI 主动告警（机载侧）** | 开启/关闭、模型配置、告警状态推送订阅（DRC + DRC Northbound） |
| **AI Inside 算法生命周期** | 算法列表、绑定设备查询、批量装载/卸载、私有化场景算法包上传（STS 直传） |
| **AI 目标检测告警（配置与查询）** | 设备级开关、算法级阈值 Upsert、告警架次统计、告警明细记录查询 |

## 安装

推荐通过 `cloudapi2@dji` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code
cp -r skills/cloudapi2-ai ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-ai .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-ai .github/skills/
```

## 触发词

- AIBoxModule、OnboardAiModule、AIInsideManageModule、AiAlertModule
- AI Box、机载 AI 盒子、fetchAIBoxInfo、installApp、operateApp
- AI 识别、AIIdentifySet、AIIdentifyFilterSet、AIIdentifyScoreSet、AIModelSelect
- AI 跟踪、DrcAISpotlightZoom、目标框选、目标跟随
- AI 主动告警（机载）、DrcAIAlarmStart、DrcAIModelsConfig
- AI Inside、机载算法、algorithmType、operateAIInsideModel、uploadAlgorithmFile
- AI 目标检测告警（配置/查询）、postDeviceSetting、postAlgorithmSetting、getAiAlertFlights、getAiAlertRecords

## 不触发

- 运行时直接调用 MCP Tool 做运维操作 → `cloudapi2-mcp-guide`
- 通用 SDK 规则、飞控/航线任务/相机云台 → `cloudapi2-sdk-core`
- 地图域（飞行区、离线地图、解锁证书、地图元素）→ `cloudapi2-map`
- PSDK 第三方负载控制（探照灯、喊话器、Widget）→ `cloudapi2-payload`
- 设备生命周期、固件升级、HMS 告警（非 AI 目标检测告警）→ `cloudapi2-device-management`
- SEI/OSD 遥测、视频流 → `cloudapi2-sei-osd`

## 目录结构

```
cloudapi2-ai/
├── SKILL.md                       # 触发条件、核心规则、决策树路由
├── constraints.yaml                # 编码约束（继承 _shared/constraints-common.yaml）
├── references/
│   ├── ai-box.md                   # AIBoxModule 全部 10 方法
│   ├── onboard-ai.md               # OnboardAiModule 全部 18 方法
│   ├── ai-inside.md                # AIInsideManageModule 全部 9 方法
│   └── ai-alert.md                 # AiAlertModule 全部 7 方法
└── eval/
    ├── routing.yaml                 # 触发/不触发边界测试
    ├── param.yaml                   # 参数/签名正确性测试
    ├── quality.yaml                 # 回答质量测试
    └── trajectory.yaml              # 多步骤流程测试
```
