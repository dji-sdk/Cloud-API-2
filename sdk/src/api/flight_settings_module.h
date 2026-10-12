// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// 飞行设置模块接口，提供智能规划、失控动作、任务/返航高度及空中回传等参数配置。
class IFlightSettingsModule {
public:
    virtual ~IFlightSettingsModule() = default;

    /// @brief 设置智能规划模式
    /// @param device_sn 设备序列号
    /// @param mode 规划模式
    virtual void setFlightSmartPlanMode(
        const std::string& device_sn,
        int mode,
        std::function<void(FlightHubError err, const FlightSmartPlanModeResult& result)> callback) = 0;

    /// @brief 设置失控动作
    /// @param device_sn 设备序列号
    /// @param action 失控动作
    virtual void setOutOfControlAction(
        const std::string& device_sn,
        const std::string& action,
        std::function<void(FlightHubError err, const FlightOutOfControlActionResult& result)> callback) = 0;

    /// @brief 设置飞行任务高度
    /// @param device_sn 设备序列号
    /// @param height 任务高度
    virtual void setFlightTaskHeight(
        const std::string& device_sn,
        int height,
        std::function<void(FlightHubError err, const FlightCommanderHeightResult& result)> callback) = 0;

    /// @brief 设置返航高度
    /// @param device_sn 设备序列号
    /// @param altitude 返航高度
    virtual void setRthAltitude(
        const std::string& device_sn,
        int altitude,
        std::function<void(FlightHubError err, const FlightRthAltitudeResult& result)> callback) = 0;

    /// @brief 设置空中回传开关
    /// @param device_sn 设备序列号
    /// @param enabled 是否开启
    virtual void setAirTransferEnable(
        const std::string& device_sn,
        bool enabled,
        std::function<void(FlightHubError err)> callback) = 0;
};

} // namespace flight_hub_sdk
