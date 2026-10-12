# cloudapi2-skeleton-deploy — FH Skeleton 部署助手

帮 ISV 开发者将 FH Skeleton 部署到自有环境，**半自动引导式**：AI 探测环境、推荐形态、生成密钥与配置、给出精确命令，用户执行系统级操作并回报，AI 校验继续、遇错排障。

## 能力

| 能力 | 说明 |
|------|------|
| **环境探测** | 检测 OS/架构/JDK/Maven/Node/Docker/kubectl/MySQL，判断前置条件 |
| **形态选型** | 按规模 + 基础设施推荐普通机器 / Docker / K8s |
| **密钥生成** | JWT / DB 密码 / MCP token 强随机生成 |
| **配置拼装** | 三形态 env / Secret / ConfigMap 完整值 |
| **部署引导** | 分步给命令、等回报、校验继续 |
| **部署排障** | 按现象定位（JWT/SDK/.so/健康检查/SSE 等） |
| **清理部署** | 三形态卸载：先确认保留/清除数据，逐步停服务、移除资源、按需删卷/PVC/库 |

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装（见 [skills/README.md](../README.md#安装方式)）。

单独安装：

```bash
# Claude Code
cp -r skills/cloudapi2-skeleton-deploy ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-skeleton-deploy .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-skeleton-deploy .github/skills/
```

## 触发词

- 部署 skeleton / 帮我部署 / 部署到这台机器
- 普通机器部署 / docker 部署 / k8s 部署 / kubernetes 部署
- 清理 skeleton / 卸载部署 / 下线 skeleton / 移除部署 / 撤销部署
- skeleton 环境准备 / 镜像构建 / native lib / systemd / nginx / ingress

## 使用示例

```
你：帮我把 skeleton 部署到这台服务器

AI：（自动加载 Skill）
→ 先给环境探测命令，你执行后回报输出
→ 根据结果推荐部署形态（如：检测到 Docker 24+，推荐 Docker Compose）
→ 生成 JWT/DB 密钥，拼接完整 .env 配置
→ 逐步骤给构建/启动命令，等你回报结果
→ 验证健康检查，确认部署完成
```

## 交互边界（半自动）

| 操作 | AI | 用户 |
|---|---|---|
| 探测/生成/拼装/解析/排障 | ✅ | — |
| sudo/docker/kubectl/systemctl/mysql | 给精确命令 | 执行 + 回报 |
| 危险操作（删数据/重建卷/覆盖配置） | 先确认 | 确认后执行 |
| 清理部署（down -v / delete namespace / 删库） | 先确认保留/清除 | 确认后逐条执行 |

## 目录结构

```
cloudapi2-skeleton-deploy/
├── SKILL.md               ← AI 主入口（决策树 + 半自动协议 + 排障路由）
├── constraints.yaml       ← 部署硬约束
├── prompts/
│   ├── env-probe.sh       — 环境探测脚本
│   └── gen-secrets.sh     — 密钥生成脚本
├── references/
│   ├── 三形态对比与选型.md  — 选型决策 + 平台限制
│   ├── 普通机器部署步骤.md  — JDK+MySQL+nginx+systemd
│   ├── docker部署步骤.md    — 镜像构建 + compose
│   ├── k8s部署步骤.md       — manifest 应用 + 升级回滚
│   ├── 环境变量速查.md      — 必填/可选/前端变量
│   ├── 部署排障.md          — 现象→排查动作
│   └── 清理部署.md          — 三形态卸载（保留/清除数据）
└── snippets/
    ├── skeleton.env.tmpl  — systemd env 模板
    ├── k8s-checklist.yaml — manifest 核验清单
    └── 验证命令.md          — 三形态健康检查
```

## 知识底座

本 Skill 的 references 均为 `skeleton/docs/部署指南.md` 的提炼动作清单。深度查阅请打开部署指南。
