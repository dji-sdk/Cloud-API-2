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

package com.dji.flighthub.mcp.context;

import com.dji.flighthub.mcp.session.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RequestContext 单元测试 — 验证 ThreadLocal 生命周期和幂等 key 构建。
 */
class RequestContextTest {

    @AfterEach
    void cleanup() {
        RequestContext.clear();
    }

    @Test
    void set_shouldMakeContextAvailableViaCurrent() {
        RequestContext.set("req-001", "fh_takeoff", "client-abc");

        RequestContext ctx = RequestContext.current();
        assertNotNull(ctx);
        assertEquals("req-001", ctx.getRequestId());
        assertEquals("fh_takeoff", ctx.getToolName());
        assertEquals("client-abc", ctx.getClientId());
        assertNotNull(ctx.getTimestamp());
    }

    @Test
    void current_shouldReturnNull_whenNotSet() {
        assertNull(RequestContext.current());
    }

    @Test
    void clear_shouldRemoveContext() {
        RequestContext.set("req-002", "fh_connect", "stdio");
        assertNotNull(RequestContext.current());

        RequestContext.clear();
        assertNull(RequestContext.current());
    }

    @Test
    void buildIdempotencyKey_shouldCombineClientIdToolNameAndRequestId() {
        RequestContext.set("42", "fh_device_list", "abc123");

        String key = RequestContext.current().buildIdempotencyKey();
        assertEquals("abc123:fh_device_list:42", key);
    }

    @Test
    void buildIdempotencyKey_shouldReturnNull_whenRequestIdIsNull() {
        RequestContext.set(null, "fh_device_list", "abc123");

        String key = RequestContext.current().buildIdempotencyKey();
        assertNull(key);
    }

    @Test
    void buildIdempotencyKey_shouldHandleStdioClient() {
        RequestContext.set(1, "fh_connect", "stdio");

        String key = RequestContext.current().buildIdempotencyKey();
        assertEquals("stdio:fh_connect:1", key);
    }

    @Test
    void buildIdempotencyKey_shouldDifferentiateSameIdDifferentTools() {
        // 验证跨工具不碰撞：相同 clientId + requestId，不同 toolName → 不同 key
        RequestContext.set(1, "fh_takeoff", "stdio");
        String key1 = RequestContext.current().buildIdempotencyKey();
        RequestContext.clear();

        RequestContext.set(1, "fh_device_list", "stdio");
        String key2 = RequestContext.current().buildIdempotencyKey();

        assertNotEquals(key1, key2);
        assertEquals("stdio:fh_takeoff:1", key1);
        assertEquals("stdio:fh_device_list:1", key2);
    }

    @Test
    void enrichWithSession_shouldPopulateSessionFields() {
        RequestContext.set("req-003", "fh_ops", "stdio");
        RequestContext ctx = RequestContext.current();

        SessionContext session = new SessionContext("sess-123", "ws-uuid-456", "test-ws", 3);
        ctx.enrichWithSession(session);

        assertEquals("sess-123", ctx.getSessionId());
        assertEquals("ws-uuid-456", ctx.getWorkspaceUuid());
    }

    @Test
    void enrichWithSession_shouldIgnoreNull() {
        RequestContext.set("req-004", "fh_ops", "stdio");
        RequestContext ctx = RequestContext.current();

        ctx.enrichWithSession(null);

        assertNull(ctx.getSessionId());
        assertNull(ctx.getWorkspaceUuid());
    }

    @Test
    void threadIsolation_shouldNotLeakBetweenThreads() throws Exception {
        RequestContext.set("main-req", "fh_main", "main-client");

        Thread other = new Thread(() -> {
            assertNull(RequestContext.current());
            RequestContext.set("other-req", "fh_other", "other-client");
            assertEquals("other-req", RequestContext.current().getRequestId());
            RequestContext.clear();
        });
        other.start();
        other.join();

        // 主线程上下文不受影响
        assertEquals("main-req", RequestContext.current().getRequestId());
    }
}
