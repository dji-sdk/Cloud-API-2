// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 舱外起降模块接口：无人机停放在机场<b>舱外</b>时的起飞与降落回舱。
///
/// 与一键起飞（`ITakeoffModule::takeoff`，从机场舱内起飞）不同，本模块面向
/// 「飞机已在舱外地面」的场景：起飞不经开舱盖流程；降落由机场自动开舱盖、
/// 飞机降落入舱、再关舱盖。降落过程状态可经
/// `IFlightControlModule::listenDrcLandingMissionState` 订阅观察。
///
/// @note [舱外起飞降落机场] 该功能目前机场3(EA230)高版本固件支持, 如需此功能请升级固件版本.
/// 特别注意: 官方固件未承诺支持前, 本模块接口不作为正式接口使用。
class IGroundFlightModule {
public:
    virtual ~IGroundFlightModule() = default;

    /// @brief 舱外起飞
    /// @param params 舱外起飞参数（见 GroundTakeoffParams）
    /// @param callback 结果回调（GroundTakeoffResult：flight_id）
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/ground-take-off
    /// @endinternal
    virtual void groundTakeoff(
        const GroundTakeoffParams& params,
        const std::function<void(FlightHubError err, const GroundTakeoffResult& result)>& callback) = 0;

    /// @brief 舱外降落（降落至机场 / 取消降落）
    /// @param cmd 降落指令：0=降落至机场（机场自动开舱盖→无人机降落→关舱盖），1=取消降落（悬停）。
    ///            参见 `GroundLandingCommand`（`value.h`）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/ground-landing
    /// @endinternal
    virtual void groundLanding(int cmd, const std::function<void(FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk
