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
import com.dji.flighthub.mcp.session.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SseTransport 单元测试。
 * 覆盖 initialize 协议协商、tools/list、tools/call、L1+ 硬拦截、未知方法错误、
 * 以及 clientId 多源解析回退逻辑。
 *
 * @author DJI FlightHub SDK Team
 */
class SseTransportTest {

    private SseTransport sseTransport;
    private McpToolRegistry toolRegistry;
    private ObjectMapper objectMapper;
    private ClientRegistry clientRegistry;
    private SessionManager sessionManager;
    private FlightHubProperties properties;

    @BeforeEach
    void setUp() {
        toolRegistry = createTestToolRegistry();
        objectMapper = new ObjectMapper();
        clientRegistry = new ClientRegistry();
        properties = new FlightHubProperties();
        sessionManager = new SessionManager(properties);
        McpPromptRegistry promptRegistry = new McpPromptRegistry();
        promptRegistry.registerAll(new FlightHubPrompts().getDefinitions());
        sseTransport = new SseTransport(toolRegistry, promptRegistry, objectMapper, clientRegistry, sessionManager, properties, "/pf-devkit/ai-plugin/mcp");

        // 注册 handleRequest 使用的默认测试客户端
        Sinks.Many<ServerSentEvent<String>> defaultSink = Sinks.many().unicast().onBackpressureBuffer();
        clientRegistry.register("test-client", defaultSink);
    }

    @Test
    void message_shouldReturnError_whenClientUnknown() {
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "method", "initialize",
                "id", 1
        );

        Map<String, Object> result = sseTransport.messageByPath("nonexistent-client", request);

        assertNotNull(result.get("error"));
        assertTrue(result.get("error").toString().contains("Unknown client"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRequest_shouldReturnProtocolVersion_whenInitialize() {
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "initialize",
                "id", 1
        ));

        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc"));
        assertEquals(1, response.get("id"));

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertNotNull(result);
        assertEquals("2025-06-18", result.get("protocolVersion"));
        assertNotNull(result.get("capabilities"));
        assertNotNull(result.get("serverInfo"));

        Map<String, Object> serverInfo = (Map<String, Object>) result.get("serverInfo");
        assertEquals("dji-cloud-api2", serverInfo.get("name"));
    }

