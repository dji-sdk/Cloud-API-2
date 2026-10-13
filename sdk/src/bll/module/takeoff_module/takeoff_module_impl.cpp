// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "takeoff_module_impl.h"

#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include <string>

using namespace flight_hub_sdk;



TakeoffModuleImpl::TakeoffModuleImpl(const std::string& drone_sn, const std::string& gateway_sn)
    : drone_sn_(drone_sn), gateway_sn_(gateway_sn) {
    LOG_INFO("[TakeoffModuleImpl] constructor, drone_sn: %s, gateway_sn: %s",
        drone_sn_.c_str(), gateway_sn_.c_str());
    registerNotificationCenterListeners();
}

TakeoffModuleImpl::~TakeoffModuleImpl() {
    if (command_task_event_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(command_task_event_listen_id_);
    }
    if (drone_commander_flight_mode_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_commander_flight_mode_listen_id_);
    }
    if (drone_current_commander_flight_mode_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_current_commander_flight_mode_listen_id_);
    }
    if (drone_commander_mode_lost_action_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(drone_commander_mode_lost_action_listen_id_);
    }
}

void TakeoffModuleImpl::registerNotificationCenterListeners() {
    WeakDummy(p);
    command_task_event_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenCommandTaskEvent(
        [this, p](const notification_center::CommandTaskEvent& msg) -> void {
            WeakDummyReturn(p);
            CommandTaskEvent event;
            event.event_id = msg.event_id;
            event.action = msg.action;
            event.uuid = msg.uuid;
            event.timestamp = msg.timestamp;
            event.task_data.id = msg.task_data.id;
            event.task_data.created_at = msg.task_data.created_at;
            event.task_data.uuid = msg.task_data.uuid;
            event.task_data.prj_uuid = msg.task_data.prj_uuid;
            event.task_data.sn = msg.task_data.sn;
            event.task_data.drone_sn = msg.task_data.drone_sn;
            event.task_data.track_id = msg.task_data.track_id;
            event.task_data.user_id = msg.task_data.user_id;
            event.task_data.status = msg.task_data.status;
            event.task_data.err_code = msg.task_data.err_code;
            event.task_data.takeoff_mode = msg.task_data.takeoff_mode;
            event.task_data.security_takeoff_height = msg.task_data.security_takeoff_height;
            event.task_data.max_speed = msg.task_data.max_speed;
            event.task_data.out_of_control_action = msg.task_data.out_of_control_action;
            event.task_data.rth_altitude = msg.task_data.rth_altitude;
            event.task_data.commander_flight_mode = msg.task_data.commander_flight_mode;
            event.task_data.commander_flight_height = msg.task_data.commander_flight_height;
            event.task_data.rth_mode = msg.task_data.rth_mode;
            event.task_data.cloud_to_cloud_id = msg.task_data.cloud_to_cloud_id;
            LOG_DEBUG("[TakeoffModuleImpl][%s] listenCommandTaskEvent, event_id: %s, action: %s, uuid: %s",
                gateway_sn_.c_str(), event.event_id.c_str(), event.action.c_str(), event.uuid.c_str());
            command_task_event_callbacks_.notifyCallbackIfChanged(event);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[TakeoffModuleImpl][%s] listenCommandTaskEvent, error: %d", gateway_sn_.c_str(), error_code);
            }
        }, gateway_sn_, notification_center::NotificationFrequency_OnChanged);

    // 指点飞行模式设置值(commander_flight_mode);
    drone_commander_flight_mode_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneCommanderFlightMode(
        [this, p](const notification_center::DroneCommanderFlightMode& msg) -> void {
            WeakDummyReturn(p);
            DroneCommanderFlightMode res{};
            res.commander_flight_mode = msg.commander_flight_mode;
            LOG_DEBUG("[TakeoffModuleImpl][%s] listenDroneCommanderFlightMode, commander_flight_mode: %lld",
                drone_sn_.c_str(), (long long)msg.commander_flight_mode);
            drone_commander_flight_mode_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[TakeoffModuleImpl][%s] listenDroneCommanderFlightMode, error: %d", drone_sn_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 指点飞行模式当前值(current_commander_flight_mode);
    drone_current_commander_flight_mode_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneCurrentCommanderFlightMode(
        [this, p](const notification_center::DroneCurrentCommanderFlightMode& msg) -> void {
            WeakDummyReturn(p);
            DroneCurrentCommanderFlightMode res{};
            res.current_commander_flight_mode = msg.current_commander_flight_mode;
            LOG_DEBUG("[TakeoffModuleImpl][%s] listenDroneCurrentCommanderFlightMode, current_commander_flight_mode: %lld",
                drone_sn_.c_str(), (long long)msg.current_commander_flight_mode);
            drone_current_commander_flight_mode_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[TakeoffModuleImpl][%s] listenDroneCurrentCommanderFlightMode, error: %d", drone_sn_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);

    // 指点飞行失控动作(commander_mode_lost_action);
    drone_commander_mode_lost_action_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenDroneCommanderModeLostAction(
        [this, p](const notification_center::DroneCommanderModeLostAction& msg) -> void {
            WeakDummyReturn(p);
            DroneCommanderModeLostAction res{};
            res.commander_mode_lost_action = msg.commander_mode_lost_action;
            LOG_DEBUG("[TakeoffModuleImpl][%s] listenDroneCommanderModeLostAction, commander_mode_lost_action: %lld",
                drone_sn_.c_str(), (long long)msg.commander_mode_lost_action);
            drone_commander_mode_lost_action_callbacks_.notifyCallbackIfChanged(res);
        }, [this, p](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(p);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[TakeoffModuleImpl][%s] listenDroneCommanderModeLostAction, error: %d", drone_sn_.c_str(), error_code);
            }
        }, drone_sn_, notification_center::NotificationFrequency_OnChanged);
}

void TakeoffModuleImpl::takeoff(
    const TakeoffParams& params,
    const std::function<void(FlightHubError err, const TakeoffResult& result)>& callback) {
    LOG_INFO("[TakeoffModuleImpl] takeoff, gateway_sn: %s", gateway_sn_.c_str());

    DroneTakeoffRequest request;
    request.security_takeoff_height = params.security_takeoff_height;
    request.device_sn = gateway_sn_;
    request.max_speed = params.max_speed;

    switch (static_cast<OutOfControlAction>(params.out_of_control_action)) {
        case OutOfControlAction::HOVER:
            request.out_of_control_action = "Hover";
            break;
        case OutOfControlAction::RETURN_HOME:
            request.out_of_control_action = "ReturnHome";
            break;
        case OutOfControlAction::CONTINUE:
            request.out_of_control_action = "Continue";
            break;
        default:
            request.out_of_control_action = "ReturnHome";
            break;
    }

    request.rth_altitude = params.rth_altitude;
    request.target_height = params.target_height;
    request.target_latitude = params.target_latitude;
    request.target_longitude = params.target_longitude;

    switch (static_cast<TakeoffMode>(params.takeoff_mode)) {
        case TakeoffMode::TAKEOFF:
            request.takeoff_mode = "Takeoff";
            break;
        case TakeoffMode::TAKEOFF_WITH_FLY_TO:
            request.takeoff_mode = "TakeoffWithFlyTo";
            break;
        default:
            request.takeoff_mode = "Takeoff";
            break;
    }

    // 可选字段：公开 API 用 -1 / 空串表示未设置；仅设置后才写入 optional
    if (params.commander_flight_mode != -1) {
        request.commander_flight_mode = params.commander_flight_mode;
    }
    if (params.commander_flight_height != -1) {
        request.commander_flight_height = params.commander_flight_height;
    }
    if (params.rth_mode != -1) {
        request.rth_mode = params.rth_mode;
    }
    if (!params.cloud_to_cloud_id.empty()) {
        request.cloud_to_cloud_id = params.cloud_to_cloud_id;
    }
    if (params.need_verify_flight_control != -1) {
        request.need_verify_flight_control = params.need_verify_flight_control != 0;
    }

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        drone_takeoff(request, [this, weak_ptr, callback](const DroneTakeoffResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, gateway_sn = gateway_sn_]() -> void {
                // 埋点: 一键起飞; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_TAKE_OFF, err, "[TakeoffModuleImpl]");

                LOG_INFO("[TakeoffModuleImpl] takeoff response: flight_id=%s, fly_to_id=%s",
                    response.flight_id.c_str(), response.fly_to_id.c_str());
                TakeoffResult result;
                result.flight_id = response.flight_id;
                result.fly_to_id = response.fly_to_id;
                if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                    callback(FlightHubError::success(), result);
                } else {
                    callback(err, result);
                }
            });
        });
    });
}

