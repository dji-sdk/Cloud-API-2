# ============== Stage 1: Build ==============
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 使用公司内部 Maven 仓库
COPY config/settings.xml /root/.m2/settings.xml

# 安装私有 SDK jar 到本地仓库（不在任何远端 Nexus 上）
# SDK 按架构分两个包，版本号与 Maven 坐标相同，只能靠构建参数二选一；故意不设默认值
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

COPY skeleton/ ./skeleton/

RUN cd skeleton && mvn package -Psdk-real -DskipTests -q

# 解压 SDK native lib（builder stage，安装 unzip 后解压）
# SDK jar 内 lib/linux-<arch>/ 含 libflighthubsdk_jni.so + 依赖 .so，目录名随 SDK_ARCH 变化
RUN apt-get update && \
    apt-get install -y --no-install-recommends unzip && \
    rm -rf /var/lib/apt/lists/*

RUN case "${SDK_ARCH}" in \
      amd64) SDK_LIB_DIR=linux-x86_64 ;; \
      arm64) SDK_LIB_DIR=linux-aarch64 ;; \
      *) echo "不支持的 SDK_ARCH: ${SDK_ARCH}（只支持 amd64/arm64）" >&2; exit 1 ;; \
    esac && \
    FAT_JAR=$(ls /build/skeleton/app/fh-skeleton-app/target/fh-skeleton-app-*.jar) && \
    mkdir -p /opt/sdk-libs && \
    unzip -j -o "${FAT_JAR}" 'BOOT-INF/lib/flight-hub-sdk-*.jar' -d /tmp/sdk && \
    unzip -j -o /tmp/sdk/flight-hub-sdk-*.jar "lib/${SDK_LIB_DIR}/*" -d /opt/sdk-libs && \
    rm -rf /tmp/sdk && \
    chmod 755 /opt/sdk-libs/*.so*

# ============== Stage 2: Runtime ==============
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="DJI DevKit Team"
LABEL description="FH Skeleton — DJI FlightHub PaaS SDK Integrator-extensible skeleton"

WORKDIR /app

# 复制 fat jar
COPY --from=builder /build/skeleton/app/fh-skeleton-app/target/fh-skeleton-app-*.jar app.jar

# 复制已解压的 SDK native lib（builder stage 完成解压，运行时镜像无需 unzip）
COPY --from=builder /opt/sdk-libs /opt/sdk-libs

# 运行期不额外安装任何系统包：
# - curl：基础镜像自带（/usr/bin/curl 7.81.0），healthcheck 直接可用；
# - libssl.so.1.1 / libcrypto.so.1.1 等 6 个依赖库由 SDK jar 自带，已随本 COPY 落在
#   /opt/sdk-libs，与 libflighthubsdk_jni.so 同目录，其 RUNPATH=$ORIGIN 可直接解析；
# - JNI 原生库实测不依赖 GL / EGL / X11 / libatomic / pulseaudio：对 amd64、arm64
#   各 7 个 .so 做过 objdump -p（NEEDED）与 strings（dlopen）全量扫描，并在未装任何
#   附加包的空基础镜像中调用 JniLibraryLoader.loadLibrary()，两架构均返回 loaded=true。
# 原先从 focal 仓库补装 libssl1.1 的步骤一并删除：该步骤依赖 Ubuntu ports 仓库的瞬时
# 状态，且正是 arm64 构建 404（libglvnd0 拉取失败）所在的 apt 链路，属于不必要的脆弱点。
RUN groupadd -r appuser && useradd -r -g appuser -d /app -s /sbin/nologin appuser \
    && chown -R appuser:appuser /app /opt/sdk-libs

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/pf-devkit/ai-plugin/skeleton/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-Djava.library.path=/opt/sdk-libs", \
    "-jar", "app.jar"]