    @Test
    void handleRequest_shouldReturnNull_whenInitialized() {
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "initialized",
                "id", 2
        ));

        assertNull(response);
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRequest_shouldReturnToolsList() {
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "tools/list",
                "id", 3
        ));

        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc"));
        assertEquals(3, response.get("id"));

        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertNotNull(result);
        assertNotNull(result.get("tools"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRequest_shouldExecuteDangerousToolAsync() {
        // L1+ 工具不再被硬拦截，而是异步执行（返回 null，结果通过 SSE 推送）
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "tools/call",
                "id", 4,
                "params", Map.of(
                        "name", "fh_dock_control",
                        "arguments", Map.of("dock_sn", "DOCK-001", "action", "cover_open")
                )
        ));

        // 异步执行：handleRequest 返回 null（结果通过 SSE 异步推送）
        assertNull(response, "L1+ tool should be executed async, handleRequest returns null");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRequest_shouldExecuteSafeTool() {
        // 安全工具（L0）应该正常执行
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "tools/call",
                "id", 5,
                "params", Map.of(
                        "name", "fh_ops",
                        "arguments", Map.of("action", "status")
                )
        ));

        assertNotNull(response);
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertNotNull(result);
        // fh_ops status 返回 success=true
        assertEquals(false, result.get("isError"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRequest_shouldReturnError_whenMethodUnknown() {
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "nonexistent/method",
                "id", 6
        ));

        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc"));
        assertEquals(6, response.get("id"));

        Map<String, Object> error = (Map<String, Object>) response.get("error");
        assertNotNull(error);
        assertEquals(-32601, error.get("code"));
        assertTrue(error.get("message").toString().contains("Method not found"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleRequest_shouldReturnPong_whenPing() {
        Map<String, Object> response = invokeHandleRequest(Map.of(
                "method", "ping",
                "id", 7
        ));

        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc"));
        assertEquals(7, response.get("id"));
        assertNotNull(response.get("result"));
    }

    // ==================== clientId 回退解析测试 ====================

    @Nested
    @DisplayName("clientId 回退解析逻辑")
    class ClientIdFallbackTests {

        private static final String TEST_CLIENT_ID = "test-abc1";

        @BeforeEach
        void registerTestClient() {
            Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
            clientRegistry.register(TEST_CLIENT_ID, sink);
        }

        @Test
        @DisplayName("路径参数方式应正常工作")
        void messageByPath_shouldAccept_whenValidClientId() {
            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            Map<String, Object> result = sseTransport.messageByPath(TEST_CLIENT_ID, request);

            assertEquals("accepted", result.get("status"));
        }

        @Test
        @DisplayName("query 参数方式应保持向后兼容")
        void message_shouldAccept_whenClientIdInQueryParam() {
            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            Map<String, Object> result = sseTransport.message(TEST_CLIENT_ID, null, null, request);

            assertEquals("accepted", result.get("status"));
        }

        @Test
        @DisplayName("缺少 query param 时应通过 X-Client-Id header 回退")
        void message_shouldFallbackToXClientIdHeader_whenQueryParamMissing() {
            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            Map<String, Object> result = sseTransport.message(null, TEST_CLIENT_ID, null, request);

            assertEquals("accepted", result.get("status"));
        }

        @Test
        @DisplayName("缺少 query param 和 X-Client-Id 时应通过 Mcp-Session-Id 反查")
        void message_shouldFallbackToMcpSessionId_whenNoClientIdAvailable() {
            // 绑定 session
            String sessionId = "sess_test_123";
            clientRegistry.bindSession(TEST_CLIENT_ID, sessionId);

            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            Map<String, Object> result = sseTransport.message(null, null, sessionId, request);

            assertEquals("accepted", result.get("status"));
        }

        @Test
        @DisplayName("单客户端场景：无任何标识时应自动推断唯一活跃客户端")
        void message_shouldAutoDetectSingleClient_whenNoIdentifierProvided() {
            // 移除 setUp 中注册的 base 客户端，确保只有 TEST_CLIENT_ID 一个
            clientRegistry.disconnect("test-client");

            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            // 当前只有 TEST_CLIENT_ID 一个活跃客户端
            Map<String, Object> result = sseTransport.message(null, null, null, request);

            assertEquals("accepted", result.get("status"));
        }

        @Test
        @DisplayName("多客户端场景：无标识时应返回错误")
        void message_shouldReturnError_whenMultipleClientsAndNoIdentifier() {
            // 注册第二个客户端
            Sinks.Many<ServerSentEvent<String>> sink2 = Sinks.many().unicast().onBackpressureBuffer();
            clientRegistry.register("test-def2", sink2);

            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            Map<String, Object> result = sseTransport.message(null, null, null, request);

            assertNotNull(result.get("error"));
            assertTrue(result.get("error").toString().contains("Missing clientId"));
        }

        @Test
        @DisplayName("空白 query param 应触发回退逻辑")
        void message_shouldFallback_whenClientIdIsBlank() {
            Map<String, Object> request = Map.of(
                    "method", "ping",
                    "id", 1
            );

            // 空字符串应被视为缺失
            Map<String, Object> result = sseTransport.message("  ", TEST_CLIENT_ID, null, request);

            assertEquals("accepted", result.get("status"));
        }

        @Test
        @DisplayName("SSE connect 应发送路径格式的 endpoint 事件（纯路径，非 JSON）")
        void connect_shouldSendPathBasedEndpointEvent() {
            // setUp 中 SseTransport 注入了 base-path=/pf-devkit/ai-plugin/mcp
            Flux<ServerSentEvent<String>> flux = sseTransport.connect(null,
                    MockServerWebExchange.from(MockServerHttpRequest.get("/sse").build()));

            ServerSentEvent<String> firstEvent = flux.blockFirst();

            assertNotNull(firstEvent);
            assertEquals("endpoint", firstEvent.event());

            String data = firstEvent.data();
            assertNotNull(data);
            // base-path 存在时下发绝对路径，避免客户端 new URL(data, baseUrl) 丢弃前缀
            assertTrue(data.startsWith("/pf-devkit/ai-plugin/mcp/message/"), "endpoint data 应含 base-path 前缀: " + data);
            assertFalse(data.contains("{"), "endpoint data 不应包含 JSON: " + data);
            assertFalse(data.contains("?clientId="), "endpoint data 不应使用 query 参数: " + data);
        }

        @Test
        @DisplayName("base-path 为空时下发相对路径 /message/{clientId}（兼容本地调试）")
        void connect_shouldSendRelativeEndpointEvent_whenBasePathEmpty() {
            SseTransport noPrefixTransport = new SseTransport(
                    toolRegistry, new McpPromptRegistry(), objectMapper, clientRegistry,
                    sessionManager, properties, "");
            Flux<ServerSentEvent<String>> flux = noPrefixTransport.connect(null,
                    MockServerWebExchange.from(MockServerHttpRequest.get("/sse").build()));

            ServerSentEvent<String> firstEvent = flux.blockFirst();

            assertNotNull(firstEvent);
            String data = firstEvent.data();
            assertNotNull(data);
            assertTrue(data.startsWith("/message/"), "endpoint data 应为相对路径: " + data);
        }
    }

    // ==================== Elicitation 反向确认测试 ====================

    @Nested
    @DisplayName("Elicitation 反向确认（SSE 模式）")
    class ElicitationTests {

        private static final String ELICIT_CLIENT = "elicit-client-1";

        @BeforeEach
        void registerElicitClient() {
            // 使用 unicast sink（与生产一致），初始化 client 并声明 elicitation 能力
            Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
            clientRegistry.register(ELICIT_CLIENT, sink);

            // 通过 initialize 声明 client 支持 elicitation
            sseTransport.messageByPath(ELICIT_CLIENT, Map.of(
                    "method", "initialize",
                    "id", "init-1",
                    "params", Map.of("capabilities", Map.of("elicitation", Map.of()))
            ));
        }

        @Test
        @DisplayName("L1+ 工具异步执行 — handleRequest 返回 null")
        void dangerousTool_shouldReturnNull_fromHandleRequest() {
            // 直接调用 handleRequest 验证返回 null（异步执行）
            Map<String, Object> response = invokeHandleRequest(Map.of(
                    "method", "tools/call",
                    "id", 100,
                    "params", Map.of(
                            "name", "fh_dock_control",
                            "arguments", Map.of("dock_sn", "DOCK-001", "action", "cover_open")
                    )
            ));

            assertNull(response, "L1+ tool handleRequest should return null (async execution)");
        }

        @Test
        @DisplayName("initialize 应在 server capabilities 中声明 elicitation")
        @SuppressWarnings("unchecked")
        void initialize_shouldDeclareElicitationCapability() {
            Map<String, Object> response = invokeHandleRequest(Map.of(
                    "method", "initialize",
                    "id", "init-cap",
                    "params", Map.of("capabilities", Map.of())
            ));

            assertNotNull(response);
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            assertNotNull(result);
            Map<String, Object> capabilities = (Map<String, Object>) result.get("capabilities");
            assertNotNull(capabilities);
            assertTrue(capabilities.containsKey("elicitation"),
                    "Server should declare elicitation capability in initialize response");
        }

        @Test
        @DisplayName("L0 安全工具仍然同步执行（返回非 null）")
        @SuppressWarnings("unchecked")
        void safeTool_shouldStillExecuteSync() {
            Map<String, Object> response = invokeHandleRequest(Map.of(
                    "method", "tools/call",
                    "id", 300,
                    "params", Map.of(
                            "name", "fh_ops",
                            "arguments", Map.of("action", "status")
                    )
            ));

            // L0 工具同步执行，返回非 null
            assertNotNull(response, "L0 tool should execute synchronously");
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            assertNotNull(result);
            assertEquals(false, result.get("isError"));
        }

        @Test
        @DisplayName("client 声明 elicitation 能力后 supportsElicitation 应为 true")
        void clientCapability_shouldBeTracked_afterInitialize() throws Exception {
            // 通过反射访问 clientElicitationSupport
            var field = SseTransport.class.getDeclaredField("clientElicitationSupport");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Boolean> capMap = (Map<String, Boolean>) field.get(sseTransport);

            // ELICIT_CLIENT 已在 setUp 中 initialize 声明了 elicitation
            assertTrue(Boolean.TRUE.equals(capMap.get(ELICIT_CLIENT)),
                    "Client that declared elicitation should be tracked as supporting it");

            // 未 initialize 的 test-client 应为 null 或 false
            assertFalse(Boolean.TRUE.equals(capMap.get("test-client")),
                    "Client without elicitation capability should not be tracked as supporting it");
        }

        @Test
        @DisplayName("恶意 elicitation 响应（非 Map result）不应导致 ClassCastException")
        void malformedElicitationResponse_shouldNotThrow() {
            // 注册一个 pending elicitation（模拟正在等待中的 elicitation）
            var registryField = getElicitationRegistry();
            String elicitId = "fh-elicit-999";
            registryField.register(elicitId);

            // 恶意 client 发送 result 为 string 而非 Map
            Map<String, Object> malformedResponse = Map.of(
                    "jsonrpc", "2.0",
                    "id", elicitId,
                    "result", "ok"  // 应为 Map，但恶意传了 String
            );

            // 不应抛出 ClassCastException
            assertDoesNotThrow(() -> invokeHandleRequest(malformedResponse));
        }

        private com.dji.flighthub.mcp.elicit.ElicitationRegistry getElicitationRegistry() {
            try {
                var field = SseTransport.class.getDeclaredField("elicitationRegistry");
                field.setAccessible(true);
                return (com.dji.flighthub.mcp.elicit.ElicitationRegistry) field.get(sseTransport);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // ================ 辅助方法 ================

    /**
     * 通过反射调用 handleRequest 方法绕过 private 访问。
     * 这比 SSE 连接更简洁直接。
     */
    private Map<String, Object> invokeHandleRequest(Map<String, Object> request) {
        try {
            var method = SseTransport.class.getDeclaredMethod("handleRequest", String.class, Map.class);
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) method.invoke(sseTransport, "test-client", request);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handleRequest", e);
        }
    }

    /**
     * 创建测试用的 McpToolRegistry 实例。
     * 注册最小的工具集，用于验证 SSE 传输层逻辑。
     */
    private static McpToolRegistry createTestToolRegistry() {
        McpToolRegistry registry = new McpToolRegistry();

        // 注册安全工具（L0，无需 confirm）
        registry.register(
                Map.of("name", "fh_ops", "description", "Ops tool"),
                args -> Map.of("success", true, "data", Map.of("status", "healthy")),
                false
        );

        // 注册危险工具（L1，需要 confirm）
        registry.register(
                Map.of("name", "fh_dock_control", "description", "Dock control (L1)"),
                args -> Map.of("success", true, "data", Map.of("action", "executed")),
                true
        );

        return registry;
    }
}
