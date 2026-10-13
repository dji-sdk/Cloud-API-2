// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/device_state_module.h"
#include "api/value.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class DeviceStateModuleImpl : public IDeviceStateModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DeviceStateModuleImpl(DeviceKind kind, const std::string& sn);
    ~DeviceStateModuleImpl() override;

    int listenDockOnlineStatus(
        const std::function<void(const DockOnlineStatus& status)>& callback) override;

    void cancelListenDockOnlineStatus(int listen_id) override;

    int listenDroneOnlineStatus(
        const std::function<void(const DroneOnlineStatus& status)>& callback) override;

    void cancelListenDroneOnlineStatus(int listen_id) override;

private:
    void registerNotificationCenterListeners();

    DeviceKind kind_;
    std::string sn_;
    CallbackContainer<DockOnlineStatus> dock_online_status_callbacks_;
    CallbackContainer<DroneOnlineStatus> drone_online_status_callbacks_;
    int dock_online_status_listen_id_ = -1;
    int drone_online_status_listen_id_ = -1;
};

} // namespace flight_hub_sdk
