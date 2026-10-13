// Copyright (c) 2026 SZ DJI Technology Co., Ltd.
// SPDX-License-Identifier: Apache-2.0

#include "maintenance_management_module_impl.h"
#include "common/executors/executors.h"
#include "common/util/async_capture_protect.h"
#include "http_interface/http_interface.h"
#include <base/common/logger.h>



namespace flight_hub_sdk {


MaintenanceManagementModuleImpl::MaintenanceManagementModuleImpl()
    {
    LOG_INFO("[MaintenanceManagementModuleImpl] constructor");
}

MaintenanceManagementModuleImpl::~MaintenanceManagementModuleImpl() {}


// (设备)报修管理模块;
// 获取设备报修配置;
void MaintenanceManagementModuleImpl::getRepairConfigs(
    const GetDeviceRepairConfigParams &params,  // 请求参数;
    const std::function<void(FlightHubError err, const DeviceRepairConfigResults& results)>& callback) {
    // callback 检查;
    if (!callback) {
        LOG_ERROR("getRepairConfigs(): callback is NULL !");
        return;
    }

    // [必须参数]检查;
    if (params.device_sns.empty()) {
        LOG_ERROR("getRepairConfigs(): device_sns param is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 获取设备报修配置(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_GET_REPAIR_CONFIGS, param_err, "[MaintenanceManagementModuleImpl]");
        callback(param_err, {});
        return;
    }

    // protection use dummy weak_ptr;
    WeakDummy(weak_ptr);

    // (设备)报修管理模块: 获取设备报修配置请求参数;
    // 获取设备报修配置;
    ObtainRepairConfigRequest request;
    request.device_sn = params.device_sns;

    // Send request;
    obtain_maintenance_orders_repair_config(request, [weak_ptr, callback](const ObtainRepairConfigResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 获取设备报修配置; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_GET_REPAIR_CONFIGS, err, "[MaintenanceManagementModuleImpl]");
            // 检查 err;
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("obtain_maintenance_orders_repair_config(): err: %d", (int)err.error_code);
                callback(err, {});

                return;
            }

            // When succeed, transfer the response to the callback;
            DeviceRepairConfigResults results;
            results.data.reserve(response.list.size());

            for (const auto &config : response.list) {
                // temp config object;
                DeviceRepairConfig item;
                item.device_sn = config.device_sn;
                item.support_repair = config.support_repair;
                item.allowed_source_types = config.allowed_source_types;

                // repair_guidance;
                item.repair_guidance.redirect_url = config.repair_guidance.redirect_url;
                item.repair_guidance.description = config.repair_guidance.description;

                // 自定义数据类型，为了真正优化性能, 可定义 move 构造函数/move 赋值函数, 避免拷贝;
                results.data.emplace_back(std::move(item));
            }

            // When succeed, transfer the response to the callback;
            callback(err, results);
        });
    });
}