int TakeoffModuleImpl::listenCommandTaskEvent(
    const std::function<void(const CommandTaskEvent& msg)>& callback) {
    return command_task_event_callbacks_.addCallback(callback);
}

void TakeoffModuleImpl::cancelListenCommandTaskEvent(int listen_id) {
    command_task_event_callbacks_.removeCallback(listen_id);
}

int TakeoffModuleImpl::listenDroneCommanderFlightMode(
    const std::function<void(const DroneCommanderFlightMode& msg)>& callback) {
    return drone_commander_flight_mode_callbacks_.addCallback(callback);
}

void TakeoffModuleImpl::cancelListenDroneCommanderFlightMode(int listen_id) {
    drone_commander_flight_mode_callbacks_.removeCallback(listen_id);
}

int TakeoffModuleImpl::listenDroneCurrentCommanderFlightMode(
    const std::function<void(const DroneCurrentCommanderFlightMode& msg)>& callback) {
    return drone_current_commander_flight_mode_callbacks_.addCallback(callback);
}

void TakeoffModuleImpl::cancelListenDroneCurrentCommanderFlightMode(int listen_id) {
    drone_current_commander_flight_mode_callbacks_.removeCallback(listen_id);
}

int TakeoffModuleImpl::listenDroneCommanderModeLostAction(
    const std::function<void(const DroneCommanderModeLostAction& msg)>& callback) {
    return drone_commander_mode_lost_action_callbacks_.addCallback(callback);
}

void TakeoffModuleImpl::cancelListenDroneCommanderModeLostAction(int listen_id) {
    drone_commander_mode_lost_action_callbacks_.removeCallback(listen_id);
}
