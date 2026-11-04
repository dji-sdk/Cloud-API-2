// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "fly_to_point_module_impl.h"

#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "session/session_bm.h"

namespace flight_hub_sdk {

FlyToPointModuleImpl::FlyToPointModuleImpl(std::shared_ptr<message_engine::IMessageEngine> message_engine, const std::string& drone_sn, const std::string& gateway_sn, int session_id)
    : message_engine_(message_engine), drone_sn_(drone_sn), gateway_sn_(gateway_sn), session_id_(session_id) {
    auto bm = ResolveBusinessManager(session_id_);
    LOG_INFO("[FlyToPointModuleImpl] constructor, drone_sn: %s", drone_sn_.c_str());
    WeakDummy(weak_ptr);
    if (bm) {
        flyto_task_event_listen_id_ = bm->ListenFlyToTaskEvent(
        [this, weak_ptr](const notification_center::FlyToTaskEvent& msg)-> void {
            WeakDummyReturn(weak_ptr);
            FlyToTaskEvent event;
            event.event_id = msg.event_id;
            event.action = msg.action;
            event.uuid = msg.uuid;
            event.timestamp = msg.timestamp;
            event.task_data.id = msg.task_data.id;
            event.task_data.uuid = msg.task_data.uuid;
            event.task_data.prj_uuid = msg.task_data.prj_uuid;
            event.task_data.sn = msg.task_data.sn;
            event.task_data.drone_sn = msg.task_data.drone_sn;
            event.task_data.user_id = msg.task_data.user_id;
            event.task_data.seq_no = msg.task_data.seq_no;
            event.task_data.flight_task_uuid = msg.task_data.flight_task_uuid;
            event.task_data.flight_task_type = msg.task_data.flight_task_type;
            event.task_data.status = msg.task_data.status;
            event.task_data.err_code = msg.task_data.err_code;
            event.task_data.way_point_index = msg.task_data.way_point_index;
            for (const auto& wp : msg.task_data.way_points) {
                FlyToWayPoint point;
                point.latitude = wp.latitude;
                point.longitude = wp.longitude;
                point.height = wp.height;
                event.task_data.way_points.push_back(point);
            }
            for (const auto& pp : msg.task_data.planned_path_points) {
                FlyToWayPoint point;
                point.latitude = pp.latitude;
                point.longitude = pp.longitude;
                point.height = pp.height;
                event.task_data.planned_path_points.push_back(point);
            }
            event.task_data.remaining_distance = msg.task_data.remaining_distance;
            event.task_data.remaining_time = msg.task_data.remaining_time;
            flyto_task_event_callbacks_.notifyCallbackIfChanged(event);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FlyToPointModuleImpl][%s] listenFlyToTaskEvent, error: %d", gateway_sn_.c_str(), error_code);
            }
        },
        gateway_sn_,
        notification_center::NotificationFrequency_OnChanged);
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }
}

FlyToPointModuleImpl::~FlyToPointModuleImpl() {
    auto bm = ResolveBusinessManager(session_id_);
    if (flyto_task_event_listen_id_ != -1) {
        if (bm) {
            bm->CancelObserve(flyto_task_event_listen_id_);
        }
        flyto_task_event_listen_id_ = -1;
    }
}

void FlyToPointModuleImpl::start(const FlyToPointParams& params, const std::function<void(FlightHubError err, std::string fly_to_id)> callback) {
    startWithResult(params, [callback](FlightHubError err, const FlyToPointStartResult& result) {
        if (callback) callback(err, result.fly_to_id);
    });
}

void FlyToPointModuleImpl::startWithResult(const FlyToPointParams& params,
    const std::function<void(FlightHubError err, const FlyToPointStartResult& result)> callback) {
    FlyToPointRequest request;
    request.device_sn = gateway_sn_;
    // start_point 是航段起点(飞机当前位置)，points 是目标点，两者语义不同、由调用方分别提供。
    request.start_point.latitude  = params.start_point.latitude;
    request.start_point.longitude = params.start_point.longitude;
    request.start_point.height    = params.start_point.height;
    for (const auto& pt : params.points) {
        FlyToPointRequestPoints p;
        p.latitude  = pt.latitude;
        p.longitude = pt.longitude;
        p.height    = pt.height;
        request.points.push_back(p);
    }
    request.max_speed = params.max_speed;

    // 可选字段：公开 API 用 -1 表示未设置；仅设置后才写入 optional（未设置则不下发，设备保持当前设置）
    if (params.speed_mode != -1) {
        request.speed_mode = params.speed_mode;
    }
    if (params.real_height_cap_enabled != -1) {
        request.real_height_cap_enabled = params.real_height_cap_enabled != 0;
    }
    if (params.real_height_cap_limit != -1) {
        request.real_height_cap_limit = params.real_height_cap_limit;
    }
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()->void
    {
        WeakDummyReturn(weak_ptr);
        fly_to_point(request, [this, weak_ptr, callback](const FlyToPointResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]()->void
            {
                WeakDummyReturn(weak_ptr);
                // 埋点: 指点飞行开始; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLY_TO_START, err, session_id_, "[FlyToPointModuleImpl]");

                FlyToPointStartResult result;
                result.fly_to_id = response.fly_to_id;
                result.flight_id = response.flight_id;
                callback(err, result);
            });
        }, session_id_);
    });
}

