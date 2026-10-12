# stdio 本地开发步骤

> 来源：`mcp/docs/部署指南.md` §7、`mcp/docs/docker-deployment.md`。AI 按此推进。
> 适用：开发者在本地 macOS/Windows/Linux 运行 MCP Server，供 Claude Code / Cursor 本地集成使用。

## 两种子路径

| 模式 | 适用场景 | 需要 Docker | 需要 Java | 平台限制 |
|------|----------|-------------|-----------|----------|
| B1: Mock JAR | Prompt 调试、功能演示、无 FlightHub 环境 | 否 | 是（JDK 17+） | 任意平台 |
| B2: Docker stdio | 需要真实 SDK（连接 FlightHub 平台） | 是 | 否 | Docker 可跑 amd64 |

## B1: Mock 模式（本地 JAR 直接跑）

### 前提条件

- JDK 17+（`java -version` 验证）
- Maven 3.8+（构建用，`mvn --version` 验证）

### 步骤

**1. 安装私有 SDK JAR（首次）**

```bash
cd <repo-root>
mvn install:install-file \
  -Dfile=libs/flight-hub-sdk-3.3.7.jar \
  -DgroupId=com.dji.flighthubsdk \
  -DartifactId=flight-hub-sdk \
  -Dversion=3.3.7 \
  -Dpackaging=jar \
  --settings config/settings.xml
```

**2. 构建 JAR**

```bash
cd mcp
mvn clean package -DskipTests
```

产物：`mcp/target/flighthub-mcp-server-1.0.0.jar`

**3. 运行（Mock + stdio）**

```bash
cd mcp
FH_SERVER_ADDRESS=http://placeholder FH_MOCK=true \
  java -jar target/flighthub-mcp-server-1.0.0.jar \
  --spring.profiles.active=stdio \
  --spring.main.web-application-type=none
```

或使用封装脚本（自动过滤非 JSON 输出）：
```bash
FH_MOCK=true ./run-mcp-local.sh
```

**4. 配置 Claude Code**

在 `~/.claude.json` 的 `mcpServers` 中添加：

```json
{
  "dji-cloud-api2": {
    "command": "java",
    "args": [
      "-Xms64m", "-Xmx384m",
      "-jar", "/path/to/mcp/target/flighthub-mcp-server-1.0.0.jar",
      "--spring.profiles.active=stdio",
      "--spring.main.web-application-type=none"
    ],
    "env": {
      "FH_SERVER_ADDRESS": "http://placeholder",
      "FH_MOCK": "true"
    }
  }
}
```

或使用脚本方式：
```json
{
  "dji-cloud-api2": {
    "command": "bash",
    "args": ["/path/to/mcp/run-mcp-local.sh"],
    "env": {
      "FH_MOCK": "true"
    }
  }
}
```

**5. 验证**

重启 Claude Code → `/mcp` → 应显示 `dji-cloud-api2 · ✓ connected · 51 tools`

---

## B2: Docker stdio 代理（真实 SDK）

### 前提条件

- Docker Desktop（macOS/Windows）或 Docker Engine（Linux）
- 已有 FlightHub 服务器凭证（`FH_SERVER_ADDRESS` / `FH_ACCESS_TOKEN` / `FH_WORKSPACE_UUID`）

### 步骤

**1. 构建镜像**

```bash
cd mcp
mvn clean package -DskipTests
docker build --platform linux/amd64 -t dji/flighthub-mcp:latest .
```

**2. 配置 Claude Code**

在 `~/.claude.json` 的 `mcpServers` 中添加：

```json
{
  "dji-cloud-api2": {
    "command": "docker",
    "args": [
      "run", "--rm", "-i", "--platform", "linux/amd64",
      "-e", "FH_MOCK=false",
      "-e", "FH_TRANSPORT=stdio",
      "-e", "FH_SERVER_ADDRESS=http://<YOUR_SERVER_IP>",
      "-e", "FH_SERVER_PORT=<YOUR_SERVER_PORT>",
      "-e", "FH_ACCESS_TOKEN=<your-token>",
      "-e", "FH_WORKSPACE_UUID=<your-uuid>",
      "dji/flighthub-mcp:latest",
      "--spring.profiles.active=stdio",
      "--spring.main.web-application-type=none"
    ]
  }
}
```

> 注意 `--rm -i`：容器在 Claude Code 关闭 stdin 后自动清理。
> Apple Silicon 必须保留 `--platform linux/amd64`。

**3. 验证**

重启 Claude Code → `/mcp` → 应显示 `dji-cloud-api2 · ✓ connected · 51 tools`

首次启动约 10-15 秒（提取原生库 + SDK 初始化），后续工具调用 < 1 秒。

---

## Mock 模式 vs 真实模式对比

| 维度 | Mock 模式 | 真实模式 |
|------|-----------|----------|
| 启动速度 | 2-3 秒 | 10-15 秒 |
| 数据来源 | 固定模拟数据 | FlightHub 真实平台 |
| 设备操控 | 模拟成功响应 | 真实下发指令 |
| 适用场景 | Prompt 调试、演示、CI | 生产运维、真实飞行 |
| 依赖 | 无外部依赖 | FlightHub 服务器 + 凭证 |

## 常见坑

| 坑 | 检查/处理 |
|---|---|
| `run-mcp-local.sh` 报错找不到 JAR | 先执行 `mvn clean package -DskipTests` |
| Claude Code 报 `command: expected string` | 检查 JSON 格式，stdio 模式不需要 `"type"` 字段 |
| Docker Desktop 未启动 | 先启动 Docker Desktop |
| 首次调用工具超时 | SDK 初始化需 10-15s，属正常现象；Claude Code 默认超时 60s 足够 |
| macOS .so 加载失败 | 本机 JAR 只能跑 Mock 模式；真 SDK 走 Docker stdio |
