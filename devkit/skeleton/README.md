# FH Skeleton — 集成商快速接入骨架

基于 DJI Cloud API 2 SDK 的集成商接入骨架项目，提供开箱即用的 Spring Boot 后端 + Vue 3 前端，集成商开发者只需实现业务逻辑即可完成 SDK 集成。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.4.1 |
| AI 集成 | Spring AI | 1.1.8 |
| 数据库 | MySQL + Spring Data JPA | — |
| 前端框架 | Vue 3 + TypeScript + Vite | — |
| Java | JDK 17+ | — |
| 构建 | Maven 多模块 | — |

## 模块结构

```
skeleton/
├── kernel/                    # 核心层（SDK 抽象）
│   ├── kernel-spi/            #   SPI 接口定义（Event、Service 契约）
│   ├── kernel-impl/           #   公共实现（事件总线、异步配置）
│   ├── kernel-mock-sdk/       #   Mock 实现（本地开发，无需真实 SDK）
│   └── kernel-real-sdk/       #   真实 SDK 适配（生产部署）
├── feature/                   # 功能模块层（按领域拆分，共 8 个）
│   ├── feature-device/        #   设备管理（列表、OSD、HMS 告警）
│   ├── feature-wayline/       #   航线任务管理
│   ├── feature-media/         #   媒体文件管理
│   ├── feature-terra/         #   三维重建任务
│   ├── feature-flight-area/   #   飞行区域管理
│   ├── feature-dashboard/     #   仪表盘概览（无条件启用）
│   ├── feature-user/          #   用户认证（注册/登录/JWT/RBAC）
│   └── feature-chat/          #   AI Chat Bot（Spring AI + RAG，默认关闭）
├── app/fh-skeleton-app/       # 启动模块（组装所有 feature）
├── web/                       # 前端（Vue 3 + FH2 PaaS 组件）
├── docker/                    # Docker Compose 本地开发环境
├── fh-skeleton-bom/           # BOM（统一依赖版本）
└── docs/                      # 使用说明文档
```

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.8+
- Node.js 18+（前端）
- MySQL 8.0+（可用 Docker 替代）
- **私有 SDK JAR**（宿主机上的任何本地 Maven 构建都需要，包括默认 Mock 构建；Docker Compose 方式在镜像内自动安装，宿主机无需执行此步）：`../libs/{amd64,arm64}/flight-hub-sdk-3.3.7.jar` 不在任何远端仓库，
  feature 模块的 `spi/sdk` 实现类在编译期依赖它，首次构建前按架构安装到本地 Maven 仓库（详见 [../libs/README.md](../libs/README.md)）：
  ```bash
  cd skeleton
  mvn install:install-file \
    -Dfile=../libs/amd64/flight-hub-sdk-3.3.7.jar \
    -DgroupId=com.dji.flighthubsdk \
    -DartifactId=flight-hub-sdk \
    -Dversion=3.3.7 \
    -Dpackaging=jar \
    --settings ../config/settings.xml
  ```
  > arm64 开发机把 `-Dfile` 换成 `../libs/arm64/flight-hub-sdk-3.3.7.jar`。

### 方式一：Docker Compose（推荐）

`skeleton/docker/docker-compose.yml` 只包含后端与前端两个服务，**本身不提供数据库**，单独执行无法启动后端。本地开发需叠加 `deploy-local-mock.override.yml`：该覆盖文件会额外启动一个 `mysql:8.0` 服务，并注入 `FH_DB_URL`、`FH_JWT_SECRET`、`FH_SDK_MODE=mock` 等变量。

首次使用先编辑 `skeleton/deploy.env.local-mock`，填写 `MYSQL_ROOT_PASSWORD` / `FH_DB_PASSWORD`（两者保持一致）和 `FH_JWT_SECRET`（可用 `openssl rand -hex 32` 生成；必须替换模板中的 `CHANGE_ME_*` 占位值，否则服务端拒绝启动）。若 `FH_JWT_SECRET` 缺失，`docker compose` 会直接报错退出，不会带起任何容器：

```bash
docker compose -f skeleton/docker/docker-compose.yml \
               -f skeleton/docker/deploy-local-mock.override.yml \
               --env-file skeleton/deploy.env.local-mock up --build
```

> 注意：`--env-file` 只提供 compose 插值，变量还必须出现在服务的 `environment:` 段才会注入容器。基线 compose 已显式透传 `FH_JWT_SECRET` 与 `FH_DB_*`；早期版本漏传该变量时，Spring 会把 `${FH_JWT_SECRET}` 当作字面量绑定，报"长度不足（当前 16 字节）"。

启动后后端运行在 `http://localhost:8081`，前端运行在 `http://localhost:8082`（覆盖文件将容器内 8080 映射到宿主机 8081/8082）。

### 方式二：本地开发

