#!/usr/bin/env bash
# scripts/build-llms-full.sh
# 将 knowledge/ 下所有 .md 文件拼接为 llms-full.txt（供单次 context 加载）
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
KNOWLEDGE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT="$KNOWLEDGE_DIR/llms-full.txt"

# 从 metadata.yaml 解析模块列表（SSOT），保持声明顺序
MODULES=()
while IFS= read -r line; do
  id=$(echo "$line" | sed 's/^.*id: //')
  MODULES+=("$id")
done < <(grep '^\s*- id:' "$KNOWLEDGE_DIR/metadata.yaml")

echo "# FlightHub PaaS SDK — Full Knowledge Base" > "$OUTPUT"
echo "" >> "$OUTPUT"
echo "> Auto-generated from knowledge/ directory. Do not edit manually." >> "$OUTPUT"
echo "> Generated at: $(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "$OUTPUT"
echo "" >> "$OUTPUT"

for module in "${MODULES[@]}"; do
  module_dir="$KNOWLEDGE_DIR/$module"
  if [ ! -d "$module_dir" ]; then
    continue
  fi

  # 先输出 index.md（如果存在）
  if [ -f "$module_dir/index.md" ]; then
    echo "---" >> "$OUTPUT"
    echo "" >> "$OUTPUT"
    cat "$module_dir/index.md" >> "$OUTPUT"
    echo "" >> "$OUTPUT"
  fi

  # 再输出其他 .md 文件（按字母序）
  for file in "$module_dir"/*.md; do
    if [ "$(basename "$file")" = "index.md" ]; then
      continue
    fi
    if [ -f "$file" ]; then
      echo "---" >> "$OUTPUT"
      echo "" >> "$OUTPUT"
      cat "$file" >> "$OUTPUT"
      echo "" >> "$OUTPUT"
    fi
  done
done

# 统计输出
LINES=$(wc -l < "$OUTPUT")
WORDS=$(wc -w < "$OUTPUT")
echo ""
echo "✅ Generated: $OUTPUT"
echo "   Lines: $LINES | Words: $WORDS"
