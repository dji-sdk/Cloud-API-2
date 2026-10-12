// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// 控制权模块接口，提供飞行/负载控制权的抢夺、释放及状态订阅。
class IControlAuthModule {
public:
    virtual ~IControlAuthModule() = default;

    /// @brief 抢夺飞行控制权
    virtual void acquireFlightControlAuthority(const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 释放飞行控制权
    virtual void releaseFlightControlAuthority(const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 订阅飞行控制权状态
    /// @return 监听 ID，用于取消订阅
    virtual int listenFlightControlAuthority(const std::function<void(bool)>& callback) = 0;
    /// @brief 取消订阅飞行控制权状态
    /// @param listen_id 监听 ID
    virtual void cancelListenFlightControlAuthority(int listen_id) = 0;

    /// @brief 抢夺负载控制权
    /// @param payload_index 负载索引
    virtual void acquirePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 释放负载控制权
    /// @param payload_index 负载索引
    virtual void releasePayloadControlAuthority(const std::string& payload_index, const std::function<void(FlightHubError)>& callback) = 0;
    /// @brief 订阅负载控制权状态
    /// @param payload_index 负载索引
    /// @return 监听 ID，用于取消订阅
    virtual int listenPayloadControlAuthority(const std::string& payload_index, const std::function<void(bool)>& callback) = 0;
    /// @brief 取消订阅负载控制权状态
    /// @param payload_index 负载索引
    /// @param listen_id 监听 ID
    virtual void cancelListenPayloadControlAuthority(const std::string& payload_index, int listen_id) = 0;
};

} // namespace flight_hub_sdk
