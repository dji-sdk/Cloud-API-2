// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 一键起飞 / 指令飞行模块（`POST /atc/api/v1/flight-tasks/command`）。
class ITakeoffModule {
public:
    virtual ~ITakeoffModule() = default;

    /// @brief 指令飞行（一键起飞）
    /// @param params 起飞参数（见 TakeoffParams，对应 CommandRequest）
    /// @param callback 结果回调（TakeoffResult：flight_id / fly_to_id）
    virtual void takeoff(
        const TakeoffParams& params,
        const std::function<void(FlightHubError err, const TakeoffResult& result)>& callback) = 0;

    /// @brief 订阅指令任务事件
    /// @param callback 指令任务事件回调（CommandTaskEvent::action：command_task_created/command_task_updated；CommandTaskData::status：0-待执行，1-执行中，2-已完成，3-失败，4-已取消）
    /// @return 监听 ID，用于取消订阅
    virtual int listenCommandTaskEvent(
        const std::function<void(const CommandTaskEvent& msg)>& callback) = 0;
    /// @brief 取消订阅指令任务事件
    /// @param listen_id 监听 ID
    virtual void cancelListenCommandTaskEvent(int listen_id) = 0;

    /// @brief 订阅指点飞行模式设置值
    /// @param callback 指点飞行模式设置值回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_commander_flight_mode
    /// @endinternal
    virtual int listenDroneCommanderFlightMode(
        const std::function<void(const DroneCommanderFlightMode& msg)>& callback) = 0;
    /// @brief 取消订阅指点飞行模式设置值
    virtual void cancelListenDroneCommanderFlightMode(int listen_id) = 0;

    /// @brief 订阅指点飞行模式当前值
    /// @param callback 指点飞行模式当前值回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_current_commander_flight_mode
    /// @endinternal
    virtual int listenDroneCurrentCommanderFlightMode(
        const std::function<void(const DroneCurrentCommanderFlightMode& msg)>& callback) = 0;
    /// @brief 取消订阅指点飞行模式当前值
    virtual void cancelListenDroneCurrentCommanderFlightMode(int listen_id) = 0;

    /// @brief 订阅指点飞行失控动作
    /// @param callback 指点飞行失控动作回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_commander_mode_lost_action
    /// @endinternal
    virtual int listenDroneCommanderModeLostAction(
        const std::function<void(const DroneCommanderModeLostAction& msg)>& callback) = 0;
    /// @brief 取消订阅指点飞行失控动作
    virtual void cancelListenDroneCommanderModeLostAction(int listen_id) = 0;
};

} // namespace flight_hub_sdk
