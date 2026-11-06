#!/bin/sh
# docker-entrypoint.sh 的注入逻辑测试。
#
# 覆盖两块无法用 vitest 验证的 shell/nginx 逻辑：
#   1. FH2 通知 ws 鉴权子协议的 base64url 编码（纯函数，跨平台可跑）
#   2. 两分支（配置 token / 未配置 token）的 nginx 占位符注入 + nginx -t 语法校验
#      （依赖 docker + nginx:alpine；无 docker 时自动跳过，不算失败）
#
# 用法：sh skeleton/web/docker-entrypoint.test.sh
# 退出码：0 全通过（含优雅跳过）；非 0 有断言失败。

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
ENTRYPOINT="${SCRIPT_DIR}/docker-entrypoint.sh"
NGINX_CONF_SRC="${SCRIPT_DIR}/nginx.conf"
BARE_METAL_CONF_SRC="${SCRIPT_DIR}/../deploy/skeleton-ui.conf"

PASS=0
FAIL=0

# 断言相等：assert_eq <用例名> <期望> <实际>
assert_eq() {
  if [ "$2" = "$3" ]; then
    PASS=$((PASS + 1))
    printf '  ✅ %s\n' "$1"
  else
    FAIL=$((FAIL + 1))
    printf '  ❌ %s\n     期望: %s\n     实际: %s\n' "$1" "$2" "$3"
  fi
}

# 断言包含子串：assert_contains <用例名> <母串> <子串>
assert_contains() {
  case "$2" in
    *"$3"*)
      PASS=$((PASS + 1))
      printf '  ✅ %s\n' "$1"
      ;;
    *)
      FAIL=$((FAIL + 1))
      printf '  ❌ %s\n     母串中未找到: %s\n' "$1" "$3"
      ;;
  esac
}

# base64url 编码：与 docker-entrypoint.sh 内保持一致（标准 base64 → +/ 换 -_ → 去 = 填充）
b64url() {
  printf '%s' "$1" | base64 | tr -d '\n' | tr '+/' '-_' | tr -d '='
}

