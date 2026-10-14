# Docker Compose SSE 远程部署步骤

> 来源：`mcp/docs/部署指南.md` §4、§8。AI 按此推进。
> 适用：将 MCP Server 部署到远程 Linux x86_64 服务器，多客户端通过 SSE 共享接入。

## 架构概览

```
开发者 MacBook / PC
  └── Claude Code / Cursor
        └── MCP Client (SSE)
              │
              │  HTTP/SSE  (内网/VPN)
              ▼
      <remote-server>:8080
        └── Docker: pf-devkit-mcp
              └── DJI CloudAPI2 SDK (JNI, linux/amd64)
                    │
                    │  gRPC/HTTP
                    ▼
              DJI CloudAPI2 平台服务
```

## 前提条件

- 远程服务器：Linux x86_64、Docker 24+、docker compose v2
- 本地机器：有 SSH 访问远程服务器 + Docker（用于构建镜像）
- DJI CloudAPI2 控制台已创建工作空间，获取 `access_token` + `workspace_uuid`

## 步骤清单

### 1. 克隆仓库（远端或本地）

```bash
git clone <repo-url> dji-flighthub-devkit
cd dji-flighthub-devkit
```

### 2. 配置环境变量

```bash
cd mcp
cp .env.example .env
```

AI 生成密钥并拼接完整 `.env` 内容（使用 `prompts/gen-secrets.sh` 生成 `FH_SSE_AUTH_TOKEN`）。

必填项：
| 变量 | 来源 |
|---|---|
| `FH_SERVER_ADDRESS` | DJI CloudAPI2 服务器地址（含协议） |
| `FH_ACCESS_TOKEN` | DJI CloudAPI2 控制台 → 工作空间 → 访问令牌 |
| `FH_WORKSPACE_UUID` | DJI CloudAPI2 控制台 → 工作空间 → UUID |
| `FH_SSE_AUTH_TOKEN` | `openssl rand -hex 32` 生成 |

### 3. 构建并推送 Docker 镜像

```bash
cd mcp
mvn clean package -DskipTests
docker build --platform linux/amd64 -t ${REGISTRY}/paas-sdk-devkit/mcp:latest .
docker push ${REGISTRY}/paas-sdk-devkit/mcp:latest
```

> Apple Silicon 必须加 `--platform linux/amd64`，否则生成 ARM 镜像在服务器报 `exec format error`。
> `REGISTRY` 为 `deploy.env` 中配置的镜像仓库地址（如 Harbor），`deploy.sh` 从该仓库拉取镜像。

### 4a. 方式 A：deploy.sh 一键部署（推荐）

前置：步骤 3 已将镜像推送到 Registry，远端服务器已 `docker login` 该 Registry。

```bash
# 在项目根目录
cp mcp/deploy.env.example mcp/deploy.env
# 填写 REMOTE_HOST / REMOTE_USER / REMOTE_PASS / REGISTRY 等
vim mcp/deploy.env

# 执行部署（远端从 Registry 拉取镜像并重启）
./mcp/deploy.sh
```

deploy.sh 四阶段：
1. **前置检查** — 校验 `deploy.env`、`sshpass` 依赖、SSH 连通
2. **远端拉取** — `docker pull ${REGISTRY}/paas-sdk-devkit/mcp:${IMAGE_TAG}`
3. **远端重启** — `docker compose up -d --force-recreate`
4. **健康检查** — `curl /pf-devkit/ai-plugin/mcp/health` 轮询（60s 超时）

### 4b. 方式 B：手动 docker compose 部署

在远程服务器上操作：

```bash
# 将 .env 和 docker-compose.yml 传到服务器
scp mcp/.env mcp/docker-compose.yml user@server:/opt/mcp/

# SSH 登录服务器
ssh user@server
cd /opt/mcp

# 拉取/加载镜像后启动
docker compose up -d
```

### 5. 验证健康检查

```bash
# 健康端点
curl http://<server-ip>:8080/pf-devkit/ai-plugin/mcp/health
# 期望：{"status":"UP","timestamp":"..."}

# 就绪端点（工具注册完成）
curl http://<server-ip>:8080/pf-devkit/ai-plugin/mcp/ready
# 期望：200 OK

# SSE 流验证（应收到 event:endpoint 事件）
curl -N -H "Accept: text/event-stream" \
  -H "Authorization: Bearer <FH_SSE_AUTH_TOKEN>" \
  http://<server-ip>:8080/pf-devkit/ai-plugin/mcp/sse
```

### 6. 配置客户端接入

见 `snippets/claude-code-sse.json`。在 `~/.claude.json` 的 `mcpServers` 字段中添加：

```json
{
  "dji-cloud-api2": {
    "type": "sse",
    "url": "http://<server-ip>:8080/pf-devkit/ai-plugin/mcp/sse",
    "headers": {
      "Authorization": "Bearer <FH_SSE_AUTH_TOKEN>"
    }
  }
}
```

> 必须包含 `"type": "sse"`，缺失会被按 stdio 解析报错。
> 修改 `~/.claude.json` 后必须**完全重启 Claude Code**（Cmd+Q 再启动）。

### 7. 验证 MCP 连接

重启 Claude Code 后执行 `/mcp`，应看到：
```
User MCPs (/Users/<username>/.claude.json)
dji-cloud-api2 · ✓ connected · 51 tools
```

## Nginx 反向代理（可选，HTTPS）

```nginx
server {
    listen 443 ssl;
    server_name mcp.your-domain.com;
    ssl_certificate     /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location /pf-devkit/ai-plugin/mcp/ {
        proxy_pass http://127.0.0.1:8080/pf-devkit/ai-plugin/mcp/;
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_cache off;
        proxy_read_timeout 3600s;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header Connection '';
    }
}
```

> SSE 必须禁用 `proxy_buffering`，否则事件流被截断。

## 常见坑（AI 需主动检查）

| 坑 | 检查/处理 |
|---|---|
| Apple Silicon 构建 exec format error | 构建时加 `--platform linux/amd64` |
| 启动失败 StartupAuthValidator | `FH_SSE_AUTH_TOKEN` 留空或占位值，生成 `openssl rand -hex 32` |
| Ubuntu 26.04+ .so 加载失败 | 设置 `GLIBC_TUNABLES=glibc.rtld.execstack=2` |
| 镜像约 500MB | SDK native lib 420MB，正常 |
| SSE 流被 Nginx 截断 | `proxy_buffering off` + `proxy_cache off` |
| 客户端配置不生效 | 确认写在 `~/.claude.json`（非 settings.json），且含 `"type":"sse"` |
| deploy.sh 拉取失败 | 确认远端已 `docker login` Registry，且步骤 3 已 push 镜像 |
