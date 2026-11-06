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
import com.dji.flighthub.mcp.security.OperatorTokenAuthenticator;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.FlightHubCallback.ResultCallbackWithError;
import com.dji.flighthubsdk.bean.FlightHubError;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.FlightControlModule;
import com.dji.flighthubsdk.generated.FlightHubSdkErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FhEmergencyStopTool 单元测试。
 * 覆盖 session 校验、控制权前置条件（无需 DRC）、confirm 校验、
 * reason 必填校验及 mock 模式正常流程（EMERGENCY 阶段转换）。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhEmergencyStopToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    /** 测试用运营商 token（L3 二次鉴权，AI 不可代填） */
    private static final String OPERATOR_TOKEN = "test-operator-token-001";

    private SessionManager sessionManager;
    private FhEmergencyStopTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        // 默认启用运营商鉴权（未配置时 L3 应被拒绝，见 execute_shouldBlock_whenOperatorTokenNotConfigured）
        properties.getSecurity().setOperatorToken(OPERATOR_TOKEN);
        sessionManager = new SessionManager(properties);

        // 覆盖 isMockMode()，确保单元测试始终走 mock 路径（无需 FH_MOCK 环境变量）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        tool = new FhEmergencyStopTool(sessionManager, sdkService,
                new OperatorTokenAuthenticator(properties));
    }

    /**
     * 执行紧急停止的便捷方法：填充运营商 token + confirm。
     *
     * @param token 运营商 token（null 表示缺失）
     * @return 执行结果
     */
    private Map<String, Object> executeWithOperatorToken(String token) {
        return tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", token,
                "confirm", true,
                "reason", "Battery critical"
        ));
    }

    @Test
    void execute_shouldReturnError_whenReasonBlank() {
        // reason 为空字符串时，应返回"requires a reason"错误
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", ""
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("requires a reason"));
    }

    @Test
    void execute_shouldReturnError_whenNoControlAuthority() {
        // 建立 session 但不设置控制权，应返回控制权错误
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Battery critical"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No control authority for " + DRONE_SN));
        assertTrue(result.get("suggestion").toString().contains("fh_control_acquire"));
    }

    @Test
    void execute_shouldSucceed_whenNoDrcLink() {
        // 持有控制权但未建立 DRC 链路 — 本工具故意不做 DRC 前置校验，
        // 链路缺失交由 SDK 返回错误，避免紧急场景被本地前置条件拦下
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        // 故意不设置 DRC：session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Lost visual contact"
        ));

        assertEquals(true, result.get("success"),
                "Emergency action should not be blocked locally when DRC link is absent");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSetEmergencyPhase_whenSucceeds() {
        // mock 模式成功路径 — session 飞行阶段应转换为 EMERGENCY
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Obstacle imminent"
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("hover", data.get("mode"));
        assertEquals("Obstacle imminent", data.get("reason"));
        assertEquals("emergency", data.get("status"));
        assertEquals(true, data.get("_mock"));

        // 验证 session 飞行阶段已转换为 EMERGENCY
        assertEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
    }

    @Test
    void execute_shouldBlock_whenOperatorTokenMissing() {
        // 未提供 operator_token（L3 二次鉴权缺失）→ 必须拒绝，即使 confirm=true
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true,
                "reason", "Obstacle imminent"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Operator token"),
                "缺失 operator token 应返回二次鉴权错误");
        assertNotEquals(FlightPhase.EMERGENCY, session.getFlightPhase(),
                "鉴权失败不得转换飞行阶段");
    }

    @Test
    void execute_shouldBlock_whenOperatorTokenWrong() {
        // 提供错误 operator_token → 必须拒绝
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = executeWithOperatorToken("wrong-token");

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Operator token"));
        assertNotEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
    }

    @Test
    void execute_shouldBlock_whenOperatorTokenNotConfigured() {
        // 未配置 FH_OPERATOR_TOKEN 时 L3 一律拒绝（fail-safe：宁可不可用，不可裸奔）
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        // 不设置 operator token
        SessionManager sessionManagerNoOp = new SessionManager(properties);
        FhEmergencyStopTool toolNoOp = new FhEmergencyStopTool(sessionManagerNoOp,
                new SdkService() {
                    @Override
                    public boolean isMockMode() {
                        return true;
                    }
                },
                new OperatorTokenAuthenticator(properties));

        SessionContext session = sessionManagerNoOp.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = toolNoOp.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", "anything",
                "confirm", true,
                "reason", "Test"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Operator token"),
                "未配置 operator token 时必须拒绝 L3 执行");
        assertNotEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
    }

    @Test
    void execute_shouldBlock_whenOperatorTokenMissingEvenWithElicitationAccept() {
        // 即使 elicitation 可用且真人 ACCEPT，operator token 仍必须校验（二次独立凭据）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", false,
                "reason", "Popup accepted but operator token absent"
        ));

        assertEquals(false, result.get("success"),
                "elicitation 通过不能替代 operator token 校验");
        assertTrue(result.get("error_message").toString().contains("Operator token"));
        assertNotEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
    }

    @Test
    void dryRun_shouldNotRequireOperatorToken() {
        // dry_run 预览不应要求 operator token（无需真实二次鉴权）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true,
                "reason", "Preview only",
                "dry_run", true
        ));

        assertEquals(true, result.get("success"),
                "dry_run 预览不应要求 operator token");
        assertTrue(result.get("description").toString().contains("Would execute"),
                "预览应描述将执行的操作");
    }

    @Test
    void execute_shouldReturnError_whenConfirmFalse() {
        // confirm=false 时，应返回确认错误
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", false,
                "reason", "Test reason"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        // 无 session 时，应返回 no session 错误
        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "confirm", true,
                "reason", "Test"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenLandingMode() {
        // mode=landing 时，应正常执行并返回成功
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Mission complete, force land",
                "mode", "landing"
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("landing", data.get("mode"));
        // landing 与 force_landing 同为降落语义，phase/status 必须一致
        assertEquals("landing", data.get("status"));
        assertEquals(FlightPhase.LANDING, session.getFlightPhase());
    }

    @Test
    void execute_shouldReturnError_whenInvalidMode() {
        // 非法 mode 值应返回错误
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Test",
                "mode", "explode"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid mode"));
    }

    // ==================== 真实 SDK 路径：mode 分派 ====================

    /**
     * 构造走真实 SDK 路径的工具实例，SDK 回调立即返回成功。
     *
     * @param module 被 mock 的 FlightControlModule
     * @return 配置为真实模式的工具
     */
    private FhEmergencyStopTool realToolWith(FlightControlModule module) {
        Drone drone = mock(Drone.class);
        when(drone.getFlightControlModule()).thenReturn(module);
        SdkService realSdk = new SdkService() {
            @Override
            public boolean isMockMode() {
                return false;
            }

            @Override
            public Drone getDrone(String sn) {
                return drone;
            }
        };
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        properties.getSecurity().setOperatorToken(OPERATOR_TOKEN);
        return new FhEmergencyStopTool(sessionManager, realSdk, new OperatorTokenAuthenticator(properties));
    }

    /**
     * 让 mock 的 SDK 方法以成功回调应答。
     *
     * @param module 被 mock 的 FlightControlModule
     */
    private void stubSuccessCallbacks(FlightControlModule module) {
        doAnswer(inv -> {
            ResultCallbackWithError cb = inv.getArgument(0);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(module).drcEmergencyStop(any(ResultCallbackWithError.class));
        doAnswer(inv -> {
            ResultCallbackWithError cb = inv.getArgument(0);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(module).drcEmergencyLanding(any(ResultCallbackWithError.class));
        doAnswer(inv -> {
            ResultCallbackWithError cb = inv.getArgument(0);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(module).drcForceLanding(any(ResultCallbackWithError.class));
        doAnswer(inv -> {
            ResultCallbackWithError cb = inv.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(module).drcOpenParachute(anyInt(), any(ResultCallbackWithError.class));
        doAnswer(inv -> {
            ResultCallbackWithError cb = inv.getArgument(1);
            cb.onResult(new FlightHubError(FlightHubSdkErrorCode.SUCCESS));
            return null;
        }).when(module).drcSetDroneHover(anyInt(), any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldCallDrcEmergencyLanding_whenModeLanding() {
        // 缺陷回归：mode=landing 必须调用 drcEmergencyLanding，而不是一刀切 drcEmergencyStop
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Force landing requested",
                "mode", "landing"
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcEmergencyLanding(any(ResultCallbackWithError.class));
        verify(module, never()).drcEmergencyStop(any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldCallDrcEmergencyStop_whenModeHover() {
        // mode=hover 仍应走切电机的 drcEmergencyStop
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Cut motors now",
                "mode", "hover"
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcEmergencyStop(any(ResultCallbackWithError.class));
        verify(module, never()).drcEmergencyLanding(any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldCallDrcEmergencyStop_whenModeOmitted() {
        // 未指定 mode 时默认 hover → drcEmergencyStop
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Default mode path"
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcEmergencyStop(any(ResultCallbackWithError.class));
        verify(module, never()).drcEmergencyLanding(any(ResultCallbackWithError.class));
    }

    // ==================== 真实 SDK 路径：强制降落 / 开伞 / 悬停保活 ====================

    @Test
    void execute_shouldCallDrcForceLanding_whenModeForceLanding() {
        // mode=force_landing → drcForceLanding（无参数）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Drone drifting toward crowd",
                "mode", "force_landing"
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcForceLanding(any(ResultCallbackWithError.class));
        verify(module, never()).drcEmergencyStop(any(ResultCallbackWithError.class));
        verify(module, never()).drcEmergencyLanding(any(ResultCallbackWithError.class));
        assertEquals(FlightPhase.LANDING, session.getFlightPhase());
    }

    @Test
    void execute_shouldCallDrcOpenParachuteWithNormalForce_whenParachuteForceOmitted() {
        // 开伞默认非强制模式，SDK 入参必须是 int 0（不是 boolean）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Motor failure detected",
                "mode", "parachute"
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcOpenParachute(eq(0), any(ResultCallbackWithError.class));
        verify(module, never()).drcForceLanding(any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldCallDrcOpenParachuteWithForcedForce_whenParachuteForceTrue() {
        // parachute_force=true → SDK force=1（强制开伞）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Drone falling",
                "mode", "parachute",
                "parachute_force", true
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcOpenParachute(eq(1), any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldCallDrcSetDroneHover_whenModeHoverHold() {
        // mode=hover_hold → drcSetDroneHover(duration_ms)，与切电机的 mode=hover 完全区分
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Extend hover keep-alive",
                "mode", "hover_hold",
                "duration_ms", 60000
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcSetDroneHover(eq(60000), any(ResultCallbackWithError.class));
        verify(module, never()).drcEmergencyStop(any(ResultCallbackWithError.class));
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    void execute_shouldAcceptUpperBound_whenHoverHoldDurationIsMax() {
        // SDK 上限 1800000ms（30 分钟），边界值必须放行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Max keep-alive",
                "mode", "hover_hold",
                "duration_ms", 1800000
        ));

        assertEquals(true, result.get("success"));
        verify(module).drcSetDroneHover(eq(1800000), any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldRejectDurationAboveMax_whenHoverHold() {
        // SDK 对超范围值静默截断且不报错，必须在 MCP 层拦截，否则操作员以为设了 31 分钟
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Too long",
                "mode", "hover_hold",
                "duration_ms", 1800001
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("duration_ms"));
        verify(module, never()).drcSetDroneHover(anyInt(), any(ResultCallbackWithError.class));
        assertNotEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    void execute_shouldRejectNegativeDuration_whenHoverHold() {
        // 负数同样必须拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        FlightControlModule module = mock(FlightControlModule.class);
        stubSuccessCallbacks(module);

        Map<String, Object> result = realToolWith(module).execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Negative duration",
                "mode", "hover_hold",
                "duration_ms", -1
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("duration_ms"));
        verify(module, never()).drcSetDroneHover(anyInt(), any(ResultCallbackWithError.class));
    }

    @Test
    void execute_shouldRejectMissingDuration_whenHoverHold() {
        // hover_hold 必须显式给出 duration_ms，不能默认 0（0 表示取消保活定时器）
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "No duration",
                "mode", "hover_hold"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("duration_ms"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSetLandingPhase_whenForceLandingInMockMode() {
        // mock 模式的飞行阶段也应正确反映动作语义
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "Force land in mock",
                "mode", "force_landing"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("force_landing", data.get("mode"));
        assertEquals(FlightPhase.LANDING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldDeclareDrcBasedActionsAndTheirParams() {
        Map<String, Object> def = tool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");

        Map<String, Object> modeProp = (Map<String, Object>) props.get("mode");
        List<?> modes = (List<?>) modeProp.get("enum");
        assertTrue(modes.containsAll(List.of("hover", "landing", "force_landing", "parachute", "hover_hold")),
                "mode 枚举必须含五种动作，且 hover 不得被复用为保活悬停");

        assertTrue(props.containsKey("duration_ms"), "hover_hold 需要 duration_ms 参数");
        assertTrue(props.containsKey("parachute_force"), "parachute 需要 parachute_force 参数");

        // 描述必须讲清三类新动作依赖 DRC 链路
        String description = def.get("description").toString();
        assertTrue(description.contains("DRC"), "description 必须说明 DRC 链路要求");
        assertTrue(description.contains("Dock 4"), "description 必须标注悬停保活仅支持 Dock 4");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldExecute_whenElicitationAccepted() {
        // client 支持 elicitation 且真人 ACCEPT → 即使 confirm=false 也应执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", false,
                "reason", "Operator approved via popup"
        ));

        assertEquals(true, result.get("success"),
                "真人在弹窗接受后应执行，不依赖 confirm 参数");
        assertEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
    }

    @Test
    void execute_shouldBlock_whenElicitationDeclined() {
        // client 支持 elicitation 但真人 DECLINE → 即使 confirm=true 也必须拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "LLM self-filled confirm"
        ));

        assertEquals(false, result.get("success"),
                "真人拒绝后必须拦截，confirm=true 不得越过人工闸门");
        assertTrue(result.get("error_message").toString().contains("declined"));
        assertNotEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
    }

    @Test
    void execute_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "operator_token", OPERATOR_TOKEN,
                "confirm", true,
                "reason", "No human present"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    void getToolDefinition_shouldIncludeOperatorTokenParam() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_emergency_stop", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        // description 应包含 EMERGENCY 关键字
        assertTrue(def.get("description").toString().toUpperCase().contains("EMERGENCY"));

        // operator_token 参数应出现在 schema 中（L3 二次鉴权）
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        assertTrue(props.containsKey("operator_token"),
                "schema 必须声明 operator_token 参数供真人带外提供");
    }
}