// 创建报修订单;
void MaintenanceManagementModuleImpl::createMaintenanceOrder(
    const CreateMaintenanceOrderParams &params,
    const std::function<void(FlightHubError err, const CreateMaintenanceOrderResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("createMaintenanceOrder(): callback is NULL !");
        return;
    }

    if (params.device_list.empty()) {
        LOG_ERROR("createMaintenanceOrder(): device_list param is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 创建报修订单(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_CREATE_ORDER, param_err, "[MaintenanceManagementModuleImpl]");
        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    // 请求参数填充;
    CreateMaintenanceOrdersRequest request;
    request.fault_occurrence_time = params.fault_occurrence_time;
    request.fault_description = params.fault_description;
    request.contact_person = params.contact_person;
    request.contact_phone = params.contact_phone;
    request.contact_email = params.contact_email;
    request.expected_service_time = params.expected_service_time;
    request.report_uuid = params.report_uuid;

    // 联系地址;
    request.contact_address.country = params.contact_address.country;
    request.contact_address.province = params.contact_address.province;
    request.contact_address.city = params.contact_address.city;
    request.contact_address.district = params.contact_address.district;
    request.contact_address.detail = params.contact_address.detail;

    // 附件列表;
    request.attachments.reserve(params.attachments.size());
    for (const auto &att : params.attachments) {
        MaintenanceOrderAttachments item;
        item.file_name = att.file_name;
        item.file_key = att.file_key;
        item.file_size = att.file_size;
        request.attachments.emplace_back(std::move(item));
    }

    // 日志列表;
    request.log_list.reserve(params.log_list.size());
    for (const auto &log : params.log_list) {
        MaintenanceLogInfo item;
        item.device_sn = log.device_sn;
        item.module = log.module;
        item.object_key = log.object_key;
        request.log_list.emplace_back(std::move(item));
    }

    // 设备列表;
    request.device_list.reserve(params.device_list.size());
    for (const auto &dev : params.device_list) {
        MaintenanceOrderDeviceItem item;
        item.device_model_key = dev.device_model_key;
        item.device_sn = dev.device_sn;
        item.module = dev.module;
        item.nameplate_sn = dev.nameplate_sn;
        item.source_type = dev.source_type;
        item.source_ref_id = dev.source_ref_id;
        request.device_list.emplace_back(std::move(item));
    }

    // 设备拓扑;
    request.device_topo.hosts.reserve(params.device_topo.hosts.size());
    for (const auto &node : params.device_topo.hosts) {
        MaintenanceTopoNode topo_node;
        topo_node.sn = node.sn;
        topo_node.device_model_key = node.device_model_key;
        topo_node.online_status = node.online_status;
        request.device_topo.hosts.emplace_back(std::move(topo_node));
    }
    request.device_topo.parents.reserve(params.device_topo.parents.size());
    for (const auto &node : params.device_topo.parents) {
        MaintenanceTopoNode n;
        n.sn = node.sn;
        n.device_model_key = node.device_model_key;
        n.online_status = node.online_status;
        request.device_topo.parents.emplace_back(std::move(n));
    }
    request.device_topo.nodes.reserve(params.device_topo.nodes.size());
    for (const auto &node : params.device_topo.nodes) {
        MaintenanceTopoNode n;
        n.sn = node.sn;
        n.device_model_key = node.device_model_key;
        n.online_status = node.online_status;
        request.device_topo.nodes.emplace_back(std::move(n));
    }

    // http 请求;
    create_maintenance_orders(std::move(request), [weak_ptr, callback](const CreateMaintenanceOrdersResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 创建报修订单; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_CREATE_ORDER, err, "[MaintenanceManagementModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("create_maintenance_orders(): err: %d", (int)err.error_code);
                callback(err, {});
                return;
            }

            CreateMaintenanceOrderResults results;
            results.success = response.success;
            results.generated_orders.reserve(response.generated_orders.size());

            for (const auto &order : response.generated_orders) {
                GeneratedOrderInfoResult item;
                item.order_number = order.order_number;
                item.status = order.status;
                item.ruiyun_order_number = order.ruiyun_order_number;

                results.generated_orders.emplace_back(std::move(item));
            }

            callback(err, results);
        });
    });
}


