// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_settings_module_impl.h"

#include <base/common/logger.h>
#include "bll/http_interface/http_interface.h"
#include "common/executors/executors.h"

using namespace flight_hub_sdk;

FlightSettingsModuleImpl::FlightSettingsModuleImpl(const std::string& gateway_sn)
    : gateway_sn_(gateway_sn) {
    LOG_INFO("[FlightSettingsModuleImpl] constructor, gateway_sn: %s", gateway_sn_.c_str());
}

void FlightSettingsModuleImpl::setFlightSmartPlanMode(
    const std::string& device_sn,
    int mode,
    std::function<void(FlightHubError err, const FlightSmartPlanModeResult& result)> callback) {
    FlightSmartPlanModeRequest request;
    // 忽略外部传入的 device_sn，使用构造时保存的 gateway_sn_
    // （后端 /manage/api/v1/flight/configs/safe 接口要求 URL 的 ?sn=xxx 和 Body 内的 sn 都是网关 SN）
    request.sn = gateway_sn_;
    request.settings.sn = gateway_sn_;   // Body 内也需要带 sn（后端要求）
    request.settings.smart_planning_mode = mode;
    WeakDummy(weak_ptr);
    set_flight_smart_plan_mode(request,
        [callback, weak_ptr](const FlightSmartPlanModeApiResponse& response, FlightHubError err) {
            drc_sdk::common::Executors::Main()->Post([err, callback, response, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 设置智能规划模式; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_SETTINGS_SET_FLIGHT_SMART_PLAN_MODE, err, "[FlightSettingsModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("[FlightSettingsModuleImpl] setFlightSmartPlanMode: err_code: %d", (int)err.error_code);
                    if (callback) {
                        callback(err, FlightSmartPlanModeResult{});
                    }
                    return;
                }

                FlightSmartPlanModeResult result;
                result.settings.smart_planning_mode = response.data.settings.smart_planning_mode;
                result.device_shadow_sync.status = response.data.device_shadow_sync.status;
                for (const auto& f : response.data.device_shadow_sync.results) {
                    FlightSafeSyncField field;
                    field.field = f.field;
                    field.synced = f.synced;
                    result.device_shadow_sync.results.push_back(field);
                }

                if (callback) {
                    callback(err, result);
                }
            });
        });
}

void FlightSettingsModuleImpl::setOutOfControlAction(
    const std::string& device_sn,
    const std::string& action,
    std::function<void(FlightHubError err, const FlightOutOfControlActionResult& result)> callback) {
    FlightOutOfControlActionRequest request;
    request.sn = gateway_sn_;   // 使用网关 SN（详见 setFlightSmartPlanMode 注释）
    request.settings.sn = gateway_sn_;   // Body 内也需要带 sn
    request.settings.out_of_control_action = action;
    WeakDummy(weak_ptr);
    set_flight_out_of_control_action(request,
        [callback, weak_ptr](const FlightOutOfControlActionApiResponse& response, FlightHubError err) {
            drc_sdk::common::Executors::Main()->Post([err, callback, response, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 设置失控动作; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_SETTINGS_SET_OUT_OF_CONTROL_ACTION, err, "[FlightSettingsModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("[FlightSettingsModuleImpl] setOutOfControlAction: err_code: %d", (int)err.error_code);
                    if (callback) callback(err, FlightOutOfControlActionResult{});
                    return;
                }
                FlightOutOfControlActionResult result;
                result.settings.out_of_control_action = response.data.settings.out_of_control_action;
                result.device_shadow_sync.status = response.data.device_shadow_sync.status;
                for (const auto& f : response.data.device_shadow_sync.results) {
                    FlightSafeSyncField field;
                    field.field = f.field;
                    field.synced = f.synced;
                    result.device_shadow_sync.results.push_back(field);
                }
                if (callback) callback(err, result);
            });
        });
}

void FlightSettingsModuleImpl::setFlightTaskHeight(
    const std::string& device_sn,
    int height,
    std::function<void(FlightHubError err, const FlightCommanderHeightResult& result)> callback) {
    FlightCommanderHeightRequest request;
    request.sn = gateway_sn_;   // 使用网关 SN（详见 setFlightSmartPlanMode 注释）
    request.settings.sn = gateway_sn_;   // Body 内也需要带 sn
    request.settings.commander_flight_height = height;
    WeakDummy(weak_ptr);
    set_flight_task_height(request,
        [callback, weak_ptr](const FlightTaskHeightApiResponse& response, FlightHubError err) {
            drc_sdk::common::Executors::Main()->Post([err, callback, response, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 设置飞行任务高度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_SETTINGS_SET_FLIGHT_TASK_HEIGHT, err, "[FlightSettingsModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("[FlightSettingsModuleImpl] setFlightTaskHeight: err_code: %d", (int)err.error_code);
                    if (callback) callback(err, FlightCommanderHeightResult{});
                    return;
                }
                FlightCommanderHeightResult result;
                result.settings.commander_flight_height = response.data.settings.commander_flight_height;
                result.device_shadow_sync.status = response.data.device_shadow_sync.status;
                for (const auto& f : response.data.device_shadow_sync.results) {
                    FlightSafeSyncField field;
                    field.field = f.field;
                    field.synced = f.synced;
                    result.device_shadow_sync.results.push_back(field);
                }
                if (callback) callback(err, result);
            });
        });
}

void FlightSettingsModuleImpl::setRthAltitude(
    const std::string& device_sn,
    int altitude,
    std::function<void(FlightHubError err, const FlightRthAltitudeResult& result)> callback) {
    FlightRthAltitudeRequest request;
    request.sn = gateway_sn_;   // 使用网关 SN（详见 setFlightSmartPlanMode 注释）
    request.settings.sn = gateway_sn_;   // Body 内也需要带 sn
    request.settings.rth_altitude = altitude;
    WeakDummy(weak_ptr);
    set_rth_altitude(request,
        [callback, weak_ptr](const FlightRthAltitudeApiResponse& response, FlightHubError err) {
            drc_sdk::common::Executors::Main()->Post([err, callback, response, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 设置返航高度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_SETTINGS_SET_RTH_ALTITUDE, err, "[FlightSettingsModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("[FlightSettingsModuleImpl] setRthAltitude: err_code: %d", (int)err.error_code);
                    if (callback) callback(err, FlightRthAltitudeResult{});
                    return;
                }
                FlightRthAltitudeResult result;
                result.settings.rth_altitude = response.data.settings.rth_altitude;
                result.device_shadow_sync.status = response.data.device_shadow_sync.status;
                for (const auto& f : response.data.device_shadow_sync.results) {
                    FlightSafeSyncField field;
                    field.field = f.field;
                    field.synced = f.synced;
                    result.device_shadow_sync.results.push_back(field);
                }
                if (callback) callback(err, result);
            });
        });
}

void FlightSettingsModuleImpl::setAirTransferEnable(
    const std::string& device_sn,
    bool enabled,
    std::function<void(FlightHubError err)> callback) {
    AirTransferEnableRequest request;
    request.device_sn = device_sn;
    request.air_transfer_enable = enabled;
    WeakDummy(weak_ptr);
    set_air_transfer_enable(request,
        [callback, weak_ptr](FlightHubError err) {
            drc_sdk::common::Executors::Main()->Post([err, callback, weak_ptr]() {
                WeakDummyReturn(weak_ptr);
                // 埋点: 设置空中回传开关; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_SETTINGS_SET_AIR_TRANSFER_ENABLE, err, "[FlightSettingsModuleImpl]");
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("[FlightSettingsModuleImpl] setAirTransferEnable: err_code: %d", (int)err.error_code);
                }
                if (callback) callback(err);
            });
        });
}
