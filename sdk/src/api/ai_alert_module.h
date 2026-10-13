// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <memory>

namespace flight_hub_sdk {

/// PaaS ai-alert 告警配置与查询模块（Workspace 级，/drc/paas/v2/ai-alert）
class IAIAlertModule {
public:
    virtual ~IAIAlertModule() = default;

    /// @brief 写设备级告警配置
    /// @note POST /drc/paas/v2/ai-alert/device-setting
    virtual void postDeviceSetting(
        const PostAiAlertDeviceSettingParam& param,
        std::function<void(FlightHubError err, PostAiAlertDeviceSettingResult result)> callback) = 0;

    /// @brief 读设备级告警配置
    /// @note GET /drc/paas/v2/ai-alert/device-setting
    virtual void getDeviceSetting(
        const GetAiAlertDeviceSettingParam& param,
        std::function<void(FlightHubError err, GetAiAlertDeviceSettingResult result)> callback) = 0;

    /// @brief 写算法级告警配置（唯一键 drone_sn + algorithm_type，Upsert）
    /// @note POST /drc/paas/v2/ai-alert/algorithm-setting
    virtual void postAlgorithmSetting(
        const PostAiAlertAlgorithmSettingParam& param,
        std::function<void(FlightHubError err, PostAiAlertAlgorithmSettingResult result)> callback) = 0;

    /// @brief 读算法级告警配置
    /// @note GET /drc/paas/v2/ai-alert/algorithm-setting
    virtual void getAlgorithmSetting(
        const GetAiAlertAlgorithmSettingParam& param,
        std::function<void(FlightHubError err, GetAiAlertAlgorithmSettingResult result)> callback) = 0;

    /// @brief 查询架次告警统计（先拿 flight_id，再调 getAiAlertRecords）
    /// @note GET /drc/paas/v2/ai-alert/flights
    virtual void getAiAlertFlights(
        const GetAiAlertFlightsParam& param,
        std::function<void(FlightHubError err, GetAiAlertFlightsResult result)> callback) = 0;

    /// @brief 查询告警明细列表（按 flight_id 分组，含 thumbnail_url）
    /// @note GET /drc/paas/v2/ai-alert/records
    virtual void getAiAlertRecords(
        const GetAiAlertRecordsParam& param,
        std::function<void(FlightHubError err, GetAiAlertRecordsResult result)> callback) = 0;
};

}  // namespace flight_hub_sdk
