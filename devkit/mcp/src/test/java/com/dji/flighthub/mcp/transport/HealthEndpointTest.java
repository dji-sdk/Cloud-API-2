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
import com.dji.flighthub.mcp.config.McpToolRegistry;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HealthEndpoint 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class HealthEndpointTest {

    private HealthEndpoint endpoint;
    private McpToolRegistry toolRegistry;
    private SessionManager sessionManager;
    private SdkService sdkService;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        sessionManager = new SessionManager(properties);
        sdkService = new SdkService();
        toolRegistry = new McpToolRegistry();
        endpoint = new HealthEndpoint(toolRegistry, sessionManager, sdkService);
    }

    // ==================== /health ====================

    @Test
    @DisplayName("/health 应始终返回 200 + UP")
    void health_shouldReturnUp() {
        ResponseEntity<Map<String, Object>> response = endpoint.health();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertNotNull(body.get("timestamp"));
    }

    // ==================== /ready ====================

    @Test
    @DisplayName("/ready 工具未注册时返回 503 + NOT_READY")
    void ready_shouldReturn503_whenNoToolsRegistered() {
        ResponseEntity<Map<String, Object>> response = endpoint.ready();

        // 空 registry 无工具 → NOT_READY
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("NOT_READY", body.get("status"));
    }

    @Test
    @DisplayName("/ready 工具已注册时返回 200 + READY")
    void ready_shouldReturnReady_whenToolsRegistered() {
        // 注册一个 dummy tool
        toolRegistry.register(
                Map.of("name", "test_tool", "description", "test"),
                args -> Map.of("success", true)
        );

        ResponseEntity<Map<String, Object>> response = endpoint.ready();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("READY", body.get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> checks = (Map<String, Object>) body.get("checks");
        assertNotNull(checks);
        assertEquals(true, checks.get("session_manager"));
        assertEquals(true, checks.get("tools_registered"));
    }

    // ==================== /status ====================

    @Test
    @DisplayName("/status 应返回服务器信息")
    void status_shouldReturnServerInfo() {
        ResponseEntity<Map<String, Object>> response = endpoint.status();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        @SuppressWarnings("unchecked")
        Map<String, Object> server = (Map<String, Object>) body.get("server");
        assertNotNull(server);
        assertEquals("flighthub-mcp-server", server.get("name"));
        assertEquals("1.0.0", server.get("version"));
        assertNotNull(server.get("uptime_seconds"));

        @SuppressWarnings("unchecked")
        Map<String, Object> jvm = (Map<String, Object>) body.get("jvm");
        assertNotNull(jvm);
        assertTrue(((Number) jvm.get("processors")).intValue() > 0);
    }
}
