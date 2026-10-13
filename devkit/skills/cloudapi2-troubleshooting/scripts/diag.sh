#!/usr/bin/env bash
# ============================================================
#  cloudapi2-troubleshooting — 本地诊断证据采集 + 脱敏 + 静态判级 + session 生命周期 + issue 链接（v3.0.0 多文件架构）
#
#  首版定位：面向 ISV「数据不出网」的本地闭环能力基座。
#  - 采集：固定命令集（环境/磁盘/网络/进程/SDK 日志），与 LLM 无关
#  - 脱敏：Python 优先（pylib/sanitize.py），sed/awk fallback（lib/sanitize.sh）
#  - 判级：四分支静态决策表（输出证据包，不走自由文本）
#  全部在本机完成，不产生任何上报；可针对 ISV 私有化场景原样使用。
#
#  架构：本文件仅含常量 + 模块加载 + CLI dispatch。
#  业务逻辑拆分到 lib/ (6 个 shell 模块) 和 pylib/ (2 个 Python 模块)。
#
#  用法：
#    diag.sh collect            # 采集到 stdout（证据包 JSON 前身）
#    diag.sh classify           # 跑 collect 并对输出做静态判级
#    diag.sh sanitize <file>    # 对指定文件内容做脱敏（stdin→stdout）
#    diag.sh report             # 生成脱敏后的本地诊断报告（含环境/日志/初步结论 + 反馈区）
#    diag.sh issue [报告路径]   # 生成 GitHub issue 提交链接（预填标题/正文；点击=出网决策）
#    diag.sh feedback <值> [报告路径]  # 一键反馈 solved|unsolved|skip，可附报告文件名
#    diag.sh trace <trace_id>   # 按 trace_id 精准采集（grep 所有已知日志路径，±5 行上下文）
#    diag.sh session <subcmd>   # 结构化反馈 session（start/set/collect/preview/confirm/issue/status）
#    diag.sh auto               # classify + 本地闭环建议（默认）
#
#  兼容：bash 3.2（macOS 默认），Linux/macOS（暂不含 Windows）
#  无网络上报，无服务端依赖。写盘仅限 $DIAG_DATA_DIR（默认 ~/.dji_cloud/diag/，可用 env 覆盖）。
# ============================================================
set -u

# 脚本所在目录（source 和 python3 调用的基准路径）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"

# ------------------------------------------------------------
# 0. 常量（无魔法值）
# ------------------------------------------------------------
readonly PROG_NAME="cloudapi2-troubleshooting"
readonly VERSION="3.0.0"
readonly DEFAULT_LOG_WINDOW_LINES=200
readonly COLLECT_EMPTY_DESC="(空)"
# 本地数据目录：反馈注册表与报告的唯一写盘点（数据不出网；仅此目录会被写入）
readonly DIAG_DATA_DIR="${DIAG_DATA_DIR:-$HOME/.dji_cloud/diag}"
readonly FEEDBACK_DB="$DIAG_DATA_DIR/feedback.tsv"
readonly REPORT_DIR="$DIAG_DATA_DIR/reports"
readonly FEEDBACK_VALUES="solved|unsolved|skip"
# issue 仓库地址（环境变量可覆盖）
readonly DEFAULT_ISSUE_REPO="https://github.com/dji-sdk/Cloud-API-2"
readonly ISSUE_REPO="${DIAG_ISSUE_REPO:-$DEFAULT_ISSUE_REPO}"
readonly ISSUE_TEMPLATE_NAME="1-问题-bug.yml"
# SDK 日志搜索路径（collect_sdk_log / collect_by_trace 共用；只在常见位置搜，命令固定、只读）
readonly SDK_LOG_PATHS=(
  "$HOME/.dji_cloud/log"
  "$HOME/.dji/log"
  "$HOME/.flight_hub_sdk/log"
  "$HOME/Library/Logs/FlightHub"
)
readonly APP_LOG_PATHS=(
  "./logs"
  "./log"
  "./target/logs"
)
readonly TRACE_CONTEXT_LINES=5
readonly TRACE_MAX_LINES=500
readonly SESSION_DIR="$DIAG_DATA_DIR/sessions"
readonly SESSION_ALLOWED_KEYS="company error_text trace_id severity contact sdk_version problem_scope frequency cloud_version remote_host docker_container repro_steps log_links log_since_minutes label"
# 与 .github/ISSUE_TEMPLATE/1-问题-bug.yml 的下拉选项严格一致
readonly SEVERITY_ALLOWED_VALUES="致命 严重 一般 提示"
readonly FREQUENCY_ALLOWED_VALUES="always sometimes random unable_to_reproduce"
# 表单 required: true 的字段，session issue 打印时若为空要显式提示
readonly ISSUE_REQUIRED_KEYS="company severity frequency"
# session.meta 是单行 key=value 格式，值里的换行会伪造出一行新的 key（如注入 state=issued）
readonly SESSION_VALUE_MAX_LEN=1000
readonly DEFAULT_UPLOAD_URL=""
readonly UPLOAD_URL="${DIAG_UPLOAD_URL:-$DEFAULT_UPLOAD_URL}"
readonly MAX_ISSUE_BODY=65000
readonly DEFAULT_DOCKER_LOG_LINES=500
# Loki 日志查询（远程采集时自动探测，有 Loki 则优先走 API 而非逐容器 docker logs）
readonly LOKI_PROBE_PORTS="3100 30828"
readonly LOKI_QUERY_LIMIT=500
# DIAG_LOG_SINCE_MINUTES 可通过环境变量或 session set log_since_minutes 覆盖（默认 60 分钟）
LOKI_TIME_RANGE_MINUTES="${DIAG_LOG_SINCE_MINUTES:-60}"
readonly MAX_CONTAINERS="${DIAG_MAX_CONTAINERS:-10}"
readonly SESSION_MIN_INTERVAL_SEC="${DIAG_SESSION_MIN_INTERVAL:-600}"
readonly SESSION_MAX_ISSUES_PER_ISV="${DIAG_MAX_ISSUES_PER_ISV:-20}"
readonly MAX_PACKAGE_SIZE_MB="${DIAG_MAX_PACKAGE_SIZE_MB:-200}"
readonly MAX_PACKAGE_SIZE_HARD_MB=500
readonly SESSION_TTL_DAYS="${DIAG_SESSION_TTL_DAYS:-30}"
# 命令白名单：L0 阶段允许给出的修复动作，防止越权；采集命令集固定，不由 LLM 支配
readonly ACTION_WHITELIST=(
  "echo '请根据官方文档核对 SDK 版本与接口签名'"
  "grep -n 调用方式"
  "访问官方文档核对初始化参数"
)
readonly COLLECT_READONLY_CMDS=(
  "env -i sh -c 'command -v uname; command -v df; command -v ping; command -v nc; command -v lsof; command -v grep'"
)

