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

package com.dji.flighthub.mcp.tools.airspace;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDeviceRecommendTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、必填参数缺失、Mock 正常返回。
 *
 * @author DJI FlightHub SDK Team
 */
class FhDeviceRecommendToolTest {

    private SessionManager sessionManager;
    private FhDeviceRecommendTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService();
        tool = new FhDeviceRecommendTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectNameAndSchema() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_device_recommend", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("candidate_sns"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of(
                "candidate_sns", List.of("DRONE-001")
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenCandidateSnsMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(new HashMap<>());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("candidate_sns"));
    }

    @Test
    void execute_shouldReturnError_whenCandidateSnsEmpty() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "candidate_sns", List.of()
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("candidate_sns"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockRecommendation_whenValidInput() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "candidate_sns", List.of("DRONE-001", "DRONE-002"),
                "strategy_id", 0
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data);
        assertEquals("DRONE-001", data.get("recommended_sn"));
        assertNotNull(data.get("scores"));
        assertEquals(true, data.get("_mock"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldValidateCoordinates_whenFlyToProvided() {
        sessionManager.createSession("uuid", "ws");

        // 无效纬度
        Map<String, Object> result = tool.execute(Map.of(
                "candidate_sns", List.of("DRONE-001"),
                "fly_to_latitude", 100.0,
                "fly_to_longitude", 113.0,
                "fly_to_height", 50.0
        ));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("latitude"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldSucceed_whenValidCoordinatesProvided() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of(
                "candidate_sns", List.of("DRONE-001"),
                "fly_to_latitude", 22.5,
                "fly_to_longitude", 113.9,
                "fly_to_height", 100.0
        ));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals(true, data.get("has_fly_to_position"));
    }
}
