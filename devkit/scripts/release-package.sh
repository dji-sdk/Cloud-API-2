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
    'dist'                      # 发行包输出目录本身，正常情况下 .gitignore 已令其不进 git archive，
                                # 这里是防止其被误 `git add -f` 跟踪后递归打包进自己的兜底

    # --- 内部 CI/部署/规划文档（与 publish-opensource.sh 排除清单保持一致，
    #     此前曾各自维护一份、彼此漂移，导致本脚本漏掉新加的排除项）---
    'CLAUDE.md'                 # 内部 Claude Code 工作流指令
    'skeleton/CLAUDE.md'
    'ci/internal-build.mk'      # 内网 Harbor/Nexus/CodeMeter 域名与项目 ID 硬编码
    'ci/verify-multiarch.sh'    # 同上，内网 Harbor 域名硬编码
    'mcp/deploy.sh'             # 内部 SSH + docker compose 部署脚本
    'skeleton/deploy.sh'
    'scripts/publish-skeleton.sh'  # 内部分发脚本，开源仓/源码包中无意义
    'scripts/publish-skills.sh'
    'scripts/publish-opensource.sh'
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

# docs/superpowers 是各模块（根目录、mcp/、skeleton/ 等）内部的规划工作
# 留痕（superpowers 技能生成的 plans/specs），与 publish-opensource.sh 的
# 排除规则保持一致，按目录名递归匹配，不随模块新增而遗漏。
find "${PKG_DIR}" -type d -path '*/docs/superpowers' -prune -exec rm -rf {} +

# 内部专用 CI 配置/私有化构建镜像定义，各模块（mcp/、skeleton/、fov-demo/、
# skill-eval/ 等）各有一份，按文件名递归匹配，不随模块新增而遗漏
# （.gitlab-ci.yml 含内部 CI 配置与 runner 标签；
# Dockerfile.private.*/.coverage/.lint/.test/.security/.owasp/.contract 均为内部
# CI 专用镜像，不是面向集成商的公开构建产物）。
find "${PKG_DIR}" -type f \( \
    -name '.gitlab-ci.yml' -o \
    -name 'Dockerfile.private.build' -o \
    -name 'Dockerfile.private.runtime' -o \
    -name 'Dockerfile.private.runtime.arm64' -o \
    -name 'Dockerfile.coverage' -o \
    -name 'Dockerfile.lint' -o \
    -name 'Dockerfile.test' -o \
    -name 'Dockerfile.security' -o \
    -name 'Dockerfile.owasp' -o \
    -name 'Dockerfile.contract' \
\) -delete

# DJI DevKit Team 是源码 Javadoc @author 标注（阿里巴巴规范强制要求每个类/公共方法
# 标注作者），源码本身不应为打包而改动；仅在打包产物中做脱敏替换，避免对外
# 分发包暴露开发者真实账号。用 grep -rl（非 -Z，BSD grep 在本机不支持
# null 分隔输出）+ while read 逐文件替换；整条管道末尾加 `|| true`，防止
# 未来匹配数降为 0 时 grep 的非零退出码在 `set -e` 下中断脚本。
AUTHOR_PLACEHOLDER='DJI DevKit Team'
grep -rl 'pioneer\.yi' "${PKG_DIR}" 2>/dev/null | while IFS= read -r f; do
    sed -i.bak "s/pioneer\.yi/${AUTHOR_PLACEHOLDER}/g" "$f" && rm -f "$f.bak"
done || true

# 3. 打包并生成 SHA256SUMS
tar -czf "${ARCHIVE}" -C "${OUT_DIR}" "${PKG_NAME}"
(cd "${OUT_DIR}" && shasum -a 256 "${PKG_NAME}.tar.gz" > "${PKG_NAME}.tar.gz.sha256")

echo "✅ 打包完成："
echo "   ${ARCHIVE}"
echo "   ${ARCHIVE}.sha256"
echo "   内容：$(tar -tzf "${ARCHIVE}" | grep -vc '/$') 个文件"
