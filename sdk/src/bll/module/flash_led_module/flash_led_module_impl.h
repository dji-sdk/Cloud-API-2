// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/flash_led_module.h"
#include "common/util/async_capture_protect.h"

namespace flight_hub_sdk {

class FlashLedModuleImpl : public IFlashLedModule, public drc_sdk::common::AsyncCaptureProtect {
public:
    FlashLedModuleImpl();
    ~FlashLedModuleImpl() override = default;

    void getFlashLedConfigList(
        std::function<void(FlightHubError err, const FlashLedConfigList& list)> callback) override;

    void createFlashLedConfig(
        const FlashLedConfigParams& params,
        std::function<void(FlightHubError err, const FlashLedConfig& config)> callback) override;

    void updateFlashLedConfig(
        const std::string& config_id,
        const FlashLedConfigParams& params,
        std::function<void(FlightHubError err, const FlashLedConfig& config)> callback) override;

    void deleteFlashLedConfig(
        const std::string& config_id,
        std::function<void(FlightHubError err)> callback) override;
};

}  // namespace flight_hub_sdk
