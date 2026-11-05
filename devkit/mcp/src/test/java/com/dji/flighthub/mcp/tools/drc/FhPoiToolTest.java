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
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.FlightPhase;
import com.dji.flighthub.mcp.session.SessionContext;
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
 * FhPoiTool 单元测试。
 * 覆盖 session 校验、参数校验、mock 模式下的距离前置校验和正常环绕流程。
 *
 * @author DJI FlightHub SDK Team
 */
class FhPoiToolTest {

    private static final String DRONE_SN = "DRONE-POI-001";

    /** Mock 无人机坐标（与 FhPoiTool.MOCK_DRONE_LATITUDE/LONGITUDE 一致） */
    private static final double MOCK_DRONE_LAT = 22.5431;
    private static final double MOCK_DRONE_LNG = 113.9465;

    private SessionManager sessionManager;
    private FhPoiTool poiTool;

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
        poiTool = new FhPoiTool(sessionManager, sdkService, new ListenChannelSupport(new SubscriptionBuffer()));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "action", "start",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "invalid_action",
                "confirm", true
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid action"));
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = poiTool.getToolDefinition();

        assertEquals("fh_poi", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnError_whenDroneTooCloseToPoiCenter() {
        // 使用 mock 无人机坐标作为 POI 中心 → 距离 ≈ 0，应被拦截
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", MOCK_DRONE_LAT);
        args.put("longitude", MOCK_DRONE_LNG);
        args.put("height", 50.0);
        args.put("radius", 30.0);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("too close to POI center"),
                "应提示无人机距 POI 中心过近");
        assertTrue(result.get("suggestion").toString().contains("fh_fly_to"),
                "应建议使用 fh_fly_to 重新定位");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenDroneFarFromPoiCenter() {
        // POI 中心距无人机约 1km 远，半径 30m → 应通过距离校验
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", MOCK_DRONE_LAT + 0.01);
        args.put("longitude", MOCK_DRONE_LNG + 0.01);
        args.put("height", 50.0);
        args.put("radius", 30.0);
        args.put("speed", 5.0);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("start", data.get("action"));
        assertEquals(FlightPhase.ORBITING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopOrbit_whenActionIsStop() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);
        session.setFlightPhase(FlightPhase.ORBITING);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "stop");
        args.put("confirm", true);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("stop", data.get("action"));
        assertEquals(FlightPhase.HOVERING, session.getFlightPhase());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnStatus_withoutDrcLink() {
        // status 查询不需要 DRC 链路和 confirm
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setFlightPhase(FlightPhase.ORBITING);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "status");
        args.put("confirm", false);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("active"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidRadius() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", 22.0);
        args.put("longitude", 114.0);
        args.put("height", 50.0);
        args.put("radius", 3.0);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid radius"));
    }

    @Test
    void execute_shouldReturnError_whenNotConfirmed() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", false);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Confirmation required"));
    }

    // ==================== 订阅三元组测试：listen_event / events / listen_stop ====================

    @Test
    void getToolDefinition_shouldIncludeSubscriptionActions() {
        Map<String, Object> def = poiTool.getToolDefinition();

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> enumValues = (List<String>) actionProp.get("enum");

        assertEquals(6, enumValues.size());
        assertTrue(enumValues.contains("listen_event"));
        assertTrue(enumValues.contains("events"));
        assertTrue(enumValues.contains("listen_stop"));
    }

    @Test
    void execute_shouldReturnError_whenListenEventNoSession() {
        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "listen_event"
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenListenEventInMockMode() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "listen_event"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("listen_event", data.get("action"));
        assertEquals("poi_task_event", data.get("channel"));
        assertTrue(((Number) data.get("listen_id")).intValue() > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEmptyStreams_whenEventsWithoutSubscription() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "events"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertTrue(streams.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnActiveStream_withEventAfterListen() {
        sessionManager.createSession("uuid", "ws");

        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "events"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        Map<String, Object> stream = (Map<String, Object>) streams.get("poi_task_event");

        assertEquals(true, stream.get("active"));
        assertEquals(1, stream.get("count"));

        List<Map<String, Object>> events = (List<Map<String, Object>>) stream.get("events");
        assertEquals(1, events.size());
        assertNotNull(events.get(0).get("task_data"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnOnlyNewerEvents_whenPollingWithSinceSeq() {
        sessionManager.createSession("uuid", "ws");
        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));

        Map<String, Object> first = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "events",
                "limit", 50
        ));
        Map<String, Object> firstData = (Map<String, Object>) first.get("data");
        Map<String, Object> firstStreams = (Map<String, Object>) firstData.get("streams");
        Map<String, Object> firstStream = (Map<String, Object>) firstStreams.get("poi_task_event");
        List<Map<String, Object>> firstEvents = (List<Map<String, Object>>) firstStream.get("events");
        long lastSeq = ((Number) firstEvents.get(firstEvents.size() - 1).get("_seq")).longValue();

        Map<String, Object> second = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "events",
                "since_seq", lastSeq
        ));
        Map<String, Object> secondData = (Map<String, Object>) second.get("data");
        Map<String, Object> secondStreams = (Map<String, Object>) secondData.get("streams");
        Map<String, Object> secondStream = (Map<String, Object>) secondStreams.get("poi_task_event");

        assertEquals(0, secondStream.get("count"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopExplicitChannels_whenListenStopWithChannels() {
        sessionManager.createSession("uuid", "ws");
        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "listen_stop",
                "channels", List.of("poi_task_event")
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<String> stopped = (List<String>) data.get("stopped");
        assertEquals(List.of("poi_task_event"), stopped);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldStopAllChannels_whenListenStopWithoutChannels() {
        sessionManager.createSession("uuid", "ws");
        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "listen_stop"
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<String> stopped = (List<String>) data.get("stopped");
        assertEquals(List.of("poi_task_event"), stopped);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldNotLeakRegistrations_whenListenEventCalledRepeatedly() {
        sessionManager.createSession("uuid", "ws");

        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));
        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));
        poiTool.execute(Map.of("drone_sn", DRONE_SN, "action", "listen_event"));

        Map<String, Object> result = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "listen_stop"
        ));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<String> stopped = (List<String>) data.get("stopped");
        // 重复 listen_event 应复用同一个 registry key，不会产生多条可停止的 channel 记录
        assertEquals(1, stopped.size());

        Map<String, Object> afterStop = poiTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "action", "listen_stop"
        ));
        Map<String, Object> afterStopData = (Map<String, Object>) afterStop.get("data");
        List<String> notActive = (List<String>) afterStopData.get("not_active");
        assertTrue(notActive.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReportDirectionNotApplied_whenDirectionGiven() {
        // SDK 的 POIEnterParams 无方向字段：direction 只能校验 + 回显，
        // 响应必须显式声明 direction_applied=false，避免 AI/测试按文档预期误判为已生效
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActiveDrcDevice(DRONE_SN);

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("confirm", true);
        args.put("latitude", MOCK_DRONE_LAT + 0.01);
        args.put("longitude", MOCK_DRONE_LNG + 0.01);
        args.put("height", 50.0);
        args.put("radius", 30.0);
        args.put("direction", "counter_clockwise");

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("counter_clockwise", data.get("direction"));
        assertEquals(false, data.get("direction_applied"));
        assertTrue(result.get("message").toString().contains("SDK has no direction field"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getToolDefinition_shouldDeclareDirectionUnsupported() {
        // schema 描述不能承诺方向能力，否则会引导 AI 生成错误的集成代码
        Map<String, Object> def = poiTool.getToolDefinition();
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        Map<String, Object> direction = (Map<String, Object>) props.get("direction");

        String description = direction.get("description").toString();
        assertTrue(description.contains("NOT SUPPORTED"));
        assertTrue(description.contains("no direction field"));
    }

    @Test
    void execute_dryRun_shouldNotAcquireControlOrConnectDrc() {
        // 干跑必须在 ensureDrcLink 之前返回：不得抢控、不得建立 DRC 链路
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("drone_sn", DRONE_SN);
        args.put("action", "start");
        args.put("latitude", MOCK_DRONE_LAT + 0.01);
        args.put("longitude", MOCK_DRONE_LNG + 0.01);
        args.put("height", 50.0);
        args.put("radius", 30.0);
        args.put("dry_run", true);

        Map<String, Object> result = poiTool.execute(args);

        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("_dry_run"));
        assertFalse(session.hasControlOf(DRONE_SN), "dry_run 不应获取控制权");
        assertFalse(session.hasDrcLinkTo(DRONE_SN), "dry_run 不应建立 DRC 链路");
        assertEquals(FlightPhase.IDLE, session.getFlightPhase());
    }
}