# ------------------------------------------------------------
# 1. 加载模块（顺序有依赖：util → classify → sanitize → collect → issue → session）
# ------------------------------------------------------------
source "${SCRIPT_DIR}/lib/util.sh"
source "${SCRIPT_DIR}/lib/classify.sh"
source "${SCRIPT_DIR}/lib/sanitize.sh"
source "${SCRIPT_DIR}/lib/collect.sh"
source "${SCRIPT_DIR}/lib/issue.sh"
source "${SCRIPT_DIR}/lib/session.sh"

# ------------------------------------------------------------
# 2. 主入口
# ------------------------------------------------------------
case "${1:-auto}" in
  collect)  collect ;;
  classify) classify ;;
  sanitize) shift
    if [ "${1:-}" = "-" ]; then
      sanitize_stdin
    else
      sanitize_file "${1:-}"
    fi
    ;;
  report)
    make_report
    ;;
  issue)
    shift
    run_issue "${1:-}"
    ;;
  feedback)
    [ $# -ge 2 ] || { echo "错误: feedback 需要值（$FEEDBACK_VALUES 之一）；有报告文件可附：diag.sh feedback <值> [报告路径]" >&2; exit 1; }
    add_feedback "$2" "${3:-}"
    ;;
  trace)
    shift
    run_trace "${1:-}"
    ;;
  session)
    shift
    run_session "$@"
    ;;
  cleanup)
    days="$SESSION_TTL_DAYS"
    while [ $# -gt 0 ]; do
      case "$1" in
        --days) shift; days="${1:-$SESSION_TTL_DAYS}" ;;
      esac
      shift
    done
    cleanup_sessions "$days"
    ;;
  auto)
    echo "== 证据包 =="
    collect
    echo
    echo "== 静态判级 =="
    classify
    echo
    echo "== 下一步 =="
    echo "  diag.sh report    # 生成脱敏报告（含反馈区，供回传大疆侧）"
    echo "  diag.sh issue     # 生成 GitHub issue 提交链接（预填标题与短摘要；点击=出网决策）"
    echo "  diag.sh feedback  # 一键反馈 solved/unsolved/skip（本地留存，当前版本不自动回传）"
    ;;
  *)
    cat <<'EOF'
用法:
  diag.sh collect             # 只采集（stdout 证据包）
  diag.sh classify            # 判级（统计证据中的错误码/缺失）
  diag.sh sanitize <f|->      # 对文件做一次脱敏（stdin→stdout）
  diag.sh report              # 生成脱敏后诊断报告（含环境/日志/初步结论 + 反馈区）
  diag.sh issue [报告路径]    # 生成 GitHub issue 提交链接（缺省取最新报告；点击=出网决策）
  diag.sh feedback <值> [报告路径]  # 一键反馈 solved|unsolved|skip，可附报告文件名（本地留存，不自动回流）
  diag.sh trace <trace_id>      # 按 trace_id 精准采集（grep 所有已知日志路径，±5 行上下文，经脱敏）
  diag.sh session start        # 新建反馈 session（返回 session ID）
  diag.sh session set <id> key=value ...  # 设置元数据（error_text/trace_id/severity/contact/sdk_version）
  diag.sh session collect <id> # 采集证据 + 判级（→ collected）
  diag.sh session preview <id> # 脱敏预览 + 命中统计（→ previewed）
  diag.sh session confirm <id> # 确认无敏感信息（→ confirmed）
  diag.sh session issue <id>   # 生成 issue 链接（→ issued）
  diag.sh session status [id]  # 查看 session 状态
  diag.sh cleanup [--days N]   # 清理过期 session（默认 30 天）
  diag.sh auto                # 采集 + 判级 + 下一步指引（默认）

本脚本是无网络上报、无服务端依赖的本地闭环。写盘仅限 $DIAG_DATA_DIR（可用 env DIAG_DATA_DIR 覆盖）。
默认数据目录: ~/.dji_cloud/diag/（sessions/ + reports/ + feedback.tsv）
EOF
    ;;
esac
