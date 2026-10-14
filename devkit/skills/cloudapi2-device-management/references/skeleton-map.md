# Skeleton 项目架构地图

> **用途**：AI 读完本文件即可在 skeleton 中扩展功能，无需遍历源码。
> **适用 skill**：cloudapi2-device-management、cloudapi2-sdk-core（涉及 skeleton 扩展时引用）。

---

## 1. 技术栈

| 项 | 版本 |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring AI | 1.0.0 |
| 构建 | Maven multi-module |
| 测试 | JUnit 5 + AssertJ |
| 数据库 | H2 (开发) / 可换 PostgreSQL |
| 包名根 | `com.dji.fh` |

---

## 2. 模块依赖图

```
┌─────────────────────────────────────────────────────────┐
│                  app/fh-skeleton-app                     │  ← Spring Boot 主入口
│         (汇聚所有 module，启动 + 安全过滤)              │
└────────┬────────────┬────────────────┬──────────────────┘
         │            │                │
  ┌──────▼──────┐ ┌──▼─────────┐ ┌───▼──────────┐
  │feature-device│ │feature-user│ │feature-chat  │   ← ISV 扩展区（每个独立 module）
  └──────┬───────┘ └──┬─────────┘ └───┬──────────┘
         │            │                │
         └────────────┼────────────────┘
                      ▼
         ┌────────────────────────┐
         │     kernel-spi         │   ← 契约层：Event、Repository、Auth 接口
         └────────────┬───────────┘
                      │ 被以下实现：
         ┌────────────┼───────────────┐
         ▼            ▼               ▼
  ┌────────────┐ ┌──────────────┐ ┌──────────────┐
  │kernel-impl │ │kernel-real-sdk│ │kernel-mock-sdk│
  │(事件总线/  │ │(真实 SDK    │ │(Mock 数据    │
  │ 存储/鉴权) │ │ JNI 适配)   │ │ 开发环境)    │
  └────────────┘ └──────────────┘ └──────────────┘
```

**规则**：feature 模块**只**依赖 `kernel-spi`，不可依赖 `kernel-impl` / `kernel-*-sdk`。

---

## 3. 目录结构（只列关键路径）

```
skeleton/
├── pom.xml                              # parent POM（<modules> 列表在此）
├── fh-skeleton-bom/pom.xml              # BOM（统一版本管理）
├── kernel/
│   ├── kernel-spi/                      # 契约层
│   │   └── src/main/java/com/dji/fh/kernel/spi/
│   │       ├── event/                   # KernelEvent 体系
│   │       ├── repository/              # Repository / KeyValueStore / AppendLog
│   │       ├── auth/                    # SecurityContext / 注解
│   │       ├── bridge/                  # EventBridge SPI
│   │       └── manifest/                # module.yaml 反序列化结构
│   ├── kernel-impl/                     # 默认实现（事件总线/存储/鉴权切面）
│   ├── kernel-real-sdk/                 # DJI CloudAPI2 SDK JNI 适配
│   └── kernel-mock-sdk/                 # Mock 模式（开发环境，无需真实 SDK）
├── feature/
│   ├── feature-device/                  # ★ 参考模板 —— ISV 新建 feature 照此模式
│   ├── feature-user/                    # 用户认证模块（JWT）
│   └── feature-chat/                    # AI Chat Bot 模块（Spring AI）
└── app/
    └── fh-skeleton-app/                 # Spring Boot 主启动入口
```

---

## 4. 新建 Feature 模块 SOP（改 4 个文件）

以新建 `feature-alert` 为例：

### 4.1 创建目录 + pom.xml

```
skeleton/feature/feature-alert/
├── pom.xml
└── src/main/java/com/dji/fh/feature/alert/
    ├── AlertAutoConfiguration.java
    ├── AlertProperties.java
    ├── AlertService.java
    └── AlertRestController.java
```

**pom.xml 模板**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.dji.fh</groupId>
        <artifactId>fh-skeleton-parent</artifactId>
        <version>0.1.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>feature-alert</artifactId>
    <name>FH Skeleton Feature Alert</name>
    <description>HMS 告警管理 feature module</description>

    <dependencies>
        <dependency>
            <groupId>com.dji.fh</groupId>
            <artifactId>kernel-spi</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 4.2 注册到 parent pom.xml

在 `skeleton/pom.xml` 的 `<modules>` 块中添加：

```xml
<module>feature/feature-alert</module>
```

### 4.3 AutoConfiguration 注册文件

创建：`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

内容（一行一个全限定类名）：

```
com.dji.fh.feature.alert.AlertAutoConfiguration
```

### 4.4 app 模块添加依赖

在 `skeleton/app/fh-skeleton-app/pom.xml` 的 `<dependencies>` 中添加：

```xml
<dependency>
    <groupId>com.dji.fh</groupId>
    <artifactId>feature-alert</artifactId>
    <version>${project.version}</version>
