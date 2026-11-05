# DJI Cloud API 2 MCP Server — Docker 部署指南

> 最后更新：2026-07-09

## 概述

DJI Cloud API 2 MCP Server 通过 Docker 容器运行，使用 DJI Cloud API 2 SDK 的 JNI 原生库连接 DJI Cloud API 2 服务器。SDK 按 CPU 架构分 **amd64 / arm64 两个发布包**，分别对应 Linux x86_64 与 Linux aarch64；镜像构建时必须用 `--build-arg SDK_ARCH=amd64|arm64` 选用与目标架构一致的那一份。macOS/Windows 环境必须通过 Docker（Linux 容器）运行。

## 快速启动

```bash
# 在 mcp/ 目录下构建镜像（SDK_ARCH 必填，漏传会直接构建失败）
mvn clean package -DskipTests
docker build --build-arg SDK_ARCH=amd64 -t dji/flighthub-mcp:latest .
docker run --rm -i \
  -e FH_MOCK=false \
  -e FH_TRANSPORT=stdio \
  -e FH_SERVER_ADDRESS=http://<YOUR_SERVER_IP> \
  -e FH_SERVER_PORT=30812 \
  dji/flighthub-mcp:latest
```

## Claude Code MCP 配置

### stdio 模式（本地 Docker）

在 `~/.claude.json`（user scope）或项目根目录 `.mcp.json`（project scope）中配置：

```json
{
  "mcpServers": {
    "dji-cloud-api2": {
      "command": "docker",
      "args": [
        "run", "--rm", "-i", "--platform", "linux/amd64",
        "-e", "FH_MOCK=false",
        "-e", "FH_TRANSPORT=stdio",
        "-e", "FH_SERVER_ADDRESS=http://<YOUR_SERVER_IP>",
        "-e", "FH_SERVER_PORT=30812",
        "dji/flighthub-mcp:latest"
      ]
    }
  }
}
```

> `--platform linux/amd64` 需与构建镜像时的 `SDK_ARCH=amd64` 配对；若镜像按 arm64 构建，本地 Docker 运行时应改为 `linux/arm64`（Apple Silicon 上可直接省略 `--platform`）。

### SSE 模式（连接远程 MCP Server）

远程 MCP Server 已独立运行时，客户端通过 SSE 端点接入，无需在本地启动 Docker：

```json
{
  "mcpServers": {
    "dji-cloud-api2": {
      "type": "sse",
      "url": "http://<REMOTE_SERVER_IP>:8080/pf-devkit/ai-plugin/mcp/sse",
      "headers": {
        "Authorization": "Bearer <FH_SSE_AUTH_TOKEN>"
      }
    }
  }
}
```

> - `url` 填远程服务器的 SSE 端点，默认端口 `8080`，路径固定为 `/pf-devkit/ai-plugin/mcp/sse`
> - 若服务器未配置 `FH_SSE_AUTH_TOKEN`，可省略 `headers` 块（仅建议内网使用）
> - Cursor 在 `.cursor/mcp.json` 中使用相同格式

---

## SSE 远程部署

以下内容适用于将 MCP Server 部署到独立服务器（Linux x86_64 或 Linux aarch64），供多个 AI 客户端通过网络接入。

### 部署流程

**1. 服务器准备**

确保服务器已安装 Docker，并开放 `8080` 端口（或通过 Nginx 反向代理到 `443`）。

**2. 配置环境变量**

```bash
cp .env.example .env
vim .env
```

SSE 模式所需配置（在 stdio 模式基础上追加）：

```bash
# DJI Cloud API 2 平台连接
FH_SERVER_ADDRESS=http://your-flighthub-server.com
FH_SERVER_PORT=30812
FH_ACCESS_TOKEN=your-access-token
FH_WORKSPACE_UUID=your-workspace-uuid

# SSE 模式
FH_TRANSPORT=sse
FH_SSE_PORT=8080

# 安全（生产必填，防止未授权访问）
FH_SSE_AUTH_TOKEN=your-secret-token
FH_MAX_SECURITY_LEVEL=2
```

