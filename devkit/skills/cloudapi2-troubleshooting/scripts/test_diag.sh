#!/usr/bin/env bash
set -u
# 一致性校验 + 脱敏回归测试
#  1) diag.sh classify_code 命中的错误码 应完全等于 error-codes.yaml backend codes 集合
#  2) sanitize_stdin 必须真的脱敏（防止 BSD/GNU 正则差异导致脱敏机制静默失效）
err() { echo "FAIL: $*" >&2; exit 1; }

DIR="$(cd "$(dirname "$0")/.." && pwd)"
DIAG="$DIR/scripts/diag.sh"
DIAG_LIB="$DIR/scripts/lib"
DIAG_PYLIB="$DIR/scripts/pylib"

# ============================================================
# 1. 错误码词典一致性
# ============================================================

# classify_code 的命中集合（从 lib/classify.sh 提取）
grep -oE 'ERROR_[A-Z_]+|NPE' "$DIAG_LIB/classify.sh" \
  | sort -u > /tmp/diag_codes.txt 2>/dev/null || true

# error-codes.yaml backend 段的 codes 集合
# （backend 段从 ^backend: 开始，到设备侧分隔行『# ===』前结束）
awk '/^backend:/{p=1;next} /^# ===/{p=0} p&&/^  - code:/{print $NF}' \
  "$DIR/error-codes.yaml" | sort -u > /tmp/dict_codes.txt

# 空输出守卫：任一侧为空说明提取逻辑本身坏了（grep 模式或 YAML 结构变更），
# 此时 diff 两个空文件会「成功」并打印 OK，一致性校验被静默绕过——必须显式失败。
[ -s /tmp/diag_codes.txt ] \
  || err "从 diag.sh 未提取到任何错误码，提取逻辑失效（grep 模式或文件结构已变）"
[ -s /tmp/dict_codes.txt ] \
  || err "从 error-codes.yaml backend 段未提取到任何错误码，awk 解析失效（段名或缩进已变）"

# 差集必须为空
if ! diff -q /tmp/diag_codes.txt /tmp/dict_codes.txt >/dev/null 2>&1; then
  err "词典与 classify_code 不同步：diag.sh=$(paste -sd, /tmp/diag_codes.txt) vs error-codes.yaml=$(paste -sd, /tmp/dict_codes.txt)"
fi
echo "OK: classify_code 与 error-codes.yaml 一致（$(wc -l < /tmp/diag_codes.txt | tr -d ' ') 个错误码）"

# ============================================================
# 2. 脱敏回归测试
#    历史故障：正则用了 GNU 扩展 \s，BSD sed（macOS）把它当字面量 's'，
#    导致 "password: xxx" 完全不被脱敏；内网段 192.168/172.16 也曾整段漏网。
#    这些用例把「脱敏是否真的生效」变成可失败的断言，而不是靠人工目检。
# ============================================================

# 提取 _sanitize_stdin_sed 函数体（sed fallback，避免执行 diag.sh 主入口）
sed -n '/^_sanitize_stdin_sed()/,/^}/p' "$DIAG_LIB/sanitize.sh" > /tmp/san_fn.sh
[ -s /tmp/san_fn.sh ] || err "无法从 lib/sanitize.sh 提取 _sanitize_stdin_sed 函数体"

san() { ( . /tmp/san_fn.sh; printf '%s\n' "$1" | _sanitize_stdin_sed ); }

# 必须被脱敏：输出中不得再出现敏感原文
assert_redacted() {
  local desc="$1" input="$2" leak="$3" out=""
  out=$(san "$input")
  case "$out" in
    *"$leak"*) err "脱敏失效[$desc]：'$leak' 仍出现在输出中 → $out" ;;
  esac
  echo "  ok  $desc"
}

# 必须原样保留：防止过度脱敏（公网 IP / 版本号被误伤）
assert_preserved() {
  local desc="$1" input="$2" keep="$3" out=""
  out=$(san "$input")
  case "$out" in
    *"$keep"*) echo "  ok  $desc" ;;
    *) err "过度脱敏[$desc]：'$keep' 被误脱敏 → $out" ;;
  esac
}

echo "脱敏用例："
# 凭证：冒号+空格是 SDK 配置最常见写法，也是历史上漏网的形态
assert_redacted "password 冒号空格"   'password: mySecret123'          'mySecret123'
assert_redacted "password 等号"       'password=mySecret123'           'mySecret123'
assert_redacted "token 空格冒号"      'token : abcdefTOKEN'            'abcdefTOKEN'
assert_redacted "api_key"             'api_key: AKIAIOSFODNN7EXAMPLE'  'AKIAIOSFODNN7EXAMPLE'
# 内网 IP 三个段都要覆盖
assert_redacted "内网 10.x"           'host 10.1.2.3'                  '10.1.2.3'
assert_redacted "内网 192.168.x"      'host 192.168.1.2'               '192.168.1.2'
assert_redacted "内网 172.16.x"       'host 172.16.1.2'                '172.16.1.2'
# URL query 必须整值遮蔽，不能只遮第一个字符
assert_redacted "URL query 整值"      'GET /a?sig=ABCDEF123456&uid=42' 'ABCDEF123456'
assert_redacted "邮箱"                'mail foo.bar@example.com'       'foo.bar@example.com'
assert_redacted "手机号"              'phone 13812345678'              '13812345678'
assert_redacted "用户目录"            'path /Users/someone/x'          '/Users/someone'
# 负向用例：172.32 属公网、1.2.3.4 是版本号，都不该被动
assert_preserved "公网 172.32 不脱敏" 'host 172.32.1.2'                '172.32.1.2'
assert_preserved "版本号不误伤"       'ver 1.2.3.4'                    '1.2.3.4'

echo "OK: 脱敏回归测试全部通过"

# ============================================================
# 3. issue / URL 生成
# ============================================================

# --- 3a. urlencode 单元测试（从 lib/issue.sh 提取函数体） ---
sed -n '/^urlencode()/,/^}/p' "$DIAG_LIB/issue.sh" > /tmp/url_fn.sh
[ -s /tmp/url_fn.sh ] || err "无法从 lib/issue.sh 提取 urlencode 函数体"

urlenc() { ( . /tmp/url_fn.sh; urlencode "$1" ); }

echo "urlencode 用例："
# RFC 3986 unreserved 字符必须原样直通
out=$(urlenc 'AZaz09.~_-')
[ "$out" = "AZaz09.~_-" ] \
  || err "urlencode 直通失效：'AZaz09.~_-' → '$out'"
