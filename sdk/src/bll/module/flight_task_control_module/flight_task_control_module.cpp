// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_task_control_module.h"

#include "base/common/logger.h"
#include "http_interface/http_interface.h"
#include "common/executors/executors.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "session/session_bm.h"

using namespace flight_hub_sdk;

namespace {
ReturnHomeInfoEvent ConvertReturnHomeInfoEvent(const notification_center::ReturnHomeInfoEvent& msg) {
    ReturnHomeInfoEvent event;
    event.flight_id = msg.flight_id;
    event.sn = msg.sn;
    event.info.timestamp = msg.info.timestamp;
    for (const auto& wp : msg.info.planned_path_points) {
        Waypoint waypoint;
        waypoint.latitude = wp.latitude;
        waypoint.longitude = wp.longitude;
        waypoint.height = wp.height;
        event.info.planned_path_points.push_back(waypoint);
    }
    event.info.last_point_type = msg.info.last_point_type;
    event.info.home_dock_sn = msg.info.home_dock_sn;
    for (const auto& dock_info : msg.info.multi_dock_home_info) {
        MultiDockHomeInfo multi_dock_home_info;
        multi_dock_home_info.sn = dock_info.sn;
        multi_dock_home_info.plan_status = dock_info.plan_status;
        multi_dock_home_info.estimated_battery_consumption = dock_info.estimated_battery_consumption;
        multi_dock_home_info.home_distance = dock_info.home_distance;
        event.info.multi_dock_home_info.push_back(multi_dock_home_info);
    }
    return event;
}
}

FlightTaskControlModuleImpl::FlightTaskControlModuleImpl(
    std::shared_ptr<message_engine::IMessageEngine> message_engine, const std::string& drone_sn,
    const std::string& gateway_sn, int session_id): message_engine_(message_engine), drone_sn_(drone_sn), gateway_sn_(gateway_sn), session_id_(session_id) {
    LOG_INFO("[FlightTaskControlModuleImpl] constructor, drone_sn: %s", drone_sn_.c_str());
    initListen();
}

FlightTaskControlModuleImpl::~FlightTaskControlModuleImpl() {
    auto bm = ResolveBusinessManager(session_id_);
    if (wayline_task_event_listen_id_ != -1) {
        if (bm) {
            bm->CancelObserve(wayline_task_event_listen_id_);
        }
        wayline_task_event_listen_id_ = -1;
    }
    if (return_home_info_event_listen_id_ != -1) {
        if (bm) {
            bm->CancelObserve(return_home_info_event_listen_id_);
        }
        return_home_info_event_listen_id_ = -1;
    }
    {
        std::lock_guard<std::mutex> lock(return_home_info_event_by_device_listen_ids_mutex_);
        for (const auto listen_id : return_home_info_event_by_device_listen_ids_) {
            if (bm) {
                bm->CancelObserve(listen_id);
            }
        }
        return_home_info_event_by_device_listen_ids_.clear();
    }
    if (task_exception_event_listen_id_ != -1) {
        if (bm) {
            bm->CancelObserve(task_exception_event_listen_id_);
        }
        task_exception_event_listen_id_ = -1;
    }
    LOG_INFO("[FlightTaskControlModuleImpl] dtor");
}

