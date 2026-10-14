---
title: "SDK 初始化指南"
module: getting-started
tags: [setup, initialize, config, quickstart]
audience: [agent, developer]
complexity: beginner
summary: "SDK 初始化配置、环境要求、Spring Boot 集成模式"
last_updated: 2026-08-05
sdk_version: "3.3.7"
related:
  - api-reference/core-rules.md
  - architecture/system-overview.md
---

SDK 初始化是所有操作的前提。initialize() 未成功完成前，其他任何 API 调用均返回 null 或静默失败。

# SDK 初始化指南

## 环境要求

| 要求 | 说明 |
|------|------|
| Java | 17+ |
| 平台 | Linux x86_64（生产环境）；macOS/Windows 可用 Mock 模式 |
| 网络 | BFF 需可达 DJI CloudAPI2 Platform（HTTP + WebSocket） |
| 凭证 | server address + access token + workspace UUID |

## 最小初始化代码

```java
import com.dji.flighthubsdk.FlightHubSDK;
import com.dji.flighthubsdk.FlightHubSdkConfig;
import com.dji.flighthubsdk.FlightHubSdkErrorCode;

public class SdkBootstrap {

    public void initialize() {
        FlightHubSdkConfig config = new FlightHubSdkConfig();
        config.setAddress("http://your-flighthub-server:port");
        config.setAccessToken("your-access-token");
        config.setWorkspaceUuid("your-workspace-uuid");

        FlightHubSdkErrorCode result = FlightHubSDK.initialize(config);

        if (result != FlightHubSdkErrorCode.SUCCESS) {
            throw new IllegalStateException(
                "SDK 初始化失败: " + result
            );
        }
        // 现在可以安全调用其他 SDK 接口
    }
}
```

## 初始化失败排查

| 返回值 | 含义 | 排查步骤 |
|--------|------|---------|
| `ERROR_INIT_FAILED` | 连接/库加载失败 | ① `curl -I http://address` 验证网络 ② 检查 token 有效性 ③ Linux 上检查 .so 文件权限 |
| `ERROR_ALREADY_INITIALIZED` | 重复初始化 | 调用前用 `FlightHubSDK.isInitialized()` 判断 |
| `ERROR_INVALID_PARAM` | 配置缺失 | 确保 address/token/workspaceUuid 非空 |

## Spring Boot 集成模式

```java
@Configuration
public class FlightHubConfig {

    @Value("${flighthub.address}")
    private String address;

    @Value("${flighthub.token}")
    private String token;

    @Value("${flighthub.workspace-uuid}")
    private String workspaceUuid;

    @Bean
    public FlightHubSDK flightHubSdk() {
        FlightHubSdkConfig config = new FlightHubSdkConfig();
        config.setAddress(address);
        config.setAccessToken(token);
        config.setWorkspaceUuid(workspaceUuid);

        FlightHubSdkErrorCode result = FlightHubSDK.initialize(config);
        if (result != FlightHubSdkErrorCode.SUCCESS) {
            throw new IllegalStateException("DJI CloudAPI2 SDK init failed: " + result);
        }
        return FlightHubSDK.getInstance();
    }

    @PreDestroy
    public void shutdown() {
        FlightHubSDK.shutdown();
    }
}
```

## Mock 模式（本地开发）

在 macOS/Windows 上无法加载原生库，使用 Mock 模式模拟 SDK 行为：

```yaml
# application-dev.yml
flighthub:
  address: http://placeholder
  token: mock-token
  workspace-uuid: mock-workspace
  mock: true
```

Mock 模式下 SDK 返回模拟数据，用于 UI 开发和集成测试。
