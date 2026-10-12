// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "flight_task_record_module_impl.h"
#include "common/executors/executors.h"
#include "http_interface/http_interface.h"
#include "base/common/logger.h"


namespace flight_hub_sdk {

FlightTaskRecordModuleImpl::FlightTaskRecordModuleImpl()
    {
    LOG_INFO("[FlightTaskRecordModuleImpl] constructor");
}

// 析构函数;
FlightTaskRecordModuleImpl::~FlightTaskRecordModuleImpl() {
    // empty body, add impl if needed;
}


// [ATC 空管服务]
// 导出飞行记录;
void FlightTaskRecordModuleImpl::exportFlightRecords(
    const std::string &user_id,                       // 用户 id, 比如 "15478287234981xxxxx";
    const ExportFlightRecordsRequestParams &params,   // 导出飞行记录的请求参数;
    const std::function<void(FlightHubError err, const ExportFlightRecordsResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("exportFlightRecords(): callback is NULL !");
        return;
    }

    // [必填参数] 检查;
    if (user_id.empty()) {
        LOG_ERROR("exportFlightRecords(): user_id is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 导出飞行记录(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_RECORD_EXPORT_FLIGHT_RECORDS, param_err, "[FlightTaskRecordModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // [可选参数] 比如 "f111d837-7740-470b-9a44-e52fba516085", 默认传空字符串;
    std::string project_id = "";  // todo: 可选参数待确定值;

    // 请求 body 参数;
    ExportFlightRecordsRequest request;
    request.content_type = params.content_type;
    request.file_type = params.file_type;
    request.file_name = params.file_name;
    request.flight_task_ids = params.flight_task_ids;
    request.metrics = params.metrics;
    request.time_zone = params.time_zone;
    request.locale = params.locale;

    // Send request;
    export_flight_records(
        user_id,
        project_id,
        request,
        [this, weak_ptr, callback](const ExportFlightRecordsResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 导出飞行记录; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_RECORD_EXPORT_FLIGHT_RECORDS, err, "[FlightTaskRecordModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("exportFlightRecords(): err_code: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // 根据 resp 组装返回值;
                ExportFlightRecordsResults results;
                results.export_id = response.export_id;

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}



// [ATC 空管服务]
// 查询导出进度;
void FlightTaskRecordModuleImpl::getExportFlightRecordsProgress(
    const GetExportFlightRecordsProgressParams &params,   // 查询导出进度的请求参数;
    const std::function<void(FlightHubError err, const GetExportFlightRecordsProgressResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getExportFlightRecordsProgress(): callback is NULL !");
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // [可选参数] 比如 "f111d837-7740-470b-9a44-e52fba516085", 默认传空字符串;
    std::string project_id = "";  // todo: 可选参数待确定值;

    // Send request;
    get_export_flight_records_progress(
        project_id,
        params,
        [this, weak_ptr, callback](const GetExportFlightRecordsProgressResponse& response, FlightHubError err) {
            WeakDummyReturn(weak_ptr);
            drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
                // 埋点: 查询导出进度; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
                // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
                // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
                // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
                report_beacon_event_helper(kBeaconReportEvent_FLIGHT_TASK_RECORD_GET_EXPORT_FLIGHT_RECORDS_PROGRESS, err, "[FlightTaskRecordModuleImpl]");
                // 检查 err;
                if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                    LOG_ERROR("getExportFlightRecordsProgress(): err_code: %d", (int)err.error_code);
                    callback(err, {});

                    return;
                }

                // When succeed, transfer the response to the callback;
                GetExportFlightRecordsProgressResults results;

                // 根据 resp 组装返回值;
                // #1 填充分页信息（服务端扁平字段 page/page_size/total，已直接映射到 response）;
                results.pagination.page = response.page;
                results.pagination.page_size = response.page_size;
                results.pagination.total = response.total;

                // #2 填充 Flight Record Export Progress 信息;
                results.list.reserve(response.list.size());
                for (const auto& item : response.list) {
                    FlightRecordExportProgressItemData progress_item;

                    progress_item.id = static_cast<long long>(item.id);
                    progress_item.export_id = item.export_id;
                    progress_item.created_at = item.created_at;
                    progress_item.content_type = item.content_type;
                    progress_item.status = item.status;
                    progress_item.progress = item.progress;
                    progress_item.failed_reason_code = item.failed_reason_code;
                    progress_item.object_key = item.object_key;
                    progress_item.file_name = item.file_name;
                    progress_item.user_id = item.user_id;
                    progress_item.prj_uuid = item.prj_uuid;
                    progress_item.file_type = item.file_type;
                    progress_item.download_url = item.download_url;

                    // save to results;
                    results.list.emplace_back(std::move(progress_item));
                }

                // When succeed, transfer the response to the callback;
                callback(err, results);
            });
        }
    );
}


}  // End of namespace flight_hub_sdk;