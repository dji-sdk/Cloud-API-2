# lib/session.sh — session 结构化生命周期

# ------------------------------------------------------------
# 5.5 session 结构化生命周期：状态机驱动的反馈闭环
#     状态流：created → collected → previewed → confirmed → issued
#     数据目录：$SESSION_DIR/<id>/（session.meta + evidence + sanitized）
# ------------------------------------------------------------

session_get() {
  local meta="$1" key="$2"
  grep "^${key}=" "$meta" 2>/dev/null | head -1 | cut -d= -f2-
}

# session.meta 是一行一个 key=value，值里的换行会伪造出一整行新 key
# （如 error_text=$'x\nstate=issued' 可绕过状态机），故此处拒绝而非静默折行：
# error_text 对外承诺「原样记录」，静默改写会破坏证据保真
session_value_invalid() {
  case "$1" in
    *'
'*) return 0 ;;
  esac
  [ "${#1}" -gt "$SESSION_VALUE_MAX_LEN" ] && return 0
  return 1
}

session_put() {
  local meta="$1" key="$2" value="$3" tmp="${1}.tmp"
  if session_value_invalid "$value"; then
    echo "错误: '$key' 的值不能包含换行，且长度不得超过 ${SESSION_VALUE_MAX_LEN} 字符" >&2
    return 1
  fi
  if grep -q "^${key}=" "$meta" 2>/dev/null; then
    grep -v "^${key}=" "$meta" > "$tmp"
    printf '%s=%s\n' "$key" "$value" >> "$tmp"
    mv "$tmp" "$meta"
  else
    printf '%s=%s\n' "$key" "$value" >> "$meta"
  fi
}

session_require() {
  local meta="$1" expected="$2"
  local current=""
  current=$(session_get "$meta" "state")
  if [ "$current" != "$expected" ]; then
    echo "错误: 当前状态为 '$current'，需要 '$expected' 才能执行此操作" >&2
    return 1
  fi
}

session_transition() {
  local meta="$1" new_state="$2"
  session_put "$meta" "state" "$new_state"
  session_put "$meta" "${new_state}_at" "$(date '+%Y-%m-%d %H:%M:%S')"
}

