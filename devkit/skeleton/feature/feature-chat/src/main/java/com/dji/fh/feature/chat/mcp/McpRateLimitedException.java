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

/**
 * MCP Server 限流异常（HTTP 429）。
 *
 * <p>独立成类的原因是：限流是<b>服务端主动要求降速</b>，与"连接已断开"语义不同。
 * 收到该异常时不得立即重连，否则会把限流窗口内剩余配额继续打满，
 * 形成 {@code 429 → 立即重试 → 更多 429} 的死循环。
 *
 * <p>调用方应至少等待 {@link #getRetryAfterMillis()} 指定的时长后再发起请求。
 *
 * @author DJI DevKit Team
 */
public class McpRateLimitedException extends McpClientException {

    /** 服务端要求的最短等待时长（毫秒） */
    private final long retryAfterMillis;

    /**
     * 构造异常。
     *
     * @param message          错误描述
     * @param retryAfterMillis 服务端要求的最短等待时长（毫秒）
     */
    public McpRateLimitedException(String message, long retryAfterMillis) {
        super(message);
        this.retryAfterMillis = retryAfterMillis;
    }

    /**
     * 返回服务端要求的最短等待时长。
     *
     * @return 等待时长（毫秒）
     */
    public long getRetryAfterMillis() {
        return retryAfterMillis;
    }
}
