// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <string>

#include "api/flight_settings_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class FlightSettingsModuleImpl : public IFlightSettingsModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    /// 构造时传入 gateway_sn（由 DroneImpl::getGatewaySn() 计算得出：
    /// 传统机场场景 gateway_sn = dock_sn；网联机型 gateway_sn = drone_sn）。
    /// 后端 /manage/api/v1/flight/configs/safe 接口的 ?sn=xxx 参数期望的是 gateway_sn。
    explicit FlightSettingsModuleImpl(const std::string& gateway_sn);
    ~FlightSettingsModuleImpl() override = default;

    void setFlightSmartPlanMode(
        const std::string& device_sn,
        int mode,
        std::function<void(FlightHubError err, const FlightSmartPlanModeResult& result)> callback) override;

    void setOutOfControlAction(
        const std::string& device_sn,
        const std::string& action,
        std::function<void(FlightHubError err, const FlightOutOfControlActionResult& result)> callback) override;

    void setFlightTaskHeight(
        const std::string& device_sn,
        int height,
        std::function<void(FlightHubError err, const FlightCommanderHeightResult& result)> callback) override;

    void setRthAltitude(
        const std::string& device_sn,
        int altitude,
        std::function<void(FlightHubError err, const FlightRthAltitudeResult& result)> callback) override;

    void setAirTransferEnable(
        const std::string& device_sn,
        bool enabled,
        std::function<void(FlightHubError err)> callback) override;

private:
    std::string gateway_sn_;   // 后端接口需要的网关 SN（HTTP Query ?sn=xxx 传这个）
};

} // namespace flight_hub_sdk
