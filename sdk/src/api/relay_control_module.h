// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <string>

namespace flight_hub_sdk {

/// 中继站控制模块，提供广播、SSID、密码、搜索模式、占用状态、重启与断开等能力。
class IRelayControlModule {
public:
    virtual ~IRelayControlModule() = default;

    /// @brief 设置中继站广播隐藏状态
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-broadcast-hide
    /// @endinternal
    virtual void setRelayBroadcastHide(bool is_broadcast_hide, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置中继站 SSID
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-ssid
    /// @endinternal
    virtual void setRelaySSID(const std::string& device_name, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置中继站密码
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-password
    /// @endinternal
    virtual void setRelayPassword(const std::string& passwd, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置中继站搜索模式
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-search-mode
    /// @endinternal
    virtual void setRelaySearchMode(int relay_search_mode, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置中继站占用状态
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-occupied-status
    /// @endinternal
    virtual void setRelayOccupiedStatus(int occupied_status, const std::function<void(FlightHubError err, SetRelayOccupiedStatusResult result)>& callback) = 0;

    /// @brief 中继站重启
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-reboot
    /// @endinternal
    virtual void relayReboot(const std::function<void(FlightHubError err, RelayRebootResult result)>& callback) = 0;

    /// @brief 中继站断开连接
    ///
    /// @internal
    /// 链路: HTTP | HTTP POST /device/api/v1/service/relay-disconnect
    /// @endinternal
    virtual void relayDisconnect(const std::function<void(FlightHubError err, RelayDisconnectResult result)>& callback) = 0;
};

} // namespace flight_hub_sdk
