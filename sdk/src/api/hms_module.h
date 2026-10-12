// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <string>
#include <vector>
#include <functional>

namespace flight_hub_sdk {

/// HMS（Health Management System）模块接口，提供历史告警数据查询功能。
class IHMSModule {
public:
    virtual ~IHMSModule() = default;

    /// @brief 查询历史 HMS 告警数据
    /// @param params 查询参数（设备 SN 列表、时间范围、分页）
    /// @param callback 结果回调，成功时返回告警数据列表和分页信息
    ///
    /// @internal
    /// 链路: HTTP | POST /device/api/v1/manager/hms
    /// @endinternal
    virtual void QueryHistoryHMSData(const HistoryHmsDataParam& params, std::function<void(FlightHubError err, const HistoryHmsDataResult&)> callback) = 0;
};

}