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

package com.dji.flighthub.mcp.tools.drc;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDrcDisconnectTool 单元测试。
 * 覆盖 session 校验、drone_sn 校验、DRC 链路存在校验及 mock 模式正常流程与状态清除。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDrcDisconnectToolTest {

    private static final String DRONE_SN = "DRONE-DRC-001";

    private SessionManager sessionManager;
    private FhDrcDisconnectTool disconnectTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        disconnectTool = new FhDrcDisconnectTool(sessionManager, sdkService);
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = disconnectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = disconnectTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNoDrcLink() {
        // session 存在但无 DRC 链路
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = disconnectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No DRC link to " + DRONE_SN));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenMockMode() {
        // 前置条件满足：session 存在 + 持有控制权 + 已建立 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = disconnectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(false, data.get("drc_active"));
        assertEquals(true, data.get("_mock"));

        // 验证包含 next_steps（提示释放控制权）
        assertNotNull(result.get("next_steps"));
    }

    @Test
    void execute_shouldClearDrcState_whenSuccessful() {
        // 成功断开 DRC 链路后，session 的 DRC 状态应被清除
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        disconnectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertFalse(session.hasDrcLinkTo(DRONE_SN));
        assertFalse(session.hasDrcLink());
        // 控制权不受影响
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = disconnectTool.getToolDefinition();

        assertEquals("fh_drc_disconnect", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }
}
