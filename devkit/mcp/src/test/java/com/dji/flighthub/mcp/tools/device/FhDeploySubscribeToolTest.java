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
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.subscription.ListenChannelSupport;
import com.dji.flighthub.mcp.tools.subscription.SubscriptionBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FhDeploySubscribeTool 单元测试。
 * 覆盖 session 校验、工具定义、4 个 listen_xxx channel 的 mock 订阅、
 * events 游标读取、listen_stop 取消以及重复订阅不泄漏注册表。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDeploySubscribeToolTest {

    private static final String DOCK_SN = "DOCK-DEPLOY-001";

    private SessionManager sessionManager;
    private FhDeploySubscribeTool deploySubscribeTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // Mock 模式 SdkService，getDock 默认返回 null（mock 路径不依赖真实 Dock 实例）
        SdkService sdkService = new SdkService() {
            @Override
            public boolean isMockMode() {
                return true;
            }
        };
        ListenChannelSupport listenSupport = new ListenChannelSupport(new SubscriptionBuffer());
        deploySubscribeTool = new FhDeploySubscribeTool(sessionManager, sdkService, listenSupport);
    }

    // ── 工具定义测试 ──

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = deploySubscribeTool.getToolDefinition();

        assertEquals("fh_deploy_subscribe", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldHaveAllSixActions() {
        Map<String, Object> def = deploySubscribeTool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertEquals(6, enumValues.size());
        assertTrue(enumValues.contains("listen_visual_scout_status"));
        assertTrue(enumValues.contains("listen_visual_scout_obstacle"));
        assertTrue(enumValues.contains("listen_visual_scout_abnormal"));
        assertTrue(enumValues.contains("listen_ephemeris_download_state"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));

        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("dock_sn"));
        assertTrue(required.contains("action"));
    }

    // ── session 校验 ──

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_visual_scout_status"
        ));

        assertFalse((boolean) result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    // ── 必填参数校验 ──

    @Test
    void execute_shouldReturnError_whenDockSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "listen_visual_scout_status");

        Map<String, Object> result = deploySubscribeTool.execute(args);

        assertFalse((boolean) result.get("success"));
        assertTrue(result.get("error_message").toString().contains("dock_sn"));
    }

    @Test
    void execute_shouldReturnError_whenActionMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("dock_sn", DOCK_SN);

        Map<String, Object> result = deploySubscribeTool.execute(args);

        assertFalse((boolean) result.get("success"));
        assertTrue(result.get("error_message").toString().contains("action"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "not_a_real_action"
        ));

        assertFalse((boolean) result.get("success"));
    }

    // ── 4 个 listen_xxx channel 的 mock 订阅 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenVisualScoutStatus_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_visual_scout_status"
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_visual_scout_status", data.get("action"));
        assertEquals("visual_scout_status", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenVisualScoutObstacle_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_visual_scout_obstacle"
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_visual_scout_obstacle", data.get("action"));
        assertEquals("visual_scout_obstacle", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenVisualScoutAbnormal_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_visual_scout_abnormal"
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_visual_scout_abnormal", data.get("action"));
        assertEquals("visual_scout_abnormal", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldListenEphemerisDownloadState_inMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_ephemeris_download_state"
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_ephemeris_download_state", data.get("action"));
        assertEquals("ephemeris_download_state", data.get("channel"));
        assertTrue((int) data.get("listen_id") > 0);
    }

    // ── events：无订阅时为空 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsCalledWithNoActiveChannels() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events"
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertTrue(streams.isEmpty());
    }

    // ── events：两个不同 channel 订阅后均可见，payload 结构正确 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReadEventsFromBothChannels_whenTwoChannelsActive() {
        sessionManager.createSession("uuid", "ws");
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_ephemeris_download_state"));

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events",
                "channels", List.of("visual_scout_status", "ephemeris_download_state")
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("events", data.get("action"));
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");

        Map<String, Object> statusStream = (Map<String, Object>) streams.get("visual_scout_status");
        assertEquals(true, statusStream.get("active"));
        assertEquals(1, statusStream.get("count"));
        List<Map<String, Object>> statusEvents = (List<Map<String, Object>>) statusStream.get("events");
        assertEquals("mock-task-001", statusEvents.get(0).get("task_id"));
        assertEquals(1, statusEvents.get(0).get("visual_scout_stage"));
        assertEquals(30L, statusEvents.get(0).get("scout_wayline_generate_remain_time"));

        Map<String, Object> ephemerisStream = (Map<String, Object>) streams.get("ephemeris_download_state");
        assertEquals(true, ephemerisStream.get("active"));
        assertEquals(1, ephemerisStream.get("count"));
        List<Map<String, Object>> ephemerisEvents = (List<Map<String, Object>>) ephemerisStream.get("events");
        assertEquals(1, ephemerisEvents.get(0).get("state"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReadObstacleAndAbnormalEvents_withCorrectPayloadShape() {
        sessionManager.createSession("uuid", "ws");
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_obstacle"));
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_abnormal"));

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events"
        ));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");

        Map<String, Object> obstacleStream = (Map<String, Object>) streams.get("visual_scout_obstacle");
        List<Map<String, Object>> obstacleEvents = (List<Map<String, Object>>) obstacleStream.get("events");
        assertEquals("mock-task-001", obstacleEvents.get(0).get("task_id"));
        assertEquals(true, obstacleEvents.get(0).get("obstacle_tag"));

        Map<String, Object> abnormalStream = (Map<String, Object>) streams.get("visual_scout_abnormal");
        List<Map<String, Object>> abnormalEvents = (List<Map<String, Object>>) abnormalStream.get("events");
        assertEquals("mock-task-001", abnormalEvents.get(0).get("task_id"));
        assertEquals(0, abnormalEvents.get(0).get("err_code"));
    }

    // ── events 游标：limit 后 since_seq 只返回更新的事件 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterBySinceSeq_whenEventsCursorAdvanced() {
        sessionManager.createSession("uuid", "ws");
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));

        Map<String, Object> first = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events",
                "channels", List.of("visual_scout_status"),
                "limit", 1
        ));
        Map<String, Object> firstStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) first.get("data")).get("streams"))
                        .get("visual_scout_status");
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        assertEquals(1, firstEvents.size());
        long lastSeq = (long) firstEvents.get(0).get("_seq");

        Map<String, Object> second = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "events",
                "channels", List.of("visual_scout_status"),
                "since_seq", lastSeq
        ));
        Map<String, Object> secondStream = (Map<String, Object>)
                ((Map<String, Object>) ((Map<String, Object>) second.get("data")).get("streams"))
                        .get("visual_scout_status");
        List<Map<String, Object>> secondEvents = (List<Map<String, Object>>) secondStream.get("events");

        assertEquals(1, secondEvents.size());
        assertTrue((long) secondEvents.get(0).get("_seq") > lastSeq);
    }

    // ── listen_stop：显式 channel 与省略（停全部） ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopOnlyRequestedChannel_whenChannelsGiven() {
        sessionManager.createSession("uuid", "ws");
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_obstacle"));

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_stop",
                "channels", List.of("visual_scout_status")
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_stop", data.get("action"));
        assertEquals(List.of("visual_scout_status"), data.get("stopped"));
        assertTrue(((List<?>) data.get("failed")).isEmpty());

        Map<String, Object> remaining = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN, "action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) remaining.get("data")).get("streams");
        assertTrue(streams.containsKey("visual_scout_obstacle"));
        assertFalse(streams.containsKey("visual_scout_status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAllChannels_whenChannelsOmitted() {
        sessionManager.createSession("uuid", "ws");
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_abnormal"));

        Map<String, Object> result = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN,
                "action", "listen_stop"
        ));

        assertTrue((boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<String> stopped = (List<String>) data.get("stopped");
        assertEquals(2, stopped.size());
        assertTrue(stopped.contains("visual_scout_status"));
        assertTrue(stopped.contains("visual_scout_abnormal"));

        Map<String, Object> after = deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "events"));
        Map<String, Object> streams = (Map<String, Object>)
                ((Map<String, Object>) after.get("data")).get("streams");
        assertTrue(streams.isEmpty());
    }

    // ── 重复订阅不泄漏注册表 ──

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotLeakRegistration_whenListenRepeated() {
        sessionManager.createSession("uuid", "ws");

        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));
        deploySubscribeTool.execute(Map.of("dock_sn", DOCK_SN, "action", "listen_visual_scout_status"));

        Map<String, Object> stop = deploySubscribeTool.execute(Map.of(
                "dock_sn", DOCK_SN, "action", "listen_stop"));
        Map<String, Object> data = (Map<String, Object>) stop.get("data");

        assertEquals(List.of("visual_scout_status"), data.get("stopped"));
        assertTrue(((List<?>) data.get("not_active")).isEmpty());
    }
}