void FlightTaskControlModuleImpl::initListen() {
    auto bm = ResolveBusinessManager(session_id_);
    WeakDummy(p);
    if (bm) {
        wayline_task_event_listen_id_ = bm->ListenWaylineTaskEvent(
        [this, p](const notification_center::WaylineTaskEvent& msg)-> void {
            WeakDummyReturn(p);
            WaylineTaskEvent event;
            event.event_id = msg.event_id;
            event.action = msg.action;
            event.uuid = msg.uuid;
            event.timestamp = msg.timestamp;
            event.task_data.name = msg.task_data.name;
            event.task_data.uuid = msg.task_data.uuid;
            event.task_data.task_type = msg.task_data.task_type;
            event.task_data.status = msg.task_data.status;
            event.task_data.sn = msg.task_data.sn;
            event.task_data.landing_dock_sn = msg.task_data.landing_dock_sn;
            event.task_data.wayline_uuid = msg.task_data.wayline_uuid;
            event.task_data.wayline_precision_type = msg.task_data.wayline_precision_type;
            event.task_data.wayline_duration = msg.task_data.wayline_duration;
            event.task_data.wayline_segment_indexes = msg.task_data.wayline_segment_indexes;
            event.task_data.total_waypoints = msg.task_data.total_waypoints;
            event.task_data.current_waypoint_index = msg.task_data.current_waypoint_index;
            event.task_data.bid = msg.task_data.bid;
            event.task_data.begin_at = msg.task_data.begin_at;
            event.task_data.end_at = msg.task_data.end_at;
            event.task_data.run_at = msg.task_data.run_at;
            event.task_data.completed_at = msg.task_data.completed_at;
            event.task_data.out_of_control_action_in_flight = msg.task_data.out_of_control_action_in_flight;
            event.task_data.rth_altitude = msg.task_data.rth_altitude;
            event.task_data.rth_mode = msg.task_data.rth_mode;
            event.task_data.min_battery_capacity = msg.task_data.min_battery_capacity;
            event.task_data.tags = msg.task_data.tags;
            event.task_data.cloud_to_cloud_id = msg.task_data.cloud_to_cloud_id;
            event.task_data.break_point.index = msg.task_data.break_point.index;
            event.task_data.break_point.state = msg.task_data.break_point.state;
            event.task_data.break_point.progress = msg.task_data.break_point.progress;
            event.task_data.break_point.wayline_id = msg.task_data.break_point.wayline_id;
            event.task_data.break_point.break_reason = msg.task_data.break_point.break_reason;
            event.task_data.break_point.latitude = msg.task_data.break_point.latitude;
            event.task_data.break_point.longitude = msg.task_data.break_point.longitude;
            event.task_data.break_point.height = msg.task_data.break_point.height;
            event.task_data.break_point.attitude_head = msg.task_data.break_point.attitude_head;
            event.task_data.is_break_point_resume = msg.task_data.is_break_point_resume;
            event.task_data.media_upload_status = msg.task_data.media_upload_status;
            event.task_data.fast_reconstruction = msg.task_data.fast_reconstruction;
            event.task_data.device_type = msg.task_data.device_type;
            event.task_data.obstacles.reserve(msg.task_data.obstacles.size());
            for (const auto& o : msg.task_data.obstacles) {
                flight_hub_sdk::WaylineObstacle obstacle;
                obstacle.id = o.id;
                obstacle.type = o.type;
                obstacle.timestamp = o.timestamp;
                obstacle.latitude = o.latitude;
                obstacle.longitude = o.longitude;
                obstacle.height = o.height;
                obstacle.wayline_id = o.wayline_id;
                obstacle.waypoint_index = o.waypoint_index;
                event.task_data.obstacles.push_back(obstacle);
            }
            event.task_data.created_at = msg.task_data.created_at;
            event.task_data.updated_at = msg.task_data.updated_at;
            event.task_data.deleted_at = msg.task_data.deleted_at;
            wayline_task_event_callbacks_.notifyCallbackIfChanged(event);
        },
        [this, p](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenWaylineTaskEvent, error: %d", gateway_sn_.c_str(), error_code);
            }
        },
        gateway_sn_,
        notification_center::NotificationFrequency_OnChanged);

    WeakDummy(p2);
    return_home_info_event_listen_id_ = bm->ListenReturnHomeInfoEvent(
        [this, p2](const notification_center::ReturnHomeInfoEvent& msg)-> void {
            WeakDummyReturn(p2);
            return_home_info_event_callbacks_.notifyCallbackIfChanged(ConvertReturnHomeInfoEvent(msg));
        },
        [this, p2](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p2);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenReturnHomeInfoEvent, error: %d", gateway_sn_.c_str(), error_code);
            }
        },
        gateway_sn_,
        notification_center::NotificationFrequency_OnChanged);

    WeakDummy(p3);
    task_exception_event_listen_id_ = bm->ListenTaskExceptionEvent(
        [this, p3](const notification_center::TaskExceptionEvent& msg)-> void {
            WeakDummyReturn(p3);
            TaskExceptionEvent event;
            event.uuid = msg.uuid;
            event.name = msg.name;
            event.sn = msg.sn;
            event.task_type = msg.task_type;
            event.status = msg.status;
            event.timestamp = msg.timestamp;
            for (const auto& item : msg.exceptions) {
                TaskExceptionItem exception_item;
                exception_item.code = item.code;
                exception_item.message = item.message;
                exception_item.sn = item.sn;
                exception_item.happen_at = item.happen_at;
                event.exceptions.push_back(exception_item);
            }
            task_exception_event_callbacks_.notifyCallbackIfChanged(event);
        },
        [this, p3](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p3);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenTaskExceptionEvent, error: %d", gateway_sn_.c_str(), error_code);
            }
        },
        gateway_sn_,
        notification_center::NotificationFrequency_OnChanged);
    } else {
        LOG_ERROR("[session_bm] null, skip Listen for session");
    }
}

int FlightTaskControlModuleImpl::listenWaylineTaskEvent(const std::function<void(const WaylineTaskEvent& msg)>& callback) {
    return wayline_task_event_callbacks_.addCallback(callback);
}

void FlightTaskControlModuleImpl::cancelListenWaylineTaskEvent(int listen_id) {
    wayline_task_event_callbacks_.removeCallback(listen_id);
}

