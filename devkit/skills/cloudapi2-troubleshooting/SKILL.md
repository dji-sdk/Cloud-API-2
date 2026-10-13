---
name: cloudapi2-troubleshooting
description: DJI FlightHub PaaS SDK 本地诊断 + 排障一体化。触发词：报错、错误码、error、errorCode、4001-7000、bug、修复、排查、debug、白屏、加载失败、timeout、连接失败、鉴权失败、本地诊断、现场采集、收集日志、diag.sh、report、收不到回调、listen 没反应、事件没推送、err_code、200612、实机验证、模拟器验证、偶现、时序问题、昨天还好好的、改了几次都没用、越修越乱、无法直播、直播失败、直播黑屏、打不开、设备离线、212015、EA340、EA610、DRTC、上云方式、上云 API2、底座 .env、私有化环境排查。
version: 2.6.0
author: DJI CloudAPI2 Team
---

# DJI FlightHub PaaS SDK 本地诊断 + 排障助手

## 这是什么

单一入口。用户报错/问错误码 → 先进**本地采集 + 静态判级**（L1/L2/L3） → L1 就地给结论与建议；L2/L3 带证据包进入 **4 阶段 RCA**。一条流水线，不设 online/offline。错误码词典唯一真值源 = `error-codes.yaml`。

`v2.0.0` 起替代原 `cloudapi2-diagnosis`：本地采集 / 脱敏 / 静态判级 / 报告 / 反馈由本 Skill 的 `scripts/diag.sh` 承载。

## 何时触发

> **先判域，再判词**：本 Skill 只处理 **DJI FlightHub PaaS SDK / ISV 集成侧**的问题（SDK 方法、错误码、`fh_*` 相关能力、ISV 自己的接入代码）。"报错/bug/排查/debug"是通用动词，不能只看动词命中就触发——如果问题主体是**调用方/集成方自己业务系统内部**的代码（不涉及本 SDK 的接口、错误码或行为），不在本 Skill 处理范围内。

- 用户报错 / 说 bug
- 用户提到错误码（4001 / 5000 / 6002 / 6 位设备码 / ERROR_* / NPE）
- 用户贴平台 HTTP 响应体（`{"code": ..., "message": ...}`，含 `err_code`）
- 用户说“白屏 / 加载失败 / 超时 / 连接断开”
- 用户说“`listenXxx` 收不到回调 / 事件没推送 / 监听没反应”
- 用户报**设备侧无法直播**（EA340/EA610 直播打不开 / 黑屏 / 设备离线 `212015` / 私有化环境上云方式与底座配置）
- 用户要求**本地收集证据 / 现场采集**（“本地帮我查一下”、“diag.sh”、“report”、“feedback”）
- 用户要求**反馈问题 / 提 issue**（”我要反馈一个问题”、”报个 bug”、”提个 issue”）——**优先走 `cloudapi2-feedback`**；本 Skill 仅在 RCA 过程中需要提交 issue 时使用内置 session 流程

**不触发**：新功能开发（用对应业务 Skill）、纯在线问题（无本地证据可采）、与 DJI SDK 无关的 bug（如调用方自己业务系统内部的问题，不涉及本 SDK 接口/错误码/行为）。

### 无错误码的冷启动入口

> **纯反馈场景建议使用 `cloudapi2-feedback` Skill**——它专门面向”没有错误码但想反馈”的用户，提供更聚焦的引导。以下流程保留给 troubleshooting 排障末端（三振出局/L2/L3 升级）需要提交 issue 的场景。

ISV 说”我要反馈一个问题”时往往**既没有错误码、也没复现步骤**，只有一句口语描述。不要因此把它当成需求开发打发走，也不要先追问到信息齐全再动 —— 直接走 `session` 闭环，`error_text` 字段本来就收自由文本：

