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

package com.dji.fh.feature.chat.tool;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;

/**
 * Chat Tool 注册表 — 聚合所有 {@link ToolCallback} 并向 Spring AI 暴露。
 *
 * <p>注册表采用构造函数注入的方式接收 tool 列表，Spring 的
 * {@code List<ToolCallback>} 自动注入机制会收集所有 bean（包括被
 * {@link SecurityGatedCallback} 包装后的实例）。</p>
 *
 * <p>调用方通过 {@link #all()} 获取完整列表，或通过 {@link #readOnly()} 获取
 * 仅限 READ_ONLY 安全等级的子集（用于轻量模型客户端）。</p>
 *
 * @author DJI DevKit Team
 */
public class ChatToolRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ChatToolRegistry.class);

    /** 所有已注册的 Tool 回调，不可变。 */
    private final List<ToolCallback> tools;

    /**
     * 构造函数。
     *
     * @param tools 所有 ToolCallback 实例（由 Spring 容器注入），不能为 null
     */
    public ChatToolRegistry(List<ToolCallback> tools) {
        this.tools = List.copyOf(tools);
    }

    /**
     * 返回全部已注册 Tool 的不可变列表。
     *
     * @return 不可变的 {@link ToolCallback} 列表
     */
    public List<ToolCallback> all() {
        return tools;
    }

    /**
     * 返回安全等级为 {@link ChatSecurityLevel#READ_ONLY} 的 Tool 列表。
     *
     * <p>用于构建轻量模型客户端（haiku）——只读工具不涉及设备控制，
     * 适合让速度更快的小模型处理，避免误触发设备控制指令。</p>
     *
     * @return 只包含 READ_ONLY 工具的列表
     */
    public List<ToolCallback> readOnly() {
        tools.stream()
                .filter(tool -> !(tool instanceof SecurityGatedCallback))
                .forEach(tool -> {
                    var def = tool.getToolDefinition();
                    LOG.warn("ChatToolRegistry：非 SecurityGatedCallback 工具被排除在只读列表之外: {}",
                            def != null ? def.name() : tool.getClass().getSimpleName());
                });
        return tools.stream()
                .filter(SecurityGatedCallback.class::isInstance)
                .map(SecurityGatedCallback.class::cast)
                .filter(cb -> cb.getSecurityLevel() == ChatSecurityLevel.READ_ONLY)
                .collect(Collectors.toList());
    }
}