**3. 构建并启动**

```bash
# 在 mcp/ 目录下构建镜像（SDK_ARCH 必填，且必须与目标服务器架构一致）
mvn clean package -DskipTests
docker build --build-arg SDK_ARCH=amd64 -t dji/flighthub-mcp:latest .

# 方式 A：docker run（快速启动）
docker run -d --name pf-devkit-mcp \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  dji/flighthub-mcp:latest

# 方式 B：docker-compose（推荐生产，含健康检查和重启策略）
docker-compose up -d
```

> `SDK_ARCH` 必须与服务器架构一致（x86_64 服务器用 `amd64`，aarch64 服务器用 `arm64`）。在 Apple Silicon 上构建 amd64 镜像还需追加 `--platform linux/amd64`（两者指向同一架构）；构建 arm64 镜像则不需要 `--platform`。

**4. 验证 SSE 连接**

```bash
# 检查健康端点
curl http://localhost:8080/pf-devkit/ai-plugin/mcp/health

# 验证 SSE 流（应返回 event: endpoint 事件）
curl -N \
  -H "Accept: text/event-stream" \
  -H "Authorization: Bearer your-secret-token" \
  http://localhost:8080/pf-devkit/ai-plugin/mcp/sse
```

正常响应示例：

```
event: endpoint
data: /messages?sessionId=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx

:ping
```

### docker-compose 示例

```yaml
version: '3.8'
services:
  flighthub-mcp:
    image: dji/flighthub-mcp:latest
    # 留空跟随宿主机架构；跨架构运行时改为 linux/amd64 或 linux/arm64
    # 必须与构建时的 --build-arg SDK_ARCH 指向同一架构
    platform: ${DOCKER_PLATFORM:-linux/amd64}
    container_name: pf-devkit-mcp
    restart: unless-stopped
    ports:
      - "8080:8080"
    env_file:
      - .env
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/pf-devkit/ai-plugin/mcp/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s
    volumes:
      # KMZ 本地路径通道：宿主机目录只读挂载，配合 FH_WAYLINE_UPLOAD_DIR=/data/waylines
      # 客户端把 .kmz 放进 ${FH_WAYLINE_HOST_DIR} 后，提交 file_path=/data/waylines/<file>.kmz
      # 跨机接入（客户端与服务不在同一主机）时该通道不可用，请改用对象存储 key
      - ${FH_WAYLINE_HOST_DIR:-./waylines}:/data/waylines:ro
```

### Nginx 反向代理（可选，HTTPS 接入）

如需通过域名 + HTTPS 暴露 SSE 端点：

```nginx
server {
    listen 443 ssl;
    server_name mcp.your-domain.com;

    ssl_certificate     /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location /pf-devkit/ai-plugin/mcp/sse {
        proxy_pass http://127.0.0.1:8080/pf-devkit/ai-plugin/mcp/sse;
        proxy_http_version 1.1;

        # SSE 必须禁用缓冲，否则事件流会被截断
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 3600s;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Connection '';
    }

    location /pf-devkit/ai-plugin/mcp/message {
        proxy_pass http://127.0.0.1:8080/pf-devkit/ai-plugin/mcp/message;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

配置 Nginx 后，客户端 `url` 改为 `https://mcp.your-domain.com/pf-devkit/ai-plugin/mcp/sse`。

---

## 环境变量

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `FH_SERVER_ADDRESS` | 是 | — | DJI Cloud API 2 服务器地址（含协议，如 `http://<YOUR_SERVER_IP>`） |
| `FH_SERVER_PORT` | 否 | 443 | 服务器端口 |
| `FH_MOCK` | 否 | false | 设为 `true` 使用 mock 数据（无需真实服务器） |
| `FH_TRANSPORT` | 否 | stdio | 传输模式：`stdio`（IDE/MCP）或 `sse`（远程 HTTP） |
| `FH_ACCESS_TOKEN` | 是* | — | 工作空间访问令牌（真实模式必填） |
| `FH_WORKSPACE_UUID` | 是* | — | 工作空间 UUID（真实模式必填） |

