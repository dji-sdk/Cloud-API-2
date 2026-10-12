// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "ai_alert_module_impl.h"
#include <base/common/logger.h>
#include "http_interface/http_interface.h"
#include "common/executors/executors.h"

using namespace flight_hub_sdk;

namespace flight_hub_sdk {

AIAlertModuleImpl::AIAlertModuleImpl() {
    LOG_INFO("AIAlertModuleImpl: created");
}

void AIAlertModuleImpl::postDeviceSetting(
    const PostAiAlertDeviceSettingParam& param,
    std::function<void(FlightHubError err, PostAiAlertDeviceSettingResult result)> callback) {
    WeakDummy(weak_ptr);
    PostAiAlertDeviceSettingRequest request;
    request.drone_sn             = param.drone_sn;
    request.enable_target_alert  = param.enable_target_alert;
    request.alarm_info.alarm_method   = param.alarm_info.alarm_method;
    request.alarm_info.alarm_interval = param.alarm_info.alarm_interval;
    post_ai_alert_device_setting(request,
        [this, weak_ptr, callback](const PostAiAlertDeviceSettingResponse&, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 写设备级告警配置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_ALERT_POST_DEVICE_SETTING, err, "[AIAlertModuleImpl]");
                if (callback) { callback(err, {}); }
            });
        });
}

void AIAlertModuleImpl::getDeviceSetting(
    const GetAiAlertDeviceSettingParam& param,
    std::function<void(FlightHubError err, GetAiAlertDeviceSettingResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_alert_device_setting(param,
        [this, weak_ptr, callback](const GetAiAlertDeviceSettingResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 读设备级告警配置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_ALERT_GET_DEVICE_SETTING, err, "[AIAlertModuleImpl]");
                GetAiAlertDeviceSettingResult result;
                result.drone_sn             = rsp.drone_sn;
                result.enable_target_alert  = rsp.enable_target_alert;
                result.alarm_info.alarm_method   = rsp.alarm_info.alarm_method;
                result.alarm_info.alarm_interval = rsp.alarm_info.alarm_interval;
                if (callback) { callback(err, result); }
            });
        });
}

void AIAlertModuleImpl::postAlgorithmSetting(
    const PostAiAlertAlgorithmSettingParam& param,
    std::function<void(FlightHubError err, PostAiAlertAlgorithmSettingResult result)> callback) {
    WeakDummy(weak_ptr);
    PostAiAlertAlgorithmSettingRequest request;
    request.drone_sn       = param.drone_sn;
    request.algorithm_type = param.algorithm_type;
    request.algorithm_name = param.algorithm_name;
    for (const auto& t : param.alarm_targets) {
        AiAlertAlarmTargetResponse tr;
        tr.target_type          = t.target_type;
        tr.target_value         = t.target_value;
        tr.use_min_threshold    = t.use_min_threshold;
        tr.use_max_threshold    = t.use_max_threshold;
        tr.target_max_threshold = t.target_max_threshold;
        tr.target_min_threshold = t.target_min_threshold;
        request.alarm_targets.push_back(tr);
    }
    post_ai_alert_algorithm_setting(request,
        [this, weak_ptr, callback](const PostAiAlertAlgorithmSettingResponse&, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback]() {
                // 埋点: 写算法级告警配置（唯一键 drone_sn + algorithm_type，Upsert）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_ALERT_POST_ALGORITHM_SETTING, err, "[AIAlertModuleImpl]");
                if (callback) { callback(err, {}); }
            });
        });
}

