# ============================================================
# FlightHub MCP Server — 多阶段构建 Dockerfile
# ============================================================
# 构建: docker build --build-arg SDK_ARCH=arm64 -t flighthub-mcp-server .
# 运行: docker run -p 8080:8080 -e FH_SSE_AUTH_TOKEN=your-token flighthub-mcp-server
# ============================================================

# --- Stage 1: 构建 ---
# 使用官方 maven:3.9.9-eclipse-temurin-17 镜像（内置 Maven，含 amd64/arm64 双架构 manifest）。
#
# 历史上这里曾手动 curl + tar 解压 Maven 压缩包以绕过 Docker Hub 大镜像下载限速，
# 但该步骤在 arm64 CI runner（DinD 嵌套容器）上触发 ENOSYS（Function not
# implemented）——根因是该 host 的嵌套容器环境对"批量解压归档/新建文件"类操作
# 普遍失效，与 Docker 存储驱动无关（overlayfs/fuse-overlayfs/vfs 均复现，已逐一
# 探测排除）。规避方式是把"批量写文件"步骤放在嵌套 DinD 容器之外的普通
# runner 上完成，只把已落盘的产物 COPY 进镜像。
#
# 本仓库 skeleton/ 与 mcp/ 其余 Dockerfile（lint/coverage/security/test/owasp）
# 均已使用官方 maven 镜像，此处改为一致做法：直接用预装 Maven 的基础镜像，
# 不再在构建阶段现场解压归档，从根上绕开 ENOSYS。
#
# 基础镜像按 digest 固定（两个 digest 都是上游的 OCI image index，仍同时包含
# linux/amd64 与 linux/arm64，不影响多架构构建）。改造前这里曾有 `sha512sum -c`
# 校验下载的 Maven 压缩包；改用官方镜像后校验由 Registry 的内容寻址完成，
# 但 tag 本身可被重新推送，因此补上 digest 以免上游 tag 漂移导致构建不可复现。
FROM maven:3.9.9-eclipse-temurin-17@sha256:f58d59b6273e785ac0a4477f6e9b5ba1d7731c75b906c0f7b34076f1851318cc AS builder

WORKDIR /build

# 使用仓库内 config/settings.xml（默认 Maven Central，CI 通过 MAVEN_MIRROR_URL 注入内网镜像）
COPY config/settings.xml /root/.m2/settings.xml

# 安装私有 SDK jar（不在任何远端仓库）
# 注意：SDK 按 CPU 架构分两个包，两者版本号与 Maven 坐标完全相同，只能靠构建参数二选一。
#       故意不设默认值——漏传时 COPY 直接失败，避免静默打进错架构的包。
#   amd64: docker build --build-arg SDK_ARCH=amd64 ...
#   arm64: docker build --build-arg SDK_ARCH=arm64 ...
ARG SDK_ARCH
COPY libs/${SDK_ARCH}/flight-hub-sdk-3.3.7.jar /tmp/flight-hub-sdk-3.3.7.jar
RUN mvn install:install-file \
    -Dfile=/tmp/flight-hub-sdk-3.3.7.jar \
    -DgroupId=com.dji.flighthubsdk \
    -DartifactId=flight-hub-sdk \
    -Dversion=3.3.7 \
    -Dpackaging=jar \
    -B

COPY mcp/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY mcp/src ./src
RUN mvn package -DskipTests -B

# --- Stage 2: 运行 ---
# 使用 eclipse-temurin:17-jre（与 builder 同 JDK 版本），同样按 digest 固定
FROM eclipse-temurin:17-jre@sha256:e04fa72be13b257a59fdec6dc14af78c8f80f035d3c1ed4e03e45552a231d4c8

LABEL maintainer="DJI FlightHub SDK Team"
LABEL description="FlightHub PaaS MCP Server - AI Agent runtime control"

