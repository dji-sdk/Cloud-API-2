# lib/issue.sh — trace 采集 + URL 编码 + issue 生成 + 报告 + 反馈

collect_by_trace() {
  local trace_id="$1"
  [ -n "$trace_id" ] || { echo "错误: trace_id 不能为空" >&2; return 1; }
  local total_lines=0 found=0 truncated=0
  local all_paths=( "${SDK_LOG_PATHS[@]}" "${APP_LOG_PATHS[@]}" )
  local files=()
  # 收集所有候选日志文件
  for p in "${all_paths[@]}"; do
    if [ -d "$p" ]; then
      while IFS= read -r f; do
        files+=("$f")
      done < <(find "$p" -type f -name '*.log' 2>/dev/null)
    elif [ -f "$p" ]; then
      files+=("$p")
    fi
  done
  if [ "${#files[@]}" -eq 0 ]; then
    echo "trace_id '$trace_id' 在已知日志路径中未找到任何日志文件。" >&2
    return 1
  fi
  # 逐文件处理、每个 chunk 单独过脱敏管道，而不是把整个循环写成 `{ ... } | sanitize_stdin`：
  # 管道左侧的命令组在子 shell 里执行，循环内对 found 的赋值父 shell 读不到，
  # 会让「命中」与「未命中」都落到下面的 return 1
  for f in "${files[@]}"; do
    [ "$truncated" -eq 1 ] && break
    grep -Fn "$trace_id" "$f" >/dev/null 2>&1 || continue
    found=$((found + 1))
    echo "== $f =="
    local chunk=""
    chunk=$(grep -Fn -B"$TRACE_CONTEXT_LINES" -A"$TRACE_CONTEXT_LINES" "$trace_id" "$f" 2>/dev/null || true)
    local chunk_lines=0
    chunk_lines=$(printf '%s\n' "$chunk" | wc -l | tr -d ' ')
    if [ $((total_lines + chunk_lines)) -gt "$TRACE_MAX_LINES" ]; then
      local remain=$((TRACE_MAX_LINES - total_lines))
      printf '%s\n' "$chunk" | head -n "$remain" | sanitize_stdin
      total_lines=$TRACE_MAX_LINES
      truncated=1
      echo "命中行数超过 ${TRACE_MAX_LINES} 行，已截断。可缩小 trace_id 范围或指定日志目录。" >&2
    else
      printf '%s\n' "$chunk" | sanitize_stdin
      total_lines=$((total_lines + chunk_lines))
    fi
    echo
  done
  if [ "$found" -eq 0 ]; then
    echo "trace_id '$trace_id' 在已知日志路径中未找到匹配。检查 trace_id 是否正确，或日志是否已轮转清除。" >&2
    return 1
  fi
  return 0
}

run_trace() {
  local trace_id="${1:-}"
  if [ -z "$trace_id" ]; then
    echo "错误: 用法 diag.sh trace <trace_id>" >&2
    return 1
  fi
  collect_by_trace "$trace_id"
}

# ------------------------------------------------------------
# 4. URL 编码：纯 bash 字节级 %XX（bash 3.2 兼容，零外部依赖）
#    RFC 3986：unreserved（A-Za-z0-9.~_-）直通，其余一律 %XX。
#    不用 curl --data-urlencode：issue 命令自身必须零出网、零依赖。
# ------------------------------------------------------------
urlencode() {
  local rest="$1" out="" ch
  local LC_ALL=C          # 按字节迭代——中文不被拆坏、码点判断不受 locale 影响
  while [ -n "$rest" ]; do
    ch=${rest:0:1}; rest=${rest:1}
    case "$ch" in
      [A-Za-z0-9.~_-]) out="$out$ch" ;;            # RFC 3986 unreserved 直通
      *) out="$out$(printf '%%%02X' "$(( $(printf '%d' "'$ch") & 255 ))")" ;;  # 其余一律 %XX（& 255 防符号扩展）
    esac
  done
  printf '%s' "$out"
}

# ------------------------------------------------------------
# 4.5 issue 链接生成：解析本地脱敏报告 → 预填 new-issue URL
#    URL body 仅含已脱敏元数据（白名单 6 行）；报告全文由用户手动粘贴。
#    打开链接 = 第一次出网决策（元数据出网）；粘贴全文 = 完整出网。
# ------------------------------------------------------------

