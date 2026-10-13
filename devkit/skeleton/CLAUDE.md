# CLAUDE.md — FH Skeleton

## 项目定位

DJI FlightHub PaaS SDK 的 ISV 接入骨架。基于 Spring Boot 3.2.5 + Java 17 的多模块架构，feature 可插拔，开箱即跑。

技术栈：后端 Spring Boot 3.2.5 / Spring AI 1.1.8 / Java 17 / Maven；前端 Vue 3 + TypeScript + Vite。

## 架构分层

| 层 | 路径 | 可变性 | 说明 |
|----|------|--------|------|
| **Kernel** | `kernel/` | ❌ ISV 不可修改 | SDK 封装、事件总线、SPI 定义。升级时直接覆盖 |
| **Feature** | `feature/feature-xxx/` | ⚡ 可禁用、可替换 | 官方预置功能，ISV 可通过配置关闭或用自己的实现替代 |
| **ISV Extension** | `feature/feature-xxx/`（ISV 新增） | ✅ 完全自由 | ISV 自己的业务模块，走 Kernel SPI |

## 架构规则

1. **feature 模块只依赖 kernel-spi**，禁止依赖 kernel-impl / kernel-mock-sdk / kernel-real-sdk
2. **新功能必须作为独立 feature module**（`feature/feature-xxx/`），禁止直接在 app 中写业务逻辑
3. **事件驱动**：SDK 数据通过 KernelEvent 事件总线流转，feature 通过 `@EventListener` 消费
4. **AutoConfiguration 模式**：多数 feature 有一个 `XxxAutoConfiguration` 入口类（@AutoConfiguration + @ConditionalOnProperty）+ `.imports` 注册文件。例外：feature-dashboard 使用 @Configuration 且无条件开关

## 禁止操作

- ❌ 修改 `kernel/` 目录下任何文件（升级时被覆盖，改动丢失）
- ❌ feature 模块间直接 import（通过 Spring Event Bus 通信）
- ❌ 绕过 kernel-spi 直接依赖 kernel-impl 或 kernel-*-sdk
- ❌ 在 `app/fh-skeleton-app/` 中写业务逻辑（app 只负责装配和启动）
- ❌ 使用魔法值（端口号、超时、错误码必须来自配置或常量）

## 新建 Feature Module（4 步）

