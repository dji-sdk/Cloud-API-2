// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "poi_module_impl.h"

#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include "http_interface/request_message.h"
#include "http_interface/response_message.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include "device_capability_mgr/device_capability_mgr.h"
#include "topo_registry/topo_registry.h"
#include <BusinessManager.h>

#include <utility>

namespace flight_hub_sdk {

// 转换函数：从内部 POITask 转换为用户接口 POITaskInfo
POITaskInfo convertToPOITaskInfo(const POITaskResponse& internal_task) {
    POITaskInfo user_task;
    user_task.uuid = internal_task.uuid;
    user_task.device_sn = internal_task.device_sn;
    user_task.sn = internal_task.sn;
    user_task.status = internal_task.status;
    user_task.err_code = internal_task.err_code;
    user_task.poi_center_point.height = internal_task.poi_center_point.height;
    user_task.poi_center_point.latitude = internal_task.poi_center_point.latitude;
    user_task.poi_center_point.longitude = internal_task.poi_center_point.longitude;
    user_task.speed = internal_task.speed;
    user_task.circle_radius = internal_task.circle_radius;
    user_task.payload_index = internal_task.payload_index;
    
    return user_task;
}

PoiModuleImpl::PoiModuleImpl(std::shared_ptr<message_engine::IMessageEngine> message_engine, std::string drone_sn, const std::string& gateway_sn, const std::string& drone_model_key)
    : message_engine_(std::move(message_engine)), drone_sn_(std::move(drone_sn)), gateway_sn_(gateway_sn), drone_model_key_(drone_model_key) {
    const std::string host_gateway_sn =
        TopoRegistry::getInstance().resolveHostGatewaySn(drone_sn_).value_or(gateway_sn_);
    LOG_INFO("[PoiModuleImpl] constructor, drone_sn: %s, host_gateway_sn: %s",
             drone_sn_.c_str(), host_gateway_sn.c_str());
    WeakDummy(weak_ptr);
    poi_task_event_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenPoiTaskEvent(
        [this, weak_ptr](const notification_center::PoiTaskEvent& msg)-> void {
            WeakDummyReturn(weak_ptr);
            PoiTaskEvent event;
            event.event_id = msg.event_id;
            event.action = msg.action;
            event.uuid = msg.uuid;
            event.timestamp = msg.timestamp;
            event.task_data.uuid = msg.task_data.uuid;
            event.task_data.sn = msg.task_data.sn;
            event.task_data.device_sn = msg.task_data.device_sn;
            event.task_data.status = msg.task_data.status;
            event.task_data.err_code = msg.task_data.err_code;
            event.task_data.poi_center_point.latitude = msg.task_data.poi_center_point.latitude;
            event.task_data.poi_center_point.longitude = msg.task_data.poi_center_point.longitude;
            event.task_data.poi_center_point.height = msg.task_data.poi_center_point.height;
            event.task_data.speed = msg.task_data.speed;
            event.task_data.circle_radius = msg.task_data.circle_radius;
            event.task_data.payload_index = msg.task_data.payload_index;
            poi_task_event_callbacks_.notifyCallbackIfChanged(event);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[PoiModuleImpl][%s] listenPoiTaskEvent, error: %d", drone_sn_.c_str(), error_code);
            }
        },
        host_gateway_sn,
        notification_center::NotificationFrequency_OnChanged);

    flight_poi_progress_listen_id_ = NotificationCenter::getInstance()->getBusinessManager()->ListenFlightPoiProgress(
        [this, weak_ptr](const notification_center::FlightPoiProgress& msg)-> void {
            WeakDummyReturn(weak_ptr);
            FlightPoiProgress res{};
            res.uuid = msg.uuid;
            res.sn = msg.sn;
            res.device_sn = msg.device_sn;
            res.poi_center_point.latitude = msg.poi_center_point.latitude;
            res.poi_center_point.longitude = msg.poi_center_point.longitude;
            res.poi_center_point.height = msg.poi_center_point.height;
            res.speed = msg.speed;
            res.circle_radius = msg.circle_radius;
            res.payload_index = msg.payload_index;
            res.status = msg.status;
            res.err_code = msg.err_code;
            flight_poi_progress_callbacks_.notifyCallbackIfChanged(res);
        },
        [this, weak_ptr](const notification_center::NotificationCenterErrorCode& error_code)-> void {
            WeakDummyReturn(weak_ptr);
            if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                LOG_ERROR("[PoiModuleImpl][%s] listenFlightPoiProgress, error: %d", drone_sn_.c_str(), error_code);
            }
        },
        host_gateway_sn,
        notification_center::NotificationFrequency_OnChanged);
}

PoiModuleImpl::~PoiModuleImpl() {
    if (poi_task_event_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(poi_task_event_listen_id_);
        poi_task_event_listen_id_ = -1;
    }
    if (flight_poi_progress_listen_id_ != -1) {
        NotificationCenter::getInstance()->getBusinessManager()->CancelObserve(flight_poi_progress_listen_id_);
        flight_poi_progress_listen_id_ = -1;
    }
}

