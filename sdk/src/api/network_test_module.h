// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 网络测速模块，提供开始/停止/查询进度与进度订阅。
class INetworkTestModule {
public:
    virtual ~INetworkTestModule() = default;

    /// @brief 开始网络测速
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/network/speed-tests/start
    /// @endinternal
    virtual void startNetworkTest(
        const std::function<void(FlightHubError err, NetworkTestProgressResult result)>& callback) = 0;

    /// @brief 停止网络测速
    ///
    /// @internal
    /// 链路: HTTP | PUT /device/api/v1/service/network/speed-tests
    /// @endinternal
    virtual void stopNetworkTest(
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 获取网络测速进度
    ///
    /// @internal
    /// 链路: HTTP | GET /device/api/v1/service/network/speed-tests
    /// @endinternal
    virtual void getNetworkTestProgress(
        const std::function<void(FlightHubError err, NetworkTestProgressResult result)>& callback) = 0;

    /// @brief 订阅网络测速进度
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_network_speed_test_progress
    /// @endinternal
    virtual int subscribeNetworkTestProgress(
        const std::function<void(NetworkTestProgressResult result)>& callback) = 0;

    /// @brief 取消订阅网络测速进度
    virtual void unsubscribeNetworkTestProgress(int listen_id) = 0;
};

} // namespace flight_hub_sdk
