// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_task_module_impl.h"
#include <base/common/logger.h>
#include "notification_center/notification_center.h"
#include <BusinessManager.h>
#include "session/session_bm.h"

namespace flight_hub_sdk {

FlightTaskModuleImpl::FlightTaskModuleImpl(int session_id) : session_id_(session_id) {
    LOG_INFO("[FlightTaskModuleImpl] constructor");
}

FlightTaskModuleImpl::~FlightTaskModuleImpl() {
    LOG_INFO("[FlightTaskModuleImpl] destructor");
}

void FlightTaskModuleImpl::getFlightTaskList(const FlightTaskListParams& params, std::function<void(FlightHubError err, const FlightTaskListResult& result)> callback) {
    get_flight_task_list(params, callback, session_id_);
}

void FlightTaskModuleImpl::getCommandTaskList(const GetCommandTaskListParams& params, std::function<void(FlightHubError err, const GetCommandTaskListResults& results)> callback) {
    get_command_task_list(params, callback, session_id_);
}

void FlightTaskModuleImpl::getFlightTaskTrack(const GetFlightTaskTrackParams& params, std::function<void(FlightHubError err, const GetFlightTaskTrackResults& results)> callback) {
    get_flight_task_track(params, callback, session_id_);
}

void FlightTaskModuleImpl::getFlightRisks(const GetFlightRisksParams& params, std::function<void(FlightHubError err, const GetFlightRisksResults& results)> callback) {
    get_flight_risks(params, callback, session_id_);
}

void FlightTaskModuleImpl::createFlightTask(const CreateTaskReq& req, std::function<void(FlightHubError err, const CreateTaskResp& result)> callback) {
    create_flight_task(req, callback, session_id_);
}

void FlightTaskModuleImpl::getFlightTaskDetail(const std::string& task_uuid, std::function<void(FlightHubError err, const TaskDetail& result)> callback) {
    get_flight_task_detail(task_uuid, callback, session_id_);
}

void FlightTaskModuleImpl::editFlightTask(const std::string& task_uuid, const EditTaskReq& req, std::function<void(FlightHubError err, const CreateTaskResp& result)> callback) {
    edit_flight_task(task_uuid, req, callback, session_id_);
}

void FlightTaskModuleImpl::deleteFlightTask(const std::string& task_uuid, std::function<void(FlightHubError err)> callback) {
    delete_flight_task(task_uuid, callback, session_id_);
}

void FlightTaskModuleImpl::updateFlightTaskStatus(const std::string& task_uuid, int status, std::function<void(FlightHubError err)> callback) {
    update_flight_task_status(task_uuid, status, callback, session_id_);
}

void FlightTaskModuleImpl::batchUpdateFlightTaskStatus(const BatchUpdateStatusReq& req, std::function<void(FlightHubError err)> callback) {
    batch_update_flight_task_status(req, callback, session_id_);
}

void FlightTaskModuleImpl::getDeviceRecommendation(const DeviceRecommendationReq& req, std::function<void(FlightHubError err, const DeviceRecommendationResp& result)> callback) {
    get_device_recommendation(req, callback, session_id_);
}

void FlightTaskModuleImpl::getDeviceTcas(const std::vector<std::string>& center_sn, std::function<void(FlightHubError err, const GetTCAInfosResp& result)> callback) {
    get_device_tcas(center_sn, callback, session_id_);
}

void FlightTaskModuleImpl::updateDeviceTcas(const std::vector<std::string>& center_sn, std::function<void(FlightHubError err)> callback) {
    update_device_tcas(center_sn, callback, session_id_);
}

void FlightTaskModuleImpl::startRoaming(const StartRoamingParams& params, std::function<void(FlightHubError err)> callback) {
    start_roaming(params, callback, session_id_);
}

void FlightTaskModuleImpl::setRoamingStationSn(
    const SetRoamingStationSnParams& params,
    std::function<void(FlightHubError err, const SetRoamingStationSnResult& result)> callback) {
    set_roaming_station_sn(params, callback, session_id_);
}

void FlightTaskModuleImpl::getInFlightTaskTopos(
    const std::vector<std::string>& drone_sns,
    std::function<void(FlightHubError err, const GetInFlightTaskToposResult& result)> callback) {
    get_in_flight_task_topos(drone_sns, callback, session_id_);
}

int FlightTaskModuleImpl::listenTaskTopoChanged(const std::function<void(const TaskTopoChangedEvent&)>& callback) {
    auto bm = ResolveBusinessManager(session_id_);
    WeakDummy(weak_ptr);
    // 工作区级广播：device_sn 为空（同 TerraBuild reconstruction status）
    if (bm) {
        return static_cast<int>(bm->ListenTaskTopoChanged(
            [weak_ptr, callback, session_id_ = session_id_](const notification_center::TaskTopoChanged& msg) {
                WeakDummyReturn(weak_ptr);
                if (!callback) {
                    return;
                }
                TaskTopoChangedEvent event;
                event.drone_sn = msg.drone_sn;
                event.flight_id = msg.flight_id;
                event.takeoff_sn = msg.takeoff_sn;
                LOG_INFO("[FlightTaskModuleImpl] TaskTopoChanged drone_sn=%s flight_id=%s takeoff_sn=%s",
                         event.drone_sn.c_str(), event.flight_id.c_str(), event.takeoff_sn.c_str());
                callback(event);
            },
            [weak_ptr](const notification_center::NotificationCenterErrorCode& error_code) {
                WeakDummyReturn(weak_ptr);
                if (error_code != notification_center::NotificationCenterErrorCode::NotificationCenterErrorCode_NoError) {
                    LOG_ERROR("[FlightTaskModuleImpl] listenTaskTopoChanged subscribe error: %d",
                              static_cast<int>(error_code));
                }
            },
            "",
            notification_center::NotificationFrequency_OnChanged));
    }
    LOG_ERROR("[FlightTaskModuleImpl] listenTaskTopoChanged: session bm null");
    return -1;
}

void FlightTaskModuleImpl::cancelListenTaskTopoChanged(int listen_id) {
    auto bm = ResolveBusinessManager(session_id_);
    if (listen_id >= 0) {
        if (bm) {
            bm->CancelObserve(listen_id);
        }
    }
}

}  // namespace flight_hub_sdk
