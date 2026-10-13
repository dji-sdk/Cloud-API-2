# cloudapi2-device-management — 设备管理模块接入助手

帮 ISV 开发者在 skeleton 后端项目中正确扩展设备管理功能，遵循 skeleton 的 feature 模块架构和 SDK 事件总线模式。

## 能力

| 能力 | 说明 |
|------|------|
| **设备生命周期** | 设备发现、上下线事件处理 |
| **OSD 订阅** | 无人机/机场实时遥测数据订阅与取消 |
| **HMS 告警** | 健康管理系统告警分级处理 |
| **Feature 模块创建** | 新建符合 skeleton 架构的 feature 模块 |
| **事件监听模式** | @EventListener + KernelEvent 标准模式 |

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装全套 Skill（见 [skills/README.md](../README.md#安装方式)）。

单独安装本 Skill：

```bash
# Claude Code
cp -r skills/cloudapi2-device-management ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-device-management .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-device-management .github/skills/
```

## 触发词

以下关键词出现在对话中时，Skill 自动加载（与 SKILL.md frontmatter 一致）：

- 设备管理、设备列表
- OSD 订阅、HMS 告警
- 固件升级、设备上下线、控制权获取
- DeviceOnlineEvent、DeviceOfflineEvent、OsdUpdateEvent、HmsAlertEvent
- DeviceStatusService、feature 模块
- KernelEvent、EventListener
- listen/cancel、getDroneInstance、getDockInstance

## 使用示例

### 场景 1：添加 OSD 订阅

```
你：帮我添加 OSD 数据订阅功能

AI：（自动加载 Skill）
→ 在 feature-device 中添加 OsdSubscriptionService
→ 使用 @EventListener 监听 OsdUpdateEvent
→ 自动加入 listen/cancel 配对 + @PreDestroy 释放
→ 提醒：null guard getDroneInstance
```

### 场景 2：创建新 Feature 模块

```
你：我想新建一个 feature-mission 模块管理飞行任务

AI：（加载 feature 模块模板）
→ 生成 Maven module（pom.xml + 标准目录结构）
→ 生成 MissionAutoConfiguration（@ConditionalOnProperty）
→ 注册到 parent pom 和 app 模块依赖
→ 生成 Service + Controller 骨架
```

### 场景 3：处理设备上下线

```
你：设备上线时要发通知

AI：（加载设备生命周期参考）
→ 创建 DeviceNotificationListener
→ @EventListener 监听 DeviceOnlineEvent
→ 从 event 中提取设备信息
→ 调用通知服务
```

## 核心约束（违反会导致资源泄漏或 NPE）

1. **listen/cancel 配对**：每个 `listen*` 必须在 `@PreDestroy` 中 `cancelListen*`
2. **null guard**：`getDroneInstance(sn)` / `getDockInstance(sn)` 返回值必须判空
3. **error_code 检查**：所有 SDK 回调必须先检查 `error.getErrorCode() == 0`
4. **feature 模块模式**：新功能必须作为独立 feature 模块，通过 AutoConfiguration 注册
5. **事件总线**：消费 SDK 事件必须通过 `@EventListener` 监听 KernelEvent 子类

## 目录结构

```
cloudapi2-device-management/
├── SKILL.md                  ← AI 主入口
├── constraints.yaml          ← 后端约束规则
├── references/
│   ├── device-lifecycle.md   — 设备生命周期 6 步
│   ├── osd-subscription.md   — OSD 订阅/取消模式
│   └── hms-alert-handling.md — HMS 告警分级处理
├── playbooks/
│   └── add-device-feature.yaml — "添加设备功能"方案
└── snippets/
    ├── feature-module-pom.xml      — Maven 模块模板
    ├── auto-configuration.java     — AutoConfiguration 模板
    ├── event-listener-service.java — EventListener 模板
    └── rest-controller.java        — REST Controller 模板
```

## 与 cloudapi2-sdk-core 的关系

| 维度 | cloudapi2-device-management（本 Skill） | cloudapi2-sdk-core |
|------|-----------------------------------|-------------------|
| 聚焦 | skeleton 项目中的设备管理扩展 | 通用 SDK 集成规则 |
| 模板 | 按 skeleton 架构生成 feature 模块 | 通用 Java 代码模板 |
| 约束 | 包含 skeleton 架构约束 | 仅 SDK 调用规则 |
| 依赖 | 引用 cloudapi2-sdk-core 的核心规则 | 独立 |

本 Skill 是 `cloudapi2-sdk-core` 在 skeleton 设备管理场景的具体化。
