// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <memory>
#include <string>

namespace flight_hub_sdk {

class IRelayControlModule;
class IRtkCalibrationModule;

/// @brief 中继设备接口
class IRelay {
public:
    virtual ~IRelay() = default;

    /// @brief 获取设备序列号
    virtual const std::string& getSn() const = 0;
    /// @brief 获取中继控制模块
    virtual std::shared_ptr<IRelayControlModule> getRelayControlModule() = 0;
    /// @brief 获取 RTK 标定模块
    virtual std::shared_ptr<IRtkCalibrationModule> getRtkCalibrationModule() = 0;
};

} // namespace flight_hub_sdk
