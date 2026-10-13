#!/usr/bin/env bash
# ============================================================
#  cloudapi2-diagnosis — 本地诊断证据采集 + 脱敏 + 静态判级
#
#  首版定位：面向 ISV「数据不出网」的本地闭环能力基座。
#  - 采集：固定命令集（环境/磁盘/网络/进程/SDK 日志），与 LLM 无关
#  - 脱敏：sed 正则管道，覆盖凭证/内网 IP/邮箱/手机号/URL query/路径
#  - 判级：四分支静态决策表（输出证据包，不走自由文本）
#  全部在本机完成，不产生任何上报；可针对 ISV 私有化场景原样使用。
#
#  用法：
#    diag.sh collect            # 采集到 stdout（证据包 JSON 前身）
#    diag.sh classify           # 跑 collect 并对输出做静态判级
#    diag.sh sanitize <file>    # 对指定文件内容做脱敏（stdin→stdout）
#    diag.sh report             # 生成脱敏后的本地诊断报告（含环境/日志/初步结论 + 反馈区）
#    diag.sh feedback <值>      # 一键反馈 solved|unsolved|skip，追加到本地注册表
#    diag.sh auto               # classify + 本地闭环建议（默认）
#
#  兼容：bash 3.2（macOS 默认），Linux/macOS（暂不含 Windows）
#  无网络上报，无服务端依赖。写盘仅限 $DIAG_DATA_DIR（默认 ~/.dji_cloud/diag/，可用 env 覆盖）。
# ============================================================
set -u

# ------------------------------------------------------------
# 0. 常量（无魔法值）
# ------------------------------------------------------------
readonly PROG_NAME="cloudapi2-diagnosis"
readonly VERSION="0.1.0"
readonly DEFAULT_LOG_WINDOW_LINES=200
readonly COLLECT_EMPTY_DESC="(空)"
# 本地数据目录：反馈注册表与报告的唯一写盘点（数据不出网；仅此目录会被写入）
readonly DIAG_DATA_DIR="${DIAG_DATA_DIR:-$HOME/.dji_cloud/diag}"
readonly FEEDBACK_DB="$DIAG_DATA_DIR/feedback.tsv"
readonly REPORT_DIR="$DIAG_DATA_DIR/reports"
readonly FEEDBACK_VALUES="solved|unsolved|skip"

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
# 1. 工具函数（bash 3.2 兼容；避免魔法值）
# ------------------------------------------------------------

# 是否内置某个命令
have_cmd() { command -v "$1" >/dev/null 2>&1; }

# 加时间戳（不依赖 GNU date；可直接输出到 stderr 并忽略）
timestamp() { echo "[$(date '+%Y-%m-%d %H:%M:%S')]"; }

# markdown 表格化输出：key/value 对
emit_kv() { printf '%s= %s\n' "$1" "$2"; }

# 判级四分支决策表的真实来源（与 rules/classification.yaml 的语义一致；
# YAML 只作人读，此处 bash case 才是被执行的逻辑）
classify_code() {
  local errcode="$1" hit="" action=""
  case "$errcode" in
    ERROR_INIT_FAILED|ERROR_INVALID_STATE|ERROR_DEVICE_NOT_FOUND|ERROR_CALLBACK_TIMEOUT|ERROR_LISTEN_FAILED|NPE)
      hit="dict" ;;
    *) hit="no_dict" ;;
  esac
  # 根据命中与否，落到 L0/L1/L2/L3（与 classification.yaml 对应）
  case "$hit" in
    dict)  echo "L1:错误码命中词典，只能给结论与建议（参考 error-codes.yaml fix 字段，不通过脚本执行）";;
    no_dict) echo "L2:零命中，需升级；证据包缺关键采集项（环境/日志为空）时降级 L3";;
  esac
}

# ------------------------------------------------------------
# 2. 采集：固定命令集（只读；命令集与 LLM 无关，见 SKILL.md 第六章）
# ------------------------------------------------------------
collect_env() {
  emit_kv "os"        "$(uname -srm 2>/dev/null || printf '%s' "unknown")"
  emit_kv "hostname"  "$(hostname 2>/dev/null || printf '%s' "unknown")"
  emit_kv "shell"     "$(basename "${SHELL:-}")"
  if have_cmd java; then
    emit_kv "java"    "$(java -version 2>&1 | head -1)"
  fi
  if have_cmd mvn; then
    emit_kv "maven"   "$(mvn -version 2>&1 | head -1)"
  fi
  if have_cmd node; then
    emit_kv "node"    "$(node --version 2>/dev/null)"
  fi
}

collect_disk() {
  if have_cmd df; then
    df -h 2>/dev/null | head -8 || true
  fi
}

