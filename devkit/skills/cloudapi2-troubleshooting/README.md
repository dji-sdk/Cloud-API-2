# cloudapi2-troubleshooting — DJI FlightHub 排障助手

> **版本**：v2.6.0 | **更新**：2026-09-22
> **适用对象**：ISV 集成工程师、售后支持、开发者

本 Skill 提供 DJI FlightHub PaaS SDK 的一站式排障能力：错误码查询、环境采集、日志分析、脱敏、问题分级、issue 生成。支持本地和远程（SSH + Loki）两种采集模式。

---

## 一、前置条件

### 1.1 系统要求

| 项目 | 要求 |
|------|------|
| 操作系统 | macOS / Linux（Windows 请使用 WSL） |
| Shell | bash 3.2+（macOS 自带） |

### 1.2 必需工具

以下工具在大多数系统中已预装，无需额外安装：

| 工具 | 用途 | 检查命令 |
|------|------|---------|
| `bash` | 脚本运行环境 | `bash --version` |
| `curl` | 远程采集 Loki 查询、错误码 API | `curl --version` |
| `grep` / `sed` / `awk` | 日志分析、脱敏 | 系统自带 |
| `ssh` | 远程主机连接 | `ssh -V` |

### 1.3 推荐工具（可选，增强体验）

| 工具 | 用途 | 安装方式 | 缺失时的影响 |
|------|------|---------|-------------|
| `python3` | Loki JSON 解析、错误码 API 解析 | 系统自带或 `brew install python3` | Loki 查询结果返回原始 JSON 而非格式化文本 |
| `sshpass` | 远程采集时自动输入 SSH 密码 | `brew install sshpass` / `apt install sshpass` | 需手动交互输入密码或配置 SSH 免密 |
| `zip` | 远程日志打包 | 系统自带或 `apt install zip` | 日志文件不打包，仍可独立查看 |
| `gh` | GitHub CLI，用 `--gh` 参数直接创建 issue | `brew install gh` | 回退到生成 URL 模式（手动打开链接提交） |
| `java` / `mvn` / `node` | 本地采集时检测开发环境版本 | 按项目需要安装 | 采集结果中环境版本段为空 |

### 1.4 AI 工具（任选其一）

本 Skill 设计为由 AI 工具驱动。安装到以下任一 AI 工具后即可通过对话使用：

