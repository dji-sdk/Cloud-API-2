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

package com.dji.flighthub.mcp.tools.media;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
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
 * FhMediaRecordTool 单元测试。
 * 覆盖 session/workspace 校验、参数校验、三种 action 的 mock 模式执行，
 * 以及 listen_state/events/listen_stop 订阅三元组。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMediaRecordToolTest {

    private static final String DEVICE_SN = "DOCK-REC-001";
    private static final String DEVICE_SN_2 = "DOCK-REC-002";

    private SessionManager sessionManager;
    private ListenChannelSupport listenSupport;
    private FhMediaRecordTool recordTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = MediaTestHelper.createSdkServiceWithWorkspace();
        listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        recordTool = new FhMediaRecordTool(sessionManager, sdkService, listenSupport);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = recordTool.getToolDefinition();

        assertEquals("fh_media_record", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldHaveAllActions() {
        Map<String, Object> def = recordTool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertEquals(6, enumValues.size());
        assertTrue(enumValues.contains("start"));
        assertTrue(enumValues.contains("stop"));
        assertTrue(enumValues.contains("list"));
        assertTrue(enumValues.contains("listen_state"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenWorkspaceNull() {
        SdkService noWorkspace = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        FhMediaRecordTool tool = new FhMediaRecordTool(sessionManager, noWorkspace, listenSupport);
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("SDK not initialized"));
    }

    @Test
    void execute_shouldReturnError_whenDeviceSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "start");
        args.put("confirm", true);

        Map<String, Object> result = recordTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: device_sn"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "record",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStartRecording_whenActionIsStart() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals("start", data.get("action"));
        assertEquals("recording", data.get("status"));
        assertNotNull(data.get("record_id"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopRecording_whenActionIsStop() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "stop",
                "confirm", true
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertEquals("stop", data.get("action"));
        assertEquals("completed", data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListRecordings_whenActionIsListWithoutConfirm() {
        sessionManager.createSession("uuid", "ws");

        // list 操作不需要 confirm
        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "list"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals(DEVICE_SN, data.get("device_sn"));
        assertNotNull(data.get("records"));
        List<?> records = (List<?>) data.get("records");
        assertFalse(records.isEmpty());
        assertEquals(true, data.get("_mock"));

        // 契约字段断言：与真实 SDK 路径（listRecordingsReal）的 LiveRecordItem 映射保持一致
        Map<String, Object> first = (Map<String, Object>) records.get(0);
        assertNotNull(first.get("record_id"));
        assertNotNull(first.get("camera"));
        assertNotNull(first.get("state"));
        assertNotNull(first.get("duration"));
        assertNotNull(first.get("created_by"));
        // 真实路径已移除的旧字段不应再出现
        assertNull(first.get("start_time"));
        assertNull(first.get("end_time"));
        assertNull(first.get("status"));
    }

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        // list 是只读操作，工具级标志为 false，避免 SSE 传输层硬拦截
        // start/stop 的确认在 execute() 内部通过 requireHumanConfirm() 实现
        assertFalse(recordTool.requiresHumanConfirmation());
    }

    // ── listen_state 校验及 mock ──

    @Test
    void execute_shouldReturnError_whenListenStateHasNoSession() {
        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_state"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenState_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_state"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("listen_state", data.get("action"));
        assertEquals("record_state", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    // ── events 游标读取 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsCalledWithNoActiveChannels() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
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
        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("events", data.get("action"));
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get("record_state");
        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertNotNull(events.get(0).get("sn"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterBySinceSeq_whenEventsCursorAdvanced() {
        sessionManager.createSession("uuid", "ws");
        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));
        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));

        Map<String, Object> first = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events",
                "limit", 1
        ));
        Map<String, Object> firstStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) first.get("data")).get("streams"))
                        .get("record_state");
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> second = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events",
                "since_seq", lastSeq
        ));
        Map<String, Object> secondStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) second.get("data")).get("streams"))
                        .get("record_state");
        List<Map<String, Object>> secondEvents = (List<Map<String, Object>>) secondStream.get("events");

        assertEquals(1, secondEvents.size());
        assertTrue((long) secondEvents.get(0).get("_seq") > lastSeq);
    }

    // ── listen_stop 显式 channel 与"全部"两条路径 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopOnlyRequestedDeviceChannel_whenChannelsGiven() {
        sessionManager.createSession("uuid", "ws");
        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));
        recordTool.execute(Map.of("device_sn", DEVICE_SN_2, "action", "listen_state"));

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_stop",
                "channels", List.of("record_state")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_stop", data.get("action"));
        assertEquals(List.of("record_state"), data.get("stopped"));
        assertTrue(((List<?>) data.get("failed")).isEmpty());

        // 另一设备的订阅不受影响，仍处于 active 状态
        Map<String, Object> remaining = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN_2,
                "action", "events"
        ));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) remaining.get("data")).get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get("record_state");
        assertEquals(true, stream.get("active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAllChannels_whenChannelsOmitted() {
        sessionManager.createSession("uuid", "ws");
        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));

        Map<String, Object> result = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_stop"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of("record_state"), data.get("stopped"));

        Map<String, Object> after = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "events"
        ));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotLeakRegistration_whenListenStateRepeated() {
        sessionManager.createSession("uuid", "ws");

        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));
        recordTool.execute(Map.of("device_sn", DEVICE_SN, "action", "listen_state"));

        Map<String, Object> stop = recordTool.execute(Map.of(
                "device_sn", DEVICE_SN,
                "action", "listen_stop"
        ));
        Map<String, Object> data = (Map<String, Object>) stop.get("data");

        assertEquals(List.of("record_state"), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
    }
}
