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
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhPayloadAcquireTool 和 FhPayloadReleaseTool 单元测试。
 * 覆盖 session 校验、前置条件、幂等保护及 mock 模式正常流程。
 *
 * @author DJI FlightHub SDK Team
 */
class FhPayloadAcquireToolTest {

    private static final String DRONE_SN = "DRONE-TEST-001";
    private static final String PAYLOAD_INDEX = "81-0-0";
    private static final String ANOTHER_PAYLOAD_INDEX = "53-0-0";

    private SessionManager sessionManager;
    private FhPayloadAcquireTool acquireTool;
    private FhPayloadReleaseTool releaseTool;

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
        acquireTool = new FhPayloadAcquireTool(sessionManager, sdkService);
        releaseTool = new FhPayloadReleaseTool(sessionManager, sdkService);
    }

    // ==================== FhPayloadAcquireTool Tests ====================

    @Test
    void acquire_shouldReturnError_whenNoSession() {
        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void acquire_shouldReturnError_whenDroneSnMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = acquireTool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Missing required parameter: drone_sn"));
    }

    @Test
    void acquire_shouldReturnError_whenNoFlightControlAuthority() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Flight control authority not acquired"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void acquire_shouldSucceed_withDefaultPayloadIndex() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals("81-0-0", data.get("payload_index"));
        assertEquals(true, data.get("granted"));

        assertTrue(session.hasPayloadAuthorityOf(DRONE_SN, "81-0-0"));
        assertNotNull(result.get("warning"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void acquire_shouldSucceed_withExplicitPayloadIndex() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "payload_index", ANOTHER_PAYLOAD_INDEX
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(ANOTHER_PAYLOAD_INDEX, data.get("payload_index"));
        assertTrue(session.hasPayloadAuthorityOf(DRONE_SN, ANOTHER_PAYLOAD_INDEX));
    }

    @Test
    void acquire_shouldReturnError_whenAlreadyHoldingSamePayload() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActivePayloadDevice(DRONE_SN, PAYLOAD_INDEX);

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "payload_index", PAYLOAD_INDEX
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Already holding payload control"));
    }

    @Test
    void acquire_shouldReturnError_whenHoldingDifferentPayload() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActivePayloadDevice(DRONE_SN, PAYLOAD_INDEX);

        Map<String, Object> result = acquireTool.execute(Map.of(
                "drone_sn", DRONE_SN,
                "payload_index", ANOTHER_PAYLOAD_INDEX
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Already controlling payload"));
    }

    // ==================== FhPayloadReleaseTool Tests ====================

    @Test
    void release_shouldReturnError_whenNoSession() {
        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void release_shouldReturnError_whenNoPayloadAuthority() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No payload control authority held"));
    }

    @Test
    void release_shouldReturnError_whenWrongDrone() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActivePayloadDevice("OTHER-DRONE", PAYLOAD_INDEX);

        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Not controlling payload of drone"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void release_shouldSucceed_inMockMode() {
        SessionContext session = sessionManager.createSession("uuid", "ws");
        session.setActiveControlDevice(DRONE_SN);
        session.setActivePayloadDevice(DRONE_SN, PAYLOAD_INDEX);

        Map<String, Object> result = releaseTool.execute(Map.of(
                "drone_sn", DRONE_SN
        ));

        assertEquals(true, result.get("success"));

        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(DRONE_SN, data.get("drone_sn"));
        assertEquals(PAYLOAD_INDEX, data.get("payload_index"));
        assertEquals(true, data.get("released"));

        assertFalse(session.hasPayloadAuthority());
    }

    // ==================== getToolDefinition Tests ====================

    @Test
    void getToolDefinition_shouldHaveCorrectName_acquire() {
        Map<String, Object> def = acquireTool.getToolDefinition();
        assertEquals("fh_payload_acquire", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    @Test
    void getToolDefinition_shouldHaveCorrectName_release() {
        Map<String, Object> def = releaseTool.getToolDefinition();
        assertEquals("fh_payload_release", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }
}
