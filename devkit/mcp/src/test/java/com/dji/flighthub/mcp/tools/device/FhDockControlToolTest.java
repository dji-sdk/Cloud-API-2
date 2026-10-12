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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDockControlTool 单元测试。
 * 覆盖 session 校验、参数校验、confirm 缺失和 dock 不存在。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDockControlToolTest {

    private static final String DOCK_SN = "DOCK-CTRL-001";

    private SessionManager sessionManager;
    private FhDockControlTool dockControlTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // Mock 模式 SdkService，getDock 返回 null（模拟 dock 不存在）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        dockControlTool = new FhDockControlTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = dockControlTool.getToolDefinition();

        assertEquals("fh_dock_control", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "cover_open",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "cover_open");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnBlank() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", "  ");
        args.put("action", "cover_open");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "self_destruct");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenActionNull() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenConfirmMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "cover_open");

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenConfirmFalse() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "cover_close");
        args.put("confirm", false);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenDockNotFound() {
        // 在 mock 模式下，getDock 返回 null
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "cover_open");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Dock not found"));
    }
}
