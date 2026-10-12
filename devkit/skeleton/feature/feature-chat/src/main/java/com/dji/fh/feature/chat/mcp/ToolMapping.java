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

import com.dji.fh.feature.chat.tool.ChatSecurityLevel;

/**
 * Chat Tool 到 MCP Tool 的映射配置。
 *
 * <p>定义哪些 MCP Tool 暴露给 chatbot，以及对应的安全等级。
 *
 * @param mcpToolName MCP Server 中的 tool name
 * @param securityLevel 在 chatbot 中的安全等级
 * @author DJI DevKit Team
 */
public record ToolMapping(String mcpToolName, ChatSecurityLevel securityLevel) {
}
