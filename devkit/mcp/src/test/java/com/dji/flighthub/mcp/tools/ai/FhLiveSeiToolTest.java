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

package com.dji.flighthub.mcp.tools.ai;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.dji.flighthubsdk.bean.LiveInfo;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.LiveDeviceModule;
import com.dji.flighthubsdk.core.LiveModule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhLiveSeiTool 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhLiveSeiToolTest {

    private static final String DEVICE_SN = "DRONE-SEI-001";
    private SessionManager sessionManager;
    private AiSubscriptionBuffer buffer;
    private FhLiveSeiTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        buffer = new AiSubscriptionBuffer();
        SdkService sdkService = new SdkService() {
            @Override public boolean isMockMode() { return true; }
        };
        tool = new FhLiveSeiTool(sessionManager, sdkService, buffer);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        assertEquals("fh_live_sei", tool.getToolDefinition().get("name"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "ranges"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of("action", "ranges"));
        assertEquals(false, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockRanges_whenRangesAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "ranges"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue((boolean) data.get("_mock"));
        assertNotNull(data.get("available_ranges"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockSuccess_whenStartAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start",
                "payload_index", "81-0-0", "confirm", true));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue((boolean) data.get("_mock"));
    }

    @Test
    void execute_shouldReturnError_whenStartMissingPayloadIndex() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start", "confirm", true));
        assertEquals(false, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockSuccess_whenSubscribeAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("ai")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("listen_ids"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEvents_whenEventsAction() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("ai")));

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "events",
                "payload_index", "81-0-0"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("events"));
    }

    @Test
    void execute_shouldReturnSuccess_whenStopAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "start",
                "payload_index", "81-0-0", "confirm", true));

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop",
                "payload_index", "81-0-0", "confirm", true));

        assertEquals(true, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidSeiChannel() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("ai", "bogus")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty());
    }

    @Test
    void execute_shouldKeepSingleRegistration_whenSubscribeCalledTwice() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> args = Map.of("device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("ai"));
        tool.execute(args);
        tool.execute(args);

        assertEquals(1, session.getListenRegistry().size());
    }

    @Test
    void execute_shouldClearRegistry_whenStopAfterSubscribe() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("ai", "fov")));
        assertEquals(2, session.getListenRegistry().size());

        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "stop",
                "payload_index", "81-0-0", "confirm", true));

        assertTrue(session.getListenRegistry().isEmpty());
    }

    @Test
    void execute_shouldNotRequireConfirm_whenRangesAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "ranges"));
        assertEquals(true, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldAcceptBothChannelForms_whenEventsAction() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("fov")));

        for (String channel : List.of("fov", "fov_sei")) {
            Map<String, Object> result = tool.execute(Map.of(
                    "device_sn", DEVICE_SN, "action", "events",
                    "payload_index", "81-0-0", "channel", channel));
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            assertEquals(1, data.get("count"), "channel form: " + channel);
            assertEquals("fov", data.get("channel"));
        }
    }

    @Test
    void execute_shouldReturnError_whenEventsChannelInvalid() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "events",
                "payload_index", "81-0-0", "channel", "bogus_sei"));
        assertEquals(false, result.get("success"));
    }

    private FhLiveSeiTool realModeTool(SessionManager manager, Drone drone) {
        SdkService realMode = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return drone; }
        };
        return new FhLiveSeiTool(manager, realMode, buffer);
    }

    @Test
    void execute_shouldRollbackAndFail_whenSdkReturnsNegativeSubscribeId() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Drone drone = mock(Drone.class);
        LiveModule live = mock(LiveModule.class);
        when(drone.getLiveStream(any(LiveInfo.class))).thenReturn(live);
        when(live.subscribeAiSeiInfo(any())).thenReturn(11);
        when(live.subscribeFovSeiInfo(any())).thenReturn(-1);

        Map<String, Object> result = realModeTool(sessionManager, drone).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("ai", "fov")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty());
        verify(live).unSubscribeAiSeiInfo(11);
    }

    @Test
    void execute_shouldUnsubscribeSdk_whenStopAfterRealSubscribe() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Drone drone = mock(Drone.class);
        LiveModule live = mock(LiveModule.class);
        when(drone.getLiveStream(any(LiveInfo.class))).thenReturn(live);
        when(live.subscribeEisSeiInfo(any())).thenReturn(21);
        FhLiveSeiTool realTool = realModeTool(sessionManager, drone);

        realTool.execute(Map.of("device_sn", DEVICE_SN, "action", "subscribe",
                "payload_index", "81-0-0", "channels", List.of("eis")));
        Map<String, Object> result = realTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop", "payload_index", "81-0-0", "confirm", true));

        assertEquals(true, result.get("success"));
        verify(live).unSubscribeEisSeiInfo(21);
        verify(live).stop();
        assertTrue(session.getListenRegistry().isEmpty());
    }

    @Test
    void execute_shouldReportFailure_whenLiveStopThrows() {
        sessionManager.createSession("uuid", "ws");
        Drone drone = mock(Drone.class);
        LiveModule live = mock(LiveModule.class);
        when(drone.getLiveStream(any(LiveInfo.class))).thenReturn(live);
        doThrow(new IllegalStateException("device offline")).when(live).stop();

        Map<String, Object> result = realModeTool(sessionManager, drone).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop", "payload_index", "81-0-0", "confirm", true));

        assertEquals(false, result.get("success"));
        assertTrue(String.valueOf(result.get("error_message")).contains("device offline"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldIncludeLiveStoppedFlag_whenStopSucceeds() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop", "payload_index", "81-0-0", "confirm", true));

        assertEquals(true, ((Map<String, Object>) result.get("data")).get("live_stream_stopped"));
    }

    @Test
    void execute_shouldFail_whenRangesListenerRejected() {
        sessionManager.createSession("uuid", "ws");
        Drone drone = mock(Drone.class);
        LiveDeviceModule dev = mock(LiveDeviceModule.class);
        when(drone.getLiveDeviceModule()).thenReturn(dev);
        when(dev.listenLiveAvailableRange(any())).thenReturn(-1);

        Map<String, Object> result = realModeTool(sessionManager, drone).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "ranges"));

        assertEquals(false, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnRangesAndCancelListener_whenCallbackFires() {
        sessionManager.createSession("uuid", "ws");
        Drone drone = mock(Drone.class);
        LiveDeviceModule dev = mock(LiveDeviceModule.class);
        when(drone.getLiveDeviceModule()).thenReturn(dev);
        when(dev.listenLiveAvailableRange(any())).thenAnswer(inv -> {
            com.dji.flighthubsdk.FlightHubCallback.LiveAvailableRangeCallback cb = inv.getArgument(0);
            LiveInfo info = new LiveInfo();
            info.setDeviceSn(DEVICE_SN);
            info.setPayloadIndex("81-0-0");
            info.setVideoId("normal-0");
            cb.onResult(List.of(info));
            return 5;
        });

        Map<String, Object> result = realModeTool(sessionManager, drone).execute(Map.of(
                "device_sn", DEVICE_SN, "action", "ranges"));

        assertEquals(true, result.get("success"));
        List<Map<String, Object>> ranges =
                (List<Map<String, Object>>) ((Map<String, Object>) result.get("data")).get("available_ranges");
        assertEquals(1, ranges.size());
        assertEquals("81-0-0", ranges.get(0).get("payload_index"));
        verify(dev).cancelListenLiveAvailableRange(5);
    }
}
