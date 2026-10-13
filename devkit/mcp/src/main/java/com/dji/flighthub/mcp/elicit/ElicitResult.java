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
 * MCP elicitation 反向请求的结果。
 * 对应 MCP 协议 {@code elicitation/create} 的响应：真人在 client 弹窗上的选择。
 *
 * <p>四种终态（{@link Action}）中，只有 {@link Action#ACCEPT} 表示真人主动同意，
 * 其余（DECLINE / CANCEL / TIMEOUT）一律视为"未授权"。安全红线：CANCEL/TIMEOUT
 * 绝不能被上层误当作同意。
 *
 * @author DJI FlightHub SDK Team
 */
public record ElicitResult(Action action, Map<String, Object> content) {

    /**
     * elicitation 终态动作。
     */
    public enum Action {
        /** 真人接受（唯一放行态） */
        ACCEPT,
        /** 真人明确拒绝 */
        DECLINE,
        /** 真人取消 / client 主动取消 */
        CANCEL,
        /** 等待超时（真人未在时限内响应） */
        TIMEOUT
    }

    /**
     * 构造 ACCEPT 结果。
     *
     * @param content 真人填写的表单内容（可能为空 Map）
     * @return ACCEPT 结果
     */
    public static ElicitResult accept(Map<String, Object> content) {
        return new ElicitResult(Action.ACCEPT, content == null ? Map.of() : content);
    }

    /**
     * 构造 DECLINE 结果。
     *
     * @return DECLINE 结果
     */
    public static ElicitResult decline() {
        return new ElicitResult(Action.DECLINE, Map.of());
    }

    /**
     * 构造 CANCEL 结果。
     *
     * @return CANCEL 结果
     */
    public static ElicitResult cancel() {
        return new ElicitResult(Action.CANCEL, Map.of());
    }

    /**
     * 构造 TIMEOUT 结果。
     *
     * @return TIMEOUT 结果
     */
    public static ElicitResult timeout() {
        return new ElicitResult(Action.TIMEOUT, Map.of());
    }

    /**
     * 是否为放行态（真人接受）。
     *
     * @return true 表示真人接受
     */
    public boolean isAccepted() {
        return action == Action.ACCEPT;
    }

    /**
     * 从 JSON-RPC 响应的 {@code result} 体解析出 elicitation 结果。
     *
     * <p>MCP elicitation 响应形如 {@code {"action":"accept","content":{...}}}。
     * 安全默认：无法识别的 action 一律视为 {@link Action#CANCEL}，绝不放行。
     *
     * @param result JSON-RPC 响应中的 result 对象（可能为 null）
     * @return 解析后的 elicitation 结果
     */
    @SuppressWarnings("unchecked")
    public static ElicitResult fromResponse(Map<String, Object> result) {
        if (result == null) {
            return cancel();
        }
        Object actionValue = result.get("action");
        String action = actionValue instanceof String s ? s.trim().toLowerCase() : "";
        return switch (action) {
            case "accept" -> {
                Object contentValue = result.get("content");
                Map<String, Object> content = contentValue instanceof Map<?, ?> m
                        ? (Map<String, Object>) m : Map.of();
                yield accept(content);
            }
            case "decline" -> decline();
            default -> cancel();
        };
    }
}
