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
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FhControlAcquireTool 和 FhControlReleaseTool 单元测试。
 * 覆盖 session 校验、幂等保护及 mock 模式正常流程。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhControlAcquireToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";
    private static final String ANOTHER_DRONE_SN = "DRONE-TEST-002";

    private SessionManager sessionManager;
    private FhControlAcquireTool acquireTool;
    private FhControlReleaseTool releaseTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // SdkService 无 FH_MOCK 环境变量时 isMockMode() 取决于环境变量，
        // 但在 CI / 本地单元测试环境中 FH_MOCK 通常未设置，getWorkspace() 返回 null，
        // SdkService 的 mockMode 字段由环境变量 FH_MOCK 决定。
        // 为了让测试走 mock 路径，直接实例化 SdkService（无真实 SDK 时 getDrone 返回 null，
        // isMockMode() 在环境变量未设置时为 false）。
        // 我们在测试中直接验证逻辑分支，mock 路径依赖 sdkService.isMockMode() == true。
        // 若 CI 未设置 FH_MOCK=true，则用子类覆盖 isMockMode。
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        acquireTool = new FhControlAcquireTool(sessionManager, sdkService);
        releaseTool = new FhControlReleaseTool(sessionManager, sdkService);
    }

    // ==================== FhControlAcquireTool Tests ====================

    @Test
    void acquire_shouldReturnErrorWithoutSession() {
        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void acquire_shouldSucceedWithoutConfirm() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"),
                "无需 confirm，直接执行成功");
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    @SuppressWarnings("unchecked")
    void acquire_shouldSucceedInMockMode() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(true, data.get("granted"));

        // 验证 session 状态已更新
        assertTrue(session.hasControlOf(DRONE_SN));

        // 验证包含 warning 和 next_steps
        assertNotNull(result.get("warning"));
        assertNotNull(result.get("next_steps"));
    }

    @Test
    void acquire_shouldReturnError_whenAlreadyControllingAnother() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        // 先获取另一台设备的控制权
        session.setActiveControlDevice(ANOTHER_DRONE_SN);

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Already controlling device: " + ANOTHER_DRONE_SN));
    }

    @Test
    void acquire_shouldReturnError_whenAlreadyControllingThisDrone() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        // 模拟已持有同一设备的控制权
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Already holding control of " + DRONE_SN));
    }

    @Test
    void acquire_shouldReturnErrorWhenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = acquireTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    // ==================== FhControlReleaseTool Tests ====================

    @Test
    @SuppressWarnings("unchecked")
    void release_shouldSucceedInMockMode() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        // 先设置控制权
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(true, data.get("released"));

        // 验证 session 状态已清除
        assertFalse(session.hasControlAuthority());
        assertEquals(FlightPhase.IDLE, session.getFlightPhase());
    }

    @Test
    void release_shouldReturnError_whenNotControlling() {
        sessionManager.createSession("uuid", "ws");
        // 未设置控制权，直接尝试释放

        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Not controlling drone: " + DRONE_SN));
    }

    @Test
    void release_shouldReturnError_whenDrcStillActive() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        // 设置控制权，同时设置 DRC 链路（模拟 DRC 未断开的场景）
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("DRC link still active"));
        assertTrue(result.get("suggestion").toString().contains("fh_drc_disconnect"));
    }

    @Test
    void release_shouldReturnErrorWithoutSession() {
        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ==================== Elicitation 人工确认测试 ====================

    @Test
    void acquire_shouldSucceed_whenElicitationBridgePresent() {
        // L0 操作无需确认，即使 ClientBridge 已设置也不触发 elicitation
        SessionContext session = sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"), "L0 操作无需确认，直接执行");
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    void acquire_shouldSucceed_evenWithElicitationDeclined() {
        // L0 操作无需确认，elicitation decline 不影响执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"),
                "L0 操作不触发 elicitation，decline 不影响执行");
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    void acquire_shouldSucceed_evenWithElicitationTimedOut() {
        // L0 操作无需确认，timeout 不影响执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    // ==================== getToolDefinition Tests ====================

    @Test
    void getToolDefinition_shouldHaveCorrectNameForAcquireTool() {
        Map<String, Object> def = acquireTool.getToolDefinition();

        assertEquals("fh_control_acquire", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void getToolDefinition_shouldHaveCorrectNameForReleaseTool() {
        Map<String, Object> def = releaseTool.getToolDefinition();

        assertEquals("fh_control_release", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }
}
