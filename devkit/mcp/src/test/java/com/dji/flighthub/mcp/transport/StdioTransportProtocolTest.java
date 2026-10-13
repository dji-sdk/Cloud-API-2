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

package com.dji.flighthub.mcp.transport;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.config.McpPromptRegistry;
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.prompts.FlightHubPrompts;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StdioTransport JSON-RPC 协议测试。
 * 通过反射调用 handleRequest 方法，验证请求路由和响应格式。
 */
class StdioTransportProtocolTest {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private StdioTransport transport;
    private ObjectMapper objectMapper;
    private Method handleRequestMethod;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(
                Map.of("name", "test_tool", "description", "A test tool"),
                args -> Map.of("success", true, "data", Map.of("result", "ok")));

        McpPromptRegistry promptRegistry = new McpPromptRegistry();
        promptRegistry.registerAll(new FlightHubPrompts().getDefinitions());

        transport = new StdioTransport(registry, promptRegistry, objectMapper, new FlightHubProperties());

        // 反射获取 private handleRequest 方法
        handleRequestMethod = StdioTransport.class.getDeclaredMethod("handleRequest", Map.class);
        handleRequestMethod.setAccessible(true);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeHandleRequest(String json) throws Exception {
        Map<String, Object> request = objectMapper.readValue(json, MAP_TYPE);
        return (Map<String, Object>) handleRequestMethod.invoke(transport, request);
    }

    @Test
    void initialize_shouldReturnProtocolVersion() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{}}";
        Map<String, Object> response = invokeHandleRequest(request);

        assertEquals("2.0", response.get("jsonrpc"));
        assertEquals(1, response.get("id"));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertEquals("2025-06-18", result.get("protocolVersion"));
        assertNotNull(result.get("serverInfo"));
        assertNotNull(result.get("capabilities"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toolsList_shouldReturnRegisteredTools() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";
        Map<String, Object> response = invokeHandleRequest(request);

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertNotNull(result.get("tools"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void toolsCall_shouldExecuteToolAndReturnResult() throws Exception {
        // tools/call 已移至工作线程异步执行，不再经 handleRequest 路由；
        // 直接反射调用同步的 handleToolsCall 验证执行与响应构建逻辑
        Method handleToolsCall = StdioTransport.class.getDeclaredMethod(
                "handleToolsCall", Object.class, Map.class);
        handleToolsCall.setAccessible(true);

        Map<String, Object> params = Map.of("name", "test_tool", "arguments", Map.of());
        Map<String, Object> response = (Map<String, Object>) handleToolsCall.invoke(transport, 3, params);

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertEquals(false, result.get("isError"));
        assertNotNull(result.get("content"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void unknownMethod_shouldReturnError() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"unknown/method\",\"params\":{}}";
        Map<String, Object> response = invokeHandleRequest(request);

        assertNotNull(response.get("error"));
        Map<String, Object> error = (Map<String, Object>) response.get("error");
        assertEquals(-32601, error.get("code"));
        assertTrue(error.get("message").toString().contains("Method not found"));
    }

    @Test
    void ping_shouldReturnEmptyResult() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"ping\",\"params\":{}}";
        Map<String, Object> response = invokeHandleRequest(request);

        assertNotNull(response.get("result"));
        assertNull(response.get("error"));
    }

    @Test
    void initialized_notification_shouldReturnNull() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":6,\"method\":\"initialized\",\"params\":{}}";
        Map<String, Object> response = invokeHandleRequest(request);

        // "initialized" 是通知，不需要响应
        assertNull(response);
    }

    @Test
    void initialize_shouldDetectElicitationCapability_whenClientDeclaresIt() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"initialize\","
                + "\"params\":{\"capabilities\":{\"elicitation\":{}}}}";
        invokeHandleRequest(request);

        assertTrue(transport.supportsElicitation(),
                "client 声明 elicitation 能力后 supportsElicitation 应为 true");
    }

    @Test
    void initialize_shouldNotDetectElicitation_whenAbsent() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":8,\"method\":\"initialize\","
                + "\"params\":{\"capabilities\":{}}}";
        invokeHandleRequest(request);

        assertFalse(transport.supportsElicitation());
    }

    @Test
    @SuppressWarnings("unchecked")
    void initialize_shouldNegotiateOlderClientVersion() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":9,\"method\":\"initialize\","
                + "\"params\":{\"protocolVersion\":\"2024-11-05\",\"capabilities\":{}}}";
        Map<String, Object> response = invokeHandleRequest(request);

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertEquals("2024-11-05", result.get("protocolVersion"),
                "server 应回显 client 请求的较低版本");
    }

    @Test
    @SuppressWarnings("unchecked")
    void initialize_shouldReturnServerVersion_whenClientVersionNewer() throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":10,\"method\":\"initialize\","
                + "\"params\":{\"protocolVersion\":\"2099-01-01\",\"capabilities\":{}}}";
        Map<String, Object> response = invokeHandleRequest(request);

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertEquals("2025-06-18", result.get("protocolVersion"),
                "client 版本高于 server 时，应返回 server 支持的最高版本");
    }
}
