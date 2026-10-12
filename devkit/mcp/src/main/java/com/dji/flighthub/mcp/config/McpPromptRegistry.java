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

package com.dji.flighthub.mcp.config;

import com.dji.flighthub.mcp.prompts.PromptDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Prompt 注册中心 — 持有所有 prompt 定义，供传输层响应 prompts/list 和 prompts/get。
 *
 * <p>MCP 协议规定：
 * <ul>
 *   <li>prompts/list — 返回所有 prompt 的名称、描述、参数列表</li>
 *   <li>prompts/get — 接收 prompt 名称和参数，返回渲染后的 messages</li>
 * </ul>
 *
 * <p>设计对齐 McpToolRegistry：非 Spring Component，由 McpServerConfig @Bean 创建并注入传输层。
 *
 * @author DJI FlightHub SDK Team
 */
public class McpPromptRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpPromptRegistry.class);

    private final List<PromptDefinition> prompts = new ArrayList<>();

    /**
     * 注册 prompt 定义。
     *
     * @param definition prompt 定义
     */
    public void register(PromptDefinition definition) {
        prompts.add(definition);
    }

    /**
     * 批量注册 prompt 定义。
     *
     * @param definitions prompt 定义列表
     */
    public void registerAll(List<PromptDefinition> definitions) {
        prompts.addAll(definitions);
    }

    /**
     * 获取所有 prompt 的元数据列表（用于 prompts/list 响应）。
     *
     * @return prompt 元数据列表（name, description, arguments）
     */
    public List<Map<String, Object>> getPromptList() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PromptDefinition prompt : prompts) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", prompt.getName());
            entry.put("description", prompt.getDescription());

            List<Map<String, Object>> args = new ArrayList<>();
            for (PromptDefinition.Argument arg : prompt.getArguments()) {
                Map<String, Object> argMap = new LinkedHashMap<>();
                argMap.put("name", arg.getName());
                argMap.put("description", arg.getDescription());
                argMap.put("required", arg.isRequired());
                args.add(argMap);
            }
            entry.put("arguments", args);
            result.add(entry);
        }
        return result;
    }

    /**
     * 根据名称和参数获取 prompt 的渲染结果（用于 prompts/get 响应）。
     *
     * @param name prompt 名称
     * @param args 客户端传入的参数（key-value）
     * @return 包含 description + messages 的响应 Map；未找到时返回 null
     * @throws IllegalArgumentException 缺少必填参数
     */
    public Map<String, Object> getPrompt(String name, Map<String, String> args) {
        PromptDefinition definition = findByName(name);
        if (definition == null) {
            return null;
        }

        // 校验必填参数
        for (PromptDefinition.Argument arg : definition.getArguments()) {
            if (arg.isRequired()) {
                String value = (args != null) ? args.get(arg.getName()) : null;
                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException(
                            "Missing required argument: " + arg.getName());
                }
            }
        }

        // 渲染 messages
        List<PromptDefinition.Message> messages = definition.generateMessages(args);

        // 构建响应
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("description", definition.getDescription());

        List<Map<String, Object>> messageList = new ArrayList<>();
        for (PromptDefinition.Message msg : messages) {
            Map<String, Object> msgMap = new LinkedHashMap<>();
            msgMap.put("role", msg.getRole());
            Map<String, Object> contentMap = new LinkedHashMap<>();
            contentMap.put("type", msg.getContent().getType());
            contentMap.put("text", msg.getContent().getText());
            msgMap.put("content", contentMap);
            messageList.add(msgMap);
        }
        result.put("messages", messageList);

        return result;
    }

    /**
     * 获取已注册 prompt 数量。
     *
     * @return prompt 数量
     */
    public int getPromptCount() {
        return prompts.size();
    }

    // ==================== 传输层共享辅助方法 ====================

    /**
     * 处理 prompts/get 请求参数解析和校验 — 供传输层复用，消除 DRY 违反。
     *
     * <p>完成以下工作：
     * <ol>
     *   <li>从 params 中提取 "name"（含类型安全检查）</li>
     *   <li>解析 "arguments" 为 String Map</li>
     *   <li>调用 {@link #getPrompt(String, Map)} 获取渲染结果</li>
     * </ol>
     *
     * @param params 来自 JSON-RPC 请求的 params Map
     * @return 包含 description + messages 的结果 Map
     * @throws PromptRequestException 参数缺失、类型错误或 prompt 不存在
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleGetRequest(Map<String, Object> params) {
        // 类型安全：防止 "name": 42 等非字符串值导致 ClassCastException
        Object rawName = params.get("name");
        if (!(rawName instanceof String name) || name.isBlank()) {
            throw new PromptRequestException("Missing required parameter: name");
        }

        // 解析 arguments
        Map<String, String> args = null;
        Object rawArgs = params.get("arguments");
        if (rawArgs instanceof Map) {
            args = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) rawArgs).entrySet()) {
                args.put(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : null);
            }
        }

        // 调用核心逻辑
        try {
            Map<String, Object> result = getPrompt(name, args);
            if (result == null) {
                throw new PromptRequestException("Unknown prompt: " + name);
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw new PromptRequestException(e.getMessage());
        }
    }

    /**
     * prompts/get 请求处理异常 — 表示参数校验失败或 prompt 不存在。
     * 传输层捕获后应返回 JSON-RPC -32602（Invalid Params）错误。
     */
    public static class PromptRequestException extends RuntimeException {

        /**
         * 构造异常。
         *
         * @param message 错误信息
         */
        public PromptRequestException(String message) {
            super(message);
        }
    }

    /**
     * 根据名称查找 prompt 定义。
     *
     * @param name prompt 名称
     * @return 定义对象，不存在返回 null
     */
    private PromptDefinition findByName(String name) {
        for (PromptDefinition prompt : prompts) {
            if (prompt.getName().equals(name)) {
                return prompt;
            }
        }
        return null;
    }
}
