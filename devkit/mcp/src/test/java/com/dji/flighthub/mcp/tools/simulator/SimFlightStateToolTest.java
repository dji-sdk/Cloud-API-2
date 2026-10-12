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

package com.dji.flighthub.mcp.tools.simulator;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.simulator.SimulatorProperties;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.simulator.FakeSimulatorClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimFlightStateTool} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SimFlightStateToolTest {

    private FakeSimulatorClient fakeClient;
    private FlightHubProperties properties;
    private SimulatorProperties simProperties;
    private SimFlightStateTool tool;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        simProperties = new SimulatorProperties();
        simProperties.setPassword("a-strong-test-secret");
        SessionManager sessionManager = new SessionManager(properties);
        fakeClient = new FakeSimulatorClient();
        tool = new SimFlightStateTool(sessionManager, fakeClient, properties, simProperties);
    }

    @Test
    void getToolDefinition_shouldReturnNameAndActionEnum() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("sim_flight_state", def.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) actionProp.get("enum");
        assertTrue(actions.contains("flyto"));
        assertTrue(actions.contains("reset"));
    }

    @Test
    void execute_shouldReturnError_whenMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "get_flight_state"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnError_whenUnknownAction() {
        Map<String, Object> result = tool.execute(Map.of("action", "bogus", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void execute_shouldGetFlightState() {
        fakeClient.enqueueResponse(Map.of("mode", "HOVERING"));

        Map<String, Object> result = tool.execute(Map.of("action", "get_flight_state", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("GET", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/drones/SN001/flight/state", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenInjectStateMissingDataJson() {
        Map<String, Object> result = tool.execute(Map.of("action", "inject_state", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing data_json", result.get("error_message"));
    }

    @Test
    void execute_shouldInjectState() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "inject_state", "sn", "SN001", "data_json", "{\"battery_percent\":50}"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices/SN001/state", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenInjectEventMissingMethod() {
        Map<String, Object> result = tool.execute(Map.of("action", "inject_event", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing method", result.get("error_message"));
    }

    @Test
    void execute_shouldInjectEvent() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "inject_event", "sn", "SN001", "method", "hms", "event_data_json", "{}"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices/SN001/event", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldUpdateFlightState() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "update_flight_state", "sn", "SN001", "patch_json", "{\"battery_percent\":80}"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("PATCH", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices/SN001/flight-state", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldResetFlight() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of("action", "reset_flight", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices/SN001/reset-flight", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldTakeoffReturnHomeAndLand() {
        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "takeoff", "sn", "SN001"));
        assertEquals("/simulator/api/v1/drones/SN001/takeoff", fakeClient.lastCall().path());

        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "return_home", "sn", "SN001"));
        assertEquals("/simulator/api/v1/drones/SN001/return-home", fakeClient.lastCall().path());

        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "land", "sn", "SN001"));
        assertEquals("/simulator/api/v1/drones/SN001/land", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenFlytoMissingCoordinates() {
        Map<String, Object> result = tool.execute(Map.of("action", "flyto", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing coordinates", result.get("error_message"));
    }

    @Test
    void execute_shouldFlyto_withYaw() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "flyto", "sn", "SN001",
                "latitude", 22.5, "longitude", 113.9, "altitude", 100.0, "yaw", 90));

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(true, body.get("set_yaw"));
        assertEquals(90, body.get("yaw"));
    }

    @Test
    void execute_shouldFlyto_withoutYaw() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "flyto", "sn", "SN001",
                "latitude", 22.5, "longitude", 113.9, "altitude", 100.0));

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(false, body.get("set_yaw"));
    }

    @Test
    void execute_shouldManual() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "manual", "sn", "SN001", "x", 1.0, "y", 0.0, "h", 0.0, "w", 0.0, "duration_ms", 500));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/drones/SN001/manual", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenPatchStateHasNoFields() {
        Map<String, Object> result = tool.execute(Map.of("action", "patch_state", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("No fields to patch", result.get("error_message"));
    }

    @Test
    void execute_shouldPatchState_withBatteryAndPowerOn() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "patch_state", "sn", "SN001", "battery", 75, "power_on", true));

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(true, body.get("set_battery"));
        assertEquals(75, body.get("battery"));
        assertEquals(true, body.get("set_power_on"));
        assertEquals(true, body.get("power_on"));
        assertEquals(false, body.get("set_position"));
    }

    @Test
    void execute_shouldPatchState_withPosition() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "patch_state", "sn", "SN001",
                "latitude", 22.5, "longitude", 113.9, "altitude", 100.0));

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(true, body.get("set_position"));
        assertEquals(22.5, body.get("latitude"));
        assertEquals(113.9, body.get("longitude"));
        assertEquals(100.0, body.get("altitude"));
        assertEquals(false, body.get("set_battery"));
        assertEquals(false, body.get("set_power_on"));
        assertEquals(false, body.get("set_attitude_head"));
        assertEquals(false, body.get("set_wireless_link"));
        assertEquals(false, body.get("set_gps"));
    }

    @Test
    void execute_shouldPatchState_withAttitudeHeadAndWirelessLink() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "patch_state", "sn", "SN001", "attitude_head", 270, "wireless_link", 4));

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(true, body.get("set_attitude_head"));
        assertEquals(270, body.get("attitude_head"));
        assertEquals(true, body.get("set_wireless_link"));
        assertEquals(4, body.get("wireless_link"));
    }

    @Test
    void execute_shouldPatchState_withGpsCountAndStatus() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of(
                "action", "patch_state", "sn", "SN001", "gps_count", 18, "gps_status", 2));

        assertTrue((Boolean) result.get("success"));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(true, body.get("set_gps"));
        assertEquals(18, body.get("gps_count"));
        assertEquals(2, body.get("gps_status"));
    }

    @Test
    void execute_shouldReset() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of("action", "reset", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/drones/SN001/flight/reset", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldDenyTakeoff_whenSecurityLevelBelowRequired() {
        FlightHubProperties restricted = new FlightHubProperties();
        restricted.getSecurity().setMaxLevel(0);
        SessionManager restrictedSessionManager = new SessionManager(restricted);
        SimFlightStateTool restrictedTool = new SimFlightStateTool(restrictedSessionManager, fakeClient, restricted, simProperties);

        Map<String, Object> result = restrictedTool.execute(Map.of("action", "takeoff", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Operation denied: insufficient security level", result.get("error_message"));
    }
}
