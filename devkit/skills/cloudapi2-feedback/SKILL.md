---
name: cloudapi2-feedback
description: |
  DJI FlightHub PaaS SDK 问题反馈助手。把用户的口语问题变成结构化 GitHub issue，走完「采集→脱敏→确认→提交」闭环。支持 4 类反馈：问题（Bug）、需求、咨询、其他。
  触发词：反馈、我要反馈、反馈问题、反馈一下、提个 issue、报个问题、提交问题、issue 提交、提交 GitHub、report a bug、file an issue、submit feedback、提个需求、有个建议、想咨询一下。
  不触发：有明确错误码的排障（走 troubleshooting）、新功能开发（走对应业务 Skill）、Jira issue 分析（走 issue-analyzer）。
version: 1.0.0
author: DJI CloudAPI2 Team
---

# DJI FlightHub PaaS SDK 问题反馈助手

## 这是什么

把用户的一句话变成结构化 GitHub issue。ISV 说"我要反馈一个问题"时往往**既没有错误码、也没复现步骤**，只有一句口语描述。本 Skill 驱动 `diag.sh session` 闭环，自动完成「采集→脱敏→确认→提交」，让用户零门槛提交高质量反馈。

脚本实现在 `cloudapi2-troubleshooting/scripts/diag.sh`（共享脚本，本 Skill 不单独维护脚本副本）。

## 何时触发

- 用户说"我要反馈一个问题"/"反馈一下"/"报个 bug"/"提个 issue"
- 用户要求提交 GitHub issue
- 用户说"提交问题"/"issue 提交"/"提交 GitHub"
- 用户说 "report a bug"/"file an issue"/"submit feedback"
- 用户有问题但**没有明确错误码**，只有口语描述
- 用户要提**需求建议**或**咨询**（"我想提个需求"/"有个建议"/"想咨询一下"）

**不触发**：
- 有明确错误码的排障（走 `cloudapi2-troubleshooting`，它有错误码词典和 4 阶段 RCA）
- 新功能开发（走对应业务 Skill：sdk-core / device-management / frontend-component 等）
- Jira issue 分析（走 issue-analyzer）
- 用户已经在 troubleshooting 排障流程中，末端产生的 issue 提交由 troubleshooting 内部 session 处理

## 硬约束（不可违反）

1. **人工确认 Gate**：`session issue` 前必须 `session confirm`；未经用户在 `preview` 复核确认，脱敏内容**不得出域**（不得创建 issue、不得生成 URL）。
2. **数据本地脱敏后才出域**：务必先 `session preview`（内含脱敏）再展示给用户，用户看到的必须是脱敏后内容。
3. **采集只读、失败不阻断**：采集项失败继续流程，不要因某项采集失败中止反馈。
4. **对客户机器只读**：写盘仅限 `$DIAG_DATA_DIR`（`~/.dji_cloud/diag/`）。

## 反馈类型

本 Skill 支持 4 类反馈，AI 开场应先确认类型：

| 类型 | 说明 | 日志采集深度 |
|------|------|-------------|
| **问题**（默认） | ISV 遇到 bug、异常、报错 | 完整采集（环境 + 容器日志 + SDK 日志） |
| **需求** | 功能建议、改进意见 | 弱化采集（仅环境信息，跳过容器日志） |
| **咨询** | 使用方法、能力确认 | 弱化采集（仅环境信息） |
| **其他** | 不属于以上分类 | 按需采集 |

**需求/咨询/其他类**不需要深度日志采集，AI 应跳过远程采集和 Loki 探测，聚焦于收集清晰的描述和联系方式即可。

## 子分类

问题类反馈应进一步分类，帮助研发团队快速分拣：

| 子分类 | 典型场景 |
|--------|---------|
| SDK 接口 | SDK 方法调用报错、回调异常、初始化失败 |
| 前端组件 | PaaS 组件加载失败、白屏、交互异常 |
| 部署 | 容器启动失败、配置错误、环境问题 |
| 其他 | 不属于以上分类 |

AI 应在收集问题描述后判断子分类，写入 `error_text` 开头（如 `[SDK接口] SDK 初始化偶尔失败`），方便研发从 issue 标题直接识别归属。

## 反馈闭环工作流

