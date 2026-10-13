// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/relay.h"
#include <memory>
#include <string>

namespace flight_hub_sdk {

class RelayImpl : public IRelay {
public:
    RelayImpl(const std::string& sn);
    ~RelayImpl() override = default;

    const std::string& getSn() const override { return sn_; }
    std::shared_ptr<IRelayControlModule> getRelayControlModule() override;
    std::shared_ptr<IRtkCalibrationModule> getRtkCalibrationModule() override;

private:
    std::string sn_;
    std::shared_ptr<IRelayControlModule> relay_control_module_;
    std::shared_ptr<IRtkCalibrationModule> rtk_calibration_module_;
};

} // namespace flight_hub_sdk
