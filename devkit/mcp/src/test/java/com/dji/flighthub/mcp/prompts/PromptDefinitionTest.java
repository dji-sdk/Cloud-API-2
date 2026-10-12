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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PromptDefinition 单元测试。
 * 覆盖数据模型构造、getter、内部类工厂方法。
 *
 * @author DJI FlightHub SDK Team
 */
class PromptDefinitionTest {

    @Test
    void constructor_shouldStoreFieldsCorrectly() {
        List<PromptDefinition.Argument> args = List.of(
                new PromptDefinition.Argument("device_sn", "设备序列号", true));

        PromptDefinition def = new PromptDefinition(
                "test-prompt", "A test prompt", args,
                params -> List.of(PromptDefinition.Message.userText("hello " + params.get("device_sn"))));

        assertEquals("test-prompt", def.getName());
        assertEquals("A test prompt", def.getDescription());
        assertEquals(1, def.getArguments().size());
        assertNotNull(def.getMessageTemplate());
    }

    @Test
    void constructor_shouldDefaultToEmptyArguments_whenNull() {
        PromptDefinition def = new PromptDefinition(
                "no-args", "No args prompt", null,
                params -> List.of(PromptDefinition.Message.userText("no args")));

        assertNotNull(def.getArguments());
        assertTrue(def.getArguments().isEmpty());
    }

    @Test
    void generateMessages_shouldInvokeTemplate() {
        PromptDefinition def = new PromptDefinition(
                "greet", "Greeting prompt", List.of(),
                params -> List.of(PromptDefinition.Message.userText("Hello, World!")));

        List<PromptDefinition.Message> messages = def.generateMessages(Map.of());
        assertEquals(1, messages.size());
        assertEquals("user", messages.get(0).getRole());
        assertEquals("Hello, World!", messages.get(0).getContent().getText());
    }

    @Test
    void generateMessages_shouldPassNullSafeEmptyMap() {
        PromptDefinition def = new PromptDefinition(
                "safe", "Null-safe", List.of(),
                params -> {
                    assertNotNull(params, "params should never be null");
                    return List.of(PromptDefinition.Message.userText("ok"));
                });

        // 传 null 应被转为空 Map
        List<PromptDefinition.Message> messages = def.generateMessages(null);
        assertEquals(1, messages.size());
    }

    // ==================== Argument 内部类 ====================

    @Test
    void argument_shouldExposeAllFields() {
        PromptDefinition.Argument arg = new PromptDefinition.Argument(
                "drone_sn", "Drone serial number", true);

        assertEquals("drone_sn", arg.getName());
        assertEquals("Drone serial number", arg.getDescription());
        assertTrue(arg.isRequired());
    }

    @Test
    void argument_shouldSupportOptional() {
        PromptDefinition.Argument arg = new PromptDefinition.Argument(
                "format", "Output format", false);

        assertFalse(arg.isRequired());
    }

    // ==================== Message 内部类 ====================

    @Test
    void messageUserText_shouldCreateUserRoleWithTextContent() {
        PromptDefinition.Message msg = PromptDefinition.Message.userText("test content");

        assertEquals("user", msg.getRole());
        assertNotNull(msg.getContent());
        assertEquals("text", msg.getContent().getType());
        assertEquals("test content", msg.getContent().getText());
    }

    @Test
    void message_shouldAllowCustomRole() {
        PromptDefinition.Content content = new PromptDefinition.Content("text", "system message");
        PromptDefinition.Message msg = new PromptDefinition.Message("assistant", content);

        assertEquals("assistant", msg.getRole());
        assertEquals("text", msg.getContent().getType());
        assertEquals("system message", msg.getContent().getText());
    }

    // ==================== Content 内部类 ====================

    @Test
    void content_shouldExposeTypeAndText() {
        PromptDefinition.Content content = new PromptDefinition.Content("image", "base64data");

        assertEquals("image", content.getType());
        assertEquals("base64data", content.getText());
    }
}
