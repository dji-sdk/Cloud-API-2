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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.dji.fh.feature.chat.tool.ChatSecurityLevel;
import com.dji.fh.feature.chat.tool.SecurityGatedCallback;
import org.springframework.ai.tool.ToolCallback;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChatToolRegistry 单元测试。
 *
 * <p>覆盖场景：
 * <ul>
 *   <li>非空 tool 列表时 all() 返回所有 tool</li>
 *   <li>空列表时 all() 返回空 list</li>
 *   <li>all() 返回的 list 顺序与构造时一致</li>
 * </ul>
 * </p>
 *
 * @author DJI DevKit Team
 */
@ExtendWith(MockitoExtension.class)
class ChatToolRegistryTest {

    @Mock
    private ToolCallback toolCallbackA;

    @Mock
    private ToolCallback toolCallbackB;

    @Test
    void all_shouldReturnAllTools_whenMultipleToolsProvided() {
        // given
        List<ToolCallback> tools = List.of(toolCallbackA, toolCallbackB);
        ChatToolRegistry registry = new ChatToolRegistry(tools);

        // when
        List<ToolCallback> result = registry.all();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(toolCallbackA, toolCallbackB);
    }

    @Test
    void all_shouldReturnEmptyList_whenNoToolsProvided() {
        // given
        ChatToolRegistry registry = new ChatToolRegistry(List.of());

        // when
        List<ToolCallback> result = registry.all();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void all_shouldReturnSingleTool_whenOneToolProvided() {
        // given
        ChatToolRegistry registry = new ChatToolRegistry(List.of(toolCallbackA));

        // when
        List<ToolCallback> result = registry.all();

        // then
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(toolCallbackA);
    }

    @Test
    void all_shouldReturnImmutableList_whenModificationAttempted() {
        // given
        ChatToolRegistry registry = new ChatToolRegistry(List.of(toolCallbackA));

        // when / then
        assertThat(registry.all()).isUnmodifiable();
    }

    // -------------------------------------------------------------------------
    // readOnly() — 按安全等级过滤
    // -------------------------------------------------------------------------

    @Test
    void readOnly_shouldReturnOnlyReadOnlyTools() {
        // given: 构造两个 SecurityGatedCallback，分别为 READ_ONLY 和 DEVICE_CONTROL
        SecurityGatedCallback readOnlyTool = new SecurityGatedCallback(toolCallbackA, ChatSecurityLevel.READ_ONLY);
        SecurityGatedCallback deviceTool = new SecurityGatedCallback(toolCallbackB, ChatSecurityLevel.DEVICE_CONTROL);
        ChatToolRegistry registry = new ChatToolRegistry(List.of(readOnlyTool, deviceTool));

        // when
        List<ToolCallback> result = registry.readOnly();

        // then: 仅返回 READ_ONLY
        assertThat(result).hasSize(1);
        assertThat(result).contains(readOnlyTool);
        assertThat(result).doesNotContain(deviceTool);
    }

    @Test
    void readOnly_shouldReturnEmpty_whenNoReadOnlyTools() {
        SecurityGatedCallback deviceTool = new SecurityGatedCallback(toolCallbackA, ChatSecurityLevel.DEVICE_CONTROL);
        ChatToolRegistry registry = new ChatToolRegistry(List.of(deviceTool));

        assertThat(registry.readOnly()).isEmpty();
    }

    @Test
    void readOnly_shouldIgnoreNonSecurityGatedCallbacks() {
        // 非 SecurityGatedCallback 实例（如原生 ToolCallback）应被过滤掉
        ChatToolRegistry registry = new ChatToolRegistry(List.of(toolCallbackA));
        assertThat(registry.readOnly()).isEmpty();
    }

}
