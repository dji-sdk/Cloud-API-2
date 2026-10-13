#!/usr/bin/env bash
#
# devkit 发行包打包脚本（安全审计 F-01/F-07/I-05 整改）
#
# 目标：对外分发源码包时只包含 git 已跟踪文件，杜绝把本地产物
# （target/、node_modules/、.serena/、.DS_Store、knowledge/llms-full.txt、
# security_review/ 原始扫描输出等）打进压缩包导致信息泄露。
#
# 实现方式：基于 `git archive` 导出已跟踪内容（从 Git 索引导出，天然排除
# 未跟踪文件与 .git 目录），再叠加排除清单过滤后打包，并生成 SHA256SUMS。
#
# 用法：
#   ./scripts/release-package.sh [输出目录]
#   ./scripts/release-package.sh dist
#
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${REPO_ROOT}"

OUT_DIR="${1:-dist}"
PKG_NAME="dji-flighthub-devkit-$(git rev-parse --short HEAD)"
PKG_DIR="${OUT_DIR}/${PKG_NAME}"
ARCHIVE="${OUT_DIR}/${PKG_NAME}.tar.gz"

# Git 索引中需要排除的分发项（git archive 导出后再剔除的相对路径）
# 注意：knowledge/scripts/build-llms-full.sh 是生成器的源码脚本，正常分发；
# 需要排除的是其产物 knowledge/llms-full.txt（该文件不应被 git 跟踪）。
EXCLUDES=(
    'security_review'           # 内部安全审计报告，不随源码包分发
    '.claude'                   # 本地 AI 会话配置（注意：.claude-plugin/ 是插件市场清单，正常分发，
                                # git archive 按 git 跟踪粒度导出，本条目只会命中未跟踪的 .claude/ 目录）
    '.serena'                   # 本地 Serena 缓存
    '.github'                   # 内部仓 issue 模板（对外源码包中无意义；
                                # 开源仓的 .github/ 由 publish-opensource.sh 的 PROTECT_PATTERNS 单独维护）
)

command -v git >/dev/null 2>&1 || { echo "⛔ 需要 git"; exit 1; }
git rev-parse --is-inside-work-tree >/dev/null 2>&1 || { echo "⛔ 不在 git 仓库内"; exit 1; }

# 工作区有未提交改动时给出提醒（不阻断，允许基于 HEAD 打包）
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  工作区存在未提交改动，打包内容基于 HEAD（不含这些改动）"
fi

mkdir -p "${OUT_DIR}"
rm -rf "${PKG_DIR}"
mkdir -p "${PKG_DIR}"

# 1. 从 Git 索引导出已跟踪文件（不含未跟踪/忽略文件，不含 .git）
git archive HEAD | tar -x -C "${PKG_DIR}"

# 2. 叠加排除清单
for pattern in "${EXCLUDES[@]}"; do
    # shellcheck disable=SC2086
    (cd "${PKG_DIR}" && rm -rf ./$pattern) || true
done

# 3. 打包并生成 SHA256SUMS
tar -czf "${ARCHIVE}" -C "${OUT_DIR}" "${PKG_NAME}"
(cd "${OUT_DIR}" && shasum -a 256 "${PKG_NAME}.tar.gz" > "${PKG_NAME}.tar.gz.sha256")

echo "✅ 打包完成："
echo "   ${ARCHIVE}"
echo "   ${ARCHIVE}.sha256"
echo "   内容：$(tar -tzf "${ARCHIVE}" | grep -vc '/$') 个文件"