> *`FH_ACCESS_TOKEN` 和 `FH_WORKSPACE_UUID` 在真实模式下（`FH_MOCK=false`）必填，Mock 模式下可省略。这两个凭证必须在服务**启动前**通过环境变量配置，不支持通过 `fh_connect` 参数在运行时传入。

## Dockerfile 关键依赖

SDK 的 VolcEngine RTC 和 Agora 原生库需要完整的 X11/EGL/GL 图形栈，即使在 headless 模式下也需要安装：

```dockerfile
FROM eclipse-temurin:17-jre-jammy

# 必须安装的系统库
RUN apt-get update && apt-get install -y --no-install-recommends \
    libatomic1 \          # VolcEngine RTC 基础依赖
    libgl1-mesa-glx \     # OpenGL
    libegl1-mesa \        # EGL
    libgles2-mesa \       # OpenGL ES
    libglib2.0-0 \        # GLib
    libx11-6 \            # X11 核心
    libxext6 \            # X11 扩展
    libxrender1 \         # X Render
    libxdamage1 \         # X Damage
    libxfixes3 \          # X Fixes
    libxcomposite1 \      # X Composite
    libxrandr2 \          # X RandR
    libxi6 \              # X Input
    libxcursor1 \         # X Cursor
    libxinerama1 \        # X Xinerama
    libxtst6 \            # X Test (XTst)
    libdrm2 \             # DRM
    libgbm1 \             # GBM
    libxshmfence1 \       # X SHM Fence
    libxxf86vm1 \         # XFree86 Video Mode
    libpulse0 \           # PulseAudio
    libasound2 \          # ALSA
    curl \
    && rm -rf /var/lib/apt/lists/*
```

## 遇到的问题及解决方案

### 问题 1：`libatomic.so.1: cannot open shared object file`

**现象：**
```
java.lang.UnsatisfiedLinkError: libVolcEngineRTC.so: libatomic.so.1: cannot open shared object file
```

**原因：** VolcEngine RTC 依赖 `libatomic1`，JRE 基础镜像（eclipse-temurin:17-jre-jammy）未包含。

**解决：** `apt-get install libatomic1`

---

### 问题 2：`libGL.so.1: cannot open shared object file`

**现象：**
```
java.lang.UnsatisfiedLinkError: libVolcEngineRTC.so: libGL.so.1: cannot open shared object file
```

**原因：** VolcEngine RTC 编译时链接了 OpenGL，即使在 headless 环境下也需要 Mesa 软件实现。

**解决：** `apt-get install libgl1-mesa-glx`

---

### 问题 3：`libEGL.so.1: cannot open shared object file`

**现象：**
```
java.lang.UnsatisfiedLinkError: libVolcEngineRTC.so: libEGL.so.1: cannot open shared object file
```

**原因：** EGL（Embedded GL）库缺失。

**解决：** `apt-get install libegl1-mesa`

---

### 问题 4：`libXdamage.so.1: cannot open shared object file`

**现象：**
```
java.lang.UnsatisfiedLinkError: libVolcEngineRTC.so: libXdamage.so.1: cannot open shared object file
```

**原因：** X11 Damage Extension 库缺失。VolcEngine RTC 链接了完整的 X11 图形栈。

**解决：** `apt-get install libxdamage1`

---

### 问题 5：`libXtst.so.6: cannot open shared object file`

**现象：**
```
java.lang.UnsatisfiedLinkError: libVolcEngineRTC.so: libXtst.so.6: cannot open shared object file
```

**原因：** X Test Extension 库缺失。

**解决：** `apt-get install libxtst6`

---

