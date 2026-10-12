---
name: cloudapi2-diagnosis
description: 【已弃用，请勿使用】本 Skill 的全部能力已于 v2.0.0 并入 cloudapi2-troubleshooting，本目录仅保留作为历史归档与设计走查记录，其中的脚本已冻结、不再维护。任何本地诊断、现场采集、脱敏、离线判级、报告与反馈需求，一律改用 cloudapi2-troubleshooting。
version: 2.0.0
author: DJI CloudAPI2 Team
---

> **⚠️ 已弃用（v2.0.0 起）**：本 Skill 的能力已并入
> `skills/cloudapi2-troubleshooting`（本地采集 / 判级 / 报告 / 反馈 + RCA 一条流水线）。
> 本目录保留作为历史与设计走查；新实现与后续维护请到 `cloudapi2-troubleshooting/`。

# Cloud API 2.0 SDK 诊断能力基座（本地闭环）

## 这个 Skill 是干什么的

面向 **ISV 私有化、数据不出网** 场景的本地诊断能力基座。用**固定命令集**在 ISV 本机收集证据（env / disk / network / process / SDK 日志），**正则脱敏**后做**静态判级**，输出本地证据包 —— 全程不产生网络上报，没有服务端依赖。

一句话：**诊断的第一跳（采集 → 脱敏 → 静态判级）在本地闭环完成；归因如果发生，发生在服务端。**

## 何时触发

- 用户要求**本地**收集证据 / 现场采集（"本地帮我查一下"、"现场跑下采集"、"数据不出网"）
- 用户在私有化现场遇到错误码 / 日志异常，先做本地判定
- 用户问 `diag.sh`、`diag` 子命令、本地判级
- 环境/日志/网络等证据需要先落地再决定是否升级

**不触发**：用户已经给了线上会话 / 明确要模型归因（走 CLI + `fh-diag-server`）；写代码（走 sdk-core）；单纯的错误码问询（走 troubleshooting）。

## 本 Skill 的边界（与 troubleshooting 的分工）

| 维度 | 本 Skill（cloudapi2-diagnosis） | troubleshooting |
|---|---|---|
| 场景 | **私有化 / 数据不出网** / 本地闭环 | 开发联调 / 有外网 |
| 核心动作 | **采集 → 脱敏 → 静态判级**（第一跳） | **归因 → 定位 → 修复**（RCA） |
| 判级 | 本地静态决策表（L0-L3 离线版） | 词典 + RAG + 排查分层 |
| 数据 | 数据不出网，本地证据包 | 可能回读线上 / 服务端知识 |
| 出口 | 结果 / 本地建议 / 升级方向（无服务端） | 修复代码 / 结论 |

路由仲裁见 `skills/_shared/skill-routing.md`。

## 工作流 — 触发即走，不可跳步

### Step 0: 意图确认（成本最低的核对）

先确认三件事，**缺一不可**：
1. **场景**：私有化现场 / 数据不出网？还是开发联调（后者转 troubleshooting）？
2. **命令集**：确认用内置固定采集集（`diag.sh` 内置），**不执行用户要求的任意命令**——固定命令集不可被对话改写（见不变量 1）。
3. **执行许可**：只读采集 + 只读白名单。**用户要求的写操作（改配置/重启服务）一律拒绝**，落到"建议由用户自行执行"。

### Step 1: 采集（collect）

运行：

```bash
devkit/skills/cloudapi2-diagnosis/scripts/diag.sh collect
```

产出结构化证据包（分组 `env / disk / network / process / sdk_log / action_whitelist`）。**只读命令集写死在脚本里，与 LLM 无关**（不变量 1）。若用户希望只对某个文件做脱敏（不跑完整采集），用 `sanitize <file>`。

### Step 2: 判级（classify）

运行：

```bash
devkit/skills/cloudapi2-diagnosis/scripts/diag.sh classify
```

静态判级器只消费**证据包内的错误码 token**（sdk_log 段的 `ERROR_* / NPE`），不读取自由文本（不变量 2）。

| 输出 | 含义 | 动作 |
|---|---|---|
| `L1: 词典命中` | 已知故障，词典有 cause/fix | 给结论与建议，引用 `dicts/error-codes.yaml` 的 fix |
| `L2: 零命中` | 词典外错误码 / SDK-服务端问题 | 生成结构化升级包（sanitized 证据摘要） |
| `L3: 关键采集缺失` | 零命中且 env/sdk_log 为空 | 降级为原始工单 + 现场快照，标注低质量包 |

> 命中词典（保守落 L1）：只给结论与建议，**不主动执行任何命令**。修复动作由用户自行执行——收窄后的 L0 语义（见 [03 §3.2]）。
> 判级逻辑的**可执行版本在 `scripts/diag.sh`**，`rules/classification.yaml` 是人读映射；改判级先改脚本，再同步 YAML。

### Step 3: 输出 + 本地留存（report/feedback）

判级后，按 `_shared/output-format.md` 结构输出完整诊断结果，必须包含：

1. **证据包摘要**：关键分组命中/缺失（如 `sdk_log_dir` 未找到）
2. **静态判级结论**：一句 `L1/L2/L3 + 理由`
3. **词典引用（仅 L1）**：`dicts/error-codes.yaml` 中的 cause/fix 条目
4. **下一步建议**：
   - L1 → 请用户按 fix 步骤自查（重申：由用户执行，不代跑）
   - L2 → 明确"此问题需要键盘升级"，给出包含证据包的升级方向
   - L3 → 要求补采集（缺 env 或 sdk_log），或记为低质量包直接升级
