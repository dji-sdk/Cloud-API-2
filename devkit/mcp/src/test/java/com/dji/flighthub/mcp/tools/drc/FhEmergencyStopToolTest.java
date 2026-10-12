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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

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
        // 持有控制权但未建立 DRC 链路 — 紧急停机应走管理通道，仍然成功
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
                "Emergency stop should succeed without DRC link (uses management channel)");
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
        assertEquals(FlightPhase.EMERGENCY, session.getFlightPhase());
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
