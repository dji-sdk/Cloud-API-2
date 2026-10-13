#!/usr/bin/env bash
# make_source_package.sh —— 源码外包打包脚本（P2-02 修复载体）
#
# 生成可外发的源码包 tar.gz。
#
# 打包语义（v3，git 白名单 + 单一排除清单）：
# - 只打包 git 已跟踪文件（与 git archive 同语义），从根上杜绝未跟踪的构建产物、
#   本地残留混入源码包（v1 的 rsync 黑名单曾漏入 build_java_release/ 等目录）
# - 在白名单基础上按 PKG_EXCLUDE_RULES 剔除两类：真实航拍测试录像（P2-02）、
#   macOS 预编译产物（README.md「官方支持平台」只有 Linux x86_64，且 mac 库占
#   921 文件 / 91.5 MiB 未压缩，约占源码包体积四成）
# - 排除清单是唯一事实源：拷贝过滤、干净度门禁、打包后回归门禁、终端摘要共用它，
#   不再散落在 case 分支与 echo 字面量里
# - 工作区与 HEAD 有差异时默认拒绝打包（保证包内容 = 某 commit，可追溯），
#   但被排除的路径不参与判定（mac 库常在本机重编，不该挡住 Linux 源码包）；
#   调试可 FHSDK_ALLOW_DIRTY=1 跳过
# - 版本号取自仓根 VERSION（唯一事实源），包名自动联动；RELEASE_TAG 门禁防
#   "包名与仓内版本脱节"（P1-04 历史问题）
# - tar 不写宿主元数据：不带打包人账号/属组，不带 macOS xattr（com.apple.provenance）
#
# 说明：二进制 Java SDK 包仍由 scripts/build_java.sh 负责，两者互不替代
#
# 用法：
#   ./make_source_package.sh              # 输出到 <repo>/dist/
#   ./make_source_package.sh <输出目录>    # 指定输出目录
#   RELEASE_TAG=vX.Y.Z ./make_source_package.sh     # 发布侧指定 tag，强制与 VERSION 一致
#   FHSDK_ALLOW_DIRTY=1 ./make_source_package.sh    # 调试：跳过干净度检查
#   FHSDK_LIST_EXCLUDED=1 ./make_source_package.sh  # 只打印排除清单命中项，不出包
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
VERSION="$(tr -d '[:space:]' < "${ROOT_DIR}/VERSION")"
VERSION="${VERSION#v}"

ARG1="${1:-}"
case "${ARG1}" in
    -*)
        echo "错误: 不支持选项 '${ARG1}'；用法: ./make_source_package.sh [输出目录]" >&2
        exit 1
        ;;
esac

command -v rsync >/dev/null 2>&1 || { echo "错误: 缺少 rsync，无法按白名单暂存" >&2; exit 1; }

# ---------------------------------------------------------------------------
# 排除清单（唯一事实源）
#
# 每项格式 "说明:ERE"。ERE 锚在 ^ 且只用 [^/]+ 而非 .*，保证零误伤 —— 近名文件
# deps/base/base/common/macros.h、third_party/aws-cpp-sdk-s3/lib/linux/x86_64/
# include/aws/common/macros.h、TerraBuildCreatePanoramaCallback.java 都不受影响。
#
# 两条 macOS 规则分别对应 Java 侧运行库与三方预编译库的两种目录布局：
#   platform/java/lib/macosx-arm64/    （arch 写成 [^/]+，防将来 macosx-x86_64 漏网）
#   third_party/<comp>/lib/macosx/arm64/
# 注意不要改成 git pathspec 写法: ':(exclude)third_party/*/lib/macosx' 这类带通配符
# 又不 glob 到文件层的 pathspec 会静默匹配 0 个文件（实测），出不了错但也过滤不掉。
#
# 只剔预编译产物，不剔 third_party/*/bin/build_*mac*.sh 这三个 mac 编译配方：
# build_aws_s3.sh / build_libcurl.sh / build_openssl_1_1_1.sh 等 Linux 配方内部也
# 各有 Darwin 分支，删一半只会制造悬空文档引用，且它们是纯文本、无体积影响。
# ---------------------------------------------------------------------------
PKG_EXCLUDE_RULES=(
    "测试录像:^deps/h264_analyze/video/"
    "macOS 预编译产物:^platform/java/lib/macosx-[^/]+/"
    "macOS 预编译产物:^third_party/[^/]+/lib/macosx/"
)