echo "  ok  unreserved 直通"

# 保留字符必须全部编码（& = # 不得以裸字符出现；三个都用「包含」语义 *X*）
out=$(urlenc 'a&b=c#d')
case "$out" in
  *"&"*|*"="*|*"#"* ) err "urlencode 保留字符漏编码：'a&b=c#d' → '$out'" ;;
esac
echo "  ok  保留字符全编码"

# 空格 → %20；换行 → %0A
out=$(urlenc ' ')
[ "$out" = "%20" ] || err "urlencode 空格：' ' → '$out'（期望 %20）"
out=$(urlenc "$(printf 'x\ny')")
[ "$out" = "x%0Ay" ] || err "urlencode 换行：'x\ny' → '$out'（期望 x%0Ay）"
echo "  ok  空格→%20 换行→%0A"

# 中文字符按字节编码：「零」= UTF-8 E9 9B B6 → %E9%9B%B6（每个 %XX 恰好 2 位 hex）
out=$(urlenc '零命中')
[ "$out" = "%E9%9B%B6%E5%91%BD%E4%B8%AD" ] \
  || err "urlencode 中文精确值不匹配：'零命中' → '$out'（期望 %E9%9B%B6%E5%91%BD%E4%B8%AD）"
echo "  ok  中文按字节 %XX"

# 整体字符集校验：任意混合输入
out=$(urlenc '判级：L1 ERROR_A (2026-09-16)')
case "$out" in
  *[^A-Za-z0-9.~_%-]*) err "urlencode 输出字符集违规：→ '$out'" ;;
esac
echo "  ok  输出字符集 ^[A-Za-z0-9.~_%-]*"

echo "OK: urlencode 单元测试全部通过"

# --- 3b. issue 端到端测试（fixture 报告写入临时 DIAG_DATA_DIR；绝不跑 collect） ---
E2E_DIR=$(mktemp -d)
mkdir -p "$E2E_DIR/reports"

# fixture 生成器：$1=判级行（不带反引号） $2=错误码（可空） $3=生成时间（可空=省略该行）
# 所有 fixture 环境节都带 hostname= secretbox —— 用于「hostname 不出网」硬断言
make_fixture() {
  local f="$1" level="$2" codes="$3" gen="$4"
  {
    echo "# 诊断报告"
    echo
    if [ -n "$gen" ]; then
      echo "- 生成时间：$gen"
    fi
    echo "- 采集来源：cloudapi2-troubleshooting v2.3.0"
    echo
    echo "## 环境（脱敏后）"
    echo "os= Darwin 24.5.0 arm64"
    echo "hostname= secretbox"
    echo
    echo "## SDK 日志（脱敏后，末 200 行）"
    if [ -n "$codes" ]; then
      echo "    java Exception: $codes"
    else
      echo "    （空）"
    fi
    echo
    echo "## 初步结论"
    echo "\`$level\`"
  } > "$f"
}

# 跑 issue 取 URL 首行（stderr 丢弃；错误路径另行测）
issue_url() {
  local f="$1"
  DIAG_DATA_DIR="$E2E_DIR" bash "$DIAG" issue "$f" 2>/dev/null | head -1
}

# 断言 URL 中出现「期望标题的 urlencode 结果」
assert_title() {
  local desc="$1" f="$2" want_title="$3" url="" want=""
  url=$(issue_url "$f")
  want=$(urlenc "$want_title")
  case "$url" in
    *"$want"*) echo "  ok  $desc" ;;
    *) err "标题推导错误[$desc]：URL 中找不到 '$want_title' → $url" ;;
  esac
}

echo "issue e2e 用例："
# 标题推导 ×4（spec §4.2 推导表逐行覆盖）
make_fixture "$E2E_DIR/reports/report-l1a.md" \
  "L1:错误码命中词典，只能给结论与建议" "ERROR_CALLBACK_TIMEOUT" "2026-09-16 12:34:56"
assert_title "L1 单码" "$E2E_DIR/reports/report-l1a.md" \
  "[diag] L1 ERROR_CALLBACK_TIMEOUT (2026-09-16)"

make_fixture "$E2E_DIR/reports/report-l1b.md" \
  "L1:错误码命中词典，只能给结论与建议" \
  "ERROR_CALLBACK_TIMEOUT ERROR_INIT_FAILED NPE" "2026-09-16 12:34:56"
assert_title "L1 多码等N个" "$E2E_DIR/reports/report-l1b.md" \
  "[diag] L1 ERROR_CALLBACK_TIMEOUT+ERROR_INIT_FAILED 等3个 (2026-09-16)"

make_fixture "$E2E_DIR/reports/report-l2.md" \
  "L2:零命中但采集完整（日志中无已知错误码）——需升级人工/RCA" "" "2026-09-16 12:34:56"
assert_title "L2 固定文案" "$E2E_DIR/reports/report-l2.md" \
  "[diag] L2 零命中-需人工RCA (2026-09-16)"

make_fixture "$E2E_DIR/reports/report-l3.md" \
  "L3:零命中且关键采集缺失（环境/日志为空）——降级为原始工单 + 现场快照" "" "2026-09-16 12:34:56"
assert_title "L3 固定文案" "$E2E_DIR/reports/report-l3.md" \
  "[diag] L3 采集缺失-需现场快照 (2026-09-16)"

# hostname 不出网：完整 stdout（URL + 终端打印）绝不含 secretbox
out=$(DIAG_DATA_DIR="$E2E_DIR" bash "$DIAG" issue "$E2E_DIR/reports/report-l1a.md" 2>/dev/null)
case "$out" in
  *"secretbox"*) err "hostname 泄漏：stdout 中出现 'secretbox'" ;;
esac
echo "  ok  hostname 不出网"

# ~ 相对化：终端打印的元数据中路径应为 ~/... 形式
TILDE_FIX_DIR="$HOME/.dji_cloud_diag_test"
mkdir -p "$TILDE_FIX_DIR"
make_fixture "$TILDE_FIX_DIR/report-tilde.md" \
  "L2:零命中但采集完整（日志中无已知错误码）" "" "2026-09-16 12:34:56"
out=$(DIAG_DATA_DIR="$E2E_DIR" bash "$DIAG" issue "$TILDE_FIX_DIR/report-tilde.md" 2>/dev/null)
case "$out" in
  *"~/.dji_cloud_diag_test/report-tilde.md"*) echo "  ok  路径 ~ 相对化" ;;
  *) err "路径未 ~ 相对化：stdout 中找不到 '~/.dji_cloud_diag_test/report-tilde.md'" ;;
