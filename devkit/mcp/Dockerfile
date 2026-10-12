# ============================================================
# FlightHub MCP Server — 多阶段构建 Dockerfile
# ============================================================
# 构建: docker build -t flighthub-mcp-server .
# 运行: docker run -p 8080:8080 -e FH_SSE_AUTH_TOKEN=your-token flighthub-mcp-server
# ============================================================

# --- Stage 1: 构建 ---
# 使用本地已有的 eclipse-temurin:17-jdk，手动安装 Maven（绕过 Docker Hub 大镜像下载限速）
FROM eclipse-temurin:17-jdk AS builder

# 从阿里云镜像安装 Maven 3.9.9，并校验 Apache 官方 SHA512 校验和
# 校验和来源：https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz.sha512
ARG MAVEN_VERSION=3.9.9
ARG MAVEN_SHA512=a555254d6b53d267965a3404ecb14e53c3827c09c3b94b5678835887ab404556bfaf78dcfe03ba76fa2508649dca8531c74bca4d5846513522404d48e8c4ac8b
ARG MAVEN_URL=https://maven.aliyun.com/repository/central/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz
RUN curl -fsSL "${MAVEN_URL}" -o /tmp/maven.tar.gz \
    && echo "${MAVEN_SHA512} /tmp/maven.tar.gz" | sha512sum -c - \
    && tar -xzf /tmp/maven.tar.gz -C /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn \
    && rm /tmp/maven.tar.gz
ENV MAVEN_HOME=/opt/apache-maven-${MAVEN_VERSION}
ENV PATH="${MAVEN_HOME}/bin:${PATH}"

WORKDIR /build

# 使用公司内部 Maven 仓库
COPY config/settings.xml /root/.m2/settings.xml

# 安装私有 SDK jar（不在任何远端仓库）
COPY libs/flight-hub-sdk-3.3.7.jar /tmp/flight-hub-sdk-3.3.7.jar
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
# 使用本地已有的 eclipse-temurin:17-jre（与 builder 同 JDK 版本）
FROM eclipse-temurin:17-jre

LABEL maintainer="DJI FlightHub SDK Team"
LABEL description="FlightHub PaaS MCP Server - AI Agent runtime control"

# SDK 原生依赖（native 库运行所需图形库）
# 注意：包名基于 Ubuntu 24.04 Noble（eclipse-temurin:17-jre 基础镜像）
# Jammy → Noble 变更：libgl1-mesa-glx→libgl1, libegl1-mesa→libegl1,
#                      libgles2-mesa→libgles2, libasound2→libasound2t64
RUN apt-get update && apt-get install -y --no-install-recommends \
    libatomic1 \
    libgl1 \
    libegl1 \
    libgles2 \
    libglib2.0-0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxdamage1 \
    libxfixes3 \
    libxcomposite1 \
    libxrandr2 \
    libxi6 \
    libxcursor1 \
    libxinerama1 \
    libxtst6 \
    libdrm2 \
    libgbm1 \
    libxshmfence1 \
    libxxf86vm1 \
    libpulse0 \
    libasound2t64 \
    curl \
    && rm -rf /var/lib/apt/lists/*

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
