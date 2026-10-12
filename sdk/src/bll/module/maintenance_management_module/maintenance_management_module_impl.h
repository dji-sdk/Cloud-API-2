// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0


#pragma once

#include "api/maintenance_management_module.h"
#include "tool/callback_container.h"
#include "common/util/async_capture_protect.h"
#include "BusinessManager.h"

#include <unordered_map>
#include <vector>
#include <string>
#include <functional>



namespace flight_hub_sdk {
/**
  * @brief (设备)报修管理模块 API;
  */
class MaintenanceManagementModuleImpl : public IMaintenanceManagementModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit MaintenanceManagementModuleImpl();

    // 析构函数;
    ~MaintenanceManagementModuleImpl();

    // (设备)报修管理模块;
    // 获取设备报修配置;
    void getRepairConfigs(
        const GetDeviceRepairConfigParams &params, // 请求参数;
        const std::function<void(FlightHubError err, const DeviceRepairConfigResults& results)>& callback) override;

    // 创建报修订单;
    void createMaintenanceOrder(
        const CreateMaintenanceOrderParams &params,
        const std::function<void(FlightHubError err, const CreateMaintenanceOrderResults& results)>& callback) override;

    // 查询单个报修订单详情;
    void getMaintenanceOrderDetail(
        const std::string &order_number,
        const std::function<void(FlightHubError err, const MaintenanceOrderDetailResults& results)>& callback) override;

    // (设备)报修管理模块;
    // 分页查询报修订单列表;
    void getMaintenanceOrdersList(
        const GetMaintenanceOrdersListParams &params,
        const std::function<void(FlightHubError err, const MaintenanceOrdersListResults& results)>& callback) override;


private:
};

} // namespace flight_hub_sdk