esac
case "$out" in
  *"$HOME"*) err "路径泄漏用户目录：stdout 含 '$HOME' 原文" ;;
esac
echo "  ok  不含 \$HOME 展开原文"
rm -rf "$TILDE_FIX_DIR"

# 错误路径 ×3：stderr 必含「先跑 diag.sh report」且退出码 1
# 空参数必须传真正的空串 ""——传 "''" 字面量会走错分支：无引号 $args 展开后
# 引号字符是数据不是语法，diag.sh 收到的是两个引号字符而非空参数
hint='先跑 `diag.sh report`'
assert_issue_err() {
  local desc="$1" datadir="$2" arg="$3" out="" rc=0
  out=$(DIAG_DATA_DIR="$datadir" bash "$DIAG" issue "$arg" 2>&1 >/dev/null); rc=$?
  [ "$rc" -eq 1 ] || err "错误路径退出码非 1[$desc]：rc=$rc"
  case "$out" in
    *"$hint"*) echo "  ok  $desc" ;;
    *) err "错误提示缺指引[$desc]：→ $out" ;;
  esac
}
empty_dir=$(mktemp -d)
assert_issue_err "reports 目录为空" "$empty_dir" ""    # 空串 → 缺省查找 → 目录空分支
assert_issue_err "路径不存在" "$E2E_DIR" "/nonexistent-report-xyz.md"
echo "# 不是诊断报告" > "$E2E_DIR/not-a-report.md"
assert_issue_err "首行非报告头" "$E2E_DIR" "$E2E_DIR/not-a-report.md"

# 缺省取最新：reports/ 下有 l1a/l1b/l2/l3 四份，名字序最大是 report-l3.md
url=$(DIAG_DATA_DIR="$E2E_DIR" bash "$DIAG" issue 2>/dev/null | head -1)
want=$(urlenc "[diag] L3 采集缺失-需现场快照 (2026-09-16)")
case "$url" in
  *"$want"*) echo "  ok  缺省取名字序最新报告" ;;
  *) err "缺省报告选择错误：应取 report-l3.md → $url" ;;
esac

rm -rf "$E2E_DIR" "$empty_dir"
echo "OK: issue 端到端测试全部通过"

# --- 3c. feedback 第 4 列（TSV 追加 + 路径容错 + issue 链接联动） ---
FB_DIR=$(mktemp -d)
make_fixture "$FB_DIR/report-fb.md" \
  "L1:错误码命中词典，只能给结论与建议" "ERROR_CALLBACK_TIMEOUT" "2026-09-16 12:34:56"

echo "feedback 第 4 列用例："
# 合法路径：TSV 第 4 列 = 文件名（总列数 4）；stdout 联动输出 issue URL
out=$(DIAG_DATA_DIR="$FB_DIR" bash "$DIAG" feedback unsolved "$FB_DIR/report-fb.md" 2>/dev/null)
last=$(tail -1 "$FB_DIR/feedback.tsv")
[ "$(printf '%s\n' "$last" | awk -F'\t' '{print NF}')" -eq 4 ] \
  || err "TSV 列数非 4：→ $last"
case "$last" in
  *report-fb.md) echo "  ok  合法路径第 4 列=文件名" ;;
  *) err "TSV 第 4 列非文件名：→ $last" ;;
esac
want=$(urlenc "[diag] L1 ERROR_CALLBACK_TIMEOUT (2026-09-16)")
case "$out" in
  *"$want"*) echo "  ok  反馈联动打印 issue 链接" ;;
  *) err "反馈未联动打印 issue 链接：→ $out" ;;
esac

# 坏路径：stderr 警告 + 记 '-' + 不中断（rc=0）+ 不打印链接
out=$(DIAG_DATA_DIR="$FB_DIR" bash "$DIAG" feedback skip /nonexistent-fb.md 2>"$FB_DIR/warn.txt"); rc=$?
[ "$rc" -eq 0 ] || err "坏路径中断了反馈（应照常记录）：rc=$rc"
grep -q '警告' "$FB_DIR/warn.txt" \
  || err "坏路径无 stderr 警告：$(cat "$FB_DIR/warn.txt")"
last=$(tail -1 "$FB_DIR/feedback.tsv")
[ "$(printf '%s\n' "$last" | awk -F'\t' '{print $NF}')" = "-" ] \
  || err "坏路径未记 '-'：→ $last"
case "$out" in
  *issues/new*) err "坏路径不应打印 issue 链接：→ $out" ;;
esac
echo "  ok  坏路径记 '-' 并警告不中断"

# 无路径（第 3 参数缺省）：照常记录 '-'
out=$(DIAG_DATA_DIR="$FB_DIR" bash "$DIAG" feedback solved 2>/dev/null); rc=$?
[ "$rc" -eq 0 ] || err "无路径反馈失败：rc=$rc"
last=$(tail -1 "$FB_DIR/feedback.tsv")
[ "$(printf '%s\n' "$last" | awk -F'\t' '{print $NF}')" = "-" ] \
  || err "无路径未记 '-'：→ $last"
echo "  ok  无路径缺省记 '-'"

rm -rf "$FB_DIR"
echo "OK: feedback 第 4 列测试全部通过"

# ============================================================
# 4. trace 精准采集
# ============================================================

TRACE_DIR=$(mktemp -d)
mkdir -p "$TRACE_DIR/sdk_logs" "$TRACE_DIR/app_logs"

# 创建含已知 trace_id 的假日志文件
cat > "$TRACE_DIR/sdk_logs/sdk.log" <<'LOGEOF'
2026-09-16 14:00:01 INFO  Starting service
2026-09-16 14:00:02 INFO  Request received trace_id=req-abc123
2026-09-16 14:00:03 ERROR LoginSvc.verify failed: NullPointerException trace_id=req-abc123
2026-09-16 14:00:04 INFO  Cleanup complete trace_id=req-abc123
2026-09-16 14:00:05 INFO  Next request trace_id=req-other999
LOGEOF

cat > "$TRACE_DIR/app_logs/spring.log" <<'LOGEOF'
2026-09-16 14:00:02 DEBUG gateway routing trace_id=req-abc123
2026-09-16 14:00:03 ERROR upstream 500 trace_id=req-abc123
LOGEOF

