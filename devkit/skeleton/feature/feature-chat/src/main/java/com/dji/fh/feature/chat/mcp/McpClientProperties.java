/*
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dji.fh.feature.chat.mcp;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MCP Server 连接配置，前缀 {@code fh.chat.mcp}。
 *
 * <p>控制 Chat Tool 透传到 MCP Server 的连接参数。
 *
 * @author DJI DevKit Team
 */
@ConfigurationProperties(prefix = "fh.chat.mcp")
public class McpClientProperties {

    /** 是否启用 MCP 透传模式，默认开启 */
    private boolean enabled = true;

    /** MCP Server 的 SSE 基础 URL，例如 http://localhost:8080 */
    private String url = "http://localhost:8080";

    /**
     * MCP Server 的 Bearer Token（SSE 鉴权）。
     *
     * <p>对应 MCP Server 的 {@code FH_SSE_AUTH_TOKEN}。留空时不发送 Authorization 头，
     * 仅适用于 MCP Server 未启用 SSE 鉴权的内网调试场景。生产环境必须与 MCP Server 配置一致。
     */
    private String authToken = "";

    /** Tool 调用超时时间，默认 30 秒 */
    private Duration timeout = Duration.ofSeconds(30);

    /** SSE 连接超时时间，默认 10 秒 */
    private Duration connectTimeout = Duration.ofSeconds(10);

    /** 是否启用 SSE 断线自动重连，默认开启 */
    private boolean reconnectEnabled = true;

    /** 重连最大尝试次数，默认无限重试 */
    private int reconnectMaxAttempts = Integer.MAX_VALUE;

    /** 重连初始延迟，默认 2 秒 */
    private Duration reconnectInitialDelay = Duration.ofSeconds(2);

    /** 重连最大延迟（指数退避上限），默认 30 秒 */
    private Duration reconnectMaxDelay = Duration.ofSeconds(30);

    /** 是否启用心跳保活，默认开启 */
    private boolean keepaliveEnabled = true;

    /** 客户端主动 ping 间隔，默认 60 秒（建议小于服务端 heartbeat-timeout 300s 的 1/3） */
    private Duration keepaliveInterval = Duration.ofSeconds(60);

    /** 连续未收到服务端 heartbeat 的容忍时长，默认 90 秒（3 个服务端心跳周期） */
    private Duration heartbeatTimeout = Duration.ofSeconds(90);

    /**
     * 返回是否启用 MCP 透传模式。
     *
     * @return 启用标志
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 MCP 透传模式。
     *
     * @param enabled 启用标志
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 返回 MCP Server 基础 URL。
     *
     * @return URL 字符串
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置 MCP Server 基础 URL。
     *
     * @param url URL 字符串
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 返回 MCP Server SSE 鉴权 Bearer Token。
     *
     * @return token 字符串，留空表示不鉴权
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * 设置 MCP Server SSE 鉴权 Bearer Token。
     *
     * @param authToken token 字符串，留空表示不鉴权
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    /**
     * 返回 Tool 调用超时时间。
     *
     * @return 超时时长
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * 设置 Tool 调用超时时间。
     *
     * @param timeout 超时时长
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * 返回 SSE 连接超时时间。
     *
     * @return 连接超时时长
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置 SSE 连接超时时间。
     *
     * @param connectTimeout 连接超时时长
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * 返回是否启用 SSE 断线自动重连。
     *
     * @return 重连启用标志
     */
    public boolean isReconnectEnabled() {
        return reconnectEnabled;
    }

    /**
     * 设置是否启用 SSE 断线自动重连。
     *
     * @param reconnectEnabled 重连启用标志
     */
    public void setReconnectEnabled(boolean reconnectEnabled) {
        this.reconnectEnabled = reconnectEnabled;
    }

    /**
     * 返回重连最大尝试次数。
     *
     * @return 最大尝试次数
     */
    public int getReconnectMaxAttempts() {
        return reconnectMaxAttempts;
    }

    /**
     * 设置重连最大尝试次数。
     *
     * @param reconnectMaxAttempts 最大尝试次数
     */
    public void setReconnectMaxAttempts(int reconnectMaxAttempts) {
        this.reconnectMaxAttempts = reconnectMaxAttempts;
    }

    /**
     * 返回重连初始延迟。
     *
     * @return 初始延迟时长
     */
    public Duration getReconnectInitialDelay() {
        return reconnectInitialDelay;
    }

    /**
     * 设置重连初始延迟。
     *
     * @param reconnectInitialDelay 初始延迟时长
     */
    public void setReconnectInitialDelay(Duration reconnectInitialDelay) {
        this.reconnectInitialDelay = reconnectInitialDelay;
    }

    /**
     * 返回重连最大延迟。
     *
     * @return 最大延迟时长
     */
    public Duration getReconnectMaxDelay() {
        return reconnectMaxDelay;
    }

    /**
     * 设置重连最大延迟。
     *
     * @param reconnectMaxDelay 最大延迟时长
     */
    public void setReconnectMaxDelay(Duration reconnectMaxDelay) {
        this.reconnectMaxDelay = reconnectMaxDelay;
    }

    /**
     * 返回是否启用心跳保活。
     *
     * @return 保活启用标志
     */
    public boolean isKeepaliveEnabled() {
        return keepaliveEnabled;
    }

    /**
     * 设置是否启用心跳保活。
     *
     * @param keepaliveEnabled 保活启用标志
     */
    public void setKeepaliveEnabled(boolean keepaliveEnabled) {
        this.keepaliveEnabled = keepaliveEnabled;
    }

    /**
     * 返回客户端主动 ping 间隔。
     *
     * @return ping 间隔时长
     */
    public Duration getKeepaliveInterval() {
        return keepaliveInterval;
    }

    /**
     * 设置客户端主动 ping 间隔。
     *
     * @param keepaliveInterval ping 间隔时长
     */
    public void setKeepaliveInterval(Duration keepaliveInterval) {
        this.keepaliveInterval = keepaliveInterval;
    }

    /**
     * 返回心跳超时容忍时长。
     *
     * @return 超时时长
     */
    public Duration getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    /**
     * 设置心跳超时容忍时长。
     *
     * @param heartbeatTimeout 超时时长
     */
    public void setHeartbeatTimeout(Duration heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }
}
