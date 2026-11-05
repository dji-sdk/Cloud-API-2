// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "relay_impl.h"
#include "bll/module/relay_control_module/relay_control_module_impl.h"
#include "bll/module/rtk_calibration_module/rtk_calibration_module_impl.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

RelayImpl::RelayImpl(const std::string& sn, int session_id)
    : sn_(sn), session_id_(session_id) {
    LOG_INFO("RelayImpl: sn=%s, session_id=%d", sn_.c_str(), session_id_);
}

std::shared_ptr<IRelayControlModule> RelayImpl::getRelayControlModule() {
    if (!relay_control_module_) {
        relay_control_module_ = std::make_shared<RelayControlModuleImpl>(sn_, session_id_);
    }
    return relay_control_module_;
}

std::shared_ptr<IRtkCalibrationModule> RelayImpl::getRtkCalibrationModule() {
    if (!rtk_calibration_module_) {
        rtk_calibration_module_ = std::make_shared<RtkCalibrationModuleImpl>(sn_, "6", session_id_);
    }
    return rtk_calibration_module_;
}

} // namespace flight_hub_sdk