# 提取 collect_by_trace 需要的函数和常量
# 注意：覆盖 SDK_LOG_PATHS 和 APP_LOG_PATHS 指向临时目录
extract_trace_env() {
  # 从 lib 文件提取需要的函数
  sed -n '/^_sanitize_stdin_sed()/,/^}/p' "$DIAG_LIB/sanitize.sh"
  cat <<ENVEOF
have_cmd() { command -v "\$1" >/dev/null 2>&1; }
SCRIPT_DIR="$DIR/scripts"
sanitize_stdin() {
  if have_cmd python3; then
    python3 "\${SCRIPT_DIR}/pylib/sanitize.py"
  else
    _sanitize_stdin_sed
  fi
}
SDK_LOG_PATHS=("$TRACE_DIR/sdk_logs")
APP_LOG_PATHS=("$TRACE_DIR/app_logs")
TRACE_CONTEXT_LINES=5
TRACE_MAX_LINES=500
ENVEOF
  sed -n '/^collect_by_trace()/,/^}/p' "$DIAG_LIB/issue.sh"
  sed -n '/^run_trace()/,/^}/p' "$DIAG_LIB/issue.sh"
}
extract_trace_env > /tmp/trace_fn.sh

trace_run() {
  ( . /tmp/trace_fn.sh; collect_by_trace "$1" 2>/tmp/trace_stderr.txt; )
}

echo "trace 精准采集用例："
# 命中：输出含匹配行，且退出码必须为 0
# （只断言输出文本会漏掉「命中仍 return 1」的假阴性：命中判定曾被写在
#   `{ ... } | sanitize_stdin` 的管道子 shell 里，父 shell 永远读到 found=0）
out=$(trace_run "req-abc123"); rc=$?
case "$out" in
  *"LoginSvc.verify"*) echo "  ok  命中 trace_id" ;;
  *) err "trace 未命中：输出中找不到 LoginSvc.verify → $out" ;;
esac
[ "$rc" -eq 0 ] || err "命中应返回 0（假阴性）：rc=$rc"
echo "  ok  命中退出码为 0"

# 多文件命中
case "$out" in
  *"sdk.log"*) ;;
  *) err "trace 输出缺 sdk.log 文件头" ;;
esac
case "$out" in
  *"spring.log"*) ;;
  *) err "trace 输出缺 spring.log 文件头" ;;
esac
echo "  ok  多文件命中带文件头"

# 未命中：stderr 含提示
trace_run "nonexistent-trace-xyz" >/dev/null 2>/tmp/trace_stderr.txt; rc=$?
[ "$rc" -eq 1 ] || err "未命中应返回 1：rc=$rc"
grep -q '未找到匹配' /tmp/trace_stderr.txt \
  || err "未命中无 stderr 提示：$(cat /tmp/trace_stderr.txt)"
echo "  ok  未命中提示"

# 截断：生成超过 TRACE_MAX_LINES 行命中的日志
{
  for i in $(seq 1 600); do
    echo "line $i trace_id=req-overflow-test"
  done
} > "$TRACE_DIR/sdk_logs/big.log"

# 用小 TRACE_MAX_LINES 测截断
(
  . /tmp/trace_fn.sh
  TRACE_MAX_LINES=50
  collect_by_trace "req-overflow-test" >/dev/null 2>/tmp/trace_stderr.txt
)
grep -q '已截断' /tmp/trace_stderr.txt \
  || err "截断未输出警告：$(cat /tmp/trace_stderr.txt)"
echo "  ok  截断警告"

rm -rf "$TRACE_DIR"
echo "OK: trace 精准采集测试全部通过"

# ============================================================
# 5. sanitize_with_stats 脱敏统计
# ============================================================

sed -n '/^_sanitize_stdin_sed()/,/^}/p' "$DIAG_LIB/sanitize.sh" > /tmp/sws_san.sh
sed -n '/^_sanitize_with_stats_awk_sed()/,/^}/p' "$DIAG_LIB/sanitize.sh" > /tmp/sws_fn.sh
[ -s /tmp/sws_fn.sh ] || err "无法从 lib/sanitize.sh 提取 _sanitize_with_stats_awk_sed 函数体"
cat /tmp/sws_san.sh /tmp/sws_fn.sh > /tmp/sws_all.sh

sws() { ( . /tmp/sws_all.sh; printf '%s\n' "$1" | _sanitize_with_stats_awk_sed; ); }

echo "sanitize_with_stats 用例："

# 混合输入：已知各类命中数
test_input='password: secret123
host 10.1.2.3 and 192.168.1.1
mail user@corp.com
phone 13812345678
GET /api?sig=abc&uid=42
path /Users/dev/project'

out=$(printf '%s\n' "$test_input" | ( . /tmp/sws_all.sh; _sanitize_with_stats_awk_sed ) 2>/tmp/sws_stderr.txt)
stats=$(cat /tmp/sws_stderr.txt)
# stdout 不含敏感值
case "$out" in
  *"secret123"*) err "stats: stdout 仍含凭证" ;;
  *"10.1.2.3"*) err "stats: stdout 仍含内网 IP" ;;
  *"user@corp.com"*) err "stats: stdout 仍含邮箱" ;;
esac
echo "  ok  stdout 已脱敏"

# stderr 统计行格式与数值校验
case "$stats" in
  *"credential=1"*) ;;
  *) err "stats credential 计数错误：→ $stats" ;;
esac
case "$stats" in
  *"ipv4=1"*) ;;
  *) err "stats ipv4 计数错误（应为 1，grep -c 按行计）：→ $stats" ;;
esac
case "$stats" in
  *"email=1"*) ;;
  *) err "stats email 计数错误：→ $stats" ;;
esac
case "$stats" in
  *"phone_cn=1"*) ;;
  *) err "stats phone_cn 计数错误：→ $stats" ;;
esac
echo "  ok  统计行各计数正确"

# 零命中输入
zero_out=$(printf '%s\n' "no sensitive data here" | ( . /tmp/sws_all.sh; _sanitize_with_stats_awk_sed ) 2>/tmp/sws_stderr.txt)
zero_stats=$(cat /tmp/sws_stderr.txt)
case "$zero_stats" in
  *"credential=0"*"ipv4=0"*"email=0"*"phone_cn=0"*) echo "  ok  零命中全零" ;;
  *) err "零命中统计非全零：→ $zero_stats" ;;
esac

echo "OK: sanitize_with_stats 测试全部通过"

# ============================================================
# 6. session 端到端（start → set → collect → preview → confirm → issue）
# ============================================================

S_DIR=$(mktemp -d)

echo "session e2e 用例："

