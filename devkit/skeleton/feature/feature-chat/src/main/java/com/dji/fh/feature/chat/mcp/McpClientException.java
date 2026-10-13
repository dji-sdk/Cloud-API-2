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
 * MCP Client 操作异常。
 *
 * <p>覆盖连接失败、超时、协议错误等场景。
 *
 * @author DJI DevKit Team
 */
public class McpClientException extends RuntimeException {

    /**
     * 构造异常。
     *
     * @param message 错误描述
     */
    public McpClientException(String message) {
        super(message);
    }

    /**
     * 构造异常（含原因）。
     *
     * @param message 错误描述
     * @param cause   原始异常
     */
    public McpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
