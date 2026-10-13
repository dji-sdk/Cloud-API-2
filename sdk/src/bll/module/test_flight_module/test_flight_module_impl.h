// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/test_flight_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class TestFlightModuleImpl : public ITestFlightModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit TestFlightModuleImpl();
    ~TestFlightModuleImpl() override = default;

    void generateTryFlightWayline(
        const std::string& project_id,
        const GenerateTryFlightWaylineParams& params,
        const std::function<void(const GenerateTryFlightWaylineResponse& response, FlightHubError err)>& callback) override;

    void createTestFlightTask(
        const std::string& project_id,
        const CreateTestFlightTaskParams& params,
        const std::function<void(const CreateTestFlightTaskResponse& response, FlightHubError err)>& callback) override;

private:
};

} // namespace flight_hub_sdk
