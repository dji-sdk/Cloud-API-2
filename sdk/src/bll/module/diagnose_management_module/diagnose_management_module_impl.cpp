// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "diagnose_management_module_impl.h"
#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include <base/common/logger.h>



namespace flight_hub_sdk {


DiagnosisManagementModuleImpl::DiagnosisManagementModuleImpl()
    {
    LOG_INFO("[DiagnosisManagementModuleImpl] constructor");
}

DiagnosisManagementModuleImpl::~DiagnosisManagementModuleImpl() {}

// 设备定损: 下发定损指令;
void DiagnosisManagementModuleImpl::createSelfDiagnosis(
    const CreateSelfDiagnosisParams &params,
    const std::function<void(FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("createSelfDiagnosis(): callback is NULL !");
        return;
    }

    if (params.device_sn.empty()) {
        LOG_ERROR("createSelfDiagnosis(): device_sn param is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 下发设备自检定损指令(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_CREATE_SELF_DIAGNOSIS, param_err, "[DiagnosisManagementModuleImpl]");
        callback(param_err);
        return;
    }

    WeakDummy(weak_ptr);

    CreateSelfDiagnosisRequest request;
    request.device_sn = params.device_sn;
    request.type = params.type;
    request.part = params.part;
    request.index = params.index;
    request.motor_action = params.motor_action;

    // 设备拓扑;
    request.device_topo.index = params.device_topo.index;

    auto copy_topo_nodes = [](const std::vector<SelfDiagnosisTopoNodeParams>& src, std::vector<SelfDiagnosisTopoNode>& dst) {
        dst.reserve(src.size());
        for (const auto &node : src) {
            SelfDiagnosisTopoNode item;
            item.sn = node.sn;
            item.type = node.type;
            item.domain = node.domain;
            item.device_model = node.device_model;
            dst.emplace_back(std::move(item));
        }
    };

    copy_topo_nodes(params.device_topo.hosts, request.device_topo.hosts);
    copy_topo_nodes(params.device_topo.parents, request.device_topo.parents);
    copy_topo_nodes(params.device_topo.nodes, request.device_topo.nodes);
    copy_topo_nodes(params.device_topo.others, request.device_topo.others);
    copy_topo_nodes(params.device_topo.docks, request.device_topo.docks);

    // Send HTTP request;
    post_self_diagnosis(request, [weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 下发设备自检定损指令; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_CREATE_SELF_DIAGNOSIS, err, "[DiagnosisManagementModuleImpl]");
            callback(err);
        });
    });
}

// 定损操作(取消/删除);
void DiagnosisManagementModuleImpl::operateSelfDiagnosis(
    const OperateSelfDiagnosisParams &params,
    const std::function<void(FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("operateSelfDiagnosis(): callback is NULL !");
        return;
    }

    if (params.report_uuid.empty()) {
        LOG_ERROR("operateSelfDiagnosis(): report_uuid param is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 定损操作（取消/删除）(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_OPERATE_SELF_DIAGNOSIS, param_err, "[DiagnosisManagementModuleImpl]");
        callback(param_err);
        return;
    }

    WeakDummy(weak_ptr);

    SelfDiagnosisOperationRequest request;
    request.report_uuid = params.report_uuid;
    request.report_cmd_method = params.report_cmd_method;

    post_self_diagnosis_operation(request, [weak_ptr, callback](FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback]() {
            // 埋点: 定损操作（取消/删除）; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_OPERATE_SELF_DIAGNOSIS, err, "[DiagnosisManagementModuleImpl]");
            callback(err);
        });
    });
}


// Response -> Results 转换辅助函数;
static DiagnosisErrorCodeItemResult convertErrorCodeItem(const DiagnosisErrorCodeItem& src) {
    DiagnosisErrorCodeItemResult dst;
    dst.error_code = src.error_code;
    dst.error_code_describe = src.error_code_describe;
    dst.error_code_describe_en = src.error_code_describe_en;
    dst.level = src.level;
    return dst;
}

static DiagnosisErrorCodeListResult convertErrorCodeList(const DiagnosisErrorCodeList& src) {
    DiagnosisErrorCodeListResult dst;
    dst.notice.reserve(src.notice.size());
    for (const auto &item : src.notice) { dst.notice.emplace_back(convertErrorCodeItem(item)); }
    dst.warning.reserve(src.warning.size());
    for (const auto &item : src.warning) { dst.warning.emplace_back(convertErrorCodeItem(item)); }
    dst.alert.reserve(src.alert.size());
    for (const auto &item : src.alert) { dst.alert.emplace_back(convertErrorCodeItem(item)); }
    return dst;
}

