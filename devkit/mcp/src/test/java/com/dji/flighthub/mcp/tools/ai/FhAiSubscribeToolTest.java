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
import com.dji.flighthubsdk.core.Drone;
import com.dji.flighthubsdk.core.OnboardAiModule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhAiSubscribeTool 单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
@ExtendWith(ElicitTestSupport.AutoClear.class)
class FhAiSubscribeToolTest {

    private static final String DEVICE_SN = "DRONE-SUB-001";
    private SessionManager sessionManager;
    private AiSubscriptionBuffer buffer;
    private FhAiSubscribeTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);
        buffer = new AiSubscriptionBuffer();
        SdkService sdkService = new SdkService() {
            @Override public boolean isMockMode() { return true; }
        };
        tool = new FhAiSubscribeTool(sessionManager, sdkService, buffer);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        assertEquals("fh_ai_subscribe", tool.getToolDefinition().get("name"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenMissingDeviceSn() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of("action", "start"));
        assertEquals(false, result.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockListenIds_whenStartAction() {
        sessionManager.createSession("uuid", "ws").setActiveDrcDevice(DEVICE_SN);
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data", "alarm_state")));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertTrue((boolean) data.get("_mock"));
        assertNotNull(data.get("listen_ids"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnEvents_whenEventsAction() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data")));

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "events", "channel", "ai_data"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("events"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnSuccess_whenStopAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data")));

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop"));

        assertEquals(true, result.get("success"));
        assertTrue(session.getActiveListenIds().isEmpty());
    }

    @Test
    void execute_shouldReturnError_whenInvalidChannel() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data", "bogus")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty(), "no subscription may be registered on invalid input");
    }

    @Test
    void execute_shouldKeepSingleRegistration_whenStartCalledTwice() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> args = Map.of("device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data"));
        tool.execute(args);
        tool.execute(args);

        assertEquals(1, session.getListenRegistry().size());
    }

    @Test
    void execute_shouldClearRegistry_whenStopAction() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveDrcDevice(DEVICE_SN);
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "start"));
        assertEquals(2, session.getListenRegistry().size());

        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "stop"));

        assertTrue(session.getListenRegistry().isEmpty());
        assertTrue(session.getActiveListenIds().isEmpty());
    }

    @Test
    void execute_shouldReturnSuccess_whenStopCalledTwice() {
        sessionManager.createSession("uuid", "ws");
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data")));
        tool.execute(Map.of("device_sn", DEVICE_SN, "action", "stop"));

        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop"));
        assertEquals(true, result.get("success"));
    }

    @Test
    void execute_shouldRequireDrcLink_whenAlarmStateChannel() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("alarm_state")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty());
    }

    @Test
    void execute_shouldNotRequireDrcLink_whenAiDataOnly() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data")));

        assertEquals(true, result.get("success"));
    }

    @Test
    void execute_shouldReturnError_whenEventsChannelInvalid() {
        sessionManager.createSession("uuid", "ws");
        Map<String, Object> result = tool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "events", "channel", "bogus"));
        assertEquals(false, result.get("success"));
    }

    @Test
    void execute_shouldRollbackAndFail_whenSdkReturnsNegativeListenId() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager manager = new SessionManager(properties);
        SessionContext session = manager.createSession("uuid", "ws");
        session.setActiveDrcDevice(DEVICE_SN);

        Drone drone = mock(Drone.class);
        OnboardAiModule module = mock(OnboardAiModule.class);
        when(drone.getOnboardAiModule()).thenReturn(module);
        when(module.listenAIDataInfo(any())).thenReturn(42);
        when(module.listenDrcAIAlarmState(any())).thenReturn(-1);
        SdkService realMode = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return drone; }
        };
        FhAiSubscribeTool realTool = new FhAiSubscribeTool(manager, realMode, buffer);

        Map<String, Object> result = realTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data", "alarm_state")));

        assertEquals(false, result.get("success"));
        assertTrue(session.getListenRegistry().isEmpty(), "channels started in this call must be rolled back");
        verify(module).cancelListenAIDataInfo(42);
    }

    @Test
    void execute_shouldCancelSdkListen_whenStopAfterRealStart() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager manager = new SessionManager(properties);
        SessionContext session = manager.createSession("uuid", "ws");

        Drone drone = mock(Drone.class);
        OnboardAiModule module = mock(OnboardAiModule.class);
        when(drone.getOnboardAiModule()).thenReturn(module);
        when(module.listenAIDataInfo(any())).thenReturn(77);
        SdkService realMode = new SdkService() {
            @Override public boolean isMockMode() { return false; }
            @Override public Drone getDrone(String sn) { return drone; }
        };
        FhAiSubscribeTool realTool = new FhAiSubscribeTool(manager, realMode, buffer);

        realTool.execute(Map.of("device_sn", DEVICE_SN, "action", "start",
                "channels", List.of("ai_data")));
        assertEquals(1, session.getListenRegistry().size());

        Map<String, Object> result = realTool.execute(Map.of(
                "device_sn", DEVICE_SN, "action", "stop", "channels", List.of("ai_data")));

        assertEquals(true, result.get("success"));
        verify(module).cancelListenAIDataInfo(77);
        assertTrue(session.getListenRegistry().isEmpty());
    }
}
