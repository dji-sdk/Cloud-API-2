#!/bin/sh

echo "fh-skeleton-app starting..."

# 私有化部署：启动 CodeMeter 授权服务，运行加壳后的 JAR
if [ -f /deploy/config_tool ]; then
    echo "Privatization mode: initializing CodeMeter..."
    /deploy/config_tool
    CODEMETER_HOST="${CODEMETER_HOST:-172.17.0.1}"
    /usr/sbin/CodeMeterLin && cmu --add-server "$CODEMETER_HOST"
    exec java ${JAVA_OPTS} -Djava.library.path=/opt/sdk-libs -jar /app/app.jar.ax
else
    exec java ${JAVA_OPTS} -Djava.library.path=/opt/sdk-libs -jar /app/app.jar
fi
