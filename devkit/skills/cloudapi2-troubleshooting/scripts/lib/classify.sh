# lib/classify.sh — 错误码分类 + 容器角色映射

is_dict_hit() {
  case "$1" in
    ERROR_INIT_FAILED|ERROR_INVALID_STATE|ERROR_DEVICE_NOT_FOUND|ERROR_CALLBACK_TIMEOUT|ERROR_LISTEN_FAILED|NPE)
      return 0 ;;
    *) return 1 ;;
  esac
}

classify_code() {
  local errcode="$1"
  if is_dict_hit "$errcode"; then
    echo "L1:错误码命中词典，只能给结论与建议（参考 error-codes.yaml fix 字段，不通过脚本执行）"
  else
    echo "L2:零命中，需升级；证据包缺关键采集项（环境/日志为空）时降级 L3"
  fi
}

service_role() {
  case "$1" in
    *beacon-reporter*)       echo "埋点上报" ;;
    *skeleton-server*)       echo "主后端/SDK" ;;
    *notify-service*)        echo "通知服务" ;;
    *gateway-service*)       echo "API 网关" ;;
    *media-*)                echo "媒体服务" ;;
    *live-*)                 echo "直播服务" ;;
    *wayline-*)              echo "航线服务" ;;
    *manage-service*)        echo "管理服务" ;;
    *thing-access*|*tas-*)   echo "设备接入" ;;
    *inventory-collector*)   echo "资产采集" ;;
    *control-core*)          echo "飞行控制" ;;
    *device-service*)        echo "设备管理" ;;
    *iot-core*)              echo "IoT 核心" ;;
    *traefik*)               echo "反向代理" ;;
    *mysql*|*postgres*|*timescaledb*) echo "数据库" ;;
    *redis*)                 echo "缓存" ;;
    *rabbitmq*)              echo "消息队列" ;;
    *emqx*|*mqtt*)            echo "MQTT Broker" ;;
    *minio*)                 echo "对象存储" ;;
    *mongo*)                 echo "文档数据库" ;;
    *)                       echo "" ;;
  esac
}

key_collection_missing() {
  local snapshot="$1"
  local env_body="" log_body=""
  env_body=$(printf '%s\n' "$snapshot" | sed -n '/^== env ==/,/^== /p' | grep -v '^== ' | grep -v '^[[:space:]]*$')
  log_body=$(printf '%s\n' "$snapshot" | sed -n '/^== sdk_log ==/,/^== /p' | grep -v '^== ' | grep -v '^[[:space:]]*$')
  if [ -z "$env_body" ]; then
    return 0
  fi
  if [ -z "$log_body" ]; then
    return 0
  fi
  case "$log_body" in
    *"未找到常见 SDK 日志目录"*|*"$COLLECT_EMPTY_DESC"*) return 0 ;;
  esac
  return 1
}

classify() {
  local -a evidence=()
  local line="" snapshot=""
  snapshot=$(collect 2>/dev/null)
  evidence=( $(printf '%s\n' "$snapshot" \
    | sed -n '/^== sdk_log ==/,/^== /p' \
    | grep -Eo 'ERROR_[A-Z_]+|NPE' | sort -u) )
  if [ "${#evidence[@]}" -eq 0 ]; then
    if key_collection_missing "$snapshot"; then
      echo "L3:零命中且关键采集缺失（环境/日志为空）——降级为原始工单 + 现场快照"
    else
      echo "L2:零命中但采集完整（日志中无已知错误码）——需升级人工/RCA，不必重新采集"
    fi
    return
  fi
  for line in "${evidence[@]}"; do
    classify_code "$line"
  done
}