</dependency>
```

---

## 5. Feature 模块代码模式

### 5.1 AutoConfiguration（入口）

```java
package com.dji.fh.feature.alert;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "fh.skeleton.feature.alert.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AlertProperties.class)
public class AlertAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AlertService alertService(AlertProperties props) {
        return new AlertService(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertRestController alertRestController(AlertService service) {
        return new AlertRestController(service);
    }
}
```

**关键约定**：
- `@ConditionalOnProperty` 的 name 格式：`fh.skeleton.feature.{module}.enabled`
- `matchIfMissing = true` → 默认启用，ISV 设 false 可禁用
- 所有 Bean 用 `@ConditionalOnMissingBean` → ISV 可声明同类型 Bean 覆盖

### 5.2 Properties（配置）

```java
package com.dji.fh.feature.alert;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fh.skeleton.feature.alert")
public record AlertProperties(
        boolean enabled,
        int maxPerDevice   // 每设备最大告警条数
) {
    public AlertProperties {
        if (maxPerDevice <= 0) { maxPerDevice = 100; }
    }
}
```

### 5.3 Service（事件消费 + 业务逻辑）

```java
package com.dji.fh.feature.alert;

import com.dji.fh.kernel.spi.event.HmsAlertEvent;
import org.springframework.context.event.EventListener;
import java.util.concurrent.ConcurrentHashMap;

public class AlertService {

    private final ConcurrentHashMap<String, Object> store = new ConcurrentHashMap<>();
    private final AlertProperties props;

    public AlertService(AlertProperties props) {
        this.props = props;
    }

    @EventListener
    public void onHmsAlert(HmsAlertEvent event) {
        // 处理告警...
    }
}
```

### 5.4 RestController（HTTP API）

```java
package com.dji.fh.feature.alert;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertRestController {

    private final AlertService service;

    public AlertRestController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public Object listAlerts() {
        return service.listAll();
    }
}
```

---

## 6. KernelEvent 体系（kernel-spi 中已有的事件）

| 事件类 | 父类 | 场景 | 频率 |
|--------|------|------|------|
| `KernelReadyEvent` | KernelEvent | SDK 初始化完成 | 一次 |
| `KernelShutdownEvent` | KernelEvent | SDK 关闭 | 一次 |
| `DeviceOnlineEvent` | AbstractKernelEvent + DeviceScopedEvent | 设备上线 | 低频 |
| `DeviceOfflineEvent` | AbstractKernelEvent + DeviceScopedEvent | 设备下线 | 低频 |
| `OsdUpdateEvent` | AbstractKernelEvent + DeviceScopedEvent | OSD 遥测推送 | 1-10 Hz |
| `DockOsdUpdateEvent` | AbstractKernelEvent + DeviceScopedEvent | 机场 OSD | 低频 |
| `HmsAlertEvent` | AbstractKernelEvent + DeviceScopedEvent | HMS 健康告警 | 不定 |
| `FlightTaskProgressEvent` | AbstractKernelEvent | 航线任务进度 | 低频 |

**消费方式**：在 Service 方法上加 `@EventListener`，Spring 事件总线自动派发。

**自定义事件**：继承 `AbstractKernelEvent`，实现 `DeviceScopedEvent`（如果关联设备），通过 `ApplicationEventPublisher.publishEvent()` 发布。

### 关键接口签名

```java
// 顶层标记接口
public interface KernelEvent {
    String eventId();
    Instant occurredAt();
    String source();
}

// 推荐基类（自动填充 eventId/occurredAt/source）
public abstract class AbstractKernelEvent implements KernelEvent { ... }

// 设备事件标记
public interface DeviceScopedEvent {
    String deviceSn();
}

// OSD 数据（开放属性 Map）
public record OsdData(Map<String, Object> attributes) {
    public Optional<Object> get(String key) { ... }
}
// 常见 key：battery_percent, altitude, longitude, latitude, speed, mode
```

---

## 7. 配置前缀约定

| 前缀 | 归属 | 示例 |
|------|------|------|
| `fh.skeleton.kernel.*` | kernel 层配置 | `fh.skeleton.kernel.sdk.server-address` |
| `fh.skeleton.feature.{name}.*` | feature 模块 | `fh.skeleton.feature.device.enabled` |
| `fh.skeleton.app.*` | app 层配置 | `fh.skeleton.app.security.enabled` |

---

## 8. 鉴权注解（可选）

```java
@RequireAuthenticated           // 仅要求已登录
@RequireRole("admin")           // 角色鉴权
@RequirePermission("device:read") // 细粒度权限
```

由 `kernel-impl` 的 `AuthorizationAspect` 切面处理。feature 模块只需加注解，无需自行校验。

---

## 9. 构建与测试

```bash
cd skeleton

# 编译全部（含新 feature 模块）
mvn clean package -DskipTests

# 运行测试
mvn test

# 只编译某个模块
mvn clean package -pl feature/feature-alert -am -DskipTests

# 启动（Mock 模式，无需真实 SDK）
java -jar app/fh-skeleton-app/target/fh-skeleton-app-0.1.0.jar \
  --spring.profiles.active=mock
```

---

## 10. 常见扩展场景速查

| 我想… | 做法 |
|-------|------|
| 新建一个业务模块 | 按 §4 SOP 建 feature module |
| 消费设备 OSD | `@EventListener` 监听 `OsdUpdateEvent` |
| 消费 HMS 告警 | `@EventListener` 监听 `HmsAlertEvent` |
| 暴露 REST API | `@RestController` + `@RequestMapping("/api/v1/xxx")` |
| 持久化数据 | 注入 `Repository<T, ID>` 或 `KeyValueStore` |
| 追加审计日志 | 注入 `AppendLog<T>` |
| 需要认证 | 方法/类上加 `@RequireAuthenticated` |
| 覆盖默认实现 | 声明同类型 Bean（`@ConditionalOnMissingBean` 让路）|
| 禁用某 feature | `fh.skeleton.feature.{name}.enabled=false` |
| 自定义事件 | 继承 `AbstractKernelEvent` + `publishEvent()` |
