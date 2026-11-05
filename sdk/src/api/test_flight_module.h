// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// 试飞模块接口，提供试飞航线生成与试飞任务创建。
class ITestFlightModule {
public:
    virtual ~ITestFlightModule() = default;

    /// @brief 生成试飞航线
    /// @param project_id 项目 ID
    /// @param params 生成参数
    virtual void generateTryFlightWayline(
        const std::string& project_id,
        const GenerateTryFlightWaylineParams& params,
        const std::function<void(const GenerateTryFlightWaylineResponse& response, FlightHubError err)>& callback) = 0;

    /// @brief 创建试飞任务
    /// @note task_type、rth_altitude、rth_mode、resumable_status、landing_dock_sn 由后端固定，SDK 不上送；试飞任务仅支持单航线。
    /// @param project_id 项目 ID
    /// @param params 创建参数；wayline_duration 应大于 0
    virtual void createTestFlightTask(
        const std::string& project_id,
        const CreateTestFlightTaskParams& params,
        const std::function<void(const CreateTestFlightTaskResponse& response, FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk
