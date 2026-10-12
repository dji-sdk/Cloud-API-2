// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "test_flight_module_impl.h"

#include <base/common/logger.h>
#include "bll/http_interface/http_interface.h"
#include "common/executors/executors.h"

using namespace flight_hub_sdk;

TestFlightModuleImpl::TestFlightModuleImpl()
    {
    LOG_INFO("[TestFlightModuleImpl] constructor");
}

void TestFlightModuleImpl::generateTryFlightWayline(
    const std::string& project_id,
    const GenerateTryFlightWaylineParams& params,
    const std::function<void(const GenerateTryFlightWaylineResponse& response, FlightHubError err)>& callback) {
    LOG_INFO("[TestFlightModuleImpl] generateTryFlightWayline, project_id: %s", project_id.c_str());

    GenerateTryFlightWaylineRequest request;
    request.name = params.name;
    request.device_type = params.device_type;
    request.target_point.lng = params.target_point.lng;
    request.target_point.lat = params.target_point.lat;
    request.target_point.alt = params.target_point.alt;
    request.observer_point.lng = params.observer_point.lng;
    request.observer_point.lat = params.observer_point.lat;
    request.observer_point.alt = params.observer_point.alt;

    WeakDummy(weak_ptr);
    std::string pid = project_id;
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, pid, request, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        generate_try_flight_wayline(pid, request, [weak_ptr, callback](const GenerateTryFlightWaylineResponseData& data_response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            GenerateTryFlightWaylineResponse api_response;
            api_response.key = data_response.key;
            api_response.name = data_response.name;
            api_response.uuid = data_response.uuid;
            api_response.request_id = data_response.request_id;
            drc_sdk::common::Executors::Main()->Post([callback, api_response, err]() -> void {
                // 埋点: 生成试飞航线; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_TEST_FLIGHT_GENERATE_TRY_FLIGHT_WAYLINE, err, "[TestFlightModuleImpl]");
                callback(api_response, err);
            });
        });
    });
}

void TestFlightModuleImpl::createTestFlightTask(
    const std::string& project_id,
    const CreateTestFlightTaskParams& params,
    const std::function<void(const CreateTestFlightTaskResponse& response, FlightHubError err)>& callback) {
    LOG_INFO("[TestFlightModuleImpl] createTestFlightTask, project_id: %s", project_id.c_str());

    CreateTestFlightTaskRequest request;
    request.name = params.name;
    request.wayline_uuid = params.wayline_uuid;
    request.sn = params.sn;
    request.landing_dock_sn = params.landing_dock_sn;
    request.task_type = params.task_type;
    request.out_of_control_action_in_flight = params.out_of_control_action_in_flight;
    request.rth_mode = params.rth_mode;
    request.repeat_type = params.repeat_type;
    request.time_zone = params.time_zone;
    request.default_name = params.default_name;
    request.wayline_precision_type = params.wayline_precision_type;
    request.wayline_duration = params.wayline_duration;
    request.rth_altitude = params.rth_altitude;
    request.resumable_status = params.resumable_status;
    request.media_folder_id_paths = params.media_folder_id_paths;
    request.device_type = params.device_type;

    WeakDummy(weak_ptr);
    std::string pid = project_id;
    drc_sdk::common::Executors::IO()->Post([this, weak_ptr, pid, request, callback]() -> void {
        WeakDummyReturn(weak_ptr);
        create_test_flight_task(pid, request, [weak_ptr, callback](const CreateTestFlightTaskResponseData& data_response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            CreateTestFlightTaskResponse api_response;
            api_response.uuid = data_response.task.uuid;
            api_response.begin_at = data_response.task.begin_at;
            api_response.end_at = data_response.task.end_at;
            drc_sdk::common::Executors::Main()->Post([callback, api_response, err]() -> void {
                // 埋点: 创建试飞任务; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_TEST_FLIGHT_CREATE_TASK, err, "[TestFlightModuleImpl]");
                callback(api_response, err);
            });
        });
    });
}