```
diag.sh session start                        # 拿到 <id>
diag.sh session set <id> company=<企业名称> error_text="用户原话，原样记录" severity=严重 contact=<邮箱>
diag.sh session collect <id>                 # 采集 + 判级（无错误码 → L2/L3，正常）
diag.sh session show <id>                    # （可选）用户审查采集内容
diag.sh session preview <id>                 # 脱敏预览 + 命中统计
diag.sh session confirm <id>                 # 用户确认无敏感信息
diag.sh session set <id> repro_steps="..."   # （可选）补充复现步骤
diag.sh session issue --gh <id>              # 自动创建 GitHub issue
```

**缺字段就留空，不要为了凑字段卡住流程** —— 判级函数不依赖 `error_text`，`session collect` 在元数据不全时照样产出证据包并判 L2/L3。追问可以穿插在 `set` 之后，但不能成为启动闭环的前置条件。

`company`（企业名称）与 `severity`（严重程度）是 issue 表单的必填项，`severity` 取值必须是表单下拉选项之一（`致命`/`严重`/`一般`/`提示`），写成 `P1`/`P2` 之类会被 `set` 直接拒绝 —— 值不在选项内，用户到表单里根本填不上。`session issue` 会在打印时提示缺失的必填项。

### AI 对话流程引导（v2.4.5）

AI 在驱动 session 闭环时，**必须主动引导以下 4 个环节**，不能跳过：

#### 1. 采集前：确认采集目标（本机 / 远程）

```
AI：问题出在本机环境，还是部署在其他服务器上？
    - 本机 → 直接采集
    - 远程 → 请提供 SSH 地址（如 diag@192.0.2.10）和容器名
```

如果用户提到 IP、服务器、Docker 容器等关键词，直接按远程流程走，不必再问。设置方式：

```
diag.sh session set <id> remote_host="diag@192.0.2.10" docker_container="skeleton-backend"
```

**确认采集目标后，AI 必须立即主动执行 `session collect`，不要等待用户下达"打包"或"采集"指令。** 整个 session 闭环（start → set → collect → preview → confirm → issue）由 AI 一气呵成推进，每一步完成后主动进入下一步，用户只需在关键节点（如 confirm 确认脱敏、补充复现步骤）做人工决策。

**`docker_container` 很重要**：私有化部署的微服务（skeleton-server、pf-notify-service 等）以容器形式运行，SDK 日志在容器内部（如 `/app/flight_hub_sdk_*.log`），宿主机路径找不到。不指定容器名 → SDK 日志采集缺失 → 判级 L3。指定后脚本会自动进入容器内搜索 SDK 日志文件并取末 200 行。

如果用户不确定容器名，AI 应主动通过 SSH 执行 `docker ps | grep -iE 'skeleton|pf-'` 帮助确认。

#### 远程采集：按服务存日志（v2.4.5）

远程采集时，脚本会自动发现远程机器上所有运行中的 Docker 容器，并为每个容器的日志存为独立文件：

```
sessions/<id>/
  evidence.txt               # 环境摘要（env/disk/network/process/容器列表）
  logs/
    pf-devkit-skeleton-server.txt   # skeleton-server 容器日志（主后端/SDK）
    pf-notify-service.txt           # 通知服务容器日志
    pf-beacon-reporter.txt          # 埋点上报容器日志
    sdk-log.txt                     # SDK 日志（从主容器内提取）
  session-<id>-logs.zip      # 上述所有文件打包
```

这样每个服务的日志独立可查，方便研发按服务定位问题。zip 包用于上传网盘。

#### Loki 自动探测（v2.5.0）

远程采集时，脚本会自动探测远程主机上是否运行 Loki 日志服务（尝试端口 3100、30828 的 `/ready` 接口）：

- **有 Loki** → 通过 Loki API（`/loki/api/v1/query_range`）按 `container_name` 查询日志，比逐容器 SSH `docker logs` 快得多
- **无 Loki** → 静默回退到原有 `docker logs` 方式，行为不变

Loki 查询自动带上 session 的 `trace_id` 作为过滤条件（如果有），精准定位相关日志。

日志文件头部标注来源，便于判断采集质量：
```
# Container: pf-beacon-reporter — 埋点上报
# Source: Loki (http://192.0.2.10:30828)
# Query: container_name=~"pf-beacon-reporter" |~ "26f1b1f6"
# Time range: last 60m, limit 500
```

