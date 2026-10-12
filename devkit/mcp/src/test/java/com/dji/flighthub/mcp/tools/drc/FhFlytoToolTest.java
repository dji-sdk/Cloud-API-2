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
 * FhFlytoTool 和 FhPoiTool 单元测试。
 * 覆盖 DRC 链路前置条件、参数校验、飞行阶段状态转换及 mock 模式正常流程。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhFlytoToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    /** 合法的测试纬度（深圳） */
    private static final double VALID_LAT = 22.5431;

    /** 合法的测试经度（深圳） */
    private static final double VALID_LNG = 114.0579;

    /** 合法的测试高度（米） */
    private static final double VALID_HEIGHT = 50.0;

    /** 合法的测试环绕半径（米） */
    private static final double VALID_RADIUS = 30.0;

    private SessionManager sessionManager;
    private FhFlytoTool flytoTool;
    private FhPoiTool poiTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // 覆盖 isMockMode()，确保单元测试始终走 mock 路径（无需 FH_MOCK 环境变量）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        flytoTool = new FhFlytoTool(sessionManager, sdkService);
        poiTool = new FhPoiTool(sessionManager, sdkService);
    }

    // ==================== FhFlytoTool Tests ====================

    @Test
    void flyto_shouldReturnError_whenInvalidCoordinates() {
        // 建立 session 并配置 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // lat=200 超过最大值 90，应返回坐标校验错误
        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", 200.0,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid latitude"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void flyto_start_shouldSetFlyingToPhase_whenValidInput() {
        // 完整前置条件：控制权 + DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertNotNull(data.get("flyto_id"), "flyto_id 不应为 null");

        Map<String, Object> target = (Map<String, Object>) data.get("target");
        assertNotNull(target);
        assertEquals(VALID_LAT, target.get("latitude"));
        assertEquals(VALID_LNG, target.get("longitude"));
        assertEquals(VALID_HEIGHT, target.get("height"));

        // 验证 session 飞行阶段已更新为 FLYING_TO
        assertEquals(FlightPhase.FLYING_TO, session.getFlightPhase());
        // 验证 activeFlyToId 已设置
        assertNotNull(session.getActiveFlyToId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void flyto_stop_shouldResetPhaseToHovering_whenStopAction() {
        // 建立 session 并配置 DRC 链路，先设置为指点飞行阶段
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        session.setActiveFlyToId("flyto_prev");
        session.setFlightPhase(FlightPhase.FLYING_TO);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "stop",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("hovering", data.get("status"));

        // 验证 session 飞行阶段已重置为 HOVERING
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
        // 验证 activeFlyToId 已清除
        assertNull(session.getActiveFlyToId());
    }

    @Test
    void flyto_shouldAutoAcquireDrcLink_whenNotEstablished() {
        // 建立 session，不设置 DRC 链路 → 应自动获取控制权 + 连接 DRC
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(true, result.get("success"),
                "DRC 链路应自动建立，无需额外确认");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    void flyto_shouldReturnError_whenNoSession() {
        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void flyto_shouldReturnError_whenHeightExceedsMax() {
        // height=20000 超过上限 10000，应返回高度校验错误（I2/I3 fix）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", 20000.0
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid height"));
    }

    @Test
    void flyto_shouldReturnError_whenInvalidAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void flyto_update_shouldUpdateFlyingTarget_whenUpdateAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        session.setFlightPhase(FlightPhase.FLYING_TO);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "update",
                "confirm", true,
                "latitude", 23.0,
                "longitude", 115.0,
                "height", 80.0
        ));

        assertEquals(true, result.get("success"));
        assertEquals(FlightPhase.FLYING_TO, session.getFlightPhase());
    }

    @Test
    void getToolDefinition_flyto_shouldHaveCorrectName() {
        Map<String, Object> def = flytoTool.getToolDefinition();

        assertEquals("fh_flyto", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    // ==================== FhFlytoTool Elicitation Tests ====================

    @Test
    void flyto_shouldExecute_whenElicitationAccepted() {
        // client 支持 elicitation 且真人 ACCEPT → 即使 confirm=false 也执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", false,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(true, result.get("success"), "真人接受后应执行，不依赖 confirm 参数");
        assertEquals(FlightPhase.FLYING_TO, session.getFlightPhase());
    }

    @Test
    void flyto_shouldBlock_whenElicitationDeclined() {
        // client 支持 elicitation 但真人 DECLINE → 即使 confirm=true 也拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(false, result.get("success"), "真人拒绝后必须拦截");
        assertTrue(result.get("error_message").toString().contains("declined"));
        assertNotEquals(FlightPhase.FLYING_TO, session.getFlightPhase());
    }

    @Test
    void flyto_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void flyto_shouldUseElicitedCoords_whenCoordsMissing() {
        // 缺坐标 + client 返回补输入的坐标 → 用真人填的值执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        // ACCEPT 既满足确认闸门，也提供坐标补输入
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(Map.of(
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT
        ))));

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true
                // 故意不传 latitude/longitude/height
        ));

        assertEquals(true, result.get("success"), "缺坐标应弹表单补输入后成功");

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> target = (Map<String, Object>) data.get("target");
        assertEquals(VALID_LAT, target.get("latitude"));
        assertEquals(VALID_LNG, target.get("longitude"));
        assertEquals(VALID_HEIGHT, target.get("height"));
        assertEquals(FlightPhase.FLYING_TO, session.getFlightPhase());
    }

    @Test
    void flyto_shouldReturnCoordError_whenNoBridgeAndCoordsMissing() {
        // 无 bridge（回退分支）+ 缺坐标 → 落到既有坐标校验报错，行为不变
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true
                // 缺坐标且无 bridge
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid latitude"));
    }

    // ==================== Flyto Progress Tests ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnIdle_whenNoActiveFlyTo() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "progress"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("progress", data.get("action"));
        assertEquals(false, data.get("active"));
        assertEquals("idle", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnProgress_whenFlyToActive() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        session.setActiveFlyToId("flyto_12345");
        session.setFlightPhase(com.dji.flighthub.mcp.session.FlightPhase.FLYING_TO);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "progress"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("progress", data.get("action"));
        assertEquals(true, data.get("active"));
        assertEquals("in_progress", data.get("status"));
        assertEquals("flyto_12345", data.get("flyto_id"));
        assertNotNull(data.get("progress_percent"));
        assertNotNull(data.get("remaining_distance_m"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void execute_progress_shouldNotRequireConfirm() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "progress"
        ));

        assertEquals(true, result.get("success"));
    }

    // ==================== FhPoiTool Tests ====================

    @Test
    void poi_shouldReturnError_whenRadiusExceedsMax() {
        // 建立 session 并配置 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // radius=1000 超过最大值 500，应返回半径校验错误
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT,
                "radius", 1000.0
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid radius"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void poi_start_shouldSetOrbitingPhase_whenValidInput() {
        // 完整前置条件：控制权 + DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT,
                "radius", VALID_RADIUS
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertNotNull(data.get("orbit_id"), "orbit_id 不应为 null");

        Map<String, Object> poi = (Map<String, Object>) data.get("poi");
        assertNotNull(poi);
        assertEquals(VALID_LAT, poi.get("latitude"));
        assertEquals(VALID_LNG, poi.get("longitude"));
        assertEquals(VALID_HEIGHT, poi.get("height"));

        assertEquals(VALID_RADIUS, data.get("radius"));
        assertNotNull(data.get("speed"));
        assertEquals("clockwise", data.get("direction"));

        // 验证 session 飞行阶段已更新为 ORBITING
        assertEquals(FlightPhase.ORBITING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void poi_stop_shouldSetHoveringPhase_whenStopAction() {
        // 建立 session 并配置 DRC 链路，先设置为环绕飞行阶段
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        session.setFlightPhase(FlightPhase.ORBITING);

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "stop",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("hovering", data.get("status"));

        // 验证 session 飞行阶段已重置为 HOVERING
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    void poi_shouldReturnError_whenNoSession() {
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT,
                "radius", VALID_RADIUS
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void poi_shouldAutoAcquireDrcLink_whenNotEstablished() {
        // 建立 session，不设置 DRC 链路 → 应自动获取控制权 + 连接 DRC
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT,
                "radius", VALID_RADIUS
        ));

        assertEquals(true, result.get("success"),
                "DRC 链路应自动建立，无需额外确认");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    @Test
    void poi_shouldReturnError_whenRadiusBelowMin() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // radius=2 低于最小值 5，应返回半径校验错误
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT,
                "radius", 2.0
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid radius"));
    }

    @Test
    void poi_shouldReturnError_whenSpeedExceedsMax() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // speed=20 超过最大值 15，应返回速度校验错误
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true,
                "latitude", VALID_LAT,
                "longitude", VALID_LNG,
                "height", VALID_HEIGHT,
                "radius", VALID_RADIUS,
                "speed", 20.0
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid speed"));
    }

    @Test
    void getToolDefinition_poi_shouldHaveCorrectName() {
        Map<String, Object> def = poiTool.getToolDefinition();

        assertEquals("fh_poi", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    // ── POI status 测试 ──

    @Test
    @SuppressWarnings("unchecked")
    void poi_status_shouldReturnIdle_whenNotOrbiting() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.HOVERING);

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("status", data.get("action"));
        assertEquals(false, data.get("active"));
        assertEquals("idle", data.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void poi_status_shouldReturnActive_whenOrbiting() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.ORBITING);

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("status", data.get("action"));
        assertEquals(true, data.get("active"));
        assertEquals("orbiting", data.get("status"));
        assertEquals(true, data.get("_mock"));
        assertNotNull(data.get("current_angle_deg"));
        assertNotNull(data.get("laps_completed"));
    }

    @Test
    void poi_status_shouldNotRequireConfirm() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "status"
        ));

        assertEquals(true, result.get("success"), "status action should not require confirm");
    }
}
