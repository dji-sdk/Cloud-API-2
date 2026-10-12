# lib/sanitize.sh — 脱敏引擎（Python 优先，sed/awk fallback）

sanitize_stdin() {
  if have_cmd python3; then
    python3 "${SCRIPT_DIR}/pylib/sanitize.py"
  else
    _sanitize_stdin_sed
  fi
}

_sanitize_stdin_sed() {
  sed -E \
    -e 's/(password|passwd|token|secret|api[_-]?key|access[_-]?key)["'"'"']?[[:space:]]*[:=][[:space:]]*["'"'"']?[^"'"'"' ,}]+/\1="***"/Ig' \
    -e 's/(^|[^0-9.])(10\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})/\1***.***.***.***/g' \
    -e 's/(^|[^0-9.])(192\.168\.[0-9]{1,3}\.[0-9]{1,3})/\1192.168.***.***/g' \
    -e 's/(^|[^0-9.])(172\.(1[6-9]|2[0-9]|3[01])\.[0-9]{1,3}\.[0-9]{1,3})/\1172.***.***.***/g' \
    -e 's/[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}/***@***/g' \
    -e 's/(1[3-9][0-9])[[:space:]]*[- ]?[[:space:]]*([0-9]{4})[[:space:]]*[- ]?[[:space:]]*([0-9]{4})/\1-****-****/g' \
    -e 's/([?&][A-Za-z0-9_]+)=[^&[:space:]]*/\1=***/g' \
    -e "s#(/Users/[A-Za-z0-9._-]+|/home/[A-Za-z0-9._-]+)#/***#g" \
    -e 's/([0-9a-fA-F]{1,4}:){3,}[0-9a-fA-F]{1,4}/***:***:***:***/g' \
    -e 's/([0-9a-fA-F]{2}[:-]){5}[0-9a-fA-F]{2}/**:**:**:**:**:**/g' \
    -e 's/(^|[^0-9])[0-9]{15}($|[^0-9])/\1***IMEI***\2/g' \
    -e 's/eyJ[A-Za-z0-9_-]+\.eyJ[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+/***JWT***/g' \
    -e 's/-----BEGIN.*PRIVATE KEY-----/***PRIVATE_KEY_REMOVED***/g' \
    -e 's/-----BEGIN CERTIFICATE-----/***CERTIFICATE_REMOVED***/g' \
    -e '/-----END.*PRIVATE KEY-----/d' \
    -e '/-----END CERTIFICATE-----/d' \
    -e 's/([+-]?[0-9]{1,3}\.[0-9]{2})[0-9]+/\1***/g' \
    -e 's/([A-Za-z_]*sn|serial[A-Za-z_]*)[[:space:]]*[:=][[:space:]]*["'"'"']?([A-Za-z0-9]{10,20})/\1=***SN***/Ig' \
    -e 's/"([A-Za-z_]*sn|serial[A-Za-z_]*)"[[:space:]]*:[[:space:]]*"[A-Za-z0-9]{10,20}"/"\1":"***SN***"/Ig' \
    -e '/^[[:space:]]*$/d'
}

sanitize_with_stats() {
  if have_cmd python3; then
    python3 "${SCRIPT_DIR}/pylib/sanitize.py" --stats
  else
    _sanitize_with_stats_awk_sed
  fi
}

_sanitize_with_stats_awk_sed() {
  awk '
    {
      low = tolower($0)
      if (low ~ /(password|passwd|token|secret|api[_-]?key|access[_-]?key)/) nc++
      if ($0 ~ /(^|[^0-9.])(10\.[0-9]|192\.168\.|172\.(1[6-9]|2[0-9]|3[01])\.)/) ni++
      if ($0 ~ /[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z][A-Za-z]/) ne++
      if ($0 ~ /1[3-9][0-9][ -]?[0-9][0-9][0-9][0-9][ -]?[0-9][0-9][0-9][0-9]/) np++
      if ($0 ~ /[?&][A-Za-z0-9_]+=[^& ]/) nq++
      if ($0 ~ /\/Users\/|\/home\//) nu++
      if ($0 ~ /[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]+:[0-9a-fA-F]/) n6++
      if ($0 ~ /[0-9a-fA-F][0-9a-fA-F][:-][0-9a-fA-F][0-9a-fA-F][:-]/) nm++
      if ($0 ~ /eyJ[A-Za-z0-9_-]+\.eyJ/) nj++
      if ($0 ~ /[0-9]?[0-9]?[0-9]\.[0-9][0-9][0-9]/) ng++
      if (low ~ /[a-z_]*sn[ \t]*[:=]|serial[a-z_]*[ \t]*[:=]/) ns++
      print
    }
    END {
      printf "命中统计: credential=%d, ipv4=%d, ipv6=%d, mac=%d, email=%d, phone_cn=%d, url_query=%d, user_path=%d, jwt=%d, gps=%d, sn=%d\n", nc+0, ni+0, n6+0, nm+0, ne+0, np+0, nq+0, nu+0, nj+0, ng+0, ns+0 > "/dev/stderr"
    }
  ' | _sanitize_stdin_sed
}
