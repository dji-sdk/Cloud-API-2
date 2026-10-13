# Docker Compose 部署步骤

> 来源：`skeleton/docs/部署指南.md` §4。AI 按此推进。
> 适用：单机容器化，推荐多数 ISV 首选。支持两种镜像来源：**从 Registry 拉取**或**从源码构建**。

## 选择镜像来源

```
用户有镜像仓库（Harbor / ACR / ECR / Docker Hub）？
├─ 是 → 走"方式 A：构建推送 + 拉取部署"
└─ 否 → 走"方式 B：源码构建部署"（无需 Registry，直接在部署机上 build）
```

## 步骤清单

### 1. 配置 .env

```bash
cd <repo-root>/skeleton
cp deploy.env.example deploy.env
# 必填：MYSQL_ROOT_PASSWORD / FH_DB_PASSWORD / FH_JWT_SECRET / FH_CHAT_MCP_URL / FH_PROFILE + FH_SKELETON_SDK_* / VITE_FH2_*
# 方式 A 额外必填：REGISTRY / IMAGE_TAG
```

> AI 用 gen-secrets.sh 生成密钥，拼接完整 deploy.env 内容给用户。

### 2A. 方式 A：构建推送 + 拉取部署（有 Registry）

```bash
cd <repo-root>
docker build --platform linux/amd64 -f skeleton/docker/Dockerfile -t <REGISTRY>/paas-sdk-devkit/skeleton:<TAG> .
docker build -f skeleton/web/Dockerfile -t <REGISTRY>/paas-sdk-devkit/skeleton-ui:<TAG> skeleton/web/
docker push <REGISTRY>/paas-sdk-devkit/skeleton:<TAG>
docker push <REGISTRY>/paas-sdk-devkit/skeleton-ui:<TAG>
```

启动：
```bash
docker compose -f skeleton/docker/docker-compose.yml --env-file skeleton/deploy.env pull
docker compose -f skeleton/docker/docker-compose.yml --env-file skeleton/deploy.env up -d
```

> 平台：后端镜像构建/运行需 `--platform linux/amd64`（Apple Silicon 加 `DOCKER_DEFAULT_PLATFORM=linux/amd64`）。

### 2B. 方式 B：从源码构建部署（无 Registry）

无需推送镜像，直接在部署机上从源码 multi-stage 构建：

```bash
cd <repo-root>
docker compose -f skeleton/docker/docker-compose.yml --env-file skeleton/deploy.env up --build -d
```

> docker-compose.yml 已配置 `build:` 段，`--build` 触发本地 multi-stage 构建（Maven 编译 + JRE 运行时 + 前端 Vite 构建 + Nginx）。
> 首次构建约 5-10 分钟（下载 Maven 依赖），后续重建利用 Docker layer 缓存会快很多。
> Apple Silicon 用户需设置：`DOCKER_DEFAULT_PLATFORM=linux/amd64`

### 3. 验证

```bash
curl http://<server-ip>:<BACKEND_PORT>/pf-devkit/ai-plugin/skeleton/actuator/health
# {"status":"UP"}
```

## 本地快速开始（mock）

> ⚠️ 即使 mock 模式，前端仍需 `VITE_FH2_ADDRESS` 非空（`useFH2Config` 初始化时校验）。
> 不配置会显示"FH2_ADDRESS 未配置。本地开发请复制 .env.example 为 .env 并填写"。
>
> ⚠️ mock 模式建议 `FH_CHAT_ENABLED=false`。Chat/RAG 启用时启动阶段会调 Embedding API，
> 若 AI API key 无效会导致 **启动崩溃**（`Application run failed`），不仅是警告。
>
> ⚠️ **必须同时设置 `FH_SDK_MODE=mock`**。仅设 `FH_PROFILE=mock`（Spring profile）不够——
> SDK 子系统装配条件读 `fh.skeleton.sdk.mode` 属性（默认 `sdk`），不显式设置则 RealSDK 激活、
> MockSDK 不注册虚拟设备，设备列表为空。
>
> ⚠️ **`VITE_MOCK_ENABLED` 必须为 `false`**。设为 `true` 时前端 login 返回假 token，
> 而设备 API 无 mock 分支，假 token 请求后端 → 401 → 跳转 welcome 页。

```bash
cd skeleton/docker
cp .env.example .env
# 必须填写 VITE_FH2_ADDRESS（可填任意合法 URL，mock 模式不实际连接）：
#   FH2_ADDRESS=http://localhost
# 或填真实 FH2 地址（如有）：
#   FH2_ADDRESS=http://<INT_FLHUB_XX>   # 占位示例，为内部测试环境地址，请替换为实际平台域名（如需）
docker compose up --build
curl http://localhost:8080/pf-devkit/ai-plugin/skeleton/actuator/health   # {"status":"UP"}
docker compose down
```

## 常见坑（AI 需主动检查）

| 坑 | 检查/处理 |
|---|---|
| Apple Silicon 构建 platform 报错 | `DOCKER_DEFAULT_PLATFORM=linux/amd64` |
| 镜像 ~700MB | SDK native lib 404MB，正常 |
| 容器内 SDK initialize 失败 | `.env` 6 个 SDK 凭据是否正确（4 个连接参数 + 2 个鉴权 header）；`docker compose logs` |
| 前端地图不显示 | `VITE_MAP_PROVIDER` 与 key 匹配（bing/tianditu 需 key） |
| Harbor 拉取 401 | 远端需先 `docker login <registry-host>` |
| deploy.env 变量设了但容器内没有 | `deploy-compose.yml` 的 `environment:` 必须显式列出需传入容器的每个变量 |
| Dashboard 全空 + 日志 401 missing auth_keys | 平台开了 HTTP 鉴权网关，配置 `FH_SKELETON_SDK_AUTH_HEADER_KEY/VALUE` |
| 语音输入报"请允许麦克风权限" | nginx 的 `Permissions-Policy` header 禁用了 microphone。改为 `microphone=(self)` |
| 镜像 pull 显示 up to date 但代码不对 | 对比 `docker inspect --format='{{.Created}}'` 与 git merge 时间；不一致则需重新构建推送 |