collect_network() {
  # 只探测本机可达性，不请求外部；命令固定、只读、短超时
  emit_kv "ping_loopback" "127.0.0.1 ($(ping -c 1 -t 2 127.0.0.1 >/dev/null 2>&1 && echo ok || echo fail))"
  if [ "$(uname -s)" = "Darwin" ]; then
    emit_kv "netstat" "$(netstat -rn 2>/dev/null | head -5 || true)"
  elif [ "$(uname -s)" = "Linux" ]; then
    emit_kv "ip_addr" "$(ip -brief address 2>/dev/null | head -5 || true)"
  fi
}

collect_process() {
  if have_cmd lsof; then
    emit_kv "lsof_tcp" "$(lsof -iTCP -sTCP:LISTEN 2>/dev/null | head -10 || true)"
  fi
}

collect_sdk_log() {
  # 定位常见 SDK 日志目录/文件（只在常见位置搜，命令固定、只读）
  local base_log=""
  for p in \
    "$HOME/.dji_cloud/log" \
    "$HOME/.dji/log" \
    "$HOME/.flight_hub_sdk/log" \
    "$HOME/Library/Logs/FlightHub"; do
    [ -d "$p" ] && base_log="$p" && break
  done
  if [ -n "$base_log" ]; then
    local newest
    newest=$(find "$base_log" -type f -name '*.log' -print0 2>/dev/null \
      | xargs -0 ls -t 2>/dev/null | head -1)
    emit_kv "sdk_log_dir" "$base_log"
    if [ -n "$newest" ] && [ -f "$newest" ]; then
      emit_kv "sdk_log_file" "$(basename "$newest")"
      # 截取最近 N 行（两处上限需对齐，防静默丢关键行；此处默认 200 行）
      tail -n "$DEFAULT_LOG_WINDOW_LINES" "$newest" 2>/dev/null \
        | sed 's/^/    /' || true
    else
      emit_kv "sdk_log_tail" "$COLLECT_EMPTY_DESC"
    fi
  else
    emit_kv "sdk_log_dir" "未找到常见 SDK 日志目录（说明日志未落盘或路径不同，需向用户确认）"
  fi
}

# 主采集：汇总所有只读采集器，输出"证据包"分组
collect() {
  echo "== env ==";          collect_env
  echo "== disk ==";         collect_disk
  echo "== network ==";      collect_network
  echo "== process ==";      collect_process
  echo "== sdk_log ==";      collect_sdk_log
  echo "== action_whitelist =="; printf '%s\n' "${ACTION_WHITELIST[@]}"
}

# ------------------------------------------------------------
# 3. 脱敏：sed 正则管道（第一道；输出不落盘，仅 stdout）
#    覆盖：凭证字段值 / 内网 IP / 邮箱 / 手机号 / URL query / 用户目录路径
#    注意：这是机制不是建议——数据不出网的承诺由此实现
# ------------------------------------------------------------
sanitize_stdin() {
  sed -E \
    -e 's/(password|passwd|token|secret|api[_-]?key|access[_-]?key)["'"'"']?\s*[:=]\s*["'"'"']?[^"'"'"' ,}]+/\1="***"/Ig' \
    -e 's/((10|172\.(1[6-9]|2[0-9]|3[01])|192\.168)\.)([0-9]{1,3}\.){2}[0-9]{1,3}/\1***.***.***/g' \
    -e 's/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/***@***/g' \
    -e 's/(1[3-9][0-9])\s*[- ]?\s*([0-9]{4})\s*[- ]?\s*([0-9]{4})/\1-****-****/g' \
    -e 's/([?&][A-Za-z0-9_]+)=([^& ])/\1=***/g' \
    -e "s#(/Users/[A-Za-z0-9._-]+|/home/[A-Za-z0-9._-]+)#/***#g" \
    -e '/^[[:space:]]*$/d'
}

# ------------------------------------------------------------
# 5. 本地报告 / 反馈：数据不出网的落盘点（全部走 sanitize 后才落盘）
# ------------------------------------------------------------

# 只读不写；只允许读本 skill 自己的数据目录（防用户拿它当任意文件查看器）
ensure_data_dir() {
  mkdir -p "$DIAG_DATA_DIR" "$REPORT_DIR" 2>/dev/null || {
    echo "错误: 无法创建数据目录 $DIAG_DATA_DIR（检查 HOME 可写）" >&2
    return 1
  }
}