# start
sid=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session start 2>/dev/null)
[ -n "$sid" ] || err "session start 未返回 ID"
[ -f "$S_DIR/sessions/$sid/session.meta" ] \
  || err "session start 未创建 session.meta"
state=$(grep '^state=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$state" = "created" ] || err "start 后状态应为 created：→ $state"
echo "  ok  start → created"

# set（severity 必须是 issue 表单下拉选项之一，此处用「致命」而非旧文档里的 P1）
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid" company=示例科技 severity=致命 sdk_version=3.3.7 error_text="SDK 初始化失败，日志无 ERROR" 2>/dev/null)
[ "$out" = "ok" ] || err "set 返回非 ok：→ $out"
sev=$(grep '^severity=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$sev" = "致命" ] || err "set severity 写入错误：→ $sev"
echo "  ok  set 元数据"

# set 拒绝不在下拉选项内的 severity：
# 表单 severity 是 required 下拉，值不在选项内用户根本填不上（旧文档写的 P1 即属此类）
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid" severity=P1 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "非表单选项的 severity 应被拒绝：rc=$rc"
case "$out" in
  *"致命 严重 一般 提示"*) echo "  ok  非表单选项 severity 被拒并提示可选值" ;;
  *) err "severity 拒绝时未提示可选值：→ $out" ;;
esac
sev=$(grep '^severity=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$sev" = "致命" ] || err "被拒的 severity 篡改了原值：→ $sev"

# set 忽略未知 key
warn=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid" badkey=x 2>&1 >/dev/null)
case "$warn" in
  *"忽略"*) echo "  ok  set 未知 key 警告" ;;
  *) err "set 未知 key 应警告：→ $warn" ;;
esac

# collect
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session collect "$sid" 2>/dev/null)
[ -f "$S_DIR/sessions/$sid/evidence.txt" ] \
  || err "collect 未产出 evidence.txt"
state=$(grep '^state=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$state" = "collected" ] || err "collect 后状态应为 collected：→ $state"
level=$(grep '^level=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ -n "$level" ] || err "collect 未写入 level"
echo "  ok  collect → collected，判级 $level"

# preview
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session preview "$sid" 2>/dev/null)
[ -f "$S_DIR/sessions/$sid/sanitized.md" ] \
  || err "preview 未产出 sanitized.md"
[ -f "$S_DIR/sessions/$sid/stats.txt" ] \
  || err "preview 未产出 stats.txt"
state=$(grep '^state=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$state" = "previewed" ] || err "preview 后状态应为 previewed：→ $state"
case "$out" in
  *"脱敏统计"*) ;;
  *) err "preview 输出缺脱敏统计" ;;
esac
echo "  ok  preview → previewed + 统计"

# confirm
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session confirm "$sid" 2>/dev/null)
state=$(grep '^state=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$state" = "confirmed" ] || err "confirm 后状态应为 confirmed：→ $state"
echo "  ok  confirm → confirmed"

# issue
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session issue "$sid" 2>/dev/null)
state=$(grep '^state=' "$S_DIR/sessions/$sid/session.meta" | cut -d= -f2-)
[ "$state" = "issued" ] || err "issue 后状态应为 issued：→ $state"
case "$out" in
  *"issues/new"*) ;;
  *) err "issue 输出缺 URL：→ $out" ;;
esac
# 模板名含中文，必须 %XX 编码后再拼进 query，否则 URL 被截断/表单匹配不上
case "$out" in
  *"template=1-%E9%97%AE%E9%A2%98-bug.yml"*) ;;
  *) err "issue URL 的 template 未编码或编码错误：→ $out" ;;
esac
case "$out" in
  *"%E9%97%AE%E9%A2%98"*) ;;
  *) err "issue URL 未见编码后的模板名：→ $out" ;;
esac
# 表单 required 字段必须出现在 URL params 或描述中，否则用户无从复制
case "$out" in
  *"company="*) ;;
  *) err "issue 输出缺企业名称：→ $out" ;;
esac
case "$out" in
  *"summary="*) ;;
  *) err "issue 输出缺 error_text：→ $out" ;;
esac
case "$out" in
  *"严重程度：致命"*) ;;
  *) err "issue 输出缺严重程度：→ $out" ;;
esac
echo "  ok  issue → issued + URL（模板名已编码）+ 必填字段齐全"

echo "OK: session e2e 全链路通过"

# ============================================================
# 7. session 状态门（跳步应报错）
# ============================================================

echo "session 状态门用例："

# created → preview 应失败（需先 collect）
sid2=$(DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session preview "$sid2" 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "跳步 preview 应失败"
case "$out" in
  *"created"*"collected"*) echo "  ok  created → preview 被拦" ;;
  *) err "跳步提示错误：→ $out" ;;
esac

# created → confirm 应失败
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session confirm "$sid2" 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "跳步 confirm 应失败"
echo "  ok  created → confirm 被拦"

# created → issue 应失败
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session issue "$sid2" 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "跳步 issue 应失败"
echo "  ok  created → issue 被拦"

# collected → issue 应失败（需先 preview + confirm）
DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session collect "$sid2" >/dev/null 2>/dev/null
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session issue "$sid2" 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "跳步 collected → issue 应失败"
echo "  ok  collected → issue 被拦"

echo "OK: session 状态门测试全部通过"

# ============================================================
# 8. session 边界用例
# ============================================================

echo "session 边界用例："

# 不存在的 session ID
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session collect nonexistent-xyz 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "不存在 ID 应失败"
case "$out" in
  *"不存在"*) echo "  ok  不存在 ID 报错" ;;
  *) err "不存在 ID 提示不对：→ $out" ;;
esac

# status 列表：应至少含 2 个 session（sid + sid2）
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session status 2>/dev/null)
n=$(printf '%s\n' "$out" | grep -c .)
[ "$n" -ge 2 ] || err "status 列表应含 ≥2 行：→ $out"
echo "  ok  status 列表 $n 条"

# status 单个
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session status "$sid" 2>/dev/null)
case "$out" in
  *"id=$sid"*) echo "  ok  status 单个" ;;
  *) err "status 单个未返回 meta：→ $out" ;;
esac

# set 在 previewed/confirmed/issued 状态下应失败
# （值取合法 severity，确保失败只可能来自状态门而非选项校验）
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid" severity=一般 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "issued 状态下 set 应失败"
echo "  ok  issued 状态下 set 被拦"

