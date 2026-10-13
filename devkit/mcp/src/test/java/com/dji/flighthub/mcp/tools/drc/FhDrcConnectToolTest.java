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
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.elicit.ElicitResult;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDrcConnectTool 单元测试。
 * 覆盖 session 校验、drone_sn 校验、控制权校验、幂等保护及 mock 模式正常流程。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhDrcConnectToolTest {

    private static final String DRONE_SN = "DRONE-DRC-001";

    private SessionManager sessionManager;
    private FhDrcConnectTool connectTool;

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
        connectTool = new FhDrcConnectTool(sessionManager, sdkService);
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = connectTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenNoControlAuthority() {
        // session 存在但未获取控制权
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No control authority for " + DRONE_SN));
    }

    @Test
    void execute_shouldSucceedDirectly() {
        // L0 操作无需确认，直接执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"),
                "L0 操作无需确认，直接执行成功");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    @Test
    void execute_shouldReturnError_whenDrcAlreadyActive() {
        // session 存在、持有控制权、且 DRC 链路已激活（幂等保护）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("DRC link already active for " + DRONE_SN));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenMockMode() {
        // 前置条件满足：session 存在 + 持有控制权 + 无现存 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(true, data.get("drc_active"));
        assertEquals(true, data.get("_mock"));

        // 验证包含 next_steps
        assertNotNull(result.get("next_steps"));
    }

    @Test
    void execute_shouldSetDrcState_whenSuccessful() {
        // 成功建立 DRC 链路后，session 状态应更新
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    // ==================== Elicitation 人工确认测试 ====================

    @Test
    void execute_shouldSucceed_whenElicitationBridgePresent() {
        // L0 操作无需确认，即使 ClientBridge 已设置也不触发 elicitation
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"), "L0 操作无需确认，直接执行");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    @Test
    void execute_shouldSucceed_evenWithElicitationDeclined() {
        // L0 操作无需确认，elicitation decline 不影响执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"),
                "L0 操作不触发 elicitation，decline 不影响执行");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    @Test
    void execute_shouldSucceed_evenWithElicitationTimedOut() {
        // L0 操作无需确认，timeout 不影响执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = connectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = connectTool.getToolDefinition();

        assertEquals("fh_drc_connect", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }
}