1. **创建模块目录和 POM** — `feature/feature-xxx/pom.xml`（parent = fh-skeleton-parent，依赖 kernel-spi + spring-boot-starter-web）
2. **注册到父 POM** — `skeleton/pom.xml` 的 `<modules>` 中添加 `<module>feature/feature-xxx</module>`
3. **注册 AutoConfiguration** — `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 写入全限定类名
4. **添加 app 依赖** — `app/fh-skeleton-app/pom.xml` 的 `<dependencies>` 中添加该模块

## SPI 接口导航

ISV 通过实现以下 SPI 接口扩展 Kernel 能力。所有 SPI 位于 `kernel/kernel-spi/src/.../spi/`。

### event — 事件总线

| 接口 | 用途 |
|------|------|
| `KernelEvent` | 所有事件基类，feature 通过 `@EventListener` 消费 |
| `SdkEventBus` | 事件发布接口（Kernel 内部使用，ISV 通常不需要实现） |

### auth — 认证鉴权

| 接口 | 用途 |
|------|------|
| `AuthenticationProvider` | 从请求解析用户身份。ISV 接入自家 SSO/LDAP/Token 系统时实现此接口 |
| `PermissionEvaluator` | 权限判定（配合 `@RequirePermission` 注解使用） |
| `SecurityContext` | 当前请求的用户上下文（userId / roles / permissions） |

### repository — 持久化

| 接口 | 用途 |
|------|------|
| `Repository<T, ID>` | 通用 CRUD（findById / save / delete / findAll 分页） |
| `KeyValueStore` | 简单 KV 存储 |
| `AppendLog` | 追加式日志存储 |

### bridge — SDK 桥接

| 接口 | 用途 |
|------|------|
| `EventBridge` | SDK 原始事件到 Spring Event 的桥接（Kernel 已实现，ISV 通常不需要） |
| `InboundEventSink` | 入站事件接收器 |

### query — 查询服务

| 接口 | 用途 |
|------|------|
| `DeviceQueryService` | 设备列表查询。Kernel 提供默认实现，ISV 可覆盖添加过滤/缓存逻辑 |

## KernelEvent 清单

| 事件 | 场景 | 频率 |
|------|------|------|
| `DeviceOnlineEvent` | 设备上线 | 低频 |
| `DeviceOfflineEvent` | 设备下线 | 低频 |
| `OsdUpdateEvent` | 无人机 OSD 遥测（经纬度/电量/速度等） | 1-10Hz |
| `DockOsdUpdateEvent` | 机场 OSD 遥测 | 低频 |
| `HmsAlertEvent` | HMS 健康告警 | 不定 |
| `FlightTaskProgressEvent` | 航线任务进度变更 | 低频 |
| `KernelReadyEvent` | SDK 初始化完成，系统就绪 | 启动时一次 |
| `KernelShutdownEvent` | 系统关闭前通知 | 关闭时一次 |

## SDK 集成规则提醒

Kernel 层已封装 SDK 调用，ISV 通常通过事件和 SPI 间接使用 SDK。若需直接调用 SDK，必须遵守：

1. `initialize()` 完成后才能调用其他 SDK 方法
2. 所有回调必须检查 `error_code == 0` 后再使用数据
3. `listen*()` 返回的 listenId 必须在 shutdown 时调用 `cancel*()`
4. 飞控指令前 `acquireFlightControlAuthority()`，结束后 `release`（含异常路径）
5. `getDroneInstance()` / `getDockInstance()` 可能返回 null，必须判空
6. 起飞/返航/紧急停止等危险操作前需用户确认

完整规则详解和代码示例：安装 `cloudapi2-sdk-core` Skill。

## 参考实现

`feature/feature-device/` 是标准的 feature 模块模板：

- `DeviceAutoConfiguration` — 入口（@AutoConfiguration + @ConditionalOnProperty + @EnableConfigurationProperties）
- `DeviceProperties` — 配置 record（@ConfigurationProperties(prefix="fh.skeleton.feature.device")）
- `DeviceStatusService` — 业务逻辑（@EventListener 消费事件 + ConcurrentHashMap 内存存储）
- `DeviceRestController` — HTTP API（@RestController）

## 已有 Feature 模块

| 模块 | 能力 |
|------|------|
| feature-device | 设备上下线、OSD 遥测、HMS 告警 |
| feature-wayline | 航线任务管理 |
| feature-media | 媒体文件管理 |
| feature-terra | 重建任务 |
| feature-flight-area | 飞行区域管理 |
| feature-dashboard | 仪表盘概览 |
| feature-user | 用户管理 |
| feature-chat | AI 对话（Spring AI + RAG） |

## 构建与运行

```bash
# 编译（跳过测试）
mvn clean package -DskipTests

# 运行测试
mvn test

# 只编译单个模块
mvn clean package -pl feature/feature-xxx -am -DskipTests

# 本地运行（Mock 模式，无需 SDK 原生库）
java -jar app/fh-skeleton-app/target/fh-skeleton-app-0.1.0.jar --spring.profiles.active=mock
```

## 快速上下文

- **包名根**：`com.dji.fh`
- **feature 包名**：`com.dji.fh.feature.{name}`
- **Bean 覆盖**：所有 Bean 用 `@ConditionalOnMissingBean`，ISV 可替换默认实现

### 配置前缀（各模块不完全统一，以代码为准）

| 模块 | 配置 key | 默认值 |
|------|---------|--------|
| feature-device | `fh.skeleton.feature.device.enabled` | true（matchIfMissing） |
| feature-wayline | `fh.skeleton.feature.wayline.enabled` | true（matchIfMissing） |
| feature-media | `fh.skeleton.feature.media.enabled` | true（matchIfMissing） |
| feature-terra | `fh.skeleton.feature.terra.enabled` | true（matchIfMissing） |
| feature-flight-area | `fh.skeleton.feature.flight-area.enabled` | true（matchIfMissing） |
| feature-user | `fh.user.enabled` | true（matchIfMissing） |
| feature-chat | `fh.chat.enabled` | **false**（无 matchIfMissing，需显式开启） |
| feature-dashboard | 无条件开关 | 始终启用（@Configuration） |

## 深度 SDK 知识

本项目基于 DJI FlightHub PaaS SDK 3.3.7。如需完整的 SDK 接口知识、错误码字典、设备管理流程、飞控权限模型等深度内容，安装以下 Skills：

- `cloudapi2-sdk-core` — SDK 集成核心规则与代码模板
- `cloudapi2-device-management` — 设备管理完整开发指南
- `cloudapi2-frontend-component` — 前端组件接入指南
- `cloudapi2-troubleshooting` — 错误排查与 RCA 方法论，并含本地诊断闭环（固定命令集采集 / 脱敏 / 静态判级 / 报告 / 反馈，数据不出网）