`session` 子命令驱动完整闭环。状态机强制每步不可跳过：

```
session start → set(描述+复现) → set(基本信息) → collect --no-zip(采集+判级+总览)
   → preview(脱敏+统计) → confirm(用户确认)
   → pack(补充材料+打包) → 上传网盘 → issue --label(创建+标签)
```

状态流：`created → collected → previewed → confirmed → issued`。`pack` 可在 `collected/previewed/confirmed` 任一状态执行。跳步会被拦截并提示当前状态与期望状态。

### 开场：检查未完成草稿

AI **每次进入反馈流程前**，先跑 `diag.sh session status` 检查是否有未完成的 session：

```
AI：让我先检查一下有没有未完成的反馈记录……
    > diag.sh session status

    （有未完成 session）发现一条未完成的反馈（ID: 20260922-143012，状态: collected）。
    要**继续**这条，还是**新建**一条？

    （无未完成 session）好的，我来新建一条反馈记录。
```

### 标准流程

```
diag.sh session start                        # 拿到 <id>
diag.sh session set <id> error_text="用户原话"
diag.sh session set <id> repro_steps="复现步骤"
diag.sh session set <id> company=<企业名称> severity=严重 contact=<邮箱>
diag.sh session set <id> log_since_minutes=10
diag.sh session collect --no-zip <id>        # 采集 + 判级 + 输出总览（不自动打 zip）
diag.sh session show <id>                    # （可选）用户审查采集内容
diag.sh session preview <id>                 # 脱敏预览 + 命中统计
diag.sh session confirm <id>                 # 用户确认无敏感信息
diag.sh session pack <id> [extra_files...]   # 补充材料 + 打包（含附件）
diag.sh session set <id> log_links="网盘URL" label=type/bug
diag.sh session issue --gh <id>              # 自动创建 GitHub issue（读取 meta 中 label）
```

**缺字段就留空，不要为了凑字段卡住流程** —— 判级函数不依赖 `error_text`，`session collect` 在元数据不全时照样产出证据包并判 L2/L3。追问可以穿插在 `set` 之后，但不能成为启动闭环的前置条件。

`company`（企业名称）与 `severity`（严重程度）是 issue 表单的必填项，`severity` 取值必须是表单下拉选项之一（`致命`/`严重`/`一般`/`提示`），写成 `P1`/`P2` 之类会被 `set` 直接拒绝 —— 值不在选项内，用户到表单里根本填不上。`session issue` 会在打印时提示缺失的必填项。

## AI 对话流程引导

AI 在驱动 session 闭环时，**必须主动引导以下环节**，不能跳过。每一步完成后**始终回显当前 session ID 和所处步骤**，方便用户中断后续填。

### 0. 确认反馈类型

```
AI：收到。先确认一下，这次反馈属于哪种类型？
    1. 问题（Bug、异常、报错）← 默认
    2. 需求（功能建议、改进意见）
    3. 咨询（使用方法、能力确认）
    4. 其他
```

用户未明确说时，从上下文推断——提到"报错/异常/失败"归**问题**，提到"希望/建议/能不能"归**需求**，提到"怎么用/支不支持"归**咨询**。推断后告知用户，不要默默假设。

### 1. 收集问题描述 + 复现步骤 + 截图提示

确认类型后，收集问题核心信息。复现步骤在此步收集（不要推迟到 issue 前），趁用户记忆最清晰：

```
AI：我理解这是一个 Bug 反馈（类型：问题）。请描述一下：

    1. **问题描述**：发生了什么？
    2. **复现步骤**：怎么操作能重现？（说不清可跳过）
    3. **截图**：如有报错截图、异常界面截图，请直接发送或告诉我文件路径
       （报错弹窗、浏览器控制台、网络请求截图等都有用）
```

收集到的信息立即写入 session：

```
diag.sh session set <id> error_text="[SDK接口] 用户描述"
diag.sh session set <id> repro_steps="1. 执行XX操作 2. 期望看到YY 3. 实际看到ZZ"
```

**截图处理**：用户发送的截图文件路径记录下来，后续 Step 6 打包时作为 `extra_files` 传入 `session pack`。

用户答不上来的信息直接跳过，不要反复追问卡住流程。

### 2. 收集基本信息