void FlyToPointModuleImpl::updateTargetPoints(
    const FlyToUpdateTargetPointsParams& params,
    const std::function<void(FlightHubError err)>& callback) {
    // 参数门禁
    if (params.fly_to_id.empty()) {
        LOG_ERROR("[FlyToPointModuleImpl] updateTargetPoints: fly_to_id is empty");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 指点飞行更新目标点; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLY_TO_UPDATE_TARGET_POINTS, param_err, session_id_, "[FlyToPointModuleImpl]");

        if (callback) callback(param_err);
        return;
    }

    if (params.points.empty()) {
        LOG_ERROR("[FlyToPointModuleImpl] updateTargetPoints: points is empty");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 指点飞行更新目标点; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLY_TO_UPDATE_TARGET_POINTS, param_err, session_id_, "[FlyToPointModuleImpl]");

        if (callback) callback(param_err);
        return;
    }

    if (params.max_speed <= 0) {
        LOG_ERROR("[FlyToPointModuleImpl] updateTargetPoints: max_speed <= 0");

        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);

        // 埋点: 指点飞行更新目标点; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLY_TO_UPDATE_TARGET_POINTS, param_err, session_id_, "[FlyToPointModuleImpl]");

        if (callback) callback(param_err);
        return;
    }

    // 构造请求
    UpdateFlyToPointsRequest request;
    request.device_sn = gateway_sn_;
    request.fly_to_id = params.fly_to_id;
    request.start_point.latitude  = params.start_point.latitude;
    request.start_point.longitude = params.start_point.longitude;
    request.start_point.height    = params.start_point.height;
    request.max_speed = params.max_speed;
    for (const auto& pt : params.points) {
        FlyToPointRequestPoints p;
        p.latitude  = pt.latitude;
        p.longitude = pt.longitude;
        p.height    = pt.height;
        request.points.push_back(p);
    }

    // 可选字段：公开 API 用 -1 表示未设置；仅设置后才写入 optional（未设置则不下发，设备保持当前设置）
    if (params.speed_mode != -1) {
        request.speed_mode = params.speed_mode;
    }
    if (params.real_height_cap_enabled != -1) {
        request.real_height_cap_enabled = params.real_height_cap_enabled != 0;
    }
    if (params.real_height_cap_limit != -1) {
        request.real_height_cap_limit = params.real_height_cap_limit;
    }
    if (params.need_verify_flight_control != -1) {
        request.need_verify_flight_control = params.need_verify_flight_control != 0;
    }

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() {
        WeakDummyReturn(weak_ptr);
        update_fly_to_points(request,
            [this, weak_ptr, callback](FlightHubError err) {
                WeakDummyReturn(weak_ptr);
                drc_sdk::common::Executors::Main()->Post([err, callback, this, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                    // 埋点: 指点飞行更新目标点; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                    // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                    // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                    // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                    report_beacon_event_helper(kBeaconReportEvent_FLY_TO_UPDATE_TARGET_POINTS, err, session_id_, "[FlyToPointModuleImpl]");

                    if (callback) callback(err);
                });
            }, session_id_);
    });
}

