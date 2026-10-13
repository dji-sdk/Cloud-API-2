// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "in_flight_wayline_module_impl.h"

#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "notification_center/notification_center.h"
#include <BusinessManager.h>

namespace flight_hub_sdk {
namespace {

InFlightWaylineTaskDetail ToApiTaskDetail(const InFlightTaskInFlightResponse& src) {
    InFlightWaylineTaskDetail detail;
    detail.uuid = src.uuid;
    detail.user_id = src.user_id;
    detail.status = src.status;
    detail.wayline_id = src.wayline_id;
    detail.err_code = src.err_code;
    detail.way_point_index = src.way_point_index;
    detail.progress_percent = src.progress_percent;
    detail.wayline_duration = src.wayline_duration;
    detail.wayline_length = src.wayline_length;
    detail.task_name = src.task_name;
    return detail;
}

ActiveInFlightWaylineTaskInfo ToApiActiveInfo(const GetActiveInFlightWaylineTaskResponse& resp) {
    ActiveInFlightWaylineTaskInfo info;
    info.flight_id = resp.flight_id;
    info.flight_type = resp.flight_type;
    info.sn = resp.sn;
    info.in_flight_task = ToApiTaskDetail(resp.in_flight_task);
    info.flight_task_data = resp.flight_task_data;
    return info;
}

} // namespace

InFlightWaylineModuleImpl::InFlightWaylineModuleImpl(
                                                     const std::string& gateway_sn,
                                                     const std::string& drone_sn)
    : gateway_sn_(gateway_sn), drone_sn_(drone_sn) {
    LOG_INFO("[InFlightWaylineModuleImpl] constructor, drone_sn: %s, gateway_sn: %s",
             drone_sn_.c_str(), gateway_sn_.c_str());
}

InFlightWaylineModuleImpl::~InFlightWaylineModuleImpl() = default;

void InFlightWaylineModuleImpl::createInFlightWaylineTask(
    const CreateInFlightWaylineTaskParams& params,
    const std::function<void(const CreateInFlightWaylineTaskResult& result, FlightHubError err)>& callback) {
    CreateInFlightWaylineTaskRequest request;
    request.drone_sn = drone_sn_;
    request.gateway_sn = gateway_sn_;
    request.wayline_uuid = params.wayline_uuid;
    request.rth_altitude = params.rth_altitude;
    if (params.wayline_precision_type != -1) {
        request.wayline_precision_type = params.wayline_precision_type;
    }
    if (params.rth_mode != -1) {
        request.rth_mode = params.rth_mode;
    }
    if (params.out_of_control_action != -1) {
        request.out_of_control_action = params.out_of_control_action;
    }
    if (params.exit_wayline_when_rc_lost != -1) {
        request.exit_wayline_when_rc_lost = params.exit_wayline_when_rc_lost;
    }
    if (params.wayline_duration != -1.0) {
        request.wayline_duration = params.wayline_duration;
    }
    if (params.wayline_length != -1.0) {
        request.wayline_length = params.wayline_length;
    }
    if (!params.task_name.empty()) {
        request.task_name = params.task_name;
    }
    if (params.fast_reconstruction >= 0) {
        request.fast_reconstruction = params.fast_reconstruction != 0;
    }

    WeakDummy(weak_ptr);
    create_in_flight_wayline_task(request,
        [weak_ptr, callback](const CreateInFlightWaylineTaskResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, response, err]() -> void {
                // 埋点: 创建空中航线任务（自动注入当前设备的 drone_sn / gateway_sn）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_IN_FLIGHT_WAYLINE_CREATE_TASK, err, "[InFlightWaylineModuleImpl]");

                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    callback({}, err);
                    return;
                }
                CreateInFlightWaylineTaskResult result;
                result.flight_task_id = response.flight_task_id;
                result.in_flight_wayline_task_id = response.in_flight_way_line_task_id;
                callback(result, FlightHubError::success());
            });
        });
}

void InFlightWaylineModuleImpl::controlInFlightWaylineTask(
    InFlightWaylineControlAction action,
    const std::function<void(FlightHubError err)>& callback) {
    ControlInFlightWaylineTaskRequest request;
    request.drone_sn = drone_sn_;
    request.action = static_cast<int32_t>(action);

    WeakDummy(weak_ptr);
    control_in_flight_wayline_task(request,
        [weak_ptr, callback](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, err]() -> void {
                // 埋点: 控制空中航线任务（暂停 / 恢复 / 取消）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_IN_FLIGHT_WAYLINE_CONTROL_TASK, err, "[InFlightWaylineModuleImpl]");
                callback(err);
            });
        });
}

void InFlightWaylineModuleImpl::getActiveInFlightWaylineTask(
    const std::function<void(const ActiveInFlightWaylineTaskInfo& info, FlightHubError err)>& callback) {
    WeakDummy(weak_ptr);
    get_active_in_flight_wayline_task(drone_sn_,
        [weak_ptr, callback](const GetActiveInFlightWaylineTaskResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([callback, response, err]() -> void {
                // 埋点: 查询当前设备活跃中的空中航线任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_IN_FLIGHT_WAYLINE_GET_ACTIVE_TASK, err, "[InFlightWaylineModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    callback({}, err);
                    return;
                }
                callback(ToApiActiveInfo(response), FlightHubError::success());
            });
        });
}

int InFlightWaylineModuleImpl::listenInFlightWaylineProgress(
    const std::function<void(const InFlightWaylineProgress& msg)>& callback) {
    WeakDummy(weak_ptr);
    auto listen_id = NotificationCenter::getInstance()->getBusinessManager()->ListenInFlightWaylineTaskNotify(
        [weak_ptr, callback](const notification_center::InFlightWaylineTaskNotify& msg) -> void {
            WeakDummyReturn(weak_ptr);
            if (!callback) {
                return;
            }
            InFlightWaylineProgress progress;
            progress.in_flight_wayline_id = msg.in_flight_wayline_id;
            progress.percent = msg.progress.percent;
            progress.status = msg.status;
            progress.result = msg.result;
            progress.gateway_sn = msg.gateway_sn;
            progress.way_point_index = msg.way_point_index;
            progress.wayline_duration = msg.wayline_duration;
            progress.wayline_length = msg.wayline_length;
            progress.task_name = msg.task_name;
            progress.timestamp = static_cast<long>(msg.timestamp);
            callback(progress);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) -> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[InFlightWaylineModuleImpl][%s] listenInFlightWaylineProgress, error: %d",
                          gateway_sn_.c_str(), error_code);
            }
        },
        gateway_sn_,
        notification_center::NotificationFrequency_OnChanged);
    return listen_id;
}

void InFlightWaylineModuleImpl::cancelListenInFlightWaylineProgress(int listen_id) {
    NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(listen_id);
}

} // namespace flight_hub_sdk