# 报告路径去掉 $HOME 前缀做 ~ 相对化（URL 不携带用户名）
tilde_path() {
  local p="$1"
  case "$p" in
    "$HOME"/*) printf '~%s' "${p#"$HOME"}" ;;
    *)         printf '%s' "$p" ;;
  esac
}

# ------------------------------------------------------------
# 4.2 issue 表单字段：severity 下拉选项校验 + 必填字段来源提示
#     模板 .github/ISSUE_TEMPLATE/5-诊断报告-diagnostic.yml 里
#     company / severity 是 required，诊断侧必须保证这两项有值可复制
# ------------------------------------------------------------

is_severity_valid() {
  case " $SEVERITY_ALLOWED_VALUES " in
    *" $1 "*) return 0 ;;
    *) return 1 ;;
  esac
}

# 表单字段无值时的占位提示：空值直接打印会让用户以为「不用填」
issue_field_value() {
  [ -n "$1" ] && printf '%s' "$1" || printf '（未设置，表单必填）'
}

# 必填项缺失时显式告警：不阻断出链接（用户可先去表单手填），但要说清楚缺什么
issue_required_warn() {
  local missing="" key="" val=""
  for key in $ISSUE_REQUIRED_KEYS; do
    case "$key" in
      company)   val="$1" ;;
      severity)  val="$2" ;;
      frequency) val="$3" ;;
      *)         val="" ;;
    esac
    [ -n "$val" ] || missing="$missing $key"
  done
  [ -n "$missing" ] || return 0
  echo "提示：issue 表单必填项缺失 →${missing}，请在提交前补上（severity 可选 ${SEVERITY_ALLOWED_VALUES}；frequency 可选 ${FREQUENCY_ALLOWED_VALUES}）。" >&2
  return 0
}

# 从报告文件推导 issue 标题：[diag] Lx <主码> (日期)
# 判级行 = 「## 初步结论」节首个非空行（跳过节标题行本身），去掉首尾反引号；
# 识别不出按 L2（保守升级）
derive_issue_title() {
  local report="$1"
  local level_line="" level="" codes="" first="" second="" n=0 date="" title=""
  level_line=$(sed -n '/^## 初步结论/,$p' "$report" \
    | tail -n +2 \
    | grep -m1 -v '^[[:space:]]*$' \
    | sed -e 's/^[[:space:]]*`//' -e 's/`[[:space:]]*$//')
  case "$level_line" in
    L1*|L2*|L3*) level="${level_line:0:2}" ;;
    *)           level="L2" ;;
  esac
  # 日期取报告头「- 生成时间：YYYY-MM-DD」；缺失或格式异常回退当天
  # （sed -n s///p：只有成功替换才输出，避免异常行整行漏进标题）
  date=$(grep -m1 '^- 生成时间：' "$report" | sed -n 's/^- 生成时间：\([0-9]\{4\}-[0-9]\{2\}-[0-9]\{2\}\).*/\1/p')
  [ -n "$date" ] || date=$(date '+%F')
  codes=$(grep -oE 'ERROR_[A-Z_]+|NPE' "$report" | sort -u)
  first=$(printf '%s\n' "$codes" | sed -n '1p')
  second=$(printf '%s\n' "$codes" | sed -n '2p')
  n=$(printf '%s\n' "$codes" | grep -c .)
  case "$level" in
    L1)
      if [ "$n" -gt 2 ]; then
        title="[diag] L1 ${first}+${second} 等${n}个 ($date)"
      elif [ -n "$second" ]; then
        title="[diag] L1 ${first}+${second} ($date)"
      elif [ -n "$first" ]; then
        title="[diag] L1 ${first} ($date)"
      else
        title="[diag] L1 无错误码 ($date)"
      fi
      ;;
    L2) title="[diag] L2 零命中-需人工RCA ($date)" ;;
    L3) title="[diag] L3 采集缺失-需现场快照 ($date)" ;;
  esac
  printf '%s' "$title"
}

# 拼装 6 行白名单 body（仅已脱敏元数据；显式排除 hostname 与日志正文）
make_issue_body() {
  local report="$1"
  local level_line="" codes="" code_str="" n=0 gen_time="" source="" os_line=""
  level_line=$(sed -n '/^## 初步结论/,$p' "$report" \
    | tail -n +2 \
    | grep -m1 -v '^[[:space:]]*$' \
    | sed -e 's/^[[:space:]]*`//' -e 's/`[[:space:]]*$//')
  [ -n "$level_line" ] || level_line="（判级行缺失）"
  codes=$(grep -oE 'ERROR_[A-Z_]+|NPE' "$report" | sort -u)
  n=$(printf '%s\n' "$codes" | grep -c .)
  if [ "$n" -eq 0 ]; then
    code_str="（无）"
  elif [ "$n" -le 2 ]; then
    code_str=$(printf '%s\n' "$codes" | paste -sd+ -)
  else
    code_str="$(printf '%s\n' "$codes" | sed -n '1p')+$(printf '%s\n' "$codes" | sed -n '2p') 等${n}个"
  fi
  gen_time=$(grep -m1 '^- 生成时间：' "$report" | sed 's/^- 生成时间：//')
  [ -n "$gen_time" ] || gen_time="（缺失）"
  source=$(grep -m1 '^- 采集来源：' "$report" | sed 's/^- 采集来源：//')
  [ -n "$source" ] || source="（缺失）"
  # 只取 os= 行；hostname= 行明确排除——sanitize 不遮蔽 hostname，属机器身份信息
  os_line=$(grep -m1 '^os= ' "$report" | sed 's/^os= //')
  [ -n "$os_line" ] || os_line="（缺失）"
  printf '判级：%s\n错误码：%s\n生成时间：%s\n来源：%s\n环境：%s\n报告文件：%s（全文请粘贴到模板「诊断报告全文」一节）' \
    "$level_line" "$code_str" "$gen_time" "$source" "os= $os_line" "$(tilde_path "$report")"
}

