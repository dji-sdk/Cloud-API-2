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
import com.dji.flighthub.mcp.sdk.StickControlExecutor;
import com.dji.flighthub.mcp.sdk.StickDirection;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FhStickMoveTool 单元测试。
 * 覆盖 DRC 链路前置条件、方向参数校验、mock 模式正常流程及默认参数行为。
 *
 * <p>注意：使用快速 {@code StickControlExecutor} 子类（覆盖 {@code execute()}）
 * 以避免单元测试中真实的 50ms × N 调度等待。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhStickMoveToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";

    /**
     * 固定返回 tick 次数的快速 mock 执行器，避免单元测试等待真实调度。
     * 返回值模拟 durationMs / INTERVAL_MS 的 tick 计数（最小 2）。
     */
    private static class FastStickExecutor extends StickControlExecutor {
        private int lastSpeedPercent;
        private int lastDurationMs;

        @Override
        public int execute(StickControlExecutor.StickSender stickSender,
                           StickDirection direction,
                           int speedPercent, int durationMs) {
            this.lastSpeedPercent = speedPercent;
            this.lastDurationMs = durationMs;
            // 模拟 tick 计数，不做真实调度
            return Math.max(durationMs / INTERVAL_MS, 2);
        }
    }

    private SessionManager sessionManager;
    private FhStickMoveTool tool;
    private FastStickExecutor fastExecutor;

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
        fastExecutor = new FastStickExecutor();
        tool = new FhStickMoveTool(sessionManager, sdkService, fastExecutor);
    }

    @Test
    void execute_shouldAutoAcquireDrcLink_whenNotEstablished() {
        // 建立 session 但不设置 DRC 链路 → 应自动获取控制权 + 连接 DRC
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "confirm", true
        ));

        assertEquals(true, result.get("success"),
                "DRC 链路应自动建立，无需额外确认");
        assertTrue(session.hasDrcLinkTo(DRONE_SN));
        assertTrue(session.hasControlOf(DRONE_SN));
    }

    @Test
    void execute_shouldReturnError_whenInvalidDirection() {
        // direction="invalid" 应返回错误，并列出有效方向
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "invalid",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid direction"));
        // suggestion 应包含有效方向列表
        String suggestion = result.get("suggestion").toString().toLowerCase();
        assertTrue(suggestion.contains("forward") || suggestion.contains("backward"),
                "Suggestion should list valid directions");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenMockModeAndValidInput() {
        // 完整前置条件 — mock 模式应成功执行并返回 ticks > 0
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "confirm", true,
                "speed_percent", 50,
                "duration_ms", 500
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("forward", data.get("direction"));
        assertEquals(50, data.get("speed_percent"));
        assertEquals(500, data.get("duration_ms"));
        // ticks_sent 必须大于 0
        int ticksSent = (int) data.get("ticks_sent");
        assertTrue(ticksSent > 0, "ticks_sent should be > 0, got: " + ticksSent);
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldUseDefaultSpeedAndDuration_whenNotProvided() {
        // 不传 speed_percent 和 duration_ms，应使用默认值 30 和 1000
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "up",
                "confirm", true
                // 不传 speed_percent，不传 duration_ms
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        // 验证默认值已被使用
        assertEquals(30, data.get("speed_percent"), "默认 speed_percent 应为 30");
        assertEquals(1000, data.get("duration_ms"), "默认 duration_ms 应为 1000");
    }

    @Test
    void execute_shouldReturnError_whenConfirmFalse() {
        // confirm=false 时，应返回确认错误
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "backward",
                "confirm", false
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        // 无 session 时，应返回 no session 错误
        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenAllValidDirections() {
        // 所有有效方向均应成功执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        String[] directions = {"forward", "backward", "left", "right", "up", "down", "rotate_cw", "rotate_ccw"};

        for (String dir : directions) {
            Map<String, Object> result = tool.execute(Map.of(
                    "drone_sn", DRONE_SN,
                    "direction", dir,
                    "confirm", true,
                    "duration_ms", 100
            ));

            assertEquals(true, result.get("success"),
                    "方向 " + dir + " 应该成功执行，但返回了: " + result.get("error_message"));

            Map<String, Object> data = (Map<String, Object>) result.get("data");
            assertEquals(dir, data.get("direction"),
                    "响应中的 direction 应与请求一致");
        }
    }

    // ==================== Elicitation 人工确认测试 ====================

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldExecute_whenElicitationAccepted() {
        // client 支持 elicitation 且真人 ACCEPT → 即使 confirm=false 也应执行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "confirm", false,
                "duration_ms", 100
        ));

        assertEquals(true, result.get("success"), "真人接受后应执行，不依赖 confirm 参数");
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("forward", data.get("direction"));
    }

    @Test
    void execute_shouldBlock_whenElicitationDeclined() {
        // client 支持 elicitation 但真人 DECLINE → 即使 confirm=true 也必须拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "confirm", true
        ));

        assertEquals(false, result.get("success"), "真人拒绝后必须拦截");
        assertTrue(result.get("error_message").toString().contains("declined"));
    }

    @Test
    void execute_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = tool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "direction", "forward",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    void getToolDefinition_shouldHaveCorrectName() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_stick_move", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }
}
