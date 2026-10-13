// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 设备报修管理模块接口，提供报修配置查询、报修订单创建与查询等功能。
class IMaintenanceManagementModule {
public:
    virtual ~IMaintenanceManagementModule() = default;

    /// @brief 获取设备报修配置
    /// @param params 请求参数
    /// @param callback 结果回调，成功时返回报修配置信息
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/maintenance-orders/repair-config
    /// @endinternal
    virtual void getRepairConfigs(
        const GetDeviceRepairConfigParams &params,
        const std::function<void(FlightHubError err, const DeviceRepairConfigResults& results)>& callback) = 0;

    /// @brief 创建报修订单
    /// @param params 创建报修订单的请求参数
    /// @param callback 结果回调，成功时返回创建结果
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/maintenance-orders
    /// @endinternal
    virtual void createMaintenanceOrder(
        const CreateMaintenanceOrderParams &params,
        const std::function<void(FlightHubError err, const CreateMaintenanceOrderResults& results)>& callback) = 0;

    /// @brief 查询单个报修订单详情
    /// @param order_number 报修订单编号
    /// @param callback 结果回调，成功时返回订单详情
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/maintenance-orders/{orderNumber}
    /// @endinternal
    virtual void getMaintenanceOrderDetail(
        const std::string &order_number,
        const std::function<void(FlightHubError err, const MaintenanceOrderDetailResults& results)>& callback) = 0;

    /// @brief 分页查询报修订单列表
    /// @param params 查询参数（分页、筛选条件）
    /// @param callback 结果回调，成功时返回订单列表和分页信息
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/manager/maintenance-orders
    /// @endinternal
    virtual void getMaintenanceOrdersList(
        const GetMaintenanceOrdersListParams &params,
        const std::function<void(FlightHubError err, const MaintenanceOrdersListResults& results)>& callback) = 0;

};  // End of IMaintenanceManagementModule;


}  // namespace flight_hub_sdk;
