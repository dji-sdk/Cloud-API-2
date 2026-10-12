# lib/util.sh — 通用工具函数（bash 3.2 兼容）

have_cmd() { command -v "$1" >/dev/null 2>&1; }

timestamp() { echo "[$(date '+%Y-%m-%d %H:%M:%S')]"; }

emit_kv() { printf '%s= %s\n' "$1" "$2"; }

progress()      { printf '  ... %s\n' "$1" >&2; }
progress_done() { printf '  OK  %s\n' "$1" >&2; }

warn() { printf '警告: %s\n' "$1" >&2; }

mask_url() {
  local url="$1"
  printf '%s' "$url" | sed -E 's|(https?://[^/]+)/.*|\1/***|'
}
