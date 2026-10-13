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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhPoiTool 单元测试。
 * 覆盖 session 校验、参数校验、mock 模式下的距离前置校验和正常环绕流程。
 *
 * @author DJI FlightHub SDK Team
 */
class FhPoiToolTest {

    private static final String DRONE_SN = "DRONE-POI-001";

    /** Mock 无人机坐标（与 FhPoiTool.MOCK_DRONE_LATITUDE/LONGITUDE 一致） */
    private static final double MOCK_DRONE_LAT = 22.5431;
    private static final double MOCK_DRONE_LNG = 113.9465;

    private SessionManager sessionManager;
    private FhPoiTool poiTool;

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
        poiTool = new FhPoiTool(sessionManager, sdkService);
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = poiTool.getToolDefinition();

        assertEquals("fh_poi", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnError_whenDroneTooCloseToPoiCenter() {
        // 使用 mock 无人机坐标作为 POI 中心 → 距离 ≈ 0，应被拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", MOCK_DRONE_LAT);
        args.put("longitude", MOCK_DRONE_LNG);
        args.put("height", 50.0);
        args.put("radius", 30.0);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("too close to POI center"),
                "应提示无人机距 POI 中心过近");
        assertTrue(result.get("suggestion").toString().contains("fh_fly_to"),
                "应建议使用 fh_fly_to 重新定位");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenDroneFarFromPoiCenter() {
        // POI 中心距无人机约 1km 远，半径 30m → 应通过距离校验
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", MOCK_DRONE_LAT + 0.01);
        args.put("longitude", MOCK_DRONE_LNG + 0.01);
        args.put("height", 50.0);
        args.put("radius", 30.0);
        args.put("speed", 5.0);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("start", data.get("action"));
        assertEquals(FlightPhase.ORBITING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopOrbit_whenActionIsStop() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        session.setFlightPhase(FlightPhase.ORBITING);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "stop");
        args.put("confirm", true);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("stop", data.get("action"));
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnStatus_withoutDrcLink() {
        // status 查询不需要 DRC 链路和 confirm
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.ORBITING);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "status");
        args.put("confirm", false);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("active"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidRadius() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", 22.0);
        args.put("longitude", 114.0);
        args.put("height", 50.0);
        args.put("radius", 3.0);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid radius"));
    }

    @Test
    void execute_shouldReturnError_whenNotConfirmed() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", false);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }
}
