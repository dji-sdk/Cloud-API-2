// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/ground_flight_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class GroundFlightModuleImpl : public IGroundFlightModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    GroundFlightModuleImpl(const std::string& drone_sn, const std::string& gateway_sn, int session_id);
    ~GroundFlightModuleImpl() override = default;

    void groundTakeoff(
        const GroundTakeoffParams& params,
        const std::function<void(FlightHubError err, const GroundTakeoffResult& result)>& callback) override;

    void groundLanding(int cmd, const std::function<void(FlightHubError err)>& callback) override;

private:
    std::string drone_sn_;
    std::string gateway_sn_;
    int session_id_;
};

} // namespace flight_hub_sdk
