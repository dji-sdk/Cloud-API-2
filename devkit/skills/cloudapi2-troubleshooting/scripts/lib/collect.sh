# lib/collect.sh — 证据采集（本地 + SSH 远程 + Loki）

probe_loki() {
  local host_ip="$1" port=""
  for port in $LOKI_PROBE_PORTS; do
    if curl -s --connect-timeout 3 "http://${host_ip}:${port}/ready" >/dev/null 2>&1; then
      echo "http://${host_ip}:${port}"
      return 0
    fi
  done
  return 1
}

# 通过 Loki API 拉取指定容器的日志，输出纯文本（一行一条）
query_loki_container() {
  local loki_url="$1" container="$2" filter="${3:-}" minutes="${4:-$LOKI_TIME_RANGE_MINUTES}" limit="${5:-$LOKI_QUERY_LIMIT}"
  local safe_container="${container//./\\.}"
  local logql="{container_name=~\"${safe_container}\"}"
  if [ -n "$filter" ]; then
    # UUID 格式 trace_id 用结构化 json 过滤，精度更高
    if printf '%s' "$filter" | grep -qE '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'; then
      logql="${logql} | json | request_id=\`${filter}\`"
    else
      logql="${logql} |~ \"${filter}\""
    fi
  fi
  local now_s end_ns start_ns
  now_s=$(date +%s)
  end_ns="${now_s}000000000"
  start_ns=$(( now_s - minutes * 60 ))
  start_ns="${start_ns}000000000"
  local response=""
  response=$(curl -sG --connect-timeout 10 --max-time 30 \
    --data-urlencode "query=${logql}" \
    --data-urlencode "start=${start_ns}" \
    --data-urlencode "end=${end_ns}" \
    --data-urlencode "limit=${limit}" \
    "${loki_url}/loki/api/v1/query_range" 2>/dev/null) || return 1
  if have_cmd python3; then
    printf '%s' "$response" | python3 "${SCRIPT_DIR}/pylib/loki.py"
  else
    printf '%s\n' "$response"
  fi
}

