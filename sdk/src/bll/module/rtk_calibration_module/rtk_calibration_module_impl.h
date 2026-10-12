// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/rtk_calibration_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class RtkCalibrationModuleImpl : public IRtkCalibrationModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    RtkCalibrationModuleImpl(const std::string& sn, std::string device_module);
    ~RtkCalibrationModuleImpl() override;

    void calibrationByNetwork(
        const NetworkRtkCalibrationParams& params,
        const std::function<void(FlightHubError err, RtkCalibrationResult result)>& callback) override;

    void calibrationByManual(
        const ManulRtkCalibrationParams& params,
        const std::function<void(FlightHubError err, RtkCalibrationResult result)>& callback) override;

    int subscribeCalibrationProgress(
        const std::function<void(RtkCalibrationResult result)>& callback) override;

    void unsubscribeCalibrationProgress(int listen_id) override;

private:
    std::string sn_;
    std::string device_module_;
};

} // namespace flight_hub_sdk
