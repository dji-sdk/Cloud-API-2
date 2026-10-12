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

import com.dji.flighthub.mcp.prompts.FlightHubPrompts;
import com.dji.flighthub.mcp.prompts.PromptDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * McpPromptRegistry 单元测试。
 * 覆盖 prompts/list、prompts/get 正常流、缺少参数、未知 prompt。
 */
class McpPromptRegistryTest {

    private McpPromptRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new McpPromptRegistry();
        FlightHubPrompts prompts = new FlightHubPrompts();
        registry.registerAll(prompts.getDefinitions());
    }

    // ==================== prompts/list ====================

    @Test
    void getPromptList_shouldReturn5Prompts() {
        List<Map<String, Object>> list = registry.getPromptList();
        assertEquals(5, list.size());
    }

    @Test
    void getPromptList_shouldContainQuickstart() {
        List<Map<String, Object>> list = registry.getPromptList();
        boolean found = list.stream().anyMatch(p -> "quickstart".equals(p.get("name")));
        assertTrue(found, "Should contain 'quickstart' prompt");
    }

    @Test
    void getPromptList_shouldContainFlyDrone() {
        List<Map<String, Object>> list = registry.getPromptList();
        boolean found = list.stream().anyMatch(p -> "fly-drone".equals(p.get("name")));
        assertTrue(found, "Should contain 'fly-drone' prompt");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPromptList_shouldIncludeArgumentsForFlyDrone() {
        List<Map<String, Object>> list = registry.getPromptList();
        Map<String, Object> flyDrone = list.stream()
                .filter(p -> "fly-drone".equals(p.get("name")))
                .findFirst().orElseThrow();

        List<Map<String, Object>> args = (List<Map<String, Object>>) flyDrone.get("arguments");
        assertNotNull(args);
        assertEquals(1, args.size());
        assertEquals("drone_sn", args.get(0).get("name"));
        assertEquals(true, args.get(0).get("required"));
    }

    @Test
    void getPromptList_quickstartShouldHaveNoArguments() {
        List<Map<String, Object>> list = registry.getPromptList();
        Map<String, Object> quickstart = list.stream()
                .filter(p -> "quickstart".equals(p.get("name")))
                .findFirst().orElseThrow();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> args = (List<Map<String, Object>>) quickstart.get("arguments");
        assertNotNull(args);
        assertTrue(args.isEmpty());
    }

    // ==================== prompts/get 正常流 ====================

    @Test
    @SuppressWarnings("unchecked")
    void getPrompt_shouldReturnMessages_whenQuickstart() {
        Map<String, Object> result = registry.getPrompt("quickstart", Map.of());

        assertNotNull(result);
        assertNotNull(result.get("description"));
        List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
        assertNotNull(messages);
        assertFalse(messages.isEmpty());

        Map<String, Object> firstMsg = messages.get(0);
        assertEquals("user", firstMsg.get("role"));
        Map<String, Object> content = (Map<String, Object>) firstMsg.get("content");
        assertEquals("text", content.get("type"));
        assertTrue(content.get("text").toString().contains("fh_connect"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPrompt_shouldSubstituteDroneSn_whenFlyDrone() {
        Map<String, Object> result = registry.getPrompt("fly-drone", Map.of("drone_sn", "DRONE-TEST-001"));

        assertNotNull(result);
        List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
        Map<String, Object> content = (Map<String, Object>) messages.get(0).get("content");
        String text = (String) content.get("text");

        assertTrue(text.contains("DRONE-TEST-001"), "Should substitute drone_sn parameter");
        assertFalse(text.contains("{drone_sn}"), "Should not contain placeholder");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getPrompt_shouldSubstituteDockSn_whenRunWayline() {
        Map<String, Object> result = registry.getPrompt("run-wayline", Map.of("dock_sn", "DOCK-ABC"));

        assertNotNull(result);
        List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
        Map<String, Object> content = (Map<String, Object>) messages.get(0).get("content");
        String text = (String) content.get("text");

        assertTrue(text.contains("DOCK-ABC"));
    }

    // ==================== prompts/get 错误场景 ====================

    @Test
    void getPrompt_shouldReturnNull_whenUnknownName() {
        Map<String, Object> result = registry.getPrompt("nonexistent-prompt", Map.of());
        assertNull(result);
    }

    @Test
    void getPrompt_shouldThrow_whenMissingRequiredArg() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.getPrompt("fly-drone", Map.of()));
        assertTrue(ex.getMessage().contains("drone_sn"));
    }

    @Test
    void getPrompt_shouldThrow_whenRequiredArgIsBlank() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.getPrompt("fly-drone", Map.of("drone_sn", "  ")));
        assertTrue(ex.getMessage().contains("drone_sn"));
    }

    @Test
    void getPrompt_shouldSucceed_whenRequiredArgProvided() {
        assertDoesNotThrow(() -> registry.getPrompt("check-device", Map.of("device_sn", "DEV-001")));
    }

    // ==================== 注册 ====================

    @Test
    void register_shouldIncrementCount() {
        McpPromptRegistry empty = new McpPromptRegistry();
        assertEquals(0, empty.getPromptCount());

        empty.register(new PromptDefinition("test", "desc", List.of(),
                args -> List.of(PromptDefinition.Message.userText("hello"))));
        assertEquals(1, empty.getPromptCount());
    }

    @Test
    void getPromptCount_shouldReturn5ForFlightHubPrompts() {
        assertEquals(5, registry.getPromptCount());
    }

    // ==================== handleGetRequest（共享辅助方法） ====================

    @Test
    @SuppressWarnings("unchecked")
    void handleGetRequest_shouldReturnResult_whenValid() {
        Map<String, Object> params = Map.of("name", "quickstart", "arguments", Map.of());
        Map<String, Object> result = registry.handleGetRequest(params);

        assertNotNull(result);
        assertNotNull(result.get("description"));
        List<Map<String, Object>> messages = (List<Map<String, Object>>) result.get("messages");
        assertFalse(messages.isEmpty());
    }

    @Test
    void handleGetRequest_shouldThrow_whenNameMissing() {
        Map<String, Object> params = Map.of("arguments", Map.of());
        McpPromptRegistry.PromptRequestException ex = assertThrows(
                McpPromptRegistry.PromptRequestException.class,
                () -> registry.handleGetRequest(params));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void handleGetRequest_shouldThrow_whenNameIsNotString() {
        // 类型安全：name 为数字时不应 ClassCastException
        Map<String, Object> params = Map.of("name", 42, "arguments", Map.of());
        McpPromptRegistry.PromptRequestException ex = assertThrows(
                McpPromptRegistry.PromptRequestException.class,
                () -> registry.handleGetRequest(params));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void handleGetRequest_shouldThrow_whenNameIsBlank() {
        Map<String, Object> params = Map.of("name", "  ", "arguments", Map.of());
        McpPromptRegistry.PromptRequestException ex = assertThrows(
                McpPromptRegistry.PromptRequestException.class,
                () -> registry.handleGetRequest(params));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void handleGetRequest_shouldThrow_whenUnknownPrompt() {
        Map<String, Object> params = Map.of("name", "nonexistent", "arguments", Map.of());
        McpPromptRegistry.PromptRequestException ex = assertThrows(
                McpPromptRegistry.PromptRequestException.class,
                () -> registry.handleGetRequest(params));
        assertTrue(ex.getMessage().contains("Unknown prompt"));
    }

    @Test
    void handleGetRequest_shouldThrow_whenRequiredArgMissing() {
        Map<String, Object> params = Map.of("name", "fly-drone", "arguments", Map.of());
        McpPromptRegistry.PromptRequestException ex = assertThrows(
                McpPromptRegistry.PromptRequestException.class,
                () -> registry.handleGetRequest(params));
        assertTrue(ex.getMessage().contains("drone_sn"));
    }
}
