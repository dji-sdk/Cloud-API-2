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

package com.dji.flighthub.mcp.tools.safety;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthubsdk.bean.DeviceSignalItem;
import com.dji.flighthubsdk.bean.DeviceSyncResults;
import com.dji.flighthubsdk.bean.DeviceSyncStatusResults;
import com.dji.flighthubsdk.bean.GridSignalItem;
import com.dji.flighthubsdk.bean.OfflineMapsModelData;
import com.dji.flighthubsdk.bean.OfflineMapsModelInfoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhOfflineMapTool 的 SDK bean → 结构化 Map 映射单元测试。
 *
 * @author DJI FlightHub SDK Team
 */
class FhOfflineMapBeanMapperTest {

    private FhOfflineMapTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        SessionManager sessionManager = new SessionManager(properties);
        tool = new FhOfflineMapTool(sessionManager, new SdkService());
    }

    // ==================== buildOfflineMapModel ====================

    @Test
    void buildOfflineMapModel_shouldReturnNull_whenInputNull() {
        assertNull(tool.buildOfflineMapModel(null));
    }

    @Test
    void buildOfflineMapModel_shouldReturnEmptyModels_whenModelsNull() {
        OfflineMapsModelData model = new OfflineMapsModelData();
        model.setId(9);
        model.setUpdatedTime(1_700_000_000_000L);
        model.setPercent(88);
        model.setStatus("sync");
        model.setResult(0);

        Map<String, Object> row = tool.buildOfflineMapModel(model);

        assertEquals(List.of("id", "updated_time", "percent", "status", "result", "models"),
                List.copyOf(row.keySet()));
        assertEquals(9, row.get("id"));
        assertEquals(1_700_000_000_000L, row.get("updated_time"));
        assertEquals(88, row.get("percent"));
        assertEquals("sync", row.get("status"));
        assertEquals(0, row.get("result"));
        assertEquals(List.of(), row.get("models"));
    }

    // ==================== buildModelInfos ====================

    @Test
    void buildModelInfos_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildModelInfos(null).isEmpty());
    }

    @Test
    void buildModelInfos_shouldSkipNullElements() {
        OfflineMapsModelInfoData info = new OfflineMapsModelInfoData();
        info.setUuid("model-uuid-1");
        List<OfflineMapsModelInfoData> models = new ArrayList<>(Arrays.asList(info, null));

        List<Map<String, Object>> mapped = tool.buildModelInfos(models);

        assertEquals(1, mapped.size());
        assertEquals(List.of("uuid"), List.copyOf(mapped.get(0).keySet()));
        assertEquals("model-uuid-1", mapped.get(0).get("uuid"));
    }

    // ==================== buildDeviceSyncResults ====================

    @Test
    void buildDeviceSyncResults_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildDeviceSyncResults(null).isEmpty());
    }

    @Test
    void buildDeviceSyncResults_shouldMapFields() {
        DeviceSyncResults result = new DeviceSyncResults();
        result.setDeviceSn("DOCK-001");
        result.setCode(0);

        List<Map<String, Object>> mapped = tool.buildDeviceSyncResults(List.of(result));

        assertEquals(1, mapped.size());
        assertEquals(List.of("device_sn", "code"), List.copyOf(mapped.get(0).keySet()));
        assertEquals("DOCK-001", mapped.get(0).get("device_sn"));
        assertEquals(0, mapped.get(0).get("code"));
    }

    // ==================== buildDeviceSyncStatuses ====================

    @Test
    void buildDeviceSyncStatuses_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildDeviceSyncStatuses(null).isEmpty());
    }

    @Test
    void buildDeviceSyncStatuses_shouldMapSnToDeviceSn() {
        DeviceSyncStatusResults status = new DeviceSyncStatusResults();
        status.setSn("DOCK-002");
        status.setStatus("synced");
        status.setReason(3);

        List<Map<String, Object>> mapped = tool.buildDeviceSyncStatuses(List.of(status));

        assertEquals(1, mapped.size());
        Map<String, Object> row = mapped.get(0);
        assertEquals(List.of("device_sn", "status", "reason"), List.copyOf(row.keySet()));
        assertEquals("DOCK-002", row.get("device_sn"));
        assertEquals("synced", row.get("status"));
        assertEquals(3, row.get("reason"));
    }

    // ==================== buildDeviceSignalItems ====================

    @Test
    void buildDeviceSignalItems_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildDeviceSignalItems(null).isEmpty());
    }

    @Test
    void buildDeviceSignalItems_shouldReturnEmptyGrids_whenGridsNull() {
        DeviceSignalItem item = new DeviceSignalItem();
        item.setDeviceSn("DOCK-003");

        List<Map<String, Object>> mapped = tool.buildDeviceSignalItems(List.of(item));

        assertEquals(1, mapped.size());
        assertEquals("DOCK-003", mapped.get(0).get("device_sn"));
        assertEquals(List.of(), mapped.get(0).get("grids"));
    }

    // ==================== buildGridSignals ====================

    @Test
    void buildGridSignals_shouldReturnEmptyList_whenInputNull() {
        assertTrue(tool.buildGridSignals(null).isEmpty());
    }

    @Test
    void buildGridSignals_shouldMapAllSignalFields() {
        GridSignalItem grid = new GridSignalItem();
        grid.setH3Index("8a2a1072b59ffff");
        grid.setSdrAvg(1);
        grid.setSdrMax(2);
        grid.setSdrMin(3);
        grid.setSignal4gAvg(4);
        grid.setSignal4gMax(5);
        grid.setSignal4gMin(6);

        Map<String, Object> row = tool.buildGridSignals(List.of(grid)).get(0);

        assertEquals(
                List.of("h3_index", "sdr_avg", "sdr_max", "sdr_min",
                        "signal_4g_avg", "signal_4g_max", "signal_4g_min"),
                List.copyOf(row.keySet()));
        assertEquals("8a2a1072b59ffff", row.get("h3_index"));
        assertEquals(1, row.get("sdr_avg"));
        assertEquals(2, row.get("sdr_max"));
        assertEquals(3, row.get("sdr_min"));
        assertEquals(4, row.get("signal_4g_avg"));
        assertEquals(5, row.get("signal_4g_max"));
        assertEquals(6, row.get("signal_4g_min"));
    }

    @Test
    void buildGridSignals_shouldSkipNullElements() {
        GridSignalItem grid = new GridSignalItem();
        grid.setH3Index("h3-1");

        List<GridSignalItem> grids = new ArrayList<>(Arrays.asList(null, grid));

        assertEquals(1, tool.buildGridSignals(grids).size());
    }
}
