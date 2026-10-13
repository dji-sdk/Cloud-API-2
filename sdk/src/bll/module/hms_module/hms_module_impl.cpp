// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "hms_module_impl.h"

#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include <base/common/logger.h>

namespace flight_hub_sdk {
HMSModuleImpl::HMSModuleImpl() {
    LOG_INFO("[HMSModuleImpl] constructor");
}

HMSModuleImpl::~HMSModuleImpl() {}

void HMSModuleImpl::QueryHistoryHMSData(const HistoryHmsDataParam& params, std::function<void(FlightHubError err, const HistoryHmsDataResult&)> callback) {
    WeakDummy(weak_ptr);
    HistoryHmsDataRequest request;
    request.sn = params.sn_list;
    request.create_time_begin = params.create_time_begin;
    request.create_time_end = params.create_time_end;
    request.page_size = params.page_size;
    request.page_num = params.page_num;
    post_history_hms(request, [weak_ptr, callback](const HistoryHmsDataResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_INFO("QueryHistoryHMSData: err_code: %d", (int)err.error_code);
                }
                HistoryHmsDataResult result;
                result.pagination.page_num = response.pagination.page_num;
                result.pagination.page_size = response.pagination.page_size;
                result.pagination.total = response.pagination.total;

                result.list.clear();
                for (const auto& item : response.list) {
                    HistoryHmsDataResultItem resultItem;
                    resultItem._id = item._id;
                    resultItem.bid = item.bid;
                    resultItem.tid = item.tid;
                    resultItem.level = item.level;
                    resultItem.module = item.module;
                    resultItem.message = item.message;
                    resultItem.status = item.status;
                    resultItem.code = item.code;
                    resultItem.device_data_create_time = item.device_data_create_time;
                    resultItem.device_data_update_time = item.device_data_update_time;
                    resultItem.create_time = item.create_time;
                    resultItem.update_time = item.update_time;

                    // args
                    resultItem.args.alarm_info = item.args.alarm_info;
                    resultItem.args.component_index = item.args.component_index;
                    resultItem.args.create_timestamp = item.args.create_timestamp;
                    resultItem.args.device_id = item.args.device_id;
                    resultItem.args.display_scene = item.args.display_scene;
                    resultItem.args.ignore = item.args.ignore;
                    resultItem.args.sender_host_id = item.args.sender_host_id;
                    resultItem.args.sensor_index = item.args.sensor_index;

                    resultItem.device_type = item.device_type;
                    resultItem.domain_type = item.domain_type;
                    resultItem.imminent = item.imminent;
                    resultItem.in_the_sky = item.in_the_sky;
                    resultItem.joint_key = item.joint_key;
                    resultItem.status_key = item.status_key;
                    resultItem.sn = item.sn;

                    result.list.push_back(resultItem);
                }

                // 埋点: 查询历史 HMS 告警数据; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_QUERY_HISTORY_HMS, err, "[HMSModuleImpl]");

                if (callback) {
                    callback(err, result);
                }
            });
    });
}

} // namespace flight_hub_sdk