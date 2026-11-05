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

package com.dji.flighthub.mcp.transport;

import java.util.Map;

/**
 * {@code tools/call} 的参数形状解析器。
 *
 * <p>职责仅限校验 {@code name} 与 {@code arguments} 的形状：两个 transport
 * （stdio / SSE）共用同一套判定，避免各自的原始强转把 {@link ClassCastException}
 * 误报成解析错误。业务语义（工具是否存在、参数是否齐全）不在这里处理。</p>
 *
 * @author DJI DevKit Team
 */
public final class ToolCallParams {

    /** 工具名字段 */
    private static final String FIELD_NAME = "name";

    /** 工具参数对象字段 */
    private static final String FIELD_ARGUMENTS = "arguments";

    private final String toolName;

    private final Map<String, Object> arguments;

    private ToolCallParams(String toolName, Map<String, Object> arguments) {
        this.toolName = toolName;
        this.arguments = arguments;
    }

    /**
     * 解析 {@code tools/call} 的 params。
     *
     * @param params JSON-RPC params（可为 null）
     * @return 解析结果
     * @throws InvalidParamsException name 缺失/非字符串/空白，或 arguments 不是对象
     */
    public static ToolCallParams parse(Map<String, Object> params) {
        if (params == null) {
            throw new InvalidParamsException("Missing required parameter: " + FIELD_NAME);
        }
        Object rawName = params.get(FIELD_NAME);
        if (!(rawName instanceof String name) || name.isBlank()) {
            throw new InvalidParamsException("Missing required parameter: " + FIELD_NAME);
        }
        return new ToolCallParams(name, parseArguments(params.get(FIELD_ARGUMENTS)));
    }

    /**
     * 解析 arguments：缺省或 null 视为空对象，其它非对象类型视为非法。
     *
     * @param rawArguments 原始值
     * @return 参数对象
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseArguments(Object rawArguments) {
        if (rawArguments == null) {
            return Map.of();
        }
        if (rawArguments instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new InvalidParamsException("Invalid '" + FIELD_ARGUMENTS + "': expected object but was "
                + rawArguments.getClass().getSimpleName());
    }

    /**
     * 取工具名。
     *
     * @return 工具名
     */
    public String toolName() {
        return toolName;
    }

    /**
     * 取参数对象。
     *
     * @return 参数对象
     */
    public Map<String, Object> arguments() {
        return arguments;
    }
}
