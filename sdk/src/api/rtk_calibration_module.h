// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// RTK 标定模块，提供网络/手动标定与进度订阅。
class IRtkCalibrationModule {
public:
    virtual ~IRtkCalibrationModule() = default;

    /// @brief RTK 标定（网络模式）
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/rtk-calibration
    /// @endinternal
    virtual void calibrationByNetwork(
        const NetworkRtkCalibrationParams& params,
        const std::function<void(FlightHubError err, RtkCalibrationResult result)>& callback) = 0;

    /// @brief RTK 标定（手动模式）
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/rtk-calibration
    /// @endinternal
    virtual void calibrationByManual(
        const ManulRtkCalibrationParams& params,
        const std::function<void(FlightHubError err, RtkCalibrationResult result)>& callback) = 0;

    /// @brief 订阅 RTK 标定进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_rtk_calibration
    /// @endinternal
    virtual int subscribeCalibrationProgress(
        const std::function<void(RtkCalibrationResult result)>& callback) = 0;

    /// @brief 取消订阅 RTK 标定进度
    virtual void unsubscribeCalibrationProgress(int listen_id) = 0;
};

} // namespace flight_hub_sdk
