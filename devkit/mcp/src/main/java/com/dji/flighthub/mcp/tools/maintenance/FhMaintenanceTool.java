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

import com.dji.flighthub.mcp.sdk.SdkCallbackBridge;
import com.dji.flighthub.mcp.sdk.SdkService;
import com.dji.flighthub.mcp.session.SessionContext;
import com.dji.flighthub.mcp.session.SessionManager;
import com.dji.flighthub.mcp.tools.AbstractMcpTool;
import com.dji.flighthub.mcp.annotation.Generated;
import com.dji.flighthubsdk.bean.CreateMaintenanceOrderParams;
import com.dji.flighthubsdk.bean.CreateMaintenanceOrderResults;
import com.dji.flighthubsdk.bean.DeviceRepairConfigResults;
import com.dji.flighthubsdk.bean.GetDeviceRepairConfigParams;
import com.dji.flighthubsdk.bean.GetMaintenanceOrdersListParams;
import com.dji.flighthubsdk.bean.MaintenanceOrderDetailResults;
import com.dji.flighthubsdk.bean.MaintenanceOrderDeviceItemParams;
import com.dji.flighthubsdk.bean.MaintenanceOrdersListItem;
import com.dji.flighthubsdk.bean.MaintenanceOrdersListResults;
import com.dji.flighthubsdk.core.MaintenanceManagementModule;
import com.dji.flighthubsdk.core.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * fh_maintenance — 维修保养工单管理。
 *
 * <p>支持 action：
 * <ul>
 *   <li>get_configs — 获取维修配置项列表（L0）</li>
 *   <li>create_order — 创建维修工单（L0）</li>
 *   <li>get_detail — 查询工单详情（L0）</li>
 *   <li>list_orders — 分页查询工单列表（L0）</li>
 * </ul>
 *
 * <p>所有操作均为 L0，无需人工确认。
 *
 * @author DJI FlightHub SDK Team
 */
@Component
public class FhMaintenanceTool extends AbstractMcpTool {

    private static final Logger log = LoggerFactory.getLogger(FhMaintenanceTool.class);

    /** 工具名称 */
    private static final String TOOL_NAME = "fh_maintenance";

    /** 有效 action 列表（固定顺序，用于 schema enum） */
    private static final List<String> VALID_ACTIONS = List.of(
            "get_configs", "create_order", "get_detail", "list_orders");

    /** 有效 action 集合（用于快速校验） */
    private static final Set<String> VALID_ACTIONS_SET = Set.copyOf(VALID_ACTIONS);

    /** 参数名常量 */
    private static final String PARAM_ACTION = "action";
    private static final String PARAM_ORDER_NUMBER = "order_number";
    private static final String PARAM_FAULT_TIME = "fault_time";
    private static final String PARAM_DEVICE_LIST = "device_list";
    private static final String PARAM_FAULT_DESC = "fault_desc";
    private static final String PARAM_CONTACT_PERSON = "contact_person";
    private static final String PARAM_CONTACT_PHONE = "contact_phone";
    private static final String PARAM_CONTACT_EMAIL = "contact_email";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_DEVICE_SN = "device_sn";
    private static final String PARAM_KEYWORD = "keyword";
    private static final String PARAM_SORT_COLUMN = "sort_column";
    private static final String PARAM_SORT_TYPE = "sort_type";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_PAGE_SIZE = "page_size";

    /** 默认分页 */
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final SdkService sdkService;

    /**
     * 构造函数。
     *
     * @param sessionManager session 管理器
     * @param sdkService     SDK 服务
     */
    public FhMaintenanceTool(SessionManager sessionManager, SdkService sdkService) {
        super(sessionManager);
        this.sdkService = sdkService;
    }

