// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

    /// 航线任务控制模块接口，提供航线任务的暂停/恢复、控制权状态订阅等功能。
    class IFlightTaskControlModule {
    public:
        virtual ~IFlightTaskControlModule() = default;

        /// @brief 暂停当前航线任务
        /// @param callback 结果回调
        ///
        /// @internal
        /// 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd（method: flight_task_pause）
        /// @endinternal
        virtual void pause(const std::function<void(FlightHubError err)>& callback) = 0;

        /// @brief 恢复当前航线任务
        /// @param callback 结果回调
        ///
        /// @internal
        /// 链路: HTTP | POST /control/api/v1/command-bus/non-realtime-cmd（method: flight_task_resume）
        /// @endinternal
        virtual void resume(const std::function<void(FlightHubError err)>& callback) = 0;

        /// @brief 订阅航线任务事件
        /// @return 监听 ID，用于取消订阅
        virtual int listenWaylineTaskEvent(const std::function<void(const WaylineTaskEvent& msg)>& callback) = 0;
        /// @brief 取消订阅航线任务事件
        /// @param listen_id 监听 ID
        virtual void cancelListenWaylineTaskEvent(int listen_id) = 0;

    };

}