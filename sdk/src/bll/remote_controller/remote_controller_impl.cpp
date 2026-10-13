// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "remote_controller_impl.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

RemoteControllerImpl::RemoteControllerImpl(const std::string& sn)
    : sn_(sn) {
    LOG_INFO("RemoteControllerImpl: sn=%s", sn_.c_str());
}

} // namespace flight_hub_sdk
