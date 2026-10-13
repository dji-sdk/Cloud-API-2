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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.dji.flighthub.mcp.elicit.ElicitTestSupport.fakeBridge;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FhLiveStartTool 单元测试。
 * 覆盖 session 校验、device_sn 校验、action 合法性校验、confirm 校验及 mock 模式各 action 分支。
 * 注意：本工具使用 device_sn（兼容无人机与机场），无需飞行控制权（L1 安全等级）。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhLiveStartToolTest {

    private static final String DEVICE_SN = "DRONE-LIVE-001";

    private SessionManager sessionManager;
    private FhLiveStartTool liveTool;

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
        liveTool = new FhLiveStartTool(sessionManager, sdkService);
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action: invalid_action"));
    }

    @Test
    void execute_shouldReturnError_whenConfirmMissing() {
        // session 存在，参数合法，但未提供 confirm=true
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", false
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStartStream_whenMockMode() {
        // 验证 action=start 返回 streaming=true 且包含 stream_url
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals(true, data.get("streaming"));
        assertNotNull(data.get("stream_url"));
        assertTrue(data.get("stream_url").toString().contains(DEVICE_SN));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopStream_whenStopAction() {
        // 验证 action=stop 返回 streaming=false
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "stop",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals(false, data.get("streaming"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSetQuality_whenQualityAction() {
        // 验证 action=quality 返回指定画质
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "quality",
                "quality", "hd",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals("hd", data.get("quality"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidQuality() {
        // quality="4k" 不在允许列表中，应返回错误（I5 fix）
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "quality",
                "quality", "4k",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid quality: 4k"));
    }

    // ==================== Elicitation 人工确认测试 ====================

    @Test
    void execute_shouldExecute_whenElicitationAccepted() {
        // client 支持 elicitation 且真人 ACCEPT → 即使 confirm=false 也应执行
        sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.accept(null)));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", false
        ));

        assertEquals(true, result.get("success"), "真人接受后应执行，不依赖 confirm 参数");
    }

    @Test
    void execute_shouldBlock_whenElicitationDeclined() {
        // client 支持 elicitation 但真人 DECLINE → 即使 confirm=true 也必须拦截
        sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.decline()));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"), "真人拒绝后必须拦截");
        assertTrue(result.get("error_message").toString().contains("declined"));
    }

    @Test
    void execute_shouldBlock_whenElicitationTimedOut() {
        // 真人未响应超时 → 拦截，绝不默认放行
        sessionManager.createSession("uuid", "ws");
        ClientBridgeHolder.set(fakeBridge(ElicitResult.timeout()));

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("timed out"));
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = liveTool.getToolDefinition();

        assertEquals("fh_live_start", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    // ── switch_source 测试 ──

    @Test
    void execute_shouldReturnError_whenSwitchSourceMissingPayloadIndex() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "switch_source",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("payload_index"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSwitchSource_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = liveTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "switch_source",
                "confirm", true,
                "payload_index", "53-0-0"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals("53-0-0", data.get("payload_index"));
        assertEquals(true, data.get("switched"));
        assertEquals(true, data.get("streaming"));
        assertNotNull(data.get("stream_url"));
        assertEquals(true, data.get("_mock"));
    }
}
