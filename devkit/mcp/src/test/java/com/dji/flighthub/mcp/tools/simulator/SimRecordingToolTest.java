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
 * {@link SimRecordingTool} 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class SimRecordingToolTest {

    private FakeSimulatorClient fakeClient;
    private FlightHubProperties properties;
    private SimulatorProperties simProperties;
    private SimRecordingTool tool;

    @BeforeEach
    void setUp() {
        properties = new FlightHubProperties();
        simProperties = new SimulatorProperties();
        simProperties.setPassword("a-strong-test-secret");
        SessionManager sessionManager = new SessionManager(properties);
        fakeClient = new FakeSimulatorClient();
        tool = new SimRecordingTool(sessionManager, fakeClient, properties, simProperties);
    }

    @Test
    void getToolDefinition_shouldReturnNameAndActionEnum() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("sim_recording", def.get("name"));
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        Map<String, Object> actionProp = (Map<String, Object>) props.get("action");
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) actionProp.get("enum");
        assertTrue(actions.contains("promote"));
        assertTrue(actions.contains("list_templates"));
    }

    @Test
    void execute_shouldReturnError_whenUnknownAction() {
        Map<String, Object> result = tool.execute(Map.of("action", "bogus"));

        assertFalse((Boolean) result.get("success"));
    }

    @Test
    void execute_shouldList() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/recordings", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenGetMissingId() {
        Map<String, Object> result = tool.execute(Map.of("action", "get"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing id", result.get("error_message"));
    }

    @Test
    void execute_shouldGet() {
        fakeClient.enqueueResponse(Map.of("id", "rec-1"));

        Map<String, Object> result = tool.execute(Map.of("action", "get", "id", "rec-1"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/recordings/rec-1", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldListCandidates() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_candidates", "id", "rec-1"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/recordings/rec-1/candidates", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldListTemplates_withProductTypeFilter() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_templates", "product_type", "M350"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/templates?product_type=M350", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldListTemplates_withoutFilter() {
        fakeClient.enqueueResponse(Map.of("items", List.of()));

        Map<String, Object> result = tool.execute(Map.of("action", "list_templates"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/templates", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenGetTemplateMissingTemplateId() {
        Map<String, Object> result = tool.execute(Map.of("action", "get_template"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing template_id", result.get("error_message"));
    }

    @Test
    void execute_shouldGetTemplate() {
        fakeClient.enqueueResponse(Map.of("id", "M350:drc_state"));

        Map<String, Object> result = tool.execute(Map.of("action", "get_template", "template_id", "M350:drc_state"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/templates/M350:drc_state", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenStartMissingSourceSns() {
        Map<String, Object> result = tool.execute(Map.of("action", "start", "product_type", "M350"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing source_sns", result.get("error_message"));
    }

    @Test
    void execute_shouldReturnError_whenStartMissingProductType() {
        Map<String, Object> result = tool.execute(Map.of("action", "start", "source_sns", List.of("SN001")));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing product_type", result.get("error_message"));
    }

    @Test
    void execute_shouldStart() {
        fakeClient.enqueueResponse(Map.of("id", "rec-1"));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "start", "source_sns", List.of("SN001"), "product_type", "M350", "duration_sec", 60));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/recordings", fakeClient.lastCall().path());
        assertEquals("POST", fakeClient.lastCall().method());
    }

    @Test
    void execute_shouldReturnError_whenStopMissingId() {
        Map<String, Object> result = tool.execute(Map.of("action", "stop"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing id", result.get("error_message"));
    }

    @Test
    void execute_shouldStop() {
        fakeClient.enqueueResponse(Map.of());

        Map<String, Object> result = tool.execute(Map.of("action", "stop", "id", "rec-1"));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/recordings/rec-1/stop", fakeClient.lastCall().path());
    }

    @Test
    void execute_shouldReturnError_whenPromoteMissingMethods() {
        Map<String, Object> result = tool.execute(Map.of("action", "promote", "id", "rec-1"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Missing methods", result.get("error_message"));
    }

    @Test
    void execute_shouldPromote_andMessageMentionsRestart() {
        fakeClient.enqueueResponse(Map.of("promoted_paths", List.of("M350/drc_state.json")));

        Map<String, Object> result = tool.execute(Map.of(
                "action", "promote", "id", "rec-1", "methods", List.of("drc_state")));

        assertTrue((Boolean) result.get("success"));
        assertEquals("/simulator/api/v1/recordings/rec-1/promote", fakeClient.lastCall().path());
        String message = (String) result.get("message");
        assertTrue(message.contains("Restart the simulator process"));
    }

    @Test
    void execute_shouldDenyStop_whenSecurityLevelBelowRequired() {
        FlightHubProperties restricted = new FlightHubProperties();
        restricted.getSecurity().setMaxLevel(0);
        SessionManager restrictedSessionManager = new SessionManager(restricted);
        SimRecordingTool restrictedTool = new SimRecordingTool(restrictedSessionManager, fakeClient, restricted, simProperties);

        Map<String, Object> result = restrictedTool.execute(Map.of("action", "stop", "id", "rec-1"));

        assertFalse((Boolean) result.get("success"));
        assertEquals("Operation denied: insufficient security level", result.get("error_message"));
    }
}
