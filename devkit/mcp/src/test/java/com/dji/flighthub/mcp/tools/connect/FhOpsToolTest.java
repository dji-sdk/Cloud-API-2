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

package com.dji.flighthub.mcp.tools.connect;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhOpsTool 单元测试。
 * 覆盖 status/set_log_level/shutdown 三种操作及参数校验。
 *
 * @author DJI FlightHub SDK Team
 */
class FhOpsToolTest {

    private SessionManager sessionManager;
    private FhOpsTool opsTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        SdkService sdkService = new SdkService() {  public boolean isMockMode() { return true; } };
        opsTool = new FhOpsTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = opsTool.getToolDefinition();

        assertEquals("fh_ops", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnStatus_whenActionIsStatus() {
        Map<String, Object> result = opsTool.execute(Map.of("action", "status"));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("1.0.0", data.get("server_version"));
        assertEquals("2025-06-18", data.get("mcp_protocol_version"));
        assertNotNull(data.get("uptime_seconds"));
        assertNotNull(data.get("java_version"));
        // P1 #4: sdk_version 字段在 mock 模式下返回 "MOCK"
        assertEquals("MOCK", data.get("sdk_version"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReflectSessionState_whenHasActiveSession() {
        // 先无 session
        Map<String, Object> resultNoSession = opsTool.execute(Map.of("action", "status"));
        Map<String, Object> dataNoSession = (Map<String, Object>) resultNoSession.get("data");
        assertEquals(false, dataNoSession.get("session_active"));

        // 创建 session 后
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> resultWithSession = opsTool.execute(Map.of("action", "status"));
        Map<String, Object> dataWithSession = (Map<String, Object>) resultWithSession.get("data");
        assertEquals(true, dataWithSession.get("session_active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSetLogLevel_whenLevelProvided() {
        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "set_log_level",
                "level", "DEBUG"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("DEBUG", data.get("log_level"));
    }

    @Test
    void execute_shouldReturnError_whenLevelMissing() {
        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "set_log_level"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing 'level' parameter"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "invalid_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Unknown action"));
    }

    @Test
    void closeAllSessions_shouldClearActiveSession() {
        // 注意：直接调用 opsTool.execute(shutdown) 会触发 System.exit(0) 杀死 JVM。
        // 此测试验证 shutdown 的核心逻辑（session 清理）通过 SessionManager 正确工作。
        // Tool → SessionManager.closeAllSessions() 的调用链在集成测试中覆盖。
        sessionManager.createSession("uuid", "ws");
        assertTrue(sessionManager.hasActiveSession());

        sessionManager.closeAllSessions();
        assertFalse(sessionManager.hasActiveSession());
    }
}