#### 1. 启动后端

```bash
cd skeleton

# 编译（默认 Mock 模式，不编译 kernel-real-sdk；feature 模块的 SDK 实现类仍需 SDK JAR，请先完成上方安装）
mvn clean package -DskipTests

# 接入真实设备时需要先安装私有 SDK JAR（见上方前置条件），再执行：
mvn clean package -DskipTests -Psdk-real

# 启动前需准备：
# 1. 本机可连的 MySQL 8.0（mock profile 同样使用 MySQL，不内置 H2；空库会由 JPA 自动建表）
# 2. FH_JWT_SECRET 必填，至少 32 字节（可用 `openssl rand -hex 32` 生成）
# 3. FH_DB_URL / FH_DB_USERNAME / FH_DB_PASSWORD 指向上述 MySQL
# 4. FH_SDK_MODE=mock —— 可选：application-mock.yml 已将 fh.skeleton.sdk.mode 设为 mock，
#    仅激活 mock profile 即可；显式设置便于阅读并防止 profile 与 mode 错配
export FH_SKELETON_PROFILE=mock
export FH_SDK_MODE=mock
export FH_JWT_SECRET=$(openssl rand -hex 32)
export FH_DB_URL='jdbc:mysql://localhost:3306/fh_skeleton?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export FH_DB_USERNAME=root
export FH_DB_PASSWORD=your-mysql-password
java -jar app/fh-skeleton-app/target/fh-skeleton-app-0.1.0.jar

# 上述变量在 Docker Compose 方式一中已由覆盖文件自动配置，无需手动导出。
```

#### 2. 启动前端

```bash
cd skeleton/web
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`，API 请求代理到后端 8080 端口。

## 配置说明

所有配置通过环境变量注入，关键变量：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `FH_SKELETON_PORT` | `8080` | 后端服务端口 |
| `FH_SKELETON_PROFILE` | `sdk` | Spring profile：`mock` 为本地开发的可选项，生产固定 `sdk` |
| `FH_SDK_MODE` | `sdk` | SDK 子系统模式：`mock`（本地，配合 mock profile）/ `sdk`（生产） |
| `FH_JWT_SECRET` | — | JWT 签名密钥（**生产必填**，至少 32 字节） |
| `FH_DB_URL` | `jdbc:mysql://localhost:3306/fh_skeleton` | 数据库连接 |
| `FH_DB_USERNAME` | `root` | 数据库用户名 |
| `FH_DB_PASSWORD` | — | 数据库密码 |
| `VITE_FH2_ADDRESS` | — | FH2 前端服务地址 |
| `VITE_FH2_PORT` | `30812` | FH2 后端端口 |

完整配置参考：`deploy.env.example`

### Chat Bot 功能配置（可选）

Chat Bot 默认关闭，启用需设置：

| 变量 | 说明 |
|------|------|
| `fh.chat.enabled=true` | 启用 Chat 功能 |
| `spring.ai.anthropic.api-key` | AI Provider API Key |
| `fh.chat.rag.enabled=true` | 启用 RAG 知识库增强 |

## 功能模块说明

### feature-user：用户认证

提供完整的用户注册/登录/JWT 鉴权体系：

- **注册**：`POST /api/v1/users/register`（首个用户自动成为 admin；默认关闭，需 `FH_USER_REGISTRATION_ENABLED=true` 临时开启）
- **登录**：`POST /api/v1/users/login`（返回 JWT token）
- **当前用户**：`GET /api/v1/users/me`（需 Authorization header）
- **安全过滤器**：`AuthenticatingSecurityFilter` 自动解析 JWT 并注入 SecurityContext
- **权限注解**：`@RequireAuthenticated` 标注需要登录的接口

集成商扩展点：实现 `AuthenticationProvider` SPI 可对接自有认证系统。

### feature-device：设备管理

提供设备列表查询和状态订阅：

- **设备列表**：`GET /api/v1/devices`
- **设备详情**：`GET /api/v1/devices/{sn}`
- **事件监听**：通过 `@EventListener` 消费 `DeviceOnlineEvent`、`OsdUpdateEvent` 等

Mock 模式下返回模拟设备数据，生产模式自动对接 DJI Cloud API 2 SDK。

### feature-chat：AI Chat Bot

基于 Spring AI 的智能助手，支持流式对话 + Tool 调用确认 + RAG 知识增强：

- **流式对话**：`POST /api/v1/chat/stream`（SSE 响应）
- **Tool 确认**：`POST /api/v1/chat/confirm/{id}`
- **RAG 知识库**：自动加载 `chat-docs/` 下的文档作为上下文

详细使用指南参见 `docs/user-guide/使用说明文档.md`。

## 前端说明

前端基于 Vue 3 + TypeScript + Vite，集成了 FH2 PaaS 前端组件：

