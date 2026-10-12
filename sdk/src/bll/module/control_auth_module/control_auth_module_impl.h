// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/control_auth_module.h"
#include "common/util/async_capture_protect.h"
#include "bll/manager/flight_ctrl_mgr/flight_ctrl_mgr.h"
#include "bll/manager/payload_ctrl_mgr/payload_ctrl_mgr.h"

namespace flight_hub_sdk {

class ControlAuthModuleImpl : public IControlAuthModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    ControlAuthModuleImpl(
        std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr,
        std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr);
    ~ControlAuthModuleImpl() override = default;

    void acquireFlightControlAuthority(const std::function<void(FlightHubError)>& callback) override;
    void releaseFlightControlAuthority(const std::function<void(FlightHubError)>& callback) override;
    int listenFlightControlAuthority(const std::function<void(bool)>& callback) override;
    void cancelListenFlightControlAuthority(int listen_id) override;

    void acquirePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    void releasePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) override;
    int listenPayloadControlAuthority(const std::string& payload_index, const std::function<void(bool)>& callback) override;
    void cancelListenPayloadControlAuthority(const std::string& payload_index, int listen_id) override;

private:
    std::shared_ptr<FlightCtrlMgr> flight_ctrl_mgr_;
    std::shared_ptr<PayloadCtrlMgr> payload_ctrl_mgr_;
};

} // namespace flight_hub_sdk
