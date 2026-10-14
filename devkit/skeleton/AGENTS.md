# AGENTS.md — AI 助手使用指南

> 本文件为 AI Agent（Claude Code / Cursor / Copilot）在 skeleton 项目中工作时提供上下文。

## 项目简介

FH Skeleton 是 DJI CloudAPI2 SDK 的 ISV 接入骨架项目。Spring Boot 3.2.5 + Java 17 多模块架构，feature 模块可插拔。

## 快速上下文

- **包名根**：`com.dji.fh`
- **feature 包名**：`com.dji.fh.feature.{name}`
- **配置前缀**：`fh.skeleton.feature.{name}.enabled`（matchIfMissing=true 默认启用）
- **事件消费**：`@EventListener` 监听 `KernelEvent` 子类
- **Bean 覆盖**：所有 Bean 用 `@ConditionalOnMissingBean`，ISV 可替换

## 架构规则

1. **feature 模块只依赖 kernel-spi**，禁止依赖 kernel-impl / kernel-*-sdk
2. **新功能必须作为独立 feature module**（`feature/feature-xxx/`），禁止直接在 app 中写业务逻辑
3. **事件驱动**：SDK 数据通过 KernelEvent 事件总线流转，feature 通过 `@EventListener` 消费
4. **AutoConfiguration 模式**：每个 feature 有一个 `XxxAutoConfiguration` 入口类 + `.imports` 注册文件

## 新建 Feature 模块需要改的 4 个地方

1. `skeleton/feature/feature-xxx/pom.xml` — 模块 POM（parent = fh-skeleton-parent，依赖 kernel-spi + spring-boot-starter-web）
2. `skeleton/pom.xml` — 在 `<modules>` 中添加 `<module>feature/feature-xxx</module>`
3. `skeleton/feature/feature-xxx/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — 注册 AutoConfiguration 全限定类名
4. `skeleton/app/fh-skeleton-app/pom.xml` — 在 `<dependencies>` 中添加该 feature 模块依赖

## 可用的 KernelEvent

| 事件 | 场景 | 频率 |
|------|------|------|
| `DeviceOnlineEvent` | 设备上线 | 低频 |
| `DeviceOfflineEvent` | 设备下线 | 低频 |
| `OsdUpdateEvent` | OSD 遥测（经纬度/电量/速度等） | 1-10Hz |
| `DockOsdUpdateEvent` | 机场 OSD | 低频 |
| `HmsAlertEvent` | HMS 健康告警 | 不定 |
| `FlightTaskProgressEvent` | 航线任务进度 | 低频 |
| `KernelReadyEvent` | SDK 初始化完成 | 一次 |
| `KernelShutdownEvent` | 系统关闭前通知 | 一次 |

## 参考实现

`feature/feature-device/` 是标准的 feature 模块模板，包含：
- `DeviceAutoConfiguration` — 入口（@AutoConfiguration + @ConditionalOnProperty + @EnableConfigurationProperties）
- `DeviceProperties` — 配置 record（@ConfigurationProperties）
- `DeviceStatusService` — 业务（@EventListener 消费 4 种事件 + ConcurrentHashMap 存储）
- `DeviceRestController` — HTTP API（@RestController + @RequestMapping）

## 构建

```bash
mvn clean package -DskipTests          # 快速编译
mvn test                               # 运行测试
mvn clean package -pl feature/feature-xxx -am -DskipTests  # 只编译某模块
```

## 运行

```bash
# Mock 模式（无需 SDK 原生库，macOS/Windows/Linux 均可）
java -jar app/fh-skeleton-app/target/fh-skeleton-app-0.1.0.jar --spring.profiles.active=mock
```
