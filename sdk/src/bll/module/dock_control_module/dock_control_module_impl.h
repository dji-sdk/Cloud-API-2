// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/dock_control_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"
#include <string>

namespace flight_hub_sdk {

class DockControlModuleImpl : public IDockControlModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DockControlModuleImpl(const std::string& sn);
    ~DockControlModuleImpl() override;

    void changeDockCameraPosition(
        const LiveInfo& live_info,
        int camera_position,
        const std::string& gateway_sn,
        const std::function<void(FlightHubError err)>& callback) override;

    int listenDockAirConditionerStatusInfo(const std::function<void(const DockAirConditionerStatusInfo& msg)>& callback) override;
    void cancelListenDockAirConditionerStatusInfo(int listen_id) override;

    int listenDockSupplementLightState(const std::function<void(const DockSupplementLightState& msg)>& callback) override;
    void cancelListenDockSupplementLightState(int listen_id) override;

    void setDockSupplementLightState(bool state, const std::function<void(FlightHubError err)>& callback) override;
    void setDockChargeState(bool state, const std::function<void(FlightHubError err)>& callback) override;
    void setDockCoverState(int mode, const std::function<void(FlightHubError err)>& callback) override;
    void setDockAirConditionerMode(int mode, const std::function<void(FlightHubError err)>& callback) override;

private:
    void registerNotificationCenterListeners();

    std::string sn_;

    CallbackContainer<DockAirConditionerStatusInfo> dock_air_conditioner_status_info_callbacks_;
    CallbackContainer<DockSupplementLightState> dock_supplement_light_state_callbacks_;

    int dock_air_conditioner_status_info_listen_id_ = -1;
    int dock_supplement_light_state_listen_id_ = -1;
};

} // namespace flight_hub_sdk
