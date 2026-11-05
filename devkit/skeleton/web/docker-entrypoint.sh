#!/bin/sh
# 容器启动时根据环境变量生成 config.js，实现运行时配置注入
# 安全加固：所有值经 sed 转义，防止环境变量注入 JS 代码

# 辅助函数：转义字符串中的单引号和反斜杠，防止 JS 注入
escape_js() {
  printf '%s' "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/\\\\'/g; s/</\\\\u003c/g; s/>/\\\\u003e/g"
}

# 辅助函数：转义 sed 替换串中的特殊字符（反斜杠、&、当前分隔符 |），防止值里的特殊字符
# 破坏 sed 表达式或触发意外的反向引用替换
escape_sed() {
  printf '%s' "$1" | sed -e 's/[&|\\]/\\&/g'
}

cat > /usr/share/nginx/html/config.js <<EOF
window.__FH_CONFIG__ = {
  FH2_ADDRESS: '$(escape_js "${VITE_FH2_ADDRESS:-}")',
  FH2_PORT: '$(escape_js "${VITE_FH2_PORT:-30812}")',
  FH2_ASSET_HOST: '$(escape_js "${VITE_FH2_ASSET_HOST:-}")',
  FH2_BASE_URL: '$(escape_js "${VITE_FH2_BASE_URL:-}")',
  FH2_WSS_URL: '$(escape_js "${VITE_FH2_WSS_URL:-}")',
  FH2_PRJ_ID: '$(escape_js "${VITE_FH2_PRJ_ID:-}")',
  FH2_SDK_AUTH_KEY: '$(escape_js "${VITE_FH2_SDK_AUTH_KEY:-}")',
  FH2_SDK_AUTH_VALUE: '$(escape_js "${VITE_FH2_SDK_AUTH_VALUE:-}")',
  DEFAULT_THEME: '$(escape_js "${VITE_DEFAULT_THEME:-light}")',
  PRIMARY_COLOR: '$(escape_js "${VITE_PRIMARY_COLOR:-#1a73e8}")',
  PRIMARY_HOVER_COLOR: '$(escape_js "${VITE_PRIMARY_HOVER_COLOR:-}")',
  MOCK_ENABLED: '$(escape_js "${VITE_MOCK_ENABLED:-false}")',
  MOCK_DEBUG_PANEL: '$(escape_js "${VITE_MOCK_DEBUG_PANEL:-false}")',
  FH2_DEVICE_SDK_ENABLED: '$(escape_js "${VITE_FH2_DEVICE_SDK_ENABLED:-true}")',
  MAP_PROVIDER: '$(escape_js "${VITE_MAP_PROVIDER:-esri}")',
  BING_KEY: '$(escape_js "${VITE_BING_KEY:-}")',
  TIANDITU_KEY: '$(escape_js "${VITE_TIANDITU_KEY:-}")',
};
EOF

NGINX_CONF=/etc/nginx/conf.d/default.conf

# ── FH2 同源反代上游注入 ──────────────────────────────────
# nginx.conf 内 /fh2-assets/ 与 /fh2-gw/ 的 proxy_pass 使用占位符，启动时按环境变量替换为
# 真实 FH2 上游。上游地址只存在于容器环境（服务端），不会烙入浏览器产物，FH2 真实 IP 不外泄。
#   FH2_ASSET_UPSTREAM: FH2 静态资源根，默认 https://${VITE_FH2_ADDRESS host}:443
#   FH2_API_UPSTREAM:   FH2 API/wss 根，默认 https://${VITE_FH2_ADDRESS host}:${VITE_FH2_PORT}
# 均可显式覆盖；未配置且无 VITE_FH2_ADDRESS 时回退到不可达 dummy，保证 nginx 能正常解析启动。
if [ -z "$FH2_ASSET_UPSTREAM" ] && [ -n "$VITE_FH2_ADDRESS" ]; then
  # 从 VITE_FH2_ADDRESS 取 scheme+host（去掉可能携带的端口），资源默认走 443
  FH2_HOST="$(printf '%s' "$VITE_FH2_ADDRESS" | sed -E 's#(https?://[^:/]+).*#\1#')"
  FH2_ASSET_UPSTREAM="${FH2_HOST}:443"