// 查询单个报修订单详情;
void MaintenanceManagementModuleImpl::getMaintenanceOrderDetail(
    const std::string &order_number,
    const std::function<void(FlightHubError err, const MaintenanceOrderDetailResults& result)>& callback) {
    if (!callback) {
        LOG_ERROR("getMaintenanceOrderDetail(): callback is NULL !");
        return;
    }

    // 必须参数的检查;
    if (order_number.empty()) {
        LOG_ERROR("getMaintenanceOrderDetail(): order_number param is empty !");
        auto param_err = FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_INVALID_PARAM);
        // 埋点: 查询单个报修订单详情(入参非法, 未发起请求); 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
        // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
        // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
        // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
        report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_GET_ORDER_DETAIL, param_err, "[MaintenanceManagementModuleImpl]");
        callback(param_err, {});
        return;
    }

    WeakDummy(weak_ptr);

    get_maintenance_order_detail(order_number, [weak_ptr, callback](const MaintenanceOrderDetailResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 查询单个报修订单详情; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_GET_ORDER_DETAIL, err, "[MaintenanceManagementModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("get_maintenance_order_detail(): err: %d", (int)err.error_code);
                callback(err, {});
                return;
            }

            MaintenanceOrderDetailResults result;
            result.order_number = response.order_number;
            result.organization_uuid = response.organization_uuid;
            result.creator_id = response.creator_id;
            result.creator_name = response.creator_name;
            result.source_type = response.source_type;
            result.source_type_text = response.source_type_text;
            result.status = response.status;
            result.status_text = response.status_text;
            result.fault_description = response.fault_description;
            result.fault_occurrence_time = response.fault_occurrence_time;
            result.expected_service_time = response.expected_service_time;
            result.contact_person = response.contact_person;
            result.contact_phone = response.contact_phone;
            result.contact_email = response.contact_email;

            result.contact_address_full.country = response.contact_address_full.country;
            result.contact_address_full.province = response.contact_address_full.province;
            result.contact_address_full.city = response.contact_address_full.city;
            result.contact_address_full.district = response.contact_address_full.district;
            result.contact_address_full.detail = response.contact_address_full.detail;

            result.created_at = response.created_at;
            result.updated_at = response.updated_at;
            result.submission_time = response.submission_time;
            result.accepted_time = response.accepted_time;
            result.completed_time = response.completed_time;
            result.ruiyun_order_number = response.ruiyun_order_number;
            result.ruiyun_sync_status = response.ruiyun_sync_status;
            result.device_sn = response.device_sn;
            result.device_model_key = response.device_model_key;
            result.module = response.module;
            result.nameplate_sn = response.nameplate_sn;
            result.diagnostic_id = response.diagnostic_id;
            result.device_report_id = response.device_report_id;

            // 附件列表;
            result.attachments.reserve(response.attachments.size());
            for (const auto &att : response.attachments) {
                MaintenanceOrderDetailAttachments item;
                item.id = att.id;
                item.file_name = att.file_name;
                item.object_key = att.object_key;
                item.file_size = att.file_size;
                item.upload_time = att.upload_time;
                item.created_at = att.created_at;
                result.attachments.emplace_back(std::move(item));
            }

            // 状态变更历史;
            result.status_history.reserve(response.status_history.size());
            for (const auto &h : response.status_history) {
                MaintenanceOrderStatusHistory item;
                item.from_status = h.from_status;
                item.to_status = h.to_status;
                item.change_time = h.change_time;
                item.operator_id = h.operator_id;
                item.operator_name = h.operator_name;
                item.remark = h.remark;
                result.status_history.emplace_back(std::move(item));
            }

            // 维修信息;
            result.repair_info.assignee = response.repair_info.assignee;
            result.repair_info.assignee_phone = response.repair_info.assignee_phone;
            result.repair_info.repair_start_time = response.repair_info.repair_start_time;

            // 日志报告;
            result.log_report.report_uuid = response.log_report.report_uuid;
            result.log_report.track_number = response.log_report.track_number;
            result.log_report.status = response.log_report.status;

            callback(err, result);
        });
    });
}


