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

package com.dji.flighthub.mcp.tools.device;

import com.dji.flighthub.mcp.config.FlightHubProperties;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhDeviceListTool Mock 模式单元测试。
 * 验证参数校验、mock 数据结构和过滤逻辑。
 */
class FhDeviceListToolTest {

    private FhDeviceListTool tool;
    private SessionManager sessionManager;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        // SdkService 在无 SDK 环境下会使 getWorkspace() 返回 null，从而走 mock 路径
        SdkService sdkService = new SdkService();
        tool = new FhDeviceListTool(sessionManager, sdkService);
    }

    @Test
    void execute_shouldReturnErrorWithoutSession() {
        Map<String, Object> result = tool.execute(Map.of());

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReturnMockDataWithSession() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of());

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("devices"));
        assertTrue(data.get("devices") instanceof List);
        assertTrue(((List<?>) data.get("devices")).size() > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterByType() {
        sessionManager.createSession("uuid", "ws");

        // 只查 dock（真实拓扑里 dock 是子设备类型，验证摊平后过滤能命中）
        Map<String, Object> result = tool.execute(Map.of("type", "dock"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");

        assertFalse(devices.isEmpty(), "mock 数据应至少包含一台 dock 子设备");
        for (Map<String, Object> device : devices) {
            assertEquals("dock", device.get("type"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterByAirportType() {
        sessionManager.createSession("uuid", "ws");

        // 只查 airport（真实拓扑里 airport 是顶层网关类型）
        Map<String, Object> result = tool.execute(Map.of("type", "airport"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");

        assertFalse(devices.isEmpty(), "mock 数据应至少包含一台 airport");
        for (Map<String, Object> device : devices) {
            assertEquals("airport", device.get("type"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldReportAirportAndBaseStationOnlineCountsSeparately() {
        // 回归防护：本次 bug 修复的核心诉求——机场（airport）和地面站（base_station）
        // 在线数必须分开统计，且不再恒为 0。
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of());
        Map<String, Object> data = (Map<String, Object>) result.get("data");

        assertEquals(1L, data.get("airports_online"), "mock 里 1 台 airport 在线");
        assertEquals(0L, data.get("base_stations_online"), "mock 里 base_station 离线");
        assertEquals(1L, data.get("drones_online"), "mock 里 1 台 drone 子设备在线");
        assertEquals(1L, data.get("docks_online"), "mock 里 1 台 dock 子设备在线");
    }

    @Test
    @SuppressWarnings("unchecked")
    void execute_shouldFilterOnlineOnly() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("online_only", true));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> devices = (List<Map<String, Object>>) data.get("devices");

        assertFalse(devices.isEmpty(), "mock 数据应至少包含一台在线设备");
        for (Map<String, Object> device : devices) {
            assertEquals(true, device.get("online"));
        }
    }

    @Test
    void getToolDefinition_shouldHaveCorrectName() {
        Map<String, Object> def = tool.getToolDefinition();
        assertEquals("fh_device_list", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));
    }

    // ==================== 真实 SDK 聚合逻辑单测 ====================
    //
    // 以下测试基于用户报告 bug 时提供的真实返回数据截图（10 台顶层设备 + 层级结构）
    // 构造夹具，复现 drones_online/docks_online 恒为 0 的问题，并验证摊平子设备后
    // 按真实 type（airport/base_station/drone/dock/relay）分类统计的聚合逻辑。
    //
    // 真实拓扑：顶层设备（topo.getDevice()）type 只有 "airport"/"base_station"；
    // 子设备（topo.getSubDevices()）type 为 "drone"/"dock"/"relay"。旧代码只扫描顶层
    // 列表且比较字面量 "drone"/"dock"，结构性地永远匹配不到任何条目。

    /**
     * 构造与用户截图等价的 10 条顶层设备数据（树状结构，含 children）。
     * 手工核算的预期在线数：airports_online=3（行1,3,9）、base_stations_online=1（行2）、
     * drones_online=1（行3的子设备）、docks_online=1（行2的 ea340d 子设备）。
     */
    private static List<Map<String, Object>> buildScreenshotFixtureDeviceTree() {
        return List.of(
                topLevelDevice("MOCK-AIRPORT-001", "测试机场A", "airport", true,
                        List.of(child("MOCK-DRONE-001", "测试无人机A", "drone", false))),
                topLevelDevice("MOCK-BS-001", "测试基站A", "base_station", true,
                        List.of(child("MOCK-DRONE-002", "测试无人机B", "drone", false),
                                child("MOCK-DOCK-001", "测试机库A", "dock", true))),
                topLevelDevice("MOCK-AIRPORT-002", "测试机场B", "airport", true,
                        List.of(child("MOCK-DRONE-003", "测试无人机C", "drone", true))),
                topLevelDevice("MOCK-BS-002", "测试基站B", "base_station", false,
                        List.of(child("MOCK-DRONE-004", "测试无人机D", "drone", false),
                                child("MOCK-DOCK-002", "测试机库B", "dock", false))),
                topLevelDevice("MOCK-BS-003", "测试基站C", "base_station", false,
                        List.of(child("MOCK-DOCK-003", "测试机库C", "dock", false))),
                topLevelDevice("MOCK-AIRPORT-003", "测试机场C", "airport", false,
                        List.of(child("MOCK-DRONE-005", "测试无人机E", "drone", false))),
                topLevelDevice("MOCK-AIRPORT-004", "测试机场D", "airport", false,
                        List.of(child("MOCK-DRONE-006", "测试无人机F", "drone", false),
                                child("MOCK-RELAY-001", "测试中继A", "relay", false))),
                topLevelDevice("MOCK-AIRPORT-005", "测试机场E", "airport", false, List.of()),
                topLevelDevice("MOCK-AIRPORT-006", "测试机场F", "airport", true,
                        List.of(child("MOCK-DRONE-007", "测试无人机G", "drone", false))),
                topLevelDevice("MOCK-AIRPORT-007", "测试机场G", "airport", false,
                        List.of(child("MOCK-DRONE-008", "测试无人机H", "drone", false)))
        );
    }

    private static Map<String, Object> topLevelDevice(String sn, String name, String type, boolean online,
                                                        List<Map<String, Object>> children) {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("sn", sn);
        item.put("name", name);
        item.put("type", type);
        item.put("online", online);
        item.put("model", "model-" + sn);
        if (!children.isEmpty()) {
            item.put("children", children);
        }
        return item;
    }

    private static Map<String, Object> child(String sn, String name, String type, boolean online) {
        Map<String, Object> item = new java.util.LinkedHashMap<>();
        item.put("sn", sn);
        item.put("name", name);
        item.put("type", type);
        item.put("online", online);
        item.put("model", "model-" + sn);
        return item;
    }

    @Test
    void flattenDevices_shouldIncludeTopLevelAndChildren() {
        List<Map<String, Object>> flat = FhDeviceListTool.flattenDevices(buildScreenshotFixtureDeviceTree());

        // 10 台顶层设备 + 12 台子设备（各行子设备数：1+2+1+2+1+1+2+0+1+1=12）
        assertEquals(22, flat.size());
    }

    @Test
    void aggregateOnlineCounts_shouldCountByRealTypeAcrossTopLevelAndChildren() {
        List<Map<String, Object>> flat = FhDeviceListTool.flattenDevices(buildScreenshotFixtureDeviceTree());
        Map<String, Long> counts = FhDeviceListTool.aggregateOnlineCounts(flat);

        assertEquals(3L, counts.getOrDefault("airport", 0L), "顶层 airport 在线数（行1,3,9）");
        assertEquals(1L, counts.getOrDefault("base_station", 0L), "顶层 base_station 在线数（行2）");
        assertEquals(1L, counts.getOrDefault("drone", 0L), "子设备 drone 在线数（行3子设备）");
        assertEquals(1L, counts.getOrDefault("dock", 0L), "子设备 dock 在线数（行2的 ea340d）");
        assertEquals(0L, counts.getOrDefault("relay", 0L), "子设备 relay 全部离线（行7）");
    }

    @Test
    void filterFlatByType_shouldReturnDroneChildrenInsteadOfEmpty() {
        // 回归防护：修复前，type="drone" 过滤只扫顶层（type 永远是 airport/base_station），
        // 恒返回空列表——这是本次连带修复的第二个 bug。
        List<Map<String, Object>> flat = FhDeviceListTool.flattenDevices(buildScreenshotFixtureDeviceTree());
        List<Map<String, Object>> drones = FhDeviceListTool.filterFlatByType(flat, "drone");

        assertEquals(8, drones.size(), "10 行里共有 8 台 drone 子设备（行1,2,3,4,6,7,9,10 各 1 台）");
        for (Map<String, Object> d : drones) {
            assertEquals("drone", d.get("type"));
        }
    }

    @Test
    void filterFlatByType_shouldReturnAllWhenFilterIsAll() {
        List<Map<String, Object>> flat = FhDeviceListTool.flattenDevices(buildScreenshotFixtureDeviceTree());
        assertEquals(flat.size(), FhDeviceListTool.filterFlatByType(flat, "all").size());
    }
}