fi
if [ -z "$FH2_API_UPSTREAM" ] && [ -n "$VITE_FH2_ADDRESS" ]; then
  FH2_HOST="$(printf '%s' "$VITE_FH2_ADDRESS" | sed -E 's#(https?://[^:/]+).*#\1#')"
  # API/wss 走 TLS（与 wss://host:30812 一致），scheme 强制 https
  FH2_API_HOST="$(printf '%s' "$FH2_HOST" | sed -E 's#^http://#https://#')"
  FH2_API_UPSTREAM="${FH2_API_HOST}:${VITE_FH2_PORT:-30812}"
fi
FH2_ASSET_UPSTREAM="${FH2_ASSET_UPSTREAM:-https://127.0.0.1:65535}"
FH2_API_UPSTREAM="${FH2_API_UPSTREAM:-https://127.0.0.1:65535}"
sed -i "s|__FH2_ASSET_UPSTREAM__|$(escape_sed "$FH2_ASSET_UPSTREAM")|g" "$NGINX_CONF"
sed -i "s|__FH2_API_UPSTREAM__|$(escape_sed "$FH2_API_UPSTREAM")|g" "$NGINX_CONF"

# ── FH2 上游 TLS 校验策略注入 ────────────────────────────────
# 默认 on（fail-closed，安全审计 CICD-09）：现场 FH2 若为自签名证书，需显式设置
# FH2_PROXY_SSL_VERIFY=off，或挂载内网 CA 并用 FH2_PROXY_SSL_CA 指向它。
# 只接受 on/off；其它非法值直接退出启动（不让配置错误被静默接受）。
# 镜像内置系统信任库路径，一般无需修改（测试可覆盖以验证 fail-closed 分支）
SYSTEM_CA_BUNDLE="${FH2_SYSTEM_CA_BUNDLE:-/etc/ssl/certs/ca-certificates.crt}"
case "${FH2_PROXY_SSL_VERIFY:-on}" in
  on|off) FH2_PROXY_SSL_VERIFY="${FH2_PROXY_SSL_VERIFY:-on}" ;;
  *)
    echo "ERROR: FH2_PROXY_SSL_VERIFY 仅支持 on/off，收到 '${FH2_PROXY_SSL_VERIFY}'，拒绝启动（fail-closed）" >&2
    exit 1
    ;;
esac
FH2_PROXY_SSL_CA_LINE=""
if [ "$FH2_PROXY_SSL_VERIFY" = "on" ]; then
  # nginx 没有系统信任库的隐式回退：proxy_ssl_verify on 缺少 proxy_ssl_trusted_certificate
  # 时会直接 [emerg] 拒绝启动（CrashLoopBackOff）。故必须落定一个真实存在的 CA 文件：
  #   1. 显式 FH2_PROXY_SSL_CA 且文件存在 → 用它
  #   2. 未配置或显式路径无效 → 回退镜像内置系统信任库，并告警提示配置有误
  #   3. 系统信任库也不存在 → 无法安全校验，FAIL-CLOSED 拒绝启动（不再降级 off）
  CA_FILE="${FH2_PROXY_SSL_CA:-$SYSTEM_CA_BUNDLE}"
  if [ -n "${FH2_PROXY_SSL_CA:-}" ] && [ ! -f "$FH2_PROXY_SSL_CA" ]; then
    echo "WARN: FH2_PROXY_SSL_CA 指向的文件不存在：${FH2_PROXY_SSL_CA}，回退系统信任库 ${SYSTEM_CA_BUNDLE}" >&2
    CA_FILE="$SYSTEM_CA_BUNDLE"
  fi
  if [ -f "$CA_FILE" ]; then
    FH2_PROXY_SSL_CA_LINE="proxy_ssl_trusted_certificate $(escape_sed "$CA_FILE");"
  else
    echo "ERROR: 可信 CA 文件不可用（${CA_FILE}），FH2_PROXY_SSL_VERIFY=on 无法安全校验上游证书，拒绝启动（fail-closed）" >&2
    exit 1
  fi
fi
echo "INFO: FH2_PROXY_SSL_VERIFY=${FH2_PROXY_SSL_VERIFY}（CA: ${CA_FILE:-不可用}）"
sed -i "s|__FH2_PROXY_SSL_VERIFY__|${FH2_PROXY_SSL_VERIFY}|g" "$NGINX_CONF"
sed -i "s|__FH2_PROXY_SSL_TRUSTED_CA__|${FH2_PROXY_SSL_CA_LINE}|g" "$NGINX_CONF"

