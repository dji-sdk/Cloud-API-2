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

package com.dji.flighthub.mcp.tools.connect;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ClientBridgeHolder;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhOpsTool 单元测试。
 * 覆盖 status/set_log_level/shutdown 三种操作及参数校验。
 *
 * @author DJI FlightHub SDK Team
 */
class FhOpsToolTest {

    private SessionManager sessionManager;
    private SdkService sdkService;
    private FhOpsTool opsTool;

    /** set_log_level 用例会修改根 logger，测试结束后恢复原级别 */
    private ch.qos.logback.classic.Level originalRootLevel;

    @BeforeEach
    void setUp() {
        sessionManager = createSessionManager(3);
        sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        opsTool = new FhOpsTool(sessionManager, sdkService);
        originalRootLevel = rootLogger().getLevel();
    }

    @AfterEach
    void tearDown() {
        rootLogger().setLevel(originalRootLevel);
    }

    /**
     * 获取根 logback logger。
     *
     * @return 根 logger
     */
    private static ch.qos.logback.classic.Logger rootLogger() {
        return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    }

    /**
     * 构建指定 session 上限的 SessionManager 实例。
     *
     * @param maxLevel session 允许的最大安全等级
     * @return 已配置上限的 SessionManager
     */
    private static SessionManager createSessionManager(int maxLevel) {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(maxLevel);
        return new SessionManager(properties);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = opsTool.getToolDefinition();

        assertEquals("fh_ops", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnStatus_whenActionIsStatus() {
        Map<String, Object> result = opsTool.execute(Map.of("action", "status"));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("1.0.0", data.get("server_version"));
        assertEquals("2025-06-18", data.get("mcp_protocol_version"));
        assertNotNull(data.get("uptime_seconds"));
        assertNotNull(data.get("java_version"));
        // P1 #4: sdk_version 字段在 mock 模式下返回 "MOCK"
        assertEquals("MOCK", data.get("sdk_version"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReflectSessionState_whenHasActiveSession() {
        // 先无 session
        Map<String, Object> resultNoSession = opsTool.execute(Map.of("action", "status"));
        Map<String, Object> dataNoSession = (Map<String, Object>) resultNoSession.get("data");
        assertEquals(false, dataNoSession.get("session_active"));

        // 创建 session 后
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> resultWithSession = opsTool.execute(Map.of("action", "status"));
        Map<String, Object> dataWithSession = (Map<String, Object>) resultWithSession.get("data");
        assertEquals(true, dataWithSession.get("session_active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSetLogLevel_whenLevelProvided() {
        // set_log_level 的 per-action 门控要求活跃 session（L1 REVERSIBLE），先建会话再执行
        sessionManager.createSession("ws-uuid", "test-workspace");
        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "set_log_level",
                "level", "DEBUG"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("DEBUG", data.get("log_level"));
    }

    @Test
    void execute_shouldReturnError_whenLevelMissing() {
        // 门控在参数校验之前执行，缺 session 会先返回 noSessionError，故先建会话
        sessionManager.createSession("ws-uuid", "test-workspace");
        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "set_log_level"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing 'level' parameter"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "invalid_action"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Unknown action"));
    }

    @Test
    void getSecurityLevel_shouldReturnReadOnly_soStatusIsNotBlockedByDispatchGate() {
        // 混合等级工具：类级返回最低声明等级 L0，set_log_level 的 L1、shutdown 的 L3 由 per-action 门控升级
        assertEquals(SecurityLevel.READ_ONLY, opsTool.getSecurityLevel());
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse_becauseConfirmIsPerAction() {
        // 传输层不做统一 confirm，shutdown 在 action 内自行调用 requireHumanConfirm
        assertFalse(opsTool.requiresHumanConfirmation());
    }

    @Test
    void executeShutdown_shouldBeDenied_whenSessionMaxLevelBelowCritical() {
        // session 上限 L2 时 shutdown（L3）必须被硬拒绝，且不能触发 System.exit
        SessionManager levelTwoSession = createSessionManager(2);
        levelTwoSession.createSession("ws-uuid", "test-workspace");
        FhOpsTool tool = new FhOpsTool(levelTwoSession, sdkService);

        Map<String, Object> result = tool.execute(Map.of("action", "shutdown"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void executeSetLogLevel_shouldBeDenied_whenSessionMaxLevelIsReadOnly() {
        // set_log_level 为 L1，session 上限 L0 时被 per-action 门控拒绝
        SessionManager levelZeroSession = createSessionManager(0);
        levelZeroSession.createSession("ws-uuid", "test-workspace");
        FhOpsTool tool = new FhOpsTool(levelZeroSession, sdkService);

        Map<String, Object> result = tool.execute(Map.of(
                "action", "set_log_level",
                "level", "DEBUG"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void executeShutdown_shouldBeDenied_whenNoSession() {
        // 无 session 时门控返回 noSessionError，避免未建连的客户端关停服务
        Map<String, Object> result = opsTool.execute(Map.of("action", "shutdown"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void executeShutdown_shouldBeDenied_whenSessionAllowsButNotConfirmed() {
        // session 上限 L3 时授权通过，但缺少人工确认必须被拦截，且绝不能走到 System.exit
        assertEquals(3, sessionManager.createSession("ws-uuid", "test-workspace").getMaxSecurityLevel());
        assertNull(ClientBridgeHolder.get(), "单元测试不应绑定 bridge，确保走本地 confirm 回退分支");

        Map<String, Object> result = opsTool.execute(Map.of("action", "shutdown"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"),
                "expected confirmation denial, got: " + result);
        // session 仍存在，说明未执行 shutdown 逻辑
        assertTrue(sessionManager.hasActiveSession());
    }

    @Test
    void executeShutdown_shouldBeDenied_whenConfirmFalse() {
        // requiresHumanConfirmation() 的契约：显式 confirm=false 同样必须被拦截
        // （confirm=true 分支会执行 System.exit(0)，不可在单测中覆盖）
        sessionManager.createSession("ws-uuid", "test-workspace");

        Map<String, Object> result = opsTool.execute(Map.of(
                "action", "shutdown",
                "confirm", false
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"),
                "expected confirmation denial, got: " + result);
        assertTrue(sessionManager.hasActiveSession());
    }

    @Test
    void sessionManagerCloseAllSessions_shouldClearActiveSession() {
        // 本用例验证的是 SessionManager 契约（非 FhOpsTool）：
        // 直接调用 opsTool.execute(shutdown) 会触发 System.exit(0) 杀死 JVM，
        // 因此 shutdown 的 session 清理逻辑通过 SessionManager 单测覆盖。
        sessionManager.createSession("uuid", "ws");
        assertTrue(sessionManager.hasActiveSession());

        sessionManager.closeAllSessions();
        assertFalse(sessionManager.hasActiveSession());
    }
}
