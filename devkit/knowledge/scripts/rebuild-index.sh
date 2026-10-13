#!/usr/bin/env bash
# scripts/rebuild-index.sh
# 扫描 knowledge/ 下所有 .md 的 YAML frontmatter，自动重建 llms.txt 和各模块 index.md
# 用法：
#   ./scripts/rebuild-index.sh          # 重建索引文件
#   ./scripts/rebuild-index.sh --check  # 检查是否过期（CI 用，不修改文件）
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
KNOWLEDGE_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
METADATA="$KNOWLEDGE_DIR/metadata.yaml"
LLMS_TXT="$KNOWLEDGE_DIR/llms.txt"

CHECK_MODE=false
if [[ "${1:-}" == "--check" ]]; then
  CHECK_MODE=true
fi

# --- 辅助函数 ---

# 从 metadata.yaml 解析模块列表（保持声明顺序）
parse_modules() {
  grep '^\s*- id:' "$METADATA" | sed 's/^.*id: //'
}

# 从 metadata.yaml 获取模块 description
get_module_description() {
  local target_id="$1"
  awk -v id="$target_id" '
    /^  - id:/ {
      current_id = $NF
    }
    /^    description:/ && current_id == id {
      sub("^    description:[ ]*", "")
      gsub(/^"/, ""); gsub(/"$/, "")
      print
      exit
    }
  ' "$METADATA"
}

# 从 .md 文件提取 frontmatter 中指定字段的值
get_field() {
  local file="$1"
  local field="$2"
  # 提取 frontmatter 区域（两个 --- 之间），然后取字段值
  awk -v field="$field" '
    /^---$/ { fm++; next }
    fm == 1 && $0 ~ "^"field":" {
      sub("^"field":[ ]*", "")
      # 去掉引号
      gsub(/^"/, ""); gsub(/"$/, "")
      print
      exit
    }
    fm >= 2 { exit }
  ' "$file"
}

# 转义 markdown 表格中的特殊字符（| → \|）
escape_table_cell() {
  printf '%s' "$1" | sed 's/|/\\|/g'
}

# 转义 markdown 链接标题中的特殊字符（[ ] → \[ \]）
escape_link_title() {
  printf '%s' "$1" | sed 's/\[/\\[/g; s/\]/\\]/g'
}

# 从 .md 文件提取 summary 字段；若无则取正文首段截断
get_summary() {
  local file="$1"
  local summary
  summary=$(get_field "$file" "summary")
  if [[ -n "$summary" ]]; then
    echo "$summary"
    return
  fi
  # fallback：跳过 frontmatter 和 # heading，取首个非空段落
  awk '
    /^---$/ { fm++; next }
    fm < 2 { next }
    fm >= 2 && /^$/ { if (started) exit; next }
    fm >= 2 && /^#/ { next }
    fm >= 2 {
      started = 1
      gsub(/^[ \t]+/, "")
      print
      exit
    }
  ' "$file" | cut -c1-60
}

# --- 生成 llms.txt ---

