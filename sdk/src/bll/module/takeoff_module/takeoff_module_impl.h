// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/takeoff_module.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class TakeoffModuleImpl : public ITakeoffModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    TakeoffModuleImpl(const std::string& drone_sn, const std::string& gateway_sn);
    ~TakeoffModuleImpl() override;

    void takeoff(
        const TakeoffParams& params,
        const std::function<void(FlightHubError err, const TakeoffResult& result)>& callback) override;

    int listenCommandTaskEvent(
        const std::function<void(const CommandTaskEvent& msg)>& callback) override;
    void cancelListenCommandTaskEvent(int listen_id) override;

    int listenDroneCommanderFlightMode(
        const std::function<void(const DroneCommanderFlightMode& msg)>& callback) override;
    void cancelListenDroneCommanderFlightMode(int listen_id) override;
    int listenDroneCurrentCommanderFlightMode(
        const std::function<void(const DroneCurrentCommanderFlightMode& msg)>& callback) override;
    void cancelListenDroneCurrentCommanderFlightMode(int listen_id) override;
    int listenDroneCommanderModeLostAction(
        const std::function<void(const DroneCommanderModeLostAction& msg)>& callback) override;
    void cancelListenDroneCommanderModeLostAction(int listen_id) override;

private:
    void registerNotificationCenterListeners();

    std::string drone_sn_;
    std::string gateway_sn_;
    CallbackContainer<CommandTaskEvent> command_task_event_callbacks_;
    CallbackContainer<DroneCommanderFlightMode> drone_commander_flight_mode_callbacks_;
    CallbackContainer<DroneCurrentCommanderFlightMode> drone_current_commander_flight_mode_callbacks_;
    CallbackContainer<DroneCommanderModeLostAction> drone_commander_mode_lost_action_callbacks_;
    int command_task_event_listen_id_ = -1;
    int drone_commander_flight_mode_listen_id_ = -1;
    int drone_current_commander_flight_mode_listen_id_ = -1;
    int drone_commander_mode_lost_action_listen_id_ = -1;
};

} // namespace flight_hub_sdk
