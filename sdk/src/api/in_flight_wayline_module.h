// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 空中下发航线模块（HTTP 创建/控制/查询 + WS 进度推送）。
class IInFlightWaylineModule {
public:
    virtual ~IInFlightWaylineModule() = default;

    /// @brief 创建空中航线任务（自动注入当前设备的 drone_sn / gateway_sn）
    /// @param params 任务参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /drc/api/v3/flight-tasks/in-flight-way-line-task
    /// @endinternal
    virtual void createInFlightWaylineTask(
        const CreateInFlightWaylineTaskParams& params,
        const std::function<void(const CreateInFlightWaylineTaskResult& result, FlightHubError err)>& callback) = 0;

    /// @brief 控制空中航线任务（暂停 / 恢复 / 取消）
    /// @param action 控制动作
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /drc/api/v3/flight-wayline-tasks/commands
    /// @endinternal
    virtual void controlInFlightWaylineTask(
        InFlightWaylineControlAction action,
        const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 查询当前设备活跃中的空中航线任务
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /drc/api/v3/flight-tasks/in-flight
    /// @endinternal
    virtual void getActiveInFlightWaylineTask(
        const std::function<void(const ActiveInFlightWaylineTaskInfo& info, FlightHubError err)>& callback) = 0;

    /// @brief 订阅空中航线任务进度推送
    /// @return 监听 ID，用于 cancelListenInFlightWaylineProgress
    ///
    /// @internal
    /// 链路: WebSocket | topic: in_flight_wayline_task_notify
    /// @endinternal
    virtual int listenInFlightWaylineProgress(
        const std::function<void(const InFlightWaylineProgress& msg)>& callback) = 0;

    /// @brief 取消订阅空中航线任务进度
    /// @param listen_id 监听 ID
    virtual void cancelListenInFlightWaylineProgress(int listen_id) = 0;
};

} // namespace flight_hub_sdk