// 分页查询报修订单列表;
void MaintenanceManagementModuleImpl::getMaintenanceOrdersList(
    const GetMaintenanceOrdersListParams &params,
    const std::function<void(FlightHubError err, const MaintenanceOrdersListResults& results)>& callback) {
    if (!callback) {
        LOG_ERROR("getMaintenanceOrdersList(): callback is NULL !");
        return;
    }

    WeakDummy(weak_ptr);

    GetMaintenanceOrdersListRequest request;
    request.page = params.page;
    request.page_size = params.page_size;
    request.order_number = params.order_number;
    request.status = params.status;
    request.initiated_from = params.initiated_from;
    request.device_sn = params.device_sn;
    request.creator_ids = params.creator_ids;
    request.key_word = params.key_word;
    request.created_at_start = params.created_at_start;
    request.created_at_end = params.created_at_end;
    request.sort_column = params.sort_column;
    request.sort_type = params.sort_type;

    get_maintenance_orders_list(request, [weak_ptr, callback](const MaintenanceOrdersListResponse& response, FlightHubError err) {
        WeakDummyReturn(weak_ptr);
        drc_sdk::common::Executors::Main()->Post([err, callback, response]() {
            // 埋点: 分页查询报修订单列表; 上报失败不影响主流程, 故不关心结果, 仅打印 log 记录。
            // 数据埋点上报 event + err(FlightHubError) 数据，不改变(未改变)原来的业务代码逻辑。
            // 比如 使用"make_http_error(http_code, response)"或者"FlightHubError::success()"
            // 或者"FlightHubError::from_sdk(FlightHubSdkErrorCode::ERROR_OPERATION_FAILED)" 生成 FlightHubError 信息。
            report_beacon_event_helper(kBeaconReportEvent_MAINTENANCE_GET_ORDERS_LIST, err, "[MaintenanceManagementModuleImpl]");
            if (err.error_code != FlightHubSdkErrorCode::SUCCESS) {
                LOG_ERROR("get_maintenance_orders_list(): err: %d", (int)err.error_code);
                callback(err, {});
                return;
            }

            MaintenanceOrdersListResults results;
            results.pagination.page = response.pagination.page;
            results.pagination.page_size = response.pagination.page_size;
            results.pagination.total = response.pagination.total;

            results.list.reserve(response.list.size());
            for (const auto &item_info : response.list) {
                MaintenanceOrdersListItem item;
                item.order_number = item_info.order_number;
                item.ruiyun_order_number = item_info.ruiyun_order_number;
                item.status = item_info.status;
                item.status_text = item_info.status_text;
                item.source_type = item_info.source_type;
                item.source_type_text = item_info.source_type_text;
                item.creator_id = item_info.creator_id;
                item.creator_name = item_info.creator_name;
                item.contact_person = item_info.contact_person;
                item.contact_phone = item_info.contact_phone;
                item.contact_email = item_info.contact_email;

                item.contact_address_full.country = item_info.contact_address_full.country;
                item.contact_address_full.province = item_info.contact_address_full.province;
                item.contact_address_full.city = item_info.contact_address_full.city;
                item.contact_address_full.district = item_info.contact_address_full.district;
                item.contact_address_full.detail = item_info.contact_address_full.detail;

                item.fault_description = item_info.fault_description;
                item.fault_occurrence_time = item_info.fault_occurrence_time;
                item.expected_service_time = item_info.expected_service_time;
                item.created_at = item_info.created_at;
                item.updated_at = item_info.updated_at;
                item.submission_time = item_info.submission_time;
                item.accepted_time = item_info.accepted_time;
                item.completed_time = item_info.completed_time;
                item.device_sn = item_info.device_sn;
                item.device_model_key = item_info.device_model_key;
                item.device_model_name = item_info.device_model_name;
                item.nameplate_sn = item_info.nameplate_sn;
                item.module = item_info.module;
                item.device_organization_callsign = item_info.device_organization_callsign;

                item.attachments.reserve(item_info.attachments.size());
                for (const auto &att : item_info.attachments) {
                    MaintenanceOrderDetailAttachments att_item;
                    att_item.id = att.id;
                    att_item.file_name = att.file_name;
                    att_item.object_key = att.object_key;
                    att_item.file_size = att.file_size;
                    att_item.upload_time = att.upload_time;
                    att_item.created_at = att.created_at;
                    item.attachments.emplace_back(std::move(att_item));
                }

                item.status_history.reserve(item_info.status_history.size());
                for (const auto &h : item_info.status_history) {
                    MaintenanceOrderStatusHistory h_item;
                    h_item.from_status = h.from_status;
                    h_item.to_status = h.to_status;
                    h_item.change_time = h.change_time;
                    h_item.operator_id = h.operator_id;
                    h_item.operator_name = h.operator_name;
                    h_item.remark = h.remark;
                    item.status_history.emplace_back(std::move(h_item));
                }

                item.repair_info.assignee = item_info.repair_info.assignee;
                item.repair_info.assignee_phone = item_info.repair_info.assignee_phone;
                item.repair_info.repair_start_time = item_info.repair_info.repair_start_time;

                item.log_report.report_uuid = item_info.log_report.report_uuid;
                item.log_report.track_number = item_info.log_report.track_number;
                item.log_report.status = item_info.log_report.status;

                results.list.emplace_back(std::move(item));
            }

            callback(err, results);
        });
    });
}


}  // namespace flight_hub_sdk
