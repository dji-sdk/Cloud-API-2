// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <cstdint>
#include <string>

namespace flight_hub_sdk {

/// @brief 遥控器设备接口（Phase 1：身份壳，Module 后续挂载）
class IRemoteController {
public:
    virtual ~IRemoteController() = default;

    /// @brief 获取设备序列号
    virtual const std::string& getSn() const = 0;
};

} // namespace flight_hub_sdk
