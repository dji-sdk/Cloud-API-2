// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/remote_controller.h"
#include <string>

namespace flight_hub_sdk {

class RemoteControllerImpl : public IRemoteController {
public:
    RemoteControllerImpl(const std::string& sn);
    ~RemoteControllerImpl() override = default;

    const std::string& getSn() const override { return sn_; }

private:
    std::string sn_;
};

} // namespace flight_hub_sdk