#### 服务角色映射（v2.5.0）

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

#### v2.6.0 增强

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

#### 2. 采集后：提供证据审查机会

采集完成后，**主动告知用户可以查看采集内容**：

```
AI：采集完成，判级 L2。如需查看原始采集内容，我可以展示。
    继续脱敏预览？
```

用户要求查看时，使用 `session show <id>` 获取内容并展示。脱敏前展示 evidence.txt，脱敏后展示 sanitized.md。

#### 3. issue 前：收集复现步骤

confirm 之后、issue 之前，**主动询问复现步骤**：

```
AI：提交 issue 前，能否补充一下复现步骤？
    比如：
    1. 执行了什么操作
    2. 期望看到什么结果
    3. 实际看到了什么
    
    如果暂时说不清可以跳过，issue 表单里也能补。
```

用户提供后，写入 `repro_steps` 字段：

```
diag.sh session set <id> repro_steps="用户提供的复现步骤"
```

用户跳过也不阻塞流程——`repro_steps` 为空时 issue 描述会保留占位提示。

#### 4. issue 后：自动创建 + 提醒截图归入日志包

confirm 之后，AI **直接执行** `diag.sh session issue --gh <id>` 自动创建 GitHub issue，不需要用户手动操作。如果 `gh` CLI 未安装或创建失败，回退到 URL 模式。

issue 创建后，**必须单独、醒目地提醒用户将截图放入日志包**。截图对研发定位问题极其重要：

```
AI：Issue 已自动创建：<issue_url>

请务必将问题截图（可以多张）放入日志包附件中！
比如：报错弹窗、异常界面、浏览器控制台错误、网络请求截图等。
将截图与日志包一起上传到网盘，把下载链接填入 issue 的「日志下载链接」字段。
截图越详细，研发定位越快。
```

截图提醒不能一笔带过，要作为独立的引导步骤、用醒目的方式输出。

这 4 步是引导性的，用户可以跳过任何一步，不构成阻塞。

## 一条流水线

```
用户报错 / 问错误码
   ↓
[自检一级]  diag.sh collect（200 行采集）+ collect_error_tokens + sanitize（机制性）
   ↓
[静态判级]  词典命中 → L1 结论/建议（引用 error-codes.yaml fix）
            零命中 → L2/L3：带证据包进入 RCA
   ↓
[RCA 二级]  4 阶段（Reproduce→Locate→[2.5 实机验证]→Fix→Verify）
   ↓
[出口]     report（脱敏摘要包 + 反馈勾选区）
           issue → 生成预填 new-issue URL（点开 = 用户出网决策；body 仅已脱敏元数据）
           feedback solved/unsolved/skip → 本地 TSV（needs_manual_upload，不自动出网）
           session → 结构化反馈闭环（start→set→collect→preview→confirm→issue，状态机强制不跳步）
```

### 结构化反馈 session（v2.4.0）

当需要完整走一轮「采集 → 脱敏预览 → 确认 → 提交」闭环时，用 `session` 子命令替代零散的 report/issue 组合。状态机强制每步不可跳过：

```
session start → set(元数据) → collect(采集+判级)
   → preview(脱敏+统计) → confirm(用户确认) → issue(出链接)
```

状态流：`created → collected → previewed → confirmed → issued`。跳步会被拦截并提示当前状态与期望状态。