void PoiModuleImpl::enter(const POIEnterParams& params, const std::function<void(FlightHubError err, const std::string& task_uuid)>& callback) {
    POIEnterRequest request;
    request.device_sn = TopoRegistry::getInstance().resolveHostGatewaySn(drone_sn_).value_or(gateway_sn_);
    LOG_INFO("[PoiModuleImpl] enter drone_sn=%s device_sn=%s", drone_sn_.c_str(), request.device_sn.c_str());
    request.payload_index = params.payload_index;
    request.poi_center_point.latitude = params.poi_center_point.latitude;
    request.poi_center_point.longitude = params.poi_center_point.longitude;
    request.poi_center_point.height = params.poi_center_point.height;
    request.speed = params.speed;
    request.circle_radius = params.circle_radius;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()-> void {
        WeakDummyReturn(weak_ptr);
        poi_enter(request,
                  [this, weak_ptr, callback](const POITaskResponse &response, FlightHubError err) {
                      WeakDummyReturn(weak_ptr);
                      drc_sdk::common::Executors::Main()->Post([err, callback, response]()-> void {
                        // 埋点: 进入 POI 环绕模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                        report_beacon_event_helper(kBeaconReportEvent_POI_ENTER, err, "[PoiModuleImpl]");
                        LOG_INFO("[PoiModuleImpl] callback, uuid: %s", response.uuid.c_str());
                          if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                              callback(err, response.uuid);
                          } else {
                              callback(err, "");
                          }
                      });
                  });
    });
}

void PoiModuleImpl::exit(const std::function<void(FlightHubError err)>& callback) {
    POIExitRequest request;
    request.device_sn = TopoRegistry::getInstance().resolveHostGatewaySn(drone_sn_).value_or(gateway_sn_);
    LOG_INFO("[PoiModuleImpl] exit drone_sn=%s device_sn=%s", drone_sn_.c_str(), request.device_sn.c_str());

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()-> void {
        WeakDummyReturn(weak_ptr);
        poi_exit(request,
                 [this, weak_ptr, callback](FlightHubError err) {
                     WeakDummyReturn(weak_ptr);
                     drc_sdk::common::Executors::Main()->Post([err, callback]()-> void {
                         // 埋点: 退出 POI 环绕模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                         // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                         // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                         // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                         report_beacon_event_helper(kBeaconReportEvent_POI_EXIT, err, "[PoiModuleImpl]");
                         callback(err);
                     });
                 });
    });
}

void PoiModuleImpl::updateSpeed(double speed, const std::function<void(FlightHubError err)>& callback) {
    POISpeedUpdateRequest request;
    request.device_sn = TopoRegistry::getInstance().resolveHostGatewaySn(drone_sn_).value_or(gateway_sn_);
    LOG_INFO("[PoiModuleImpl] updateSpeed drone_sn=%s device_sn=%s", drone_sn_.c_str(), request.device_sn.c_str());
    request.speed = speed;

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()-> void {
        WeakDummyReturn(weak_ptr);
        poi_speed_update(request,
                        [this, weak_ptr, callback](FlightHubError err) {
                            WeakDummyReturn(weak_ptr);
                            drc_sdk::common::Executors::Main()->Post([err, callback]()-> void {
                                // 埋点: 更新 POI 环绕速度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                                report_beacon_event_helper(kBeaconReportEvent_POI_UPDATE_SPEED, err, "[PoiModuleImpl]");
                                callback(err);
                            });
                        });
    });
}

void PoiModuleImpl::getTask(const std::function<void(FlightHubError err, const POITaskInfo& task)>& callback) {
    GetPOITaskRequest request;
    request.device_sn = TopoRegistry::getInstance().resolveHostGatewaySn(drone_sn_).value_or(gateway_sn_);
    LOG_INFO("[PoiModuleImpl] getTask drone_sn=%s device_sn=%s", drone_sn_.c_str(), request.device_sn.c_str());

    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback]()-> void {
        WeakDummyReturn(weak_ptr);
        get_poi_task(request,
                    [this, weak_ptr, callback](const POITaskResponse& response, FlightHubError err) {
                        WeakDummyReturn(weak_ptr);
                        drc_sdk::common::Executors::Main()->Post([err, callback, response]()-> void {
                            // 埋点: 查询当前 POI 任务信息; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                            report_beacon_event_helper(kBeaconReportEvent_POI_GET_TASK, err, "[PoiModuleImpl]");
                            if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                                // 转换内部结构为用户接口结构
                                POITaskInfo user_task = convertToPOITaskInfo(response);
                                callback(err, user_task);
                            } else {
                                callback(err, {});
                            }
                        });
                    });
    });
}

int PoiModuleImpl::listenPoiTaskEvent(const std::function<void(const PoiTaskEvent& msg)>& callback) {
    return poi_task_event_callbacks_.addCallback(callback);
}

void PoiModuleImpl::cancelListenPoiTaskEvent(int listen_id) {
    poi_task_event_callbacks_.removeCallback(listen_id);
}

int PoiModuleImpl::listenFlightPoiProgress(const std::function<void(const FlightPoiProgress& msg)>& callback) {
    return flight_poi_progress_callbacks_.addCallback(callback);
}

void PoiModuleImpl::cancelListenFlightPoiProgress(int listen_id) {
    flight_poi_progress_callbacks_.removeCallback(listen_id);
}

} // namespace flight_hub_sdk