# ── FH2 网关 isv-webhook-auth 鉴权头服务端注入 ────────────────
# 网关读名为 auth_keys 的声明头（分号分隔的头名清单），再按清单读 x-user-token / user_id 等凭据头交
# webhook 校验。浏览器带的 x-user-token 是骨架自身后端 JWT（FH2 不认），且 auth_keys/user_id 带下划线
# 会被默认丢弃 —— 故由反代用 proxy_set_header 统一覆盖注入三件套，使 FH2 凭据仅存于容器环境，不烙入
# config.js / 浏览器 / git。VITE_FH2_USER_TOKEN 未配置时占位符替换为空串，保持浏览器原值透传（无回归）。
#   X-User-Token 值 ← VITE_FH2_USER_TOKEN（FH2 身份 JWT，经 k8s Secret 注入）
#   附加凭据头名/值 ← VITE_FH2_SDK_AUTH_KEY / VITE_FH2_SDK_AUTH_VALUE（如 user_id / paas_test）
#   auth_keys 清单 = "X-User-Token" 再按需追加 ";<SDK_AUTH_KEY>"
if [ -n "$VITE_FH2_USER_TOKEN" ]; then
  AUTH_KEY="${VITE_FH2_SDK_AUTH_KEY:-}"
  AUTH_VAL="${VITE_FH2_SDK_AUTH_VALUE:-}"
  TOKEN_ESC="$(escape_sed "$VITE_FH2_USER_TOKEN")"
  AUTH_KEYS_LIST="X-User-Token"
  EXTRA_HEADER=""
  if [ -n "$AUTH_KEY" ]; then
    AUTH_KEY_ESC="$(escape_sed "$AUTH_KEY")"
    AUTH_VAL_ESC="$(escape_sed "$AUTH_VAL")"
    AUTH_KEYS_LIST="X-User-Token;${AUTH_KEY_ESC}"
    EXTRA_HEADER="proxy_set_header ${AUTH_KEY_ESC} \"${AUTH_VAL_ESC}\"; "
  fi
  INJECT="proxy_set_header auth_keys \"${AUTH_KEYS_LIST}\"; proxy_set_header X-User-Token \"${TOKEN_ESC}\"; ${EXTRA_HEADER}"
  sed -i "s|__FH2_USER_TOKEN_HEADER__|${INJECT}|g" "$NGINX_CONF"

  # ── FH2 通知 ws 鉴权子协议服务端注入 ──────────────────────────
  # 通知服务只认 Sec-WebSocket-Protocol 里的 auth.<base64url> 子协议项（header/query/cookie 均 401）。
  # 明文格式固定为 "X-User-Token=<JWT>[;<SDK_AUTH_KEY>=<SDK_AUTH_VALUE>]"，base64url = 标准 base64
  # 把 +/ 换成 -_ 再去掉 = 填充。浏览器握手仍 offer v1.notify，服务端只回显 v1.notify，透明通过。
  AUTH_PLAIN="X-User-Token=${VITE_FH2_USER_TOKEN}"
  if [ -n "$AUTH_KEY" ]; then
    AUTH_PLAIN="${AUTH_PLAIN};${AUTH_KEY}=${AUTH_VAL}"
  fi
  AUTH_B64="$(printf '%s' "$AUTH_PLAIN" | base64 | tr -d '\n' | tr '+/' '-_' | tr -d '=')"
  SUBPROTO="v1.notify, auth.${AUTH_B64}"
  SUBPROTO_ESC="$(escape_sed "$SUBPROTO")"
  sed -i "s|__FH2_WS_SUBPROTO_VALUE__|${SUBPROTO_ESC}|g" "$NGINX_CONF"
  sed -i "s|__FH2_WS_SUBPROTO_HEADER__|proxy_set_header Sec-WebSocket-Protocol \$fh2_ws_auth_proto;|g" "$NGINX_CONF"
else
  sed -i "s|__FH2_USER_TOKEN_HEADER__||g" "$NGINX_CONF"
  # 未配置 token：ws 子协议占位符清空，Sec-WebSocket-Protocol 不覆盖，浏览器原值透传（无回归）。
  sed -i "s|__FH2_WS_SUBPROTO_VALUE__||g" "$NGINX_CONF"
  sed -i "s|__FH2_WS_SUBPROTO_HEADER__||g" "$NGINX_CONF"
fi

exec nginx -g 'daemon off;'
