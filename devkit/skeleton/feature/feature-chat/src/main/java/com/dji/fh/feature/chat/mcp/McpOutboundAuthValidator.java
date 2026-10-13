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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 出站 MCP 鉴权 token 缺失提醒 —— 安全审计整改（F-04，P2）。
 *
 * <p>MCP Server 侧的 {@code StartupAuthValidator} 对 SSE 鉴权 token 采用 fail-fast 策略，
 * 但 feature-chat 作为出站调用方，其 {@code fh.chat.mcp.auth-token} 留空在本地/Mock 调试场景下
 * 是合法用法（对应 MCP Server 未启用 {@code FH_SSE_AUTH_TOKEN} 的情形），因此不能同等 fail-fast，
 * 否则会破坏合法的本地调试场景。
 *
 * <p>本校验器仅在 {@code fh.chat.mcp.url} 已指向非本机地址（很可能是生产/测试环境）且
 * {@code fh.chat.mcp.auth-token} 仍为空时输出 WARN 级提醒，帮助 ISV 在连接真实 MCP Server
 * 前发现"忘记配置鉴权 token"的配置疏漏，弥补与服务端侧防呆校验之间的防御深度不对称。
 *
 * @author DJI DevKit Team
 */
public class McpOutboundAuthValidator implements SmartInitializingSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(McpOutboundAuthValidator.class);

    /** 视为本机调试场景的 host 白名单。 */
    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1");

    private final McpClientProperties properties;

    /**
     * 构造函数。
     *
     * @param properties MCP 连接配置
     */
    public McpOutboundAuthValidator(McpClientProperties properties) {
        this.properties = properties;
    }

    /**
     * 在所有单例 Bean 初始化完成后检查出站 MCP 鉴权 token 是否缺失，仅记录 WARN 日志。
     */
    @Override
    public void afterSingletonsInstantiated() {
        if (!properties.isEnabled()) {
            return;
        }
        String authToken = properties.getAuthToken();
        if (authToken != null && !authToken.isBlank()) {
            return;
        }
        if (isLoopback(properties.getUrl())) {
            return;
        }
        LOG.warn("【配置提醒】event=missing_outbound_mcp_auth_token fh.chat.mcp.url='{}' "
                + "指向非本机地址，但 fh.chat.mcp.auth-token 为空。若对端 MCP Server 已启用 "
                + "SSE 鉴权（FH_SSE_AUTH_TOKEN），请检查是否忘记配置 auth-token，否则连接会被拒绝；"
                + "本机/Mock 调试场景可忽略此提醒。",
                properties.getUrl());
    }

    /**
     * 判断给定 URL 的 host 是否属于本机调试地址。
     *
     * @param url MCP Server 基础 URL
     * @return 空值、解析失败或命中本机白名单时返回 true
     */
    private boolean isLoopback(String url) {
        if (url == null || url.isBlank()) {
            return true;
        }
        try {
            String host = new URI(url).getHost();
            return host == null || LOOPBACK_HOSTS.contains(host);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