# ── 测试 1：子协议编码 round-trip（纯函数，无需 docker）─────────────────
# 用原生页面抓取的真实子协议值反推，确认我们的编码与 FH2 SDK 完全一致。
test_subproto_encoding() {
  echo '[1] 子协议 base64url 编码'

  # 原生页面 DevTools 抓到的真实 auth.<...> 负载（JWT 为公开 demo 令牌，非敏感凭据）
  native='WC1Vc2VyLVRva2VuPWV5SmhiR2NpT2lKSVV6VXhNaUlzSW10cFpDSTZJalUzWW1ReU5tRXdMVFl5TURrdE5HRTVNeTFoTmpnNExXWTROelV5WW1VMVpERTVNU0lzSW5SNWNDSTZJa3BYVkNKOS5leUpoWTJOdmRXNTBJam9pWkdWbVlYVnNkQ0lzSW1WNGNDSTZNVGt5TkRneE9USXdNQ3dpYm1KbUlqb3hOakE1TkRVNU1qQXdMQ0p2Y21kaGJtbDZZWFJwYjI1ZmRYVnBaQ0k2SWpWaE5tWTVaRFJpTFRKaE16Z3ROR0kwWWkwNE5tWTVMVE5oTmpjNFpHRXdZbVk0T0NJc0luQnliMnBsWTNSZmRYVnBaQ0k2SWlJc0luTjFZaUk2SW1ab01pSXNJblZ6WlhKZmFXUWlPaUprWldaaGRXeDBJbjAucHNLUVRmVE9FVkI5Zm4wT3JCVzRwdW9IeklWNkM5cGdud2s2SnFFanB3b1BFNEQxRERpTEF1Q2JNY0hfaHBuYjhEVVpyV3ZOSTZkNlg3aFFUSHpFblE7dXNlcl9pZD1wYWFzX3Rlc3Q'

  # 从原生值解码出明文，再用我们的编码器重编，应完全一致（round-trip）
  pad="$native"
  case $(( ${#pad} % 4 )) in
    2) pad="${pad}==" ;;
    3) pad="${pad}=" ;;
  esac
  plain="$(printf '%s' "$pad" | tr '_-' '/+' | base64 -d)"
  reencoded="$(b64url "$plain")"

  assert_eq 'round-trip 重编与原生值一致' "$native" "$reencoded"
  assert_contains '明文含 X-User-Token 前缀' "$plain" 'X-User-Token='
  assert_contains '明文含 user_id 租户项' "$plain" ';user_id=paas_test'
}

# ── 测试 2：entrypoint 双分支注入 + nginx -t（需 docker）────────────────
test_entrypoint_injection() {
  echo '[2] entrypoint 占位符注入 + nginx -t'

  if ! command -v docker >/dev/null 2>&1 || ! docker info >/dev/null 2>&1; then
    echo '  ⏭️  docker 不可用，跳过容器注入校验（编码测试已覆盖核心逻辑）'
    return 0
  fi

  # 合成 JWT（仅用于校验占位符注入，非任何环境的真实凭据）
  jwt='eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwidXNlcl9pZCI6InBhb3NfdGVzdCJ9.c2lnbmF0dXJlLWZvci1pbmplY3Rpb24tdGVzdC1vbmx5'

  # 复制真实 entrypoint，把末行 exec nginx 换成 nginx -t，供容器内语法校验
  tmp_entry="$(mktemp)"
  sed 's|^exec nginx -g .*|nginx -t|' "$ENTRYPOINT" > "$tmp_entry"

  # 容器内：铺底 nginx.conf → 跑 entrypoint 注入 → 输出残留占位符数、子协议头、TLS 校验策略与 nginx -t 结果
  run_case() {
    docker run --rm --add-host skeleton:127.0.0.1 \
      -v "${NGINX_CONF_SRC}:/seed/nginx.conf:ro" \
      -v "${tmp_entry}:/seed/entry.sh:ro" \
      -e VITE_FH2_ADDRESS='https://192.0.2.10' -e VITE_FH2_PORT='30812' \
      "$@" \
      nginx:alpine sh -c '
        mkdir -p /etc/nginx/conf.d
        cp /seed/nginx.conf /etc/nginx/conf.d/default.conf
        sh /seed/entry.sh >/tmp/nt.log 2>&1
        nt_rc=$?
        residual=$(grep -c "__FH2" /etc/nginx/conf.d/default.conf || true)
        has_subproto=no
        grep -q "proxy_set_header Sec-WebSocket-Protocol" /etc/nginx/conf.d/default.conf && has_subproto=yes
        verify=$(grep -m1 -o "proxy_ssl_verify *[a-z]*" /etc/nginx/conf.d/default.conf | sed "s/.* //")
        has_ca=no
        grep -q "proxy_ssl_trusted_certificate" /etc/nginx/conf.d/default.conf && has_ca=yes
        nt_ok=no
        if grep -q "test is successful" /tmp/nt.log; then
          nt_ok=yes
        elif grep -q "ERROR: FH2_PROXY_SSL_VERIFY" /tmp/nt.log; then
          nt_ok=fail_closed
        fi
        printf "residual=%s subproto=%s verify=%s ca=%s nt=%s\n" \
          "$residual" "$has_subproto" "$verify" "$has_ca" "$nt_ok"
      '
  }

  # CASE A：配置 token → 应无残留、下发子协议头、默认镜像内置 CA → verify=on + 写入CA + 语法通过
  out_a="$(run_case \
    -e VITE_FH2_USER_TOKEN="$jwt" \
    -e VITE_FH2_SDK_AUTH_KEY='user_id' \
    -e VITE_FH2_SDK_AUTH_VALUE='paas_test')"
  assert_eq 'A 配置token：无残留+下发子协议+默认校验TLS+语法通过' \
    'residual=0 subproto=yes verify=on ca=yes nt=yes' "$out_a"

  # CASE B：未配置 token → 应无残留、不下发子协议头、默认镜像内置 CA → verify=on（无回归）
  out_b="$(run_case)"
  assert_eq 'B 未配token：无残留+不下发子协议+默认校验TLS+语法通过' \
    'residual=0 subproto=no verify=on ca=yes nt=yes' "$out_b"

  # CASE C：显式开启上游 TLS 校验并提供内部 CA → 校验 on + 写入 trusted_certificate + 语法通过
  out_c="$(run_case \
    -e FH2_PROXY_SSL_VERIFY='on' \
    -e FH2_PROXY_SSL_CA='/etc/ssl/certs/ca-certificates.crt')"
  assert_eq 'C 开启TLS校验+指定CA：verify=on+写入CA+语法通过' \
    'residual=0 subproto=no verify=on ca=yes nt=yes' "$out_c"

  # CASE D：CA 路径不存在 → 回退镜像内置系统信任库（nginx 不允许 verify=on 而无 trusted_certificate）
  out_d="$(run_case \
    -e FH2_PROXY_SSL_VERIFY='on' \
    -e FH2_PROXY_SSL_CA='/nonexistent/ca.pem')"
  assert_eq 'D CA文件缺失：回退系统信任库+保持verify=on+语法通过' \
    'residual=0 subproto=no verify=on ca=yes nt=yes' "$out_d"

  # CASE E：系统信任库也不可用 → 无法安全校验，FAIL-CLOSED 拒绝启动（安全审计 CICD-09）
  out_e="$(run_case \
    -e FH2_PROXY_SSL_VERIFY='on' \
    -e FH2_SYSTEM_CA_BUNDLE='/nonexistent/system-bundle.crt')"
  assert_eq 'E 可信CA完全不可用：fail-closed 拒绝启动' \
    'residual=0 subproto=no verify=on ca=no nt=fail_closed' "$out_e"

  # CASE F：非法 FH2_PROXY_SSL_VERIFY 值 → FAIL-CLOSED 拒绝启动
  out_f="$(run_case -e FH2_PROXY_SSL_VERIFY='abc')"
  assert_eq 'F 非法verify值（abc）：fail-closed 拒绝启动' \
    'residual=0 subproto=no verify=on ca=no nt=fail_closed' "$out_f"

  # CASE G：显式 off → 不校验上游证书（兼容现场自签名）+ 语法通过
  out_g="$(run_case -e FH2_PROXY_SSL_VERIFY='off')"
  assert_eq 'G 显式关闭TLS校验：verify=off+不写CA+语法通过' \
    'residual=0 subproto=no verify=off ca=no nt=yes' "$out_g"

  rm -f "$tmp_entry"
}

# ── 测试 3：FH2 子应用正则反代 location（静态，无需 docker）───────────────
# 黑窗根因是 qiankun 子应用（/project-app/ 等绝对根路径）落到 SPA fallback 返回 index.html。
# 修复为用正则 location 匹配 "<名>-app/" 统一反代门户。此处静态校验该 location 存在且形态正确，
# 并确认骨架自身 SPA 路由不会被该正则误伤（骨架路由均无 -app 后缀）。
test_microapp_location() {
  echo '[3] FH2 子应用正则反代 location'

  conf="$(cat "$NGINX_CONF_SRC")"
  assert_contains '存在 <名>-app 正则 location' \
    "$conf" 'location ~ ^/[a-z0-9-]+-app(/|$)'
  assert_contains '子应用反代到资源上游占位符' \
    "$conf" 'proxy_pass            __FH2_ASSET_UPSTREAM__;'

  # 骨架 SPA 路由样本不得匹配 "<名>-app/" 正则（否则会被错误反代到门户）
  for route in cockpit devices wayline media flight-tasks device-window live-stream; do
    case "/$route/" in
      */[a-z0-9-]*-app/*)
        assert_eq "骨架路由 /$route 不被 -app 正则误伤" 'no-match' "matched:/$route"
        ;;
      *)
        assert_eq "骨架路由 /$route 不被 -app 正则误伤" 'no-match' 'no-match'
        ;;
    esac
  done
}

# ── 测试 4：MOCK_DEBUG_PANEL 默认值注入（静态，无需 docker）─────────────
# 该开关只在前端与 entrypoint 落地（Dockerfile ARG / k8s configmap 本次未铺），
# 因此必须保证 entrypoint 始终写出默认值，否则前端拿到空串。
test_mock_debug_panel_default() {
  echo '[4] MOCK_DEBUG_PANEL 默认值注入'

  count="$(grep -c 'MOCK_DEBUG_PANEL: .*VITE_MOCK_DEBUG_PANEL:-false' "$ENTRYPOINT" || true)"
  assert_eq 'entrypoint 注入 MOCK_DEBUG_PANEL 且默认 false' '1' "$count"
}

# ── 测试 5：运行时配置缓存策略（静态，无需 docker）─────────────────────
# 回归测试：config.js 由 entrypoint 在容器启动时重写，文件名固定。若 nginx 不下发
# 缓存头，浏览器会把旧版本（window.__FH_CONFIG__ = {}）长期缓存，出现「容器内文件已有值、
# 页面仍报 FH2_ADDRESS 未配置」。镜像内与裸机两套 nginx 配置必须一致地强制重新校验。
test_cache_policy() {
  echo '[5] config.js 缓存策略'

  assert_no_cache_location() {
    label="$1"
    conf="$2"
    assert_contains "${label}：config.js 强制重新校验" \
      "$conf" 'location = /config.js {
        expires -1;'
    assert_contains "${label}：index.html 强制重新校验" \
      "$conf" 'location = /index.html {
        expires -1;'
    assert_contains "${label}：带 hash 的 /assets/ 长缓存" \
      "$conf" 'location /assets/ {
        expires 1y;'
  }

  assert_no_cache_location '镜像 nginx.conf' "$(cat "$NGINX_CONF_SRC")"
  assert_no_cache_location '裸机 skeleton-ui.conf' "$(cat "$BARE_METAL_CONF_SRC")"
}

echo '=== docker-entrypoint.sh 注入逻辑测试 ==='
test_subproto_encoding
test_microapp_location
test_mock_debug_panel_default
test_cache_policy
test_entrypoint_injection

echo '==========================================='
printf '通过 %s，失败 %s\n' "$PASS" "$FAIL"
[ "$FAIL" -eq 0 ]
