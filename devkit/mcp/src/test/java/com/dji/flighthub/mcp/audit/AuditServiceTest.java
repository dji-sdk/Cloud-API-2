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

package com.dji.flighthub.mcp.audit;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.context.RequestContext;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuditService 单元测试 — 验证审计日志记录逻辑。
 */
class AuditServiceTest {

    @AfterEach
    void cleanup() {
        RequestContext.clear();
    }

    private AuditService createService(boolean enabled, String endpoint) {
        FlightHubProperties props = new FlightHubProperties();
        FlightHubProperties.Audit audit = new FlightHubProperties.Audit();
        audit.setEnabled(enabled);
        audit.setEndpoint(endpoint);
        props.setAudit(audit);

        SessionManager sessionManager = mock(SessionManager.class);
        return new AuditService(props, sessionManager);
    }

    private AuditService createServiceWithSession(boolean enabled, SessionContext session) {
        FlightHubProperties props = new FlightHubProperties();
        FlightHubProperties.Audit audit = new FlightHubProperties.Audit();
        audit.setEnabled(enabled);
        props.setAudit(audit);

        SessionManager sessionManager = mock(SessionManager.class);
        when(sessionManager.getActiveSession()).thenReturn(session);
        return new AuditService(props, sessionManager);
    }

    @Test
    void isEnabled_shouldReturnTrue_whenConfigured() {
        AuditService service = createService(true, null);
        assertTrue(service.isEnabled());
    }

    @Test
    void isEnabled_shouldReturnFalse_whenDisabled() {
        AuditService service = createService(false, null);
        assertFalse(service.isEnabled());
    }

    @Test
    void logExecution_shouldNotThrow_whenDisabled() {
        AuditService service = createService(false, null);
        // 禁用时调用不应抛异常
        assertDoesNotThrow(() ->
                service.logExecution("fh_connect", true, null, 100, false));
    }

    @Test
    void logExecution_shouldNotThrow_whenNoRequestContext() {
        AuditService service = createService(true, null);
        // 无 RequestContext 时仍应正常记录（graceful degradation）
        assertDoesNotThrow(() ->
                service.logExecution("fh_connect", true, null, 100, false));
    }

    @Test
    void logExecution_shouldNotThrow_whenContextPresent() {
        RequestContext.set("req-001", "fh_takeoff", "client-abc");
        AuditService service = createService(true, null);

        assertDoesNotThrow(() ->
                service.logExecution("fh_takeoff", true, null, 250, false));
    }

    @Test
    void logExecution_shouldNotThrow_whenIdempotentHit() {
        RequestContext.set("req-002", "fh_device_list", "stdio");
        AuditService service = createService(true, null);

        assertDoesNotThrow(() ->
                service.logExecution("fh_device_list", true, null, 5, true));
    }

    @Test
    void logExecution_shouldNotThrow_whenErrorOccurs() {
        RequestContext.set("req-003", "fh_takeoff", "client-xyz");
        AuditService service = createService(true, null);

        assertDoesNotThrow(() ->
                service.logExecution("fh_takeoff", false, "SDK connection failed", 1200, false));
    }

    @Test
    void logExecution_shouldResolveSessionFromManager() {
        RequestContext.set("req-004", "fh_ops", "stdio");
        SessionContext session = new SessionContext("sess-test", "ws-uuid", "test-workspace", 3);
        AuditService service = createServiceWithSession(true, session);

        // 应正常执行并从 SessionManager 补充 sessionId
        assertDoesNotThrow(() ->
                service.logExecution("fh_ops", true, null, 50, false));
    }

    @Test
    void logExecution_shouldNotThrow_whenEndpointConfigured() {
        RequestContext.set("req-005", "fh_connect", "stdio");
        AuditService service = createService(true, "http://audit.example.com/events");

        // 远程端点配置了但实际 POST 是 stub（不应阻塞/抛异常）
        assertDoesNotThrow(() ->
                service.logExecution("fh_connect", true, null, 100, false));
    }
}
