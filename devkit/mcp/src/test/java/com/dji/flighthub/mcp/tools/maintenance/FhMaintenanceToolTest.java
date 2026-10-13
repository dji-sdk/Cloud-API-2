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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FhMaintenanceTool 单元测试。
 * 覆盖：getToolDefinition、无 session 报错、各 action 参数校验、Mock 路径。
 *
 * @author DJI FlightHub SDK Team
 */
class FhMaintenanceToolTest {

    private SessionManager sessionManager;
    private FhMaintenanceTool tool;

    @BeforeEach
    void setUp() {
        FlightHubProperties properties = new FlightHubProperties();
        properties.getSecurity().setMaxLevel(3);
        sessionManager = new SessionManager(properties);

        SdkService sdkService = new SdkService() {  public boolean isMockMode() { return true; } };
        tool = new FhMaintenanceTool(sessionManager, sdkService);
    }

    @Test
    void getToolDefinition_shouldReturnCorrectName() {
        Map<String, Object> def = tool.getToolDefinition();

        assertEquals("fh_maintenance", def.get("name"));
        assertNotNull(def.get("description"));
        assertNotNull(def.get("inputSchema"));

        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) def.get("inputSchema");
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertTrue(required.contains("action"));
    }

    @Test
    void execute_shouldReturnError_whenNoSession() {
        Map<String, Object> result = tool.execute(Map.of("action", "get_configs"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("No active session"));
    }

    @Test
    void execute_shouldReturnError_whenActionInvalid() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "destroy_all"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("Invalid or missing action"));
    }

    // ==================== get_configs ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeGetConfigs_shouldReturnMockConfigs() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "get_configs"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertNotNull(data.get("configs"));
        List<Map<String, Object>> configs = (List<Map<String, Object>>) data.get("configs");
        assertEquals(3, configs.size());
        assertEquals("battery_replacement", configs.get(0).get("name"));
        assertEquals("motor_repair", configs.get(1).get("name"));
        assertEquals("gimbal_calibration", configs.get(2).get("name"));
        assertNotNull(configs.get(0).get("estimated_days"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== create_order ====================

    @Test
    void executeCreateOrder_shouldReturnError_whenFaultTimeMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_order");
        args.put("device_list", List.of("DOCK-001"));
        args.put("fault_desc", "Motor vibration");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("fault_time"));
    }

    @Test
    void executeCreateOrder_shouldReturnError_whenDeviceListMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_order");
        args.put("fault_time", "2025-06-15T10:30:00Z");
        args.put("fault_desc", "Motor vibration");

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("device_list"));
    }

    @Test
    void executeCreateOrder_shouldReturnError_whenFaultDescMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_order");
        args.put("fault_time", "2025-06-15T10:30:00Z");
        args.put("device_list", List.of("DOCK-001"));

        Map<String, Object> result = tool.execute(args);

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("fault_desc"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeCreateOrder_shouldReturnOrderNumber() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "create_order");
        args.put("fault_time", "2025-06-15T10:30:00Z");
        args.put("device_list", List.of("DOCK-001", "DRONE-M3D-001"));
        args.put("fault_desc", "Motor abnormal vibration");
        args.put("contact_person", "Zhang San");

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        String orderNumber = (String) data.get("order_number");
        assertNotNull(orderNumber);
        assertTrue(orderNumber.startsWith("MO-"));
        assertEquals(0, data.get("status"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== get_detail ====================

    @Test
    void executeGetDetail_shouldReturnError_whenOrderNumberMissing() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "get_detail"));

        assertEquals(false, result.get("success"));
        assertTrue(result.get("error_message").toString().contains("order_number"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void executeGetDetail_shouldReturnMockDetail() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> args = new HashMap<>();
        args.put("action", "get_detail");
        args.put("order_number", "MO-1719000001000");

        Map<String, Object> result = tool.execute(args);

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        assertEquals("MO-1719000001000", data.get("order_number"));
        assertEquals(1, data.get("status"));
        assertNotNull(data.get("fault_desc"));
        assertNotNull(data.get("contact_person"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== list_orders ====================

    @Test
    @SuppressWarnings("unchecked")
    void executeListOrders_shouldReturnMockList() {
        sessionManager.createSession("uuid", "ws");

        Map<String, Object> result = tool.execute(Map.of("action", "list_orders"));

        assertEquals(true, result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<Map<String, Object>> orders = (List<Map<String, Object>>) data.get("orders");
        assertNotNull(orders);
        assertEquals(2, orders.size());
        assertEquals(2, data.get("total"));
        assertEquals(true, data.get("_mock"));
    }

    // ==================== requiresHumanConfirmation ====================

    @Test
    void requiresHumanConfirmation_shouldReturnFalse() {
        assertFalse(tool.requiresHumanConfirmation());
    }
}
