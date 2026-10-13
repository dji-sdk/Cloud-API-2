// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "relay_impl.h"
#include "bll/module/relay_control_module/relay_control_module_impl.h"
#include "bll/module/rtk_calibration_module/rtk_calibration_module_impl.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

RelayImpl::RelayImpl(const std::string& sn)
    : sn_(sn) {
    LOG_INFO("RelayImpl: sn=%s", sn_.c_str());
}

std::shared_ptr<IRelayControlModule> RelayImpl::getRelayControlModule() {
    if (!relay_control_module_) {
        relay_control_module_ = std::make_shared<RelayControlModuleImpl>(sn_);
    }
    return relay_control_module_;
}

std::shared_ptr<IRtkCalibrationModule> RelayImpl::getRtkCalibrationModule() {
    if (!rtk_calibration_module_) {
        rtk_calibration_module_ = std::make_shared<RtkCalibrationModuleImpl>(sn_, "6");
    }
    return rtk_calibration_module_;
}

} // namespace flight_hub_sdk
