// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flight_task_record_module.h"
#include "common/util/async_capture_protect.h"
#include "BusinessManager.h"

#include <vector>
#include <string>
#include <functional>



namespace flight_hub_sdk {

class FlightTaskRecordModuleImpl : public IFlightTaskRecordModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit FlightTaskRecordModuleImpl();

    // 析构函数;
    ~FlightTaskRecordModuleImpl();


    // [ATC 空管服务]
    // 导出飞行记录;
    void exportFlightRecords(
        const std::string &user_id,                       // [必填参数] 用户 id, 比如 "15478287234981xxxxx";
        const ExportFlightRecordsRequestParams &params,   // 导出飞行记录的请求参数;
        const std::function<void(FlightHubError err, const ExportFlightRecordsResults& results)>& callback) override;


    // [ATC 空管服务]
    // 查询导出进度;
    void getExportFlightRecordsProgress(
        const GetExportFlightRecordsProgressParams &params,   // 查询导出进度的请求参数;
        const std::function<void(FlightHubError err, const GetExportFlightRecordsProgressResults& results)>& callback) override;

private:
};

} // namespace flight_hub_sdk