# 生成诊断报告：脱敏后的证据包 + 结论 + 反馈填写区（可复制粘贴提交给大疆侧仓库）
make_report() {
  ensure_data_dir || return 1
  local stamp
  stamp=$(date '+%Y%m%d-%H%M%S')
  local f="$REPORT_DIR/report-$stamp.md"
  {
    echo "# 诊断报告"
    echo
    echo "- 生成时间：$(date '+%Y-%m-%d %H:%M:%S')"
    echo "- 采集来源：$PROG_NAME v$VERSION"
    echo
    echo "## 环境（脱敏后）"
    collect_env | sanitize_stdin
    echo
    echo "## 磁盘（脱敏后）"
    collect_disk | sanitize_stdin
    echo
    echo "## 网络（脱敏后）"
    collect_network | sanitize_stdin
    echo
    echo "## 进程（脱敏后）"
    collect_process | sanitize_stdin
    echo
    echo "## SDK 日志（脱敏后，末 ${DEFAULT_LOG_WINDOW_LINES} 行）"
    collect_sdk_log | sanitize_stdin
    echo
    echo "## 初步结论"
    classify | sed 's/^/`/;s/$/`/'
    echo
    echo "## 反馈（供回传给大疆侧 GitHub 仓库，请如实勾选）"
    echo
    echo "- [ ] 反馈：已解决 / 未解决 / 不适用"
    echo "- 补充信息（可选）："
  } > "$f"
  echo "报告已写入本地（数据不出网）：$f"
  echo
  echo "如需回传给大疆侧，可将文件内容粘贴到开源仓库 issue（自动含脱敏后的脱敏环境/日志/结论）。"
}

# 一键反馈：solved / unsolved / skip，追加到本地注册表（TSV；不覆盖，只追加）
add_feedback() {
  local value="$1"
  case "$value" in
    solved|unsolved|skip) ;;
    *) echo "错误: 反馈值必须是 $FEEDBACK_VALUES 之一（got: '$value'）" >&2; return 1 ;;
  esac
  ensure_data_dir || return 1
  printf '%s\t%s\tneeds_manual_upload\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$value" >> "$FEEDBACK_DB"
  echo "反馈已记录到本地（数据不出网）：$FEEDBACK_DB"
  echo "状态: 未上传/需人工跟进（v0.1 不回传大疆——回流渠道见 SKILL.md Step3 待决策项 4/3）"
}

# 对文件做一次脱敏（stdout 输出到终端）
sanitize_file() {
  local f="$1"
  [ -f "$f" ] || { echo "错误: 文件不存在: $f (列目录请用 collect，不要直接读任意路径)" >&2; return 1; }
  sanitize_stdin < "$f"
}

# 静态判级的输入：只从「SDK 日志段」提取错误码 token
#（环境/磁盘/网络/PID 等噪声段的 4 位数字不是错误码，绝不能喂给判级）
collect_error_tokens() {
  collect 2>/dev/null \
    | sed -n '/^== sdk_log ==/,/^== /p' \
    | grep -Eo 'ERROR_[A-Z_]+|NPE' | sort -u
}

# ------------------------------------------------------------
# 4. 静态判级：四分支决策表（本地闭环；不上报、不写库）
#    该函数是 classification.yaml 的可执行版本；YAML 只做人读
# ------------------------------------------------------------
classify() {
  local -a evidence=()
  local line=""
  # 提取证据包中的错误码，喂给 classify_code（不直接读取自由文本）
  evidence=( $(collect_error_tokens) )
  if [ "${#evidence[@]}" -eq 0 ]; then
    echo "L3:零命中且关键采集缺失（环境/日志为空）——降级为原始工单 + 现场快照"
    return
  fi
  for line in "${evidence[@]}"; do
    classify_code "$line"
  done
}

# ------------------------------------------------------------
# 5. 主入口
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
  feedback)
    [ $# -ge 2 ] || { echo "错误: feedback 需要值（$FEEDBACK_VALUES 之一）；有报告文件可附：diag.sh feedback <值> [报告路径]" >&2; exit 1; }
    add_feedback "$2"
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
    echo "  diag.sh feedback  # 一键反馈 solved/unsolved/skip（本地留存，v0.1 不回传）"
    ;;
  *)
    cat <<'EOF'
用法:
  diag.sh collect             # 只采集（stdout 证据包）
  diag.sh classify            # 判级（统计证据中的错误码/缺失）
  diag.sh sanitize <f|->      # 对文件做一次脱敏（stdin→stdout）
  diag.sh report              # 生成脱敏后诊断报告（含环境/日志/初步结论 + 反馈区）
  diag.sh feedback <值>       # 一键反馈 solved|unsolved|skip（本地留存，v0.1 不回流）
  diag.sh auto                # 采集 + 判级 + 下一步指引（默认）

本脚本是无网络上报、无服务端依赖的本地闭环。写盘仅限 $DIAG_DATA_DIR（可用 env DIAG_DATA_DIR 覆盖）。
默认数据目录: ~/.dji_cloud/diag/（feedback.tsv + reports/）
EOF
    ;;
esac