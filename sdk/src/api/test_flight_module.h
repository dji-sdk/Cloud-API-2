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
    /// @param project_id 项目 ID
    /// @param params 创建参数
    virtual void createTestFlightTask(
        const std::string& project_id,
        const CreateTestFlightTaskParams& params,
        const std::function<void(const CreateTestFlightTaskResponse& response, FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk
