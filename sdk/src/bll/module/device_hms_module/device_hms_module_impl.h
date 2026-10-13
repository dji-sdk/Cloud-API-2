// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/device_hms_module.h"
#include "api/value.h"
#include "common/util/async_capture_protect.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class DeviceHMSModuleImpl : public IDeviceHMSModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DeviceHMSModuleImpl(DeviceKind kind, const std::string& sn);
    ~DeviceHMSModuleImpl() override;

    int listenDeviceHMS(
        const std::function<void(const HmsData& msg)>& callback) override;

    void cancelListenDeviceHMS(int listen_id) override;

private:
    void registerNotificationCenterListener();

    DeviceKind kind_;
    std::string sn_;
    CallbackContainer<HmsData> hms_data_callbacks_;
    int hms_listen_id_ = -1;
};

} // namespace flight_hub_sdk