问题描述收集完后，**单独询问**基本信息：

```
AI：再补充几个基本信息（能填就填）：

    - **企业名称**
    - **联系方式**（邮箱）
    - **严重程度**：致命 / 严重 / 一般 / 提示
```

```
diag.sh session set <id> company="企业名" severity=严重 contact=<邮箱>
```

`company` 与 `severity` 是 issue 表单必填项，但**不要在此卡住流程**——用户不填就先空着，后续 `session issue` 会提示缺失。

### 3. 确认采集目标 + 选择日志时间范围

> 需求/咨询/其他类可跳过此步（不需要深度日志采集）。

```
AI：问题出在本机环境，还是部署在其他服务器上？
    - 本机 → 直接采集
    - 远程 → 请提供 SSH 地址（如 diag@192.0.2.10）和容器名
```

如果用户提到 IP、服务器、Docker 容器等关键词，直接按远程流程走，不必再问。设置方式：

```
diag.sh session set <id> remote_host="diag@192.0.2.10" docker_container="skeleton-backend"
```

然后**选择日志时间范围**：

```
AI：日志需要采集多长时间范围的？
    1. 当前（最近几分钟）
    2. 最近 10 分钟
    3. 最近 1 小时 ← 默认
    4. 指定时间范围（最大 24 小时）
```

选择后写入 session：

```
diag.sh session set <id> log_since_minutes=10
```

不设置则默认 60 分钟。`session collect` 会自动读取 meta 中的 `log_since_minutes` 值。

**`docker_container` 很重要**：私有化部署的微服务（skeleton-server、pf-notify-service 等）以容器形式运行，SDK 日志在容器内部（如 `/app/flight_hub_sdk_*.log`），宿主机路径找不到。不指定容器名 → SDK 日志采集缺失 → 判级 L3。指定后脚本会自动进入容器内搜索 SDK 日志文件并取末 200 行。

如果用户不确定容器名，AI 应主动通过 SSH 执行 `docker ps | grep -iE 'skeleton|pf-'` 帮助确认。

**确认采集目标和时间范围后，AI 必须立即主动执行 `session collect --no-zip`，不要等待用户下达"打包"或"采集"指令。** 整个 session 闭环由 AI 一气呵成推进，用户只需在关键节点做人工决策。

### 4. 日志采集 + 采集总览

执行采集（使用 `--no-zip` 不自动压缩，压缩推迟到 Step 6）：

```
diag.sh session collect --no-zip <id>
```

采集完成后，脚本自动输出结构化采集总览（含各服务日志文件名、大小、角色映射）。AI 将 session 目录路径展示给用户：

```
AI：采集完成，判级 L2。以下是采集总览：
    [脚本输出的总览内容]

    日志目录：~/.dji_cloud/diag/sessions/20260922-143012/
    您可以打开此目录查看原始采集内容。继续脱敏预览？
```

采集完成后，AI 应**基于报错内容智能追问**关键信息（一次问清一组相关信息，避免冗长）：

- 报错含 NPE/NullPointer → 追问"哪个接口调用时触发的？传了什么参数？"
- 报错含 timeout/连接失败 → 追问"目标地址是什么？之前能正常连接吗？"
- 报错含 401/鉴权 → 追问"token 是何时获取的？有没有过期？"
- 报错含设备相关 → 追问"设备型号？固件版本？"
- 无明确报错 → 追问"问题发生的大致时间？发生前做了什么操作？"

追问穿插在 `set` 之后，但**不能成为推进流程的前置条件**——用户答不上来就跳过。

用户要求查看详细采集内容时，使用 `session show <id>` 获取并展示。

### 5. 脱敏预览 + 确认

```
diag.sh session preview <id>                 # 脱敏 + 11 类命中统计
diag.sh session confirm <id>                 # 用户确认无敏感信息
```

脱敏前展示 evidence.txt，脱敏后展示 sanitized.md。

### 6. 补充材料 + 打包

confirm 之后，**主动询问是否有补充材料**，然后打包：

```
AI：准备打包日志。还有需要补充的材料吗？
    - 更多截图 / 配置文件 / HAR 文件 / 网络抓包等
    - 如果没有，我将把日志和已有截图一起打包
```

用户无补充 → 直接打包；用户有补充 → 等用户提供后再打包。