cleanup_sessions() {
  local days="${1:-$SESSION_TTL_DAYS}"
  [ -d "$SESSION_DIR" ] || return 0
  local now_ts="" cutoff_ts=0 cleaned=0
  now_ts=$(date '+%s')
  cutoff_ts=$((now_ts - days * 86400))
  local d=""
  for d in "$SESSION_DIR"/*/; do
    [ -d "$d" ] || continue
    local m="$d/session.meta"
    [ -f "$m" ] || continue
    local ca=""
    ca=$(grep '^created_at=' "$m" 2>/dev/null | head -1 | cut -d= -f2-)
    [ -n "$ca" ] || continue
    local ts=0
    ts=$(date -j -f '%Y-%m-%d %H:%M:%S' "$ca" '+%s' 2>/dev/null || date -d "$ca" '+%s' 2>/dev/null || echo 0)
    if [ "$ts" -gt 0 ] && [ "$ts" -lt "$cutoff_ts" ]; then
      rm -rf "$d"
      cleaned=$((cleaned + 1))
    fi
  done
  [ "$cleaned" -gt 0 ] && echo "已清理 ${cleaned} 个过期 session（>${days} 天）" >&2
  return 0
}

# 打包 session 日志（从 collect 提取的独立函数，也供 session pack 调用）
# 参数：sid [extra_file1 extra_file2 ...]
# 返回：zip 文件路径（stdout），进度信息（stderr）
session_do_pack() {
  local sid="$1"; shift
  local sdir="$SESSION_DIR/$sid"
  [ -d "$sdir" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
  # 复制额外附件到 attachments/
  if [ $# -gt 0 ]; then
    mkdir -p "$sdir/attachments"
    local f=""
    for f in "$@"; do
      if [ -f "$f" ]; then
        cp "$f" "$sdir/attachments/" 2>/dev/null && echo "已添加附件: $(basename "$f")" >&2
      else
        echo "警告: 附件不存在，跳过: $f" >&2
      fi
    done
  fi
  local zip_file="$sdir/session-${sid}-logs.zip"
  progress "打包日志文件"
  local zip_contents=""
  [ -d "$sdir/logs" ] && zip_contents="logs/"
  [ -f "$sdir/evidence.txt" ] && zip_contents="$zip_contents evidence.txt"
  [ -f "$sdir/trace_evidence.txt" ] && zip_contents="$zip_contents trace_evidence.txt"
  [ -d "$sdir/attachments" ] && [ "$(ls -A "$sdir/attachments" 2>/dev/null)" ] && zip_contents="$zip_contents attachments/"
  if [ -z "$zip_contents" ]; then
    progress_done "打包日志文件"
    echo "警告: 无可打包内容" >&2
    return 1
  fi
  (cd "$sdir" && zip -q -r "session-${sid}-logs.zip" $zip_contents 2>/dev/null) || true
  progress_done "打包日志文件"
  if [ -f "$zip_file" ]; then
    local zip_size=0
    zip_size=$(wc -c < "$zip_file" | tr -d ' ')
    local log_count=0
    [ -d "$sdir/logs" ] && log_count=$(ls "$sdir/logs/" 2>/dev/null | wc -l | tr -d ' ')
    local attach_count=0
    [ -d "$sdir/attachments" ] && attach_count=$(ls "$sdir/attachments/" 2>/dev/null | wc -l | tr -d ' ')
    echo "已打包：session-${sid}-logs.zip（${zip_size} 字节，${log_count} 个服务日志$([ "$attach_count" -gt 0 ] && echo "，${attach_count} 个附件")）" >&2
    local zip_mb=$((zip_size / 1048576))
    if [ "$zip_mb" -ge "$MAX_PACKAGE_SIZE_HARD_MB" ]; then
      warn "包大小 ${zip_mb}MB 超出硬上限 ${MAX_PACKAGE_SIZE_HARD_MB}MB，正在裁剪最大日志文件..."
      while [ "$zip_mb" -ge "$MAX_PACKAGE_SIZE_HARD_MB" ] && [ "$(ls "$sdir/logs/" 2>/dev/null | wc -l)" -gt 0 ]; do
        local largest=""
        largest=$(ls -S "$sdir/logs/" 2>/dev/null | head -1)
        [ -n "$largest" ] || break
        rm -f "$sdir/logs/$largest"
        (cd "$sdir" && zip -q -r "session-${sid}-logs.zip" $zip_contents 2>/dev/null) || true
        zip_size=$(wc -c < "$zip_file" | tr -d ' ')
        zip_mb=$((zip_size / 1048576))
        echo "  已移除 ${largest}，当前包大小 ${zip_mb}MB" >&2
      done
    elif [ "$zip_mb" -ge "$MAX_PACKAGE_SIZE_MB" ]; then
      warn "包大小 ${zip_mb}MB 超出建议上限 ${MAX_PACKAGE_SIZE_MB}MB，建议减少采集范围"
    fi
  fi
  [ -f "$zip_file" ] && echo "$zip_file"
}

# 输出采集总览（结构化摘要：文件列表/大小/路径）
session_print_summary() {
  local sid="$1"
  local sdir="$SESSION_DIR/$sid"
  [ -d "$sdir" ] || return 0
  echo "" >&2
  echo "═══ 采集总览 ═══" >&2
  if [ -f "$sdir/evidence.txt" ]; then
    local ev_kb=0
    ev_kb=$(( $(wc -c < "$sdir/evidence.txt" | tr -d ' ') / 1024 ))
    echo "  环境信息: evidence.txt (${ev_kb}KB)" >&2
  fi
  if [ -f "$sdir/trace_evidence.txt" ]; then
    local tr_kb=0
    tr_kb=$(( $(wc -c < "$sdir/trace_evidence.txt" | tr -d ' ') / 1024 ))
    echo "  Trace 日志: trace_evidence.txt (${tr_kb}KB)" >&2
  fi
  if [ -d "$sdir/logs" ] && [ "$(ls -A "$sdir/logs" 2>/dev/null)" ]; then
    echo "  服务日志:" >&2
    local total_bytes=0 count=0 f=""
    for f in "$sdir/logs/"*; do
      [ -f "$f" ] || continue
      local fname="" fsize=0 fkb=0 role=""
      fname=$(basename "$f")
      fsize=$(wc -c < "$f" | tr -d ' ')
      fkb=$((fsize / 1024))
      total_bytes=$((total_bytes + fsize))
      count=$((count + 1))
      local svc_name="${fname%.txt}"
      role=$(service_role "$svc_name" 2>/dev/null || echo "")
      echo "    ${fname}${role:+ ($role)} — ${fkb}KB" >&2
    done
    echo "  合计: ${count} 个服务日志, $((total_bytes / 1024))KB" >&2
  fi
  if [ -d "$sdir/attachments" ] && [ "$(ls -A "$sdir/attachments" 2>/dev/null)" ]; then
    local ac=0
    ac=$(ls "$sdir/attachments/" 2>/dev/null | wc -l | tr -d ' ')
    echo "  附件: ${ac} 个文件" >&2
  fi
  if [ -f "$sdir/session-${sid}-logs.zip" ]; then
    local zs=0
    zs=$(( $(wc -c < "$sdir/session-${sid}-logs.zip" | tr -d ' ') / 1024 ))
    echo "  压缩包: session-${sid}-logs.zip (${zs}KB)" >&2
  fi
  echo "  路径: $sdir/" >&2
  echo "═══════════════" >&2
}

run_session() {
  local subcmd="${1:-status}"
  shift 2>/dev/null || true
  case "$subcmd" in
    start)
      ensure_data_dir || return 1
      # E4a: 限频检查——距上次 session 不足 N 秒则拒绝
      if [ -d "$SESSION_DIR" ]; then
        local latest_ts=0 now_ts=""
        now_ts=$(date '+%s')
        local meta_file=""
        for meta_file in "$SESSION_DIR"/*/session.meta; do
          [ -f "$meta_file" ] || continue
          local ca=""
          ca=$(grep '^created_at=' "$meta_file" 2>/dev/null | head -1 | cut -d= -f2-)
          [ -n "$ca" ] || continue
          local ts=0
          ts=$(date -j -f '%Y-%m-%d %H:%M:%S' "$ca" '+%s' 2>/dev/null || date -d "$ca" '+%s' 2>/dev/null || echo 0)
          [ "$ts" -gt "$latest_ts" ] && latest_ts=$ts
        done
        if [ "$latest_ts" -gt 0 ]; then
          local elapsed=$((now_ts - latest_ts))
          if [ "$elapsed" -lt "$SESSION_MIN_INTERVAL_SEC" ]; then
            local remain=$((SESSION_MIN_INTERVAL_SEC - elapsed))
            echo "错误: 距上次 session 仅 ${elapsed} 秒，请等待 ${remain} 秒后重试（限频 ${SESSION_MIN_INTERVAL_SEC} 秒）" >&2
            return 1
          fi
        fi
      fi
      # E6b: 自动清理过期 session
      cleanup_sessions "$SESSION_TTL_DAYS" 2>/dev/null || true
      local sid
      sid=$(date '+%Y%m%d-%H%M%S')
      local sdir="$SESSION_DIR/$sid"
      # 同一秒内连续 start 会撞名并覆盖前一个 session，加序号后缀保证唯一
      local seq=1
      while [ -d "$sdir" ]; do
        sid="$(date '+%Y%m%d-%H%M%S')-$seq"
        sdir="$SESSION_DIR/$sid"
        seq=$((seq + 1))
      done
      mkdir -p "$sdir"
      local meta="$sdir/session.meta"
      {
        printf 'id=%s\n' "$sid"
        printf 'state=created\n'
        printf 'created_at=%s\n' "$(date '+%Y-%m-%d %H:%M:%S')"
        printf 'error_text=\n'
        printf 'trace_id=\n'
        printf 'severity=\n'
        printf 'contact=\n'
        printf 'sdk_version=\n'
        printf 'problem_scope=\n'
        printf 'frequency=\n'
        printf 'cloud_version=\n'
        printf 'remote_host=\n'
        printf 'docker_container=\n'
        printf 'repro_steps=\n'
        printf 'log_links=\n'
        printf 'level=\n'
        printf 'error_codes=\n'
      } > "$meta"
      echo "$sid"
      ;;
    set)
      local sid="${1:-}"
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session set <id> key=value ..." >&2; return 1; }
      shift
      local sdir="$SESSION_DIR/$sid"
      local meta="$sdir/session.meta"
      [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      local state=""
      state=$(session_get "$meta" "state")
      case "$state" in
        created|collected|previewed|confirmed) ;;
        *) echo "错误: 状态 '$state' 下不能 set（需 created、collected、previewed 或 confirmed）" >&2; return 1 ;;
      esac
      local kv="" key="" val="" bad=0
      for kv in "$@"; do
        key="${kv%%=*}"
        val="${kv#*=}"
        case " $SESSION_ALLOWED_KEYS " in
          *" $key "*)
            if session_value_invalid "$val"; then
              echo "错误: '$key' 的值不能包含换行，且长度不得超过 ${SESSION_VALUE_MAX_LEN} 字符" >&2
              bad=1
              continue
            fi
            # severity 必须落在 issue 表单的下拉选项内，否则用户到表单里填不上
            if [ "$key" = "severity" ]; then
              case " $SEVERITY_ALLOWED_VALUES " in
                *" $val "*) ;;
                *) echo "错误: severity '$val' 不是表单选项（可选: ${SEVERITY_ALLOWED_VALUES}）" >&2; bad=1; continue ;;
              esac
            fi
            if [ "$key" = "frequency" ]; then
              case " $FREQUENCY_ALLOWED_VALUES " in
                *" $val "*) ;;
                *) echo "错误: frequency '$val' 不是表单选项（可选: ${FREQUENCY_ALLOWED_VALUES}）" >&2; bad=1; continue ;;
              esac
            fi
            if [ "$key" = "problem_scope" ]; then
              case "$val" in
                "上云 API 2 底座"|SDK|"前端组件") ;;
                *) echo "错误: problem_scope '$val' 不是表单选项（可选: 上云 API 2 底座 / SDK / 前端组件）" >&2; bad=1; continue ;;
              esac
            fi
            if [ "$key" = "log_since_minutes" ]; then
              case "$val" in
                ''|*[!0-9]*) echo "错误: log_since_minutes '$val' 必须是正整数（分钟数）" >&2; bad=1; continue ;;
              esac
              if [ "$val" -gt 1440 ]; then
                echo "错误: log_since_minutes '$val' 超出上限（最大 1440，即 24 小时）" >&2; bad=1; continue
              fi
              if [ "$val" -eq 0 ]; then
                echo "错误: log_since_minutes 不能为 0" >&2; bad=1; continue
              fi
            fi
            session_put "$meta" "$key" "$val"
            ;;
          *) echo "警告: 忽略未知 key '$key'（允许: ${SESSION_ALLOWED_KEYS}）" >&2 ;;
        esac
      done
      [ "$bad" -eq 0 ] || return 1
      echo "ok"
      ;;
    collect)
      local sid="${1:-}" no_zip=0
      # 支持 --no-zip 参数：采集但不打包
      while [ $# -gt 0 ]; do
        case "$1" in
          --no-zip) no_zip=1; shift ;;
          *) sid="$1"; shift ;;
        esac
      done
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session collect [--no-zip] <id>" >&2; return 1; }
      local sdir="$SESSION_DIR/$sid"
      local meta="$sdir/session.meta"
      [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      session_require "$meta" "created" || return 1
      local remote_host="" docker_container=""
      remote_host=$(session_get "$meta" "remote_host")
      docker_container=$(session_get "$meta" "docker_container")
      # 读取用户设置的日志时间范围
      local log_since=""
      log_since=$(session_get "$meta" "log_since_minutes")
      if [ -n "$log_since" ]; then
        DIAG_LOG_SINCE_MINUTES="$log_since"
        LOKI_TIME_RANGE_MINUTES="$log_since"
        export DIAG_LOG_SINCE_MINUTES
      fi
      if [ -n "$remote_host" ]; then
        if ! collect_remote "$remote_host" "$docker_container" "$sdir" > "$sdir/evidence.txt"; then
          echo "错误: 远程采集失败，session 保持 created 状态" >&2
          return 1
        fi
      else
        collect > "$sdir/evidence.txt"
      fi
      local tid=""
      tid=$(session_get "$meta" "trace_id")
      if [ -n "$tid" ]; then
        collect_by_trace "$tid" > "$sdir/trace_evidence.txt" 2>/dev/null || true
      fi
      local snapshot="" level=""
      snapshot=$(cat "$sdir/evidence.txt")
      local evidence_arr=()
      evidence_arr=( $(printf '%s\n' "$snapshot" \
        | sed -n '/^== sdk_log ==/,/^== /p' \
        | grep -Eo 'ERROR_[A-Z_]+|NPE' | sort -u) )
      # 从所有日志文件中也提取 6 位设备侧错误码（100000-999999）
      local device_codes=()
      if [ -d "$sdir/logs" ]; then
        device_codes=( $(grep -rhoE '\b[1-9][0-9]{5}\b' "$sdir/logs/" 2>/dev/null | sort -un) )
      fi
      if [ "${#evidence_arr[@]}" -eq 0 ]; then
        if key_collection_missing "$snapshot"; then
          level="L3"
        else
          level="L2"
        fi
        session_put "$meta" "error_codes" ""
      else
        # L1 = 词典命中；采到词典外错误码属零命中，与 classify 保持一致
        # （据 classification.yaml：零命中 → L2）
        level="L2"
        for code in "${evidence_arr[@]}"; do
          if is_dict_hit "$code"; then
            level="L1"
            break
          fi
        done
        session_put "$meta" "error_codes" "$(printf '%s\n' "${evidence_arr[@]}" | paste -sd+ -)"
      fi
      session_put "$meta" "level" "$level"
      session_transition "$meta" "collected"
      # E4b: 内容指纹去重
      if [ -f "$sdir/evidence.txt" ]; then
        local ev_hash=""
        ev_hash=$(md5 -q "$sdir/evidence.txt" 2>/dev/null || md5sum "$sdir/evidence.txt" 2>/dev/null | cut -d' ' -f1 || echo "")
        if [ -n "$ev_hash" ]; then
          session_put "$meta" "evidence_hash" "$ev_hash"
          local dup_sid="" dup_meta=""
          for dup_meta in "$SESSION_DIR"/*/session.meta; do
            [ -f "$dup_meta" ] || continue
            [ "$dup_meta" = "$meta" ] && continue
            local dh=""
            dh=$(grep '^evidence_hash=' "$dup_meta" 2>/dev/null | head -1 | cut -d= -f2-)
            if [ "$dh" = "$ev_hash" ]; then
              dup_sid=$(grep '^id=' "$dup_meta" 2>/dev/null | head -1 | cut -d= -f2-)
              warn "与 session ${dup_sid} 的证据包相同（hash=${ev_hash}），可能是重复提交"
              break
            fi
          done
        fi
      fi
      # 打包日志文件：如果 logs/ 目录存在（远程采集），zip 打包
      local zip_file=""
      if [ "$no_zip" -eq 0 ] && [ -d "$sdir/logs" ] && [ "$(ls -A "$sdir/logs" 2>/dev/null)" ]; then
        zip_file=$(session_do_pack "$sid")
      fi
      # 采集总览
      session_print_summary "$sid"
      # 大日志检测：超出 GitHub issue body 上限时提示上传网盘
      local ev_size=0
      if [ -n "$zip_file" ] && [ -f "$zip_file" ]; then
        ev_size=$(wc -c < "$zip_file" | tr -d ' ')
      else
        ev_size=$(wc -c < "$sdir/evidence.txt" | tr -d ' ')
      fi
      if [ "$ev_size" -gt "$MAX_ISSUE_BODY" ]; then
        echo "" >&2
        echo "警告: 日志较大（${ev_size} 字节），超出 GitHub issue 承载上限。" >&2
        if [ -n "$zip_file" ] && [ -f "$zip_file" ]; then
          echo "请将 $zip_file 上传到网盘后，把下载链接填入 issue 的「日志下载链接」字段：" >&2
        else
          echo "请将 $sdir/evidence.txt 上传到网盘后，把下载链接填入 issue 的「日志下载链接」字段：" >&2
        fi
        if [ -n "$UPLOAD_URL" ]; then
          echo "  上传地址：$(mask_url "$UPLOAD_URL")（完整地址请联系管理员或设置 DIAG_UPLOAD_URL 环境变量）" >&2
        else
          echo "  未配置默认上传地址，请自行准备网盘/对象存储，并通过 DIAG_UPLOAD_URL 环境变量指定" >&2
        fi
      fi
      echo "已采集：evidence.txt$([ -n "$tid" ] && echo ' + trace_evidence.txt')$([ -n "$zip_file" ] && [ -f "$zip_file" ] && echo " + session-${sid}-logs.zip")，判级 $level"
      ;;
    preview)
      local sid="${1:-}"
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session preview <id>" >&2; return 1; }
      local sdir="$SESSION_DIR/$sid"
      local meta="$sdir/session.meta"
      [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      session_require "$meta" "collected" || return 1
      {
        cat "$sdir/evidence.txt"
        [ -f "$sdir/trace_evidence.txt" ] && cat "$sdir/trace_evidence.txt"
        # 包含 logs/ 目录下的各服务日志文件
        if [ -d "$sdir/logs" ]; then
          for lf in "$sdir/logs/"*.txt; do
            [ -f "$lf" ] || continue
            echo ""
            echo "== $(basename "$lf" .txt) =="
            cat "$lf"
          done
        fi
        true
      } | sanitize_with_stats > "$sdir/sanitized.md" 2>"$sdir/stats.txt"
      session_transition "$meta" "previewed"
      echo "=== 脱敏预览 ==="
      cat "$sdir/sanitized.md"
      echo
      echo "=== 脱敏统计 ==="
      cat "$sdir/stats.txt"
      echo
      echo "确认无敏感信息后运行: diag.sh session confirm $sid"
      ;;
    confirm)
      local sid="${1:-}"
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session confirm <id>" >&2; return 1; }
      local sdir="$SESSION_DIR/$sid"
      local meta="$sdir/session.meta"
      [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      session_require "$meta" "previewed" || return 1
      session_transition "$meta" "confirmed"
      echo "已确认。提交 issue: diag.sh session issue $sid"
      ;;
    issue)
      local sid="" use_gh=0 label_value=""
      # 解析参数：--gh / --label <label> / <id>
      while [ $# -gt 0 ]; do
        case "$1" in
          --gh) use_gh=1; shift ;;
          --label) shift; [ -n "${1:-}" ] && label_value="$1"; [ $# -gt 0 ] && shift ;;
          *) sid="$1"; shift ;;
        esac
      done
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session issue [--gh] [--label <label>] <id>" >&2; return 1; }
      local sdir="$SESSION_DIR/$sid"
      local meta="$sdir/session.meta"
      [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      session_require "$meta" "confirmed" || return 1
      # E5: per-ISV 提单配额检查 + 相似单检测
      local cur_company=""
      cur_company=$(session_get "$meta" "company")
      if [ -n "$cur_company" ]; then
        local issued_count=0 check_meta=""
        for check_meta in "$SESSION_DIR"/*/session.meta; do
          [ -f "$check_meta" ] || continue
          [ "$check_meta" = "$meta" ] && continue
          local cs="" st=""
          cs=$(grep '^company=' "$check_meta" 2>/dev/null | head -1 | cut -d= -f2-)
          st=$(grep '^state=' "$check_meta" 2>/dev/null | head -1 | cut -d= -f2-)
          [ "$cs" = "$cur_company" ] && [ "$st" = "issued" ] && issued_count=$((issued_count + 1))
        done
        if [ "$issued_count" -ge "$SESSION_MAX_ISSUES_PER_ISV" ]; then
          warn "企业「${cur_company}」已提交 ${issued_count} 个 issue（上限 ${SESSION_MAX_ISSUES_PER_ISV}），请确认是否继续"
        fi
      fi
      local cur_error=""
      cur_error=$(session_get "$meta" "error_text")
      if [ -n "$cur_error" ]; then
        local prefix="${cur_error:0:30}" sim_meta="" sim_id=""
        for sim_meta in "$SESSION_DIR"/*/session.meta; do
          [ -f "$sim_meta" ] || continue
          [ "$sim_meta" = "$meta" ] && continue
          local sim_st=""
          sim_st=$(grep '^state=' "$sim_meta" 2>/dev/null | head -1 | cut -d= -f2-)
          [ "$sim_st" = "issued" ] || continue
          if grep -q "^error_text=${prefix}" "$sim_meta" 2>/dev/null; then
            sim_id=$(grep '^id=' "$sim_meta" 2>/dev/null | head -1 | cut -d= -f2-)
            warn "与已提交的 session ${sim_id} 存在相似 error_text，请确认是否重复"
            break
          fi
        done
      fi
      local level="" error_codes="" error_text="" company="" severity="" contact=""
      local sdk_version="" problem_scope="" frequency="" cloud_version="" repro_steps="" log_links=""
      level=$(session_get "$meta" "level")
      error_codes=$(session_get "$meta" "error_codes")
      error_text=$(session_get "$meta" "error_text")
      company=$(session_get "$meta" "company")
      severity=$(session_get "$meta" "severity")
      contact=$(session_get "$meta" "contact")
      sdk_version=$(session_get "$meta" "sdk_version")
      problem_scope=$(session_get "$meta" "problem_scope")
      frequency=$(session_get "$meta" "frequency")
      cloud_version=$(session_get "$meta" "cloud_version")
      repro_steps=$(session_get "$meta" "repro_steps")
      log_links=$(session_get "$meta" "log_links")
      # 从 meta 读取 label（命令行 --label 优先）
      if [ -z "$label_value" ]; then
        label_value=$(session_get "$meta" "label")
      fi
      # 标题：[问题] <error_text 截断 50 字符>
      local title_text="${error_text:0:50}"
      [ ${#error_text} -gt 50 ] && title_text="${title_text}..."
      local title="[问题] ${title_text:-问题反馈}"
      # description 预填：联系人 + 诊断元数据 + dropdown 提示 + 复现步骤
      # 注意：GitHub Issue Forms 的 dropdown 字段不支持 URL 预填，
      # 因此将 severity/frequency/problem_scope 写入 description 供用户参考手动选择。
      local desc=""
      desc="联系人：${contact:-（未设置）}"
      desc="${desc}
Session：${sid}"
      desc="${desc}
判级：${level:-（未设置）}"
      [ -z "$error_codes" ] || desc="${desc}
错误码：${error_codes}"
      desc="${desc}

--- 以下为下拉字段，请在表单中手动选择 ---"
      [ -z "$severity" ]      || desc="${desc}
严重程度：${severity}"
      [ -z "$frequency" ]     || desc="${desc}
出现频率：${frequency}"
      [ -z "$problem_scope" ] || desc="${desc}
问题类型：${problem_scope}"
      if [ -n "$repro_steps" ]; then
        desc="${desc}

复现步骤：
${repro_steps}"
      else
        desc="${desc}

（请在此补充复现步骤与期望/实际结果）"
      fi
      # gh CLI 模式：直接创建 issue
      if [ "$use_gh" -eq 1 ]; then
        if ! have_cmd gh; then
          echo "错误: 未安装 gh CLI，请先安装: https://cli.github.com/" >&2
          return 1
        fi
        # 从 ISSUE_REPO URL 提取 owner/repo
        local gh_repo=""
        gh_repo=$(echo "$ISSUE_REPO" | sed 's|https\?://github\.com/||')
        # 构造 issue body（Markdown 格式，包含所有结构化字段）
        local body=""
        body="### 企业名称

${company:-（未填写）}

### 问题概要

${error_text:-（未填写）}

### 问题类型

${problem_scope:-SDK}

### 严重程度

${severity:-一般}

### 出现频率

${frequency:-unable_to_reproduce}

### 上云 API 2 版本

${cloud_version:-（未填写）}

### SDK 版本

${sdk_version:-（未填写）}

### 复现步骤 / 详细描述

${repro_steps:-（请补充复现步骤与期望/实际结果）}

联系人：${contact:-（未设置）}
Session：${sid}
判级：${level:-（未设置）}
$([ -n "$error_codes" ] && echo "错误码：${error_codes}")

### 日志下载链接

${log_links:-（无）}

### 问题截图

（请编辑此 issue 补充截图）"

        echo "=== 即将创建 issue ==="
        echo "仓库：${gh_repo}"
        echo "标题：${title}"
        echo "────────────────────────────────"
        printf '%s\n' "$body"
        echo "────────────────────────────────"
        # 创建 issue
        local issue_url=""
        issue_url=$(gh issue create \
          --repo "$gh_repo" \
          --title "$title" \
          --body "$body" \
          ${label_value:+--label "$label_value"} \
          2>&1)
        if [ $? -eq 0 ] && [ -n "$issue_url" ]; then
          echo ""
          echo "Issue 已创建：$issue_url"
          session_transition "$meta" "issued"
        else
          echo "错误: gh issue create 失败：$issue_url" >&2
          echo "回退到 URL 模式：请用 diag.sh session issue $sid（不带 --gh）" >&2
          return 1
        fi
      else
      # URL 模式（原有逻辑）
      # 构造预填 URL（只放短字段，避免 URL 过长终端无法点击）
      local url="${ISSUE_REPO}/issues/new?template=$(urlencode "$ISSUE_TEMPLATE_NAME")"
      url="${url}&title=$(urlencode "$title")"
      [ -z "$company" ]       || url="${url}&company=$(urlencode "$company")"
      [ -z "$error_text" ]    || url="${url}&summary=$(urlencode "$error_text")"
      [ -z "$severity" ]      || url="${url}&severity=$(urlencode "$severity")"
      [ -z "$frequency" ]     || url="${url}&frequency=$(urlencode "$frequency")"
      [ -z "$problem_scope" ] || url="${url}&problem-scope=$(urlencode "$problem_scope")"
      [ -z "$cloud_version" ] || url="${url}&cloud-version=$(urlencode "$cloud_version")"
      [ -z "$sdk_version" ]   || url="${url}&sdk-version=$(urlencode "$sdk_version")"
      [ -z "$log_links" ]     || url="${url}&log-links=$(urlencode "$log_links")"
      url="${url}&description=$(urlencode "$desc")"
      # 若 URL 超过 2000 字符，回退：去掉 description 和 summary（最长的两个）
      if [ ${#url} -gt 2000 ]; then
        url="${ISSUE_REPO}/issues/new?template=$(urlencode "$ISSUE_TEMPLATE_NAME")"
        url="${url}&title=$(urlencode "$title")"
        [ -z "$company" ]       || url="${url}&company=$(urlencode "$company")"
        [ -z "$severity" ]      || url="${url}&severity=$(urlencode "$severity")"
        [ -z "$frequency" ]     || url="${url}&frequency=$(urlencode "$frequency")"
        [ -z "$problem_scope" ] || url="${url}&problem-scope=$(urlencode "$problem_scope")"
        [ -z "$cloud_version" ] || url="${url}&cloud-version=$(urlencode "$cloud_version")"
        [ -z "$sdk_version" ]   || url="${url}&sdk-version=$(urlencode "$sdk_version")"
        [ -z "$log_links" ]     || url="${url}&log-links=$(urlencode "$log_links")"
      fi
      echo "$url"
      echo
      issue_required_warn "$company" "$severity" "$frequency"
      # 打印需要手动粘贴的长文本字段
      echo "────────────────────────────────"
      echo "以下内容请粘贴到「复现步骤 / 详细描述」："
      echo
      printf '%s\n' "$desc"
      echo "────────────────────────────────"
      echo "表单已预填，打开链接检查后提交。"
      session_transition "$meta" "issued"
      fi # end if use_gh
      ;;
    pack)
      local sid="${1:-}"
      shift 2>/dev/null || true
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session pack <id> [extra_file ...]" >&2; return 1; }
      local sdir="$SESSION_DIR/$sid"
      local meta="$sdir/session.meta"
      [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      local cur_state=""
      cur_state=$(session_get "$meta" "state")
      case "$cur_state" in
        collected|previewed|confirmed) ;;
        *) echo "错误: session 状态为 '$cur_state'，pack 需要 collected/previewed/confirmed" >&2; return 1 ;;
      esac
      local zip_file=""
      zip_file=$(session_do_pack "$sid" "$@")
      if [ -n "$zip_file" ] && [ -f "$zip_file" ]; then
        session_print_summary "$sid"
        echo "$zip_file"
      fi
      ;;
    status)
      local sid="${1:-}"
      if [ -n "$sid" ]; then
        local sdir="$SESSION_DIR/$sid"
        local meta="$sdir/session.meta"
        [ -f "$meta" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
        cat "$meta"
      else
        mkdir -p "$SESSION_DIR" 2>/dev/null
        local found=0 d="" m="" s_id="" s_state="" s_level=""
        for d in "$SESSION_DIR"/*/; do
          [ -d "$d" ] || continue
          m="$d/session.meta"
          [ -f "$m" ] || continue
          found=1
          s_id=$(session_get "$m" "id")
          s_state=$(session_get "$m" "state")
          s_level=$(session_get "$m" "level")
          printf '%s\t%s\t%s\n' "$s_id" "$s_state" "${s_level:----}"
        done
        if [ "$found" -eq 0 ]; then
          echo "无 session。创建: diag.sh session start"
        fi
      fi
      ;;
    show)
      local sid="${1:-}"
      [ -n "$sid" ] || { echo "错误: 用法 diag.sh session show <id>" >&2; return 1; }
      local sdir="$SESSION_DIR/$sid"
      [ -d "$sdir" ] || { echo "错误: session '$sid' 不存在" >&2; return 1; }
      if [ -f "$sdir/sanitized.md" ]; then
        cat "$sdir/sanitized.md"
      elif [ -f "$sdir/evidence.txt" ]; then
        cat "$sdir/evidence.txt"
      else
        echo "错误: session '$sid' 尚未采集（无 evidence.txt）" >&2
        return 1
      fi
      # 列出 logs/ 目录下的服务日志文件
      if [ -d "$sdir/logs" ] && [ "$(ls -A "$sdir/logs" 2>/dev/null)" ]; then
        echo ""
        echo "=== 服务日志文件 ==="
        for lf in "$sdir/logs/"*.txt; do
          [ -f "$lf" ] || continue
          local lf_size=0
          lf_size=$(wc -c < "$lf" | tr -d ' ')
          echo "  $(basename "$lf") (${lf_size} 字节)"
        done
        local zf="$sdir/session-${sid}-logs.zip"
        if [ -f "$zf" ]; then
          local zf_size=0
          zf_size=$(wc -c < "$zf" | tr -d ' ')
          echo "  session-${sid}-logs.zip (${zf_size} 字节)"
        fi
      fi
      ;;
    *)
      echo "用法: diag.sh session <start|set|collect|preview|confirm|issue|show|status> [args...]" >&2
      return 1
      ;;
  esac
}
