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
 * FhTakeoffTool 和 FhReturnHomeTool 单元测试。
 * 覆盖 session 校验、DRC/控制权前置条件、参数校验及 mock 模式正常流程。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhTakeoffToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    private SessionManager sessionManager;
    private FhTakeoffTool takeoffTool;
    private FhReturnHomeTool rthTool;

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
        takeoffTool = new FhTakeoffTool(sessionManager, sdkService, properties);
        rthTool = new FhReturnHomeTool(sessionManager, sdkService);
    }

    // ==================== FhTakeoffTool Tests ====================

    @Test
    void takeoff_shouldReturnError_whenNoSession() {
        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void takeoff_shouldAutoAcquireDrcLink_whenNotEstablished() {
        // 建立 session，设置控制权但不设置 DRC 链路 → 应自动连接 DRC
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "dock_sn", "DOCK-001",
                "target_height", 50,
                "confirm", true
        ));

        assertEquals(true, result.get("success"),
                "DRC 链路应自动建立，无需额外确认");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
    }

    @Test
    void takeoff_shouldReturnError_whenTargetHeightBelowMin() {
        // 建立 session，设置控制权和 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // target_height=0.5 低于最小值 2
        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 0.5,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("target_height"));
    }

    @Test
    void takeoff_shouldReturnError_whenTargetHeightExceedsMax() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // target_height=99999 超过最大值 10000
        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 99999,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("target_height"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldSetTakingOffPhase_whenMockModeAndValidInput() {
        // 完整前置条件：控制权 + DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 80,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertNotNull(data.get("flight_id"), "flight_id 不应为 null");
        assertEquals(80.0, data.get("target_height"));
        assertEquals("taking_off", data.get("status"));

        // 验证 session 飞行阶段已更新
        assertEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
        // 验证 activeFlightId 已设置
        assertNotNull(session.getActiveFlightId());
    }

    @Test
    void takeoff_shouldReturnError_whenConfirmFalse() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", false
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void takeoff_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "target_height", 50,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void takeoff_shouldReturnError_whenInvalidOutOfControlAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", true,
                "out_of_control_action", "self_destruct"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid out_of_control_action"));
    }

    // ==================== FhTakeoffTool Elicitation 测试 ====================

    @Test
    void takeoff_shouldExecute_whenElicitationAccepted() {
        // 真人 ACCEPT → 即使 confirm=false 也应执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", false
        ));

        assertEquals(true, result.get("success"), "真人接受后应执行，不依赖 confirm 参数");
        assertEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
    }

    @Test
    void takeoff_shouldBlock_whenElicitationDeclined() {
        // 真人 DECLINE → 即使 confirm=true 也必须拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", true
        ));

        assertEquals(false, result.get("success"), "真人拒绝后必须拦截");
        assertTrue(result.get("error_message").toString().contains("declined"));
        assertNotEquals(FlightPhase.TAKING_OFF, session.getFlightPhase());
    }

    @Test
    void takeoff_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    void getToolDefinition_takeoff_shouldHaveCorrectName() {
        Map<String, Object> def = takeoffTool.getToolDefinition();

        assertEquals("fh_takeoff", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldReturnDryRunPreview_whenDryRunTrue() {
        // 完整前置条件：控制权 + DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", true,
                "dry_run", true
        ));

        // 应返回成功的 dry_run 预览，而非实际执行
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        assertNotNull(result.get("description"));
        assertTrue(result.get("description").toString().contains("Would take off"));
        assertNotNull(result.get("would_execute"));
        assertTrue(result.get("message").toString().contains("Dry run"));

        // 验证 session 飞行阶段未改变（dry_run 不应修改状态）
        assertNull(session.getActiveFlightId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldReturnDryRunPreview_withoutConfirm() {
        // dry_run 不应要求 confirm=true（Fix #2：无需确认即可预览）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "dry_run", true
                // 注意：没有传 confirm 参数
        ));

        // 即使没有 confirm=true，dry_run 也应正常返回预览
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        assertTrue(result.get("description").toString().contains("Would take off"));

        // 验证 would_execute 中的 security_level 使用枚举 name()
        Map<String, Object> wouldExecute = (Map<String, Object>) result.get("would_execute");
        assertEquals("FLIGHT", wouldExecute.get("security_level"));
    }

    @Test
    void takeoff_shouldHandleStringDryRun() {
        // Fix #1：getBooleanArg 应支持字符串 "true"
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "dry_run", "true"  // 字符串 "true" 而非 Boolean
        ));

        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
    }

    @Test
    void takeoff_shouldStillValidateParams_whenDryRunTrue() {
        // dry_run 模式仍应做参数校验
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // target_height 超出范围，即使 dry_run=true 也应报错
        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", -1,
                "confirm", true,
                "dry_run", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("target_height"));
    }

    @Test
    void takeoff_shouldReturnError_whenRealSdkMode_andDockSnMissing() {
        // 真实 SDK 模式下 dock_sn 必填（用于获取机场 GPS 坐标）
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(3);
        SessionManager sm = new SessionManager(props);
        // 创建 isMockMode() == false 的 SdkService
        SdkService realModeService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }
        };
        FhTakeoffTool realModeTool = new FhTakeoffTool(sm, realModeService, props);

        SessionContext session = sm.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        // 不传 dock_sn
        Map<String, Object> result = realModeTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 50,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: dock_sn"));
    }

    // ==================== Cockpit URL 测试 ====================

    /**
     * 创建配置了 cockpit URL 的 FhTakeoffTool 实例（消除测试重复代码）。
     *
     * @param cockpitUrl cockpit 基础 URL
     * @return 已配置好 session + 控制权 + DRC 的 tool 实例
     */
    private FhTakeoffTool createToolWithCockpitUrl(String cockpitUrl) {
        FlightHubProperties props = new FlightHubProperties();
        props.getSecurity().setMaxLevel(3);
        props.getCockpit().setUrl(cockpitUrl);
        SessionManager sm = new SessionManager(props);
        SdkService mockSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhTakeoffTool tool = new FhTakeoffTool(sm, mockSdk, props);

        SessionContext session = sm.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        return tool;
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldIncludeCockpitUrl_whenCockpitUrlConfigured() {
        FhTakeoffTool tool = createToolWithCockpitUrl("http://localhost:5177");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "dock_sn", "DOCK-001",
                "target_height", 80,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("http://localhost:5177/cockpit?gateway_sn=DOCK-001&drone_sn=" + DRONE_SN,
                data.get("cockpit_url"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldNotIncludeCockpitUrl_whenCockpitUrlNotConfigured() {
        // 默认配置（cockpit URL 为空）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = takeoffTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "dock_sn", "DOCK-001",
                "target_height", 80,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNull(data.get("cockpit_url"), "cockpit_url 未配置时不应出现在返回中");
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldNotIncludeCockpitUrl_whenDockSnMissing() {
        FhTakeoffTool tool = createToolWithCockpitUrl("http://localhost:5177");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "target_height", 80,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNull(data.get("cockpit_url"), "dock_sn 缺失时不应生成 cockpit_url");
    }

    @Test
    @SuppressWarnings("unchecked")
    void takeoff_shouldTrimTrailingSlash_whenCockpitUrlHasTrailingSlash() {
        FhTakeoffTool tool = createToolWithCockpitUrl("http://localhost:5177/");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "dock_sn", "DOCK-002",
                "target_height", 80,
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        String url = (String) data.get("cockpit_url");
        assertNotNull(url);
        assertFalse(url.contains("//cockpit"), "不应出现双斜杠");
        assertTrue(url.startsWith("http://localhost:5177/cockpit?"));
    }

    // ==================== FhReturnHomeTool Tests ====================

    @Test
    void rth_shouldReturnError_whenNoSession() {
        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void rth_shouldAutoAcquireControl_whenNoControlAuthority() {
        // 建立 session 但不设置控制权 → 应自动获取
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));

        assertEquals(true, result.get("success"),
                "控制权应自动获取，无需额外确认");
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rth_shouldSetReturningPhase_whenStartAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
                // action 默认为 "start"
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("start", data.get("action"));
        assertEquals("returning", data.get("status"));

        // 验证 session 飞行阶段为 RETURNING
        assertEquals(FlightPhase.RETURNING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rth_cancel_shouldSetHoveringPhase_whenCancelAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        // 先设置为返航阶段，模拟正在返航
        session.setFlightPhase(FlightPhase.RETURNING);

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true,
                "action", "cancel"
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("cancel", data.get("action"));
        assertEquals("hovering", data.get("status"));

        // 验证 session 飞行阶段为 HOVERING
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    void rth_shouldReturnError_whenConfirmFalse() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", false
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void rth_shouldReturnError_whenInvalidAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true,
                "action", "invalid_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    // ==================== FhReturnHomeTool Elicitation 测试 ====================

    @Test
    void rth_shouldExecute_whenElicitationAccepted() {
        // 真人 ACCEPT → 即使 confirm=false 也应执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", false
        ));

        assertEquals(true, result.get("success"), "真人接受后应执行，不依赖 confirm 参数");
        assertEquals(FlightPhase.RETURNING, session.getFlightPhase());
    }

    @Test
    void rth_shouldBlock_whenElicitationDeclined() {
        // 真人 DECLINE → 即使 confirm=true 也必须拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));

        assertEquals(false, result.get("success"), "真人拒绝后必须拦截");
        assertTrue(result.get("error_message").toString().contains("declined"));
    }

    @Test
    void rth_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = rthTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    void getToolDefinition_rth_shouldHaveCorrectName() {
        Map<String, Object> def = rthTool.getToolDefinition();

        assertEquals("fh_return_home", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }
}
