// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#pragma once

#include <functional>
#include <memory>
#include <string>
#include <vector>
#include "message.h"

namespace flight_hub_sdk {

/// 航线任务管理模块接口，提供任务的增删改查、执行、状态更新、设备推荐、TCAS 等功能。
class IFlightTaskModule {
public:
    virtual ~IFlightTaskModule() = default;

    /// @brief 获取航线任务列表
    /// @param params 查询参数（分页、SN 过滤、时间范围、状态过滤等）
    /// @param callback 结果回调，成功时返回任务列表和分页信息
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks
    /// @endinternal
    virtual void getFlightTaskList(const FlightTaskListParams& params, std::function<void(FlightHubError err, const FlightTaskListResult& result)> callback) = 0;

    /// @brief 获取一键起飞任务列表
    /// @param params 查询参数（分页、SN 过滤、时间范围、状态过滤等）
    /// @param callback 结果回调，成功时返回任务列表和分页信息
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/command-tasks
    /// @endinternal
    virtual void getCommandTaskList(const GetCommandTaskListParams& params, std::function<void(FlightHubError err, const GetCommandTaskListResults& results)> callback) = 0;

    /// @brief 查询飞行任务的飞行轨迹
    /// @param params 查询参数（task_uuid 必填）
    /// @param callback 结果回调，成功时返回任务信息和轨迹点列表
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks/track
    /// @endinternal
    virtual void getFlightTaskTrack(const GetFlightTaskTrackParams& params, std::function<void(FlightHubError err, const GetFlightTaskTrackResults& results)> callback) = 0;

    /// @brief 创建航线任务
    /// @param req 创建任务请求（名称、SN、航线、起止时间等）
    /// @param callback 结果回调，成功时返回任务 UUID
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks
    /// @endinternal
    virtual void createFlightTask(const CreateTaskReq& req, std::function<void(FlightHubError err, const CreateTaskResp& result)> callback) = 0;

    /// @brief 获取航线任务详情
    /// @param task_uuid 任务 UUID
    /// @param callback 结果回调，成功时返回任务详情
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks/{task_uuid}
    /// @endinternal
    virtual void getFlightTaskDetail(const std::string& task_uuid, std::function<void(FlightHubError err, const TaskDetail& result)> callback) = 0;

    /// @brief 编辑航线任务（全量替换）
    /// @param task_uuid 任务 UUID
    /// @param req 编辑请求。服务端为全量替换设计，所有必填字段（name, task_type, time_zone, sn,
    ///           wayline_uuid, out_of_control_action_in_flight, rth_altitude）每次都必须传入。
    ///           建议先调用 getFlightTaskDetail 获取完整数据，修改目标字段后再传入。
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /atc/api/v1/flight-tasks/{task_uuid}
    /// @endinternal
    virtual void editFlightTask(const std::string& task_uuid, const EditTaskReq& req, std::function<void(FlightHubError err, const CreateTaskResp& result)> callback) = 0;

    /// @brief 删除航线任务
    /// @param task_uuid 任务 UUID
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | DELETE /atc/api/v1/flight-tasks/{task_uuid}
    /// @endinternal
    virtual void deleteFlightTask(const std::string& task_uuid, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 更新航线任务状态
    /// @param task_uuid 任务 UUID
    /// @param status 目标状态值
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /atc/api/v1/flight-tasks/{task_uuid}/status
    /// @endinternal
    virtual void updateFlightTaskStatus(const std::string& task_uuid, int status, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 批量更新航线任务状态
    /// @param req 批量更新请求（任务 UUID 列表 + 目标状态）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /atc/api/v1/flight-tasks/batch/status
    /// @endinternal
    virtual void batchUpdateFlightTaskStatus(const BatchUpdateStatusReq& req, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 获取设备推荐（根据航线和策略推荐最优设备）
    /// @param req 推荐请求（SN 列表、航线坐标、策略 ID 等）
    /// @param callback 结果回调，成功时返回推荐设备列表
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/device-recommendation
    /// @endinternal
    virtual void getDeviceRecommendation(const DeviceRecommendationReq& req, std::function<void(FlightHubError err, const DeviceRecommendationResp& result)> callback) = 0;

    /// @brief 获取设备 TCAS（防碰撞）信息
    /// @param center_sn 中心设备 SN 列表
    /// @param callback 结果回调，成功时返回 TCAS 信息列表
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks/tcas
    /// @endinternal
    virtual void getDeviceTcas(const std::vector<std::string>& center_sn, std::function<void(FlightHubError err, const GetTCAInfosResp& result)> callback) = 0;

    /// @brief 更新设备 TCAS（防碰撞）信息
    /// @param center_sn 中心设备 SN 列表
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | PUT /atc/api/v1/flight-tasks/tcas
    /// @endinternal
    virtual void updateDeviceTcas(const std::vector<std::string>& center_sn, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 开始漫游（针对进行中架次开启漫游基站调度）
    /// @param params drone_sn + flight_id + source_task_type（1=航线，2=command_tasks）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/start-roaming
    /// @endinternal
    virtual void startRoaming(const StartRoamingParams& params, std::function<void(FlightHubError err)> callback) = 0;

    /// @brief 设置飞机级漫游基站白名单
    /// @param params drone_sn + station_sns
    /// @param callback 结果回调（含逐 SN 成功/失败原因）
    ///
    /// @internal
    /// 链路: HTTP | POST /atc/api/v1/flight-tasks/roaming/base-stations
    /// @endinternal
    virtual void setRoamingStationSn(const SetRoamingStationSnParams& params,
                                     std::function<void(FlightHubError err, const SetRoamingStationSnResult& result)> callback) = 0;

    /// @brief 查询飞行中任务拓扑（起飞基站等）
    /// @param drone_sns 飞机 SN 列表（1–10）
    /// @param callback 结果回调
    ///
    /// @internal
    /// 链路: HTTP | GET /atc/api/v1/flight-tasks/in-flight-task-topos
    /// @endinternal
    virtual void getInFlightTaskTopos(const std::vector<std::string>& drone_sns,
                                      std::function<void(FlightHubError err, const GetInFlightTaskToposResult& result)> callback) = 0;

    /// @brief 订阅任务拓扑变更（漫游期间架次 topo 变化）
    /// @return 监听 ID；失败返回 -1（当前 NCSDK Listen 未齐时可 stub）
    ///
    /// @internal
    /// 链路: WebSocket | topic: topic_task_topo_changed
    /// 来源: es-task · 消息汇总 pageId=669868012
    /// @endinternal
    virtual int listenTaskTopoChanged(const std::function<void(const TaskTopoChangedEvent&)>& callback) = 0;

    /// @brief 取消任务拓扑变更订阅
    virtual void cancelListenTaskTopoChanged(int listen_id) = 0;
};

}  // namespace flight_hub_sdk
