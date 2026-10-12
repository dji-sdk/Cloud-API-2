// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/live_sync_module.h"
#include "common/util/async_capture_protect.h"
#include <memory>

namespace flight_hub_sdk {

class LiveSyncModuleImpl : public ILiveSyncModule,
                            public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit LiveSyncModuleImpl();
    ~LiveSyncModuleImpl() override = default;

    void createLiveSync(
        const LiveSyncCreateParams& params,
        std::function<void(FlightHubError err, LiveSyncCreateResult result)> callback) override;

    void deleteLiveSync(
        const std::string& id,
        std::function<void(FlightHubError err)> callback) override;

    void updateLiveSync(
        const LiveSyncUpdateParams& params,
        std::function<void(FlightHubError err, LiveSyncItem result)> callback) override;

    void getLiveSyncList(
        const LiveSyncListParams& params,
        std::function<void(FlightHubError err, LiveSyncListResult result)> callback) override;

    void getLiveSyncDevicesV2(
        std::function<void(FlightHubError err, LiveSyncDevicesV2Result result)> callback) override;

    void getLiveSyncSummary(
        std::function<void(FlightHubError err, LiveSyncSummaryResult result)> callback) override;
};

}  // namespace flight_hub_sdk