### 问题 6：SDK `registerWorkspaceToken` 返回 -1

**现象：**
```
Workspace registration returned sessionId=-1
```
导致 `getWorkspaceInstance(sessionId)` 无法获取 workspace 对象，设备列表查询 fallback 到 mock 数据。

**原因：** 传入空字符串或无效的 `workspace_uuid` 时注册失败。

**解决：** 使用无参 `FlightHubSdkManager.getWorkspaceInstance()` 获取默认 workspace 实例，不依赖 session 注册。

代码修改（`SdkService.java`）：
```java
long sessionId = FlightHubSdkManager.registerWorkspaceToken(wsUuid, token);
if (sessionId <= 0) {
    // 注册失败，尝试获取默认 workspace
    Workspace ws = FlightHubSdkManager.getWorkspaceInstance();
    if (ws != null) {
        this.workspace = ws;
        this.sdkSessionId = 1;
    }
}
```

---

### 问题 7：WebSocket 通知连接失败

**现象：**
```
CppWebSocket: URL 解析失败:  - 请检查URL格式是否正确 (支持 ws://host:port/path 或 wss://host:port/path)
```
SDK 持续尝试重连 WebSocket。

**原因：** SDK 初始化时未配置 `notificationServerAddress`（FlightHubSdkConfig 中的 WebSocket 推送地址）。

**影响：** 不影响 HTTP 层的设备查询功能（getDeviceList 等），仅实时推送不可用。

**解决（如需实时推送）：** 在 `FlightHubSdkConfig` 中设置：
```java
config.setNotificationServerAddress("ws://<YOUR_SERVER_IP>:PORT/path");
```

---

### 问题 8：`exec format error` / `.so` 加载失败 — 镜像架构与服务器不符

**现象：** 部署后容器启动失败，服务器 `docker logs` 显示：
```
exec /usr/bin/sh: exec format error
```
或：
```
standard_init_linux.go:228: exec user process caused: exec format error
```
也可能是 Java 启动后报 `UnsatisfiedLinkError: libflighthubsdk_jni.so`。

**原因：** 两种错配都指向同一个问题——镜像架构 ≠ 运行架构：
1. 在 Apple Silicon Mac（ARM64）上执行 `docker build` 时未指定 `--platform`，默认生成 ARM64 镜像，部署到 x86_64 Linux 服务器时内核无法执行不匹配的 ELF。
2. `--build-arg SDK_ARCH` 选用的 SDK 包与目标架构不一致，SDK 找不到对应 `lib/linux-<arch>/libflighthubsdk_jni.so`。

**解决：** 让 `--platform` 与 `SDK_ARCH` 同时指向目标架构：

```bash
# ❌ 错误 1：在 Apple Silicon 上默认构建出 ARM64 镜像（且未选 SDK 包）
docker build -f mcp/Dockerfile -t "$IMAGE_NAME" .

# ❌ 错误 2：平台与 SDK 包错配（amd64 镜像里塞了 aarch64 的 .so）
docker build --platform linux/amd64 --build-arg SDK_ARCH=arm64 -t "$IMAGE_NAME" .

# ✅ 正确：x86_64 服务器
docker build --platform linux/amd64 --build-arg SDK_ARCH=amd64 -f mcp/Dockerfile -t "$IMAGE_NAME" .

# ✅ 正确：aarch64 服务器（在 ARM 机器上原生构建，无需 --platform）
docker build --build-arg SDK_ARCH=arm64 -f mcp/Dockerfile -t "$IMAGE_NAME" .
```

> `mcp/deploy.sh` 不构建镜像，它只从镜像仓库拉取 CI 已构建好的多架构镜像再重启远端容器；镜像由 CI 构建。本地构建请用 `docker build --build-arg SDK_ARCH=<amd64|arm64>`（见上方命令）。

---

### 问题 9：Claude Code MCP 配置层级混乱，SSE 连接始终显示旧地址

