// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/in_flight_wayline_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class InFlightWaylineModuleImpl : public IInFlightWaylineModule,
                                  public drc_sdk::common::AsyncCaptureProtect {
public:
    InFlightWaylineModuleImpl(
                              const std::string& gateway_sn,
                              const std::string& drone_sn);
    ~InFlightWaylineModuleImpl() override;

    void createInFlightWaylineTask(
        const CreateInFlightWaylineTaskParams& params,
        const std::function<void(const CreateInFlightWaylineTaskResult& result, FlightHubError err)>& callback) override;

    void controlInFlightWaylineTask(
        InFlightWaylineControlAction action,
        const std::function<void(FlightHubError err)>& callback) override;

    void getActiveInFlightWaylineTask(
        const std::function<void(const ActiveInFlightWaylineTaskInfo& info, FlightHubError err)>& callback) override;

    int listenInFlightWaylineProgress(
        const std::function<void(const InFlightWaylineProgress& msg)>& callback) override;

    void cancelListenInFlightWaylineProgress(int listen_id) override;

private:
    std::string gateway_sn_;
    std::string drone_sn_;
};

} // namespace flight_hub_sdk
