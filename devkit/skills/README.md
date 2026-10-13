# Skills 总览 — Cloud API 2.0 SDK AI 能力包

本目录包含面向 AI Agent 的 Skill（能力包），安装后 AI 在你编写 Cloud API 2.0 集成代码或操控设备时**自动加载**对应知识，确保生成正确、安全、符合规范的代码。

## Skill 列表

| Skill | 层级 | 方向 | 说明 |
|-------|------|------|------|
| [`cloudapi2-sdk-core`](cloudapi2-sdk-core/) | 水平基础 | 后端 Java SDK | 6 条核心规则 + 6 场景范本 + 代码模板，教 AI 写正确的 SDK 集成代码 |
| [`cloudapi2-frontend-component`](cloudapi2-frontend-component/) | 水平基础 | 前端组件 | FH2 PaaS 4 大前端组件（航线/驾驶舱/设备/回放）接入助手 |
| [`cloudapi2-mcp-guide`](cloudapi2-mcp-guide/) | 水平基础 | MCP 工具路由 | 教 AI 在运行时选对 MCP Tool、编排多步调用 |
| [`cloudapi2-device-management`](cloudapi2-device-management/) | 垂直场景 | 设备管理 | 设备管理模块扩展（OSD 订阅/HMS 告警/feature 模块创建） |
| [`cloudapi2-sei-osd`](cloudapi2-sei-osd/) | 垂直场景 | SEI/OSD 遥测 | 帧级位置数据(SEI) + 设备遥测(OSD) 接入，含前端 Cesium 可视化 |
| [`cloudapi2-skeleton-deploy`](cloudapi2-skeleton-deploy/) | 垂直场景 | 部署 | FH Skeleton 三形态部署引导（普通机器/Docker/K8s），半自动 + 排障 |
| [`cloudapi2-mcp-deploy`](cloudapi2-mcp-deploy/) | 垂直场景 | 部署 | FlightHub MCP Server Docker 部署引导（SSE 远程/stdio 本地），半自动 + 排障 |
| [`cloudapi2-troubleshooting`](cloudapi2-troubleshooting/) | 横切 | 本地诊断 + 排障 | 本地采集/脱敏/静态判级（L1/L2/L3）+ 4 阶段 RCA + 错误码词典，单一入口一条流水线；`v2.0.0` 起并入原 `cloudapi2-diagnosis` |
| [`cloudapi2-feedback`](cloudapi2-feedback/) | 横切 | 问题反馈 | 问题反馈闭环（session 采集→脱敏→确认→issue 自动创建），无需错误码即可启动；共享 troubleshooting 的 `diag.sh` 脚本 |
| [`cloudapi2-diagnosis`](cloudapi2-diagnosis/) | 横切 | 本地诊断（**已弃用**） | ⚠️ 已并入 `cloudapi2-troubleshooting` v2.0.0——固定命令集采集 + 正则脱敏 + 静态判级（L0-L3）本地闭环；目录保留作历史与设计走查，新实现请走 troubleshooting |
| [`_shared`](_shared/) | — | 共享模板 | SOP 模板 + 共享约束 SSOT + Skill 路由规则 + 输出格式标准（其他 Skill 引用） |

> **层级说明**：水平基础层教通用规则（被垂直层引用）；垂直场景层教具体业务接入（引用水平层）；横切层不受场景限制。

---

## 推荐搭配：superpowers