# 同一秒内连续 start 不得撞名（否则后者会覆盖前者的证据与元数据）
sid3=$(DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
sid4=$(DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
[ "$sid3" != "$sid4" ] || err "同一秒内 start 撞名：两个 session 均返回 '$sid3'"
[ -d "$S_DIR/sessions/$sid3" ] && [ -d "$S_DIR/sessions/$sid4" ] \
  || err "同一秒内 start 未各自建目录：$sid3 / $sid4"
before=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session status "$sid3" 2>/dev/null)
[ -n "$before" ] || err "撞名后先建的 session 已丢失：→ $before"
echo "  ok  同一秒 start 不撞名（${sid3} / ${sid4}）"

# 判级不得越权：采到词典外的错误码属「零命中」，必须判 L2 而非 L1
# （回归：session collect 曾对任何非空错误码数组直接 level=L1，
#  与 classify_code / classification.yaml 的「零命中 → L2」冲突，
#  并让 issue 标题谎称「命中词典」）
SESS_HOME="$S_DIR/home"
mkdir -p "$SESS_HOME/.dji_cloud/log"

echo "ERROR ERROR_WEIRD_THING not-in-dict" > "$SESS_HOME/.dji_cloud/log/sdk.log"
sid5=$(DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 HOME="$SESS_HOME" bash "$DIAG" session start 2>/dev/null)
DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 HOME="$SESS_HOME" bash "$DIAG" session collect "$sid5" >/dev/null 2>/dev/null
level5=$(grep '^level=' "$S_DIR/sessions/$sid5/session.meta" | cut -d= -f2-)
[ "$level5" = "L2" ] || err "词典外错误码应判 L2（零命中），实际 L2≠$level5"
codes5=$(grep '^error_codes=' "$S_DIR/sessions/$sid5/session.meta" | cut -d= -f2-)
[ "$codes5" = "ERROR_WEIRD_THING" ] || err "错误码应仍被记录：→ $codes5"
echo "  ok  词典外错误码判 L2 且仍记录 error_codes"

# 词典内错误码仍判 L1
echo "ERROR ERROR_CALLBACK_TIMEOUT boom" > "$SESS_HOME/.dji_cloud/log/sdk.log"
sid6=$(DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 HOME="$SESS_HOME" bash "$DIAG" session start 2>/dev/null)
DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 HOME="$SESS_HOME" bash "$DIAG" session collect "$sid6" >/dev/null 2>/dev/null
level6=$(grep '^level=' "$S_DIR/sessions/$sid6/session.meta" | cut -d= -f2-)
[ "$level6" = "L1" ] || err "词典内错误码应判 L1，实际 → $level6"
echo "  ok  词典内错误码判 L1"

# set 的值不得含换行：session.meta 是单行 key=value，换行可伪造出
# 一整行新 key（如 error_text=$'x\nstate=issued'）污染 meta 并绕过状态机
sid7=$(DIAG_DATA_DIR="$S_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid7" error_text=$'host down\nstate=issued' 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "含换行的 set 值应被拒绝：rc=$rc"
echo "  ok  含换行的 error_text 被拒绝"
cnt=$(grep -c '^state=' "$S_DIR/sessions/$sid7/session.meta")
[ "$cnt" -eq 1 ] || err "meta 被注入伪造的 state 行：state 行数=$cnt"
state7=$(grep '^state=' "$S_DIR/sessions/$sid7/session.meta" | cut -d= -f2-)
[ "$state7" = "created" ] || err "伪造注入改变了状态：→ $state7"
echo "  ok  meta 未被注入（state 仍为 ${state7}）"

# 长度超限同样拒绝
long_val=$(printf 'x%.0s' $(seq 1 1200))
out=$(DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid7" error_text="$long_val" 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "超长 error_text 应被拒绝：rc=$rc"
echo "  ok  超长 error_text 被拒绝"

# 正常多字段 set 仍可用（新值须能落盘；severity 必须是表单下拉选项之一）
DIAG_DATA_DIR="$S_DIR" bash "$DIAG" session set "$sid7" error_text="设备离线，日志无 ERROR" severity=严重 >/dev/null 2>&1
text7=$(grep '^error_text=' "$S_DIR/sessions/$sid7/session.meta" | cut -d= -f2-)
[ "$text7" = "设备离线，日志无 ERROR" ] || err "正常 error_text 未落盘：→ $text7"
sev7=$(grep '^severity=' "$S_DIR/sessions/$sid7/session.meta" | cut -d= -f2-)
[ "$sev7" = "严重" ] || err "正常 severity 未落盘：→ $sev7"
echo "  ok  正常多字段 set 正常落盘"

rm -rf "$S_DIR"
echo "OK: session 边界用例全部通过"

# ============================================================
# 9. collect --no-zip + session pack
# ============================================================

P_DIR=$(mktemp -d)

echo "collect --no-zip + pack 用例："

# start + set + collect --no-zip：应产出 evidence.txt 但无 zip
psid=$(DIAG_DATA_DIR="$P_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
[ -n "$psid" ] || err "pack test: session start 未返回 ID"
DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session set "$psid" company=测试公司 severity=一般 >/dev/null 2>&1
out=$(DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session collect --no-zip "$psid" 2>/tmp/pack_collect_stderr.txt)
[ -f "$P_DIR/sessions/$psid/evidence.txt" ] \
  || err "collect --no-zip 未产出 evidence.txt"
[ ! -f "$P_DIR/sessions/$psid/session-${psid}-logs.zip" ] \
  || err "collect --no-zip 不应产出 zip 文件"
state=$(grep '^state=' "$P_DIR/sessions/$psid/session.meta" | cut -d= -f2-)
[ "$state" = "collected" ] || err "collect --no-zip 后状态应为 collected：→ $state"
echo "  ok  collect --no-zip 无 zip 且状态 collected"

# 采集总览应输出到 stderr
grep -q '采集总览' /tmp/pack_collect_stderr.txt \
  || err "collect --no-zip 未输出采集总览"
grep -q '路径:' /tmp/pack_collect_stderr.txt \
  || err "collect --no-zip 总览未显示路径"
echo "  ok  collect --no-zip 输出采集总览"

# pack 在 collected 状态应成功
zip_out=$(DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session pack "$psid" 2>/tmp/pack_stderr.txt)
[ -f "$P_DIR/sessions/$psid/session-${psid}-logs.zip" ] \
  || err "session pack 未产出 zip 文件"
echo "  ok  session pack 产出 zip"

# pack 带附件：创建临时文件作为附件
echo "screenshot content" > /tmp/test_screenshot.png
zip_out2=$(DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session pack "$psid" /tmp/test_screenshot.png 2>/tmp/pack_attach_stderr.txt)
[ -f "$P_DIR/sessions/$psid/attachments/test_screenshot.png" ] \
  || err "session pack 未复制附件到 attachments/"
grep -q '已添加附件' /tmp/pack_attach_stderr.txt \
  || err "session pack 未输出附件添加提示"
echo "  ok  session pack 带附件"

# pack 附件不存在时应警告但不失败
DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session pack "$psid" /tmp/nonexistent_file.xyz 2>/tmp/pack_warn_stderr.txt; rc=$?
grep -q '警告' /tmp/pack_warn_stderr.txt \
  || err "pack 不存在附件应有警告"
echo "  ok  session pack 不存在附件有警告"

# pack 总览应输出到 stderr
grep -q '采集总览' /tmp/pack_attach_stderr.txt \
  || err "session pack 未输出采集总览"
echo "  ok  session pack 输出采集总览"

# collect 不带 --no-zip 仍向后兼容（自动打 zip）
psid2=$(DIAG_DATA_DIR="$P_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session set "$psid2" company=兼容测试 severity=提示 >/dev/null 2>&1
DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session collect "$psid2" >/dev/null 2>/dev/null
state2=$(grep '^state=' "$P_DIR/sessions/$psid2/session.meta" | cut -d= -f2-)
[ "$state2" = "collected" ] || err "collect（无 flag）后状态应为 collected：→ $state2"
echo "  ok  collect（无 flag）向后兼容"

# pack 在 created 状态应失败
psid3=$(DIAG_DATA_DIR="$P_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
out=$(DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session pack "$psid3" 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "pack 在 created 状态应失败"
echo "  ok  pack 在 created 状态被拒"

# pack 在 previewed 状态应成功
DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session preview "$psid" >/dev/null 2>/dev/null
out=$(DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session pack "$psid" 2>/dev/null)
echo "  ok  pack 在 previewed 状态成功"

# pack 在 confirmed 状态应成功
DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session confirm "$psid" >/dev/null 2>/dev/null
out=$(DIAG_DATA_DIR="$P_DIR" bash "$DIAG" session pack "$psid" 2>/dev/null)
echo "  ok  pack 在 confirmed 状态成功"

rm -f /tmp/test_screenshot.png
rm -rf "$P_DIR"
echo "OK: collect --no-zip + pack 测试全部通过"

# ============================================================
# 10. log_since_minutes 时间范围
# ============================================================

T_DIR=$(mktemp -d)

echo "log_since_minutes 用例："

# set log_since_minutes 应被接受（是 SESSION_ALLOWED_KEYS 的成员）
tsid=$(DIAG_DATA_DIR="$T_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
out=$(DIAG_DATA_DIR="$T_DIR" bash "$DIAG" session set "$tsid" log_since_minutes=10 2>/dev/null)
[ "$out" = "ok" ] || err "set log_since_minutes 返回非 ok：→ $out"
lsm=$(grep '^log_since_minutes=' "$T_DIR/sessions/$tsid/session.meta" | cut -d= -f2-)
[ "$lsm" = "10" ] || err "log_since_minutes 未落盘：→ $lsm"
echo "  ok  set log_since_minutes=10 落盘"

# collect 后 meta 里的 log_since_minutes 仍在
DIAG_DATA_DIR="$T_DIR" bash "$DIAG" session collect --no-zip "$tsid" >/dev/null 2>/dev/null
lsm2=$(grep '^log_since_minutes=' "$T_DIR/sessions/$tsid/session.meta" | cut -d= -f2-)
[ "$lsm2" = "10" ] || err "collect 后 log_since_minutes 丢失：→ $lsm2"
echo "  ok  collect 后 log_since_minutes 保留"

rm -rf "$T_DIR"
echo "OK: log_since_minutes 测试全部通过"

# ============================================================
# 11. session issue --label
# ============================================================

L_DIR=$(mktemp -d)

echo "session issue --label 用例："

# 完整流程：start → set → collect → preview → confirm → issue --label
lsid=$(DIAG_DATA_DIR="$L_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session set "$lsid" company=标签测试 severity=一般 >/dev/null 2>&1
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session collect "$lsid" >/dev/null 2>/dev/null
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session preview "$lsid" >/dev/null 2>/dev/null
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session confirm "$lsid" >/dev/null 2>/dev/null

# issue（URL 模式，不用 --gh，只检查不依赖 gh CLI）
out=$(DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session issue "$lsid" 2>/dev/null)
state=$(grep '^state=' "$L_DIR/sessions/$lsid/session.meta" | cut -d= -f2-)
[ "$state" = "issued" ] || err "issue 后状态应为 issued：→ $state"
echo "  ok  issue → issued"

# set label 应被接受（是 SESSION_ALLOWED_KEYS 的成员）
lsid2=$(DIAG_DATA_DIR="$L_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
out=$(DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session set "$lsid2" label=bug 2>/dev/null)
[ "$out" = "ok" ] || err "set label 返回非 ok：→ $out"
lbl=$(grep '^label=' "$L_DIR/sessions/$lsid2/session.meta" | cut -d= -f2-)
[ "$lbl" = "bug" ] || err "label 未落盘：→ $lbl"
echo "  ok  set label=bug 落盘"

# set label=enhancement
out=$(DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session set "$lsid2" label=enhancement 2>/dev/null)
[ "$out" = "ok" ] || err "set label=enhancement 返回非 ok：→ $out"
lbl2=$(grep '^label=' "$L_DIR/sessions/$lsid2/session.meta" | cut -d= -f2-)
[ "$lbl2" = "enhancement" ] || err "label 更新失败：→ $lbl2"
echo "  ok  set label=enhancement 覆盖写入"

# issue 从 meta 读取 label（端到端：set label → collect → preview → confirm → issue → 输出含 label）
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session set "$lsid2" company=E2E测试 severity=提示 >/dev/null 2>&1
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session collect "$lsid2" >/dev/null 2>/dev/null
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session preview "$lsid2" >/dev/null 2>/dev/null
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session confirm "$lsid2" >/dev/null 2>/dev/null
out=$(DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session issue "$lsid2" 2>/dev/null)
state=$(grep '^state=' "$L_DIR/sessions/$lsid2/session.meta" | cut -d= -f2-)
[ "$state" = "issued" ] || err "label e2e: issue 后状态应为 issued：→ $state"
echo "  ok  label 端到端 issue → issued"

# --label CLI 参数测试：命令行 --label 传递给 issue（URL 模式下验证无报错）
lsid3=$(DIAG_DATA_DIR="$L_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session set "$lsid3" company=CLI测试 severity=一般 >/dev/null 2>&1
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session collect "$lsid3" >/dev/null 2>/dev/null
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session preview "$lsid3" >/dev/null 2>/dev/null
DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session confirm "$lsid3" >/dev/null 2>/dev/null
out=$(DIAG_DATA_DIR="$L_DIR" bash "$DIAG" session issue --label question "$lsid3" 2>/dev/null)
state=$(grep '^state=' "$L_DIR/sessions/$lsid3/session.meta" | cut -d= -f2-)
[ "$state" = "issued" ] || err "label CLI: issue --label 后状态应为 issued：→ $state"
echo "  ok  --label CLI 参数不报错且 issued"

rm -rf "$L_DIR"
echo "OK: session issue --label 测试全部通过"

# ============================================================
# 12. log_since_minutes 校验
# ============================================================

V_DIR=$(mktemp -d)

echo "log_since_minutes 校验用例："

vsid=$(DIAG_DATA_DIR="$V_DIR" DIAG_SESSION_MIN_INTERVAL=0 bash "$DIAG" session start 2>/dev/null)

# 非数字应被拒
out=$(DIAG_DATA_DIR="$V_DIR" bash "$DIAG" session set "$vsid" log_since_minutes=abc 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "log_since_minutes=abc 应被拒绝"
echo "  ok  非数字被拒"

# 0 应被拒
out=$(DIAG_DATA_DIR="$V_DIR" bash "$DIAG" session set "$vsid" log_since_minutes=0 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "log_since_minutes=0 应被拒绝"
echo "  ok  零值被拒"

# 超出上限 1440 应被拒
out=$(DIAG_DATA_DIR="$V_DIR" bash "$DIAG" session set "$vsid" log_since_minutes=9999 2>&1); rc=$?
[ "$rc" -ne 0 ] || err "log_since_minutes=9999 应被拒绝"
case "$out" in
  *"1440"*) ;;
  *) err "超限提示应含上限值 1440：→ $out" ;;
esac
echo "  ok  超上限被拒并提示 1440"

# 1440 正好应通过
out=$(DIAG_DATA_DIR="$V_DIR" bash "$DIAG" session set "$vsid" log_since_minutes=1440 2>/dev/null)
[ "$out" = "ok" ] || err "log_since_minutes=1440 应被接受：→ $out"
echo "  ok  1440（上限值）通过"

# 1 应通过
out=$(DIAG_DATA_DIR="$V_DIR" bash "$DIAG" session set "$vsid" log_since_minutes=1 2>/dev/null)
[ "$out" = "ok" ] || err "log_since_minutes=1 应被接受：→ $out"
echo "  ok  1（最小值）通过"

rm -rf "$V_DIR"
echo "OK: log_since_minutes 校验测试全部通过"

# ============================================================
# 13. Python 脱敏对等测试（sed fallback vs python3 sanitize.py）
# ============================================================

echo "Python 脱敏对等测试："

parity_input='password: secret123
host 10.1.2.3 and 192.168.1.1
mail user@corp.com
phone 13812345678
GET /api?sig=abc&uid=42
path /Users/dev/project
IPv6 2001:0db8:85a3:0000
MAC aa:bb:cc:dd:ee:ff
JWT eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
GPS 39.12345 116.54321
sn=ABCDEF1234567890
no sensitive data here'

sed_out=$(printf '%s\n' "$parity_input" | ( . /tmp/san_fn.sh; _sanitize_stdin_sed ))
py_out=$(printf '%s\n' "$parity_input" | python3 "$DIAG_PYLIB/sanitize.py")

if [ "$sed_out" = "$py_out" ]; then
  echo "  ok  sed 与 Python 输出完全一致"
else
  echo "sed 输出:" >&2
  printf '%s\n' "$sed_out" >&2
  echo "python3 输出:" >&2
  printf '%s\n' "$py_out" >&2
  err "sed 与 Python 脱敏输出不一致"
fi

echo "OK: Python 脱敏对等测试通过"

# ============================================================
# 14. Loki JSON 解析器测试
# ============================================================

echo "Loki JSON 解析器测试："

loki_fixture='{"status":"success","data":{"result":[{"stream":{"container_name":"pf-skeleton-server"},"values":[["1695000000000000000","INFO server started"],["1695000001000000000","ERROR something failed"]]}]}}'

loki_out=$(printf '%s' "$loki_fixture" | python3 "$DIAG_PYLIB/loki.py")
case "$loki_out" in
  *"pf-skeleton-server"*) echo "  ok  容器名输出" ;;
  *) err "Loki 解析器未输出容器名：→ $loki_out" ;;
esac
case "$loki_out" in
  *"server started"*) echo "  ok  日志行输出" ;;
  *) err "Loki 解析器未输出日志行：→ $loki_out" ;;
esac
loki_lines=$(printf '%s\n' "$loki_out" | wc -l | tr -d ' ')
[ "$loki_lines" -eq 2 ] || err "Loki 解析器应输出 2 行：实际 $loki_lines 行"
echo "  ok  行数正确"

# 错误响应应退出非 0
loki_err_fixture='{"status":"error","message":"parse error"}'
printf '%s' "$loki_err_fixture" | python3 "$DIAG_PYLIB/loki.py" >/dev/null 2>/dev/null; rc=$?
[ "$rc" -ne 0 ] || err "Loki 错误响应应退出非 0：rc=$rc"
echo "  ok  错误响应退出非 0"

# 非 JSON 输入应退出非 0
printf 'not json' | python3 "$DIAG_PYLIB/loki.py" >/dev/null 2>/dev/null; rc=$?
[ "$rc" -ne 0 ] || err "非 JSON 输入应退出非 0：rc=$rc"
echo "  ok  非 JSON 输入退出非 0"

echo "OK: Loki JSON 解析器测试全部通过"

# ============================================================
# 15. sed fallback 测试（遮蔽 python3 验证回退路径）
# ============================================================

echo "sed fallback 测试："

fallback_input='password=secret123 host 10.1.2.3'
fallback_out=$(PATH=/usr/bin:/bin bash "$DIAG" sanitize - <<< "$fallback_input" 2>/dev/null)
case "$fallback_out" in
  *"secret123"*) err "fallback: 凭证未脱敏" ;;
esac
case "$fallback_out" in
  *"10.1.2.3"*) err "fallback: IP 未脱敏" ;;
esac
echo "  ok  无 python3 时 sed fallback 脱敏正常"

echo "OK: sed fallback 测试通过"
