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

package com.dji.flighthub.mcp.tools.psdk;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.security.SecurityLevel;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FhPsdkSubscribeTool 单元测试。
 *
 * <p>覆盖：工具定义、无 session 报错、四个 listen action 的 mock 路径、
 * events 轮询与游标、listen_stop 的显式/全量语义、per-index 作用域隔离、
 * 重复订阅不泄漏注册、必填参数校验。
 *
 * @author DJI FlightHub SDK Team
 */
class FhPsdkSubscribeToolTest {

    private static final String DRONE_SN = "DRONE-001";
    private static final String CHANNEL_DEVICE_LIST = "device_list";
    private static final String CHANNEL_STATE_SUMMARY = "state_summary";
    private static final String CHANNEL_STATE_SPEAKER = "state_speaker";
    private static final String CHANNEL_STATE_SEARCHLIGHT = "state_searchlight";

    private SessionManager sessionManager;
    private FhPsdkSubscribeTool tool;

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
        tool = new FhPsdkSubscribeTool(sessionManager, sdkService,
                new ListenChannelSupport(new SubscriptionBuffer()));
    }

    // ==================== 工具定义 / 安全属性 ====================

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_psdk_subscribe", def.get("name"));
        assertNotNull(def.get("description"));

        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");
        List<String> required = (List<String>) schema.get("required");

        assertEquals(6, enumValues.size());
        assertEquals(List.of("listen_device_list", "listen_state_summary",
                "listen_state_speaker", "listen_state_searchlight", "events", "listen_stop"),
                enumValues);
        assertEquals(List.of("action", "drone_sn"), required);
        assertTrue(props.containsKey("psdk_index"));
        assertTrue(props.containsKey("channels"));
        assertTrue(props.containsKey("since_seq"));
        assertTrue(props.containsKey("limit"));
        assertFalse(required.contains("confirm"));
    }

    @Test
    void securityProperties_shouldBeReadOnlyWithoutConfirmation() {
        assertEquals(SecurityLevel.READ_ONLY, tool.getSecurityLevel());
        assertFalse(tool.requiresHumanConfirmation());
    }

    // ==================== 参数校验 ====================

    @Test
    void execute_shouldReturnError_whenNoSession() {
        for (String action : List.of("listen_device_list", "listen_state_summary",
                "listen_state_speaker", "listen_state_searchlight")) {
            Map<String, Object> result = tool.execute(Map.of("action", action, "drone_sn", DRONE_SN));

            assertEquals(false, result.get("success"), "action=" + action);
            assertTrue(result.get("error_message").toString().contains("No active session"),
                    "action=" + action);
        }
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "listen_device_list"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenActionMissingOrInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> missing = tool.execute(Map.of("drone_sn", DRONE_SN));
        assertEquals(false, missing.get("success"));
        assertTrue(missing.get("error_message").toString().contains("Invalid or missing action"));

        Map<String, Object> invalid = tool.execute(Map.of("action", "explode", "drone_sn", DRONE_SN));
        assertEquals(false, invalid.get("success"));
        assertTrue(invalid.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== listen_* ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListenDeviceList_shouldSucceedWithoutConfirm_whenMock() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "action", "listen_device_list", "drone_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_device_list", data.get("action"));
        assertEquals(CHANNEL_DEVICE_LIST, data.get("channel"));
        assertEquals(-1, data.get("psdk_index"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    void executeListenStateChannels_shouldSucceedForEachChannel_whenMock() {
        sessionManager.createSession("uuid", "ws");

        Map<String, String> expected = Map.of(
                "listen_state_summary", CHANNEL_STATE_SUMMARY,
                "listen_state_speaker", CHANNEL_STATE_SPEAKER,
                "listen_state_searchlight", CHANNEL_STATE_SEARCHLIGHT);

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            Map<String, Object> result = tool.execute(Map.of(
                    "action", entry.getKey(), "drone_sn", DRONE_SN, "psdk_index", 0));

            assertEquals(true, result.get("success"), "action=" + entry.getKey());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            assertEquals(entry.getKey(), data.get("action"));
            assertEquals(entry.getValue(), data.get("channel"));
            assertEquals(0, data.get("psdk_index"));
            assertTrue((int) data.get("listen_id") > 0);
        }
    }

    @Test
    void executeListenStateSummary_shouldRejectNegativeIndex_whenInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "listen_state_summary",
                "drone_sn", DRONE_SN, "psdk_index", -1));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("psdk_index"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeListenDeviceList_shouldIgnorePsdkIndex_whenProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "listen_device_list",
                "drone_sn", DRONE_SN, "psdk_index", 3));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(CHANNEL_DEVICE_LIST, data.get("channel"));
        assertEquals(-1, data.get("psdk_index"));
    }

    // ==================== events ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeEvents_shouldReturnEmptyStreams_whenNoSubscription() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) result.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeEvents_shouldReturnDeviceInfos_whenDeviceListSubscribed() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_device_list", "drone_sn", DRONE_SN));

        Map<String, Object> result = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("events", data.get("action"));
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get(CHANNEL_DEVICE_LIST);
        assertNotNull(stream);
        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));

        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        List<Map<String, Object>> deviceInfos =
                (List<Map<String, Object>>) events.get(0).get("device_infos");
        assertEquals(1, deviceInfos.size());
        assertEquals(0, deviceInfos.get(0).get("psdk_index"));
        assertEquals("mock-key-0", deviceInfos.get(0).get("object_key"));
        assertEquals(true, events.get(0).get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeEvents_shouldReturnStateSummaryEvent_whenSubscribed() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_state_summary",
                "drone_sn", DRONE_SN, "psdk_index", 1));

        Map<String, Object> result = tool.execute(Map.of("action", "events",
                "drone_sn", DRONE_SN, "channels", List.of("state_summary@1")));

        Map<String, Object> stream = streamOf(result, "state_summary@1");
        assertEquals(true, stream.get("active"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        Map<String, Object> event = events.get(0);
        assertEquals(1, event.get("psdk_index"));
        assertEquals("mock", event.get("psdk_type"));
        assertEquals("mock-psdk", event.get("psdk_name"));
        assertEquals("mock-sn", event.get("psdk_sn"));
        assertEquals(1, event.get("psdk_restrict_state"));
        assertEquals(true, event.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeEvents_shouldReturnSpeakerEvent_whenSubscribed() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_state_speaker", "drone_sn", DRONE_SN));

        Map<String, Object> result = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));

        Map<String, Object> stream = streamOf(result, CHANNEL_STATE_SPEAKER);
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        Map<String, Object> event = events.get(0);
        assertEquals("mock.wav", event.get("play_file_name"));
        assertEquals("mockmd5", event.get("play_file_md5"));
        assertEquals(0, event.get("tts_speed"));
        assertEquals(true, event.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeEvents_shouldReturnSearchlightEvent_whenSubscribed() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_state_searchlight", "drone_sn", DRONE_SN));

        Map<String, Object> result = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));

        Map<String, Object> stream = streamOf(result, CHANNEL_STATE_SEARCHLIGHT);
        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        Map<String, Object> event = events.get(0);
        assertEquals("mock", event.get("work_mode"));
        assertEquals(0.0, event.get("left_value"));
        assertEquals(0.0, event.get("right_value"));
        assertEquals(true, event.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeEvents_shouldFilterBySinceSeq_whenCursorAdvanced() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_state_speaker", "drone_sn", DRONE_SN));
        tool.execute(Map.of("action", "listen_state_speaker", "drone_sn", DRONE_SN));

        Map<String, Object> first = tool.execute(Map.of("action", "events",
                "drone_sn", DRONE_SN, "limit", 1));
        List<Map<String, Object>> firstEvents =
                (List<Map<String, Object>>) streamOf(first, CHANNEL_STATE_SPEAKER).get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> second = tool.execute(Map.of("action", "events",
                "drone_sn", DRONE_SN, "since_seq", lastSeq));
        List<Map<String, Object>> secondEvents =
                (List<Map<String, Object>>) streamOf(second, CHANNEL_STATE_SPEAKER).get("events");
        assertEquals(1, secondEvents.size());
        assertTrue((long) secondEvents.get(0).get("_seq") > lastSeq);
    }

    @Test
    void executeEvents_shouldReturnError_whenChannelInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "events",
                "drone_sn", DRONE_SN, "channels", List.of("not_a_channel")));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid channel"));
    }

    // ==================== per-index 作用域 ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListenStateSummary_shouldKeepIndependentChannels_whenIndexDiffers() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> one = tool.execute(Map.of("action", "listen_state_summary",
                "drone_sn", DRONE_SN, "psdk_index", 1));
        Map<String, Object> two = tool.execute(Map.of("action", "listen_state_summary",
                "drone_sn", DRONE_SN, "psdk_index", 2));

        assertEquals(true, one.get("success"));
        assertEquals(true, two.get("success"));
        assertNotEquals((int) ((Map<String, Object>) one.get("data")).get("listen_id"),
                (int) ((Map<String, Object>) two.get("data")).get("listen_id"));

        Map<String, Object> events = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) events.get("data")).get("streams");
        assertEquals(2, streams.size());
        assertEquals(true, ((Map<String, Object>) streams.get("state_summary@1")).get("active"));
        assertEquals(true, ((Map<String, Object>) streams.get("state_summary@2")).get("active"));

        // 独立停止 1 号索引，2 号索引仍保持活跃
        Map<String, Object> stop = tool.execute(Map.of("action", "listen_stop",
                "drone_sn", DRONE_SN, "channels", List.of("state_summary@1")));
        assertEquals(true, stop.get("success"));
        assertEquals(List.of("state_summary@1"),
                ((Map<String, Object>) stop.get("data")).get("stopped"));

        Map<String, Object> after = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));
        Map<String, Object> remaining = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertEquals(List.of("state_summary@2"), new ArrayList<>(remaining.keySet()));
        assertEquals(true, ((Map<String, Object>) remaining.get("state_summary@2")).get("active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeListenStop_shouldTargetPerIndexChannel_whenIndexParamGiven() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_state_speaker", "drone_sn", DRONE_SN, "psdk_index", 1));
        tool.execute(Map.of("action", "listen_state_speaker", "drone_sn", DRONE_SN, "psdk_index", 2));

        Map<String, Object> stop = tool.execute(Map.of("action", "listen_stop",
                "drone_sn", DRONE_SN, "channels", List.of("state_speaker"), "psdk_index", 2));

        // stopped 回显规范化后的 stream key（与 events 输出的 stream 键一致）
        assertEquals(true, stop.get("success"));
        assertEquals(List.of("state_speaker@2"),
                ((Map<String, Object>) stop.get("data")).get("stopped"));

        Map<String, Object> after = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertEquals(List.of("state_speaker@1"), new ArrayList<>(streams.keySet()));
    }

    // ==================== listen_stop ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListenStop_shouldStopOnlyRequestedChannel_whenChannelsGiven() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_device_list", "drone_sn", DRONE_SN));
        tool.execute(Map.of("action", "listen_state_summary", "drone_sn", DRONE_SN));

        Map<String, Object> result = tool.execute(Map.of("action", "listen_stop",
                "drone_sn", DRONE_SN, "channels", List.of(CHANNEL_DEVICE_LIST)));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of(CHANNEL_DEVICE_LIST), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
        assertTrue(((List<?>) data.get("failed")).isEmpty());

        Map<String, Object> after = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertEquals(List.of(CHANNEL_STATE_SUMMARY), new ArrayList<>(streams.keySet()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeListenStop_shouldStopAllChannels_whenChannelsOmitted() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_device_list", "drone_sn", DRONE_SN));
        tool.execute(Map.of("action", "listen_state_summary", "drone_sn", DRONE_SN, "psdk_index", 1));

        Map<String, Object> result = tool.execute(Map.of("action", "listen_stop", "drone_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of(CHANNEL_DEVICE_LIST, "state_summary@1"), data.get("stopped"));

        Map<String, Object> after = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));
        assertTrue(((Map<?, ?>) ((Map<String, Object>) after.get("data")).get("streams")).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeListenDeviceList_shouldNotLeakRegistration_whenRepeated() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("action", "listen_device_list", "drone_sn", DRONE_SN));
        tool.execute(Map.of("action", "listen_device_list", "drone_sn", DRONE_SN));

        Map<String, Object> events = tool.execute(Map.of("action", "events", "drone_sn", DRONE_SN));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) events.get("data")).get("streams");
        assertEquals(1, streams.size());

        Map<String, Object> stop = tool.execute(Map.of("action", "listen_stop", "drone_sn", DRONE_SN));
        Map<String, Object> data = (Map<String, Object>) stop.get("data");
        assertEquals(List.of(CHANNEL_DEVICE_LIST), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeListenStop_shouldReportNotActive_whenChannelNeverSubscribed() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "listen_stop",
                "drone_sn", DRONE_SN, "channels", List.of("state_searchlight@5")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue(((List<?>) data.get("stopped")).isEmpty());
        assertEquals(List.of("state_searchlight@5"), data.get("not_active"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> streamOf(Map<String, Object> result, String channel) {
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        return (Map<String, Object>) streams.get(channel);
    }
}