```bash
cd skeleton/web

npm run dev        # 开发服务器
npm run build      # 生产构建
npm run test       # 运行测试
```

### 主要功能页面

- **驾驶舱**（Cockpit）：实时飞行监控
- **航线管理**（Wayline）：航线创建/编辑/预览
- **设备管理**：设备列表与状态
- **AI 助手**：Chat Bot 对话界面

### 鉴权

前端使用 `authFetch()` 封装所有 API 请求，自动附加 JWT token 并处理 401 跳转登录。

## 运行测试

```bash
cd skeleton

# 全量测试
mvn test

# 单模块测试
mvn test -pl feature/feature-user

# 带覆盖率
mvn verify -Pci
```

测试全部基于 Mock，不依赖外部服务，可在任何平台运行。

## 部署

### 总入口：部署指南

面向 **集成商的完整部署指南**（普通机器 / Docker Compose / Kubernetes 三种形态 + MCP Server 部署）见 [部署指南.md](./docs/部署指南.md)。以下为内部远端部署脚本方式。

### 远端部署（SSH + Docker Compose）

```bash
# 1. 配置部署环境
cp skeleton/deploy.env.example skeleton/deploy.env
# 编辑 deploy.env 填写服务器信息和环境变量

# 2. 执行部署
./skeleton/deploy.sh
```

详细部署指南见 [部署指南.md](./docs/部署指南.md)（唯一部署文档）。

> **注意**：DJI 内部流水线额外包含私有化构建 stage（依赖内部镜像仓库和 Wibu AxProtector 工具链），该流水线配置不随本仓库对外分发，外部集成商**无需也无法使用**，可安全忽略，不影响标准构建和部署流程。

### 生产模式（接入真实 SDK）

生产环境需使用真实 SDK profile 打包：

```bash
mvn clean package -Psdk-real -DskipTests
```

并配置 SDK 连接参数：

```bash
FH_SKELETON_PROFILE=sdk
FH_SKELETON_SDK_ADDRESS=https://your-flighthub-server
FH_SKELETON_SDK_TOKEN=your-access-token
```

## 集成商扩展指南

### 添加新 Feature 模块

1. 在 `feature/` 下创建新 Maven 模块（参考 `feature-device` 结构）
2. 编写 `XxxAutoConfiguration` 类，通过 `@ConditionalOnProperty` 控制启用
3. 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册
4. 在 `app/fh-skeleton-app/pom.xml` 添加依赖

### 对接自有认证系统

实现 `AuthenticationProvider` 接口：

```java
@Component
public class MyAuthProvider implements AuthenticationProvider {
    @Override
    public Optional<SecurityContext> authenticate(HttpServletRequest request) {
        // 从 request 中解析你的认证信息（如 SSO token、OAuth2 等）
        // 返回 SecurityContext 或 Optional.empty()（匿名）
    }
}
```

### 消费 SDK 事件

```java
@Component
public class MyEventListener {
    @EventListener
    public void onDeviceOnline(DeviceOnlineEvent event) {
        // 处理设备上线事件
    }
}
```

## 相关文档

- [部署指南](docs/部署指南.md) — 唯一部署文档（普通机器/Docker/K8s）
- [使用说明文档](docs/user-guide/使用说明文档.md) — 功能操作指南（含截图）

## 安全加固指南

集成商部署前务必完成以下安全检查清单：

### 必须项

| 项目 | 说明 |
|------|------|
| JWT 密钥 | `FH_JWT_SECRET` 设置为随机 32+ 字节字符串，**禁止使用默认值** |
| 数据库 SSL | 生产环境 `FH_DB_URL` 必须包含 `useSSL=true&requireSSL=true` |
| 注册开关 | `fh.user.registration.enabled` 默认关闭，首用户由管理员在可信网络创建 |
| Profile 隔离 | 生产部署 `FH_SKELETON_PROFILE=sdk`，永远不在公网启用 `mock` profile |
| 容器非 root | Docker 镜像默认以 `appuser` 用户运行，禁止修改为 root |
| 敏感文件 | `.env`、`*.key`、`*.pem` 等已在 `.gitignore` 和 `.dockerignore` 中排除 |

### 推荐项

| 项目 | 说明 |
|------|------|
| HTTPS | 前端 Nginx 和后端均应启用 TLS，配合 `useSSL=true` 端到端加密 |
| 密码策略 | 系统要求密码至少 8 位且含大小写字母和数字 |
| JWT 过期 | 默认 24h，高安全场景建议缩短至 2-8h |
| 日志脱敏 | 生产日志级别建议 `WARN`，避免 DEBUG 级别泄露设备 SN 等敏感信息 |
| 网络隔离 | MinIO / MySQL / Redis 等内部服务不应暴露公网端口 |
| 依赖审计 | 定期执行 `mvn dependency-check:check` 和 `npm audit` |
