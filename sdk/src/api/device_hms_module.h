// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 设备 HMS 实时推送模块（机场/飞机 WS 告警，统一 listenDeviceHMS）。
class IDeviceHMSModule {
public:
    virtual ~IDeviceHMSModule() = default;

    /// @brief 订阅设备 HMS 实时告警
    ///
    /// @internal
    /// Dock: WebSocket | topic: topic_dock_hms
    /// Drone: WebSocket | topic: topic_drone_hms
    /// impl 按 DeviceKind + sn_ 选择 NC 入口，调用方无需区分。
    /// @endinternal
    virtual int listenDeviceHMS(
        const std::function<void(const HmsData& msg)>& callback) = 0;

    /// @brief 取消订阅设备 HMS 实时告警
    /// @param listen_id 监听 ID
    virtual void cancelListenDeviceHMS(int listen_id) = 0;
};

} // namespace flight_hub_sdk