void FlyToPointModuleImpl::drcStop(const std::function<void(FlightHubError err)> callback) {
    auto msg = std::make_shared<message_engine::DrcDroneEmergencyStopSouthbound>();
    auto err = message_engine_->getCloudMessageManager()->SendDrcDroneEmergencyStopSouthbound(msg, [callback, this](int error_code, const std::shared_ptr<message_engine::DrcReplyDroneEmergencyStopNorthbound>& msg) {
        if (error_code == message_engine::MeErrorCode::ME_NO_ERROR && msg->msg.result == 0)
        {
            auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::SUCCESS);

            // 埋点: 指点飞行急停; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_FLY_TO_DRC_STOP, result, session_id_, "[FlyToPointModuleImpl]");

            callback(result);
            return;
        }

        auto result = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED);

        // 埋点: 指点飞行急停; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLY_TO_DRC_STOP, result, session_id_, "[FlyToPointModuleImpl]");

        callback(result);
    });

    if (err != message_engine::MeErrorCode::ME_NO_ERROR) {
        auto link_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_DRC_LINK);

        // 埋点: 指点飞行急停(DRC 链路异常, 指令未发出); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLY_TO_DRC_STOP, link_err, session_id_, "[FlyToPointModuleImpl]");

        callback(link_err);
    }
}

int FlyToPointModuleImpl::listenFlyToTaskEvent(const std::function<void(const FlyToTaskEvent& msg)>& callback) {
    return flyto_task_event_callbacks_.addCallback(callback);
}

void FlyToPointModuleImpl::cancelListenFlyToTaskEvent(int listen_id) {
    flyto_task_event_callbacks_.removeCallback(listen_id);
}

int FlyToPointModuleImpl::subscribeFlyToDroneSelfPlanPath(const std::function<void(const FlyToDroneSelfPlanPath& path)>& callback) {
    auto bm = ResolveBusinessManager(session_id_);
    WeakDummy(weak_ptr);
    auto listen_id = -1;
    if (bm) {
        listen_id = bm->ListenFlyToDroneSelfPlanPath(
        [this, weak_ptr, callback](const notification_center::FlyToDroneSelfPlanPath& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            FlyToDroneSelfPlanPath path;
            path.fly_to_id = msg.fly_to_id;
            path.sn = msg.sn;
            path.timestamp = msg.timestamp;
            for (const auto& item: msg.planned_path_points)
            {
                FlyToDroneSelfPlanPathPoint point;
                point.height = item.height;
                point.longitude = item.longitude;
                point.latitude = item.latitude;
                path.planned_path_points.emplace_back(point);
            }
            path.remaining_distance = msg.remaining_distance;
            path.remaining_time = msg.remaining_time;
            LOG_INFO("[DroneImplOsdModule] subscribeFlyToDroneSelfPlanPath, fly_to_id: %s", path.fly_to_id.c_str());
            callback(path);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DroneImplOsdModule] subscribeFlyToDroneSelfPlanPath, error: %d", error_code);
            }
        }, gateway_sn_, notification_center::NotificationFrequency_OnChanged);
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }
    return listen_id;
}

void FlyToPointModuleImpl::unsubscribeFlyToDroneSelfPlanPath(int listen_id) {
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        bm->CancelObserve(listen_id);
    }
}


int FlyToPointModuleImpl::subscribeFlyToPointProgress(const std::function<void(const FlyToPointProgress& progress)>& callback)
{
    auto bm = ResolveBusinessManager(session_id_);
    WeakDummy(weak_ptr);
    auto listen_id = -1;
    if (bm) {
        listen_id = bm->ListenFlyToPointProgress(
        [this, weak_ptr, callback](const notification_center::FlyToPointProgress& msg)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (!callback)
            {
                return;
            }
            FlyToPointProgress progress;
            progress.bid = msg.bid;
            progress.tid = msg.tid;
            progress.timestamp = msg.timestamp;
            progress.fly_to_id = msg.fly_to_id;
            progress.flight_id = msg.flight_id;
            progress.task_seq = msg.task_seq;
            progress.sn = msg.sn;
            progress.status = msg.status;
            progress.result = msg.result;
            progress.waypoint_index = msg.waypoint_index;
            LOG_INFO("[DroneImplOsdModule] subscribeFlyToPointProgress, fly_to_id: %s", progress.fly_to_id.c_str());
            callback(progress);
        }, [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void
        {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError)
            {
                LOG_INFO("[DroneImplOsdModule] subscribeFlyToPointProgress, error: %d", error_code);
            }
        }, gateway_sn_, notification_center::NotificationFrequency_OnChanged);
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }
    return listen_id;
}

void FlyToPointModuleImpl::unsubscribeFlyToPointProgress(int listen_id)
{
    auto bm = ResolveBusinessManager(session_id_);
    if (bm) {
        bm->CancelObserve(listen_id);
    }
}
} // namespace flight_hub_sdk