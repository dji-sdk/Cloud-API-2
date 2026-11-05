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

package com.dji.flighthub.mcp.tools.maintenance;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.DeviceRepairConfig;
import com.dji.flighthubsdk.bean.RepairGuidance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhMaintenanceTool 的 SDK bean → 结构化 Map 映射单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMaintenanceBeanMapperTest {

    private FhMaintenanceTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhMaintenanceTool(sessionManager, new SdkService());
    }

    // ==================== buildRepairConfigs ====================

    @Test
    void buildRepairConfigs_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildRepairConfigs(null).isEmpty());
    }

    @Test
    void buildRepairConfigs_shouldSkipNullElements() {
        List<DeviceRepairConfig> configs = new ArrayList<>(Arrays.asList(new DeviceRepairConfig(), null));

        assertEquals(1, tool.buildRepairConfigs(configs).size());
    }

    @Test
    void buildRepairConfigs_shouldMapAllFields() {
        RepairGuidance guidance = new RepairGuidance();
        guidance.setRedirectUrl("https://example.com/repair");
        guidance.setDescription("请返厂维修");

        DeviceRepairConfig config = new DeviceRepairConfig();
        config.setDeviceSn("DOCK-001");
        config.setSupportRepair(true);
        config.setAllowedSourceTypes(List.of(1, 2));
        config.setRepairGuidance(guidance);

        Map<String, Object> row = tool.buildRepairConfigs(List.of(config)).get(0);

        assertEquals(List.of("device_sn", "support_repair", "allowed_source_types", "repair_guidance"),
                List.copyOf(row.keySet()));
        assertEquals("DOCK-001", row.get("device_sn"));
        assertEquals(true, row.get("support_repair"));
        assertEquals(List.of(1, 2), row.get("allowed_source_types"));

        @SuppressWarnings("unchecked")
        Map<String, Object> mappedGuidance = (Map<String, Object>) row.get("repair_guidance");
        assertEquals(List.of("redirect_url", "description"), List.copyOf(mappedGuidance.keySet()));
        assertEquals("https://example.com/repair", mappedGuidance.get("redirect_url"));
        assertEquals("请返厂维修", mappedGuidance.get("description"));
    }

    @Test
    void buildRepairConfigs_shouldReturnEmptyAllowedTypes_whenNull() {
        DeviceRepairConfig config = new DeviceRepairConfig();
        config.setDeviceSn("DOCK-002");

        Map<String, Object> row = tool.buildRepairConfigs(List.of(config)).get(0);

        assertEquals(List.of(), row.get("allowed_source_types"));
        assertNull(row.get("repair_guidance"));
    }

    // ==================== buildRepairGuidance ====================

    @Test
    void buildRepairGuidance_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildRepairGuidance(null));
    }

    @Test
    void buildRepairGuidance_shouldMapAllFields() {
        RepairGuidance guidance = new RepairGuidance();
        guidance.setRedirectUrl("https://example.com/repair");
        guidance.setDescription("请返厂维修");

        Map<String, Object> row = tool.buildRepairGuidance(guidance);

        assertEquals(List.of("redirect_url", "description"), List.copyOf(row.keySet()));
        assertEquals("https://example.com/repair", row.get("redirect_url"));
        assertEquals("请返厂维修", row.get("description"));
    }
}