**现象：** 修改了 `~/.claude/.mcp.json` 或 `~/.claude/settings.json` 中的 SSE 地址后，`/mcp` 面板仍然显示旧 URL；重启 Claude Code 后依然无效。

**根本原因：Claude Code 有三层 MCP 配置文件，优先级从高到低：**

| 优先级 | 文件路径 | 说明 | GUI 中标注 |
|--------|---------|------|-----------|
| **最高** | `~/.claude.json` | 主状态文件，GUI 操作（`/mcp add`）写入此处 | "User MCPs" |
| 中 | `~/.claude/settings.json` | 手动配置文件，支持 SSE | 无标注 |
| 低 | `~/.claude/.mcp.json` | MCP 专用配置 | 无标注 |

**`~/.claude.json` 始终覆盖其他两个文件**，因此在 `settings.json` 或 `.mcp.json` 中的修改会被静默忽略。

**定位方法：** 在 Claude Code 中执行 `/mcp`，面板顶部会明确显示：
```
User MCPs (/Users/<你的用户名>/.claude.json)
```
这就是实际生效的配置文件路径。

**解决：** 在 `~/.claude.json` 的 `mcpServers` 字段中直接修改：

```bash
# 用 Python 脚本安全修改（避免手动编辑大型 JSON 文件出错）
python3 - << 'EOF'
import json

with open('/Users/<你的用户名>/.claude.json', 'r') as f:
    data = json.load(f)

data['mcpServers']['flighthub-sse'] = {
    "type": "sse",
    "url": "http://<REMOTE_SERVER_IP>:8080/pf-devkit/ai-plugin/mcp/sse"
}

with open('/Users/<你的用户名>/.claude.json', 'w') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print("Updated:", data['mcpServers']['flighthub-sse'])
EOF
```

修改后**必须重启 Claude Code** 才能生效（热重载不会重新读取此文件）。

---

### 问题 10：SSE 配置缺少 `type` 字段，报 `command: expected string, received undefined`

**现象：** `/mcp` 面板顶部显示警告（黄色 ⚠）：
```
[Warning] [dji-cloud-api2-sse] mcpServers.dji-cloud-api2-sse: Skipped — invalid MCP server config 
for "dji-cloud-api2-sse": command: expected string, received undefined
```
且 `dji-cloud-api2-sse` 完全不出现在服务列表中（被跳过，不是"failed"）。

**原因：** `~/.claude.json` 中 `mcpServers` 里的 SSE 服务器配置缺少 `"type": "sse"` 字段：

```json
// ❌ 错误：缺少 type 字段，Claude Code 尝试按 stdio 解析，找不到 command 字段
{
  "dji-cloud-api2-sse": {
    "url": "http://<your-mcp-host>:8080/pf-devkit/ai-plugin/mcp/sse"
  }
}

// ✅ 正确：必须明确声明 type
{
  "dji-cloud-api2-sse": {
    "type": "sse",
    "url": "http://<your-mcp-host>:8080/pf-devkit/ai-plugin/mcp/sse"
  }
}
```

**解决：** 确保写入 `~/.claude.json` 时包含 `"type": "sse"` 字段（见上方问题 9 的修改脚本，已包含此字段）。

---

### 问题 11：MCP Server 在 Claude Code 中不显示（配置文件路径错误）

**现象：** `/mcp` 命令只显示 plugin-based MCP server（sdd、spectra），DJI Cloud API 2 完全不出现，也没有警告。

**原因：** 
- 只在 `~/.claude/.mcp.json` 中配置了 MCP，但此文件优先级最低，且在已有 `~/.claude.json` 配置时可能被完全忽略
- 或者在错误的项目目录下查看（Project MCPs 来自当前工作目录的 `.mcp.json`）

**解决：** 
- 用户级 MCP：配置到 `~/.claude.json` 的 `mcpServers` 字段（见问题 9）
- 项目级 MCP：在项目工作目录下创建 `.mcp.json`（此文件在 `/mcp` 面板中会标注来源路径）

