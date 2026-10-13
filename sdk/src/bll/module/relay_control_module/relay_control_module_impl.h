// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/relay_control_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class RelayControlModuleImpl : public IRelayControlModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    RelayControlModuleImpl(const std::string& sn);
    ~RelayControlModuleImpl() override;

    void setRelayBroadcastHide(bool is_broadcast_hide, const std::function<void(FlightHubError err)>& callback) override;
    void setRelaySSID(const std::string& device_name, const std::function<void(FlightHubError err)>& callback) override;
    void setRelayPassword(const std::string& passwd, const std::function<void(FlightHubError err)>& callback) override;
    void setRelaySearchMode(int relay_search_mode, const std::function<void(FlightHubError err)>& callback) override;
    void setRelayOccupiedStatus(int occupied_status, const std::function<void(FlightHubError err, SetRelayOccupiedStatusResult result)>& callback) override;
    void relayReboot(const std::function<void(FlightHubError err, RelayRebootResult result)>& callback) override;
    void relayDisconnect(const std::function<void(FlightHubError err, RelayDisconnectResult result)>& callback) override;

private:
    std::string sn_;
};

} // namespace flight_hub_sdk
