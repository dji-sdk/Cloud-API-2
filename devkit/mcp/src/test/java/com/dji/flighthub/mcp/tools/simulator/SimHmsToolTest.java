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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SimHmsTool} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SimHmsToolTest {

    private FakeSimulatorClient fakeClient;
    private FlightHubProperties properties;
    private SimulatorProperties simProperties;
    private SimHmsTool tool;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        simProperties = new SimulatorProperties();
        simProperties.setPassword("a-strong-test-secret");
        SessionManager sessionManager = new SessionManager(properties);
        fakeClient = new FakeSimulatorClient();
        tool = new SimHmsTool(sessionManager, fakeClient, properties, simProperties);
    }

    @Test
    void getToolDefinition_shouldReturnNameAndActionEnum() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("sim_hms", def.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) actionProp.get("enum");
        assertTrue(actions.contains("inject"));
        assertTrue(actions.contains("cancel_all"));
    }

    @Test
    void execute_shouldReturnError_whenUnknownAction() {
        Map<String, Object> result = tool.execute(Map.of("action", "bogus"));

        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenListMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldList() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/hms/SN001", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenListActiveBlockersMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "list_active_blockers"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldListActiveBlockers() {
        fakeClient.enqueueResponse(Map.of("codes", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_active_blockers", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/hms/SN001/blockers", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldListCodes_withDeviceTypeFilter() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_codes", "device_type", 60));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/hms:codes?device_type=60", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldListPresets() {
        fakeClient.enqueueResponse(Map.of("presets", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_presets"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/hms:presets", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenListActiveMissingSns() {
        Map<String, Object> result = tool.execute(Map.of("action", "list_active"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sns", result.get("error_message"));
    }

    @Test
    void execute_shouldListActive_withMultipleSns() {
        fakeClient.enqueueResponse(Map.of("by_sn", Map.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_active", "sns", List.of("SN001", "SN002")));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/hms:active?sns=SN001&sns=SN002", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenInjectMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "inject", "code", "0x1A000100"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnError_whenInjectMissingCode() {
        Map<String, Object> result = tool.execute(Map.of("action", "inject", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing code", result.get("error_message"));
    }

    @Test
    void execute_shouldInject() {
        fakeClient.enqueueResponse(Map.of("id", "hms-1"));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "inject", "sn", "SN001", "code", "0x1A000100", "level", 2, "module", 3));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/hms/SN001/inject", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenBatchInjectMissingSn() {
        List<Map<String, Object>> injections = List.of(Map.of("code", "0x1A000100"));

        Map<String, Object> result = tool.execute(Map.of("action", "batch_inject", "injections", injections));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnError_whenBatchInjectMissingInjections() {
        Map<String, Object> result = tool.execute(Map.of("action", "batch_inject", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing injections", result.get("error_message"));
    }

    @Test
    void execute_shouldBatchInject() {
        fakeClient.enqueueResponse(Map.of("ids", List.of("hms-1", "hms-2")));
        List<Map<String, Object>> injections = List.of(
                Map.of("code", "0x1A000100"), Map.of("code", "0x1A000101"));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "batch_inject", "sn", "SN001", "injections", injections));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices/SN001/hms:batch", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenInjectPresetMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "inject_preset", "preset_name", "low-battery"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnError_whenInjectPresetMissingPresetName() {
        Map<String, Object> result = tool.execute(Map.of("action", "inject_preset", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing preset_name", result.get("error_message"));
    }

    @Test
    void execute_shouldInjectPreset() {
        fakeClient.enqueueResponse(Map.of("ids", List.of("hms-1")));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "inject_preset", "sn", "SN001", "preset_name", "low-battery"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/devices/SN001/hms/presets/low-battery:inject", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenCancelMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "cancel", "id", "hms-1"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnError_whenCancelMissingId() {
        Map<String, Object> result = tool.execute(Map.of("action", "cancel", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing id", result.get("error_message"));
    }

    @Test
    void execute_shouldCancel() {
        fakeClient.enqueueResponse(Map.of("ok", true));

        Map<String, Object> result = tool.execute(Map.of("action", "cancel", "sn", "SN001", "id", "hms-1"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("DELETE", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/hms/SN001/hms-1", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenCancelAllMissingSn() {
        Map<String, Object> result = tool.execute(Map.of("action", "cancel_all"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing sn", result.get("error_message"));
    }

    @Test
    void execute_shouldCancelAll() {
        fakeClient.enqueueResponse(Map.of("cancelled", 3));

        Map<String, Object> result = tool.execute(Map.of("action", "cancel_all", "sn", "SN001"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("DELETE", fakeClient.lastCall().method());
        assertEquals("/simulator/api/v1/hms/SN001", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldSuggestCheckingSimulator_whenUnreachable() {
        fakeClient.throwOnNextCall(new SimulatorApiException(0, "/simulator/api/v1/hms/SN001: connection failed"));

        Map<String, Object> result = tool.execute(Map.of("action", "list", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("suggestion")).contains("pf-device-simulator-service"));
    }

    @Test
    void execute_shouldDenyCancelAll_whenSecurityLevelBelowRequired() {
        FlightHubProperties restricted = new FlightHubProperties();
        restricted.getSecurity().setMaxLevel(0);
        SessionManager restrictedSessionManager = new SessionManager(restricted);
        SimHmsTool restrictedTool = new SimHmsTool(restrictedSessionManager, fakeClient, restricted, simProperties);

        Map<String, Object> result = restrictedTool.execute(Map.of("action", "cancel_all", "sn", "SN001"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Operation denied: insufficient security level", result.get("error_message"));
    }
}
