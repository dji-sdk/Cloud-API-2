#!/bin/sh

echo "flighthub-mcp-server starting..."

# 私有化同机部署：从 env.map 自动获取后端地址（避免手动设置 FH_SERVER_ADDRESS）
if [ -z "$FH_SERVER_ADDRESS" ] && [ -f /deploy/env.map ]; then
    _callback=$(grep '^TAS_CALLBACK_HOST=' /deploy/env.map | cut -d'"' -f2)
    if [ -n "$_callback" ]; then
        _host=$(echo "$_callback" | cut -d: -f1)
        _port=$(echo "$_callback" | cut -d: -f2)
        export FH_SERVER_ADDRESS="http://${_host}"
        [ -n "$_port" ] && export FH_SERVER_PORT="$_port"
        echo "Auto-detected FH_SERVER_ADDRESS=${FH_SERVER_ADDRESS} FH_SERVER_PORT=${_port} from env.map"
    fi
fi

# 私有化同机部署：自动设置平台网关鉴权 header（isv-webhook-auth 插件要求 auth_keys + 实际鉴权头）
# 网关校验 header 格式：auth_keys: user_id  +  user_id: <已注册用户>
# FH_HTTP_AUTH_VALUE 必须为网关 webhook 已认证的用户标识（示例占位：your-auth-value，需按平台实际配置）
if [ -z "$FH_HTTP_AUTH_KEY" ] && [ -f /deploy/env.map ]; then
    _webhook_url=$(grep '^ISV_WEBHOOK_URL=' /deploy/env.map | cut -d'"' -f2)
    if [ -n "$_webhook_url" ]; then
        export FH_HTTP_AUTH_KEY="${FH_HTTP_AUTH_KEY:-user_id}"
        # 默认值为空，必须由 /deploy/env.map 或环境变量显式提供，避免使用内部测试标识
        export FH_HTTP_AUTH_VALUE="${FH_HTTP_AUTH_VALUE:-}"
        echo "Auto-detected gateway auth: FH_HTTP_AUTH_KEY=${FH_HTTP_AUTH_KEY} FH_HTTP_AUTH_VALUE=***"
    fi
fi

# 私有化部署：启动 CodeMeter 授权服务，运行加壳后的 JAR
if [ -f /deploy/config_tool ]; then
    echo "Privatization mode: initializing CodeMeter..."
    /deploy/config_tool
    /usr/sbin/CodeMeterLin && cmu --add-server 172.17.0.1
    exec java ${JAVA_OPTS} -jar /app/flighthub-mcp.jar.ax --spring.profiles.active=sse
else
    exec java ${JAVA_OPTS} -jar /app/flighthub-mcp.jar --spring.profiles.active=sse
fi