```
diag.sh session pack <id> [/path/to/screenshot.png /path/to/extra.har ...]
```

`session pack` 会将 extra files 复制到 `sessions/<id>/attachments/`，然后将 `logs/` + `evidence.txt` + `attachments/` 一起打 zip。打包完成后输出 zip 路径和大小。

### 7. 上传网盘

打包完成后，提示用户上传：

```
AI：日志包已就绪：sessions/<id>/session-<id>-logs.zip (12MB)
    需要上传到网盘吗？
    - 请提供上传地址（支持 scp://、WebDAV 等）
    - 也可以手动上传，日志包路径如上

    上传完成后请告诉我下载链接，我会写入 issue。
```

用户提供上传地址后，AI 执行上传（scp / rclone / curl，根据 URL scheme 判断）。上传完成后记录链接：

```
diag.sh session set <id> log_links="https://example.com/path/to/logs.zip"
```

用户选择手动上传也不阻塞——可以先创建 issue，后续补充链接。

### 8. 创建 GitHub Issue（含 label）

所有准备就绪后，根据反馈类型自动设置 label 并创建 issue：

| 反馈类型 | label |
|---------|-------|
| 问题 | `type/bug` |
| 需求 | `type/enhancement` |
| 咨询 | `type/question` |
| 其他 | `type/other` |

```
diag.sh session set <id> label=type/bug
diag.sh session issue --gh <id>
```

`session issue` 自动读取 meta 中的 `label` 字段，传递给 `gh issue create --label`。issue body 中包含已脱敏元数据和上传链接（如有）。

```
AI：Issue 已自动创建：<issue_url>
    标签：type/bug
    日志下载链接已包含在 issue 描述中。
```

**离线降级**：如果 `gh` CLI 未安装、GitHub 不可达、或创建失败：

```
AI：GitHub 提交未成功。已生成离线问题包：
    > sessions/<id>/session-<id>-logs.zip

    请将此 zip 拷到可访问 GitHub 的机器：
    1. 打开 dji-sdk/Cloud-API-2 的 Issues 页面
    2. 新建 issue，标题和正文从 session 元数据复制（我已打印在上方）
    3. 将 zip 包和截图作为附件拖入 issue
    4. 保留 session ID「<id>」以便后续匹配回流
```

这 9 步（0-8）是引导性的，用户可以跳过任何一步，不构成阻塞。

### 敏感行业提醒

如果用户提到属于**敏感行业**（电力、测绘、公安、军工、国防、政务），AI 应在 preview 步骤**额外强调**：

```
AI：注意：您所在行业对数据安全要求较高。
    请仔细核对脱敏预览中是否还有需要遮蔽的内容（如内部项目名、坐标、设备编号等）。
    如需额外遮蔽，告诉我具体内容，我来帮您处理。
```

## 远程采集

### 按服务存日志

远程采集时，脚本会自动发现远程机器上所有运行中的 Docker 容器，并为每个容器的日志存为独立文件：

```
sessions/<id>/
  evidence.txt               # 环境摘要（env/disk/network/process/容器列表）
  logs/
    pf-devkit-skeleton-server.txt   # skeleton-server 容器日志（主后端/SDK）
    pf-notify-service.txt           # 通知服务容器日志
    sdk-log.txt                     # SDK 日志（从主容器内提取）
  attachments/                 # 用户补充材料（截图、HAR、配置文件等）
  session-<id>-logs.zip      # pack 后生成（含 logs/ + evidence.txt + attachments/）
```

每个服务的日志独立可查，方便研发按服务定位问题。zip 包用于上传网盘。

### Loki 自动探测

远程采集时，脚本会自动探测远程主机上是否运行 Loki 日志服务（尝试端口 3100、30828 的 `/ready` 接口）：

- **有 Loki** → 通过 Loki API（`/loki/api/v1/query_range`）按 `container_name` 查询日志，比逐容器 SSH `docker logs` 快得多
- **无 Loki** → 静默回退到原有 `docker logs` 方式，行为不变

Loki 查询自动带上 session 的 `trace_id` 作为过滤条件（如果有），精准定位相关日志。

### 服务角色映射

容器列表和日志文件头部自动标注服务角色：

