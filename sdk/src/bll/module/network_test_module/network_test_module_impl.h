// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/network_test_module.h"
#include "common/util/async_capture_protect.h"
#include <memory>

namespace flight_hub_sdk {

class NetworkTestModuleImpl : public INetworkTestModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    NetworkTestModuleImpl(const std::string& sn, int session_id);
    ~NetworkTestModuleImpl() override;

    void startNetworkTest(
        const std::function<void(FlightHubError err, NetworkTestProgressResult result)>& callback) override;

    void stopNetworkTest(
        const std::function<void(FlightHubError err)>& callback) override;

    void getNetworkTestProgress(
        const std::function<void(FlightHubError err, NetworkTestProgressResult result)>& callback) override;

    int subscribeNetworkTestProgress(
        const std::function<void(NetworkTestProgressResult result)>& callback) override;

    void unsubscribeNetworkTestProgress(int listen_id) override;

private:
    int session_id_;
    std::string sn_;
};

} // namespace flight_hub_sdk