# SDK 原生依赖（native 库运行所需图形库）
# 包名沿用的是 Noble 起的 t64 命名（libgl1-mesa-glx→libgl1、libegl1-mesa→libegl1、
# libgles2-mesa→libgles2、libasound2→libasound2t64），但**基础镜像的 apt 源不是
# 24.04 Noble**：eclipse-temurin:17-jre 当前发布的是 Ubuntu resolute（26.04 开发系列），
# pool 里的具体文件名会随开发系列滚动更新。
#
# arm64 CI（受限网络环境，无任何直连出口），apt 只能经内网 HTTP 代理
# 转发；该代理会瞬时拒绝/找不到某个 pool 文件——实测同一分钟、同一台 runner 上，
# 一个 job 报 `libsndfile1_1.2.2-4_arm64.deb 404` 退出，另一个 job 正常装上同
# 一版本。所以这条指令必须能扛住瞬时失败，不能一次 404 就判死。
#
# 重试策略：最多 apt_attempts 次，最后一次才加 --fix-missing（允许跳过取不到的包，
# 先把能装的装上）。--fix-missing 的语义是"跳过而不是失败"，因此无论走哪个分支，
# 安装结束后都要校验——否则会产出缺原生库的镜像，问题要到运行时才暴露。
#
# 校验为什么不复用 dpkg：列表里存在**过渡名/虚拟包**。t64 迁移后 libglib2.0-0
# 由 libglib2.0-0t64 通过 Provides 提供，dpkg 里压根没有 libglib2.0-0 这个包名，
# `dpkg -s` 必然报"未安装"（实测把 amd64 构建判死过）。改为让 apt 自己对同一份
# 列表做安装模拟：只要还有任何一个包没到位，输出里就会有一条 `Inst ` 行；全部
# 就位时一行都没有。用 `Inst ` 而不是解析末尾的摘要句，是因为摘要的措辞随 apt
# 版本会变，`Inst <pkg> (<ver> ...)` 是模拟器逐包输出的稳定格式。包名在索引里
# 根本不存在时，模拟命令本身会非 0 退出，同样判失败。
RUN set -eux; \
    apt_attempts=3; \
    packages="libatomic1 libgl1 libegl1 libgles2 libglib2.0-0 libx11-6 libxext6 \
libxrender1 libxdamage1 libxfixes3 libxcomposite1 libxrandr2 libxi6 libxcursor1 \
libxinerama1 libxtst6 libdrm2 libgbm1 libxshmfence1 libxxf86vm1 libpulse0 \
libasound2t64 curl"; \
    export DEBIAN_FRONTEND=noninteractive; \
    attempt=1; \
    while :; do \
        extra=""; \
        if [ "${attempt}" -ge "${apt_attempts}" ]; then \
            extra="--fix-missing"; \
        fi; \
        if apt-get update -o Acquire::Retries=3 \
            && apt-get install -y --no-install-recommends ${extra} ${packages}; then \
            break; \
        fi; \
        if [ "${attempt}" -ge "${apt_attempts}" ]; then \
            echo "ERROR: apt 安装重试 ${apt_attempts} 次后仍失败" >&2; \
            exit 1; \
        fi; \
        echo "WARN: apt 第 ${attempt} 次安装失败，10s 后重试" >&2; \
        attempt=$((attempt + 1)); \
        sleep 10; \
    done; \
    if ! sim_out=$(LC_ALL=C apt-get install -s -y --no-install-recommends ${packages}); then \
        echo "ERROR: 依赖校验的模拟安装执行失败，输出如下" >&2; \
        echo "${sim_out}" >&2; \
        exit 1; \
    fi; \
    if printf '%s\n' "${sim_out}" | grep -q '^Inst '; then \
        echo "ERROR: 仍有原生依赖未安装到位，模拟安装明细如下" >&2; \
        echo "${sim_out}" >&2; \
        exit 1; \
    fi; \
    rm -rf /var/lib/apt/lists/*

# 安全：非 root 用户
RUN groupadd -r mcp && useradd -r -g mcp mcp

WORKDIR /app

# 从构建阶段复制 fat jar
COPY --from=builder /build/target/*.jar /app/flighthub-mcp.jar

# 默认环境变量
ENV FH_TRANSPORT=sse
ENV FH_SSE_PORT=8080
ENV FH_MAX_SECURITY_LEVEL=2
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# 健康检查 — 使用自定义 /pf-devkit/ai-plugin/mcp/health 端点（spring.webflux.base-path 前置）
HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl -sf http://localhost:8080/pf-devkit/ai-plugin/mcp/health || exit 1

USER mcp

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar /app/flighthub-mcp.jar --spring.profiles.active=sse"]
