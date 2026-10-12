// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 设备状态模块：在线状态订阅。
class IDeviceStateModule {
public:
    virtual ~IDeviceStateModule() = default;

    /// @brief 订阅机场在线状态（Dock owner）
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_online_status
    /// @endinternal
    virtual int listenDockOnlineStatus(
        const std::function<void(const DockOnlineStatus& status)>& callback) = 0;

    /// @brief 取消订阅机场在线状态
    /// @param listen_id 监听 ID
    virtual void cancelListenDockOnlineStatus(int listen_id) = 0;

    /// @brief 订阅飞机在线状态（Drone owner）
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_device_online_status
    /// @endinternal
    virtual int listenDroneOnlineStatus(
        const std::function<void(const DroneOnlineStatus& status)>& callback) = 0;

    /// @brief 取消订阅飞机在线状态
    /// @param listen_id 监听 ID
    virtual void cancelListenDroneOnlineStatus(int listen_id) = 0;
};

} // namespace flight_hub_sdk
