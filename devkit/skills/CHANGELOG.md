# Skills 变更日志

本文件记录 skills/ 目录下所有 cloudapi2-* Skill 的变更历史。

---

## [Unreleased]

### 全局变更

- **新增** `cloudapi2-feedback` Skill：从 troubleshooting 提取问题反馈闭环为独立入口（session 采集→脱敏→确认→issue 自动创建），无需错误码即可启动；共享 `diag.sh` 脚本，不拆分不复制；`skill-routing.md` 横切层/路由表/转交信号表同步更新；`feedback-route.sh` hook 路由目标从 troubleshooting 改为 feedback

- **新增** `cloudapi2-skeleton-deploy` Skill：FH Skeleton 三形态部署引导（普通机器/Docker/K8s），半自动交互 + 排障闭环

- **增强** `cloudapi2-skeleton-deploy` Skill：新增"清理部署"能力 — 三形态卸载引导（普通机器 systemd/nginx/Docker down[-v]/K8s namespace/PVC），先确认保留/清除数据后逐步删除命令；同步在 `skeleton/docs/部署指南.md` 新增 §9 清理部署章节、references 新增 `清理部署.md`、constraints 新增 `cleanup-confirm-data-keep`

- **升级** FlightHub PaaS SDK 3.0.0 → 3.1.5（`knowledge-ingest` SKILL.md 的 `sdk_version` 同步更新）。SDK 为向后兼容升级：核心模块仅新增监听接口（live status / dongle infos / flysafe db version / mode code reason / wpmz version / air conditioner mode / camera watermark / psdk ui resource / offline map enable / commander flight mode 等），无删除。`sdk-core` 规则中的类名/方法名无需变更。

### cloudapi2-feedback

- **增强** 反馈流程从 8 步（0-7）扩展为 9 步（0-8）：Step 1 拆分为"问题描述+复现+截图"和"基本信息"两个独立对话步骤，减少单次对话信息负担
- **修复** label 名称与 GitHub 仓库实际标签对齐：`bug` → `type/bug`、`enhancement` → `type/enhancement`、`question` → `type/question`，新增 `type/other`
- **增强** 反馈流程从 5 步（0-4）重写为 8 步（0-7）：确认类型 → 描述+复现+截图 → 目标+时间范围 → 采集+总览 → 脱敏+确认 → 补充材料+打包 → 上传网盘 → 创建 issue（含 label）
- **新增** 截图提示：Step 1 主动引导用户上传截图，后续 `session pack` 一起打包
- **新增** 复现步骤前置：从 issue 前移到问题描述阶段（Step 1），趁用户记忆最清晰
- **新增** 日志时间范围选择（Step 2）：当前/10 分钟/1 小时/自定义（最大 24 小时），通过 `log_since_minutes` 元数据字段传递
- **增强** 采集总览输出（Step 3）：`collect --no-zip` 采集后输出各服务日志文件名、大小、角色，并展示 session 目录路径
- **新增** 补充材料 + 打包步骤（Step 5）：`session pack <id> [extra_files...]` 独立打包，支持追加截图/HAR/配置文件等附件
- **新增** 上传网盘步骤（Step 6）：打包后引导用户上传，支持 scp/WebDAV 等方式
- **新增** issue label 支持（Step 7）：问题→`bug`、需求→`enhancement`、咨询→`question`，通过 `--label` 参数或 meta `label` 字段传递
- **增强** issue 创建移到最后一步（Step 7）：确保上传链接写入 issue body

### cloudapi2-troubleshooting