| 容器名模式 | 角色 |
|---|---|
| `*skeleton-server*` | 主后端/SDK |
| `*notify-service*` | 通知服务 |
| `*gateway-service*` | API 网关 |
| `*media-*` | 媒体服务 |
| `*live-*` | 直播服务 |
| `*wayline-*` | 航线服务 |
| `*manage-service*` | 管理服务 |
| `*thing-access*` / `*tas-*` | 设备接入 |
| `*inventory-collector*` | 资产采集 |

> `*beacon-reporter*`（埋点上报）已从采集范围排除，不会出现在证据包中——该服务属于内部遥测基础设施，对 ISV 问题诊断无价值且暴露内部容器信息。

## 增强特性

1. **脱敏扩展**：新增 IPv6、MAC 地址、IMEI（15位数字）、JWT 三段式、PEM 私钥/证书块、GPS 经纬度降精度（保留小数点后 2 位）、设备 SN 码（key=value 和 JSON 两种格式）。命中统计从 6 类扩展到 11 类
2. **session 限频**：同一机器 10 分钟内不可重复创建 session（`DIAG_SESSION_MIN_INTERVAL` 环境变量可调）
3. **内容指纹去重**：`session collect` 后对 evidence.txt 做 MD5 哈希，与已有 session 比对提示重复
4. **per-ISV 提单配额**：默认每企业上限 20 个 issued session（`DIAG_MAX_ISSUES_PER_ISV` 可调），超限警告不阻断
5. **相似单检测**：`session issue` 前比对 error_text 前 30 字符，提示已有相似已提交单
6. **包大小上限**：zip 打包后检查大小，200MB 警告 / 500MB 自动裁剪最大日志文件（`DIAG_MAX_PACKAGE_SIZE_MB` 可调）
7. **TTL 清理**：`diag.sh cleanup [--days N]` 清理过期 session，`session start` 时自动静默清理 >30 天的（`DIAG_SESSION_TTL_DAYS` 可调）
8. **容器上限**：远程采集 `MAX_CONTAINERS` 默认 10，超出按名称排序截取（skeleton-server 优先），`DIAG_MAX_CONTAINERS` 可调
9. **容器排除**：`*beacon-reporter*`（埋点上报）已加入排除名单，不采集其日志、不出现在证据包中
10. **issue 自动创建**：`session issue --gh <id>` 通过 `gh` CLI 直接创建 GitHub issue，AI 流程默认使用此模式
11. **上传地址脱敏**：大日志提示中的网盘上传地址仅显示域名，路径部分自动掩码
12. **采集与打包分离**：`session collect --no-zip` 只采集不打包，`session pack` 独立打包并支持追加附件文件（截图、HAR 等）
13. **日志时间范围**：`log_since_minutes` 元数据字段控制 docker logs `--since` 和 Loki 查询时间范围（默认 60 分钟）
14. **采集总览**：`session collect` 完成后输出结构化总览（各服务日志文件名、大小、角色、路径）
15. **issue label**：`session issue --label <label>` 或通过 meta `label` 字段，支持 type/bug、type/enhancement、type/question、type/other 等标签

## 命令参考

脚本位于 `cloudapi2-troubleshooting/scripts/diag.sh`（与 troubleshooting 共享）。

