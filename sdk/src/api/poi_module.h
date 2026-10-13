// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include "value.h"
#include <functional>
#include <memory>

namespace flight_hub_sdk {

/// POI（兴趣点环绕）模块接口
class IPoiModule {
public:
    virtual ~IPoiModule() = default;

    /// @brief 进入 POI 环绕模式
    /// @param params 环绕参数（中心点坐标、环绕速度、环绕半径、负载索引）
    /// @param callback 结果回调，成功时返回任务 UUID
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/poi-enter
    /// @endinternal
    virtual void enter(const POIEnterParams& params, const std::function<void(FlightHubError err, const std::string& task_uuid)>& callback) = 0;

    /// @brief 退出 POI 环绕模式
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | DELETE /atc/api/v1/flight-tasks/poi-exit
    /// @endinternal
    virtual void exit(const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 更新 POI 环绕速度
    /// @param speed 目标环绕速度，单位 m/s
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /atc/api/v1/flight-tasks/poi-speed
    /// @endinternal
    virtual void updateSpeed(double speed, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 查询当前 POI 任务信息
    /// @param callback 结果回调，成功时返回 POITaskInfo
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks/poi-task
    /// @endinternal
    virtual void getTask(const std::function<void(FlightHubError err, const POITaskInfo& task)>& callback) = 0;

    /// @brief 订阅 POI 任务事件
    virtual int listenPoiTaskEvent(const std::function<void(const PoiTaskEvent& msg)>& callback) = 0;
    /// @brief 取消订阅 POI 任务事件
    /// @param listen_id 监听 ID
    virtual void cancelListenPoiTaskEvent(int listen_id) = 0;

    /// @brief 订阅 POI 飞行进度通知
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_flight_poi_progress
    /// @endinternal
    virtual int listenFlightPoiProgress(const std::function<void(const FlightPoiProgress& msg)>& callback) = 0;
    /// @brief 取消订阅 POI 飞行进度通知
    /// @param listen_id 监听 ID
    virtual void cancelListenFlightPoiProgress(int listen_id) = 0;
};

}