    @Override
    public Map<String, Object> getToolDefinition() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("name", TOOL_NAME);
        def.put("description",
                "Maintenance order management: get repair configs, create maintenance orders, "
                + "query order details, list orders with filters. "
                + "Actions: get_configs, create_order, get_detail, list_orders.");

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(PARAM_ACTION, Map.of("type", "string", "enum", VALID_ACTIONS, "description", "Operation to perform"));
        props.put(PARAM_ORDER_NUMBER, Map.of("type", "string", "description", "Maintenance order number (for get_detail)"));
        props.put(PARAM_FAULT_TIME, Map.of("type", "string", "description", "Fault occurrence time in ISO format (for create_order)"));
        props.put(PARAM_DEVICE_LIST, Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Device serial numbers involved (for create_order)"));
        props.put(PARAM_FAULT_DESC, Map.of("type", "string", "description", "Fault description (for create_order)"));
        props.put(PARAM_CONTACT_PERSON, Map.of("type", "string", "description", "Contact person name (for create_order)"));
        props.put(PARAM_CONTACT_PHONE, Map.of("type", "string", "description", "Contact phone number (for create_order)"));
        props.put(PARAM_CONTACT_EMAIL, Map.of("type", "string", "description", "Contact email address (for create_order)"));
        props.put(PARAM_STATUS, Map.of(
                "type", "integer",
                "description", "Order status filter (for list_orders): 0=pending, 1=in_progress, 2=completed"));
        props.put(PARAM_DEVICE_SN, Map.of("type", "string", "description", "Device SN filter (for list_orders)"));
        props.put(PARAM_KEYWORD, Map.of("type", "string", "description", "Search keyword (for list_orders)"));
        props.put(PARAM_SORT_COLUMN, Map.of("type", "string", "description", "Sort column name (for list_orders)"));
        props.put(PARAM_SORT_TYPE, Map.of("type", "string", "description", "Sort type: asc or desc (for list_orders)"));
        props.put(PARAM_PAGE, Map.of("type", "integer", "description", "Page number (default: 1)"));
        props.put(PARAM_PAGE_SIZE, Map.of("type", "integer", "description", "Items per page (default: 20)"));

        schema.put("properties", props);
        schema.put("required", List.of(PARAM_ACTION));
        def.put("inputSchema", schema);
        return def;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args) {
        SessionContext session = requireSession();
        if (session == null) {
            return noSessionError();
        }

        String action = getStringArg(args, PARAM_ACTION, null);
        if (action == null || !VALID_ACTIONS_SET.contains(action)) {
            return errorResult("Invalid or missing action", "Valid actions: " + VALID_ACTIONS);
        }

        switch (action) {
            case "get_configs":
                return executeGetConfigs(args);
            case "create_order":
                return executeCreateOrder(args);
            case "get_detail":
                return executeGetDetail(args);
            case "list_orders":
                return executeListOrders(args);
            default:
                return errorResult("Unhandled action: " + action, null);
        }
    }

    @Override
    public boolean requiresHumanConfirmation() {
        return false;
    }

    // ==================== get_configs（L0） ====================

    private Map<String, Object> executeGetConfigs(Map<String, Object> args) {
        log.info("[{}] get_configs: querying repair configs", TOOL_NAME);

        return sdkService.isMockMode()
                ? getConfigsInMockMode()
                : getConfigsWithRealSdk();
    }

    private Map<String, Object> getConfigsInMockMode() {
        Map<String, Object> config1 = new LinkedHashMap<>();
        config1.put("config_id", "repair_001");
        config1.put("name", "battery_replacement");
        config1.put("description", "Battery module replacement service");
        config1.put("estimated_days", 3);

        Map<String, Object> config2 = new LinkedHashMap<>();
        config2.put("config_id", "repair_002");
        config2.put("name", "motor_repair");
        config2.put("description", "Motor unit repair and calibration");
        config2.put("estimated_days", 5);

        Map<String, Object> config3 = new LinkedHashMap<>();
        config3.put("config_id", "repair_003");
        config3.put("name", "gimbal_calibration");
        config3.put("description", "Gimbal axis calibration and adjustment");
        config3.put("estimated_days", 2);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("configs", List.of(config1, config2, config3));
        data.put("_mock", true);
        return successResult(data, "Repair configs retrieved.");
    }

