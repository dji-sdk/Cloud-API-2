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

package com.dji.fh.feature.chat.tool.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;

/**
 * Chat Tool 基类 — 统一 JSON 解析、错误响应构建、类型转换等公共逻辑。
 *
 * <p>所有 Chat Tool 应继承本类，复用以下能力：
 * <ul>
 *   <li>{@link #buildErrorResponse(String, String)} — 统一错误响应格式</li>
 *   <li>{@link #toDouble(Object)} / {@link #toInt(Object)} — 安全类型转换</li>
 *   <li>{@link #extractOptionalString(JsonNode, String)} — 可选字段提取</li>
 *   <li>{@link #objectMapper} — 共享 ObjectMapper 实例</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public abstract class AbstractChatTool implements ToolCallback {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractChatTool.class);

    /** 共享 ObjectMapper 实例，所有子类复用，避免重复创建和 classpath 扫描。 */
    protected static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    /** 默认数值转换失败时的兜底值。 */
    private static final double DEFAULT_DOUBLE = 0.0;
    private static final int DEFAULT_INT = 0;

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * 构建统一格式的错误响应 JSON。
     *
     * @param code    错误码（如 missing_parameter、not_found）
     * @param message 错误消息
     * @return JSON 格式错误响应字符串
     */
    protected String buildErrorResponse(String code, String message) {
        try {
            ObjectNode error = OBJECT_MAPPER.createObjectNode();
            error.put("status", "error");
            error.put("code", code);
            error.put("message", message);
            return OBJECT_MAPPER.writeValueAsString(error);
        } catch (JsonProcessingException e) {
            // 兜底：直接返回简单字符串避免 NPE
            return "{\"status\":\"error\",\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
        }
    }

    /**
     * 安全地将 OSD 属性值转为 double。
     *
     * <p>如果值为非数字字符串（如 "N/A"、"offline"），返回 0.0 而非抛出异常。
     *
     * @param value OSD 属性值
     * @return 数值，转换失败返回 0.0
     */
    protected double toDouble(Object value) {
        if (value instanceof Number num) {
            return num.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            LOG.debug("OSD 属性值无法转为 double: {}", value);
            return DEFAULT_DOUBLE;
        }
    }

    /**
     * 安全地将 OSD 属性值转为 int。
     *
     * <p>如果值为非数字字符串，返回 0 而非抛出异常。
     *
     * @param value OSD 属性值
     * @return 数值，转换失败返回 0
     */
    protected int toInt(Object value) {
        if (value instanceof Number num) {
            return num.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            LOG.debug("OSD 属性值无法转为 int: {}", value);
            return DEFAULT_INT;
        }
    }

    /**
     * 从 JSON 节点中提取可选字符串字段。
     *
     * @param node  JSON 节点
     * @param field 字段名
     * @return 字段值；缺失、null 或空白时返回 null
     */
    protected String extractOptionalString(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        String value = fieldNode.asText("");
        return value.isBlank() ? null : value;
    }
}
