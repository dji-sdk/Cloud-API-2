---
name: cloudapi2-skeleton-deploy
description: |
  仅在用户要求部署或清理 FH Skeleton（DJI FlightHub PaaS SDK ISV 接入骨架）到自有环境时使用。
  适用场景：普通机器 / Docker Compose / Kubernetes 三种形态的 skeleton 部署引导与清理卸载，
  含环境探测、形态选型、密钥生成、配置填写、构建启动、健康检查验证、部署/清理排障。
  关键词：部署, 清理, 卸载, 下线, remove, cleanup, undeploy, 清理部署, 卸载骨架,
  deploy, skeleton 部署, 普通机器部署, docker 部署, k8s 部署, kubernetes 部署,
  部署到这台机器, 移除部署, 撤销部署, 环境准备, 镜像构建, native lib, systemd, nginx, ingress,
  FH_JWT_SECRET, FH_SKELETON_SDK, MCP Server 部署
author: DJI CloudAPI2 Team
version: 1.0.0
---

# FH Skeleton 部署 Skill

## 适用场景

当开发者描述以下场景时触发本 Skill：

- 将 FH Skeleton 部署到自有服务器（普通机器 / Docker Compose / Kubernetes）
- 部署前的环境准备、选型咨询、密钥/密码生成
- 部署过程中的配置填写、构建、启动、验证引导
- 部署失败后的排障（日志定位、错误码排查）
- **清理/卸载已部署的 FH Skeleton**（停止服务、移除容器/K8s 资源、按需删数据）

## 半自动交互协议（硬规则）

部署是**引导式半自动**流程：AI 负责思考与生成，用户负责执行系统级操作并回报输出。

| 操作 | 谁做 |
|---|---|
| 环境探测、密钥生成、配置拼装、命令生成、结果解析、日志排障 | **AI** |
| `sudo apt` / `docker build|push` / `kubectl apply` / `systemctl` / `mysql` | **用户执行**，AI 给精确命令并等待回报 |
| 危险操作（删数据、重建卷、覆盖已有配置） | **必须先确认** |
| 清理部署（`docker compose down -v` / `kubectl delete namespace` / 删数据库 / `rm -rf` 配置） | **必须先确认"保留数据 or 清除数据"**，删除命令逐条二次确认 |

**流程约束**：
1. 每步只给一个命令（或一组强相关的命令），等用户回报输出后再进入下一步，禁止一口气抛全部命令
2. 用户回报输出后，AI 先**判断成败**（成功标志/错误标志），再决定继续或排障
3. 任何 `FH_JWT_SECRET` 等必填变量缺失时，先生成再继续，**禁止用默认值/占位符替代**
4. 遇到部署指南未覆盖的错误时，加载 `references/部署排障.md` 按现象定位
5. **清理部署时**：先加载 `references/清理部署.md` 并确认数据保留/清除策略，再逐步推进

## 三形态决策树

```
用户说"帮我部署"
→ 加载 prompts/env-probe.sh 内容，让用户执行（或读取环境）
→ 解析探测结果：OS / 架构 / JDK / Maven / Node / Docker / kubectl / MySQL
→ 按以下逻辑推荐形态：
   ├─ 已有 K8s 集群 + 镜像仓库 → 推荐 Kubernetes（加载 references/k8s部署步骤.md）
   ├─ 单机 + Docker 24+ → 推荐 Docker Compose（加载 references/docker部署步骤.md）
   └─ 单机无 Docker（或已有物理机/虚拟机）→ 推荐普通机器（加载 references/普通机器部署步骤.md）
→ 若探测到非 amd64 架构：明确提示"SDK JNI 原生库仅支持 Linux x86_64（amd64），
   sdk 模式无法在该架构运行；可选 mock 模式演示或换 amd64 机器"
→ 与用户确认形态后，进入对应 references 的步骤清单逐步推进
```

## 各形态步骤入口

| 形态 | 步骤清单 | 关键产物 |
|---|---|---|
| 普通机器 | `references/普通机器部署步骤.md` | `deploy/native-lib-extract.sh`、`deploy/skeleton.service`、`deploy/skeleton-ui.conf` |
| Docker Compose | `references/docker部署步骤.md` | `docker/docker-compose.yml`、`deploy-compose.yml`、`deploy.env.example` |
| Kubernetes | `references/k8s部署步骤.md` | `deploy/k8s/*.yaml`（9 个 manifest） |
| 清理/卸载（三形态） | `references/清理部署.md` | 保留数据 vs 清除数据的完整卸载命令 |

## 必填变量速查（缺失即停）

见 `references/环境变量速查.md`。核心必填：

| 变量 | 生成命令 | 说明 |
|---|---|---|
| `FH_JWT_SECRET` | `openssl rand -hex 32` | JWT 签名密钥，至少 32 字节，**未配启动失败** |
| `FH_DB_PASSWORD` / `MYSQL_ROOT_PASSWORD` | `openssl rand -hex 24` | 数据库密码 |
| `FH_SKELETON_SDK_TOKEN` | 从 FlightHub 控制台获取 | SDK 身份令牌（sdk profile） |
| `FH_SKELETON_SDK_ADDRESS` | — | SDK 服务地址 |
| `FH_SKELETON_SDK_NOTIFICATION_SERVER` | — | SDK 通知服务地址（sdk profile 必填） |

## 排障路由

```
用户回报报错信息 / 部署卡住
→ 加载 references/部署排障.md，按"现象→排查动作"定位
→ 常见：JWT 未配 / SDK init 失败 / .so 加载失败 / 健康检查超时 / SSE 被 buffer / 前端 404
→ 修复后回到原步骤继续
```

## 清理路由

```
用户说"清理/卸载/下线 skeleton"
→ 加载 references/清理部署.md（确认形态 + 保留数据 or 清除数据）
→ 按三形态卸载步骤逐步执行：先停服务/移除运行态资源，删除命令（卷/PVC/库）逐条二次确认
→ 最后引导验证无残留（无容器 / 无 namespace / 无监控进程）
```

## 反幻觉规则

1. 部署命令、镜像路径、端口、context-path 必须来自 `skeleton/docs/部署指南.md` 或 `references/*.md`，禁止凭记忆编造
2. 不确定某个配置项/命令时，先读部署指南对应章节确认，再给出
3. 任何引用必须可溯源到部署指南章节或 `deploy/` 资产文件

## 共享规范

> 本 Skill 的部署约束收录在 `constraints.yaml`（对齐 `_shared/constraints-common.yaml` 的通用约束风格）。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。
> 部署指南（知识底座）：`skeleton/docs/部署指南.md`，references 均为其提炼的动作清单。