    @Generated
    private Map<String, Object> getConfigsWithRealSdk() {
        try {
            MaintenanceManagementModule mmm = getMaintenanceModule();
            if (mmm == null) {
                return errorResult("MaintenanceManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            GetDeviceRepairConfigParams params = new GetDeviceRepairConfigParams();

            CompletableFuture<DeviceRepairConfigResults> future = new CompletableFuture<>();
            mmm.getRepairConfigs(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getRepairConfigs failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            DeviceRepairConfigResults result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("configs", result.getData() != null ? result.getData().toString() : List.of());
            return successResult(data, "Repair configs retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] get_configs timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] get_configs SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] get_configs error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get repair configs: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== create_order（L0） ====================

    private Map<String, Object> executeCreateOrder(Map<String, Object> args) {
        String faultTime = getStringArg(args, PARAM_FAULT_TIME, null);
        if (faultTime == null) {
            return errorResult("Missing required parameter: fault_time",
                    "Provide fault occurrence time in ISO format.");
        }

        List<String> deviceList = getStringListArg(args, PARAM_DEVICE_LIST);
        if (deviceList == null || deviceList.isEmpty()) {
            return errorResult("Missing required parameter: device_list",
                    "Provide at least one device serial number.");
        }

        String faultDesc = getStringArg(args, PARAM_FAULT_DESC, null);
        if (faultDesc == null) {
            return errorResult("Missing required parameter: fault_desc",
                    "Provide a description of the fault.");
        }

        String contactPerson = getStringArg(args, PARAM_CONTACT_PERSON, null);
        String contactPhone = getStringArg(args, PARAM_CONTACT_PHONE, null);
        String contactEmail = getStringArg(args, PARAM_CONTACT_EMAIL, null);

        log.info("[{}] create_order: faultTime={}, devices={}, desc={}",
                TOOL_NAME, faultTime, deviceList, faultDesc);

        return sdkService.isMockMode()
                ? createOrderInMockMode(faultTime, deviceList, faultDesc, contactPerson, contactPhone, contactEmail)
                : createOrderWithRealSdk(faultTime, deviceList, faultDesc, contactPerson, contactPhone, contactEmail);
    }

    private Map<String, Object> createOrderInMockMode(String faultTime, List<String> deviceList, String faultDesc,
                                                       String contactPerson, String contactPhone, String contactEmail) {
        String orderNumber = "MO-" + System.currentTimeMillis();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order_number", orderNumber);
        data.put("status", 0);
        data.put("fault_time", faultTime);
        data.put("device_list", deviceList);
        data.put("fault_desc", faultDesc);
        if (contactPerson != null) {
            data.put("contact_person", contactPerson);
        }
        if (contactPhone != null) {
            data.put("contact_phone", contactPhone);
        }
        if (contactEmail != null) {
            data.put("contact_email", contactEmail);
        }
        data.put("_mock", true);
        return successResult(data, "Maintenance order created.");
    }

    @Generated
    private Map<String, Object> createOrderWithRealSdk(String faultTime, List<String> deviceList, String faultDesc,
                                                        String contactPerson, String contactPhone, String contactEmail) {
        try {
            MaintenanceManagementModule mmm = getMaintenanceModule();
            if (mmm == null) {
                return errorResult("MaintenanceManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CreateMaintenanceOrderParams params = new CreateMaintenanceOrderParams();
            params.setFaultOccurrenceTime(faultTime);
            params.setFaultDescription(faultDesc);

            // 将设备 SN 列表转换为 MaintenanceOrderDeviceItemParams 列表
            List<MaintenanceOrderDeviceItemParams> deviceItems = new ArrayList<>();
            for (String sn : deviceList) {
                MaintenanceOrderDeviceItemParams item = new MaintenanceOrderDeviceItemParams();
                item.setDeviceSn(sn);
                deviceItems.add(item);
            }
            params.setDeviceList(deviceItems);

            if (contactPerson != null) {
                params.setContactPerson(contactPerson);
            }
            if (contactPhone != null) {
                params.setContactPhone(contactPhone);
            }
            if (contactEmail != null) {
                params.setContactEmail(contactEmail);
            }

            CompletableFuture<CreateMaintenanceOrderResults> future = new CompletableFuture<>();
            mmm.createMaintenanceOrder(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "createMaintenanceOrder failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            CreateMaintenanceOrderResults result = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("success", result.isSuccess());
            // 提取首个工单号作为 order_number，保持与 mock 响应结构一致
            if (result.getGeneratedOrders() != null && !result.getGeneratedOrders().isEmpty()) {
                data.put("order_number", result.getGeneratedOrders().get(0).getOrderNumber());
                List<Map<String, Object>> orders = new ArrayList<>();
                for (var order : result.getGeneratedOrders()) {
                    Map<String, Object> orderMap = new LinkedHashMap<>();
                    orderMap.put("order_number", order.getOrderNumber());
                    orderMap.put("status", order.getStatus());
                    orders.add(orderMap);
                }
                data.put("generated_orders", orders);
            } else {
                data.put("generated_orders", List.of());
            }
            return successResult(data, "Maintenance order created.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] create_order timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] create_order SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] create_order error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to create order: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== get_detail（L0） ====================

    private Map<String, Object> executeGetDetail(Map<String, Object> args) {
        String orderNumber = getStringArg(args, PARAM_ORDER_NUMBER, null);
        if (orderNumber == null) {
            return errorResult("Missing required parameter: order_number",
                    "Provide order number to query details.");
        }

        log.info("[{}] get_detail: orderNumber={}", TOOL_NAME, orderNumber);

        return sdkService.isMockMode()
                ? getDetailInMockMode(orderNumber)
                : getDetailWithRealSdk(orderNumber);
    }

    private Map<String, Object> getDetailInMockMode(String orderNumber) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order_number", orderNumber);
        data.put("status", 1);
        data.put("fault_time", "2025-06-15T10:30:00Z");
        data.put("device_list", List.of("DOCK-001", "DRONE-M3D-001"));
        data.put("fault_desc", "Motor abnormal vibration during flight");
        data.put("contact_person", "Zhang San");
        data.put("contact_phone", "13800138000");
        data.put("contact_email", "zhangsan@example.com");
        data.put("created_at", "2025-06-15T11:00:00Z");
        data.put("updated_at", "2025-06-16T09:00:00Z");
        data.put("_mock", true);
        return successResult(data, "Order detail retrieved.");
    }

    @Generated
    private Map<String, Object> getDetailWithRealSdk(String orderNumber) {
        try {
            MaintenanceManagementModule mmm = getMaintenanceModule();
            if (mmm == null) {
                return errorResult("MaintenanceManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            CompletableFuture<MaintenanceOrderDetailResults> future = new CompletableFuture<>();
            mmm.getMaintenanceOrderDetail(orderNumber, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getMaintenanceOrderDetail failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            MaintenanceOrderDetailResults detail = SdkCallbackBridge.await(future);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("order_number", detail.getOrderNumber());
            data.put("status", detail.getStatus());
            data.put("status_text", detail.getStatusText());
            data.put("fault_description", detail.getFaultDescription());
            data.put("fault_occurrence_time", detail.getFaultOccurrenceTime());
            data.put("contact_person", detail.getContactPerson());
            data.put("creator_name", detail.getCreatorName());
            return successResult(data, "Order detail retrieved.");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] get_detail timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] get_detail SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] get_detail error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to get order detail: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== list_orders（L0） ====================

    private Map<String, Object> executeListOrders(Map<String, Object> args) {
        int page = getIntArg(args, PARAM_PAGE, DEFAULT_PAGE);
        int pageSize = getIntArg(args, PARAM_PAGE_SIZE, DEFAULT_PAGE_SIZE);
        if (page < 1) {
            page = DEFAULT_PAGE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        log.info("[{}] list_orders: page={}, pageSize={}", TOOL_NAME, page, pageSize);

        return sdkService.isMockMode()
                ? listOrdersInMockMode(page, pageSize)
                : listOrdersWithRealSdk(args, page, pageSize);
    }

    private Map<String, Object> listOrdersInMockMode(int page, int pageSize) {
        Map<String, Object> order1 = new LinkedHashMap<>();
        order1.put("order_number", "MO-1719000001000");
        order1.put("status", 0);
        order1.put("fault_desc", "Battery abnormal temperature");
        order1.put("device_list", List.of("DOCK-001"));
        order1.put("created_at", "2025-06-10T08:00:00Z");

        Map<String, Object> order2 = new LinkedHashMap<>();
        order2.put("order_number", "MO-1719000002000");
        order2.put("status", 2);
        order2.put("fault_desc", "Gimbal drift after collision");
        order2.put("device_list", List.of("DRONE-M3D-002"));
        order2.put("created_at", "2025-06-12T14:30:00Z");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orders", List.of(order1, order2));
        data.put("total", 2);
        data.put("page", page);
        data.put("page_size", pageSize);
        data.put("_mock", true);
        return successResult(data, "Maintenance orders retrieved.");
    }

    @Generated
    private Map<String, Object> listOrdersWithRealSdk(Map<String, Object> args, int page, int pageSize) {
        try {
            MaintenanceManagementModule mmm = getMaintenanceModule();
            if (mmm == null) {
                return errorResult("MaintenanceManagementModule not available",
                        "SDK may not be fully initialized. Call fh_connect first.");
            }

            GetMaintenanceOrdersListParams params = new GetMaintenanceOrdersListParams();
            params.setPage(page);
            params.setPageSize(pageSize);

            String deviceSn = getStringArg(args, PARAM_DEVICE_SN, null);
            if (deviceSn != null) {
                params.setDeviceSn(deviceSn);
            }

            CompletableFuture<MaintenanceOrdersListResults> future = new CompletableFuture<>();
            mmm.getMaintenanceOrdersList(params, (error, result) -> {
                if (!error.isSuccess()) {
                    future.completeExceptionally(
                            new SdkCallbackBridge.SdkException(error.getServerCode(),
                                    "getMaintenanceOrdersList failed: " + error.getServerMessage()));
                    return;
                }
                future.complete(result);
            });

            MaintenanceOrdersListResults result = SdkCallbackBridge.await(future);

            List<Map<String, Object>> orders = new ArrayList<>();
            if (result.getList() != null) {
                for (MaintenanceOrdersListItem item : result.getList()) {
                    Map<String, Object> orderMap = new LinkedHashMap<>();
                    orderMap.put("order_number", item.getOrderNumber());
                    orderMap.put("status", item.getStatus());
                    orderMap.put("fault_description", item.getFaultDescription());
                    orders.add(orderMap);
                }
            }

            int total = (result.getPagination() != null) ? result.getPagination().getTotal() : orders.size();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("orders", orders);
            data.put("total", total);
            data.put("page", page);
            data.put("page_size", pageSize);
            return successResult(data, "Maintenance orders retrieved (" + orders.size() + " items).");

        } catch (SdkCallbackBridge.SdkTimeoutException e) {
            log.error("[{}] list_orders timeout: {}", TOOL_NAME, e.getMessage());
            return errorResult("SDK timeout: " + e.getMessage(), "Retry or check connectivity.");
        } catch (SdkCallbackBridge.SdkException e) {
            log.error("[{}] list_orders SDK error: [{}] {}", TOOL_NAME, e.getErrorCode(), e.getMessage());
            return errorResult("SDK error [" + e.getErrorCode() + "]: " + e.getMessage(), "Check error details.");
        } catch (Exception e) {
            log.error("[{}] list_orders error: {}", TOOL_NAME, e.getMessage(), e);
            return errorResult("Failed to list orders: " + e.getMessage(), "Check logs for details.");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 MaintenanceManagementModule 实例。
     *
     * @return MaintenanceManagementModule，不可用时返回 null
     */
    private MaintenanceManagementModule getMaintenanceModule() {
        Workspace workspace = sdkService.getWorkspace();
        if (workspace == null) {
            return null;
        }
        return workspace.getMaintenanceManagementModule();
    }
}
