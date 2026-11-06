// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// AirSense（ADS-B）民航飞机告警实时推送模块（飞机 WS 上行）。
class IAirsenseWarningModule {
public:
    virtual ~IAirsenseWarningModule() = default;

    /// @brief 订阅 AirSense 民航飞机告警
    /// @param callback 告警回调，参数含周边民航飞机告警数组（可能为空）
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_airsense_warning
    /// @endinternal
    virtual int listenAirsenseWarning(
        const std::function<void(const AirsenseWarning& msg)>& callback) = 0;

    /// @brief 取消订阅 AirSense 民航飞机告警
    /// @param listen_id 监听 ID
    virtual void cancelListenAirsenseWarning(int listen_id) = 0;
};

} // namespace flight_hub_sdk