---

## SDK 原生库加载清单

SDK 在 Docker 中成功加载的原生库（共 15 个）：

| 库 | 大小 | 用途 |
|----|------|------|
| libcrypto.so.1.1 | 2.7 MB | OpenSSL 加密 |
| libssl.so.1.1 | 618 KB | SSL/TLS |
| libwebsockets.so.12 | 193 KB | WebSocket 通信 |
| libpaho-mqtt3as.so.1 | 257 KB | MQTT 消息 |
| libbytenn.so | 4.6 MB | 字节跳动网络库 |
| libVolcEngineRTC.so | 42.5 MB | 火山引擎实时通信 |
| libRTCFFmpeg.so | 3.6 MB | RTC FFmpeg |
| libbytertc_vp8codec_extension.so | 1.9 MB | VP8 编解码 |
| libbytertc_nico_extension.so | 700 KB | NICO 扩展 |
| libbytertc_ffmpeg_audio_extension.so | 5.0 MB | FFmpeg 音频 |
| libbytertc_fdk-aac_extension.so | 949 KB | FDK-AAC |
| libaosl.so | 465 KB | Agora OSL |
| libagora-fdkaac.so | 743 KB | Agora FDK-AAC |
| libagora_rtc_sdk.so | 36.5 MB | Agora RTC SDK |
| **libflighthubsdk_jni.so** | **323 MB** | **DJI Cloud API 2 SDK 主 JNI 库** |

总计约 **420 MB** 原生库，首次启动提取耗时约 5-10 秒。

## 性能说明

- **首次启动：** 10-15 秒（提取原生库 + SDK 初始化）
- **后续工具调用：** < 1 秒（SDK 已初始化）
- **镜像大小：** 约 500 MB（JRE + 系统库 + fat JAR）
- **运行内存：** 约 512 MB

## 验证测试

```bash
# 发送 MCP 协议测试
(echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"test","version":"1.0.0"}}}'
sleep 12
echo '{"jsonrpc":"2.0","id":2,"method":"tools/call","params":{"name":"fh_connect","arguments":{"server_address":"http://<YOUR_SERVER_IP>","port":30812}}}'
sleep 5
echo '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"fh_device_list","arguments":{}}}'
sleep 20) | docker run --rm -i --platform linux/amd64 \
  -e FH_MOCK=false -e FH_TRANSPORT=stdio \
  dji/flighthub-mcp:latest
```

> 该命令假设镜像按 amd64 构建（`SDK_ARCH=amd64`）。若构建时用的是 `SDK_ARCH=arm64`，`--platform` 需相应改为 `linux/arm64`，两者必须一致。

预期返回真实设备数据（26 台设备，包括 DJI Dock 3、Matrice 4D 等）。

---

## 远程部署接入指南（推荐生产形态）

> 本节描述推荐的生产使用形态：MCP Server 统一部署到远程服务器，开发者在本地 Claude Code 中通过 SSE 接入，**无需在本机运行任何 Docker 容器**。

### 架构概览

```
开发者 MacBook
  └── Claude Code
        └── MCP Client (SSE)
              │
              │  HTTP/SSE  (内网/VPN)
              ▼
      <your-mcp-host>:8080
        └── Docker: pf-devkit-mcp
              └── DJI Cloud API 2 SDK (JNI, linux/amd64 或 linux/arm64)
                    │
                    │  gRPC/HTTP
                    ▼
              DJI Cloud API 2 平台服务
```

### 运维侧：部署/更新服务器（一键脚本）

```bash
# 在项目根目录执行（需要先复制并填写配置）
cp mcp/deploy.env.example mcp/deploy.env
vim mcp/deploy.env   # 填写 REMOTE_HOST、REMOTE_USER、REMOTE_PASS、FH_* 等

# 部署（自动完成：远端拉取镜像 → 远端重启 → 健康检查）
./mcp/deploy.sh
```

