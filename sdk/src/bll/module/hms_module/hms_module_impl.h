// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/hms_module.h"
#include "tool/callback_container.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"

namespace flight_hub_sdk {

class HMSModuleImpl : public IHMSModule, public drc_sdk::common::AsyncCaptureProtect{
public:
    explicit HMSModuleImpl();
    ~HMSModuleImpl();
    void QueryHistoryHMSData(const HistoryHmsDataParam& params, std::function<void(FlightHubError err, const HistoryHmsDataResult&)> callback) override;

private:
};
}// namespace flight_hub_sdk