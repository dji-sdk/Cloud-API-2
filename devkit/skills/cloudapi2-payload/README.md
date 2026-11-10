# cloudapi2-payload — PSDK 负载域 SDK 接入助手

帮集成商开发者正确编写调用 DJI Cloud API 2 Java SDK **PSDK 负载模块**（`PsdkControlModule` / `PsdkResourceModule`）的集成代码，核心是守住本域最贵的坑：**能力边界**——源码里有方法被注释关闭（实时喊话）、有类 javadoc 声明但从未实现（抛投器）、有枚举保留值但无对应控制接口（`MegaphoneWorkMode` 的 3/4/5）。

## 能力

| 能力 | 说明 |
|------|------|
| **PSDK 设备发现** | 设备列表 / 状态摘要 / 状态子集订阅（DRC Northbound），拿到 `int psdkIndex` |
| **探照灯控制** | 开关、亮度、模式（常亮 / 频闪 / 快频闪 / 交替频闪）、校准、微调、广角、灯随云台、灯类型 |
| **喊话器控制** | 播放音频 / TTS、重播、停止、音量、播放模式、TTS 参数（音量 / 音色 / 语言 / 语速） |
| **PSDK Widget** | Widget 数值设置、输入框文本设置、Widget 资源 `objectKey` → URL 批量换取 |
| **播放进度与浮窗** | 音频 / TTS 播放进度、浮窗文本、PSDK UI 资源订阅（WebSocket） |
| **喊话器音频资源** | `getSTS` → 直传对象存储 → 资源 CRUD → 回到 DRC 播放的完整四步流程 |

## 安装

推荐通过 `cloudapi2@dji` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# 克隆仓库（如尚未克隆）
git clone https://github.com/dji-sdk/Cloud-API-2.git
cd Cloud-API-2/devkit

# Claude Code
cp -r skills/cloudapi2-payload ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-payload .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-payload .github/skills/
```

## 触发词

- PsdkControlModule、PsdkResourceModule
- PSDK 负载、psdkIndex
- 探照灯、searchlight、searchlightModeSet、searchlightBrightnessSet
- 喊话器、speaker、Megaphone、speakerAudioPlayStart、speakerTTSPlayStart、speakerPlayVolumeSet
- 音频播放、TTS、语音播报、播放进度
- widget、widgetValueSet、输入框、浮动窗口、floatingWindow、PSDK UI 资源
- 喊话器资源、getSTS、uploadSpeakerAudioFile、STS 直传
- DRC 负载控制、connectDrcLink

## 不触发

- 运行时直接调用 MCP Tool（`fh_searchlight` / `fh_speaker` / `fh_psdk_widget` / `fh_camera`）做运维操作 → `cloudapi2-mcp-guide`
- 通用 SDK 规则、飞控 / 航线任务 / 相机云台 → `cloudapi2-sdk-core`
- 地图域（飞行区、离线地图、解锁证书、地图元素）→ `cloudapi2-map`
- 设备生命周期、固件升级、HMS 告警 → `cloudapi2-device-management`
- SEI/OSD 遥测、视频流 → `cloudapi2-sei-osd`

## 目录结构

```
cloudapi2-payload/
├── SKILL.md                       # 触发条件、核心规则、决策树路由
├── constraints.yaml                # 编码约束（继承 _shared/constraints-common.yaml）
├── references/
│   ├── psdk-control.md             # PsdkControlModule 全部 34 方法
│   └── psdk-resource.md            # PsdkResourceModule 全部 7 方法
└── eval/
    ├── routing.yaml                 # 触发/不触发边界测试
    ├── param.yaml                   # 参数/签名正确性测试
    ├── quality.yaml                 # 回答质量测试
    └── trajectory.yaml              # 多步骤流程测试
```