static DiagnosisMaterialItemResult convertMaterialItem(const DiagnosisMaterialItem& src) {
    DiagnosisMaterialItemResult dst;
    dst.error_code_list = convertErrorCodeList(src.error_code_list);
    dst.material_num = src.material_num;
    dst.material_name = src.material_name;
    dst.material_probability = src.material_probability;
    dst.hms_total = src.hms_total;
    return dst;
}

static std::vector<DiagnosisMaterialItemResult> convertMaterialList(const std::vector<DiagnosisMaterialItem>& src) {
    std::vector<DiagnosisMaterialItemResult> dst;
    dst.reserve(src.size());
    for (const auto &item : src) { dst.emplace_back(convertMaterialItem(item)); }
    return dst;
}

static GetSelfDiagnosisReportResults convertReportResponse(const GetSelfDiagnosisReportResponse& resp) {
    GetSelfDiagnosisReportResults results;
    results.report_uuid = resp.report_uuid;
    results.report_user_id = resp.report_user_id;
    results.report_user_organization_callsign = resp.report_user_organization_callsign;
    results.create_time = resp.create_time;
    results.complete_machine_sn = resp.complete_machine_sn;
    results.device_sn = resp.device_sn;
    results.device_model_key = resp.device_model_key;
    results.device_organization_callsign = resp.device_organization_callsign;
    results.status = resp.status;
    results.result_code = resp.result_code;
    results.bid = resp.bid;
    results.part = resp.part;
    results.origin = resp.origin;
    results.qr_code_content = resp.qr_code_content;
    results.qr_guide_text = resp.qr_guide_text;

    // self_diagnostic_result;
    results.self_diagnostic_result.high = convertMaterialList(resp.self_diagnostic_result.high);
    results.self_diagnostic_result.mid = convertMaterialList(resp.self_diagnostic_result.mid);
    results.self_diagnostic_result.low = convertMaterialList(resp.self_diagnostic_result.low);

    // device_report_progress;
    results.device_report_progress.result = resp.device_report_progress.result;
    results.device_report_progress.sn = resp.device_report_progress.sn;
    results.device_report_progress.status = resp.device_report_progress.status;
    results.device_report_progress.percent = resp.device_report_progress.percent;
    results.device_report_progress.all_device.reserve(resp.device_report_progress.all_device.size());
    for (const auto &d : resp.device_report_progress.all_device) {
        DiagnosisDeviceProgressItemResult item;
        item.sn = d.sn;
        item.status = d.status;
        item.percent = d.percent;
        results.device_report_progress.all_device.emplace_back(std::move(item));
    }

    return results;
}


// 设备定损: 查询单条定损报告详情;
void DiagnosisManagementModuleImpl::getSelfDiagnosisReport(
    const std::string &report_uuid,
    const std::function<void(const GetSelfDiagnosisReportResults& results, FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("getSelfDiagnosisReport(): callback is NULL !");
        return;
    }

    if (report_uuid.empty()) {
        LOG_ERROR("getSelfDiagnosisReport(): report_uuid param is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 查询单条定损报告详情(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_GET_SELF_DIAGNOSIS_REPORT, param_err, "[DiagnosisManagementModuleImpl]");
        callback({}, param_err);
        return;
    }

    WeakDummy(weak_ptr);

    get_self_diagnosis_report(report_uuid, [weak_ptr, callback](const GetSelfDiagnosisReportResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询单条定损报告详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_GET_SELF_DIAGNOSIS_REPORT, err, "[DiagnosisManagementModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("getSelfDiagnosisReport(): err: %d", (int)err.error_code);
                callback({}, err);
                return;
            }

            callback(convertReportResponse(response), err);
        });
    });
}

// 设备定损: 查询定损报告列表;
void DiagnosisManagementModuleImpl::getSelfDiagnosisReportList(
    const GetSelfDiagnosisReportListParams &params,
    const std::function<void(const GetSelfDiagnosisReportListResults& results, FlightHubError err)>& callback) {
    if (!callback) {
        LOG_ERROR("getSelfDiagnosisReportList(): callback is NULL !");
        return;
    }

    WeakDummy(weak_ptr);

    get_self_diagnosis_report_lists(params, [weak_ptr, callback](const GetSelfDiagnosisReportListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询定损报告列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_DIAGNOSE_GET_SELF_DIAGNOSIS_REPORT_LIST, err, "[DiagnosisManagementModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("getSelfDiagnosisReportList(): err: %d", (int)err.error_code);
                callback({}, err);
                return;
            }

            GetSelfDiagnosisReportListResults results;
            results.pagination.page = response.pagination.page;
            results.pagination.page_size = response.pagination.page_size;
            results.pagination.total = response.pagination.total;

            results.list.reserve(response.list.size());
            for (const auto &item : response.list) {
                results.list.emplace_back(convertReportResponse(item));
            }

            callback(results, err);
        });
    });
}


}  // namespace flight_hub_sdk
