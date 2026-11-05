#!/usr/bin/env bash
# Export SVG slides to PNG (2200px wide). Requires: npx @resvg/resvg-js-cli
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
OUT="$DIR/png"
mkdir -p "$OUT"

# macOS CJK font (required for Chinese labels in resvg)
FONT="/System/Library/Fonts/Hiragino Sans GB.ttc"
if [[ ! -f "$FONT" ]]; then
  FONT="/System/Library/Fonts/STHeiti Medium.ttc"
fi

for f in "$DIR"/*.svg; do
  [[ "$(basename "$f")" == "*.svg" ]] && continue
  name=$(basename "$f" .svg)
  npx --yes @resvg/resvg-js-cli \
    --font-file "$FONT" \
    --fit-width 2200 \
    "$f" "$OUT/${name}.png"
  echo "OK $OUT/${name}.png"
done
