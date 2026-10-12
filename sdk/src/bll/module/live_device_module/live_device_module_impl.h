// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/live_device_module.h"
#include "api/value.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"
#include "base/async/timer.h"

namespace flight_hub_sdk {

class LiveDeviceModuleImpl : public ILiveDeviceModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    LiveDeviceModuleImpl(DeviceKind kind, const std::string& sn);
    ~LiveDeviceModuleImpl() override;

    int listenLiveAvailableRange(
        const std::function<void(const std::vector<LiveInfo>&)>& callback) override;

    void cancelListenLiveAvailableRange(int listen_id) override;

    void getAudienceCount(
        const AudienceCountGroup* group,
        const std::function<void(FlightHubError err, const std::vector<DeviceAudienceCountItem>& result)>& callback) override;

    int listenDockLiveStatus(const std::function<void(const DockLiveStatus& msg)>& callback) override;
    void cancelListenDockLiveStatus(int listen_id) override;
    int listenDockLiveCapacity(const std::function<void(const DockLiveCapacity& msg)>& callback) override;
    void cancelListenDockLiveCapacity(int listen_id) override;

private:
    void getLiveAvailableRange();
    void registerNotificationCenterListeners();

    DeviceKind kind_;
    std::string sn_;
    CallbackContainer<std::vector<LiveInfo>> live_info_list_callbacks_;
    std::vector<LiveInfo> live_info_list_;
    drc_sdk::Timer get_live_available_range_timer_;

    CallbackContainer<DockLiveStatus> dock_live_status_callbacks_;
    CallbackContainer<DockLiveCapacity> dock_live_capacity_callbacks_;
    int dock_live_status_listen_id_ = -1;
    int dock_live_capacity_listen_id_ = -1;
};

} // namespace flight_hub_sdk