- **合并** diagnosis → troubleshooting（v2.0.0）：单一入口，collect/sanitize/classify/report/feedback/auto 迁入 `scripts/diag.sh`
- **词典收敛**：`error-dict.yaml` → `error-codes.yaml`（唯一真值源，frontend/backend/设备侧全量）
- **出网控制点**：`report` 产物 + 用户决策才发大疆；`feedback` 恒 `needs_manual_upload`
- **不变量改写**：数据"默认不出网 + 用户决策出网"
- **新增** `rules/classification.yaml`（判级决策表，人读）
- **新增 Stage 2.5 实机验证（v2.1.0）**：RCA 流水线插入按需阶段 `Reproduce→Locate→[2.5 实机验证]→Fix→Verify`，用 MCP + 模拟器把假设证实/证伪；只读快照优先、`confirm` 前先 `dry_run`、控制权必释放、不一致回 Stage 2
- **新增** `references/live-verification.md`（前置链、验证配方、8 条已知陷阱对照表）
- **新增** `references/platform-error-response.md`（平台 HTTP 响应 4 种形态、双错误码解构、`2XXYYYY` 域前缀表、报障必附清单）
- **新增** `references/event-callback-checklist.md`（`listenXxx` 收不到回调 5 步排查：事件语义混淆 / 订阅 SN 陷阱 / 平台已知缺失 / 上游触发 / 生命周期自检）
- **词典扩充**：`error-codes.yaml` 增加 `platform_error_domains`（12 个服务域）与 `platform_response`（8 条平台通用框架码），置于 `frontend:` 之前以不影响 `test_diag.sh` 的 backend 段一致性校验
- 以上内容从内部 `issue-analyzer` skill 提炼，**已剥离全部内部专有材料**（Jira 流程、网关路由分析、内部仓库路径、SSH 凭证）
- **强化排障行为护栏（v2.2.0）**：Stage 1 增加「最近变更」与「频率」两项必收信息（偶现 ≈ 时序问题，走不同排查路径）；Stage 2 增加**边界埋点取证**（5 个组件边界跑一次看证据在哪层断，替代逐层猜）与**反向溯源**（追坏值到首次赋值处，修在源头不修在报错点）；Stage 2.5 增加「一次只验一个假设、只动一个变量」；Stage 3 增加「一次一个修复」「修复没生效回 Stage 2」与**三振出局**（修 3 次不好停手质疑前提，指向平台侧则走升级不再改代码）
- **新增「反模式自查」段**：8 条合理化念头对照表 + 3 条用户信号（"别猜了"/"你确认过吗"/"这样能看到吗"）→ 触发即回 Stage 2
- **扩写** `references/rca-methodology.md`：最近变更清单（5 类变更源查法）、频率→排查策略表、5 边界埋点取证表与埋点纪律（凭证必遮蔽）、反向溯源常见坏值源头、时序类问题的条件等待写法（CountDownLatch + 超时，替代固定 sleep）、三振升级、修复后加固
- 以上行为护栏借鉴 `superpowers:systematic-debugging` 的方法论，**已全部改写为 FlightHub 领域语言**（SDK/平台/设备边界、MCP 只读快照、平台能力缺失）；未引入其外部 skill 依赖与通用示例代码
- **新增 `diag.sh issue [报告路径]` 子命令（v2.3.0）**：解析本地脱敏报告，生成 GitHub new-issue 预填 URL（template/title/body），用户点击=出网决策、粘贴全文=完整出网；纯 bash urlencode（RFC 3986，bash 3.2 兼容）；`DIAG_ISSUE_REPO` 环境变量可覆盖仓库地址
- **新增 `.github/ISSUE_TEMPLATE/diagnostic-report.md`**：诊断报告回传模板（判级/环境/全文/补充信息/敏感自查清单）
- **修复 `feedback` 未实现的第 3 参数提示**：TSV 增至 4 列（追加报告文件名），路径非法时记 `-` 并告警；反馈后联动打印 issue 链接
- **出网通道收紧**：issue URL body 仅含已脱敏元数据，显式排除 hostname（脱敏管道不遮蔽 hostname 的已知缺口）；版本对齐 SKILL.md 2.2.0 / diag.sh 2.1.0 → 2.3.0
- **模板迁移 `.yml` 表单**：`diagnostic-report.md` → `5-诊断报告-diagnostic.yml`（Issue Forms 结构化字段：企业名称/判级/错误码/严重程度/SDK 版本/环境/报告全文/自查），同步搬入参考模板全套（bug/feature/question/other + `config.yml`）；`run_issue()` URL 仅携带 `?template=&title=`，元数据改为终端打印由用户复制
- **新增 `diag.sh trace <trace_id>` 精准采集（v2.4.0）**：从盲采 200 行日志升级为按 trace_id grep 所有已知日志路径（SDK + 应用），±5 行上下文，500 行截断，经脱敏管道输出；`SDK_LOG_PATHS`/`APP_LOG_PATHS` 提取为共享常量
- **新增 `sanitize_with_stats()` 脱敏统计**：第一遍 `grep -c` 统计 6 类敏感模式命中行数（credential/ipv4/email/phone_cn/url_query/user_path），第二遍 `sanitize_stdin` 替换；stdout=脱敏文本，stderr=统计行
- **新增 `diag.sh session` 结构化反馈生命周期**：7 个子命令（start/set/collect/preview/confirm/issue/status）驱动状态机 `created→collected→previewed→confirmed→issued`；跳步强制拦截；`session preview` 集成 `sanitize_with_stats` 脱敏预览 + 统计；`session issue` 直接从 meta 构造 URL，不依赖报告文件解析；数据目录 `sessions/<id>/`（session.meta + evidence.txt + sanitized.md + stats.txt）
- **补反馈流程触发入口（v2.4.1）**：SKILL.md frontmatter `description` 增补中文触发词 `反馈 / 我要反馈 / 反馈问题 / 反馈一下 / 提个 issue / 报个问题 / 提交问题`（此前只有英文 `feedback` + `issue 提交` + `提交 GitHub`，ISV 说"我要反馈一个问题"匹配不上）；「何时触发」新增「用户要求反馈问题 / 提 issue」一条并显式标注**无错误码也触发**；新增「无错误码的冷启动入口」小节（`session start → set error_text=用户原话 → collect → …`，缺字段留空不卡流程）；`skill-routing.md` 横切层说明与两张路由表同步补该信号（含"不要当成需求开发转交垂直层"）
- **修复 `session start` 同秒撞名覆盖（v2.4.1）**：session ID 原为 `date +%Y%m%d-%H%M%S`，同一秒内连续 `start`（冷启动入口下用户连报两个问题很容易触发）会命中同名目录，前一个 session 的 `session.meta` 被直接覆盖 —— 已采集的证据与状态全部丢失。改为撞名时追加序号后缀（`20260917-110523-1`），`test_diag.sh` 新增回归用例（先建的 session 必须存活），无修复时该用例失败
- **修复 `trace` 命中仍返回 1（v2.4.2）**：`collect_by_trace()` 的每文件循环被包在 `{ ... } | sanitize_stdin` 管道里，管道左侧命令组在子 shell 中执行，循环内对 `found` 的赋值传不回父 shell —— 只要有命中（哪怕命中的正是要找的 trace_id）函数一律 `return 1`。改为逐文件 `for` 循环、每个 chunk 单独过脱敏管道，命中计数得以回传。此前 `test_diag.sh` 只断言「输出含 trace_id」未断言退出码，故假阴性长期未被发现；新增退出码断言，无修复时失败
- **修复 `session collect` 把词典外错误码判成 L1（v2.4.2）**：判级分支用「错误码非空」当作「词典命中」，导致任何非空错误码（含词典里根本不存在的）都被判 L1 —— 而 L1 的语义是"有明确线索可直接定位"，会把未收录的错误码伪装成已定位问题，误导后续排查方向。抽出 `is_dict_hit()` 作为词典唯一裁决点，`classify_code()` 与该分支共用，词典外错误码正确落到 L2（零命中）。新增隔离回归用例
- **修复 `session set` 换行注入可伪造状态（v2.4.2）**：`session.meta` 是单行 `key=value` 格式，值里带换行会凭空多出一整行新 key —— `error_text=x\nstate=issued` 可把状态机直接推到 `issued`，绕过 `session_require` 的跳步拦截。新增 `session_value_invalid()` 双层校验（`session set` 拒绝 + `session_put` 兜底），拒绝含换行或超 1000 字符的值并显式报错（`error_text` 对外承诺"原样记录"，静默折行会破坏证据保真）
- **修复 issue 出网通道三处不一致（v2.4.2）**：① `run_issue()` / `run_session_issue()` 的 `template` 参数直接拼原始中文模板名，未做 URL 编码，URL 被截断且 GitHub 匹配不上模板，改用 `urlencode`；② issue 表单 `required: true` 的「企业名称」从未被打印，用户到表单必然卡住，`session issue` 补打印 `company`/`error_text`/`severity`，并对缺失必填项输出 stderr 提示；③ 文档与表单的 `severity` 取值不一致（文档写 `P1`/`P2`，表单下拉实际是 `致命`/`严重`/`一般`/`提示`），值不在选项内等于填不上 —— `session set` 增加选项校验并提示可选值，SKILL.md/README.md 示例同步改为真实选项
- **新增 `references/replay-media-troubleshooting.md`（v2.4.3）**：回放页/媒体页「列表与缩略图正常、点开照片全黑、视频进度条停在 `0:00`、下载无反应」专项清单 —— 根因是媒体取流地址指向对象存储内网端口 `:30802`、客户网段不可达（存储本身健康），ISV 侧无法修复（回放页媒体面板由 FH2 托管的 `FlyDataReplay` UMD 自行取流，返回的 `url` 原样赋给 `<img>/<video>`，无改写钩子）。含症状分流表（与直播黑屏 `212015`、列表接口故障区分）、两条定案命令（对预签名 URL 发 `Range: bytes=0-1023` 期望 `206 Partial Content` 证明存储健康；客户网段 `nc -vz <存储主机> 30802` 不通证明可达性是缺陷）、平台侧两种修复方向、以及 skeleton 媒体页代理端点的对照。SKILL.md Stage 2 Locate 专项清单与权威参考两处同步挂载
- **「何时触发」增加域判定前置说明（v2.4.4）**：与内部 `issue-analyzer` skill 的触发词高度重叠（"报错/bug/排查/debug"两边通用），单靠动词容易误判。新增一句前置规则——本 Skill 只处理 SDK/ISV 集成侧问题（SDK 方法、错误码、`fh_*`、ISV 自己的接入代码），调用方业务系统内部代码的问题不在范围内；同步把「不触发」的最后一条从"与 DJI SDK 无关的 bug"细化为举例说明
- **增强** `diag.sh` v2.7.0：`session collect --no-zip` 采集与打包分离（不带 flag 行为不变，向后兼容）；新增 `session pack <id> [extra_files...]` 独立打包子命令（支持追加附件文件）；`session issue --label <label>` label 支持（也可通过 meta `label` 字段）；`log_since_minutes` 元数据控制 docker logs `--since` 和 Loki 查询时间范围；`session_print_summary()` 采集完成后输出结构化总览（服务日志名称/大小/角色/路径）；移除 `session issue` 创建成功后的截图提示框（截图已在反馈流程 Step 1 引导收集并打入 zip 包）

