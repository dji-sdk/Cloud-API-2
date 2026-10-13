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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionManager 多 session 路由测试。
 * 验证 RequestContext + ClientRegistry 驱动的 session 解析逻辑。
 */
class SessionManagerMultiSessionTest {

    private SessionManager sessionManager;
    private ClientRegistry clientRegistry;
    private FlightHubProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        clientRegistry = new ClientRegistry();
        sessionManager.setClientRegistry(clientRegistry);
    }

    @AfterEach
    void tearDown() {
        RequestContext.clear();
    }

    // ==================== createSessionForUser ====================

    @Nested
    @DisplayName("createSessionForUser 用户 session 创建")
    class CreateSessionForUserTests {

        @Test
        @DisplayName("应为用户创建 session 并建立 userId 索引")
        void createSessionForUser_shouldBuildUserIndex() {
            SessionContext session = sessionManager.createSessionForUser("user-A", "ws-uuid", "Test");

            assertNotNull(session);
            assertEquals("user-A", session.getUserId());
            assertSame(session, sessionManager.getSessionByUserId("user-A"));
        }

        @Test
        @DisplayName("userId 为 null 时应正常创建（stdio 兼容）")
        void createSessionForUser_shouldWorkWithNullUserId() {
            SessionContext session = sessionManager.createSessionForUser(null, "ws-uuid", "Test");

            assertNotNull(session);
            assertNull(session.getUserId());
            assertNull(sessionManager.getSessionByUserId(null));
        }

        @Test
        @DisplayName("不同用户应创建独立 session")
        void createSessionForUser_shouldIsolateSessions() {
            SessionContext sessionA = sessionManager.createSessionForUser("user-A", "ws", "WS-A");
            SessionContext sessionB = sessionManager.createSessionForUser("user-B", "ws", "WS-B");

            assertNotEquals(sessionA.getSessionId(), sessionB.getSessionId());
            assertSame(sessionA, sessionManager.getSessionByUserId("user-A"));
            assertSame(sessionB, sessionManager.getSessionByUserId("user-B"));
        }
    }

    // ==================== getActiveSession 路由 ====================

    @Nested
    @DisplayName("getActiveSession 多 session 路由")
    class GetActiveSessionRoutingTests {

        @Test
        @DisplayName("无 RequestContext 时应 fallback 到已有 session（stdio 兼容）")
        void getActiveSession_shouldFallbackToExisting_whenNoContext() {
            SessionContext first = sessionManager.createSession("ws", "First");
            SessionContext second = sessionManager.createSession("ws", "Second");

            // 无 RequestContext — fallback 到某个 session（ConcurrentHashMap 不保证顺序）
            RequestContext.clear();
            SessionContext active = sessionManager.getActiveSession();
            assertNotNull(active);
            assertTrue(active.getSessionId().equals(first.getSessionId())
                    || active.getSessionId().equals(second.getSessionId()));
        }

        @Test
        @DisplayName("RequestContext 携带 sessionId 时直接命中")
        void getActiveSession_shouldResolveBySessionId() {
            SessionContext sessionA = sessionManager.createSessionForUser("user-A", "ws", "A");
            SessionContext sessionB = sessionManager.createSessionForUser("user-B", "ws", "B");

            RequestContext.set(1, "fh_test", "client-x");
            RequestContext.current().setSessionId(sessionB.getSessionId());

            assertSame(sessionB, sessionManager.getActiveSession());
        }

        @Test
        @DisplayName("通过 ClientRegistry 间接解析 session")
        void getActiveSession_shouldResolveViaClientRegistry() {
            SessionContext sessionA = sessionManager.createSessionForUser("user-A", "ws", "A");

            // 注册客户端并绑定 session
            String clientId = "sse-client-1";
            Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
            clientRegistry.register(clientId, sink);
            clientRegistry.bindSession(clientId, sessionA.getSessionId());

            // 设置 RequestContext 带有 clientId（模拟 SSE 请求）
            RequestContext.set(1, "fh_test", clientId);

            assertSame(sessionA, sessionManager.getActiveSession());
        }

        @Test
        @DisplayName("SSE 客户端未绑定 session 时返回 null（跨用户隔离）")
        void getActiveSession_shouldReturnNull_whenSseClientHasNoSession() {
            sessionManager.createSession("ws", "First");

            String clientId = "sse-client-2";
            Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
            clientRegistry.register(clientId, sink);
            // 不绑定 session

            RequestContext.set(1, "fh_test", clientId);

            // SSE 客户端未绑定 session → 返回 null（不得 fallback 到其他用户的 session）
            assertNull(sessionManager.getActiveSession());
        }

        @Test
        @DisplayName("stdio clientId 应跳过 ClientRegistry 走 fallback")
        void getActiveSession_shouldSkipRegistryForStdio() {
            SessionContext session = sessionManager.createSession("ws", "Stdio");

            RequestContext.set(1, "fh_test", "stdio");

            assertSame(session, sessionManager.getActiveSession());
        }
    }

    // ==================== findSessionControlling 冲突检测 ====================

    @Nested
    @DisplayName("findSessionControlling 跨 session 冲突检测")
    class ConflictDetectionTests {

        @Test
        @DisplayName("无 session 控制指定设备时返回 null")
        void findSessionControlling_shouldReturnNull_whenNoConflict() {
            sessionManager.createSessionForUser("user-A", "ws", "A");
            assertNull(sessionManager.findSessionControlling("drone-001"));
        }

        @Test
        @DisplayName("应检测到另一 session 的控制权冲突")
        void findSessionControlling_shouldDetectConflict() {
            SessionContext sessionA = sessionManager.createSessionForUser("user-A", "ws", "A");
            sessionA.setActiveControlDevice("drone-001");

            SessionContext conflict = sessionManager.findSessionControlling("drone-001");
            assertNotNull(conflict);
            assertEquals("user-A", conflict.getUserId());
            assertSame(sessionA, conflict);
        }

        @Test
        @DisplayName("同一 session 控制同一设备不算冲突")
        void findSessionControlling_shouldReturnSelf_whenSameSession() {
            SessionContext session = sessionManager.createSessionForUser("user-A", "ws", "A");
            session.setActiveControlDevice("drone-001");

            // findSessionControlling 返回的是持有者，调用方需自行比对 sessionId
            SessionContext holder = sessionManager.findSessionControlling("drone-001");
            assertSame(session, holder);
        }
    }

    // ==================== closeSession 清理 userId 索引 ====================

    @Nested
    @DisplayName("closeSession 用户索引清理")
    class CloseSessionTests {

        @Test
        @DisplayName("关闭 session 应清理 userId 索引")
        void closeSession_shouldCleanupUserIndex() {
            SessionContext session = sessionManager.createSessionForUser("user-A", "ws", "A");
            assertNotNull(sessionManager.getSessionByUserId("user-A"));

            sessionManager.closeSession(session.getSessionId());
            assertNull(sessionManager.getSessionByUserId("user-A"));
        }

        @Test
        @DisplayName("closeAllSessions 应清理所有 userId 索引")
        void closeAllSessions_shouldCleanupAllUserIndexes() {
            sessionManager.createSessionForUser("user-A", "ws", "A");
            sessionManager.createSessionForUser("user-B", "ws", "B");

            sessionManager.closeAllSessions();

            assertNull(sessionManager.getSessionByUserId("user-A"));
            assertNull(sessionManager.getSessionByUserId("user-B"));
        }
    }
}