int FlightTaskControlModuleImpl::listenReturnHomeInfoEvent(const std::function<void(const ReturnHomeInfoEvent& msg)>& callback) {
    return return_home_info_event_callbacks_.addCallback(callback);
}

void FlightTaskControlModuleImpl::cancelListenReturnHomeInfoEvent(int listen_id) {
    return_home_info_event_callbacks_.removeCallback(listen_id);
}

int FlightTaskControlModuleImpl::listenReturnHomeInfoEventByDevice(const std::string& device_sn, const std::function<void(const ReturnHomeInfoEvent& msg)>& callback) {
    if (!callback) {
        LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenReturnHomeInfoEventByDevice, empty callback", gateway_sn_.c_str());
        return -1;
    }
    if (device_sn.empty()) {
        LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenReturnHomeInfoEventByDevice, empty device_sn", gateway_sn_.c_str());
        return -1;
    }

    WeakDummy(p);
    auto bm = ResolveBusinessManager(session_id_);
    if (!bm) {
        LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenReturnHomeInfoEventByDevice, session_bm null", gateway_sn_.c_str());
        return -1;
    }
    auto listen_id = bm->ListenReturnHomeInfoEvent(
        [p, callback](const notification_center::ReturnHomeInfoEvent& msg)-> void {
            WeakDummyReturn(p);
            callback(ConvertReturnHomeInfoEvent(msg));
        },
        [this, p, device_sn](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenReturnHomeInfoEventByDevice(%s), error: %d", gateway_sn_.c_str(), device_sn.c_str(), error_code);
            }
        },
        device_sn,
        notification_center::NotificationFrequency_OnChanged);

    if (listen_id < 0) {
        LOG_ERROR("[FlightTaskControlModuleImpl][%s] listenReturnHomeInfoEventByDevice(%s) failed, ret: %lld", gateway_sn_.c_str(), device_sn.c_str(), static_cast<long long>(listen_id));
        return static_cast<int>(listen_id);
    }

    {
        std::lock_guard<std::mutex> lock(return_home_info_event_by_device_listen_ids_mutex_);
        return_home_info_event_by_device_listen_ids_.insert(static_cast<int>(listen_id));
    }
    return static_cast<int>(listen_id);
}

void FlightTaskControlModuleImpl::cancelListenReturnHomeInfoEventByDevice(int listen_id) {
    std::lock_guard<std::mutex> lock(return_home_info_event_by_device_listen_ids_mutex_);
    if (return_home_info_event_by_device_listen_ids_.erase(listen_id) > 0) {
        auto bm = ResolveBusinessManager(session_id_);
        if (bm) {
            bm->CancelObserve(listen_id);
        }
    }
}

int FlightTaskControlModuleImpl::listenTaskExceptionEvent(const std::function<void(const TaskExceptionEvent& msg)>& callback) {
    return task_exception_event_callbacks_.addCallback(callback);
}

void FlightTaskControlModuleImpl::cancelListenTaskExceptionEvent(int listen_id) {
    task_exception_event_callbacks_.removeCallback(listen_id);
}

void FlightTaskControlModuleImpl::pause(const std::function<void(FlightHubError err)>& callback) {
    FlightTaskPauseRequest request;
    request.sn = gateway_sn_;

    LOG_INFO("[FlightTaskControlModuleImpl] pause, sn: %s %s, type: %s, method: %s", drone_sn_.c_str(), request.sn.c_str(), request.type.c_str(), request.method.c_str());

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()->void {
        WeakDummyReturn(weak_ptr);
        flight_task_pause(request, [this, weak_ptr, callback](const NonRealtimeCmdResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]()->void {
                WeakDummyReturn(weak_ptr);
                LOG_INFO("[FlightTaskControlModuleImpl] pause, response: %d %s", response.code, response.message.c_str());

                // 埋点: 航线任务暂停; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_PAUSE, err, session_id_, "[FlightTaskControlModuleImpl]");

                callback(err);
            });
        }, session_id_);
    });
}

void FlightTaskControlModuleImpl::resume(const std::function<void(FlightHubError err)>& callback) {
    FlightTaskResumeRequest request;
    request.sn = gateway_sn_;

    LOG_INFO("[FlightTaskControlModuleImpl] resume, sn: %s %s, type: %s, method: %s", drone_sn_.c_str(), request.sn.c_str(), request.type.c_str(), request.method.c_str());

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()->void {
        WeakDummyReturn(weak_ptr);
        flight_task_resume(request, [this, weak_ptr, callback](const NonRealtimeCmdResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, this, weak_ptr]()->void {
                WeakDummyReturn(weak_ptr);
                LOG_INFO("[FlightTaskControlModuleImpl] resume, response: %d %s", response.code, response.message.c_str());

                // 埋点: 航线任务恢复; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_RESUME, err, session_id_, "[FlightTaskControlModuleImpl]");

                callback(err);
            });
        }, session_id_);
    });
}