generate_llms_txt() {
  local tmp="$1"
  local modules
  modules=$(parse_modules)

  # 从 metadata.yaml 读取版本和日期
  local sdk_version last_updated
  sdk_version=$(awk '/^sdk_version:/ { gsub(/"/, "", $2); print $2 }' "$METADATA")
  last_updated=$(awk '/^last_updated:/ { gsub(/"/, "", $2); print $2 }' "$METADATA")

  # header
  cat > "$tmp" << HEADER
# FlightHub PaaS SDK

> DJI FlightHub PaaS SDK 知识库 — 为 ISV 的 AI Agent 和编码助手提供无人机管理领域知识。
> SDK Version: ${sdk_version} | Last Updated: ${last_updated}

## Docs

HEADER

  # Docs 区：非 index 的内容文件，按模块顺序
  for module in $modules; do
    local module_dir="$KNOWLEDGE_DIR/$module"
    [[ -d "$module_dir" ]] || continue
    for file in "$module_dir"/*.md; do
      [[ -f "$file" ]] || continue
      [[ "$(basename "$file")" == "index.md" ]] && continue
      local title summary basename_f escaped_title escaped_summary
      title=$(get_field "$file" "title")
      summary=$(get_summary "$file")
      basename_f=$(basename "$file")
      escaped_title=$(escape_link_title "$title")
      escaped_summary=$(escape_table_cell "$summary")
      echo "- [${escaped_title}](knowledge/${module}/${basename_f}): ${escaped_summary}" >> "$tmp"
    done
  done

  # Optional 区：所有 index.md，描述从 metadata.yaml 取
  printf "\n## Optional\n\n" >> "$tmp"
  for module in $modules; do
    local index_file="$KNOWLEDGE_DIR/$module/index.md"
    [[ -f "$index_file" ]] || continue
    local title desc
    title=$(get_field "$index_file" "title")
    desc=$(get_module_description "$module")
    echo "- [${title} Index](knowledge/${module}/index.md): ${desc}" >> "$tmp"
  done
}

# --- 生成各模块 index.md ---

generate_module_index() {
  local module="$1"
  local index_file="$KNOWLEDGE_DIR/$module/index.md"
  local tmp="$2"

  # 如果 index.md 不存在，跳过
  [[ -f "$index_file" ]] || return 0

  # 收集该模块下的非 index 内容文件
  local content_files=()
  for file in "$KNOWLEDGE_DIR/$module"/*.md; do
    [[ -f "$file" ]] || continue
    [[ "$(basename "$file")" == "index.md" ]] && continue
    content_files+=("$file")
  done

  # 无内容文件时保留 index.md 原样（不覆盖人工占位文本）
  if [[ ${#content_files[@]} -eq 0 ]]; then
    cp "$index_file" "$tmp"
    return 0
  fi

  # 提取 index.md 的三个部分：
  # Part 1: 从文件开头到 "## 包含内容" 行（含）
  # Part 2: 自动生成的表格（替换区）
  # Part 3: "## 包含内容" 之后的下一个 ## heading 到文件末尾（保留区）

  # 检查 index.md 是否包含 "## 包含内容" heading
  if ! grep -q '^## 包含内容' "$index_file"; then
    echo "  ⚠️  ${module}/index.md 缺少 '## 包含内容' heading，跳过重建" >&2
    cp "$index_file" "$tmp"
    return 0
  fi

  # Part 1: 从开头到 "## 包含内容"（含该行 + 一个空行）
  awk '
    { print }
    /^## 包含内容/ { exit }
  ' "$index_file" > "$tmp"
  echo "" >> "$tmp"

  # Part 2: 生成表格（说明列优先用 summary，无则用 title）
  echo "| 文档 | 说明 |" >> "$tmp"
  echo "|------|------|" >> "$tmp"
  for file in "${content_files[@]}"; do
    local title summary desc basename_f escaped_desc
    title=$(get_field "$file" "title")
    summary=$(get_field "$file" "summary")
    desc="${summary:-$title}"
    escaped_desc=$(escape_table_cell "$desc")
    basename_f=$(basename "$file")
    echo "| [${basename_f}](${basename_f}) | ${escaped_desc} |" >> "$tmp"
  done

  # Part 3: "## 包含内容" 之后的下一个 ## heading 到文件末尾（保留区）
  # 注意：## 包含内容 和下一个 ## 之间的人工文本（如占位说明）会被表格替换
  local after_section
  after_section=$(awk '
    /^## 包含内容/ { found=1; next }
    found && /^## / { printing=1 }
    printing { print }
  ' "$index_file")

  if [[ -n "$after_section" ]]; then
    echo "" >> "$tmp"
    echo "$after_section" >> "$tmp"
  fi
}

# --- 主流程 ---

TMPDIR_WORK=$(mktemp -d)
trap 'rm -rf "$TMPDIR_WORK"' EXIT

HAS_DIFF=false

# 1) 重建 llms.txt
TMP_LLMS="$TMPDIR_WORK/llms.txt"
generate_llms_txt "$TMP_LLMS"

if ! diff -q "$LLMS_TXT" "$TMP_LLMS" > /dev/null 2>&1; then
  HAS_DIFF=true
  if [[ "$CHECK_MODE" == true ]]; then
    echo "❌ llms.txt 需要更新："
    diff --unified=3 "$LLMS_TXT" "$TMP_LLMS" || true
  else
    cp "$TMP_LLMS" "$LLMS_TXT"
    echo "✅ 已更新 llms.txt"
  fi
else
  echo "  llms.txt 已是最新"
fi

# 2) 重建各模块 index.md
MODULES=$(parse_modules)
for module in $MODULES; do
  INDEX_FILE="$KNOWLEDGE_DIR/$module/index.md"
  [[ -f "$INDEX_FILE" ]] || continue

  TMP_INDEX="$TMPDIR_WORK/${module}-index.md"
  generate_module_index "$module" "$TMP_INDEX"

  if ! diff -q "$INDEX_FILE" "$TMP_INDEX" > /dev/null 2>&1; then
    HAS_DIFF=true
    if [[ "$CHECK_MODE" == true ]]; then
      echo "❌ ${module}/index.md 需要更新："
      diff --unified=3 "$INDEX_FILE" "$TMP_INDEX" || true
    else
      cp "$TMP_INDEX" "$INDEX_FILE"
      echo "✅ 已更新 ${module}/index.md"
    fi
  else
    echo "  ${module}/index.md 已是最新"
  fi
done

# 3) 同步 llms-full.txt（如果脚本存在）
if [[ "$CHECK_MODE" == false ]] && [[ -f "$SCRIPT_DIR/build-llms-full.sh" ]]; then
  echo ""
  bash "$SCRIPT_DIR/build-llms-full.sh"
fi

# --check 模式：有差异则 exit 1
if [[ "$CHECK_MODE" == true ]] && [[ "$HAS_DIFF" == true ]]; then
  echo ""
  echo "索引未同步，请运行: ./knowledge/scripts/rebuild-index.sh"
  exit 1
fi

if [[ "$CHECK_MODE" == true ]]; then
  echo ""
  echo "✅ 所有索引已同步"
fi
