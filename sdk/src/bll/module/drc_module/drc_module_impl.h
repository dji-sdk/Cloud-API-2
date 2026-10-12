// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/drc_module.h"
#include "common/util/async_capture_protect.h"
#include "manager/drc_link_mgr/drc_link_mgr.h"
#include "tool/callback_container.h"

namespace flight_hub_sdk {

class DrcModuleImpl : public IDrcModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    DrcModuleImpl(std::shared_ptr<DrcLinkMgr> drc_link_mgr, const std::string& log_tag);
    ~DrcModuleImpl() override;

    int listenLinkStatus(const std::function<void(const LinkStatus& status)>& callback) override;
    void cancelListenLinkStatus(int listen_id) override;

    void connectDrcLink(const std::function<void(FlightHubSdkErrorCode)>& callback) override;
    void disconnectDrcLink(const std::function<void(FlightHubSdkErrorCode)>& callback) override;

    // ── DRC Link Auxiliary ──
    void drcRtcQualityTest(const RtcQualityTestParams& params, const std::function<void(FlightHubError)>& callback) override;
    int listenDrcDelayInfo(const std::function<void(const DrcDelayInfo&)>& callback) override;
    void cancelListenDrcDelayInfo(int listen_id) override;
    int listenDrcCountDownInfo(const std::function<void(const DrcCountDownInfo&)>& callback) override;
    void cancelListenDrcCountDownInfo(int listen_id) override;

private:
    void registerDrcLinkStateListener();

    std::shared_ptr<DrcLinkMgr> drc_link_mgr_;
    std::string log_tag_;
    CallbackContainer<LinkStatus> link_status_callbacks_;
    int ctrl_link_status_listen_id_ = -1;
};

} // namespace flight_hub_sdk
