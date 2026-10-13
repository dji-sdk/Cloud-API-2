// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "api/ai_alert_module.h"
#include "common/util/async_capture_protect.h"
#include <memory>

namespace flight_hub_sdk {

class AIAlertModuleImpl : public IAIAlertModule,
                          public drc_sdk::common::AsyncCaptureProtect {
public:
    explicit AIAlertModuleImpl();
    ~AIAlertModuleImpl() override = default;

    void postDeviceSetting(
        const PostAiAlertDeviceSettingParam& param,
        std::function<void(FlightHubError err, PostAiAlertDeviceSettingResult result)> callback) override;

    void getDeviceSetting(
        const GetAiAlertDeviceSettingParam& param,
        std::function<void(FlightHubError err, GetAiAlertDeviceSettingResult result)> callback) override;

    void postAlgorithmSetting(
        const PostAiAlertAlgorithmSettingParam& param,
        std::function<void(FlightHubError err, PostAiAlertAlgorithmSettingResult result)> callback) override;

    void getAlgorithmSetting(
        const GetAiAlertAlgorithmSettingParam& param,
        std::function<void(FlightHubError err, GetAiAlertAlgorithmSettingResult result)> callback) override;

    void getAiAlertFlights(
        const GetAiAlertFlightsParam& param,
        std::function<void(FlightHubError err, GetAiAlertFlightsResult result)> callback) override;

    void getAiAlertRecords(
        const GetAiAlertRecordsParam& param,
        std::function<void(FlightHubError err, GetAiAlertRecordsResult result)> callback) override;
};

}  // namespace flight_hub_sdk
