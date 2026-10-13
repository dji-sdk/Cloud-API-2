// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 飞行任务记录模块接口，提供飞行记录导出和导出进度查询功能。
class IFlightTaskRecordModule {
public:
    virtual ~IFlightTaskRecordModule() = default;

    /// @brief 导出飞行记录
    /// @param user_id 用户 id, 比如 "15478287234981xxxxx"
    /// @param params 导出飞行记录的请求参数。必填字段：content_type, file_type, file_name,
    ///              flight_task_ids；缺少任一必填字段将返回错误。
    /// @param callback 结果回调，成功时返回导出结果
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/records/export
    /// @endinternal
    virtual void exportFlightRecords(
        const std::string &user_id,
        const ExportFlightRecordsRequestParams &params,
        const std::function<void(FlightHubError err, const ExportFlightRecordsResults& results)>& callback) = 0;

    /// @brief 查询导出进度
    /// @param params 查询导出进度的请求参数
    /// @param callback 结果回调，成功时返回导出进度信息
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks/records/export
    /// @endinternal
    virtual void getExportFlightRecordsProgress(
        const GetExportFlightRecordsProgressParams &params,
        const std::function<void(FlightHubError err, const GetExportFlightRecordsProgressResults& results)>& callback) = 0;

};  // End of IFlightTaskRecordModule;


}  // namespace flight_hub_sdk;
