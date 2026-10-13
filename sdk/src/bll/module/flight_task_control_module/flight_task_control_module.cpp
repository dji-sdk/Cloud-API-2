// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_task_control_module.h"

#include "base/common/logger.h"
#include "http_interface/http_interface.h"
#include "common/executors/executors.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

using namespace flight_hub_sdk;

FlightTaskControlModuleImpl::FlightTaskControlModuleImpl(
    std::shared_ptr<message_engine::IMessageEngine> message_engine, const std::string& drone_sn,
    const std::string& gateway_sn): message_engine_(message_engine), drone_sn_(drone_sn), gateway_sn_(gateway_sn)
{
    LOG_INFO("[FlightTaskControlModuleImpl] constructor, drone_sn: %s", drone_sn_.c_str());
    initListen();
}

FlightTaskControlModuleImpl::~FlightTaskControlModuleImpl() {
    if (wayline_task_event_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(wayline_task_event_listen_id_);
        wayline_task_event_listen_id_ = -1;
    }
    LOG_INFO("[FlightTaskControlModuleImpl] dtor");
}

void FlightTaskControlModuleImpl::initListen() {
    WeakDummy(p);
    wayline_task_event_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenWaylineTaskEvent(
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
            event.task_data.current_waypoint_index = msg.task_data.current_waypoint_index;
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
}

int FlightTaskControlModuleImpl::listenWaylineTaskEvent(const std::function<void(const WaylineTaskEvent& msg)>& callback) {
    return wayline_task_event_callbacks_.addCallback(callback);
}

void FlightTaskControlModuleImpl::cancelListenWaylineTaskEvent(int listen_id) {
    wayline_task_event_callbacks_.removeCallback(listen_id);
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
            drc_sdk::common::Executors::Main()->Post([err, callback, response]()->void {
                LOG_INFO("[FlightTaskControlModuleImpl] pause, response: %d %s", response.code, response.message.c_str());

                // 埋点: 航线任务暂停; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_PAUSE, err, "[FlightTaskControlModuleImpl]");

                callback(err);
            });
        });
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
            drc_sdk::common::Executors::Main()->Post([err, callback, response]()->void {
                LOG_INFO("[FlightTaskControlModuleImpl] resume, response: %d %s", response.code, response.message.c_str());

                // 埋点: 航线任务恢复; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_RESUME, err, "[FlightTaskControlModuleImpl]");

                callback(err);
            });
        });
    });
}
