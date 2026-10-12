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

package com.dji.flighthub.mcp.elicit;

import java.util.Map;

/**
 * client 反向通道抽象 —— 让在工作线程上执行的 tool 能向 MCP client 发起
 * {@code elicitation/create} 反向请求，阻塞等待真人在 client 弹窗上的选择。
 *
 * <p>由具体 transport（stdio / SSE）实现。tool 层不直接依赖 transport，
 * 而是通过 {@link ClientBridgeHolder} 在 ThreadLocal 中取得当前调用的 bridge。
 *
 * @author DJI FlightHub SDK Team
 */
public interface ClientBridge {

    /**
     * 当前 client 是否在 initialize 阶段声明了 elicitation 能力。
     *
     * @return true 表示可发起 elicitation 反向请求
     */
    boolean supportsElicitation();

    /**
     * 当前 bridge 是否为远程传输通道（如 SSE/HTTP）。
     *
     * <p>远程模式下，confirm 参数不可信（由 AI 自行构造），
     * 必须通过 elicitation 弹窗获取真人确认；
     * 本地模式（stdio）下，confirm 参数视为可信。
     *
     * @return true 表示远程传输（SSE），false 表示本地传输（stdio）
     */
    default boolean isRemote() {
        return false;
    }

    /**
     * 向 client 发起 elicitation 反向请求并阻塞等待真人响应。
     *
     * @param message         展示给真人的提示信息
     * @param requestedSchema 请求真人填写的表单 schema（仅 flat primitives；确认场景可传空 Map）
     * @param timeoutMillis   等待超时（毫秒）
     * @return 真人的选择结果；超时返回 {@link ElicitResult#timeout()}
     */
    ElicitResult elicit(String message, Map<String, Object> requestedSchema, long timeoutMillis);
}
