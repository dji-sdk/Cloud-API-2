// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "defines.h"

namespace message_engine {

class IChannelStatusCallbackBridge {
public:
    virtual ~IChannelStatusCallbackBridge() = default;

    virtual void OnUsersListUpdate(const std::vector<std::string>& users_list) = 0;
    virtual void OnTokenPrivilegeWillExpire() = 0;
    virtual void OnLicenseWillExpireWarning(int left_days) = 0;
    virtual void OnGlobalError(const std::string& error, const std::string& message) = 0;
};

}