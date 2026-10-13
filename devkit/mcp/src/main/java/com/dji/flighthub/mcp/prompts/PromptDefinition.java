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

package com.dji.flighthub.mcp.prompts;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * MCP Prompt 定义 — 描述一个可被 LLM Agent 调用的业务流程模板。
 *
 * <p>对应 MCP 协议 prompts/list 返回的单个 prompt 元数据，
 * 以及 prompts/get 时通过 messageTemplate 生成的引导消息。
 *
 * @author DJI FlightHub SDK Team
 */
public class PromptDefinition {

    /** Prompt 唯一名称（如 "fly-drone"） */
    private final String name;

    /** Prompt 描述（英文，面向 LLM） */
    private final String description;

    /** 参数列表（prompts/list 返回给客户端） */
    private final List<Argument> arguments;

    /** 消息生成器：接收参数 Map，返回 messages 列表 */
    private final Function<Map<String, String>, List<Message>> messageTemplate;

    public PromptDefinition(String name, String description, List<Argument> arguments,
                            Function<Map<String, String>, List<Message>> messageTemplate) {
        this.name = name;
        this.description = description;
        this.arguments = arguments != null ? arguments : List.of();
        this.messageTemplate = messageTemplate;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public Function<Map<String, String>, List<Message>> getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * 生成 messages（调用 messageTemplate）。
     *
     * @param args 客户端传入的参数
     * @return MCP messages 列表
     */
    public List<Message> generateMessages(Map<String, String> args) {
        return messageTemplate.apply(args != null ? args : Map.of());
    }

    // ==================== 内部数据类 ====================

    /**
     * Prompt 参数定义。
     */
    public static class Argument {

        private final String name;
        private final String description;
        private final boolean required;

        public Argument(String name, String description, boolean required) {
            this.name = name;
            this.description = description;
            this.required = required;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isRequired() {
            return required;
        }
    }

    /**
     * MCP Prompt message — role + content。
     */
    public static class Message {

        private final String role;
        private final Content content;

        public Message(String role, Content content) {
            this.role = role;
            this.content = content;
        }

        /**
         * 快捷构建 user 角色的 text 消息。
         */
        public static Message userText(String text) {
            return new Message("user", new Content("text", text));
        }

        public String getRole() {
            return role;
        }

        public Content getContent() {
            return content;
        }
    }

    /**
     * MCP message content — type + text。
     */
    public static class Content {

        private final String type;
        private final String text;

        public Content(String type, String text) {
            this.type = type;
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }
}