`deploy.sh` 的 4 个阶段：
1. **前置检查** — 检查 `deploy.env` 存在、SSH 连通、远端 Docker 可用
2. **远端拉取镜像** — 在远端执行 `docker compose pull`，拉取 `${REGISTRY}/paas-sdk-devkit/mcp:${IMAGE_TAG}`
3. **远端重启** — `docker compose up -d` 滚动重启
4. **健康检查** — `curl /pf-devkit/ai-plugin/mcp/health` 轮询，30 秒超时

> `deploy.sh` **不在本地构建镜像**。镜像由 CI 构建为多架构 manifest，推送到镜像仓库后由远端按自身架构拉取对应层。需要本地构建时，请用 `docker build --build-arg SDK_ARCH=<amd64|arm64>`（见问题 8）。

### 用户侧：配置 Claude Code 接入远程 MCP Server

**第一步：确认 `~/.claude.json` 中的配置**

```bash
python3 - << 'EOF'
import json

config_path = '/Users/<你的用户名>/.claude.json'

with open(config_path, 'r') as f:
    data = json.load(f)

# 写入或更新 flighthub-sse
data.setdefault('mcpServers', {})['flighthub-sse'] = {
    "type": "sse",
    "url": "http://<REMOTE_SERVER_IP>:8080/pf-devkit/ai-plugin/mcp/sse"
}

with open(config_path, 'w') as f:
    json.dump(data, f, indent=2, ensure_ascii=False)

print("已写入:", data['mcpServers']['flighthub-sse'])
EOF
```

> ⚠️ **关键注意事项：**
> - 必须包含 `"type": "sse"`，缺少此字段会导致服务器被静默跳过（报 `command: expected string`）
> - `~/.claude.json` 优先级高于 `~/.claude/settings.json` 和 `~/.claude/.mcp.json`，在后两者修改无效
> - 修改后必须**完全重启 Claude Code**（Cmd+Q 退出再重启），不支持热重载

**第二步：验证连接**

重启 Claude Code 后执行 `/mcp`，应看到：
```
User MCPs (/Users/<你的用户名>/.claude.json)
flighthub-sse · ✓ connected · N tools
```

如果显示 `✗ failed`，检查：
1. 服务器是否在线：`curl http://<REMOTE_SERVER_IP>:8080/pf-devkit/ai-plugin/mcp/health`
2. 网络是否可达（内网/VPN 是否连接）
3. `~/.claude.json` 中 `type` 字段是否存在

**第三步：使用 MCP Tools**

成功连接后，在 Claude Code 对话中可直接使用 DJI Cloud API 2 能力：

```
# 查看可用设备
使用 fh_device_list 查询当前工作空间的设备

# 创建飞行任务
帮我用 fh_flight_task 创建一个航线任务，使用 wayline_uuid=xxx

# 飞行执行
使用 fh_wayline_execute 执行航线，drone_sn=xxx，dock_sn=xxx
```

### 常见排查命令

```bash
# 检查远程服务器容器状态
ssh user@<REMOTE_SERVER_IP> "docker ps | grep flighthub"

# 查看服务器实时日志
ssh user@<REMOTE_SERVER_IP> "docker logs -f pf-devkit-mcp --tail 50"

# 手动验证 SSE 端点（应收到 event:endpoint 事件）
curl -N -H "Accept: text/event-stream" http://<REMOTE_SERVER_IP>:8080/pf-devkit/ai-plugin/mcp/sse

# 健康检查
curl http://<REMOTE_SERVER_IP>:8080/pf-devkit/ai-plugin/mcp/health | python3 -m json.tool

# 查看本地 Claude Code 实际加载的 MCP 配置
python3 -c "
import json
with open('/Users/<你的用户名>/.claude.json') as f:
    d = json.load(f)
for k, v in d.get('mcpServers', {}).items():
    print(f'{k}: {v}')
"
```
