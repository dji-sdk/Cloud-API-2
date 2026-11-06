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

        /// @brief 订阅多机场返航路径信息事件（网关设备）
        /// @return 监听 ID，用于 cancelListenReturnHomeInfoEvent 取消订阅
        ///
        /// @note 推送语义：内容去重。与上一条完全相同的事件不会重复回调。
        ///       如需收到每一条原始推送，请使用 listenReturnHomeInfoEventByDevice。
        ///
        /// @internal
        /// 链路: MQTT/WS | topic_atc_return_home_info_event
        /// @endinternal
        virtual int listenReturnHomeInfoEvent(const std::function<void(const ReturnHomeInfoEvent& msg)>& callback) = 0;
        /// @brief 取消订阅多机场返航路径信息事件（网关设备）
        /// @param listen_id listenReturnHomeInfoEvent 返回的监听 ID
        /// @note 该 ID 与 listenReturnHomeInfoEventByDevice 返回的 ID 属于不同的 ID 空间，
        ///       不可交叉传入。
        virtual void cancelListenReturnHomeInfoEvent(int listen_id) = 0;

        /// @brief 订阅指定设备的多机场返航路径信息事件
        /// @param device_sn 订阅设备 SN。蛙跳场景下应传目标降落机场 SN，
        ///                  与后端该 topic 的路由 SN 对齐。
        /// @return 监听 ID，用于 cancelListenReturnHomeInfoEventByDevice 取消订阅；
        ///         失败时返回负值
        ///
        /// @note 推送语义：不去重。每条到达的事件都会回调，
        ///       与 listenReturnHomeInfoEvent 的去重行为不同。
        ///
        /// @internal
        /// 链路: MQTT/WS | topic_atc_return_home_info_event
        /// @endinternal
        virtual int listenReturnHomeInfoEventByDevice(const std::string& device_sn, const std::function<void(const ReturnHomeInfoEvent& msg)>& callback) = 0;
        /// @brief 取消订阅指定设备的多机场返航路径信息事件
        /// @param listen_id listenReturnHomeInfoEventByDevice 返回的监听 ID
        /// @note 该 ID 与 listenReturnHomeInfoEvent 返回的 ID 属于不同的 ID 空间，
        ///       不可交叉传入。
        virtual void cancelListenReturnHomeInfoEventByDevice(int listen_id) = 0;

        /// @brief 订阅任务异常事件
        /// @return 监听 ID，用于取消订阅
        ///
        /// @internal
        /// 链路: MQTT/WS | topic_atc_task_exception_event
        /// @endinternal
        virtual int listenTaskExceptionEvent(const std::function<void(const TaskExceptionEvent& msg)>& callback) = 0;
        /// @brief 取消订阅任务异常事件
        /// @param listen_id 监听 ID
        virtual void cancelListenTaskExceptionEvent(int listen_id) = 0;

    };

}