void AIAlertModuleImpl::getAlgorithmSetting(
    const GetAiAlertAlgorithmSettingParam& param,
    std::function<void(FlightHubError err, GetAiAlertAlgorithmSettingResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_alert_algorithm_setting(param,
        [this, weak_ptr, callback](const GetAiAlertAlgorithmSettingResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 读算法级告警配置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_ALERT_GET_ALGORITHM_SETTING, err, "[AIAlertModuleImpl]");
                GetAiAlertAlgorithmSettingResult result;
                result.drone_sn       = rsp.drone_sn;
                result.algorithm_type = rsp.algorithm_type;
                result.algorithm_name = rsp.algorithm_name;
                for (const auto& t : rsp.alarm_targets) {
                    AiAlertAlarmTarget out;
                    out.target_type          = t.target_type;
                    out.target_value         = t.target_value;
                    out.use_min_threshold    = t.use_min_threshold;
                    out.use_max_threshold    = t.use_max_threshold;
                    out.target_max_threshold = t.target_max_threshold;
                    out.target_min_threshold = t.target_min_threshold;
                    result.alarm_targets.push_back(out);
                }
                if (callback) { callback(err, result); }
            });
        });
}

void AIAlertModuleImpl::getAiAlertFlights(
    const GetAiAlertFlightsParam& param,
    std::function<void(FlightHubError err, GetAiAlertFlightsResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_alert_flights(param,
        [this, weak_ptr, callback](const GetAiAlertFlightsResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 查询架次告警统计（先拿 flight_id，再调 getAiAlertRecords）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_ALERT_GET_FLIGHTS, err, "[AIAlertModuleImpl]");
                GetAiAlertFlightsResult result;
                result.total      = rsp.total;
                result.page       = rsp.page;
                result.page_size  = rsp.page_size;
                result.page_count = rsp.page_count;
                for (const auto& item : rsp.data) {
                    AiAlertFlightItem out;
                    out.flight_id       = item.flight_id;
                    out.count           = item.count;
                    out.flight_task_name = item.flight_task_name;
                    out.status          = item.status;
                    out.start_time      = item.start_time;
                    result.data.push_back(out);
                }
                if (callback) { callback(err, result); }
            });
        });
}

void AIAlertModuleImpl::getAiAlertRecords(
    const GetAiAlertRecordsParam& param,
    std::function<void(FlightHubError err, GetAiAlertRecordsResult result)> callback) {
    WeakDummy(weak_ptr);
    get_ai_alert_records(param,
        [this, weak_ptr, callback](const GetAiAlertRecordsResponse& rsp, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, rsp]() {
                // 埋点: 查询告警明细列表（按 flight_id 分组，含 thumbnail_url）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_AI_ALERT_GET_RECORDS, err, "[AIAlertModuleImpl]");
                GetAiAlertRecordsResult result;
                result.total      = rsp.total;
                result.page       = rsp.page;
                result.page_size  = rsp.page_size;
                result.page_count = rsp.page_count;
                for (const auto& kv : rsp.data) {
                    std::vector<AiAlertRecordItem> items;
                    for (const auto& item : kv.second) {
                        AiAlertRecordItem out;
                        out.alert_uuid       = item.alert_uuid;
                        out.flight_id        = item.flight_id;
                        out.drone_sn         = item.drone_sn;
                        out.gateway_sn       = item.gateway_sn;
                        out.status           = item.status;
                        out.algorithm_source = item.algorithm_source;
                        out.location.latitude  = item.location.latitude;
                        out.location.longitude = item.location.longitude;
                        out.location.altitude  = item.location.altitude;
                        for (const auto& ti : item.target_alert_infos) {
                            AiAlertTargetAlertInfo tinfo;
                            tinfo.target_type  = ti.target_type;
                            tinfo.target_value = ti.target_value;
                            tinfo.label        = ti.label;
                            out.target_alert_infos.push_back(tinfo);
                        }
                        out.thumbnail_url = item.thumbnail_url;
                        out.labels        = item.labels;
                        out.timestamp     = item.timestamp;
                        out.task_name     = item.task_name;
                        out.media_index   = item.media_index;
                        items.push_back(out);
                    }
                    result.data[kv.first] = items;
                }
                if (callback) { callback(err, result); }
            });
        });
}

}  // namespace flight_hub_sdk
