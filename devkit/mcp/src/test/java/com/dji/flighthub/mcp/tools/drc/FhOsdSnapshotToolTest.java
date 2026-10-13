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
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhOsdSnapshotTool 单元测试。
 * 覆盖 session 校验、drone_sn 校验及 mock 模式遥测数据正确性验证（含飞行阶段分支）。
 * 本工具为 L0 只读操作，无需 confirm 参数。
 *
 * @author DJI FlightHub SDK Team
 */
class FhOsdSnapshotToolTest {

    private static final String DRONE_SN = "DRONE-OSD-001";

    private SessionManager sessionManager;
    private FhOsdSnapshotTool osdTool;

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
        osdTool = new FhOsdSnapshotTool(sessionManager, sdkService);
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = osdTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = osdTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnTelemetry_whenMockMode() {
        // 验证核心遥测字段存在且数值符合预期（固定 mock 常量）
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = osdTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));

        // 位置信息
        assertNotNull(data.get("latitude"));
        assertNotNull(data.get("longitude"));
        assertNotNull(data.get("height"));

        // 电量
        assertEquals(82, data.get("battery_percent"));

        // 飞行阶段（默认 IDLE）
        assertEquals("IDLE", data.get("flight_phase"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReflectFlightPhase_whenPhaseIsFlying() {
        // 飞行中（FLYING_TO）时 horizontal_speed 应大于 0
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.FLYING_TO);

        Map<String, Object> result = osdTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("FLYING_TO", data.get("flight_phase"));

        double horizontalSpeed = ((Number) data.get("horizontal_speed")).doubleValue();
        assertTrue(horizontalSpeed > 0,
                "飞行中的 horizontal_speed 应大于 0，实际值: " + horizontalSpeed);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = osdTool.getToolDefinition();

        assertEquals("fh_osd_snapshot", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotCorrectPhase_whenTakingOffWithVerticalSpeed() {
        // TAKING_OFF 阶段且 vertical_speed > 0（mock 返回 2.0），不应修正
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.TAKING_OFF);

        Map<String, Object> result = osdTool.execute(Map.of("drone_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        // mock 模式 TAKING_OFF 时 vertical_speed=2.0（仍在上升），不修正
        assertEquals("TAKING_OFF", data.get("flight_phase"));
        assertEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotCorrectPhase_whenHovering() {
        // 非 TAKING_OFF 阶段不应触发修正
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.HOVERING);

        Map<String, Object> result = osdTool.execute(Map.of("drone_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("HOVERING", data.get("flight_phase"));
    }

    @Test
    void correctFlightPhase_shouldCorrectToHovering_whenSpeedNearZero() {
        // 直接测试修正逻辑：TAKING_OFF + 速度≈0 → 应修正为 HOVERING
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.TAKING_OFF);

        FlightPhase result = osdTool.correctFlightPhase(session, 0.1, 0.05);

        assertEquals(FlightPhase.HOVERING, result);
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    void correctFlightPhase_shouldNotCorrect_whenVerticalSpeedHigh() {
        // TAKING_OFF + vertical_speed 超过阈值 → 不修正
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.TAKING_OFF);

        FlightPhase result = osdTool.correctFlightPhase(session, 0.0, 1.5);

        assertEquals(FlightPhase.TAKING_OFF, result);
        assertEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
    }

    @Test
    void correctFlightPhase_shouldNotCorrect_whenNotTakingOff() {
        // 非 TAKING_OFF 阶段，即使速度为 0 也不修正
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.FLYING_TO);

        FlightPhase result = osdTool.correctFlightPhase(session, 0.0, 0.0);

        assertEquals(FlightPhase.FLYING_TO, result);
    }
}