数据目录：`$DIAG_DATA_DIR/sessions/<id>/`（session.meta + evidence.txt + sanitized.md + stats.txt + logs/*.txt + session-*-logs.zip）。

## 4 阶段 RCA 铁律

**绝对不许跳步。** 每个阶段完成才能进入下一阶段。唯一例外是 **Stage 2.5 实机验证** —— 它按需触发，但一旦触发就必须跑完并记录预期 vs 实际。

### Stage 1: Reproduce（复现）

**目标**：确认问题确实存在，收集关键环境信息。必须确认：

1. **环境**：开发/测试/生产？Mock 模式还是真实 SDK？
2. **版本**：SDK JAR / paas.js / skeleton 版本？
3. **触发路径**：用户执行了什么操作导致问题？
4. **错误信息**：完整错误消息/错误码/堆栈/浏览器控制台输出
5. **最近变更**：**"以前是好的吗？中间改了什么？"** —— 代码提交、依赖/SDK 升级、配置或环境变量调整、平台侧变更。有变更就先对着变更查，比盲查全栈快一个量级
6. **频率**：必现 / 偶现 / 只出现一次 —— 直接决定排查策略（见 `references/rca-methodology.md`：**偶现优先按时序/竞态查**，其次才是资源耗尽、缓存失效、负载退化；总之不要按必现的路子查）

用户信息不足时**必须先问清楚，不要猜测**（复用 `scripts/diag.sh collect` 的 200 行日志可提高采集一致性）。

### Stage 2: Locate（定位）

**目标**：找到根因所在的层级和代码位置。定位路径（按优先级）：

1. **查错误码字典**（`error-codes.yaml`）→ 匹配则直接给根因与修复方案
2. **查 auto memory**（`~/.claude/projects/*/memory/`）→ 历史同场景复用
3. **按问题形态走专项清单**（命中即优先于分层排查）：
   - 拿到的是**平台 HTTP 响应体** → `references/platform-error-response.md`（先解构双错误码，外层 `code` 常是包装码）
   - **`listenXxx` 收不到回调** → `references/event-callback-checklist.md`（事件语义 / 订阅 SN / 平台已知缺失）
   - **设备无法直播 / 直播黑屏 / 设备离线 `212015`** → `references/live-stream-device-troubleshooting.md`（固件 → 上云方式 → 底座 `.env` → 端口，这类问题通常不在应用代码里）
   - **回放页 / 媒体页照片全黑、视频进度条停在 `0:00`、下载无反应，但列表与缩略图正常** → `references/replay-media-troubleshooting.md`（取流地址指向对象存储 `:30802`，客户网段不可达；存储本身健康）
4. **分层排查**：前端（浏览器→网络→DOM→paas.js）→ 协议层（HTTP/WS→鉴权 token）→ 后端（Spring 日志→SDK 回调→事件监听）→ SDK 层（初始化→连接→控制权→设备在线）
5. **边界埋点取证**（前 4 步定不下来时，**停止猜测**）：在 5 个组件边界各加一行日志，**跑一次**看证据在哪一层断掉，再只查那一层。逐层猜的成本远高于一次全链路取证 —— 埋点清单见 `references/rca-methodology.md`
6. **反向溯源**：报错点拿到的坏值（SN / workspace / token / host / 设备状态），顺着调用链往上追到它**第一次被赋值**的地方，**修在源头，不修在报错点**

> **在定位到根因之前，不要提出任何修复方案。** 上面的步骤没走完就不算定位到了 —— 看到症状 ≠ 找到根因。

### Stage 2.5: Verify Hypothesis（实机验证，按需）

**目标**：把 Stage 2 得出的「假设」用 MCP + 模拟器实测成「结论」。**不是每次都要做**。

**触发条件**（满足任一）：

- 代码逻辑看起来正确，但实际行为与之不符
- 报错模糊（`INVALID_STATE`、无错误码），存在多个可能分支
- 怀疑是设备状态/时序问题而非代码问题
- 需要确认某能力在当前平台版本上的**实际**行为

**不触发**：错误码已在词典中直接命中（L1）、纯静态问题（编译/类型/明显 NPE）。

**铁律**：

1. **先只读快照**（L0，永远安全）：`fh_device_list` → `fh_osd_snapshot` / `fh_dock_snapshot` → `fh_wayline_status` → `fh_hms`。大多数问题在这一步就能定性，不要急着起飞。
2. 有动作的调用（L1+）**只在模拟器 / 测试环境**执行；真机需用户**显式确认**。
3. `confirm=true` 之前先 `dry_run=true` 验证前置条件。
4. 控制权 / DRC / payload 必须释放，**即使中途报错**。
5. **一次只验一个假设、只动一个变量**。把假设写成一句话："我认为根因是 X，因为 Y。" 同时改多处 → 无法归因是哪条生效。
6. 记录**预期 vs 实际**：预期来自 Stage 2，实际是原始返回 / 遥测值。

**出口**：一致 → 假设证实，进 Stage 3；**不一致 → 回 Stage 2 重新定位，不要强行解释**。

**参考**：`references/live-verification.md` — 前置链、验证配方、8 条已知陷阱对照表。

### Stage 3: Fix（修复）

**目标**：生成修复代码。遵守对应业务 Skill 的 constraints.yaml（前端 → `cloudapi2-frontend-component`；后端 → `cloudapi2-device-management`；SDK 调用 → `cloudapi2-sdk-core` 核心规则）。

**一次一个修复**：只改根因那一处，不打包"顺手"的重构。改完立刻进 Stage 4 验证，**不要在没验证的修复上叠加下一个修复**。

**修复没生效 → 回 Stage 2 重新定位**，带着新信息重新分析，不要在原方案上继续加补丁。

#### 三振出局：修 3 次还不好，停手别修第 4 次

连续 3 次修复都没解决，说明**前提假设错了**，不是这次的方案不够好。典型信号：每修一处就在别的地方冒出新问题、修复要求"大改一片"。此时停下来改问三个问题：

1. **这个能力平台当前版本到底支不支持？** → 查 `references/event-callback-checklist.md` 的「平台已知缺失」（例如设备删除、非 Airport 绑定压根不推事件，代码写得再对也收不到）
2. **错误码归属域和你调的接口对得上吗？** → 查 `references/platform-error-response.md`。对不上 = 失败在平台内部下游，**你侧改参数大概率无效**
3. **是设备/固件状态问题而非代码问题吗？** → 回 Stage 2.5 用只读快照定性

**结论指向平台侧 → 停止改代码，走升级**：`diag.sh report` 出包 → `diag.sh issue` 生成提交链接，附完整响应体 + `request_id` + 设备 SN + 时间点。给用户一个明确的"这不是你的 bug"结论，比让他继续试第 4 次有价值得多。

### Stage 4: Verify（验证）

验证清单：
- [ ] 修复代码编译/构建通过
- [ ] 原始触发路径不再报错
- [ ] 没有引入新问题（回归检查）
- [ ] constraints.yaml 中 critical 规则仍满足

## 反模式自查（冒出这些念头 = 立刻回 Stage 2）

排障失控几乎总是从一句合理化开始。下面这些念头出现时，说明已经在猜了：

| 冒出的念头 | 现实 |
|---|---|
| "先快速修一下，回头再查" | 第一次修复定基调；方向错了后面全是返工 |
| "改改 X 试试看好不好" | 这是猜，不是排查 |
| "一次多改几处，省事" | 改动一多就无法归因是哪条生效，还会引入新 bug |
| "我大概知道是 X，直接修" | 看到症状 ≠ 找到根因 |
| "错误码看不懂，先绕过去" | 错误码常常直接写着答案，尤其内层 `err_code` |
| "不太懂但这么改可能行" | 半懂必错。说"我不确定 X"，然后去查 |
| "问题很简单，不用走流程" | 简单问题也有根因，走流程反而更快 |
| "再试最后一次"（已失败 2 次） | 三振了。质疑前提，别发起第 4 次修复 |

**用户说这些话 = 你走偏了，立刻停下回 Stage 2**：

- "你确认过吗 / 是不是根本没发生？" → 你在假设，没验证
- "别猜了" → 你在没理解的情况下提方案
- "这样能看到吗？" → 该加边界埋点了，不该继续推理

## 本地采集与反馈（合并自 diagnosis）

`scripts/diag.sh` 是本地闭环的可运行实现，命令：

| 命令 | 作用 |
|---|---|
| `diag.sh collect` | 固定只读命令集采集：env/disk/network/process/sdk_log（末 200 行）/action_whitelist |
| `diag.sh sanitize <f\|->` | 对文件/标准输入做一次脱敏（凭证/内网 IP/IPv6/MAC/邮箱/手机号/URL query/路径/JWT/PEM/GPS 降精度/设备 SN） |
| `diag.sh classify` | 静态判级：消费 sdk_log 段错误码 token → L1/L2/L3（决策表见 `rules/classification.yaml`） |
| `diag.sh report` | 生成脱敏诊断报告 `~/.dji_cloud/diag/reports/report-*.md`（环境/日志/初步结论 + 反馈区） |
| `diag.sh issue [报告路径]` | 生成 GitHub new-issue 预填 URL（模板/标题/正文；缺省取最新报告；点击=出网决策） |
| `diag.sh feedback <solved\|unsolved\|skip> [报告路径]` | 追加到本地 `feedback.tsv`（4 列：第 4 列=报告文件名或 `-`；状态恒 `needs_manual_upload`） |
| `diag.sh trace <trace_id>` | 按 trace_id 精准采集（grep SDK + 应用日志路径，±5 行上下文，500 行截断，经脱敏） |
| `diag.sh session start` | 新建结构化反馈 session（返回 session ID）|
| `diag.sh session set <id> key=value` | 设置元数据（company/error_text/trace_id/severity/contact/sdk_version）；severity 须为表单选项 致命/严重/一般/提示 |
| `diag.sh session collect <id>` | 采集证据 + 判级（本地或远程；远程时自动探测 Loki，有则走 API 查询，否则 docker logs；按服务存为独立日志文件 + zip 打包；含包大小检查和内容指纹去重）→ collected |
| `diag.sh session preview <id>` | 脱敏预览 + 10 类命中统计 → previewed |
| `diag.sh session confirm <id>` | 用户确认无敏感信息 → confirmed |
| `diag.sh session issue <id>` | 从 meta 构造 issue URL → issued；加 `--gh` 则通过 gh CLI 直接创建（AI 默认用此模式）|
| `diag.sh session show <id>` | 查看采集内容（脱敏后优先展示 sanitized.md，否则 evidence.txt） |
| `diag.sh session status [id]` | 查看单个或全部 session 状态 |
| `diag.sh cleanup [--days N]` | 清理过期 session（默认 30 天）|
| `diag.sh auto` | collect + classify + 下一步指引 |

**判级语义**（详见 `rules/classification.yaml`）：词典命中保守落 **L1**（给结论/建议，引用 fix，不代跑）；零命中 → **L2**（结构化升级包）；零命中且 env/sdk_log 缺失 → **L3**（原始工单 + 低质量包）。

## 数据与出网

- **默认不出网**：collect 只读采集、sanitize 脱敏、classify 静态判级，全部本地完成。
- **出网控制点 = 用户决策**：`report` 产出脱敏摘要包（含环境/日志/初步结论 + 反馈勾选区），**由用户决定**是否将报告内容贴到 GitHub 开源仓库 issue（人工操作）。
- `issue` URL body 仅含已脱敏元数据（判级/错误码/时间/来源/os 行/报告文件名），**显式排除 hostname 与日志正文**——元数据打印到终端由用户复制粘贴到表单，URL 仅携带 `?title=`（模板名 + 标题），全文只能由用户手动粘贴。
- 仓库地址真值源在 diag.sh 常量 `DEFAULT_ISSUE_REPO`；`DIAG_ISSUE_REPO` 环境变量可覆盖（默认地址待最终定稿）。
- `feedback` 恒为 `needs_manual_upload`，**不自动上报、不回流大疆**。
- 对客户机器只读；数据目录 `~/.dji_cloud/diag/`（sessions/ + reports/ + feedback.tsv）是唯一写盘点。

## 不变量

1. **采集命令集固定**，LLM/对话不可改写；用户要求跑任意新命令 → 拒绝。
2. **判级以证据包 token 为输入**，不依赖自由文本；但允许 AI 介入（本机 AI 在旁）。
3. **敏感数据默认不出网；进入出网通道（issue URL）的内容仅限已脱敏元数据，完整出网仅由用户主动决策**。
4. **对客户机器只读**；写盘仅限 `$DIAG_DATA_DIR`（`~/.dji_cloud/diag/`）。

## 错误码速查

完整词典见**唯一真值源** `error-codes.yaml`（frontend + backend + 设备侧 6 位码）。快速索引：

| 码段 | 归属 | 典型问题 |
|------|------|----------|
| 4001-4002 | WebSocket/鉴权 | token 过期、auth 配置错误 |
| 5000-5001 | 接口调用 | API 请求失败、参数错误 |
| 6000-6002 | 地图/渲染 | Cesium 初始化失败、paas.js 未加载 |
| 7000 | 通用 | 未分类错误 |
| ERROR_* | SDK 后端 | 初始化/状态/设备未找到/回调超时/监听失败 |
| NPE | Java | null guard 缺失 |
| 312xxx-514xxx | 设备端 6 位码 | 升级/任务/链路/起飞前检查/直播/远程控制等 |
| 200XXX | 平台通用框架 | token/权限/参数/RPC 包装码（`200612` 需解构内层 `err_code`）|
| 210XXX-220XXX | 平台业务域 | 按 `2XXYYYY` 的 `XX` 判归属域，详见 `references/platform-error-response.md` |
| 212015 / 2130xx | 设备接入 / 直播 | 设备离线、未推流 —— 私有化环境先按 `references/live-stream-device-troubleshooting.md` 查 |

## 与 diagnosis 的关系（弃用）

- 本 Skill 从 **v2.0.0** 起替代 `cloudapi2-diagnosis` → 本地诊断 + RCA 一条流水线。
- `cloudapi2-diagnosis` 目录保留作历史/设计走查，其 SKILL.md 已含弃用声明。
- 若用户提到 `cloudapi2-diagnosis` / `diag.sh`，一律转到本 Skill 的 `scripts/diag.sh`。

## Memory 回写规则

修复/判级完成后询问用户“本次是否有效？是否记录？”：

- **优先落本地 TSV**：用户确认后运行 `diag.sh feedback [报告路径]`，只落本地，不自动进 auto memory；附报告路径时联动打印 `diag.sh issue` 提交链接。
- **auto memory（可选）**：仅在用户明确要记录经验时，写 `feedback` 类型 memory（`~/.claude/projects/*/memory/`），记录场景/根因/方案/约束。

## 权威参考

- `error-codes.yaml` — 错误码唯一真值源（frontend/backend/设备侧）
- `rules/classification.yaml` — 判级决策表（人读）
- `scripts/diag.sh` — 采集/脱敏/判级/报告/反馈真身
- `references/rca-methodology.md` — 4 阶段 RCA 方法论：变更清单、边界埋点取证、反向溯源、三振升级
- `references/live-verification.md` — Stage 2.5 实机验证：前置链、验证配方、已知陷阱
- `references/platform-error-response.md` — 平台 HTTP 响应解构：双错误码、域前缀表
- `references/event-callback-checklist.md` — `listenXxx` 收不到回调排查清单
- `references/live-stream-device-troubleshooting.md` — EA340/EA610 私有化环境无法直播：固件版本、上云方式（`WZR2JJ`）、DRTC/FlightHub2 底座 `.env`、端口表、两个历史案例
- `references/replay-media-troubleshooting.md` — 回放页/媒体页照片全黑、视频停在 `0:00`：取流地址指向对象存储 `:30802`，客户网段不可达（存储健康），附两条判定命令与平台侧修复方向
- `cloudapi2-feedback` — 独立问题反馈 Skill（纯反馈场景优先使用；共享 diag.sh 脚本）
- `skills/_shared/constraints-common.yaml` — 跨 Skill 共享约束（8 条通用规则）
- `skills/_shared/skill-routing.md` — Skill 路由/仲裁
- `skills/_shared/output-format.md` — 统一输出结构

> Reference 按需懒加载。输出格式见 `_shared/output-format.md`。