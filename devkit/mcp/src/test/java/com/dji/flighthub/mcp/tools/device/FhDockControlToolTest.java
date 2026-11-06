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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDockControlTool 单元测试。
 * 覆盖 session 校验、参数校验、confirm 缺失和 dock 不存在。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDockControlToolTest {

    private static final String DOCK_SN = "DOCK-CTRL-001";

    private SessionManager sessionManager;
    private FhDockControlTool dockControlTool;
    private ListenChannelSupport listenSupport;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // Mock 模式 SdkService，getDock 返回 null（模拟 dock 不存在）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        dockControlTool = new FhDockControlTool(sessionManager, sdkService, listenSupport);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = dockControlTool.getToolDefinition();

        assertEquals("fh_dock_control", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void getSecurityLevel_shouldReturnReversible() {
        // 机场控制是物理硬件操作，必须注册为 L1，否则工具级限流会落到 L0 的宽松配额
        assertEquals(SecurityLevel.REVERSIBLE, dockControlTool.getSecurityLevel());
    }

    @Test
    void execute_shouldDeny_whenSessionLevelBelowReversible() {
        // session 上限 L0 时，机场控制必须在 dry_run / confirm 之前就被硬拒绝
        FlightHubProperties lowLevelProps = new FlightHubProperties();
        lowLevelProps.getSecurity().setMaxLevel(0);
        SessionManager lowLevelSessions = new SessionManager(lowLevelProps);
        lowLevelSessions.createSession("uuid", "ws");
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhDockControlTool tool = new FhDockControlTool(lowLevelSessions, sdkService,
                new ListenChannelSupport(new SubscriptionBuffer()));

        Map<String, Object> result = tool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "cover_open",
                "confirm", true,
                "dry_run", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("insufficient security level"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "cover_open",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "cover_open");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenDockSnBlank() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", "  ");
        args.put("action", "cover_open");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "self_destruct");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenActionNull() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void execute_shouldReturnError_whenConfirmMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "cover_open");

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenConfirmFalse() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "cover_close");
        args.put("confirm", false);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    @Test
    void execute_shouldReturnError_whenDockNotFound() {
        // 在 mock 模式下，getDock 返回 null
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "cover_open");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Dock not found"));
    }

    @Test
    void getToolDefinition_shouldContainCameraPositionAction() {
        Map<String, Object> def = dockControlTool.getToolDefinition();

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actionEnum = (List<String>) actionProp.get("enum");

        assertTrue(actionEnum.contains("camera_position"));
        assertTrue(props.containsKey("gateway_sn"));
        assertTrue(props.containsKey("camera_position"));
        assertTrue(props.containsKey("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenCameraPositionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "camera_position");
        args.put("gateway_sn", DOCK_SN);
        args.put("camera_position", "top");

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid camera_position"));
        assertTrue(result.get("suggestion").toString().contains("inside"));
    }

    @Test
    void execute_shouldReturnError_whenGatewaySnMissingForCameraPosition() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "camera_position");
        args.put("camera_position", "inside");

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing gateway_sn"));
    }

    @Test
    void execute_shouldReturnMockSuccess_whenCameraPositionValid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);
        args.put("action", "camera_position");
        args.put("gateway_sn", DOCK_SN);
        args.put("camera_position", "outside");
        args.put("confirm", true);

        Map<String, Object> result = dockControlTool.execute(args);

        assertEquals(true, result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        assertEquals(DOCK_SN, data.get("gateway_sn"));
        assertEquals(1, data.get("camera_position"));
    }

    @Test
    void execute_shouldReturnError_whenNoSessionForCameraPosition() {
        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "camera_position",
                "gateway_sn", DOCK_SN,
                "camera_position", "inside",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ── 订阅三元组：listen_light_state / events / listen_stop ──

    @Test
    void getToolDefinition_shouldContainSubscriptionActions() {
        Map<String, Object> def = dockControlTool.getToolDefinition();

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actionEnum = (List<String>) actionProp.get("enum");

        assertTrue(actionEnum.contains("listen_light_state"));
        assertTrue(actionEnum.contains("events"));
        assertTrue(actionEnum.contains("listen_stop"));
    }

    @Test
    void execute_shouldReturnError_whenListenLightStateHasNoSession() {
        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_light_state"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenLightState_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_light_state"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_light_state", data.get("action"));
        assertEquals("supplement_light_state", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsCalledWithNoActiveChannels() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReadEvents_whenChannelActive() {
        sessionManager.createSession("uuid", "ws");
        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));

        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events",
                "channels", List.of("supplement_light_state")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("events", data.get("action"));
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get("supplement_light_state");
        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertTrue(events.get(0).containsKey("supplement_light_state"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterBySinceSeq_whenEventsCursorAdvanced() {
        sessionManager.createSession("uuid", "ws");
        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));
        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));

        Map<String, Object> first = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events",
                "channels", List.of("supplement_light_state"),
                "limit", 1
        ));
        Map<String, Object> firstStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) first.get("data")).get("streams"))
                        .get("supplement_light_state");
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> second = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events",
                "channels", List.of("supplement_light_state"),
                "since_seq", lastSeq
        ));
        Map<String, Object> secondStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) second.get("data")).get("streams"))
                        .get("supplement_light_state");
        List<Map<String, Object>> secondEvents = (List<Map<String, Object>>) secondStream.get("events");

        assertEquals(1, secondEvents.size());
        assertTrue((long) secondEvents.get(0).get("_seq") > lastSeq);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopOnlyRequestedChannel_whenChannelsGiven() {
        sessionManager.createSession("uuid", "ws");
        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));

        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_stop",
                "channels", List.of("supplement_light_state")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_stop", data.get("action"));
        assertEquals(List.of("supplement_light_state"), data.get("stopped"));
        assertTrue(((List<?>) data.get("failed")).isEmpty());

        Map<String, Object> remaining = dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) remaining.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAllChannels_whenChannelsOmitted() {
        sessionManager.createSession("uuid", "ws");
        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));

        Map<String, Object> result = dockControlTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_stop"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of("supplement_light_state"), data.get("stopped"));

        Map<String, Object> after = dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotLeakRegistration_whenListenRepeated() {
        sessionManager.createSession("uuid", "ws");

        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));
        dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_light_state"));

        Map<String, Object> stop = dockControlTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_stop"));
        Map<String, Object> data = (Map<String, Object>) stop.get("data");

        assertEquals(List.of("supplement_light_state"), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
    }
}