- [Claude Code](https://docs.anthropic.com/en/docs/claude-code)
- [Cursor](https://cursor.sh/)
- [GitHub Copilot](https://github.com/features/copilot)

> 也可以不借助 AI 工具，直接在终端手动执行 `diag.sh` 命令（见第五节）。

---

## 二、安装

### 2.1 通过 devkit Plugin 一键安装（推荐）

如果已安装 `devkit@cloud-api2` Plugin，所有 Skill 已自动就绪，无需额外操作。

### 2.2 手动安装

从 devkit 仓库复制本 Skill 到 AI 工具的 skills 目录：

```bash
# 克隆仓库（如尚未克隆）
git clone https://github.com/dji-sdk/dji-cloudapi2-devkit.git
cd dji-cloudapi2-devkit

# Claude Code
cp -r skills/cloudapi2-troubleshooting ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-troubleshooting .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-troubleshooting .github/skills/
```

### 2.3 验证安装

```bash
# 确认脚本可执行
chmod +x ~/.claude/skills/cloudapi2-troubleshooting/scripts/diag.sh

# 运行版本检查
~/.claude/skills/cloudapi2-troubleshooting/scripts/diag.sh version
# 预期输出: cloudapi2-troubleshooting v2.6.0

# 运行自检（采集本机环境信息）
~/.claude/skills/cloudapi2-troubleshooting/scripts/diag.sh auto
```

---

## 三、配置

所有配置通过环境变量注入，**均为可选**，不配置也能正常使用。

### 3.1 环境变量一览

| 变量 | 默认值 | 用途 |
|------|--------|------|
| `DIAG_DATA_DIR` | `~/.dji_cloud/diag` | 数据存储目录（采集结果、报告、session） |
| `DIAG_ISSUE_REPO` | `https://github.com/dji-sdk/Cloud-API-2` | issue 提交的目标 GitHub 仓库 |
| `DIAG_UPLOAD_URL` | （空） | 大文件上传网盘地址，未配置时仅在终端提示"请自行准备上传方式"，不影响本地采集/脱敏/生成报告的闭环流程 |
| `DIAG_SSH_PASS` | （空） | SSH 密码，用于远程采集自动化（不存文件） |
| `DIAG_MAX_CONTAINERS` | `10` | 远程采集时最大容器数，超出按名称排序截取 |
| `DIAG_MAX_PACKAGE_SIZE_MB` | `200` | zip 包大小警告阈值（MB），500MB 硬上限自动裁剪 |
| `DIAG_SESSION_TTL_DAYS` | `30` | session 过期天数，`cleanup` 命令和自动清理使用 |
| `DIAG_SESSION_MIN_INTERVAL` | `600` | session 创建最小间隔（秒），防止短时间内重复创建 |
| `DIAG_MAX_ISSUES_PER_ISV` | `20` | 每企业最大已提交 issue 数，超限警告不阻断 |

### 3.2 推荐配置

环境变量均为可选，大多数场景下无需额外配置即可使用。

---

## 四、使用方式（AI 对话）

安装完成后，在 AI 工具中用自然语言描述问题即可触发。**你不需要记任何命令**。

### 4.1 触发方式

以下任意一种说法都能触发本 Skill：

```
"调用 SDK 报 ERROR_INIT_FAILED"
"前端组件白屏了"
"返回了 4001 错误"
"直播打不开，报 212015"
"帮我采集一下环境信息"
"我要反馈一个问题"
"帮我提个 issue"
```

即使没有错误码，一句口语描述也能触发。

### 4.2 场景：最简反馈（一句话搞定）

```
你：我要反馈一个问题，看板在线设备数不对

AI：好的，请提供以下信息（缺失的可以先跳过）：
    - 企业名称？
    - 严重程度？（致命 / 严重 / 一般 / 提示）

你：XX电力，严重

AI：问题出在本机环境，还是部署在其他服务器上？

你：本机

AI：正在采集环境信息...
    ✓ 采集完成，判级 L2
    继续脱敏预览？

你：继续

AI：脱敏预览中... 以下敏感信息已被替换：
    - 内网 IP：3 处
    - 用户路径：2 处
    确认脱敏后的内容没有遗漏敏感信息？

你：确认

AI：已生成 issue 链接：https://github.com/dji-sdk/Cloud-API-2/issues/new?...
    请务必补充问题截图！
```

### 4.3 场景：远程服务器问题

```
你：线上服务报错了，部署在 192.0.2.10 上

AI：好的，我会通过 SSH 远程采集。请提供 SSH 用户名？

你：root

AI：请输入 SSH 密码：

    ... 连接远程主机 diag@192.0.2.10
    ... 探测 Loki 日志服务
    OK  发现 Loki: http://192.0.2.10:30828
    OK  Loki 查询: pf-beacon-reporter (埋点上报) (42 行)
    OK  Loki 查询: pf-devkit-skeleton-server (主后端/SDK) (156 行)
    OK  Loki 查询: pf-notify-service (通知服务) (23 行)
    ✓ 已打包：session-xxx-logs.zip
```

> 如果远程主机没有 Loki，会自动回退到 `docker logs` 逐容器采集。

### 4.4 场景：用 trace_id 精准定位

```
你：回调超时了，trace_id 是 trace-20260921-abc123

AI：收到 trace_id，我会在采集时联动搜索所有相关日志。
    ✓ trace_evidence.txt 中找到 3 处匹配
```

### 4.5 场景：带错误码的排查

```
你：调用 initialize 报了 ERROR_INIT_FAILED

AI：ERROR_INIT_FAILED 是 SDK 初始化失败。常见原因：
    1. serverAddress 配置错误
    2. accessToken 无效或已过期
    3. 网络不通
    
    我来采集环境信息确认...
    ✓ 判级 L1（错误码已命中词典），修复建议：...
```

---

## 五、使用方式（手动命令）

以下是底层 `diag.sh` 的完整命令，可直接在终端执行。

### 5.1 快速入门

```bash
# 简写：DIAG 指向脚本路径
DIAG=~/.claude/skills/cloudapi2-troubleshooting/scripts/diag.sh

# 一键诊断（采集 + 判级 + 下一步指引）
$DIAG auto

# 按 trace_id 精准采集
$DIAG trace <trace_id>
```

### 5.2 结构化反馈 session（完整闭环）

```bash
# 1. 新建 session
$DIAG session start
# 输出: 20260922-143025

# 2. 设置元数据
$DIAG session set 20260922-143025 \
  company=示例科技 \
  severity=严重 \
  error_text="看板在线设备数不对" \
  sdk_version=3.3.7

# 3. 采集 + 判级（本地）
$DIAG session collect 20260922-143025

# 3b. 远程采集（二选一）
$DIAG session set 20260922-143025 \
  remote_host="diag@192.0.2.10" \
  docker_container="skeleton-backend"
$DIAG session collect 20260922-143025

# 4. 脱敏预览
$DIAG session preview 20260922-143025

# 5. 确认无敏感信息
$DIAG session confirm 20260922-143025

# 6. 生成 issue 链接
$DIAG session issue 20260922-143025

# 6b. 直接用 gh CLI 创建 issue（需安装 gh 并登录）
$DIAG session issue --gh 20260922-143025
```

状态流：`created → collected → previewed → confirmed → issued`，不可跳步。

### 5.3 独立命令

| 命令 | 作用 |
|------|------|
| `diag.sh collect` | 采集本机环境信息到 stdout |
| `diag.sh classify` | 采集 + 静态判级（L1/L2/L3） |
| `diag.sh sanitize <file>` | 对文件内容做脱敏 |
| `diag.sh report` | 生成脱敏后的诊断报告 |
| `diag.sh issue [报告路径]` | 生成 GitHub issue 预填链接 |
| `diag.sh feedback <solved\|unsolved\|skip>` | 记录反馈到本地 TSV |
| `diag.sh trace <trace_id>` | 按 trace_id 精准采集日志 |
| `diag.sh session status` | 查看所有 session 状态 |
| `diag.sh cleanup [--days N]` | 清理过期 session（默认 30 天） |
| `diag.sh version` | 显示版本号 |

---

## 六、v2.6.0 新功能

### 脱敏增强（10 类覆盖）

在原有 6 类（凭证/内网 IP/邮箱/手机号/URL 参数/用户路径）基础上新增：

| 类型 | 示例 |
|------|------|
| IPv6 | `fe80::1:2:3:4` → `***:***:***:***` |
| MAC 地址 | `aa:bb:cc:dd:ee:ff` → `**:**:**:**:**:**` |
| JWT | `eyJhbG...` → `***JWT***` |
| PEM 私钥/证书 | `-----BEGIN PRIVATE KEY-----` → `***PRIVATE_KEY_REMOVED***` |
| GPS 坐标 | `113.946521` → `113.94***`（降精度到 2 位小数） |
| IMEI（15 位） | `123456789012345` → `***IMEI***` |

### 防滥用与存储管理

- **session 限频**：10 分钟内不可重复创建 session
- **内容指纹去重**：证据包 MD5 哈希比对，重复提交警告
- **per-ISV 配额**：每企业最多 20 个已提交 issue，超限警告
- **相似单检测**：error_text 前缀比对已提交单
- **包大小上限**：200MB 警告 / 500MB 自动裁剪
- **TTL 清理**：`diag.sh cleanup [--days N]` + session start 自动清理
- **容器上限**：远程采集默认最多 10 个容器（`DIAG_MAX_CONTAINERS` 可配）

---

## 七、v2.5.0 功能

### 7.1 Loki 自动探测

远程采集时，脚本自动探测远程主机上的 Loki 日志服务（端口 3100 / 30828）：

- **有 Loki** → 通过 API 按容器名查询日志，比逐容器 `docker logs` 快
- **无 Loki** → 静默回退到 `docker logs`，行为不变

日志文件头部标注来源，方便判断采集方式：

```
# Container: pf-beacon-reporter — 埋点上报
# Source: Loki (http://192.0.2.10:30828)
# Query: container_name=~"pf-beacon-reporter" |~ "trace-abc123"
# Time range: last 60m, limit 500
```

### 7.2 服务角色映射

容器列表和日志文件自动标注中文服务角色：

| 容器名模式 | 角色 |
|---|---|
| `*beacon-reporter*` | 埋点上报 |
| `*skeleton-server*` | 主后端/SDK |
| `*notify-service*` | 通知服务 |
| `*gateway-service*` | API 网关 |
| `*media-*` | 媒体服务 |
| `*live-*` | 直播服务 |
| `*wayline-*` | 航线服务 |
| `*manage-service*` | 管理服务 |
| `*thing-access*` / `*tas-*` | 设备接入 |
| `*inventory-collector*` | 资产采集 |

---

## 八、数据与目录结构

### 8.1 数据目录

所有数据存放在 `~/.dji_cloud/diag/`（可通过 `DIAG_DATA_DIR` 覆盖）：

```
~/.dji_cloud/diag/
├── sessions/                          # session 数据
│   └── 20260922-143025/
│       ├── session.meta               # 元数据（key=value）
│       ├── evidence.txt               # 采集原始证据
│       ├── sanitized.md               # 脱敏后内容
│       ├── stats.txt                  # 脱敏命中统计
│       ├── logs/                      # 远程采集的各服务日志
│       │   ├── pf-devkit-skeleton-server.txt
│       │   ├── pf-notify-service.txt
│       │   └── sdk-log.txt
│       └── session-20260922-143025-logs.zip
├── reports/                           # 独立诊断报告
│   └── report-20260922-150000.md
└── feedback.tsv                       # 反馈记录
```

### 8.2 Skill 目录结构

```
cloudapi2-troubleshooting/
├── SKILL.md                 # AI 入口（4 阶段 RCA + 路由规则）
├── README.md                # 本文档
├── error-codes.yaml         # 错误码词典（唯一真值源）
├── scripts/
│   ├── diag.sh              # 采集/脱敏/判级/session/issue 主脚本
│   └── test_diag.sh         # 自动化测试
├── rules/
│   └── classification.yaml  # 判级决策表
└── references/
    ├── rca-methodology.md                   # RCA 方法论
    ├── live-verification.md                 # 实机验证指南
    ├── platform-error-response.md           # 平台响应解构
    ├── event-callback-checklist.md          # 回调排查清单
    ├── live-stream-device-troubleshooting.md # 直播排障
    └── replay-media-troubleshooting.md      # 回放/媒体排障
```

---

## 九、自动脱敏

采集后会自动脱敏 10 类敏感信息：

| 类型 | 示例 |
|------|------|
| 凭证 | `password=abc123` → `password="***"` |
| 内网 IP | `192.0.2.10` → `***.***.***.***` |
| 邮箱 | `dev@test.com` → `***@***` |
| 手机号 | `13912345678` → `139-****-****` |
| URL 参数 | `?token=abc` → `?token=***` |
| 用户路径 | `/Users/someone/` → `/***/` |
| IPv6 | `fe80::1:2:3:4` → `***:***:***:***` |
| MAC 地址 | `aa:bb:cc:dd:ee:ff` → `**:**:**:**:**:**` |
| JWT | `eyJhbG...` → `***JWT***` |
| GPS 坐标 | `113.946521` → `113.94***` |

脱敏强制执行，不可绕过。

---

## 十、三级判定

| 级别 | 含义 | 结果 |
|------|------|------|
| L1 | 错误码已在词典中 | 直接给出结论和修复建议 |
| L2 | 未匹配已知错误码 | 生成完整证据包，建议提 issue |
| L3 | 关键日志缺失 | 提示补充日志后重新采集 |

---

## 十一、安全承诺

| 项目 | 保证 |
|------|------|
| 数据出网 | 默认不出网，唯一出口是你手动点开 issue 链接 |
| 采集范围 | 固定命令集，只读，不修改任何文件 |
| 写盘范围 | 仅 `~/.dji_cloud/diag/` 目录 |
| SSH 密码 | 不写入任何文件，不进入日志 |
| 脱敏 | 强制执行，不可绕过 |

---

## 十二、常见问题

### "我说了问题但 AI 没触发这个 Skill"

确保描述中包含 SDK 相关的上下文。与 DJI SDK 无关的业务问题不会触发。

### "远程采集用了 Loki 还是 docker logs"

看日志文件头部 `# Source:` 行。`Loki (http://...)` 说明走了 Loki API，`docker logs` 说明走了 SSH 逐容器采集。

### "远程采集失败了怎么办"

检查 SSH 连接（地址、密码、网络）。可以先手动测试：`ssh diag@192.0.2.10 'echo ok'`。

### "判级是 L3 说明什么"

没找到 SDK 日志。告诉 AI 你的日志实际路径，或把日志复制到 `~/.dji_cloud/log/` 后重新采集。

### "可以不提 issue 只看诊断结果吗"

可以。说"帮我诊断一下当前环境"或直接执行 `diag.sh auto`。

### "大日志怎么办"

超过 65KB 时，AI 会提示上传到网盘。操作：打开上传地址 → 上传 zip → 复制下载链接 → 粘贴到 issue 表单。

---

## 十三、错误码速查

遇到错误码时直接告诉 AI，它会自动查词典。常见码段：

| 码段 | 归属 | 典型问题 |
|------|------|---------|
| 4001-4002 | 前端 WebSocket | token 过期、连接失败 |
| 5000-5001 | 前端 HTTP | API 请求失败 |
| 6000-6002 | 前端地图 | Cesium 加载/渲染失败 |
| ERROR_* | 后端 SDK | 初始化/状态/设备/回调/监听 |
| 200xxx | 平台 | token/权限/参数 |
| 212015 | 设备接入 | 设备离线 |
| 312xxx-514xxx | 设备端 | 升级/任务/直播/远程控制 |

完整词典见 `error-codes.yaml`（唯一真值源）。
