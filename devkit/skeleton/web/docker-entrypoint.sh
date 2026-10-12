#!/bin/sh
# 容器启动时根据环境变量生成 config.js，实现运行时配置注入
# 安全加固：所有值经 sed 转义，防止环境变量注入 JS 代码

# 辅助函数：转义字符串中的单引号和反斜杠，防止 JS 注入
escape_js() {
  printf '%s' "$1" | sed "s/\\\\/\\\\\\\\/g; s/'/\\\\'/g; s/</\\\\u003c/g; s/>/\\\\u003e/g"
}

cat > /usr/share/nginx/html/config.js <<EOF
window.__FH_CONFIG__ = {
  FH2_ADDRESS: '$(escape_js "${VITE_FH2_ADDRESS:-}")',
  FH2_PORT: '$(escape_js "${VITE_FH2_PORT:-30812}")',
  FH2_WSS_URL: '$(escape_js "${VITE_FH2_WSS_URL:-}")',
  FH2_PRJ_ID: '$(escape_js "${VITE_FH2_PRJ_ID:-}")',
  FH2_SDK_AUTH_KEY: '$(escape_js "${VITE_FH2_SDK_AUTH_KEY:-}")',
  FH2_SDK_AUTH_VALUE: '$(escape_js "${VITE_FH2_SDK_AUTH_VALUE:-}")',
  DEFAULT_THEME: '$(escape_js "${VITE_DEFAULT_THEME:-light}")',
  PRIMARY_COLOR: '$(escape_js "${VITE_PRIMARY_COLOR:-#1a73e8}")',
  PRIMARY_HOVER_COLOR: '$(escape_js "${VITE_PRIMARY_HOVER_COLOR:-}")',
  MOCK_ENABLED: '$(escape_js "${VITE_MOCK_ENABLED:-false}")',
  FH2_DEVICE_SDK_ENABLED: '$(escape_js "${VITE_FH2_DEVICE_SDK_ENABLED:-true}")',
  MAP_PROVIDER: '$(escape_js "${VITE_MAP_PROVIDER:-esri}")',
  BING_KEY: '$(escape_js "${VITE_BING_KEY:-}")',
  TIANDITU_KEY: '$(escape_js "${VITE_TIANDITU_KEY:-}")',
};
EOF

# 将 FH2 服务地址加入 CSP 白名单，允许加载 SDK 脚本/样式及 API 通信
if [ -n "$VITE_FH2_ADDRESS" ]; then
  FH2_ORIGIN="${VITE_FH2_ADDRESS}"
  FH2_API_ORIGIN="${VITE_FH2_ADDRESS}:${VITE_FH2_PORT:-30812}"
  NGINX_CONF=/etc/nginx/conf.d/default.conf
  sed -i "s|script-src 'self'|script-src 'self' ${FH2_ORIGIN}|" "$NGINX_CONF"
  sed -i "s|style-src 'self'|style-src 'self' ${FH2_ORIGIN}|" "$NGINX_CONF"
  sed -i "s|img-src 'self'|img-src 'self' ${FH2_ORIGIN} ${FH2_API_ORIGIN}|" "$NGINX_CONF"
  sed -i "s|connect-src 'self'|connect-src 'self' ${FH2_ORIGIN} ${FH2_API_ORIGIN}|" "$NGINX_CONF"
fi

exec nginx -g 'daemon off;'