| 命令 | 作用 |
|---|---|
| `diag.sh session start` | 新建结构化反馈 session（返回 session ID）|
| `diag.sh session set <id> key=value` | 设置元数据（company/error_text/trace_id/severity/contact/sdk_version/repro_steps/log_since_minutes/label 等）；severity 须为表单选项 致命/严重/一般/提示 |
| `diag.sh session collect [--no-zip] <id>` | 采集证据 + 判级 + 输出采集总览（本地或远程；远程时自动探测 Loki；按服务存为独立日志文件；`--no-zip` 跳过自动打包；不带 flag 则向后兼容自动打 zip；含内容指纹去重）→ collected |
| `diag.sh session pack <id> [extra_files...]` | 打包日志（将 extra files 复制到 attachments/，然后 logs/ + evidence.txt + attachments/ 打 zip；含包大小检查和裁剪）。可在 collected/previewed/confirmed 状态执行 |
| `diag.sh session preview <id>` | 脱敏预览 + 11 类命中统计 → previewed |
| `diag.sh session confirm <id>` | 用户确认无敏感信息 → confirmed |
| `diag.sh session issue [--gh] [--label <label>] <id>` | 从 meta 构造 issue URL → issued；`--gh` 通过 gh CLI 直接创建（AI 默认用此模式）；`--label` 设置 issue 标签（也可通过 meta 的 `label` 字段设置）|
| `diag.sh session show <id>` | 查看采集内容（脱敏后优先展示 sanitized.md，否则 evidence.txt） |
| `diag.sh session status [id]` | 查看单个或全部 session 状态 |
| `diag.sh collect` | 固定只读命令集采集：env/disk/network/process/sdk_log |
| `diag.sh sanitize <f\|->` | 对文件/标准输入做一次脱敏 |
| `diag.sh feedback <solved\|unsolved\|skip> [报告路径]` | 追加到本地 `feedback.tsv`（状态恒 `needs_manual_upload`，不自动出网） |
| `diag.sh cleanup [--days N]` | 清理过期 session（默认 30 天）|

## 数据与出网

- **默认不出网**：collect 只读采集、sanitize 脱敏，全部本地完成。
- **出网控制点 = 用户决策**：`session issue` 生成的 issue body 仅含已脱敏元数据（判级/错误码/时间/来源/os 行/报告文件名），**显式排除 hostname 与日志正文**。`--gh` 模式通过 gh CLI 直接创建，URL 模式由用户手动打开链接粘贴。
- 仓库地址真值源在 diag.sh 常量 `DEFAULT_ISSUE_REPO`；`DIAG_ISSUE_REPO` 环境变量可覆盖。
- `feedback` 恒为 `needs_manual_upload`，**不自动上报、不回流大疆**。
- 对客户机器只读；数据目录 `~/.dji_cloud/diag/`（sessions/ + reports/ + feedback.tsv）是唯一写盘点。

## 不变量

> 硬约束（见上方）同样属于不变量，此处不重复列出。

1. **采集命令集固定**，LLM/对话不可改写；用户要求跑任意新命令 → 拒绝。
2. **判级以证据包 token 为输入**，不依赖自由文本；但允许 AI 介入（本机 AI 在旁）。
3. **敏感数据默认不出网；进入出网通道（issue URL / gh CLI）的内容仅限已脱敏元数据，完整出网仅由用户主动决策**。

## 风格

- 一次问清一组相关信息，避免冗长来回（如"企业名称、联系方式、严重程度"一起问）。
- 始终回显当前 `session ID` 与所处步骤，方便用户中断后续填。
- 敏感行业（电力/测绘/公安/军工/政务）默认强调脱敏与人工确认。
- 推断用户意图后**告知用户**（"我理解这是一个 Bug 反馈"），不要默默假设。
- 用户答不上来的问题直接跳过，不要反复追问卡住流程。

## 与 troubleshooting 的关系

| 维度 | feedback（本 Skill） | troubleshooting |
|---|---|---|
| 入口 | 用户说"反馈/提 issue/报 bug"，**无需错误码** | 用户报错/问错误码/说 bug |
| 核心 | session 闭环（采集→脱敏→确认→提交 issue） | 4 阶段 RCA（Reproduce→Locate→Fix→Verify） |
| 脚本 | 共享 `cloudapi2-troubleshooting/scripts/diag.sh` | 同左 |
| 出口 | 结构化 GitHub issue | 修复代码 / 结论 / 升级（也可能走 session issue） |

**转交协议**：
- 如果反馈过程中发现用户有明确错误码 → 转交 `cloudapi2-troubleshooting`（它有错误码词典和 RCA 能力）
- troubleshooting 三振出局或 L2/L3 升级后可走 session 提交 issue，此时仍由 troubleshooting 内部处理

## 权威参考

- `cloudapi2-troubleshooting/scripts/diag.sh` — 采集/脱敏/session/issue 脚本真身
- `cloudapi2-troubleshooting/scripts/test_diag.sh` — 测试套件
- `skills/_shared/constraints-common.yaml` — 跨 Skill 共享约束
- `skills/_shared/skill-routing.md` — Skill 路由/仲裁
- `skills/_shared/output-format.md` — 统一输出结构

> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。
> 当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载。