5. **本地留存两条命令**（数据不出网，落 `$HOME/.dji_cloud/diag/`）：
   - `diag.sh report` → 生成脱敏诊断报告（含环境/日志/初步结论 + 反馈区），供后续回传
   - `diag.sh feedback <solved|unsolved|skip>` → 一键反馈，追加到本地 `feedback.tsv`

> **反馈环节的当前边界（重要）**：反馈**只在本地留存**（Type B：本地注册表 `feedback.tsv`），**不同步回流到大疆**——第三章不变量 3 的数据不出网底线 + 12 方案第 7.2 待决策项 4/3（`trigger_pattern-only` 回流 / 增强通道）尚未裁决。**回传大疆的预期渠道是 GitHub 开源仓库**（issue 人工跟进），报告文件名可作为 issue 素材。**本 Skill 不铸造度量标识、不做数据上报，即使本地有反馈也不发大疆。**

> **L0（本版本未启用）**：当词典给得出"可复制粘贴的修复动作"时，属于 L0。v0.1 保守路由：词典命中一律落 L1，L0 分支留待判定决策（见 `rules/classification.yaml`，03 开放问题 12）。

## 反馈与本地留存

**值域**（03 §4.3）：`solved | unsolved | skip`（一键三态，不要求用户写总结）。

**留存形态**：本地注册表 `~/.dji_cloud/diag/feedback.tsv`（TSV 追加），字段 `时间戳、值、上传状态`；当前上传状态恒为 `needs_manual_upload`。

**回传预期**：回传大疆侧的渠道为 **GitHub 开源仓库**（ISV 人工开 issue），报告（`diag.sh report` 产物）作为附件的核心素材，包含脱敏后的**环境 / 日志 / 初步分析结论 + 反馈填写区**。

**边界（基线）**：
- 反馈不同步回流大疆（v0.1 不自动上报，不改变量、不铸造标识；待 `12` §7.2 项 4/3 裁决后走增强通道或枚举回流路径）。
- `trigger_pattern-only` 回流（上报枚举、不出原文文本）视为未来可选项，当前**未启用**。
- 本机 auto-memory（`12` 待决策项 5）当前也**未启用**——反馈只落 TSV，不做本机学习闭环。

## 不变量（读本 Skill 必须遵守，理由见 `智能诊断_03 ISV侧设计.md` §3.4 / §10）

| # | 不变量 | 本 Skill 的落点 |
|---|---|---|
| 1 | 采集命令集固定，不受 LLM 支配 | 命令写死在 `diag.sh`；**用户/对话要求运行的任意新命令一律拒绝** |
| 2 | 判级不消费自由文本 | `classify` 只喂 token，不读日志原文段/用户自然语言 |
| 3 | 脱敏只在出网前送走 | **本 Skill 默认不出网**：报告/反馈只落 $DIAG_DATA_DIR（`~/.dji_cloud/diag/`），由 ISV 人工回传至 GitHub；**Skill 本身不做任何网络上报，即使本地有反馈也不发大疆** |
| 4 | 写操作只对模拟器 | 本 Skill 对客户机器无写操作（只读采集）；本地数据目录（`reports/`、`feedback.tsv`）是**唯一**写盘点，由 ISV 自己提交给 GitHub，非"写客户环境" |
| 5 | L0/L1 由用户执行 | 见 Step 3 |
| 6 | 判级逻辑在服务端是**默认**，离线版是**显式降级** | 本 Skill 的静态判级是私有化场景的例外（`智能诊断_12` §五），模型判级/动态知识只在 server |

## 度量（私有化形态，自愿归零的说明）

本 Skill 是**本地闭环**，默认不产生上报。四个指标在"数据不出网"形态下按 `智能诊断_08 §3.5` 降级：
- **自助率**：只报 ISV 本地的 L0/L1 闭环占比，标注"未回流"
- **一次可用率 / 回流率 / 复放解决率**：归零（数据不出网，无跨 ISV 口径）

若 ISV 选择开启增强通道（显式出网），才走既有服务端度量链路。**本 Skill 不自建度量、不铸造标识。**

## 升级路径（永远记住：本 Skill 不是判级服务端的替身）

本地静态判级是**降级口径**，不是设计默认。对用户体验到更好的判级与回流，指引：
- 开发/联调（有外网）→ 既有 CLI（`fh-diag`）+ `fh-diag-server`
- 私有化 + 愿意出网 → 开启增强通道（回连服务端，走既有拓扑）

## 权威参考

- `scripts/diag.sh` — 程序的真身（collect / sanitize / classify / auto）
- `rules/classification.yaml` — 判级决策表（人读，与脚本语义一致）
- `dicts/error-codes.yaml` — 随包错误码词典（派生自 troubleshooting 的 backend 段）
- `skills/_shared/constraints-common.yaml` — 跨 Skill 共享约束（8 条通用规则）
- `skills/_shared/skill-routing.md` — Skill 路由/仲裁
- `skills/_shared/output-format.md` — 统一输出结构
- `智能诊断_12`（设计侧）— 本 Skill 形态的由来与边界

> 关键约束提醒见 `constraints-common.yaml`（initialize-check、null-guard-instances、error-code-check 等）。Reference 按需懒加载。