# cloudapi2-diagnosis — 本地诊断能力基座

> 版本：0.1.0 · 2026-09-03 · 对应设计文档 `智能诊断_12_ISVSideSkill方案.md`（v0.1）

## 这是什么

> **⚠️ 已弃用（v2.0.0 起）**：本 Skill 的能力已并入 `cloudapi2-troubleshooting`（本地采集 / 判级 / 报告 / 反馈 + RCA 一条流水线），详见 `cloudapi2-troubleshooting/SKILL.md` 的弃用说明。

Cloud API 2.0 SDK 的**本地诊断能力基座**，面向 ISV **私有化 / 数据不出网** 场景。

一句话：**诊断的第一跳（采集 → 脱敏 → 静态判级）在本地闭环完成；归因如果发生，发生在服务端。**

它在文档谱系中的位置：是 `智能诊断_12` 方案第 7.1 节落地顺序**步骤一**（能力基座骨架）的可运行实现，
与既有 `cloudapi2-troubleshooting` **不重叠**——那是开发联调期有外网的归因链路，这里是私有化现场的采集+判级第一跳。

## 目录结构

```
cloudapi2-diagnosis/
├── SKILL.md                      # Skill 入口（触发条件 + 工作流 + 不变量）
├── README.md                     # 本文件
├── scripts/
│   └── diag.sh                   # 真身：collect / sanitize / classify / auto
├── rules/
│   └── classification.yaml       # 判级决策表（人读版，与 diag.sh 语义一致）
└── dicts/
    └── error-codes.yaml          # 随包错误码词典（派生自 troubleshooting backend 段）
```

## 快速开始

```bash
# 完整演示：采集 + 静态判级（零出网）
skills/cloudapi2-diagnosis/scripts/diag.sh auto

# 只采集（输出证据包分组）
skills/cloudapi2-diagnosis/scripts/diag.sh collect

# 只判级（消费上一步证据包，输出 L0/L1/L2/L3）
skills/cloudapi2-diagnosis/scripts/diag.sh classify

# 对指定文件做一次脱敏（凭证/内网 IP/邮箱/手机号/路径）
skills/cloudapi2-diagnosis/scripts/diag.sh sanitize path/to/file
```

## 它是怎么工作的（三层，全本地）

| 层 | 位置 | 作用 |
|---|---|---|
| 采集 | `diag.sh collect` | 固定只读命令集：uname/df/ping/netstat|ip/lsof + 常见 SDK 日志目录 tail 200 行 |
| 脱敏 | `diag.sh sanitize` | sed 正则管道：凭证字段值/内网 IP/邮箱/手机号/URL query/用户路径 |
| 判级 | `diag.sh classify` | 静态决策表消费 **sdk_log 段的错误码 token**：词典命中→L1，未命中→L2，零命中且关键采集缺失→L3 |

## 判级语义（简短版，详见 rules/classification.yaml）

| 输出 | 条件 | 动作 |
|---|---|---|
| L1 | 词典命中（保守路由，v0.1 无 L0） | 给结论与建议，引用 `dicts/error-codes.yaml` fix |
| L2 | 零命中 | 结构化升级包方向（sanitized 摘要） |
| L3 | 零命中 且 env/sdk_log 为空 | 原始工单 + 现场快照，标注低质量包 |

## 与其它 Skill 的关系

- **cloudapi2-troubleshooting**：有外网开发联调 → 归因 RCA。本 Skill **不是**它的替代。
- **cloudapi2-sdk-core / device-management / sei-osd**：写代码走它们，诊断不碰。
- **`_shared/`**：跨 Skill 共享约束 / 路由 / 输出格式统一复用。

## 边界与红线（摘要）

- **采集命令集固定**，LLM/对话不可改写；用户要求跑任意新命令 → 拒绝。
- **判级只消费 token**，不读自由文本。
- **无写操作**，修复动作由用户自行执行。
- 本地闭环默认**不产生上报、不铸造度量标识**；增强通道（显式出网）才走服务端度量。

## 已知限制（v0.1 如实承认）

- **L0 未启用**：词典命中统一保守落 L1。L0 需要「动作具体到可复制粘贴」的分类启发式，按 ISV 侧设计开放问题 12 决策后放开。
- **词典只有 6 条 backend 错误码**：覆盖 SDK 常见项（initialize 失败/状态错误/设备未找到/回调超时/监听失败/NPE）；词典外一律 L2，宁可升级不误判。
- **脱敏 URL query 正则会带上尾随引号**（`?token=******"`）：已知瑕疵，v0.1 最小处理，后续改进 regex。
- **SDK 日志只扫常见路径**：未命中目录时 sdk_log 组为空 → L3 信号之一，需向用户确认路径。
- **bash 3.2 兼容**：`set -u` 启用；未覆盖 Windows。

## 升级/回归（三处一致性）

> **⚠️ v2.0.0 起本 Skill 已并入 `cloudapi2-troubleshooting`，本目录已冻结**：唯一维护点在
> `../cloudapi2-troubleshooting/error-codes.yaml`（backend 段，唯一真值源）。以下三处同步清单与校验命令
> 仅作**档案留档**（记录合并前的自查流程）；本 skill 的 `dicts/error-codes.yaml` 与 `scripts/diag.sh`
> 不再作为维护副本，后续改造请直接改 troubleshooting 侧。

改词典或判级时必须同步 **三处**，否则语义漂移：
1. `../cloudapi2-troubleshooting/error-codes.yaml`（backend 段，唯一真值源）
2. `dicts/error-codes.yaml`（本 skill 随包词典）
3. `scripts/diag.sh` 的 `classify_code` case

校验命令：
```bash
grep -Eo 'ERROR_[A-Z_]+|NPE' scripts/diag.sh | sort -u > /tmp/a
grep -E '^  - code: (ERROR_[A-Z_]+|NPE)$' dicts/error-codes.yaml | awk '{print $3}' | sort -u > /tmp/b
diff /tmp/a /tmp/b   # 空 = 一致
```