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

package com.dji.fh.feature.chat.controller;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SSE 事件 DTO，用于流式推送 Chat 状态至前端。
 *
 * <p>每个事件包含一个 {@code type} 字段标识事件类型，其余字段随类型不同而变化。
 * 通过静态工厂方法构造各类事件。
 *
 * <p>事件类型：
 * <ul>
 *   <li>{@code token} — 流式文本片段</li>
 *   <li>{@code done} — 流结束，携带 token 统计</li>
 *   <li>{@code error} — 错误事件</li>
 *   <li>{@code status} — 工具执行中间状态提示（如"正在查询设备列表..."）</li>
 *   <li>{@code tool_call} — 工具调用通知（含参数，供调试用）</li>
 *   <li>{@code tool_result} — 工具执行结果</li>
 *   <li>{@code navigation} — 前端路由跳转指令</li>
 * </ul>
 *
 * @author DJI DevKit Team
 */
public class ChatSseEvent {

    /** 事件类型：流式文本片段。 */
    public static final String TYPE_TOKEN = "token";

    /** 事件类型：流结束。 */
    public static final String TYPE_DONE = "done";

    /** 事件类型：错误。 */
    public static final String TYPE_ERROR = "error";

    /** 事件类型：工具执行中间状态提示。 */
    public static final String TYPE_STATUS = "status";

    /** 事件类型：工具调用通知。 */
    public static final String TYPE_TOOL_CALL = "tool_call";

    /** 事件类型：工具执行结果。 */
    public static final String TYPE_TOOL_RESULT = "tool_result";

    /** 事件类型：前端路由跳转指令。 */
    public static final String TYPE_NAVIGATION = "navigation";

    /** 事件类型：输出内容审核告警。 */
    public static final String TYPE_WARNING = "warning";

    /** 事件数据，保持字段有序以便前端解析。 */
    private final Map<String, Object> data;

    /**
     * 内部构造函数。
     *
     * @param data 事件数据
     */
    private ChatSseEvent(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * 创建工具执行中间状态提示事件。
     *
     * <p>在工具调用开始前推送给前端，用于显示 "正在查询设备列表..." 等等待状态。
     * 文本使用对用户可见的自然语言，不含内部工具名。
     *
     * @param text 用户可见的操作描述
     * @return status 事件
     */
    public static ChatSseEvent status(String text) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_STATUS);
        data.put("text", text);
        return new ChatSseEvent(data);
    }

    /**
     * 创建流式文本片段事件。
     *
     * @param content 文本内容片段
     * @return token 事件
     */
    public static ChatSseEvent token(String content) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_TOKEN);
        data.put("content", content);
        return new ChatSseEvent(data);
    }

    /**
     * 创建流结束事件，携带 token 用量统计。
     *
     * @param inputTokens  输入 token 数
     * @param outputTokens 输出 token 数
     * @return done 事件
     */
    public static ChatSseEvent done(int inputTokens, int outputTokens) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_DONE);
        data.put("inputTokens", inputTokens);
        data.put("outputTokens", outputTokens);
        return new ChatSseEvent(data);
    }

    /**
     * 创建错误事件。
     *
     * @param code    错误码
     * @param message 错误消息
     * @return error 事件
     */
    public static ChatSseEvent error(String code, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_ERROR);
        data.put("code", code);
        data.put("message", message);
        return new ChatSseEvent(data);
    }

    /**
     * 创建工具调用通知事件。
     *
     * @param toolName 工具名
     * @param args     工具输入参数（原始 JSON 字符串或对象）
     * @return tool_call 事件
     */
    public static ChatSseEvent toolCall(String toolName, Object args) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_TOOL_CALL);
        data.put("toolName", toolName);
        data.put("args", args);
        return new ChatSseEvent(data);
    }

    /**
     * 创建工具执行结果事件。
     *
     * @param content 工具执行返回的内容
     * @return tool_result 事件
     */
    public static ChatSseEvent toolResult(String content) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_TOOL_RESULT);
        data.put("content", content);
        return new ChatSseEvent(data);
    }

    /**
     * 创建前端路由跳转指令事件。
     *
     * @param path  前端路由路径，如 {@code /cockpit}
     * @param query 路由查询参数，如 {@code {drone_sn: "xxx"}}
     * @return navigation 事件
     */
    public static ChatSseEvent navigation(String path, Map<String, String> query) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_NAVIGATION);
        data.put("path", path);
        data.put("query", query);
        return new ChatSseEvent(data);
    }

    /**
     * 构造输出内容审核告警事件。
     *
     * <p>当 LLM 输出内容命中审核规则时发送，前端可据此遮罩或提示用户。
     *
     * @param reason 告警原因描述
     * @return warning 事件
     */
    public static ChatSseEvent warning(String reason) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", TYPE_WARNING);
        data.put("reason", reason);
        return new ChatSseEvent(data);
    }

    /**
     * 返回事件数据 Map。
     *
     * @return 事件数据
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * 返回事件类型字符串。
     *
     * @return 类型字符串
     */
    public String getType() {
        Object type = data.get("type");
        return type != null ? type.toString() : "";
    }
}
