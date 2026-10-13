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

package com.dji.flighthub.mcp.session;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.transport.ClientRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionManager 单元测试。
 */
class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        properties.getSecurity().setSessionTimeout(1800);
        sessionManager = new SessionManager(properties);
    }

    @Test
    void createSession_shouldReturnValidSession() {
        SessionContext session = sessionManager.createSession("ws-uuid-123", "TestWorkspace");

        assertNotNull(session);
        assertTrue(session.getSessionId().startsWith("sess_"));
        assertEquals(12, session.getSessionId().replace("sess_", "").length());
        assertEquals("ws-uuid-123", session.getWorkspaceUuid());
        assertEquals("TestWorkspace", session.getWorkspaceName());
        assertEquals(3, session.getMaxSecurityLevel());
    }

    @Test
    void getActiveSession_shouldReturnNullWhenEmpty() {
        assertNull(sessionManager.getActiveSession());
    }

    @Test
    void getActiveSession_shouldReturnSessionAfterCreate() {
        SessionContext created = sessionManager.createSession("uuid", "name");
        SessionContext active = sessionManager.getActiveSession();

        assertNotNull(active);
        assertEquals(created.getSessionId(), active.getSessionId());
    }

    @Test
    void hasActiveSession_shouldReflectState() {
        assertFalse(sessionManager.hasActiveSession());
        sessionManager.createSession("uuid", "name");
        assertTrue(sessionManager.hasActiveSession());
    }

    @Test
    void closeSession_shouldRemoveSession() {
        SessionContext session = sessionManager.createSession("uuid", "name");
        sessionManager.closeSession(session.getSessionId());

        assertFalse(sessionManager.hasActiveSession());
        assertNull(sessionManager.getActiveSession());
    }

    @Test
    void closeSession_shouldBeIdempotent() {
        SessionContext session = sessionManager.createSession("uuid", "name");
        sessionManager.closeSession(session.getSessionId());
        // 第二次调用不应抛异常
        sessionManager.closeSession(session.getSessionId());
        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void closeAllSessions_shouldClearAll() {
        sessionManager.createSession("uuid1", "name1");
        sessionManager.createSession("uuid2", "name2");
        sessionManager.closeAllSessions();

        assertFalse(sessionManager.hasActiveSession());
    }

    @Test
    void getSession_shouldReturnByIdOrNull() {
        SessionContext session = sessionManager.createSession("uuid", "name");

        assertNotNull(sessionManager.getSession(session.getSessionId()));
        assertNull(sessionManager.getSession("non-existent-id"));
    }

    // ==================== stdio 模式 clientId 路由（回归防护） ====================

    /**
     * 验证 stdio 模式下 ClientRegistry 已注入但无 "stdio" entry 时，
     * getActiveSession 仍应回退返回首个 session，而非误返回 null。
     *
     * <p>回归场景：StdioTransport 设置 RequestContext.clientId="stdio"，
     * ClientRegistry 作为 @Component 在 stdio profile 下也会被注入（非 null），
     * 若不识别 STDIO_CLIENT_ID 会走进 SSE 路由分支并返回 null，
     * 导致本地 IDE 集成 connect 后所有工具调用失败。
     */
    @Test
    void getActiveSession_shouldReturnFirstSession_whenStdioClientWithRegistryInjected() {
        // 模拟 Spring 在 stdio profile 下注入 ClientRegistry（bean 存在但无 stdio entry）
        ClientRegistry registry = new ClientRegistry();
        sessionManager.setClientRegistry(registry);

        SessionContext created = sessionManager.createSession("uuid", "name");

        // 模拟 StdioTransport 设置的请求上下文
        RequestContext.set("req-1", "fh_device_list", RequestContext.STDIO_CLIENT_ID);
        try {
            SessionContext active = sessionManager.getActiveSession();
            assertNotNull(active, "stdio 模式下应回退返回首个 session，而非 null");
            assertEquals(created.getSessionId(), active.getSessionId());
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * 验证 SSE 模式下客户端未绑定 session 时返回 null（不回退到首个 session）。
     */
    @Test
    void getActiveSession_shouldReturnNull_whenSseClientNotBound() {
        ClientRegistry registry = new ClientRegistry();
        sessionManager.setClientRegistry(registry);
        sessionManager.createSession("uuid", "name");

        // SSE 客户端已注册但未 bindSession
        registry.register("sse-client-1", null);

        RequestContext.set("req-2", "fh_takeoff", "sse-client-1");
        try {
            assertNull(sessionManager.getActiveSession(),
                    "SSE 客户端未绑定 session 时应返回 null，不应回退到首个 session");
        } finally {
            RequestContext.clear();
        }
    }

    /**
     * 验证 SSE 模式下客户端已绑定 session 时精确路由返回绑定的 session。
     */
    @Test
    void getActiveSession_shouldReturnBoundSession_whenSseClientBound() {
        ClientRegistry registry = new ClientRegistry();
        sessionManager.setClientRegistry(registry);

        SessionContext first = sessionManager.createSession("uuid-1", "name-1");
        SessionContext second = sessionManager.createSession("uuid-2", "name-2");

        registry.register("sse-client-2", null);
        registry.bindSession("sse-client-2", second.getSessionId());

        RequestContext.set("req-3", "fh_takeoff", "sse-client-2");
        try {
            SessionContext active = sessionManager.getActiveSession();
            assertNotNull(active);
            assertEquals(second.getSessionId(), active.getSessionId(),
                    "SSE 模式应精确路由到客户端绑定的 session，而非首个");
            assertNotEquals(first.getSessionId(), active.getSessionId());
        } finally {
            RequestContext.clear();
        }
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }
}