### cloudapi2-frontend-component

- **新增约束 `media-url-must-not-be-presigned-direct`（v2.0.1，`level: critical`）**：照片预览 / 视频播放 / 下载的地址必须指向本服务的代理端点，禁止把 SDK 或平台接口返回的对象存储预签名 URL（`http://<主机>:30802/<桶>/...?X-Amz-*`）直接赋给 `<img src>` / `<video src>` 或下载链接 —— 该端口客户网段不可达，表现为列表正常但点开全黑、进度条停 `0:00`
- **新增 `references/troubleshooting.md` 第五节 FAQ 条目（v2.0.1）**：`FlyDataReplay` 媒体面板点开全黑 / 视频停在 `0:00` 的原因说明（列表走网关 `:30812` 通、取流直连 `:30802` 不通）、ISV 侧为何改不掉、以及媒体面板默认关闭（`playerBarBtn.media` / `media_list` 默认 `false`）这一前置确认点；指向 troubleshooting Skill 的完整排查清单

### cloudapi2-diagnosis

- **弃用（v2.0.0 起）**：能力并入 troubleshooting；目录保留作历史，SKILL.md 增加弃用声明

## [1.1.0] — 2026-08-04

### 全局变更（所有 Skill 受影响）

- **新增** `_shared/constraints-common.yaml`：跨 Skill 共享约束的 Single Source of Truth（8 条规则）
- **新增** `_shared/skill-routing.md`：多 Skill 同时匹配时的仲裁、组合和转交规则
- **新增** `_shared/output-format.md`：统一输出格式标准（约束提醒段、引用来源、置信度信号、Reference 加载策略）
- **变更** 各 Skill constraints.yaml 通过 `extends: _shared/constraints-common.yaml` 引用共享约束，消除重复定义