# Loki 全局错误扫描：跨容器 grep error/panic/fatal，应用噪音过滤
query_loki_errors() {
  local loki_url="$1" minutes="${2:-$LOKI_TIME_RANGE_MINUTES}" limit="${3:-$LOKI_QUERY_LIMIT}"
  local logql='{container_name=~".+"} |~ "(?i)error|panic|fatal"'
  # 从外置噪音配置读取排除模式
  local noise_conf="${SCRIPT_DIR}/loki_noise_filter.conf"
  if [ -f "$noise_conf" ]; then
    while IFS= read -r pat; do
      [[ -z "$pat" || "$pat" =~ ^[[:space:]]*# ]] && continue
      logql="${logql} !~ \"${pat}\""
    done < "$noise_conf"
  fi
  local now_s end_ns start_ns
  now_s=$(date +%s)
  end_ns="${now_s}000000000"
  start_ns=$(( now_s - minutes * 60 ))
  start_ns="${start_ns}000000000"
  local response=""
  response=$(curl -sG --connect-timeout 10 --max-time 30 \
    --data-urlencode "query=${logql}" \
    --data-urlencode "start=${start_ns}" \
    --data-urlencode "end=${end_ns}" \
    --data-urlencode "limit=${limit}" \
    "${loki_url}/loki/api/v1/query_range" 2>/dev/null) || return 1
  if have_cmd python3; then
    printf '%s' "$response" | python3 "${SCRIPT_DIR}/pylib/loki.py"
  else
    printf '%s\n' "$response"
  fi
}

# Loki trace_id/request_id 专用查询：跨所有容器查找指定 UUID
query_loki_trace_id() {
  local loki_url="$1" uuid="$2" minutes="${3:-$LOKI_TIME_RANGE_MINUTES}" limit="${4:-$LOKI_QUERY_LIMIT}"
  local logql='{container_name=~".+"} | json | request_id=`'"${uuid}"'`'
  local now_s end_ns start_ns
  now_s=$(date +%s)
  end_ns="${now_s}000000000"
  start_ns=$(( now_s - minutes * 60 ))
  start_ns="${start_ns}000000000"
  local response=""
  response=$(curl -sG --connect-timeout 10 --max-time 30 \
    --data-urlencode "query=${logql}" \
    --data-urlencode "start=${start_ns}" \
    --data-urlencode "end=${end_ns}" \
    --data-urlencode "limit=${limit}" \
    "${loki_url}/loki/api/v1/query_range" 2>/dev/null) || return 1
  if have_cmd python3; then
    printf '%s' "$response" | python3 "${SCRIPT_DIR}/pylib/loki.py"
  else
    printf '%s\n' "$response"
  fi
}

# 容器日志文件 grep：docker logs 慢时的替代方案
# 通过 docker inspect 拿到宿主机日志文件路径，直接 grep，适用于大日志场景
# 参数：$1=_cr_ssh 函数引用（调用方已定义），$2=容器名，$3=grep 模式（默认 error|panic|fatal），$4=最大行数（默认 100）
query_container_log_file() {
  local ssh_func="$1" name="$2" pattern="${3:-error|panic|fatal}" max_lines="${4:-100}"
  $ssh_func "LOG_PATH=\$(docker inspect --format='{{.LogPath}}' '$name' 2>/dev/null) && [ -n \"\$LOG_PATH\" ] && [ -f \"\$LOG_PATH\" ] && grep -i '$pattern' \"\$LOG_PATH\" | tail -${max_lines}" 2>/dev/null
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
  for p in "${SDK_LOG_PATHS[@]}"; do
    [ -d "$p" ] && base_log="$p" && break
  done
  if [ -n "$base_log" ]; then
    local newest
    # 用 find -exec ... + 而不是 xargs：GNU xargs 无 -r 时，空输入仍会执行一次
    # `ls -t`（无参数），从而列出「当前工作目录」并把其中的文件当成 SDK 日志采进证据包。
    # find -exec 在无匹配时根本不执行，且跨 BSD/GNU 行为一致。
    newest=$(find "$base_log" -type f -name '*.log' -exec ls -t {} + 2>/dev/null | head -1)
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
  progress "采集环境信息"
  echo "== env ==";          collect_env
  progress_done "采集环境信息"
  progress "采集磁盘信息"
  echo "== disk ==";         collect_disk
  progress_done "采集磁盘信息"
  progress "采集网络信息"
  echo "== network ==";      collect_network
  progress_done "采集网络信息"
  progress "采集进程信息"
  echo "== process ==";      collect_process
  progress_done "采集进程信息"
  progress "采集 SDK 日志"
  echo "== sdk_log ==";      collect_sdk_log
  progress_done "采集 SDK 日志"
  echo "== action_whitelist =="; printf '%s\n' "${ACTION_WHITELIST[@]}"
}

# 远程采集：通过 SSH 采集远程机器的环境信息 + Docker 容器日志
# 参数：$1=remote_host（如 diag@192.0.2.10），$2=docker_container（可选，指定主容器），$3=session_dir（可选，存放日志文件）
# stdout → evidence.txt（环境摘要），各容器日志单独存为 $session_dir/<container>.txt
collect_remote() {
  local host="$1" container="${2:-}" sdir="${3:-}"
  local _cr_pass="${DIAG_SSH_PASS:-}"
  local _cr_use_sshpass=0
  if have_cmd sshpass; then
    _cr_use_sshpass=1
    if [ -z "$_cr_pass" ]; then
      printf '请输入 %s 的 SSH 密码: ' "$host" >&2
      read -rs _cr_pass
      echo >&2
    fi
  else
    echo "提示: 未安装 sshpass，将使用普通 ssh（可能需要交互输入密码或配置免密）" >&2
  fi

  _cr_ssh() {
    if [ "$_cr_use_sshpass" -eq 1 ]; then
      sshpass -p "$_cr_pass" ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 "$host" "$@"
    else
      ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 "$host" "$@"
    fi
  }

  progress "连接远程主机 $host"
  if ! _cr_ssh 'echo ok' >/dev/null 2>&1; then
    progress_done "连接远程主机失败"
    echo "错误: 无法 SSH 连接 ${host} (请检查地址/密码/网络)" >&2
    return 1
  fi
  progress_done "连接远程主机 $host"

  progress "采集远程环境信息"
  echo "== env =="
  _cr_ssh 'uname -srm 2>/dev/null; hostname 2>/dev/null; cat /etc/os-release 2>/dev/null | head -5; echo "uptime=$(uptime 2>/dev/null)"; echo "date=$(date 2>/dev/null)"' 2>/dev/null || echo "(采集失败)"
  progress_done "采集远程环境信息"

  progress "采集远程磁盘信息"
  echo "== disk =="
  _cr_ssh 'df -h 2>/dev/null | head -8' 2>/dev/null || echo "(采集失败)"
  progress_done "采集远程磁盘信息"

  progress "采集远程网络信息"
  echo "== network =="
  _cr_ssh 'ip -brief address 2>/dev/null | head -5 || ifconfig 2>/dev/null | head -20' 2>/dev/null || echo "(采集失败)"
  progress_done "采集远程网络信息"

  progress "采集远程进程信息"
  echo "== process =="
  _cr_ssh 'ss -tlnp 2>/dev/null | head -15 || netstat -tlnp 2>/dev/null | head -15' 2>/dev/null || echo "(采集失败)"
  progress_done "采集远程进程信息"

  # 容器日志：只采集 pf-* 前缀的应用服务容器，排除基础设施
  progress "发现 Docker 容器"
  local all_containers_raw=""
  all_containers_raw=$(_cr_ssh "docker ps --format '{{.Names}}' 2>/dev/null" 2>/dev/null || true)
  local all_count=0 container_list=()
  if [ -n "$all_containers_raw" ]; then
    while IFS= read -r c; do
      [ -n "$c" ] || continue
      all_count=$((all_count + 1))
      # 只采 pf-* 前缀，排除基础设施容器
      case "$c" in
        pf-*-mysql|pf-*-redis|pf-*-ssl|pf-*-web|pf-*-minio|pf-*simulator*|pf-devkit-mcp|pf-terra-*|*beacon-reporter*) ;;
        pf-*) container_list+=("$c") ;;
      esac
    done <<< "$all_containers_raw"
  fi
  # 如果用户指定了 docker_container（逗号分隔），也加入
  if [ -n "$container" ]; then
    local IFS_BAK="$IFS" uc=""
    IFS=','
    for uc in $container; do
      uc=$(echo "$uc" | tr -d ' ')
      [ -n "$uc" ] || continue
      local already=0
      for c in ${container_list[@]+"${container_list[@]}"}; do
        [ "$c" = "$uc" ] && already=1 && break
      done
      [ "$already" -eq 1 ] || container_list+=("$uc")
    done
    IFS="$IFS_BAK"
  fi
  progress_done "发现 ${all_count} 个容器，采集 ${#container_list[@]} 个应用服务"

  # E7: max_containers 上限截取
  if [ "${#container_list[@]}" -gt "$MAX_CONTAINERS" ]; then
    local total_found=${#container_list[@]}
    local sorted_list=()
    local skeleton_first=""
    for c in "${container_list[@]}"; do
      case "$c" in *skeleton-server*) skeleton_first="$c" ;; *) sorted_list+=("$c") ;; esac
    done
    IFS=$'\n' sorted_list=($(printf '%s\n' "${sorted_list[@]}" | sort)); unset IFS
    [ -n "$skeleton_first" ] && sorted_list=("$skeleton_first" "${sorted_list[@]}")
    container_list=("${sorted_list[@]:0:$MAX_CONTAINERS}")
    warn "发现 ${total_found} 个应用容器，超出上限 ${MAX_CONTAINERS}，仅采集前 ${MAX_CONTAINERS} 个"
  fi

  # evidence.txt 中记录容器列表概要（含异常高亮）
  echo "== containers =="
  echo "总计 ${all_count} 个运行中容器，采集 ${#container_list[@]} 个应用服务："
  local abnormal_list=()
  if [ "${#container_list[@]}" -gt 0 ]; then
    for c in "${container_list[@]}"; do
      local img="" role="" state=""
      role=$(service_role "$c")
      img=$(_cr_ssh "timeout 10 docker inspect --format='{{.Config.Image}} | Status={{.State.Status}}' '$c' 2>/dev/null" 2>/dev/null || echo "unknown")
      state=$(printf '%s' "$img" | sed -n 's/.*Status=//p')
      echo "  $c${role:+ ($role)}: $img"
      case "$state" in
        exited|restarting|dead) abnormal_list+=("$c ($role): $state") ;;
      esac
    done
    if [ "${#abnormal_list[@]}" -gt 0 ]; then
      echo ""
      echo "--- ${#abnormal_list[@]} ABNORMAL ---"
      for a in "${abnormal_list[@]}"; do echo "  $a"; done
    fi
  else
    echo "(无 pf-* 前缀的应用服务容器)"
  fi

  # Loki 自动探测：从本地尝试访问远程主机上的 Loki 服务
  local host_ip="${host#*@}"
  local loki_url=""
  progress "探测 Loki 日志服务"
  if loki_url=$(probe_loki "$host_ip"); then
    progress_done "发现 Loki: $loki_url"
  else
    loki_url=""
    progress_done "未发现 Loki，使用 docker logs"
  fi

  # 按服务采集日志：优先 Loki API，回退 docker logs
  if [ -n "$loki_url" ] && [ -n "$sdir" ] && [ "${#container_list[@]}" -gt 0 ]; then
    local logs_dir="$sdir/logs"
    mkdir -p "$logs_dir"
    local collected_count=0
    local trace_filter=""
    [ -f "$sdir/session.meta" ] && \
      trace_filter=$(grep '^trace_id=' "$sdir/session.meta" 2>/dev/null | head -1 | cut -d= -f2-)
    for c in "${container_list[@]}"; do
      local role=""
      role=$(service_role "$c")
      progress "Loki 查询: $c${role:+ ($role)}"
      local logfile="$logs_dir/${c}.txt"
      {
        echo "# Container: $c${role:+ — $role}"
        echo "# Source: Loki (${loki_url})"
        echo "# Query: container_name=~\"${c}\"${trace_filter:+ |~ \"${trace_filter}\"}"
        echo "# Time range: last ${LOKI_TIME_RANGE_MINUTES}m, limit ${LOKI_QUERY_LIMIT}"
        echo "# Collected: $(date '+%Y-%m-%d %H:%M:%S')"
        echo "# ---"
        query_loki_container "$loki_url" "$c" "$trace_filter" || echo "(Loki 查询失败)"
      } > "$logfile"
      collected_count=$((collected_count + 1))
      local line_count=0
      line_count=$(wc -l < "$logfile" 2>/dev/null || echo 0)
      progress_done "Loki 查询: $c ($line_count 行)"
    done
    echo "== docker_log =="
    echo "已通过 Loki 按服务查询日志（${collected_count} 个），见 logs/ 目录"
  elif [ -n "$sdir" ] && [ "${#container_list[@]}" -gt 0 ]; then
    local logs_dir="$sdir/logs"
    mkdir -p "$logs_dir"
    local collected_count=0
    for c in "${container_list[@]}"; do
      local role=""
      role=$(service_role "$c")
      progress "采集容器日志: $c${role:+ ($role)}"
      local logfile="$logs_dir/${c}.txt"
      local docker_log_arg="--tail $DEFAULT_DOCKER_LOG_LINES"
      [ "${DIAG_LOG_SINCE_MINUTES:-}" ] && docker_log_arg="--since ${DIAG_LOG_SINCE_MINUTES}m"
      {
        echo "# Container: $c${role:+ — $role}"
        echo "# Source: docker logs ($docker_log_arg)"
        echo "# Collected: $(date '+%Y-%m-%d %H:%M:%S')"
        echo "# ---"
        _cr_ssh "timeout 30 docker logs $docker_log_arg '$c' 2>&1" 2>/dev/null || echo "(日志采集失败或超时)"
      } > "$logfile"
      collected_count=$((collected_count + 1))
      progress_done "采集容器日志: $c${role:+ ($role)}"
    done
    echo "== docker_log =="
    echo "已按服务存为独立文件（${collected_count} 个），见 logs/ 目录"
  else
    echo "== docker_log =="
    if [ -n "$container" ]; then
      local docker_log_arg="--tail $DEFAULT_DOCKER_LOG_LINES"
      [ "${DIAG_LOG_SINCE_MINUTES:-}" ] && docker_log_arg="--since ${DIAG_LOG_SINCE_MINUTES}m"
      _cr_ssh "docker logs $docker_log_arg '$container' 2>&1" 2>/dev/null || echo "(容器日志采集失败)"
    else
      echo "(未指定 docker_container 且无 session 目录，跳过)"
    fi
  fi

  # SDK 日志：从主容器内搜索，单独存文件
  progress "采集 SDK 日志"
  local sdk_container="${container:-}"
  # 如果未指定主容器，自动查找 skeleton-server
  if [ -z "$sdk_container" ] && [ "${#container_list[@]}" -gt 0 ]; then
    for c in "${container_list[@]}"; do
      case "$c" in *skeleton-server*) sdk_container="$c"; break ;; esac
    done
  fi
  echo "== sdk_log =="
  if [ -n "$sdk_container" ]; then
    local sdk_newest=""
    sdk_newest=$(_cr_ssh "docker exec '$sdk_container' sh -c \"find /app /opt /home -name 'flight_hub_sdk_*.log' -o -name '*.log' 2>/dev/null | head -30\"" 2>/dev/null | grep -i 'flight_hub_sdk' | head -1)
    if [ -z "$sdk_newest" ]; then
      sdk_newest=$(_cr_ssh "docker exec '$sdk_container' sh -c \"find /app /opt /home /var/log -name '*.log' -type f 2>/dev/null | xargs ls -t 2>/dev/null | head -1\"" 2>/dev/null)
    fi
    if [ -n "$sdk_newest" ]; then
      echo "sdk_log_dir: container:${sdk_container}"
      echo "sdk_log_file: $(basename "$sdk_newest")"
      local sdk_content=""
      sdk_content=$(_cr_ssh "docker exec '$sdk_container' tail -n $DEFAULT_LOG_WINDOW_LINES '$sdk_newest' 2>/dev/null" || echo "(日志读取失败)")
      echo "$sdk_content" | sed 's/^/    /'
      # 同时存为独立文件
      if [ -n "$sdir" ]; then
        mkdir -p "$sdir/logs"
        {
          echo "# SDK Log from container: $sdk_container"
          echo "# File: $sdk_newest"
          echo "# Collected: $(date '+%Y-%m-%d %H:%M:%S')"
          echo "# ---"
          echo "$sdk_content"
        } > "$sdir/logs/sdk-log.txt"
      fi
    else
      echo "sdk_log_dir: 容器 ${sdk_container} 内未找到 SDK 日志文件"
    fi
  else
    local sdk_found=""
    for sp in "${SDK_LOG_PATHS[@]}"; do
      local remote_newest=""
      remote_newest=$(_cr_ssh "[ -d '$sp' ] && find '$sp' -type f -name '*.log' -exec ls -t {} + 2>/dev/null | head -1" 2>/dev/null)
      if [ -n "$remote_newest" ]; then
        echo "sdk_log_dir: $sp"
        echo "sdk_log_file: $(basename "$remote_newest")"
        _cr_ssh "tail -n $DEFAULT_LOG_WINDOW_LINES '$remote_newest' 2>/dev/null" | sed 's/^/    /' || echo "(日志读取失败)"
        sdk_found=1
        break
      fi
    done
    [ -n "$sdk_found" ] || echo "sdk_log_dir: 未找到常见 SDK 日志目录（说明日志未落盘或路径不同，需向用户确认）"
  fi
  progress_done "采集 SDK 日志"

  echo "== action_whitelist =="; printf '%s\n' "${ACTION_WHITELIST[@]}"
  unset -f _cr_ssh
}