devkit 提供 **DJI 领域知识**（SDK 规则、错误码、代码模板），但不包含通用的设计思考方法论。强烈推荐搭配 [superpowers](https://github.com/anthropics/superpowers) 使用：

```bash
# 安装 superpowers（通用方法论：brainstorming / debugging / TDD）
claude plugin install superpowers
```

**为什么需要两个？**

| | superpowers | devkit |
|---|---|---|
| 解决 | "怎么思考和设计" | "怎么用 DJI SDK 实现" |
| 举例 | "从零设计一个设备监控架构" → 先发散再收敛 | "在 skeleton 中加 OSD 订阅" → 直接给代码 |
| 类比 | 通用思维框架 | 领域专家手册 |

两者协作时 AI 会自动分工：superpowers 管方法论流程，devkit 管领域约束和代码模板。未安装 superpowers 时 devkit 仍可独立工作（使用内置 SOP 流程），但面对开放式设计问题时体验会降级。

---

## 安装方式

### 方式 1：Plugin 一键安装（推荐）

本套 Skill 已打包为 `cloudapi2@dji` Plugin，一次安装即可获得全部 11 个 Skill。

#### 前置条件

- 已安装 [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI（版本 ≥ 1.0）
- 已有 Plugin 仓库（公开 Git 仓库地址）的读取权限
- Git 已配置可访问该仓库的凭证（HTTPS 或 SSH）

#### 第 1 步：添加 Marketplace

```bash
claude plugin marketplace add dji-sdk/Cloud-API-2
```

> 此命令将 `dji` marketplace 注册到你的 Claude Code 配置中。只需执行一次。

#### 第 2 步：安装 Plugin

```bash
claude plugin install cloudapi2@dji
```

> 此命令启用 `cloudapi2` plugin（包含全部 11 个 Skill）。安装完成后新开会话即生效。

#### 第 3 步：验证安装

新开一个 Claude Code 会话，输入：

```
/plugins
```

应看到输出中包含 `cloudapi2@dji (enabled)`。

然后试一条触发语句：

```
帮我初始化 FlightHub SDK
```

如果 AI 回复中提到了 "6 条核心规则"、"initialize 必须先完成" 等内容，说明 Skill 已成功加载。

#### 更新 Plugin

当仓库有新版本时，只需 `git pull` 本地仓库（或 Claude Code 自动同步），新会话即加载最新版本。无需重新执行安装命令。

---

### 方式 2：手动配置 settings.json

如果 `claude plugin` 命令不可用（旧版 Claude Code），可手动编辑配置文件。

#### 第 1 步：Clone 仓库

```bash
git clone https://github.com/dji-sdk/Cloud-API-2.git
# 记住 clone 后的绝对路径，例如：/Users/yourname/projects/Cloud-API-2
```

#### 第 2 步：编辑 settings.json

打开 `~/.claude/settings.json`（不存在则新建），添加以下内容：

```jsonc
{
  // ... 你的其他配置 ...

  "enabledPlugins": {
    "cloudapi2@dji": true
  },
  "extraKnownMarketplaces": {
    "dji": {
      "source": {
        "source": "directory",
        "path": "/Users/yourname/projects/Cloud-API-2"  // ← 替换为实际路径
      }
    }
  }
}
```

> **配置文件位置**：
> - macOS / Linux：`~/.claude/settings.json`
> - Windows：`%USERPROFILE%\.claude\settings.json`
>
> 如果文件已存在，将 `enabledPlugins` 和 `extraKnownMarketplaces` 的内容**合并**进去（不要覆盖已有配置）。

#### 第 3 步：重启 Claude Code 验证

```bash
claude   # 新开会话
/plugins # 确认 cloudapi2@dji 已启用
```

---

### 方式 3：手动拷贝（不想配置 Plugin 时使用）

```bash
cd Cloud-API-2/devkit

# Claude Code — 全部安装
for d in skills/cloudapi2-*/; do cp -r "$d" ~/.claude/skills/; done

# Claude Code — 只装一个
cp -r skills/cloudapi2-sdk-core ~/.claude/skills/

# Cursor
for d in skills/cloudapi2-*/; do cp -r "$d" .cursor/skills/; done

# GitHub Copilot (VS Code)
for d in skills/cloudapi2-*/; do cp -r "$d" .github/skills/; done
```

> **注意**：`_shared/` 目录是被其他 Skill 内部引用的 SOP 模板，不是独立 Skill，无需手动安装（Plugin 模式会自动包含）。
>
> **缺点**：手动拷贝后，仓库更新时需重新拷贝。Plugin 模式自动保持同步。

### 方式 4：保持在项目内（零配置）

如果你的日常开发就在本仓库内工作，Claude Code 会自动发现 `skills/` 下的 Skill，无需任何安装操作。

---

## 验证安装

无论使用哪种安装方式，都可以通过以下测试确认 Skill 已生效：

| 测试输入 | 期望触发的 Skill | 验证标志 |
|---------|---------|---------|
| "帮我初始化 FlightHub SDK" | `cloudapi2-sdk-core` | 提到 initialize 返回值检查 |
| "让无人机飞到这个坐标" | `cloudapi2-mcp-guide` | 提到 fh_flyto / fh_control_acquire |
| "接入航线编辑器组件" | `cloudapi2-frontend-component` | 提到 WaylineEditor / paas.js |
| "添加 OSD 订阅功能" | `cloudapi2-device-management` | 提到 @EventListener / OsdUpdateEvent |
| "怎么获取帧级位置数据" | `cloudapi2-sei-osd` | 提到 SEI / subscribeArSeiInfo |
| "帮我把 MCP Server 部署到服务器" | `cloudapi2-mcp-deploy` | 提到环境探测 / SSE 部署 / FH_SSE_AUTH_TOKEN |
| "SDK 返回 error_code: 4001" | `cloudapi2-troubleshooting` | 提到 4 阶段 RCA |

### 常见问题

**Q: `claude plugin marketplace add` 报错 "command not found"？**

你的 Claude Code 版本可能较旧。请升级到 ≥ 1.0 版本，或使用方式 2（手动编辑 settings.json）。

**Q: Plugin 安装后没有触发？**

1. 确认 `claude plugin list` 输出中 `cloudapi2@dji` 状态为 enabled
2. 如果用方式 2：确认 `path` 是绝对路径，指向仓库根目录（包含 `devkit/` 子目录的那一级）
3. 确认 JSON 格式正确 — 运行 `cat ~/.claude/settings.json | python3 -m json.tool` 验证
4. 重启 Claude Code（新开终端窗口）

**Q: 更新 Skill 后需要重新安装吗？**

- Plugin 模式（方式 1/2）：`git pull` 本仓库即可，新会话自动加载最新版
- 手动拷贝（方式 3）：需重新执行 `cp -r` 命令
- 项目内（方式 4）：`git pull` 即可

**Q: 可以只安装部分 Skill 吗？**

Plugin 模式安装全部 11 个 Skill（推荐）。如果只需要部分 Skill，使用方式 3 手动拷贝指定的 Skill 目录。

---

## Skill vs MCP Server

| 维度 | Skill（本目录） | MCP Server（`mcp/` 目录） |
|------|---------------|--------------------------|
| 作用时机 | AI **写代码**时 | AI **运行时调用工具**时 |
| 能力 | 教 AI "怎么写对" | 让 AI 直接操控设备 |
| 安装方式 | Plugin 一键 / 复制 .md 文件 | 启动服务进程 |
| 运行依赖 | 无 | 需要 SDK 运行环境 |
| 输出物 | 源代码 / 诊断建议 | 设备控制指令执行结果 |

两者互补：Skill 教 AI 写正确的集成代码，MCP Server 让 AI 在开发/调试时直接调用 SDK。

## 各 Skill 详细说明

每个 Skill 目录下有独立的 `README.md`，包含：
- 功能说明
- 目录结构
- 使用示例
- 触发词列表

## 贡献指南

1. 新建 Skill 目录：`skills/<skill-name>/`
2. 编写 `SKILL.md`（必须包含 YAML frontmatter：name、description、version、author）
3. 按需添加 `references/`、`snippets/`、`commands/` 子目录
4. 编写 `README.md` 使用说明
5. 更新本文件的 Skill 列表
6. 提交 MR
