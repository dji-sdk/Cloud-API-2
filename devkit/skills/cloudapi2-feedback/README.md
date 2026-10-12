# cloudapi2-feedback — DJI FlightHub 问题反馈助手

> **版本**：v1.0.0 | **更新**：2026-09-22
> **适用对象**：ISV 集成工程师、售后支持、开发者

把用户的一句话变成结构化 GitHub issue。无需错误码即可启动反馈闭环：采集环境证据 → 脱敏 → 用户确认 → 自动创建 issue。支持 4 类反馈：问题（Bug）、需求、咨询、其他。

---

## 功能

| 功能 | 说明 |
|------|------|
| 结构化反馈 session | 状态机驱动的完整闭环：start → set → collect → preview → confirm → pack → issue |
| 9 步引导流程 | 确认类型 → 问题描述+复现+截图 → 基本信息 → 目标+时间范围 → 采集+总览 → 脱敏+确认 → 补充材料+打包 → 上传网盘 → 创建 issue |
| 4 类反馈 | 问题（完整采集）、需求（弱化采集）、咨询（仅环境）、其他（按需） |
| 子分类标签 | SDK 接口 / 前端组件 / 部署 / 其他，自动标注到 issue 标题 |
| 自动采集 | 本地/远程（SSH + Docker）环境证据，按服务存为独立日志文件 |
| 采集总览 | 采集完成后输出结构化总览（各服务日志文件名、大小、角色、路径） |
| 日志时间范围 | 支持选择采集范围（当前/10 分钟/1 小时/自定义，最大 24 小时） |
| 采集与打包分离 | `collect --no-zip` 只采集不打包，`pack` 独立打包并支持追加附件（截图/HAR 等） |
| 脱敏预览 | 11 类敏感信息自动脱敏（凭证/IP/邮箱/手机/SN/GPS/JWT/PEM 等），命中统计 |
| Loki 自动探测 | 远程采集时自动探测 Loki 日志服务，有则走 API 查询（更快更精准） |
| issue 自动创建 | `--gh` 模式通过 gh CLI 直接创建 GitHub issue，支持 `--label` 自动标签 |
| 草稿续填 | 未完成的 session 自动持久化，下次进入可续填 |
| 离线降级 | GitHub 不可达时生成离线 zip 包 + 手动上传指引 |
| 敏感行业提醒 | 电力/测绘/公安等行业额外强调脱敏确认 |
| 隐私安全 | 默认不出网，数据仅写入本地 `~/.dji_cloud/diag/`，出网由用户决策 |

---

## 前置条件

与 `cloudapi2-troubleshooting` 相同，详见其 README。本 Skill 共享同一个 `diag.sh` 脚本。

### 必需工具

| 工具 | 用途 | 检查命令 |
|------|------|---------|
| `bash` | 脚本运行环境 | `bash --version` |
| `grep` / `sed` / `awk` | 日志分析、脱敏 | 系统自带 |
| `ssh` | 远程主机连接 | `ssh -V` |

### 推荐工具（可选）

| 工具 | 用途 | 缺失时的影响 |
|------|------|-------------|
| `gh` | GitHub CLI，`--gh` 参数直接创建 issue | 回退到生成 URL 模式 |
| `zip` | 远程日志打包 | 日志文件不打包 |
| `python3` | Loki JSON 解析 | Loki 结果返回原始 JSON |

---

## 安装

### 方式 1：Plugin 一键安装（推荐）

通过 `devkit@cloud-api2` Plugin 安装全套 Skill（见 `skills/README.md`），本 Skill 包含在内。

### 方式 2：手动安装

#### Claude Code

```bash
cp -r skills/cloudapi2-feedback ~/.claude/skills/cloudapi2-feedback
```

#### Cursor

```bash
cp -r skills/cloudapi2-feedback .cursor/skills/cloudapi2-feedback
```

#### GitHub Copilot

```bash
cp -r skills/cloudapi2-feedback .github/skills/cloudapi2-feedback
```

---

## 触发词

