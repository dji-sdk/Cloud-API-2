// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>

namespace flight_hub_sdk {

/// 指点飞行模块接口
class IFlyToPointModule {
public:
    virtual ~IFlyToPointModule() = default;

    /// @brief 开始指点飞行
    /// @param params 目标点坐标与最大速度
    /// @param callback 结果回调，成功时返回 fly_to_id
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/fly-to-points
    /// @endinternal
    virtual void start(const FlyToPointParams& params, const std::function<void(FlightHubError err, std::string fly_to_id)> callback) = 0;

    /// @brief 停止指点飞行（紧急停止）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: DRC | method: drone_emergency_stop
    /// @endinternal
    virtual void drcStop(const std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 更新指点飞行目标点
    /// @param params 更新参数（fly_to_id、起始点、目标点数组、最大速度）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /atc/api/v1/flight-tasks/fly-to-points
    /// @endinternal
    virtual void updateTargetPoints(const FlyToUpdateTargetPointsParams& params, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 订阅指点飞行任务事件（创建与更新），含路径规划与执行进度
    /// @param callback 任务事件回调
    /// @return 监听 ID
    ///
    /// 替代已废弃的 subscribeFlyToPointProgress 与 subscribeFlyToDroneSelfPlanPath。
    /// 事件数据见 FlyToTaskEvent::task_data：planned_path_points（路径）；
    /// status、way_point_index、remaining_distance、remaining_time（进度）。
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_atc_flyto_task_event
    /// @endinternal
    virtual int listenFlyToTaskEvent(const std::function<void(const FlyToTaskEvent& msg)>& callback) = 0;

    /// @brief 取消订阅指点飞行任务事件
    /// @param listen_id 监听 ID
    virtual void cancelListenFlyToTaskEvent(int listen_id) = 0;

    /// @brief 订阅飞行器自主规划路径
    /// @deprecated 已废弃。topic topic_drone_self_plan_path 已替换为 topic_atc_flyto_task_event。
    ///             请改用 IFlyToPointModule::listenFlyToTaskEvent；路径见 FlyToTaskEvent::task_data.planned_path_points。
    /// @param callback 路径回调，每次路径更新时触发
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_self_plan_path
    /// @endinternal
    virtual int subscribeFlyToDroneSelfPlanPath(const std::function<void(const FlyToDroneSelfPlanPath& path)>& callback) = 0;

    /// @brief 取消订阅飞行器自主规划路径
    /// @deprecated 已废弃。请改用 IFlyToPointModule::cancelListenFlyToTaskEvent。
    /// @param listen_id 订阅时返回的监听 ID
    ///
    /// @internal
    /// 链路: SDK 内部 | 取消 NotificationCenter 监听
    /// @endinternal
    virtual void unsubscribeFlyToDroneSelfPlanPath(int listen_id) = 0;

    /// @brief 订阅指点飞行进度
    /// @deprecated 已废弃。topic topic_drone_flyto_point_progress 已替换为 topic_atc_flyto_task_event。
    ///             请改用 IFlyToPointModule::listenFlyToTaskEvent；进度见 FlyToTaskEvent::task_data
    ///             （status、way_point_index、remaining_distance、remaining_time）。
    /// @param callback 进度回调，每次状态变化时触发
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_flyto_point_progress
    /// @endinternal
    virtual int subscribeFlyToPointProgress(const std::function<void(const FlyToPointProgress& progress)>& callback) = 0;

    /// @brief 取消订阅指点飞行进度
    /// @deprecated 已废弃。请改用 IFlyToPointModule::cancelListenFlyToTaskEvent。
    /// @param listen_id 订阅时返回的监听 ID
    ///
    /// @internal
    /// 链路: SDK 内部 | 取消 NotificationCenter 监听
    /// @endinternal
    virtual void unsubscribeFlyToPointProgress(int listen_id) = 0;
};

}