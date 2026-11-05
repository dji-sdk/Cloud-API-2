// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "remote_controller_impl.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {

RemoteControllerImpl::RemoteControllerImpl(const std::string& sn, int session_id)
    : sn_(sn), session_id_(session_id) {
    LOG_INFO("RemoteControllerImpl: sn=%s, session_id=%d", sn_.c_str(), session_id_);
}

} // namespace flight_hub_sdk