- `反馈` / `我要反馈` / `反馈问题` / `反馈一下`
- `提个 issue` / `报个问题` / `报个 bug`
- `提交问题` / `issue 提交` / `提交 GitHub`
- `report a bug` / `file an issue` / `submit feedback`
- `提个需求` / `有个建议` / `想咨询一下`

---

## 使用示例

### 场景：最简反馈（AI 全自动驱动）

```
用户：我要反馈一个问题，SDK 初始化偶尔失败

AI：收到，我理解这是一个 Bug 反馈（类型：问题）。请描述一下：
    1. 问题描述：SDK 初始化偶尔失败（已记录）
    2. 复现步骤：怎么操作能重现？
    3. 截图：如有报错截图请直接发送

用户：就是启动的时候偶尔报错，没有截图

AI：好的。再补充几个基本信息（能填就填）：
    - 企业名称 / 联系方式 / 严重程度

用户：示例科技，严重，在 192.0.2.10 上部署的

AI：好的。日志需要采集多长时间范围的？
    1. 当前（最近几分钟）  2. 最近 10 分钟
    3. 最近 1 小时（默认）  4. 指定时间范围

用户：最近 10 分钟

AI：> diag.sh session start
    > diag.sh session set <id> error_text="[SDK接口] SDK 初始化偶尔失败"
    > diag.sh session set <id> repro_steps="启动时偶尔报错"
    > diag.sh session set <id> company=示例科技 severity=严重 remote_host="diag@192.0.2.10" log_since_minutes=10
    > diag.sh session collect --no-zip <id>

    采集完成，判级 L2。采集总览：
    [结构化总览输出]
    日志目录：~/.dji_cloud/diag/sessions/<id>/
    继续脱敏预览？

用户：继续

AI：> diag.sh session preview <id>
    脱敏预览完成，命中 3 处内网 IP、1 处设备 SN。确认无敏感信息？

用户：确认

AI：> diag.sh session confirm <id>
    准备打包日志。还有需要补充的材料吗？

用户：没有了

AI：> diag.sh session pack <id>
    日志包已就绪：sessions/<id>/session-<id>-logs.zip (5MB)
    需要上传到网盘吗？

用户：手动上传吧

AI：> diag.sh session set <id> label=type/bug
    > diag.sh session issue --gh <id>
    Issue 已自动创建：https://github.com/dji-sdk/Cloud-API-2/issues/42
    标签：type/bug
    上传日志包后，请将下载链接填入 issue 的「日志下载链接」字段。
```

---

## 目录结构

```
skills/cloudapi2-feedback/
├── SKILL.md                       ← AI 入口（触发条件 + 工作流 + 引导逻辑）
└── README.md                      ← 本文件（人类可读文档）
```

脚本位于 `cloudapi2-troubleshooting/scripts/diag.sh`（共享，不重复维护）。

数据目录（运行时产生）：
```
~/.dji_cloud/diag/
├── sessions/                      ← session 数据（meta + evidence + sanitized + logs + attachments + zip）
├── reports/                       ← 诊断报告
└── feedback.tsv                   ← 本地反馈注册表
```

---

## 与其它 Skill 的关系

| Skill | 关系 |
|-------|------|
| `cloudapi2-troubleshooting` | **共享 diag.sh 脚本**。feedback 面向"没有错误码但想反馈"；troubleshooting 面向"有错误码要排查"。反馈过程中发现错误码 → 转交 troubleshooting |
| `cloudapi2-diagnosis` | 已弃用，能力并入 troubleshooting |
| 其它业务 Skill | 不直接关联。任何 Skill 排障过程中需要反馈 → 路由到本 Skill |

---

## 已知限制

- 反馈只在本地留存（`feedback.tsv`），不自动回流大疆
- 回传大疆的预期渠道是 GitHub 开源仓库（issue 人工跟进）
- `session start` 有 10 分钟限频（`DIAG_SESSION_MIN_INTERVAL` 可调）
- 每企业默认上限 20 个 issued session（`DIAG_MAX_ISSUES_PER_ISV` 可调）
