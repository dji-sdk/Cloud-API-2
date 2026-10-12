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

import java.util.List;

/**
 * Chat 请求 DTO。
 *
 * <p>前端通过 {@code POST /api/v1/chat/stream} 发送此对象，携带用户消息和会话上下文。
 *
 * @author DJI DevKit Team
 */
public class ChatRequest {

    /** 用户本轮消息内容，不能为空。 */
    private String message;

    /** 会话 ID（UUID 格式），用于关联同一轮对话。 */
    private String conversationId;

    /** 历史消息列表，前端负责维护并按轮次传入。 */
    private List<HistoryMessage> history;

    /**
     * 返回用户本轮消息内容。
     *
     * @return 消息字符串
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置用户本轮消息内容。
     *
     * @param message 消息字符串
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 返回会话 ID。
     *
     * @return 会话 ID（UUID 格式）
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * 设置会话 ID。
     *
     * @param conversationId 会话 ID（UUID 格式）
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * 返回历史消息列表。
     *
     * @return 历史消息列表，可能为 null
     */
    public List<HistoryMessage> getHistory() {
        return history;
    }

    /**
     * 设置历史消息列表。
     *
     * @param history 历史消息列表
     */
    public void setHistory(List<HistoryMessage> history) {
        this.history = history;
    }

    /**
     * 历史消息条目，代表一轮对话中的单条消息。
     *
     * @author DJI DevKit Team
     */
    public static class HistoryMessage {

        /** 消息角色：{@code user} 或 {@code assistant}。 */
        private String role;

        /** 消息内容。 */
        private String content;

        /**
         * 返回消息角色。
         *
         * @return 角色字符串（user / assistant）
         */
        public String getRole() {
            return role;
        }

        /**
         * 设置消息角色。
         *
         * @param role 角色字符串（user / assistant）
         */
        public void setRole(String role) {
            this.role = role;
        }

        /**
         * 返回消息内容。
         *
         * @return 内容字符串
         */
        public String getContent() {
            return content;
        }

        /**
         * 设置消息内容。
         *
         * @param content 内容字符串
         */
        public void setContent(String content) {
            this.content = content;
        }
    }
}