# issue 主流程：定位报告 → 校验 → 出 URL + 两行指引
run_issue() {
  local report="${1:-}"
  if [ -z "$report" ]; then
    # 缺省取 reports/ 下名字序最新一份（report-YYYYMMDD-HHMMSS.md 文件名即时间序）
    report=$(ls -1 "$REPORT_DIR"/report-*.md 2>/dev/null | sort | tail -1)
    if [ -z "$report" ]; then
      echo "错误: $REPORT_DIR 下没有报告，先跑 \`diag.sh report\`" >&2
      return 1
    fi
  fi
  if [ ! -f "$report" ]; then
    echo "错误: 报告文件不存在: ${report}，先跑 \`diag.sh report\`" >&2
    return 1
  fi
  if [ "$(head -1 "$report")" != "# 诊断报告" ]; then
    echo "错误: 不是诊断报告（首行应为「# 诊断报告」）: ${report}，先跑 \`diag.sh report\`" >&2
    return 1
  fi
  local title="" body=""
  title=$(derive_issue_title "$report")
  body=$(make_issue_body "$report")
  echo "${ISSUE_REPO}/issues/new?template=$(urlencode "$ISSUE_TEMPLATE_NAME")&title=$(urlencode "$title")"
  echo
  echo "以下字段请复制到 issue 表单对应位置："
  echo "────────────────────────────────"
  printf '%s\n' "$body"
  echo "────────────────────────────────"
  echo "打开链接 → 填入上述字段 → 粘贴报告全文 → 提交"
  echo "注意：打开链接即向 GitHub 传输预填标题（已脱敏）；粘贴全文是你自己的出网决策。"
}

# ------------------------------------------------------------
# 5. 本地报告 / 反馈：数据不出网的落盘点（全部走 sanitize 后才落盘）
# ------------------------------------------------------------

# 只读不写；只允许读本 skill 自己的数据目录（防用户拿它当任意文件查看器）
ensure_data_dir() {
  mkdir -p "$DIAG_DATA_DIR" "$REPORT_DIR" "$SESSION_DIR" 2>/dev/null || {
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
  echo "如需回传给大疆侧，先跑 diag.sh issue 生成提交链接（预填标题与短摘要），打开后把本报告全文粘贴进 issue 模板。"
}

# 一键反馈：solved / unsolved / skip + 可选报告路径（TSV 3→4 列：追加报告文件名或 -）
# 路径校验失败（不存在/非报告）→ stderr 警告 + 照常记录 '-'，不中断（反馈记录价值高于路径精确性）
# 路径合法 → 记录文件名并联动打印 issue 提交链接（反馈 unsolved 的用户正是最该提 issue 的人）
# 旧 3 列行不迁移：TSV 无表头、按行解析，读侧容忍缺列
add_feedback() {
  local value="$1" report="${2:-}" report_col="-"
  case "$value" in
    solved|unsolved|skip) ;;
    *) echo "错误: 反馈值必须是 $FEEDBACK_VALUES 之一（got: '$value'）" >&2; return 1 ;;
  esac
  ensure_data_dir || return 1
  if [ -n "$report" ]; then
    # 与 run_issue 同一判定标准：文件存在 且 首行为「# 诊断报告」
    if [ -f "$report" ] && [ "$(head -1 "$report" 2>/dev/null)" = "# 诊断报告" ]; then
      report_col=$(basename "$report")
    else
      echo "警告: 报告路径无效（不存在或非诊断报告），第 4 列记 '-': $report" >&2
    fi
  fi
  printf '%s\t%s\tneeds_manual_upload\t%s\n' \
    "$(date '+%Y-%m-%d %H:%M:%S')" "$value" "$report_col" >> "$FEEDBACK_DB"
  echo "反馈已记录到本地（数据不出网）：$FEEDBACK_DB"
  if [ "$report_col" != "-" ]; then
    echo "状态: 未上传/需人工跟进——提交链接如下（粘贴报告全文后手动提交）"
    echo
    run_issue "$report"
  else
    echo "状态: 未上传/需人工跟进（生成提交链接：先跑 diag.sh report，再 diag.sh issue）"
  fi
}

# 对文件做一次脱敏（stdout 输出到终端）
sanitize_file() {
  local f="$1"
  [ -f "$f" ] || { echo "错误: 文件不存在: $f (列目录请用 collect，不要直接读任意路径)" >&2; return 1; }
  sanitize_stdin < "$f"
}