PKG_EXCLUDE_RE=""
for _rule in "${PKG_EXCLUDE_RULES[@]}"; do
    PKG_EXCLUDE_RE="${PKG_EXCLUDE_RE:+${PKG_EXCLUDE_RE}|}${_rule#*:}"
done

# P1-04 门禁 1: 版本号必须是 X.Y.Z 三段式
if ! [[ "${VERSION}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "错误: VERSION 文件内容格式非法: '${VERSION}'（应为 X.Y.Z，如 3.3.7）" >&2
    exit 1
fi

# P1-04 门禁 2: 发布侧若指定 RELEASE_TAG（如 RELEASE_TAG=v3.3.7），必须与 VERSION 一致，
# 防止"包目录名与仓内版本号脱节"的历史问题（v3.3.5 包名 vs VERSION 3.3.0）复发
if [ -n "${RELEASE_TAG:-}" ]; then
    TAG_VER="${RELEASE_TAG#v}"
    if [ "${TAG_VER}" != "${VERSION}" ]; then
        echo "错误: RELEASE_TAG(${RELEASE_TAG}) 与 VERSION(${VERSION}) 不一致" >&2
        echo "      请先更新仓根 VERSION 文件再打包（版本号唯一事实源）" >&2
        exit 1
    fi
fi

# CFG-02 门禁: 内部测试场地坐标不得回流进外发源码（git grep 只查已跟踪文件的工作区内容）。
# 扫全仓而不只扫 platform/java: 同一批坐标出现在 src/、deps/、README 里一样是泄露。
# -I 跳过二进制文件: 全仓有近 300MB 预编译库, 不加会因某个 .a 里偶然出现相同数字串而误拦。
if git -C "${ROOT_DIR}" grep -qIE '22\.79[0-9]{4,}|114\.35[0-9]{4,}'; then
    echo "错误: 检出内部测试场地坐标残留(CFG-02), 禁止打包:" >&2
    git -C "${ROOT_DIR}" grep -nIE '22\.79[0-9]{4,}|114\.35[0-9]{4,}' >&2 || true
    exit 1
fi

# P0-01/P0-02 门禁: TLS 验证安全默认必须为 true——白名单精确检查法。
# 逐一检查必须为 true 的 6 个声明位置(结构体默认值 / 全局兜底值 / 函数参数默认值)。
# 白名单法比 grep 黑名单更稳定: 不受 LOG 字符串、回调局部变量等误报干扰,
# 新增默认值位置时必须同步到这里——遗漏会被全仓 fallback 兜底检查捕获。
P0_TLS_FAIL=0
# 格式: "文件:grep模式" —— 模式必须精确匹配声明行, 避免命中同文件内的局部变量/日志字符串。
# CppWebSocket.cpp 特殊: struct WssTlsConfig 的 verify_ssl 字段在 L280 附近,
# 而 L982 处回调内 `bool verify_ssl = false` 是合法的 null-guard 局部变量(随后从 tls_cfg 读取真实值)。
# 因此用 struct 声明上下文(WssTlsConfig)识别: 取 struct 块内的行。
P0_TLS_CHECKS=(
    "deps/base/common/http_client/http_request_sync.cc|g_verify_ssl\s*=\s*false"
    "deps/notification_center_sdk/src/notification_center_sdk_api/Common.h|bool verify_ssl\s*=\s*false"
    "src/api/message.h|bool verify_ssl\s*=\s*false"
    "src/bll/notification_center/notification_center.h|bool verify_ssl\s*=\s*false"
    "platform/java/flight_hub_sdk/src/main/java/com/dji/flighthubsdk/bean/FlightHubSdkConfig.java|verifySsl\s*=\s*false"
)
for _chk in "${P0_TLS_CHECKS[@]}"; do
    _file="${_chk%%|*}"
    _re="${_chk#*|}"
    if [ ! -f "${ROOT_DIR}/${_file}" ]; then
        echo "错误: P0-01/P0-02 门禁文件缺失: ${_file}" >&2
        P0_TLS_FAIL=1
        continue
    fi
    if grep -qE "${_re}" "${ROOT_DIR}/${_file}"; then
        echo "错误: ${_file}: 匹配 '${_re}' — TLS 默认值为 false(P0-01/P0-02 回归)" >&2
        P0_TLS_FAIL=1
    fi
done
# CppWebSocket.cpp 单独检查: WssTlsConfig 结构体的 verify_ssl 字段(L278-285 附近)。
# 不能全文件 grep, 否则误报回调内的合法局部变量。取 struct 声明到下一个 '}' 之间的行。
_cppws="${ROOT_DIR}/deps/notification_center_sdk/src/websocket/CppWebSocket.cpp"
if [ -f "${_cppws}" ]; then
    if sed -n '/^struct WssTlsConfig/,/^}/p' "${_cppws}" | grep -qE 'verify_ssl\s*=\s*false'; then
        echo "错误: CppWebSocket.cpp: WssTlsConfig::verify_ssl 默认值为 false(P0-01/P0-02 回归)" >&2
        P0_TLS_FAIL=1
    fi
else
    echo "错误: P0-01/P0-02 门禁文件缺失: deps/notification_center_sdk/src/websocket/CppWebSocket.cpp" >&2
    P0_TLS_FAIL=1
fi
# fallback: 全仓扫描兜底——捕获白名单未覆盖的新增默认值位置。
# 排除已知安全的 false 出现: LOG 字符串、回调局部变量(tls_cfg 上下文)、注释、引号内字符串。
# CppWebSocket.cpp 已由白名单 sed 方式精确检查 struct 字段, fallback 排除整个文件
# (该文件内回调函数的 `bool verify_ssl = false` 局部变量是合法 null-guard, 不是默认值)。
P0_FALLBACK="$(git -C "${ROOT_DIR}" grep -nE '(verify_ssl|verifySsl)\s*=\s*false' \
        -- 'deps/*.cc' 'deps/*.cpp' 'deps/*.h' 'src/*.h' 'src/*.cpp' \
           'platform/java/flight_hub_sdk/src/*.java' 2>/dev/null \
    | grep -vE 'LOG_|tls_cfg|//.*verify_ssl|"[^"]*verify_ssl|CppWebSocket\.cpp' || true)"
if [ -n "${P0_FALLBACK}" ]; then
    echo "错误: P0-01/P0-02 兜底扫描发现未覆盖的 TLS 默认值 false:" >&2
    echo "${P0_FALLBACK}" >&2
    P0_TLS_FAIL=1
fi
if [ "${P0_TLS_FAIL}" -eq 1 ]; then
    echo "      安全默认必须为 true; 私有化自签名场景应经 ssl_ca_path 配置 CA" >&2
    exit 1
fi

# FH36-P2-02 门禁(单规则): 日志内容实参必须经 log_safe() 包裹。
# 规则: LOG 调用行(含次行实参)出现 response/body/headers_str 等内容形态的
# .c_str(), 且该行不含 log_safe( → 违规。注释行跳过。
# 已知边界(接受): 同一行同时存在包裹与裸实参时放行, 依赖评审兜底。
if git -C "${ROOT_DIR}" grep -A1 -nE 'LOG_(INFO|WARN|ERROR|DEBUG)\(' -- src/ deps/ 2>/dev/null \
    | grep -vE ':[0-9]+[:-][[:space:]]*//' \
    | grep -E '\b(response|response_data|body|headers_str)\.c_str\(\)|\(\*common_response\.data\)\.c_str\(\)|to_string\(request' \
    | grep -v 'log_safe('; then
    echo "错误: 检出未经 log_safe() 包裹的请求/响应体日志实参(FH36-P2-02), 禁止打包" >&2
    echo "      内容实参须包裹(见 deps/base/common/log_safe.h), 且内容类日志应为 DEBUG 级" >&2
    exit 1
fi

# LIC-01 门禁: SDK 源码版权头(SPDX)全覆盖, 缺失即拒绝打包。
# 范围: SDK 自有代码(src/ deps/ platform/java); 三方预编译库不含源码文件。
if git -C "${ROOT_DIR}" grep -L 'SPDX-License-Identifier' -- \
        'src/*.cpp' 'src/*.cc' 'src/*.h' 'deps/*.cpp' 'deps/*.cc' 'deps/*.h' \
        'platform/java/*.java' 2>/dev/null | grep -q .; then
    echo "错误: 存在缺失 SPDX 版权头的源码文件(LIC-01), 禁止打包:" >&2
    git -C "${ROOT_DIR}" grep -L 'SPDX-License-Identifier' -- \
        'src/*.cpp' 'src/*.cc' 'src/*.h' 'deps/*.cpp' 'deps/*.cc' 'deps/*.h' \
        'platform/java/*.java' 2>/dev/null | head -10 >&2
    exit 1
fi

# FH36-L-01 门禁: gradle-wrapper.jar 必须与官方 Gradle 原版字节一致（防替换植入），
# 且 gradle-wrapper.properties 必须带 distributionSha256Sum（wrapper 下载发行包时自校验；
# 值与 distributionUrl 版本是否匹配由 wrapper 运行时强制，门禁只查存在性）。
# 基线已取证(2026-09-30): 33ad4583… = 官方 Gradle 7.1.0-7.3.3 wrapper jar，字节级一致。
# 升级 Gradle 换 jar 时须同步更新基线（官方来源: gradle/gradle 仓库对应 tag）。
WRAPPER_JAR_SHA256="33ad4583fd7ee156f533778736fa1b4940bd83b433934d1cc4e9f608e99a6a89"
for _jar in $(git -C "${ROOT_DIR}" ls-files -- '*gradle-wrapper.jar'); do
    if command -v shasum >/dev/null 2>&1; then
        _h="$(shasum -a 256 "${ROOT_DIR}/${_jar}" | cut -d' ' -f1)"
    else
        _h="$(sha256sum "${ROOT_DIR}/${_jar}" | cut -d' ' -f1)"
    fi
    if [ "${_h}" != "${WRAPPER_JAR_SHA256}" ]; then
        echo "错误: ${_jar} 哈希与官方基线不符(FH36-L-01), 禁止打包:" >&2
        echo "      实际 ${_h}" >&2
        echo "      基线 ${WRAPPER_JAR_SHA256} (官方 Gradle 7.1-7.3.3; 升级 Gradle 须同步更新)" >&2
        exit 1
    fi
done
for _prop in $(git -C "${ROOT_DIR}" ls-files -- '*gradle-wrapper.properties'); do
    if ! grep -q '^distributionSha256Sum=' "${ROOT_DIR}/${_prop}"; then
        echo "错误: ${_prop} 缺 distributionSha256Sum(FH36-L-01), 禁止打包" >&2
        echo "      须填 distributionUrl 指向的官方发行包 SHA256, 由 wrapper 下载时自校验" >&2
        exit 1
    fi
done

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "${WORK_DIR}"' EXIT
TRACKED_LIST="${WORK_DIR}/tracked"
KEPT_LIST="${WORK_DIR}/kept"
FILE_LIST="${WORK_DIR}/filelist"
TAR_LIST="${WORK_DIR}/tarlist"

# NUL 分隔清单的条目数
nul_count() { tr -cd '\0' < "$1" | wc -c | tr -d ' '; }

git -C "${ROOT_DIR}" ls-files -z > "${TRACKED_LIST}"
TRACKED_COUNT="$(nul_count "${TRACKED_LIST}")"

# 逐条规则计数: 既是给打包人看的摘要, 也是"模式写错"的自检 ——
# 正则/pathspec 写错时过滤会静默命中 0 个, 包照样出, 只有这里能把它变成硬失败
echo "排除清单（共 ${#PKG_EXCLUDE_RULES[@]} 条规则，对 ${TRACKED_COUNT} 个已跟踪文件生效）:"
for _rule in "${PKG_EXCLUDE_RULES[@]}"; do
    _label="${_rule%%:*}"
    _re="${_rule#*:}"
    _n="$(tr '\0' '\n' < "${TRACKED_LIST}" | grep -cE "${_re}" || true)"
    printf '  %-40s %5s 个  %s\n' "${_re}" "${_n}" "${_label}"
    if [ "${_n}" -eq 0 ]; then
        echo "错误: 排除规则未命中任何文件, 疑似模式写错或规则已过期: ${_re}" >&2
        echo "      确认仓内确实已无此类文件后, 请从 PKG_EXCLUDE_RULES 删除该条规则" >&2
        exit 1
    fi
done

grep -zvE "${PKG_EXCLUDE_RE}" "${TRACKED_LIST}" > "${KEPT_LIST}" || true
KEPT_COUNT="$(nul_count "${KEPT_LIST}")"
EXCLUDED_COUNT=$((TRACKED_COUNT - KEPT_COUNT))

if [ "${EXCLUDED_COUNT}" -eq 0 ]; then
    echo "错误: 排除清单一个文件都没剔除, 过滤链路已失效" >&2
    exit 1
fi

if [ -n "${FHSDK_LIST_EXCLUDED:-}" ]; then
    echo "将被剔除的 ${EXCLUDED_COUNT} 个文件:"
    tr '\0' '\n' < "${TRACKED_LIST}" | grep -E "${PKG_EXCLUDE_RE}" || true
    echo "（FHSDK_LIST_EXCLUDED 已设置，不生成包）"
    exit 0
fi

# 工作区干净度检查: 包内容取自工作区中"已跟踪文件"的内容, 与 HEAD 有差异即拒绝,
# 保证外发包始终对应某个 commit, 可追溯可复现。
# 被排除的路径不参与判定 —— mac 库在本机重编后长期处于 modified, 而它根本不进包,
# 让它挡住打包只会逼人开 FHSDK_ALLOW_DIRTY, 反而把真正的干净度保护一起关掉。
if [ -z "${FHSDK_ALLOW_DIRTY:-}" ]; then
    DIRTY="$(git -C "${ROOT_DIR}" diff --name-only -z HEAD \
        | grep -zvE "${PKG_EXCLUDE_RE}" | tr '\0' '\n' || true)"
    if [ -n "${DIRTY}" ]; then
        echo "错误: 以下入包文件与 HEAD 存在差异, 拒绝打包（外发包必须对应确定 commit）:" >&2
        # || true: head 提前退出会给 printf 一个 SIGPIPE, pipefail 下会把整脚本带走(退 141)
        printf '%s\n' "${DIRTY}" | head -20 >&2 || true
        echo "      请先提交改动; 调试打包可 FHSDK_ALLOW_DIRTY=1 跳过本检查" >&2
        echo "      注: 排除清单内的路径（测试录像 / macOS 产物）不计入本检查" >&2
        exit 1
    fi
fi

OUT_DIR="${ARG1:-${ROOT_DIR}/dist}"
PKG_NAME="flight_hub_sdk_v${VERSION}_src_$(date +%Y%m%d)"
PKG_FILE="${OUT_DIR}/${PKG_NAME}.tar.gz"
STAGING="${WORK_DIR}/${PKG_NAME}"
PKG_TMP="${WORK_DIR}/${PKG_NAME}.tar.gz"

# 过滤索引中已从工作区删除的条目: rsync 遇清单内缺失文件会退 23, set -e 下直接中止
while IFS= read -r -d '' f; do
    [ -f "${ROOT_DIR}/${f}" ] || [ -L "${ROOT_DIR}/${f}" ] || continue
    printf '%s\0' "$f"
done < "${KEPT_LIST}" > "${FILE_LIST}"
PACKED_COUNT="$(nul_count "${FILE_LIST}")"
if [ "${PACKED_COUNT}" -ne "${KEPT_COUNT}" ]; then
    echo "提示: 索引中有 $((KEPT_COUNT - PACKED_COUNT)) 个条目已从工作区删除, 未入包" >&2
fi

# 白名单防御: 已跟踪文件中不应出现机密类文件（防历史误提交）。
# .env 类锚定到路径段起始 (^|/): 子目录 demo/.env 与 .env.local/.envrc 变体同样拦截
# （原先的 ^\.env 只锚定仓库根, 子目录 .env 静默放行）;
# 扩展名类（pem/key/jks/keystore/p12）锚定记录尾。两处 grep 共用同一变量, 防漂移。
SECRET_FILE_RE='(^|/)\.env|\.(pem|key|jks|keystore|p12)$'
if grep -zqE "${SECRET_FILE_RE}" "${FILE_LIST}"; then
    echo "错误: git 跟踪文件中存在机密类文件（.env* / pem / key / jks / p12）, 禁止外发:" >&2
    grep -zE "${SECRET_FILE_RE}" "${FILE_LIST}" | tr '\0' '\n' >&2
    exit 1
fi

mkdir -p "${OUT_DIR}"
mkdir -p "${STAGING}"

# 按白名单复制（--files-from 模式下不会带入清单外任何文件, 包括未跟踪文件）
rsync -a --from0 --files-from="${FILE_LIST}" "${ROOT_DIR}/" "${STAGING}/"

# tar 参数: 不写打包人 uid/gid/账号名, 不带 macOS 扩展属性。
# macOS 自带 bsdtar 会把 com.apple.provenance 之类 xattr 写成 pax 头（实测已发货的
# v3.3.6 源码包里有 15324 处），--no-xattrs 才能去掉，--no-mac-metadata 单用无效。
TAR_OPTS=(--no-xattrs)
if tar --version 2>/dev/null | grep -q bsdtar; then
    TAR_OPTS+=(--no-mac-metadata --uid 0 --gid 0 --uname "" --gname "")
else
    TAR_OPTS+=(--owner=0 --group=0 --numeric-owner)
fi
# BUILD-01 止血: 清除暂存目录中所有 .so 内嵌的构建机路径(等长二进制替换, 不改段布局)。
# 覆盖 platform/java/lib/ 和 third_party/ 下所有预编译库, 保证源码包内残留归零。
# 操作对象是 rsync 副本, 不修改仓库工作区原文件。
if command -v python3 >/dev/null 2>&1; then
    python3 - "${STAGING}" <<'PY'
import os, sys, glob
staging = sys.argv[1]
mapping = [
    (b'/build/vendor', b'/opt/vendor__'),  # 等长 13 字符
    (b'/home/jenkins', b'/opt/vendor__'),  # 等长 13 字符
    (b'/code_base',    b'/opensrc__'),     # 等长 10 字符
]
so_files = glob.glob(os.path.join(staging, '**', '*.so*'), recursive=True)
total_replacements = 0
for p in sorted(set(so_files)):
    if not os.path.isfile(p) or os.path.islink(p):
        continue
    name = os.path.relpath(p, staging)
    with open(p, 'rb') as f:
        data = f.read()
    size_before = len(data)
    changed = False
    for old, new in mapping:
        assert len(old) == len(new), f'长度不等: {old!r} vs {new!r}'
        n = data.count(old)
        if n:
            data = data.replace(old, new)
            print(f'BUILD-01(pkg): {name}: {old.decode()} -> {new.decode()}: {n} 处')
            total_replacements += n
            changed = True
    assert len(data) == size_before, f'{name}: 替换后长度变化!'
    if changed:
        with open(p, 'wb') as f:
            f.write(data)
if total_replacements:
    print(f'BUILD-01(pkg): 共替换 {total_replacements} 处构建路径残留')
else:
    print('BUILD-01(pkg): 无构建路径残留(已清洁)')
PY
else
    echo "警告: python3 不可用, 跳过 BUILD-01 .so 路径清理(源码包可能含构建路径残留)" >&2
fi

# gzip -n: 不把内层 tar 文件名与时间戳写进 gz 头, 同一 commit 重复打包字节更接近一致
# 先落在 WORK_DIR: 门禁全过再 mv 进 OUT_DIR, 避免门禁失败时把不合格的包留在发布目录里
COPYFILE_DISABLE=1 tar -C "${WORK_DIR}" "${TAR_OPTS[@]}" -cf - "${PKG_NAME}" \
    | gzip -9n > "${PKG_TMP}"

# ---------------------------------------------------------------------------
# 打包后门禁复查: 列一次成员清单反复 grep（原先四次 tar -tzf 要把整包解压四遍）
# ---------------------------------------------------------------------------
tar -tzf "${PKG_TMP}" > "${TAR_LIST}"
FAIL=0
# BUILD-01 门禁: 随包 .so 不得含构建机路径残留（/build/vendor、/home/jenkins）。
# 解包到临时目录做 strings 检查, 对已替换后的二进制逐文件扫描。
BUILD01_TMPDIR="${WORK_DIR}/_build01_check"
mkdir -p "${BUILD01_TMPDIR}"
tar -xzf "${PKG_TMP}" -C "${BUILD01_TMPDIR}"
BUILD01_HITS=""
while IFS= read -r _so; do
    _count="$(strings "${_so}" | grep -cE '/build/vendor|/home/jenkins' || true)"
    if [ "${_count}" -gt 0 ]; then
        _relpath="${_so#${BUILD01_TMPDIR}/}"
        BUILD01_HITS="${BUILD01_HITS}  ${_relpath}: ${_count} 处\n"
    fi
done < <(find "${BUILD01_TMPDIR}" -name '*.so*' -type f)
rm -rf "${BUILD01_TMPDIR}"
if [ -n "${BUILD01_HITS}" ]; then
    echo "错误: 源码包内 .so 文件仍含构建机路径残留(BUILD-01), 禁止打包:" >&2
    printf '%b' "${BUILD01_HITS}" >&2
    FAIL=1
fi
# P2-02: 测试录像目录不得混入
if grep -q 'deps/h264_analyze/video/' "${TAR_LIST}"; then
    echo "错误: 源码包内检出测试录像目录（P2-02）" >&2; FAIL=1
fi
# macOS 产物不得混入（README.md「官方支持平台」只有 Linux x86_64）。
# 前置斜杠是关键: '/macosx[-/]' 挡住 macosx-arm64/ 与 macosx/, 又不误伤必须保留的
# third_party/paho_mqtt_c/bin/build_macosx_arm64.sh（那里是 _macosx）。
# 类别放宽到 macos|darwin|osx: 实测对保留清单零命中, 顺带防将来换名复活。
if grep -qE '\.dylib$|/(macosx|macos|darwin|osx)[-/]' "${TAR_LIST}"; then
    echo "错误: 源码包内检出 macOS 预编译产物" >&2
    grep -E '\.dylib$|/(macosx|macos|darwin|osx)[-/]' "${TAR_LIST}" | head -20 >&2 || true
    FAIL=1
fi
# 防误伤回归: AWS SDK credentials.h 必须在包内（曾被 credentials.* 排除项误伤致编译失败）。
# 路径钉死到 linux 树: 原先的 .* 通配会被 macosx 树满足, 等于漏掉真正要守的那一份。
if ! grep -q 'third_party/aws-cpp-sdk-s3/lib/linux/x86_64/include/aws/auth/credentials\.h' "${TAR_LIST}"; then
    echo "错误: 源码包缺失 AWS SDK credentials.h（编译必需头文件被误排除）" >&2; FAIL=1
fi
# 未跟踪构建产物不得混入（白名单机制回归验证; 注意 $ 锚点, 勿误伤 src/version.h.in 模板）
if grep -qE 'build_java_release/|platform/android/local-maven/|^'"${PKG_NAME}"'/src/version\.h$' "${TAR_LIST}"; then
    echo "错误: 源码包内检出未跟踪构建产物/生成文件混入" >&2; FAIL=1
fi
# 包内文件数必须与白名单清单一致, 防止暂存/打包环节静默丢文件
FILE_COUNT="$(grep -vc '/$' "${TAR_LIST}" || true)"
if [ "${FILE_COUNT}" -ne "${PACKED_COUNT}" ]; then
    echo "错误: 包内文件数 ${FILE_COUNT} 与白名单清单 ${PACKED_COUNT} 不一致" >&2; FAIL=1
fi
if [ "$FAIL" -eq 1 ]; then
    echo "      不合格的包未写入 ${OUT_DIR}（随临时目录一并清理）" >&2
    exit 1
fi

mv "${PKG_TMP}" "${PKG_FILE}"

echo "源码包已生成: ${PKG_FILE}"
echo "包含文件数: ${FILE_COUNT}（git 已跟踪 ${TRACKED_COUNT} 个, 按排除清单剔除 ${EXCLUDED_COUNT} 个）"
du -sh "${PKG_FILE}"

# 生成 .sha256 旁车校验文件, 供下游完整性验证(对齐 devkit 实践)
if command -v sha256sum >/dev/null 2>&1; then
    ( cd "$(dirname "${PKG_FILE}")" && sha256sum "$(basename "${PKG_FILE}")" > "${PKG_FILE}.sha256" )
elif command -v shasum >/dev/null 2>&1; then
    ( cd "$(dirname "${PKG_FILE}")" && shasum -a 256 "$(basename "${PKG_FILE}")" > "${PKG_FILE}.sha256" )
fi
if [ -f "${PKG_FILE}.sha256" ]; then
    echo "SHA256 校验文件: ${PKG_FILE}.sha256"
    cat "${PKG_FILE}.sha256"
fi
