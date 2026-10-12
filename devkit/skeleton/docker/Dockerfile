# ============== Stage 1: Build ==============
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 使用公司内部 Maven 仓库
COPY config/settings.xml /root/.m2/settings.xml

# 安装私有 SDK jar 到本地仓库（不在任何远端 Nexus 上）
COPY libs/flight-hub-sdk-3.3.7.jar /tmp/flight-hub-sdk-3.3.7.jar
RUN mvn install:install-file \
    -Dfile=/tmp/flight-hub-sdk-3.3.7.jar \
    -DgroupId=com.dji.flighthubsdk \
    -DartifactId=flight-hub-sdk \
    -Dversion=3.3.7 \
    -Dpackaging=jar \
    -B

COPY skeleton/ ./skeleton/

RUN cd skeleton && mvn package -Psdk-real -DskipTests -q

# 解压 SDK native lib（builder stage，安装 unzip 后解压）
# SDK jar 内 lib/linux-x86_64/ 含 libflighthubsdk_jni.so + 依赖 .so
RUN apt-get update && \
    apt-get install -y --no-install-recommends unzip && \
    rm -rf /var/lib/apt/lists/*

RUN FAT_JAR=$(ls /build/skeleton/app/fh-skeleton-app/target/fh-skeleton-app-*.jar) && \
    mkdir -p /opt/sdk-libs && \
    unzip -j -o "${FAT_JAR}" 'BOOT-INF/lib/flight-hub-sdk-*.jar' -d /tmp/sdk && \
    unzip -j -o /tmp/sdk/flight-hub-sdk-*.jar 'lib/linux-x86_64/*' -d /opt/sdk-libs && \
    rm -rf /tmp/sdk && \
    chmod 755 /opt/sdk-libs/*.so*

# ============== Stage 2: Runtime ==============
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="DJI DevKit Team"
LABEL description="FH Skeleton — DJI FlightHub PaaS SDK ISV-extensible skeleton"

WORKDIR /app

# 复制 fat jar
COPY --from=builder /build/skeleton/app/fh-skeleton-app/target/fh-skeleton-app-*.jar app.jar

# 复制已解压的 SDK native lib（builder stage 完成解压，运行时镜像无需 unzip）
COPY --from=builder /opt/sdk-libs /opt/sdk-libs

# 安装 curl（healthcheck）+ SDK JNI 原生库完整依赖链
# SDK 原生库运行所需：libatomic1, libgl1, libegl1, libx11-6 及相关 X11 库, libssl1.1
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        libatomic1 \
        libgl1 \
        libegl1 \
        libx11-6 \
        libxcomposite1 \
        libxdamage1 \
        libxext6 \
        libxfixes3 \
        libxi6 \
        libxrandr2 \
        libxrender1 \
        libxtst6 \
    && rm -rf /var/lib/apt/lists/*

# libssl1.1 / libcrypto1.1：jammy 不提供，从 focal 仓库安装
RUN echo "deb https://archive.ubuntu.com/ubuntu focal-updates main" > /etc/apt/sources.list.d/focal.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends libssl1.1 && \
    rm -f /etc/apt/sources.list.d/focal.list && \
    rm -rf /var/lib/apt/lists/*

# 安全加固：以非 root 用户运行服务
RUN groupadd -r appuser && useradd -r -g appuser -d /app -s /sbin/nologin appuser \
    && chown -R appuser:appuser /app /opt/sdk-libs

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/pf-devkit/ai-plugin/skeleton/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Djava.library.path=/opt/sdk-libs", \
    "-jar", "app.jar"]
