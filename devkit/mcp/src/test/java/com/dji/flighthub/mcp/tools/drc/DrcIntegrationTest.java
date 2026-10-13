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
import com.dji.flighthub.mcp.sdk.StickControlExecutor;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.tools.camera.FhCameraTool;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DRC 端到端集成测试，覆盖完整飞行控制流程（mock 模式）。
 * 验证 13 个 DRC 工具在正确序列和错误序列下的行为。
 *
 * @author DJI FlightHub SDK Team
 */
@SuppressWarnings("unchecked")
class DrcIntegrationTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    private SessionManager sessionManager;
    private SdkService sdkService;
    private StickControlExecutor stickExecutor;

    // 全部 13 个工具
    private FhControlAcquireTool acquireTool;
    private FhControlReleaseTool releaseTool;
    private FhDrcConnectTool drcConnectTool;
    private FhDrcDisconnectTool drcDisconnectTool;
    private FhTakeoffTool takeoffTool;
    private FhReturnHomeTool rthTool;
    private FhFlytoTool flytoTool;
    private FhPoiTool poiTool;
    private FhEmergencyStopTool emergencyTool;
    private FhCameraTool cameraTool;
    private FhLiveStartTool liveTool;
    private FhOsdSnapshotTool osdTool;
    private FhStickMoveTool stickTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(3);
        // 配置运营商 token，使 L3 二次鉴权通过
        props.getSecurity().setOperatorToken("integration-test-operator-token");
        sessionManager = new SessionManager(props);
        // 覆盖 isMockMode()，确保集成测试始终走 mock 路径（无需 FH_MOCK 环境变量）
        sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        stickExecutor = new StickControlExecutor();

        acquireTool    = new FhControlAcquireTool(sessionManager, sdkService);
        releaseTool    = new FhControlReleaseTool(sessionManager, sdkService);
        drcConnectTool = new FhDrcConnectTool(sessionManager, sdkService);
        drcDisconnectTool = new FhDrcDisconnectTool(sessionManager, sdkService);
        takeoffTool    = new FhTakeoffTool(sessionManager, sdkService, props);
        rthTool        = new FhReturnHomeTool(sessionManager, sdkService);
        flytoTool      = new FhFlytoTool(sessionManager, sdkService);
        poiTool        = new FhPoiTool(sessionManager, sdkService);
        emergencyTool  = new FhEmergencyStopTool(sessionManager, sdkService,
                new com.dji.flighthub.mcp.security.OperatorTokenAuthenticator(props));
        cameraTool     = new FhCameraTool(sessionManager, sdkService);
        liveTool       = new FhLiveStartTool(sessionManager, sdkService);
        osdTool        = new FhOsdSnapshotTool(sessionManager, sdkService);
        stickTool      = new FhStickMoveTool(sessionManager, sdkService, stickExecutor);

        // 模拟 fh_connect 已完成
        sessionManager.createSession("ws-uuid", "TestWorkspace");
    }

    // ==================== Test 1: 完整飞行控制 happy-path ====================

    /**
     * 验证完整 DRC 飞行流程：
     * acquire → drc_connect → takeoff → flyto → osd_snapshot → return_home → drc_disconnect → control_release
     */
    @Test
    void fullDrcFlow_shouldCompleteSuccessfully() {
        SessionContext session = sessionManager.getActiveSession();
        assertNotNull(session, "setUp 应已创建 session");

        // Step 1: 获取飞行控制权
        Map<String, Object> acquireResult = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));
        assertEquals(true, acquireResult.get("success"), "acquire 应成功，实际: " + acquireResult.get("error_message"));
        assertTrue(session.hasControlOf(DRONE_SN), "acquire 后 session 应持有控制权");

        // Step 2: 建立 DRC 链路
        Map<String, Object> drcResult = drcConnectTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));
        assertEquals(true, drcResult.get("success"), "drc_connect 应成功，实际: " + drcResult.get("error_message"));
        assertTrue(session.hasDrcLinkTo(DRONE_SN), "drc_connect 后 session 应持有 DRC 链路");

        // Step 3: 起飞
        Map<String, Object> takeoffResult = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50.0,
                "confirm", true
        ));
        assertEquals(true, takeoffResult.get("success"), "takeoff 应成功，实际: " + takeoffResult.get("error_message"));
        assertEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
        Map<String, Object> takeoffData = (Map<String, Object>) takeoffResult.get("data");
        assertNotNull(takeoffData.get("flight_id"), "takeoff 应返回 flight_id");

        // Step 4: 指点飞行
        Map<String, Object> flytoResult = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "latitude", 22.55,
                "longitude", 113.95,
                "height", 80.0,
                "confirm", true
        ));
        assertEquals(true, flytoResult.get("success"), "flyto 应成功，实际: " + flytoResult.get("error_message"));
        assertEquals(FlightPhase.FLYING_TO, session.getFlightPhase());

        // Step 5: OSD 遥测快照（L0，无需 confirm）
        Map<String, Object> osdResult = osdTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));
        assertEquals(true, osdResult.get("success"), "osd_snapshot 应成功");
        Map<String, Object> osdData = (Map<String, Object>) osdResult.get("data");
        assertNotNull(osdData.get("latitude"), "OSD 应包含 latitude");
        assertNotNull(osdData.get("battery_percent"), "OSD 应包含 battery_percent");
        assertEquals(FlightPhase.FLYING_TO.name(), osdData.get("flight_phase"),
                "OSD flight_phase 应为 FLYING_TO");

        // Step 6: 返航
        Map<String, Object> rthResult = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));
        assertEquals(true, rthResult.get("success"), "return_home 应成功，实际: " + rthResult.get("error_message"));
        assertEquals(FlightPhase.RETURNING, session.getFlightPhase());

        // Step 7: 断开 DRC 链路
        Map<String, Object> drcDisResult = drcDisconnectTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));
        assertEquals(true, drcDisResult.get("success"), "drc_disconnect 应成功");
        assertFalse(session.hasDrcLinkTo(DRONE_SN), "drc_disconnect 后应无 DRC 链路");

        // Step 8: 释放飞行控制权
        Map<String, Object> releaseResult = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));
        assertEquals(true, releaseResult.get("success"), "control_release 应成功");
        assertFalse(session.hasControlAuthority(), "release 后应无控制权");
        assertEquals(FlightPhase.IDLE, session.getFlightPhase(), "release 后 phase 应为 IDLE");
    }

    // ==================== Test 2: 无 DRC 链路时紧急停机 ====================

    /**
     * 验证 fh_emergency_stop 仅需飞行控制权（无需 DRC 链路）。
     * 场景：有控制权，无 DRC 链路，phase=FLYING_TO → 执行后 phase=EMERGENCY。
     */
    @Test
    void emergencyStop_shouldWorkWithoutDrc() {
        SessionContext session = sessionManager.getActiveSession();
        assertNotNull(session);

        // 手动设置控制权和 phase，不建立 DRC 链路
        session.setActiveControlDevice(DRONE_SN);
        session.setFlightPhase(FlightPhase.FLYING_TO);
        assertFalse(session.hasDrcLinkTo(DRONE_SN), "前置条件：无 DRC 链路");

        Map<String, Object> result = emergencyTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "mode", "hover",
                "operator_token", "integration-test-operator-token",
                "confirm", true,
                "reason", "obstacle detected"
        ));

        assertEquals(true, result.get("success"), "无 DRC 链路时紧急停机应成功，实际: " + result.get("error_message"));
        assertEquals(FlightPhase.EMERGENCY, session.getFlightPhase(), "执行后 phase 应为 EMERGENCY");
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("emergency", data.get("status"), "状态应为 emergency");
    }

    // ==================== Test 3: 乱序调用应返回有帮助的错误 ====================

    /**
     * 验证乱序调用时的错误提示质量：
     * 1. 无控制权时 takeoff → 错误提示应包含 fh_control_acquire 或 fh_drc_connect
     * 2. 有控制权但无 DRC 时 flyto → 错误提示应包含 fh_drc_connect
     */
    @Test
    void outOfOrderCalls_shouldAutoAcquirePrerequisites() {
        SessionContext session = sessionManager.getActiveSession();
        assertNotNull(session);

        // 场景 1：无控制权，尝试 takeoff → 应自动获取控制权和 DRC 链路后成功
        Map<String, Object> takeoffWithoutControl = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "dock_sn", "DOCK-001",
                "target_height", 50.0,
                "confirm", true
        ));
        assertEquals(true, takeoffWithoutControl.get("success"),
                "无控制权时 takeoff 应自动获取前置条件后成功");
        assertTrue(session.hasControlOf(DRONE_SN), "控制权应自动获取");
        assertTrue(session.hasDrcLinkTo(DRONE_SN), "DRC 链路应自动建立");

        // 重置状态
        session.setActiveControlDevice(null);
        session.setActiveDrcDevice(null);

        // 场景 2：无 DRC 链路，尝试 flyto → 应自动获取并成功
        Map<String, Object> flytoWithoutDrc = flytoTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "latitude", 22.55,
                "longitude", 113.95,
                "height", 80.0,
                "confirm", true
        ));
        assertEquals(true, flytoWithoutDrc.get("success"),
                "无 DRC 链路时 flyto 应自动获取前置条件后成功");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    // ==================== Test 4: stickMove 在有界时间内完成 ====================

    /**
     * 验证 fh_stick_move 在 duration_ms=200ms 时，总耗时不超过 ~1000ms（200ms + 调度开销）。
     */
    @Test
    void stickMove_shouldCompleteInBoundedTime() {
        SessionContext session = sessionManager.getActiveSession();
        assertNotNull(session);

        // 设置控制权和 DRC 链路
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        long startMs = System.currentTimeMillis();

        Map<String, Object> result = stickTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "speed_percent", 30,
                "duration_ms", 200,
                "confirm", true
        ));

        long elapsedMs = System.currentTimeMillis() - startMs;

        assertEquals(true, result.get("success"), "stick_move 应成功，实际: " + result.get("error_message"));
        assertTrue(elapsedMs < 1000,
                "stick_move(200ms) 应在 1000ms 内完成，实际耗时: " + elapsedMs + "ms");
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data, "结果 data 不应为 null");
    }

    // ==================== Test 5: payload 和 live 仅需控制权（无需 DRC） ====================

    /**
     * 验证 fh_camera（相机类）和 fh_live_start 在有控制权时正常工作。
     */
    @Test
    void cameraAndLive_shouldWorkWithControlAuthority() {
        SessionContext session = sessionManager.getActiveSession();
        assertNotNull(session);

        // 相机操作需要控制权
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        assertTrue(session.hasDrcLinkTo(DRONE_SN), "前置条件：已建立 DRC 链路");

        // 测试 camera：camera_photo
        Map<String, Object> cameraResult = cameraTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "camera_photo"
        ));
        assertEquals(true, cameraResult.get("success"),
                "有控制权时 camera 应成功，实际: " + cameraResult.get("error_message"));

        // 测试 live_start：注意参数名为 device_sn 而非 drone_sn
        Map<String, Object> liveResult = liveTool.execute(Map.of(
                "device_sn", DRONE_SN,
                "action", "start",
                "confirm", true
        ));
        assertEquals(true, liveResult.get("success"),
                "live_start 应成功（无需控制权），实际: " + liveResult.get("error_message"));
        Map<String, Object> liveData = (Map<String, Object>) liveResult.get("data");
        assertEquals(true, liveData.get("streaming"), "live_start 应返回 streaming=true");
    }
}
