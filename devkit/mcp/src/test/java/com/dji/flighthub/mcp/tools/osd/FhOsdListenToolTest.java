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

package com.dji.flighthub.mcp.tools.osd;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.elicit.ElicitTestSupport;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.ai.AiSubscriptionBuffer;
import com.dji.flighthubsdk.FlightHubCallback;
import com.dji.flighthubsdk.bean.AircraftWindSpeed;
import com.dji.flighthubsdk.bean.DockAlarmState;
import com.dji.flighthubsdk.core.Dock;
import com.dji.flighthubsdk.core.DockOsdModule;
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.DroneOsdModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FhOsdListenTool 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhOsdListenToolTest {

    private static final String DRONE_SN = "DRONE-OSD-001";
    private static final String DOCK_SN = "DOCK-OSD-001";

    private SessionManager sessionManager;
    private AiSubscriptionBuffer buffer;
    private FhOsdListenTool mockTool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        buffer = new AiSubscriptionBuffer();
        SdkService mockSdk = new SdkService() {
            @Override public boolean isMockMode() { return true; }
        };
        mockTool = new FhOsdListenTool(sessionManager, mockSdk, buffer);
    }

    @AfterEach
    void clearInterrupt() {
        Thread.interrupted();
    }

    // ==================== 基础校验 ====================

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        assertEquals("fh_osd_listen", mockTool.getToolDefinition().get("name"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = mockTool.execute(Map.of("action", "catalog"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidAction() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "bogus"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenInvalidDeviceType() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "catalog", "device_type", "rc"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "read", "fields", List.of("wind_speed")));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenFieldsMissingForReadAndStart() {
        sessionManager.createSession("uuid", "ws");
        for (String action : List.of("read", "start")) {
            Map<String, Object> result = mockTool.execute(Map.of("action", action, "device_sn", DRONE_SN));
            assertEquals(false, result.get("success"), action);
            assertTrue(String.valueOf(result.get("error_message")).contains("fields"), action);
        }
    }

    @Test
    void execute_shouldReturnError_whenFieldUnknown() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        for (String action : List.of("read", "start")) {
            Map<String, Object> result = mockTool.execute(Map.of("action", action, "device_sn", DRONE_SN,
                    "fields", List.of("wind_speed", "bogus")));
            assertEquals(false, result.get("success"), action);
            assertTrue(String.valueOf(result.get("error_message")).contains("bogus"), action);
        }
        assertTrue(session.getListenRegistry().isEmpty(), "nothing may be registered on invalid input");
    }

    @Test
    void execute_shouldValidateFieldsAgainstDeviceTypeTable() {
        sessionManager.createSession("uuid", "ws");
        // wind_speed 只存在于 drone 表
        Map<String, Object> result = mockTool.execute(Map.of("action", "read", "device_sn", DOCK_SN,
                "device_type", "dock", "fields", List.of("wind_speed")));
        assertEquals(false, result.get("success"));
    }

    // ==================== catalog ====================

    @Test
    @SuppressWarnings("unchecked")
    void catalog_shouldListBothTables_whenDeviceTypeOmitted() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "catalog"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> drone = (List<Map<String, Object>>) data.get("drone");
        List<Map<String, Object>> dock = (List<Map<String, Object>>) data.get("dock");
        assertEquals(40, drone.size());
        assertEquals(34, dock.size());
        assertEquals("activation_time", drone.get(0).get("name"));
        assertTrue(drone.stream().anyMatch(item -> "wind_speed".equals(item.get("name"))
                && "listenAircraftWindSpeed".equals(item.get("sdk_method"))
                && "AircraftWindSpeed".equals(item.get("payload_type"))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void catalog_shouldListOnlyRequestedTable() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> data = (Map<String, Object>) mockTool.execute(
                Map.of("action", "catalog", "device_type", "dock")).get("data");

        assertTrue(data.containsKey("dock"));
        assertFalse(data.containsKey("drone"));
    }

    // ==================== mock 模式 ====================

    @Test
    @SuppressWarnings("unchecked")
    void read_shouldReturnMockPayloads_inMockMode() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "read", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed", "wind_speed", "attitude")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("_mock"));
        Map<String, Object> fields = (Map<String, Object>) data.get("fields");
        assertEquals(List.of("wind_speed", "attitude"), List.copyOf(fields.keySet()), "duplicates collapsed");
        Map<String, Object> wind = (Map<String, Object>) fields.get("wind_speed");
        assertTrue(wind.containsKey("wind_direction"));
        assertEquals(true, wind.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void start_shouldRegisterAndBufferMockEvent_inMockMode() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> started = mockTool.execute(Map.of("action", "start", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed", "speed")));

        assertEquals(true, started.get("success"));
        assertEquals(true, ((Map<String, Object>) started.get("data")).get("_mock"));
        assertEquals(2, session.getListenRegistry().size());
        assertTrue(session.getListenRegistry().containsKey("osd::DRONE-OSD-001::wind_speed"));

        Map<String, Object> data = (Map<String, Object>) mockTool.execute(
                Map.of("action", "events", "device_sn", DRONE_SN)).get("data");
        Map<String, Object> streams = (Map<String, Object>) data.get("streams");
        assertEquals(List.of("speed", "wind_speed"), List.copyOf(streams.keySet()));
        Map<String, Object> wind = (Map<String, Object>) streams.get("wind_speed");
        assertEquals(true, wind.get("active"));
        assertEquals(1, wind.get("count"));
        List<Map<String, Object>> events = (List<Map<String, Object>>) wind.get("events");
        assertNotNull(events.get(0).get("_seq"));
        assertNotNull(events.get(0).get("_ts"));
        assertEquals(1L, wind.get("last_seq"));
    }

    @Test
    void start_shouldKeepSingleRegistration_whenCalledTwice() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> args = Map.of("action", "start", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed"));
        mockTool.execute(args);
        mockTool.execute(args);

        assertEquals(1, session.getListenRegistry().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void stop_shouldClearRegistryAndBuffer_andBeIdempotent() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        mockTool.execute(Map.of("action", "start", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed", "speed")));

        Map<String, Object> stopped = mockTool.execute(Map.of("action", "stop", "device_sn", DRONE_SN));
        assertEquals(true, stopped.get("success"));
        assertEquals(List.of("speed", "wind_speed"),
                ((Map<String, Object>) stopped.get("data")).get("stopped"));
        assertTrue(session.getListenRegistry().isEmpty());

        Map<String, Object> events = (Map<String, Object>) mockTool.execute(Map.of("action", "events",
                "device_sn", DRONE_SN, "fields", List.of("wind_speed"))).get("data");
        Map<String, Object> wind = (Map<String, Object>) ((Map<String, Object>) events.get("streams")).get("wind_speed");
        assertEquals(false, wind.get("active"));
        assertEquals(0, wind.get("count"));

        Map<String, Object> again = mockTool.execute(Map.of("action", "stop", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed")));
        assertEquals(true, again.get("success"));
        assertEquals(List.of("wind_speed"), ((Map<String, Object>) again.get("data")).get("not_active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void stop_shouldOnlyAffectRequestedDevice() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        mockTool.execute(Map.of("action", "start", "device_sn", DRONE_SN, "fields", List.of("wind_speed")));
        mockTool.execute(Map.of("action", "start", "device_sn", DOCK_SN, "device_type", "dock",
                "fields", List.of("alarm_state")));

        mockTool.execute(Map.of("action", "stop", "device_sn", DRONE_SN));

        assertEquals(1, session.getListenRegistry().size());
        assertTrue(session.getListenRegistry().containsKey("osd::DOCK-OSD-001::alarm_state"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void events_shouldHintToStart_whenNothingActive() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = mockTool.execute(Map.of("action", "events", "device_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        assertTrue(((Map<String, Object>) ((Map<String, Object>) result.get("data")).get("streams")).isEmpty());
    }

    // ==================== 真实模式：read ====================

    private FhOsdListenTool realTool(Drone drone, Dock dock) {
        SdkService realSdk = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return drone; }
            @Override public Dock getDock(String sn) { return dock; }
        };
        return new FhOsdListenTool(sessionManager, realSdk, buffer);
    }

    private static Drone droneWith(DroneOsdModule module) {
        Drone drone = mock(Drone.class);
        when(drone.getDroneOsdModule()).thenReturn(module);
        return drone;
    }

    private static void fireOnListen(DroneOsdModule module, AircraftWindSpeed payload, int listenId) {
        when(module.listenAircraftWindSpeed(any())).thenAnswer(inv -> {
            FlightHubCallback.AircraftWindSpeedCallback cb = inv.getArgument(0);
            cb.onResult(payload);
            return listenId;
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void read_shouldReturnFirstFrameAndCancelListen_whenCallbackFires() {
        sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        fireOnListen(module, new AircraftWindSpeed(180, 6.5), 11);

        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "read", "device_sn", DRONE_SN, "fields", List.of("wind_speed")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNull(data.get("_mock"));
        Map<String, Object> wind = (Map<String, Object>) ((Map<String, Object>) data.get("fields")).get("wind_speed");
        assertEquals(180, wind.get("wind_direction"));
        assertEquals(6.5, wind.get("wind_speed"));
        assertEquals(List.of(), data.get("timed_out"));
        verify(module).cancelListenAircraftWindSpeed(11);
    }

    @Test
    @SuppressWarnings("unchecked")
    void read_shouldClassifyTimedOutAndRejectedFields_withoutFailing() {
        sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        fireOnListen(module, new AircraftWindSpeed(1, 2.0), 11);
        when(module.listenAircraftAttitude(any())).thenReturn(12);
        when(module.listenAircraftSpeed(any())).thenReturn(-1);
        when(module.listenAircraftGear(any())).thenThrow(new IllegalStateException("boom"));

        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "read", "device_sn", DRONE_SN, "timeout_ms", 50,
                "fields", List.of("wind_speed", "attitude", "speed", "gear")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(List.of("wind_speed"), List.copyOf(((Map<String, Object>) data.get("fields")).keySet()));
        assertEquals(List.of("attitude"), data.get("timed_out"));
        assertEquals(List.of("speed", "gear"), data.get("rejected"));
        verify(module).cancelListenAircraftWindSpeed(11);
        verify(module).cancelListenAircraftAttitude(12);
        verify(module, never()).cancelListenAircraftSpeed(anyInt());
    }

    @Test
    void read_shouldFail_whenNoFieldReceivedData() {
        sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftAttitude(any())).thenReturn(12);

        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "read", "device_sn", DRONE_SN, "timeout_ms", 50, "fields", List.of("attitude")));

        assertEquals(false, result.get("success"));
        verify(module).cancelListenAircraftAttitude(12);
    }

    @Test
    void read_shouldStillCompleteWhenCancelThrows() {
        sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        fireOnListen(module, new AircraftWindSpeed(1, 2.0), 11);
        doThrow(new IllegalStateException("offline")).when(module).cancelListenAircraftWindSpeed(11);

        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "read", "device_sn", DRONE_SN, "fields", List.of("wind_speed")));

        assertEquals(true, result.get("success"));
    }

    @Test
    void read_shouldFail_whenDeviceNotFound() {
        sessionManager.createSession("uuid", "ws");
        for (String type : List.of("drone", "dock")) {
            Map<String, Object> result = realTool(null, null).execute(Map.of(
                    "action", "read", "device_sn", "GHOST", "device_type", type,
                    "fields", List.of(type.equals("drone") ? "wind_speed" : "alarm_state")));
            assertEquals(false, result.get("success"), type);
        }
    }

    @Test
    void read_shouldFailAndRestoreInterruptFlag_whenInterrupted() {
        sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftAttitude(any())).thenReturn(12);

        Thread.currentThread().interrupt();
        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "read", "device_sn", DRONE_SN, "fields", List.of("attitude")));

        assertEquals(false, result.get("success"));
        assertTrue(Thread.currentThread().isInterrupted());
        verify(module).cancelListenAircraftAttitude(12);
    }

    @Test
    @SuppressWarnings("unchecked")
    void read_shouldUseDockModule_whenDeviceTypeDock() {
        sessionManager.createSession("uuid", "ws");
        DockOsdModule module = mock(DockOsdModule.class);
        Dock dock = mock(Dock.class);
        when(dock.getDockOsdModule()).thenReturn(module);
        DockAlarmState alarm = new DockAlarmState();
        alarm.setAlarmState(3);
        when(module.listenDockAlarmState(any())).thenAnswer(inv -> {
            FlightHubCallback.DockAlarmStateCallback cb = inv.getArgument(0);
            cb.onResult(alarm);
            return 21;
        });

        Map<String, Object> result = realTool(null, dock).execute(Map.of(
                "action", "read", "device_sn", DOCK_SN, "device_type", "dock", "fields", List.of("alarm_state")));

        assertEquals(true, result.get("success"));
        Map<String, Object> fields = (Map<String, Object>) ((Map<String, Object>) result.get("data")).get("fields");
        assertEquals(3, ((Map<String, Object>) fields.get("alarm_state")).get("alarm_state"));
        verify(module).cancelListenDockAlarmState(21);
    }

    // ==================== 真实模式：start / events / stop ====================

    @Test
    @SuppressWarnings("unchecked")
    void start_shouldBufferCallbackPayloads_andSupportIncrementalEvents() {
        sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        AtomicReference<FlightHubCallback.AircraftWindSpeedCallback> captured = new AtomicReference<>();
        when(module.listenAircraftWindSpeed(any())).thenAnswer(inv -> {
            captured.set(inv.getArgument(0));
            return 31;
        });
        FhOsdListenTool tool = realTool(droneWith(module), null);

        Map<String, Object> started = tool.execute(Map.of("action", "start", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed")));
        assertEquals(true, started.get("success"));
        assertEquals(Map.of("wind_speed", 31),
                ((Map<String, Object>) started.get("data")).get("listen_ids"));

        captured.get().onResult(new AircraftWindSpeed(10, 1.0));
        captured.get().onResult(new AircraftWindSpeed(20, 2.0));

        Map<String, Object> all = streamOf(tool.execute(Map.of("action", "events", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed"))), "wind_speed");
        assertEquals(2, all.get("count"));
        assertEquals(2L, all.get("last_seq"));

        Map<String, Object> incremental = streamOf(tool.execute(Map.of("action", "events",
                "device_sn", DRONE_SN, "fields", List.of("wind_speed"), "since_seq", 1, "limit", 10)), "wind_speed");
        List<Map<String, Object>> events = (List<Map<String, Object>>) incremental.get("events");
        assertEquals(1, events.size());
        assertEquals(20, events.get(0).get("wind_direction"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> streamOf(Map<String, Object> result, String field) {
        assertEquals(true, result.get("success"));
        Map<String, Object> streams = (Map<String, Object>) ((Map<String, Object>) result.get("data")).get("streams");
        return (Map<String, Object>) streams.get(field);
    }

    @Test
    void start_shouldRollbackAndFail_whenSdkReturnsNegativeId() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenReturn(42);
        when(module.listenAircraftSpeed(any())).thenReturn(-1);

        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "start", "device_sn", DRONE_SN, "fields", List.of("wind_speed", "speed")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty());
        verify(module).cancelListenAircraftWindSpeed(42);
    }

    @Test
    void start_shouldRollbackAndFail_whenSdkListenThrows() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenReturn(42);
        when(module.listenAircraftSpeed(any())).thenThrow(new IllegalStateException("boom"));

        Map<String, Object> result = realTool(droneWith(module), null).execute(Map.of(
                "action", "start", "device_sn", DRONE_SN, "fields", List.of("wind_speed", "speed")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty());
        verify(module).cancelListenAircraftWindSpeed(42);
    }

    @Test
    void start_shouldFail_whenDeviceNotFound() {
        SessionContext session = sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = realTool(null, null).execute(Map.of(
                "action", "start", "device_sn", "GHOST", "fields", List.of("wind_speed")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty());
    }

    @Test
    void start_shouldCancelPreviousSdkListen_whenRestartingSameField() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenReturn(1, 2);
        FhOsdListenTool tool = realTool(droneWith(module), null);
        Map<String, Object> args = Map.of("action", "start", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed"));

        tool.execute(args);
        tool.execute(args);

        verify(module).cancelListenAircraftWindSpeed(1);
        assertEquals(1, session.getListenRegistry().size());
    }

    @Test
    void stop_shouldCancelSdkListen() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenReturn(77);
        FhOsdListenTool tool = realTool(droneWith(module), null);
        tool.execute(Map.of("action", "start", "device_sn", DRONE_SN, "fields", List.of("wind_speed")));

        Map<String, Object> result = tool.execute(Map.of("action", "stop", "device_sn", DRONE_SN));

        assertEquals(true, result.get("success"));
        verify(module).cancelListenAircraftWindSpeed(77);
        assertTrue(session.getListenRegistry().isEmpty());
    }

    @Test
    void stop_shouldReportFailure_whenSdkCancelThrows() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenReturn(77);
        doThrow(new IllegalStateException("offline")).when(module).cancelListenAircraftWindSpeed(77);
        FhOsdListenTool tool = realTool(droneWith(module), null);
        tool.execute(Map.of("action", "start", "device_sn", DRONE_SN, "fields", List.of("wind_speed")));

        Map<String, Object> result = tool.execute(Map.of("action", "stop", "device_sn", DRONE_SN));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty(), "local registration is removed even if SDK cancel fails");
    }

    @Test
    void closeSession_shouldCancelActiveOsdListens() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        DroneOsdModule module = mock(DroneOsdModule.class);
        when(module.listenAircraftWindSpeed(any())).thenReturn(88);
        realTool(droneWith(module), null).execute(Map.of("action", "start", "device_sn", DRONE_SN,
                "fields", List.of("wind_speed")));

        sessionManager.closeSession(session.getSessionId());

        verify(module).cancelListenAircraftWindSpeed(88);
    }
}