### cloudapi2-sdk-core

- **新增** 规则 6：危险操作警告 — `takeoff`/`returnHome`/`emergencyStop`/`flyToPoint` 生成代码前必须输出结构化风险警告
- **变更** "5 条核心规则" → "6 条核心规则"
- **变更** `prompts/add-flight-control.md` 安全警告升级为输出前置条件

### cloudapi2-device-management

- **变更** constraints.yaml 移除 5 条与 constraints-common.yaml 重复的规则，保留 6 条领域特有规则

### cloudapi2-sei-osd

- **变更** constraints.yaml 移除 2 条与 constraints-common.yaml 重复的规则，保留 10 条领域特有规则

### cloudapi2-frontend-component

- **变更** constraints.yaml 添加 `extends` 声明（16 条规则均为领域特有，无删减）

### cloudapi2-mcp-guide

- **变更** 决策树 TODO 分支添加临时引导说明

### cloudapi2-troubleshooting

- **变更** Memory 路径引用对齐 Claude Code auto memory 机制

---

## [1.0.1] — 2026-07-28

### cloudapi2-sdk-core

- 初始发布后的小幅修正

---

## [1.0.0] — 2026-07-20

- 6 个 cloudapi2-* Skill 首次发布
- `_shared/sop-template.md` 7 章节 SOP + 3 档执行深度
