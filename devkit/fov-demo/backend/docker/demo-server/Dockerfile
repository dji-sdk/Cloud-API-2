# Demo 后端：linux/amd64（含 DRTC so），不依赖本机 FHSDK 开发仓
FROM --platform=linux/amd64 public.ecr.aws/docker/library/eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl xz-utils ca-certificates \
    && curl -fsSL -o /tmp/ffmpeg.tar.xz \
         https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz \
    && mkdir -p /opt/ffmpeg \
    && tar -xJf /tmp/ffmpeg.tar.xz -C /opt/ffmpeg --strip-components=1 \
    && ln -sf /opt/ffmpeg/bin/ffmpeg /usr/local/bin/ffmpeg \
    && ln -sf /opt/ffmpeg/bin/ffprobe /usr/local/bin/ffprobe \
    && rm -rf /tmp/ffmpeg.tar.xz /var/lib/apt/lists/* \
    && useradd -r -u 10001 -s /usr/sbin/nologin appuser

WORKDIR /app
COPY app.jar /app/app.jar
COPY config/ /app/config/
RUN chown -R appuser:appuser /app

ENV TZ=Asia/Shanghai \
    FHSDK_DRTC_CONFIG=/app/config/drtc_config_node.json

USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-Xms256m","-Xmx1024m","-jar","/app/app.jar","--server.port=8080","--server.address=0.0.0.0"]
