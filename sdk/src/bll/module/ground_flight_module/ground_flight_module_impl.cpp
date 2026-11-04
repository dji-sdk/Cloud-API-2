// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ground_flight_module_impl.h"

#include <base/common/logger.h>
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include <string>

using namespace flight_hub_sdk;

GroundFlightModuleImpl::GroundFlightModuleImpl(const std::string& drone_sn, const std::string& gateway_sn, int session_id)
    : drone_sn_(drone_sn), gateway_sn_(gateway_sn), session_id_(session_id) {
    LOG_INFO("[GroundFlightModuleImpl] constructor, drone_sn: %s, gateway_sn: %s, session_id: %d",
        drone_sn_.c_str(), gateway_sn_.c_str(), session_id_);
}

void GroundFlightModuleImpl::groundTakeoff(
    const GroundTakeoffParams& params,
    const std::function<void(FlightHubError err, const GroundTakeoffResult& result)>& callback) {
    LOG_INFO("[GroundFlightModuleImpl] groundTakeoff, gateway_sn: %s, auto_takeoff_height: %d",
        gateway_sn_.c_str(), params.auto_takeoff_height);

    GroundTakeoffRequest request;
    request.device_sn = gateway_sn_;
    request.auto_takeoff_height = params.auto_takeoff_height;

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

    int sid = session_id_;
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback, sid]() -> void {
        WeakDummyReturn(weak_ptr);
        ground_takeoff(request, [this, weak_ptr, callback, sid](const GroundTakeoffResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response, sid]() -> void {
                // 埋点: 舱外起飞; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                report_beacon_event_helper(kBeaconReportEvent_GROUND_TAKE_OFF, err, sid, "[GroundFlightModuleImpl]");

                LOG_INFO("[GroundFlightModuleImpl] groundTakeoff response: flight_id=%s", response.flight_id.c_str());
                GroundTakeoffResult result;
                result.flight_id = response.flight_id;
                if (err.error_code == FlightHubSdkErrorCode::SUCCESS) {
                    callback(FlightHubError::success(), result);
                } else {
                    callback(err, result);
                }
            });
        }, sid);
    });
}

void GroundFlightModuleImpl::groundLanding(int cmd, const std::function<void(FlightHubError err)>& callback) {
    LOG_INFO("[GroundFlightModuleImpl] groundLanding, gateway_sn: %s, cmd: %d", gateway_sn_.c_str(), cmd);

    GroundLandingRequest request;
    request.device_sn = gateway_sn_;
    request.cmd = cmd;

    int sid = session_id_;
    WeakDummy(weak_ptr);
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, request, callback, sid]() -> void {
        WeakDummyReturn(weak_ptr);
        ground_landing(request, [this, weak_ptr, callback, sid](FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, sid]() -> void {
                // 埋点: 舱外降落; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                report_beacon_event_helper(kBeaconReportEvent_GROUND_LANDING, err, sid, "[GroundFlightModuleImpl]");
                callback(err);
            });
        }, sid);
    });
}
