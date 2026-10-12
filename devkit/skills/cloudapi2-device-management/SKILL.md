---
name: cloudapi2-device-management
description: Cloud API 2.0 SDK 设备管理模块接入助手。当用户要在 skeleton 后端项目中扩展设备管理功能（设备列表、设备状态订阅、HMS 告警、设备上下线、固件升级、控制权获取等），或新建自定义 feature 模块时调用。触发词：设备管理、设备列表、设备状态订阅、HMS 告警、固件升级、设备上下线、DeviceOnlineEvent、DeviceOfflineEvent、OsdUpdateEvent、HmsAlertEvent、DeviceStatusService、feature 模块、KernelEvent、EventListener、listen/cancel、getDroneInstance、getDockInstance。
version: 1.1.0
author: DJI CloudAPI2 Team
---

# DJI FlightHub PaaS SDK 设备管理接入助手

## 这个 Skill 是干什么的

帮 ISV 开发者在 **skeleton 后端项目**（Spring Boot 3.2.5 + Java 17）中正确扩展设备管理功能：新建 feature 模块、接入 SDK 事件、暴露 REST API、处理 OSD/HMS 数据。

## 何时触发

- 用户要扩展设备管理功能（查设备列表、订阅设备状态、处理告警）
- 用户要新建自定义 feature 模块（模式与 feature-device 相同）
- 用户提到 SDK 事件相关类名（DeviceOnlineEvent、OsdUpdateEvent、HmsAlertEvent 等）
- 用户问 listen/cancel 模式、设备控制权、OSD 数据结构
- 用户问 skeleton 的模块化架构（kernel-spi → feature → app）

**不触发**：前端组件问题（用 cloudapi2-frontend-component Skill）、MCP Tool 开发（用 cloudapi2-sdk-core Skill）、纯 SDK API 查询。

## 工作流

本 Skill 遵循 `skills/_shared/sop-template.md` 定义的 7 章节 SOP。
根据用户请求自动选择执行深度（L1/L2/L3）。

以下为本 Skill 的特化规则：

### Ch2 澄清问题

当用户意图不明确时，从以下维度澄清：

1. **要做什么**：查设备？订阅状态？处理告警？控制设备？还是新建 feature 模块？
2. **数据来源**：从 SDK 事件获取，还是从已有 DeviceStatusService 查询？
3. **是否需要新模块**：可以在已有 feature-device 中扩展，还是需要独立的 feature 模块？

### Ch5 编码约束

参见 `constraints.yaml`，所有 `critical` 规则在生成代码时必须满足。

核心铁律：
1. **listen/cancel 必须配对**：每个 `@EventListener` 对应的底层 SDK 订阅，在 `@PreDestroy` 中释放
2. **null guard**：`getDroneInstance(sn)` / `getDockInstance(sn)` 返回值必须 null 检查
3. **error_code 检查**：所有 SDK 回调先检查 `error.getErrorCode() == SUCCESS`
4. **模块化**：新功能必须作为独立 feature 模块，通过 AutoConfiguration 注册
5. **事件总线**：只通过 `@EventListener` 消费 KernelEvent，不可直接操作底层 SDK

### Ch6 验证扩展

除通用验证外，必须检查：
- [ ] `@EventListener` 方法对应的事件类型正确
- [ ] ConcurrentHashMap / volatile / synchronized 使用正确
- [ ] AutoConfiguration 条件注解配置正确
- [ ] `spring.factories` 或 `AutoConfiguration.imports` 已注册
- [ ] 新模块 POM 继承关系和依赖声明正确

## 决策树

```
用户要做什么？
├── 查设备列表
│   └── 使用 DeviceStatusService.listAll() + REST endpoint
├── 订阅 OSD 状态
│   └── @EventListener + OsdUpdateEvent → 更新内存状态
├── 处理 HMS 告警
│   └── @EventListener + HmsAlertEvent → bounded deque + 分级处理
├── 设备控制指令
│   └── 需要 acquireFlightControlAuthority → 指令 → release
├── 新建 feature 模块
│   └── 按 playbooks/add-device-feature.yaml 步骤执行
└── 其他 SDK 调用
    └── 转交 cloudapi2-sdk-core Skill
```

## skeleton 模块架构

```
skeleton/
├── kernel/
│   ├── kernel-spi/          ← 接口定义（KernelEvent、SdkEventBus）
│   ├── kernel-impl/         ← Spring 实现（ApplicationEventPublisher）
│   ├── kernel-mock-sdk/     ← 模拟 SDK（本地开发用）
│   └── kernel-real-sdk/     ← 真实 SDK（生产用）
├── feature/
│   ├── feature-device/      ← 设备管理（已实现，参考模板）
│   ├── feature-user/        ← 用户管理
│   └── feature-chat/        ← AI 对话
└── app/
    └── fh-skeleton-app/     ← 启动入口，聚合所有模块
```

ISV 扩展方式：在 `feature/` 下新建模块，通过 `@EventListener` 消费 kernel-spi 中定义的事件。

## 权威参考

- `references/skeleton-map.md` — **★ skeleton 完整架构地图（新建模块前必读）**
- `references/device-lifecycle.md` — 设备上下线生命周期
- `references/osd-subscription.md` — OSD 数据订阅模式
- `references/hms-alert-handling.md` — HMS 告警分级处理
- `playbooks/add-device-feature.yaml` — 新建 feature 模块步骤
- `snippets/` — 代码模板

> 本 Skill 的 constraints.yaml 仅包含领域特有规则；通用 SDK 约束见 `skills/_shared/constraints-common.yaml`（生成代码时需同时遵守）。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
