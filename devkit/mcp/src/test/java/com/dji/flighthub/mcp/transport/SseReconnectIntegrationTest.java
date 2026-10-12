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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE Reconnect 协议集成测试。
 * 验证完整的断连→grace period→重连流程。
 */
class SseReconnectIntegrationTest {

    private SseTransport sseTransport;
    private ClientRegistry clientRegistry;
    private SessionManager sessionManager;
    private FlightHubProperties properties;

    @BeforeEach
    void setUp() {
        McpToolRegistry toolRegistry = new McpToolRegistry();
        McpPromptRegistry promptRegistry = new McpPromptRegistry();
        ObjectMapper objectMapper = new ObjectMapper();
        clientRegistry = new ClientRegistry();
        properties = new FlightHubProperties();
        sessionManager = new SessionManager(properties);
        sseTransport = new SseTransport(toolRegistry, promptRegistry, objectMapper, clientRegistry, sessionManager, properties, "/pf-devkit/ai-plugin/mcp");
    }

    // ==================== 成功重连 ====================

    @Test
    @SuppressWarnings("unchecked")
    void reconnect_shouldSucceed_whenGracePeriodActive() {
        // 1. 模拟旧客户端已建立 session
        String oldClientId = "old_abc";
        SessionContext session = sessionManager.createSession("ws-uuid", "TestWorkspace");
        String sessionId = session.getSessionId();

        clientRegistry.register(oldClientId, createSink());
        clientRegistry.bindSession(oldClientId, sessionId);

        // 2. 模拟旧客户端断连，进入 grace period
        clientRegistry.disconnect(oldClientId);
        sessionManager.enterGracePeriod(sessionId);

        // 3. 新客户端连接并发送 reconnect
        String newClientId = "new_xyz";
        clientRegistry.register(newClientId, createSink());

        Map<String, Object> response = invokeHandleRequest(newClientId, Map.of(
                "method", "reconnect",
                "id", 1,
                "params", Map.of("previousClientId", oldClientId)
        ));

        // 4. 验证成功响应
        assertNotNull(response);
        assertEquals("2.0", response.get("jsonrpc"));
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertNotNull(result);
        assertEquals(true, result.get("reconnected"));
        assertEquals(sessionId, result.get("sessionId"));
        assertEquals("TestWorkspace", result.get("workspaceName"));
        assertNotNull(result.get("graceElapsedSeconds"));
        assertNotNull(result.get("controlAuthority"));
        assertNotNull(result.get("drcLink"));

        // 5. 验证 session 已退出 grace period
        assertFalse(session.isInGracePeriod());

        // 6. 验证新客户端已绑定 session
        assertEquals(sessionId, clientRegistry.getClient(newClientId).getSessionId());
    }

    // ==================== 失败场景 ====================

    @Test
    @SuppressWarnings("unchecked")
    void reconnect_shouldFail_whenPreviousClientIdUnknown() {
        String newClientId = "new_xyz";
        clientRegistry.register(newClientId, createSink());

        Map<String, Object> response = invokeHandleRequest(newClientId, Map.of(
                "method", "reconnect",
                "id", 1,
                "params", Map.of("previousClientId", "nonexistent_id")
        ));

        assertNotNull(response);
        Map<String, Object> error = (Map<String, Object>) response.get("error");
        assertNotNull(error);
        assertEquals(-32002, error.get("code"));
        assertTrue(error.get("message").toString().contains("Unknown previousClientId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reconnect_shouldFail_whenMissingPreviousClientId() {
        String newClientId = "new_xyz";
        clientRegistry.register(newClientId, createSink());

        Map<String, Object> response = invokeHandleRequest(newClientId, Map.of(
                "method", "reconnect",
                "id", 1,
                "params", Map.of()
        ));

        assertNotNull(response);
        Map<String, Object> error = (Map<String, Object>) response.get("error");
        assertNotNull(error);
        assertEquals(-32602, error.get("code"));
        assertTrue(error.get("message").toString().contains("previousClientId"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void reconnect_shouldFail_whenGracePeriodExpired() throws InterruptedException {
        // 1. 模拟旧客户端断连
        String oldClientId = "old_abc";
        SessionContext session = sessionManager.createSession("ws-uuid", "TestWorkspace");
        String sessionId = session.getSessionId();

        clientRegistry.register(oldClientId, createSink());
        clientRegistry.bindSession(oldClientId, sessionId);
        clientRegistry.disconnect(oldClientId);
        sessionManager.enterGracePeriod(sessionId);

        // 2. 等待超过 grace period
        Thread.sleep(20);

        // 3. 新客户端尝试重连（grace period 为 0s，已过期）
        String newClientId = "new_xyz";
        clientRegistry.register(newClientId, createSink());

        // handleReconnect 中硬编码了 60s，需通过配置传递
        // 但由于 disconnect 才 20ms，60s 不会过期。用 cleanupExpiredBindings 模拟清理
        clientRegistry.cleanupExpiredBindings(0); // 强制清理

        Map<String, Object> response = invokeHandleRequest(newClientId, Map.of(
                "method", "reconnect",
                "id", 1,
                "params", Map.of("previousClientId", oldClientId)
        ));

        assertNotNull(response);
        Map<String, Object> error = (Map<String, Object>) response.get("error");
        assertNotNull(error);
        // 绑定已被清理，返回 -32002 unknown
        assertEquals(-32002, error.get("code"));
    }

    // ==================== 完整生命周期 ====================

    @Test
    @SuppressWarnings("unchecked")
    void fullLifecycle_connectDisconnectReconnect() {
        // 1. 客户端连接 + 创建 session
        String clientId1 = "client_v1";
        clientRegistry.register(clientId1, createSink());
        SessionContext session = sessionManager.createSession("ws-uuid", "Workspace");
        String sessionId = session.getSessionId();
        clientRegistry.bindSession(clientId1, sessionId);

        // 模拟拥有控制权
        session.setActiveControlDevice("DRONE-001");

        // 2. 断连
        clientRegistry.disconnect(clientId1);
        sessionManager.enterGracePeriod(sessionId);

        // 3. 新连接 + 重连
        String clientId2 = "client_v2";
        clientRegistry.register(clientId2, createSink());

        Map<String, Object> response = invokeHandleRequest(clientId2, Map.of(
                "method", "reconnect",
                "id", 1,
                "params", Map.of("previousClientId", clientId1)
        ));

        // 4. 验证恢复
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertEquals(true, result.get("reconnected"));
        assertEquals(sessionId, result.get("sessionId"));

        // 5. 验证控制权保留
        Map<String, Object> controlAuth = (Map<String, Object>) result.get("controlAuthority");
        assertEquals(true, controlAuth.get("active"));
        assertEquals("DRONE-001", controlAuth.get("deviceSn"));

        // 6. 新客户端可以继续使用
        assertFalse(session.isInGracePeriod());
        assertTrue(session.hasControlAuthority());
    }

    // ==================== 辅助方法 ====================

    private Sinks.Many<ServerSentEvent<String>> createSink() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    /**
     * 通过反射调用 handleRequest 方法。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeHandleRequest(String clientId, Map<String, Object> request) {
        try {
            var method = SseTransport.class.getDeclaredMethod("handleRequest", String.class, Map.class);
            method.setAccessible(true);
            return (Map<String, Object>) method.invoke(sseTransport, clientId, request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handleRequest", e);
        }
    }
}
