// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include "message.h"
#include <functional>

namespace flight_hub_sdk {

/// 部署/踏勘模块接口，提供离场点监听、视觉踏勘、星历下载、进离场航线侦察等能力。
class IDeployModule {
public:
    virtual ~IDeployModule() = default;

    /// @brief 订阅机场离场点信息
    /// @param callback 离场点回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_dock_departure_point
    /// @endinternal
    virtual int listenDockDeparturePoint(const std::function<void(const DockDeparturePoint& msg)>& callback) = 0;
    /// @brief 取消订阅机场离场点信息
    /// @param listen_id 监听 ID
    virtual void cancelListenDockDeparturePoint(int listen_id) = 0;

    /// @brief 订阅进离场航线侦察任务状态
    /// @param callback 侦察任务状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_visual_scout_status
    /// @endinternal
    virtual int listenAircraftVisualScoutStatus(const std::function<void(const AircraftVisualScoutStatus& msg)>& callback) = 0;
    /// @brief 取消订阅进离场航线侦察任务状态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftVisualScoutStatus(int listen_id) = 0;

    /// @brief 订阅踏勘任务异常信息
    /// @param callback 异常信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_visual_scout_abnormal_info
    /// @endinternal
    virtual int listenAircraftVisualScoutAbnormalInfo(const std::function<void(const AircraftVisualScoutAbnormalInfo& msg)>& callback) = 0;
    /// @brief 取消订阅踏勘任务异常信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftVisualScoutAbnormalInfo(int listen_id) = 0;

    /// @brief 订阅障碍物过近告警
    /// @param callback 障碍物信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_visual_scout_obstacle_info
    /// @endinternal
    virtual int listenAircraftVisualScoutObstacleInfo(const std::function<void(const AircraftVisualScoutObstacleInfo& msg)>& callback) = 0;
    /// @brief 取消订阅障碍物过近告警
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftVisualScoutObstacleInfo(int listen_id) = 0;

    /// @brief 订阅星历下载状态
    /// @param callback 下载状态回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_ephemeris_download_state
    /// @endinternal
    virtual int listenAircraftEphemerisDownloadState(const std::function<void(const EphemerisDownloadState& msg)>& callback) = 0;
    /// @brief 取消订阅星历下载状态
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftEphemerisDownloadState(int listen_id) = 0;

    /// @brief 订阅星历信息
    /// @param callback 星历信息回调
    /// @return 监听 ID，用于取消订阅
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_drone_ephemeris_info
    /// @endinternal
    virtual int listenAircraftEphemerisInfo(const std::function<void(const EphemerisInfo& msg)>& callback) = 0;
    /// @brief 取消订阅星历信息
    /// @param listen_id 监听 ID
    virtual void cancelListenAircraftEphemerisInfo(int listen_id) = 0;

    /// @brief 下载星历数据
    /// @param params 下载参数（星历 URL、文件名）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/manual/scout/ephemeris/url
    /// @endinternal
    virtual void downloadEphemeris(const DownloadEphemerisParams& params, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 执行进离场航线侦察任务（开始/暂停）
    /// @param params 侦察任务参数
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/manual/scout/task
    /// @endinternal
    virtual void executeInOutWaylineDetect(const InOutWaylineDetectParams& params, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 设置进离场航线飞行参数
    /// @param params 飞行设置参数（返航高度、失联动作、最大高度/距离限制）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /control/api/v1/manual/scout/flight/setting
    /// @endinternal
    virtual void setInOutWaylineFlight(const InOutWaylineFlightSettingParams& params, const std::function<void(FlightHubError err)>& callback) = 0;

    /// @brief 获取进离场航线飞行设置
    /// @param callback 结果回调，成功时返回当前飞行设置参数
    ///
    /// @internal
    /// 链路: HTTP | GET /control/api/v1/manual/scout/flight/setting
    /// @endinternal
    virtual void getInOutWaylineFlightSetting(const std::function<void(const InOutWaylineFlightSettingParams& params, FlightHubError err)>& callback) = 0;
};

} // namespace flight_hub_sdk
