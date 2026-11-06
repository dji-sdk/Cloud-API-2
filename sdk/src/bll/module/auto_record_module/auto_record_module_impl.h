// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/auto_record_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class AutoRecordModuleImpl : public IAutoRecordModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit AutoRecordModuleImpl(int session_id);
    ~AutoRecordModuleImpl() override = default;

    void getAutoRecordConfig(
        std::function<void(FlightHubError err, const AutoRecordConfig& config)> callback) override;

    void saveAutoRecordConfig(
        const SaveAutoRecordConfigParams& params,
        std::function<void(FlightHubError err)> callback) override;

private:
    int session_id_;
};

}  // namespace flight_hub_sdk
