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
import com.dji.flighthub.mcp.simulator.SimulatorApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimDeviceTool} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SimDeviceToolTest {

    private FakeSimulatorClient fakeClient;
    private FlightHubProperties properties;
    private SimulatorProperties simProperties;
    private SimDeviceTool tool;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        simProperties = new SimulatorProperties();
        simProperties.setPassword("a-strong-test-secret");
        SessionManager sessionManager = new SessionManager(properties);
        fakeClient = new FakeSimulatorClient();
        tool = new SimDeviceTool(sessionManager, fakeClient, properties, simProperties);
    }

    @Test
    void getToolDefinition_shouldReturnNameAndActionEnum() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("sim_device", def.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) actionProp.get("enum");
        assertTrue(actions.contains("delete"));
        assertTrue(actions.contains("batch_create"));
    }

    @Test
    void execute_shouldReturnError_whenUnknownAction() {
        Map<String, Object> result = tool.execute(Map.of("action", "bogus"));

        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenGetMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "get"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldListDevices_withQueryParams() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list", "product_type", "M350"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("GET", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices?product_type=M350", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldListDevices_withProductTypeAndStatusQueryParams() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "list", "product_type", "M350", "status", "online"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("GET", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices?product_type=M350&status=online", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldGetDevice() {
        fakeClient.enqueueResponse(Map.of("device", Map.of("sn", "SN001")));

        Map<String, Object> result = tool.execute(Map.of("action", "get", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("GET", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices/SN001", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenCreateMissingDevice() {
        Map<String, Object> result = tool.execute(Map.of("action", "create"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing device", result.get("error_message"));
    }

    @Test
    void execute_shouldCreateDevice_withDevicePayload() {
        fakeClient.enqueueResponse(Map.of("sn", "SN001"));
        Map<String, Object> device = Map.of("sn", "SN001", "product_type", "M350");

        Map<String, Object> result = tool.execute(Map.of("action", "create", "device", device));

        assertTrue((Boolean) result.get("success"));
        assertEquals("POST", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices", fakeClient.lastCall().path());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(device, body.get("device"));
    }

    @Test
    void execute_shouldUpdateDevice() {
        fakeClient.enqueueResponse(Map.of("sn", "SN001"));
        Map<String, Object> device = Map.of("name", "renamed");

        Map<String, Object> result = tool.execute(Map.of("action", "update", "sn", "SN001", "device", device));

        assertTrue((Boolean) result.get("success"));
        assertEquals("PATCH", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices/SN001", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldStartAndStopDevice() {
        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "start", "sn", "SN001"));
        assertEquals("/simulator/api/v1/devices/SN001/start", fakeClient.lastCall().path());

        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "stop", "sn", "SN001"));
        assertEquals("/simulator/api/v1/devices/SN001/stop", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenSubOnlineMissingSubSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "sub_online", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sub_sn", result.get("error_message"));
    }

    @Test
    void execute_shouldSubOnlineAndSubOffline() {
        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "sub_online", "sn", "SN001", "sub_sn", "SUB01"));
        assertEquals("/simulator/api/v1/devices/SN001/sub-online", fakeClient.lastCall().path());

        fakeClient.enqueueResponse(Map.of());
        tool.execute(Map.of("action", "sub_offline", "sn", "SN001", "sub_sn", "SUB01"));
        assertEquals("/simulator/api/v1/devices/SN001/sub-offline", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenBatchCreateMissingDevices() {
        Map<String, Object> result = tool.execute(Map.of("action", "batch_create"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing devices", result.get("error_message"));
    }

    @Test
    void execute_shouldBatchCreateDevices() {
        fakeClient.enqueueResponse(Map.of("created", List.of()));
        List<Map<String, Object>> devices = List.of(Map.of("sn", "SN001"), Map.of("sn", "SN002"));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_create", "devices", devices, "auto_start", true));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices:batch", fakeClient.lastCall().path());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) fakeClient.lastCall().body();
        assertEquals(Boolean.TRUE, body.get("auto_start"));
    }

    @Test
    void execute_shouldReturnDryRunPreview_forDelete_withoutCallingSimulator() {
        Map<String, Object> result = tool.execute(Map.of("action", "delete", "sn", "SN001", "dry_run", true));

        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("_dry_run"));
        assertNull(fakeClient.lastCall());
    }

    @Test
    void execute_shouldRequireConfirm_forDelete() {
        Map<String, Object> result = tool.execute(Map.of("action", "delete", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Confirmation required", result.get("error_message"));
        assertNull(fakeClient.lastCall());
    }

    @Test
    void execute_shouldDeleteDevice_whenConfirmTrue() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of("action", "delete", "sn", "SN001", "confirm", true));

        assertTrue((Boolean) result.get("success"));
        assertEquals("DELETE", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/devices/SN001", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldRequireConfirm_forBatchDelete() {
        Map<String, Object> result = tool.execute(Map.of("action", "batch_delete", "sns", List.of("SN001", "SN002")));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Confirmation required", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnDryRunPreview_forBatchDelete_withoutCallingSimulator() {
        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_delete", "sns", List.of("SN001"), "dry_run", true));

        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("_dry_run"));
        assertNull(fakeClient.lastCall());
    }

    @Test
    void execute_shouldBatchDeleteDevices_whenConfirmTrue() {
        fakeClient.enqueueResponse(Map.of("deleted", List.of("SN001", "SN002")));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_delete", "sns", List.of("SN001", "SN002"), "confirm", true));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices:batchDelete", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenSimulatorUnreachable() {
        fakeClient.throwOnNextCall(new SimulatorApiException(0, "/simulator/api/v1/devices: connection failed"));

        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("suggestion")).contains("pf-device-simulator-service"));
    }

    @Test
    void execute_shouldDenyDelete_whenSecurityLevelBelowRequired() {
        FlightHubProperties restricted = new FlightHubProperties();
        restricted.getSecurity().setMaxLevel(0);
        SessionManager restrictedSessionManager = new SessionManager(restricted);
        SimDeviceTool restrictedTool = new SimDeviceTool(restrictedSessionManager, fakeClient, restricted, simProperties);

        Map<String, Object> result = restrictedTool.execute(Map.of("action", "delete", "sn", "SN1", "confirm", true));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Operation denied: insufficient security level", result.get("error_message"));
    }
}
